package ftn.ma.myapplication.ui.tasks;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.data.model.Task;
import ftn.ma.myapplication.domain.LevelingManager;
import ftn.ma.myapplication.ui.game.BossFightActivity;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView textViewDetailTaskName, textViewDetailTaskDescription, textViewDetailCategory;
    private TextView textViewDetailDifficulty, textViewDetailImportance;
    private LinearLayout detailRecurringLayout, detailOneTimeLayout;
    private TextView textViewDetailRecurringInfo, textViewDetailExecutionDate;
    private Spinner spinnerStatus;
    private FloatingActionButton fabEditTask;

    private Task currentTask;
    private TaskDao taskDao;
    private ExecutorService executorService;
    private boolean isSpinnerInitialSetup = true;
    private SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("d.M.yyyy 'u' HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        taskDao = AppDatabase.getDatabase(this).taskDao();
        executorService = Executors.newSingleThreadExecutor();

        bindViews();

        currentTask = (Task) getIntent().getSerializableExtra("VIEW_TASK");
        if (currentTask == null) {
            Toast.makeText(this, "Greška: Zadatak nije pronađen.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateData();
        setupStatusSpinner();

        fabEditTask.setOnClickListener(v -> {
            Intent intent = new Intent(TaskDetailActivity.this, CreateEditTaskActivity.class);
            intent.putExtra("EDIT_TASK", currentTask);
            if (currentTask.getRecurringGroupId() != null) {
                intent.putExtra("RECURRING_EDIT_DATE", currentTask.getExecutionTime().getTime());
            }
            startActivity(intent);
            finish();
        });
    }

    private void bindViews() {
        textViewDetailTaskName = findViewById(R.id.textViewDetailTaskName);
        textViewDetailTaskDescription = findViewById(R.id.textViewDetailTaskDescription);
        textViewDetailCategory = findViewById(R.id.textViewDetailCategory);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        fabEditTask = findViewById(R.id.fabEditTask);
        textViewDetailDifficulty = findViewById(R.id.textViewDetailDifficulty);
        textViewDetailImportance = findViewById(R.id.textViewDetailImportance);
        detailRecurringLayout = findViewById(R.id.detailRecurringLayout);
        detailOneTimeLayout = findViewById(R.id.detailOneTimeLayout);
        textViewDetailRecurringInfo = findViewById(R.id.textViewDetailRecurringInfo);
        // Uklonili smo textViewDetailRecurringRange jer više nema smisla
        textViewDetailExecutionDate = findViewById(R.id.textViewDetailExecutionDate);
    }

    private void populateData() {
        setTitle("Detalji zadatka");
        textViewDetailTaskName.setText(currentTask.getName());

        if (currentTask.getDescription() != null && !currentTask.getDescription().isEmpty()) {
            findViewById(R.id.textViewDetailTaskDescriptionLabel).setVisibility(View.VISIBLE);
            textViewDetailTaskDescription.setVisibility(View.VISIBLE);
            textViewDetailTaskDescription.setText(currentTask.getDescription());
        } else {
            findViewById(R.id.textViewDetailTaskDescriptionLabel).setVisibility(View.GONE);
            textViewDetailTaskDescription.setVisibility(View.GONE);
        }

        if (currentTask.getCategory() != null) {
            textViewDetailCategory.setText(currentTask.getCategory().getName());
        } else {
            textViewDetailCategory.setText("Nema kategoriju");
        }

        textViewDetailDifficulty.setText(currentTask.getDifficulty().name());
        textViewDetailImportance.setText(currentTask.getImportance().name());

        detailOneTimeLayout.setVisibility(View.VISIBLE);
        if (currentTask.getExecutionTime() != null) {
            textViewDetailExecutionDate.setText(dateTimeFormatter.format(currentTask.getExecutionTime()));
        }

        if (currentTask.getRecurringGroupId() != null) {
            detailRecurringLayout.setVisibility(View.VISIBLE);
            textViewDetailRecurringInfo.setText("Ovo je deo ponavljajućeg zadatka.");
            findViewById(R.id.textViewDetailRecurringRange).setVisibility(View.GONE);
        } else {
            detailRecurringLayout.setVisibility(View.GONE);
        }

        if (isTaskLocked(currentTask)) {
            fabEditTask.setVisibility(View.GONE);
            spinnerStatus.setEnabled(false);
        } else {
            fabEditTask.setVisibility(View.VISIBLE);
            spinnerStatus.setEnabled(true);
        }
    }

    // --- IZMENA: Logika za zaključavanje sada omogućava "grace period" od 3 dana ---
    private boolean isTaskLocked(Task task) {
        if (task.getStatus() == Task.Status.URADJEN ||
                task.getStatus() == Task.Status.OTKAZAN ||
                task.getStatus() == Task.Status.NEURADJEN) {
            return true;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Date gracePeriodLimit = calendar.getTime();

        return task.getExecutionTime() != null && task.getExecutionTime().before(gracePeriodLimit);
    }

    private void setupStatusSpinner() {
        List<Task.Status> statusOptions = new ArrayList<>(Arrays.asList(Task.Status.values()));
        // Ako je zadatak jednokratan (nema grupu), uklanjamo opciju "PAUZIRAN"
        if (currentTask.getRecurringGroupId() == null) {
            statusOptions.remove(Task.Status.PAUZIRAN);
        }

        ArrayAdapter<Task.Status> statusAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        if (currentTask.getStatus() != null) {
            spinnerStatus.setSelection(statusOptions.indexOf(currentTask.getStatus()));
        }

        spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isSpinnerInitialSetup) {
                    isSpinnerInitialSetup = false;
                    return;
                }
                Task.Status newStatus = (Task.Status) parent.getItemAtPosition(position);
                if (currentTask.getStatus() != newStatus) {
                    currentTask.setStatus(newStatus);
                    if (newStatus == Task.Status.URADJEN && !currentTask.isXpAwarded()) {
                        currentTask.setCompletionDate(currentTask.getExecutionTime());
                        awardXpForTask(currentTask);
                    } else {
                        executorService.execute(() -> taskDao.update(currentTask));
                        Toast.makeText(TaskDetailActivity.this, "Status promenjen u: " + newStatus.name(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
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
            Intent intent = new Intent(TaskDetailActivity.this, BossFightActivity.class);
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
}
