package ftn.ma.myapplication.ui.categories;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.model.Category;

public class CategoriesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private CategoryAdapter adapter;
    private List<Category> categoryList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        recyclerView = findViewById(R.id.recyclerViewCategories);
        fab = findViewById(R.id.fabAddCategory);

        // --- Postojeći kod za prikazivanje liste ---
        setupRecyclerView();


        // --- NOVI KOD ---
        // Postavljamo listener na klik "plus" dugmeta
        fab.setOnClickListener(v -> {
            showAddCategoryDialog();
        });
    }

    private void setupRecyclerView() {
        // 1. Kreiramo privremenu listu podataka
        categoryList = new ArrayList<>();
        categoryList.add(new Category(1L, "Učenje", Color.BLUE));
        categoryList.add(new Category(2L, "Zdravlje", Color.GREEN));

        // 2. Kreiramo instancu našeg adaptera i prosleđujemo mu listu
        adapter = new CategoryAdapter(categoryList);

        // 3. Postavljamo adapter na naš RecyclerView
        recyclerView.setAdapter(adapter);
    }

    // --- NOVI KOD ---
    // Metoda koja kreira i prikazuje dijalog za unos nove kategorije
    private void showAddCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dodaj novu kategoriju");

        // Kreiramo EditText polje za unos teksta unutar dijaloga
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Definišemo "Dodaj" dugme i šta se dešava kada se klikne
        builder.setPositiveButton("Dodaj", (dialog, which) -> {
            String categoryName = input.getText().toString();

            // Proveravamo da li je korisnik uneo neko ime
            if (!categoryName.isEmpty()) {
                addNewCategory(categoryName);
            }
        });

        // Definišemo "Otkaži" dugme
        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.cancel());

        // Prikazujemo dijalog
        builder.show();
    }

    // --- NOVI KOD ---
    // Metoda koja dodaje novu kategoriju u listu i obaveštava adapter
    private void addNewCategory(String name) {
        // Kreiramo nasumičnu boju za novu kategoriju (privremeno rešenje)
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

        // Kreiramo novi Category objekat
        // Za ID privremeno koristimo veličinu liste da bismo osigurali unikatnost
        Category newCategory = new Category((long) (categoryList.size() + 1), name, color);

        // Dodajemo novu kategoriju u našu listu podataka
        categoryList.add(newCategory);

        // OBAVEŠTAVAMO ADAPTER da je novi element dodat na kraj liste
        // Ovo je ključan korak!
        adapter.notifyItemInserted(categoryList.size() - 1);
    }
}