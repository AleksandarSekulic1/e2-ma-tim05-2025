package ftn.ma.myapplication.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import ftn.ma.myapplication.R;
import ftn.ma.myapplication.adapters.AllianceMembersAdapter;
import ftn.ma.myapplication.adapters.FriendsListAdapter;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.model.Alliance;
import ftn.ma.myapplication.data.model.AllianceInvitation;
import ftn.ma.myapplication.data.model.AllianceMember;
import ftn.ma.myapplication.data.model.ChatMessage;
import ftn.ma.myapplication.data.model.Friend;
import ftn.ma.myapplication.data.model.User;
import ftn.ma.myapplication.utils.UserSessionManager;

/**
 * Activity za upravljanje savezom
 * Omogućava kreiranje saveza, upravljanje članovima, pozivanje prijatelja
 */
public class AllianceActivity extends AppCompatActivity implements AllianceMembersAdapter.OnMemberActionListener {
    
    // UI komponente
    private LinearLayout noAllianceLayout;
    private LinearLayout allianceInfoLayout;
    private TextView allianceNameText;
    private TextView allianceDescriptionText;
    private TextView allianceStatusText;
    private TextView memberCountText;
    private ImageView allianceStatusIcon;
    private Button createAllianceButton;
    private Button joinAllianceButton;
    private Button leaveAllianceButton;
    private Button startMissionButton;
    private Button inviteFriendsButton;
    private LinearLayout openChatButton;
    private FloatingActionButton fabAllianceActions;
    private RecyclerView membersRecyclerView;
    
    // Data komponente
    private AppDatabase db;
    private UserSessionManager sessionManager;
    private AllianceMembersAdapter membersAdapter;
    private int currentUserId;
    private Alliance currentAlliance;
    private AllianceMember currentMember;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alliance);
        
        initializeViews();
        setupActionBar();
        initializeData();
        setupRecyclerView();
        setupClickListeners();
        setupBottomNavigation();
        loadAllianceData();
    }
    
    private void initializeViews() {
        noAllianceLayout = findViewById(R.id.no_alliance_layout);
        allianceInfoLayout = findViewById(R.id.alliance_info_layout);
        allianceNameText = findViewById(R.id.alliance_name_text);
        allianceDescriptionText = findViewById(R.id.alliance_description_text);
        allianceStatusText = findViewById(R.id.alliance_status_text);
        memberCountText = findViewById(R.id.member_count_text);
        allianceStatusIcon = findViewById(R.id.alliance_status_icon);
        createAllianceButton = findViewById(R.id.create_alliance_button);
        joinAllianceButton = findViewById(R.id.join_alliance_button);
        leaveAllianceButton = findViewById(R.id.leave_alliance_button);
        startMissionButton = findViewById(R.id.start_mission_button);
        inviteFriendsButton = findViewById(R.id.invite_friends_button);
        openChatButton = findViewById(R.id.open_chat_button);
        fabAllianceActions = findViewById(R.id.fab_alliance_actions);
        membersRecyclerView = findViewById(R.id.members_recycler_view);
    }
    
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Savez");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void initializeData() {
        db = AppDatabase.getDatabase(this);
        sessionManager = new UserSessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
    }
    
    private void setupRecyclerView() {
        membersAdapter = new AllianceMembersAdapter(this, this);
        membersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        membersRecyclerView.setAdapter(membersAdapter);
    }
    
    private void setupClickListeners() {
        createAllianceButton.setOnClickListener(v -> showCreateAllianceDialog());
        joinAllianceButton.setOnClickListener(v -> showJoinAllianceDialog());
        leaveAllianceButton.setOnClickListener(v -> confirmLeaveAlliance());
        startMissionButton.setOnClickListener(v -> startAllianceMission());
        inviteFriendsButton.setOnClickListener(v -> showInviteFriendsDialog());
        openChatButton.setOnClickListener(v -> openAllianceChat());
        fabAllianceActions.setOnClickListener(v -> showAllianceActionsMenu());
    }
    
    private void loadAllianceData() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Pronađi trenutni savez korisnika
                AllianceMember memberData = db.allianceMemberDao().getMemberByUserId(currentUserId);
                
                if (memberData != null) {
                    currentMember = memberData;
                    currentAlliance = db.allianceDao().getAllianceById(memberData.getAllianceId());
                    
                    if (currentAlliance != null) {
                        List<AllianceMember> members = db.allianceMemberDao().getMembersByAlliance(currentAlliance.getAllianceId());
                        
                        runOnUiThread(() -> {
                            displayAllianceInfo();
                            membersAdapter.updateMembers(members);
                        });
                    } else {
                        runOnUiThread(this::showNoAllianceState);
                    }
                } else {
                    runOnUiThread(this::showNoAllianceState);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(AllianceActivity.this, "Greška pri učitavanju podataka o savezu", Toast.LENGTH_SHORT).show();
                    showNoAllianceState();
                });
            }
        });
    }
    
    private void showNoAllianceState() {
        noAllianceLayout.setVisibility(View.VISIBLE);
        allianceInfoLayout.setVisibility(View.GONE);
        fabAllianceActions.setVisibility(View.GONE);
    }
    
    private void displayAllianceInfo() {
        noAllianceLayout.setVisibility(View.GONE);
        allianceInfoLayout.setVisibility(View.VISIBLE);
        fabAllianceActions.setVisibility(View.VISIBLE);
        
        allianceNameText.setText(currentAlliance.getName());
        allianceDescriptionText.setText(currentAlliance.getDescription());
        memberCountText.setText(currentAlliance.getCurrentMemberCount() + "/" + currentAlliance.getMaxMembers() + " članova");
        
        // Postavi status
        switch (currentAlliance.getStatus()) {
            case RECRUITING:
                allianceStatusText.setText("Regrutuje članove");
                allianceStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                allianceStatusIcon.setImageResource(R.drawable.ic_people);
                break;
            case MISSION_ACTIVE:
                allianceStatusText.setText("Misija u toku");
                allianceStatusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                allianceStatusIcon.setImageResource(R.drawable.ic_mission_active);
                break;
            case DISBANDED:
                allianceStatusText.setText("Raspušten");
                allianceStatusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                allianceStatusIcon.setImageResource(R.drawable.ic_close);
                break;
        }
        
        // Postavi dugmad na osnovu uloge
        setupButtonsBasedOnRole();
    }
    
    private void setupButtonsBasedOnRole() {
        boolean isLeader = currentMember.getRole() == AllianceMember.MemberRole.LEADER;
        boolean canStartMission = isLeader && currentAlliance.getStatus() == Alliance.AllianceStatus.RECRUITING;
        boolean canInvite = currentAlliance.canAcceptNewMembers();
        
        startMissionButton.setVisibility(canStartMission ? View.VISIBLE : View.GONE);
        inviteFriendsButton.setVisibility(canInvite ? View.VISIBLE : View.GONE);
        leaveAllianceButton.setVisibility(isLeader ? View.GONE : View.VISIBLE);
    }
    
    private void showCreateAllianceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Kreiraj savez");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_alliance, null);
        EditText nameInput = dialogView.findViewById(R.id.alliance_name_input);
        EditText descriptionInput = dialogView.findViewById(R.id.alliance_description_input);
        
        builder.setView(dialogView);
        builder.setPositiveButton("Kreiraj", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            
            if (!name.isEmpty()) {
                createAlliance(name, description);
            } else {
                Toast.makeText(AllianceActivity.this, "Ime saveza je obavezno", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Otkaži", null);
        
        builder.show();
    }
    
    private void createAlliance(String name, String description) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Dobij podatke o trenutnom korisniku
                User currentUser = db.userDao().getUserById(currentUserId);
                String currentUsername = currentUser != null ? currentUser.getUsername() : sessionManager.getCurrentUsername();
                
                // Kreiraj novi savez
                Alliance newAlliance = new Alliance(name, currentUserId, currentUsername, description);
                long allianceId = db.allianceDao().insertAlliance(newAlliance);
                
                // Dodaj kreatora kao lidera
                AllianceMember leader = new AllianceMember((int)allianceId, currentUserId, 
                                                         currentUsername, AllianceMember.MemberRole.LEADER);
                db.allianceMemberDao().insertMember(leader);
                
                // Kreiraj welcome poruku
                ChatMessage welcomeMessage = ChatMessage.createSystemMessage((int)allianceId, 
                        "Savez '" + name + "' je kreiran! Dobrodošli!");
                db.chatMessageDao().insertMessage(welcomeMessage);
                
                runOnUiThread(() -> {
                    Toast.makeText(AllianceActivity.this, "Savez je uspešno kreiran!", Toast.LENGTH_SHORT).show();
                    loadAllianceData();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(AllianceActivity.this, "Greška pri kreiranju saveza", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void showJoinAllianceDialog() {
        // Za sada jednostavna implementacija - možete proširiti sa listom dostupnih saveza
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pridruži se savezu");
        builder.setMessage("Funkcionalnost za pridruživanje savezu će biti implementirana u budućoj verziji.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }
    
    private void confirmLeaveAlliance() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Napusti savez");
        builder.setMessage("Da li ste sigurni da želite da napustite savez '" + currentAlliance.getName() + "'?");
        
        builder.setPositiveButton("Napusti", (dialog, which) -> leaveAlliance());
        builder.setNegativeButton("Otkaži", null);
        
        builder.show();
    }
    
    private void leaveAlliance() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Ukloni člana iz saveza
                db.allianceMemberDao().deleteMember(currentMember);
                
                // Kreiraj poruku o napuštanju
                ChatMessage leaveMessage = ChatMessage.createSystemMessage(currentAlliance.getAllianceId(), 
                        currentMember.getUsername() + " je napustio/la savez.");
                db.chatMessageDao().insertMessage(leaveMessage);
                
                runOnUiThread(() -> {
                    Toast.makeText(AllianceActivity.this, "Napustili ste savez", Toast.LENGTH_SHORT).show();
                    loadAllianceData();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(AllianceActivity.this, "Greška pri napuštanju saveza", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void startAllianceMission() {
        if (currentMember.getRole() != AllianceMember.MemberRole.LEADER) {
            Toast.makeText(AllianceActivity.this, "Samo lider može pokrenuti misiju", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pokreni misiju");
        builder.setMessage("Da li želite da pokrenete misiju saveza? Tokom misije neće biti moguće dodavanje novih članova.");
        
        builder.setPositiveButton("Pokreni", (dialog, which) -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    db.allianceDao().startMission(currentAlliance.getAllianceId());
                    
                    ChatMessage missionMessage = ChatMessage.createSystemMessage(currentAlliance.getAllianceId(), 
                            "Misija saveza je pokrenuta! Svi članovi su pozvani da učestvuju.");
                    db.chatMessageDao().insertMessage(missionMessage);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(AllianceActivity.this, "Misija je pokrenuta!", Toast.LENGTH_SHORT).show();
                        loadAllianceData();
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(AllianceActivity.this, "Greška pri pokretanju misije", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });
        
        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }
    
    private void showInviteFriendsDialog() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<Friend> friends = db.friendDao().getFriendsForUser(currentUserId);
                
                runOnUiThread(() -> {
                    if (friends.isEmpty()) {
                        Toast.makeText(AllianceActivity.this, "Nemate prijatelje za pozivanje", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Pozovi prijatelje");
                    
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_invite_friends, null);
                    RecyclerView friendsRecyclerView = dialogView.findViewById(R.id.friends_recycler_view);
                    
                    FriendsListAdapter friendsAdapter = new FriendsListAdapter(this, friends, 
                            friend -> sendAllianceInvitation(friend));
                    
                    friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                    friendsRecyclerView.setAdapter(friendsAdapter);
                    
                    builder.setView(dialogView);
                    builder.setNegativeButton("Zatvori", null);
                    builder.show();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(AllianceActivity.this, "Greška pri učitavanju prijatelja", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void sendAllianceInvitation(Friend friend) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Proveri da li je prijatelj već pozvan
                AllianceInvitation existingInvitation = db.allianceInvitationDao()
                        .getExistingPendingInvitation(currentAlliance.getAllianceId(), friend.getFriendId());
                
                if (existingInvitation != null) {
                    runOnUiThread(() -> {
                        Toast.makeText(AllianceActivity.this, friend.getFriendUsername() + " je već pozvan", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // Kreiraj novi poziv
                String inviterUsername = sessionManager.getCurrentUsername();
                String message = "Pozvan ste da se pridružite savezu '" + currentAlliance.getName() + "'";
                AllianceInvitation invitation = new AllianceInvitation(currentAlliance.getAllianceId(), 
                        currentAlliance.getName(), currentUserId, inviterUsername, 
                        friend.getFriendId(), friend.getFriendUsername(), message);
                db.allianceInvitationDao().insertInvitation(invitation);
                
                runOnUiThread(() -> {
                    Toast.makeText(AllianceActivity.this, "Poziv je poslat korisniku " + friend.getFriendUsername(), Toast.LENGTH_SHORT).show();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(AllianceActivity.this, "Greška pri slanju poziva", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private void openAllianceChat() {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("alliance_id", currentAlliance.getAllianceId());
        chatIntent.putExtra("alliance_name", currentAlliance.getName());
        startActivity(chatIntent);
    }
    
    private void showAllianceActionsMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Akcije saveza");
        
        String[] actions;
        if (currentMember.getRole() == AllianceMember.MemberRole.LEADER) {
            actions = new String[]{"Otvori chat", "Pozovi prijatelje", "Upravljaj članovima", "Raspusti savez"};
        } else {
            actions = new String[]{"Otvori chat", "Prikaži članove", "Napusti savez"};
        }
        
        builder.setItems(actions, (dialog, which) -> {
            if (currentMember.getRole() == AllianceMember.MemberRole.LEADER) {
                switch (which) {
                    case 0: openAllianceChat(); break;
                    case 1: showInviteFriendsDialog(); break;
                    case 2: showMemberManagementDialog(); break;
                    case 3: confirmDisbandAlliance(); break;
                }
            } else {
                switch (which) {
                    case 0: openAllianceChat(); break;
                    case 1: /* Već prikazani */ break;
                    case 2: confirmLeaveAlliance(); break;
                }
            }
        });
        
        builder.show();
    }
    
    private void showMemberManagementDialog() {
        Toast.makeText(AllianceActivity.this, "Upravljanje članovima - u razvoju", Toast.LENGTH_SHORT).show();
    }
    
    private void confirmDisbandAlliance() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Raspusti savez");
        builder.setMessage("Da li ste sigurni da želite da raspustite savez? Ova akcija je nepovratna!");
        
        builder.setPositiveButton("Raspusti", (dialog, which) -> disbandAlliance());
        builder.setNegativeButton("Otkaži", null);
        
        builder.show();
    }
    
    private void disbandAlliance() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Obriši sve članove
                db.allianceMemberDao().deleteAllMembersFromAlliance(currentAlliance.getAllianceId());
                
                // Obriši sve pozive
                db.allianceInvitationDao().deleteAllInvitationsForAlliance(currentAlliance.getAllianceId());
                
                // Dodaj završnu poruku
                ChatMessage disbandMessage = ChatMessage.createSystemMessage(currentAlliance.getAllianceId(), 
                        "Savez je raspušten od strane lidera.");
                db.chatMessageDao().insertMessage(disbandMessage);
                
                // Označi savez kao raspušten
                db.allianceDao().disbandAlliance(currentAlliance.getAllianceId());
                
                runOnUiThread(() -> {
                    Toast.makeText(AllianceActivity.this, "Savez je raspušten", Toast.LENGTH_SHORT).show();
                    loadAllianceData();
                });
                
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(AllianceActivity.this, "Greška pri raspuštanju saveza", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    // AllianceMembersAdapter.OnMemberActionListener implementacija
    @Override
    public void onPromoteMember(AllianceMember member) {
        if (currentMember.getRole() != AllianceMember.MemberRole.LEADER) {
            Toast.makeText(AllianceActivity.this, "Samo lider može unaprediti članove", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Implementiraj unapređenje člana
        Toast.makeText(AllianceActivity.this, "Unapređenje člana - u razvoju", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onRemoveMember(AllianceMember member) {
        if (currentMember.getRole() != AllianceMember.MemberRole.LEADER) {
            Toast.makeText(AllianceActivity.this, "Samo lider može ukloniti članove", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ukloni člana");
        builder.setMessage("Da li ste sigurni da želite da uklonite " + member.getUsername() + " iz saveza?");
        
        builder.setPositiveButton("Ukloni", (dialog, which) -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    db.allianceMemberDao().deleteMember(member);
                    
                    ChatMessage removeMessage = ChatMessage.createSystemMessage(currentAlliance.getAllianceId(), 
                            member.getUsername() + " je uklonjen/a iz saveza.");
                    db.chatMessageDao().insertMessage(removeMessage);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(AllianceActivity.this, "Član je uklonjen", Toast.LENGTH_SHORT).show();
                        loadAllianceData();
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(AllianceActivity.this, "Greška pri uklanjanju člana", Toast.LENGTH_SHORT).show();
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
        bottomNav.setSelectedItemId(ftn.ma.myapplication.R.id.navigation_alliance);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == ftn.ma.myapplication.R.id.navigation_alliance) {
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
            } else if (itemId == ftn.ma.myapplication.R.id.navigation_friends) {
                startActivity(new android.content.Intent(getApplicationContext(), ftn.ma.myapplication.activities.FriendsActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }
}
