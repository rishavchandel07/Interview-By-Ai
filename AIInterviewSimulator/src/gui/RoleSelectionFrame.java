package gui;

import model.Candidate;
import model.Difficulty;
import model.Role;
import service.EvaluationService;
import service.FileService;
import service.InterviewService;
import service.QuestionService;
import utils.UI;

import javax.swing.*;
import java.awt.*;

/** Screen 3: choose the job role and the difficulty. */
public class RoleSelectionFrame extends JFrame {
    private final String candidateName;
    private final UI.ChoiceButton[] roleButtons = new UI.ChoiceButton[Role.values().length];
    private final UI.ChoiceButton[] diffButtons = new UI.ChoiceButton[Difficulty.values().length];

    public RoleSelectionFrame(String candidateName) {
        this.candidateName = candidateName;
        UI.setupFrame(this, "Choose Interview - AI Interview Simulator", 1060, 640);
        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(UI.PRIMARY);
        header.setBorder(UI.pad(22, 34, 22, 34));
        header.add(UI.left(UI.label("Set up your interview", 26, Font.BOLD, Color.WHITE)));
        header.add(Box.createVerticalStrut(4));
        header.add(UI.left(UI.label("Pick a role and a difficulty level, " + candidateName + ".",
                14, Font.PLAIN, new Color(0xE0E7FF))));
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(UI.BG);
        body.setBorder(UI.pad(22, 34, 10, 34));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 16, 0);

        // ---- Step 1: role ----
        UI.RoundedPanel roleCard = new UI.RoundedPanel(18, UI.CARD);
        roleCard.setLayout(new BorderLayout(0, 12));
        roleCard.setBorder(UI.pad(18, 20, 20, 20));
        roleCard.add(UI.label("1.  Select interview role", 17, Font.BOLD, UI.TEXT), BorderLayout.NORTH);
        JPanel roleRow = new JPanel(new GridLayout(1, roleButtons.length, 12, 0));
        roleRow.setOpaque(false);
        ButtonGroup roleGroup = new ButtonGroup();
        Role[] roles = Role.values();
        for (int i = 0; i < roles.length; i++) {
            roleButtons[i] = new UI.ChoiceButton(roles[i].getDisplayName(), roles[i].getDescription());
            roleGroup.add(roleButtons[i]);
            roleRow.add(roleButtons[i]);
        }
        roleButtons[0].setSelected(true);
        roleCard.add(roleRow, BorderLayout.CENTER);
        gc.gridy = 0;
        body.add(roleCard, gc);

        // ---- Step 2: difficulty ----
        UI.RoundedPanel diffCard = new UI.RoundedPanel(18, UI.CARD);
        diffCard.setLayout(new BorderLayout(0, 12));
        diffCard.setBorder(UI.pad(18, 20, 20, 20));
        diffCard.add(UI.label("2.  Select difficulty", 17, Font.BOLD, UI.TEXT), BorderLayout.NORTH);
        JPanel diffRow = new JPanel(new GridLayout(1, diffButtons.length, 12, 0));
        diffRow.setOpaque(false);
        ButtonGroup diffGroup = new ButtonGroup();
        Difficulty[] levels = Difficulty.values();
        for (int i = 0; i < levels.length; i++) {
            diffButtons[i] = new UI.ChoiceButton(levels[i].getDisplayName(),
                    levels[i].getTimeLimitSeconds() + " seconds per question");
            diffGroup.add(diffButtons[i]);
            diffRow.add(diffButtons[i]);
        }
        diffButtons[0].setSelected(true);
        diffCard.add(diffRow, BorderLayout.CENTER);
        diffCard.add(UI.label("The interview is adaptive: a score of 8+ gives harder questions, 4 or less gives easier ones.",
                12, Font.PLAIN, UI.MUTED), BorderLayout.SOUTH);
        gc.gridy = 1;
        gc.weighty = 1;
        gc.anchor = GridBagConstraints.NORTH;
        body.add(diffCard, gc);
        add(body, BorderLayout.CENTER);

        // ---- Buttons ----
        UI.RoundedButton back = new UI.RoundedButton("Back", new Color(0x6B7280));
        UI.RoundedButton start = new UI.RoundedButton("Start Interview", UI.SUCCESS);
        back.addActionListener(e -> {
            dispose();
            new DashboardFrame(candidateName).setVisible(true);
        });
        start.addActionListener(e -> startInterview());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottom.setBackground(UI.BG);
        bottom.setBorder(UI.pad(0, 0, 8, 22));
        bottom.add(back);
        bottom.add(start);
        add(bottom, BorderLayout.SOUTH);
    }

    private void startInterview() {
        Role role = Role.values()[selectedIndex(roleButtons)];
        Difficulty difficulty = Difficulty.values()[selectedIndex(diffButtons)];

        QuestionService questionService = new QuestionService(new FileService());
        if (questionService.countFor(role) == 0) {
            JOptionPane.showMessageDialog(this, "No questions found for " + role + ".\nCheck data/questions.txt",
                    "No questions", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Candidate candidate = new Candidate(candidateName, role, difficulty);
        InterviewService service = new InterviewService(candidate, questionService, new EvaluationService());
        service.start();
        dispose();
        new InterviewFrame(service).setVisible(true);
    }

    private int selectedIndex(UI.ChoiceButton[] buttons) {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isSelected()) return i;
        }
        return 0;
    }
}
