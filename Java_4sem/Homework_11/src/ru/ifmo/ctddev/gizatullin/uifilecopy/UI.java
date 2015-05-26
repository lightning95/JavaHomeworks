package ru.ifmo.ctddev.gizatullin.uifilecopy;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * @author Aydar Gizatullin a.k.a. lightning95, aydar.gizatullin@gmail.com
 *         Created on 5/21/15.
 */
public class UI extends JFrame {
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

        Transfer(JLabel elapsedTimeLabel, JLabel estimatingTimeLabel, JLabel averageSpeedLabel, JLabel currentSpeedLabel) {
            this.elapsedTimeLabel = elapsedTimeLabel;
            this.estimatingTimeLabel = estimatingTimeLabel;
            this.averageSpeedLabel = averageSpeedLabel;
            this.currentSpeedLabel = currentSpeedLabel;
        }
    }

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

    private JLabel elapsedTimeLabel;
    private JLabel estimatingTimeLabel;
    private JLabel averageSpeedLabel;
    private JLabel currentSpeedLabel;
    private JProgressBar progressBar;
    private JButton cancelButton;
    private JPanel panel;
    private CopyFiles2 copyFiles2;

    UI(Path source, Path destination) {
        super("UI");

        setContentPane(panel);
        setMinimumSize(panel.getMinimumSize());

        elapsedTimeLabel.setText("Elapsed time: 0 minutes");
        estimatingTimeLabel.setText("Estimating time: computing...");
        averageSpeedLabel.setText("Average speed: 0 mb/s");
        currentSpeedLabel.setText("Current speed: 0 mb/s");

        Action cancelAction = new BasicAction("Cancel", "Cancel the operation",
                KeyEvent.VK_C, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)) {
            public void actionPerformed(ActionEvent e) {
                copyFiles2.cancel(true);
                setVisible(false);
                dispose();
            }
        };
        cancelButton.setAction(cancelAction);

        pack();
        setVisible(true);

        copyFiles2 = new CopyFiles2(source, destination, false, true,
                new Transfer(elapsedTimeLabel, estimatingTimeLabel, averageSpeedLabel, currentSpeedLabel));

        copyFiles2.addPropertyChangeListener(evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                progressBar.setValue((Integer) evt.getNewValue());
            }
        });

        copyFiles2.execute();
//        try {
//            copyFiles2.get();
//        } catch (InterruptedException | ExecutionException | CancellationException ignored) {
//        }
    }

    public static void main(String[] args) {
        args = new String[]{"/home/lightning95/Downloads/", "/home/lightning95/javatmp"};
        final String[] finalArgs = args;
        SwingUtilities.invokeLater(() -> new UI(Paths.get(finalArgs[0]), Paths.get(finalArgs[1])));
    }
}
