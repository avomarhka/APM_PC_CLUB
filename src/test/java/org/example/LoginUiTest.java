package org.example;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import utils.DBHelper;
import utils.Texts;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public class LoginUiTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Texts.apply(Locale.forLanguageTag("ru-RU"));
        Parent root = Texts.loader("/loginView.fxml").load();
        stage.setScene(new Scene(root, 520, 360));
        stage.show();
    }

    @Test
    @DisplayName("Пустой логин не открывает приложение")
    void testEmptyLogin() {
        clickOn("#buttonEnter");
        assertThat(lookup("#loginTF").tryQuery()).isPresent();
        assertThat(lookup("#buttonEnter").queryButton().getText()).isEqualTo("Войти");
    }

    @Test
    @DisplayName("Неверный пароль оставляет окно входа")
    void testWrongPassword() {
        clickOn("#loginTF").write("appuser");
        clickOn("#passswordTF").write("wrong-password");
        clickOn("#buttonEnter");
        sleep(500, TimeUnit.MILLISECONDS);
        assertThat(lookup("#loginTF").tryQuery()).isPresent();
        DBHelper.initConnection("appuser", "pass");
    }

    @Test
    @DisplayName("Английский язык на форме входа")
    void testLoginLanguage() throws Exception {
        ChoiceBox<String> language = lookup("#cbLang").query();
        interact(() -> language.setValue("English"));
        waitFor(3, TimeUnit.SECONDS, () -> "Enter".equals(lookup("#buttonEnter").queryButton().getText()));
        assertThat(lookup("#lblAutho").queryLabeled().getText()).isEqualTo("Authorization");
    }

    @Test
    @DisplayName("Вход с верным логином закрывает окно")
    void testSuccessfulLogin() throws Exception {
        clickOn("#loginTF").write("appuser");
        clickOn("#passswordTF").write("pass");
        clickOn("#buttonEnter");
        waitFor(3, TimeUnit.SECONDS, () -> lookup("#loginTF").tryQuery().isEmpty());
    }
}
