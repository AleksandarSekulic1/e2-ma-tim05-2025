package ftn.ma.myapplication.ui.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.CategoryDao;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.domain.LevelingManager;
import ftn.ma.myapplication.data.model.Category;
import ftn.ma.myapplication.data.model.Task;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class TasksActivity extends AppCompatActivity implements TaskAdapter.OnTaskListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        taskDao = database.taskDao();
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();

        recyclerView = findViewById(R.id.recyclerViewTasks);
        fab = findViewById(R.id.fabAddTask);

        setupRecyclerView();

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(TasksActivity.this, CreateEditTaskActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks();
    }

    private void setupRecyclerView() {
        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList, taskDao, executorService, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadTasks() {
        executorService.execute(() -> {
            List<Task> tasksFromDb = taskDao.getAllTasks();
            List<Category> allCategories = categoryDao.getAllCategories();
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
        new AlertDialog.Builder(this)
                .setTitle("Obriši zadatak")
                .setMessage("Da li ste sigurni da želite da obrišete zadatak '" + task.getName() + "'?")
                .setPositiveButton("Obriši", (dialog, which) -> deleteTask(task))
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private void deleteTask(Task task) {
        executorService.execute(() -> {
            taskDao.delete(task);
            loadTasks();
        });
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(TasksActivity.this, CreateEditTaskActivity.class);
        intent.putExtra("EDIT_TASK", task);
        startActivity(intent);
    }

    // --- ISPRAVLJENA LOGIKA ---
    @Override
    public void onTaskCheckedChanged(Task task, boolean isChecked) {
        // Dodeljujemo poene SAMO ako je zadatak štikliran I AKO POENI VEĆ NISU DODELJENI
        if (isChecked && !task.isXpAwarded()) {
            task.setXpAwarded(true); // Označavamo da su poeni dodeljeni
            awardXpForTask(task);
        }

        // Ažuriramo status zadatka bez obzira na poene
        task.setStatus(isChecked ? Task.Status.URADJEN : Task.Status.AKTIVAN);

        // Čuvamo promenu u bazi (i statusa i xpAwarded zastavice)
        executorService.execute(() -> taskDao.update(task));
    }

    private void awardXpForTask(Task task) {
        int xpForDifficulty = 0;
        switch (task.getDifficulty()) {
            case VEOMA_LAK: xpForDifficulty = 1; break;
            case LAK: xpForDifficulty = 3; break;
            case TEZAK: xpForDifficulty = 7; break;
            case EKSTREMNO_TEZAK: xpForDifficulty = 20; break;
        }

        int xpForImportance = 0;
        switch (task.getImportance()) {
            case NORMALAN: xpForImportance = 1; break;
            case VAZAN: xpForImportance = 3; break;
            case EKSTREMNO_VAZAN: xpForImportance = 10; break;
            case SPECIJALAN: xpForImportance = 100; break;
        }

        int totalXp = xpForDifficulty + xpForImportance;

        int currentLevel = SharedPreferencesManager.getUserLevel(this);
        int currentXp = SharedPreferencesManager.getUserXp(this);
        int newTotalXp = currentXp + totalXp;

        Toast.makeText(this, "Osvojili ste " + totalXp + " XP! (Ukupno: " + newTotalXp + ")", Toast.LENGTH_SHORT).show();
        SharedPreferencesManager.saveUserXp(this, newTotalXp);

        int xpNeededForNextLevel = LevelingManager.calculateXpForNextLevel(currentLevel);
        boolean leveledUp = false;
        while (newTotalXp >= xpNeededForNextLevel) {
            currentLevel++;
            leveledUp = true;
            xpNeededForNextLevel = LevelingManager.calculateXpForNextLevel(currentLevel);
        }

        if (leveledUp) {
            SharedPreferencesManager.saveUserLevel(this, currentLevel);
            Toast.makeText(this, "ČESTITAMO! Prešli ste na NIVO " + currentLevel + "!", Toast.LENGTH_LONG).show();
        }
    }
}