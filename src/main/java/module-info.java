module org.example.doublependulumproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.doublependulumproject to javafx.fxml;
    exports org.example.doublependulumproject;
}