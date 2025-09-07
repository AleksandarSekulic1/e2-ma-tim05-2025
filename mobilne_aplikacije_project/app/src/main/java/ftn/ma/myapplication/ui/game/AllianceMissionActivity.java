// AllianceMissionActivity.java
// AllianceMissionActivity.java
package ftn.ma.myapplication.ui.game;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.model.SpecialMission;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class AllianceMissionActivity extends AppCompatActivity implements MissionAdapter.OnMissionClickListener {

    private RecyclerView recyclerViewMissions;
    private MissionAdapter adapter;
    private List<SpecialMission> missionList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_mission);
        setTitle("Izaberi Misiju");

        recyclerViewMissions = findViewById(R.id.recyclerViewMissions);
        recyclerViewMissions.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMissions(); // Učitavamo misije svaki put kad se vratimo na ekran
        if (adapter == null) {
            adapter = new MissionAdapter(missionList, this);
            recyclerViewMissions.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private void loadMissions() {
        if (missionList == null) {
            missionList = new ArrayList<>();
            missionList.add(new SpecialMission(1, "Napad na Zmajevu Tvrđavu", 0, false));
            missionList.add(new SpecialMission(2, "Odbrana sela Oakhaven", 0, false));
            missionList.add(new SpecialMission(3, "Potraga za Izgubljenim Artefaktom", 0, false));
            missionList.add(new SpecialMission(4, "Proboj kroz Mračnu Šumu", 0, false));
            missionList.add(new SpecialMission(5, "Opsada Ledene Citadele", 0, false));
        }

        int activeMissionId = SharedPreferencesManager.getActiveMissionId(this);
        for (SpecialMission mission : missionList) {
            // Učitaj trajni status i datum početka za svaku misiju
            mission.setHasExpired(SharedPreferencesManager.getMissionExpiredStatus(this, mission.getId()));
            mission.setStartDate(SharedPreferencesManager.getMissionStartDate(this, mission.getId()));
            mission.setActive(mission.getId() == activeMissionId && !mission.hasExpired());
        }
    }

    @Override
    public void onMissionClick(SpecialMission mission) {
        Intent intent = new Intent(this, MissionDetailActivity.class);
        intent.putExtra("MISSION_EXTRA", mission);
        startActivity(intent);
    }

    @Override
    public void onMissionLongClick(SpecialMission mission) {
        if (mission.hasExpired()) {
            Toast.makeText(this, "Ova misija je trajno istekla.", Toast.LENGTH_SHORT).show();
            return;
        }

        int activeMissionId = SharedPreferencesManager.getActiveMissionId(this);
        if (activeMissionId != -1 && activeMissionId != mission.getId()) {
            Toast.makeText(this, "Već postoji aktivna misija!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mission.isActive()) {
            Toast.makeText(this, "Ova misija je već aktivna.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Aktivacija Misije")
                .setMessage("Da li želite da aktivirate misiju '" + mission.getTitle() + "'? Vreme počinje da teče sada.")
                .setPositiveButton("Aktiviraj", (dialog, which) -> {
                    long currentTime = SharedPreferencesManager.getSimulatedDate(this);
                    mission.setStartDate(currentTime);
                    mission.setActive(true);

                    SharedPreferencesManager.saveMissionStartDate(this, mission.getId(), currentTime);
                    SharedPreferencesManager.setActiveMissionId(this, mission.getId());

                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, "Misija aktivirana!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Odustani", null)
                .show();
        }
}