package utils;

import javax.swing.SwingUtilities;

/**
 * Countdown timer that runs on its OWN THREAD (multithreading).
 * Every second it calls the listener on the Swing thread, so the GUI can
 * update safely.
 */
public class TimerManager {

    /** Callback interface used by the GUI. */
    public interface TimerListener {
        void onTick(int secondsRemaining);
        void onTimeUp();
    }

    private final TimerListener listener;
    private Thread worker;
    private volatile boolean running = false;
    private volatile int generation = 0;   // protects against old timers firing late

    public TimerManager(TimerListener listener) {
        this.listener = listener;
    }

    public void start(int seconds) {
        stop();
        running = true;
        final int gen = ++generation;
        worker = new Thread(() -> {
            int remaining = seconds;
            while (running && gen == generation && remaining > 0) {
                final int r = remaining;
                SwingUtilities.invokeLater(() -> {
                    if (running && gen == generation) listener.onTick(r);
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;                       // timer was stopped
                }
                remaining--;
            }
            if (running && gen == generation) {
                SwingUtilities.invokeLater(() -> {
                    if (running && gen == generation) {
                        running = false;
                        listener.onTick(0);
                        listener.onTimeUp();
                    }
                });
            }
        }, "question-timer");
        worker.setDaemon(true);
        worker.start();
    }

    public void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
            worker = null;
        }
    }

    /** Formats seconds as mm:ss. */
    public static String format(long totalSeconds) {
        long m = totalSeconds / 60;
        long s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }
}
