/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PropertiesPanel.java
 *
 * Created on Mar 14, 2011, 2:55:45 PM
 */

package cz.noctapuru.puzzlehuntmodel.view;

import cz.noctapuru.puzzlehuntmodel.controller.CheckpointsViewController;
import cz.noctapuru.puzzlehuntmodel.model.Checkpoint;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author viktor
 */
public final class CheckpointsView extends View implements TableModelListener {

    public static final String PROP_CHECKPOINTS = "checkpoints";

    private CheckpointsViewController controller;
    private CheckpointTableModel tableModel;

    /** Creates new form PropertiesPanel */
    public CheckpointsView(CheckpointsViewController controller) {
        this.controller = controller;
        this.tableModel = new CheckpointTableModel();
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        jTable1.setModel(this.tableModel);
        jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTable1.setFillsViewportHeight(true);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTable1.setShowGrid(true);
        this.tableModel.addTableModelListener(this);
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable1KeyPressed(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 363, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTable1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyPressed
        if(evt.getKeyCode() == KeyEvent.VK_ENTER) {
            evt.consume();
            int row = this.jTable1.getSelectedRow() + 1;
            this.tableModel.insertCheckpoint(row, new Checkpoint(1.0, 30.0));
            this.jTable1.setRowSelectionInterval(row, row);
        }

        if(evt.getKeyCode() == KeyEvent.VK_BACK_SPACE || evt.getKeyCode() == KeyEvent.VK_DELETE) {
            evt.consume();
            int row = this.jTable1.getSelectedRow();
            this.tableModel.removeCheckpoint(row);
            this.jTable1.setRowSelectionInterval(row-1, row-1);
        }
    }//GEN-LAST:event_jTable1KeyPressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    public void setCheckpoints(List<Checkpoint> checkpoints) {
        if(!checkpoints.equals(this.tableModel.checkpoints)) { // prevent unnecessary view updates
            this.tableModel.checkpoints = checkpoints;
        }
    }

    // TableModelListener

    public void tableChanged(TableModelEvent tme) {
        this.propertySupport.firePropertyChange(CheckpointsView.PROP_CHECKPOINTS, null, tableModel.checkpoints);
    }

    // Model for checkpoint table

    private class CheckpointTableModel extends AbstractTableModel {
        private List<Checkpoint> checkpoints = null;

        public CheckpointTableModel() {
            this.checkpoints = new ArrayList<Checkpoint>();
        }

        public void setCheckpoints(List<Checkpoint> checkpoints) {
            this.checkpoints = checkpoints;
            this.fireTableDataChanged();
        }

        public int getRowCount() {
            return checkpoints.size();
        }

        public int getColumnCount() {
            return 3;
        }

        // row editing

        public void insertCheckpoint(int i, Checkpoint c) {
            this.checkpoints.add(i, c);
            this.fireTableRowsInserted(i, i);
        }

        public Checkpoint removeCheckpoint(int i) {
            if(this.checkpoints.size() < 2)
                return null;

            Checkpoint cp = this.checkpoints.remove(i);
            this.fireTableRowsDeleted(i, i);

            return cp;
        }

        // overrides

        @Override
        public String getColumnName(int i) {
            String names[] = {"Checkpoint", "Alpha (shape)", "Beta (scale)"};

            return names[i];
        }


        @Override
        public Class getColumnClass(int c) {
            if(c < 1)
                return Integer.class;
            else
                return Double.class;
        }

        public Object getValueAt(int i, int i1) {
            switch(i1) {
                case 0:
                    return i;
                case 1:
                    return this.checkpoints.get(i).getAlpha();                    
                case 2:
                    return this.checkpoints.get(i).getBeta();
            }

            return null;
        }

        @Override
        public boolean isCellEditable(int i, int i1) {
            return i1 > 0;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if(value == null)
                return;
            
            Checkpoint original = this.checkpoints.get(row);
            switch(col) {
                case 1:
                    this.checkpoints.set(row, new Checkpoint((Double) value, original.getBeta()));
                    break;
                case 2:
                    this.checkpoints.set(row, new Checkpoint(original.getAlpha(), (Double) value));
                    break;
            }

            this.fireTableRowsUpdated(row, row);
        }
    }

}