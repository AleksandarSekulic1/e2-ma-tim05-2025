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
import ftn.ma.myapplication.data.local.NotificationStorage;
import ftn.ma.myapplication.data.local.UserStorage;
import ftn.ma.myapplication.data.model.User;
import ftn.ma.myapplication.utils.SessionManager;

public class FriendsActivity extends AppCompatActivity {

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
    private NotificationStorage notificationStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDatabase();
        createFriendsLayout();
        setupRecyclerView();
        loadFriends();
        checkForPendingRequests(); // Automatski proverava zahteve
    }

    private void initDatabase() {
        database = AppDatabase.getDatabase(this);
        userDao = database.userDao();
        // friendDao = database.friendDao(); // Ne postoji u bazi
        sessionManager = new SessionManager(this);
        notificationStorage = new NotificationStorage(this);
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
        friendsAdapter = new FriendsAdapter(friendsList);
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
        // Pretražujemo korisnika u UserStorage
        List<User> allUsers = UserStorage.getUserList(this);
        User foundUser = null;
        
        for (User user : allUsers) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                foundUser = user;
                break;
            }
        }
        
        if (foundUser != null) {
            User finalFoundUser = foundUser;
            new AlertDialog.Builder(this)
                    .setTitle("User Found: " + username)
                    .setMessage("Do you want to add " + username + " as a friend?")
                    .setPositiveButton("Add Friend", (dialog, which) -> addFriend(finalFoundUser))
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            Toast.makeText(this, "User not found: " + username, Toast.LENGTH_SHORT).show();
        }
    }

    private void addFriend(User targetUser) {
        int currentUserIdInt = sessionManager.getUserId();
        String currentUserId = String.valueOf(currentUserIdInt);
        String currentUsername = sessionManager.getUsername();
        String targetUserId = String.valueOf(targetUser.getId());
        
        // Debug informacije
        Toast.makeText(this, 
            "Debug Info:\n" +
            "Current User ID: " + currentUserIdInt + "\n" +
            "Current Username: " + currentUsername + "\n" +
            "Target User ID: " + targetUser.getId() + "\n" +
            "Target Username: " + targetUser.getUsername() + "\n" +
            "Are same? " + targetUserId.equals(currentUserId), 
            Toast.LENGTH_LONG).show();
        
        if (currentUserIdInt == -1) {
            Toast.makeText(this, "You need to be logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (targetUserId.equals(currentUserId)) {
            Toast.makeText(this, "Cannot send friend request to yourself", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Kreiraj friend request
        NotificationStorage.FriendRequest request = new NotificationStorage.FriendRequest(
            currentUserId, currentUsername, targetUserId, targetUser.getUsername()
        );
        
        // Sačuvaj request
        notificationStorage.saveFriendRequest(request);
        
        Toast.makeText(this, "Friend request sent to " + targetUser.getUsername(), Toast.LENGTH_SHORT).show();
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

    // Helper methods for future use
    
    private void checkForPendingRequests() {
        String currentUserId = String.valueOf(sessionManager.getUserId());
        if (sessionManager.getUserId() == -1) return;
        
        List<NotificationStorage.FriendRequest> pendingRequests = 
            notificationStorage.getPendingFriendRequestsForUser(currentUserId);
            
        if (!pendingRequests.isEmpty()) {
            showFriendRequestDialog(pendingRequests.get(0)); // Prikaži prvi zahtev
        }
    }
    
    private void showFriendRequestDialog(NotificationStorage.FriendRequest request) {
        new AlertDialog.Builder(this)
            .setTitle("Friend Request")
            .setMessage(request.fromUsername + " wants to be your friend!")
            .setPositiveButton("Accept", (dialog, which) -> {
                notificationStorage.updateFriendRequestStatus(request.fromUserId, request.toUserId, "accepted");
                Toast.makeText(this, "Friend request accepted!", Toast.LENGTH_SHORT).show();
                // Proverava za sledeći zahtev
                checkForPendingRequests();
            })
            .setNegativeButton("Reject", (dialog, which) -> {
                notificationStorage.updateFriendRequestStatus(request.fromUserId, request.toUserId, "rejected");
                Toast.makeText(this, "Friend request rejected!", Toast.LENGTH_SHORT).show();
                // Proverava za sledeći zahtev
                checkForPendingRequests();
            })
            .setCancelable(false)
            .show();
    }
    
    public void onInviteToAlliance(User friend) {
        // TODO: Pozovi prijatelja u savez
        Toast.makeText(this, "Invite sent to " + friend.getUsername(), Toast.LENGTH_SHORT).show();
    }

    public void onRemoveFriend(User friend) {
        // TODO: Ukloni prijatelja iz liste/baze
        Toast.makeText(this, "Removed friend: " + friend.getUsername(), Toast.LENGTH_SHORT).show();
        loadFriends();
    }
}
