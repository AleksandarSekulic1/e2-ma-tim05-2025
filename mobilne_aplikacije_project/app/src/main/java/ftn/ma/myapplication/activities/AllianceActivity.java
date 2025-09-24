package ftn.ma.myapplication.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ftn.ma.myapplication.data.dao.AllianceDao;
import ftn.ma.myapplication.data.dao.AllianceMemberDao;
import ftn.ma.myapplication.data.dao.UserDao;
import ftn.ma.myapplication.data.local.AppDatabase;
import ftn.ma.myapplication.data.model.Alliance;
import ftn.ma.myapplication.data.model.AllianceMember;
import ftn.ma.myapplication.utils.SessionManager;
import ftn.ma.myapplication.adapters.AllianceMemberAdapter;

public class AllianceActivity extends AppCompatActivity {

    // UI
    private TextView allianceStatusText;
    private TextView allianceLevelText;
    private TextView allianceMembersText;
    private TextView allianceNameText;
    private TextView allianceDescText;
    private TextView memberRoleText;

    private Button searchButton;
    private Button chatButton;
    private Button inviteButton;
    private Button leaveButton;
    private Button manageButton;
    private Button deleteButton;

    private RecyclerView memberList;
    private AllianceMemberAdapter memberAdapter;

    // Data
    private AppDatabase database;
    private AllianceDao allianceDao;
    private AllianceMemberDao allianceMemberDao;
    private UserDao userDao;
    private SessionManager sessionManager;

    private Alliance currentAlliance;
    private AllianceMember currentMembership;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDatabase();
        createLayout();
        loadAllianceStatus();
    }

    private void initDatabase() {
        database = AppDatabase.getDatabase(this);
        allianceDao = database.allianceDao();
        allianceMemberDao = database.allianceMemberDao();
        userDao = database.userDao();
        sessionManager = new SessionManager(this);
    }

    private void createLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // Status
        allianceStatusText = new TextView(this);
        allianceStatusText.setText("Alliance Status: Loading...");
        allianceStatusText.setTextSize(16);
        layout.addView(allianceStatusText);

        // Info (hidden by default)
        allianceNameText = new TextView(this);
        allianceNameText.setTextSize(20);
        allianceNameText.setVisibility(View.GONE);
        layout.addView(allianceNameText);

        allianceDescText = new TextView(this);
        allianceDescText.setVisibility(View.GONE);
        layout.addView(allianceDescText);

        memberRoleText = new TextView(this);
        memberRoleText.setTextSize(14);
        memberRoleText.setVisibility(View.GONE);
        layout.addView(memberRoleText);

        allianceLevelText = new TextView(this);
        allianceLevelText.setText("Level: -");
        layout.addView(allianceLevelText);

        allianceMembersText = new TextView(this);
        allianceMembersText.setText("Members: -");
        layout.addView(allianceMembersText);

        // Buttons
        searchButton = new Button(this);
        searchButton.setText("Search Alliance");
        searchButton.setOnClickListener(v -> showJoinAllianceDialog());
        layout.addView(searchButton);

        chatButton = new Button(this);
        chatButton.setText("Alliance Chat");
        chatButton.setOnClickListener(v -> openAllianceChat());
        chatButton.setVisibility(View.GONE);
        layout.addView(chatButton);

        inviteButton = new Button(this);
        inviteButton.setText("Invite Friends");
        inviteButton.setOnClickListener(v -> showInviteFriendsDialog());
        inviteButton.setVisibility(View.GONE);
        layout.addView(inviteButton);

        leaveButton = new Button(this);
        leaveButton.setText("Leave Alliance");
        leaveButton.setOnClickListener(v -> confirmLeaveAlliance());
        leaveButton.setVisibility(View.GONE);
        layout.addView(leaveButton);

        manageButton = new Button(this);
        manageButton.setText("Manage Alliance");
        manageButton.setOnClickListener(v -> Toast.makeText(this, "Manage - coming soon", Toast.LENGTH_SHORT).show());
        manageButton.setVisibility(View.GONE);
        layout.addView(manageButton);

        deleteButton = new Button(this);
        deleteButton.setText("Delete Alliance");
        deleteButton.setOnClickListener(v -> confirmDeleteAlliance());
        deleteButton.setVisibility(View.GONE);
        layout.addView(deleteButton);

        // Members list
        memberList = new RecyclerView(this);
        memberList.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new AllianceMemberAdapter();
        memberList.setAdapter(memberAdapter);
        memberList.setVisibility(View.GONE);
        layout.addView(memberList);

        setContentView(layout);
    }

    private void loadAllianceStatus() {
        // Učitaj pravi alliance status iz baze podataka
        int currentUserId = sessionManager.getUserId();
        
        if (currentUserId != -1) {
            // Potraži membership za trenutnog korisnika u background thread-u
            new Thread(() -> {
                try {
                    currentMembership = allianceMemberDao.getMembershipForUser((long) currentUserId);
                    
                    if (currentMembership != null) {
                        // Korisnik je u alliance-u, učitaj alliance
                        currentAlliance = allianceDao.getById(currentMembership.getAllianceId());
                        
                        runOnUiThread(() -> {
                            if (currentAlliance != null) {
                                showAllianceState();
                            } else {
                                showNoAllianceState();
                            }
                        });
                    } else {
                        // Korisnik nije u alliance-u
                        runOnUiThread(() -> showNoAllianceState());
                    }
                } catch (Exception e) {
                    // Greška ili baza nije kreirana, fallback na test alliance
                    runOnUiThread(() -> createTestAlliance(currentUserId));
                }
            }).start();
        } else {
            showNoAllianceState();
        }
    }
    
    private void createTestAlliance(int currentUserId) {
        // Fallback: kreiraj test alliance ako baza nije dostupna
        currentAlliance = new Alliance();
        currentAlliance.setId(1L);
        currentAlliance.setName("Test Alliance");
        currentAlliance.setDescription("Demo alliance for testing chat functionality");
        currentAlliance.setLeaderId((long) currentUserId);
        currentAlliance.setMaxMembers(10);
        currentAlliance.setStatus("ACTIVE");
        
        currentMembership = new AllianceMember();
        currentMembership.setUserId((long) currentUserId);
        currentMembership.setAllianceId(1L);
        currentMembership.setRole("Leader");
        
        showAllianceState();
    }

    private void showNoAllianceState() {
        currentAlliance = null;
        allianceStatusText.setText("Alliance Status: No alliance");
        allianceLevelText.setText("Level: -");
        allianceMembersText.setText("Members: -");

        // Sakrij sve osim pretrage
        allianceNameText.setVisibility(View.GONE);
        allianceDescText.setVisibility(View.GONE);
        memberRoleText.setVisibility(View.GONE);

        chatButton.setVisibility(View.GONE);
        inviteButton.setVisibility(View.GONE);
        leaveButton.setVisibility(View.GONE);
        manageButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);

        searchButton.setVisibility(View.VISIBLE);
        memberList.setVisibility(View.GONE);
    }

    private void showAllianceState() {
        if (currentAlliance == null) {
            return;
        }

        allianceStatusText.setText("Alliance: " + currentAlliance.getName());
        allianceLevelText.setText("Level: 1"); // Privremeno fiksno
        allianceMembersText.setText("Members: 1/" + currentAlliance.getMaxMembers());

        allianceNameText.setText(currentAlliance.getName());
        allianceNameText.setVisibility(View.VISIBLE);

        allianceDescText.setText(currentAlliance.getDescription());
        allianceDescText.setVisibility(View.VISIBLE);

        // Uloge/članstvo (ako želiš, popuni memberRoleText na osnovu currentMembership)
        memberRoleText.setVisibility(View.GONE);

        // Prikaz dugmadi
        chatButton.setVisibility(View.VISIBLE);
        inviteButton.setVisibility(View.VISIBLE);
        leaveButton.setVisibility(View.VISIBLE);

        // (Opcionalno) prikaz menadžment dugmadi za lidera — dodaćeš uslov kada budeš imao currentMembership/currentUser
        manageButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);

        searchButton.setVisibility(View.GONE);

        // Lista članova
        memberList.setVisibility(View.VISIBLE);
        loadMemberList();
    }

    private void loadMemberList() {
        // TODO: Učitati članove iz baze
        // Placeholder: prazna lista
        List<AllianceMember> members = new ArrayList<>();
        memberAdapter.updateMembers(members);
    }

    private void showJoinAllianceDialog() {
        android.widget.EditText nameEdit = new android.widget.EditText(this);
        nameEdit.setHint("Alliance name to join");

        new AlertDialog.Builder(this)
                .setTitle("Join Alliance")
                .setView(nameEdit)
                .setPositiveButton("Join", (dialog, which) -> {
                    String name = nameEdit.getText().toString().trim();
                    if (!name.isEmpty()) {
                        joinAlliance(name);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showCreateAllianceDialog() {
        android.widget.EditText nameEdit = new android.widget.EditText(this);
        nameEdit.setHint("Alliance name");

        new AlertDialog.Builder(this)
                .setTitle("Create Alliance")
                .setView(nameEdit)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = nameEdit.getText().toString().trim();
                    if (!name.isEmpty()) {
                        createAlliance(name);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void createAlliance(String name) {
        // TODO: Snimi novi savez u bazu
        Toast.makeText(this, "Alliance created: " + name, Toast.LENGTH_SHORT).show();
        allianceStatusText.setText("Alliance Status: Leader of " + name);
        // Po želji, postavi currentAlliance i pozovi showAllianceState()
    }

    private void joinAlliance(String name) {
        int currentUserId = sessionManager.getUserId();
        if (currentUserId == -1) {
            Toast.makeText(this, "You must be logged in to join an alliance", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Pridruži se alliance-u u background thread-u
        new Thread(() -> {
            try {
                // Proverti da li korisnik već ima alliance
                AllianceMember existingMembership = allianceMemberDao.getMembershipForUser((long) currentUserId);
                if (existingMembership != null) {
                    runOnUiThread(() -> Toast.makeText(this, "You are already in an alliance!", Toast.LENGTH_SHORT).show());
                    return;
                }
                
                // Pronađi alliance po imenu
                Alliance targetAlliance = allianceDao.getByName(name);
                if (targetAlliance == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Alliance '" + name + "' not found!", Toast.LENGTH_SHORT).show());
                    return;
                }
                
                // Kreiraj membership
                AllianceMember newMembership = new AllianceMember();
                newMembership.setAllianceId(targetAlliance.getId());
                newMembership.setUserId((long) currentUserId);
                newMembership.setRole("Member");
                newMembership.setActive(true);
                allianceMemberDao.insert(newMembership);
                
                // Ažuriraj UI
                runOnUiThread(() -> {
                    currentAlliance = targetAlliance;
                    currentMembership = newMembership;
                    Toast.makeText(this, "Successfully joined '" + name + "' alliance!", Toast.LENGTH_SHORT).show();
                    showAllianceState();
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error joining alliance: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void openAllianceChat() {
        Intent intent = new Intent(this, ChatActivity.class);
        if (currentAlliance != null) {
            // uskladi getter sa svojim modelom (getAllianceId ili getId)
            intent.putExtra("allianceId", currentAlliance.getId());
            intent.putExtra("allianceName", currentAlliance.getName());
        }
        startActivity(intent);
    }

    private void showInviteFriendsDialog() {
        // TODO: Prikaz prijatelja i slanje pozivnica
        Toast.makeText(this, "Invite Friends - Feature coming soon", Toast.LENGTH_SHORT).show();
    }

    private void confirmLeaveAlliance() {
        new AlertDialog.Builder(this)
                .setTitle("Leave Alliance")
                .setMessage("Are you sure you want to leave this alliance?")
                .setPositiveButton("Leave", (dialog, which) -> leaveAlliance())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void leaveAlliance() {
        // TODO: Ukloni korisnika iz saveza u bazi
        Toast.makeText(this, "You have left the alliance", Toast.LENGTH_SHORT).show();
        loadAllianceStatus();
    }

    private void confirmDeleteAlliance() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Alliance")
                .setMessage("Are you sure you want to delete this alliance? All members will be removed.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAlliance())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAlliance() {
        // TODO: Obrisi savez i članstva iz baze
        Toast.makeText(this, "Alliance deleted", Toast.LENGTH_SHORT).show();
        loadAllianceStatus();
    }
}
