package gui;

import model.Answer;
import model.InterviewResult;
import utils.TimerManager;
import utils.UI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Screen 5: the final interview report. */
public class ResultFrame extends JFrame {

    public ResultFrame(InterviewResult r) {
        UI.setupFrame(this, "Interview Report - AI Interview Simulator", 1020, 800);
        setLayout(new BorderLayout());

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(UI.BG);
        content.setBorder(UI.pad(20, 28, 20, 28));
        int row = 0;
        row = addRow(content, buildHeader(r), row);
        row = addRow(content, buildStats(r), row);
        row = addRow(content, buildDetails(r), row);
        row = addRow(content, buildQuestionTable(r), row);
        row = addRow(content, buildStrengthsWeaknesses(r), row);
        row = addRow(content, buildRecommendation(r), row);
        addRow(content, buildButtons(r), row);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UI.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private int addRow(JPanel parent, JComponent c, int row) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 16, 0);
        parent.add(c, gc);
        return row + 1;
    }

    private String score(double v) {
        return String.format(Locale.US, "%.1f/10", v);
    }

    private JPanel buildHeader(InterviewResult r) {
        UI.RoundedPanel p = new UI.RoundedPanel(20, UI.PRIMARY);
        p.setLayout(new BorderLayout());
        p.setBorder(UI.pad(22, 28, 24, 28));
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(UI.left(UI.label("Interview Report", 28, Font.BOLD, Color.WHITE)));
        left.add(Box.createVerticalStrut(6));
        left.add(UI.left(UI.label(r.getCandidateName() + "  \u2022  " + r.getRole() + "  \u2022  "
                + r.getDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
                14, Font.PLAIN, new Color(0xE0E7FF))));
        p.add(left, BorderLayout.WEST);
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setOpaque(false);
        JLabel big = UI.label(score(r.getOverallScore()), 38, Font.BOLD, Color.WHITE);
        big.setAlignmentX(Component.RIGHT_ALIGNMENT);
        JLabel small = UI.label("Overall Score", 13, Font.PLAIN, new Color(0xE0E7FF));
        small.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(big);
        right.add(small);
        p.add(right, BorderLayout.EAST);
        return p;
    }

    private JPanel buildStats(InterviewResult r) {
        JPanel p = new JPanel(new GridLayout(1, 4, 14, 0));
        p.setOpaque(false);
        p.add(UI.statCard("Average Score", score(r.getAverageScore()), UI.PRIMARY));
        p.add(UI.statCard("Strong Answers", r.getStrongAnswers() + " / " + r.getQuestionsAttempted(), UI.SUCCESS));
        p.add(UI.statCard("Technical Score", score(r.getTechnicalScore()), UI.PURPLE));
        p.add(UI.statCard("Interview Duration", TimerManager.format(r.getDurationSeconds()), UI.WARNING));
        return p;
    }

    private JPanel buildDetails(InterviewResult r) {
        UI.RoundedPanel card = new UI.RoundedPanel(18, UI.CARD);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(UI.pad(18, 22, 18, 22));
        card.add(UI.label("Interview Summary", 17, Font.BOLD, UI.TEXT), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 2, 40, 8));
        grid.setOpaque(false);
        String[][] items = {
            {"Candidate Name", r.getCandidateName()},
            {"Role", r.getRole().getDisplayName()},
            {"Starting Difficulty", r.getStartDifficulty().getDisplayName()},
            {"Questions Attempted", String.valueOf(r.getQuestionsAttempted())},
            {"Correct / Strong Answers", String.valueOf(r.getStrongAnswers())},
            {"Average Score", score(r.getAverageScore())},
            {"Technical Score", score(r.getTechnicalScore())},
            {"Communication Score", score(r.getCommunicationScore())},
            {"Accuracy Score", score(r.getAccuracyScore())},
            {"Completeness Score", score(r.getCompletenessScore())},
            {"Overall Score", score(r.getOverallScore())},
            {"Interview Duration", TimerManager.format(r.getDurationSeconds())}
        };
        for (String[] item : items) {
            JPanel cell = new JPanel(new BorderLayout());
            cell.setOpaque(false);
            cell.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UI.BORDER));
            cell.add(UI.label(item[0], 14, Font.PLAIN, UI.MUTED), BorderLayout.WEST);
            cell.add(UI.label(item[1], 14, Font.BOLD, UI.TEXT), BorderLayout.EAST);
            grid.add(cell);
        }
        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildQuestionTable(InterviewResult r) {
        UI.RoundedPanel card = new UI.RoundedPanel(18, UI.CARD);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(UI.pad(18, 22, 18, 22));
        card.add(UI.label("Question-wise Performance", 17, Font.BOLD, UI.TEXT), BorderLayout.NORTH);

        String[] columns = {"#", "Category", "Question", "Level", "Score", "Time"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        List<Answer> answers = r.getAnswers();
        int i = 1;
        for (Answer a : answers) {
            model.addRow(new Object[]{
                i++, a.getQuestion().getCategory(), a.getQuestion().getText(),
                a.getQuestion().getDifficulty().getDisplayName(),
                String.format(Locale.US, "%.1f", a.getFinalScore()),
                TimerManager.format(a.getSecondsTaken())
            });
        }
        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(UI.font(Font.PLAIN, 13));
        table.getTableHeader().setFont(UI.font(Font.BOLD, 13));
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        int[] widths = {30, 140, 420, 100, 60, 60};
        for (int c = 0; c < widths.length; c++) {
            table.getColumnModel().getColumn(c).setPreferredWidth(widths[c]);
        }
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UI.BORDER));
        scroll.setPreferredSize(new Dimension(400, table.getRowHeight() * Math.max(answers.size(), 1) + 32));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildStrengthsWeaknesses(InterviewResult r) {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setOpaque(false);
        p.add(listCard("Strengths", r.getStrengths(), UI.SUCCESS));
        p.add(listCard("Weaknesses", r.getWeaknesses(), UI.DANGER));
        return p;
    }

    private JPanel listCard(String title, List<String> items, Color accent) {
        UI.RoundedPanel card = new UI.RoundedPanel(18, UI.CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(UI.pad(18, 22, 18, 22));
        card.add(UI.left(UI.label(title, 17, Font.BOLD, accent)));
        card.add(Box.createVerticalStrut(8));
        for (String s : items) {
            JLabel l = UI.label("<html>\u2022  " + s + "</html>", 14, Font.PLAIN, UI.TEXT);
            l.setBorder(UI.pad(3, 0, 3, 0));
            card.add(UI.left(l));
        }
        return card;
    }

    private JPanel buildRecommendation(InterviewResult r) {
        UI.RoundedPanel card = new UI.RoundedPanel(18, UI.CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(UI.pad(18, 22, 18, 22));
        card.add(UI.left(UI.label("Recommendation", 17, Font.BOLD, UI.PRIMARY)));
        card.add(Box.createVerticalStrut(8));
        card.add(UI.left(UI.label("<html>" + r.getRecommendation() + "</html>", 15, Font.PLAIN, UI.TEXT)));
        return card;
    }

    private JPanel buildButtons(InterviewResult r) {
        UI.RoundedButton dashboard = new UI.RoundedButton("Back to Dashboard", UI.PRIMARY);
        UI.RoundedButton history = new UI.RoundedButton("View History", UI.PURPLE);
        UI.RoundedButton exit = new UI.RoundedButton("Exit", new Color(0x6B7280));
        dashboard.addActionListener(e -> {
            dispose();
            new DashboardFrame(r.getCandidateName()).setVisible(true);
        });
        history.addActionListener(e -> new HistoryDialog(this).setVisible(true));
        exit.addActionListener(e -> System.exit(0));
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        p.setOpaque(false);
        p.add(dashboard);
        p.add(history);
        p.add(exit);
        return p;
    }
}
