package se.lu.ics.models;

public class Role {
    private String id;
    private String title;
    private String description;
    private String department;
    private int ongoingRecruitments;

    public Role(String id, String title, String description, String department) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.department = department;
        this.ongoingRecruitments = 0;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getOngoingRecruitments() {
        return ongoingRecruitments;
    }

    public void incrementOngoingRecruitments() {
        this.ongoingRecruitments++;
    }

    public void decrementOngoingRecruitments() {
        if (this.ongoingRecruitments > 0) {
            this.ongoingRecruitments--;
        }
    }
} 