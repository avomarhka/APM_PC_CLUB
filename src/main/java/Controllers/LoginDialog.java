package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Locale;
import java.util.ResourceBundle;

import utils.DBHelper;
import org.example.Main;

public class LoginDialog {
    @FXML
    public TextField loginTF;
    public TextField passswordTF;
    public Button buttonEnter;
    public ChoiceBox<String> cbLang;
    public Label lblLanguage;
    public Label lblPassword;
    public Label lblLogin;
    public Label lblAutho;

    private ResourceBundle bundle;


    @FXML
    private void initialize() {
        cbLang.getItems().addAll("Русский", "English");
        cbLang.setValue("Русский");

        cbLang.setOnAction(event -> changeLanguage());

        buttonEnter.setOnAction(event -> handleLogin());
    }

    private void changeLanguage() {
        String selected = cbLang.getValue();

        // Определяем локаль
        Locale locale;
        if ("English".equals(selected)) {
            locale = new Locale("en", "EN");
        } else {
            locale = new Locale("ru", "RU");
        }

        bundle = ResourceBundle.getBundle("text", locale);

        updateTexts();
    }

    private void updateTexts() {
        lblAutho.setText(bundle.getString("text.authorization"));
        lblLanguage.setText(bundle.getString("text.languages"));
        lblLogin.setText(bundle.getString("text.login"));
        lblPassword.setText(bundle.getString("text.password"));

        buttonEnter.setText(bundle.getString("button.enter"));

        loginTF.setPromptText(bundle.getString("login.enter"));
        passswordTF.setPromptText(bundle.getString("password.enter"));
    }

    private void handleLogin() {
        String username = loginTF.getText();
        String password = passswordTF.getText();

        if (username.isEmpty() || password.isEmpty()) {
            //showAlert(Alert.AlertType.WARNING, "Ошибка", "Заполните логин и пароль!");
            System.out.println("Заполните логин и пароль!");
            return;
        }
        boolean success = DBHelper.initConnection(username, password);


        if (success) {
            loadUserInfo(username);
            Stage stage = (Stage) buttonEnter.getScene().getWindow();
            stage.close();
        } else {
            //showAlert(Alert.AlertType.ERROR, "Ошибка доступа", "Неверный логин или пароль!");
        }
    }

    private void loadUserInfo(String dbLogin) {
        String sql = "SELECT surname, \"name\" FROM workers WHERE login = ?";

        try (PreparedStatement pst = DBHelper.getConnection().prepareStatement(sql)) {
            pst.setString(1, dbLogin);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String surname = rs.getString("surname");
                String name = rs.getString("name");

                    // Сохраняем ФИО в статическую переменную, чтобы главное окно её увидело
                Main.currentUserName = surname + " " + name;
            } else {
                    // Если пользователя с таким логином нет в таблице workers
                    Main.currentUserName = dbLogin;
            }
        } catch (Exception e) {
                e.printStackTrace();
                Main.currentUserName = dbLogin;
        }

    }

}
