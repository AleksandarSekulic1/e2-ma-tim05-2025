package ftn.ma.myapplication.ui.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
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
                case "Izmeni": onTaskClick(task); break;
                case "Obriši": showDeleteConfirmationDialog(task); break;
                case "Pauziraj": changeTaskStatus(task, Task.Status.PAUZIRAN); break;
                case "Otkaži": changeTaskStatus(task, Task.Status.OTKAZAN); break;
                case "Aktiviraj": changeTaskStatus(task, Task.Status.AKTIVAN); break;
                case "Nazad": dialog.dismiss(); break;
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
        task.setStatus(isChecked ? Task.Status.URADJEN : Task.Status.AKTIVAN);
        if (isChecked && !task.isXpAwarded()) {
            task.setCompletionDate(task.getExecutionTime());
            awardXpForTask(task);
        } else {
            executorService.execute(() -> taskDao.update(task));
        }
    }

    // U TasksActivity.java

    private void awardXpForTask(Task completedTask) {
        executorService.execute(() -> {
            List<Task> allTasks = taskDao.getAllTasks();
            int xpForDifficulty = 0;
            int xpForImportance = 0;
            String quotaMessage = "";

            // --- ISPRAVKA: Dodajemo 'final' ---
            final Date referenceDate = (completedTask.getCompletionDate() == null) ? new Date() : completedTask.getCompletionDate();
            final Task.Difficulty difficulty = completedTask.getDifficulty();
            final Task.Importance importance = completedTask.getImportance();


            // Provera kvote za Težinu
            int difficultyBaseXp = getXpForDifficulty(difficulty);
            if (difficulty == Task.Difficulty.EKSTREMNO_TEZAK) {
                long count = allTasks.stream().filter(t -> t.getId() != completedTask.getId() && t.getStatus() == Task.Status.URADJEN && t.getCompletionDate() != null && isSameWeek(t.getCompletionDate(), referenceDate) && t.getDifficulty() == difficulty).count();
                if (count < 1) xpForDifficulty = difficultyBaseXp; else quotaMessage += "Ispunjena nedeljna kvota za Težinu! ";
            } else {
                List<Task> tasksCompletedOnDate = allTasks.stream().filter(t -> t.getId() != completedTask.getId() && t.getStatus() == Task.Status.URADJEN && t.getCompletionDate() != null && isSameDay(t.getCompletionDate(), referenceDate)).collect(Collectors.toList());
                long count = tasksCompletedOnDate.stream().filter(t -> t.getDifficulty() == difficulty).count();
                int limit = (difficulty == Task.Difficulty.TEZAK) ? 2 : 5;
                if (count < limit) xpForDifficulty = difficultyBaseXp; else quotaMessage += "Ispunjena dnevna kvota za Težinu! ";
            }

            // Provera kvote za Bitnost
            int importanceBaseXp = getXpForImportance(importance);
            if (importance == Task.Importance.SPECIJALAN) {
                long count = allTasks.stream().filter(t -> t.getId() != completedTask.getId() && t.getStatus() == Task.Status.URADJEN && t.getCompletionDate() != null && isSameMonth(t.getCompletionDate(), referenceDate) && t.getImportance() == importance).count();
                if (count < 1) xpForImportance = importanceBaseXp; else quotaMessage += "Ispunjena mesečna kvota za Bitnost! ";
            } else {
                List<Task> tasksCompletedOnDate = allTasks.stream().filter(t -> t.getId() != completedTask.getId() && t.getStatus() == Task.Status.URADJEN && t.getCompletionDate() != null && isSameDay(t.getCompletionDate(), referenceDate)).collect(Collectors.toList());
                long count = tasksCompletedOnDate.stream().filter(t -> t.getImportance() == importance).count();
                int limit = (importance == Task.Importance.EKSTREMNO_VAZAN) ? 2 : 5;
                if (count < limit) xpForImportance = importanceBaseXp; else quotaMessage += "Ispunjena dnevna kvota za Bitnost! ";
            }

            // Kreiramo finalne kopije za lambda izraz
            final int finalTotalXpGained = xpForDifficulty + xpForImportance;
            final String finalQuotaMessage = quotaMessage.trim();

            runOnUiThread(() -> {
                if (!finalQuotaMessage.isEmpty()) {
                    Toast.makeText(this, finalQuotaMessage, Toast.LENGTH_SHORT).show();
                }
                if (finalTotalXpGained > 0) {
                    updateUserStats(finalTotalXpGained);
                }
            });

            completedTask.setXpAwarded(true);
            taskDao.update(completedTask);
        });
    }


    private void updateUserStats(int totalXpGained) {
        int currentLevel = SharedPreferencesManager.getUserLevel(this);
        int currentXp = SharedPreferencesManager.getUserXp(this);
        int newTotalXp = currentXp + totalXpGained;

        Toast.makeText(this, "Osvojili ste " + totalXpGained + " XP! (Ukupno: " + newTotalXp + ")", Toast.LENGTH_SHORT).show();
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

    private int getXpForDifficulty(Task.Difficulty difficulty) {
        if(difficulty == null) return 0;
        switch (difficulty) {
            case VEOMA_LAK: return 1;
            case LAK: return 3;
            case TEZAK: return 7;
            case EKSTREMNO_TEZAK: return 20;
            default: return 0;
        }
    }

    private int getXpForImportance(Task.Importance importance) {
        if(importance == null) return 0;
        switch (importance) {
            case NORMALAN: return 1;
            case VAZAN: return 3;
            case EKSTREMNO_VAZAN: return 10;
            case SPECIJALAN: return 100;
            default: return 0;
        }
    }

    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate1.equals(localDate2);
    }

    private boolean isSameWeek(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        LocalDate localDate1 = date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate localDate2 = date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return localDate1.get(weekFields.weekOfYear()) == localDate2.get(weekFields.weekOfYear())
                && localDate1.getYear() == localDate2.getYear();
    }

    private boolean isSameMonth(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        YearMonth month1 = YearMonth.from(date1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        YearMonth month2 = YearMonth.from(date2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        return month1.equals(month2);
    }
}