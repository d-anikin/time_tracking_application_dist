package ru.dealerpoint;

import ru.dealerpoint.redmine.Api;
import ru.dealerpoint.redmine.Details;

import javax.swing.*;
import java.awt.event.*;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginForm extends JDialog {
    private JPanel contentPane;
    private JButton buttonLogin;
    private JButton buttonClose;
    private JTextField textFieldUrl;
    private JTextField textFieldKey;
    private JLabel versionLabel;
    private Preferences userPrefs;

    public LoginForm(Preferences userPrefs) {
        this.userPrefs = userPrefs;
        setTitle("Login");
        setToPreferred();
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

    private boolean isUrlValid() {
//        Pattern pattern = Pattern.compile("^https?://.*?\\..*?$");
//        Matcher matcher = pattern.matcher(textFieldUrl.getText());
//        return matcher.find();
        return !textFieldUrl.getText().isEmpty();
    }

    private void onLogin() {
        if (textFieldUrl.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Redmine URL is required!", "Field is required", JOptionPane.INFORMATION_MESSAGE);
        } else if (!isUrlValid()) {
            JOptionPane.showMessageDialog(this, "Redmine URL is invalid!", "Field is invalid", JOptionPane.INFORMATION_MESSAGE);
        } else if (textFieldKey.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "User API key is required!", "Field is required", JOptionPane.INFORMATION_MESSAGE);
        } else {
            Api api = new Api(textFieldUrl.getText(), textFieldKey.getText());
            Details details = api.getDetails();
            if (details == null) {
                JOptionPane.showMessageDialog(this, "Wrong url or api key!", "Login", JOptionPane.ERROR_MESSAGE);
            } else if (details.getVersion().equals(Api.getVersion())) {
                userPrefs.put("redmine_url", textFieldUrl.getText());
                userPrefs.put("redmine_api_key", textFieldKey.getText());
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please update your application to continue!", "Login", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void setToPreferred() {
        textFieldUrl.setText(userPrefs.get("redmine_url", ""));
        textFieldKey.setText(userPrefs.get("redmine_api_key", ""));
    }

    private void onClose() {
        System.exit(0);
    }

}
