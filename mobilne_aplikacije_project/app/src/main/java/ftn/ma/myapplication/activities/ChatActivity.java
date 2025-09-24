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

public class ChatActivity extends AppCompatActivity {

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messagesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
        // TODO: Load messages from database
        // Add sample message
        ChatMessage sample = new ChatMessage(1L, 1L, "TestUser", "Welcome to alliance chat!");
        messagesList.add(sample);
        chatAdapter.notifyDataSetChanged();
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            // TODO: Save to database
            ChatMessage newMessage = new ChatMessage(1L, 1L, "You", messageText);
            messagesList.add(newMessage);
            chatAdapter.notifyDataSetChanged();
            messageEditText.setText("");
            messagesRecyclerView.scrollToPosition(messagesList.size() - 1);
            
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        }
    }
}
