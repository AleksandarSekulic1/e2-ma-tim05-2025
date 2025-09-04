package ftn.ma.myapplication.ui.tasks;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

public class CreateEditTaskActivity extends AppCompatActivity {

    private enum RepetitionUnit {
        DAN,
        NEDELJA
    }

    private EditText editTextTaskName, editTextTaskDescription;
    private Spinner spinnerCategory, spinnerDifficulty, spinnerImportance;
    private Button buttonSaveTask;
    private LinearLayout oneTimeTaskDateLayout;
    private TextView textViewSelectedDate;
    private Button buttonPickDate, buttonPickTime;
    private CheckBox checkBoxRecurring;
    private LinearLayout recurringOptionsLayout;
    private EditText editTextInterval;
    private Spinner spinnerRepetitionUnit;
    private TextView textViewStartDate, textViewEndDate;
    private Button buttonPickStartDate, buttonPickEndDate;

    private Task taskToEdit = null;
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    private ExecutorService executorService;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    private Calendar executionDateCalendar = Calendar.getInstance();
    private Calendar startDateCalendar = Calendar.getInstance();
    private Calendar endDateCalendar = Calendar.getInstance();
    private long recurringEditDateMillis = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_task);

        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        taskDao = database.taskDao();
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();

        bindViews();
        setupListeners();

        if (getIntent().hasExtra("EDIT_TASK")) {
            taskToEdit = (Task) getIntent().getSerializableExtra("EDIT_TASK");
            recurringEditDateMillis = getIntent().getLongExtra("RECURRING_EDIT_DATE", -1);

            // --- IZMENA: Ispravljena logika prikaza za "edit" mod ---
            if (taskToEdit.getRecurringGroupId() != null) {
                // Ako menjamo zadatak iz serije, moramo prikazati opcije za ponavljanje
                // da bi korisnik mogao da definiše NOVU seriju za budućnost.
                setTitle("Izmeni buduće zadatke");
                checkBoxRecurring.setVisibility(View.GONE); // Sakrivamo checkbox, jer je odluka već doneta
                recurringOptionsLayout.setVisibility(View.VISIBLE);
                oneTimeTaskDateLayout.setVisibility(View.GONE);
                Toast.makeText(this, "Unesite nove podatke i pravila za buduća ponavljanja.", Toast.LENGTH_LONG).show();
            } else {
                // Ako menjamo običan, jednokratni zadatak
                setTitle("Izmeni zadatak");
                checkBoxRecurring.setVisibility(View.GONE);
                recurringOptionsLayout.setVisibility(View.GONE);
                oneTimeTaskDateLayout.setVisibility(View.VISIBLE);
            }

        } else {
            setTitle("Dodaj novi zadatak");
            endDateCalendar.add(Calendar.MONTH, 1);
        }

        setupSpinners();
    }

    private void bindViews() {
        // ... (nepromenjeno)
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
        // ... (nepromenjeno)
        buttonSaveTask.setOnClickListener(v -> saveTask());
        buttonPickDate.setOnClickListener(v -> showDatePickerDialog(executionDateCalendar, textViewSelectedDate));
        buttonPickStartDate.setOnClickListener(v -> showDatePickerDialog(startDateCalendar, textViewStartDate));
        buttonPickEndDate.setOnClickListener(v -> showDatePickerDialog(endDateCalendar, textViewEndDate));
        // Ovaj poziv je pravio grešku jer metoda ispod nije postojala
        buttonPickTime.setOnClickListener(v -> showTimePickerDialog());
        checkBoxRecurring.setOnCheckedChangeListener((buttonView, isChecked) -> {
            recurringOptionsLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            oneTimeTaskDateLayout.setVisibility(isChecked ? View.GONE : View.VISIBLE);
        });
    }

    private void setupSpinners() {
        // ... (nepromenjeno)
        ArrayAdapter<Task.Difficulty> difficultyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.Difficulty.values());
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);

        ArrayAdapter<Task.Importance> importanceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Task.Importance.values());
        importanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerImportance.setAdapter(importanceAdapter);

        ArrayAdapter<RepetitionUnit> repetitionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, RepetitionUnit.values());
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

        // --- IZMENA: Prilagođeno popunjavanje datuma ---
        if (task.getRecurringGroupId() != null && recurringEditDateMillis != -1) {
            // Za izmenu serije, postavljamo početni datum na dan od kog izmena kreće
            startDateCalendar.setTimeInMillis(recurringEditDateMillis);
            // Krajnji datum ne diramo, korisnik će ga izabrati ako želi
            endDateCalendar.setTime(task.getExecutionTime()); // Privremeno, da ima neku vrednost
        } else {
            // Za običan zadatak, popunjavamo samo njegovo vreme
            if (task.getExecutionTime() != null) {
                executionDateCalendar.setTime(task.getExecutionTime());
            }
        }
        updateAllDateTextViews();
    }

    private void saveTask() {
        String name = editTextTaskName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Molimo unesite naziv zadatka.", Toast.LENGTH_SHORT).show();
            return;
        }
        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        if (selectedCategory == null) {
            Toast.makeText(this, "Molimo izaberite kategoriju.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (taskToEdit != null) {
            if (recurringEditDateMillis != -1 && taskToEdit.getRecurringGroupId() != null) {
                editFutureRecurringTasks(name, selectedCategory);
            } else {
                editSingleTask(name, selectedCategory);
            }
        } else {
            if (checkBoxRecurring.isChecked()) {
                createNewRecurringTasks(name, selectedCategory);
            } else {
                createSingleTask(name, selectedCategory);
            }
        }
    }

    private void createSingleTask(String name, Category category) { /* ... nepromenjeno ... */
        Task task = new Task();
        populateTaskFromFields(task, name, category);
        task.setExecutionTime(executionDateCalendar.getTime());
        task.setRecurringGroupId(null);
        executorService.execute(() -> {
            taskDao.insert(task);
            runOnUiThread(() -> {
                Toast.makeText(this, "Zadatak kreiran.", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void createNewRecurringTasks(String name, Category category) {
        if (startDateCalendar.after(endDateCalendar)) {
            Toast.makeText(this, "Datum početka mora biti pre datuma završetka.", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- IZMENA: Dodata validacija za interval ---
        String intervalStr = editTextInterval.getText().toString();
        if (TextUtils.isEmpty(intervalStr) || Integer.parseInt(intervalStr) <= 0) {
            Toast.makeText(this, "Interval ponavljanja mora biti broj veći od 0.", Toast.LENGTH_SHORT).show();
            return;
        }
        int interval = Integer.parseInt(intervalStr);

        List<Task> tasksToInsert = new ArrayList<>();
        long groupId = System.currentTimeMillis();
        RepetitionUnit unit = (RepetitionUnit) spinnerRepetitionUnit.getSelectedItem();
        Calendar iterator = (Calendar) startDateCalendar.clone();

        while (!iterator.after(endDateCalendar)) {
            Task task = new Task();
            populateTaskFromFields(task, name, category);
            task.setExecutionTime(iterator.getTime());
            task.setRecurringGroupId(groupId);
            tasksToInsert.add(task);
            if (unit == RepetitionUnit.DAN) {
                iterator.add(Calendar.DAY_OF_YEAR, interval);
            } else {
                iterator.add(Calendar.WEEK_OF_YEAR, interval);
            }
        }

        executorService.execute(() -> {
            if (!tasksToInsert.isEmpty()) {
                taskDao.insertAll(tasksToInsert);
            }
            runOnUiThread(() -> {
                Toast.makeText(this, "Ponavljajući zadaci kreirani.", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void editSingleTask(String name, Category category) { /* ... nepromenjeno ... */
        populateTaskFromFields(taskToEdit, name, category);
        taskToEdit.setExecutionTime(executionDateCalendar.getTime());
        taskToEdit.setRecurringGroupId(null);
        executorService.execute(() -> {
            taskDao.update(taskToEdit);
            runOnUiThread(() -> {
                Toast.makeText(this, "Zadatak ažuriran.", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void editFutureRecurringTasks(String name, Category category) {
        // --- IZMENA: Dodata ista validacija za interval ---
        String intervalStr = editTextInterval.getText().toString();
        if (TextUtils.isEmpty(intervalStr) || Integer.parseInt(intervalStr) <= 0) {
            Toast.makeText(this, "Interval ponavljanja mora biti broj veći od 0.", Toast.LENGTH_SHORT).show();
            return;
        }
        int interval = Integer.parseInt(intervalStr);

        long groupId = taskToEdit.getRecurringGroupId();
        Date startDateForNew = new Date(recurringEditDateMillis);

        executorService.execute(() -> {
            taskDao.deleteFutureByGroupId(groupId, startDateForNew);
            List<Task> tasksToInsert = new ArrayList<>();
            RepetitionUnit unit = (RepetitionUnit) spinnerRepetitionUnit.getSelectedItem();
            Calendar iterator = Calendar.getInstance();
            iterator.setTime(startDateForNew);

            while (!iterator.after(endDateCalendar)) {
                Task task = new Task();
                populateTaskFromFields(task, name, category);
                task.setExecutionTime(iterator.getTime());
                task.setRecurringGroupId(groupId);
                tasksToInsert.add(task);
                if (unit == RepetitionUnit.DAN) {
                    iterator.add(Calendar.DAY_OF_YEAR, interval);
                } else {
                    iterator.add(Calendar.WEEK_OF_YEAR, interval);
                }
            }

            if (!tasksToInsert.isEmpty()) {
                taskDao.insertAll(tasksToInsert);
            }

            runOnUiThread(() -> {
                Toast.makeText(this, "Serija zadataka ažurirana.", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void populateTaskFromFields(Task task, String name, Category c) { /* ... nepromenjeno ... */
        task.setName(name);
        task.setDescription(editTextTaskDescription.getText().toString());
        task.setDifficulty((Task.Difficulty) spinnerDifficulty.getSelectedItem());
        task.setImportance((Task.Importance) spinnerImportance.getSelectedItem());
        task.setStatus(Task.Status.AKTIVAN);
        task.setCategoryId(c.getId());
    }

    private void showDatePickerDialog(Calendar c, TextView tv) { /* ... nepromenjeno ... */
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            c.set(year, month, dayOfMonth);
            updateAllDateTextViews();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    // --- VRAĆENA METODA KOJA JE NEDOSTAJALA ---
    private void showTimePickerDialog() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            executionDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            executionDateCalendar.set(Calendar.MINUTE, minute);
            updateAllDateTextViews();
        }, executionDateCalendar.get(Calendar.HOUR_OF_DAY), executionDateCalendar.get(Calendar.MINUTE), true).show();
    }

    private void updateAllDateTextViews() { /* ... nepromenjeno ... */
        SimpleDateFormat justDateFormatter = new SimpleDateFormat("d.M.yyyy", Locale.getDefault());
        textViewSelectedDate.setText(dateFormatter.format(executionDateCalendar.getTime()));
        textViewStartDate.setText(justDateFormatter.format(startDateCalendar.getTime()));
        textViewEndDate.setText(justDateFormatter.format(endDateCalendar.getTime()));
    }
}