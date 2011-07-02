/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.iastate.pathwayaccess.cytoscape.metnet.ResultListItems;

import edu.iastate.metnet.Entity;
import java.sql.Date;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.SearchResultInterface;

/**
 *
 * @author Greg
 */
public class EntitySearchResult implements SearchResultInterface {
    
    private Entity ent;
    private String name = null;
    private String type = null;
    private String organism = null;
    private int id = 0;
    private Date created = null;
    private String creator = null;
    private String source = null;

    public EntitySearchResult(Entity ent) {
        this.ent = ent;
        name = ent.name;
        if(name == null) name = "unknown";
        type = ent.type;
        if(type == null) type = "unknown";
        organism = ent.organism;
        if(organism == null) organism = "unknown";
        id = ent.id;
        created = ent.created;
        creator = ent.creator;
        if(creator == null) creator = "unknown";
        source = ent.source;
        if(source == null) source = "unknown";
    }

    public Entity getResult() {
        return ent;
    }

    public String getDetails() {
        String htmlDetails = "<html><h2>" + name + "</h2>"
                + "Type: " + type + "<br>"
                + "Organism: " + organism + "<br>"
                + "ID: " + id + "<br><br>"
                + "Creator: " + creator + "<br>"
                + "Source: " + source + "<br>";
        if(created == null)
            htmlDetails += ("Created: unknown");
        else
            htmlDetails += ("Created: " + created.toString());
        return htmlDetails;
    }

    public String getName() {
        return name;
    }

    public String getOrganismName() {
        return organism;
    }

    public String getResultTypeName() {
        //return "Entity";
        return type;
    }

    /**
     * Returns the name of the entity. This method is called by the list
     * of search result to get a string with which to respresent the result in
     * the the list.
     * @return The name of the entity
     */
    @Override
    public String toString()
    {
        return name;// + " [" + type + "]";
    }
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
    /*
    @Override
    public int hashCode()
    {

    }
    */
    /**
     * Compares this search result object to the given object and only returns
     * true if the given object is exactly the same as this.
     * @param o Object to compare equality
     * @return True if the given object is exactly equivalent to this search
     *         result.
     */
    @Override
    public boolean equals(Object o)
    {
        if(getClass() == o.getClass())
            if(((EntitySearchResult) o).getResult().id == this.getResult().id)
                return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.ent != null ? this.ent.hashCode() : 0);
        hash = 23 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 23 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 23 * hash + (this.organism != null ? this.organism.hashCode() : 0);
        hash = 23 * hash + this.id;
        hash = 23 * hash + (this.creator != null ? this.creator.hashCode() : 0);
        hash = 23 * hash + (this.source != null ? this.source.hashCode() : 0);
        return hash;
    }
}
