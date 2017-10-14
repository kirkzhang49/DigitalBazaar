package com.csm117.digitalbazaar;

import android.*;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;

public class DashBoard extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        Button Verification = (Button) findViewById(R.id.Profile);
        Verification.setEnabled(false);
        findViewById(R.id.Logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logout = new Intent();
                setResult(RESULT_OK, logout);
                finish();
            }
        });
    }
    //--------------------------Buttons-----------------------------------
    /** Called when the user clicks the GoToPayment button */
    public void clickButtonGoToMain(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, MainFunc.class);
//        ArrayList<String> Users = getIntent().getExtras().getStringArrayList("userIDs");
//        intent.putStringArrayListExtra("userIDs", Users);
        String curUser = getIntent().getExtras().getString("userID");
        intent.putExtra("userID", curUser);
        startActivity(intent);
    }
    public void clickButtonGoToVerification(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, Verfication.class);
        startActivity(intent);
    }
    /** Called when the user clicks the GoToPayment button */
    public void clickButtonGoToLogin(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /** Called when the user clicks the GoToPayment button */
    public void clickButtonGoToRegisterPaymentInfo(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, RegisterPaymentInfo.class);
        startActivity(intent);
    }

}
