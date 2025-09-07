package ftn.ma.myapplication.ui.game;

import android.os.Bundle;
import android.util.Pair;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Date;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class AllianceMissionActivity extends AppCompatActivity {

    // --- ZABETONIRANI PODACI ---
    private static final int FAKE_ALLIANCE_MEMBERS = 3;
    private static final int MAX_SHOP_ACTIONS = 5;
    private static final int MAX_HARD_TASK_ACTIONS = 6;

    private int missionBossMaxHp;
    private int missionBossCurrentHp;
    private int userContribution = 0;

    private TextView textViewMissionBossHp, textViewUserContribution;
    private ProgressBar progressBarMissionBoss;
    private Button buttonActionShop, buttonActionHardTask, buttonResetMission;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance_mission);
        setTitle("Misija Saveza");

        bindViews();

        // Izračunaj max HP na osnovu lažnog broja članova
        missionBossMaxHp = 100 * FAKE_ALLIANCE_MEMBERS;

        // Učitaj sačuvan napredak, ako postoji
        missionBossCurrentHp = SharedPreferencesManager.getMissionBossHp(this);
        if (missionBossCurrentHp == -1) {
            missionBossCurrentHp = missionBossMaxHp;
        }

        updateUI();

        buttonActionShop.setOnClickListener(v -> performSpecialAction("shop", 2, MAX_SHOP_ACTIONS));
        buttonActionHardTask.setOnClickListener(v -> performSpecialAction("hard_task", 4, MAX_HARD_TASK_ACTIONS));
        buttonResetMission.setOnClickListener(v -> {
            SharedPreferencesManager.resetMissionProgress(this);
            missionBossCurrentHp = missionBossMaxHp;
            Toast.makeText(this, "Napredak misije je resetovan.", Toast.LENGTH_SHORT).show();
            updateUI();
        });
    }

    private void bindViews() {
        textViewMissionBossHp = findViewById(R.id.textViewMissionBossHp);
        textViewUserContribution = findViewById(R.id.textViewUserContribution);
        progressBarMissionBoss = findViewById(R.id.progressBarMissionBoss);
        buttonActionShop = findViewById(R.id.buttonActionShop);
        buttonActionHardTask = findViewById(R.id.buttonActionHardTask);
        buttonResetMission = findViewById(R.id.buttonResetMission);
    }

    private void performSpecialAction(String key, int damage, int maxCount) {
        Pair<Integer, Long> progress = SharedPreferencesManager.getSpecialTaskProgress(this, key);
        int currentCount = progress.first;
        long lastActionTimestamp = progress.second;

        // --- IZMENA: Proveravamo da li je novi dan u odnosu na poslednju akciju ---
        // Koristimo našu novu metodu koja uzima u obzir simulirani datum
        if (!isSameDayConsideringSimulation(new Date(lastActionTimestamp))) {
            currentCount = 0; // Resetuj brojač ako je novi dan
        }

        if (currentCount < maxCount) {
            currentCount++;
            missionBossCurrentHp -= damage;
            if (missionBossCurrentHp < 0) {
                missionBossCurrentHp = 0;
            }

            // Čuvamo novi napredak sa TRENUTNIM vremenom (bilo stvarnim ili simuliranim)
            long now = getcurrentTime();
            SharedPreferencesManager.saveSpecialTaskProgress(this, key, currentCount, now);
            SharedPreferencesManager.saveMissionBossHp(this, missionBossCurrentHp);

            Toast.makeText(this, "Naneseno " + damage + " štete bosu!", Toast.LENGTH_SHORT).show();
            updateUI();

            if (missionBossCurrentHp == 0) {
                Toast.makeText(this, "ČESTITAMO! Bos misije je pobeđen!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Ispunjena dnevna kvota za ovu akciju (" + currentCount + "/" + maxCount + ")", Toast.LENGTH_SHORT).show();
        }
    }
    private void updateUI() {
        // --- IZMENA: Ažurirano da koristi novu logiku datuma ---
        int shopContribution = 0;
        Pair<Integer, Long> shopProgress = SharedPreferencesManager.getSpecialTaskProgress(this, "shop");
        if (isSameDayConsideringSimulation(new Date(shopProgress.second))) {
            shopContribution = shopProgress.first * 2;
        }

        int hardTaskContribution = 0;
        Pair<Integer, Long> hardTaskProgress = SharedPreferencesManager.getSpecialTaskProgress(this, "hard_task");
        if (isSameDayConsideringSimulation(new Date(hardTaskProgress.second))) {
            hardTaskContribution = hardTaskProgress.first * 4;
        }

        userContribution = shopContribution + hardTaskContribution;

        textViewMissionBossHp.setText("HP Bosa: " + missionBossCurrentHp + " / " + missionBossMaxHp);
        progressBarMissionBoss.setMax(missionBossMaxHp);
        progressBarMissionBoss.setProgress(missionBossCurrentHp);
        textViewUserContribution.setText("Tvoj doprinos (danas): " + userContribution + " HP");
    }

    // --- NOVA METODA: Vraća simulirano vreme ako postoji, inače stvarno ---
    private long getcurrentTime() {
        long simulatedTimestamp = SharedPreferencesManager.getSimulatedDate(this);
        if (simulatedTimestamp != 0L) {
            return simulatedTimestamp;
        } else {
            return System.currentTimeMillis();
        }
    }

    // --- NOVA METODA: Proverava da li su datumi isti, uzimajući u obzir simulaciju ---
    private boolean isSameDayConsideringSimulation(Date date1) {
        Date currentDate = new Date(getcurrentTime());

        if (date1 == null) return false;
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(currentDate);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}