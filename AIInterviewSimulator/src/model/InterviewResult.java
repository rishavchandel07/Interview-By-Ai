package model;

import utils.TimerManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** The final report of one completed interview. Implements Reportable. */
public class InterviewResult implements Reportable {
    private final String candidateName;
    private final Role role;
    private final Difficulty startDifficulty;
    private final LocalDateTime dateTime;
    private final long durationSeconds;
    private final int questionsAttempted;
    private final int strongAnswers;
    private final double averageScore;
    private final double technicalScore;
    private final double communicationScore;
    private final double accuracyScore;
    private final double completenessScore;
    private final double overallScore;
    private final List<String> strengths;
    private final List<String> weaknesses;
    private final String recommendation;
    private final List<Answer> answers;

    public InterviewResult(String candidateName, Role role, Difficulty startDifficulty,
                           LocalDateTime dateTime, long durationSeconds,
                           int questionsAttempted, int strongAnswers, double averageScore,
                           double technicalScore, double communicationScore,
                           double accuracyScore, double completenessScore, double overallScore,
                           List<String> strengths, List<String> weaknesses,
                           String recommendation, List<Answer> answers) {
        this.candidateName = candidateName;
        this.role = role;
        this.startDifficulty = startDifficulty;
        this.dateTime = dateTime;
        this.durationSeconds = durationSeconds;
        this.questionsAttempted = questionsAttempted;
        this.strongAnswers = strongAnswers;
        this.averageScore = averageScore;
        this.technicalScore = technicalScore;
        this.communicationScore = communicationScore;
        this.accuracyScore = accuracyScore;
        this.completenessScore = completenessScore;
        this.overallScore = overallScore;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.recommendation = recommendation;
        this.answers = answers;
    }

    public String getCandidateName() { return candidateName; }
    public Role getRole() { return role; }
    public Difficulty getStartDifficulty() { return startDifficulty; }
    public LocalDateTime getDateTime() { return dateTime; }
    public long getDurationSeconds() { return durationSeconds; }
    public int getQuestionsAttempted() { return questionsAttempted; }
    public int getStrongAnswers() { return strongAnswers; }
    public double getAverageScore() { return averageScore; }
    public double getTechnicalScore() { return technicalScore; }
    public double getCommunicationScore() { return communicationScore; }
    public double getAccuracyScore() { return accuracyScore; }
    public double getCompletenessScore() { return completenessScore; }
    public double getOverallScore() { return overallScore; }
    public List<String> getStrengths() { return strengths; }
    public List<String> getWeaknesses() { return weaknesses; }
    public String getRecommendation() { return recommendation; }
    public List<Answer> getAnswers() { return answers; }

    @Override
    public String toReportText() {
        String line = "=".repeat(54);
        StringBuilder sb = new StringBuilder();
        sb.append(line).append('\n');
        sb.append(" AI INTERVIEW REPORT\n");
        sb.append(line).append('\n');
        sb.append(String.format(Locale.US, "Date               : %s%n",
                dateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))));
        sb.append(String.format("Candidate          : %s%n", candidateName));
        sb.append(String.format("Role               : %s%n", role.getDisplayName()));
        sb.append(String.format("Starting Difficulty: %s%n", startDifficulty.getDisplayName()));
        sb.append(String.format("Questions Attempted: %d%n", questionsAttempted));
        sb.append(String.format("Strong Answers     : %d%n", strongAnswers));
        sb.append(String.format(Locale.US, "Average Score      : %.1f/10%n", averageScore));
        sb.append(String.format(Locale.US, "Technical Score    : %.1f/10%n", technicalScore));
        sb.append(String.format(Locale.US, "Communication Score: %.1f/10%n", communicationScore));
        sb.append(String.format(Locale.US, "Accuracy Score     : %.1f/10%n", accuracyScore));
        sb.append(String.format(Locale.US, "Completeness Score : %.1f/10%n", completenessScore));
        sb.append(String.format(Locale.US, "Overall Score      : %.1f/10%n", overallScore));
        sb.append(String.format("Duration           : %s%n", TimerManager.format(durationSeconds)));
        sb.append("\nStrengths:\n");
        for (String s : strengths) sb.append("  - ").append(s).append('\n');
        sb.append("\nWeaknesses:\n");
        for (String w : weaknesses) sb.append("  - ").append(w).append('\n');
        sb.append("\nRecommendation:\n  ").append(recommendation).append('\n');
        sb.append("\nQuestion-wise scores:\n");
        int i = 1;
        for (Answer a : answers) {
            sb.append(String.format(Locale.US, "  %d. [%s] %.1f/10 - %s%n", i++,
                    a.getQuestion().getCategory(), a.getFinalScore(), a.getQuestion().getText()));
        }
        sb.append(line).append('\n');
        return sb.toString();
    }
}
