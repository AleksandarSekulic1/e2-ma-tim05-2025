package ftn.ma.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ftn.ma.myapplication.data.model.User;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {

    private List<User> friends;

    public FriendsAdapter(List<User> friends) {
        this.friends = friends;
    }

    @Override
    public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FriendViewHolder holder, int position) {
        User friend = friends.get(position);
        holder.nameText.setText(friend.getUsername());
    }

    @Override
    public int getItemCount() {
        return friends != null ? friends.size() : 0;
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;

        public FriendViewHolder(View itemView) {
            super(itemView);
            nameText = itemView.findViewById(android.R.id.text1);
        }
    }
}
