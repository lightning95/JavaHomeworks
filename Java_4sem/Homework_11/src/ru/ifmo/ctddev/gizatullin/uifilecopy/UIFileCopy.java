package ru.ifmo.ctddev.gizatullin.uifilecopy;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 5/21/15.
 */
public class UIFileCopy extends JFrame {
    public abstract class BasicAction extends AbstractAction {
        public BasicAction(String name, String description, int mnemonicKey, KeyStroke acceleratorKey) {
            super(name);
            putValue(SHORT_DESCRIPTION, description);
            putValue(MNEMONIC_KEY, mnemonicKey);
            putValue(ACCELERATOR_KEY, acceleratorKey);
        }
    }

    class Transfer {
        final JLabel elapsedTimeLabel;
        final JLabel estimatingTimeLabel;
        final JLabel averageSpeedLabel;
        final JLabel currentSpeedLabel;
        final JFrame frame;
        final JLabel currentFileLabel;
        final JProgressBar currentFileProgressBar;
        final JProgressBar progressBar;
        final JPanel panel;

        Transfer(JLabel elapsedTimeLabel, JLabel estimatingTimeLabel,
                 JLabel averageSpeedLabel, JLabel currentSpeedLabel,
                 JLabel currentFileLabel, JProgressBar currentFileProgressBar,
                 JProgressBar progressBar, JPanel rootPanel, JFrame frame) {
            this.elapsedTimeLabel = elapsedTimeLabel;
            this.estimatingTimeLabel = estimatingTimeLabel;
            this.averageSpeedLabel = averageSpeedLabel;
            this.currentSpeedLabel = currentSpeedLabel;
            this.frame = frame;
            this.currentFileLabel = currentFileLabel;
            this.currentFileProgressBar = currentFileProgressBar;
            this.progressBar = progressBar;
            panel = rootPanel;
        }
    }

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

    private JLabel elapsedTimeLabel;
    private JLabel estimatingTimeLabel;
    private JLabel averageSpeedLabel;
    private JLabel currentSpeedLabel;
    private JProgressBar progressBar;
    private JButton cancelButton;
    private JPanel rootPanel;
    private JProgressBar currentFileProgressBar;
    private JLabel currentFileLabel;
    private JButton detailsButton;
    private JLabel goalLabel;
    private CopyFiles copyFiles2;
    private boolean detailsOn;

    public UIFileCopy(Path source, Path destination) {
        super("UI");

        setContentPane(rootPanel);
        setMinimumSize(rootPanel.getMinimumSize());
        setLocationRelativeTo(null);

        goalLabel.setText("<html><b>From:</b> " + source + "<br><b>To:</b> " + destination + "</html>");
        elapsedTimeLabel.setText("Elapsed time: 0 minutes");
        estimatingTimeLabel.setText("Estimating time: computing...");
        averageSpeedLabel.setText("Average speed: 0 mb/s");
        currentSpeedLabel.setText("Current speed: 0 mb/s");
        currentFileLabel.setText("Current file: Unknown");

        detailsOn = false;
        Action detailsAction = new BasicAction("Details", "Show details",
                KeyEvent.VK_D, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)) {
            public void actionPerformed(ActionEvent e) {
                currentFileLabel.setVisible(!detailsOn);
                currentFileProgressBar.setVisible(!detailsOn);
                currentSpeedLabel.setVisible(!detailsOn);
                detailsOn ^= true;
            }
        };
        Action cancelAction = new BasicAction("Cancel", "Cancel the operation",
                KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)) {
            public void actionPerformed(ActionEvent e) {
                copyFiles2.cancel(true);
                setVisible(false);
                dispose();
            }
        };
        cancelButton.setAction(cancelAction);
        detailsButton.setAction(detailsAction);

        pack();
        setVisible(true);

        copyFiles2 = new CopyFiles(source, destination, true, true,
                new Transfer(elapsedTimeLabel, estimatingTimeLabel, averageSpeedLabel, currentSpeedLabel,
                        currentFileLabel, currentFileProgressBar, progressBar, rootPanel, this));

        copyFiles2.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        copyFiles2.execute();
    }

    public static final String USAGE = "Usage: <source> <destination>";

    public static void main(String[] args) {
        /*if (args == null || args.length != 2 || Arrays.stream(args).anyMatch(Predicate.isEqual(null))) {
            System.out.println(USAGE);
            return;
        }*/
        args = new String[]{"/home/lightning95/Studings.Codes", "/home/lightning95/javatmp"};
        final String[] finalArgs1 = args;
        SwingUtilities.invokeLater(() -> new UIFileCopy(Paths.get(finalArgs1[0]), Paths.get(finalArgs1[1])));
//        SwingUtilities.invokeLater(() -> new UI(Paths.get(args[0]), Paths.get(args[1])));
    }
}
