package ftn.ma.myapplication.data.model;

import android.content.Context;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ftn.ma.myapplication.data.local.AppDatabase;

/**
 * Manager klasa za upravljanje equipment efektima i bonusima
 */
public class EquipmentManager {
    
    private final AppDatabase database;
    private final ExecutorService executor;
    
    public EquipmentManager(Context context) {
        this.database = AppDatabase.getDatabase(context);
        this.executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * Računa ukupne Power Points bonuse od aktivne opreme
     */
    public void calculateTotalPowerBonus(User user, PowerBonusCallback callback) {
        executor.execute(() -> {
            try {
                int totalBonus = 0;
                
                // Bonus od napitaka
                totalBonus += calculatePotionPowerBonus(user);
                
                // Bonus od odeće (rukavice)
                totalBonus += calculateClothingPowerBonus();
                
                // Bonus od oružja (mač)
                totalBonus += calculateWeaponPowerBonus();
                
                callback.onCalculated(totalBonus);
                
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    /**
     * Računa bonus od aktivnih napitaka
     */
    private int calculatePotionPowerBonus(User user) {
        try {
            List<Potion> activePotions = database.equipmentDao().getTemporaryActivePotions();
            int totalBonus = 0;
            
            for (Potion potion : activePotions) {
                if (!potion.isConsumed()) {
                    totalBonus += potion.calculateBonusPP(user.getPowerPoints());
                }
            }
            
            return totalBonus;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Računa bonus od aktivnih rukavica
     */
    private int calculateClothingPowerBonus() {
        try {
            List<Clothing> activeGloves = database.equipmentDao()
                .getActiveClothingByType(Clothing.ClothingType.GLOVES);
            
            int totalBonus = 0;
            for (Clothing gloves : activeGloves) {
                if (gloves.getBattlesRemaining() > 0) {
                    // Saberi sve aktivne rukavice
                    totalBonus += (int)(gloves.getBonusPercentage());
                }
            }
            
            return totalBonus;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Računa bonus od aktivnog mača
     */
    private int calculateWeaponPowerBonus() {
        try {
            Weapon sword = database.equipmentDao()
                .getWeaponByType(Weapon.WeaponType.SWORD);
            
            if (sword != null && sword.isActive()) {
                return (int)sword.getBonusPercentage();
            }
            
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Računa bonus šanse napada od štita
     */
    public void calculateAttackChanceBonus(AttackBonusCallback callback) {
        executor.execute(() -> {
            try {
                List<Clothing> activeShields = database.equipmentDao()
                    .getActiveClothingByType(Clothing.ClothingType.SHIELD);
                
                double totalBonus = 0.0;
                for (Clothing shield : activeShields) {
                    if (shield.getBattlesRemaining() > 0) {
                        totalBonus += shield.getBonusPercentage();
                    }
                }
                
                callback.onCalculated(totalBonus);
                
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    /**
     * Računa šansu za dodatni napad od čizama
     */
    public void calculateExtraAttackChance(AttackBonusCallback callback) {
        executor.execute(() -> {
            try {
                List<Clothing> activeBoots = database.equipmentDao()
                    .getActiveClothingByType(Clothing.ClothingType.BOOTS);
                
                double totalChance = 0.0;
                for (Clothing boots : activeBoots) {
                    if (boots.getBattlesRemaining() > 0) {
                        totalChance += boots.getBonusPercentage();
                    }
                }
                
                callback.onCalculated(totalChance);
                
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    /**
     * Računa bonus novčića od luka
     */
    public void calculateCoinBonus(int baseCoins, CoinBonusCallback callback) {
        executor.execute(() -> {
            try {
                Weapon bow = database.equipmentDao()
                    .getWeaponByType(Weapon.WeaponType.BOW);
                
                int bonus = 0;
                if (bow != null && bow.isActive()) {
                    bonus = bow.calculateCoinBonus(baseCoins);
                }
                
                callback.onCalculated(bonus);
                
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    /**
     * Smanjuje trajanje odeće nakon borbe
     */
    public void decreaseClothingDuration(BattleEndCallback callback) {
        executor.execute(() -> {
            try {
                // Smanji broj borbi za svu aktivnu odeću
                database.equipmentDao().decreaseAllClothingBattleCount();
                
                // Deaktiviraj odeću kojoj je isteklo trajanje
                database.equipmentDao().deactivateExpiredClothing();
                
                callback.onCompleted();
                
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    /**
     * Potrosi jednokratne napitke nakon borbe
     */
    public void consumeTemporaryPotions(BattleEndCallback callback) {
        executor.execute(() -> {
            try {
                // Potrosi sve aktivne jednokratne napitke
                database.equipmentDao().consumeTemporaryPotions();
                
                callback.onCompleted();
                
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    /**
     * Dodaje oružje dobijeno od bossa
     */
    public void addBossWeapon(Weapon.WeaponType type, WeaponRewardCallback callback) {
        executor.execute(() -> {
            try {
                Weapon existingWeapon = database.equipmentDao()
                    .getWeaponByType(type);
                
                if (existingWeapon != null) {
                    // Dodaj duplikat bonus
                    existingWeapon.addDuplicate();
                    database.equipmentDao().updateWeapon(existingWeapon);
                    callback.onDuplicateAdded(existingWeapon);
                } else {
                    // Dodaj novo oružje
                    Weapon newWeapon = new Weapon(type);
                    newWeapon.setActive(true);
                    database.equipmentDao().insertWeapon(newWeapon);
                    callback.onNewWeaponAdded(newWeapon);
                }
                
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }
    
    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    // Callback interfejsi
    public interface PowerBonusCallback {
        void onCalculated(int bonus);
        void onError(Exception e);
    }
    
    public interface AttackBonusCallback {
        void onCalculated(double bonus);
        void onError(Exception e);
    }
    
    public interface CoinBonusCallback {
        void onCalculated(int bonus);
        void onError(Exception e);
    }
    
    public interface BattleEndCallback {
        void onCompleted();
        void onError(Exception e);
    }
    
    public interface WeaponRewardCallback {
        void onNewWeaponAdded(Weapon weapon);
        void onDuplicateAdded(Weapon weapon);
        void onError(Exception e);
    }
}