package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.util.Date;

/**
 * Model klasa za članove saveza
 */
@Entity(tableName = "alliance_members")
public class AllianceMember {
    
    @PrimaryKey(autoGenerate = true)
    private int membershipId;
    
    private int allianceId;       // ID saveza
    private int userId;           // ID korisnika
    private String username;      // Username korisnika
    private MemberRole role;      // Uloga u savezu
    private Date joinedAt;        // Datum pridruživanja
    private boolean isActive;     // Da li je član aktivan
    private int contributionPoints; // Poeni doprinosa
    
    public enum MemberRole {
        LEADER,     // Vođa saveza
        MEMBER      // Običan član
    }
    
    public AllianceMember() {
        this.joinedAt = new Date();
        this.role = MemberRole.MEMBER;
        this.isActive = true;
        this.contributionPoints = 0;
    }
    
    @Ignore
    public AllianceMember(int allianceId, int userId, String username, MemberRole role) {
        this();
        this.allianceId = allianceId;
        this.userId = userId;
        this.username = username;
        this.role = role;
    }
    
    /**
     * Proverava da li je član vođa
     */
    public boolean isLeader() {
        return role == MemberRole.LEADER;
    }
    
    /**
     * Promovise člana u vođu (transfer leadership)
     */
    public void promoteToLeader() {
        this.role = MemberRole.LEADER;
    }
    
    /**
     * Uklanja člana iz saveza
     */
    public void leave() {
        this.isActive = false;
    }
    
    /**
     * Dodaje poene doprinosa
     */
    public void addContributionPoints(int points) {
        this.contributionPoints += points;
    }
    
    /**
     * Formatira datum pridruživanja
     */
    public String getFormattedJoinDate() {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd.MM.yyyy");
        return formatter.format(joinedAt);
    }
    
    /**
     * Vraća display naziv uloge
     */
    public String getRoleDisplay() {
        switch (role) {
            case LEADER: return "Vođa";
            case MEMBER: return "Član";
            default: return "Nepoznato";
        }
    }
    
    // Getters and Setters
    public int getMembershipId() { return membershipId; }
    public void setMembershipId(int membershipId) { this.membershipId = membershipId; }
    
    public int getAllianceId() { return allianceId; }
    public void setAllianceId(int allianceId) { this.allianceId = allianceId; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public MemberRole getRole() { return role; }
    public void setRole(MemberRole role) { this.role = role; }
    
    public Date getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Date joinedAt) { this.joinedAt = joinedAt; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public int getContributionPoints() { return contributionPoints; }
    public void setContributionPoints(int contributionPoints) { this.contributionPoints = contributionPoints; }
}