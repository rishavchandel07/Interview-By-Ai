package gui;

import service.FileService;
import utils.UI;

import javax.swing.*;
import java.awt.*;

/** Pop-up window that shows all saved interviews from data/interview_history.txt. */
public class HistoryDialog extends JDialog {

    public HistoryDialog(Frame owner) {
        super(owner, "Interview History", true);
        setSize(780, 560);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UI.BG);
        setLayout(new BorderLayout(0, 12));

        JLabel title = UI.label("Previous Interviews", 22, Font.BOLD, UI.TEXT);
        title.setBorder(UI.pad(16, 20, 0, 20));
        add(title, BorderLayout.NORTH);

        JTextArea area = new JTextArea(new FileService().loadHistory());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setForeground(UI.TEXT);
        area.setBorder(UI.pad(12, 14, 12, 14));
        area.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createCompoundBorder(UI.pad(0, 20, 0, 20),
                BorderFactory.createLineBorder(UI.BORDER)));
        add(scroll, BorderLayout.CENTER);

        UI.RoundedButton close = new UI.RoundedButton("Close", UI.PRIMARY);
        close.addActionListener(e -> dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 12));
        bottom.setOpaque(false);
        bottom.add(close);
        add(bottom, BorderLayout.SOUTH);
    }
}
