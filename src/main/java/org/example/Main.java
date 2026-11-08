package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.DBHelper;
import utils.Texts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;


public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Stage primaryStage;
    public static String currentUserName = "Сотрудник";

    public static Stage getPrimaryStage () {
        return primaryStage;
    }
    @Override
    public void start(Stage stage) throws Exception {
        logger.info("Приложение запущено");
        Texts.apply(new Locale("ru", "RU"));

        FXMLLoader loginLoader = Texts.loader("/loginView.fxml");
        Pane loginRoot = loginLoader.load();

        Stage loginStage = new Stage();
        stage.setTitle(Texts.get("app.title"));
        loginStage.setTitle(Texts.get("text.authorization"));
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setScene(new Scene(loginRoot));

        loginStage.showAndWait();

        try {
            if (DBHelper.getConnection() == null) {
                logger.warn("Авторизация не пройдена. Выход.");
                return; // или System.exit(0)
            }
        } catch (Exception e) {
            logger.warn("Авторизация не пройдена. Выход.");
            return;
        }

        FXMLLoader mainLoader = Texts.loader("/mainView.fxml");
        Pane mainRoot = mainLoader.load();

        primaryStage = stage;

        stage.setTitle(Texts.get("app.title"));
        stage.setScene(new Scene(mainRoot));
        stage.show();
        logger.info("Главное окно открыто, пользователь {}", currentUserName);
    }

    public void stop() {
        logger.info("Приложение остановлено");
        DBHelper.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}