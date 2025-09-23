package ftn.ma.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ftn.ma.myapplication.data.model.ChatMessage;
import ftn.ma.myapplication.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
    private static final int VIEW_TYPE_MESSAGE_SYSTEM = 3;
    private static final int VIEW_TYPE_MESSAGE_ANNOUNCEMENT = 4;
    
    private List<ChatMessage> messages;
    private int currentUserId;
    private SimpleDateFormat timeFormat;
    
    public ChatAdapter(List<ChatMessage> messages, int currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        
        if (message.messageType.equals(ChatMessage.MESSAGE_TYPE_SYSTEM)) {
            return VIEW_TYPE_MESSAGE_SYSTEM;
        } else if (message.messageType.equals(ChatMessage.MESSAGE_TYPE_ANNOUNCEMENT)) {
            return VIEW_TYPE_MESSAGE_ANNOUNCEMENT;
        } else if (message.senderId == currentUserId) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        switch (viewType) {
            case VIEW_TYPE_MESSAGE_SENT:
                return new SentMessageViewHolder(
                    inflater.inflate(R.layout.item_message_sent, parent, false)
                );
            case VIEW_TYPE_MESSAGE_RECEIVED:
                return new ReceivedMessageViewHolder(
                    inflater.inflate(R.layout.item_message_received, parent, false)
                );
            case VIEW_TYPE_MESSAGE_SYSTEM:
                return new SystemMessageViewHolder(
                    inflater.inflate(R.layout.item_message_system, parent, false)
                );
            case VIEW_TYPE_MESSAGE_ANNOUNCEMENT:
                return new AnnouncementMessageViewHolder(
                    inflater.inflate(R.layout.item_message_announcement, parent, false)
                );
            default:
                throw new IllegalArgumentException("Unknown view type: " + viewType);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        } else if (holder instanceof SystemMessageViewHolder) {
            ((SystemMessageViewHolder) holder).bind(message);
        } else if (holder instanceof AnnouncementMessageViewHolder) {
            ((AnnouncementMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    public void updateMessages(List<ChatMessage> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }
    
    // ViewHolder for sent messages
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView textViewTime;
        
        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
            textViewTime = itemView.findViewById(R.id.text_view_time);
        }
        
        void bind(ChatMessage message) {
            textViewMessage.setText(message.messageText);
            textViewTime.setText(timeFormat.format(message.timestamp));
        }
    }
    
    // ViewHolder for received messages
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView textViewTime;
        private TextView textViewSender;
        
        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
            textViewTime = itemView.findViewById(R.id.text_view_time);
            textViewSender = itemView.findViewById(R.id.text_view_sender);
        }
        
        void bind(ChatMessage message) {
            textViewMessage.setText(message.messageText);
            textViewTime.setText(timeFormat.format(message.timestamp));
            // TODO: Get sender name from database
            textViewSender.setText("Player " + message.senderId);
        }
    }
    
    // ViewHolder for system messages
    class SystemMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        
        SystemMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
        }
        
        void bind(ChatMessage message) {
            textViewMessage.setText(message.messageText);
        }
    }
    
    // ViewHolder for announcement messages
    class AnnouncementMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private TextView textViewTime;
        
        AnnouncementMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.text_view_message);
            textViewTime = itemView.findViewById(R.id.text_view_time);
        }
        
        void bind(ChatMessage message) {
            textViewMessage.setText(message.messageText);
            textViewTime.setText(timeFormat.format(message.timestamp));
        }
    }
}