package com.example.ahmed.thechatapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<FriendlyMessage> {
    public String author;
    public String currentAuthor;
    //Activity layout;
    Activity context;
    LayoutInflater mLayoutInflater;

    //defining firebaseauth object
    private FirebaseAuth firebaseAuth;


    public MessageAdapter(Activity context, int resource, List<FriendlyMessage> objects) {
        super(context, resource, objects);

        //initializing firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();
        currentAuthor = firebaseAuth.getCurrentUser().getEmail();
        //context=activity(main)
        this.context = context;
        //Layout Inflater
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FriendlyMessage message = getItem(position);

        //Inflate in convertView
        convertView = mLayoutInflater.inflate(currentAuthor.matches(message.getName()) ? R.layout.item_my_message : R.layout.item_else_message,null);

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.photoImageView);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        //Checks in Photo
        boolean isPhoto = message.getPhotoUrl() != null;
        if (isPhoto) {
            messageTextView.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            Glide.with(photoImageView.getContext())
                    .load(message.getPhotoUrl())
                    .into(photoImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(message.getText());
        }
        //for author text view(Name)
        author = message.getName();
        authorTextView.setText(author);


        return convertView;
    }
}
