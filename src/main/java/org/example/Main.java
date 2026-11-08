package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.DBHelper;

import java.util.Locale;
import java.util.ResourceBundle;


public class Main extends Application {
    private static Stage primaryStage;
    public static String currentUserName = "Сотрудник";

    public static Stage getPrimaryStage () {
        return primaryStage;
    }
    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(new Locale("ru", "RU"));

        ResourceBundle bundle = ResourceBundle.getBundle("text", Locale.getDefault());


        FXMLLoader loginLoader = new FXMLLoader(Main.class.getResource("/loginView.fxml"), bundle);
        Pane loginRoot = loginLoader.load();

        Stage loginStage = new Stage();
        stage.setTitle("АРМ Администратора компьютерного клуба");
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setScene(new Scene(loginRoot));

        loginStage.showAndWait();

        try {
            if (DBHelper.getConnection() == null) {
                System.out.println("Авторизация не пройдена. Выход.");
                return; // или System.exit(0)
            }
        } catch (Exception e) {
            System.out.println("Авторизация не пройдена. Выход.");
            return;
        }

        FXMLLoader mainLoader = new FXMLLoader(Main.class.getResource("/mainView.fxml"), bundle);
        Pane mainRoot = mainLoader.load();

        primaryStage = stage;

        stage.setTitle("АРМ Администратора компьютерного клуба");
        stage.setScene(new Scene(mainRoot));
        stage.show();
    }

    public void stop() {
        DBHelper.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}