module com.buchta.pomodoro {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens com.buchta.pomodoro to javafx.fxml;
    exports com.buchta.pomodoro;
}