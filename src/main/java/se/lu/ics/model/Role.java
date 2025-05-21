package se.lu.ics.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a job role or position in the company.
 */
public class Role {
    private String id;
    private String title;
    private String description;
    private String department;

    /**
     * Default constructor
     */
    public Role() {
        this.id = "";
        this.title = "";
        this.description = "";
        this.department = "";
    }

    /**
     * Constructor with title and department
     * @param title The job title
     * @param department The department
     */
    public Role(String title, String department) {
        this.id = "";
        this.title = title;
        this.department = department;
        this.description = "";
    }

    /**
     * Constructor with all fields
     * @param id The role ID
     * @param title The job title
     * @param description The job description
     * @param department The department
     */
    public Role(String id, String title, String description, String department) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.department = department;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public String toString() {
        return title + " (" + department + ")";
    }
}
