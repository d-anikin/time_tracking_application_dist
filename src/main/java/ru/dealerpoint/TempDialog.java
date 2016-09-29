package ru.dealerpoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TempDialog extends JDialog {
    public static final int TIMEOUT = 0;
    public static final int RESUME = 1;
    public static final int STOP = 2;
    public static final int REMEMBER = 3;
    public static final int KIND_WORK_IN = 0;
    public static final int KIND_WORK_OUT = 1;
    public static final int KIND_REST = 2;

    private JPanel contentPane;
    private JButton buttonResume;
    private JButton buttonRemember;
    private JButton buttonStop;
    private JLabel labelIcon;
    private JLabel labelMessage;
    private JLabel labelTimer;
    private Timer timeoutTimer;
    private Integer result = null;
    private int timerTicks = 20;

    static public Integer showDialog(String message, int kind) {
        TempDialog dialog = new TempDialog(message, kind, REMEMBER);
        dialog.setVisible(true);
        return dialog.result;
    }

    public TempDialog(String message, int kind, int defaultResult) {
        labelMessage.setText("<html><body style='width:200px'>" + message + "</body></html>");

        String image_path;
        if (kind == KIND_REST)
            image_path = "/restaurant.png";
        else if (kind == KIND_WORK_OUT)
            image_path = "/shutdown.png";
        else if (kind == KIND_WORK_IN) {
            image_path = "/work.png";
            buttonResume.setVisible(false);
            buttonStop.setVisible(false);
        } else {
            image_path = "/info_big.png";
            buttonResume.setVisible(false);
            buttonStop.setVisible(false);
        }
        ImageIcon image = new ImageIcon(getClass().getResource(image_path));
        labelIcon.setIcon(image);
        result = defaultResult;
        setContentPane(contentPane);
        setAlwaysOnTop(true);
        setResizable(false);
        setUndecorated(true);
        setModal(true);
        getRootPane().setDefaultButton(buttonResume);
        pack();

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int) rect.getMaxX() - getWidth();
        int y = (int) rect.getMinY();
        setLocation(x, y);

        buttonResume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onResume();
            }
        });
        buttonStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStop();
            }
        });

        buttonRemember.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRemeber();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onRemeber();
            }
        });

        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onRemeber();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        timeoutTimer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timerTicks--;
                if (timerTicks < 10)
                    labelTimer.setText(Integer.toString(timerTicks));
                if (timerTicks <= 0) {
                    timeoutTimer.stop();
                    onTimeout();
                }
            }
        });
        timeoutTimer.start();
    }

    private void onClose() {
        timeoutTimer.stop();
        setVisible(false);
        dispose();
    }

    private void onTimeout() {
        result = TIMEOUT;
        onClose();
    }

    private void onResume() {
        result = RESUME;
        onClose();
    }

    private void onRemeber() {
        result = REMEMBER;
        onClose();
    }

    private void onStop() {
        result = STOP;
        onClose();
    }
}
