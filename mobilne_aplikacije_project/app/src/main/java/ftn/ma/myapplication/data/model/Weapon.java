package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Klasa za oružje koje daje trajne bonuse
 */
@Entity(tableName = "weapons")
public class Weapon extends Equipment {
    
    @PrimaryKey(autoGenerate = true)
    private int weaponId;
    
    // Equipment polja koja moraju biti u Room entitetu
    private String name;
    private String description;
    private int cost;
    private boolean isActive;
    private int quantity;
    
    private WeaponType weaponType;
    private double bonusPercentage;
    private int upgradeLevel; // Nivo unapređenja
    private static final double UPGRADE_BONUS = 0.01; // Svako unapređenje dodaje 0.01%
    private static final double DUPLICATE_BONUS = 0.02; // Duplikat dodaje 0.02%
    
    public enum WeaponType {
        SWORD(5.0, "Mač", "Trajno povećava snagu za 5%"),
        BOW(5.0, "Luk i strela", "Trajno povećava dobijeni novac za 5%");
        
        private final double basePercentage;
        private final String displayName;
        private final String description;
        
        WeaponType(double basePercentage, String displayName, String description) {
            this.basePercentage = basePercentage;
            this.displayName = displayName;
            this.description = description;
        }
        
        public double getBasePercentage() { return basePercentage; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public Weapon() {
        super();
        this.isActive = false;
        this.quantity = 0;
        this.upgradeLevel = 0;
        this.bonusPercentage = 0.0;
    }
    
    @Ignore
    public Weapon(WeaponType weaponType) {
        super();
        
        this.name = weaponType.getDisplayName();
        this.description = weaponType.getDescription();
        this.cost = 0; // Oružje se ne kupuje, dobija se od bossa
        this.isActive = false;
        this.quantity = 1; // Uvek imamo samo jedno oružje istog tipa
              
        this.weaponType = weaponType;
        this.bonusPercentage = weaponType.getBasePercentage();
        this.upgradeLevel = 0;
    }
    
    @Override
    public void applyEffect(User user) {
        // Oružje je uvek aktivno kada se poseduje
        setActive(true);
        
        // Efekti oružja se primenjuju automatski u battle sistemu
        // ili kada se rade kalkulacije
    }
    
    @Override
    public void removeEffect(User user) {
        // Oružje se ne uklanja, ali može se resetovati
        setActive(false);
        setBonusPercentage(0.0);
        setUpgradeLevel(0);
    }
    
    /**
     * Dodaje duplikat oružja (dodaje 0.02% bonus)
     */
    public void addDuplicate() {
        this.bonusPercentage += DUPLICATE_BONUS;
        setActive(true);
    }
    
    /**
     * Unapređuje oružje za jedan nivo
     */
    public boolean upgrade(int userCoins, int userLevel) {
        int upgradeCost = calculateUpgradeCost(userLevel);
        
        if (userCoins >= upgradeCost) {
            this.upgradeLevel++;
            this.bonusPercentage += UPGRADE_BONUS;
            return true; // Uspešno unapređeno
        }
        return false; // Nedovoljno novčića
    }
    
    /**
     * Računa cenu unapređenja na osnovu korisničkog nivoa
     */
    public int calculateUpgradeCost(int userLevel) {
        int previousLevelReward = Math.max(1, userLevel - 1) * 20; // Boss reward formula
        return (int)(previousLevelReward * 0.6); // 60% od nagrade
    }
    
    /**
     * Računa bonus za Power Points (samo mač)
     */
    public int calculatePowerBonus(int basePP) {
        if (weaponType == WeaponType.SWORD && isActive()) {
            return (int)(basePP * (bonusPercentage / 100.0));
        }
        return 0;
    }
    
    /**
     * Računa bonus za novac (samo luk)
     */
    public int calculateCoinBonus(int baseCoins) {
        if (weaponType == WeaponType.BOW && isActive()) {
            return (int)(baseCoins * (bonusPercentage / 100.0));
        }
        return 0;
    }
    
    /**
     * Vraća informacije o oružju za prikaz
     */
    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append(getName());
        info.append(" (Nivo ").append(upgradeLevel).append(")");
        info.append("\nBonus: ").append(String.format("%.2f", bonusPercentage)).append("%");
        
        if (weaponType == WeaponType.SWORD) {
            info.append("\nEfekat: Povećava snagu");
        } else if (weaponType == WeaponType.BOW) {
            info.append("\nEfekat: Povećava dobijeni novac");
        }
        
        return info.toString();
    }
    
    // Getters and Setters
    public int getWeaponId() { return weaponId; }
    public void setWeaponId(int weaponId) { this.weaponId = weaponId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public WeaponType getWeaponType() { return weaponType; }
    public void setWeaponType(WeaponType weaponType) { this.weaponType = weaponType; }
    
    public double getBonusPercentage() { return bonusPercentage; }
    public void setBonusPercentage(double bonusPercentage) { this.bonusPercentage = bonusPercentage; }
    
    public int getUpgradeLevel() { return upgradeLevel; }
    public void setUpgradeLevel(int upgradeLevel) { this.upgradeLevel = upgradeLevel; }
}