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
import com.google.android.material.tabs.TabLayout;
import java.util.Calendar;

public class TasksActivity extends AppCompatActivity implements TaskAdapter.OnTaskListener {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private TaskAdapter adapter;
    private List<Task> taskList;
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    private ExecutorService executorService;
    private enum FilterState {
        ALL, ONE_TIME, RECURRING
    }
    private TabLayout tabLayout;
    private FilterState currentFilter = FilterState.ALL;


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
        tabLayout = findViewById(R.id.tabLayout);

        setupRecyclerView();
        setupTabLayout();

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(TasksActivity.this, CreateEditTaskActivity.class);
            startActivity(intent);
        });
    }

    private void setupTabLayout() {
        // ... (nepromenjeno)
        tabLayout.addTab(tabLayout.newTab().setText("Svi"));
        tabLayout.addTab(tabLayout.newTab().setText("Jednokratni"));
        tabLayout.addTab(tabLayout.newTab().setText("Ponavljajući"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: currentFilter = FilterState.ALL; break;
                    case 1: currentFilter = FilterState.ONE_TIME; break;
                    case 2: currentFilter = FilterState.RECURRING; break;
                }
                loadTasks();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
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

    // --- GLAVNA IZMENA: Učitavanje i filtriranje zadataka ---
    private void loadTasks() {
        executorService.execute(() -> {
            List<Task> tasksFromDb = taskDao.getAllTasks();
            Date today = getTodayAtMidnight();

            // Specifikacija: "u listi prikazuju samo trenutni i budući zadaci"
            // Sada je filter mnogo jednostavniji
            List<Task> currentAndFutureTasks = tasksFromDb.stream()
                    .filter(task -> task.getExecutionTime() != null && !task.getExecutionTime().before(today))
                    .collect(Collectors.toList());

            // Filtriranje po tabovima je sada zasnovano na `recurringGroupId`
            List<Task> filteredTasks;
            switch (currentFilter) {
                case ONE_TIME:
                    filteredTasks = currentAndFutureTasks.stream()
                            .filter(task -> task.getRecurringGroupId() == null)
                            .collect(Collectors.toList());
                    break;
                case RECURRING:
                    filteredTasks = currentAndFutureTasks.stream()
                            .filter(task -> task.getRecurringGroupId() != null)
                            .collect(Collectors.toList());
                    break;
                case ALL:
                default:
                    filteredTasks = currentAndFutureTasks;
                    break;
            }

            // Učitavanje kategorija (nepromenjeno)
            List<Category> allCategories = categoryDao.getAllCategories();
            for (Task task : filteredTasks) {
                for (Category category : allCategories) {
                    if (task.getCategoryId() == category.getId()) {
                        task.setCategory(category);
                        break;
                    }
                }
            }

            runOnUiThread(() -> {
                taskList.clear();
                taskList.addAll(filteredTasks);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private Date getTodayAtMidnight() {
        // ... (nepromenjeno)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    // --- IZMENA: Logika za zaključane zadatke ---
    private boolean isTaskLocked(Task task) {
        if (task.getStatus() == Task.Status.URADJEN || task.getStatus() == Task.Status.OTKAZAN || task.getStatus() == Task.Status.NEURADJEN) {
            return true;
        }
        // Provera je sada jednostavnija - da li je vreme izvršenja prošlo
        return task.getExecutionTime() != null && task.getExecutionTime().before(new Date());
    }

    // --- IZMENA: Meni sa opcijama ---
    @Override
    public void onTaskLongClick(Task task) {
        if (isTaskLocked(task)) {
            Toast.makeText(this, "Završeni ili istekli zadaci se ne mogu menjati.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> options = new ArrayList<>();
        options.add("Izmeni");

        // Ako je zadatak deo serije, nudimo dve opcije za brisanje
        if (task.getRecurringGroupId() != null) {
            options.add("Obriši samo ovaj zadatak");
            options.add("Obriši ovaj i sve buduće");
        } else {
            options.add("Obriši");
        }

        // Statusi (nepromenjeno)
        switch (task.getStatus()) {
            case AKTIVAN:
                options.add("Otkaži");
                break;
            // Pauziranje cele serije je kompleksno, za sada ga izostavljamo iz ovog menija
        }
        options.add("Nazad");

        final CharSequence[] items = options.toArray(new CharSequence[0]);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Izaberi akciju za: '" + task.getName() + "'");
        builder.setItems(items, (dialog, item) -> {
            String selected = items[item].toString();
            switch (selected) {
                case "Izmeni":
                    onTaskClick(task);
                    break;
                case "Obriši": // Brisanje jednokratnog
                    deleteTask(task, true);
                    break;
                case "Obriši samo ovaj zadatak":
                    deleteTask(task, true); // Brišemo samo ovu instancu
                    break;
                case "Obriši ovaj i sve buduće":
                    deleteTask(task, false); // Brišemo celu buduću seriju
                    break;
                case "Otkaži":
                    changeTaskStatus(task, Task.Status.OTKAZAN);
                    break;
                case "Nazad":
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    // --- IZMENA: Logika brisanja ---
    private void deleteTask(Task task, boolean justThisOne) {
        executorService.execute(() -> {
            if (justThisOne || task.getRecurringGroupId() == null) {
                // Brišemo samo jedan zadatak (ili zato što je jednokratan, ili je korisnik tako izabrao)
                taskDao.delete(task);
            } else {
                // Brišemo ovaj i sve buduće zadatke iz serije
                taskDao.deleteFutureByGroupId(task.getRecurringGroupId(), task.getExecutionTime());
            }
            // Osvežavamo prikaz
            loadTasks();
        });
    }

    private void changeTaskStatus(Task task, Task.Status newStatus) {
        // ... (nepromenjeno)
        task.setStatus(newStatus);
        executorService.execute(() -> {
            taskDao.update(task);
            loadTasks();
        });
    }

    // --- IZMENA: Logika klika ---
    @Override
    public void onTaskClick(Task task) {
        if (isTaskLocked(task)) {
            Toast.makeText(this, "Završeni ili istekli zadaci se ne mogu menjati.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(TasksActivity.this, CreateEditTaskActivity.class);
        intent.putExtra("EDIT_TASK", task);

        // Ako je zadatak deo serije, šaljemo datum da bi forma za izmenu znala da ponudi opciju "izmeni sve buduće"
        if (task.getRecurringGroupId() != null) {
            intent.putExtra("RECURRING_EDIT_DATE", task.getExecutionTime().getTime());
        }

        startActivity(intent);
    }

    @Override
    public void onTaskCheckedChanged(Task task, boolean isChecked) {
        // ... (nepromenjeno)
        task.setStatus(isChecked ? Task.Status.URADJEN : Task.Status.AKTIVAN);
        if (isChecked && !task.isXpAwarded()) {
            task.setCompletionDate(task.getExecutionTime());
            awardXpForTask(task);
        } else {
            executorService.execute(() -> taskDao.update(task));
        }
    }

    // Ostatak koda ostaje nepromenjen...
    // awardXpForTask, updateUserStats, getXpForDifficulty, getXpForImportance,
    // isSameDay, isSameWeek, isSameMonth

    private void awardXpForTask(Task completedTask) {
        executorService.execute(() -> {
            List<Task> allTasks = taskDao.getAllTasks();
            int xpForDifficulty = 0;
            int xpForImportance = 0;
            String quotaMessage = "";

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