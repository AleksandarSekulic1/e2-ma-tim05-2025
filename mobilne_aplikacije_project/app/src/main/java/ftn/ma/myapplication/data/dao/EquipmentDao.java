package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.Potion;
import ftn.ma.myapplication.data.model.Clothing;
import ftn.ma.myapplication.data.model.Weapon;

/**
 * DAO za upravljanje opremom u bazi podataka
 */
@Dao
public interface EquipmentDao {
    
    // Napici (Potions)
    @Insert
    long insertPotion(Potion potion);
    
    @Update
    void updatePotion(Potion potion);
    
    @Query("SELECT * FROM potions")
    List<Potion> getAllPotions();
    
    @Query("SELECT * FROM potions WHERE isActive = 1")
    List<Potion> getActivePotions();
    
    @Query("SELECT * FROM potions WHERE quantity > 0")
    List<Potion> getAvailablePotions();
    
    @Query("SELECT * FROM potions WHERE isPermanent = 1 AND isActive = 1")
    List<Potion> getPermanentActivePotions();
    
    @Query("SELECT * FROM potions WHERE isPermanent = 0 AND isActive = 1 AND isConsumed = 0")
    List<Potion> getTemporaryActivePotions();
    
    // Odeća (Clothing)
    @Insert
    long insertClothing(Clothing clothing);
    
    @Update
    void updateClothing(Clothing clothing);
    
    @Query("SELECT * FROM clothing")
    List<Clothing> getAllClothing();
    
    @Query("SELECT * FROM clothing WHERE isActive = 1")
    List<Clothing> getActiveClothing();
    
    @Query("SELECT * FROM clothing WHERE quantity > 0")
    List<Clothing> getAvailableClothing();
    
    @Query("SELECT * FROM clothing WHERE clothingType = :type AND isActive = 1")
    List<Clothing> getActiveClothingByType(Clothing.ClothingType type);
    
    @Query("SELECT * FROM clothing WHERE isActive = 1 AND battlesRemaining > 0")
    List<Clothing> getActiveClothingWithBattles();
    
    // Oružje (Weapons)
    @Insert
    long insertWeapon(Weapon weapon);
    
    @Update
    void updateWeapon(Weapon weapon);
    
    @Query("SELECT * FROM weapons")
    List<Weapon> getAllWeapons();
    
    @Query("SELECT * FROM weapons WHERE isActive = 1")
    List<Weapon> getActiveWeapons();
    
    @Query("SELECT * FROM weapons WHERE weaponType = :type")
    Weapon getWeaponByType(Weapon.WeaponType type);
    
    @Query("SELECT * FROM weapons WHERE weaponType = :type AND quantity > 0")
    Weapon getAvailableWeaponByType(Weapon.WeaponType type);
    
    // Ostale korisne queries
    @Query("UPDATE clothing SET battlesRemaining = battlesRemaining - 1 WHERE isActive = 1 AND battlesRemaining > 0")
    void decreaseAllClothingBattleCount();
    
    @Query("UPDATE clothing SET isActive = 0 WHERE battlesRemaining <= 0 AND isActive = 1")
    void deactivateExpiredClothing();
    
    @Query("UPDATE potions SET isActive = 0, isConsumed = 1, quantity = quantity - 1 WHERE isActive = 1 AND isPermanent = 0")
    void consumeTemporaryPotions();
    
    // Reset funkcije
    @Query("DELETE FROM potions")
    void deleteAllPotions();
    
    @Query("DELETE FROM clothing") 
    void deleteAllClothing();
    
    @Query("DELETE FROM weapons")
    void deleteAllWeapons();
}