// SpecialMission.java
package ftn.ma.myapplication.data.model;

import java.io.Serializable;
import java.util.Calendar;

public class SpecialMission implements Serializable {
    private int id;
    private String title;
    private long startDate;
    private boolean active;
    private boolean hasExpired; // NOVO: Za trajno pamćenje isteka

    public SpecialMission(int id, String title, long startDate, boolean active) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.active = active;
        this.hasExpired = false; // Po default-u nije istekla
    }

    // Getteri
    public int getId() { return id; }
    public String getTitle() { return title; }
    public long getStartDate() { return startDate; }
    public boolean isActive() { return active; }
    public boolean hasExpired() { return hasExpired; }

    // Setteri
    public void setActive(boolean active) { this.active = active; }
    public void setHasExpired(boolean hasExpired) { this.hasExpired = hasExpired; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    // Metoda je ista, ali će se koristiti za postavljanje 'hasExpired' polja
    public boolean isExpiredByTime(long currentDate) {
        if (startDate == 0) return false;
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(startDate);
        startCal.add(Calendar.DAY_OF_YEAR, 14); // Misija traje 14 dana
        Calendar currentCal = Calendar.getInstance();
        currentCal.setTimeInMillis(currentDate);
        return currentCal.after(startCal);
    }
}