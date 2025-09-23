package ftn.ma.myapplication.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.adapters.FriendsAdapter;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.model.Friend;
import ftn.ma.myapplication.data.model.User;
import ftn.ma.myapplication.utils.QRCodeUtils;
import ftn.ma.myapplication.utils.UserSessionManager;

/**
 * Activity za upravljanje prijateljima
 * Omogućava pregled prijatelja, slanje zahteva, dodavanje preko QR koda
 */
public class FriendsActivity extends AppCompatActivity implements FriendsAdapter.OnFriendActionListener {
    
    private static final int QR_SCANNER_REQUEST = 100;
    
    // UI komponente
    private TabLayout tabLayout;
    private RecyclerView friendsRecyclerView;
    private EditText searchEditText;
    private ImageButton searchButton;
    private FloatingActionButton fabAddFriend;
    private TextView emptyStateText;
    
    // Data i adapteri
    private AppDatabase db;
    private UserSessionManager sessionManager;
    private FriendsAdapter friendsAdapter;
    private List<Friend> allFriends;
    private int currentUserId;
    
    // Tab pozicije
    private static final int TAB_ALL_FRIENDS = 0;
    private static final int TAB_PENDING_REQUESTS = 1;
    private static final int TAB_SENT_REQUESTS = 2;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        
        initializeViews();
        setupActionBar();
        initializeData();
        setupRecyclerView();
        setupTabs();
        setupClickListeners();
        setupBottomNavigation();
        loadFriends();
    }
    
    private void initializeViews() {
        tabLayout = findViewById(R.id.tab_layout);
        friendsRecyclerView = findViewById(R.id.friends_recycler_view);
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        fabAddFriend = findViewById(R.id.fab_add_friend);
        emptyStateText = findViewById(R.id.empty_state_text);
    }
    
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Prijatelji");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void initializeData() {
        db = AppDatabase.getDatabase(this);
        sessionManager = new UserSessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        allFriends = new ArrayList<>();
    }
    
    private void setupRecyclerView() {
        friendsAdapter = new FriendsAdapter(this, this);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        friendsRecyclerView.setAdapter(friendsAdapter);
    }
    
    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Prijatelji"));
        tabLayout.addTab(tabLayout.newTab().setText("Zahtevi"));
        tabLayout.addTab(tabLayout.newTab().setText("Poslato"));
        
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterFriendsByTab(tab.getPosition());
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void setupClickListeners() {
        fabAddFriend.setOnClickListener(v -> showAddFriendDialog());
        
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFriends(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                searchUsersByUsername(query);
            }
        });
    }
    
    private void loadFriends() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Friend> friends = db.friendDao().getAllFriendsForUser(currentUserId);
                
                runOnUiThread(() -> {
                    allFriends.clear();
                    allFriends.addAll(friends);
                    filterFriendsByTab(tabLayout.getSelectedTabPosition());
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri učitavanju prijatelja", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void filterFriendsByTab(int tabPosition) {
        List<Friend> filteredFriends = new ArrayList<>();
        
        switch (tabPosition) {
            case TAB_ALL_FRIENDS:
                for (Friend friend : allFriends) {
                    if (friend.getStatus() == Friend.FriendshipStatus.ACCEPTED) {
                        filteredFriends.add(friend);
                    }
                }
                emptyStateText.setText("Nemate prijatelje.\nDodajte prijatelje pomoću QR koda ili username-a.");
                break;
                
            case TAB_PENDING_REQUESTS:
                for (Friend friend : allFriends) {
                    if (friend.getStatus() == Friend.FriendshipStatus.PENDING && 
                        friend.getFriendId() == currentUserId) {
                        filteredFriends.add(friend);
                    }
                }
                emptyStateText.setText("Nemate pendinge zahteve za prijateljstvo.");
                break;
                
            case TAB_SENT_REQUESTS:
                for (Friend friend : allFriends) {
                    if (friend.getStatus() == Friend.FriendshipStatus.PENDING && 
                        friend.getUserId() == currentUserId) {
                        filteredFriends.add(friend);
                    }
                }
                emptyStateText.setText("Niste poslali zahteve za prijateljstvo.");
                break;
        }
        
        friendsAdapter.updateFriends(filteredFriends);
        updateEmptyState(filteredFriends.isEmpty());
    }
    
    private void filterFriends(String query) {
        if (query.isEmpty()) {
            filterFriendsByTab(tabLayout.getSelectedTabPosition());
            return;
        }
        
        List<Friend> currentTabFriends = getCurrentTabFriends();
        List<Friend> filteredFriends = new ArrayList<>();
        
        for (Friend friend : currentTabFriends) {
            if (friend.getFriendUsername().toLowerCase().contains(query.toLowerCase())) {
                filteredFriends.add(friend);
            }
        }
        
        friendsAdapter.updateFriends(filteredFriends);
        updateEmptyState(filteredFriends.isEmpty());
    }
    
    private List<Friend> getCurrentTabFriends() {
        List<Friend> currentTabFriends = new ArrayList<>();
        int tabPosition = tabLayout.getSelectedTabPosition();
        
        switch (tabPosition) {
            case TAB_ALL_FRIENDS:
                for (Friend friend : allFriends) {
                    if (friend.getStatus() == Friend.FriendshipStatus.ACCEPTED) {
                        currentTabFriends.add(friend);
                    }
                }
                break;
                
            case TAB_PENDING_REQUESTS:
                for (Friend friend : allFriends) {
                    if (friend.getStatus() == Friend.FriendshipStatus.PENDING && 
                        friend.getFriendId() == currentUserId) {
                        currentTabFriends.add(friend);
                    }
                }
                break;
                
            case TAB_SENT_REQUESTS:
                for (Friend friend : allFriends) {
                    if (friend.getStatus() == Friend.FriendshipStatus.PENDING && 
                        friend.getUserId() == currentUserId) {
                        currentTabFriends.add(friend);
                    }
                }
                break;
        }
        
        return currentTabFriends;
    }
    
    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateText.setVisibility(View.VISIBLE);
            friendsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            friendsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dodaj prijatelja");
        builder.setMessage("Kako želite da dodate prijatelja?");
        
        builder.setPositiveButton("QR kod", (dialog, which) -> {
            Intent intent = new Intent(this, QRScannerActivity.class);
            startActivityForResult(intent, QR_SCANNER_REQUEST);
        });
        
        builder.setNeutralButton("Username", (dialog, which) -> showUsernameInputDialog());
        
        builder.setNegativeButton("Moj QR", (dialog, which) -> {
            Intent intent = new Intent(this, QRDisplayActivity.class);
            startActivity(intent);
        });
        
        builder.show();
    }
    
    private void showUsernameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dodaj prijatelja");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_friend_username, null);
        EditText usernameInput = dialogView.findViewById(R.id.username_input);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Dodaj", (dialog, which) -> {
            String username = usernameInput.getText().toString().trim();
            if (!username.isEmpty()) {
                searchAndAddFriend(username);
            }
        });
        builder.setNegativeButton("Otkaži", null);
        
        builder.show();
    }
    
    private void searchUsersByUsername(String username) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                User user = db.userDao().getUserByUsername(username);
                
                runOnUiThread(() -> {
                    if (user != null && user.getUserId() != currentUserId) {
                        showFoundUserDialog(user);
                    } else if (user != null && user.getUserId() == currentUserId) {
                        Toast.makeText(this, "Ne možete dodati sebe kao prijatelja", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Korisnik sa tim username-om nije pronađen", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri pretrazi korisnika", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void searchAndAddFriend(String username) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                User user = db.userDao().getUserByUsername(username);
                
                runOnUiThread(() -> {
                    if (user != null && user.getUserId() != currentUserId) {
                        sendFriendRequest(user.getUserId(), user.getUsername());
                    } else if (user != null && user.getUserId() == currentUserId) {
                        Toast.makeText(this, "Ne možete dodati sebe kao prijatelja", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Korisnik sa tim username-om nije pronađen", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri pretrazi korisnika", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showFoundUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pronađen korisnik");
        builder.setMessage("Želite li da pošaljete zahtev za prijateljstvo korisniku: " + user.getUsername() + "?");
        
        builder.setPositiveButton("Pošalji zahtev", (dialog, which) -> {
            sendFriendRequest(user.getUserId(), user.getUsername());
        });
        
        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }
    
    private void sendFriendRequest(int friendId, String friendUsername) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Proveri da li već postoji prijateljstvo
                Friend existingFriend = db.friendDao().getFriendship(currentUserId, friendId);
                
                if (existingFriend != null) {
                    runOnUiThread(() -> {
                        String message;
                        switch (existingFriend.getStatus()) {
                            case ACCEPTED:
                                message = "Već ste prijatelji sa ovim korisnikom";
                                break;
                            case PENDING:
                                message = "Zahtev za prijateljstvo je već poslat";
                                break;
                            case BLOCKED:
                                message = "Ne možete dodati ovog korisnika";
                                break;
                            default:
                                message = "Greška pri slanju zahteva";
                        }
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // Kreiraj novi Friend objekat
                User currentUser = db.userDao().getUserById(currentUserId);
                Friend newFriend = new Friend(currentUserId, currentUser.getUsername(), 
                                            friendId, friendUsername, Friend.FriendshipStatus.PENDING);
                
                db.friendDao().insertFriend(newFriend);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Zahtev za prijateljstvo je poslat!", Toast.LENGTH_SHORT).show();
                    loadFriends(); // Refresh liste
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri slanju zahteva za prijateljstvo", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == QR_SCANNER_REQUEST && resultCode == RESULT_OK && data != null) {
            boolean qrResult = data.getBooleanExtra(QRScannerActivity.EXTRA_QR_RESULT, false);
            
            if (qrResult) {
                int scannedUserId = data.getIntExtra(QRScannerActivity.EXTRA_USER_ID, -1);
                String scannedUsername = data.getStringExtra(QRScannerActivity.EXTRA_USERNAME);
                
                if (scannedUserId != -1 && scannedUsername != null) {
                    if (scannedUserId == currentUserId) {
                        Toast.makeText(this, "Ne možete skenirati svoj QR kod", Toast.LENGTH_SHORT).show();
                    } else {
                        sendFriendRequest(scannedUserId, scannedUsername);
                    }
                }
            }
        }
    }
    
    // FriendsAdapter.OnFriendActionListener implementacija
    @Override
    public void onAcceptFriend(Friend friend) {
        acceptFriendRequest(friend);
    }
    
    @Override
    public void onRejectFriend(Friend friend) {
        rejectFriendRequest(friend);
    }
    
    @Override
    public void onRemoveFriend(Friend friend) {
        removeFriend(friend);
    }
    
    @Override
    public void onBlockFriend(Friend friend) {
        blockFriend(friend);
    }
    
    private void acceptFriendRequest(Friend friend) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                db.friendDao().acceptFriendship(friend.getFriendshipId());
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Zahtev za prijateljstvo je prihvaćen", Toast.LENGTH_SHORT).show();
                    loadFriends();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri prihvatanju zahteva", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void rejectFriendRequest(Friend friend) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                db.friendDao().rejectFriendship(friend.getFriendshipId());
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "Zahtev za prijateljstvo je odbačen", Toast.LENGTH_SHORT).show();
                    loadFriends();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Greška pri odbacivanju zahteva", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void removeFriend(Friend friend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Uklanjanje prijatelja");
        builder.setMessage("Da li ste sigurni da želite da uklonite " + friend.getFriendUsername() + " iz liste prijatelja?");
        
        builder.setPositiveButton("Ukloni", (dialog, which) -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    db.friendDao().deleteFriend(friend);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Prijatelj je uklonjen", Toast.LENGTH_SHORT).show();
                        loadFriends();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Greška pri uklanjanju prijatelja", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
        
        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }
    
    private void blockFriend(Friend friend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Blokiranje korisnika");
        builder.setMessage("Da li ste sigurni da želite da blokirate " + friend.getFriendUsername() + "?");
        
        builder.setPositiveButton("Blokiraj", (dialog, which) -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    db.friendDao().blockFriend(friend.getFriendshipId());
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Korisnik je blokiran", Toast.LENGTH_SHORT).show();
                        loadFriends();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Greška pri blokiranju korisnika", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
        
        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    private void setupBottomNavigation() {
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(ftn.ma.myapplication.R.id.bottom_navigation);
        bottomNav.setSelectedItemId(ftn.ma.myapplication.R.id.navigation_friends);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == ftn.ma.myapplication.R.id.navigation_friends) {
                return true;
            } else if (itemId == ftn.ma.myapplication.R.id.navigation_tasks) {
                startActivity(new android.content.Intent(getApplicationContext(), ftn.ma.myapplication.ui.tasks.TasksActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == ftn.ma.myapplication.R.id.navigation_calendar) {
                startActivity(new android.content.Intent(getApplicationContext(), ftn.ma.myapplication.ui.calendar.TasksCalendarActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == ftn.ma.myapplication.R.id.navigation_categories) {
                startActivity(new android.content.Intent(getApplicationContext(), ftn.ma.myapplication.ui.categories.CategoriesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == ftn.ma.myapplication.R.id.navigation_profile) {
                startActivity(new android.content.Intent(getApplicationContext(), ftn.ma.myapplication.ui.ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            } else if (itemId == ftn.ma.myapplication.R.id.navigation_alliance) {
                startActivity(new android.content.Intent(getApplicationContext(), ftn.ma.myapplication.activities.AllianceActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}