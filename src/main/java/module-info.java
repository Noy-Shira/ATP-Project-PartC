module org.example.atpprojectpartc {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.atpprojectpartc to javafx.fxml;
    exports org.example.atpprojectpartc;
}