// MissionAdapter.java
package ftn.ma.myapplication.ui.game;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ftn.ma.myapplication.R;
// Proveri da li je putanja do modela ispravna
import ftn.ma.myapplication.data.model.SpecialMission;

public class MissionAdapter extends RecyclerView.Adapter<MissionAdapter.MissionViewHolder> {

    private List<SpecialMission> missions;
    private OnMissionClickListener listener;

    // INTERFEJS JE KLJUČAN DEO KOJI SE MORA ISPRAVITI
    // Mora da sadrži OBE metode koje tvoja aktivnost implementira.
    public interface OnMissionClickListener {
        void onMissionClick(SpecialMission mission);
        void onMissionLongClick(SpecialMission mission); // Ova linija verovatno nedostaje kod tebe
    }

    public MissionAdapter(List<SpecialMission> missions, OnMissionClickListener listener) {
        this.missions = missions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mission, parent, false);
        return new MissionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MissionViewHolder holder, int position) {
        SpecialMission mission = missions.get(position);
        holder.bind(mission, listener);
    }

    @Override
    public int getItemCount() {
        return missions.size();
    }

    static class MissionViewHolder extends RecyclerView.ViewHolder {
        private TextView missionTitle;
        private ImageView missionStatus;

        public MissionViewHolder(@NonNull View itemView) {
            super(itemView);
            missionTitle = itemView.findViewById(R.id.textViewMissionTitle);
            missionStatus = itemView.findViewById(R.id.imageViewStatus);
        }

        public void bind(final SpecialMission mission, final OnMissionClickListener listener) {
            missionTitle.setText(mission.getTitle());

            // Postavljanje ikonice na osnovu statusa
            if (mission.hasExpired()) {
                missionStatus.setImageResource(R.drawable.ic_dot_expired);
            } else if (mission.isActive()) {
                missionStatus.setImageResource(R.drawable.ic_dot_active);
            } else {
                missionStatus.setImageResource(R.drawable.ic_dot_inactive);
            }

            itemView.setOnClickListener(v -> listener.onMissionClick(mission));

            // Postavljanje listener-a za dugi klik
            itemView.setOnLongClickListener(v -> {
                listener.onMissionLongClick(mission);
                return true;
            });
        }
    }
}
