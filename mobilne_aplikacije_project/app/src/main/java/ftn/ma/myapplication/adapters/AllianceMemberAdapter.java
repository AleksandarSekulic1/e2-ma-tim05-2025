package ftn.ma.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ftn.ma.myapplication.data.model.AllianceMember;

public class AllianceMemberAdapter extends RecyclerView.Adapter<AllianceMemberAdapter.MemberViewHolder> {
    private List<AllianceMember> members = new ArrayList<>();

    public void updateMembers(List<AllianceMember> newMembers) {
        members.clear();
        members.addAll(newMembers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create simple text view for member
        TextView textView = new TextView(parent.getContext());
        textView.setPadding(32, 16, 32, 16);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return new MemberViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        AllianceMember member = members.get(position);
        holder.memberText.setText("User " + member.getUserId() + " (" + member.getRole() + ")");
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView memberText;

        MemberViewHolder(View itemView) {
            super(itemView);
            memberText = (TextView) itemView;
        }
    }
}
