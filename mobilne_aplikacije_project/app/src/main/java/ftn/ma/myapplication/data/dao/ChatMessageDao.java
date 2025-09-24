package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.ChatMessage;

@Dao
public interface ChatMessageDao {
    
    @Insert
    long insert(ChatMessage message);
    
    @Update
    void update(ChatMessage message);
    
    @Delete
    void delete(ChatMessage message);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesForAlliance(long allianceId);
    
    @Query("SELECT * FROM chat_messages WHERE id = :id")
    ChatMessage getById(long id);
    
    @Query("DELETE FROM chat_messages WHERE allianceId = :allianceId")
    void deleteAllMessagesForAlliance(long allianceId);
}
