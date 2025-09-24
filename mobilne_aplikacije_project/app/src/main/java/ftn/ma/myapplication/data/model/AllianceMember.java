package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alliance_members")
public class AllianceMember {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long allianceId;
    private long userId;
    private String role;
    private boolean isActive;
    
    public AllianceMember() {}
    
    public AllianceMember(long allianceId, long userId, String role) {
        this.allianceId = allianceId;
        this.userId = userId;
        this.role = role;
        this.isActive = true;
    }
    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getAllianceId() { return allianceId; }
    public void setAllianceId(long allianceId) { this.allianceId = allianceId; }
    
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
