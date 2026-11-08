package utils;

import javafx.fxml.FXMLLoader;
import org.example.Main;

import java.util.Locale;
import java.util.ResourceBundle;

public final class Texts {
    private static Locale locale = new Locale("ru", "RU");
    private static ResourceBundle bundle = ResourceBundle.getBundle("text", locale);

    private Texts() {
    }

    public static void apply(Locale newLocale) {
        locale = newLocale;
        Locale.setDefault(newLocale);
        bundle = ResourceBundle.getBundle("text", locale);
    }

    public static Locale locale() {
        return locale;
    }

    public static ResourceBundle bundle() {
        return bundle;
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static FXMLLoader loader(String resource) {
        return new FXMLLoader(Main.class.getResource(resource), bundle);
    }
}
