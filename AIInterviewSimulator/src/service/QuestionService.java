package service;

import model.Difficulty;
import model.Question;
import model.Role;
import utils.QuestionBank;

import java.util.*;

/** Loads questions and picks random ones by role and difficulty. */
public class QuestionService {
    private final Map<String, List<Question>> index = new HashMap<>();
    private final Random random = new Random();

    public QuestionService(FileService fileService) {
        List<String> lines = fileService.loadQuestionLines();
        if (lines.isEmpty()) {
            lines = QuestionBank.getDefaultQuestionLines();   // fall back to built-in questions
        }
        int id = 1;
        for (String line : lines) {
            try {
                Question q = Question.fromLine(id++, line);
                index.computeIfAbsent(key(q.getRole(), q.getDifficulty()), k -> new ArrayList<>()).add(q);
            } catch (IllegalArgumentException e) {
                System.err.println("Skipping invalid question line: " + line);
            }
        }
    }

    private String key(Role role, Difficulty difficulty) {
        return role.name() + "_" + difficulty.name();
    }

    /**
     * Random question that has not been asked yet.
     * If the wanted difficulty is exhausted, tries easier and then harder levels.
     */
    public Question getRandomQuestion(Role role, Difficulty wanted, Set<Integer> alreadyAsked) {
        List<Difficulty> order = new ArrayList<>();
        order.add(wanted);
        order.add(wanted.easier());
        order.add(wanted.harder());
        for (Difficulty d : Difficulty.values()) order.add(d);

        for (Difficulty d : order) {
            List<Question> candidates = new ArrayList<>();
            for (Question q : index.getOrDefault(key(role, d), new ArrayList<>())) {
                if (!alreadyAsked.contains(q.getId())) candidates.add(q);
            }
            if (!candidates.isEmpty()) {
                return candidates.get(random.nextInt(candidates.size()));
            }
        }
        return null;
    }

    public int countFor(Role role) {
        int total = 0;
        for (Difficulty d : Difficulty.values()) {
            total += index.getOrDefault(key(role, d), new ArrayList<>()).size();
        }
        return total;
    }
}
