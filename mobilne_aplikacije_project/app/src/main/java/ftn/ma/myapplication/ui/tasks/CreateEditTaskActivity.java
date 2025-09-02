package ftn.ma.myapplication.ui.tasks;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.CategoryDao;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.data.model.Category;
import ftn.ma.myapplication.data.model.Task;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.app.DatePickerDialog;
import java.util.Calendar;
import java.util.Date;
public class CreateEditTaskActivity extends AppCompatActivity {

    private EditText editTextTaskName;
    private EditText editTextTaskDescription;
    private Spinner spinnerCategory;
    private Spinner spinnerDifficulty;
    private Spinner spinnerImportance;
    private Button buttonSaveTask;
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    private ExecutorService executorService;
    private CheckBox checkBoxRecurring;
    private LinearLayout recurringOptionsLayout;
    private EditText editTextInterval;
    private Spinner spinnerRepetitionUnit;
    private Task taskToEdit = null;
    private TextView textViewSelectedDate;
    private Button buttonPickDate;
    private Calendar selectedDateCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_task);

        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        taskDao = database.taskDao();
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();

        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerImportance = findViewById(R.id.spinnerImportance);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);

        bindViews(); // Sada ova metoda povezuje i nove elemente

        if (getIntent().hasExtra("EDIT_TASK")) {
            taskToEdit = (Task) getIntent().getSerializableExtra("EDIT_TASK");
            setTitle("Izmeni zadatak");
            // Postavljamo datum ako smo u edit modu
            if (taskToEdit.getExecutionTime() != null) {
                selectedDateCalendar.setTime(taskToEdit.getExecutionTime());
            }
        } else {
            setTitle("Dodaj novi zadatak");
        }

        updateDateTextView(); // Prikazujemo početni datum
        setupSpinners();
        setupRecurringOptions();

        buttonSaveTask.setOnClickListener(v -> saveTask());
        buttonPickDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void bindViews() {
        editTextTaskName = findViewById(R.id.editTextTaskName);
        editTextTaskDescription = findViewById(R.id.editTextTaskDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerImportance = findViewById(R.id.spinnerImportance);
        buttonSaveTask = findViewById(R.id.buttonSaveTask);
        // NOVO
        checkBoxRecurring = findViewById(R.id.checkBoxRecurring);
        recurringOptionsLayout = findViewById(R.id.recurringOptionsLayout);
        editTextInterval = findViewById(R.id.editTextInterval);
        spinnerRepetitionUnit = findViewById(R.id.spinnerRepetitionUnit);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        buttonPickDate = findViewById(R.id.buttonPickDate);
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateCalendar.set(year, month, dayOfMonth);
                    updateDateTextView();
                },
                selectedDateCalendar.get(Calendar.YEAR),
                selectedDateCalendar.get(Calendar.MONTH),
                selectedDateCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // NOVA METODA
    private void updateDateTextView() {
        String dateText = selectedDateCalendar.get(Calendar.DAY_OF_MONTH) + "." +
                (selectedDateCalendar.get(Calendar.MONTH) + 1) + "." +
                selectedDateCalendar.get(Calendar.YEAR);
        textViewSelectedDate.setText(dateText);
    }

    private void setupSpinners() {
        // Popunjavanje za Difficulty i Importance
        ArrayAdapter<Task.Difficulty> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.Difficulty.values());
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);

        ArrayAdapter<Task.Importance> importanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.Importance.values());
        importanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerImportance.setAdapter(importanceAdapter);

        // Učitavamo stvarne kategorije iz baze podataka
        executorService.execute(() -> {
            List<Category> categoriesFromDb = categoryDao.getAllCategories();
            runOnUiThread(() -> {
                ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesFromDb);
                categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategory.setAdapter(categoryAdapter);
                if (taskToEdit != null) {
                    populateForm(taskToEdit);
                }
            });
        });

        ArrayAdapter<Task.RepetitionUnit> repetitionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.RepetitionUnit.values());
        repetitionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRepetitionUnit.setAdapter(repetitionAdapter);
    }

    private void setupRecurringOptions() {
        checkBoxRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            recurringOptionsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
    }

    private void populateForm(Task task) {
        editTextTaskName.setText(task.getName());
        editTextTaskDescription.setText(task.getDescription());
        buttonSaveTask.setText("Sačuvaj izmene");

        // Postavljanje selekcije za spinere (malo je komplikovanije)
        ArrayAdapter<Category> categoryAdapter = (ArrayAdapter<Category>) spinnerCategory.getAdapter();
        for (int i = 0; i < categoryAdapter.getCount(); i++) {
            if (categoryAdapter.getItem(i).getId() == task.getCategoryId()) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        spinnerDifficulty.setSelection(task.getDifficulty().ordinal());
        spinnerImportance.setSelection(task.getImportance().ordinal());

        checkBoxRecurring.setChecked(task.isRecurring());
        if (task.getExecutionTime() != null) {
            selectedDateCalendar.setTime(task.getExecutionTime());
            updateDateTextView();
        }

        if (task.isRecurring()) {
            recurringOptionsLayout.setVisibility(View.VISIBLE);
            editTextInterval.setText(String.valueOf(task.getRepetitionInterval()));
            spinnerRepetitionUnit.setSelection(task.getRepetitionUnit().ordinal());
        }
    }

    private void saveTask() {
        String name = editTextTaskName.getText().toString();
        if (name.trim().isEmpty()) {
            Toast.makeText(this, "Molimo unesite naziv zadatka", Toast.LENGTH_SHORT).show();
            return;
        }

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        if (selectedCategory == null) {
            Toast.makeText(this, "Molimo izaberite kategoriju", Toast.LENGTH_SHORT).show();
            return;
        }

        final Task task = (taskToEdit == null) ? new Task() : taskToEdit;

        task.setName(name);
        task.setDescription(editTextTaskDescription.getText().toString());
        task.setDifficulty((Task.Difficulty) spinnerDifficulty.getSelectedItem());
        task.setImportance((Task.Importance) spinnerImportance.getSelectedItem());
        task.setStatus(task.getStatus() == null ? Task.Status.AKTIVAN : task.getStatus()); // Zadržavamo status ako postoji
        task.setCategoryId(selectedCategory.getId());

        task.setRecurring(checkBoxRecurring.isChecked());
        if (checkBoxRecurring.isChecked()) {
            String intervalStr = editTextInterval.getText().toString();
            task.setRepetitionInterval(intervalStr.isEmpty() ? 1 : Integer.parseInt(intervalStr));
            task.setRepetitionUnit((Task.RepetitionUnit) spinnerRepetitionUnit.getSelectedItem());
        }

        task.setExecutionTime(selectedDateCalendar.getTime());
        // Za ponavljajuće zadatke, postavljamo i početni i krajnji datum
        // (Za sada, stavićemo da je početni datum izabrani, a krajnji npr. za godinu dana)
        if (checkBoxRecurring.isChecked()) {
            task.setStartDate(selectedDateCalendar.getTime());
            Calendar endCal = (Calendar) selectedDateCalendar.clone();
            endCal.add(Calendar.YEAR, 1);
            task.setEndDate(endCal.getTime());
        }

        executorService.execute(() -> {
            if (taskToEdit == null) {
                taskDao.insert(task); // Ako je novi, radi INSERT
            } else {
                taskDao.update(task); // Ako je postojeći, radi UPDATE
            }
            finish();
        });
    }
}