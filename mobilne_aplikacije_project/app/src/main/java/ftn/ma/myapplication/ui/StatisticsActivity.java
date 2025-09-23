package ftn.ma.myapplication.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.formatter.ValueFormatter;
import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.data.local.CategoryDao;
import ftn.ma.myapplication.data.model.Task;
import ftn.ma.myapplication.data.model.Category;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsActivity extends AppCompatActivity {
    private ExecutorService executorService;
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
    AppDatabase database = AppDatabase.getDatabase(this);
    taskDao = database.taskDao();
    categoryDao = database.categoryDao();
    executorService = Executors.newSingleThreadExecutor();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        TextView textViewActiveDays = findViewById(R.id.textViewActiveDays);
        PieChart pieChartTasks = findViewById(R.id.pieChartTasks);
        BarChart barChartCategories = findViewById(R.id.barChartCategories);
        LineChart lineChartDifficulty = findViewById(R.id.lineChartDifficulty);
        LineChart lineChartXp7days = findViewById(R.id.lineChartXp7days);
        TextView textViewStreak = findViewById(R.id.textViewStreak);
        TextView textViewSpecialMissions = findViewById(R.id.textViewSpecialMissions);

        executorService.execute(() -> {
            List<Task> allTasks = taskDao.getAllTasks();
            List<Category> allCategories = categoryDao.getAllCategories();

            // --- Aktivni dani ---
            int activeDays = 0;
            java.util.Set<String> uniqueDays = new java.util.HashSet<>();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            for (Task t : allTasks) {
                if (t.getStatus() == Task.Status.URADJEN && t.getCompletionDate() != null) {
                    uniqueDays.add(sdf.format(t.getCompletionDate()));
                }
            }
            activeDays = uniqueDays.size();

            // --- Pie chart: status zadataka ---
            int done = 0, notDone = 0, cancelled = 0, created = allTasks.size();
            for (Task t : allTasks) {
                if (t.getStatus() == Task.Status.URADJEN) done++;
                else if (t.getStatus() == Task.Status.NEURADJEN) notDone++;
                else if (t.getStatus() == Task.Status.OTKAZAN) cancelled++;
            }
            java.util.List<com.github.mikephil.charting.data.PieEntry> pieEntries = new java.util.ArrayList<>();
            pieEntries.add(new com.github.mikephil.charting.data.PieEntry(done, "Urađeni"));
            pieEntries.add(new com.github.mikephil.charting.data.PieEntry(notDone, "Neurađeni"));
            pieEntries.add(new com.github.mikephil.charting.data.PieEntry(cancelled, "Otkazani"));
            com.github.mikephil.charting.data.PieDataSet pieDataSet = new com.github.mikephil.charting.data.PieDataSet(pieEntries, "Zadaci");
            com.github.mikephil.charting.data.PieData pieData = new com.github.mikephil.charting.data.PieData(pieDataSet);

            // --- Bar chart: zadaci po kategoriji ---
            java.util.Map<Long, Integer> catCount = new java.util.HashMap<>();
            for (Task t : allTasks) {
                if (t.getStatus() == Task.Status.URADJEN) {
                    catCount.put(t.getCategoryId(), catCount.getOrDefault(t.getCategoryId(), 0) + 1);
                }
            }
            java.util.List<com.github.mikephil.charting.data.BarEntry> barEntries = new java.util.ArrayList<>();
            java.util.List<String> catLabels = new java.util.ArrayList<>();
            int idx = 0;
            for (Category c : allCategories) {
                int count = catCount.getOrDefault(c.getId(), 0);
                barEntries.add(new com.github.mikephil.charting.data.BarEntry(idx, count));
                catLabels.add(c.getName());
                idx++;
            }
            com.github.mikephil.charting.data.BarDataSet barDataSet = new com.github.mikephil.charting.data.BarDataSet(barEntries, "Kategorije");
            com.github.mikephil.charting.data.BarData barData = new com.github.mikephil.charting.data.BarData(barDataSet);

            // --- Najduži niz uspešno urađenih zadataka ---
            java.util.List<String> sortedDays = new java.util.ArrayList<>(uniqueDays);
            java.util.Collections.sort(sortedDays);
            int maxStreak = 0, currentStreak = 0;
            String prevDay = null;
            for (String day : sortedDays) {
                if (prevDay == null) {
                    currentStreak = 1;
                } else {
                    java.util.Calendar cal1 = java.util.Calendar.getInstance();
                    java.util.Calendar cal2 = java.util.Calendar.getInstance();
                    try {
                        cal1.setTime(sdf.parse(prevDay));
                        cal2.setTime(sdf.parse(day));
                        long diff = (cal2.getTimeInMillis() - cal1.getTimeInMillis()) / (1000 * 60 * 60 * 24);
                        if (diff == 1) currentStreak++;
                        else currentStreak = 1;
                    } catch (Exception ignored) {}
                }
                if (currentStreak > maxStreak) maxStreak = currentStreak;
                prevDay = day;
            }

            // --- Line chart: XP po danima (poslednjih 7 dana) ---
            java.util.Map<String, Integer> xpPerDay = new java.util.HashMap<>();
            long now = System.currentTimeMillis();
            for (Task t : allTasks) {
                if (t.getStatus() == Task.Status.URADJEN && t.getCompletionDate() != null) {
                    long diff = now - t.getCompletionDate().getTime();
                    if (diff <= 7L * 24 * 60 * 60 * 1000) {
                        String day = sdf.format(t.getCompletionDate());
                        int xp = 10; // TODO: Prava vrednost XP po zadatku
                        xpPerDay.put(day, xpPerDay.getOrDefault(day, 0) + xp);
                    }
                }
            }
            java.util.List<com.github.mikephil.charting.data.Entry> xpEntries = new java.util.ArrayList<>();
            java.util.List<String> last7days = new java.util.ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_YEAR, -i);
                String day = sdf.format(cal.getTime());
                last7days.add(day);
                xpEntries.add(new com.github.mikephil.charting.data.Entry(6 - i, xpPerDay.getOrDefault(day, 0)));
            }
            com.github.mikephil.charting.data.LineDataSet xpDataSet = new com.github.mikephil.charting.data.LineDataSet(xpEntries, "XP poslednjih 7 dana");
            com.github.mikephil.charting.data.LineData xpLineData = new com.github.mikephil.charting.data.LineData(xpDataSet);

            // --- Line chart: prosečna težina završenih zadataka ---
            java.util.List<com.github.mikephil.charting.data.Entry> diffEntries = new java.util.ArrayList<>();
            // Za demo: prosečna težina po danima (0=veoma lak, 1=lak, 2=težak, 3=ekstremno težak)
            for (int i = 6; i >= 0; i--) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_YEAR, -i);
                String day = sdf.format(cal.getTime());
                int sum = 0, cnt = 0;
                for (Task t : allTasks) {
                    if (t.getStatus() == Task.Status.URADJEN && t.getCompletionDate() != null && sdf.format(t.getCompletionDate()).equals(day)) {
                        sum += t.getDifficulty() != null ? t.getDifficulty().ordinal() : 0;
                        cnt++;
                    }
                }
                float avg = cnt > 0 ? (float) sum / cnt : 0;
                diffEntries.add(new com.github.mikephil.charting.data.Entry(6 - i, avg));
            }
            com.github.mikephil.charting.data.LineDataSet diffDataSet = new com.github.mikephil.charting.data.LineDataSet(diffEntries, "Prosečna težina");
            com.github.mikephil.charting.data.LineData diffLineData = new com.github.mikephil.charting.data.LineData(diffDataSet);

            // --- Specijalne misije (stub) ---
            int startedMissions = 0, finishedMissions = 0;
            // TODO: Prikupi iz baze specijalne misije

            final int finalActiveDays = activeDays;
            final com.github.mikephil.charting.data.PieData finalPieData = pieData;
            final com.github.mikephil.charting.data.BarData finalBarData = barData;
            final java.util.List<String> finalCatLabels = catLabels;
            final com.github.mikephil.charting.data.LineData finalXpLineData = xpLineData;
            final com.github.mikephil.charting.data.LineData finalDiffLineData = diffLineData;
            final int finalMaxStreak = maxStreak;
            final int finalFinishedMissions = finishedMissions;
            final int finalStartedMissions = startedMissions;
            runOnUiThread(() -> {
                textViewActiveDays.setText("Aktivnih dana: " + finalActiveDays);
                pieChartTasks.setData(finalPieData);
                pieChartTasks.invalidate();
                barChartCategories.setData(finalBarData);
                barChartCategories.getXAxis().setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
                        int i = (int) value;
                        return i >= 0 && i < finalCatLabels.size() ? finalCatLabels.get(i) : "";
                    }
                });
                barChartCategories.invalidate();
                lineChartXp7days.setData(finalXpLineData);
                lineChartXp7days.invalidate();
                lineChartDifficulty.setData(finalDiffLineData);
                lineChartDifficulty.invalidate();
                textViewStreak.setText("Najduži niz: " + finalMaxStreak + " dana");
                textViewSpecialMissions.setText("Specijalne misije: " + finalFinishedMissions + "/" + finalStartedMissions);
            });
        });
    }
}
