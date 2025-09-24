package ftn.ma.myapplication.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.data.local.CategoryDao;
import ftn.ma.myapplication.data.model.Task;
import ftn.ma.myapplication.data.model.Category;
import ftn.ma.myapplication.util.SharedPreferencesManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsActivity extends AppCompatActivity {
    
    private ExecutorService executorService;
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    
    private TextView textViewStats;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics_simple);
        
        AppDatabase database = AppDatabase.getDatabase(this);
        taskDao = database.taskDao();
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();
        
        textViewStats = findViewById(R.id.textViewStats);
        Button buttonBack = findViewById(R.id.buttonBack);
        
        buttonBack.setOnClickListener(v -> finish());
        
        loadStatistics();
    }
    
    private void loadStatistics() {
        // Prvo postaviti placeholder tekst
        textViewStats.setText("Učitavanje statistika...");
        
        executorService.execute(() -> {
            try {
                // Proverava da li su DAO objekti inicijalizovani
                if (taskDao == null || categoryDao == null) {
                    runOnUiThread(() -> textViewStats.setText("Greška: Baza podataka nije dostupna."));
                    return;
                }
                
                List<Task> allTasks = taskDao.getAllTasks();
                List<Category> allCategories = categoryDao.getAllCategories();
                
                // Null checks
                if (allTasks == null) allTasks = new java.util.ArrayList<>();
                if (allCategories == null) allCategories = new java.util.ArrayList<>();
                
                // Osnovne statistike
                int totalTasks = allTasks.size();
                int completedTasks = 0;
                int notCompletedTasks = 0;
                int cancelledTasks = 0;
                
                // Aktivni dani
                java.util.Set<String> uniqueDays = new java.util.HashSet<>();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                
                for (Task task : allTasks) {
                    if (task != null && task.getStatus() != null) {
                        if (task.getStatus() == Task.Status.URADJEN) {
                            completedTasks++;
                            if (task.getCompletionDate() != null) {
                                uniqueDays.add(sdf.format(task.getCompletionDate()));
                            }
                        } else if (task.getStatus() == Task.Status.NEURADJEN) {
                            notCompletedTasks++;
                        } else if (task.getStatus() == Task.Status.OTKAZAN) {
                            cancelledTasks++;
                        }
                    }
                }
                
                int activeDays = uniqueDays.size();
                
                // Kreiranje final varijabli za lambda expression
                final List<Task> finalAllTasks = allTasks;
                final List<Category> finalAllCategories = allCategories;
                final int finalTotalTasks = totalTasks;
                final int finalCompletedTasks = completedTasks;
                final int finalNotCompletedTasks = notCompletedTasks;
                final int finalCancelledTasks = cancelledTasks;
                final int finalActiveDays = activeDays;
                
                // SharedPreferencesManager pozivamo iz UI thread-a
                runOnUiThread(() -> {
                    int completedMissions = SharedPreferencesManager.getCompletedMissionsCount(StatisticsActivity.this);
                    
                    // Proverava da li ima podataka za prikaz
                    if (finalTotalTasks == 0) {
                        textViewStats.setText(
                            "STATISTIKE KORISNIKA\n\n" +
                            "📊 POČETNO STANJE:\n" +
                            "• Još uvek nemate kreirane zadatke\n" +
                            "• Kreirajte prvi zadatak da počnete\n" +
                            "• Statistike će se prikazivati kada\n" +
                            "  počnete da koristite aplikaciju\n\n" +
                            "💡 SAVETI:\n" +
                            "• Idite na 'Zadaci' da kreirate novi zadatak\n" +
                            "• Organizujte zadatke po kategorijama\n" +
                            "• Redovno označavajte završene zadatke\n\n" +
                            "📈 Vaš napredak će biti ovde prikazan!"
                        );
                        return;
                    }
                    
                    // Statistike po kategorijama
                    StringBuilder categoryStats = new StringBuilder();
                    for (Category category : finalAllCategories) {
                        if (category != null) {
                            int tasksInCategory = 0;
                            int completedInCategory = 0;
                            
                            for (Task task : finalAllTasks) {
                                if (task != null && task.getCategoryId() == category.getId()) {
                                    tasksInCategory++;
                                    if (task.getStatus() != null && task.getStatus() == Task.Status.URADJEN) {
                                        completedInCategory++;
                                    }
                                }
                            }
                            
                            categoryStats.append(String.format("%s: %d/%d urađenih\n", 
                                category.getName() != null ? category.getName() : "Unknown", 
                                completedInCategory, tasksInCategory));
                        }
                    }
                    
                    // Kreiranje final varijabli za prikaz
                    final int finalCompletedMissions = completedMissions;
                    final String finalCategoryStats = categoryStats.toString();
                    
                    String statsText = String.format(
                        "STATISTIKE KORISNIKA\n\n" +
                        "📊 OSNOVNE STATISTIKE:\n" +
                        "• Ukupno zadataka: %d\n" +
                        "• Urađeni zadaci: %d\n" +
                        "• Neurađeni zadaci: %d\n" +
                        "• Otkazani zadaci: %d\n" +
                        "• Procenat završenih: %.1f%%\n\n" +
                        "📅 AKTIVNOST:\n" +
                        "• Aktivnih dana: %d\n" +
                        "• Specijalne misije: %d\n\n" +
                        "📂 KATEGORIJE:\n%s\n" +
                        "═══════════════════════════\n" +
                        "Ova statistika prikazuje vaš napredak\n" +
                        "u organizaciji i rešavanju zadataka.",
                        finalTotalTasks,
                        finalCompletedTasks,
                        finalNotCompletedTasks,
                        finalCancelledTasks,
                        finalTotalTasks > 0 ? (finalCompletedTasks * 100.0 / finalTotalTasks) : 0.0,
                        finalActiveDays,
                        finalCompletedMissions,
                        finalCategoryStats.isEmpty() ? "Nema kategorija" : finalCategoryStats
                    );
                    
                    textViewStats.setText(statsText);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    textViewStats.setText("Greška pri učitavanju statistika: " + e.getMessage());
                });
            }
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}