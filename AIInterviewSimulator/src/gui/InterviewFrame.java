package gui;

import model.Answer;
import model.Question;
import service.FileService;
import service.InterviewService;
import model.InterviewResult;
import utils.TimerManager;
import utils.UI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.Locale;

/** Screen 4: the live interview (question, answer box, timer, progress, feedback). */
public class InterviewFrame extends JFrame {
    private final InterviewService service;
    private final TimerManager questionTimer;
    private final javax.swing.Timer totalTimer;
    private javax.swing.Timer autoAdvance;

    private Question current;
    private boolean answered = false;
    private long startNanos;

    private final JLabel difficultyLabel = UI.label(" ", 13, Font.PLAIN, UI.MUTED);
    private final JLabel progressLabel = UI.label(" ", 14, Font.BOLD, UI.TEXT);
    private final JProgressBar progressBar = new JProgressBar(0, InterviewService.TOTAL_QUESTIONS);
    private final JLabel timerLabel = UI.label("Time Remaining: 00:00", 20, Font.BOLD, UI.TEXT);
    private final JLabel totalLabel = UI.label("Total Time: 00:00", 13, Font.PLAIN, UI.MUTED);
    private final JLabel scoreLabel = UI.label("Average Score: -", 13, Font.PLAIN, UI.MUTED);

    private final JLabel categoryLabel = UI.label(" ", 12, Font.BOLD, UI.PRIMARY);
    private final JTextArea questionArea = UI.textArea(2, false, 20);
    private final JTextArea answerArea = UI.textArea(8, true, 15);
    private final JLabel wordCountLabel = UI.label("0 words", 12, Font.PLAIN, UI.MUTED);
    private final UI.RoundedButton submitButton = new UI.RoundedButton("Submit Answer", UI.PRIMARY);
    private final UI.RoundedButton nextButton = new UI.RoundedButton("Next Question", UI.SUCCESS);

    private final UI.RoundedPanel feedbackCard = new UI.RoundedPanel(18, UI.CARD);
    private final JLabel feedbackScoreLabel = UI.label(" ", 20, Font.BOLD, UI.SUCCESS);
    private final JLabel breakdownLabel = UI.label(" ", 13, Font.PLAIN, UI.MUTED);
    private final JTextArea feedbackArea = UI.textArea(3, false, 14);
    private final JLabel adaptLabel = UI.label(" ", 13, Font.BOLD, UI.PRIMARY);

    public InterviewFrame(InterviewService service) {
        this.service = service;
        UI.setupFrame(this, "Interview - AI Interview Simulator", 1100, 780);
        setLayout(new BorderLayout());

        // Timer on its own thread (see TimerManager)
        questionTimer = new TimerManager(new TimerManager.TimerListener() {
            @Override
            public void onTick(int secondsRemaining) {
                timerLabel.setText("Time Remaining: " + TimerManager.format(secondsRemaining));
                timerLabel.setForeground(secondsRemaining <= 10 ? UI.DANGER : UI.TEXT);
            }

            @Override
            public void onTimeUp() {
                submit(true);          // auto-submit when time is over
            }
        });
        totalTimer = new javax.swing.Timer(1000, e ->
                totalLabel.setText("Total Time: " + TimerManager.format(service.getElapsedSeconds())));
        totalTimer.start();

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(buildHeader(), BorderLayout.NORTH);
        top.add(buildQuestionCard(), BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setBackground(UI.BG);
        content.setBorder(UI.pad(16, 22, 16, 22));
        content.add(top, BorderLayout.NORTH);
        content.add(buildAnswerCard(), BorderLayout.CENTER);
        content.add(buildFeedbackCard(), BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UI.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        submitButton.addActionListener(e -> submit(false));
        nextButton.addActionListener(e -> goNext());

        loadNextQuestion();
    }

    // ------------------------------------------------------------------ UI building
    private JPanel buildHeader() {
        UI.RoundedPanel header = new UI.RoundedPanel(18, UI.CARD);
        header.setLayout(new BorderLayout(20, 0));
        header.setBorder(UI.pad(14, 22, 16, 22));

        JPanel west = new JPanel();
        west.setLayout(new BoxLayout(west, BoxLayout.Y_AXIS));
        west.setOpaque(false);
        west.add(UI.left(UI.label(service.getCandidate().getRole() + " Interview", 18, Font.BOLD, UI.TEXT)));
        west.add(UI.left(UI.label("Candidate: " + service.getCandidate().getName(), 13, Font.PLAIN, UI.MUTED)));
        west.add(UI.left(difficultyLabel));

        progressBar.setForeground(UI.PRIMARY);
        progressBar.setBackground(new Color(0xE5E7EB));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(200, 10));
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);
        center.setBorder(UI.pad(4, 30, 0, 30));
        center.add(UI.left(progressLabel));
        center.add(Box.createVerticalStrut(8));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        center.add(progressBar);

        JPanel east = new JPanel();
        east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
        east.setOpaque(false);
        for (JLabel l : new JLabel[]{timerLabel, totalLabel, scoreLabel}) {
            l.setAlignmentX(Component.RIGHT_ALIGNMENT);
            east.add(l);
        }

        header.add(west, BorderLayout.WEST);
        header.add(center, BorderLayout.CENTER);
        header.add(east, BorderLayout.EAST);
        return header;
    }

    private JPanel buildQuestionCard() {
        UI.RoundedPanel card = new UI.RoundedPanel(18, UI.CARD);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(UI.pad(18, 22, 18, 22));
        categoryLabel.setBorder(UI.pad(0, 10, 0, 0));
        card.add(categoryLabel, BorderLayout.NORTH);
        questionArea.setFont(UI.font(Font.BOLD, 21));
        questionArea.setOpaque(false);
        card.add(questionArea, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAnswerCard() {
        UI.RoundedPanel card = new UI.RoundedPanel(18, UI.CARD);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(UI.pad(16, 22, 16, 22));
        card.add(UI.label("Your Answer", 15, Font.BOLD, UI.TEXT), BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(answerArea);
        scroll.setBorder(BorderFactory.createLineBorder(UI.BORDER, 1, true));
        scroll.setPreferredSize(new Dimension(400, 170));
        card.add(scroll, BorderLayout.CENTER);

        answerArea.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String t = answerArea.getText().trim();
                int words = t.isEmpty() ? 0 : t.split("\\s+").length;
                wordCountLabel.setText(words + (words == 1 ? " word" : " words"));
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(wordCountLabel, BorderLayout.WEST);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(submitButton);
        buttons.add(nextButton);
        bottom.add(buttons, BorderLayout.EAST);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFeedbackCard() {
        feedbackCard.setLayout(new BoxLayout(feedbackCard, BoxLayout.Y_AXIS));
        feedbackCard.setBorder(UI.pad(16, 22, 16, 22));
        feedbackArea.setOpaque(false);
        feedbackArea.setBorder(UI.pad(6, 0, 6, 0));
        feedbackCard.add(UI.left(feedbackScoreLabel));
        feedbackCard.add(UI.left(breakdownLabel));
        feedbackCard.add(UI.left(feedbackArea));
        feedbackCard.add(UI.left(adaptLabel));
        feedbackCard.setVisible(false);
        return feedbackCard;
    }

    // ------------------------------------------------------------------ interview flow
    private void loadNextQuestion() {
        current = service.nextQuestion();
        if (current == null) {            // no more questions in the bank
            finishInterview();
            return;
        }
        answered = false;
        answerArea.setText("");
        answerArea.setEditable(true);
        feedbackCard.setVisible(false);
        submitButton.setEnabled(true);
        nextButton.setEnabled(false);
        nextButton.setText("Next Question");

        progressLabel.setText("Question " + service.getCurrentQuestionNumber() + " of " + InterviewService.TOTAL_QUESTIONS);
        progressBar.setValue(service.getAnsweredCount());
        difficultyLabel.setText("Difficulty: " + current.getDifficulty());
        categoryLabel.setText(current.getCategory().toUpperCase());
        questionArea.setText(current.getText());

        int limit = current.getDifficulty().getTimeLimitSeconds();
        timerLabel.setText("Time Remaining: " + TimerManager.format(limit));
        timerLabel.setForeground(UI.TEXT);
        startNanos = System.nanoTime();
        questionTimer.start(limit);
        answerArea.requestFocusInWindow();
    }

    private void submit(boolean timedOut) {
        if (answered || current == null) return;
        answered = true;
        questionTimer.stop();
        int seconds = (int) ((System.nanoTime() - startNanos) / 1_000_000_000L);

        Answer answer = service.submitAnswer(current, answerArea.getText(), seconds, timedOut);
        answerArea.setEditable(false);
        submitButton.setEnabled(false);
        progressBar.setValue(service.getAnsweredCount());
        scoreLabel.setText(String.format(Locale.US, "Average Score: %.1f/10", service.getRunningAverage()));
        showFeedback(answer, timedOut);

        nextButton.setText(service.hasMoreQuestions() ? "Next Question" : "Finish & View Report");
        nextButton.setEnabled(true);

        if (timedOut) {                   // move on automatically after 3 seconds
            autoAdvance = new javax.swing.Timer(3000, e -> goNext());
            autoAdvance.setRepeats(false);
            autoAdvance.start();
        }
    }

    private void showFeedback(Answer a, boolean timedOut) {
        Color c = a.getFinalScore() >= 7 ? UI.SUCCESS : (a.getFinalScore() >= 4 ? UI.WARNING : UI.DANGER);
        feedbackScoreLabel.setForeground(c);
        feedbackScoreLabel.setText(String.format(Locale.US, "Score: %.1f/10", a.getFinalScore()));
        breakdownLabel.setText(String.format(Locale.US,
                "Technical %.1f   |   Communication %.1f   |   Accuracy %.1f   |   Completeness %.1f",
                a.getTechnical(), a.getCommunication(), a.getAccuracy(), a.getCompleteness()));
        feedbackArea.setText("Feedback: " + a.getFeedback());
        String prefix = timedOut ? "Time is up - answer submitted automatically. Moving on in 3 seconds.   " : "";
        adaptLabel.setText(prefix + service.getLastAdaptMessage());
        feedbackCard.setVisible(true);
        feedbackCard.revalidate();
        SwingUtilities.invokeLater(() -> feedbackCard.scrollRectToVisible(feedbackCard.getBounds()));
    }

    private void goNext() {
        if (autoAdvance != null) autoAdvance.stop();
        if (!answered) return;
        if (!service.hasMoreQuestions()) finishInterview();
        else loadNextQuestion();
    }

    private void finishInterview() {
        questionTimer.stop();
        totalTimer.stop();
        InterviewResult result = service.finish();
        if (!new FileService().saveResult(result)) {
            JOptionPane.showMessageDialog(this, "Could not save the result to data/interview_history.txt",
                    "Save failed", JOptionPane.WARNING_MESSAGE);
        }
        dispose();
        new ResultFrame(result).setVisible(true);
    }
}
