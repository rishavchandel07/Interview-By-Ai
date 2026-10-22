package model;

/** Interview difficulty. Each level has its own time limit and "ideal" answer length. */
public enum Difficulty {
    BEGINNER("Beginner", 60, 25),
    INTERMEDIATE("Intermediate", 90, 40),
    ADVANCED("Advanced", 120, 55);

    private final String displayName;
    private final int timeLimitSeconds;
    private final int idealWords;

    Difficulty(String displayName, int timeLimitSeconds, int idealWords) {
        this.displayName = displayName;
        this.timeLimitSeconds = timeLimitSeconds;
        this.idealWords = idealWords;
    }

    public String getDisplayName() { return displayName; }
    public int getTimeLimitSeconds() { return timeLimitSeconds; }
    public int getIdealWords() { return idealWords; }

    /** Next harder level (Advanced stays Advanced). */
    public Difficulty harder() {
        return this == BEGINNER ? INTERMEDIATE : ADVANCED;
    }

    /** Next easier level (Beginner stays Beginner). */
    public Difficulty easier() {
        return this == ADVANCED ? INTERMEDIATE : BEGINNER;
    }

    @Override
    public String toString() { return displayName; }
}
