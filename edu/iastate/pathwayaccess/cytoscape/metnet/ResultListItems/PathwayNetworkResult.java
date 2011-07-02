package edu.iastate.pathwayaccess.cytoscape.metnet.ResultListItems;

import edu.iastate.pathwayaccess.cytoscape.ResultListItems.NetworkResultInterface;
import edu.iastate.metnet.Pathway;
import java.sql.Date;

/**
 *
 * @author Greg
 */
public class PathwayNetworkResult implements NetworkResultInterface<Pathway> {
    private boolean selected = false;
    private Pathway path;
    private String name;
    private String organism;
    private int id;
    private Date created;
    private String creator;

   public PathwayNetworkResult(Pathway path)
    {
        this.path = path;
        name = path.name;
        if(name == null) name = "unknown";
        organism = path.organism;
        if(organism == null) organism = "unknown";
        id = path.id;
        created = path.created;
        creator = path.creator;
        if(creator == null) creator = "unknown";
    }

    public boolean selected() {
        return selected;
    }

    public Pathway getNetwork() {
        return path;
    }

    public String getDetails() {
        String htmlDetails = "<html><h2>" + name + "</h2>"
                + "Type: Pathway<br>"
                + "Organism: " + organism + "<br>"
                + "ID: " + id + "<br><br>"
                + "Creator: " + creator + "<br>";
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

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if(getClass() == o.getClass())
            if(((PathwayNetworkResult) o).getNetwork().id == this.getNetwork().id)
                return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.path != null ? this.path.hashCode() : 0);
        hash = 59 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 59 * hash + (this.organism != null ? this.organism.hashCode() : 0);
        hash = 59 * hash + this.id;
        hash = 59 * hash + (this.creator != null ? this.creator.hashCode() : 0);
        return hash;
    }

}
