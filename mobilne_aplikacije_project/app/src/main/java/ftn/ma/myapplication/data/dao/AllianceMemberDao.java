package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.AllianceMember;

/**
 * DAO za upravljanje članovima saveza u bazi podataka
 */
@Dao
public interface AllianceMemberDao {
    
    // CRUD operacije
    @Insert
    long insertMember(AllianceMember member);
    
    @Update
    void updateMember(AllianceMember member);
    
    @Delete
    void deleteMember(AllianceMember member);
    
    // Pretraga članova
    @Query("SELECT * FROM alliance_members WHERE membershipId = :membershipId")
    AllianceMember getMemberById(int membershipId);
    
    @Query("SELECT * FROM alliance_members WHERE allianceId = :allianceId AND isActive = 1 ORDER BY role DESC, joinedAt ASC")
    List<AllianceMember> getMembersForAlliance(int allianceId);
    
    @Query("SELECT * FROM alliance_members WHERE allianceId = :allianceId AND isActive = 1 ORDER BY role DESC, joinedAt ASC")
    List<AllianceMember> getMembersByAlliance(int allianceId);
    
    @Query("SELECT * FROM alliance_members WHERE userId = :userId AND isActive = 1")
    AllianceMember getCurrentMembershipForUser(int userId);
    
    @Query("SELECT * FROM alliance_members WHERE userId = :userId AND isActive = 1")
    AllianceMember getMemberByUserId(int userId);
    
    @Query("SELECT * FROM alliance_members WHERE allianceId = :allianceId AND userId = :userId AND isActive = 1")
    AllianceMember getMemberInAlliance(int allianceId, int userId);
    
    @Query("SELECT * FROM alliance_members WHERE allianceId = :allianceId AND role = 'LEADER' AND isActive = 1")
    AllianceMember getAllianceLeader(int allianceId);
    
    // Proverava članstvo
    @Query("SELECT COUNT(*) > 0 FROM alliance_members WHERE userId = :userId AND isActive = 1")
    boolean isUserInAnyAlliance(int userId);
    
    @Query("SELECT COUNT(*) > 0 FROM alliance_members WHERE allianceId = :allianceId AND userId = :userId AND isActive = 1")
    boolean isUserInAlliance(int allianceId, int userId);
    
    // Upravljanje ulogama
    @Query("UPDATE alliance_members SET role = 'LEADER' WHERE membershipId = :membershipId")
    void promoteToLeader(int membershipId);
    
    @Query("UPDATE alliance_members SET role = 'MEMBER' WHERE allianceId = :allianceId AND role = 'LEADER'")
    void demoteCurrentLeader(int allianceId);
    
    // Upravljanje članstvom
    @Query("UPDATE alliance_members SET isActive = 0 WHERE membershipId = :membershipId")
    void removeMember(int membershipId);
    
    @Query("UPDATE alliance_members SET isActive = 0 WHERE userId = :userId AND isActive = 1")
    void removeUserFromAlliances(int userId);
    
    @Query("UPDATE alliance_members SET isActive = 0 WHERE allianceId = :allianceId")
    void removeAllMembersFromAlliance(int allianceId);
    
    // Doprinos
    @Query("UPDATE alliance_members SET contributionPoints = contributionPoints + :points WHERE membershipId = :membershipId")
    void addContributionPoints(int membershipId, int points);
    
    @Query("SELECT * FROM alliance_members WHERE allianceId = :allianceId AND isActive = 1 ORDER BY contributionPoints DESC LIMIT :limit")
    List<AllianceMember> getTopContributors(int allianceId, int limit);
    
    // Statistike
    @Query("SELECT COUNT(*) FROM alliance_members WHERE allianceId = :allianceId AND isActive = 1")
    int getMemberCountForAlliance(int allianceId);
    
    @Query("SELECT COUNT(*) FROM alliance_members WHERE userId = :userId")
    int getTotalMembershipsForUser(int userId);
    
    @Query("SELECT SUM(contributionPoints) FROM alliance_members WHERE allianceId = :allianceId AND isActive = 1")
    int getTotalContributionForAlliance(int allianceId);
    
    @Query("SELECT AVG(contributionPoints) FROM alliance_members WHERE allianceId = :allianceId AND isActive = 1")
    double getAverageContributionForAlliance(int allianceId);
    
    // Transfer ownership
    @Transaction
    default void transferLeadership(int allianceId, int newLeaderMembershipId) {
        removeCurrentLeader(allianceId);
        promoteToLeader(newLeaderMembershipId);
    }
    
    @Query("UPDATE alliance_members SET role = 'MEMBER' WHERE allianceId = :allianceId AND role = 'LEADER'")
    void removeCurrentLeader(int allianceId);
    
    // Cleanup operacije
    @Query("DELETE FROM alliance_members WHERE isActive = 0 AND joinedAt < datetime('now', '-90 days')")
    void cleanupInactiveMembers();
    
    @Query("DELETE FROM alliance_members WHERE allianceId = :allianceId")
    void deleteAllMembersFromAlliance(int allianceId);
    
    @Query("DELETE FROM alliance_members")
    void deleteAllMembers();
}