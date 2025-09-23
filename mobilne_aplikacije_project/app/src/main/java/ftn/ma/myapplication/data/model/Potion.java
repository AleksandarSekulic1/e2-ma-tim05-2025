package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Klasa za napitke koji daju bonuse korisniku
 */
@Entity(tableName = "potions")
public class Potion extends Equipment {
    
    @PrimaryKey(autoGenerate = true)
    private int potionId;
    
    // Equipment polja koja moraju biti u Room entitetu
    private String name;
    private String description;
    private int cost;
    private boolean isActive;
    private int quantity;
    
    private PotionEffect effect;
    private double bonusPercentage;
    private boolean isPermanent;
    private boolean isConsumed; // Da li je potrošen u borbi
    
    public enum PotionEffect {
        POWER_BOOST_20(20.0, false, "PP +20%"), // Jednokratno povećanje 20% PP
        POWER_BOOST_40(40.0, false, "PP +40%"), // Jednokratno povećanje 40% PP  
        PERMANENT_POWER_5(5.0, true, "Trajno PP +5%"), // Trajno povećanje 5% PP
        PERMANENT_POWER_10(10.0, true, "Trajno PP +10%"); // Trajno povećanje 10% PP
        
        private final double percentage;
        private final boolean permanent;
        private final String displayName;
        
        PotionEffect(double percentage, boolean permanent, String displayName) {
            this.percentage = percentage;
            this.permanent = permanent;
            this.displayName = displayName;
        }
        
        public double getPercentage() { return percentage; }
        public boolean isPermanent() { return permanent; }
        public String getDisplayName() { return displayName; }
    }
    
    public Potion() {
        super();
        this.isActive = false;
        this.quantity = 0;
    }
    
    @Ignore
    public Potion(PotionEffect effect) {
        super();
        
        this.name = effect.getDisplayName();
        this.description = "Napitak koji " + (effect.isPermanent() ? "trajno " : "jednokratno ") + 
                          "povećava snagu za " + effect.getPercentage() + "%";
        this.cost = calculateCost(effect);
        this.isActive = false;
        this.quantity = 0;
        
        this.effect = effect;
        this.bonusPercentage = effect.getPercentage();
        this.isPermanent = effect.isPermanent();
        this.isConsumed = false;
    }
    
    /**
     * Računa cenu napitka na osnovu trenutnog korisničkog nivoa
     */
    private static int calculateCost(PotionEffect effect) {
        // Cena se računa kao procenat od boss nagrade prethodnog nivoa
        // Boss nagrada je 20 novčića po nivou, tako da je za nivo N-1 to (N-1)*20
        // Ova logika će biti ažurirana kada se pozove iz konteksta gde znamo nivo
        switch (effect) {
            case POWER_BOOST_20:
                return 100; // 50% od nagrade (placeholder)
            case POWER_BOOST_40: 
                return 140; // 70% od nagrade (placeholder)
            case PERMANENT_POWER_5:
                return 400; // 200% od nagrade (placeholder)
            case PERMANENT_POWER_10:
                return 2000; // 1000% od nagrade (placeholder)
            default:
                return 100;
        }
    }
    
    /**
     * Računa stvarnu cenu na osnovu korisničkog nivoa
     */
    public void updateCostForLevel(int userLevel) {
        int previousLevelReward = Math.max(1, userLevel - 1) * 20; // Boss reward formula
        
        switch (effect) {
            case POWER_BOOST_20:
                setCost((int)(previousLevelReward * 0.5)); // 50%
                break;
            case POWER_BOOST_40:
                setCost((int)(previousLevelReward * 0.7)); // 70%
                break;
            case PERMANENT_POWER_5:
                setCost(previousLevelReward * 2); // 200%
                break;
            case PERMANENT_POWER_10:
                setCost(previousLevelReward * 10); // 1000%
                break;
        }
    }
    
    @Override
    public void applyEffect(User user) {
        if (isPermanent) {
            // Trajni efekti se dodaju na bazne PP
            int currentBasePP = user.getPowerPoints();
            int bonusPP = (int)(currentBasePP * (bonusPercentage / 100.0));
            int newPP = currentBasePP + bonusPP;
            user.setPowerPoints(newPP);
            
            // Debug log
            System.out.println("TRAJNI NAPITAK: " + getName() + 
                             " | Stari PP: " + currentBasePP + 
                             " | Bonus: " + bonusPP + 
                             " | Novi PP: " + newPP);
        } else {
            // Jednokratni efekti se primenjuju u borbi
            // Ova logika će biti implementirana u battle sistemu
            setActive(true);
            
            // Debug log
            System.out.println("JEDNOKRATNI NAPITAK: " + getName() + 
                             " | Aktiviran za sledeću borbu");
        }
    }
    
    @Override
    public void removeEffect(User user) {
        if (!isPermanent && isActive()) {
            setActive(false);
            setConsumed(true);
            // Smanji količinu nakon potrošnje
            setQuantity(Math.max(0, getQuantity() - 1));
        }
    }
    
    /**
     * Računa bonus PP za jednokratne napitke
     */
    public int calculateBonusPP(int basePP) {
        if (!isPermanent && isActive() && !isConsumed) {
            return (int)(basePP * (bonusPercentage / 100.0));
        }
        return 0;
    }
    
    // Getters and Setters
    public int getPotionId() { return potionId; }
    public void setPotionId(int potionId) { this.potionId = potionId; }
    
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
    
    public PotionEffect getEffect() { return effect; }
    public void setEffect(PotionEffect effect) { this.effect = effect; }
    
    public double getBonusPercentage() { return bonusPercentage; }
    public void setBonusPercentage(double bonusPercentage) { this.bonusPercentage = bonusPercentage; }
    
    public boolean isPermanent() { return isPermanent; }
    public void setPermanent(boolean permanent) { isPermanent = permanent; }
    
    public boolean isConsumed() { return isConsumed; }
    public void setConsumed(boolean consumed) { isConsumed = consumed; }
}