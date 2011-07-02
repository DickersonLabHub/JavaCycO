package edu.iastate.pathwayaccess.cytoscape.biocyc.ResultListItems;

import edu.iastate.javacyco.Pathway;
import edu.iastate.javacyco.PtoolsErrorException;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.NetworkResultInterface;

/**
 *
 * @author Greg
 */
public class PathwayNetworkResult implements NetworkResultInterface<Pathway> {
    private boolean selected = false;
    private Pathway path;
    private String name;
    private String organism;
    private String id;
    private String created;
    private String creator;
    private String comments;
    private String type;

    public PathwayNetworkResult(Pathway path)
    {
        this.path = path;
        try {
            name = path.getCommonName(); //.name;
        } catch (PtoolsErrorException ex) {
            name = "unknown";
        }
        if(name == null) name = "unknown";
        type = path.getGFPtype();//.type;
        if(type == null) type = "unknown";
        try {
            organism = path.getOrganism().getSpecies();
            //path.getSlotValue("ORGANISM"); didn't work because pathways didn't have a slot for organism //path.organism; //RETURNS NULL HERE!!!
        } catch (PtoolsErrorException ex) {
            organism = "unknown";
        }
        if(organism == null) organism = "unknown";
        id = path.getLocalID();//.id;
        if(id == null) id = "unknown";
        try {
            created = path.getSlotValue("CREATED"); //path.created;
        } catch (PtoolsErrorException ex) {
            created = "unknown";
        }
        if(created == null) created = "unknown";
        try {
            creator = path.getSlotValue("CREATOR"); //path.creator;
        } catch (PtoolsErrorException ex) {
            creator = "unknown";
        }
        if(creator == null) creator = "unknown";
        try {
            comments = path.getComment();
        } catch (PtoolsErrorException ex) {
            comments = "";
        }
        if(comments == null) comments = "";
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
                + "Creator: " + creator + "<br>"
                + "Created: " + created + "<br>"
                + "Comments: " + comments + "<br>";
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
            if(((PathwayNetworkResult) o).getNetwork().getLocalID().equals(this.getNetwork().getLocalID()))
                return true;
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.path != null ? this.path.hashCode() : 0);
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 97 * hash + (this.organism != null ? this.organism.hashCode() : 0);
        hash = 23 * hash + (path.getLocalID() != null ? path.getLocalID().hashCode() : 0);
        hash = 97 * hash + (this.creator != null ? this.creator.hashCode() : 0);
        return hash;
    }

}
