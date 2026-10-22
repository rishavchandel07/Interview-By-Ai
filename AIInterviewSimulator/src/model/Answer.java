package model;

import java.util.ArrayList;
import java.util.List;

/** A candidate's answer to one question together with its evaluation. */
public class Answer {
    private final Question question;
    private final String text;
    private final int secondsTaken;
    private final boolean timedOut;

    private double technical;
    private double communication;
    private double accuracy;
    private double completeness;
    private double finalScore;
    private String feedback = "";
    private List<String> matchedConcepts = new ArrayList<>();
    private List<String> missingConcepts = new ArrayList<>();

    public Answer(Question question, String text, int secondsTaken, boolean timedOut) {
        this.question = question;
        this.text = text;
        this.secondsTaken = secondsTaken;
        this.timedOut = timedOut;
    }

    public void setScores(double technical, double communication, double accuracy,
                          double completeness, double finalScore) {
        this.technical = technical;
        this.communication = communication;
        this.accuracy = accuracy;
        this.completeness = completeness;
        this.finalScore = finalScore;
    }

    public void setFeedback(String feedback) { this.feedback = feedback; }

    public void setConcepts(List<String> matched, List<String> missing) {
        this.matchedConcepts = matched;
        this.missingConcepts = missing;
    }

    public Question getQuestion() { return question; }
    public String getText() { return text; }
    public int getSecondsTaken() { return secondsTaken; }
    public boolean isTimedOut() { return timedOut; }
    public double getTechnical() { return technical; }
    public double getCommunication() { return communication; }
    public double getAccuracy() { return accuracy; }
    public double getCompleteness() { return completeness; }
    public double getFinalScore() { return finalScore; }
    public String getFeedback() { return feedback; }
    public List<String> getMatchedConcepts() { return matchedConcepts; }
    public List<String> getMissingConcepts() { return missingConcepts; }

    /** An answer scoring 7 or more is counted as a strong answer. */
    public boolean isStrong() { return finalScore >= 7.0; }
}
