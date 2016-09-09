package ru.dealerpoint;

import ru.dealerpoint.redmine.IssuesData;
import ru.dealerpoint.redmine.Item;
import ru.dealerpoint.redmine.Api;

import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.prefs.Preferences;

public class MainForm extends JFrame {
    private JComboBox comboBox1;
    private JTabbedPane tabbedPane1;
    private JComboBox cbQueries;
    private JTable issuesTable;
    private JPanel contentPane;
    private JTextField pageTextField;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageCountLabel;

    private Preferences userPrefs;
    private Api api;
    private ArrayList<Item> queries;
    private IssuesData issuesData;
    private int pageIndex = 0;

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
    }

    private void setPageIndex(int value) {
        if (value >= 0 && value < issuesData.getPageCount()) {
            pageIndex = value;
        }
        pageTextField.setText(Integer.toString(pageIndex + 1));
    }

    private void loadData() {
        queries = api.getQueries();
        cbQueries.setModel(new DefaultComboBoxModel(queries.toArray()));
        loadIssues();
    }

    private void loadIssues() {
        try {
            issuesData = api.getIssues(getQuerieId(), getIssuesOffset());
            pageCountLabel.setText("of " + issuesData.getPageCount());

            IssueTableModel model = new IssueTableModel(issuesData.getItems());
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

    public Long getQuerieId() {
        int index = cbQueries.getSelectedIndex();
        if (index > -1) {
            Item query = queries.get(index);
            return query.getId();
        } else {
            return null;
        }
    }

    public int getIssuesOffset() {
        if (issuesData != null) {
            return  issuesData.getLimit() * pageIndex;
        } else {
            return 0;
        }
    }

    public void onQueriesChanged() {
        setPageIndex(0);
        loadIssues();
    }

    public static void main(String[] args) {
        MainForm mainForm = new MainForm();
    }

}
