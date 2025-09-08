package ftn.ma.myapplication.ui.game;

import android.animation.Animator;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.data.model.Task;
import ftn.ma.myapplication.domain.LevelingManager;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class BossFightActivity extends AppCompatActivity implements SensorEventListener {

    private TextView textViewBossName, textViewBossHp, textViewPlayerStats, textViewAttacksLeft, textViewAttackChance;
    private LottieAnimationView lottieAnimationBoss, lottieAnimationChest;
    private TextView textViewRewards, textViewShake, textViewActiveEquipment;
    private ProgressBar progressBarBossHp;
    private Button buttonAttack;
    private LinearLayout rewardsIconLayout;
    private ImageView imageViewCoinReward;
    private LottieAnimationView lottieArmorReward, lottieWeaponReward;

    private int userLevel, basePp, userPp, bossMaxHp, bossCurrentHp, bossLevel;
    private int attacksLeft = 5;
    private int baseSuccessChance = 0, successChance = 0;
    private boolean battleEnded = false;
    private TaskDao taskDao;
    private ExecutorService executorService;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float lastX, lastY, lastZ;
    private long lastUpdate = 0;
    private static final int SHAKE_THRESHOLD = 500;
    private ProgressBar progressBarPlayerPp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        taskDao = database.taskDao();
        executorService = Executors.newSingleThreadExecutor();
        bindViews();

        userLevel = getIntent().getIntExtra("USER_LEVEL", 1);
        basePp = getIntent().getIntExtra("USER_PP", 40);
        int permanentBonus = SharedPreferencesManager.getPermanentPpBonus(this);
        userPp = basePp + permanentBonus;

        bossLevel = LevelingManager.getNextBossToFight(this, userLevel);
        bossMaxHp = LevelingManager.calculateBossHpForLevel(bossLevel);
        bossCurrentHp = bossMaxHp;

        prepareNextFight();
    }

    private void prepareNextFight() {
        // Resetujemo promenljive za novu borbu
        battleEnded = false;
        attacksLeft = 5;

        // Ponovo učitavamo osnovne podatke korisnika
        int permanentBonus = SharedPreferencesManager.getPermanentPpBonus(this);
        userPp = basePp + permanentBonus;

        // Određujemo kog bosa napadamo
        bossLevel = LevelingManager.getNextBossToFight(this, userLevel);
        bossMaxHp = LevelingManager.calculateBossHpForLevel(bossLevel);

        // --- IZMENA: Proveravamo da li bos ima sačuvan HP ---
        int savedHp = SharedPreferencesManager.getBossCurrentHp(this, bossLevel);
        if (savedHp != -1) {
            bossCurrentHp = savedHp; // Ako ima, nastavljamo gde smo stali
        } else {
            bossCurrentHp = bossMaxHp; // Ako nema, HP je na maksimumu
        }

        // Sakrivamo UI za nagrade i prikazujemo UI za borbu
        resetUIForBattle();
        calculateBaseAttackChance(); // Ovo pokreće ceo lanac (dijalog -> startFight)
    }
    private void bindViews() {
        textViewBossName = findViewById(R.id.textViewBossName);
        textViewBossHp = findViewById(R.id.textViewBossHp);
        textViewPlayerStats = findViewById(R.id.textViewPlayerStats);
        textViewAttacksLeft = findViewById(R.id.textViewAttacksLeft);
        textViewAttackChance = findViewById(R.id.textViewAttackChance);
        lottieAnimationBoss = findViewById(R.id.lottieAnimationBoss);
        lottieAnimationChest = findViewById(R.id.lottieAnimationChest);
        textViewRewards = findViewById(R.id.textViewRewards);
        textViewShake = findViewById(R.id.textViewShake);
        progressBarBossHp = findViewById(R.id.progressBarBossHp);
        buttonAttack = findViewById(R.id.buttonAttack);
        textViewActiveEquipment = findViewById(R.id.textViewActiveEquipment);
        rewardsIconLayout = findViewById(R.id.rewardsIconLayout);
        imageViewCoinReward = findViewById(R.id.imageViewCoinReward);
        lottieArmorReward = findViewById(R.id.lottieArmorReward);
        lottieWeaponReward = findViewById(R.id.lottieWeaponReward);
        progressBarPlayerPp = findViewById(R.id.progressBarPlayerPp);
    }

    private void showEquipmentDialog() {
        final String[] equipmentItems = {
                // Odeća
                "Rukavice (+10% PP)",           // 0
                "Štit (+10% šansa za pogodak)", // 1
                "Čizme (40% šansa za +1 napad)",  // 2
                // Napici (jednokratni)
                "Napitak snage (+20% PP)",      // 3
                "Jači napitak snage (+40% PP)"  // 4
        };
        final boolean[] selectedItems = new boolean[equipmentItems.length];

        new AlertDialog.Builder(this)
                .setTitle("Pripremi se za borbu!")
                .setMultiChoiceItems(equipmentItems, selectedItems, (dialog, which, isChecked) -> {
                    selectedItems[which] = isChecked;
                })
                .setPositiveButton("Započni Borbu", (dialog, which) -> {
                    startFight(selectedItems);
                })
                .setCancelable(false)
                .show();
    }

    private void startFight(boolean[] selectedItems) {
        List<String> activeEquipmentNames = new ArrayList<>();
        int temporaryPpBonusPercent = 0;

        if (selectedItems[0]) { // Rukavice
            temporaryPpBonusPercent += 10;
            activeEquipmentNames.add("Rukavice");
        }
        if (selectedItems[1]) { // Štit
            successChance = Math.min(100, baseSuccessChance + 10);
            activeEquipmentNames.add("Štit");
        } else {
            successChance = baseSuccessChance;
        }
        if (selectedItems[2]) { // Čizme
            if (new Random().nextInt(100) < 40) {
                attacksLeft++;
                Toast.makeText(this, "Čizme su ti dale dodatni napad!", Toast.LENGTH_SHORT).show();
            }
            activeEquipmentNames.add("Čizme");
        }
        if (selectedItems[3]) { // Napitak
            temporaryPpBonusPercent += 20;
            activeEquipmentNames.add("Napitak snage");
        }
        if (selectedItems[4]) { // Jači napitak
            temporaryPpBonusPercent += 40;
            activeEquipmentNames.add("Jači napitak");
        }

        // Primenjujemo sve PP bonuse odjednom
        if (temporaryPpBonusPercent > 0) {
            userPp = userPp + (int)(userPp * (temporaryPpBonusPercent / 100.0));
        }

        if (!activeEquipmentNames.isEmpty()) {
            textViewActiveEquipment.setText("Aktivno: " + TextUtils.join(", ", activeEquipmentNames));
            textViewActiveEquipment.setVisibility(View.VISIBLE);
        }

        updateUI();
        setupBossAnimation();
        buttonAttack.setOnClickListener(v -> performAttack());
    }

    private void setupBossAnimation() {
        String animationFile;
        switch (bossLevel) {
            case 1:
                animationFile = "boss_level_1.json";
                break;
            case 2:
                animationFile = "boss_level_2.json";
                break;
            case 3:
                animationFile = "boss_level_3.json";
                break;
            default:
                animationFile = "boss_monster.json";
                break;
        }
        lottieAnimationBoss.setAnimation(animationFile);
        lottieAnimationBoss.loop(true);
        lottieAnimationBoss.playAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        // --- NOVO: Čuvamo HP bosa ako borba nije gotova ---
        if (!battleEnded && bossCurrentHp < bossMaxHp) {
            SharedPreferencesManager.saveBossCurrentHp(this, bossLevel, bossCurrentHp);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    if (!battleEnded) {
                        performAttack();
                    } else {
                        lottieAnimationChest.playAnimation();
                        textViewShake.setVisibility(View.GONE);
                        sensorManager.unregisterListener(this);
                    }
                }
                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    private void calculateBaseAttackChance() {
        executorService.execute(() -> {
            long lastLevelUpTimestamp = SharedPreferencesManager.getLastLevelUpDate(this);
            Date startDate = new Date(lastLevelUpTimestamp);
            List<Task> tasksInEtapa = taskDao.getTasksCreatedAfter(startDate);

            // Prvo filtriramo zadatke koji se uopšte računaju (nisu pauzirani ili otkazani)
            List<Task> relevantTasks = tasksInEtapa.stream()
                    .filter(task -> task.getStatus() != Task.Status.PAUZIRAN && task.getStatus() != Task.Status.OTKAZAN)
                    .collect(Collectors.toList());

            long totalTasksInEtapa = relevantTasks.size();

            if (totalTasksInEtapa == 0) {
                baseSuccessChance = 75; // Podrazumevana vrednost ako nema zadataka
            } else {
                // "Uspešno rešen" zadatak je onaj koji je URAĐEN i za koji su DOBIJENI POENI
                long completedTasksWithXp = relevantTasks.stream()
                        .filter(task -> task.getStatus() == Task.Status.URADJEN && task.isXpAwarded())
                        .count();

                baseSuccessChance = (int) (((double) completedTasksWithXp / totalTasksInEtapa) * 100);
            }

            runOnUiThread(this::showEquipmentDialog);
        });
    }

    private void performAttack() {
        if (attacksLeft <= 0 || bossCurrentHp <= 0 || battleEnded) return;

        attacksLeft--;
        int roll = new Random().nextInt(101);

        if (roll < successChance) {
            bossCurrentHp -= userPp;
            if (bossCurrentHp < 0) bossCurrentHp = 0;
            Toast.makeText(this, "Pogodak! Naneo si " + userPp + " štete!", Toast.LENGTH_SHORT).show();
            recordSpecialMissionProgressForHit();
        } else {
            Toast.makeText(this, "Promašaj!", Toast.LENGTH_SHORT).show();
        }

        updateUI();

        if (attacksLeft == 0 || bossCurrentHp == 0) {
            endBattle();
        }
    }

    private void updateUI() {
        textViewBossName.setText("Bos Nivo " + bossLevel);
        textViewPlayerStats.setText("Tvoja snaga (PP): " + userPp);
        textViewAttacksLeft.setText("Preostalo napada: " + attacksLeft);
        textViewAttackChance.setText("Šansa za pogodak: " + successChance + "%");
        textViewBossHp.setText(bossCurrentHp + " / " + bossMaxHp + " HP");
        progressBarBossHp.setMax(bossMaxHp);
        progressBarBossHp.setProgress(bossCurrentHp);

        // --- NOVO: Ažuriramo ProgressBar za PP ---
        // Postavljamo maksimum na snagu sledećeg bosa radi skaliranja, a progress na trenutnu snagu
        progressBarPlayerPp.setMax(LevelingManager.calculateBossHpForLevel(bossLevel + 1));
        progressBarPlayerPp.setProgress(userPp);
    }

    private void endBattle() {
        battleEnded = true;
        hideBattleUI();

        int coinReward = 0;
        boolean hasArmorReward = false;
        boolean hasWeaponReward = false;

        if (bossCurrentHp <= 0) { // POBEDA
            SharedPreferencesManager.saveBossDefeatedStatus(this, bossLevel, true);
            // Resetujemo sačuvan HP za ovog bosa, jer je pobeđen
            SharedPreferencesManager.saveBossCurrentHp(this, bossLevel, -1);

            coinReward = LevelingManager.calculateCoinReward(bossLevel);
            if (new Random().nextInt(100) < 20) {
                if (new Random().nextInt(100) < 5) {
                    hasWeaponReward = true;
                } else {
                    hasArmorReward = true;
                }
            }

            int nextBossLevel = LevelingManager.getNextBossToFight(this, userLevel);
            if (nextBossLevel != bossLevel) {
                new AlertDialog.Builder(this)
                        .setTitle("Pobeda!")
                        .setMessage("Pobedili ste bosa Nivoa " + bossLevel + "! Ali čeka vas još jedan...")
                        .setPositiveButton("Nastavi borbu!", (dialog, which) -> prepareNextFight())
                        .setNegativeButton("Nazad", (dialog, which) -> finish())
                        .setCancelable(false)
                        .show();
                return;
            }

        } else { // PORAZ ILI DELIMIČNA POBEDA
            // --- NOVO: Čuvamo preostali HP bosa ---
            SharedPreferencesManager.saveBossCurrentHp(this, bossLevel, bossCurrentHp);

            if ((double)(bossMaxHp - bossCurrentHp) / bossMaxHp >= 0.5) { // DELIMIČNA POBEDA
                coinReward = LevelingManager.calculateCoinReward(bossLevel) / 2;
                if (new Random().nextInt(100) < 10) {
                    if (new Random().nextInt(100) < 5) {
                        hasWeaponReward = true;
                    } else {
                        hasArmorReward = true;
                    }
                }
            }
        }

        if (coinReward > 0) {
            int currentCoins = SharedPreferencesManager.getUserCoins(this);
            SharedPreferencesManager.saveUserCoins(this, currentCoins + coinReward);
        }
        String rewardText = "Osvojili ste: " + coinReward + " novčića!";
        textViewRewards.setText(rewardText);
        showRewardUI(coinReward > 0, hasArmorReward, hasWeaponReward);
    }


    private void hideBattleUI() {
        buttonAttack.setVisibility(View.GONE);
        lottieAnimationBoss.setVisibility(View.GONE);
        progressBarBossHp.setVisibility(View.GONE);
        textViewBossHp.setVisibility(View.GONE);
        textViewPlayerStats.setVisibility(View.GONE);
        textViewAttacksLeft.setVisibility(View.GONE);
        textViewAttackChance.setVisibility(View.GONE);
        textViewBossName.setVisibility(View.GONE);
        textViewActiveEquipment.setVisibility(View.GONE);
    }

    private void showRewardUI(boolean hasCoin, boolean hasArmor, boolean hasWeapon) {
        lottieAnimationChest.setVisibility(View.VISIBLE);
        textViewShake.setVisibility(View.VISIBLE);

        lottieAnimationChest.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {}

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                textViewRewards.setVisibility(View.VISIBLE);
                rewardsIconLayout.setVisibility(View.VISIBLE);

                if (hasCoin) {
                    imageViewCoinReward.setVisibility(View.VISIBLE);
                }
                if (hasArmor) {
                    lottieArmorReward.setVisibility(View.VISIBLE);
                    lottieArmorReward.playAnimation();
                }
                if (hasWeapon) {
                    lottieWeaponReward.setVisibility(View.VISIBLE);
                    lottieWeaponReward.playAnimation();
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {}
            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {}
        });
    }
    private void resetUIForBattle() {
        buttonAttack.setVisibility(View.VISIBLE);
        lottieAnimationBoss.setVisibility(View.VISIBLE);
        progressBarBossHp.setVisibility(View.VISIBLE);
        textViewBossHp.setVisibility(View.VISIBLE);
        textViewPlayerStats.setVisibility(View.VISIBLE);
        textViewAttacksLeft.setVisibility(View.VISIBLE);
        textViewAttackChance.setVisibility(View.VISIBLE);
        textViewBossName.setVisibility(View.VISIBLE);

        lottieAnimationChest.setVisibility(View.GONE);
        textViewShake.setVisibility(View.GONE);
        rewardsIconLayout.setVisibility(View.GONE);
        textViewRewards.setVisibility(View.GONE);
        textViewActiveEquipment.setVisibility(View.GONE);
    }

    /**
     * NOVO: Metoda koja proverava da li je specijalna misija aktivna
     * i beleži uspešan udarac ako jeste.
     */
    private void recordSpecialMissionProgressForHit() {
        // 1. Proveri da li je ijedna misija aktivna
        int activeMissionId = SharedPreferencesManager.getActiveMissionId(this);
        if (activeMissionId == -1) {
            return; // Nijedna misija nije aktivna, ne radi ništa
        }

        // 2. Proveri da li je kvota za udarce ispunjena
        final String actionKey = "hit";
        final String userName = "Ja (student)"; // Koristimo isti ključ kao u MissionDetailActivity
        final int maxHits = 10; // Maksimalan broj udaraca po specifikaciji

        int currentHits = SharedPreferencesManager.getMemberActionCount(this, activeMissionId, userName, actionKey);

        if (currentHits < maxHits) {
            // 3. Ako kvota nije ispunjena, povećaj je i sačuvaj
            currentHits++;
            SharedPreferencesManager.saveMemberActionCount(this, activeMissionId, userName, actionKey, currentHits);

            // 4. Obavesti korisnika
            Toast.makeText(this, "Napredak za specijalnu misiju zabeležen!", Toast.LENGTH_SHORT).show();
        }
    }

}