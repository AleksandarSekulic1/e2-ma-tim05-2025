package ftn.ma.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import ftn.ma.myapplication.util.SharedPreferencesManager;
// Importujte vaše fragmente ovde
// import ftn.ma.myapplication.ui.tasks.TasksFragment;
// import ftn.ma.myapplication.ui.calendar.TasksCalendarFragment;
// import ftn.ma.myapplication.ui.categories.CategoriesFragment;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SharedPreferencesManager.isUserLoggedIn(this)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // Učitavamo početni fragment
        if (savedInstanceState == null) {
            // Zamenite TasksFragment() sa vašim početnim fragmentom kada ga napravite
            // loadFragment(new TasksFragment());
        }
    }

    private final BottomNavigationView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_tasks) {
                    // selectedFragment = new TasksFragment();
                } else if (itemId == R.id.navigation_calendar) {
                    // selectedFragment = new TasksCalendarFragment();
                } else if (itemId == R.id.navigation_categories) {
                    // selectedFragment = new CategoriesFragment();
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}