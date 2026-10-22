package service;

import model.InterviewResult;
import utils.QuestionBank;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** All file handling: questions.txt (read) and interview_history.txt (append / read). */
public class FileService {
    public static final String DATA_DIR = "data";
    private final Path historyPath = Paths.get(DATA_DIR, "interview_history.txt");
    private final Path questionsPath = Paths.get(DATA_DIR, "questions.txt");

    public FileService() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            if (!Files.exists(questionsPath)) {
                writeDefaultQuestions();
            }
        } catch (IOException e) {
            System.err.println("Could not prepare data folder: " + e.getMessage());
        }
    }

    private void writeDefaultQuestions() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# FORMAT: ROLE|DIFFICULTY|CATEGORY|QUESTION|keyword1,keyword2/synonym,keyword3");
        lines.add("# '/' separates synonyms, ',' separates concepts. Lines starting with # are ignored.");
        lines.add("# You can add your own questions at the end of this file.");
        lines.addAll(QuestionBank.getDefaultQuestionLines());
        Files.write(questionsPath, lines, StandardCharsets.UTF_8);
    }

    /** Reads question lines (ignores blank lines and comments). */
    public List<String> loadQuestionLines() {
        List<String> result = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(questionsPath, StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (!t.isEmpty() && !t.startsWith("#")) result.add(t);
            }
        } catch (IOException e) {
            System.err.println("Could not read questions file: " + e.getMessage());
        }
        return result;
    }

    /** Appends a finished interview report to the history file. */
    public boolean saveResult(InterviewResult result) {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
            byte[] data = (result.toReportText() + System.lineSeparator()).getBytes(StandardCharsets.UTF_8);
            Files.write(historyPath, data, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            System.err.println("Could not save interview: " + e.getMessage());
            return false;
        }
    }

    /** Returns the whole history file as text. */
    public String loadHistory() {
        if (!Files.exists(historyPath)) return "No interviews saved yet. Complete an interview to see it here.";
        try {
            String text = new String(Files.readAllBytes(historyPath), StandardCharsets.UTF_8);
            return text.trim().isEmpty() ? "No interviews saved yet." : text;
        } catch (IOException e) {
            return "Could not read history: " + e.getMessage();
        }
    }

    /** Reads every "Overall Score" from the history (for dashboard statistics). */
    public List<Double> readOverallScores() {
        List<Double> scores = new ArrayList<>();
        if (!Files.exists(historyPath)) return scores;
        try {
            for (String line : Files.readAllLines(historyPath, StandardCharsets.UTF_8)) {
                if (line.startsWith("Overall Score")) {
                    String value = line.substring(line.indexOf(':') + 1).trim();
                    value = value.substring(0, value.indexOf('/'));
                    try {
                        scores.add(Double.parseDouble(value));
                    } catch (NumberFormatException ignored) {
                        // skip a damaged line
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read history: " + e.getMessage());
        }
        return scores;
    }
}
