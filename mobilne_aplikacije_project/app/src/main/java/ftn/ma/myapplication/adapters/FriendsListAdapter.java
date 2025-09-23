package ftn.ma.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.model.Friend;

/**
 * Adapter za prikaz liste prijatelja za pozivanje u savez
 */
public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.FriendViewHolder> {
    
    private List<Friend> friends;
    private Context context;
    private OnFriendInviteListener listener;
    
    public interface OnFriendInviteListener {
        void onInviteFriend(Friend friend);
    }
    
    public FriendsListAdapter(Context context, List<Friend> friends, OnFriendInviteListener listener) {
        this.context = context;
        this.friends = friends;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_invite, parent, false);
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
        private Button inviteButton;
        
        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            inviteButton = itemView.findViewById(R.id.invite_button);
        }
        
        public void bind(Friend friend) {
            usernameText.setText(friend.getFriendUsername());
            
            inviteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onInviteFriend(friend);
                    inviteButton.setEnabled(false);
                    inviteButton.setText("Pozvan");
                }
            });
        }
    }
}