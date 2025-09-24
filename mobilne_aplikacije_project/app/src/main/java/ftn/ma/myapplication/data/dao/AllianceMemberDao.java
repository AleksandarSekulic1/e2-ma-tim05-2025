package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.AllianceMember;

@Dao
public interface AllianceMemberDao {
    
    @Insert
    long insert(AllianceMember member);
    
    @Update
    void update(AllianceMember member);
    
    @Delete
    void delete(AllianceMember member);
    
    @Query("SELECT * FROM alliance_members WHERE allianceId = :allianceId")
    List<AllianceMember> getMembersForAlliance(long allianceId);
    
    @Query("SELECT * FROM alliance_members WHERE userId = :userId")
    AllianceMember getMembershipForUser(long userId);
    
    @Query("DELETE FROM alliance_members WHERE userId = :userId")
    void removeUserFromAlliance(long userId);
}
