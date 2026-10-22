package model;

import java.util.ArrayList;
import java.util.List;

/**
 * One interview question.
 * Each "keyword group" is a concept; the first word is the main name and the
 * others are synonyms. Example: [extends, extend, extended].
 */
public class Question {
    private final int id;
    private final Role role;
    private final Difficulty difficulty;
    private final String category;
    private final String text;
    private final List<List<String>> keywordGroups;

    public Question(int id, Role role, Difficulty difficulty, String category,
                    String text, List<List<String>> keywordGroups) {
        this.id = id;
        this.role = role;
        this.difficulty = difficulty;
        this.category = category;
        this.text = text;
        this.keywordGroups = keywordGroups;
    }

    public int getId() { return id; }
    public Role getRole() { return role; }
    public Difficulty getDifficulty() { return difficulty; }
    public String getCategory() { return category; }
    public String getText() { return text; }
    public List<List<String>> getKeywordGroups() { return keywordGroups; }

    /**
     * Parses a line of questions.txt:
     * ROLE|DIFFICULTY|CATEGORY|QUESTION|keyword1,keyword2/synonym,keyword3
     *
     * @throws IllegalArgumentException if the line is not valid
     */
    public static Question fromLine(int id, String line) {
        String[] p = line.split("\\|", 5);
        if (p.length < 5) {
            throw new IllegalArgumentException("Bad question line: " + line);
        }
        Role role = Role.valueOf(p[0].trim().toUpperCase());
        Difficulty difficulty = Difficulty.valueOf(p[1].trim().toUpperCase());
        List<List<String>> groups = new ArrayList<>();
        for (String group : p[4].split(",")) {
            List<String> synonyms = new ArrayList<>();
            for (String s : group.split("/")) {
                if (!s.trim().isEmpty()) {
                    synonyms.add(s.trim());
                }
            }
            if (!synonyms.isEmpty()) {
                groups.add(synonyms);
            }
        }
        return new Question(id, role, difficulty, p[2].trim(), p[3].trim(), groups);
    }
}
