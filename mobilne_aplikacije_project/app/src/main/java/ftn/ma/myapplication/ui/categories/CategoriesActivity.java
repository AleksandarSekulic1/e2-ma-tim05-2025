package ftn.ma.myapplication.ui.categories;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.graphics.Color;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.CategoryDao;
import ftn.ma.myapplication.data.model.Category;

public class CategoriesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private CategoryAdapter adapter;
    private List<Category> categoryList;

    // --- NOVO: Varijable za bazu podataka ---
    private AppDatabase database;
    private CategoryDao categoryDao;
    private ExecutorService executorService; // Za izvršavanje operacija u pozadini

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        // --- NOVO: Inicijalizacija baze i DAO-a ---
        database = AppDatabase.getDatabase(getApplicationContext());
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();

        recyclerView = findViewById(R.id.recyclerViewCategories);
        fab = findViewById(R.id.fabAddCategory);

        setupRecyclerView();

        // Učitavamo kategorije iz baze po prvi put
        loadCategories();

        fab.setOnClickListener(v -> {
            showAddCategoryDialog();
        });
    }

    private void setupRecyclerView() {
        // Sada inicijalizujemo listu kao praznu
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(categoryList);
        recyclerView.setAdapter(adapter);
    }

    // --- NOVA METODA: Učitava sve kategorije iz baze ---
    private void loadCategories() {
        executorService.execute(() -> {
            // Operacija se izvršava u pozadini
            List<Category> categoriesFromDb = categoryDao.getAllCategories();

            // Ažuriranje UI-a se mora izvršiti na glavnoj (UI) niti
            runOnUiThread(() -> {
                categoryList.clear();
                categoryList.addAll(categoriesFromDb);
                // Obaveštavamo adapter da se ceo set podataka promenio
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dodaj novu kategoriju");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Dodaj", (dialog, which) -> {
            String categoryName = input.getText().toString();
            if (!categoryName.trim().isEmpty()) {
                // Pozivamo modifikovanu metodu za dodavanje
                addNewCategory(categoryName);
            } else {
                Toast.makeText(this, "Naziv ne može biti prazan", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // --- MODIFIKOVANA METODA: Sada upisuje u bazu umesto u listu ---
    private void addNewCategory(String name) {
        executorService.execute(() -> {
            // Operacija se izvršava u pozadini
            Random rnd = new Random();
            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

            // Kreiramo objekat bez ID-ja, jer će ga baza sama generisati
            Category newCategory = new Category(name, color);

            // Upisujemo novu kategoriju u bazu
            categoryDao.insert(newCategory);

            // Nakon upisa, ponovo učitavamo sve kategorije da bi se prikazala i nova
            loadCategories();
        });
    }
}