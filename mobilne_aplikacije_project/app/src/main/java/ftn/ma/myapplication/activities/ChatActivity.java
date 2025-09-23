package ftn.ma.myapplication.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.dao.ChatMessageDao;
import ftn.ma.myapplication.data.model.ChatMessage;
import ftn.ma.myapplication.adapters.ChatAdapter;
import ftn.ma.myapplication.utils.UserSessionManager;
import ftn.ma.myapplication.R;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private static final String EXTRA_CHAT_TYPE = "chat_type";
    private static final String EXTRA_CHAT_ID = "chat_id";
    private static final String EXTRA_CHAT_TITLE = "chat_title";
    
    public static final String CHAT_TYPE_FRIEND = "friend";
    public static final String CHAT_TYPE_ALLIANCE = "alliance";
    
    private RecyclerView recyclerViewMessages;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ChatAdapter chatAdapter;
    
    private AppDatabase database;
    private ChatMessageDao chatMessageDao;
    private ExecutorService executor;
    private UserSessionManager sessionManager;
    
    private String chatType;
    private int chatId;
    private String chatTitle;
    private int currentUserId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        initializeViews();
        parseIntent();
        initializeDatabase();
        setupRecyclerView();
        setupSendButton();
        loadMessages();
    }
    
    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        recyclerViewMessages = findViewById(R.id.recycler_view_messages);
        editTextMessage = findViewById(R.id.edit_text_message);
        buttonSend = findViewById(R.id.button_send);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void parseIntent() {
        chatType = getIntent().getStringExtra(EXTRA_CHAT_TYPE);
        chatId = getIntent().getIntExtra(EXTRA_CHAT_ID, -1);
        chatTitle = getIntent().getStringExtra(EXTRA_CHAT_TITLE);
        
        if (getSupportActionBar() != null && chatTitle != null) {
            getSupportActionBar().setTitle(chatTitle);
        }
    }
    
    private void initializeDatabase() {
        database = AppDatabase.getDatabase(this);
        chatMessageDao = database.chatMessageDao();
        executor = Executors.newSingleThreadExecutor();
        sessionManager = new UserSessionManager(this);
        
        // Get current user ID from session
        currentUserId = sessionManager.getCurrentUserId();
    }
    
    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter(new ArrayList<>(), currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setAdapter(chatAdapter);
    }
    
    private void setupSendButton() {
        buttonSend.setOnClickListener(v -> sendMessage());
        
        // Enable/disable send button based on text input
        editTextMessage.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonSend.setEnabled(s.toString().trim().length() > 0);
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        buttonSend.setEnabled(false);
    }
    
    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }
        
        ChatMessage message = new ChatMessage();
        message.setSenderId(currentUserId);
        message.setAllianceId(chatId); // Pretpostavljam da je chatId zapravo allianceId
        message.setSenderUsername(sessionManager.getCurrentUsername());
        message.setContent(messageText);
        message.setType(ChatMessage.MessageType.TEXT);
        message.setTimestamp(new Date());
        message.setRead(false);
        
        executor.execute(() -> {
            chatMessageDao.insert(message);
            
            runOnUiThread(() -> {
                editTextMessage.setText("");
                loadMessages(); // Refresh messages
            });
        });
    }
    
    private void loadMessages() {
        executor.execute(() -> {
            List<ChatMessage> messages = chatMessageDao.getMessagesForChat(chatType, chatId);
            
            runOnUiThread(() -> {
                chatAdapter.updateMessages(messages);
                if (!messages.isEmpty()) {
                    recyclerViewMessages.smoothScrollToPosition(messages.size() - 1);
                }
            });
        });
        
        // Mark messages as read
        markMessagesAsRead();
    }
    
    private void markMessagesAsRead() {
        executor.execute(() -> {
            chatMessageDao.markMessagesAsRead(chatType, chatId, currentUserId);
        });
    }
    
    public void sendSystemMessage(String messageText) {
        ChatMessage message = new ChatMessage();
        message.senderId = 0; // System message
        message.chatType = chatType;
        message.chatId = chatId;
        message.messageText = messageText;
        message.messageType = ChatMessage.MESSAGE_TYPE_SYSTEM;
        message.timestamp = new Date();
        message.isRead = true;
        
        executor.execute(() -> {
            chatMessageDao.insert(message);
            runOnUiThread(this::loadMessages);
        });
    }
    
    public void sendAnnouncementMessage(String messageText) {
        ChatMessage message = new ChatMessage();
        message.senderId = 0; // System message
        message.chatType = chatType;
        message.chatId = chatId;
        message.messageText = messageText;
        message.messageType = ChatMessage.MESSAGE_TYPE_ANNOUNCEMENT;
        message.timestamp = new Date();
        message.isRead = false;
        
        executor.execute(() -> {
            chatMessageDao.insert(message);
            runOnUiThread(this::loadMessages);
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}