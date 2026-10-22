package model;

/** The person who is taking the interview. Extends Person. */
public class Candidate extends Person {
    private final Role role;
    private final Difficulty difficulty;

    public Candidate(String name, Role role, Difficulty difficulty) {
        super(name);
        this.role = role;
        this.difficulty = difficulty;
    }

    public Role getRole() { return role; }
    public Difficulty getDifficulty() { return difficulty; }

    @Override
    public String getDescription() {
        return "Candidate " + getName() + " applying for " + role.getDisplayName()
                + " (" + difficulty.getDisplayName() + ")";
    }
}
