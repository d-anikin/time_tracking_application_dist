package ru.dealerpoint;

import ru.dealerpoint.redmine.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class MainForm extends JFrame implements IStartIssueListener {
    private JComboBox cbActivities;
    private JTabbedPane mainTabbedPanel;
    private JComboBox cbQueries;
    private JTable issuesTable;
    private JPanel contentPane;
    private JTextField pageTextField;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageCountLabel;
    private JButton button1;
    private JTextArea issueSubject;
    private JLabel estimatedHoursLabel;
    private JLabel spentHoursLabel;
    private JPanel issuePane;
    private JButton logoutButton;
    private JLabel usernameLabel;
    private JLabel todayLabel;
    private JLabel thisWeekLabel;
    private JLabel thisMonthLabel;

    private Preferences userPrefs;
    private Api api;
    private ArrayList<Item> queries, activities;
    private IssuesData issuesData;
    private int pageIndex = 1;
    private Issue activeIssue = null;

    public MainForm() {
        super("Redmine Time Tracker");
        setContentPane(contentPane);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        userPrefs = Preferences.userRoot().node("redmine_time_tracker_app");
        authorize();
        loadData();
        issuePane.setVisible(false);
        issueSubject.setBackground(getBackground());
        setVisible(true);

        cbQueries.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQueriesChanged();
            }
        });

        prevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setPageIndex(pageIndex - 1);
                loadIssues();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                setPageIndex(pageIndex + 1);
                loadIssues();
            }
        });
        pageTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                int newIndex = Integer.parseInt(pageTextField.getText());
                setPageIndex(newIndex);
                loadIssues();
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onLogout();
            }
        });
        mainTabbedPanel.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                if (mainTabbedPanel.getSelectedIndex() == 1) {
                    onShowDetails();
                }
            }
        });
    }

    private void initIssuesTable() {
        StartIssueButtonEditor startIssueButtonEditor = new StartIssueButtonEditor();
        startIssueButtonEditor.addStartIssueListener(this);
        issuesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableColumn controlColumn = issuesTable.getColumnModel().getColumn(4);
        controlColumn.setCellRenderer(new StartIssueButtonRenderer());
        controlColumn.setCellEditor(startIssueButtonEditor);

        issuesTable.getColumnModel().getColumn(4).setMaxWidth(30);
        issuesTable.getColumnModel().getColumn(1).setPreferredWidth(400);
        issuesTable.getColumnModel().getColumn(0).setPreferredWidth(50);
    }

    private void setPageIndex(int value) {
        if (value > 0 && value <= issuesData.getPageCount()) {
            pageIndex = value;
        }
        pageTextField.setText(Integer.toString(pageIndex));
    }

    /* Setters and getters */
    private Long getQuerieId() {
        int index = cbQueries.getSelectedIndex();
        if (index > -1) {
            Item query = queries.get(index);
            return query.getId();
        } else {
            return null;
        }
    }

    private int getIssuesOffset() {
        if (issuesData != null) {
            return  issuesData.getLimit() * (pageIndex - 1);
        } else {
            return 0;
        }
    }

    private void setDefaultActivity() {
        if (activities != null){
            for (int i = 0; i < activities.size(); i++) {
                if (activities.get(i).getDefault()) {
                    cbActivities.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    /* Helpers */

    private String formatTimeEntry(Float timeEntry) {
        int value = Math.round(timeEntry * 60);
        int hours = value / 60;
        int minutes = value % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    /* === */

    private void loadIssues() {
        try {
            issuesData = api.getIssues(getQuerieId(), getIssuesOffset());
            pageCountLabel.setText("of " + issuesData.getPageCount());

            IssueTableModel model = new IssueTableModel(issuesData.getItems());
            issuesTable.setModel(model);
            initIssuesTable();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void loadData() {
        queries = api.getQueries();
        cbQueries.setModel(new DefaultComboBoxModel(queries.toArray()));
        activities = api.getActivities();
        cbActivities.setModel(new DefaultComboBoxModel(activities.toArray()));
        setDefaultActivity();
        loadIssues();
    }

    private void authorize() {
        if (userPrefs.get("redmine_url", "").isEmpty()) {
            authorizeForm();
        } else {
            this.api = new Api(userPrefs.get("redmine_url", ""), userPrefs.get("redmine_api_key", ""));
            if (!api.checkAccess()) {
                authorizeForm();
            }
        }
    }

    private void authorizeForm() {
        LoginForm dialog = new LoginForm(userPrefs);
        dialog.setVisible(true);
        this.api = new Api(userPrefs.get("redmine_url", ""), userPrefs.get("redmine_api_key", ""));
    }

    /* Events */
    private void onQueriesChanged() {
        setPageIndex(1);
        loadIssues();
    }

    private void onLogout(){
        userPrefs.remove("redmine_url");
        userPrefs.remove("redmine_api_key");
        System.exit(0);
    }

    public void onStartIssue(int selectedRow, int selectedColumn) {
//        if (activeIssue != null) { api.stopWork(activeIssue.getId()); }
        Issue issue = issuesData.getItems().get(selectedRow);
        activeIssue = api.getIssue(issue.getId());
        final String str = '#' + activeIssue.getId().toString() + ' ' +  activeIssue.getSubject();
        issueSubject.setText(str);
        estimatedHoursLabel.setText(activeIssue.getEstimatedHours().toString());
        spentHoursLabel.setText(activeIssue.getSpentHours().toString());
        issuePane.setVisible(true);
    }

    public void onShowDetails() {
        Details details = api.getDetails();
        usernameLabel.setText(details.getUserName());
        todayLabel.setText(formatTimeEntry(details.getToday()));
        thisWeekLabel.setText(formatTimeEntry(details.getThisWeek()));
        thisMonthLabel.setText(formatTimeEntry(details.getThisMonth()));
    }

    static public void main(String[] args) {
        MainForm mainForm = new MainForm();
    }

}
