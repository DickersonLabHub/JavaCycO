/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.iastate.pathwayaccess.cytoscape.ClientUI;

import edu.iastate.pathwayaccess.cytoscape.PluginProperties;
import cytoscape.Cytoscape;
import java.awt.Component;
import javax.swing.JOptionPane;

/**
 *
 * @author Greg
 */
public class PopupMessages {

    /**
     * Display an error about the connection to the plugin's database.
     * @param parentComponent The Component to align the popup with; if null, the
     * Component returned by Cytoscape.getDesktop().getContentPane() is used.
     * @param winManager WindowManager to use for canceling tasks and closing the
     * plugin's JFrame
     * @param properties Plugin properties to get the the title of the plugin
     * @param cancelTasks If true, cancels any tasks currently running for the plugin.
     * @param closePlugin If true, closes the plugin's JFrame using the default action.
     * @return If cancelTasks is false, true is returned, otherwise false if the
     * cancel task could not be completed, typically because the currently running
     * task has already completed normally; true otherwise
     * @see WindowManager
     */
    public static boolean databaseConnectionError(Component parentComponent, WindowManager winManager,
            PluginProperties properties, boolean cancelTasks, boolean closePlugin)
    {
        if(winManager == null || properties == null)
            throw new NullPointerException("Parameter can't be null");
        if(parentComponent == null) parentComponent = Cytoscape.getDesktop().getContentPane();
        JOptionPane.showMessageDialog(null, "An error occured when connecting " +
                    "to the " + properties.PLUGIN_TITLE + " database. Please retry " +
                    "connecting again later.",
                    properties.PLUGIN_TITLE + " Connection Error", JOptionPane.ERROR_MESSAGE);
        boolean ret = true;
        if(cancelTasks)
            ret = winManager.cancel(true);
        if(closePlugin)
            winManager.close();
        return ret;
    }

    /**
     * Display an error about Cytoscape, cancels any tasks, and closes the plugin.
     * @param parentComponent The Component to align the popup with; if null, the
     * Component returned by Cytoscape.getDesktop().getContentPane() is used.
     * @param winManager WindowManager to use for canceling tasks and closing the
     * plugin's JFrame
     * @param properties Plugin properties to get the the title of the plugin
     * @return If cancelTasks is false, true is returned, otherwise false if the
     * cancel task could not be completed, typically because the currently running
     * task has already completed normally; true otherwise
     * @see WindowManager
     */
    public static boolean cytoscapeFatalError(Component parentComponent, WindowManager winManager,
            PluginProperties properties)
    {
        if(winManager == null || properties == null)
            throw new NullPointerException("Parameter can't be null");
        if(parentComponent == null) parentComponent = Cytoscape.getDesktop().getContentPane();
        JOptionPane.showMessageDialog(null, "An error occured in Cytoscape and " +
                properties.PLUGIN_TITLE + "needs to close. Please try again later " +
                "and contact support if the problem persists.",
                "Cytoscape Error", JOptionPane.ERROR_MESSAGE);
        boolean ret = winManager.cancel(true);
        winManager.close();
        return ret;
    }

    /**
     * Display an error about the of the network with this plugin.
     * @param parentComponent The Component to align the popup with; if null, the
     * Component returned by Cytoscape.getDesktop().getContentPane() is used.
     * @param winManager WindowManager to use for canceling tasks and closing the
     * plugin's JFrame
     * @param properties Plugin properties to get the the title of the plugin
     * @param cancelTasks If true, cancels any tasks currently running for the plugin.
     * @param closePlugin If true, closes the plugin's JFrame using the default action.
     * @return If cancelTasks is false, true is returned, otherwise false if the
     * cancel task could not be completed, typically because the currently running
     * task has already completed normally; true otherwise
     * @see WindowManager
     */
    public static boolean networkCompatibilityError(Component parentComponent, WindowManager winManager,
            PluginProperties properties, boolean cancelTasks, boolean closePlugin)
    {
        if(winManager == null || properties == null)
            throw new NullPointerException("Parameter can't be null");
        if(parentComponent == null) parentComponent = Cytoscape.getDesktop().getContentPane();
        JOptionPane.showMessageDialog(parentComponent, "The network doesn't contain the correct " +
                "synonym data. Please re-check the network's compatibility with " + properties.PLUGIN_TITLE + ".",
                "Network Data Error", JOptionPane.ERROR_MESSAGE);
        boolean ret = true;
        if(cancelTasks)
            ret = winManager.cancel(true);
        if(closePlugin)
            winManager.close();
        return ret;
    }

}
