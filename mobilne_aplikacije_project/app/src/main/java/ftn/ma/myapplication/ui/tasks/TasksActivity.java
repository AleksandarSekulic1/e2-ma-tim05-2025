package ftn.ma.myapplication.ui.tasks;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.concurrent.ExecutorService; // NOVI IMPORT
import java.util.concurrent.Executors;     // NOVI IMPORT
import ftn.ma.myapplication.data.local.AppDatabase; // NOVI IMPORT
import ftn.ma.myapplication.data.local.TaskDao;     // NOVI IMPORT
import ftn.ma.myapplication.data.local.CategoryDao; // NOVI IMPORT
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.app.Activity; // Dodajte ovaj import
import androidx.activity.result.ActivityResultLauncher; // Dodajte i ovaj
import androidx.activity.result.contract.ActivityResultContracts; // I ovaj
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.model.Category;
import ftn.ma.myapplication.data.model.Task;
import androidx.appcompat.app.AlertDialog;

public class TasksActivity extends AppCompatActivity implements TaskAdapter.OnTaskListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private TaskAdapter adapter;
    private List<Task> taskList;
    // Definišemo request code (jedinstveni broj za naš zahtev)
    private static final int ADD_TASK_REQUEST_CODE = 1;
    private TaskDao taskDao;
    private CategoryDao categoryDao; // Trebaće nam za dohvatanje imena kategorija
    private ExecutorService executorService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        // NOVO: Inicijalizacija baze
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        taskDao = database.taskDao();
        categoryDao = database.categoryDao(); // Inicijalizujemo i njega
        executorService = Executors.newSingleThreadExecutor();

        recyclerView = findViewById(R.id.recyclerViewTasks);
        fab = findViewById(R.id.fabAddTask);

        setupRecyclerView();

        // Više ne pozivamo loadTasks() ovde, već u onResume()

        // Pokrećemo CreateEditTaskActivity kao i do sada, bez očekivanja rezultata
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(TasksActivity.this, CreateEditTaskActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Uvek ponovo učitamo zadatke da bismo videli promene
        loadTasks();
    }

    private void setupRecyclerView() {
        // Postavljamo praznu listu i adapter
        taskList = new ArrayList<>();
        // ISPRAVNA LINIJA
        adapter = new TaskAdapter(taskList, taskDao, executorService,this);
        recyclerView.setAdapter(adapter);
    }

    // MODIFIKOVANA METODA za učitavanje iz baze
    private void loadTasks() {
        executorService.execute(() -> {
            // Učitavamo sve zadatke iz baze
            List<Task> tasksFromDb = taskDao.getAllTasks();
            // Učitavamo sve kategorije iz baze da bismo mogli da spojimo imena
            List<Category> allCategories = categoryDao.getAllCategories();

            // Spajamo zadatke sa kategorijama
            for (Task task : tasksFromDb) {
                for (Category category : allCategories) {
                    if (task.getCategoryId() == category.getId()) {
                        task.setCategory(category);
                        break;
                    }
                }
            }

            runOnUiThread(() -> {
                taskList.clear();
                taskList.addAll(tasksFromDb);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public void onTaskLongClick(Task task) {
        // Prikazujemo dijalog za potvrdu brisanja
        new AlertDialog.Builder(this)
                .setTitle("Obriši zadatak")
                .setMessage("Da li ste sigurni da želite da obrišete zadatak '" + task.getName() + "'?")
                .setPositiveButton("Obriši", (dialog, which) -> {
                    // Ako korisnik potvrdi, pozivamo metodu za brisanje
                    deleteTask(task);
                })
                .setNegativeButton("Otkaži", null) // "Otkaži" ne radi ništa
                .show();
    }

    // --- NOVA METODA za brisanje iz baze ---
    private void deleteTask(Task task) {
        executorService.execute(() -> {
            // Komanda za brisanje se izvršava u pozadini
            taskDao.delete(task);
            // Nakon brisanja, ponovo učitavamo sve zadatke da osvežimo listu
            loadTasks();
        });
    }

}
