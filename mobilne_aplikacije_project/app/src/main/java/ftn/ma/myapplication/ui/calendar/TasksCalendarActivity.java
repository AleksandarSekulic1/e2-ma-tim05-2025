package ftn.ma.myapplication.ui.calendar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.CategoryDao;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.data.model.Category;
import ftn.ma.myapplication.data.model.Task;
import ftn.ma.myapplication.domain.LevelingManager;
import ftn.ma.myapplication.ui.ProfileActivity;
import ftn.ma.myapplication.ui.categories.CategoriesActivity;
import ftn.ma.myapplication.ui.game.BossFightActivity;
import ftn.ma.myapplication.ui.tasks.CreateEditTaskActivity;
import ftn.ma.myapplication.ui.tasks.TaskAdapter;
import ftn.ma.myapplication.ui.tasks.TaskDetailActivity;
import ftn.ma.myapplication.ui.tasks.TasksActivity;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class TasksCalendarActivity extends AppCompatActivity implements TaskAdapter.OnTaskListener {

    private CalendarView calendarView;
    private RecyclerView recyclerViewTasksForDay;
    private TextView textViewSelectedDateTasks;
    private TaskAdapter taskAdapter;

    private List<Task> allTasks = new ArrayList<>(); // Keširana lista svih zadataka
    private List<Task> displayedTasks = new ArrayList<>();
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    private ExecutorService executorService;
    private Calendar selectedCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_calendar);
        setTitle("Kalendar Zadataka");

        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        taskDao = database.taskDao();
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();

        calendarView = findViewById(R.id.calendarView);
        recyclerViewTasksForDay = findViewById(R.id.recyclerViewTasksForDay);
        textViewSelectedDateTasks = findViewById(R.id.textViewSelectedDateTasks);

        taskAdapter = new TaskAdapter(displayedTasks, taskDao, executorService, this);
        recyclerViewTasksForDay.setAdapter(taskAdapter);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedCalendar.set(year, month, dayOfMonth);
            // Sada samo filtriramo već učitanu listu, nema potrebe za novim pozivom baze
            filterAndDisplayTasksForDate();
        });
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllTasksFromDb();
    }

    private void loadAllTasksFromDb() {
        executorService.execute(() -> {
            allTasks.clear();
            List<Task> tasksFromDb = taskDao.getAllTasks();

            // --- NOVO: Pokrećemo proveru i ažuriranje statusa za zastarele zadatke ---
            updateStatusesForPastTasks(tasksFromDb);

            allTasks.addAll(tasksFromDb);

            List<Category> allCategories = categoryDao.getAllCategories();
            Map<Long, Category> categoryMap = allCategories.stream().collect(Collectors.toMap(Category::getId, c -> c));
            for (Task task : allTasks) {
                task.setCategory(categoryMap.get(task.getCategoryId()));
            }

            runOnUiThread(this::filterAndDisplayTasksForDate);
        });
    }

    // --- GLAVNA IZMENA: PRIKAZ ZADATAKA ---
    private void filterAndDisplayTasksForDate() {
        int year = selectedCalendar.get(Calendar.YEAR);
        int month = selectedCalendar.get(Calendar.MONTH);
        int dayOfMonth = selectedCalendar.get(Calendar.DAY_OF_MONTH);

        String selectedDateText = "Zadaci za: " + dayOfMonth + "." + (month + 1) + "." + year;
        textViewSelectedDateTasks.setText(selectedDateText);

        // NEMA VIŠE KOMPLEKSNE LOGIKE!
        // Jednostavno filtriramo listu svih zadataka i tražimo one čiji se executionTime poklapa sa izabranim danom
        List<Task> tasksForSelectedDate = allTasks.stream()
                .filter(task -> {
                    if (task.getExecutionTime() == null) return false;
                    Calendar taskCal = Calendar.getInstance();
                    taskCal.setTime(task.getExecutionTime());
                    return taskCal.get(Calendar.YEAR) == year &&
                            taskCal.get(Calendar.MONTH) == month &&
                            taskCal.get(Calendar.DAY_OF_MONTH) == dayOfMonth;
                })
                .collect(Collectors.toList());

        displayedTasks.clear();
        displayedTasks.addAll(tasksForSelectedDate);
        taskAdapter.notifyDataSetChanged();
    }

    private void setMidnight(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void updateStatusesForPastTasks(List<Task> allTasks) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Date threeDaysAgo = calendar.getTime();

        List<Task> tasksToUpdate = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.getStatus() == Task.Status.AKTIVAN &&
                    task.getExecutionTime() != null &&
                    task.getExecutionTime().before(threeDaysAgo)) {

                task.setStatus(Task.Status.NEURADJEN);
                tasksToUpdate.add(task);
            }
        }

        if (!tasksToUpdate.isEmpty()) {
            for (Task task : tasksToUpdate) {
                taskDao.update(task);
            }
        }
    }

    // --- IZMENA: Logika za zaključane zadatke sada omogućava "grace period" ---
    private boolean isTaskLocked(Task task) {
        if (task.getStatus() == Task.Status.URADJEN || task.getStatus() == Task.Status.OTKAZAN || task.getStatus() == Task.Status.NEURADJEN) {
            return true;
        }

        // Zadatak je zaključan samo ako je stariji od 3 dana
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Date gracePeriodLimit = calendar.getTime();

        return task.getExecutionTime() != null && task.getExecutionTime().before(gracePeriodLimit);
    }

    @Override
    public void onTaskClick(Task task) {
        // Vraćeno na originalno stanje po želji - otvara prikaz detalja
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra("VIEW_TASK", task);
        startActivity(intent);
    }

    @Override
    public void onTaskLongClick(Task task) {
        if (isTaskLocked(task)) {
            Toast.makeText(this, "Zadaci iz prošlosti se ne mogu menjati.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> options = new ArrayList<>();
        options.add("Izmeni"); // Uvek nudimo izmenu

        if (task.getRecurringGroupId() != null) {
            options.add("Obriši samo ovaj zadatak");
            options.add("Obriši ovaj i sve buduće");
        } else {
            options.add("Obriši");
        }
        options.add("Nazad");

        final CharSequence[] items = options.toArray(new CharSequence[0]);
        new AlertDialog.Builder(this)
                .setTitle("Izaberi akciju za: '" + task.getName() + "'")
                .setItems(items, (dialog, item) -> {
                    String selected = items[item].toString();
                    switch (selected) {
                        case "Izmeni":
                            Intent intent = new Intent(this, CreateEditTaskActivity.class);
                            intent.putExtra("EDIT_TASK", task);
                            if (task.getRecurringGroupId() != null) {
                                intent.putExtra("RECURRING_EDIT_DATE", task.getExecutionTime().getTime());
                            }
                            startActivity(intent);
                            break;
                        case "Obriši":
                        case "Obriši samo ovaj zadatak":
                            deleteTask(task, true);
                            break;
                        case "Obriši ovaj i sve buduće":
                            deleteTask(task, false);
                            break;
                        case "Nazad":
                            dialog.dismiss();
                            break;
                    }
                }).show();
    }

    private void deleteTask(Task task, boolean justThisOne) {
        executorService.execute(() -> {
            if (justThisOne || task.getRecurringGroupId() == null) {
                taskDao.delete(task);
            } else {
                taskDao.deleteFutureByGroupId(task.getRecurringGroupId(), task.getExecutionTime());
            }
            // Ponovo učitavamo SVE zadatke iz baze da bi se promene odrazile
            loadAllTasksFromDb();
        });
    }

    @Override
    public void onTaskCheckedChanged(Task task, boolean isChecked) {
        // ====================================================================================
        // NOVO: Provera da li je zadatak zakazan u budućnosti
        // Specifikacija: "...ne mogu se označiti kao urađeni zadaci zakazani u budućnosti."
        // ====================================================================================
        Date currentTime = new Date();
        if (isChecked && task.getExecutionTime() != null && task.getExecutionTime().after(currentTime)) {
            Toast.makeText(this, "Ne možete završiti zadatak pre njegovog vremena izvršenja.", Toast.LENGTH_SHORT).show();
            // Koristimo notifyDataSetChanged() jer je to siguran način da se UI osveži
            // i vrati checkbox u prvobitno stanje
            taskAdapter.notifyDataSetChanged();
            return;
        }

        // Provera da li je zadatak stariji od 3 dana (ova logika ostaje ista)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Date gracePeriodLimit = calendar.getTime();

        if (task.getExecutionTime() != null && task.getExecutionTime().before(gracePeriodLimit)) {
            Toast.makeText(this, "Ne možete menjati status zadataka starijih od 3 dana.", Toast.LENGTH_SHORT).show();
            taskAdapter.notifyDataSetChanged();
            return;
        }

        // Ako su sve provere prošle, nastavljamo sa standardnom logikom
        task.setStatus(isChecked ? Task.Status.URADJEN : Task.Status.AKTIVAN);
        if (isChecked && !task.isXpAwarded()) {
            task.setCompletionDate(task.getExecutionTime());
            awardXpForTask(task);
        } else {
            executorService.execute(() -> taskDao.update(task));
        }
    }

    private void awardXpForTask(Task completedTask) {
        executorService.execute(() -> {
            int userLevel = SharedPreferencesManager.getUserLevel(this);
            // Dobavljamo SVE zadatke koji su ikada označeni kao URAĐENI
            List<Task> allCompletedTasks = taskDao.getAllTasks().stream()
                    .filter(t -> t.getStatus() == Task.Status.URADJEN && t.getCompletionDate() != null)
                    .collect(Collectors.toList());

            int xpForDifficulty = 0;
            int xpForImportance = 0;
            String quotaMessage = "";

            final Date completionDate = completedTask.getCompletionDate();
            final Task.Difficulty difficulty = completedTask.getDifficulty();
            final Task.Importance importance = completedTask.getImportance();

            // --- Provera kvote za Težinu ---
            int difficultyBaseXp = LevelingManager.getDynamicXpForDifficulty(difficulty, userLevel);
            if (difficulty == Task.Difficulty.EKSTREMNO_TEZAK) { // Max 1 nedeljno
                long count = allCompletedTasks.stream()
                        .filter(t -> t.getId() != completedTask.getId() &&
                                t.getDifficulty() == difficulty &&
                                isSameWeek(t.getCompletionDate(), completionDate))
                        .count();
                if (count < 1) xpForDifficulty = difficultyBaseXp; else quotaMessage += "Ispunjena nedeljna kvota za Težinu! ";
            } else { // Dnevne kvote
                long count = allCompletedTasks.stream()
                        .filter(t -> t.getId() != completedTask.getId() &&
                                t.getDifficulty() == difficulty &&
                                isSameDay(t.getCompletionDate(), completionDate))
                        .count();
                int limit;
                if (difficulty == Task.Difficulty.TEZAK) limit = 2; // Težak max 2 dnevno
                else limit = 5; // Veoma lak i Lak max 5 dnevno

                if (count < limit) xpForDifficulty = difficultyBaseXp; else quotaMessage += "Ispunjena dnevna kvota za Težinu! ";
            }

            // --- Provera kvote za Bitnost ---
            int importanceBaseXp = LevelingManager.getDynamicXpForImportance(importance, userLevel);
            if (importance == Task.Importance.SPECIJALAN) { // Max 1 mesečno
                long count = allCompletedTasks.stream()
                        .filter(t -> t.getId() != completedTask.getId() &&
                                t.getImportance() == importance &&
                                isSameMonth(t.getCompletionDate(), completionDate))
                        .count();
                if (count < 1) xpForImportance = importanceBaseXp; else quotaMessage += "Ispunjena mesečna kvota za Bitnost! ";
            } else { // Dnevne kvote
                long count = allCompletedTasks.stream()
                        .filter(t -> t.getId() != completedTask.getId() &&
                                t.getImportance() == importance &&
                                isSameDay(t.getCompletionDate(), completionDate))
                        .count();
                int limit;
                if (importance == Task.Importance.EKSTREMNO_VAZAN) limit = 2; // Ekstremno važan max 2 dnevno
                else limit = 5; // Normalan i Važan max 5 dnevno

                if (count < limit) xpForImportance = importanceBaseXp; else quotaMessage += "Ispunjena dnevna kvota za Bitnost! ";
            }

            // Ostatak metode je isti...
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

            if (finalTotalXpGained > 0) {
                completedTask.setXpAwarded(true);
            }
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
            // Prvo, izračunavamo snagu za borbu, a to je snaga PRETHODNOG nivoa
            int previousLevel = currentLevel - 1;
            int ppForFight = LevelingManager.calculateTotalPpForLevel(previousLevel);

            // Zatim, izračunavamo i čuvamo NOVU snagu koju će korisnik imati NAKON borbe
            int newPpForNextStage = LevelingManager.calculateTotalPpForLevel(currentLevel);
            SharedPreferencesManager.saveUserPp(this, newPpForNextStage);
            SharedPreferencesManager.saveUserLevel(this, currentLevel); // Čuvamo i novi nivo

            Toast.makeText(this, "ČESTITAMO! Prešli ste na NIVO " + currentLevel + "!", Toast.LENGTH_LONG).show();

            // Pokrećemo borbu
            Intent intent = new Intent(TasksCalendarActivity.this, BossFightActivity.class);
            intent.putExtra("USER_LEVEL", currentLevel);    // Prosleđujemo novi nivo
            intent.putExtra("USER_PP", ppForFight);       // ALI prosleđujemo STARU snagu za borbu
            startActivity(intent);
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

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        // Postavljamo da je "Zadaci" ikonica selektovana na ovom ekranu
        bottomNav.setSelectedItemId(R.id.navigation_calendar);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_calendar) {
                // Već smo na ovom ekranu, ne radi ništa
                return true;
            } else if (itemId == R.id.navigation_tasks) {
                // Pokreni TasksCalendarActivity
                startActivity(new Intent(getApplicationContext(), TasksActivity.class));
                // Dodaj animaciju da prelaz bude lepši
                overridePendingTransition(0, 0);
                finish(); // Zatvori trenutnu aktivnost
                return true;
            } else if (itemId == R.id.navigation_categories) {
                // Pokreni CategoriesActivity
                startActivity(new Intent(getApplicationContext(), CategoriesActivity.class));
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
