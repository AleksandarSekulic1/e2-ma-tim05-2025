package ftn.ma.myapplication.data.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.Category;

@Dao // 1. Govorimo Room-u da je ovo DAO interfejs
public interface CategoryDao {

    // 2. Definišemo metodu za dodavanje nove kategorije
    @Insert
    void insert(Category category);

    // 3. Definišemo metodu za ažuriranje postojeće kategorije
    @Update
    void update(Category category);

    // 4. Definišemo metodu za brisanje kategorije
    @Delete
    void delete(Category category);

    // 5. Definišemo metodu za dobijanje svih kategorija iz baze
    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getAllCategories();

    // --- NOVA METODA ZA RESET ---
    @Query("DELETE FROM categories")
    void deleteAllCategories();
}