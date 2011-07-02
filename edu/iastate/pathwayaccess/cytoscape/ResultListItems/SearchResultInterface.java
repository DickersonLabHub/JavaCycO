package edu.iastate.pathwayaccess.cytoscape.ResultListItems;

/**
 * Represents a search result found to be similar to the search criteria
 * specified by the user. When a search result is chosen in the client UI,
 * a list of networks involving that search result are listed to be converted
 * into a CyNetwork. This represents the search results.
 *
 * @author Greg Hazen   ghazen@iastate.edu
 * @author Curtis Ullerich <curtisu@iastate.edu>
 */
public interface SearchResultInterface {//<resultType> { Can be any kind of type

    /**
     * Returns the result object represented by this class
     * @return The result object represented by this class
     */
    public Object getResult();
    /**
     * Returns HTML formatted details of interest about the result. This is
     * used by the details panel of the client UI to show a quick exerpt about
     * the selected search result.
     * @return HTML formatted details of interest about the search result
     */
    public String getDetails();
    /**
     * Returns the name of the search result
     * @return The name of the search result
     */
    public String getName();
    /**
     * Returns the name of the organism the search result is involved in
     * @return The name of the organism the search result is involved in
     */
    public String getOrganismName();
    /**
     * Returns the name of the type of search result this represnents (i.e. if 
     * this object represents an interction, this may return "Interaction").
     * @return The name of the type of search result this represnents
     */
    public String getResultTypeName();
    /**
     * Returns the name of the search result. This method is called by the list
     * of search result to get a string with which to respresent the result in
     * the the list.
     * @return The name of the search result
     */
    @Override
    public String toString();
    /**
     * This method is implemented to allow easy comparison of other results
     * found. The integer returned should be unique, therefore hashcodes from
     * mutiple objects included in this class should be included in the
     * claculations of the hashcode (i.e. if a result can be in multiple
     * organisms, the hashcode should include the hashcode from the result and
     * the organism, for example:
     * hashcode=5+79*result.hashCode()+79*organism.hashCode() ). The goal is to 
     * make this hash code completely unique.
     * @return An integer unique to this search result object
     */
    @Override
    public int hashCode();
    /**
     * Compares this search result object to the given object and only returns
     * true if the given object is exactly the same as this.
     * @param o Object to compare equality
     * @return True if the given object is exactly equivalent to this search
     *         result.
     */
    @Override
    public boolean equals(Object o);

}
