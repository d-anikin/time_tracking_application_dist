package ru.dealerpoint;


import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

interface IStartIssueListener {
    void onStartIssue(int selectedRow, int selectedColumn);
}

class StartIssueButtonEditor extends DefaultCellEditor {
    protected JButton button;

    private int selectedRow;
    private int selectedColumn;
    private final List<IStartIssueListener> listeners = new ArrayList<IStartIssueListener>();

    public StartIssueButtonEditor() {
        super(new JCheckBox());

        button = new JButton();
        button.setOpaque(true);
        try {
            button.setIcon(new ImageIcon(getClass().getResource("/play.png")));
        } catch (Exception ex) {
        }
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (IStartIssueListener listener : listeners) {
                    //сообщаем всем слушателям о событии
                    listener.onStartIssue(selectedRow, selectedColumn);
                }
                fireEditingStopped();
            }
        });
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        selectedRow = row;
        selectedColumn = column;
        if (isSelected) {
            button.setForeground(table.getSelectionForeground());
            button.setBackground(table.getSelectionBackground());
        } else {
            button.setForeground(table.getForeground());
            button.setBackground(table.getBackground());
        }
        return button;
    }

    public void addStartIssueListener(IStartIssueListener l) {listeners.add(l);}
    public void removeStartIssueListener(IStartIssueListener l) {listeners.remove(l);}
}

class StartIssueButtonRenderer extends JButton implements TableCellRenderer {

    public StartIssueButtonRenderer() {
        setOpaque(true);
        try {
            setIcon(new ImageIcon(getClass().getResource("/play.png")));
        } catch (Exception ex) {
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setForeground(table.getSelectionForeground());
            setBackground(table.getSelectionBackground());
        } else {
            setForeground(table.getForeground());
            setBackground(UIManager.getColor("Button.background"));
        }
        setText((value == null) ? "" : value.toString());
        return this;
    }
}