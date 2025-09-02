package ftn.ma.myapplication.data.model;
import java.io.Serializable;
import java.util.Date;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks")
public class Task implements Serializable {

    // --- Enums for predefined values ---
    // The enum TYPE is in English, but the VALUES remain in Serbian
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
        PAUZIRAN,
        OTKAZAN
    }

    public enum RepetitionUnit {
        DAN,
        NEDELJA
    }
    @PrimaryKey(autoGenerate = true)

    // --- Attributes of the Task class (in English) ---
    private long id;
    private String name;
    private String description;
    @Ignore
    private Category category;
    private Date executionTime;
    private Difficulty difficulty; // The variable is of type Difficulty
    private Importance importance; // The variable is of type Importance
    private Status status;

    // Attributes for recurring tasks
    private boolean isRecurring;
    private Date startDate;
    private Date endDate;
    private int repetitionInterval;
    private RepetitionUnit repetitionUnit; // The variable is of type RepetitionUnit
    // --- NOVO POLJE ---
    private boolean xpAwarded;

    // --- NOVO: Polje za čuvanje ID-ja kategorije ---
    private long categoryId;

    // --- NOVO: Getteri i setteri za categoryId ---
    public long getCategoryId() {
        return categoryId;
    }

    // --- Constructors ---
    public Task() {
        // Empty constructor
    }

    // --- Getters and Setters (in English) ---

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Importance getImportance() {
        return importance;
    }

    public void setImportance(Importance importance) {
        this.importance = importance;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public int getRepetitionInterval() {
        return repetitionInterval;
    }

    public void setRepetitionInterval(int repetitionInterval) {
        this.repetitionInterval = repetitionInterval;
    }

    public RepetitionUnit getRepetitionUnit() {
        return repetitionUnit;
    }

    public void setRepetitionUnit(RepetitionUnit repetitionUnit) {
        this.repetitionUnit = repetitionUnit;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isXpAwarded() {
        return xpAwarded;
    }

    public void setXpAwarded(boolean xpAwarded) {
        this.xpAwarded = xpAwarded;
    }
}