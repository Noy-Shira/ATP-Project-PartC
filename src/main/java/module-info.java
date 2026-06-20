module org.example.atpprojectpartc {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.graphics;
    requires ATPProjectJAR;

    opens org.example.atpprojectpartc to javafx.fxml;
    exports org.example.atpprojectpartc;

    opens org.example.atpprojectpartc.View to javafx.fxml, javafx.graphics;
    exports org.example.atpprojectpartc.View to javafx.fxml, javafx.graphics;

    opens org.example.atpprojectpartc.ViewModel to javafx.fxml;
    exports org.example.atpprojectpartc.ViewModel;

    opens org.example.atpprojectpartc.Model to javafx.fxml;
    exports org.example.atpprojectpartc.Model;
}