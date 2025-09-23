package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Klasa za odeću koja daje bonuse tokom dve borbe
 */
@Entity(tableName = "clothing")
public class Clothing extends Equipment {
    
    @PrimaryKey(autoGenerate = true)
    private int clothingId;
    
    // Equipment polja koja moraju biti u Room entitetu
    private String name;
    private String description;
    private int cost;
    private boolean isActive;
    private int quantity;
    
    private ClothingType clothingType;
    private double bonusPercentage;
    private int battlesRemaining; // Koliko borbi još traje (max 2)
    
    public enum ClothingType {
        GLOVES(10.0, "Rukavice", "Povećavaju snagu za 10%"),
        SHIELD(10.0, "Štit", "Povećavaju šansu uspešnog napada za 10%"), 
        BOOTS(40.0, "Čizme", "40% šanse za dodatni napad");
        
        private final double basePercentage;
        private final String displayName;
        private final String description;
        
        ClothingType(double basePercentage, String displayName, String description) {
            this.basePercentage = basePercentage;
            this.displayName = displayName;
            this.description = description;
        }
        
        public double getBasePercentage() { return basePercentage; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public Clothing() {
        super();
        this.isActive = false;
        this.quantity = 0;
        this.battlesRemaining = 0;
    }
    
    @Ignore
    public Clothing(ClothingType clothingType) {
        super();
        
        this.name = clothingType.getDisplayName();
        this.description = clothingType.getDescription();
        this.cost = calculateCost(clothingType);
        this.isActive = false;
        this.quantity = 0;
              
        this.clothingType = clothingType;
        this.bonusPercentage = clothingType.getBasePercentage();
        this.battlesRemaining = 0; // Postavlja se na 2 kada se aktivira
    }
    
    /**
     * Računa cenu odeće na osnovu tipa
     */
    private static int calculateCost(ClothingType type) {
        // Cena se računa kao procenat od boss nagrade prethodnog nivoa
        switch (type) {
            case GLOVES:
            case SHIELD:
                return 120; // 60% od nagrade (placeholder)
            case BOOTS:
                return 160; // 80% od nagrade (placeholder)
            default:
                return 100;
        }
    }
    
    /**
     * Računa stvarnu cenu na osnovu korisničkog nivoa
     */
    public void updateCostForLevel(int userLevel) {
        int previousLevelReward = Math.max(1, userLevel - 1) * 20; // Boss reward formula
        
        switch (clothingType) {
            case GLOVES:
            case SHIELD:
                setCost((int)(previousLevelReward * 0.6)); // 60%
                break;
            case BOOTS:
                setCost((int)(previousLevelReward * 0.8)); // 80%
                break;
        }
    }
    
    @Override
    public void applyEffect(User user) {
        if (!isActive()) {
            setActive(true);
            setBattlesRemaining(2); // Odeća traje 2 borbe
            
            // Ako korisnik već ima istu odeću, saberi bonuse
            // Ova logika će biti implementirana u User klasi
        }
    }
    
    @Override
    public void removeEffect(User user) {
        if (isActive()) {
            setActive(false);
            setBattlesRemaining(0);
            // Ukloni jedan deo odeće iz inventara
            setQuantity(Math.max(0, getQuantity() - 1));
        }
    }
    
    /**
     * Smanjuje broj preostalih borbi nakon svake borbe
     */
    public void decreaseBattleCount() {
        if (isActive() && battlesRemaining > 0) {
            battlesRemaining--;
            if (battlesRemaining <= 0) {
                setActive(false);
                // Ukloni jedan deo iz inventara
                setQuantity(Math.max(0, getQuantity() - 1));
            }
        }
    }
    
    /**
     * Kombinuje bonuse sa istom odeću
     */
    public void combineBonusWith(Clothing other) {
        if (other.clothingType == this.clothingType) {
            this.bonusPercentage += other.bonusPercentage;
        }
    }
    
    /**
     * Računa bonus za Power Points (samo rukavice)
     */
    public int calculatePowerBonus(int basePP) {
        if (clothingType == ClothingType.GLOVES && isActive()) {
            return (int)(basePP * (bonusPercentage / 100.0));
        }
        return 0;
    }
    
    /**
     * Računa bonus za šansu napada (štit)
     */
    public double calculateAttackChanceBonus() {
        if (clothingType == ClothingType.SHIELD && isActive()) {
            return bonusPercentage;
        }
        return 0.0;
    }
    
    /**
     * Računa šansu za dodatni napad (čizme)
     */
    public double calculateExtraAttackChance() {
        if (clothingType == ClothingType.BOOTS && isActive()) {
            return bonusPercentage;
        }
        return 0.0;
    }
    
    // Getters and Setters
    public int getClothingId() { return clothingId; }
    public void setClothingId(int clothingId) { this.clothingId = clothingId; }
    
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
    
    public ClothingType getClothingType() { return clothingType; }
    public void setClothingType(ClothingType clothingType) { this.clothingType = clothingType; }
    
    public double getBonusPercentage() { return bonusPercentage; }
    public void setBonusPercentage(double bonusPercentage) { this.bonusPercentage = bonusPercentage; }
    
    public int getBattlesRemaining() { return battlesRemaining; }
    public void setBattlesRemaining(int battlesRemaining) { this.battlesRemaining = battlesRemaining; }
}