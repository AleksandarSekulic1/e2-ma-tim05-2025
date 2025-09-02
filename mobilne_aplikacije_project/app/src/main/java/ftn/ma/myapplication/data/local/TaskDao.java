package ftn.ma.myapplication.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update; // Dodajte ovaj import
import androidx.room.Delete; // Dodajte ovaj import
import java.util.List;

import ftn.ma.myapplication.data.model.Task;

@Dao
public interface TaskDao {

    @Insert
    void insert(Task task);

    @Update // NOVA METODA
    void update(Task task);

    @Delete // NOVA METODA
    void delete(Task task);

    @Query("SELECT * FROM tasks")
    List<Task> getAllTasks();
}
