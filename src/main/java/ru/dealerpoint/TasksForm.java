package ru.dealerpoint;

import ru.dealerpoint.redmine.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.prefs.Preferences;

public class TasksForm extends JFrame implements ILoginFormListener {
    private static String[] WORK_MESSAGES = {
        "Эх, по<b>работать</b> бы!:)",
            "Пора <b>работать</b>!",
            "Work, Work опять <b>работа</b>?!",
            "<b>Работа</b> способна доставить гораздо больше удовольствия",
            "<b>Работа</b>, <b>работа</b>, опять <b>работа</b>",
            "Когда люди заняты <b>работой</b>, у них бывает лучшее настроение.",
            "<b>Работа</b> — лучшее лекарство от всех бед.",
            "<b>Работать</b> не так скучно, как развлекаться.",
            "Чем упорнее вы <b>работаете</b>, тем удачливее вы становитесь.",
            "Для меня жить — значит <b>работать</b>.",
            "<b>Работайте</b> так, словно деньги не имеют для Вас никакого значения.",
            "Хорошо <b>работается</b>, когда любишь свою профессию, с увлечением занимаешься ею.",
            "Сделайте вашу <b>работу</b> наполненной жизнью, а не жизнь наполненной работой.",
            "Для того чтобы научиться хорошо <b>работать</b>, надо искренне увлекаться <b>работой</b>, без увлечения <b>работать</b> не научишься.",
            "Наслаждайтесь тем, что вы делаете и вы никогда в своей жизни не будете <b>работать</b>.",
            "Счастье не в том, чтобы делать всегда, что хочешь, а в том, чтобы всегда хотеть того, что делаешь."
    };
    private static String[] LUNCH_MESSAGES = {
        "Кушать хочешь?",
            "Любой может испортить обед. Но повар делает это профессионально.",
            "Жрать time!",
            "Обед!",
            "Пора кушать!",
            "После сытного обеда, ближе кажется победа.",
            "Жизнь по-настоящему ощущаешь только тогда, когда туго набил брюхо",
            "Вся жизнь в борьбе: до обеда с голодом, после обеда — со сном."
    };


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
    private JButton refreshButton;

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
    private Timer notifyTimer;

    public TasksForm() {
        super("Redmine Time Tracker");

        try {
            Image img = ImageIO.read(getClass().getResource("/logo.png"));
            setIconImage(img);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        setContentPane(contentPane);
        setResizable(false);
        pack();
        setMinimumSize(new Dimension(320, 280));
        setSize(new Dimension(320, 460));
        setDefaultLocation();
//        setLocationRelativeTo(null);
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
        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                loadIssues();
            }
        });
        notifyTimer = new Timer(2 * 1000, new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                onNotifyTimer();
            }
        });
        notifyTimer.setRepeats(false);
        onActivate();
    }

    /* Setters and getters */
    private void setDefaultLocation() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle rect = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int) rect.getMaxX() - getWidth();
        int y = (int) rect.getMaxY() - getHeight();
        setLocation(x, y);
    }
    private void setUserSession(Session session) {
        userSession = session;
        setTitle("Redmine Time Tracker - " + userSession.getUserName());
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

    private boolean isWorkingDay() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        return (hour >= userSession.getDayStartingAt() && hour < userSession.getDayEndingIn());
    }

    private boolean isLunchTime() {
        LocalDateTime now = LocalDateTime.now();
        int hour = now.getHour();
        return (hour >= userSession.getLunchStartingAt() && hour < userSession.getLunchEndingIn());
    }

    private boolean isWorkingTime() {
        return isWorkingDay() && !isLunchTime();
    }

    private boolean isActiveIssue() {
        return activeTimeEntry != null;
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
        issuesTable.clearSelection();
        if (issueTableModel.getRowCount() > 0) {
            issuesTable.setRowSelectionInterval(0, 0);
            issuesTable.requestFocus();
        }
        issuesTable.repaint();
    }

    private void authorizeForm() {
        String url = userPrefs.get("redmine_url", "");
        String key = userPrefs.get("redmine_api_key", "");
        LoginForm dialog = new LoginForm(url, key);
        dialog.addLoginFormListener(this);
        dialog.setVisible(true);
    }

    private void onNotifyTimer() {
        notifyTimer.setInitialDelay(2 * 1000);
        if (isWorkingTime()) {
            // рабочее время
            if (!isActiveIssue()) {
                // Если не работаем в рабочее время
                String msg = WORK_MESSAGES[randInt(0, WORK_MESSAGES.length - 1)];
                int result = TempDialog.showDialog(msg, TempDialog.KIND_WORK_IN);
                switch (result) {
                    case TempDialog.REMEMBER:
                        notifyTimer.setInitialDelay(5 * 60 * 1000);
                        break;
                    default:
                        notifyTimer.setInitialDelay(60 * 1000);
                        break;
                }
            }

        } else {
            // не рабочее время
            if (isActiveIssue()) {
                String msg;
                int kind;
                // Если работаем в рабочее время
                if (isLunchTime()) {
                    msg = LUNCH_MESSAGES[randInt(0, LUNCH_MESSAGES.length - 1)];
                    kind = TempDialog.KIND_REST;
                } else {
                    msg = "Пора домой!";
                    kind = TempDialog.KIND_WORK_OUT;
                }
                int result = TempDialog.showDialog(msg, kind);
                switch (result) {
                    case TempDialog.REMEMBER:
                        notifyTimer.setInitialDelay(5 * 60 * 1000);
                        break;
                    case TempDialog.RESUME:
                        notifyTimer.setInitialDelay(30 * 60 * 1000);
                        break;
                    default:
                        onStopTimeEntry();
                        break;
                }
            }
        }
        notifyTimer.restart();
    }

    /* Helpers */
    private String formatHours(Float timeEntry) {
        int value = Math.round(timeEntry * 60);
        int hours = value / 60;
        int minutes = value % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
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
                }
            }
        };
        workerTimer.schedule(timerTask, 60000, 60000);
        notifyTimer.start();
    }

    public void onSuccessLogin(String redmineUrl, String redmineApiKey, Session session) {
        userPrefs.put("redmine_url", redmineUrl);
        userPrefs.put("redmine_api_key", redmineApiKey);
        setUserSession(session);
        this.api = new Api(redmineUrl, redmineApiKey, session);
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
        }
    }


    private void onStopTimeEntry() {
        try {
            api.stopWork(activeTimeEntry);
            activeTimeEntry = null;
            loadIssues();
            currentTaskPanel.setVisible(false);
            taskListPanel.setVisible(true);
            notifyTimer.setInitialDelay(5*60*1000);
            notifyTimer.restart();
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
