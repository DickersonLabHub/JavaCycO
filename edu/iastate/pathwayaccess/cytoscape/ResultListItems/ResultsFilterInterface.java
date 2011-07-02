package edu.iastate.pathwayaccess.cytoscape.ResultListItems;

import java.util.ArrayList;

/**
 * Represents a network results filter. Potential networks in the client UI can
 * be filtered based on any object type involved in the network that this
 * interface is implemented for. For example, if this filter represents an
 * interaction, any networks involving that interaction will be removed from
 * the list of network results.
 *
 * @author Greg Hazen   ghazen@iastate.edu
 * @author Curtis Ullerich <curtisu@iastate.edu>
 */
public interface ResultsFilterInterface <FilterType> {

    /**
     * Returns true if the filter has been selected in the client UI
     * @return True if the filter has been selected in the client UI.
     */
    public boolean selected();
    /**
     * Sets the status of selected() to the given boolean
     */
    public void setSelected(Boolean sel);
    /**
     * Returns an array list of the networks this filter applies to.
     * @return An array list of the networks this filter applies to.
     */
    public ArrayList<NetworkResultInterface> resultsFiltered();
    /**
     * Resturns the number of networks this filter applies to.
     * @return The number of networks this filter applies to.
     */
    public int numResults();
    /**
     * Returns the filter object represented by this class
     * @return The filter object represented by this class
     */
    public FilterType getFilter();
    /**
     * Returns HTML formatted details of interest about the filter. This is
     * used by the details panel of the client UI to show a quick exerpt about
     * the selected filter.
     * @return HTML formatted details of interest about the filter
     */
    public String getDetails();
    /**
     * Returns the name of the filter
     * @return The name of the filter
     */
    public String getName();
    /**
     * Returns the name of the type of filter this represnents (i.e. if
     * this object represents an interction, this may return "Interaction").
     * @return The name of the type of search result this represnents
     */
    public String getFilterTypeName();
    /**
     * Returns the name of the filter. This method is called by the table
     * of filter to get a string with which to respresent the filter in
     * the the table.
     * @return The name of the filter
     */
    @Override
    public String toString();
    /**
     * This method is implemented to allow easy comparison of other filters
     * found. The integer returned should be unique, therefore hashcodes from
     * mutiple objects included in this class should be included in the
     * claculations of the hashcode (i.e. if a filter can be in multiple
     * organisms, the hashcode should include the hashcode from the filter and
     * the organism, for example:
     * hashcode=5+79*fileter.hashCode()+79*organism.hashCode() ). The goal is to
     * make this hash code completely unique.
     * @return An integer unique to this filter object
     */
    @Override
    public int hashCode();
    /**
     * Compares this filter object to the given object and only returns
     * true if the given object is exactly the same as this.
     * @param o Object to compare equality
     * @return True if the given object is exactly equivalent to this filter.
     */
    @Override
    public boolean equals(Object o);

}
