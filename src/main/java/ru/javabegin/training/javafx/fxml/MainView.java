package ru.javabegin.training.javafx.fxml;

import org.springframework.stereotype.Component;

@Component
public class MainView extends SpringFxmlView {

    private static final String FXML_MAIN = "ru/javabegin/training/javafx/fxml/main.fxml";

    public MainView() {
        super(MainView.class.getClassLoader().getResource(FXML_MAIN));
    }
}
