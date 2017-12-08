package ru.javabegin.training.javafx.controllers;

import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import ru.javabegin.training.javafx.entity.Person;
import ru.javabegin.training.javafx.fxml.EditView;
import ru.javabegin.training.javafx.fxml.MainView;
import ru.javabegin.training.javafx.objects.Lang;
import ru.javabegin.training.javafx.service.AddressBook;
import ru.javabegin.training.javafx.utils.DialogManager;
import ru.javabegin.training.javafx.utils.LocaleManager;

import java.lang.reflect.Method;
import java.util.Observable;
import java.util.ResourceBundle;

@Component
public class MainController extends Observable{

    private static final int PAGE_SIZE = 10;
    public static final int MAX_PAGE_SHOW = 10;

    private Page page;// текущие постраничные данные

    @Autowired
    private AddressBook addressBook;


    @Autowired
    private MainView mainView;

    @Autowired
    private EditView editView;

    private ObservableList<Person> personList;


    @FXML
    private Button btnAdd;

    @FXML
    private Button btnEdit;

    @FXML
    private Button btnDelete;

    @FXML
    private CustomTextField txtSearch;

    @FXML
    private Button btnSearch;

    @FXML
    private TableView tableAddressBook;

    @FXML
    private TableColumn<Person, String> columnFIO;

    @FXML
    private TableColumn<Person, String> columnPhone;

    @FXML
    private Label labelCount;

    @FXML
    private Pagination pagination;

    @FXML
    private ComboBox comboLocales;


    private Parent fxmlEdit;

    private FXMLLoader fxmlLoader = new FXMLLoader();

    @Autowired
    private EditController editController;

    private Stage editDialogStage;

    private ResourceBundle resourceBundle;



    private static final String RU_CODE="ru";
    private static final String EN_CODE="en";


    @FXML
    public void initialize() {
        pagination.setMaxPageIndicatorCount(MAX_PAGE_SHOW);
        this.resourceBundle = mainView.getResourceBundle();
        columnFIO.setCellValueFactory(new PropertyValueFactory<Person, String>("fio"));
        columnPhone.setCellValueFactory(new PropertyValueFactory<Person, String>("phone"));
        setupClearButtonField(txtSearch);
        fillData();
        initListeners();
    }

    private void setupClearButtonField(CustomTextField customTextField) {
        try {
            Method m = TextFields.class.getDeclaredMethod("setupClearButtonField", TextField.class, ObjectProperty.class);
            m.setAccessible(true);
            m.invoke(null, customTextField, customTextField.rightProperty());
//            customTextField.textProperty().addListener(new ChangeListener<String>() {
//                @Override
//                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//                    if (txtSearch.getText().trim().length() == 0) {// если полностью очистили текст - вернуть все записи
//                        addressBookImpl.getPersonList().clear();
//                    }
//                }
//            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }




    private void fillData() {
        fillLangComboBox();
        fillTable();
    }


    private void fillPagination(Page page) {
        if (page.getTotalPages()<=1){
            pagination.setDisable(true);
        }else {
            pagination.setDisable(false);
        }

        pagination.setPageCount(page.getTotalPages());

        personList = FXCollections.observableArrayList(page.getContent());
        tableAddressBook.setItems(personList);
    }



    private void fillLangComboBox() {


        Lang langRU = new Lang(0, RU_CODE, resourceBundle.getString("ru"), LocaleManager.RU_LOCALE);
        Lang langEN = new Lang(1, EN_CODE, resourceBundle.getString("en"), LocaleManager.EN_LOCALE);

        comboLocales.getItems().add(langRU);
        comboLocales.getItems().add(langEN);


        if (LocaleManager.getCurrentLang() == null){
            comboLocales.getSelectionModel().select(0);
            LocaleManager.setCurrentLang(langRU);
        }else{
            comboLocales.getSelectionModel().select(LocaleManager.getCurrentLang().getIndex());
        }
    }

    private void initListeners() {



        // слушает двойное нажатие для редактирования записи
        tableAddressBook.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
//                    editDialogController.setPerson((Person) tableAddressBook.getSelectionModel().getSelectedItem());
//                    showDialog();
                    btnEdit.fire();
                }
            }
        });


        // слушает переключение языка
        comboLocales.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ov, Object t, Object t1) {

                Lang selectedLang = (Lang)ov.getValue();

                LocaleManager.setCurrentLang(selectedLang);

                // уведомить всех слушателей, что произошла смена языка
                setChanged();
                notifyObservers(selectedLang);

            }
        });

        pagination.currentPageIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                fillTable(newValue.intValue());
            }
        });


    }


    private void updateCountLabel(long count) {
        labelCount.setText(resourceBundle.getString("count") + ": " + count);
    }


    // выполнить действие в зависимости от нажатой кнопки
    public void actionButtonPressed(ActionEvent actionEvent) {

        Object source = actionEvent.getSource();

        // если нажата не кнопка - выходим из метода
        if (!(source instanceof Button)) {
            return;
        }

        // получтиь выбранного person (если редактирование или удаление)
        Person selectedPerson = (Person) tableAddressBook.getSelectionModel().getSelectedItem();

        boolean dataChanged = false;

        // определить нажатую кнопку
        Button clickedButton = (Button) source;

        switch (clickedButton.getId()) {
            case "btnAdd":
                editController.setPerson(new Person());
                showDialog();


                if (editController.isSaveClicked()) {
                    addressBook.add(editController.getPerson());
                    dataChanged = true;
                }


                break;

            case "btnEdit":
                if (!personIsSelected(selectedPerson)) {
                    return;
                }
                editController.setPerson(selectedPerson);
                showDialog();

                if (editController.isSaveClicked()) {
                    // коллекция в addressBookImpl и так обновляется, т.к. мы ее редактируем в диалоговом окне и сохраняем при нажатии на ОК
                    addressBook.update(selectedPerson);
                    dataChanged = true;
                }

                break;

            case "btnDelete":
                if (!personIsSelected(selectedPerson) || !(confirmDelete())) {
                    return;
                }

                dataChanged = true;
                addressBook.delete(selectedPerson);
                break;
        }


        // обновить список, если запись была изменена
        if (dataChanged) {
            actionSearch(actionEvent);
        }

    }


    private boolean confirmDelete() {
        if (DialogManager.showConfirmDialog(resourceBundle.getString("confirm"), resourceBundle.getString("confirm_delete")).get() == ButtonType.OK){
            return true;
        } else {
            return false;
        }

    }

    private boolean personIsSelected(Person selectedPerson) {
        if(selectedPerson == null){
            DialogManager.showInfoDialog(resourceBundle.getString("error"), resourceBundle.getString("select_person"));
            return false;
        }
        return true;
    }


    // диалоговое окно при создании/редактировании
    private void showDialog() {

        if (editDialogStage == null) {
            editDialogStage = new Stage();

            editDialogStage.setMinHeight(150);
            editDialogStage.setMinWidth(300);
            editDialogStage.setResizable(false);
            editDialogStage.initModality(Modality.WINDOW_MODAL);
            editDialogStage.initOwner(comboLocales.getParent().getScene().getWindow());
            editDialogStage.centerOnScreen();
        }

        editDialogStage.setScene(new Scene(editView.getView(LocaleManager.getCurrentLang().getLocale())));

        editDialogStage.setTitle(resourceBundle.getString("edit"));

        editDialogStage.showAndWait(); // для ожидания закрытия модального окна

    }


    public void actionSearch(ActionEvent actionEvent) {
        fillTable();
    }

    // для показа данных с первой страницы
    private void fillTable() {
        if (txtSearch.getText().trim().length() == 0) {
            page = addressBook.findAll(0, PAGE_SIZE);
        }else {
            page = addressBook.findAll(0, PAGE_SIZE, txtSearch.getText());
        }
        fillPagination(page);
        pagination.setCurrentPageIndex(0);
        updateCountLabel(page.getTotalElements());

    }

    // для показа данных с любой страницы
    private void fillTable(int pageNumber) {
        if (txtSearch.getText().trim().length() == 0) {
            page = addressBook.findAll(pageNumber, PAGE_SIZE);
        }else {
            page = addressBook.findAll(pageNumber, PAGE_SIZE, txtSearch.getText());
        }
        fillPagination(page);
        updateCountLabel(page.getTotalElements());

    }
}
