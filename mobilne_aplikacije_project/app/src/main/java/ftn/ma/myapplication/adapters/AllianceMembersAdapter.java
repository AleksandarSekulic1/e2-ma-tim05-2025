package ftn.ma.myapplication.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.model.AllianceMember;

/**
 * Adapter za prikaz članova saveza
 */
public class AllianceMembersAdapter extends RecyclerView.Adapter<AllianceMembersAdapter.MemberViewHolder> {
    
    private List<AllianceMember> members;
    private Context context;
    private OnMemberActionListener listener;
    
    public interface OnMemberActionListener {
        void onPromoteMember(AllianceMember member);
        void onRemoveMember(AllianceMember member);
    }
    
    public AllianceMembersAdapter(Context context, OnMemberActionListener listener) {
        this.context = context;
        this.listener = listener;
        this.members = new ArrayList<>();
    }
    
    public void updateMembers(List<AllianceMember> newMembers) {
        this.members.clear();
        this.members.addAll(newMembers);
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alliance_member, parent, false);
        return new MemberViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        AllianceMember member = members.get(position);
        holder.bind(member);
    }
    
    @Override
    public int getItemCount() {
        return members.size();
    }
    
    class MemberViewHolder extends RecyclerView.ViewHolder {
        private TextView usernameText;
        private TextView roleText;
        private TextView contributionText;
        private TextView joinDateText;
        private ImageView roleIcon;
        private ImageView actionButton;
        
        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            roleText = itemView.findViewById(R.id.role_text);
            contributionText = itemView.findViewById(R.id.contribution_text);
            joinDateText = itemView.findViewById(R.id.join_date_text);
            roleIcon = itemView.findViewById(R.id.role_icon);
            actionButton = itemView.findViewById(R.id.action_button);
        }
        
        public void bind(AllianceMember member) {
            usernameText.setText(member.getUsername());
            contributionText.setText("Doprinosi: " + member.getContributionPoints());
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            joinDateText.setText("Pridružio se: " + dateFormat.format(member.getJoinedAt()));
            
            // Postavi ulogu
            switch (member.getRole()) {
                case LEADER:
                    roleText.setText("Lider");
                    roleText.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                    roleIcon.setImageResource(R.drawable.ic_crown);
                    roleIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_orange_dark));
                    actionButton.setVisibility(View.GONE); // Ne može se ukloniti lider
                    break;
                    
                case MEMBER:
                    roleText.setText("Član");
                    roleText.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                    roleIcon.setImageResource(R.drawable.ic_person);
                    roleIcon.setColorFilter(context.getResources().getColor(android.R.color.holo_blue_dark));
                    actionButton.setVisibility(View.VISIBLE);
                    break;
            }
            
            // Postavi akcije
            actionButton.setOnClickListener(v -> showMemberActions(member));
        }
        
        private void showMemberActions(AllianceMember member) {
            // Možete implementirati popup meni za akcije
            if (listener != null) {
                listener.onRemoveMember(member);
            }
        }
    }
}