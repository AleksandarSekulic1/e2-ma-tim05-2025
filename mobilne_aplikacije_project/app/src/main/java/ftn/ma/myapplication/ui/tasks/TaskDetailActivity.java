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
            List<Task> allTasks = taskDao.getAllTasks();
            int xpForDifficulty = 0;
            int xpForImportance = 0;
            String quotaMessage = "";

            final Date referenceDate = (completedTask.getCompletionDate() == null) ? new Date() : completedTask.getCompletionDate();
            final Task.Difficulty difficulty = completedTask.getDifficulty();
            final Task.Importance importance = completedTask.getImportance();

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
            Intent intent = new Intent(this, BossFightActivity.class);
            intent.putExtra("USER_LEVEL", currentLevel);
            intent.putExtra("USER_PP", newPp);
            startActivity(intent);
        }
    }

    private int getXpForDifficulty(Task.Difficulty difficulty) {
        if (difficulty == null) return 0;
        switch (difficulty) {
            case VEOMA_LAK: return 1;
            case LAK: return 3;
            case TEZAK: return 7;
            case EKSTREMNO_TEZAK: return 20;
            default: return 0;
        }
    }

    private int getXpForImportance(Task.Importance importance) {
        if (importance == null) return 0;
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
