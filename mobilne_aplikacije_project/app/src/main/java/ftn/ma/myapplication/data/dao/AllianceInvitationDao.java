package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.AllianceInvitation;

/**
 * DAO za upravljanje pozivima u savez u bazi podataka
 */
@Dao
public interface AllianceInvitationDao {
    
    // CRUD operacije
    @Insert
    long insertInvitation(AllianceInvitation invitation);
    
    @Update
    void updateInvitation(AllianceInvitation invitation);
    
    @Delete
    void deleteInvitation(AllianceInvitation invitation);
    
    // Pretraga poziva
    @Query("SELECT * FROM alliance_invitations WHERE invitationId = :invitationId")
    AllianceInvitation getInvitationById(int invitationId);
    
    @Query("SELECT * FROM alliance_invitations WHERE inviteeId = :userId AND status = 'PENDING' ORDER BY createdAt DESC")
    List<AllianceInvitation> getPendingInvitationsForUser(int userId);
    
    @Query("SELECT * FROM alliance_invitations WHERE inviterId = :userId ORDER BY createdAt DESC")
    List<AllianceInvitation> getSentInvitationsByUser(int userId);
    
    @Query("SELECT * FROM alliance_invitations WHERE allianceId = :allianceId ORDER BY createdAt DESC")
    List<AllianceInvitation> getInvitationsForAlliance(int allianceId);
    
    @Query("SELECT * FROM alliance_invitations WHERE allianceId = :allianceId AND status = 'PENDING' ORDER BY createdAt DESC")
    List<AllianceInvitation> getPendingInvitationsForAlliance(int allianceId);
    
    // Proverava postojanje poziva
    @Query("SELECT * FROM alliance_invitations WHERE allianceId = :allianceId AND inviteeId = :inviteeId AND status = 'PENDING'")
    AllianceInvitation getExistingPendingInvitation(int allianceId, int inviteeId);
    
    @Query("SELECT COUNT(*) > 0 FROM alliance_invitations WHERE allianceId = :allianceId AND inviteeId = :inviteeId AND status = 'PENDING'")
    boolean hasPendingInvitation(int allianceId, int inviteeId);
    
    // Status operacije
    @Query("UPDATE alliance_invitations SET status = :status, respondedAt = datetime('now') WHERE invitationId = :invitationId")
    void updateInvitationStatus(int invitationId, AllianceInvitation.InvitationStatus status);
    
    @Query("UPDATE alliance_invitations SET status = 'ACCEPTED', respondedAt = datetime('now') WHERE invitationId = :invitationId")
    void acceptInvitation(int invitationId);
    
    @Query("UPDATE alliance_invitations SET status = 'REJECTED', respondedAt = datetime('now') WHERE invitationId = :invitationId")
    void rejectInvitation(int invitationId);
    
    @Query("UPDATE alliance_invitations SET status = 'EXPIRED', respondedAt = datetime('now') WHERE invitationId = :invitationId")
    void expireInvitation(int invitationId);
    
    // Expire old invitations
    @Query("UPDATE alliance_invitations SET status = 'EXPIRED', respondedAt = datetime('now') WHERE status = 'PENDING' AND createdAt < datetime('now', '-7 days')")
    void expireOldInvitations();
    
    // Notifikacije
    @Query("SELECT COUNT(*) FROM alliance_invitations WHERE inviteeId = :userId AND status = 'PENDING'")
    int getPendingInvitationsCount(int userId);
    
    @Query("SELECT * FROM alliance_invitations WHERE inviteeId = :userId AND status = 'PENDING' ORDER BY createdAt DESC LIMIT 1")
    AllianceInvitation getLatestPendingInvitation(int userId);
    
    // Batch operations
    @Query("UPDATE alliance_invitations SET status = 'EXPIRED', respondedAt = datetime('now') WHERE allianceId = :allianceId AND status = 'PENDING'")
    void expireAllPendingInvitationsForAlliance(int allianceId);
    
    @Query("UPDATE alliance_invitations SET status = 'EXPIRED', respondedAt = datetime('now') WHERE inviteeId = :userId AND status = 'PENDING'")
    void expireAllPendingInvitationsForUser(int userId);
    
    // Statistike
    @Query("SELECT COUNT(*) FROM alliance_invitations WHERE allianceId = :allianceId")
    int getTotalInvitationsForAlliance(int allianceId);
    
    @Query("SELECT COUNT(*) FROM alliance_invitations WHERE allianceId = :allianceId AND status = 'ACCEPTED'")
    int getAcceptedInvitationsForAlliance(int allianceId);
    
    @Query("SELECT COUNT(*) FROM alliance_invitations WHERE inviterId = :userId")
    int getTotalInvitationsSentByUser(int userId);
    
    @Query("SELECT COUNT(*) FROM alliance_invitations WHERE inviteeId = :userId")
    int getTotalInvitationsReceivedByUser(int userId);
    
    // Cleanup operacije
    @Query("DELETE FROM alliance_invitations WHERE status IN ('REJECTED', 'EXPIRED') AND createdAt < datetime('now', '-30 days')")
    void cleanupOldInvitations();
    
    @Query("DELETE FROM alliance_invitations WHERE allianceId = :allianceId")
    void deleteAllInvitationsForAlliance(int allianceId);
    
    @Query("DELETE FROM alliance_invitations")
    void deleteAllInvitations();
}