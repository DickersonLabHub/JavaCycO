package edu.iastate.pathwayaccess.cytoscape.ClientUI;

import edu.iastate.pathwayaccess.cytoscape.PathwayAccessPlugin;
import edu.iastate.pathwayaccess.cytoscape.NewThreadWorker;
import edu.iastate.pathwayaccess.cytoscape.PluginProperties;
import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeAction;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;

/**
 *
 * @author Greg
 */
public class WindowManager<networkType, reactionType, participantType, modifierType> extends CytoscapeAction {

    /**
     * Stores class containing all methods for accessing the database
     */
    private PathwayAccessPlugin<networkType, reactionType, participantType, modifierType> database;
    /**
     * Stores the plugin properties specific to the current database
     */
    private PluginProperties properties = null;

    /**
     *  This constructor creates an action and adds it to the Plugins menu.
     */
    public WindowManager(PathwayAccessPlugin<networkType, reactionType, participantType, modifierType> database,
            PluginProperties properties)
    {
        //Send super constructer the title of the plugin for the Plugins menu
        super(properties.PLUGIN_TITLE);
        //Stores parameters
        this.database = database;
        this.properties = properties;
    }
    
    /**
     * This method is called when the plugin is chosen from the Plugins menu
     * in Cytoscape. It creates the intial main frame of the plugin's GUI.
     * @param ae
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        //getPluginFrame();
        pluginFrame = new frmPluginFrame(database, this, properties);
        pluginFrame.showFrame(getPnlMainPanel());
        //Set the location of the window relative to Cytoscape
        pluginFrame.setLocationRelativeTo(Cytoscape.getDesktop().getContentPane());
        pluginFrame.setVisible(true);
    }

    /**
     * Main JFrame to hold all GUI panels for the plugin
     */
    private frmPluginFrame pluginFrame = null;
    /**
     * Returns the JFrame containing the user interface for the plugin or null
     * if the plugin hasn't been selected from the plugins menu yet.
     * @return The JFrame containing the user interface for the plugin or null
     * if the plugin hasn't been selected from the plugins menu yet.
     */
    public JFrame getPluginFrame()
    {
        if(pluginFrame == null)
            pluginFrame = new frmPluginFrame(database, this, properties);
        return pluginFrame;
    }
    /**
     * A singleton instance of the pnlMainPanel (GUI containing the search panel
     * to type and search for search terms). The variable being singleton allows
     * the plugin to be closed and re-opened to the same window and results.
     */
    private pnlMainPanel pnlMainPanel = null;
    /**
     * Returns the singleton instance of the pnlMainPanel. The object MUST be
     * singleton for the plugin to work properly. Singleton means there is no
     * more than one single copy of the object, for example:
     *      if(obj == null)
     *          obj = new Singleton();
     *      return obj;
     * @return A singleton instance of the pnlNetworkTabs object
     */
    protected pnlMainPanel getPnlMainPanel() {
        if(pnlMainPanel == null)
            pnlMainPanel = new pnlMainPanel(database, this, properties, getPluginFrame());
        return pnlMainPanel;
    }
    /**
     * Returns the organism selected by the user. It can be anything returned by
     * PathwayAccessPlugin.getOrganismNames(), including "All Organisms"
     * @return The organism selected by the user
     * @see PathwayAccessPlugin
     * @see pnlMainPanel
     */
    protected String getSelectedOrganism()
    {
        return pnlMainPanel.getOrganism();
    }
    /**
     * A singleton instance of the pnlNetworkTabs (GUI containing a list of all
     * pathways and filters retrieved from the database). The variable being
     * singleton allows the plugin to be closed and re-opened to the same window
     * and results.
     */
    private pnlNetworkTabs<networkType> pnlNetworkTabs = null;
    /**
     * Returns the singleton instance of the pnlNetworkTabs. The object MUST be
     * singleton for the plugin to work properly. Singleton means there is no
     * more than one single copy of the object, for example:
     *      if(obj == null)
     *          obj = new Singleton();
     *      return obj;
     * @return A singleton instance of the pnlNetworkTabs object
     */
    protected pnlNetworkTabs<networkType> getPnlNetworkTabs() {
        if(pnlNetworkTabs == null)
            pnlNetworkTabs = new pnlNetworkTabs<networkType>(database, this);
        return pnlNetworkTabs;
    }
    /**
     * A singleton instance of the pnlDetails (GUI containing information about
     * the selected object in the search results). The variable being singleton
     * allows the plugin to be closed and re-opened to the same window and
     * results.
     */
    private pnlDetails pnlDetails = null;
    /**
     * Returns the singleton instance of the pnlDetails. The object MUST be
     * singleton for the plugin to work properly. Singleton means there is no
     * more than one single copy of the object, for example:
     *      if(obj == null)
     *          obj = new Singleton();
     *      return obj;
     * @return A singleton instance of the pnlDetails object
     */
    protected pnlDetails getPnlDetails() {
        if(pnlDetails == null)
            pnlDetails = new pnlDetails(getPluginFrame());
        return pnlDetails;
    }
    /**
     * Displays the panel of results. This should ONLY be called to go back from 
     * the pnlSelectNetwork without having to create a new search.
     */
    protected void displayPnlSearchResults()
    {
        pnlMainPanel.displayPnlSearchResults();
    }
    /**
     * Displays the Network Search panel for advanced options and choosing the
     * CyNetwork to download data to.
     */
    protected void displayPnlNetworkSearch()
    {
        pnlMainPanel.displayPnlNetworkSearch();
    }
    /**
     * Stores the Thread Worker for any tasks whos progress is displayed in the
     * Main Panel's status area.
     */
    NewThreadWorker worker = null;
    /**
     * Sets the status bar Thread Worker to the given Thread Worker to recieve updates
     * @param worker Thread Worker for the status bar to recieve updates from
     * @param indeterminate True if the progress bar status is indeterminate
     */
    public void setStatusWorker(NewThreadWorker worker, boolean indeterminate)
    {
        this.worker = worker;
        pnlMainPanel.setStatusWorker(worker, indeterminate);
    }
    /**
     * Performs the close operation on the pluing's frame as set by the frame's 
     * setDefaultCloseOperation() method. The default is set to hide the frame 
     * (EXIT_ON_CLOSE results in hiding the frame).
     * @see javax.swing.JFrame
     */
    public void close()
    {
        int closeOp = pluginFrame.getDefaultCloseOperation();
        if(closeOp == JFrame.DISPOSE_ON_CLOSE)
            pluginFrame.dispose();
        else if(closeOp == JFrame.HIDE_ON_CLOSE || closeOp == JFrame.EXIT_ON_CLOSE)
            pluginFrame.setVisible(false);
        return;
    }
    /**
     * Cancels any network creation specific to this database in progress. This
     * method does not close the plugin's frame; to do this, this method should
     * be followed by a call to the WindowManager.close() method.
     * @param mayInterruptIfRunning True if the thread executing this task
     * should be interrupted; otherwise, in-progress tasks are allowed to complete
     * @return False if the task could not be cancelled, typically because it has
     * already completed normally; true otherwise
     * @see NewThreadWorker
     */
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        if(worker != null)
            return worker.cancel(mayInterruptIfRunning);
        return true;
    }
}
