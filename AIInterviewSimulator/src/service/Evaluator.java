package service;

import model.Answer;
import model.Question;

/**
 * Anything that can evaluate an answer (INTERFACE).
 * Today we use a rule-based evaluator; later you can add a real AI-API evaluator
 * without changing the rest of the program (POLYMORPHISM).
 */
public interface Evaluator {
    Answer evaluate(Question question, String answerText, int secondsTaken, boolean timedOut);
}
