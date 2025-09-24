package ftn.ma.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import ftn.ma.myapplication.util.SharedPreferencesManager;
import ftn.ma.myapplication.activities.FriendsActivity;
import ftn.ma.myapplication.activities.AllianceActivity;
import ftn.ma.myapplication.activities.ChatActivity;
import ftn.ma.myapplication.activities.NotificationActivity;
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

        // Dodaj dugmad za social funkcionalnosti
        addSocialButtons();

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

    private void addSocialButtons() {
        // Kreiraj LinearLayout za social dugmad
        LinearLayout socialLayout = new LinearLayout(this);
        socialLayout.setOrientation(LinearLayout.HORIZONTAL);
        socialLayout.setPadding(10, 10, 10, 10);

        // Friends dugme
        Button friendsBtn = new Button(this);
        friendsBtn.setText("Friends");
        friendsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, FriendsActivity.class);
            startActivity(intent);
        });

        // Alliance dugme
        Button allianceBtn = new Button(this);
        allianceBtn.setText("Alliance");
        allianceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, AllianceActivity.class);
            startActivity(intent);
        });

        // Chat dugme
        Button chatBtn = new Button(this);
        chatBtn.setText("Chat");
        chatBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            startActivity(intent);
        });

        // Notifications dugme
        Button notifBtn = new Button(this);
        notifBtn.setText("Notifications");
        notifBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);
        });

        socialLayout.addView(friendsBtn);
        socialLayout.addView(allianceBtn);
        socialLayout.addView(chatBtn);
        socialLayout.addView(notifBtn);

        // Dodaj layout direktno na glavni RelativeLayout
        RelativeLayout mainLayout = findViewById(R.id.main);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        socialLayout.setLayoutParams(params);
        
        if (mainLayout != null) {
            mainLayout.addView(socialLayout);
        }
    }
}