package ftn.ma.myapplication.ui.tasks;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

        setupSpinners();
        buttonSaveTask.setOnClickListener(v -> saveTask());
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
            });
        });
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

        Task newTask = new Task();
        newTask.setName(name);
        newTask.setDescription(editTextTaskDescription.getText().toString());
        newTask.setDifficulty((Task.Difficulty) spinnerDifficulty.getSelectedItem());
        newTask.setImportance((Task.Importance) spinnerImportance.getSelectedItem());
        newTask.setStatus(Task.Status.AKTIVAN);
        newTask.setCategoryId(selectedCategory.getId());

        executorService.execute(() -> {
            taskDao.insert(newTask);
            finish();
        });
    }
}