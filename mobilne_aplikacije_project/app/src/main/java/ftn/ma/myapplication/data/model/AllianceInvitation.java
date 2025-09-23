package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.util.Date;

/**
 * Model klasa za pozive u savez
 */
@Entity(tableName = "alliance_invitations")
public class AllianceInvitation {
    
    @PrimaryKey(autoGenerate = true)
    private int invitationId;
    
    private int allianceId;           // ID saveza
    private String allianceName;      // Naziv saveza
    private int inviterId;            // ID korisnika koji poziva
    private String inviterUsername;   // Username korisnika koji poziva
    private int inviteeId;            // ID korisnika koji je pozvan
    private String inviteeUsername;   // Username korisnika koji je pozvan
    private InvitationStatus status;
    private Date createdAt;
    private Date respondedAt;
    private String message;           // Opciona poruka uz poziv
    
    public enum InvitationStatus {
        PENDING,    // Poziv poslat, čeka se odgovor
        ACCEPTED,   // Poziv prihvaćen
        REJECTED,   // Poziv odbačen
        EXPIRED     // Poziv istekao
    }
    
    public AllianceInvitation() {
        this.createdAt = new Date();
        this.status = InvitationStatus.PENDING;
    }
    
    @Ignore
    public AllianceInvitation(int allianceId, String allianceName, int inviterId, 
                             String inviterUsername, int inviteeId, String inviteeUsername, 
                             String message) {
        this();
        this.allianceId = allianceId;
        this.allianceName = allianceName;
        this.inviterId = inviterId;
        this.inviterUsername = inviterUsername;
        this.inviteeId = inviteeId;
        this.inviteeUsername = inviteeUsername;
        this.message = message;
    }
    
    /**
     * Prihvata poziv
     */
    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
        this.respondedAt = new Date();
    }
    
    /**
     * Odbacuje poziv
     */
    public void reject() {
        this.status = InvitationStatus.REJECTED;
        this.respondedAt = new Date();
    }
    
    /**
     * Označava poziv kao istekao
     */
    public void expire() {
        this.status = InvitationStatus.EXPIRED;
        this.respondedAt = new Date();
    }
    
    /**
     * Proverava da li poziv čeka odgovor
     */
    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }
    
    /**
     * Proverava da li je poziv prihvaćen
     */
    public boolean isAccepted() {
        return status == InvitationStatus.ACCEPTED;
    }
    
    /**
     * Kreira poruku notifikacije
     */
    public String getNotificationMessage() {
        return inviterUsername + " te poziva u savez '" + allianceName + "'";
    }
    
    // Getters and Setters
    public int getInvitationId() { return invitationId; }
    public void setInvitationId(int invitationId) { this.invitationId = invitationId; }
    
    public int getAllianceId() { return allianceId; }
    public void setAllianceId(int allianceId) { this.allianceId = allianceId; }
    
    public String getAllianceName() { return allianceName; }
    public void setAllianceName(String allianceName) { this.allianceName = allianceName; }
    
    public int getInviterId() { return inviterId; }
    public void setInviterId(int inviterId) { this.inviterId = inviterId; }
    
    public String getInviterUsername() { return inviterUsername; }
    public void setInviterUsername(String inviterUsername) { this.inviterUsername = inviterUsername; }
    
    public int getInviteeId() { return inviteeId; }
    public void setInviteeId(int inviteeId) { this.inviteeId = inviteeId; }
    
    public String getInviteeUsername() { return inviteeUsername; }
    public void setInviteeUsername(String inviteeUsername) { this.inviteeUsername = inviteeUsername; }
    
    public InvitationStatus getStatus() { return status; }
    public void setStatus(InvitationStatus status) { this.status = status; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getRespondedAt() { return respondedAt; }
    public void setRespondedAt(Date respondedAt) { this.respondedAt = respondedAt; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}