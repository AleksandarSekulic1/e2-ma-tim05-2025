package ftn.ma.myapplication.activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ftn.ma.myapplication.data.model.Notification;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private List<Notification> notificationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        createNotificationLayout();
        setupRecyclerView();
        loadNotifications();
    }

    private void createNotificationLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        TextView titleText = new TextView(this);
        titleText.setText("Notifications");
        titleText.setTextSize(20);
        titleText.setPadding(0, 0, 0, 20);
        layout.addView(titleText);

        notificationsRecyclerView = new RecyclerView(this);
        layout.addView(notificationsRecyclerView);

        setContentView(layout);
    }

    private void setupRecyclerView() {
        notificationsList = new ArrayList<>();
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        // TODO: Create NotificationsAdapter
    }

    private void loadNotifications() {
        // TODO: Load notifications from database
        Toast.makeText(this, "Notifications loaded", Toast.LENGTH_SHORT).show();
    }
}
