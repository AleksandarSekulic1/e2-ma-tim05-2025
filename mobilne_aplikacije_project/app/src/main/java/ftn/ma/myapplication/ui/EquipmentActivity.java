package ftn.ma.myapplication.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.UserStorage;
import ftn.ma.myapplication.data.model.User;
import ftn.ma.myapplication.data.model.Potion;
import ftn.ma.myapplication.data.model.Clothing;
import ftn.ma.myapplication.data.model.Weapon;

/**
 * Aktivnost za upravljanje opremom - aktivaciju, deaktivaciju, prikaz
 */
public class EquipmentActivity extends AppCompatActivity {
    
    private TextView activeEquipmentHeader;
    private ListView potionsListView;
    private ListView clothingListView;
    private ListView weaponsListView;
    private Button upgradeWeaponsButton;
    private Button deleteAllEquipmentButton;
    private Button addCoinsButton;
    
    private EquipmentAdapter potionsAdapter;
    private EquipmentAdapter clothingAdapter;
    private EquipmentAdapter weaponsAdapter;
    
    private User currentUser;
    private AppDatabase database;
    private ExecutorService executor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equipment);
        
        initViews();
        initDatabase();
        loadUserData();
        loadEquipment();
    }
    
    private void initViews() {
        activeEquipmentHeader = findViewById(R.id.activeEquipmentHeader);
        potionsListView = findViewById(R.id.potionsListView);
        clothingListView = findViewById(R.id.clothingListView);
        weaponsListView = findViewById(R.id.weaponsListView);
        upgradeWeaponsButton = findViewById(R.id.upgradeWeaponsButton);
        deleteAllEquipmentButton = findViewById(R.id.deleteAllEquipmentButton);
        addCoinsButton = findViewById(R.id.addCoinsButton);
        
        upgradeWeaponsButton.setOnClickListener(v -> showWeaponUpgradeDialog());
        deleteAllEquipmentButton.setOnClickListener(v -> showDeleteAllEquipmentDialog());
        addCoinsButton.setOnClickListener(v -> addCoins());
    }
    
    private void initDatabase() {
        database = AppDatabase.getDatabase(this);
        executor = Executors.newSingleThreadExecutor();
    }
    
    private void loadUserData() {
        currentUser = UserStorage.getUser(this);
        if (currentUser != null) {
            updateActiveEquipmentHeader();
        }
    }
    
    private void updateActiveEquipmentHeader() {
        String headerText = "Oprema korisnika: " + currentUser.getUsername() + 
                           " (Nivo " + currentUser.getLevel() + ")";
        activeEquipmentHeader.setText(headerText);
    }
    
    private void loadEquipment() {
        loadPotions();
        loadClothing();
        loadWeapons();
    }
    
    private void loadPotions() {
        executor.execute(() -> {
            try {
                List<Potion> potions = database.equipmentDao().getAvailablePotions();
                List<EquipmentItem> potionItems = new ArrayList<>();
                
                for (Potion potion : potions) {
                    String status = potion.isActive() ? "AKTIVNO" : "NEAKTIVNO";
                    if (potion.isActive() && potion.isConsumed()) {
                        status = "POTROŠENO";
                    }
                    
                    EquipmentItem item = new EquipmentItem(
                        potion.getName(),
                        potion.getDescription() + " (Količina: " + potion.getQuantity() + ")",
                        status,
                        potion.isActive(),
                        () -> togglePotionActivation(potion)
                    );
                    potionItems.add(item);
                }
                
                runOnUiThread(() -> {
                    potionsAdapter = new EquipmentAdapter(this, potionItems);
                    potionsListView.setAdapter(potionsAdapter);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri učitavanju napitaka: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void loadClothing() {
        executor.execute(() -> {
            try {
                List<Clothing> clothing = database.equipmentDao().getAvailableClothing();
                List<EquipmentItem> clothingItems = new ArrayList<>();
                
                for (Clothing item : clothing) {
                    String status = item.isActive() ? 
                        "AKTIVNO (" + item.getBattlesRemaining() + " borbi)" : "NEAKTIVNO";
                    
                    EquipmentItem equipItem = new EquipmentItem(
                        item.getName(),
                        item.getDescription() + " (Količina: " + item.getQuantity() + 
                        ", Bonus: " + item.getBonusPercentage() + "%)",
                        status,
                        item.isActive(),
                        () -> toggleClothingActivation(item)
                    );
                    clothingItems.add(equipItem);
                }
                
                runOnUiThread(() -> {
                    clothingAdapter = new EquipmentAdapter(this, clothingItems);
                    clothingListView.setAdapter(clothingAdapter);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri učitavanju odeće: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void loadWeapons() {
        executor.execute(() -> {
            try {
                List<Weapon> weapons = database.equipmentDao().getAllWeapons();
                List<EquipmentItem> weaponItems = new ArrayList<>();
                
                for (Weapon weapon : weapons) {
                    String status = weapon.isActive() ? "AKTIVNO" : "NEAKTIVNO";
                    
                    EquipmentItem item = new EquipmentItem(
                        weapon.getName(),
                        weapon.getDetailedInfo(),
                        status,
                        weapon.isActive(),
                        () -> toggleWeaponActivation(weapon)
                    );
                    weaponItems.add(item);
                }
                
                runOnUiThread(() -> {
                    weaponsAdapter = new EquipmentAdapter(this, weaponItems);
                    weaponsListView.setAdapter(weaponsAdapter);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri učitavanju oružja: " + e.getMessage(), 
                                 Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void togglePotionActivation(Potion potion) {
        executor.execute(() -> {
            try {
                // Debug log na početku
                System.out.println("=== AKTIVACIJA NAPITKA ===");
                System.out.println("Napitak: " + potion.getName());
                System.out.println("Effect: " + potion.getEffect());
                System.out.println("isPermanent: " + potion.isPermanent());
                System.out.println("bonusPercentage: " + potion.getBonusPercentage());
                System.out.println("Trenutno aktivan: " + potion.isActive());
                
                if (potion.isActive()) {
                    // Deaktiviraj napitak
                    potion.setActive(false);
                    potion.removeEffect(currentUser);
                } else {
                    // Aktiviraj napitak
                    if (potion.getQuantity() > 0) {
                        potion.setActive(true);
                        
                        // Debug log PRE primene efekta
                        System.out.println("PRE AKTIVACIJE - User PP: " + currentUser.getPowerPoints());
                        
                        potion.applyEffect(currentUser);
                        
                        // Debug log POSLE primene efekta
                        System.out.println("POSLE AKTIVACIJE - User PP: " + currentUser.getPowerPoints());
                        
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Nemate ovaj napitak!", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                }
                
                database.equipmentDao().updatePotion(potion);
                database.userDao().updateUser(currentUser); // Sačuvaj promene u User-u
                
                // Debug log za čuvanje
                System.out.println("ČUVAM USER - PP: " + currentUser.getPowerPoints());
                
                UserStorage.saveUser(this, currentUser);
                
                // Debug log posle čuvanja
                System.out.println("USER SAČUVAN - PP: " + currentUser.getPowerPoints());
                
                runOnUiThread(() -> {
                    loadPotions(); // Osvezi prikaz
                    Toast.makeText(this, 
                        potion.isActive() ? "Napitak aktiviran!" : "Napitak deaktiviran!", 
                        Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void toggleClothingActivation(Clothing clothing) {
        executor.execute(() -> {
            try {
                if (clothing.isActive()) {
                    // Deaktiviraj odeću
                    clothing.setActive(false);
                    clothing.setBattlesRemaining(0);
                    clothing.removeEffect(currentUser);
                } else {
                    // Aktiviraj odeću
                    if (clothing.getQuantity() > 0) {
                        clothing.setActive(true);
                        clothing.applyEffect(currentUser);
                    } else {
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Nemate ovaj deo odeće!", Toast.LENGTH_SHORT).show();
                        });
                        return;
                    }
                }
                
                database.equipmentDao().updateClothing(clothing);
                UserStorage.saveUser(this, currentUser);
                
                runOnUiThread(() -> {
                    loadClothing(); // Osvezi prikaz
                    Toast.makeText(this, 
                        clothing.isActive() ? "Odeća aktivirana!" : "Odeća deaktivirana!", 
                        Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void toggleWeaponActivation(Weapon weapon) {
        executor.execute(() -> {
            try {
                weapon.setActive(!weapon.isActive());
                weapon.applyEffect(currentUser);
                
                database.equipmentDao().updateWeapon(weapon);
                UserStorage.saveUser(this, currentUser);
                
                runOnUiThread(() -> {
                    loadWeapons(); // Osvezi prikaz
                    Toast.makeText(this, 
                        weapon.isActive() ? "Oružje aktivirano!" : "Oružje deaktivirano!", 
                        Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showWeaponUpgradeDialog() {
        // TODO: Implementirati dialog za unapređenje oružja
        Toast.makeText(this, "Unapređenje oružja - uskoro!", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    /**
     * Klasa za predstavljanje stavki opreme
     */
    public static class EquipmentItem {
        private final String name;
        private final String description;
        private final String status;
        private final boolean isActive;
        private final Runnable toggleAction;
        
        public EquipmentItem(String name, String description, String status, 
                           boolean isActive, Runnable toggleAction) {
            this.name = name;
            this.description = description;
            this.status = status;
            this.isActive = isActive;
            this.toggleAction = toggleAction;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getStatus() { return status; }
        public boolean isActive() { return isActive; }
        public Runnable getToggleAction() { return toggleAction; }
    }
    
    /**
     * Prikazuje dialog za potvrdu brisanja sve opreme
     */
    private void showDeleteAllEquipmentDialog() {
        new AlertDialog.Builder(this)
            .setTitle("⚠️ Upozorenje")
            .setMessage("Da li ste sigurni da želite da obrišete SVU opremu?\n\nOva akcija se ne može opozvati!")
            .setPositiveButton("OBRIŠI SVE", (dialog, which) -> deleteAllEquipment())
            .setNegativeButton("OTKAŽI", null)
            .show();
    }
    
    /**
     * Briše svu opremu iz baze podataka
     */
    private void deleteAllEquipment() {
        executor.execute(() -> {
            try {
                // Obriši sve napitke
                database.equipmentDao().deleteAllPotions();
                
                // Obriši svu odeću
                database.equipmentDao().deleteAllClothing();
                
                // Obriši svo oružje
                database.equipmentDao().deleteAllWeapons();
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ SVA OPREMA JE OBRISANA!", Toast.LENGTH_LONG).show();
                    loadEquipment(); // Osvezi prikaz
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri brisanju: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    /**
     * Dodaje 1000 novčića korisniku
     */
    private void addCoins() {
        executor.execute(() -> {
            try {
                // Dodaj 1000 novčića
                int oldCoins = currentUser.getCoins();
                int newCoins = oldCoins + 1000;
                currentUser.setCoins(newCoins);
                
                // Sačuvaj u bazu i SharedPreferences
                database.userDao().updateUser(currentUser);
                UserStorage.saveUser(this, currentUser);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, 
                        "💰 Dodano 1000 novčića!\n" + 
                        "Staro: " + oldCoins + " → Novo: " + newCoins, 
                        Toast.LENGTH_LONG).show();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri dodavanju novčića: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}