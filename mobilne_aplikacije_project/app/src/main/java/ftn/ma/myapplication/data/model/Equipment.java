package ftn.ma.myapplication.data.model;

/**
 * Bazna klasa za svu opremu u igri
 * NAPOMENA: Ova klasa nije Room entitet zbog abstract prirode
 */
public abstract class Equipment {
    private int id;
    
    private String name;
    private String description;
    private int cost; // Cena u novčićima
    private EquipmentType type;
    private boolean isActive;
    private int quantity; // Koliko korisnik ima
    
    public enum EquipmentType {
        POTION,
        CLOTHING, 
        WEAPON
    }
    
    public Equipment() {}
    
    public Equipment(String name, String description, int cost, EquipmentType type) {
        this.name = name;
        this.description = description;
        this.cost = cost;
        this.type = type;
        this.isActive = false;
        this.quantity = 0;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getCost() { return cost; }
    public void setCost(int cost) { this.cost = cost; }
    
    public EquipmentType getType() { return type; }
    public void setType(EquipmentType type) { this.type = type; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    /**
     * Abstraktna metoda za primenu efekata opreme
     */
    public abstract void applyEffect(User user);
    
    /**
     * Abstraktna metoda za uklanjanje efekata opreme
     */
    public abstract void removeEffect(User user);
}