/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.ahmed.thechatapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

//Firebase Authentication


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN = 1;
    public static final int RC_PHOTO_PICKER = 2;

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;

    public String mUid = ANONYMOUS;

    //Firebase storage references
    private FirebaseDatabase mFirebasedatabase;
    private DatabaseReference mMessageDatabaseReference;
    private ChildEventListener mChildEventListener;

    //Assistant
    private StorageReference mChatPhotosStorageReference;
    private FirebaseStorage mFirebaseStorage;

    //Firebase UI SignUp/In
    // private FirebaseAuth mFirebaseAuth;
    // private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Sending Name from Login Activity..
        //Intent intent = getIntent();
        //mUid = intent.getStringExtra("keyUser");

        //mUsername = ANONYMOUS;

        mFirebaseStorage = FirebaseStorage.getInstance();

        //Initialize Firebase Components
        mFirebasedatabase = FirebaseDatabase.getInstance();

        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();

        //UserID
        //mUid = firebaseAuth.getCurrentUser().getUid();

        //UserID
        mUid = firebaseAuth.getCurrentUser().getEmail();


        mMessageDatabaseReference = mFirebasedatabase.getReference().child("messages");
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");


        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_else_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                // intent.setType("image/jpeg");
                intent.setType("image/*");
                intent.putExtra(intent.EXTRA_LOCAL_ONLY, true);
                // intent.setAction(Intent.ACTION_GET_CONTENT);
               // startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
                startActivityForResult(intent, RC_PHOTO_PICKER);

            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click
                EditText nameText = (EditText) findViewById(R.id.messageEditText);
                String message = nameText.getText().toString();

                FriendlyMessage newMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUid, null);

                mMessageDatabaseReference.push().setValue(newMessage);
                // mMessageDatabaseReference.setValue(newMessage);

                // Clear input box
                mMessageEditText.setText("");
            }
        });
        attachDatabaseReadListener();

    }

    //For Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                //logging out the user
                firebaseAuth.signOut();
                //closing activity
                finish();
                //starting login activity
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;

            case R.id.exit:
                //logging out the user
                firebaseAuth.signOut();
                //closing activity
                finish();
                //starting login activity
                System.exit(0);
                break;
        }

        return true;
    }

    //My Implimentation
    private void updateTask(DataSnapshot dataSnapshot) {
        FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
        mMessageAdapter.add(friendlyMessage);
    }

    protected void onPause() {
        super.onPause();
        //  mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        //mMessageAdapter.clear();
    }


    protected void onResume() {
        super.onResume();
        //  mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }


    //Reading data from firebase
    void attachDatabaseReadListener() {

        if (mChildEventListener == null) {

            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    updateTask(dataSnapshot);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    updateTask(dataSnapshot);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    updateTask(dataSnapshot);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mMessageDatabaseReference.addChildEventListener(mChildEventListener);

        }
    }

    //Storing Images
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER) {
            Uri selectedImageUri = data.getData();

            //Get a reference to store file at chat_photos<FILENAME>
            StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

            //Upload File to firebase storge
            photoRef.putFile(selectedImageUri).addOnSuccessListener(
                    this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            //     FriendlyMessage friendlyMessage = new FriendlyMessage(null,mUsername,downloadUrl.toString());
                            //     mMessageDatabaseReference.push().setValue(friendlyMessage);

                            FriendlyMessage newMessage = new FriendlyMessage(null, mUid, downloadUrl.toString());
                            mMessageDatabaseReference.push().setValue(newMessage);
                        }
                    });
        }

    }

}
