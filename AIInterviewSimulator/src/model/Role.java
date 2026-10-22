package model;

/** The job roles a candidate can practise for. */
public enum Role {
    JAVA_DEVELOPER("Java Developer", "Core Java & OOP"),
    WEB_DEVELOPER("Web Developer", "HTTP, REST, HTML/CSS/JS"),
    SOFTWARE_ENGINEER("Software Engineer", "DSA & engineering basics"),
    DATA_ANALYST("Data Analyst", "SQL & statistics"),
    GENERAL_HR("General HR", "Behavioural questions");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    @Override
    public String toString() { return displayName; }
}
