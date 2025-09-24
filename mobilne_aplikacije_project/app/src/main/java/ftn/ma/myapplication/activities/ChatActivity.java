package ftn.ma.myapplication.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ftn.ma.myapplication.adapters.ChatAdapter;
import ftn.ma.myapplication.data.model.ChatMessage;
import ftn.ma.myapplication.data.local.ChatStorage;
import ftn.ma.myapplication.utils.SessionManager;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messagesList;
    
    private SessionManager sessionManager;
    private long allianceId = 1L; // Default alliance ID
    private String allianceName = "Alliance Chat";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        sessionManager = new SessionManager(this);
        
        // Dobij alliance info iz Intent-a
        allianceId = getIntent().getLongExtra("allianceId", 1L);
        allianceName = getIntent().getStringExtra("allianceName");
        if (allianceName == null) allianceName = "Alliance Chat";
        
        // Postavi title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(allianceName);
        }
        
        createChatLayout();
        setupRecyclerView();
        loadMessages();
    }

    private void createChatLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        messagesRecyclerView = new RecyclerView(this);
        messagesRecyclerView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        layout.addView(messagesRecyclerView);

        LinearLayout inputLayout = new LinearLayout(this);
        inputLayout.setOrientation(LinearLayout.HORIZONTAL);

        messageEditText = new EditText(this);
        messageEditText.setHint("Type message...");
        messageEditText.setLayoutParams(new LinearLayout.LayoutParams(0, 
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        inputLayout.addView(messageEditText);

        sendButton = new Button(this);
        sendButton.setText("Send");
        sendButton.setOnClickListener(v -> sendMessage());
        inputLayout.addView(sendButton);

        layout.addView(inputLayout);
        setContentView(layout);
    }

    private void setupRecyclerView() {
        messagesList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messagesList);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(chatAdapter);
    }

    private void loadMessages() {
        // Učitaj postojeće poruke iz storage-a
        messagesList.addAll(ChatStorage.getMessagesForAlliance(this, allianceId));
        
        // Ako nema poruka, dodaj welcome poruku
        if (messagesList.isEmpty()) {
            ChatMessage welcomeMessage = ChatStorage.createMessage(
                allianceId, 0L, "System", "Welcome to " + allianceName + " chat!"
            );
            ChatStorage.saveMessage(this, welcomeMessage);
            messagesList.add(welcomeMessage);
        }
        
        chatAdapter.notifyDataSetChanged();
        
        // Skroluj na poslednju poruku
        if (!messagesList.isEmpty()) {
            messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
        }
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // Kreiraj novu poruku
            long senderId = sessionManager.getUserId();
            String senderUsername = sessionManager.getUsername();
            if (senderUsername == null || senderUsername.isEmpty()) {
                senderUsername = "User" + senderId;
            }
            
            ChatMessage newMessage = ChatStorage.createMessage(
                allianceId, senderId, senderUsername, messageText
            );
            
            // Sačuvaj u storage
            ChatStorage.saveMessage(this, newMessage);
            
            // Dodaj u listu i refreshuj UI
            messagesList.add(newMessage);
            chatAdapter.notifyDataSetChanged();
            messageEditText.setText("");
            messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
            
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        }
    }
}
