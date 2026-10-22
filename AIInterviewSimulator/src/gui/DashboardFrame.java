package gui;

import service.FileService;
import utils.UI;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Locale;

/** Screen 2: dashboard with statistics and main actions. */
public class DashboardFrame extends JFrame {
    private final String candidateName;

    public DashboardFrame(String candidateName) {
        this.candidateName = candidateName;
        UI.setupFrame(this, "Dashboard - AI Interview Simulator", 960, 600);
        setLayout(new BorderLayout());

        // ---- Header ----
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(UI.PRIMARY);
        header.setBorder(UI.pad(26, 34, 26, 34));
        header.add(UI.left(UI.label("Welcome, " + candidateName, 28, Font.BOLD, Color.WHITE)));
        header.add(Box.createVerticalStrut(4));
        header.add(UI.left(UI.label("Ready to practise your next interview?", 14, Font.PLAIN, new Color(0xE0E7FF))));
        add(header, BorderLayout.NORTH);

        // ---- Body ----
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(UI.BG);
        body.setBorder(UI.pad(24, 34, 24, 34));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 18, 0);

        List<Double> scores = new FileService().readOverallScores();
        double best = 0, sum = 0;
        for (double s : scores) { best = Math.max(best, s); sum += s; }
        String avg = scores.isEmpty() ? "-" : String.format(Locale.US, "%.1f", sum / scores.size());
        String bestText = scores.isEmpty() ? "-" : String.format(Locale.US, "%.1f", best);

        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0));
        stats.setOpaque(false);
        stats.add(UI.statCard("Interviews completed", String.valueOf(scores.size()), UI.PRIMARY));
        stats.add(UI.statCard("Best overall score", bestText, UI.SUCCESS));
        stats.add(UI.statCard("Average overall score", avg, UI.WARNING));
        gc.gridy = 0;
        body.add(stats, gc);

        UI.RoundedPanel actions = new UI.RoundedPanel(18, UI.CARD);
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setBorder(UI.pad(22, 26, 22, 26));
        actions.add(UI.left(UI.label("What would you like to do?", 18, Font.BOLD, UI.TEXT)));
        actions.add(Box.createVerticalStrut(16));

        UI.RoundedButton start = new UI.RoundedButton("Start New Interview", UI.PRIMARY);
        UI.RoundedButton history = new UI.RoundedButton("View Interview History", UI.PURPLE);
        UI.RoundedButton exit = new UI.RoundedButton("Exit", new Color(0x6B7280));
        start.addActionListener(e -> {
            dispose();
            new RoleSelectionFrame(candidateName).setVisible(true);
        });
        history.addActionListener(e -> new HistoryDialog(this).setVisible(true));
        exit.addActionListener(e -> System.exit(0));
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(start);
        row.add(Box.createHorizontalStrut(12));
        row.add(history);
        row.add(Box.createHorizontalStrut(12));
        row.add(exit);
        actions.add(row);
        gc.gridy = 1;
        body.add(actions, gc);

        UI.RoundedPanel tips = new UI.RoundedPanel(18, UI.CARD);
        tips.setLayout(new BoxLayout(tips, BoxLayout.Y_AXIS));
        tips.setBorder(UI.pad(20, 26, 20, 26));
        tips.add(UI.left(UI.label("Tips for a better score", 16, Font.BOLD, UI.TEXT)));
        tips.add(Box.createVerticalStrut(8));
        String[] tips1 = {
            "Answer in full sentences and explain your points with an example.",
            "Mention the key technical terms - the evaluator looks for concepts and synonyms.",
            "Watch the timer: when it reaches zero your answer is submitted automatically."
        };
        for (String t : tips1) {
            tips.add(UI.left(UI.label("\u2022  " + t, 13, Font.PLAIN, UI.MUTED)));
            tips.add(Box.createVerticalStrut(4));
        }
        gc.gridy = 2;
        gc.weighty = 1;
        gc.anchor = GridBagConstraints.NORTH;
        body.add(tips, gc);

        add(body, BorderLayout.CENTER);
    }
}
