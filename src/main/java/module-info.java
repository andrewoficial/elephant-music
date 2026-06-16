module ru.kantser.elephantmusic {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires org.slf4j;
    requires com.google.guice;
    requires java.xml;
    requires com.fasterxml.jackson.annotation;
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires javafx.web;
    requires java.desktop;
    requires jaudiotagger;
    requires com.fasterxml.jackson.datatype.jsr310; // Добавлена поддержка JSR-310 (java.time)



    // Экспорт пакетов, которые используются Guice
    exports ru.kantser.elephantmusic.controller to com.google.guice;
    exports ru.kantser.elephantmusic.service to com.google.guice;
    exports ru.kantser.elephantmusic.service.settings to com.google.guice;
    exports ru.kantser.elephantmusic.service.lastfm to com.google.guice;
    exports ru.kantser.elephantmusic.service.update to com.google.guice;
    exports ru.kantser.elephantmusic.model to com.fasterxml.jackson.databind;
    exports ru.kantser.elephantmusic.view.dialog to com.google.guice;
    exports ru.kantser.elephantmusic.view.update to com.google.guice;

    // Пакеты для рефлексии (нужно для FXML и Guice)
    opens ru.kantser.elephantmusic.controller to javafx.fxml, com.google.guice;
    opens ru.kantser.elephantmusic.service to com.google.guice;
    opens ru.kantser.elephantmusic.service.settings to com.google.guice;
    opens ru.kantser.elephantmusic.service.lastfm to com.google.guice;
    opens ru.kantser.elephantmusic.model to com.fasterxml.jackson.databind, com.fasterxml.jackson.datatype.jsr310; // Добавлен модуль jsr310
    opens ru.kantser.elephantmusic.view.update to com.google.guice;
    opens ru.kantser.elephantmusic.service.update to com.google.guice;

    // Если используете FXML
    //opens ru.kantser.elephantmusic.view to javafx.fxml;

    //Какие-то сложности, открыл для рефлексии для всех вообще

    exports ru.kantser.elephantmusic;
    // Exports the package containing MyApplication and Launcher
}