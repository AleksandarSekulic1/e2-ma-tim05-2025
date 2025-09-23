package ftn.ma.myapplication.ui;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Aktivnost za prodavnicu gde korisnik kupuje napitke i odeću
 */
public class ShopActivity extends AppCompatActivity {
    
    private TextView coinsDisplay;
    private ListView potionsListView;
    private ListView clothingListView;
    private ShopAdapter potionsAdapter;
    private ShopAdapter clothingAdapter;
    
    private User currentUser;
    private AppDatabase database;
    private ExecutorService executor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        
        initViews();
        initDatabase();
        loadUserData();
        setupShopItems();
    }
    
    private void initViews() {
        coinsDisplay = findViewById(R.id.coinsDisplay);
        potionsListView = findViewById(R.id.potionsListView);
        clothingListView = findViewById(R.id.clothingListView);
    }
    
    private void initDatabase() {
        database = AppDatabase.getDatabase(this);
        executor = Executors.newSingleThreadExecutor();
    }
    
    private void loadUserData() {
        currentUser = UserStorage.getUser(this);
        if (currentUser != null) {
            updateCoinsDisplay();
        }
    }
    
    private void updateCoinsDisplay() {
        coinsDisplay.setText("Novčići: " + currentUser.getCoins());
    }
    
    private void setupShopItems() {
        setupPotionsShop();
        setupClothingShop();
    }
    
    private void setupPotionsShop() {
        List<ShopItem> potionItems = new ArrayList<>();
        
        // Kreiraj sve tipove napitaka sa cenama za trenutni nivo
        for (Potion.PotionEffect effect : Potion.PotionEffect.values()) {
            Potion potion = new Potion(effect);
            potion.updateCostForLevel(currentUser.getLevel());
            
            ShopItem item = new ShopItem(
                potion.getName(),
                potion.getDescription(),
                potion.getCost(),
                () -> buyPotion(potion)
            );
            potionItems.add(item);
        }
        
        potionsAdapter = new ShopAdapter(this, potionItems);
        potionsListView.setAdapter(potionsAdapter);
    }
    
    private void setupClothingShop() {
        List<ShopItem> clothingItems = new ArrayList<>();
        
        // Kreiraj sve tipove odeće sa cenama za trenutni nivo
        for (Clothing.ClothingType type : Clothing.ClothingType.values()) {
            Clothing clothing = new Clothing(type);
            clothing.updateCostForLevel(currentUser.getLevel());
            
            ShopItem item = new ShopItem(
                clothing.getName(),
                clothing.getDescription(),
                clothing.getCost(),
                () -> buyClothing(clothing)
            );
            clothingItems.add(item);
        }
        
        clothingAdapter = new ShopAdapter(this, clothingItems);
        clothingListView.setAdapter(clothingAdapter);
    }
    
    private void buyPotion(Potion potion) {
        if (currentUser.getCoins() >= potion.getCost()) {
            executor.execute(() -> {
                try {
                    // Oduzmi novčiće
                    currentUser.setCoins(currentUser.getCoins() - potion.getCost());
                    
                    // Proveri da li korisnik već ima ovaj napitak
                    List<Potion> existingPotions = database.equipmentDao().getAllPotions();
                    Potion existingPotion = null;
                    
                    for (Potion existing : existingPotions) {
                        if (existing.getEffect() == potion.getEffect()) {
                            existingPotion = existing;
                            break;
                        }
                    }
                    
                    if (existingPotion != null) {
                        // Povećaj količinu postojećeg napitka
                        existingPotion.setQuantity(existingPotion.getQuantity() + 1);
                        database.equipmentDao().updatePotion(existingPotion);
                    } else {
                        // Dodaj novi napitak
                        potion.setQuantity(1);
                        database.equipmentDao().insertPotion(potion);
                    }
                    
                    // Ažuriraj korisnika u bazi ili storage
                    UserStorage.saveUser(this, currentUser);
                    
                    runOnUiThread(() -> {
                        updateCoinsDisplay();
                        Toast.makeText(this, "Kupljen " + potion.getName() + "!", Toast.LENGTH_SHORT).show();
                    });
                    
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Greška pri kupovini: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            Toast.makeText(this, "Nemate dovoljno novčića!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void buyClothing(Clothing clothing) {
        if (currentUser.getCoins() >= clothing.getCost()) {
            executor.execute(() -> {
                try {
                    // Oduzmi novčiće
                    currentUser.setCoins(currentUser.getCoins() - clothing.getCost());
                    
                    // Proveri da li korisnik već ima ovaj tip odeće
                    List<Clothing> existingClothing = database.equipmentDao().getAllClothing();
                    Clothing existingItem = null;
                    
                    for (Clothing existing : existingClothing) {
                        if (existing.getClothingType() == clothing.getClothingType()) {
                            existingItem = existing;
                            break;
                        }
                    }
                    
                    if (existingItem != null) {
                        // Povećaj količinu postojeće odeće
                        existingItem.setQuantity(existingItem.getQuantity() + 1);
                        database.equipmentDao().updateClothing(existingItem);
                    } else {
                        // Dodaj novi deo odeće
                        clothing.setQuantity(1);
                        database.equipmentDao().insertClothing(clothing);
                    }
                    
                    // Ažuriraj korisnika
                    UserStorage.saveUser(this, currentUser);
                    
                    runOnUiThread(() -> {
                        updateCoinsDisplay();
                        Toast.makeText(this, "Kupljena " + clothing.getName() + "!", Toast.LENGTH_SHORT).show();
                    });
                    
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Greška pri kupovini: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } else {
            Toast.makeText(this, "Nemate dovoljno novčića!", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
    
    /**
     * Klasa za predstavljanje stavki u prodavnici
     */
    public static class ShopItem {
        private final String name;
        private final String description;
        private final int cost;
        private final Runnable purchaseAction;
        
        public ShopItem(String name, String description, int cost, Runnable purchaseAction) {
            this.name = name;
            this.description = description;
            this.cost = cost;
            this.purchaseAction = purchaseAction;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public int getCost() { return cost; }
        public Runnable getPurchaseAction() { return purchaseAction; }
    }
}