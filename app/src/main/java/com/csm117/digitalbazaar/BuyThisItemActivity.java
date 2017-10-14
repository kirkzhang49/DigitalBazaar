package com.csm117.digitalbazaar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class BuyThisItemActivity extends AppCompatActivity implements PaymentFrontEnd {
    DatabaseReference paymentsReference;
    BuyThisItemActivity me;
    PaymentBackend paymentBackend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_this_item);
        me = this;
        paymentBackend = new PaymentBackend(this);

        final String userId = getIntent().getExtras().getString("userID");
        final String otherUserId = getIntent().getExtras().getString("otheruserID");
        String path = "payments/";
        paymentsReference = FirebaseDatabase.getInstance().getReference(path);

        final int amount = Integer.parseInt(getIntent().getExtras().getString("amount"));


        paymentsReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Find out if this user has payment info
                if (userId.equals(otherUserId)) {
                    me.notify("Hey, you cannot buy your own stuff", true);
                }
                else if (dataSnapshot.hasChildren() && dataSnapshot.hasChild(userId) && dataSnapshot.hasChild(otherUserId)) {
                    me.notifyWithoutFinish("Seller have a credit card on file, but we'll take everything anyway.");
                    String paymentId = dataSnapshot.child(userId).child("paymentId").getValue(String.class);
                    paymentBackend.charge(amount, paymentId);
                } else if (dataSnapshot.hasChildren() && dataSnapshot.hasChild(userId)) {
                    me.notifyWithoutFinish("Seller doesn't have a credit card on file, we'll take everything.");
                    String paymentId = dataSnapshot.child(userId).child("paymentId").getValue(String.class);
                    paymentBackend.charge(amount, paymentId);
                }
                else
                    {
                    // No payment info, redirect to register payment info
                    Intent intent = new Intent(me, RegisterPaymentInfo.class);
                    startActivity(intent);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void notifyWithoutFinish(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void notify(CharSequence text, boolean success) {
        notifyWithoutFinish(text);
        finish();
        return;
    }

}
