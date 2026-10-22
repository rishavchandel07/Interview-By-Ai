package gui;

import utils.UI;

import javax.swing.*;
import java.awt.*;

/** Screen 1: welcome + candidate name. */
public class WelcomeFrame extends JFrame {
    private final JTextField nameField = new JTextField();
    private final JLabel errorLabel = UI.label(" ", 12, Font.PLAIN, UI.DANGER);

    public WelcomeFrame() {
        UI.setupFrame(this, "AI Interview Simulator", 920, 560);
        setLayout(new GridLayout(1, 2));
        add(buildLeftPanel());
        add(buildRightPanel());
    }

    private JPanel buildLeftPanel() {
        JPanel left = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, UI.PRIMARY, getWidth(), getHeight(), UI.PURPLE));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(UI.pad(70, 50, 40, 40));

        left.add(UI.left(UI.label("<html>AI Interview<br>Simulator</html>", 38, Font.BOLD, Color.WHITE)));
        left.add(Box.createVerticalStrut(16));
        left.add(UI.left(UI.label("<html>Practise real interview questions and get instant, smart feedback.</html>",
                16, Font.PLAIN, new Color(0xE0E7FF))));
        left.add(Box.createVerticalStrut(34));
        String[] points = {
            "5 job roles and 3 difficulty levels",
            "Adaptive questions that match your level",
            "Score, feedback and a full report",
            "Interview history saved on your computer"
        };
        for (String p : points) {
            JLabel b = UI.label("\u2022  " + p, 15, Font.PLAIN, Color.WHITE);
            b.setBorder(UI.pad(6, 0, 6, 0));
            left.add(UI.left(b));
        }
        left.add(Box.createVerticalGlue());
        return left;
    }

    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(UI.BG);

        UI.RoundedPanel card = new UI.RoundedPanel(24, UI.CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(UI.pad(30, 32, 30, 32));
        card.setPreferredSize(new Dimension(360, 340));

        nameField.setFont(UI.font(Font.PLAIN, 16));
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UI.BORDER, 1, true), UI.pad(10, 12, 10, 12)));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        UI.RoundedButton go = new UI.RoundedButton("Continue", UI.PRIMARY);
        go.setAlignmentX(Component.LEFT_ALIGNMENT);
        go.addActionListener(e -> proceed());
        nameField.addActionListener(e -> proceed());

        card.add(UI.left(UI.label("Welcome!", 28, Font.BOLD, UI.TEXT)));
        card.add(Box.createVerticalStrut(6));
        card.add(UI.left(UI.label("Enter your name to begin", 14, Font.PLAIN, UI.MUTED)));
        card.add(Box.createVerticalStrut(26));
        card.add(UI.left(UI.label("Candidate name", 13, Font.BOLD, UI.TEXT)));
        card.add(Box.createVerticalStrut(8));
        card.add(nameField);
        card.add(Box.createVerticalStrut(6));
        card.add(UI.left(errorLabel));
        card.add(Box.createVerticalStrut(16));
        card.add(go);

        right.add(card);
        return right;
    }

    private void proceed() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Please enter your name to continue.");
            return;
        }
        dispose();
        new DashboardFrame(name).setVisible(true);
    }
}
