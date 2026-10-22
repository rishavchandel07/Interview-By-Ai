package model;

/** Abstract base class for any person in the system (ABSTRACTION + INHERITANCE). */
public abstract class Person {
    private String name;

    protected Person(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    /** Every kind of person describes itself differently (POLYMORPHISM). */
    public abstract String getDescription();
}
