package ftn.ma.myapplication.ui.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import ftn.ma.myapplication.ui.ProfileActivity;
import ftn.ma.myapplication.ui.calendar.TasksCalendarActivity;
import ftn.ma.myapplication.ui.categories.CategoriesActivity;
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
        setupBottomNavigation();
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
            // Prvo dobavljamo sve zadatke iz baze
            List<Task> tasksFromDb = taskDao.getAllTasks();

            // --- NOVO: Pokrećemo proveru i ažuriranje statusa za zastarele zadatke ---
            updateStatusesForPastTasks(tasksFromDb);

            Date today = getTodayAtMidnight();

            // Filtriramo da se prikažu samo trenutni i budući zadaci
            List<Task> currentAndFutureTasks = tasksFromDb.stream()
                    .filter(task -> task.getExecutionTime() != null && !task.getExecutionTime().before(today))
                    .collect(Collectors.toList());

            // Ostatak filtriranja po tabovima i prikazivanje ostaje isti
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

    private void updateStatusesForPastTasks(List<Task> allTasks) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3); // Postavljamo granicu na 3 dana u prošlosti
        Date threeDaysAgo = calendar.getTime();

        List<Task> tasksToUpdate = new ArrayList<>();
        for (Task task : allTasks) {
            // Proveravamo samo aktivne zadatke koji su stariji od 3 dana
            if (task.getStatus() == Task.Status.AKTIVAN &&
                    task.getExecutionTime() != null &&
                    task.getExecutionTime().before(threeDaysAgo)) {

                task.setStatus(Task.Status.NEURADJEN);
                tasksToUpdate.add(task);
            }
        }

        // Ako ima zadataka za ažuriranje, radimo to u bazi
        if (!tasksToUpdate.isEmpty()) {
            for (Task task : tasksToUpdate) {
                taskDao.update(task);
            }
        }
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
        // Provera statusa ostaje ista
        if (task.getStatus() == Task.Status.URADJEN || task.getStatus() == Task.Status.OTKAZAN || task.getStatus() == Task.Status.NEURADJEN) {
            return true;
        }

        // Zadatak je zaključan samo ako je stariji od 3 dana
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Date gracePeriodLimit = calendar.getTime();

        return task.getExecutionTime() != null && task.getExecutionTime().before(gracePeriodLimit);
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
        // ====================================================================================
        // NOVO: Provera da li je zadatak zakazan u budućnosti
        // Specifikacija: "...ne mogu se označiti kao urađeni zadaci zakazani u budućnosti."
        // ====================================================================================
        Date currentTime = new Date();
        if (isChecked && task.getExecutionTime() != null && task.getExecutionTime().after(currentTime)) {
            Toast.makeText(this, "Ne možete završiti zadatak pre njegovog vremena izvršenja.", Toast.LENGTH_SHORT).show();
            // Vraćamo checkbox u prvobitno (nečekirano) stanje
            adapter.notifyItemChanged(taskList.indexOf(task));
            return;
        }

        // Provera da li je zadatak stariji od 3 dana (ova logika ostaje ista)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Date gracePeriodLimit = calendar.getTime();

        if (task.getExecutionTime() != null && task.getExecutionTime().before(gracePeriodLimit)) {
            Toast.makeText(this, "Ne možete menjati status zadataka starijih od 3 dana.", Toast.LENGTH_SHORT).show();
            adapter.notifyItemChanged(taskList.indexOf(task));
            return;
        }

        // Ako su sve provere prošle, nastavljamo sa standardnom logikom
        task.setStatus(isChecked ? Task.Status.URADJEN : Task.Status.AKTIVAN);
        if (isChecked && !task.isXpAwarded()) {
            task.setCompletionDate(task.getExecutionTime());
            awardXpForTask(task);
        } else {
            // Ako korisnik odčekira zadatak, samo ažuriramo status
            executorService.execute(() -> taskDao.update(task));
        }
    }

    private void awardXpForTask(Task completedTask) {
        executorService.execute(() -> {
            recordSpecialMissionProgressForTask(completedTask);
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
            Intent intent = new Intent(TasksActivity.this, BossFightActivity.class);
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

    // --- NOVA METODA ZA NAVIGACIJU ---
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        // Postavljamo da je "Zadaci" ikonica selektovana na ovom ekranu
        bottomNav.setSelectedItemId(R.id.navigation_tasks);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_tasks) {
                // Već smo na ovom ekranu, ne radi ništa
                return true;
            } else if (itemId == R.id.navigation_calendar) {
                // Pokreni TasksCalendarActivity
                startActivity(new Intent(getApplicationContext(), TasksCalendarActivity.class));
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

    /**
     * NOVO: Metoda koja proverava da li je specijalna misija aktivna
     * i beleži rešen zadatak ako jeste.
     */
    private void recordSpecialMissionProgressForTask(Task completedTask) {
        // 1. Proveri da li je ijedna misija aktivna
        int activeMissionId = SharedPreferencesManager.getActiveMissionId(this);
        if (activeMissionId == -1) {
            return; // Nijedna misija nije aktivna
        }

        final String userName = "Ja (student)";
        String actionKey;
        int maxCount;

        // 2. Odredi u koju kategoriju spada zadatak
        Task.Difficulty difficulty = completedTask.getDifficulty();
        Task.Importance importance = completedTask.getImportance();

        // Pravilo iz specifikacije: "Rešavanje veoma lakog, lakog, normalnog ili važnog zadatka"
        if (difficulty == Task.Difficulty.VEOMA_LAK || difficulty == Task.Difficulty.LAK ||
                importance == Task.Importance.NORMALAN || importance == Task.Importance.VAZAN) {
            actionKey = "easy_task";
            maxCount = 10;
        } else {
            // Svi ostali spadaju u drugu grupu: "Rešavanje ostalih zadataka"
            actionKey = "hard_task";
            maxCount = 6;
        }

        // 3. Proveri kvotu i ažuriraj ako je potrebno
        int currentCount = SharedPreferencesManager.getMemberActionCount(this, activeMissionId, userName, actionKey);
        if (currentCount < maxCount) {
            currentCount++;
            SharedPreferencesManager.saveMemberActionCount(this, activeMissionId, userName, actionKey, currentCount);

            // 4. Obavesti korisnika (runOnUiThread je potreban jer smo u pozadinskoj niti)
            runOnUiThread(() -> Toast.makeText(this, "Napredak za specijalnu misiju zabeležen!", Toast.LENGTH_SHORT).show());
        }
    }
}