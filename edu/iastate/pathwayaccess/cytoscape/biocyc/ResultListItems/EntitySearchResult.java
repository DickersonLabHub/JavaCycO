package edu.iastate.pathwayaccess.cytoscape.biocyc.ResultListItems;

import edu.iastate.pathwayaccess.cytoscape.ResultListItems.SearchResultInterface;
import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.PtoolsErrorException;

/**
 *
 * @author Greg
 */
public class EntitySearchResult implements SearchResultInterface {
    
    private Frame ent;
    private String name = null;
    private String type = null;
    private String organism = null;
    private String id = null;
    private String created = null;
    private String creator = null;
    private String source = null;
    private String comments = null;

    public EntitySearchResult(Frame ent) {
        this.ent = ent;
        try {
            name = ent.getCommonName(); //.name;
        } catch (PtoolsErrorException ex) {
            name = "unknown";
        }
        if(name == null) name = "unknown";
        type = ent.getGFPtype();//.type;
        if(type == null) type = "unknown";
        try {
           // organism = ent.getSlotValue("ORGANISM"); entities don't have slotValue//ent.organism;
            organism = ent.getOrganism().getSpecies();
        } catch (PtoolsErrorException ex) {
            organism = "unknown";
        }
        if(organism == null) organism = "unknown";
        id = ent.getLocalID();//.id;
        if(id == null) id = "unknown";
        try {
            created = ent.getSlotValue("CREATED"); //ent.created;
        } catch (PtoolsErrorException ex) {
            created = "unknown";
        }
        if(created == null) created = "unknown";
        try {
            creator = ent.getSlotValue("CREATOR"); //ent.creator;
        } catch (PtoolsErrorException ex) {
            creator = "unknown";
        }
        if(creator == null) creator = "unknown";
        try {
            source = ent.getSlotValue("SOURCE"); //ent.source;
        } catch (PtoolsErrorException ex) {
            source = "unknown";
        }
        if(source == null) source = "unknown";
        try {
            comments = ent.getComment(); //this is empty???
        } catch (PtoolsErrorException ex) {
            comments = "";
        }
        if(comments == null) comments = "";
    }

    public Frame getResult() {
        return ent;
    }

    public String getDetails() {
        String htmlDetails = "<html><h2>" + name + "</h2>"
                + "Type: " + type + "<br>"
                + "Organism: " + organism + "<br>"
                + "ID: " + id + "<br><br>"
                /*+ "Creator: " + creator + "<br>"
                + "Created: " + created + "<br>"
                + "Source: " + source + "<br>"*/ //For Now, this info isn't shown since it's not provided by javacyco
                + "Comments: " + comments + "<br>";
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
        return name;
    }
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
            if(((EntitySearchResult) o).getResult().getLocalID().equals(this.getResult().getLocalID()))
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
        hash = 23 * hash + (ent.getLocalID() != null ? ent.getLocalID().hashCode() : 0);
        hash = 23 * hash + (this.creator != null ? this.creator.hashCode() : 0);
        hash = 23 * hash + (this.source != null ? this.source.hashCode() : 0);
        return hash;
    }
}
