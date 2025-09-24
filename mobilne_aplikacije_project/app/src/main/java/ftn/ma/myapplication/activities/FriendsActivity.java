package ftn.ma.myapplication.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ftn.ma.myapplication.adapters.FriendsAdapter;
import ftn.ma.myapplication.data.dao.FriendDao;
import ftn.ma.myapplication.data.dao.UserDao;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.model.User;
import ftn.ma.myapplication.utils.SessionManager;

public class FriendsActivity extends AppCompatActivity implements FriendsAdapter.OnFriendClickListener {

    // UI
    private RecyclerView friendsRecyclerView;
    private FriendsAdapter friendsAdapter;
    private Button searchUsersButton;
    private Button createAllianceButton;

    // Data
    private final List<User> friendsList = new ArrayList<>();
    private AppDatabase database;
    private UserDao userDao;
    private FriendDao friendDao;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDatabase();
        createFriendsLayout();
        setupRecyclerView();
        loadFriends();
    }

    private void initDatabase() {
        database = AppDatabase.getDatabase(this);
        userDao = database.userDao();
        friendDao = database.friendDao();
        sessionManager = new SessionManager(this);
    }

    private void createFriendsLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // Search Users
        searchUsersButton = new Button(this);
        searchUsersButton.setText("Search Users");
        searchUsersButton.setOnClickListener(v -> showSearchUsersDialog());
        layout.addView(searchUsersButton);

        // Create Alliance
        createAllianceButton = new Button(this);
        createAllianceButton.setText("Create Alliance");
        createAllianceButton.setOnClickListener(v -> showCreateAllianceDialog());
        layout.addView(createAllianceButton);

        // RecyclerView
        friendsRecyclerView = new RecyclerView(this);
        LinearLayout.LayoutParams recyclerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        );
        friendsRecyclerView.setLayoutParams(recyclerParams);
        layout.addView(friendsRecyclerView);

        setContentView(layout);
    }

    private void setupRecyclerView() {
        friendsAdapter = new FriendsAdapter(friendsList, this);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendsAdapter);
    }

    private void loadFriends() {
        // TODO: Učitati prijatelje iz baze u background niti
        friendsList.clear();
        // Placeholder
        Toast.makeText(this, "Friends loaded", Toast.LENGTH_SHORT).show();
        friendsAdapter.notifyDataSetChanged();
    }

    private void showSearchUsersDialog() {
        EditText editText = new EditText(this);
        editText.setHint("Enter username to search");

        new AlertDialog.Builder(this)
                .setTitle("Search Users")
                .setView(editText)
                .setPositiveButton("Search", (dialog, which) -> {
                    String searchUsername = editText.getText().toString().trim();
                    if (!searchUsername.isEmpty()) {
                        searchUsers(searchUsername);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void searchUsers(String username) {
        // TODO: Pretraga korisnika u bazi
        // Placeholder prikaz: nađeni korisnik sa mogućnošću dodavanja
        new AlertDialog.Builder(this)
                .setTitle("User Found: " + username)
                .setMessage("Do you want to add " + username + " as a friend?")
                .setPositiveButton("Add Friend", (dialog, which) -> addFriend(username))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addFriend(String username) {
        // TODO: Dodati prijatelja u bazu
        Toast.makeText(this, "Friend request sent to " + username, Toast.LENGTH_SHORT).show();
        loadFriends(); // refresh
    }

    private void showCreateAllianceDialog() {
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.setPadding(20, 20, 20, 20);

        EditText allianceNameEdit = new EditText(this);
        allianceNameEdit.setHint("Alliance name");
        dialogLayout.addView(allianceNameEdit);

        EditText allianceDescEdit = new EditText(this);
        allianceDescEdit.setHint("Alliance description");
        allianceDescEdit.setLines(3);
        dialogLayout.addView(allianceDescEdit);

        new AlertDialog.Builder(this)
                .setTitle("Create Alliance")
                .setView(dialogLayout)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = allianceNameEdit.getText().toString().trim();
                    String description = allianceDescEdit.getText().toString().trim();
                    if (!name.isEmpty()) {
                        createAlliance(name, description);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createAlliance(String name, String description) {
        // TODO: Kreirati savez u bazi
        Toast.makeText(this, "Alliance '" + name + "' created!", Toast.LENGTH_SHORT).show();
        // TODO: Navigacija na AllianceActivity po potrebi
    }

    // FriendsAdapter.OnFriendClickListener

    @Override
    public void onInviteToAlliance(User friend) {
        // TODO: Pozovi prijatelja u savez
        Toast.makeText(this, "Invite sent to " + friend.getUsername(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRemoveFriend(User friend) {
        // TODO: Ukloni prijatelja iz liste/baze
        Toast.makeText(this, "Removed friend: " + friend.getUsername(), Toast.LENGTH_SHORT).show();
        loadFriends();
    }
}
