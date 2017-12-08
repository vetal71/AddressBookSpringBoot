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
package ru.javabegin.training.javafx.fxml;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import ru.javabegin.training.javafx.objects.Lang;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static java.util.ResourceBundle.getBundle;

// класс для "соединения" FXML контроллеров с контекстом Spring
public abstract class SpringFxmlView implements ApplicationContextAware, Observer {

	private static final String BUNDLES_FOLDER = "ru.javabegin.training.javafx.bundles.Locale";


	protected ObjectProperty<Object> presenterProperty;
	protected FXMLLoader fxmlLoader;
	protected ResourceBundle bundle;
	protected URL resource;
	protected Pane parent;
	protected Pane rootPane;

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

		if (this.applicationContext != null) {
			return;
		}

		this.applicationContext = applicationContext;
	}

	public SpringFxmlView() {

		this.presenterProperty = new SimpleObjectProperty<>();
		this.resource = getClass().getResource(getFxmlName());
		this.bundle = getResourceBundle(getBundleName());
	}

	public SpringFxmlView(URL resource){
		this.presenterProperty = new SimpleObjectProperty<>();
		this.resource = resource;
	}

	private Object createControllerForType(Class<?> type) {
		return this.applicationContext.getBean(type);
	}

		FXMLLoader loadSynchronously(URL resource, ResourceBundle bundle) throws IllegalStateException {

		FXMLLoader loader = new FXMLLoader(resource, bundle);
		loader.setControllerFactory(this::createControllerForType);

		try {
			loader.load();
		} catch (IOException ex) {
			throw new IllegalStateException("Cannot load " + getConventionalName(), ex);
		}

		return loader;
	}


	void ensureFxmlLoaderInitialized() {

//		if (this.fxmlLoader != null) {
//			return;
//		}

		this.fxmlLoader = loadSynchronously(resource, bundle);
		this.presenterProperty.set(this.fxmlLoader.getController());

		if (this.fxmlLoader.getController() instanceof Observable){
			((Observable)this.fxmlLoader.getController()).addObserver(this);
		}

	}




	/**
	 * Initializes the view by loading the FXML (if not happened yet) and returns the top Node (parent) specified in the
	 * FXML file.
	 *
	 * @return
	 */
	public Parent getView() {

		bundle = ResourceBundle.getBundle(BUNDLES_FOLDER);

		ensureFxmlLoaderInitialized();

		parent = fxmlLoader.getRoot();
		addCSSIfAvailable(parent);
		return parent;
	}


	public Parent getView(Locale locale) {

		bundle = ResourceBundle.getBundle(BUNDLES_FOLDER, locale);

		ensureFxmlLoaderInitialized();

		parent = fxmlLoader.getRoot();
		addCSSIfAvailable(parent);
		return parent;
	}


	/**
	 * Initializes the view synchronously and invokes the consumer with the created parent Node within the FX UI thread.
	 *
	 * @param consumer - an object interested in received the {@link Parent} as callback
	 */
	public void getView(Consumer<Parent> consumer) {
		CompletableFuture.supplyAsync(this::getView, Platform::runLater).thenAccept(consumer);
	}

	/**
	 * Scene Builder creates for each FXML document a root container. This method omits the root container (e.g.
	 * {@link AnchorPane}) and gives you the access to its first child.
	 *
	 * @return the first child of the {@link AnchorPane}
	 */
	public Node getViewWithoutRootContainer() {

		ObservableList<Node> children = getView().getChildrenUnmodifiable();
		if (children.isEmpty()) {
			return null;
		}

		return children.listIterator().next();
	}

	void addCSSIfAvailable(Parent parent) {

		URL uri = getClass().getResource(getStyleSheetName());
		if (uri == null) {
			return;
		}

		String uriToCss = uri.toExternalForm();
		parent.getStylesheets().add(uriToCss);
	}

	String getStyleSheetName() {
		return getConventionalName(".css");
	}

	/**
	 * In case the view was not initialized yet, the conventional fxml (airhacks.fxml for the AirhacksView and
	 * AirhacksPresenter) are loaded and the specified presenter / controller is going to be constructed and returned.
	 *
	 * @return the corresponding controller / presenter (usually for a AirhacksView the AirhacksPresenter)
	 */
	public Object getPresenter() {

		ensureFxmlLoaderInitialized();

		return this.presenterProperty.get();
	}

	/**
	 * Does not initialize the view. Only registers the Consumer and waits until the the view is going to be created / the
	 * method FXMLView#getView or FXMLView#getViewAsync invoked.
	 *
	 * @param presenterConsumer listener for the presenter construction
	 */
	public void getPresenter(Consumer<Object> presenterConsumer) {

		this.presenterProperty.addListener((ObservableValue<? extends Object> o, Object oldValue, Object newValue) -> {
			presenterConsumer.accept(newValue);
		});
	}

	/**
	 * @param ending the suffix to append
	 * @return the conventional name with stripped ending
	 */
	protected String getConventionalName(String ending) {
		return getConventionalName() + ending;
	}

	/**
	 * @return the name of the view without the "View" prefix in lowerCase. For AirhacksView just airhacks is going to be
	 *         returned.
	 */
	protected String getConventionalName() {
		return stripEnding(getClass().getSimpleName().toLowerCase());
	}

	String getBundleName() {
		return getClass().getPackage().getName() + "." + getConventionalName();
	}

	static String stripEnding(String clazz) {

		if (!clazz.endsWith("view")) {
			return clazz;
		}

		return clazz.substring(0, clazz.lastIndexOf("view"));
	}

	/**
	 * @return the name of the fxml file derived from the FXML view. e.g. The name for the AirhacksView is going to be
	 *         airhacks.fxml.
	 */
	final String getFxmlName() {
		return getConventionalName(".fxml");
	}

	private ResourceBundle getResourceBundle(String name) {
		try {
			return getBundle(name);
		} catch (MissingResourceException ex) {
			return null;
		}
	}

	/**
	 * @return an existing resource bundle, or null
	 */
	public ResourceBundle getResourceBundle() {
		return this.bundle;
	}


	@Override
	public void update(Observable o, Object arg) {
		Lang lang = (Lang) arg;

		if (rootPane == null)
		{
			rootPane = parent;// один раз в начале сохраняем ссылку на корновой контейнер
		}

		parent = (Pane)getView(lang.getLocale()); // получить новое дерево компонетов с нужной локалью

		rootPane.getChildren().setAll(parent.getChildren());// заменить старые дочерник компонента на новые - с другой локалью


	}


}
