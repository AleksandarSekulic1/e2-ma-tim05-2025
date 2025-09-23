package ftn.ma.myapplication.data.model;

public class User {
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
    // Ostali podaci: broj bedževa, lista bedževa, oprema, QR kod itd.

    public User(String email, String username, String passwordHash, int avatarIndex, boolean isActive, long activationExpiry) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.avatarIndex = avatarIndex;
        this.isActive = isActive;
        this.activationExpiry = activationExpiry;
        this.level = 1;
        this.title = "Početnik";
        this.powerPoints = 0;
        this.xp = 0;
        this.coins = 0;
    }

    // Getteri i setteri
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

    public void setActive(boolean active) { isActive = active; }
    public void setActivationExpiry(long expiry) { activationExpiry = expiry; }
    public void setLevel(int level) { this.level = level; }
    public void setTitle(String title) { this.title = title; }
    public void setPowerPoints(int pp) { this.powerPoints = pp; }
    public void setXp(int xp) { this.xp = xp; }
    public void setCoins(int coins) { this.coins = coins; }
}
