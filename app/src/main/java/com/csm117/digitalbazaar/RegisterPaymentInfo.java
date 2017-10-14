package com.csm117.digitalbazaar;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;

public class RegisterPaymentInfo extends AppCompatActivity implements PaymentFrontEnd {
    PaymentBackend paymentBackend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_payment_info);

        paymentBackend = new PaymentBackend(this);
    }

    public void notify(CharSequence text, boolean success) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        if (success) {
            finish();
            return;
        }
    }


    public void savePaymentInfo(View view) {
        // Get form info
        EditText et = (EditText) findViewById(R.id.creditcardNumberEdit);
        String cardNumber = et.getText().toString();
        et = (EditText) findViewById(R.id.expirationMonthEdit);
        int cardExpMonth = Integer.parseInt(et.getText().toString());
        et = (EditText) findViewById(R.id.expirationYearEdit);
        int cardExpYear = Integer.parseInt(et.getText().toString());
        et = (EditText) findViewById(R.id.ccvEdit);
        String cardCCV = et.getText().toString();

        Card card = new Card(cardNumber, cardExpMonth, cardExpYear, cardCCV);
        if (!card.validateCard()) {
            // Show errors
            notify("Invalid Card!", false);
            return;
        }

        Stripe stripe = new Stripe();
        try {
            stripe.setDefaultPublishableKey("pk_test_PRUasoC2c2VrLqBR4WV1tFwS");
        } catch (Exception e) {
            notify("Error connecting to network!", false);
        }
        stripe.createToken(
                card,
                new TokenCallback() {
                    public void onSuccess(Token token) {
                        // Send token to your server
                        paymentBackend.newCustomer(token.getId());
                    }

                    public void onError(Exception error) {
                        // Show localized error message
                        RegisterPaymentInfo.this.notify("Error processing credit card!", false);
                    }
                }
        );
    }
}
