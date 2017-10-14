package com.csm117.digitalbazaar;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;


//--------fb login with firebase reference----
//https://firebase.google.com/docs/auth/android/facebook-login
//https://github.com/firebase/quickstart-android/blob/master/auth/app/src/minSdkJellybean/java/com/google/firebase/quickstart/auth/FacebookLoginActivity.java#L150-L156
//-------------------------------------------

public class MainActivity extends AppCompatActivity {

    //for debugging purposes
    private static final String TAG = "FacebookLogin"; //final means constant - cannot change the value

    private TextView mStatusTextView;
    private TextView mDetailTextView;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    // [END declare_auth]
    // [START declare_auth_listener]
    private FirebaseAuth.AuthStateListener mAuthListener;
    // [END declare_auth_listener]
    private CallbackManager mCallbackManager;
    //to keep track of the child activity activity_dash_board
    private static final int REQUEST_CODE_LOGIN = 0;
    private static String curUser;

    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

            // Views
            mStatusTextView = (TextView) findViewById(R.id.status);
            mDetailTextView = (TextView) findViewById(R.id.detail);
            // [START initialize_auth]
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            // [END initialize_auth]

            // [START auth_state_listener]
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        // User is signed in
                        findViewById(R.id.login_button).setVisibility(View.GONE);
                        curUser = user.getUid();
                        Globals.getInstance().userId = user.getUid();

                        //put a value into location fields in Firebase so app doesn't crash when user opens Maps for the first time
                        String currentUserPath = "accounts/" + curUser;
                        FirebaseDatabase.getInstance()
                                .getReference(currentUserPath)
                                .setValue(new Date().getTime());
                        String currentUserPathLat = "accounts/" + curUser + "/location/latitude";
                        FirebaseDatabase.getInstance().getReference(currentUserPathLat).setValue(0);
                        String currentUserPathLong = "accounts/" + curUser + "/location/longitude";
                        FirebaseDatabase.getInstance().getReference(currentUserPathLong).setValue(0);
                        ///////////////////////////////////


                        Log.d(TAG, "onAuthStateChanged:signed_in:" + curUser);
                        //upon successful login direct to payment page
                        goToDashBoard();
                    }
//                      else {
//                        // User is signed out
//                        Log.d(TAG, "onAuthStateChanged:signed_out");
//                    }
//                    // [START_EXCLUDE]
//                    updateUI(user);
//                    // [END_EXCLUDE]
                }
            };
            // [END auth_state_listener]

            // [START initialize_fblogin]
            // Initialize Facebook Login button
            mCallbackManager = CallbackManager.Factory.create();
            LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
            loginButton.setReadPermissions("email", "public_profile");
            loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "facebook:onSuccess:" + loginResult);
                    //on successful login, get facebook access token and exchange it with Firebase credential
                    //function defined below
                    handleFacebookAccessToken(loginResult.getAccessToken());
                }

                @Override
                public void onCancel() {
                    Log.d(TAG, "facebook:onCancel");
                    // [START_EXCLUDE]
                    updateUI(null);
                    // [END_EXCLUDE]
                }

                @Override
                public void onError(FacebookException error) {
                    Log.d(TAG, "facebook:onError", error);
                    // [START_EXCLUDE]
                    updateUI(null);
                    // [END_EXCLUDE]
                }
            });
            // [END initialize_fblogin]
        }

        // [START on_start_add_listener]
        @Override
        public void onStart() {
            super.onStart();
            mAuth.addAuthStateListener(mAuthListener);
        }
        // [END on_start_add_listener]

        // [START on_stop_remove_listener]
        @Override
        public void onStop() {
            super.onStop();
            if (mAuthListener != null) {
                mAuth.removeAuthStateListener(mAuthListener);
            }
        }
        // [END on_stop_remove_listener]


        // [START on_activity_result]
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if(requestCode == REQUEST_CODE_LOGIN && resultCode == Activity.RESULT_OK)
            {
//                finishActivity(REQUEST_CODE_LOGIN);
                signOut();
//                return;
            }
            super.onActivityResult(requestCode, resultCode, data);

            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
        // [END on_activity_result]

        // [START auth_with_facebook]
        private void handleFacebookAccessToken(AccessToken token) {
            Log.d(TAG, "handleFacebookAccessToken:" + token);
            // [START_EXCLUDE silent]
//        showProgressDialog();
            // [END_EXCLUDE]

            //getToken() gets access token for signed in user
            //exchange it for Firebase credential in AuthCredential credential
            //authenticate with firebase using the Firebase credential
            AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithCredential", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        // [END auth_with_facebook]

        public void signOut() {
            mAuth.signOut();
            LoginManager.getInstance().logOut();
            updateUI(null);
        }

        private void updateUI(FirebaseUser user) {
//        hideProgressDialog();
            if (user != null) {
                findViewById(R.id.login_button).setVisibility(View.GONE);
            } else {
                mStatusTextView.setText(R.string.signed_out);
                mDetailTextView.setText(null);
                findViewById(R.id.login_button).setVisibility(View.VISIBLE);
//            findViewById(R.id.button_facebook_signout).setVisibility(View.GONE);
        }
    }



    private void goToDashBoard() {
        Intent intent = new Intent(this, DashBoard.class);
        intent.putExtra("userID", curUser);
        startActivityForResult(intent, REQUEST_CODE_LOGIN);
    }
}
