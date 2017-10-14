package com.csm117.digitalbazaar;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.util.Log;
import android.support.design.widget.FloatingActionButton;
import android.view.*;
import android.widget.*;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.ui.database.FirebaseListAdapter;
import android.text.format.DateFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class ChatMainActivity extends AppCompatActivity {
    private FirebaseListAdapter<ChatMessage> adapter;
    private String currentUserId;
    private String otherUserId;
    private String conversationId;
    private String chatPath;
    private static String curUser;
    private static String otherUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_main);
        Log.d("tag", "Inside on-create for ChatMainActivity.");

        //get currentUserId and otherUserid
        curUser = getIntent().getExtras().getString("userID");
        otherUser = getIntent().getExtras().getString("otheruserID");

        currentUserId = curUser;
        otherUserId = otherUser;
        String temp = currentUserId + otherUserId;
        char[] chars = temp.toCharArray();
        Arrays.sort(chars);
        String sorted = new String(chars);
        conversationId = "conv-id-" + sorted;

        // Create new chat thread for the two users. Store thread id in each user's account info
        String currentUserPath = "accounts/" + currentUserId + "/conversations/" + conversationId;
        FirebaseDatabase.getInstance()
                        .getReference(currentUserPath)
                        .setValue(new Date().getTime());

//        String otherUserPath = "accounts/" + otherUserId + "/conversations/" + conversationId;
//        FirebaseDatabase.getInstance()
//                .getReference(otherUserPath)
//                .setValue(new Date().getTime());

        chatPath = "messages/" + conversationId;

        FloatingActionButton fab =
                (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("tag", "User clicked floating action button");
                EditText input = (EditText)findViewById(R.id.input);

                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database
                FirebaseDatabase.getInstance()
                        .getReference(chatPath)
                        .push()
                        .setValue(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getDisplayName())
                        );

                // Clear the input
                input.setText("");
            }
        });

        displayChatRoomMessages();


//        }

    }

    private void displayChatRoomMessages() {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);


        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference(chatPath)) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());

                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }


}
