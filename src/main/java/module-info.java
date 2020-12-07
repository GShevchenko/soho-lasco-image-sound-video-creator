module solar.download.images {
    exports org.example;
    opens org.example;
    requires javafx.controls;
    requires java.desktop;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
}