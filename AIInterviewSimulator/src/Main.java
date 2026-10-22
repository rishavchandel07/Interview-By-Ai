import gui.WelcomeFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point of the AI Interview Simulator. */
public class Main {
    public static void main(String[] args) {
        try {
            // Same look on Windows, Mac and Linux
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // default look and feel will be used
        }
        SwingUtilities.invokeLater(() -> new WelcomeFrame().setVisible(true));
    }
}
