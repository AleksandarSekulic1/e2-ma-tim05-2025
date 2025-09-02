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
import ftn.ma.myapplication.ui.game.BossFightActivity;
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
        List<String> options = new ArrayList<>();

        switch (task.getStatus()) {
            case AKTIVAN:
            case URADJEN:
                options.add("Izmeni");
                options.add("Obriši");
                if (task.isRecurring()) {
                    options.add("Pauziraj");
                }
                options.add("Otkaži");
                break;
            case PAUZIRAN:
                options.add("Aktiviraj");
                break;
            case OTKAZAN:
                Toast.makeText(this, "Ovaj zadatak je otkazan i ne može se menjati.", Toast.LENGTH_SHORT).show();
                return;
        }
        options.add("Nazad");

        final CharSequence[] items = options.toArray(new CharSequence[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Izaberi akciju za: '" + task.getName() + "'");
        builder.setItems(items, (dialog, item) -> {
            String selectedOption = items[item].toString();
            switch (selectedOption) {
                case "Izmeni":
                    onTaskClick(task);
                    break;
                case "Obriši":
                    showDeleteConfirmationDialog(task);
                    break;
                case "Pauziraj":
                    changeTaskStatus(task, Task.Status.PAUZIRAN);
                    break;
                case "Otkaži":
                    changeTaskStatus(task, Task.Status.OTKAZAN);
                    break;
                case "Aktiviraj":
                    changeTaskStatus(task, Task.Status.AKTIVAN);
                    break;
                case "Nazad":
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private void showDeleteConfirmationDialog(Task task) {
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

    private void changeTaskStatus(Task task, Task.Status newStatus) {
        task.setStatus(newStatus);
        executorService.execute(() -> {
            taskDao.update(task);
            loadTasks();
        });
        Toast.makeText(this, "Status zadatka promenjen u: " + newStatus.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(TasksActivity.this, CreateEditTaskActivity.class);
        intent.putExtra("EDIT_TASK", task);
        startActivity(intent);
    }

    @Override
    public void onTaskCheckedChanged(Task task, boolean isChecked) {
        if (isChecked && !task.isXpAwarded()) {
            task.setXpAwarded(true);
            awardXpForTask(task);
        }
        task.setStatus(isChecked ? Task.Status.URADJEN : Task.Status.AKTIVAN);
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

        int xpNeeded = LevelingManager.getXpNeededForLevel(currentLevel);
        boolean leveledUp = false;
        while (newTotalXp >= xpNeeded) {
            currentLevel++;
            leveledUp = true;
            xpNeeded = LevelingManager.getXpNeededForLevel(currentLevel);
        }

        if (leveledUp) {
            SharedPreferencesManager.saveUserLevel(this, currentLevel);
            int newPp = LevelingManager.calculateTotalPpForLevel(currentLevel);
            SharedPreferencesManager.saveUserPp(this, newPp);
            Toast.makeText(this, "ČESTITAMO! Prešli ste na NIVO " + currentLevel + "!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(TasksActivity.this, BossFightActivity.class);
            intent.putExtra("USER_LEVEL", currentLevel);
            intent.putExtra("USER_PP", newPp);
            startActivity(intent);
        }
    }
}