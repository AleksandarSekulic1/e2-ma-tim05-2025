package ftn.ma.myapplication.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.CategoryDao;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.data.model.Category;
import ftn.ma.myapplication.data.model.Task;
import android.app.TimePickerDialog;
public class CreateEditTaskActivity extends AppCompatActivity {

    // UI Elementi
    private EditText editTextTaskName, editTextTaskDescription;
    private Spinner spinnerCategory, spinnerDifficulty, spinnerImportance;
    private Button buttonSaveTask;
    private LinearLayout oneTimeTaskDateLayout;
    private TextView textViewSelectedDate;
    private Button buttonPickDate;
    private CheckBox checkBoxRecurring;
    private LinearLayout recurringOptionsLayout;
    private EditText editTextInterval;
    private Spinner spinnerRepetitionUnit;
    private TextView textViewStartDate, textViewEndDate;
    private Button buttonPickStartDate, buttonPickEndDate;

    // Logika i podaci
    private Task taskToEdit = null;
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());
    private Calendar executionDateCalendar = Calendar.getInstance();
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private Button buttonPickTime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_task);

        // Inicijalizacija
        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        taskDao = database.taskDao();
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();

        bindViews();
        setupListeners();

        // Provera da li smo u "Edit" modu i popunjavanje
        if (getIntent().hasExtra("EDIT_TASK")) {
            taskToEdit = (Task) getIntent().getSerializableExtra("EDIT_TASK");
            setTitle("Izmeni zadatak");
        } else {
            setTitle("Dodaj novi zadatak");
            // Postavljamo da je kraj ponavljanja za mesec dana od danas kao default
            endDateCalendar.add(Calendar.MONTH, 1);
        }

        setupSpinners();
    }

    private void bindViews() {
        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerImportance = findViewById(R.id.spinnerImportance);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);
        oneTimeTaskDateLayout = findViewById(R.id.oneTimeTaskDateLayout);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        buttonPickDate = findViewById(R.id.buttonPickDate);
        checkBoxRecurring = findViewById(R.id.checkBoxRecurring);
        recurringOptionsLayout = findViewById(R.id.recurringOptionsLayout);
        editTextInterval = findViewById(R.id.editTextInterval);
        spinnerRepetitionUnit = findViewById(R.id.spinnerRepetitionUnit);
        textViewStartDate = findViewById(R.id.textViewStartDate);
        textViewEndDate = findViewById(R.id.textViewEndDate);
        buttonPickStartDate = findViewById(R.id.buttonPickStartDate);
        buttonPickEndDate = findViewById(R.id.buttonPickEndDate);
        buttonPickTime = findViewById(R.id.buttonPickTime);
    }

    private void setupListeners() {
        buttonSaveTask.setOnClickListener(v -> saveTask());
        buttonPickDate.setOnClickListener(v -> showDatePickerDialog(executionDateCalendar, textViewSelectedDate));
        buttonPickStartDate.setOnClickListener(v -> showDatePickerDialog(startDateCalendar, textViewStartDate));
        buttonPickEndDate.setOnClickListener(v -> showDatePickerDialog(endDateCalendar, textViewEndDate));
        buttonPickTime.setOnClickListener(v -> showTimePickerDialog());

        checkBoxRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            recurringOptionsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            oneTimeTaskDateLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });
    }
    private void showTimePickerDialog() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            executionDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            executionDateCalendar.set(Calendar.MINUTE, minute);
            updateAllDateTextViews(); // Osvežavamo prikaz da uključi i vreme
        }, executionDateCalendar.get(Calendar.HOUR_OF_DAY), executionDateCalendar.get(Calendar.MINUTE), true).show();
    }
    private void setupSpinners() {
        ArrayAdapter<Task.Difficulty> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.Difficulty.values());
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);

        ArrayAdapter<Task.Importance> importanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.Importance.values());
        importanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerImportance.setAdapter(importanceAdapter);

        ArrayAdapter<Task.RepetitionUnit> repetitionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.RepetitionUnit.values());
        repetitionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepetitionUnit.setAdapter(repetitionAdapter);

        executorService.execute(() -> {
            List<Category> categoriesFromDb = categoryDao.getAllCategories();
            runOnUiThread(() -> {
                ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesFromDb);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(categoryAdapter);

                if (taskToEdit != null) {
                    populateForm(taskToEdit);
                } else {
                    updateAllDateTextViews();
                }
            });
        });
    }

    private void showDatePickerDialog(Calendar calendarToUpdate, TextView textViewToUpdate) {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendarToUpdate.set(year, month, dayOfMonth);
            textViewToUpdate.setText(dateFormatter.format(calendarToUpdate.getTime()));
        }, calendarToUpdate.get(Calendar.YEAR), calendarToUpdate.get(Calendar.MONTH), calendarToUpdate.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void populateForm(Task task) {
        editTextTaskName.setText(task.getName());
        editTextTaskDescription.setText(task.getDescription());
        buttonSaveTask.setText("Sačuvaj izmene");

        ArrayAdapter<Category> categoryAdapter = (ArrayAdapter<Category>) spinnerCategory.getAdapter();
        for (int i = 0; i < categoryAdapter.getCount(); i++) {
            if (categoryAdapter.getItem(i).getId() == task.getCategoryId()) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
        spinnerDifficulty.setSelection(task.getDifficulty().ordinal());
        spinnerImportance.setSelection(task.getImportance().ordinal());

        if (task.getExecutionTime() != null) executionDateCalendar.setTime(task.getExecutionTime());
        if (task.getStartDate() != null) startDateCalendar.setTime(task.getStartDate());
        if (task.getEndDate() != null) endDateCalendar.setTime(task.getEndDate());
        updateAllDateTextViews();

        checkBoxRecurring.setChecked(task.isRecurring());
        recurringOptionsLayout.setVisibility(task.isRecurring() ? View.VISIBLE : View.GONE);
        oneTimeTaskDateLayout.setVisibility(task.isRecurring() ? View.GONE : View.VISIBLE);

        if (task.isRecurring()) {
            editTextInterval.setText(String.valueOf(task.getRepetitionInterval()));
            spinnerRepetitionUnit.setSelection(task.getRepetitionUnit().ordinal());
        }
    }

    private void updateAllDateTextViews() {
        textViewSelectedDate.setText(dateFormatter.format(executionDateCalendar.getTime()));
        textViewStartDate.setText(dateFormatter.format(startDateCalendar.getTime()));
        textViewEndDate.setText(dateFormatter.format(endDateCalendar.getTime()));
    }

    private void saveTask() {
        String name = editTextTaskName.getText().toString();
        if (name.trim().isEmpty()) {
            Toast.makeText(this, "Molimo unesite naziv zadatka", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        if (selectedCategory == null) {
            Toast.makeText(this, "Molimo izaberite kategoriju ili dodajte novu.", Toast.LENGTH_SHORT).show();
            return;
        }

        final Task task = (taskToEdit == null) ? new Task() : taskToEdit;
        task.setName(name);
        task.setDescription(editTextTaskDescription.getText().toString());
        task.setDifficulty((Task.Difficulty) spinnerDifficulty.getSelectedItem());
        task.setImportance((Task.Importance) spinnerImportance.getSelectedItem());
        task.setStatus(task.getStatus() == null ? Task.Status.AKTIVAN : task.getStatus());
        task.setCategoryId(selectedCategory.getId());

        task.setRecurring(checkBoxRecurring.isChecked());
        if (task.isRecurring()) {
            if (startDateCalendar.after(endDateCalendar)) {
                Toast.makeText(this, "Datum početka mora biti pre datuma završetka.", Toast.LENGTH_SHORT).show();
                return;
            }
            String intervalStr = editTextInterval.getText().toString();
            task.setRepetitionInterval(intervalStr.isEmpty() ? 1 : Integer.parseInt(intervalStr));
            task.setRepetitionUnit((Task.RepetitionUnit) spinnerRepetitionUnit.getSelectedItem());
            task.setStartDate(startDateCalendar.getTime());
            task.setEndDate(endDateCalendar.getTime());
            task.setExecutionTime(null);
        } else {
            task.setExecutionTime(executionDateCalendar.getTime());
            task.setStartDate(null);
            task.setEndDate(null);
        }

        executorService.execute(() -> {
            if (taskToEdit == null) taskDao.insert(task);
            else taskDao.update(task);
            finish();
        });
    }
}