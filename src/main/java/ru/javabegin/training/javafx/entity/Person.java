package ru.javabegin.training.javafx.entity;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "Person")
@EqualsAndHashCode(of = "id")
@ToString(of = "fio")
public class Person {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private SimpleStringProperty fio = new SimpleStringProperty("");
    private SimpleStringProperty phone = new SimpleStringProperty("");


    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id.get();
    }

    @Column(name = "fio")
    public String getFio() {
        return fio.get();
    }

    @Column(name = "phone")
    public String getPhone() {
        return phone.get();
    }

    @Column (name="photo")
    @Getter
    @Setter
    private byte[] photo;

    @Column(name = "address")
    @Getter
    @Setter
    private String address;


    public void setFio(String fio) {
        this.fio.set(fio);
    }


    public void setPhone(String phone) {
        this.phone.set(phone);
    }


    public void setId(int id) {
        this.id.set(id);
    }


}

