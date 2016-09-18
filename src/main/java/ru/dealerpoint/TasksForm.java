package ru.dealerpoint;

import ru.dealerpoint.redmine.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.prefs.Preferences;

public class TasksForm extends JFrame implements ILoginFormListener {
    private JTable issuesTable;
    private JPanel contentPane;
    private JButton startButton;
    private JButton logoutButton;
    private JPanel taskListPanel;
    private JPanel currentTaskPanel;
    private JPanel activeIssuePanel;
    private JComboBox cbActivities;
    private JButton viewButton;
    private JLabel spentHoursLabel;
    private JLabel issueIdLabel;
    private JButton stopButton;

    private Preferences userPrefs;
    private Api api;
    private ArrayList<Item> queries;
    private ArrayList<Issue> issues;
    private int pageIndex = 1;
    private Session userSession;
    private Issue selectedIssue;
    private ArrayList<Item> activities;
    private TimeEntry activeTimeEntry = null;
    private IssueTableModel issueTableModel;
    private java.util.Timer workerTimer;

    public TasksForm() {
        super("Redmine Time Tracker");
        setContentPane(contentPane);
//        setResizable(false);
        pack();
        setMinimumSize(new Dimension(320, 280));
        setSize(new Dimension(320, 280));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        userPrefs = Preferences.userRoot().node("redmine_time_tracker_app");
        issueTableModel = new IssueTableModel();
        issuesTable.setModel(issueTableModel);
        issuesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        issuesTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        issuesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        issuesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                int index = issuesTable.getSelectedRow();
                if (index > -1) {
                    selectedIssue = issues.get(issuesTable.getSelectedRow());
                    startButton.setText("Start #" + selectedIssue.getId().toString());
                    startButton.setEnabled(true);
                } else {
                    startButton.setText("Start");
                    startButton.setEnabled(false);
                }
            }
        });
        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onLogout();
            }
        });
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                onStartTimeEntry();
            }
        });
        issuesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
            if (mouseEvent.getClickCount() == 2) {
                onStartTimeEntry();
            }
            }
        });
        currentTaskPanel.setVisible(false);
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                onStopTimeEntry();
            }
        });
        onActivate();
        viewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if (isActiveIssue()) {
                    String url = api.getRedmineUrl() + "/issues/" + activeTimeEntry.getIssue().getId().toString();
                    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(new URI(url));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        cbActivities.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                onUpdateTimeEntry();
            }
        });
    }

    private void initIssuesTable() {
        issuesTable.clearSelection();
        if (issueTableModel.getRowCount() > 0) {
            issuesTable.setRowSelectionInterval(0, 0);
            issuesTable.requestFocus();
        }
    }

    /* Setters and getters */
    private void setUserSession(Session session) {
        userSession = session;
        setTitle("Redmine Time Tracker - " + userSession.getUserName());
    }

    /* === */
    private void loadIssues() {
        try {
            issues = api.getIssues();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), getTitle(), JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        issueTableModel.setIssues(issues);
        initIssuesTable();
    }

    private void authorizeForm() {
        String url = userPrefs.get("redmine_url", "");
        String key = userPrefs.get("redmine_api_key", "");
        LoginForm dialog = new LoginForm(url, key);
        dialog.addLoginFormListener(this);
        dialog.setVisible(true);
    }

    private Long getActivitieId() {
        int index = cbActivities.getSelectedIndex();
        if (index > -1) {
            Item activitie = activities.get(index);
            return activitie.getId();
        } else {
            return null;
        }
    }

    private void setDefaultActivity() {
        if (activities != null) {
            for (int i = 0; i < activities.size(); i++) {
                if (activities.get(i).getDefault()) {
                    cbActivities.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    /* Helpers */
    private String formatHours(Float timeEntry) {
        int value = Math.round(timeEntry * 60);
        int hours = value / 60;
        int minutes = value % 60;
        return String.format("%02d:%02d", hours, minutes);
    }
    
    /* Events */
    private void onLogout() {
        userPrefs.remove("redmine_url");
        userPrefs.remove("redmine_api_key");
        System.exit(0);
    }

    private void onActivate() {
        String url = userPrefs.get("redmine_url", "");
        String key = userPrefs.get("redmine_api_key", "");
        if (url.isEmpty()) {
            authorizeForm();
        } else {
            this.api = new Api(url, key);
            try {
                setUserSession(api.createSession());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), getTitle(), JOptionPane.ERROR_MESSAGE);
                authorizeForm();
            }
        }

        activities = api.getActivities();
        cbActivities.setModel(new DefaultComboBoxModel(activities.toArray()));
        setDefaultActivity();

        loadIssues();
        setVisible(true);
        runTimers();
    }

    private void runTimers() {
        workerTimer = new java.util.Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    if (isActiveIssue()) {
                        activeTimeEntry.setActivityId(getActivitieId());
                        activeTimeEntry = api.updateWork(activeTimeEntry);
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                spentHoursLabel.setText(formatHours(activeTimeEntry.getHours()));
                            }
                        });
                    } else {
                        api.idle();
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            JOptionPane.showMessageDialog(null, e.getMessage(), getTitle(), JOptionPane.ERROR_MESSAGE);
                        }
                    });
                }
            }
        };
        workerTimer.schedule(timerTask, 60000, 60000);
    }

    public void onSuccessLogin(String redmineUrl, String redmineApiKey, Session session) {
        userPrefs.put("redmine_url", redmineUrl);
        userPrefs.put("redmine_api_key", redmineApiKey);
        setUserSession(session);
        this.api = new Api(redmineUrl, redmineApiKey, session);
    }

    private boolean isActiveIssue() {
        return activeTimeEntry != null;
    }

    private void onStartTimeEntry() {
        try {
            activeTimeEntry = api.startWork(selectedIssue.getId());
            issueIdLabel.setText("#" + selectedIssue.getId().toString());
            TitledBorder border = (TitledBorder) activeIssuePanel.getBorder();
            border.setTitle(selectedIssue.getSubject());
            spentHoursLabel.setText(formatHours(activeTimeEntry.getHours()));
            taskListPanel.setVisible(false);
            currentTaskPanel.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), getTitle(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void onUpdateTimeEntry() {
        try {
            if (isActiveIssue()) {
                activeTimeEntry.setActivityId(getActivitieId());
                activeTimeEntry = api.updateWork(activeTimeEntry);
                spentHoursLabel.setText(formatHours(activeTimeEntry.getHours()));
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, e.getMessage(), getTitle(), JOptionPane.ERROR_MESSAGE);
        }
    }


    private void onStopTimeEntry() {
        try {
            api.stopWork(activeTimeEntry);
            activeTimeEntry = null;
            loadIssues();
            currentTaskPanel.setVisible(false);
            taskListPanel.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), getTitle(), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public void onExit() {
        try {
            if (isActiveIssue())
                api.stopWork(activeTimeEntry);
            api.destroySession();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void main(String[] args) {
        System.setProperty("Quaqua.tabLayoutPolicy","wrap");
        try {
            UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
        } catch (Exception e) {
        }
        final TasksForm tasksForm = new TasksForm();

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                tasksForm.onExit();
            }
        });
    }

}
