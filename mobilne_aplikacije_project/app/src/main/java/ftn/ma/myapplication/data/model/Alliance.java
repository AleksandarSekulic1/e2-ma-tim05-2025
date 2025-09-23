package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.util.Date;

/**
 * Model klasa za savez korisnika
 */
@Entity(tableName = "alliances")
public class Alliance {
    
    @PrimaryKey(autoGenerate = true)
    private int allianceId;
    
    private String name;              // Naziv saveza
    private int leaderId;             // ID vođe saveza
    private String leaderUsername;    // Username vođe
    private AllianceStatus status;
    private Date createdAt;
    private Date missionStartedAt;
    private int maxMembers;           // Maksimalno članova (default 10)
    private int currentMemberCount;   // Trenutni broj članova
    private String description;       // Opis saveza
    
    public enum AllianceStatus {
        RECRUITING,      // Traže se novi članovi
        MISSION_ACTIVE,  // Misija je pokrenuta
        DISBANDED        // Savez ukinut
    }
    
    public Alliance() {
        this.createdAt = new Date();
        this.status = AllianceStatus.RECRUITING;
        this.maxMembers = 10;
        this.currentMemberCount = 1; // Vođa je član
    }
    
    @Ignore
    public Alliance(String name, int leaderId, String leaderUsername, String description) {
        this();
        this.name = name;
        this.leaderId = leaderId;
        this.leaderUsername = leaderUsername;
        this.description = description;
    }
    
    /**
     * Proverava da li je korisnik vođa saveza
     */
    public boolean isLeader(int userId) {
        return leaderId == userId;
    }
    
    /**
     * Proverava da li savez može da prima nove članove
     */
    public boolean canAcceptNewMembers() {
        return status == AllianceStatus.RECRUITING && currentMemberCount < maxMembers;
    }
    
    /**
     * Proverava da li je misija pokrenuta
     */
    public boolean isMissionActive() {
        return status == AllianceStatus.MISSION_ACTIVE;
    }
    
    /**
     * Pokreće misiju saveza
     */
    public void startMission() {
        this.status = AllianceStatus.MISSION_ACTIVE;
        this.missionStartedAt = new Date();
    }
    
    /**
     * Ukida savez
     */
    public void disband() {
        this.status = AllianceStatus.DISBANDED;
    }
    
    /**
     * Dodaje člana u savez
     */
    public void addMember() {
        this.currentMemberCount++;
    }
    
    /**
     * Uklanja člana iz saveza
     */
    public void removeMember() {
        if (currentMemberCount > 1) {
            this.currentMemberCount--;
        }
    }
    
    // Getters and Setters
    public int getAllianceId() { return allianceId; }
    public void setAllianceId(int allianceId) { this.allianceId = allianceId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getLeaderId() { return leaderId; }
    public void setLeaderId(int leaderId) { this.leaderId = leaderId; }
    
    public String getLeaderUsername() { return leaderUsername; }
    public void setLeaderUsername(String leaderUsername) { this.leaderUsername = leaderUsername; }
    
    public AllianceStatus getStatus() { return status; }
    public void setStatus(AllianceStatus status) { this.status = status; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getMissionStartedAt() { return missionStartedAt; }
    public void setMissionStartedAt(Date missionStartedAt) { this.missionStartedAt = missionStartedAt; }
    
    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int maxMembers) { this.maxMembers = maxMembers; }
    
    public int getCurrentMemberCount() { return currentMemberCount; }
    public void setCurrentMemberCount(int currentMemberCount) { this.currentMemberCount = currentMemberCount; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}