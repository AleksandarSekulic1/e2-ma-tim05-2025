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

import com.airbnb.lottie.LottieAnimationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.model.Contribution;
import ftn.ma.myapplication.data.model.SpecialMission;
import ftn.ma.myapplication.util.RewardManager;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class MissionDetailActivity extends AppCompatActivity implements ContributionAdapter.OnContributionClickListener {

    // ... (Sve konstante ostaju iste) ...
    private static final int MAX_SHOP_ACTIONS = 5;
    private static final int MAX_HITS = 10;
    private static final int MAX_EASY_TASKS_BUCKET = 10;
    private static final int MAX_HARD_TASKS = 6;
    private static final int MAX_BONUS = 1;
    private static final int SHOP_DAMAGE = 2;
    private static final int HIT_DAMAGE = 2;
    private static final int EASY_TASK_DAMAGE = 1;
    private static final int HARD_TASK_DAMAGE = 4;
    private static final int MESSAGE_DAMAGE = 4;
    private static final int BONUS_DAMAGE = 10;
    private static final String REAL_USER_NAME = "Ja (student)";
    private static final String TEST_USER_KEY = "TEST_DAMAGE_USER"; // Ključ za beleženje test štete

    // ... (Sve varijable ostaju iste)
    private SpecialMission mission;
    private int allianceMembers;
    private int bossMaxHp;
    private int bossCurrentHp;
    private long simulatedCurrentTime;
    private TextView textViewTitle, textViewStatus, textViewBossHp, textViewStartDate;
    private ProgressBar progressBarBoss;
    private EditText editTextMembers;
    private Button buttonActionShop, buttonActionHit, buttonActionEasyTask, buttonActionHardTask, buttonActionMessage, buttonActionBonus, buttonTestActionDamage;
    private LinearLayout actionsLayout;
    private RecyclerView recyclerViewContributions;
    private ContributionAdapter contributionAdapter;
    private List<String> allMemberNames;
    private LottieAnimationView lottieAnimationMissionBoss;


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
            FakeMemberSimulator.simulateDailyProgress(this, mission.getId(), fakeMembers, simulatedCurrentTime);
            SharedPreferencesManager.saveLong(this, "last_sim_day_" + mission.getId(), simulatedCurrentTime);
        }

        recalculateBossHpFromContributions();
        checkMissionStatus();
        updateUI();

        if ((!mission.isActive() || bossCurrentHp <= 0) && bossCurrentHp <= 0 && !SharedPreferencesManager.isMissionRewardClaimed(this, mission.getId())) {
            awardMissionRewards();
        }
    }

    // bindViews i loadMissionState ostaju isti
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
        buttonActionMessage = findViewById(R.id.buttonActionMessage);
        buttonActionBonus = findViewById(R.id.buttonActionBonus);
        actionsLayout = findViewById(R.id.actionsLayout);
        recyclerViewContributions = findViewById(R.id.recyclerViewContributions);
        buttonTestActionDamage = findViewById(R.id.buttonTestActionDamage);
        lottieAnimationMissionBoss = findViewById(R.id.lottieAnimationMissionBoss);
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
        // --- ISPRAVKA LOGIKE ZA TEST DUGME ---
        buttonTestActionDamage.setOnClickListener(v -> {
            if (!mission.isActive()) {
                Toast.makeText(this, "Misija nije aktivna!", Toast.LENGTH_SHORT).show();
                return;
            }
            // Umesto direktne izmene HP-a, beležimo test štetu kao akciju
            int currentTestDamage = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), TEST_USER_KEY, "test_damage");
            int newTestDamage = currentTestDamage + 50;
            SharedPreferencesManager.saveMemberActionCount(this, mission.getId(), TEST_USER_KEY, "test_damage", newTestDamage);

            // Pozivamo centralni obračun koji će sada videti i ovu štetu
            recalculateBossHpFromContributions();
            updateUI();
            Toast.makeText(this, "TEST: Dodato 50 test štete!", Toast.LENGTH_SHORT).show();
        });

        // Ostali listeneri ostaju isti...
        editTextMembers.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                try {
                    int newMemberCount = Integer.parseInt(s.toString());
                    if (newMemberCount > 0 && newMemberCount != allianceMembers) {
                        allianceMembers = newMemberCount;
                        bossMaxHp = 100 * allianceMembers;
                        SharedPreferencesManager.saveMissionAllianceMembers(MissionDetailActivity.this, mission.getId(), allianceMembers);
                        SharedPreferencesManager.saveMissionMaxHp(MissionDetailActivity.this, mission.getId(), bossMaxHp);
                        loadMissionState();
                        recalculateBossHpFromContributions();
                        updateUI();
                        Toast.makeText(MissionDetailActivity.this, "Broj članova i HP bosa su ažurirani!", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {}
            }
        });
        buttonActionShop.setOnClickListener(v -> performUserMissionAction("shop"));
        buttonActionHit.setOnClickListener(v -> performUserMissionAction("hit"));
        buttonActionEasyTask.setOnClickListener(v -> performUserMissionAction("easy_task"));
        buttonActionHardTask.setOnClickListener(v -> performUserMissionAction("hard_task"));
        buttonActionBonus.setOnClickListener(v -> performUserMissionAction("bonus"));
        buttonActionMessage.setOnClickListener(v -> performUserDailyAction("message"));
    }

    // --- ISPRAVKA LOGIKE ZA OBRAČUN ---
    private void recalculateBossHpFromContributions() {
        int totalDamageDealt = 0;
        // 1. Saberi štetu od svih članova
        for (String memberName : allMemberNames) {
            totalDamageDealt += calculateMemberDamage(memberName);
        }
        // 2. Dodaj na to i test štetu
        totalDamageDealt += SharedPreferencesManager.getMemberActionCount(this, mission.getId(), TEST_USER_KEY, "test_damage");

        // 3. Izračunaj i sačuvaj finalni HP
        bossCurrentHp = bossMaxHp - totalDamageDealt;
        if (bossCurrentHp < 0) bossCurrentHp = 0;
        SharedPreferencesManager.saveMissionBossHp(this, mission.getId(), bossCurrentHp);
    }

    // Ostale metode ostaju nepromenjene
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
    private void performUserMissionAction(String actionKey) {
        if (!mission.isActive()) { Toast.makeText(this, "Misija nije aktivna!", Toast.LENGTH_SHORT).show(); return; }
        int totalMaxCount = getMaxCountForKey(actionKey);
        int totalCount = SharedPreferencesManager.getMemberActionCount(this, mission.getId(), REAL_USER_NAME, actionKey);
        if (totalCount < totalMaxCount) {
            totalCount++;
            SharedPreferencesManager.saveMemberActionCount(this, mission.getId(), REAL_USER_NAME, actionKey, totalCount);
            recalculateBossHpFromContributions();
            updateUI();
            if (bossCurrentHp == 0) Toast.makeText(this, "ČESTITAMO! Bos je pobeđen!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Ispunili ste ukupnu kvotu za ovu akciju.", Toast.LENGTH_SHORT).show();
        }
    }
    private void performUserDailyAction(String actionKey) {
        if (!mission.isActive()) { Toast.makeText(this, "Misija nije aktivna!", Toast.LENGTH_SHORT).show(); return; }
        int dailyCount = SharedPreferencesManager.getDailyActionCount(this, mission.getId(), REAL_USER_NAME, actionKey, simulatedCurrentTime);
        if (dailyCount < 1) {
            dailyCount++;
            SharedPreferencesManager.saveDailyActionCount(this, mission.getId(), REAL_USER_NAME, actionKey, simulatedCurrentTime, dailyCount);
            recalculateBossHpFromContributions();
            updateUI();
            if (bossCurrentHp == 0) Toast.makeText(this, "ČESTITAMO! Bos je pobeđen!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Već ste izvršili ovu dnevnu akciju.", Toast.LENGTH_SHORT).show();
        }
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
        buttonActionShop.setText(String.format(Locale.getDefault(), "Kupovina (Uk: %d/%d)", SharedPreferencesManager.getMemberActionCount(this, mission.getId(), REAL_USER_NAME, "shop"), MAX_SHOP_ACTIONS));
        buttonActionHit.setText(String.format(Locale.getDefault(), "Udarac (Uk: %d/%d)", SharedPreferencesManager.getMemberActionCount(this, mission.getId(), REAL_USER_NAME, "hit"), MAX_HITS));
        buttonActionEasyTask.setText(String.format(Locale.getDefault(), "Lak zadatak (Uk: %d/%d)", SharedPreferencesManager.getMemberActionCount(this, mission.getId(), REAL_USER_NAME, "easy_task"), MAX_EASY_TASKS_BUCKET));
        buttonActionHardTask.setText(String.format(Locale.getDefault(), "Težak zadatak (Uk: %d/%d)", SharedPreferencesManager.getMemberActionCount(this, mission.getId(), REAL_USER_NAME, "hard_task"), MAX_HARD_TASKS));
        buttonActionMessage.setText(String.format(Locale.getDefault(), "Pošalji poruku (Danas: %d/1)", SharedPreferencesManager.getDailyActionCount(this, mission.getId(), REAL_USER_NAME, "message", simulatedCurrentTime)));
        buttonActionBonus.setText(String.format(Locale.getDefault(), "Bonus: Bez greške (%d/1)", SharedPreferencesManager.getMemberActionCount(this, mission.getId(), REAL_USER_NAME, "bonus")));
        updateContributionsList();
    }
    private void updateContributionsList() {
        List<Contribution> contributionList = new ArrayList<>();
        for (String memberName : allMemberNames) {
            contributionList.add(new Contribution(memberName, calculateMemberDamage(memberName)));
        }
        contributionAdapter = new ContributionAdapter(contributionList, this);
        recyclerViewContributions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContributions.setAdapter(contributionAdapter);
    }
    private int calculateMemberDamage(String memberName) {
        int totalDamage = 0;
        totalDamage += Math.min(SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "shop"), MAX_SHOP_ACTIONS) * SHOP_DAMAGE;
        totalDamage += Math.min(SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "hit"), MAX_HITS) * HIT_DAMAGE;
        totalDamage += Math.min(SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "easy_task"), MAX_EASY_TASKS_BUCKET) * EASY_TASK_DAMAGE;
        totalDamage += Math.min(SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "hard_task"), MAX_HARD_TASKS) * HARD_TASK_DAMAGE;
        totalDamage += Math.min(SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "bonus"), MAX_BONUS) * BONUS_DAMAGE;
        if (mission.getStartDate() > 0) {
            long daysBetween = TimeUnit.MILLISECONDS.toDays(simulatedCurrentTime - mission.getStartDate());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(mission.getStartDate());
            for (int i = 0; i <= daysBetween && i < 14; i++) {
                if (i > 0) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }
                if (SharedPreferencesManager.getDailyActionCount(this, mission.getId(), memberName, "message", calendar.getTimeInMillis()) > 0) {
                    totalDamage += MESSAGE_DAMAGE;
                }
            }
        }
        return totalDamage;
    }
    @Override
    public void onContributionLongClick(Contribution contribution) {
        String memberName = contribution.getMemberName();
        StringBuilder details = new StringBuilder();
        details.append(String.format(Locale.getDefault(), "Kupovine: %d/%d\n", SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "shop"), MAX_SHOP_ACTIONS));
        details.append(String.format(Locale.getDefault(), "Uspešni udarci: %d/%d\n", SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "hit"), MAX_HITS));
        details.append(String.format(Locale.getDefault(), "Laki zadaci (grupa): %d/%d\n", SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "easy_task"), MAX_EASY_TASKS_BUCKET));
        details.append(String.format(Locale.getDefault(), "Teški zadaci: %d/%d\n", SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "hard_task"), MAX_HARD_TASKS));
        details.append(String.format(Locale.getDefault(), "Bonus bez greške: %d/%d\n", SharedPreferencesManager.getMemberActionCount(this, mission.getId(), memberName, "bonus"), MAX_BONUS));
        new AlertDialog.Builder(this)
                .setTitle("Detalji za: " + memberName)
                .setMessage(details.toString())
                .setPositiveButton("U redu", null)
                .show();
    }
    private int getMaxCountForKey(String actionKey) {
        switch (actionKey) {
            case "shop": return MAX_SHOP_ACTIONS;
            case "hit": return MAX_HITS;
            case "easy_task": return MAX_EASY_TASKS_BUCKET;
            case "hard_task": return MAX_HARD_TASKS;
            case "bonus": return MAX_BONUS;
            default: return 0;
        }
    }
    private void awardMissionRewards() {
        SharedPreferencesManager.saveMissionRewardClaimed(this, mission.getId(), true);
        int currentCompleted = SharedPreferencesManager.getCompletedMissionsCount(this);
        int newCompletedCount = currentCompleted + 1;
        SharedPreferencesManager.saveCompletedMissionsCount(this, newCompletedCount);
        int coinsWon = RewardManager.calculateMissionCoinReward(this);
        String rewardMessage = "Osvojili ste:\n\n" +
                "● 1x Napitak\n" +
                "● 1x Komad odeće\n" +
                "● " + coinsWon + " novčića\n" +
                "● 1x Bedž za misiju (ukupno: " + newCompletedCount + ")";
        new AlertDialog.Builder(this)
                .setTitle("Misija Uspešno Završena!")
                .setMessage(rewardMessage)
                .setPositiveButton("Sjajno!", null)
                .setIcon(R.drawable.ic_treasure_chest)
                .show();
    }
}