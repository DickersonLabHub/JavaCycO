package edu.iastate.pathwayaccess.cytoscape.ResultListItems;

/**
 * Represents a network which can be converted into a CyNetwork. When a user
 * chooses a search result, networks in which that search result is involed
 * are listed to convert into CyNetworks.
 * 
 * @author Greg Hazen   ghazen@iastate.edu
 */
public interface NetworkResultInterface<networkType> {

    /**
     * Returns true if the network result has been selected in the client UI
     * @return True if the network result has been selected in the client UI.
     */
    public boolean selected();
    /**
     * Returns the network object represented by this class
     * @return The network object represented by this class
     */
    public networkType getNetwork();
    /**
     * Returns HTML formatted details of interest about the network. This is
     * used by the details panel of the client UI to show a quick exerpt about
     * the selected network.
     * @return HTML formatted details of interest about the network
     */
    public String getDetails();
    /**
     * Returns the name of the network
     * @return The name of the network
     */
    public String getName();
    /**
     * Returns the name of the organism the network is in
     * @return The name of the organism the network is in
     */
    public String getOrganismName();
    /**
     * Returns the name of the network. This method is called by the table
     * of resulting networks to get a string with which to respresent the
     * network in the table.
     * @return The name of the network
     */
    @Override
    public String toString();
    /**
     * This method is implemented to allow easy comparison of other networks
     * found. The integer returned should be unique, therefore hashcodes from
     * mutiple objects included in this class should be included in the
     * claculations of the hashcode (i.e. if a network can be in multiple
     * organisms, the hashcode should include the hashcode from the network and
     * the organism, for example:
     * hashcode=5+79*netork.hashCode()+79*organism.hashCode() ). The goal is to
     * make this hash code completely unique.
     * @return An integer unique to this network result object
     */
    @Override
    public int hashCode();
    /**
     * Compares this network result object to the given object and only returns 
     * true if the given object is exactly the same as this.
     * @param o Object to compare equality
     * @return True if the given object is exactly equivalent to this network 
     *         result.
     */
    @Override
    public boolean equals(Object o);
}
