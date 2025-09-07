package ftn.ma.myapplication.ui.game;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.model.Contribution;
import ftn.ma.myapplication.data.model.SpecialMission;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class MissionDetailActivity extends AppCompatActivity implements ContributionAdapter.OnContributionClickListener {

    // Konstante za UKUPNE misijske kvote
    private static final int MAX_SHOP_ACTIONS = 5;
    private static final int MAX_HITS = 10;
    private static final int MAX_EASY_TASKS_BUCKET = 10;
    private static final int MAX_HARD_TASKS = 6;
    private static final int DAILY_ACTION_LIMIT = 3; // Dnevni limit za korisnika

    // Konstante za štetu
    private static final int SHOP_DAMAGE = 2;
    private static final int HIT_DAMAGE = 2;
    private static final int EASY_TASK_DAMAGE = 1;
    private static final int HARD_TASK_DAMAGE = 4;
    private static final String REAL_USER_NAME = "Ja (student)";

    // ... (sve ostale varijable ostaju iste)
    private SpecialMission mission;
    private int allianceMembers;
    private int bossMaxHp;
    private int bossCurrentHp;
    private long simulatedCurrentTime;
    private TextView textViewTitle, textViewStatus, textViewBossHp, textViewStartDate;
    private ProgressBar progressBarBoss;
    private EditText editTextMembers;
    private Button buttonActionShop, buttonActionHit, buttonActionEasyTask, buttonActionHardTask;
    private LinearLayout actionsLayout;
    private RecyclerView recyclerViewContributions;
    private ContributionAdapter contributionAdapter;
    private List<String> allMemberNames;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_detail);
        mission = (SpecialMission) getIntent().getSerializableExtra("MISSION_EXTRA");
        if (mission == null) {
            finish();
            return;
        }
        bindViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        simulatedCurrentTime = SharedPreferencesManager.getSimulatedDate(this);
        loadMissionState();

        long lastSimulatedDay = SharedPreferencesManager.getLong(this, "last_sim_day_" + mission.getId(), 0);
        String currentDayStr = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date(simulatedCurrentTime));
        String lastSimDayStr = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date(lastSimulatedDay));

        if (mission.isActive() && !currentDayStr.equals(lastSimDayStr)) {
            List<String> fakeMembers = new ArrayList<>(allMemberNames);
            fakeMembers.remove(REAL_USER_NAME);
            int damageByBots = FakeMemberSimulator.simulateDailyProgress(this, mission.getId(), fakeMembers, simulatedCurrentTime);
            if (damageByBots > 0) {
                bossCurrentHp -= damageByBots;
                if (bossCurrentHp < 0) bossCurrentHp = 0;
                SharedPreferencesManager.saveMissionBossHp(this, mission.getId(), bossCurrentHp);
                Toast.makeText(this, "Članovi saveza su naneli " + damageByBots + " HP štete!", Toast.LENGTH_SHORT).show();
            }
            SharedPreferencesManager.saveLong(this, "last_sim_day_" + mission.getId(), simulatedCurrentTime);
        }

        checkMissionStatus();
        updateUI();
    }

    // bindViews, loadMissionState i checkMissionStatus ostaju isti
    private void bindViews() {
        textViewTitle = findViewById(R.id.textViewDetailMissionTitle);
        textViewStatus = findViewById(R.id.textViewMissionStatus);
        textViewBossHp = findViewById(R.id.textViewMissionBossHp);
        textViewStartDate = findViewById(R.id.textViewStartDate);
        progressBarBoss = findViewById(R.id.progressBarMissionBoss);
        editTextMembers = findViewById(R.id.editTextAllianceMembers);
        buttonActionShop = findViewById(R.id.buttonActionShop);
        buttonActionHit = findViewById(R.id.buttonActionHit);
        buttonActionEasyTask = findViewById(R.id.buttonActionEasyTask);
        buttonActionHardTask = findViewById(R.id.buttonActionHardTask);
        actionsLayout = findViewById(R.id.actionsLayout);
        recyclerViewContributions = findViewById(R.id.recyclerViewContributions);
    }

    private void loadMissionState() {
        mission.setHasExpired(SharedPreferencesManager.getMissionExpiredStatus(this, mission.getId()));
        mission.setStartDate(SharedPreferencesManager.getMissionStartDate(this, mission.getId()));
        int activeMissionId = SharedPreferencesManager.getActiveMissionId(this);
        mission.setActive(mission.getId() == activeMissionId && !mission.hasExpired());
        allianceMembers = SharedPreferencesManager.getMissionAllianceMembers(this, mission.getId());
        if (allianceMembers == 0) allianceMembers = 3;
        allMemberNames = new ArrayList<>();
        allMemberNames.add(REAL_USER_NAME);
        for (int i = 2; i <= allianceMembers; i++) {
            allMemberNames.add("Član " + i);
        }
        bossMaxHp = 100 * allianceMembers;
        bossCurrentHp = SharedPreferencesManager.getMissionBossHp(this, mission.getId());
        if (bossCurrentHp == -1 || SharedPreferencesManager.getMissionMaxHp(this, mission.getId()) != bossMaxHp) {
            bossCurrentHp = bossMaxHp;
            SharedPreferencesManager.saveMissionMaxHp(this, mission.getId(), bossMaxHp);
        }
    }

    private void setupListeners() {
        editTextMembers.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    int newMemberCount = Integer.parseInt(s.toString());
                    if (newMemberCount > 0 && newMemberCount != allianceMembers) {
                        allianceMembers = newMemberCount;
                        bossMaxHp = 100 * allianceMembers;
                        bossCurrentHp = bossMaxHp;
                        SharedPreferencesManager.saveMissionAllianceMembers(MissionDetailActivity.this, mission.getId(), allianceMembers);
                        SharedPreferencesManager.saveMissionBossHp(MissionDetailActivity.this, mission.getId(), bossCurrentHp);
                        SharedPreferencesManager.saveMissionMaxHp(MissionDetailActivity.this, mission.getId(), bossMaxHp);
                        loadMissionState();
                        updateUI();
                        Toast.makeText(MissionDetailActivity.this, "Broj članova i HP bosa su ažurirani!", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {}
            }
        });

        buttonActionShop.setOnClickListener(v -> performUserAction("shop", SHOP_DAMAGE, MAX_SHOP_ACTIONS));
        buttonActionHit.setOnClickListener(v -> performUserAction("hit", HIT_DAMAGE, MAX_HITS));
        buttonActionEasyTask.setOnClickListener(v -> performUserAction("easy_task", EASY_TASK_DAMAGE, MAX_EASY_TASKS_BUCKET));
        buttonActionHardTask.setOnClickListener(v -> performUserAction("hard_task", HARD_TASK_DAMAGE, MAX_HARD_TASKS));
    }

    private void checkMissionStatus() {
        if (mission.hasExpired()) {
            mission.setActive(false);
            return;
        }
        if (mission.isActive() && mission.isExpiredByTime(simulatedCurrentTime)) {
            mission.setHasExpired(true);
            mission.setActive(false);
            SharedPreferencesManager.saveMissionExpiredStatus(this, mission.getId(), true);
            SharedPreferencesManager.setActiveMissionId(this, -1);
        }
    }

    // --- KLJUČNA ISPRAVKA JE U OVOJ METODI ---
    private void performUserAction(String actionKey, int damage, int totalMaxCount) {
        if (!mission.isActive()) {
            Toast.makeText(this, "Misija nije aktivna!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Proveravamo i dnevnu i ukupnu kvotu
        int dailyCount = SharedPreferencesManager.getDailyActionCount(this, mission.getId(), REAL_USER_NAME, actionKey, simulatedCurrentTime);
        int totalCount = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), REAL_USER_NAME, actionKey);

        if (totalCount >= totalMaxCount) {
            Toast.makeText(this, "Ispunili ste ukupnu kvotu za ovu akciju.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dailyCount >= DAILY_ACTION_LIMIT) {
            Toast.makeText(this, "Ispunili ste dnevnu kvotu za ovu akciju. Promenite datum.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Ako su obe provere prošle, izvrši akciju
        dailyCount++;
        totalCount++;
        bossCurrentHp -= damage;
        if (bossCurrentHp < 0) bossCurrentHp = 0;

        // 3. Sačuvaj obe vrednosti
        SharedPreferencesManager.saveDailyActionCount(this, mission.getId(), REAL_USER_NAME, actionKey, simulatedCurrentTime, dailyCount);
        SharedPreferencesManager.saveMemberActionCount(this, mission.getId(), REAL_USER_NAME, actionKey, totalCount);
        SharedPreferencesManager.saveMissionBossHp(this, mission.getId(), bossCurrentHp);

        updateUI();
        if (bossCurrentHp == 0) Toast.makeText(this, "ČESTITAMO! Bos je pobeđen!", Toast.LENGTH_LONG).show();
    }

    private void updateUI() {
        textViewTitle.setText(mission.getTitle());
        editTextMembers.setText(String.valueOf(allianceMembers));

        if (mission.getStartDate() > 0) {
            String dateStr = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date(mission.getStartDate()));
            textViewStartDate.setText("Započeto: " + dateStr);
        } else {
            textViewStartDate.setText("Misija nije započeta");
        }

        if (mission.isActive()) {
            textViewStatus.setText("Status: Aktivna");
            actionsLayout.setVisibility(View.VISIBLE);
        } else {
            actionsLayout.setVisibility(View.GONE);
            textViewStatus.setText(mission.hasExpired() ? "Status: Istekla" : "Status: Neaktivna");
        }

        progressBarBoss.setMax(bossMaxHp);
        progressBarBoss.setProgress(bossCurrentHp);
        textViewBossHp.setText(getString(R.string.mission_boss_hp_format, bossCurrentHp, bossMaxHp));

        // --- POBOLJŠAN PRIKAZ NA DUGMIĆIMA ---
        int shopDaily = SharedPreferencesManager.getDailyActionCount(this, mission.getId(), REAL_USER_NAME, "shop", simulatedCurrentTime);
        int shopTotal = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), REAL_USER_NAME, "shop");
        buttonActionShop.setText(String.format(Locale.getDefault(), "Kupovina (Danas: %d/%d, Uk: %d/%d)", shopDaily, DAILY_ACTION_LIMIT, shopTotal, MAX_SHOP_ACTIONS));

        int hitDaily = SharedPreferencesManager.getDailyActionCount(this, mission.getId(), REAL_USER_NAME, "hit", simulatedCurrentTime);
        int hitTotal = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), REAL_USER_NAME, "hit");
        buttonActionHit.setText(String.format(Locale.getDefault(), "Udarac (Danas: %d/%d, Uk: %d/%d)", hitDaily, DAILY_ACTION_LIMIT, hitTotal, MAX_HITS));

        int easyDaily = SharedPreferencesManager.getDailyActionCount(this, mission.getId(), REAL_USER_NAME, "easy_task", simulatedCurrentTime);
        int easyTotal = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), REAL_USER_NAME, "easy_task");
        buttonActionEasyTask.setText(String.format(Locale.getDefault(), "Lak zadatak (Danas: %d/%d, Uk: %d/%d)", easyDaily, DAILY_ACTION_LIMIT, easyTotal, MAX_EASY_TASKS_BUCKET));

        int hardDaily = SharedPreferencesManager.getDailyActionCount(this, mission.getId(), REAL_USER_NAME, "hard_task", simulatedCurrentTime);
        int hardTotal = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), REAL_USER_NAME, "hard_task");
        buttonActionHardTask.setText(String.format(Locale.getDefault(), "Težak zadatak (Danas: %d/%d, Uk: %d/%d)", hardDaily, DAILY_ACTION_LIMIT, hardTotal, MAX_HARD_TASKS));

        updateContributionsList();
    }

    private void updateContributionsList() {
        List<Contribution> contributionList = new ArrayList<>();
        for (String memberName : allMemberNames) {
            int shopCount = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "shop");
            int hitCount = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "hit");
            int easyTaskCount = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "easy_task");
            int hardTaskCount = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "hard_task");

            int totalDamage = 0;
            totalDamage += Math.min(shopCount, MAX_SHOP_ACTIONS) * SHOP_DAMAGE;
            totalDamage += Math.min(hitCount, MAX_HITS) * HIT_DAMAGE;
            totalDamage += Math.min(easyTaskCount, MAX_EASY_TASKS_BUCKET) * EASY_TASK_DAMAGE;
            totalDamage += Math.min(hardTaskCount, MAX_HARD_TASKS) * HARD_TASK_DAMAGE;

            contributionList.add(new Contribution(memberName, totalDamage));
        }

        contributionAdapter = new ContributionAdapter(contributionList, this);
        recyclerViewContributions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContributions.setAdapter(contributionAdapter);
    }

    @Override
    public void onContributionLongClick(Contribution contribution) {
        String memberName = contribution.getMemberName();
        StringBuilder details = new StringBuilder();
        int shopCount = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "shop");
        int hitCount = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "hit");
        int easyTaskCount = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "easy_task");
        int hardTaskCount = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "hard_task");

        details.append(String.format(Locale.getDefault(), "Kupovine: %d/%d\n", shopCount, MAX_SHOP_ACTIONS));
        details.append(String.format(Locale.getDefault(), "Uspešni udarci: %d/%d\n", hitCount, MAX_HITS));
        details.append(String.format(Locale.getDefault(), "Laki zadaci (grupa): %d/%d\n", easyTaskCount, MAX_EASY_TASKS_BUCKET));
        details.append(String.format(Locale.getDefault(), "Teški zadaci: %d/%d\n", hardTaskCount, MAX_HARD_TASKS));

        new AlertDialog.Builder(this)
                .setTitle("Detalji za: " + memberName)
                .setMessage(details.toString())
                .setPositiveButton("U redu", null)
                .show();
    }
}