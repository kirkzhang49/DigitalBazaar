package com.csm117.digitalbazaar;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * PaymentInformation, to stored in database
 */

@IgnoreExtraProperties
public class PaymentInformation {
    public String id;

    public PaymentInformation() {
        // Default constructor required for calls to DataSnapshot.getValue(PaymentInformation.class)
    }

    public PaymentInformation(String id) {
        this.id = id;
    }

}
