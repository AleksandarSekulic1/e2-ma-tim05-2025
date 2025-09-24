package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "alliance_invitations")
public class AllianceInvitation {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long allianceId;
    private long invitedUserId;
    private long invitedByUserId;
    private String status;
    private Date createdAt;
    
    public AllianceInvitation() {}
    
    public AllianceInvitation(long allianceId, long invitedUserId, long invitedByUserId) {
        this.allianceId = allianceId;
        this.invitedUserId = invitedUserId;
        this.invitedByUserId = invitedByUserId;
        this.status = "PENDING";
        this.createdAt = new Date();
    }
    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getAllianceId() { return allianceId; }
    public void setAllianceId(long allianceId) { this.allianceId = allianceId; }
    
    public long getInvitedUserId() { return invitedUserId; }
    public void setInvitedUserId(long invitedUserId) { this.invitedUserId = invitedUserId; }
    
    public long getInvitedByUserId() { return invitedByUserId; }
    public void setInvitedByUserId(long invitedByUserId) { this.invitedByUserId = invitedByUserId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
