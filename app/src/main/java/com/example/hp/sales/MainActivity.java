package com.example.hp.sales;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public EditText  mail, pass;
    public SignInButton signInButton;
    public LoginButton loginButton;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Button regi,log_up,sign_up;
    private FirebaseUser user;
    private CallbackManager callbackManager;
    private static final int RC_SIGN_IN = 1;
    private GoogleApiClient mGoogleSignInClient;
    private static final String TAG = "MainActivity";

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        if (user == null) {

            FacebookSdk.sdkInitialize(getApplication());
            loginButton = (LoginButton)findViewById(R.id.login_button);
            callbackManager = CallbackManager.Factory.create();
            loginButton.setReadPermissions("email");

        }else
        {
            Intent i = new Intent(MainActivity.this,Logout.class);
            startActivity(i);
        }
        signInButton = (SignInButton)findViewById(R.id.btn_sign_in);
        regi = (Button) findViewById(R.id.button3);

        log_up = (Button) findViewById(R.id.but);
        mail = (EditText) findViewById(R.id.editText);
        pass = (EditText) findViewById(R.id.editText2);
        sign_up = (Button) findViewById(R.id.sign);
        loginButton = (LoginButton) findViewById(R.id.login_button);

        log_up.setOnClickListener(this);
        sign_up.setOnClickListener(this);
        regi.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
                    {
                        Toast.makeText(MainActivity.this, "Connection to Google Sign in failed...", Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                signIn();
            }
        });

    }
    // Register Page
    public void signup() {
        Intent intent = new Intent(this,Third.class);
        startActivity(intent);
    }
    //Regi with mobile
    private void RegisterwithMobile() {
        Intent intent = new Intent(MainActivity.this,Otpmobile.class);
        startActivity(intent);

    }

    //initialize the facebook login button
    public void buttonClick(View v)
    {
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this,"Cancelled",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(MainActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            updateUI(user);

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                    }
                });
    }
   // google sign in activity
    private void signIn()
    {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {    callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN)
        {

            progressDialog.setMessage("Please wait, while we are allowing you to login using your Google Account...");
            progressDialog.setCanceledOnTouchOutside(true);
            progressDialog.show();

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess())
            {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Toast.makeText(this, "Please wait, while we are getting your auth result...", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this, "Can't get Auth result.", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }
   //auth with google
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "signInWithCredential:success");
                            SendUserToMainActivity();
                            progressDialog.dismiss();
                        }
                        else
                        {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = task.getException().toString();
                            SendUserToLoginActivity();
                            Toast.makeText(MainActivity.this, "Not Authenticated : " + message, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }


    private void updateUI(FirebaseUser user) {
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
    }

    //google signin successfull
    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(MainActivity.this, Logout.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    //google signin unsuccessfull
    private void SendUserToLoginActivity()
    {
        Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    //Login
    private void userlogin() {
        String emmail = mail.getText().toString().trim();
        String passi = pass.getText().toString().trim();
        if (emmail.isEmpty())
        {
            mail.setError("Email Required");
            mail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(emmail).matches())
        {
            mail.setError("Enter a valid Email ");
            mail.requestFocus();
            return;
        }
        if (passi.isEmpty())
        {
            pass.setError("Password Required");
            pass.requestFocus();
            return;
        }
        if (passi.length()<6)
        {
            pass.setError("Password shoul be atleast 6 characters long");
            pass.requestFocus();
            return;
        }
        progressDialog.setMessage("Login....");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(emmail,passi).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if (task.isSuccessful())
                {
                    finish();
                    startActivity(new Intent(MainActivity.this,Third.class));
                }else
                {
                    Toast.makeText(MainActivity.this,"Login Unsuccessful",Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.but:
                userlogin();
                 break;
            case R.id.sign:
                signup();
                break;
            case R.id.button3:
                RegisterwithMobile();
                break;
        }
    }

}

