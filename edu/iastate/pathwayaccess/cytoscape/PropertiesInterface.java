/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.iastate.pathwayaccess.cytoscape;

import edu.iastate.pathwayaccess.cytoscape.ClientUI.pnlDetails;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.pnlNetworkTabs;
import edu.iastate.pathwayaccess.cytoscape.PathwayAccessPlugin;

/**
 *
 * @author Greg
 */
public interface PropertiesInterface {

    /**
     * Returns the name of the database
     * @return The name of the database
     */
    abstract public String getDatabaseName();
    /**
     * Returns the version of the database specific plugin
     * @return The version of the database specific plugin
     */
    abstract public String getVersion();
    /**
     * Returns the distributor of the database specific plugin
     * @return The distributor of the database specific plugin
     */
    abstract public String getDistributor();
    /**
     * Returns the URL to the database specific plugin's project
     * @return The URL to the database specific plugin's project
     */
    abstract public String projectURL();

    /**
     * Returns the class extending the PathwayAccessPluginInterface with which to
     * access all database interactions.
     * @return The class extending the PathwayAccessPluginInterface with which to
     *         access all database interactions.
     */
    //abstract public PathwayAccessPlugin getPathwayAccessPlugin();
    /**
     * Returns the singleton instance of the pnlNetworkTabs. The object MUST be 
     * singleton for the plugin to work properly. Singleton means there is no 
     * more than one single copy of the object, for example:
     *      if(obj == null)
     *          obj = new Singleton();
     *      return obj;
     * @return A singleton instance of the pnlNetworkTabs object
     */
    //abstract public pnlNetworkTabs getPnlNetworkTabs();
    /**
     * Returns the singleton instance of the pnlDetails. The object MUST be
     * singleton for the plugin to work properly. Singleton means there is no
     * more than one single copy of the object, for example:
     *      if(obj == null)
     *          obj = new Singleton();
     *      return obj;
     * @return A singleton instance of the pnlDetails object
     */
    //abstract public pnlDetails getPnlDetails();
    /**
     * Returns a name for the type of network the client will create in
     * Cytoscape (i.e. the MetNet database calls them pathways). One instance
     * when this is used is in the details panel when no search results are
     * selected the message, "Select a search result to see its details and
     * available <INSERT NAME HERE>(s)..."
     * @return A name for the type of network the client will create in
     * Cytoscape.
     */
    abstract public String getNetworkTypeName();
    /**
     * Returns an html formatted description of the purpose, uses, and other 
     * information about the database. Links are supported. This will display 
     * before a search has been performed, so this is useful for explaining to 
     * users what the database is used for.
     * @return An html formatted description of the purpose, uses, and other 
     * information about the database.
     */
    abstract public String databaseDescription();
    /**
     * Returns an array of the names of all highly connected entities in the
     * database.
     * @return An array of the names of all highly connected entities in the
     * database.
     */
    abstract public String[] getHighlyConnectedEntityNames();
}
