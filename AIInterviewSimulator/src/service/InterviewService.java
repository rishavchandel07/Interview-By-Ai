package service;

import model.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

/** Controls one interview: question flow, adaptive difficulty and the final report. */
public class InterviewService {
    public static final int TOTAL_QUESTIONS = 5;

    private final Candidate candidate;
    private final QuestionService questionService;
    private final Evaluator evaluator;

    private final List<Answer> answers = new ArrayList<>();
    private final Set<Integer> askedIds = new HashSet<>();
    private Difficulty currentDifficulty;
    private Instant startTime;
    private String lastAdaptMessage = "";

    public InterviewService(Candidate candidate, QuestionService questionService, Evaluator evaluator) {
        this.candidate = candidate;
        this.questionService = questionService;
        this.evaluator = evaluator;
        this.currentDifficulty = candidate.getDifficulty();
    }

    public void start() {
        startTime = Instant.now();
        currentDifficulty = candidate.getDifficulty();
        answers.clear();
        askedIds.clear();
    }

    /** Returns the next question (null if there are none left). */
    public Question nextQuestion() {
        Question q = questionService.getRandomQuestion(candidate.getRole(), currentDifficulty, askedIds);
        if (q != null) askedIds.add(q.getId());
        return q;
    }

    public Answer submitAnswer(Question q, String text, int secondsTaken, boolean timedOut) {
        Answer a = evaluator.evaluate(q, text, secondsTaken, timedOut);
        answers.add(a);
        adaptDifficulty(a.getFinalScore());
        return a;
    }

    /** Adaptive difficulty: good score = harder, weak score = easier. */
    private void adaptDifficulty(double score) {
        Difficulty old = currentDifficulty;
        if (score >= 8) {
            currentDifficulty = currentDifficulty.harder();
            lastAdaptMessage = (old == currentDifficulty)
                    ? "Excellent! You are already at the highest difficulty."
                    : "Great performance! Next question will be harder (" + currentDifficulty + ").";
        } else if (score <= 4) {
            currentDifficulty = currentDifficulty.easier();
            lastAdaptMessage = (old == currentDifficulty)
                    ? "Keep practising! Next question stays at the easiest level."
                    : "No problem. Next question will be easier (" + currentDifficulty + ").";
        } else {
            lastAdaptMessage = "Next question stays at " + currentDifficulty + " level.";
        }
    }

    public boolean hasMoreQuestions() { return answers.size() < TOTAL_QUESTIONS; }
    public int getAnsweredCount() { return answers.size(); }
    public int getCurrentQuestionNumber() { return Math.min(answers.size() + 1, TOTAL_QUESTIONS); }
    public String getLastAdaptMessage() { return lastAdaptMessage; }
    public Candidate getCandidate() { return candidate; }
    public Difficulty getCurrentDifficulty() { return currentDifficulty; }

    public long getElapsedSeconds() {
        return startTime == null ? 0 : Duration.between(startTime, Instant.now()).getSeconds();
    }

    public double getRunningAverage() {
        if (answers.isEmpty()) return 0;
        double sum = 0;
        for (Answer a : answers) sum += a.getFinalScore();
        return sum / answers.size();
    }

    /** Builds the final report. */
    public InterviewResult finish() {
        int n = answers.size();
        double tech = 0, comm = 0, acc = 0, comp = 0;
        int strong = 0;
        Map<String, List<Double>> byCategory = new TreeMap<>();
        for (Answer a : answers) {
            tech += a.getTechnical();
            comm += a.getCommunication();
            acc += a.getAccuracy();
            comp += a.getCompleteness();
            if (a.isStrong()) strong++;
            byCategory.computeIfAbsent(a.getQuestion().getCategory(), k -> new ArrayList<>())
                    .add(a.getFinalScore());
        }
        if (n > 0) { tech /= n; comm /= n; acc /= n; comp /= n; }
        tech = EvaluationService.round1(tech);
        comm = EvaluationService.round1(comm);
        acc = EvaluationService.round1(acc);
        comp = EvaluationService.round1(comp);

        double average = EvaluationService.round1((tech + comm + acc + comp) / 4.0);
        double overall = EvaluationService.round1(EvaluationService.weightedScore(tech, comm, acc, comp));

        List<String> strengths = new ArrayList<>();
        List<String> weaknesses = new ArrayList<>();
        for (Map.Entry<String, List<Double>> e : byCategory.entrySet()) {
            double avg = 0;
            for (double v : e.getValue()) avg += v;
            avg /= e.getValue().size();
            if (avg >= 7) strengths.add("Good understanding of " + e.getKey());
            else if (avg < 5) weaknesses.add(e.getKey() + " needs improvement");
        }
        if (comm >= 7) strengths.add("Clear and well-structured communication");
        if (comm < 5) weaknesses.add("Answers need better structure (use full sentences and examples)");
        if (comp < 5) weaknesses.add("Answers could be more detailed");
        if (tech >= 7) strengths.add("Strong technical knowledge for the " + candidate.getRole() + " role");
        if (strengths.isEmpty()) strengths.add("Attempted every question and kept going");
        if (weaknesses.isEmpty()) weaknesses.add("No major weakness found - try a higher difficulty next time");

        String role = candidate.getRole().getDisplayName();
        String recommendation;
        if (overall >= 8) {
            recommendation = "Excellent candidate. Ready for a real " + role + " interview - try Advanced difficulty next.";
        } else if (overall >= 6.5) {
            recommendation = "Good candidate for an entry-level " + role + " interview. Revise the weaker topics once more.";
        } else if (overall >= 4.5) {
            recommendation = "Needs more preparation before a " + role + " interview. Practise the basics and give detailed answers.";
        } else {
            recommendation = "Not ready yet. Study the fundamentals of the " + role + " role and practise again.";
        }

        return new InterviewResult(candidate.getName(), candidate.getRole(), candidate.getDifficulty(),
                LocalDateTime.now(), getElapsedSeconds(), n, strong, average,
                tech, comm, acc, comp, overall, strengths, weaknesses, recommendation,
                new ArrayList<>(answers));
    }
}
