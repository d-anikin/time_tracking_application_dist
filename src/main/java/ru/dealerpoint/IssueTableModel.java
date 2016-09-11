package ru.dealerpoint;

import ru.dealerpoint.redmine.Issue;
import ru.dealerpoint.redmine.Item;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class IssueTableModel implements TableModel {

    private Set<TableModelListener> listeners = new HashSet<TableModelListener>();

    private ArrayList<Issue> issues;

    public IssueTableModel(ArrayList<Issue> issues) {
        this.issues = issues;
    }

    public void addTableModelListener(TableModelListener listener) {
        listeners.add(listener);
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 4:
                return Button.class;
        }
        return String.class;
    }

    public int getColumnCount() {
        return 5;
    }

    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "ID";
            case 1:
                return "Subject";
            case 2:
                return "Status";
            case 3:
                return "Assignee";
        }
        return "";
    }

    public int getRowCount() {
        return issues.size();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Issue issue = issues.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return issue.getId();
            case 1:
                return issue.getSubject();
            case 2:
                Item status = issue.getStatus();
                if (status != null) { return status.getName(); }
                else { return ""; }
            case 3:
                Item assignee = issue.getAssignedTo();
                if (assignee != null) { return assignee.getName(); }
                else { return ""; }
        }
        return "";
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 4;
    }

    public void removeTableModelListener(TableModelListener listener) {
        listeners.remove(listener);
    }

    public void setValueAt(Object value, int rowIndex, int columnIndex) {

    }

}