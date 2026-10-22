package utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Shared colours, fonts and custom-painted components so every screen looks modern. */
public final class UI {
    public static final Color PRIMARY = new Color(0x4F46E5);
    public static final Color PRIMARY_DARK = new Color(0x3730A3);
    public static final Color PURPLE = new Color(0x7C3AED);
    public static final Color SUCCESS = new Color(0x10B981);
    public static final Color WARNING = new Color(0xF59E0B);
    public static final Color DANGER = new Color(0xEF4444);
    public static final Color BG = new Color(0xF3F4F8);
    public static final Color CARD = Color.WHITE;
    public static final Color TEXT = new Color(0x1F2937);
    public static final Color MUTED = new Color(0x6B7280);
    public static final Color BORDER = new Color(0xE5E7EB);

    private UI() { }

    public static Font font(int style, int size) {
        return new Font("SansSerif", style, size);
    }

    public static JLabel label(String text, int size, int style, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    public static <T extends JComponent> T left(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    public static EmptyBorder pad(int top, int left, int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }

    public static JTextArea textArea(int rows, boolean editable, int size) {
        JTextArea t = new JTextArea(rows, 30);
        t.setLineWrap(true);
        t.setWrapStyleWord(true);
        t.setEditable(editable);
        t.setFont(font(Font.PLAIN, size));
        t.setForeground(TEXT);
        t.setBorder(pad(8, 10, 8, 10));
        return t;
    }

    public static void setupFrame(JFrame f, String title, int w, int h) {
        f.setTitle(title);
        f.setSize(w, h);
        f.setLocationRelativeTo(null);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.getContentPane().setBackground(BG);
    }

    public static RoundedPanel statCard(String title, String value, Color accent) {
        RoundedPanel p = new RoundedPanel(18, CARD);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(pad(16, 22, 16, 22));
        p.add(left(label(value, 30, Font.BOLD, accent)));
        p.add(left(label(title, 13, Font.PLAIN, MUTED)));
        return p;
    }

    /** White card with rounded corners and a soft shadow. */
    public static class RoundedPanel extends JPanel {
        private final int arc;
        private final Color fill;

        public RoundedPanel(int arc, Color fill) {
            this.arc = arc;
            this.fill = fill;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 0, 0, 18));
            g2.fillRoundRect(2, 3, getWidth() - 4, getHeight() - 4, arc, arc);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 4, arc, arc);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, getWidth() - 4, getHeight() - 5, arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Flat rounded button with hover effect. */
    public static class RoundedButton extends JButton {
        private final Color base;
        private boolean hover = false;

        public RoundedButton(String text, Color base) {
            super(text);
            this.base = base;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(font(Font.BOLD, 14));
            setBorder(pad(11, 24, 11, 24));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Color c = !isEnabled() ? new Color(0xD1D5DB) : (hover ? base.darker() : base);
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.setColor(Color.WHITE);
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            g2.dispose();
        }
    }

    /** Selectable card (used for roles and difficulty levels). */
    public static class ChoiceButton extends JToggleButton {
        private final String title;
        private final String subtitle;

        public ChoiceButton(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setPreferredSize(new Dimension(180, 84));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean sel = isSelected();
            g2.setColor(sel ? PRIMARY : Color.WHITE);
            g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(sel ? PRIMARY_DARK : BORDER);
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 16, 16);
            g2.setFont(font(Font.BOLD, 15));
            g2.setColor(sel ? Color.WHITE : TEXT);
            g2.drawString(title, 16, 34);
            g2.setFont(font(Font.PLAIN, 12));
            g2.setColor(sel ? new Color(0xE0E7FF) : MUTED);
            g2.drawString(subtitle, 16, 58);
            g2.dispose();
        }
    }
}
