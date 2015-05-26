package ru.ifmo.ctddev.gizatullin.uifilecopy;

import swing.common.BasicAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 5/12/15.
 */
public class UIFileCopy {
    public static final String USAGE = "Usage: source destination ";
    private CopyFiles copyFiles;
    private static final Dimension FRAME_START_DIMENSION = new Dimension(400, 300);
    private static final Dimension PROGRESSBAR_START_DIMENSION = new Dimension(300, 30);
    private static final int TIMER_DELAY = 1000;

    public static String timeConvert(long time) {
        String res;
        if (time < 60) {
            res = time + " second" + (time > 1 ? "s" : "");
        } else if (time < 3600) {
            res = time / 60 + " minute" + (time / 60 > 1 ? "s" : "");
        } else {
            res = time / 3600 + " hour" + (time / 3600 > 1 ? "s" : "");
        }
        return res;
    }

    private UIFileCopy(Path source, Path destination) {
        JFrame frame = new JFrame("UIFileCopy");
        frame.setJMenuBar(createMainMenu(frame));
//
        frame.setPreferredSize(FRAME_START_DIMENSION);
        frame.setMinimumSize(FRAME_START_DIMENSION);
//
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//
        JPanel panel = new JPanel();
        frame.getContentPane().add(panel);

        panel.setLayout(new GridLayout(6, 1, 10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel elapsedTimeLabel = new JLabel();
        elapsedTimeLabel.setText("Elapsed time: 0 minutes");

        JLabel estimatingTimeLabel = new JLabel();
        estimatingTimeLabel.setText("Estimating time: computing...");

        JLabel averageSpeedLabel = new JLabel();
        averageSpeedLabel.setText("Average speed: 0 mb/s");

        JLabel currentSpeedLabel = new JLabel();
        currentSpeedLabel.setText("Current speed: 0 mb/s");

        Action cancelAction = new BasicAction("Cancel",
                "Cancel the operation", "/toolbarButtonGraphics/general/Help24.gif",
                KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)) {
            public void actionPerformed(ActionEvent e) {
                copyFiles.cancel(true);
                JOptionPane.showMessageDialog(null, "Copying is canceled");
            }
        };
        JButton cancelButton = new JButton(cancelAction);
        cancelButton.setPreferredSize(new Dimension(50, 30));
        cancelButton.setMaximumSize(new Dimension(50, 30));

        JProgressBar progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(PROGRESSBAR_START_DIMENSION);
        progressBar.setMinimumSize(PROGRESSBAR_START_DIMENSION);
        progressBar.setValue(0);
        
        panel.add(elapsedTimeLabel);
        panel.add(estimatingTimeLabel);
        panel.add(averageSpeedLabel);
        panel.add(currentSpeedLabel);
        panel.add(progressBar);

        JPanel buttonPanel = new JPanel();
//        buttonPanel.setLayout(new FlowLayout());
        cancelButton.setMaximumSize(new Dimension(70, 30));

        panel.add(buttonPanel.add(cancelButton));

        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        AtomicLong finalSize = new AtomicLong(0);
        AtomicLong curSize = new AtomicLong(0);

        long startTime = System.currentTimeMillis();
        Timer timer = new Timer(TIMER_DELAY, e -> {
            long cur = (System.currentTimeMillis() - startTime) / TIMER_DELAY;
            elapsedTimeLabel.setText("Elapsed time: " + timeConvert(cur));
        });

        final long[] lasts = {0};

        Timer timer2 = new Timer(TIMER_DELAY, e -> {
            long lLastSize = lasts[0];
            long lCurSize = curSize.get() / (1 << 20);

            long ave = lCurSize / ((System.currentTimeMillis() - startTime) / TIMER_DELAY);
            long est = Math.max(ave > 0 ? (finalSize.get() / (1 << 20) - lCurSize) / ave : 0, 0);
            long cur = lCurSize - lLastSize == 0 ? ave : lCurSize - lLastSize;

            estimatingTimeLabel.setText("Estimating time: " + timeConvert(est));
            averageSpeedLabel.setText("Average speed: " + ave + " mb/s");
            currentSpeedLabel.setText("Current speed: " + cur + " mb/s");

            lasts[0] = lCurSize;
        });

        copyFiles = new CopyFiles(source, destination, false, true, finalSize, curSize, timer2);

        copyFiles.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        timer.start();
        copyFiles.execute();

        try {
            copyFiles.get();
        } catch (InterruptedException | ExecutionException | CancellationException ignored) {
        }

        timer.stop();
        timer2.stop();

        frame.setVisible(false);
        frame.dispose();
    }

    private JMenuBar createMainMenu(JFrame frame) {
        JMenuBar menu = new JMenuBar();
        menu.add(swing.common.Toolkit.createLookAndFeelMenu(frame, KeyEvent.VK_L));
        return menu;
    }

    public static void main(String[] args) {
        /*if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Predicate.isEqual(null))) {
            System.out.println(USAGE);
            return;
        }*/
        args = new String[]{"/home/lightning95/Downloads/", "/home/lightning95/javatmp"};
        new UIFileCopy(Paths.get(args[0]), Paths.get(args[1]));
    }
}