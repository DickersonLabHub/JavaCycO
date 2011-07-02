/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.iastate.pathwayaccess.cytoscape;

/**
 *
 * @author Greg
 */
public abstract class PluginProperties implements PropertiesInterface {
    /**
     * Title of the database specific plugin. NOTE: To maintain backwards
     * compatability between versions of the plugin, this Plugin Title should
     * never change. This is used when repeatedley downloading data from the
     * same database to search for identical data which is why changing this
     * title between plugin versions will break the plugin.
     */
    public final String PLUGIN_TITLE;
    /**
     * Description of the database specific plugin.
     */
    public final String PLUGIN_DESCRIPTION;

    public PluginProperties(String pluginTitle, String pluginDescription)
    {
        PLUGIN_TITLE = pluginTitle;
        PLUGIN_DESCRIPTION = pluginDescription;
    }
}
