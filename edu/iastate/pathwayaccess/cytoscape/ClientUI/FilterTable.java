package edu.iastate.pathwayaccess.cytoscape.ClientUI;

import edu.iastate.pathwayaccess.cytoscape.PathwayAccessPlugin;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.NetworkResultInterface;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.ResultsFilterInterface;
import edu.iastate.pathwayaccess.cytoscape.PropertiesInterface;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Greg
 */
public class FilterTable extends javax.swing.JPanel {

    /**
     * Stores name of type of object the containing filters represent. This is
     * to be used for the tab name.
     */
    private String typeName;
    /**
     * Stores the containing pnlNetworkTabs class
     */
    private pnlNetworkTabs pnlNetworkTabs;
    /**
     * Stores the details panel to update with the details of each filter
     */
    private pnlDetails pnlDetails;
    /**
     * Stores the row number currently selectedRows (-1 when none selectedRows)
     */
    private int curRow = -1;
    /**
     * Stores the database access class
     */
    private WindowManager winManager = null;

    /**
     * We must create our own table model in order to allow check boxes to work
     */
    private class FilterTableModel extends AbstractTableModel {
        /**
         * Names of each column
         */
        private String[] columnNames = {"Apply",
                                        "Filter Name (# of Networks)",
                                        "Filter Type"};
        /**
         * Data of the table
         */
        Object[][] data = null;

        /**
         * Creates/Stores all the data for the table
         * @param filters Pathways whose info to fill the table with
         */
        public FilterTableModel(ResultsFilterInterface[] filters)
        {
            if(filters == null)
                throw new NullPointerException("Null Array");
            //Count number of non-null filters
            int numFilters = 0;
            for(int i = 0; i < filters.length; i++)
                if(filters[i] != null)
                    numFilters++;
            data = new Object[numFilters][3];
            //Fill the table
            for(int row = 0; row < filters.length; row++)
            {
                if(filters[row] == null) //Skip null networks
                    continue;
                //Set the name of the tab representing this type of filters
                if(row == 0)
                    typeName = filters[0].getFilterTypeName();
                //Create the data
                data[row][0] = new Boolean(filters[row].selected());
                data[row][1] = filters[row];
                data[row][2] = filters[row].getFilterTypeName();
                //Check that no values in the table are null
                for(int col = 1; col < columnNames.length - 1; col++)
                {
                    if(data[row][col] == null)
                        data[row][col] = "Unknown";
                }
            }
        }
        /**
         * Returns the number of rows
         * @return The number of rows
         */
        public int getRowCount() {
            return data.length;
        }
        /**
         * Returns the number of columns
         * @return The number of columns
         */
        public int getColumnCount() {
            return columnNames.length;
        }
        /**
         * Returns the name of the given column
         * @param col Column
         * @return The name of the given column
         */
        @Override
        public String getColumnName(int col) {
            if(col >= columnNames.length)
                return "";
            return columnNames[col];
        }
        /**
         * This returns the value at the given row and column
         * @param rowIndex Row
         * @param columnIndex Column
         * @return Value at the given row and column. Null if it's not a valid
         *         index.
         */
        public Object getValueAt(int rowIndex, int columnIndex) {
            if(rowIndex < data.length && columnIndex < columnNames.length)
                return data[rowIndex][columnIndex];
            return null;
        }
        /*
         * Implemented to allow the checkboxes to change value. Also, updates
         * the list of networks to filter in pnlNetworkTabs.
         */
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if(isCellEditable(rowIndex, columnIndex))
            {
                //Set the new value and update listeners of the change
                data[rowIndex][columnIndex] = value;
                fireTableCellUpdated(rowIndex, columnIndex);

                //Update list status of selected of the filter
                ResultsFilterInterface selNetwork = (ResultsFilterInterface)
                        this.getValueAt(rowIndex, 1);
                //Add or Remove the filter's applied networks
                Iterator itr = selNetwork.resultsFiltered().iterator();
                while(itr.hasNext())
                {
                    NetworkResultInterface net = (NetworkResultInterface)itr.next();
                    if((Boolean)value == false)
                    {
                        selNetwork.setSelected(false);
                        pnlNetworkTabs.removeFilteredNetwork(net);
                    }
                    else
                    {
                        selNetwork.setSelected(true);
                        pnlNetworkTabs.addFilteredNetwork(net);
                    }
                }
                
            }
        }
        /*
         * This is neccessary to let the checkbox work; without this it was only
         * display the boolean. This returns the class of the objects stored
         * under the given column.
         */
        @Override
        public Class getColumnClass(int col) {
            if(col >= columnNames.length)
                return Object.class;
            Object obj = getValueAt(0, col);
            if(obj == null)
                return Object.class;
            return obj.getClass();
        }
        /**
         * Returns whether the given row and column is editable
         * @param row Row
         * @param col Column
         * @return Whether the given row and column is editable. Returns true
         *         only for the checkbox.
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            if(col == 0 && row < data.length)
                return true;
            return false;
        }
    }

    /**
     * Creates the table represented by this class with the given filters
     * @param pnlNetworkTabs Class containing the filters tabs
     * @param details Details class to update with the details of the currently
     *                selected filter
     * @param filters Filters to fill the table with
     */
    public FilterTable(WindowManager winManager, PropertiesInterface properties, ResultsFilterInterface[] filters)
    {
        if(winManager == null || properties == null || filters == null)
            throw new NullPointerException();
        this.winManager = winManager;
        if(pnlNetworkTabs == null)
            pnlNetworkTabs = winManager.getPnlNetworkTabs();
        if(pnlDetails == null)
            pnlDetails = winManager.getPnlDetails();
        initComponents();
        //Create the table
        FilterTableModel model = new FilterTableModel(filters);
        jTable1.setModel(model);
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

                jTable1.setModel(new javax.swing.table.DefaultTableModel());
                jTable1.setToolTipText("Select filters to apply to all search results");
                jTable1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
                jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                jTable1MouseClicked(evt);
                        }
                });
                jScrollPane1.setViewportView(jTable1);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                );
        }// </editor-fold>//GEN-END:initComponents

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        //Check if table has any data
        if(jTable1.getRowCount() == 0) return;
        //Check if the selection has changed
        int newRow = jTable1.getSelectedRow();
        if(newRow == curRow) return; //Row hasn't changed
        curRow = newRow;
        if(curRow == -1) //No row is selectedRows
        {
            pnlDetails.setText("");
            return;
        }
        //Get the selectedRows pathway and update the pnlDetails panel with its info
        ResultsFilterInterface selFilter = (ResultsFilterInterface)jTable1.getValueAt(curRow, 1);
        if(selFilter != null)
            pnlDetails.setText(selFilter.getDetails());
    }//GEN-LAST:event_jTable1MouseClicked

    /**
     * Returns the name of the type of filter this table represents
     * @return The name of the type of filter this table represents
     */
    public String getTypeName()
    {
        return typeName;
    }

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JScrollPane jScrollPane1;
        private javax.swing.JTable jTable1;
        // End of variables declaration//GEN-END:variables

}
