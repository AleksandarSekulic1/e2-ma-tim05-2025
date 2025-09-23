package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.ChatMessage;

/**
 * DAO za upravljanje porukama u chat-u saveza u bazi podataka
 */
@Dao
public interface ChatMessageDao {
    
    // CRUD operacije
    @Insert
    long insertMessage(ChatMessage message);
    
    @Update
    void updateMessage(ChatMessage message);
    
    @Delete
    void deleteMessage(ChatMessage message);
    
    // Pretraga poruka
    @Query("SELECT * FROM chat_messages WHERE messageId = :messageId")
    ChatMessage getMessageById(int messageId);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesForAlliance(int allianceId);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId ORDER BY timestamp DESC LIMIT :limit")
    List<ChatMessage> getRecentMessagesForAlliance(int allianceId, int limit);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    List<ChatMessage> getMessagesForAlliancePaginated(int allianceId, int limit, int offset);
    
    @Query("SELECT * FROM chat_messages WHERE senderId = :userId ORDER BY timestamp DESC")
    List<ChatMessage> getMessagesByUser(int userId);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND senderId = :userId ORDER BY timestamp DESC")
    List<ChatMessage> getMessagesFromUserInAlliance(int allianceId, int userId);
    
    // Filtriraj po tipu poruke
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND type = :type ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesByType(int allianceId, ChatMessage.MessageType type);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND type = 'TEXT' ORDER BY timestamp ASC")
    List<ChatMessage> getTextMessagesForAlliance(int allianceId);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND type = 'SYSTEM' ORDER BY timestamp ASC")
    List<ChatMessage> getSystemMessagesForAlliance(int allianceId);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND type = 'ANNOUNCEMENT' ORDER BY timestamp DESC")
    List<ChatMessage> getAnnouncementsForAlliance(int allianceId);
    
    // Pretraga po sadržaju
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND content LIKE '%' || :searchText || '%' ORDER BY timestamp DESC")
    List<ChatMessage> searchMessagesInAlliance(int allianceId, String searchText);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND senderUsername LIKE '%' || :usernameQuery || '%' ORDER BY timestamp DESC")
    List<ChatMessage> searchMessagesByUsername(int allianceId, String usernameQuery);
    
    // Vreme i datum
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND timestamp >= :fromDate ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesFromDate(int allianceId, String fromDate);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND timestamp BETWEEN :fromDate AND :toDate ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesBetweenDates(int allianceId, String fromDate, String toDate);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND date(timestamp) = date('now') ORDER BY timestamp ASC")
    List<ChatMessage> getTodaysMessages(int allianceId);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId ORDER BY timestamp DESC LIMIT 1")
    ChatMessage getLatestMessage(int allianceId);
    
    // Read status tracking
    @Query("UPDATE chat_messages SET isRead = 1 WHERE messageId = :messageId")
    void markMessageAsRead(int messageId);
    
    @Query("UPDATE chat_messages SET isRead = 1 WHERE allianceId = :allianceId AND messageId <= :lastMessageId")
    void markMessagesAsReadUpTo(int allianceId, int lastMessageId);
    
    @Query("UPDATE chat_messages SET isRead = 1 WHERE allianceId = :allianceId")
    void markAllMessagesAsRead(int allianceId);
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE allianceId = :allianceId AND isRead = 0 AND senderId != :userId")
    int getUnreadMessagesCount(int allianceId, int userId);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND isRead = 0 AND senderId != :userId ORDER BY timestamp ASC")
    List<ChatMessage> getUnreadMessages(int allianceId, int userId);
    
    // Statistike
    @Query("SELECT COUNT(*) FROM chat_messages WHERE allianceId = :allianceId")
    int getTotalMessagesForAlliance(int allianceId);
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE allianceId = :allianceId AND type = 'TEXT'")
    int getTextMessagesCount(int allianceId);
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE senderId = :userId")
    int getTotalMessagesByUser(int userId);
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE allianceId = :allianceId AND senderId = :userId")
    int getMessagesCountByUserInAlliance(int allianceId, int userId);
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE allianceId = :allianceId AND date(timestamp) = date('now')")
    int getTodaysMessagesCount(int allianceId);
    
    @Query("SELECT COUNT(DISTINCT senderId) FROM chat_messages WHERE allianceId = :allianceId")
    int getUniqueParticipantsCount(int allianceId);
    
    // Posebne operacije
    @Query("SELECT DISTINCT senderUsername FROM chat_messages WHERE allianceId = :allianceId AND type = 'TEXT' ORDER BY senderUsername")
    List<String> getChatParticipants(int allianceId);
    
    @Query("SELECT * FROM chat_messages WHERE allianceId = :allianceId AND senderId = :userId ORDER BY timestamp DESC LIMIT 1")
    ChatMessage getLastMessageFromUser(int allianceId, int userId);
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE allianceId = :allianceId AND senderId = :userId AND date(timestamp) = date('now')")
    int getUserTodaysMessagesCount(int allianceId, int userId);
    
    // Rate limiting check
    @Query("SELECT COUNT(*) FROM chat_messages WHERE senderId = :userId AND timestamp >= datetime('now', '-1 minute')")
    int getMessagesInLastMinute(int userId);
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE senderId = :userId AND timestamp >= datetime('now', '-1 hour')")
    int getMessagesInLastHour(int userId);
    
    // Cleanup operacije
    @Query("DELETE FROM chat_messages WHERE allianceId = :allianceId")
    void deleteAllMessagesForAlliance(int allianceId);
    
    @Query("DELETE FROM chat_messages WHERE senderId = :userId")
    void deleteAllMessagesByUser(int userId);
    
    @Query("DELETE FROM chat_messages WHERE timestamp < datetime('now', '-90 days')")
    void deleteOldMessages();
    
    @Query("DELETE FROM chat_messages WHERE allianceId = :allianceId AND timestamp < datetime('now', :olderThan)")
    void deleteOldMessagesForAlliance(int allianceId, String olderThan);
    
    @Query("DELETE FROM chat_messages")
    void deleteAllMessages();
    
    // Batch insert za migracije ili backup
    @Insert
    void insertMessages(List<ChatMessage> messages);
}
