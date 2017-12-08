/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.javabegin.training.javafx;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.javabegin.training.javafx.fxml.MainView;
import ru.javabegin.training.javafx.service.AddressBookService;
import ru.javabegin.training.javafx.utils.LocaleManager;

import java.util.Locale;


@SpringBootApplication
public class Main extends JavaFxSpringIntegrator{

	@Autowired
	private MainView mainView;

	@Autowired
	private AddressBookService addressBookService;

	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		super.start(primaryStage);

		loadMainFXML(LocaleManager.RU_LOCALE, primaryStage);

	}

	@Override
	public void init() throws Exception {
		super.init();
	}

	// загружает дерево компонентов и возвращает в виде VBox (корневой элемент в FXML)
	private void loadMainFXML(Locale locale, Stage primaryStage) {
		this.primaryStage = primaryStage;
		primaryStage.setScene(new Scene(mainView.getView(locale)));
		primaryStage.setMinHeight(700);
		primaryStage.setMinWidth(600);
		primaryStage.centerOnScreen();
		primaryStage.setTitle(mainView.getResourceBundle().getString("address_book"));
		primaryStage.show();


	}


	public static void main(String[] args) {
		// старт приложения
		launchSpringJavaFXApp(Main.class, args);
	}


	@Override
	public void stop() throws Exception {
		System.exit(0);
	}

}
