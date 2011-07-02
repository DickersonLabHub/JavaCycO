/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.iastate.pathwayaccess.cytoscape.metnet.ResultListItems;

import edu.iastate.pathwayaccess.cytoscape.biocyc.ResultListItems.PathwayNetworkResult;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.NetworkResultInterface;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.ResultsFilterInterface;
import edu.iastate.metnet.Interaction;
import edu.iastate.metnet.Pathway;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Greg
 */
public class InteractionFilter implements ResultsFilterInterface<Interaction> {
    
    private Interaction interaction;
    private Boolean selected = false;
    private ArrayList<NetworkResultInterface> filtered = new ArrayList<NetworkResultInterface>();
    private String cellLocation;
    private String created;
    private String creator;
    private int id;
    private String name;
    private String organism;
    private int strength;
    private int proreversal;
    private int timescale;
    private String type;

    public InteractionFilter(Interaction interaction)
    {
        if(interaction == null)
            throw new NullPointerException("Null Interaction");
        this.interaction = interaction;
        //Store information about the interaction
        this.cellLocation = interaction.cellLocation;
        if(cellLocation == null) cellLocation = "unknown";
        Date dateCreated = interaction.created;
        if(dateCreated == null)
            this.created = "unknown";
        else
            this.created = dateCreated.toString();
        this.creator = interaction.creator;
        if(creator == null) creator = "unknown";
        this.id = interaction.id;
        this.name = interaction.name;
        if(name == null) name = "unknown";
        this.organism = interaction.organism;
        if(organism == null) organism = "unknown";
        this.strength = interaction.strength;
        this.proreversal = interaction.proreversal;
        this.timescale = interaction.timescale;
        this.type = interaction.type;
        if(type == null) type = "unknown";
    }

    public boolean selected() {
        return selected;
    }

    public void setSelected(Boolean sel) {
        selected = sel;
    }

    /**
     * Adds the given pathway to the list of filtered network results
     * @param newFilter Pathway to add to the list of filtered networks
     */
    public void addFiltered(PathwayNetworkResult newFilter)
    {
        if(newFilter == null)
            throw new NullPointerException("Null Pathway");
        if(!filtered.contains(newFilter))
            filtered.add(newFilter);
    }

    public ArrayList<NetworkResultInterface> resultsFiltered() {
        return filtered;
    }

    public int numResults() {
        return filtered.size();
    }

    public Interaction getFilter() {
        return interaction;
    }

    public String getDetails() {
        String htmlDetails = "<html><h2>" + name + "</h2>"
                + "Type: " + type + "<br>"
                + "Organism: " + organism + "<br>"
                + "ID: " + id + "<br><br>"
                + "Creator: " + creator + "<br>"
                + "Date Created: " + created + "<br>"
                + "Cell Location: " + cellLocation + "<br>"
                + "Strength: " + strength + "<br>"
                + "Proreversal: " + proreversal + "<br>"
                + "Timescale: " + timescale + "<br></html>";
        return htmlDetails;
    }

    public String getName() {
        return name;
    }

    public String getFilterTypeName() {
        return "Interaction";
    }

    @Override
    public String toString()
    {
        return name + " (" + filtered.size() + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if(getClass() == o.getClass())
            if(((InteractionFilter) o).getFilter().id == this.id)
                return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.interaction != null ? this.interaction.hashCode() : 0);
        hash = 79 * hash + (this.cellLocation != null ? this.cellLocation.hashCode() : 0);
        hash = 79 * hash + (this.created != null ? this.created.hashCode() : 0);
        hash = 79 * hash + (this.creator != null ? this.creator.hashCode() : 0);
        hash = 79 * hash + this.id;
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 79 * hash + (this.organism != null ? this.organism.hashCode() : 0);
        hash = 79 * hash + this.strength;
        hash = 79 * hash + this.proreversal;
        hash = 79 * hash + this.timescale;
        hash = 79 * hash + (this.type != null ? this.type.hashCode() : 0);
        return hash;
    }
}
