package com.csm117.digitalbazaar;

import android.os.AsyncTask;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stripe.Stripe;
import com.stripe.model.Charge;
import com.stripe.model.Customer;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class PaymentBackend {
    private class ChargeParams {
        String customerId;
        int amount;     // in cents
    }
    private class ChargeTask extends AsyncTask<ChargeParams, Void, Integer> {
        protected Integer doInBackground(ChargeParams... params) {
            String customerId = params[0].customerId;
            int amount = params[0].amount;

            try {
                // Charge the Customer instead of the card:
                Map<String, Object> chargeParams = new HashMap<String, Object>();
                chargeParams.put("amount", amount);
                chargeParams.put("currency", "usd");
                chargeParams.put("customer", customerId);
                Charge charge = Charge.create(chargeParams);
            } catch (Exception e) {
                System.out.println("Debug info:");
                System.out.println(e);
                System.out.println(customerId);
                System.out.println(amount);
                return -1;
            }

            return amount;
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result>0) {
                long amount = result;
                NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
                String s = n.format(amount / 100.0);
                CharSequence msg = "Payment success! Paying " + s;
                notifyFrontEnd(msg, true);
            } else {
                notifyFrontEnd("Payment declined!", false);
            }
        }
    }

    private class NewCustomerTask extends AsyncTask<String, Void, Boolean> {
        protected Boolean doInBackground(String... tokenId) {
            String tok = tokenId[0];

            // Create a Customer:
            Map<String, Object> customerParams = new HashMap<String, Object>();
            customerParams.put("email", "cxv@g.ucla.edu");
            customerParams.put("source", tok);
            String customerId;
            try {
                Customer customer = Customer.create(customerParams);
                customerId = customer.getId();

            } catch (Exception e) {
                return false;
            }

            // Save him to database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            String user = Globals.getInstance().userId;

            // Add the info
            DatabaseReference userRef = database.getReference()
                    .child("payments").child(user);
            Map<String, String> paymentData = new HashMap<String, String>();
            paymentData.put("userId", user);
            paymentData.put("paymentId", customerId);
            userRef.setValue(paymentData);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                notifyFrontEnd("Save success!", true);
            } else {
                notifyFrontEnd("Save failed!", false);
            }
        }
    }

    PaymentFrontEnd frontEnd;

    PaymentBackend(PaymentFrontEnd fe) {
        frontEnd = fe;

        // Set your secret key: remember to change this to your live secret key in production
        // See your keys here: https://dashboard.stripe.com/account/apikeys
        Stripe.apiKey = "sk_test_6qfgvcOlBqCvFaakv1URDLgF";
    }

    private void notifyFrontEnd(CharSequence text, boolean success) {
        frontEnd.notify(text, success);
    }

    public void newCustomer(String tokenId) {
        new NewCustomerTask().execute(tokenId);
    }

    public void charge(int amount, String customerId) {
        // Check params
        if (amount<100) {
            notifyFrontEnd("Transaction amount too small, minimum 100 cents", true);
            return;
        }

        ChargeParams params = new ChargeParams();
        params.amount = amount;
        params.customerId = customerId;
        new ChargeTask().execute(params);
    }

}
