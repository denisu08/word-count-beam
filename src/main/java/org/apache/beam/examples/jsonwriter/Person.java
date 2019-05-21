package org.apache.beam.examples.jsonwriter;

import java.io.Serializable;

public class Person implements Serializable {
    protected int age;
    protected String name;
    protected String id;

    public Person() {
    }

    public Person(String id, String name, int age) {
        this.age = age;
        this.name = name;
        this.id = id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public static Person of(String id, String name, int age) {
        Person person = new Person();
        person.age = age;
        person.name = name;
        person.id = id;
        return person;
    }
}
