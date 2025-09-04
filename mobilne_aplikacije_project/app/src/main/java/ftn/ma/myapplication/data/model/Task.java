package ftn.ma.myapplication.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "tasks")
public class Task implements Serializable {

    public enum Difficulty {
        VEOMA_LAK,
        LAK,
        TEZAK,
        EKSTREMNO_TEZAK
    }

    public enum Importance {
        NORMALAN,
        VAZAN,
        EKSTREMNO_VAZAN,
        SPECIJALAN
    }

    public enum Status {
        AKTIVAN,
        URADJEN,
        NEURADJEN,
        PAUZIRAN, // Pauziran status se sada može odnositi na celu seriju, ali ga zadržavamo
        OTKAZAN
    }

    // --- RepetitionUnit enum je sada uklonjen jer više nije potreban u ovom modelu ---

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String description;
    private Date executionTime; // Ovo je sada ključni datum za svaki zadatak
    private Difficulty difficulty;
    private Importance importance;
    private Status status;
    private long categoryId;
    private boolean xpAwarded;
    private Date completionDate;

    // --- NOVO POLJE ZA POVEZIVANJE PONAVLJAJUĆIH ZADATAKA ---
    // Biće null za jednokratne zadatke, a imaće vrednost za zadatke iz iste serije
    private Long recurringGroupId;

    @Ignore
    private Category category;

    public Task() {
        // Prazan konstruktor
    }

    // --- Getters and Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getExecutionTime() { return executionTime; }
    public void setExecutionTime(Date executionTime) { this.executionTime = executionTime; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public Importance getImportance() { return importance; }
    public void setImportance(Importance importance) { this.importance = importance; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public boolean isXpAwarded() { return xpAwarded; }
    public void setXpAwarded(boolean xpAwarded) { this.xpAwarded = xpAwarded; }

    public Date getCompletionDate() { return completionDate; }
    public void setCompletionDate(Date completionDate) { this.completionDate = completionDate; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    // --- Getteri i setteri za novo polje ---
    public Long getRecurringGroupId() { return recurringGroupId; }
    public void setRecurringGroupId(Long recurringGroupId) { this.recurringGroupId = recurringGroupId; }
}