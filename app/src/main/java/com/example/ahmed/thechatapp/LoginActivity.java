package com.example.ahmed.thechatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


/**
 * Created by Ahmed on 1/13/2018.
 */

public class LoginActivity extends AppCompatActivity {


    //defining views
    private Button buttonSignIn;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignup;

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    //progress dialog
    private ProgressDialog progressDialog;

    //MainActivity Instance
    private MainActivity mainActivity;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //getting firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //if the objects getcurrentuser method is not null
        //means user is already logged in
        if (firebaseAuth.getCurrentUser() != null) {
            //close this activity
            finish();
            //opening profile(main) activity
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }

         //initializing views
         editTextEmail = (EditText) findViewById(R.id.editTextEmail);
         editTextPassword = (EditText) findViewById(R.id.editTextPassword);
         buttonSignIn = (Button) findViewById(R.id.buttonSignin);
         textViewSignup  = (TextView) findViewById(R.id.textViewSignUp);


        progressDialog = new ProgressDialog(this);

        //attaching click listener
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = editTextEmail.getText().toString().trim();
                String password  = editTextPassword.getText().toString().trim();


                //checking if email and passwords are empty
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(LoginActivity.this,"Please enter email", Toast.LENGTH_LONG).show();
                    return;
                }

                if(TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this,"Please enter password", Toast.LENGTH_LONG).show();
                    return;
                }

                //if the email and password are not empty
                //displaying a progress dialog
                progressDialog.setMessage("Logging in Please Wait...");
                progressDialog.show();

                //logging in the user
                Task<AuthResult> authResultTask = firebaseAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                //if the task is successfull
                                if (task.isSuccessful()) {
                                    //Logical bug (onResume user name disappears)
                                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                    i.putExtra("keyUser", ""+email);// key is used to get value in Second Activiyt

                                    //start the profile activity
                                    finish();
                                    startActivity(i);
                                }
                                else {
                                    //display some message here
                                    Toast.makeText(LoginActivity.this,"Login Error", Toast.LENGTH_LONG).show();
                                }
                                progressDialog.dismiss();
                            }
                        });
            }
        });

        //Setting listener
        textViewSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Starting SignUp
                startActivity(new Intent(LoginActivity.this,SignUp.class));
                }
            });

        }
    }