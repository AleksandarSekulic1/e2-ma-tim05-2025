package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.Notification;

@Dao
public interface NotificationDao {
    
    @Insert
    long insert(Notification notification);
    
    @Update
    void update(Notification notification);
    
    @Delete
    void delete(Notification notification);
    
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    List<Notification> getNotificationsForUser(long userId);
    
    @Query("SELECT * FROM notifications WHERE userId = :userId AND status = 'UNREAD'")
    List<Notification> getUnreadNotificationsForUser(long userId);
    
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND status = 'UNREAD'")
    int getUnreadCount(long userId);
}
