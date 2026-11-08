package org.example;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import org.testfx.framework.junit5.ApplicationTest;
import utils.DBHelper;
import utils.Texts;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.util.WaitForAsyncUtils.waitFor;

public abstract class AppTestBase extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Texts.apply(Locale.forLanguageTag("ru-RU"));
        DBHelper.initConnection("appuser", "pass");
        Parent root = Texts.loader("/mainView.fxml").load();
        stage.setScene(new Scene(root, 1200, 760));
        stage.show();
    }

    protected void assertAlertContains(String part) throws Exception {
        waitFor(3, TimeUnit.SECONDS, () -> lookup(".alert").tryQuery().isPresent());
        DialogPane pane = lookup(".dialog-pane").query();
        String text = String.valueOf(pane.getHeaderText()) + " " + String.valueOf(pane.getContentText());
        assertThat(text).contains(part);
        clickOn(lookup(".alert .button").queryButton());
    }

    protected void closeDialog(String nodeId) {
        interact(() -> ((Stage) lookup(nodeId).query().getScene().getWindow()).close());
    }
}
