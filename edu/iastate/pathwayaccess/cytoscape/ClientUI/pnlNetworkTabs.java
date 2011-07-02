package edu.iastate.pathwayaccess.cytoscape.ClientUI;

import edu.iastate.pathwayaccess.cytoscape.PathwayAccessPlugin;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.NetworkResultInterface;
import edu.iastate.pathwayaccess.cytoscape.PropertiesInterface;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Greg
 */
public class pnlNetworkTabs <networkType> extends javax.swing.JPanel {

    /**
     * Stores the maximum number of rows displayed at one time.
     */
    private final int MAX_ROWS_DISPLAYED = 25;
    /**
     * Properties interface of the specific database in use
     */
    private PropertiesInterface properties;
    /**
     * Access to the Database
     */
    private PathwayAccessPlugin PathwayAccessPlugin;
    /**
     * Stores the window manager to call when next or back buttons are pressed
     */
    private WindowManager winManager;
    /**
     * Details panel to update with info about the current selecetion
     */
    private pnlDetails pnlDetails;
    /**
     * Stores the row number currently selectedRows (-1 when none selectedRows)
     */
    private int curRow = -1;
    /**
     * All data in table, including data not displayed on current page
     */
    private Object[][] moreData = null;
    /**
     * Stores the networks selected to create a CyNetwork
     */
    private ArrayList<NetworkResultInterface> selectedNetworks = new ArrayList<NetworkResultInterface>();
    /**
     * Stores the networks to be filtered from lists of network results.
     */
    private ArrayList<NetworkResultInterface> filteredNetworks = new ArrayList<NetworkResultInterface>();

    /** Creates new form pnlTabbedPane */
    public <networkType> pnlNetworkTabs(PathwayAccessPlugin database, WindowManager winManager) {
        properties = database.getProperties();
        PathwayAccessPlugin = database; //properties.getPathwayAccessPlugin();
        this.winManager = winManager;
        pnlDetails = winManager.getPnlDetails();
        initComponents();
    }
    private PathwayTableModel tableModel = null;

    /**
     * We must create our own table model in order to allow check boxes to work
     */
    private class PathwayTableModel extends AbstractTableModel {
        /**
         * Names of each column
         */
        private String[] columnNames = {"Create",
                                        "Pathway Name",
                                        "Organism"};
        /**
         * Data currently being displayed in the table
         */
        private Object[][] currentData = null;
        /**
         * Stores the given networks un-filtered
         */
        private NetworkResultInterface<networkType>[] unfilteredNetworks;

        /**
         * Creates/Stores all the data for the table
         * @param paths Pathways whose info to fill the table with
         */
        public PathwayTableModel(NetworkResultInterface<networkType>[] paths)
        {
            if(paths == null)
                throw new NullPointerException("Null Array");
            //Store the un-filtered networks
            unfilteredNetworks = paths;
            //data = new Object[paths.length][3];
            //Don't know yet how many rows there will be since some may get filtered
            //Create a temp data array and count the number of valid rows
            Object[][] tempData = null;
            tempData = new Object[paths.length][3];
            int numData = 0;
            //Fill in the temp data
            for(int numPath = 0; numPath < paths.length; numPath++)
            {
                if(paths[numPath] == null) //Skip null networks
                    continue;
                //Don't add if in list of filtered networks
                if(!filteredNetworks.contains(paths[numPath]))
                {
                    //Check if the path was previously selected
                    boolean select = false;
                    if(selectedNetworks.contains(paths[numPath]))
                        select = true;
                    //Create the data
                    tempData[numData][0] = new Boolean(select); //Select if previously selected
                    tempData[numData][1] = paths[numPath];
                    tempData[numData][2] = paths[numPath].getOrganismName();
                    //data[row][3] = paths[row].getCreated();
                    //Check that no values in the table are null
                    for(int col = 1; col < columnNames.length - 1; col++)
                    {
                        if(tempData[numData][col] == null)
                            tempData[numData][col] = "Unknown";
                    }
                    numData++;
                }
            }
            //Copy over the actual data from the temp data
            int numRows = numData;
            if(numData > MAX_ROWS_DISPLAYED)
                numRows = MAX_ROWS_DISPLAYED;
            currentData = new Object[numRows][3];
            moreData = new Object[numData][3];
            for(int row = 0; row < numData; row++)
            {
                if(row < MAX_ROWS_DISPLAYED)
                    currentData[row] = tempData[row];
                moreData[row] = tempData[row];
            }
            firstRow = 0;
        }
        /**
         * Stores the first row currently displayed in the table
         */
        private int firstRow = 0;
        /**
         * If more pages of data in the table are available, goes to the next page.
         * @return The index of the first result on the current page with the
         * index count starting at zero (i.e. if there are 25 results per page and
         * it's the second page, the given index would be 25). There is no next
         * page, -1 is returned.
         */
        public int nextPage()
        {
            if(!hasNextPage()) return -1;
            firstRow += MAX_ROWS_DISPLAYED;
            int pos = firstRow;
            int posMax = MAX_ROWS_DISPLAYED;
            int totalRows = getTotalRowCount();
            //If on the last page, check end position and resize table accordingly
            if(firstRow < totalRows && (firstRow + MAX_ROWS_DISPLAYED) > totalRows)
            {
                posMax = totalRows - firstRow;
                currentData = new Object[posMax][3];
            }
            //Copy new results to table
            for(int row = 0; row < posMax; row++, pos++)
                currentData[row] = moreData[pos];
            fireTableDataChanged();
            return firstRow;
        }
        /**
         * Returns true if a page following the current page of data is available,
         * otherwise false.
         * @return True if a page following the current page of data is available,
         * otherwise false.
         */
        public boolean hasNextPage()
        {
            if((firstRow + MAX_ROWS_DISPLAYED) > moreData.length)
                return false;
            return true;
        }
        /**
         * If previous pages of data in the table are available, goes to the previous page.
         * @return The index of the first result on the current page with the
         * index count starting at zero (i.e. if there are 25 results per page and
         * it's the second page, the given index would be 25). If there is no
         * previous page, -1 is returned.
         */
        public int previousPage()
        {
            if(!hasPreviousPage()) return -1;
            //If on the last page, resize table back to full size
            int totalRows = getTotalRowCount();
            if(firstRow < totalRows && (firstRow + MAX_ROWS_DISPLAYED) > totalRows)
            {
                currentData = new Object[MAX_ROWS_DISPLAYED][3];
            }
            firstRow -= MAX_ROWS_DISPLAYED;
            int pos = firstRow;
            //Copy new results to table
            for(int row = 0; row < MAX_ROWS_DISPLAYED; row++, pos++)
                currentData[row] = moreData[pos];
            fireTableDataChanged();
            return firstRow;
        }
        /**
         * Returns true if a page prior to the current page of data is available,
         * otherwise false.
         * @return True if a page prior to the current page of data is available,
         * otherwise false.
         */
        public boolean hasPreviousPage()
        {
            if(firstRow < MAX_ROWS_DISPLAYED)
                return false;
            return true;
        }
        /**
         * Returns the number of rows currently showing
         * @return The number of rows currently showing
         */
        public int getRowCount() {
            return currentData.length;
        }
        /**
         * Returns the total number of rows of results available
         * @return The total number of rows available
         */
        public int getTotalRowCount() {
            return moreData.length;
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
            if(rowIndex < currentData.length && columnIndex < columnNames.length)
                return currentData[rowIndex][columnIndex];
            return null;
        }
        /*
         * Implemented to allow the checkboxes to change value. Also, updates
         * the list of networks selectedRows to create and enables/disables the
         * network create button according to whether there are any pathways
         * selectedRows. Keeps a list of unique IDs of pathways selected to
         * create.
         */
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if(isCellEditable(rowIndex, columnIndex))
            {
                //Set the new value and update listeners of the change
                currentData[rowIndex][columnIndex] = value;
                fireTableCellUpdated(rowIndex, columnIndex);

                //Update list of selected networks to create
                NetworkResultInterface selNetwork = (NetworkResultInterface)
                        tblPathways.getModel().getValueAt(rowIndex, 1);
                if((Boolean)value == false)
                {
                    //Remove the row index from the list of selected rows
                    //if(selectedRows.contains((Integer)rowIndex))
                    //    selectedRows.remove((Integer)rowIndex);
                    //Remove the network from the list of selected networks
                    if(selectedNetworks.contains(selNetwork))
                        selectedNetworks.remove(selNetwork);
                }
                else
                {
                    //Add the row index from the list of selected rows
                    //if(!selectedRows.contains((Integer)rowIndex))
                    //    selectedRows.add((Integer)rowIndex);
                    //Add the network to the list of selected networks
                    if(!selectedNetworks.contains(selNetwork))
                        selectedNetworks.add(selNetwork);
                }
                //Enable/Disable Empty Selected and Create Network buttons and update text
                if(selectedNetworks.size() == 0)
                {
                    btnCreateNetwork.setEnabled(false);
                    btnCreateNetwork.setText("Create Network");
                    btnEmptySelected.setEnabled(false);
                } else if(selectedNetworks.size() == 1) {
                    btnEmptySelected.setEnabled(true);
                    btnCreateNetwork.setEnabled(true);
                    btnCreateNetwork.setText("Create Network (1)");
                } else
                {
                    btnEmptySelected.setEnabled(true);
                    btnCreateNetwork.setEnabled(true);
                    btnCreateNetwork.setText("Create Networks ("
                            + selectedNetworks.size() + ")");
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
            if(col == 0 && row < currentData.length)
                return true;
            return false;
        }
        /**
         * Returns an array of the unfiltered user data originally sent to the
         * constructor of this table model
         * @return An array of the unfiltered user data
         */
        public NetworkResultInterface[] getUnfilteredUserData()
        {
            return unfilteredNetworks;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                tabPathwaySelection = new javax.swing.JTabbedPane();
                pnlPathwayTab = new javax.swing.JPanel();
                scrPathwaysTable = new javax.swing.JScrollPane();
                tblPathways = new javax.swing.JTable();
                btnCreateNetwork = new javax.swing.JButton();
                chkSelectAll = new javax.swing.JCheckBox();
                btnPrevResultsPage = new javax.swing.JButton();
                lblNumResultsDisplayed = new javax.swing.JLabel();
                btnNextResultsPage = new javax.swing.JButton();
                btnEmptySelected = new javax.swing.JButton();

                tblPathways.setModel(new javax.swing.table.DefaultTableModel());
                tblPathways.setToolTipText("Select a pathway to open as a CyNetwork");
                tblPathways.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
                tblPathways.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                tblPathwaysMouseClicked(evt);
                        }
                });
                scrPathwaysTable.setViewportView(tblPathways);

                btnCreateNetwork.setText("Create Network");
                btnCreateNetwork.setToolTipText("Open the selected pathway as a CyNetwork");
                btnCreateNetwork.setEnabled(false);
                btnCreateNetwork.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnCreateNetworkActionPerformed(evt);
                        }
                });

                chkSelectAll.setText("All Pathways");
                chkSelectAll.setToolTipText("Selects all pathways currently displayed");
                chkSelectAll.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                chkSelectAllMouseClicked(evt);
                        }
                });

                btnPrevResultsPage.setText("Prev " + MAX_ROWS_DISPLAYED);
                btnPrevResultsPage.setToolTipText("Select to go to the previous results page");
                btnPrevResultsPage.setEnabled(false);
                btnPrevResultsPage.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnPrevResultsPageActionPerformed(evt);
                        }
                });

                lblNumResultsDisplayed.setText("Results ### to ###/###");

                btnNextResultsPage.setText("Next " + MAX_ROWS_DISPLAYED);
                btnNextResultsPage.setToolTipText("Select to go to the next results page");
                btnNextResultsPage.setEnabled(false);
                btnNextResultsPage.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnNextResultsPageActionPerformed(evt);
                        }
                });

                btnEmptySelected.setText("Remove All Selected Networks");
                btnEmptySelected.setToolTipText("Empties the list of networks to create");
                btnEmptySelected.setEnabled(false);
                btnEmptySelected.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                                btnEmptySelectedActionPerformed(evt);
                        }
                });

                javax.swing.GroupLayout pnlPathwayTabLayout = new javax.swing.GroupLayout(pnlPathwayTab);
                pnlPathwayTab.setLayout(pnlPathwayTabLayout);
                pnlPathwayTabLayout.setHorizontalGroup(
                        pnlPathwayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlPathwayTabLayout.createSequentialGroup()
                                .addComponent(chkSelectAll)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                                .addComponent(btnEmptySelected)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnCreateNetwork))
                        .addGroup(pnlPathwayTabLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblNumResultsDisplayed)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnPrevResultsPage)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnNextResultsPage)
                                .addGap(101, 101, 101))
                        .addComponent(scrPathwaysTable, javax.swing.GroupLayout.DEFAULT_SIZE, 449, Short.MAX_VALUE)
                );
                pnlPathwayTabLayout.setVerticalGroup(
                        pnlPathwayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlPathwayTabLayout.createSequentialGroup()
                                .addGroup(pnlPathwayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblNumResultsDisplayed)
                                        .addComponent(btnPrevResultsPage, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnNextResultsPage, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrPathwaysTable, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(pnlPathwayTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnCreateNetwork)
                                        .addComponent(chkSelectAll)
                                        .addComponent(btnEmptySelected)))
                );

                tabPathwaySelection.addTab("Pathways", pnlPathwayTab);

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
                this.setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(tabPathwaySelection, javax.swing.GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(tabPathwaySelection, javax.swing.GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE)
                );
        }// </editor-fold>//GEN-END:initComponents

    /**
     * When a pathway is selectedRows, this updates the details panel with its info
     * @param evt Button click event
     */
    private void btnCreateNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCreateNetworkActionPerformed
        //Disable button so it can't be pressed multiple times
        btnCreateNetwork.setEnabled(false);
        //Set networks to download in the database access class
        PathwayAccessPlugin.setNetworksToDownload(selectedNetworks);
        winManager.displayPnlNetworkSearch();
        /*
        //Cycle through all pathways to find which are selectedRows
        Iterator itr = selectedNetworks.iterator();
        while(itr.hasNext())
        {
            //TODO: Do on a seperate thread
            NetworkResultInterface netContainer = (NetworkResultInterface)itr.next();
            networkType net = (networkType)netContainer.getNetwork();
            PathwayAccessPlugin.getNetwork(net);
        }
        */
        //Re-enable button for future views of this panel
        btnCreateNetwork.setEnabled(true);
    }//GEN-LAST:event_btnCreateNetworkActionPerformed

    /**
     * When a pathway is clicked, this displays its info in the details panel
     * @param evt Click event
     */
    private void tblPathwaysMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblPathwaysMouseClicked
        //Check if table has any data
        if(tblPathways.getRowCount() == 0) return;
        //Check if the selection has changed
        int newRow = tblPathways.getSelectedRow();
        if(newRow == curRow) return; //Row hasn't changed
        curRow = newRow;
        if(curRow == -1) //No row is selectedRows
        {
            pnlDetails.setText("");
            return;
        }
        //Get the selectedRows pathway and update the pnlDetails panel with its info
        NetworkResultInterface selPath = (NetworkResultInterface)tblPathways.getValueAt(curRow, 1);
        if(selPath != null)
            pnlDetails.setText(selPath.getDetails());
    }//GEN-LAST:event_tblPathwaysMouseClicked

    /**
     * Selects/Deselects all checkboxes in the table
     * @param evt Checkbox select all mouse click event
     */
    private void chkSelectAllMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_chkSelectAllMouseClicked
        //Get current state
        boolean selectAll = chkSelectAll.isSelected();
        //Set all checkboxes accordingly
        for(int row = 0; row < tblPathways.getModel().getRowCount(); row++)
            tblPathways.setValueAt(selectAll, row, 0);
    }//GEN-LAST:event_chkSelectAllMouseClicked

    /**
     * Removes all network results in the filtered networks list from the 
     * network results table. If a filtered network was selected for creation,
     * it will no longer be created.
     * @param evt Button click event
     */
    /**
     * If a next page is available in the table, it is displayed and page
     * navigation components are updated.
     * @param evt Next Page button selection event
     */
    private void btnNextResultsPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextResultsPageActionPerformed
        enablePageComponents(tableModel.nextPage());
    }//GEN-LAST:event_btnNextResultsPageActionPerformed

    /**
     * If a previous page is availabe in the table, it is displayed and page
     * navigation components are updated.
     * @param evt Previous Page button selection event
     */
    private void btnPrevResultsPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevResultsPageActionPerformed
        enablePageComponents(tableModel.previousPage());
    }//GEN-LAST:event_btnPrevResultsPageActionPerformed

    private void btnEmptySelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEmptySelectedActionPerformed
        //Set all checkboxes to false
        for(int i = 0; i < moreData.length; i++)
            moreData[i][0] = new Boolean(false);
        selectedNetworks.clear();
        for(int row = 0; row < tblPathways.getModel().getRowCount(); row++)
            tblPathways.setValueAt(false, row, 0);
    }//GEN-LAST:event_btnEmptySelectedActionPerformed

    /**
     * Enables/Disables the page buttons according to the pages available and
     * updates the label showing the current results numbers currently in the table.
     * @param The index within all results available of the first result showing.
     * The index starts at zero (i.e. if there are 25 results per page and it's
     * the second page, the given index would be 25).
     */
    private void enablePageComponents(int firstResultIndex)
    {
        //Enable/Disable Next/Previous Page button
        btnPrevResultsPage.setEnabled(tableModel.hasPreviousPage());
        btnNextResultsPage.setEnabled(tableModel.hasNextPage());
        //Update results numbers currently displayed in the table
        int first = firstResultIndex + 1;
        int totalNumResults = tableModel.getTotalRowCount();
        int last = firstResultIndex + tableModel.getRowCount();
        //If no results, set numbers to all zeros
        if(totalNumResults == 0)
        {
            lblNumResultsDisplayed.setText("Results 0 to 0/0");
            return;
        }
        //If on last page, display the total # of results instead
        if(first < totalNumResults && (first + MAX_ROWS_DISPLAYED) > totalNumResults)
                last = totalNumResults;
        lblNumResultsDisplayed.setText(
                "Results " + first + " to " + last + "/" + totalNumResults);
    }

    /**
     * Given an array of network results, this method will set the
     * data in the network results table and create tabs in the filter tabs pane
     * for each filter that applies to the given networks.
     * @param networks List of networks to list in the table
     */
    public void setResults(NetworkResultInterface<networkType>[] networks)
    {
        if(networks == null)
            throw new NullPointerException("Null Array");
        //Create the network results table
        tableModel = new PathwayTableModel(networks);
        tblPathways.setModel(tableModel);
        //Update page navigation components
        enablePageComponents(0);
        //Get filter tables for the given networks
        ArrayList<FilterTable> filters = null;
        try {
            filters = PathwayAccessPlugin.getFilters(networks, properties);
        } catch(Exception e) {
            PopupMessages.databaseConnectionError(null, winManager, PathwayAccessPlugin.getProperties(), true, true);
        }
        //Create tabs for each filter table
        //TODO: do in seperate thread
//        tabPaneFilters.removeAll();
//        Iterator<FilterTable> itr = filters.iterator();
//        while(itr.hasNext())
//        {
//            FilterTable table = (FilterTable)itr.next();
//            tabPaneFilters.addTab(table.getTypeName(), table);
//        }
    }

    /**
     * Adds the given network to the list of networks to filter when all filters
     * are applied
     * @param networks Network to remove from lists of network results
     */
    public void addFilteredNetwork(NetworkResultInterface network)
    {
        if(network == null)
            throw new NullPointerException("Null Network");
        filteredNetworks.add(network);
        updateButtonApplyFilters();
    }

    /**
     * Removes the given network from the list of filtered networks. Filter is
     * applied when the apply all filter button is selected.
     * @param networks Network to remove from the filtered network results
     */
    public void removeFilteredNetwork(NetworkResultInterface network)
    {
        if(network == null)
            throw new NullPointerException("Null Network");
        if(filteredNetworks.contains(network))
            filteredNetworks.remove(network);
        updateButtonApplyFilters();
    }

    private void updateButtonApplyFilters()
    {
        //Enable/Disable Create Network button and update its text
//        if(filteredNetworks.size() == 0)
//        {
//            btnApplyFilters.setEnabled(true);
//            btnApplyFilters.setText("Apply No Filters to All Results");
//        } else if(filteredNetworks.size() == 1) {
//            btnApplyFilters.setEnabled(true);
//            btnApplyFilters.setText("Apply Filter (1) to All Results");
//        } else {
//            btnApplyFilters.setEnabled(true);
//            btnApplyFilters.setText("Apply Filters ("
//                    + filteredNetworks.size() + ") to All Results");
//        }
    }

    /**
     * Clears the table, sets the details panel text to the given text, and
     * sets buttons to default state.
     * @param details Text to set the details panel to
     */
    public void clearResults(String details)
    {
        //Clear the details panel
        pnlDetails.setText(details);
        //Clear the pathways table
        tblPathways.setModel(new javax.swing.table.DefaultTableModel());
        chkSelectAll.setSelected(false);
        //Clear the filters
        filteredNetworks = new ArrayList<NetworkResultInterface>();
//        tabPaneFilters.removeAll();
    }

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JButton btnCreateNetwork;
        private javax.swing.JButton btnEmptySelected;
        private javax.swing.JButton btnNextResultsPage;
        private javax.swing.JButton btnPrevResultsPage;
        private javax.swing.JCheckBox chkSelectAll;
        private javax.swing.JLabel lblNumResultsDisplayed;
        private javax.swing.JPanel pnlPathwayTab;
        private javax.swing.JScrollPane scrPathwaysTable;
        private javax.swing.JTabbedPane tabPathwaySelection;
        private javax.swing.JTable tblPathways;
        // End of variables declaration//GEN-END:variables

}
