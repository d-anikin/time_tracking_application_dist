import javax.swing.*;
import java.awt.event.ActionListener;

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
    private JTable table1;
    private JPanel contentPane;

    public MainForm() {
        setTitle("Redmine Time Tracker");
        setContentPane(contentPane);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}
