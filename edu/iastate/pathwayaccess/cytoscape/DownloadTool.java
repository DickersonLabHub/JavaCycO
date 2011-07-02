package edu.iastate.pathwayaccess.cytoscape;

import edu.iastate.pathwayaccess.cytoscape.ClientUI.PopupMessages;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.WindowManager;
import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.Semantics;
import giny.model.RootGraph;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

/**
 * Offers all mehods to create CyNetworks from a given database
 * @author Greg
 */
public class DownloadTool<networkType, reactionType, participantType, modifierType> {

    /**
     * PathwayAccessPlugin to use for database backend methods
     */
    private PathwayAccessPlugin<networkType, reactionType, participantType, modifierType> database;
    /**
     * Stores the window manager to call when next or back buttons are pressed
     */
    private WindowManager winManager;
    /**
     * Tool for checking synonym relations
     */
    SynonymTool synonymTool;
    /**
     * CyNetwork to add downloaded data to
     */
    CyNetwork workingNetwork;
    /**
     * Stores the thread worker to update with task progress and check for
     * canceling. This can be used for any non-static methods only since the
     * initialized class is to only be used for a single download process
     * (the entire download process should be monitored by a single thread worker).
     */
    NewThreadWorker worker;
    /**
     * Stores whether to use synonyms when downloading new data to eliminate
     * duplicate nodes and edges.
     */
    boolean useSynonyms = true;

    /**
     * Creates a new DownloadTool
     * @param database PathwayAccessPlugin to use for database backend methods
     * @param synonymTool Initialized tool for checking synonym relations
     * @param network CyNetwork to add downloaded data to
     */
    public DownloadTool(PathwayAccessPlugin<networkType, reactionType, participantType, modifierType> database,
            WindowManager winManager, PluginProperties properties, CyNetwork network, boolean useSynonyms, NewThreadWorker worker)
    {
        this.database = database;
        this.winManager = winManager;
        this.useSynonyms = useSynonyms;
        synonymTool = new SynonymTool(winManager, properties, database.getProperties().PLUGIN_TITLE, network,
                useSynonyms, worker, winManager.getPluginFrame());
        workingNetwork = network;
        this.worker = worker;
    }

    /**
     * Downloads the given network(s) from the database into the given existing
     * CyNetwork chosen by the user. The user is updated on progress status using
     * the given worker.
     * @param netsToDownload Generic network object to download from database
     * and load into the given CyNetwork
     * @param network CyNetwork to load network from database into
     */
    public synchronized void download(ArrayList<networkType> netsToDownload, CyNetwork network)
    {
        worker.setStatusTitle("Waiting for another plugin to finish");
        //Wait for other CyNetworkSearchClient downloads to finish
        synchronized(System.out)
        {
            try
            {
                //For each network to download
                for(networkType pwy : netsToDownload)
                {
                    try {
                        worker.setStatusTitle("Loading reactions in "+database.getName(pwy));
                    } catch(Exception e) {
                        PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                    }
                    //For each reaction in the network to download
                    String netOrganism = null;
                    try {
                        netOrganism = database.getOrganism(pwy);
                    } catch(Exception e) {
                        PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                    }
                    if(netOrganism == null) netOrganism = PathwayAccessPlugin.UNKNOWN;
                    Iterator reactItr = null;
                    try {
                        reactItr = database.getNetworkReactions(pwy).iterator();
                    } catch(Exception e) {
                        PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                    }
                    while(reactItr.hasNext())//for(reactionType curReact : database.getNetworkReactions(pwy))
                    {
                        reactionType curReact = (reactionType) reactItr.next();
                        try {
                            worker.setStatusTitle("Importing reaction "+database.getName(curReact));
                        } catch(Exception e) {
                            PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                        }
                        //Get all sources, targets, and modifiers of the edge, if it doesn't have one skip it
                        Iterable<participantType> sources = null;
                        try {
                            sources = database.getEdgeSource(curReact);
                        } catch(Exception e) {
                            PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                        }
                        if(!sources.iterator().hasNext())
                                continue;
                        Iterable<participantType> targets = null;
                        try {
                            targets = database.getEdgeTarget(curReact);
                        } catch(Exception e) {
                            PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                        }
                        if(!targets.iterator().hasNext())
                                continue;
                        Iterable<modifierType> modifiers = null;
                        try {
                            modifiers = database.getEdgeModifiers(curReact);
                        } catch(Exception e) {
                            PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                        }

                        //Create the node representing the reaction
                        CyNode reactionNode = createCyNode(curReact, network, netOrganism, useSynonyms, SynonymTool.REACTION_NODE_PARTICIPATION_TYPE);
                        String type = null;
                        try {
                            type = database.getType(curReact);
                        } catch(Exception e) {
                            PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                        }
                        boolean reactionReversal = true;
                        if(type.equals(PathwayAccessPlugin.TRANSCRIPTION) || type.equals(PathwayAccessPlugin.TRANSLATION))
                            reactionReversal = false;

                        //Create CyNodes for each source, target, and modifier and connect
                        //to the reaction node via new CyEdge
                        try {
                            worker.setStatusTitle("Loading reaction "+database.getName(curReact));
                        } catch(Exception e) {
                            PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                        }
                        for(participantType participant : sources)
                        {
                            CyNode node = createCyNode(participant, network, netOrganism, useSynonyms, SynonymTool.PARTICIPANT_PARTICIPATION_TYPE);//.SOURCE_PARTICIPATION_TYPE);
                            CyEdge newEdge = Cytoscape.getCyEdge(node, reactionNode, Semantics.INTERACTION, type, true, reactionReversal);
                            try {
                                synonymTool.setEdgeAttributes(newEdge, type, database.getOrganism(curReact), database.getLocation(curReact));
                            } catch(Exception e) {
                                PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                            }
                            network.addEdge(newEdge);
                        }
                        for(participantType participant : targets)
                        {
                            CyNode node = createCyNode(participant, network, netOrganism, useSynonyms, SynonymTool.PARTICIPANT_PARTICIPATION_TYPE);//.TARGET_PARTICIPATION_TYPE);
                            CyEdge newEdge = Cytoscape.getCyEdge(reactionNode, node, Semantics.INTERACTION, type, true, reactionReversal);
                            try {
                                synonymTool.setEdgeAttributes(newEdge, type, database.getOrganism(curReact), database.getLocation(curReact));
                            } catch(Exception e) {
                                PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                            }
                            network.addEdge(newEdge);
                        }
                        //* NOT SURE WHETHER TO CREATE A CYNODE OR NOT
                        for(modifierType participant : modifiers)
                        {
                            CyNode node = createCyNode(participant, network, netOrganism, useSynonyms, SynonymTool.MODIFIER_PARTICIPATION_TYPE);
                            CyEdge newEdge = Cytoscape.getCyEdge(node, reactionNode, Semantics.INTERACTION, type, true, reactionReversal);
                            try {
                                synonymTool.setEdgeAttributes(newEdge, type, database.getOrganism(curReact), database.getLocation(curReact));
                            } catch(Exception e) {
                                PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
                            }
                            network.addEdge(newEdge);
                        }
                        //*/
                    }
                }
                //VisualTool.applyDefaultLayout(network, worker);
            }
            catch(Exception ex)
            {
                JOptionPane.showMessageDialog(winManager.getPluginFrame(), "An error" +
                        "occured when trying to download the data. Please retry " +
                        "downloading the data.",
                        "Download Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            finally
            {
                PathwayAccessPlugin.download.release();
            }
        }
    }

    /**
     * Create a CyNode based on a generic pathway database object (compound, gene, protein, etc)
     * using its name, id, type, and other information for implemented get methods.
     * Uses synonyms if frame's "Use Synonyms" checkbox is checked. Updates all
     * CyAttributes of the node returned and adds the new node to the given CyNetwork.
     * @param o the generic pathway database object
     * @param network CyNetwork in which to create the node or search for existing nodes
     * @param netOrganism Organism name from the containing network
     * @param useSynonyms True to use synonyms from the given object to find any matching existing nodes
     * @param partType Participation type the node has in its reaction (reaction node,
     * source, target, or modifier). Must use one of the constants defined in Synonym Tool.
     * @return the existing Cytoscape CyNode if one of the generic pathway database object's name already
     * exists in the given Cytoscape network, else a newly created one based on o.
     * @throws IllegalArgumentException When the given partType is not one of the
     * static constants defined in this class.
     * @see SynonymTool
     */
    private CyNode createCyNode(Object o, CyNetwork network, String netOrganism, boolean useSynonyms, String partType)
    {
        //Get basic data and synonyms
        String databaseName = null, name = null, type = null, org = null, id = null, locationName = null;
        ArrayList<String> names = null;
        try {
            databaseName = database.getClass().getName();
            name = database.getName(o);
            if(name == null) name = PathwayAccessPlugin.UNKNOWN;
            type = database.getType(o);
            if(type == null) type = PathwayAccessPlugin.UNKNOWN;
            org = database.getOrganism(o);
            if(org == null) org = netOrganism;
            else if(org.trim().equals("")) org = netOrganism;
            if(org == null) org = PathwayAccessPlugin.UNKNOWN;
            else if(org.trim().equals("")) org = PathwayAccessPlugin.UNKNOWN;
            id = database.getId(o);
            if(id == null) id = PathwayAccessPlugin.UNKNOWN;
            locationName = database.getLocation(o);
            if(locationName==null || locationName.toLowerCase().equals("cytoplasm")) locationName = PathwayAccessPlugin.CYTOSOL;
            else locationName = locationName.toLowerCase();
            worker.setStatusTitle("Loading "+name+" of type "+type+" in the "+locationName);
            names = database.getSynonyms(o);
            names.add(0,name);
        } catch(Exception e) {
            PopupMessages.databaseConnectionError(null, winManager, database.getProperties(), true, true);
        }
        //Check if the entity is a selected highly connected entity
        boolean highlyCon = false;
        for(String nm : names)
            if(database.isSelectedHighlyConnectedEntity(nm)) highlyCon = true;
        //Find any matching existing nodes, unless the node represents a reaction node
        CyNode aliasNode = null;
        if(!highlyCon) //Duplicate (don't use an existing node) selected highly connected entities
        {
            aliasNode = synonymTool.findNodeAlias(databaseName, names,
                org, locationName, type, id, useSynonyms, partType);
        }
        if(aliasNode!=null)
        {
            //Add any new attributes
            synonymTool.setNodeAttributes(databaseName, id, aliasNode,
                    names, org, locationName, type, partType);
            return aliasNode;
        }
        //Create the new CyNode
        RootGraph rootGraph = network.getRootGraph();
        //CyNode newNode = new CyNode(rootGraph, rootGraph.createNode());
        CyNode newNode = Cytoscape.getCyNode(rootGraph.createNode()+"", true);
        network.addNode(newNode);
        //Add attributes
        synonymTool.setNodeAttributes(databaseName, id, newNode,
                names, org, locationName, type, partType);
        //Add node to group based on location/compartment name
        /* TODO: Add the new node to a CyGroup based on location/compartment name
        PluginCompartment pcomp = getPluginCompartment(locationName);
        aliasNode.setFramePosition(pcomp.getX()+offsetInCompartment, pcomp.getY()+offsetInCompartment);
        newNode.setCompartment(pcomp.getId());

        if(aliasNode.getType().equals(PluginSpeciesSymbolType.COMPLEX))
        {
            aliasNode.setFrameSize(50.0,50.0);
            aliasNode.setFramePosition(aliasNode.getX(),aliasNode.getY()+this.compartmentH/2.0);
        }
        */
        return newNode;
    }
}
