package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import utils.Texts;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class TextsTest {

    @AfterEach
    void restoreRussian() {
        Texts.apply(Locale.forLanguageTag("ru-RU"));
    }

    @Test
    @DisplayName("Подписи на русском и английском")
    void russianAndEnglishBundles() {
        Texts.apply(Locale.forLanguageTag("ru-RU"));
        assertThat(Texts.get("tab.clients")).isEqualTo("Клиенты");
        assertThat(Texts.get("tab.orders")).isEqualTo("Заказы");
        assertThat(Texts.get("tab.pc")).isEqualTo("Компьютеры");
        assertThat(Texts.get("alert.fillName")).contains("фамилию");
        assertThat(Texts.get("alert.phoneDigits")).contains("цифры");

        Texts.apply(Locale.forLanguageTag("en-EN"));
        assertThat(Texts.get("tab.clients")).isEqualTo("Clients");
        assertThat(Texts.get("tab.orders")).isEqualTo("Orders");
        assertThat(Texts.get("tab.pc")).isEqualTo("Computers");
        assertThat(Texts.get("button.enter")).isEqualTo("Enter");
        assertThat(Texts.get("status.connected")).isEqualTo("Connected");
    }
}
