package com.AMS.jBEAM.objectInspection.swing.gui.table;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import java.util.Optional;

/**
 * A table whose rows values only depend on a 1-dimensional list.
 * Which value is displayed in which row is defined by the
 * corresponding {@link ColumnDescriptionIF}.
 *
 * @param <T>
 */
public class ListBasedTable<T> extends JPanel
{
    private final ListBasedTableModel<T>    tableModel;
    private final TableRowSorter            rowSorter;
    private final JTable                    table;
    private final JScrollPane               scrollPane;

    public ListBasedTable(List<T> list, List<ColumnDescriptionIF<T>> columnDescriptions) {
        super(new BorderLayout());

        tableModel = new ListBasedTableModel<>(list, columnDescriptions);

        RowFilter<ListBasedTableModel<?>, Integer> rowFilter = new ListBasedTableRowFilter();
        rowSorter = new TableRowSorter<>(tableModel);
        rowSorter.setRowFilter(rowFilter);

        table = new JTable(tableModel);
        table.setRowSorter(rowSorter);

        scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        addListeners();
    }

    private void addListeners() {
        addListenerForFilterPopup();
        addFilterChangedListeners();
        addListenerForMouseCursor();
        addListenerForMouseClickAction();
    }

    private void addListenerForFilterPopup() {
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!SwingUtilities.isRightMouseButton(e)) {
                    return;
                }
                Point mousePositionInTable = e.getPoint();
                int col = table.columnAtPoint(mousePositionInTable);
                if (col < 0) {
                    return;
                }
                TableValueFilterIF valueFilter = tableModel.getValueFilter(col);
                String columnName = tableModel.getPlainColumnName(col);
                JDialog filterPopup = new FilterPopup(valueFilter, columnName);
                Point framePosition = table.getTopLevelAncestor().getLocationOnScreen();
                Point tablePosition = table.getLocationOnScreen();
                Point tableShift = new Point(tablePosition.x - framePosition.x, tablePosition.y - framePosition.y);
                Point position = mousePositionInTable;
                position.translate(tableShift.x, tableShift.y);
                filterPopup.setLocation(position);
                filterPopup.pack();
                SwingUtilities.invokeLater(() -> {
                    filterPopup.setVisible(true);
                    filterPopup.toFront();
                });
            }
        });
    }

    private void addFilterChangedListeners() {
        int numColumns = tableModel.getColumnCount();
        for (int col = 0; col < numColumns; col++) {
            TableValueFilterIF valueFilter = tableModel.getValueFilter(col);
            valueFilter.addFilterChangedListener(this::onFilterChanged);
        }
    }

    private void addListenerForMouseCursor() {
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Runnable runnable = getCellValueAsRunnable(e.getPoint());
                Cursor cursor = runnable == null ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                table.setCursor(cursor);
            }
        });
    }

    private void addListenerForMouseClickAction() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Runnable runnable = getCellValueAsRunnable(e.getPoint());
                    if (runnable != null) {
                        runnable.run();
                    }
                }
            }
        });
    }

    private void onFilterChanged() {
        rowSorter.sort();
        updateColumnNames();
    }

    private Runnable getCellValueAsRunnable(Point p) {
        CellCoordinates modelCoordinates = getModelCoordinates(p);
        if (modelCoordinates != null) {
            int row = modelCoordinates.getRow();
            int col = modelCoordinates.getColumn();
            if (row >= 0 && col >= 0) {
                Object cellValue = tableModel.getValueAt(row, col);
                if (cellValue instanceof Runnable) {
                    return (Runnable) cellValue;
                }
            }
        }
        return null;
    }

    private void updateColumnNames() {
        TableColumnModel columnModel = table.getColumnModel();
        int numColumns = tableModel.getColumnCount();
        for (int col = 0; col < numColumns; col++) {
            String columnName = tableModel.getColumnName(col);
            TableColumn column = columnModel.getColumn(col);
            column.setHeaderValue(columnName);
        }
        table.getTableHeader().repaint();
    }

    public JTable getInternalTable() {
        return table;
    }

    CellCoordinates getModelCoordinates(Point p) {
        int row = table.rowAtPoint(p);
        int col = table.columnAtPoint(p);
        if (row < 0 || col < 0) {
            return null;
        }
        return new CellCoordinates(table.convertRowIndexToModel(row), table.convertColumnIndexToModel(col));
    }

    private static class CellCoordinates
    {
        private final int row;
        private final int column;

        CellCoordinates(int row, int col) {
            this.row = row;
            this.column = col;
        }

        int getRow() {
            return row;
        }

        int getColumn() {
            return column;
        }
    }
}