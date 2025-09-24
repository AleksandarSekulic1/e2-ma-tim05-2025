package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.AllianceInvitation;

@Dao
public interface AllianceInvitationDao {
    
    @Insert
    long insert(AllianceInvitation invitation);
    
    @Update
    void update(AllianceInvitation invitation);
    
    @Delete
    void delete(AllianceInvitation invitation);
    
    @Query("SELECT * FROM alliance_invitations WHERE invitedUserId = :userId AND status = 'PENDING'")
    List<AllianceInvitation> getPendingInvitationsForUser(long userId);
    
    @Query("SELECT * FROM alliance_invitations WHERE allianceId = :allianceId")
    List<AllianceInvitation> getInvitationsForAlliance(long allianceId);
}
