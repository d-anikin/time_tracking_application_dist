package ru.dealerpoint;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Created by dmitry on 05.09.16.
 */
public class MainForm extends JFrame {
    private JComboBox comboBox1;
    private JTabbedPane tabbedPane1;
    private JComboBox cbQueries;
    private JTable issuesTable;
    private JPanel contentPane;
    private JTextField a45TextField;
    private JButton a1Button;
    private JButton button2;

    private Preferences userPrefs;
    private RedmineApi redmineApi;
    private ArrayList<ItemData> queries;

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

        cbQueries.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQueriesChanged();
            }
        });
        setVisible(true);
    }

    private void loadData() {
        queries = redmineApi.getQueries();
        cbQueries.setModel(new DefaultComboBoxModel(queries.toArray()));
        onQueriesChanged();
    }

    private void refreshIssues(Long querieId) {
        try {
            ArrayList<IssueData> issues = redmineApi.getIssues(querieId);
            IssueTableModel model = new IssueTableModel(issues);
            issuesTable.setModel(model);
            TableColumn column = null;
            for (int i = 0; i < 2; i++) {
                column = issuesTable.getColumnModel().getColumn(i);
                if (i == 0) {
                    column.setPreferredWidth(50);
                } else if (i == 1) {
                    column.setPreferredWidth(300);
                } else {
                    column.setPreferredWidth(300);
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void authorize() {
        if (userPrefs.get("redmine_url", "").isEmpty()) {
            authorizeForm();
        } else {
            this.redmineApi = new RedmineApi(userPrefs.get("redmine_url", ""), userPrefs.get("redmine_api_key", ""));
            if (!redmineApi.checkAccess()) {
                authorizeForm();
            }
        }
    }

    private void authorizeForm() {
        LoginForm dialog = new LoginForm(userPrefs);
        dialog.setVisible(true);
        this.redmineApi = new RedmineApi(userPrefs.get("redmine_url", ""), userPrefs.get("redmine_api_key", ""));
    }

    public void onQueriesChanged() {
        int index = cbQueries.getSelectedIndex();
        if (index > -1) {
            ItemData query = queries.get(index);
            refreshIssues(query.getId());
        } else {
            refreshIssues(null);
        }
    }

    public static void main(String[] args) {
        MainForm mainForm = new MainForm();
    }

}
