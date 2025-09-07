// ContributionAdapter.java
package ftn.ma.myapplication.ui.game;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.model.Contribution;

public class ContributionAdapter extends RecyclerView.Adapter<ContributionAdapter.ViewHolder> {

    private List<Contribution> contributions;
    private OnContributionClickListener listener;

    // NOVO: Interfejs sada ima i dugi klik
    public interface OnContributionClickListener {
        void onContributionLongClick(Contribution contribution);
    }

    public ContributionAdapter(List<Contribution> contributions, OnContributionClickListener listener) {
        this.contributions = contributions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contribution_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(contributions.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return contributions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView memberName;
        TextView memberDamage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.textViewMemberName);
            memberDamage = itemView.findViewById(R.id.textViewMemberDamage);
        }

        // NOVO: Bind metoda sada postavlja i listener za dugi klik
        public void bind(final Contribution contribution, final OnContributionClickListener listener) {
            memberName.setText(contribution.getMemberName());
            memberDamage.setText("Ukupno HP: " + contribution.getTotalDamage());
            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onContributionLongClick(contribution);
                }
                return true;
            });
        }
    }
}
