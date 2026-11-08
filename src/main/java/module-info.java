module APM  {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.sql;
    requires javafx.base;
    requires static lombok;


    opens org.example to javafx.fxml;
    exports org.example;
    exports Controllers;
    opens Controllers to javafx.fxml;
    exports utils;
    opens utils to javafx.fxml;
    exports Dao;
    opens Dao to javafx.fxml;


}
