module se.lu.ics {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    
    requires java.sql;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;

    opens se.lu.ics to javafx.fxml;
    opens se.lu.ics.controller to javafx.fxml;
    opens se.lu.ics.model to javafx.base;
    
    exports se.lu.ics;
    exports se.lu.ics.controller;
    exports se.lu.ics.model;
    exports se.lu.ics.service;
} 