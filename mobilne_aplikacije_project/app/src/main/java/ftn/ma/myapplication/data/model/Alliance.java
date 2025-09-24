package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "alliances")
public class Alliance {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String name;
    private String description;
    private long leaderId;
    private int maxMembers;
    private String status;
    
    public Alliance() {}
    
    public Alliance(String name, String description, long leaderId) {
        this.name = name;
        this.description = description;
        this.leaderId = leaderId;
        this.maxMembers = 10;
        this.status = "RECRUITING";
    }
    
    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public long getLeaderId() { return leaderId; }
    public void setLeaderId(long leaderId) { this.leaderId = leaderId; }
    
    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int maxMembers) { this.maxMembers = maxMembers; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
