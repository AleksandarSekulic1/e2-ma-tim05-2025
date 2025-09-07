// Contribution.java
package ftn.ma.myapplication.data.model; // Prilagodi paketu ako je potrebno

public class Contribution {
    private String memberName;
    private int totalDamage;

    public Contribution(String memberName, int totalDamage) {
        this.memberName = memberName;
        this.totalDamage = totalDamage;
    }

    public String getMemberName() {
        return memberName;
    }

    public int getTotalDamage() {
        return totalDamage;
    }
}