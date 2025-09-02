package ftn.ma.myapplication;
import android.content.Intent; // Dodajte ovaj import na vrh fajla
import android.widget.Button;   // I ovaj takođe
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ftn.ma.myapplication.ui.categories.CategoriesActivity;
import ftn.ma.myapplication.ui.tasks.TasksActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Button openCategoriesButton = findViewById(R.id.btnOpenCategories);
        openCategoriesButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
            startActivity(intent);
        });

        Button openTasksButton = findViewById(R.id.btnOpenTasks);
        openTasksButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TasksActivity.class);
            startActivity(intent);
        });
    }
}