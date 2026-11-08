package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DateFields {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static boolean alertOpen;

    private DateFields() {
    }

    public static void setup(DatePicker picker) {
        picker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date == null ? "" : date.format(FORMAT);
            }

            @Override
            public LocalDate fromString(String text) {
                if (text == null || text.isBlank()) {
                    return null;
                }
                try {
                    return LocalDate.parse(text.trim(), FORMAT);
                } catch (DateTimeParseException e) {
                    LocalDate current = picker.getValue();
                    if (current != null) {
                        picker.getEditor().setText(current.format(FORMAT));
                    } else {
                        picker.getEditor().clear();
                    }
                    showInvalidDate();
                    return current;
                }
            }
        });
        picker.setPromptText(Texts.get("prompt.date"));
        String css = DateFields.class.getResource("/date-picker.css").toExternalForm();
        if (!picker.getStylesheets().contains(css)) {
            picker.getStylesheets().add(css);
        }
    }

    private static void showInvalidDate() {
        if (alertOpen) {
            return;
        }
        alertOpen = true;
        try {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText(Texts.get("alert.badDateHeader"));
            alert.setContentText(Texts.get("alert.badDateBody"));
            alert.showAndWait();
        } finally {
            alertOpen = false;
        }
    }
}
