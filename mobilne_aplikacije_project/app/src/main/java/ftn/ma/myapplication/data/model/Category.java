package ftn.ma.myapplication.data.model;

public class Category {

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
}
