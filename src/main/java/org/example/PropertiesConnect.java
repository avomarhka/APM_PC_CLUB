package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesConnect {
    private final Properties property = new Properties();

    public PropertiesConnect() {
        try (InputStream is = getClass().getResourceAsStream("/statements.properties")) {
            if (is == null) {
                throw new RuntimeException("Файл statements.properties не найден в ресурсах!");
            }
            property.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла statements.properties", e);
        }
    }

    public String getProperty(String key) {
        return property.getProperty(key);
    }
}