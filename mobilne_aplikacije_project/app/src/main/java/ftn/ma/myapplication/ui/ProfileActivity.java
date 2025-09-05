package ftn.ma.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ftn.ma.myapplication.LoginActivity;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.CategoryDao;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.ui.calendar.TasksCalendarActivity;
import ftn.ma.myapplication.ui.categories.CategoriesActivity;
import ftn.ma.myapplication.ui.tasks.TasksActivity;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewUsername, textViewUserXp;
    private Button buttonLogout, buttonAddXp, buttonResetApp;
    private ExecutorService executorService;
    private TaskDao taskDao;
    private CategoryDao categoryDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        AppDatabase database = AppDatabase.getDatabase(this);
        taskDao = database.taskDao();
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();

        textViewUsername = findViewById(R.id.textViewUsername);
        textViewUserXp = findViewById(R.id.textViewUserXp);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonAddXp = findViewById(R.id.buttonAddXp);
        buttonResetApp = findViewById(R.id.buttonResetApp);

        loadUserData();
        setupBottomNavigation();

        buttonLogout.setOnClickListener(v -> logout());
        buttonAddXp.setOnClickListener(v -> add100Xp());
        buttonResetApp.setOnClickListener(v -> showResetConfirmationDialog());
    }

    private void loadUserData() {
        textViewUsername.setText("Korisničko ime: student");
        int currentXp = SharedPreferencesManager.getUserXp(this);
        textViewUserXp.setText("Ukupno XP: " + currentXp);
    }

    private void logout() {
        SharedPreferencesManager.setUserLoggedIn(this, false);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void add100Xp() {
        int currentXp = SharedPreferencesManager.getUserXp(this);
        currentXp += 100;
        SharedPreferencesManager.saveUserXp(this, currentXp);
        loadUserData(); // Osveži prikaz
        Toast.makeText(this, "Dodato 100 XP!", Toast.LENGTH_SHORT).show();
        // Ovde bi se mogla pozvati updateUserStats ako je prekopirana u ovu klasu
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Resetovanje podataka")
                .setMessage("Da li ste sigurni? Svi zadaci, kategorije i napredak će biti trajno obrisani.")
                .setPositiveButton("Resetuj", (dialog, which) -> resetData())
                .setNegativeButton("Otkaži", null)
                .show();
    }

    private void resetData() {
        executorService.execute(() -> {
            taskDao.deleteAllTasks();
            categoryDao.deleteAllCategories();
            SharedPreferencesManager.resetAllUserData(this);
            runOnUiThread(() -> {
                Toast.makeText(this, "Podaci su resetovani.", Toast.LENGTH_SHORT).show();
                loadUserData();
            });
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.navigation_profile);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_profile) {
                return true;
            } else if (itemId == R.id.navigation_tasks) {
                startActivity(new Intent(getApplicationContext(), TasksActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_calendar) {
                startActivity(new Intent(getApplicationContext(), TasksCalendarActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == R.id.navigation_categories) {
                startActivity(new Intent(getApplicationContext(), CategoriesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}
