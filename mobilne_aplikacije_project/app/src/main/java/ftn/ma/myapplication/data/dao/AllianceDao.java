package ftn.ma.myapplication.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ftn.ma.myapplication.data.model.Alliance;

@Dao
public interface AllianceDao {
    
    @Insert
    long insert(Alliance alliance);
    
    @Update
    void update(Alliance alliance);
    
    @Delete
    void delete(Alliance alliance);
    
    @Query("SELECT * FROM alliances WHERE id = :id")
    Alliance getById(long id);
    
    @Query("SELECT * FROM alliances WHERE name = :name")
    Alliance getByName(String name);
    
    @Query("SELECT * FROM alliances")
    List<Alliance> getAllAlliances();
}
