package ftn.ma.myapplication.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ftn.ma.myapplication.LoginActivity;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.CategoryDao;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.ui.calendar.TasksCalendarActivity;
import ftn.ma.myapplication.ui.categories.CategoriesActivity;
import ftn.ma.myapplication.ui.game.AllianceMissionActivity;
import ftn.ma.myapplication.ui.tasks.TasksActivity;
import ftn.ma.myapplication.util.SharedPreferencesManager;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewUsername, textViewUserXp, textViewSimulatedDate, textViewBadgeCount;
    private TextView textViewLevel, textViewTitle, textViewPowerPoints, textViewCoins;
    private ImageView imageViewAvatar, imageViewQr;
    private Button buttonLogout, buttonAddXp, buttonResetApp, buttonAllianceMission, buttonSimulateDate, buttonResetDate;
    private ImageView imageViewBadge;

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

        // Povezivanje svih elemenata
    imageViewAvatar = findViewById(R.id.imageViewAvatar);
    textViewUsername = findViewById(R.id.textViewUsername);
    textViewLevel = findViewById(R.id.textViewLevel);
    textViewTitle = findViewById(R.id.textViewTitle);
    textViewPowerPoints = findViewById(R.id.textViewPowerPoints);
    textViewUserXp = findViewById(R.id.textViewUserXp);
    textViewCoins = findViewById(R.id.textViewCoins);
    imageViewQr = findViewById(R.id.imageViewQr);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonAddXp = findViewById(R.id.buttonAddXp);
        buttonResetApp = findViewById(R.id.buttonResetApp);
    buttonAllianceMission = findViewById(R.id.buttonAllianceMission);
    Button buttonStatistics = findViewById(R.id.buttonStatistics);
        textViewSimulatedDate = findViewById(R.id.textViewSimulatedDate);
        buttonSimulateDate = findViewById(R.id.buttonSimulateDate);
        buttonResetDate = findViewById(R.id.buttonResetDate);
        imageViewBadge = findViewById(R.id.imageViewBadge);
        textViewBadgeCount = findViewById(R.id.textViewBadgeCount);

        // PREMEŠTENO: Poziv 'loadUserData()' je prebačen u onResume da bi se podaci uvek osvežavali.

        setupBottomNavigation();

        // Postavljanje listener-a
        buttonLogout.setOnClickListener(v -> logout());
        buttonAddXp.setOnClickListener(v -> add100Xp());
        buttonResetApp.setOnClickListener(v -> showResetConfirmationDialog());
        buttonAllianceMission.setOnClickListener(v -> startActivity(new Intent(this, AllianceMissionActivity.class)));
        buttonSimulateDate.setOnClickListener(v -> showDatePickerDialog());
        buttonResetDate.setOnClickListener(v -> resetSimulatedDate());
        buttonStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            startActivity(intent);
        });
    }

    /**
     * NOVO: Metoda onResume se poziva svaki put kad se korisnik vrati na ovaj ekran.
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Poziv je sada ovde da bi se podaci osvežili svaki put kad se vratiš na ekran,
        // npr. nakon završetka misije.
        loadUserData();
    }

    private void loadUserData() {
        ftn.ma.myapplication.data.model.User user = ftn.ma.myapplication.data.local.UserStorage.getUser(this);
        if (user != null) {
            // Avatar
            int avatarResId = getResources().getIdentifier("avatar_" + (user.getAvatarIndex() + 1), "drawable", getPackageName());
            imageViewAvatar.setImageResource(avatarResId);
            // Username
            textViewUsername.setText("Korisničko ime: " + user.getUsername());
            // Level
            textViewLevel.setText("Nivo: " + user.getLevel());
            // Title
            textViewTitle.setText("Titula: " + user.getTitle());
            // Power Points
            textViewPowerPoints.setText("Snaga: " + user.getPowerPoints() + " PP");
            // XP
            textViewUserXp.setText("XP: " + user.getXp());
            // Coins
            textViewCoins.setText("Novčići: " + user.getCoins());
            // QR kod (placeholder)
            imageViewQr.setImageResource(R.drawable.ic_profile); // TODO: generiši pravi QR kod
        }
        updateSimulatedDateDisplay();
        loadBadgeData();
    }

    private void loadBadgeData() {
        int completedMissions = SharedPreferencesManager.getCompletedMissionsCount(this);
        textViewBadgeCount.setText("Uspešne misije: " + completedMissions);

        if (completedMissions >= 10) {
            imageViewBadge.setImageResource(R.drawable.badge_gold);
            imageViewBadge.setVisibility(View.VISIBLE);
        } else if (completedMissions >= 5) {
            imageViewBadge.setImageResource(R.drawable.badge_silver);
            imageViewBadge.setVisibility(View.VISIBLE);
        } else if (completedMissions >= 1) {
            imageViewBadge.setImageResource(R.drawable.badge_bronze);
            imageViewBadge.setVisibility(View.VISIBLE);
        } else {
            imageViewBadge.setVisibility(View.GONE);
            textViewBadgeCount.setText("Nema uspešnih misija");
        }
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

    // Ostatak koda ostaje nepromenjen...

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
        loadUserData();
        Toast.makeText(this, "Dodato 100 XP!", Toast.LENGTH_SHORT).show();
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Resetovanje podataka")
                .setMessage("Da li ste sigurni? Svi zadaci, kategorije i napredak će biti trajno obrisani.")
                .setPositiveButton("Resetuj", (dialog, which) -> resetData())
                .setNegativeButton("Otkaži", null)
                .show();
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

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        long simulatedDate = SharedPreferencesManager.getSimulatedDate(this);
        if (simulatedDate != 0L) {
            calendar.setTimeInMillis(simulatedDate);
        }

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            SharedPreferencesManager.saveSimulatedDate(this, calendar.getTimeInMillis());
            updateSimulatedDateDisplay();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void resetSimulatedDate() {
        SharedPreferencesManager.clearSimulatedDate(this);
        updateSimulatedDateDisplay();
        Toast.makeText(this, "Simulirani datum je resetovan.", Toast.LENGTH_SHORT).show();
    }

    private void updateSimulatedDateDisplay() {
        long simulatedTimestamp = SharedPreferencesManager.getSimulatedDate(this);
        String todayStr = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        String simulatedDateStr = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date(simulatedTimestamp));

        if (todayStr.equals(simulatedDateStr)) {
            textViewSimulatedDate.setText("Simulirani datum: Danas");
        } else {
            textViewSimulatedDate.setText("Simulirani datum: " + simulatedDateStr);
        }
    }
}