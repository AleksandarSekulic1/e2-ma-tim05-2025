package ftn.ma.myapplication.ui.game;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.data.model.Task;
import ftn.ma.myapplication.domain.LevelingManager;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class BossFightActivity extends AppCompatActivity implements SensorEventListener {

    // UI Elementi
    private TextView textViewBossName, textViewBossHp, textViewPlayerStats, textViewAttacksLeft, textViewAttackChance;
    private LottieAnimationView lottieAnimationBoss, lottieAnimationChest;
    private TextView textViewRewards, textViewShake;
    private ProgressBar progressBarBossHp;
    private Button buttonAttack;

    // Logika borbe
    private int userLevel, userPp, bossMaxHp, bossCurrentHp;
    private int attacksLeft = 5;
    private int successChance = 0;
    private boolean battleEnded = false;

    // Baza i niti
    private TaskDao taskDao;
    private ExecutorService executorService;

    // Senzor
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float lastX, lastY, lastZ;
    private long lastUpdate = 0;
    private static final int SHAKE_THRESHOLD = 800;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_fight);

        // Inicijalizacija
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        taskDao = database.taskDao();
        executorService = Executors.newSingleThreadExecutor();

        bindViews();

        // Učitavanje podataka i priprema ekrana
        userLevel = getIntent().getIntExtra("USER_LEVEL", 1);
        userPp = getIntent().getIntExtra("USER_PP", 40);
        bossMaxHp = LevelingManager.calculateBossHpForLevel(userLevel);
        bossCurrentHp = bossMaxHp;

        calculateAttackChance();
        updateUI();
        setupBossAnimation();

        buttonAttack.setOnClickListener(v -> performAttack());
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
    }

    private void setupBossAnimation() {
        lottieAnimationBoss.setAnimation("boss_monster.json");
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
                        textViewRewards.setVisibility(View.VISIBLE);
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    private void calculateAttackChance() {
        executorService.execute(() -> {
            List<Task> allTasks = taskDao.getAllTasks();
            if (allTasks.isEmpty()) {
                successChance = 75;
            } else {
                int completedTasks = 0;
                for (Task task : allTasks) {
                    if (task.getStatus() == Task.Status.URADJEN) {
                        completedTasks++;
                    }
                }
                successChance = (int) (((double) completedTasks / allTasks.size()) * 100);
            }
            runOnUiThread(() -> textViewAttackChance.setText("Šansa za pogodak: " + successChance + "%"));
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
        } else {
            Toast.makeText(this, "Promašaj!", Toast.LENGTH_SHORT).show();
        }

        updateUI();

        if (attacksLeft == 0 || bossCurrentHp == 0) {
            endBattle();
        }
    }

    private void updateUI() {
        textViewBossName.setText("Bos Nivo " + userLevel);
        textViewPlayerStats.setText("Tvoja snaga (PP): " + userPp);
        textViewAttacksLeft.setText("Preostalo napada: " + attacksLeft);
        textViewBossHp.setText(bossCurrentHp + " / " + bossMaxHp + " HP");
        progressBarBossHp.setMax(bossMaxHp);
        progressBarBossHp.setProgress(bossCurrentHp);
    }

    private void endBattle() {
        battleEnded = true;
        hideBattleUI();

        int coinReward = 0;
        String equipmentReward = "";

        if (bossCurrentHp <= 0) { // POBEDA
            coinReward = LevelingManager.calculateCoinReward(userLevel);
            if (new Random().nextInt(100) < 20) {
                equipmentReward = (new Random().nextInt(100) < 5) ? "Oružje!" : "Odeća!";
            }
        } else if ((double)(bossMaxHp - bossCurrentHp) / bossMaxHp >= 0.5) { // DELIMIČNA POBEDA
            coinReward = LevelingManager.calculateCoinReward(userLevel) / 2;
            if (new Random().nextInt(100) < 10) {
                equipmentReward = (new Random().nextInt(100) < 5) ? "Oružje!" : "Odeća!";
            }
        }

        if (coinReward > 0) {
            int currentCoins = SharedPreferencesManager.getUserCoins(this);
            SharedPreferencesManager.saveUserCoins(this, currentCoins + coinReward);
        }

        String rewardText = "Osvojili ste: " + coinReward + " novčića!";
        if (!equipmentReward.isEmpty()) {
            rewardText += "\nDobili ste: " + equipmentReward;
        }
        textViewRewards.setText(rewardText);

        showRewardUI();
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
    }

    private void showRewardUI() {
        lottieAnimationChest.setVisibility(View.VISIBLE);
        textViewShake.setVisibility(View.VISIBLE);
    }
}