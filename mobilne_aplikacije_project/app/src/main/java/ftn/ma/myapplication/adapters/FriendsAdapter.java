package ftn.ma.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.model.Friend;
import ftn.ma.myapplication.utils.UserSessionManager;

/**
 * Adapter za prikaz liste prijatelja u RecyclerView
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    
    private List<Friend> friends;
    private Context context;
    private OnFriendActionListener listener;
    private int currentUserId;
    
    public interface OnFriendActionListener {
        void onAcceptFriend(Friend friend);
        void onRejectFriend(Friend friend);
        void onRemoveFriend(Friend friend);
        void onBlockFriend(Friend friend);
    }
    
    public FriendsAdapter(Context context, OnFriendActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.friends = new ArrayList<>();
        
        // Dobij trenutni user ID iz sesije
        UserSessionManager sessionManager = new UserSessionManager(context);
        this.currentUserId = sessionManager.getCurrentUserId();
    }
    
    public void updateFriends(List<Friend> newFriends) {
        this.friends.clear();
        this.friends.addAll(newFriends);
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        Friend friend = friends.get(position);
        holder.bind(friend);
    }
    
    @Override
    public int getItemCount() {
        return friends.size();
    }
    
    class FriendViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameText;
        private TextView statusText;
        private TextView dateText;
        private ImageView statusIcon;
        private Button actionButton1;
        private Button actionButton2;
        private View actionsLayout;
        
        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            statusText = itemView.findViewById(R.id.status_text);
            dateText = itemView.findViewById(R.id.date_text);
            statusIcon = itemView.findViewById(R.id.status_icon);
            actionButton1 = itemView.findViewById(R.id.action_button_1);
            actionButton2 = itemView.findViewById(R.id.action_button_2);
            actionsLayout = itemView.findViewById(R.id.actions_layout);
        }
        
        public void bind(Friend friend) {
            usernameText.setText(friend.getFriendUsername());
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            dateText.setText(dateFormat.format(friend.getCreatedAt()));
            
            setupStatusAndActions(friend);
        }
        
        private void setupStatusAndActions(Friend friend) {
            switch (friend.getStatus()) {
                case ACCEPTED:
                    statusText.setText("Prijatelj");
                    statusText.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
                    statusIcon.setImageResource(R.drawable.ic_check_circle);
                    statusIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_green_dark));
                    
                    // Dugmad za accepted prijatelje
                    actionsLayout.setVisibility(View.VISIBLE);
                    actionButton1.setText("Ukloni");
                    actionButton1.setBackgroundResource(R.drawable.button_danger);
                    actionButton1.setOnClickListener(v -> listener.onRemoveFriend(friend));
                    
                    actionButton2.setText("Blokiraj");
                    actionButton2.setBackgroundResource(R.drawable.button_outline);
                    actionButton2.setOnClickListener(v -> listener.onBlockFriend(friend));
                    break;
                    
                case PENDING:
                    // Razlikuj ko je poslao zahtev
                    if (friend.getFriendId() == getCurrentUserId()) {
                        // Primljen zahtev - mogu da prihvatim/odbacim
                        statusText.setText("Zahtev primljen");
                        statusText.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                        statusIcon.setImageResource(R.drawable.ic_pending);
                        statusIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_orange_dark));
                        
                        actionsLayout.setVisibility(View.VISIBLE);
                        actionButton1.setText("Prihvati");
                        actionButton1.setBackgroundResource(R.drawable.button_success);
                        actionButton1.setOnClickListener(v -> listener.onAcceptFriend(friend));
                        
                        actionButton2.setText("Odbaci");
                        actionButton2.setBackgroundResource(R.drawable.button_danger);
                        actionButton2.setOnClickListener(v -> listener.onRejectFriend(friend));
                    } else {
                        // Poslat zahtev - mogu samo da otkažem
                        statusText.setText("Zahtev poslat");
                        statusText.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                        statusIcon.setImageResource(R.drawable.ic_send);
                        statusIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_blue_dark));
                        
                        actionsLayout.setVisibility(View.VISIBLE);
                        actionButton1.setText("Otkaži");
                        actionButton1.setBackgroundResource(R.drawable.button_outline);
                        actionButton1.setOnClickListener(v -> listener.onRejectFriend(friend));
                        
                        actionButton2.setVisibility(View.GONE);
                    }
                    break;
                    
                case REJECTED:
                    statusText.setText("Odbačen");
                    statusText.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
                    statusIcon.setImageResource(R.drawable.ic_close);
                    statusIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_red_dark));
                    actionsLayout.setVisibility(View.GONE);
                    break;
                    
                case BLOCKED:
                    statusText.setText("Blokiran");
                    statusText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                    statusIcon.setImageResource(R.drawable.ic_block);
                    statusIcon.setColorFilter(context.getResources().getColor(android.R.color.darker_gray));
                    actionsLayout.setVisibility(View.GONE);
                    break;
            }
        }
        
        private int getCurrentUserId() {
            return currentUserId;
        }
    }
}