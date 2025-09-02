package ftn.ma.myapplication.ui.calendar;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.local.CategoryDao;
import ftn.ma.myapplication.data.local.TaskDao;
import ftn.ma.myapplication.data.model.Category;
import ftn.ma.myapplication.data.model.Task;
import ftn.ma.myapplication.ui.tasks.TaskAdapter;

public class TasksCalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private RecyclerView recyclerViewTasksForDay;
    private TextView textViewSelectedDateTasks;
    private TaskAdapter taskAdapter;

    private List<Task> allTasks = new ArrayList<>();
    private TaskDao taskDao;
    private CategoryDao categoryDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks_calendar);
        setTitle("Kalendar Zadataka");

        AppDatabase database = AppDatabase.getDatabase(getApplicationContext());
        taskDao = database.taskDao();
        categoryDao = database.categoryDao();
        executorService = Executors.newSingleThreadExecutor();

        calendarView = findViewById(R.id.calendarView);
        recyclerViewTasksForDay = findViewById(R.id.recyclerViewTasksForDay);
        textViewSelectedDateTasks = findViewById(R.id.textViewSelectedDateTasks);

        taskAdapter = new TaskAdapter(new ArrayList<>(), taskDao, executorService, null);
        recyclerViewTasksForDay.setAdapter(taskAdapter);

        loadAllTasksFromDb();

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                filterAndDisplayTasksForDate(year, month, dayOfMonth);
            }
        });
    }

    private void loadAllTasksFromDb() {
        executorService.execute(() -> {
            allTasks = taskDao.getAllTasks();
            List<Category> allCategories = categoryDao.getAllCategories();

            Map<Long, Category> categoryMap = allCategories.stream().collect(Collectors.toMap(Category::getId, c -> c));
            for (Task task : allTasks) {
                task.setCategory(categoryMap.get(task.getCategoryId()));
            }

            runOnUiThread(() -> {
                Calendar today = Calendar.getInstance();
                filterAndDisplayTasksForDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
            });
        });
    }

    private void filterAndDisplayTasksForDate(int year, int month, int dayOfMonth) {
        String selectedDateText = "Zadaci za: " + dayOfMonth + "." + (month + 1) + "." + year;
        textViewSelectedDateTasks.setText(selectedDateText);

        List<Task> tasksForSelectedDate = new ArrayList<>();
        Calendar selectedCal = Calendar.getInstance();
        selectedCal.set(year, month, dayOfMonth);
        // Nuliramo vreme da bismo poredili samo datume
        setMidnight(selectedCal);

        for (Task task : allTasks) {
            if (task.isRecurring()) {
                if (task.getStartDate() == null || task.getEndDate() == null) continue;

                Calendar startCal = Calendar.getInstance();
                startCal.setTime(task.getStartDate());
                setMidnight(startCal);

                // --- ISPRAVLJENA LINIJA KODA ---
                Calendar endCal = Calendar.getInstance();
                endCal.setTime(task.getEndDate());
                setMidnight(endCal);

                // Proveravamo da li je izabrani datum unutar opsega ponavljanja
                if (!selectedCal.before(startCal) && !selectedCal.after(endCal)) {
                    long diffInMillis = selectedCal.getTimeInMillis() - startCal.getTimeInMillis();
                    long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

                    if (task.getRepetitionUnit() == Task.RepetitionUnit.DAN) {
                        if (diffInDays % task.getRepetitionInterval() == 0) {
                            tasksForSelectedDate.add(task);
                        }
                    } else { // NEDELJA
                        if (startCal.get(Calendar.DAY_OF_WEEK) == selectedCal.get(Calendar.DAY_OF_WEEK)) {
                            long diffInWeeks = diffInDays / 7;
                            if (diffInWeeks % task.getRepetitionInterval() == 0) {
                                tasksForSelectedDate.add(task);
                            }
                        }
                    }
                }
            } else {
                if (task.getExecutionTime() != null) {
                    Calendar taskCal = Calendar.getInstance();
                    taskCal.setTime(task.getExecutionTime());
                    if (taskCal.get(Calendar.YEAR) == year && taskCal.get(Calendar.MONTH) == month && taskCal.get(Calendar.DAY_OF_MONTH) == dayOfMonth) {
                        tasksForSelectedDate.add(task);
                    }
                }
            }
        }

        taskAdapter = new TaskAdapter(tasksForSelectedDate, taskDao, executorService, null);
        recyclerViewTasksForDay.setAdapter(taskAdapter);
    }

    // Pomoćna metoda za postavljanje vremena na ponoć radi lakšeg poređenja datuma
    private void setMidnight(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}
