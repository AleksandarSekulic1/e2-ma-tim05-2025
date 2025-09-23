package ftn.ma.myapplication.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.UserStorage;
import ftn.ma.myapplication.data.model.User;
import ftn.ma.myapplication.data.model.EquipmentManager;
import ftn.ma.myapplication.data.model.Weapon;

/**
 * Aktivnost za borbu sa bosom sa equipment efektima
 */
public class BossBattleActivity extends AppCompatActivity {
    
    private TextView bossNameText;
    private TextView bossLevelText;
    private ProgressBar bossHealthBar;
    private TextView bossHealthText;
    
    private TextView playerStatsText;
    private TextView battleLogText;
    private Button attackButton;
    private Button useEquipmentButton;
    
    private User player;
    private Boss currentBoss;
    private EquipmentManager equipmentManager;
    private Random random;
    
    // Battle state
    private int playerPowerPoints;
    private double attackChanceBonus;
    private double extraAttackChance;
    private boolean battleEnded = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boss_battle);
        
        initViews();
        initBattle();
        loadEquipmentBonuses();
    }
    
    private void initViews() {
        bossNameText = findViewById(R.id.bossNameText);
        bossLevelText = findViewById(R.id.bossLevelText);
        bossHealthBar = findViewById(R.id.bossHealthBar);
        bossHealthText = findViewById(R.id.bossHealthText);
        
        playerStatsText = findViewById(R.id.playerStatsText);
        battleLogText = findViewById(R.id.battleLogText);
        attackButton = findViewById(R.id.attackButton);
        useEquipmentButton = findViewById(R.id.useEquipmentButton);
        
        attackButton.setOnClickListener(v -> performAttack());
        useEquipmentButton.setOnClickListener(v -> showEquipmentOptions());
    }
    
    private void initBattle() {
        player = UserStorage.getUser(this);
        equipmentManager = new EquipmentManager(this);
        random = new Random();
        
        // Kreiraj bossa na osnovu korisničkog nivoa
        currentBoss = new Boss(player.getLevel());
        
        // Postavi UI
        bossNameText.setText(currentBoss.getName());
        bossLevelText.setText("Nivo: " + currentBoss.getLevel());
        updateBossHealth();
        
        battleLogText.setText("Borba počinje! Pripremi se za bitku sa " + currentBoss.getName() + "!");
    }
    
    private void loadEquipmentBonuses() {
        // Učitaj power points bonuse
        equipmentManager.calculateTotalPowerBonus(player, new EquipmentManager.PowerBonusCallback() {
            @Override
            public void onCalculated(int bonus) {
                runOnUiThread(() -> {
                    playerPowerPoints = player.getPowerPoints() + bonus;
                    updatePlayerStats();
                });
            }
            
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    playerPowerPoints = player.getPowerPoints();
                    updatePlayerStats();
                });
            }
        });
        
        // Učitaj attack chance bonus
        equipmentManager.calculateAttackChanceBonus(new EquipmentManager.AttackBonusCallback() {
            @Override
            public void onCalculated(double bonus) {
                runOnUiThread(() -> {
                    attackChanceBonus = bonus;
                    updatePlayerStats();
                });
            }
            
            @Override
            public void onError(Exception e) {
                attackChanceBonus = 0.0;
            }
        });
        
        // Učitaj extra attack chance
        equipmentManager.calculateExtraAttackChance(new EquipmentManager.AttackBonusCallback() {
            @Override
            public void onCalculated(double bonus) {
                runOnUiThread(() -> {
                    extraAttackChance = bonus;
                    updatePlayerStats();
                });
            }
            
            @Override
            public void onError(Exception e) {
                extraAttackChance = 0.0;
            }
        });
    }
    
    private void updatePlayerStats() {
        String stats = String.format(
            "Snaga: %d PP (+%.0f%% bonus)\\n" +
            "Šansa napada: %.1f%% (+%.1f%%)\\n" +
            "Dodatni napad: %.1f%%",
            playerPowerPoints, 
            ((double)(playerPowerPoints - player.getPowerPoints()) / player.getPowerPoints()) * 100,
            70.0 + attackChanceBonus, attackChanceBonus,
            extraAttackChance
        );
        playerStatsText.setText(stats);
    }
    
    private void updateBossHealth() {
        bossHealthBar.setProgress((int)((double)currentBoss.getCurrentHealth() / currentBoss.getMaxHealth() * 100));
        bossHealthText.setText(currentBoss.getCurrentHealth() + " / " + currentBoss.getMaxHealth());
    }
    
    private void performAttack() {
        if (battleEnded) return;
        
        // Proveri šansu napada (sa bonusom od štita)
        double hitChance = 70.0 + attackChanceBonus;
        boolean hit = random.nextDouble() * 100 < hitChance;
        
        if (hit) {
            // Napravi štetu sa power points bonusima
            int damage = calculateDamage();
            currentBoss.takeDamage(damage);
            
            addToBattleLog("Pogodio si bossa za " + damage + " štete!");
            
            // Proveri za dodatni napad (čizme)
            if (random.nextDouble() * 100 < extraAttackChance) {
                int extraDamage = calculateDamage() / 2; // Polovična šteta
                currentBoss.takeDamage(extraDamage);
                addToBattleLog("DODATNI NAPAD! Još " + extraDamage + " štete!");
            }
            
        } else {
            addToBattleLog("Promašio si!");
        }
        
        updateBossHealth();
        
        // Proveri da li je boss poražen
        if (currentBoss.getCurrentHealth() <= 0) {
            handleBossDefeated();
        } else {
            // Boss napada nazad
            handleBossAttack();
        }
    }
    
    private int calculateDamage() {
        // Osnovna šteta + random factor + equipment bonusi
        int baseDamage = playerPowerPoints / 4;
        int randomFactor = random.nextInt(playerPowerPoints / 8) + 1;
        return baseDamage + randomFactor;
    }
    
    private void handleBossAttack() {
        // Boss napada igrača (jednostavna logika)
        int bossAttack = currentBoss.getAttackPower();
        addToBattleLog("Boss te napada za " + bossAttack + " štete!");
        
        // TODO: Implementirati player health sistem ako je potrebno
    }
    
    private void handleBossDefeated() {
        battleEnded = true;
        attackButton.setEnabled(false);
        
        addToBattleLog("POBEDA! Porazili ste " + currentBoss.getName() + "!");
        
        // Daj nagrade
        int xpReward = currentBoss.getLevel() * 50;
        int coinReward = currentBoss.getLevel() * 20;
        
        // Primeni coin bonus od luka
        equipmentManager.calculateCoinBonus(coinReward, new EquipmentManager.CoinBonusCallback() {
            @Override
            public void onCalculated(int bonus) {
                runOnUiThread(() -> {
                    int totalCoins = coinReward + bonus;
                    giveRewards(xpReward, totalCoins);
                    if (bonus > 0) {
                        addToBattleLog("Luk bonus: +" + bonus + " novčića!");
                    }
                });
            }
            
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> giveRewards(xpReward, coinReward));
            }
        });
        
        // Možda dodeli oružje
        giveRandomWeapon();
        
        // Završi battle efekte
        finishBattleEffects();
    }
    
    private void giveRewards(int xp, int coins) {
        player.addXP(xp);
        player.setCoins(player.getCoins() + coins);
        UserStorage.saveUser(this, player);
        
        addToBattleLog("Nagrade: +" + xp + " XP, +" + coins + " novčića");
    }
    
    private void giveRandomWeapon() {
        // 30% šanse za oružje
        if (random.nextDouble() < 0.3) {
            Weapon.WeaponType[] types = Weapon.WeaponType.values();
            Weapon.WeaponType randomType = types[random.nextInt(types.length)];
            
            equipmentManager.addBossWeapon(randomType, new EquipmentManager.WeaponRewardCallback() {
                @Override
                public void onNewWeaponAdded(Weapon weapon) {
                    runOnUiThread(() -> {
                        addToBattleLog("NOVO ORUŽJE! Dobili ste: " + weapon.getName());
                    });
                }
                
                @Override
                public void onDuplicateAdded(Weapon weapon) {
                    runOnUiThread(() -> {
                        addToBattleLog("Duplikat oružja! " + weapon.getName() + 
                                     " je pojačan (+0.02% bonus)");
                    });
                }
                
                @Override
                public void onError(Exception e) {
                    // Ignore errors for weapon rewards
                }
            });
        }
    }
    
    private void finishBattleEffects() {
        // Smanji trajanje odeće
        equipmentManager.decreaseClothingDuration(new EquipmentManager.BattleEndCallback() {
            @Override
            public void onCompleted() {
                // OK
            }
            
            @Override
            public void onError(Exception e) {
                // Ignore
            }
        });
        
        // Potrosi jednokratne napitke
        equipmentManager.consumeTemporaryPotions(new EquipmentManager.BattleEndCallback() {
            @Override
            public void onCompleted() {
                runOnUiThread(() -> {
                    addToBattleLog("Jednokratni napici su potrošeni.");
                });
            }
            
            @Override
            public void onError(Exception e) {
                // Ignore
            }
        });
    }
    
    private void showEquipmentOptions() {
        // TODO: Implementirati quick equipment panel tokom borbe
        Toast.makeText(this, "Equipment panel - uskoro!", Toast.LENGTH_SHORT).show();
    }
    
    private void addToBattleLog(String message) {
        String currentLog = battleLogText.getText().toString();
        String newLog = currentLog + "\\n" + message;
        battleLogText.setText(newLog);
        
        // Scroll to bottom (if in ScrollView)
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (equipmentManager != null) {
            equipmentManager.shutdown();
        }
    }
    
    /**
     * Klasa za predstavljanje bossa
     */
    private static class Boss {
        private final String name;
        private final int level;
        private final int maxHealth;
        private int currentHealth;
        private final int attackPower;
        
        public Boss(int playerLevel) {
            this.level = Math.max(1, playerLevel);
            this.name = "Boss Nivo " + level;
            this.maxHealth = level * 100 + 200;
            this.currentHealth = maxHealth;
            this.attackPower = level * 10 + 20;
        }
        
        public void takeDamage(int damage) {
            currentHealth = Math.max(0, currentHealth - damage);
        }
        
        // Getters
        public String getName() { return name; }
        public int getLevel() { return level; }
        public int getMaxHealth() { return maxHealth; }
        public int getCurrentHealth() { return currentHealth; }
        public int getAttackPower() { return attackPower; }
    }
}