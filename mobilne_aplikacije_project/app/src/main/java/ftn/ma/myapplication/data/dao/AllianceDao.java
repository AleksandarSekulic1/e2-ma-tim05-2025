package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.Alliance;

/**
 * DAO za upravljanje savezima u bazi podataka
 */
@Dao
public interface AllianceDao {
    
    // CRUD operacije
    @Insert
    long insertAlliance(Alliance alliance);
    
    @Update
    void updateAlliance(Alliance alliance);
    
    @Delete
    void deleteAlliance(Alliance alliance);
    
    // Pretraga saveza
    @Query("SELECT * FROM alliances WHERE allianceId = :allianceId")
    Alliance getAllianceById(int allianceId);
    
    @Query("SELECT * FROM alliances WHERE leaderId = :leaderId AND status != 'DISBANDED'")
    Alliance getAllianceByLeader(int leaderId);
    
    @Query("SELECT * FROM alliances WHERE name = :name AND status != 'DISBANDED'")
    Alliance getAllianceByName(String name);
    
    @Query("SELECT * FROM alliances WHERE status = 'RECRUITING' ORDER BY createdAt DESC")
    List<Alliance> getRecruitingAlliances();
    
    @Query("SELECT * FROM alliances WHERE status != 'DISBANDED' ORDER BY createdAt DESC")
    List<Alliance> getAllActiveAlliances();
    
    // Pretraga saveza po imenu
    @Query("SELECT * FROM alliances WHERE name LIKE '%' || :searchQuery || '%' AND status = 'RECRUITING' ORDER BY name")
    List<Alliance> searchAlliances(String searchQuery);
    
    // Status operacije
    @Query("UPDATE alliances SET status = :status WHERE allianceId = :allianceId")
    void updateAllianceStatus(int allianceId, Alliance.AllianceStatus status);
    
    @Query("UPDATE alliances SET status = 'MISSION_ACTIVE', missionStartedAt = datetime('now') WHERE allianceId = :allianceId")
    void startMission(int allianceId);
    
    @Query("UPDATE alliances SET status = 'DISBANDED' WHERE allianceId = :allianceId")
    void disbandAlliance(int allianceId);
    
    // Upravljanje članovima
    @Query("UPDATE alliances SET currentMemberCount = currentMemberCount + 1 WHERE allianceId = :allianceId")
    void incrementMemberCount(int allianceId);
    
    @Query("UPDATE alliances SET currentMemberCount = currentMemberCount - 1 WHERE allianceId = :allianceId AND currentMemberCount > 0")
    void decrementMemberCount(int allianceId);
    
    @Query("UPDATE alliances SET currentMemberCount = (SELECT COUNT(*) FROM alliance_members WHERE allianceId = :allianceId AND isActive = 1) WHERE allianceId = :allianceId")
    void syncMemberCount(int allianceId);
    
    // Statistike
    @Query("SELECT COUNT(*) FROM alliances WHERE status != 'DISBANDED'")
    int getTotalActiveAlliances();
    
    @Query("SELECT COUNT(*) FROM alliances WHERE status = 'RECRUITING'")
    int getRecruitingAlliancesCount();
    
    @Query("SELECT COUNT(*) FROM alliances WHERE status = 'MISSION_ACTIVE'")
    int getActivemissionsCount();
    
    @Query("SELECT AVG(currentMemberCount) FROM alliances WHERE status != 'DISBANDED'")
    double getAverageMemberCount();
    
    // Cleanup operacije
    @Query("DELETE FROM alliances WHERE status = 'DISBANDED' AND createdAt < datetime('now', '-90 days')")
    void cleanupOldDisbandedAlliances();
    
    @Query("DELETE FROM alliances")
    void deleteAllAlliances();
}