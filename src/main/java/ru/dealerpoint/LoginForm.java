package ru.dealerpoint;

import ru.dealerpoint.redmine.Api;
import ru.dealerpoint.redmine.Session;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.*;

interface ILoginFormListener {
    void onSuccessLogin(String redmineUrl, String redmineApiKey, Session session);
}

public class LoginForm extends JDialog {
    private JPanel contentPane;
    private JButton buttonLogin;
    private JButton buttonClose;
    private JTextField textFieldUrl;
    private JTextField textFieldKey;
    private JLabel versionLabel;
    private final java.util.List<ILoginFormListener> listeners = new ArrayList<ILoginFormListener>();

    public LoginForm(String redmineUrl, String redmineApiKey) {
        setTitle("Login");
        textFieldUrl.setText(redmineUrl);
        textFieldKey.setText(redmineApiKey);
        setContentPane(contentPane);
        setResizable(false);
        setModal(true);
        versionLabel.setText("Version: " + Api.getVersion());
        pack();
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(buttonLogin);
        buttonLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onLogin();
            }
        });

        buttonClose.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        });

        // call onClose() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
        });

        // call onClose() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onClose();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

    }

    private void onLogin() {
        if (textFieldUrl.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Redmine URL is required!", "Field is required", JOptionPane.INFORMATION_MESSAGE);
        } else if (textFieldKey.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "User API key is required!", "Field is required", JOptionPane.INFORMATION_MESSAGE);
        } else {
            String url = textFieldUrl.getText();
            String key = textFieldKey.getText();
            Api api = new Api(url, key);
            try {
                Session session = api.createSession();
                for (ILoginFormListener listener : listeners) {
                    listener.onSuccessLogin(url, key, session);
                }
                dispose();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), getTitle(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onClose() {
        System.exit(0);
    }
    public void addLoginFormListener(ILoginFormListener l) { listeners.add(l); }
    public void removeLoginFormListener(ILoginFormListener l) { listeners.remove(l); }
}
