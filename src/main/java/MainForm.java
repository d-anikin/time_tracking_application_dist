import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.prefs.Preferences;

/**
 * Created by dmitry on 05.09.16.
 */
public class MainForm extends JFrame {
    private JComboBox comboBox1;
    private JButton stopButton;
    private JTabbedPane tabbedPane1;
    private JComboBox comboBox2;
    private JButton runButton;
    private JButton refreshButton;
    private JTable issuesTable;
    private JPanel contentPane;

    private Preferences userPrefs;
    private RedmineApi redmineApi;

    public MainForm() {
        setTitle("Redmine Time Tracker");
        setContentPane(contentPane);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        userPrefs = Preferences.userRoot().node("redmine_time_tracker_app");
        authorize();
        refreshIssues();
        setVisible(true);
    }

    private void refreshIssues() {
        try {
            ArrayList<IssueData> issues = redmineApi.getIssues();
            IssueTableModel model = new IssueTableModel(issues);
            TableColumn column = null;
            issuesTable.setModel(model);
            for (int i = 0; i < 2; i++) {
                column = issuesTable.getColumnModel().getColumn(i);
                if (i == 1) {
                    column.setPreferredWidth(500);
                } else {
                    column.setPreferredWidth(50);
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

    public static void main(String[] args) {
        MainForm mainForm = new MainForm();
    }
}
