// MissionDetailActivity.java
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
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ftn.ma.myapplication.R;
// Proveri da li je putanja do modela ispravna u tvom projektu
import ftn.ma.myapplication.data.model.SpecialMission;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class MissionDetailActivity extends AppCompatActivity {

    // Konstante za kvote i štetu
    private static final int MAX_SHOP_ACTIONS_PER_DAY = 5;
    private static final int MAX_HARD_TASK_ACTIONS_PER_DAY = 2;
    private static final int SHOP_DAMAGE = 2;
    private static final int HARD_TASK_DAMAGE = 4;

    private SpecialMission mission;
    private int allianceMembers;
    private int bossMaxHp;
    private int bossCurrentHp;
    private long simulatedCurrentTime;

    private TextView textViewTitle, textViewStatus, textViewBossHp, textViewStartDate;
    private ProgressBar progressBarBoss;
    private EditText editTextMembers;
    private Button buttonActionShop, buttonActionHardTask;
    private LinearLayout actionsLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission_detail);

        mission = (SpecialMission) getIntent().getSerializableExtra("MISSION_EXTRA");
        if (mission == null) {
            Toast.makeText(this, "Greška pri učitavanju misije.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Uvek pročitaj najnovije simulirano vreme kad se vratimo na ekran.
        // Ovo je ključno za sinhronizaciju sa Profil ekranom.
        simulatedCurrentTime = SharedPreferencesManager.getSimulatedDate(this);
        loadMissionState();
        checkMissionStatus();
        updateUI();
    }

    private void bindViews() {
        textViewTitle = findViewById(R.id.textViewDetailMissionTitle);
        textViewStatus = findViewById(R.id.textViewMissionStatus);
        textViewBossHp = findViewById(R.id.textViewMissionBossHp);
        textViewStartDate = findViewById(R.id.textViewStartDate);
        progressBarBoss = findViewById(R.id.progressBarMissionBoss);
        editTextMembers = findViewById(R.id.editTextAllianceMembers);
        buttonActionShop = findViewById(R.id.buttonActionShop);
        buttonActionHardTask = findViewById(R.id.buttonActionHardTask);
        actionsLayout = findViewById(R.id.actionsLayout);
    }

    private void loadMissionState() {
        // Učitaj trajno sačuvane podatke za ovu specifičnu misiju
        mission.setHasExpired(SharedPreferencesManager.getMissionExpiredStatus(this, mission.getId()));
        mission.setStartDate(SharedPreferencesManager.getMissionStartDate(this, mission.getId()));

        int activeMissionId = SharedPreferencesManager.getActiveMissionId(this);
        mission.setActive(mission.getId() == activeMissionId && !mission.hasExpired());

        allianceMembers = SharedPreferencesManager.getMissionAllianceMembers(this, mission.getId());
        if (allianceMembers == 0) allianceMembers = 3; // Default vrednost

        bossMaxHp = 100 * allianceMembers;
        bossCurrentHp = SharedPreferencesManager.getMissionBossHp(this, mission.getId());

        // Resetuj HP ako se promenio broj članova ili ako HP nije inicijalizovan
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
                        bossCurrentHp = bossMaxHp; // Resetujemo HP

                        SharedPreferencesManager.saveMissionAllianceMembers(MissionDetailActivity.this, mission.getId(), allianceMembers);
                        SharedPreferencesManager.saveMissionBossHp(MissionDetailActivity.this, mission.getId(), bossCurrentHp);
                        SharedPreferencesManager.saveMissionMaxHp(MissionDetailActivity.this, mission.getId(), bossMaxHp);

                        updateUI();
                        Toast.makeText(MissionDetailActivity.this, "Broj članova i HP bosa su ažurirani!", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    // Ignorišemo grešku ako je polje prazno
                }
            }
        });

        // Listeneri sada pozivaju novu metodu koja proverava kvote
        buttonActionShop.setOnClickListener(v -> performAction("shop", SHOP_DAMAGE, MAX_SHOP_ACTIONS_PER_DAY));
        buttonActionHardTask.setOnClickListener(v -> performAction("hard_task", HARD_TASK_DAMAGE, MAX_HARD_TASK_ACTIONS_PER_DAY));
    }

    /**
     * Proverava status misije na osnovu simuliranog vremena.
     * Ako misija istekne, trajno je označava kao takvu.
     */
    private void checkMissionStatus() {
        if (mission.hasExpired()) { // Ako je jednom istekla, uvek je istekla
            mission.setActive(false);
            return;
        }

        // Proveravamo da li je istekla po pravilu od 14 dana [cite: 253]
        if (mission.isActive() && mission.isExpiredByTime(simulatedCurrentTime)) {
            mission.setHasExpired(true);
            mission.setActive(false);
            SharedPreferencesManager.saveMissionExpiredStatus(this, mission.getId(), true);
            SharedPreferencesManager.setActiveMissionId(this, -1); // Deaktiviraj misiju globalno
        }
    }

    /**
     * Izvršava akciju, proverava dnevnu kvotu i nanosi štetu bosu.
     */
    private void performAction(String actionKey, int damage, int maxCount) {
        if (!mission.isActive()) {
            Toast.makeText(this, "Misija nije aktivna!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Provera dnevne kvote za TRENUTNI SIMULIRANI DATUM
        int currentCount = SharedPreferencesManager.getDailyActionCount(this, mission.getId(), actionKey, simulatedCurrentTime);

        if (currentCount < maxCount) {
            currentCount++;
            bossCurrentHp -= damage;
            if (bossCurrentHp < 0) bossCurrentHp = 0;

            // Sačuvaj novi broj za današnji dan i novi HP bosa
            SharedPreferencesManager.saveDailyActionCount(this, mission.getId(), actionKey, simulatedCurrentTime, currentCount);
            SharedPreferencesManager.saveMissionBossHp(this, mission.getId(), bossCurrentHp);

            updateUI(); // Ažuriraj prikaz nakon akcije

            if (bossCurrentHp == 0) {
                Toast.makeText(this, "ČESTITAMO! Bos je pobeđen!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "Ispunjena je dnevna kvota za ovu akciju.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ažurira sve elemente na ekranu na osnovu trenutnog stanja.
     */
    private void updateUI() {
        textViewTitle.setText(mission.getTitle());
        editTextMembers.setText(String.valueOf(allianceMembers));

        // Prikaz datuma početka
        if (mission.getStartDate() > 0) {
            String dateStr = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date(mission.getStartDate()));
            textViewStartDate.setText("Započeto: " + dateStr);
        } else {
            textViewStartDate.setText("Misija nije započeta");
        }

        // Prikaz statusa i omogućavanje/onemogućavanje akcija
        if (mission.isActive()) {
            textViewStatus.setText("Status: Aktivna");
            textViewStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            actionsLayout.setVisibility(View.VISIBLE);
        } else {
            actionsLayout.setVisibility(View.GONE);
            if(mission.hasExpired()){
                textViewStatus.setText("Status: Istekla");
                textViewStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                textViewStatus.setText("Status: Neaktivna");
                textViewStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        }

        // Ažuriranje HP bara
        progressBarBoss.setMax(bossMaxHp);
        progressBarBoss.setProgress(bossCurrentHp);
        textViewBossHp.setText("HP Bosa: " + bossCurrentHp + " / " + bossMaxHp);

        // Ažuriranje teksta na dugmićima sa trenutnom dnevnom kvotom
        int shopCount = SharedPreferencesManager.getDailyActionCount(this, mission.getId(), "shop", simulatedCurrentTime);
        buttonActionShop.setText(String.format(Locale.getDefault(), "Kupovina (Danas: %d/%d)", shopCount, MAX_SHOP_ACTIONS_PER_DAY));

        int hardTaskCount = SharedPreferencesManager.getDailyActionCount(this, mission.getId(), "hard_task", simulatedCurrentTime);
        buttonActionHardTask.setText(String.format(Locale.getDefault(), "Težak zadatak (Danas: %d/%d)", hardTaskCount, MAX_HARD_TASK_ACTIONS_PER_DAY));
    }
}