package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String email;
    private String username;
    private String passwordHash;
    private int avatarIndex;
    private boolean isActive;
    private long activationExpiry;
    private int level;
    private String title;
    private int powerPoints;
    private int xp;
    private int coins;
    private String equipment; // Oprema koju korisnik trenutno nosi
    
    // Level sistem konstante
    private static final int FIRST_LEVEL_XP = 200;
    private static final int BASE_IMPORTANCE_XP = 10;
    private static final int BASE_DIFFICULTY_XP = 5;
    private static final int BOSS_REWARD_COINS = 50;
    
    // PP (Power Points) sistem konstante
    private static final int FIRST_LEVEL_UP_PP = 40; // PP za prelazak na nivo 2
    
    // Ostali podaci: broj bedževa, lista bedževa, QR kod itd.

    public User() {}

    @Ignore
    public User(String email, String username, String passwordHash, int avatarIndex, boolean isActive, long activationExpiry) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.avatarIndex = avatarIndex;
        this.isActive = isActive;
        this.activationExpiry = activationExpiry;
        this.level = 0; // Počinje sa nivoom 0
        this.title = "Početnik";
        this.powerPoints = 0;
        this.xp = 0;
        this.coins = 0;
        this.equipment = "Osnovna oprema"; // Početna oprema
    }

    // Getteri i setteri
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public int getAvatarIndex() { return avatarIndex; }
    public boolean isActive() { return isActive; }
    public long getActivationExpiry() { return activationExpiry; }
    public int getLevel() { return level; }
    public String getTitle() { return title; }
    public int getPowerPoints() { return powerPoints; }
    public int getXp() { return xp; }
    public int getCoins() { return coins; }
    public String getEquipment() { return equipment; }

    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setAvatarIndex(int avatarIndex) { this.avatarIndex = avatarIndex; }
    public void setActive(boolean active) { isActive = active; }
    public void setActivationExpiry(long expiry) { activationExpiry = expiry; }
    public void setLevel(int level) { this.level = level; }
    public void setTitle(String title) { this.title = title; }
    public void setPowerPoints(int pp) { this.powerPoints = pp; }
    public void setXp(int xp) { this.xp = xp; }
    public void setCoins(int coins) { this.coins = coins; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    
    // Level progression metode
    
    /**
     * Računa koliko XP je potrebno za određeni nivo
     * Nivo 0: 0 XP (početak)
     * Nivo 1: 200 XP
     * Sledeći nivoi: prethodni * 2 + prethodni / 2 (zaokruženo na stotinu)
     */
    public static int getXPRequiredForLevel(int level) {
        if (level <= 0) return 0; // Nivo 0 ne treba XP
        if (level == 1) return FIRST_LEVEL_XP; // Nivo 1 treba 200 XP
        
        int previousXP = getXPRequiredForLevel(level - 1);
        int nextXP = previousXP * 2 + previousXP / 2;
        
        // Zaokruži na prvu narednu stotinu
        return ((nextXP + 99) / 100) * 100;
    }
    
    /**
     * Računa ukupno XP potrebno za dostizanje određenog nivoa
     */
    public static int getTotalXPForLevel(int level) {
        int total = 0;
        for (int i = 1; i <= level; i++) { // Počinje od nivoa 1
            total += getXPRequiredForLevel(i);
        }
        return total;
    }
    
    /**
     * Računa trenutni nivo na osnovu XP-a
     */
    public int calculateCurrentLevel() {
        if (this.xp < FIRST_LEVEL_XP) return 0; // Manje od 200 XP = nivo 0
        
        int level = 0;
        int remainingXP = this.xp;
        
        // Oduzimaj XP potreban za svaki nivo dok ne ostane premalo
        while (remainingXP >= getXPRequiredForLevel(level + 1)) {
            level++;
            remainingXP -= getXPRequiredForLevel(level);
        }
        
        return level;
    }
    
    /**
     * Računa koliko XP fali do sledećeg nivoa
     */
    public int getXPToNextLevel() {
        int currentLevel = calculateCurrentLevel();
        int totalXPForCurrentLevel = getTotalXPForLevel(currentLevel);
        int xpNeededForNextLevel = getXPRequiredForLevel(currentLevel + 1);
        
        return totalXPForCurrentLevel + xpNeededForNextLevel - this.xp;
    }
    
    /**
     * Računa XP bonus za bitnost zadatka na trenutnom nivou
     */
    public int getImportanceXPMultiplier() {
        if (level <= 1) return BASE_IMPORTANCE_XP;
        
        int multiplier = BASE_IMPORTANCE_XP;
        for (int i = 2; i <= level; i++) {
            multiplier = multiplier + multiplier / 2;
        }
        return Math.round(multiplier);
    }
    
    /**
     * Računa XP bonus za težinu zadatka na trenutnom nivou
     */
    public int getDifficultyXPMultiplier() {
        if (level <= 1) return BASE_DIFFICULTY_XP;
        
        int multiplier = BASE_DIFFICULTY_XP;
        for (int i = 2; i <= level; i++) {
            multiplier = multiplier + multiplier / 2;
        }
        return Math.round(multiplier);
    }
    
    /**
     * Računa koliko PP treba dobiti za određeni nivo prema formuli:
     * Nivo 0-1: 0 PP
     * Nivo 2: 40 PP (prvi level up)
     * Sledeći nivoi: PP_prethodni + 3/4 * PP_prethodni
     */
    public static int getPPForLevel(int level) {
        if (level <= 1) return 0; // Nivo 0 i 1 nemaju PP
        if (level == 2) return FIRST_LEVEL_UP_PP; // Prvi level up (1→2) daje 40 PP
        
        // Za nivoe 3+: PP_prethodni + 3/4 * PP_prethodni
        int previousPP = getPPForLevel(level - 1);
        double newPP = previousPP + (3.0 / 4.0) * previousPP;
        return (int) Math.round(newPP);
    }
    
    /**
     * Računa ukupno PP koje korisnik treba da ima na određenom nivou
     */
    public static int getTotalPPForLevel(int level) {
        int total = 0;
        for (int i = 2; i <= level; i++) { // Počinje od nivoa 2
            total += getPPForLevel(i);
        }
        return total;
    }
    
    /**
     * Računa koliko PP korisnik treba da ima na svom trenutnom nivou
     */
    public int getExpectedPPForCurrentLevel() {
        return getTotalPPForLevel(this.level);
    }
    
    /**
     * Sinhronizuje PP sa trenutnim nivoom (za postojeće korisnike)
     * Poziva se kada se učita korisnik da osigura da ima tačan PP za svoj nivo
     */
    public void syncPPToCurrentLevel() {
        int expectedPP = getExpectedPPForCurrentLevel();
        this.powerPoints = expectedPP;
    }
    
    /**
     * Popravlja level i PP na osnovu trenutnog XP-a
     * Korisno za migraciju postojećih korisnika
     */
    public void recalculateLevelAndPP() {
        this.level = calculateCurrentLevel();
        syncPPToCurrentLevel();
        updateTitle();
    }
    
    /**
     * Dodaje XP i proverava da li je vreme za level up
     * @param xpAmount količina XP za dodavanje
     * @return true ako je došlo do level up-a
     */
    public boolean addXP(int xpAmount) {
        int oldLevel = this.level;
        this.xp += xpAmount;
        
        int newLevel = calculateCurrentLevel();
        if (newLevel > oldLevel) {
            levelUp(newLevel);
            return true;
        }
        
        return false;
    }
    
    /**
     * Izvršava level up i daje nagrade
     */
    private void levelUp(int newLevel) {
        int oldLevel = this.level;
        int levelsGained = newLevel - this.level;
        this.level = newLevel;
        
        // Dodeli PP za svaki preskočeni nivo
        int totalPPGained = 0;
        for (int level = oldLevel + 1; level <= newLevel; level++) {
            int ppForThisLevel = getPPForLevel(level);
            this.powerPoints += ppForThisLevel;
            totalPPGained += ppForThisLevel;
        }
        
        // Nagradi igrača novčićima za "pobedu nad bosom"
        int coinReward = levelsGained * BOSS_REWARD_COINS;
        this.coins += coinReward;
        
        // Ažuriraj titulu na osnovu nivoa
        updateTitle();
    }
    
    /**
     * Ažurira titulu na osnovu trenutnog nivoa
     */
    private void updateTitle() {
        if (level >= 50) {
            this.title = "Legenda";
        } else if (level >= 30) {
            this.title = "Majstor";
        } else if (level >= 20) {
            this.title = "Ekspert";
        } else if (level >= 10) {
            this.title = "Veteran";
        } else if (level >= 5) {
            this.title = "Napredni";
        } else {
            this.title = "Početnik";
        }
    }
    
    /**
     * Simulira dodavanje random XP-a za testiranje
     */
    public int addRandomXP() {
        int randomXP = (int) (Math.random() * 100) + 10; // 10-110 XP
        boolean leveledUp = addXP(randomXP);
        return randomXP;
    }
    
    // ==================== EQUIPMENT BONUS METHODS ====================
    
    /**
     * Računa ukupne Power Points sa svim aktivnim bonusima
     * Uključuje: napitke, odeću (rukavice), oružje (mač)
     */
    public int getTotalPowerPoints() {
        // TODO: Implementirati kada se doda Equipment integration
        // Ova metoda će biti pozivana iz battle sistema
        return this.powerPoints;
    }
    
    /**
     * Računa bonus PP od aktivnih napitaka
     */
    public int getPotionBonus() {
        // TODO: Pronaći aktivne napitke iz baze i sabrati bonuse
        return 0;
    }
    
    /**
     * Računa bonus PP od aktivne odeće (rukavice)
     */
    public int getClothingPowerBonus() {
        // TODO: Pronaći aktivne rukavice i sabrati bonuse
        return 0;
    }
    
    /**
     * Računa bonus PP od aktivnog oružja (mač)
     */
    public int getWeaponPowerBonus() {
        // TODO: Pronaći aktivni mač i vratiti bonus
        return 0;
    }
    
    /**
     * Računa bonus šanse za uspešan napad (štit)
     */
    public double getAttackChanceBonus() {
        // TODO: Pronaći aktivni štit i vratiti bonus
        return 0.0;
    }
    
    /**
     * Računa šansu za dodatni napad (čizme)
     */
    public double getExtraAttackChance() {
        // TODO: Pronaći aktivne čizme i vratiti bonus
        return 0.0;
    }
    
    /**
     * Računa bonus novčića od aktivnog oružja (luk)
     */
    public int getCoinBonus(int baseCoins) {
        // TODO: Pronaći aktivni luk i vratiti bonus
        return 0;
    }
    
    /**
     * Primenjuje equipment bonuse za borbu sa bosom
     */
    public BattleStats getBattleStats() {
        int totalPP = getTotalPowerPoints();
        double attackChance = 100.0 + getAttackChanceBonus(); // Bazna šansa + bonus
        double extraAttackChance = getExtraAttackChance();
        
        return new BattleStats(totalPP, attackChance, extraAttackChance);
    }
    
    /**
     * Klasa za držanje battle statistika sa bonusima
     */
    public static class BattleStats {
        private final int powerPoints;
        private final double attackChance;
        private final double extraAttackChance;
        
        public BattleStats(int powerPoints, double attackChance, double extraAttackChance) {
            this.powerPoints = powerPoints;
            this.attackChance = attackChance;
            this.extraAttackChance = extraAttackChance;
        }
        
        public int getPowerPoints() { return powerPoints; }
        public double getAttackChance() { return attackChance; }
        public double getExtraAttackChance() { return extraAttackChance; }
    }
}
