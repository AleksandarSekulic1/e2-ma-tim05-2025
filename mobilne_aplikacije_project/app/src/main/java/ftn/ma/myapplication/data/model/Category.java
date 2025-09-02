package ftn.ma.myapplication.data.model;
import java.io.Serializable; // Dodajte ovaj import
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private int color; // In Android, colors are most often stored as an integer value

    // An empty constructor is needed for working with databases (Firebase/SQLite)
    public Category() {
    }

    // Constructor for easily creating new objects
    public Category(long id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public Category(String name, int color) {
        this.name = name;
        this.color = color;
    }

    // Getters and Setters - methods for accessing and changing private fields

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
