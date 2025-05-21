module se.lu.ics {
    /* JavaFX */
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    /* JDBC */
    requires java.sql;
    requires org.xerial.sqlitejdbc;   // modulnamnet är org.xerial.sqlitejdbc

    /* (valfritt) om du lägger till slf4j-simple behöver du inte deklarera det här */

    /* Öppna paket för FXML / JavaFX reflection */
    opens se.lu.ics           to javafx.fxml;
    opens se.lu.ics.controller to javafx.fxml;
    opens se.lu.ics.model      to javafx.base;

    /* Exportera publika paket */
    exports se.lu.ics;
    exports se.lu.ics.controller;
    exports se.lu.ics.model;
    exports se.lu.ics.service;
    exports se.lu.ics.exception;
    exports se.lu.ics.dao;
}
