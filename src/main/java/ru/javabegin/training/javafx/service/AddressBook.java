package ru.javabegin.training.javafx.service;

import javafx.collections.ObservableList;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.javabegin.training.javafx.entity.Person;


public interface AddressBook {

    void add(Person person);

    void update(Person person);

    void delete(Person person);

    ObservableList<Person> findAll();

    ObservableList<Person> find(String text);

    Page findAll(int from, int count);

    Page findAll(int from, int count, String... text);


}
