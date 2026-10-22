package service;

import model.Answer;
import model.Question;

import java.util.*;

/**
 * Rule-based "AI-style" evaluator.
 *
 * Smarter than exact word matching because it:
 *  1. matches word PREFIXES   (extend -> extends, extended)
 *  2. understands SYNONYMS    (parent / superclass / base class)
 *  3. forgives small TYPOS    (inheratance -> inheritance)
 *  4. checks length, sentences, connectors (communication quality)
 *  5. penalises uncertain words and keyword stuffing
 */
public class EvaluationService implements Evaluator {

    // Weights for the final score (they add up to 1.0)
    public static final double W_TECHNICAL = 0.30;
    public static final double W_COMMUNICATION = 0.20;
    public static final double W_ACCURACY = 0.25;
    public static final double W_COMPLETENESS = 0.25;

    private static final String[] UNCERTAIN = {
        "i don t know", "i dont know", "not sure", "no idea", "maybe", "i guess", "don t remember"
    };
    private static final String[] CONNECTORS = {
        "because", "therefore", "for example", "for instance", "such as", "also", "however",
        "first", "second", "finally", "in addition", "so that", "which", "while", "whereas", "as a result"
    };
    private static final String[] EXAMPLE_MARKERS = {"for example", "for instance", "such as", "e g"};

    @Override
    public Answer evaluate(Question q, String answerText, int secondsTaken, boolean timedOut) {
        String raw = answerText == null ? "" : answerText.trim();
        Answer answer = new Answer(q, raw, secondsTaken, timedOut);

        String norm = normalize(raw);
        List<String> allConcepts = new ArrayList<>();
        for (List<String> group : q.getKeywordGroups()) allConcepts.add(group.get(0));

        // ---- Empty answer ----
        if (norm.isEmpty()) {
            answer.setScores(0, 0, 0, 0, 0);
            answer.setConcepts(new ArrayList<>(), allConcepts);
            answer.setFeedback("No answer was submitted. Try to cover these ideas next time: "
                    + join(allConcepts, 4) + ".");
            return answer;
        }

        String[] tokens = norm.split(" ");
        int words = tokens.length;
        int ideal = q.getDifficulty().getIdealWords();
        String padded = " " + norm + " ";

        // ---- Keyword coverage (with synonyms, prefixes and typo tolerance) ----
        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (List<String> group : q.getKeywordGroups()) {
            if (groupMatches(group, padded, tokens)) matched.add(group.get(0));
            else missing.add(group.get(0));
        }
        double coverage = q.getKeywordGroups().isEmpty()
                ? 0 : (double) matched.size() / q.getKeywordGroups().size();

        int uncertain = 0;
        for (String u : UNCERTAIN) if (padded.contains(" " + u + " ")) uncertain++;
        boolean stuffing = matched.size() >= 3 && (double) words / matched.size() < 2.0;

        // ---- The four sub-scores (each 0-10) ----
        double technical = 10 * Math.min(1.0, coverage / 0.7);
        for (String m : EXAMPLE_MARKERS) {
            if (padded.contains(" " + m + " ")) { technical = Math.min(10, technical + 0.5); break; }
        }

        double lengthRatio = Math.min(1.0, words / (double) ideal);
        double completeness = 10 * (0.65 * Math.min(1.0, coverage / 0.8) + 0.35 * lengthRatio);

        double accuracy = 10 * Math.min(1.0, coverage / 0.6);
        accuracy -= 1.5 * Math.min(2, uncertain);
        if (stuffing) accuracy -= 2;
        if (words < 5) accuracy = Math.min(accuracy, 3);

        double communication = scoreCommunication(raw, norm, tokens, ideal);

        technical = round1(clamp(technical));
        completeness = round1(clamp(completeness));
        accuracy = round1(clamp(accuracy));
        communication = round1(clamp(communication));
        double finalScore = round1(weightedScore(technical, communication, accuracy, completeness));

        answer.setScores(technical, communication, accuracy, completeness, finalScore);
        answer.setConcepts(matched, missing);
        answer.setFeedback(buildFeedback(finalScore, communication, coverage, matched, missing,
                words, ideal, uncertain, stuffing));
        return answer;
    }

    /** Weighted average of the four sub-scores. */
    public static double weightedScore(double technical, double communication,
                                       double accuracy, double completeness) {
        return technical * W_TECHNICAL + communication * W_COMMUNICATION
                + accuracy * W_ACCURACY + completeness * W_COMPLETENESS;
    }

    public static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static double clamp(double v) {
        return Math.max(0, Math.min(10, v));
    }

    // ------------------------------------------------------------------
    //  Communication score: length, sentences, connectors, variety, polish
    // ------------------------------------------------------------------
    private double scoreCommunication(String raw, String norm, String[] tokens, int ideal) {
        int words = tokens.length;
        int sentences = 0;
        for (String part : raw.split("[.!?]+")) {
            if (!part.trim().isEmpty()) sentences++;
        }
        double c = 0;
        c += 3.0 * Math.min(1.0, words / (double) ideal);          // enough detail
        c += 2.5 * Math.min(sentences, 3) / 3.0;                    // several sentences
        int connectors = 0;
        String padded = " " + norm + " ";
        for (String k : CONNECTORS) if (padded.contains(" " + k + " ")) connectors++;
        c += 0.5 * Math.min(connectors, 3);                         // logical flow
        Set<String> unique = new HashSet<>(Arrays.asList(tokens));
        c += 1.5 * Math.min(1.0, ((double) unique.size() / words) / 0.6);   // vocabulary variety
        if (Character.isUpperCase(raw.charAt(0))) c += 0.5;         // starts with capital
        char last = raw.charAt(raw.length() - 1);
        if (last == '.' || last == '!' || last == '?') c += 0.5;    // ends with punctuation
        return c * 10.0 / 9.5;
    }

    // ------------------------------------------------------------------
    //  Feedback text
    // ------------------------------------------------------------------
    private String buildFeedback(double score, double communication, double coverage,
                                 List<String> matched, List<String> missing,
                                 int words, int ideal, int uncertain, boolean stuffing) {
        StringBuilder sb = new StringBuilder();
        if (score >= 8) sb.append("Excellent answer.");
        else if (score >= 6) sb.append("Good answer.");
        else if (score >= 4) sb.append("Fair attempt, but it needs more depth.");
        else sb.append("This answer needs significant improvement.");

        if (!matched.isEmpty()) sb.append(" You covered: ").append(join(matched, 4)).append(".");
        if (!missing.isEmpty() && coverage < 1.0) {
            sb.append(" You could improve your answer by mentioning: ").append(join(missing, 3)).append(".");
        }
        if (words < ideal / 2) sb.append(" Try to give a more detailed answer.");
        if (communication < 5) {
            sb.append(" Structure your answer in complete sentences and use connectors like 'because' or 'for example'.");
        }
        if (uncertain > 0) sb.append(" Avoid sounding unsure; answer with confidence.");
        if (stuffing) sb.append(" Do not just list keywords; explain the ideas in sentences.");
        return sb.toString();
    }

    private String join(List<String> list, int max) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size() && i < max; i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------
    //  Text matching helpers
    // ------------------------------------------------------------------
    /** Lower-case, remove punctuation, collapse spaces. */
    static String normalize(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]+", " ").trim();
    }

    private boolean groupMatches(List<String> group, String padded, String[] tokens) {
        for (String synonym : group) {
            String k = normalize(synonym);
            if (k.isEmpty()) continue;
            if (padded.contains(" " + k)) return true;           // word-start (prefix) match
            if (!k.contains(" ") && k.length() >= 6) {            // typo tolerance
                for (String t : tokens) {
                    if (Math.abs(t.length() - k.length()) <= 1 && editDistance(t, k) <= 1) return true;
                }
            }
        }
        return false;
    }

    /** Classic Levenshtein edit distance. */
    private int editDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }
}
