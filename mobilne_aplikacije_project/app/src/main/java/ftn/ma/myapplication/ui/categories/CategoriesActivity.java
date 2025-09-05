package ftn.ma.myapplication.ui.categories;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.CategoryDao;
import ftn.ma.myapplication.data.model.Category;
import ftn.ma.myapplication.ui.ProfileActivity;
import ftn.ma.myapplication.ui.calendar.TasksCalendarActivity;
import ftn.ma.myapplication.ui.tasks.TasksActivity;

public class CategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private AppDatabase database;
    private CategoryDao categoryDao;
    private ExecutorService executorService;

    // --- NOVO: Mapa sa predefinisanim bojama ---
    private final Map<String, Integer> colorMap = new LinkedHashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        database = AppDatabase.getDatabase(getApplicationContext());
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();

        // --- NOVO: Popunjavamo mapu boja ---
        initializeColorMap();

        recyclerView = findViewById(R.id.recyclerViewCategories);
        fab = findViewById(R.id.fabAddCategory);

        setupRecyclerView();
        loadCategories();

        fab.setOnClickListener(v -> showAddCategoryDialog());
        setupBottomNavigation();
    }

    private void initializeColorMap() {
        colorMap.put("Crvena", Color.parseColor("#F44336"));
        colorMap.put("Plava", Color.parseColor("#2196F3"));
        colorMap.put("Zelena", Color.parseColor("#4CAF50"));
        colorMap.put("Žuta", Color.parseColor("#FFEB3B"));
        colorMap.put("Narandžasta", Color.parseColor("#FF9800"));
        colorMap.put("Ljubičasta", Color.parseColor("#9C27B0"));
        colorMap.put("Tirkizna", Color.parseColor("#009688"));
        colorMap.put("Roze", Color.parseColor("#E91E63"));
    }

    private void setupRecyclerView() {
        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(categoryList, this);
        recyclerView.setAdapter(adapter);
    }
    private void loadCategories() {
        executorService.execute(() -> {
            List<Category> categoriesFromDb = categoryDao.getAllCategories();
            runOnUiThread(() -> {
                categoryList.clear();
                categoryList.addAll(categoriesFromDb);
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

    private void addNewCategory(String name) {
        // --- IZMENA: Dodata provera za jedinstvenost boje prilikom kreiranja ---
        executorService.execute(() -> {
            List<Category> allCategories = categoryDao.getAllCategories();
            int color;
            boolean isColorUnique;
            Random rnd = new Random();

            do {
                isColorUnique = true;
                color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                for (Category existingCategory : allCategories) {
                    if (existingCategory.getColor() == color) {
                        isColorUnique = false;
                        break;
                    }
                }
            } while (!isColorUnique);

            Category newCategory = new Category(name, color);
            categoryDao.insert(newCategory);
            loadCategories();
        });
    }
    @Override
    public void onCategoryClick(Category category) {
        showEditColorDialog(category);
    }

    // --- IZMENA: Potpuno prerađena metoda, ne koristi spoljnu biblioteku ---
    private void showEditColorDialog(final Category categoryToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Izaberi novu boju");

        // Uzimamo imena boja iz mape da ih prikažemo u listi
        final CharSequence[] colorNames = colorMap.keySet().toArray(new CharSequence[0]);

        builder.setItems(colorNames, (dialog, which) -> {
            // "which" je pozicija kliknutog imena boje
            String selectedColorName = colorNames[which].toString();
            int selectedColorValue = colorMap.get(selectedColorName);

            // Pozivamo istu logiku za proveru i ažuriranje kao i pre
            checkColorAndupdate(categoryToEdit, selectedColorValue);
        });

        builder.show();
    }

    private void checkColorAndupdate(final Category categoryToEdit, final int newColor) {
        executorService.execute(() -> {
            List<Category> allCategories = categoryDao.getAllCategories();
            boolean isColorTaken = false;
            for (Category existingCategory : allCategories) {
                if (existingCategory.getId() != categoryToEdit.getId() && existingCategory.getColor() == newColor) {
                    isColorTaken = true;
                    break;
                }
            }

            // Kreiramo finalnu kopiju rezultata koju ćemo koristiti u lambdi
            final boolean finalIsColorTaken = isColorTaken;

            runOnUiThread(() -> {
                // Koristimo finalnu kopiju
                if (finalIsColorTaken) {
                    Toast.makeText(this, "Izabrana boja je već zauzeta!", Toast.LENGTH_SHORT).show();
                } else {
                    categoryToEdit.setColor(newColor);
                    executorService.execute(() -> {
                        categoryDao.update(categoryToEdit);
                        loadCategories();
                    });
                    Toast.makeText(this, "Boja uspešno promenjena.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        // Postavljamo da je "Zadaci" ikonica selektovana na ovom ekranu
        bottomNav.setSelectedItemId(R.id.navigation_categories);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_categories) {
                // Već smo na ovom ekranu, ne radi ništa
                return true;
            } else if (itemId == R.id.navigation_tasks) {
                // Pokreni TasksCalendarActivity
                startActivity(new Intent(getApplicationContext(), TasksActivity.class));
                // Dodaj animaciju da prelaz bude lepši
                overridePendingTransition(0, 0);
                finish(); // Zatvori trenutnu aktivnost
                return true;
            } else if (itemId == R.id.navigation_calendar) {
                // Pokreni CategoriesActivity
                startActivity(new Intent(getApplicationContext(), TasksCalendarActivity.class));
                overridePendingTransition(0, 0);
                finish(); // Zatvori trenutnu aktivnost
                return true;
            }else if (itemId == R.id.navigation_profile) { // DODAJTE OVAJ DEO
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}