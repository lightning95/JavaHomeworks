package ru.ifmo.ctddev.gizatullin.uifilecopy;

import swing.common.BasicAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 5/12/15.
 */
public class UIFileCopy {
    public static final String USAGE = "Usage: source destination ";
    private CopyFiles copyFiles;

    private UIFileCopy(Path source, Path destination) {
        JFrame frame = new JFrame("UIFileCopy");
        frame.setJMenuBar(createMainMenu(frame));
        frame.setPreferredSize(new Dimension(600, 300));
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        frame.getContentPane().add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel elapsedTimeLabel = new JLabel();
        elapsedTimeLabel.setText("Elapsed time: 0 minute(s)");

        JLabel estimatingTimeLabel = new JLabel();
        estimatingTimeLabel.setText("Estimating time: computing...");

        JLabel averageSpeedLabel = new JLabel();
        averageSpeedLabel.setText("Average speed: 0 mb/s");

        JLabel currentSpeedLabel = new JLabel();
        currentSpeedLabel.setText("Current speed: 0 mb/s");

        Action cancelAction = new BasicAction("Cancel",
                "Perform BasicAction", "/toolbarButtonGraphics/general/Help24.gif",
                KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)) {
            public void actionPerformed(ActionEvent e) {
                copyFiles.cancel(true);
                JOptionPane.showMessageDialog(null, "Copying is canceled");
            }
        };
        JButton cancelButton = new JButton(cancelAction);
        cancelButton.setPreferredSize(new Dimension(50, 30));

        JProgressBar progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        progressBar.setPreferredSize(new Dimension(600, 30));
        progressBar.setValue(0);

        panel.add(elapsedTimeLabel);
        panel.add(estimatingTimeLabel);
        panel.add(averageSpeedLabel);
        panel.add(currentSpeedLabel);
        panel.add(progressBar);
        panel.add(cancelButton);

        frame.pack();
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        AtomicLong finalSize = new AtomicLong(0);
        AtomicLong curSize = new AtomicLong(0);

        long startTime = System.currentTimeMillis();
        Timer timer = new Timer(1000, e -> {
            long cur = (System.currentTimeMillis() - startTime) / 1000;
            elapsedTimeLabel.setText("Elapsed time: " + (cur > 59 ? cur / 60 + " minute(s)" : cur + " second(s)"));
        });

        final long[] lasts = {0};

        Timer timer2 = new Timer(1000, e -> {
            long lLastSize = lasts[0];
            long lCurSize = curSize.get() / (1 << 20);

            long ave = lCurSize / ((System.currentTimeMillis() - startTime) / 1000);
            long est = ave > 0 ? (finalSize.get() - lCurSize) / ave : 0;
            long cur = lCurSize - lLastSize == 0 ? ave : lCurSize - lLastSize;

            estimatingTimeLabel.setText("Estimating time: " + (est > 59 ? est / 60 + " minute(s)" : est + " second(s)"));
            averageSpeedLabel.setText("Average speed: " + ave + " mb/s");
            currentSpeedLabel.setText("Current speed: " + cur + " mb/s");
            progressBar.setValue((int) (lCurSize * 100 / finalSize.get()));

            lasts[0] = lCurSize;
        });

        copyFiles = new CopyFiles(source, destination, false, true,
                finalSize, curSize, timer2);

        copyFiles.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        copyFiles.execute();
        timer.start();
        try {
            copyFiles.get();
            timer.stop();
        } catch (InterruptedException | ExecutionException | CancellationException ignored) {
        }

        timer.stop();
        timer2.stop();
        System.out.println("here");
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
        args = new String[]{"/home/lightning95/Studings.Codes/", "/home/lightning95/javatmp"};
        new UIFileCopy(Paths.get(args[0]), Paths.get(args[1]));
    }
}
