package edu.iastate.pathwayaccess.cytoscape;

import edu.iastate.pathwayaccess.cytoscape.ClientUI.PopupMessages;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.WindowManager;
import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Greg
 */
public class SynonymTool {

    /*
     * NOTE: The following attribute names should never be changed to keep
     * backwards compatability with previous version of this plugin. Networks
     * will be saved with these attributes under these exact names and the
     * plugin will not work without these attributes.
     */

    /**
     * The attribute name used to store the list of Strings of synonyms.
     * 
     * Synonym names are stored in the CyAttributes as a SIMPLE LIST; use
     * CyAttributes.getListAttribute(String id, String attributeName) to
     * retrieve the list of synonyms. To edit the list of synonyms use
     * setListAttribute() with a completely new List, for example:
     * <code>
     * List list = nodeAttributes.getListAttribute(id, attributeName);
     * //  Add new item
     * list.add(new String("Hello, World");
     * //  Save modified list back
     * nodeAttributes.setListAttribute(id, attributeName, list);
     * </code>
     */
    public static final String SYNONYM_ATTRIBUTE_NAME = "CyNetworkSearchClient_Synonyms";

    /**
     * The attribute name used to store the list of Strings of hash codes for
     * each node/edge. Each node/edge created by CyNetworkSearchClient stores a hash code
     * made from data true for all nodes/edges that are the same, but from different
     * databses, along with the unique identifier of the corresponding CyNode/CyEdge.
     *
     * Hash codes are stored in the CyAttributes as a SIMPLE LIST; use
     * CyAttributes.getListAttribute(String id, String attributeName) to
     * retrieve the list of hash codes. To edit the list of hash codes use
     * setListAttribute() with a completely new List, for example:
     * <code>
     * List list = nodeAttributes.getListAttribute(id, attributeName);
     * //  Add new item
     * list.add(new String("Hello, World");
     * //  Save modified list back
     * nodeAttributes.setListAttribute(id, attributeName, list);
     * </code>
     */
    public static final String HASH_ATTRIBUTE_NAME = "CyNetworkSearchClient_Hash";

    /**
     * The attribute name used to store the list of Strings of database name + ID combinations for
     * each node. Each node created by CyNetworkSearchClient stores this comboination so when nodes
     * are created repetitivley by the same database, the unique identifier from that database
     * allows quick identification of the same node.
     *
     * The combinations are stored in the CyAttributes as a SIMPLE LIST; use
     * CyAttributes.getListAttribute(String id, String attributeName) to
     * retrieve the list of Strings. To edit the list of Strings use
     * setListAttribute() with a completely new List, for example:
     * <code>
     * List list = nodeAttributes.getListAttribute(id, attributeName);
     * //  Add new item
     * list.add(new String("Hello, World");
     * //  Save modified list back
     * nodeAttributes.setListAttribute(id, attributeName, list);
     * </code>
     */
    public static final String DATABASE_ID_ATTRIBUTE_NAME = "CyNetworkSearchClient_DatabaseID";

    /**
     * The attribute name used to store the list of Strings of interaction types
     * each edge represents. Each interaction type stored in the list should be the
     * same, but spelling may be different depending on database.
     *
     * Interaction Types are stored in the CyAttributes as a SIMPLE LIST; use
     * CyAttributes.getListAttribute(String id, String attributeName) to
     * retrieve the list of interactions. To edit the list of interactions use
     * setListAttribute() with a completely new List, for example:
     * <code>
     * List list = nodeAttributes.getListAttribute(id, attributeName);
     * //  Add new item
     * list.add(new String("Hello, World");
     * //  Save modified list back
     * nodeAttributes.setListAttribute(id, attributeName, list);
     * </code>
     */
    //public static final String REACTION_TYPE_ATTRIBUTE_NAME = "CyNetworkSearchClient_ReactType";
    /**
     * The attribute name used to store the name of an edge interaction type.
     * The interaction type is stored only as a single String so only one
     * interaction type can be stored per edge.
     */
    public static final String INTERACTION_TYPE_ATTRIBUTE_NAME = cytoscape.data.Semantics.INTERACTION;
    /**
     * The attribute name used to store the name of a node type. The node type
     * is stored only as a single String so only one node type can be stored per
     * node.
     */
    public static final String NODE_TYPE_ATTRIBUTE_NAME = "CyNetworkSearchClient_NodeType";
    /**
     * The attribute name used to store the name of the type of participation a
     * node has in a reaction (Rreaction Node, Source, Target, or Modifier). The
     * type is stored only as a single String so only one type can be stored per
     * node.
     */
    public static final String PARTICIPATION_TYPE_ATTRIBUTE_NAME = "CyNetworkSearchClient_ParticipationType";
    /**
     * The attribute name used to store the organism name. The
     * organism name is stored only as a single String so only one organism
     * name can be stored per node.
     */
    public static final String ORGANISM_ATTRIBUTE_NAME = "CyNetworkSearchClient_OrganismName";
    /**
     * The attribute name used to store the name of a compartment name. The 
     * compartment name is stored only as a single String so only one compartment 
     * name can be stored per node.
     */
    public static final String COMPARTMENT_ATTRIBUTE_NAME = "CyNetworkSearchClient_CompartName";

    /**
     * A participation type of a node in a reaction. This type represents the
     * node representing the entire reaction.
     */
    public static final String REACTION_NODE_PARTICIPATION_TYPE = "Reaction";
    /* NODES CAN BE BOTH A TARGET AND A SOURCE AND WE ONLY WANT A SINGLE
     * ATTRIBUTE SO SOURCE AND TARGET HAVE BEEN REPLACED BY JUST "PARTICIPANT".
     */
    /**
     * A participation type of a node in a reaction. This type represents the
     * node representing a source in the reaction.
     */
    //public static final String SOURCE_PARTICIPATION_TYPE = "Source";
    /**
     * A participation type of a node in a reaction. This type represents the
     * node representing a target in the reaction.
     */
    //public static final String TARGET_PARTICIPATION_TYPE = "Target";
    /**
     * A participation type of a node in a reaction. This type represents the
     * node representing a partipant in the reaction.
     */
    public static final String PARTICIPANT_PARTICIPATION_TYPE = "Participant";
    /**
     * A participation type of a node in a reaction. This type represents the
     * node representing a modifier in the reaction.
     */
    public static final String MODIFIER_PARTICIPATION_TYPE = "Modifier";

    /**
     * Stores the window manager to call when next or back buttons are pressed
     */
    private WindowManager winManager;
    /**
     * Stores the plugin properties specific to the current plugin
     */
    private PluginProperties properties = null;
    /**
     * For each node in the current CyNetwork, this stores a list of synonym
     * names that other database sources may recognize it by. Along with each
     * list of synonyms is stored the unique identifier of the existing CyNode
     * it corresponds to.
     */
    protected HashMap<String,String> synonymsDatabase = new HashMap();

    /**
     * For each edge in the current CyNetwork, this stores a hash(es) from
     * information true for all edges that are the same, but from different databses, along
     * with the unique identifier of the corresponding existing CyNode. Multiple
     * hashes can be stored for each CyNode since each CyNode can have a list of
     * reaction types; a hash is stored for each reaction type.
     */
    protected HashMap<String,String> reactionHashes = new HashMap();

    /**
     * Stores the thread worker to update with task progress and check for
     * canceling. This can be used for any non-static methods only since the
     * initialized class is to only be used for a single download process
     * (the entire download process should be monitored by a single thread worker).
     */
    NewThreadWorker worker;
    /**
     * JFrame with which to align popup messages to the user
     */
    JFrame frame;

    /**
     * Creates a new Synonym Tool and stores data for all nodes in the given
     * CyNetwork corresponding to their synonyms for quick access. This class
     * should only be initialized and stored for a single download as the
     * given CyNetwork can change at any time.
     * @param databaseName Name of database class extending PathwayAccessPlugin (from
     * PathwayAccessPlugin use this.getClass().getName())
     * @param network CyNetwork to create synonym data for
     * @param useSynonyms True to use synonym data when calling methods in this class
     * @param worker The thread worker to update with task progress and check for
     * @param frame JFrame with which to align popup messages to the user
     * canceling (the entire download process should be monitored by a single thread worker)
     */
    public SynonymTool(WindowManager winManager, PluginProperties properties, String databaseName,
            CyNetwork network, Boolean useSynonyms, NewThreadWorker worker, JFrame frame)
    {
        this.worker = worker;
        this.frame = frame;
        this.winManager = winManager;
        this.properties = properties;
        updateHashes(databaseName, network, useSynonyms);
    }

    /**
     * Checks that the given CyNetwork has CyAttributes which are
     * compatible with this plugin. Uncompatible CyNetworks include CyAttributes
     * with the same names as defined by static constants in this class, but are
     * of a different CyAttributes type (i.e. Boolean, String, Complex...).
     * Canceling of the given Thread Worker is supported.
     * @param network CyNetwork to check for compatability
     * @return False if any CyNetworks in the current Cytoscape session define
     * CyAttributes of the same name as defined by static constants in this
     * class, but are of a different CyAttribute types. Otherwise true.
     * @see CyAttributes
     */
    public static boolean isCompatible(CyNetwork network, NewThreadWorker worker)
    {
        if(worker.isCancelled()) return false;
        //boolean isCompatible = true;
        CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
        CyAttributes edgeAttrs = Cytoscape.getEdgeAttributes();
        //Check Simple List CyAttributes
        byte type = nodeAttrs.getType(SYNONYM_ATTRIBUTE_NAME);
        if(type != CyAttributes.TYPE_SIMPLE_LIST && type != CyAttributes.TYPE_UNDEFINED)
            return false; //isCompatible = false;
        type = nodeAttrs.getType(HASH_ATTRIBUTE_NAME);
        if(type != CyAttributes.TYPE_SIMPLE_LIST && type != CyAttributes.TYPE_UNDEFINED)
            return false; //isCompatible = false;
        type = edgeAttrs.getType(HASH_ATTRIBUTE_NAME);
        if(type != CyAttributes.TYPE_SIMPLE_LIST && type != CyAttributes.TYPE_UNDEFINED)
            return false; //isCompatible = false;
        type = nodeAttrs.getType(DATABASE_ID_ATTRIBUTE_NAME);
        if(type != CyAttributes.TYPE_SIMPLE_LIST && type != CyAttributes.TYPE_UNDEFINED)
            return false; //isCompatible = false;
        if(worker.isCancelled()) return false;
        //Check that each Simple List has the correct invdividual CyAttribute types
        Iterator nodesIter = network.nodesIterator();
        while(nodesIter.hasNext() && (!worker.isCancelled()))
        {
            CyNode curNode = (CyNode)(nodesIter.next());
            try {
                List synList = nodeAttrs.getListAttribute(curNode.getIdentifier(), SYNONYM_ATTRIBUTE_NAME);
                if(synList != null && synList.size() > 0)
                    if(synList.get(0).getClass() != String.class) return false; //isCompatible = false;
                List hashList = nodeAttrs.getListAttribute(curNode.getIdentifier(), HASH_ATTRIBUTE_NAME);
                if(hashList != null && hashList.size() > 0)
                    if(hashList.get(0).getClass() != String.class) return false; //isCompatible = false;
                List idList = nodeAttrs.getListAttribute(curNode.getIdentifier(), DATABASE_ID_ATTRIBUTE_NAME);
                if(idList != null && idList.size() > 0)
                    if(idList.get(0).getClass() != String.class) return false; //isCompatible = false;
            } catch(ClassCastException e) { return false; } //isCompatible = false; }
        }
        Iterator edgeIter = network.edgesIterator();
        while(edgeIter.hasNext() && (!worker.isCancelled()))
        {
            CyEdge curEdge = (CyEdge)(edgeIter.next());
            try {
                List hashList = edgeAttrs.getListAttribute(curEdge.getIdentifier(), HASH_ATTRIBUTE_NAME);
                if(hashList != null && hashList.size() > 0)
                    if(hashList.get(0).getClass() != String.class) return false; //isCompatible = false;
            } catch(ClassCastException e) { return false; } //isCompatible = false; }
        }
        if(worker.isCancelled()) return false;
        //Check individual String CyAttributes
        type = edgeAttrs.getType(INTERACTION_TYPE_ATTRIBUTE_NAME);
        if(type != CyAttributes.TYPE_STRING && type != CyAttributes.TYPE_UNDEFINED)
            return false; //isCompatible = false;
        type = nodeAttrs.getType(NODE_TYPE_ATTRIBUTE_NAME);
        if(type != CyAttributes.TYPE_STRING && type != CyAttributes.TYPE_UNDEFINED)
            return false; //isCompatible = false;
        type = nodeAttrs.getType(PARTICIPATION_TYPE_ATTRIBUTE_NAME);
        if(type != CyAttributes.TYPE_STRING && type != CyAttributes.TYPE_UNDEFINED)
            return false; //isCompatible = false;
        type = nodeAttrs.getType(ORGANISM_ATTRIBUTE_NAME);
        if(type != CyAttributes.TYPE_STRING && type != CyAttributes.TYPE_UNDEFINED)
            return false; //isCompatible = false;
        type = edgeAttrs.getType(ORGANISM_ATTRIBUTE_NAME);
        if(type != CyAttributes.TYPE_STRING && type != CyAttributes.TYPE_UNDEFINED)
            return false; //isCompatible = false;
        type = nodeAttrs.getType(COMPARTMENT_ATTRIBUTE_NAME);
        if(type != CyAttributes.TYPE_STRING && type != CyAttributes.TYPE_UNDEFINED)
            return false; //isCompatible = false;
        type = edgeAttrs.getType(COMPARTMENT_ATTRIBUTE_NAME);
        if(type != CyAttributes.TYPE_STRING && type != CyAttributes.TYPE_UNDEFINED)
            return false; //isCompatible = false;
        return true; //isCompatible;
    }

    /**
     * Checks that all edges in the network have a hash code attribute for each
     * reaction type name (edge is assumed to already have an attribute list of
     * all reaction type names specified by REACTION_TYPE_ATTRIBUTE_NAME). The
     * hash map reactionHashes is filled with all of the hash codes from each
     * edge in the given network for fast comparison later.
     * Canceling of the given Thread Worker is supported.
     * @param databaseName Name of database class extending PathwayAccessPlugin (from
     * PathwayAccessPlugin use this.getClass().getName())
     * @param network Network to retrieve synonym data from
     * @param useSynonyms True to check for synonyms in of nodes
     */
    private void updateHashes(String databaseName, CyNetwork network, Boolean useSynonyms)
    {
        if(worker.isCancelled()) return;
        reactionHashes.clear();
        //Fill reactionHashes with hash codes of all edges in the current network
        //along with their associated edge ID
        //CyEdge[] edges = (CyEdge[]) Cytoscape.getCyEdgesList().toArray();
        //Method 1
        /*
        int[] edgeInd = network.getEdgeIndicesArray();
        for(int i = 0; i < edgeInd.length; i++){
            CyEdge node = (CyEdge) (network.getEdge(edgeInd[i]));}
        //Method 2
        Object[] arr = network.edgesList().toArray();
        for(int i = 0; i < edgeInd.length; i++){
            CyEdge node = (CyEdge) (arr[i]);}
        */
        //int rootGraphIndexOfEdge = network.getRootGraph().getIndex(edges[0]);
        List list = network.edgesList();
        if(list == null)
            list = new ArrayList();
        //Get all edge attributes once
        CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
        //Object[] arr = list.toArray();
        for(Object obj : list) //(CyEdge[])(network.edgesList().toArray()))
        {
            if(worker.isCancelled()) return;
            CyEdge curCyEdge = (CyEdge) obj;
            //Get any existing hashcodes for the current CyEdge
            String curID = curCyEdge.getIdentifier();
            List<String> hashes = null;
            try {
                hashes = edgeAttributes.getListAttribute(curID, HASH_ATTRIBUTE_NAME);
            } catch (ClassCastException e) { }
            String interaction;
            try {
                interaction = edgeAttributes.getStringAttribute(
                        curCyEdge.getIdentifier(), INTERACTION_TYPE_ATTRIBUTE_NAME);
                if(interaction == null)
                    interaction = PathwayAccessPlugin.UNKNOWN;
            } catch (ClassCastException e) { //CyAttribute is of the wrong type
                PopupMessages.networkCompatibilityError(frame, winManager, properties, true, false);
                return;
            }
            //Make sure hash is up to date for current edge
            String newHash = getHashCode(curCyEdge, interaction)+"";
            if(hashes == null)
                hashes = new ArrayList();
            if(!hashes.contains(newHash))
            {
                hashes.add(newHash);
                edgeAttributes.setListAttribute(curID, HASH_ATTRIBUTE_NAME, hashes);
            }
            //Store all hash codes in reactionHashes for fast retrieval
            for(String hash : hashes)
                reactionHashes.put(hash,curID);
        }
        if(worker.isCancelled()) return;
        //Fill synonymsDatabase with all node synonyms and their corresponding node ID
        synonymsDatabase.clear();
        List nodeList = network.nodesList();
        if(nodeList == null)
            nodeList = new ArrayList();
        //Get all node attributes once
        CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
        for(Object obj : nodeList) //(CyNode[])network.nodesList().toArray())
        {
            CyNode sb = (CyNode) obj;
            if(worker.isCancelled()) return;
            String nodeID = sb.getIdentifier();
            //If using synonyms, store all know synonyms as a seperate name
            if(useSynonyms)
            {
                List<String> names;
                try { //If the synonym data isn't available, the data has changed since
                      //checking the network for compatability with this plugin. Show
                      //error message and cancel the download task
                    names = nodeAttributes.getListAttribute(nodeID, SYNONYM_ATTRIBUTE_NAME);
                    if(names == null) names = new ArrayList();
                } catch(ClassCastException e) {
                    //JOptionPane.showMessageDialog(frame, "The network doesn't contain the correct " +
                    //        "synonym data. Please re-check the network's compatibility with this plugin.",
                    //        "Network Data Error", JOptionPane.ERROR_MESSAGE);
                    PopupMessages.networkCompatibilityError(frame, winManager, properties, true, false);
                    //worker.cancel(true);
                    return;
                }
                //Create hash codes for each name and store in synonym database
                addToDatabaseHashCodes(names, nodeAttributes, nodeID);
            }
            if(worker.isCancelled()) return;
            //Checks if the node has the same unique ID from the same database
            List<String> ids;
            try { //If the attribute doesn't exist, an exception is thrown
                ids = nodeAttributes.getListAttribute(nodeID, DATABASE_ID_ATTRIBUTE_NAME);
                if(ids == null) ids = new ArrayList();
            } catch (ClassCastException e) { //Not of the right type
                PopupMessages.networkCompatibilityError(frame, winManager, properties, true, false);
                return;
            }
            for(String id : ids)
                synonymsDatabase.put(databaseName+id,nodeID);
        }
    }

    /**
     * Adds a hash code for each given synonym name along with it's corresponding
     * CyNode's Cytoscape unique ID to the synonym database.
     * @param names Synonym names to create hash codes for
     * @param nodeAttributes CyAttributes for the node group containing the given CyNode ID
     * @param nodeID Cytoscape unique ID for the CyNode to add hash codes for
     */
    private void addToDatabaseHashCodes(List<String> names, CyAttributes nodeAttributes, String nodeID)
    {
        for(String name : names)
        {
            if(worker.isCancelled()) return;
            //Store the "Location+Type+Name" with corresponding "Node ID"
            String orgName;
            try { //Execption is thrown if attribute doesn't exist
                orgName = nodeAttributes.getStringAttribute(nodeID, ORGANISM_ATTRIBUTE_NAME);
                if(orgName == null) orgName = PathwayAccessPlugin.UNKNOWN;
            } catch(ClassCastException e) { //Not of the right type
                PopupMessages.networkCompatibilityError(frame, winManager, properties, true, false);
                return;
            }
            String compName;
            try { //Execption is thrown if attribute doesn't exist
                compName = nodeAttributes.getStringAttribute(nodeID, COMPARTMENT_ATTRIBUTE_NAME);
                if(compName == null) compName = PathwayAccessPlugin.UNKNOWN;
            } catch(ClassCastException e) { //Not of the right type
                PopupMessages.networkCompatibilityError(frame, winManager, properties, true, false);
                return;
            }
            String nodeType;
            try { //Execption is thrown if attribute doesn't exist
                nodeType = nodeAttributes.getStringAttribute(nodeID, NODE_TYPE_ATTRIBUTE_NAME);
                if(nodeType == null) nodeType = PathwayAccessPlugin.UNKNOWN;
            } catch(ClassCastException e) { //Not of the right type
                PopupMessages.networkCompatibilityError(frame, winManager, properties, true, false);
                return;
            }
            String nodeParticipation;
            try { //Execption is thrown if attribute doesn't exist
                nodeParticipation = nodeAttributes.getStringAttribute(nodeID, PARTICIPATION_TYPE_ATTRIBUTE_NAME);
                if(nodeParticipation == null)
                {
                    PopupMessages.networkCompatibilityError(frame, winManager, properties, true, false);
                    return;
                }
            } catch(ClassCastException e) { //Not of the right type
                PopupMessages.networkCompatibilityError(frame, winManager, properties, true, false);
                return;
            }
            synonymsDatabase.put(getHashCode(name, orgName, compName, nodeType, nodeParticipation)+"",nodeID);
        }
    }

    /**
     * Creates a hash code which given the same edge but different name will
     * return the same hash code. The method uses reaction type, edge direction,
     * source, and target to determine the hash code. No CyAttributes are used
     * in this method.
     * Canceling of the given Thread Worker is supported.
     * @param edge Edge to create a hash code for
     * @param interactionType Reaction type the edge represents
     * @return Hash code unique to the given edge
     */
    protected int getHashCode(CyEdge edge, String interactionType)
    {
        if(edge == null || interactionType == null)
            throw new NullPointerException("Parameter can't be null");
        //Get strings true for all edges that are the same, but from different databses
        ArrayList<String> hashStrings = new ArrayList<String>();
        //hashStrings.add(edge.getReactionType());  No equivalency in Cytoscape
        //                                           Instead require as parameter
        hashStrings.add(interactionType.trim().toUpperCase());
        hashStrings.add(edge.isDirected() ? "R" : "I");
        hashStrings.add(edge.getSource().getIdentifier());
        hashStrings.add(edge.getTarget().getIdentifier());
        //Modifiers apply to entire interactions, single edges are only part of them
        //for(PluginSBase sb : edge.getListOfModifiers().toArray())
        //	hashStrings.add(((PluginModifierSpeciesReference)sb).getSpeciesInstance().getId());

        //Sort the collection of strings and combine/convert into one hashcode
        Collections.sort(hashStrings);
        String hashString = "";
        for(String s : hashStrings)
        {
            if(worker.isCancelled()) return 0;
            hashString += s;
        }
        return hashString.hashCode();
    }

    /**
     * Creates a hash code which given the same edge but different name will
     * return the same hash code. The method uses reaction type, edge direction,
     * source, and target to determine the hash code. No CyAttributes are used
     * in this method.
     * Canceling of the given Thread Worker is supported.
     * @param reactionType Reaction type the edge represents
     * @return Hash code unique to the given edge
     */
    protected int getHashCode(String name, String organism, String compName, String nodeType, String nodeParticipation)
    {
        if(name == null || organism == null || compName == null || nodeType == null)
            throw new NullPointerException("Parameter can't be null");
        //Get strings true for all nodes that are the same, but from different databses
        ArrayList<String> hashStrings = new ArrayList<String>();
        hashStrings.add(name.trim().toUpperCase());
        hashStrings.add(organism.trim().toUpperCase());
        hashStrings.add(compName.trim().toUpperCase());
        hashStrings.add(nodeType.trim().toUpperCase());
        hashStrings.add(nodeParticipation.trim().toUpperCase());
        Collections.sort(hashStrings);
        String hashString = "";
        for(String s : hashStrings)
        {
            if(worker.isCancelled()) return 0;
            hashString += s;
        }
        return hashString.hashCode();
    }

    /**
     * This method does not support simple maps or complex data structures. The type
     * of CyAttribute of an existing attribute will not be changed even if replace is true.
     * Sets the given CyAttribute(s) of the given Cytoscape object. If the attribute
     * doesn't already exist it creates it. If the attribute already exists and
     * replace is false, it adds it to the List if it's the same type of CyAttribute
     * and is of type List. If the attribute already exists and replace is true, the
     * current attribute(s) are replaced with the given one(s) again if the values are
     * of the same type of CyAttribute. A network compatibility popup message is
     * displayed to the user and the task is canceled if the given attribute name
     * doesn't represent a Boolean, Double, Integer, String, or Simple List
     * (Complex and Simple Map CyAttribute types are not supported).
     * Canceling of the given Thread Worker is supported.
     * @param attributeName Name of the attribute to set
     * @param cyObject CyNode, CyEdge, or CyNetwork to add attribute to
     * @param values Values to set the given attribute to
     * @param isList True if attribute should be a simple list, flase otherwise
     * @param replace True to replace the given attribute, flase otherwise
     * @throws NullPointerException If attributeName, cyObject, or raplace are null
     * @throws IllegalArgumentException Occurs if: (1) attributeName contains no string (when
     *         attributeName.trim().equals("") is true), (2) cyObject is not a CyNode,
     *         CyEdge, or CyNetwork, or (3) the attribute can't be set (occurs when an existing
     *         attribute is not a List and replace is false or if given type is not
     *         the same as an an existing attribute).
     * @throws ClassCastException If the given value(s) is not of the same type of
     *         CyAttributes as an already set attribute.
     * @see CyAttributes
     * @see PopupMessages
     */
    private void addAttributes(String attributeName, Object cyObject, List values, boolean isList, boolean replace)
    {
        if(worker.isCancelled()) return;
        //Check for null parameters
        if(attributeName == null || cyObject == null)
            throw new NullPointerException("Invalid null parameter");
        if(values == null) return; //No values available so end
        if(values.size() == 0) return;//No values available so end
        //Check the attribute name is valid
        if(attributeName.trim().equals("")) throw new IllegalArgumentException("Invalid attribute name");
        //Check type of objects in lists
        if(!(values.get(0).getClass() != Boolean.class || values.get(0).getClass() != Integer.class ||
                values.get(0).getClass() != Double.class || values.get(0).getClass() != String.class))
            throw new IllegalArgumentException("Only Boolean, Integer, Double, " +
                    "or String are supported by CyAttributes");
        //Get the CyAttributes and ID for the corresponding type cyObject
        CyAttributes attributes = null;
        String id = null;
        if(cyObject instanceof CyNode)
        {
            attributes = Cytoscape.getNodeAttributes();
            id = ((CyNode)cyObject).getIdentifier();
        }
        else if(cyObject instanceof CyEdge)
        {
            attributes = Cytoscape.getEdgeAttributes();
            id = ((CyEdge)cyObject).getIdentifier();
        }
        else if(cyObject instanceof CyNetwork)
        {
            attributes = Cytoscape.getNetworkAttributes();
            id = ((CyNetwork)cyObject).getIdentifier();
        }
        if(id == null) //Object doesn't exist or isn't 
            throw new IllegalArgumentException("The object given doesn't exist in Cytoscape");
        if(attributes == null)
            PopupMessages.cytoscapeFatalError(frame, winManager, properties);
        //Get the type of CyAttribute the given name represents
        byte type = attributes.getType(attributeName);
        //See if attribute exists for this specific cyObject
        if(type != CyAttributes.TYPE_UNDEFINED)
        {
            Object attr = null;// = attributes.getAttribute(id, attributeName);
            if(type == CyAttributes.TYPE_BOOLEAN) attr = attributes.getBooleanAttribute(id, attributeName);
            else if(type == CyAttributes.TYPE_FLOATING) attr = attributes.getDoubleAttribute(id, attributeName);
            else if(type == CyAttributes.TYPE_INTEGER) attr = attributes.getIntegerAttribute(id, attributeName);
            else if(type == CyAttributes.TYPE_SIMPLE_LIST) attr = attributes.getListAttribute(id, attributeName);
            else if(type == CyAttributes.TYPE_STRING) attr = attributes.getStringAttribute(id, attributeName);
            else {// if(type == CyAttributes.TYPE_SIMPLE_MAP || type == CyAttributes.TYPE_COMPLEX)
                //throw new IllegalArgumentException("Simple maps and complex types not supported");
                PopupMessages.networkCompatibilityError(frame, winManager, properties, true, false);
                return;
            }
            if(attr == null) type = CyAttributes.TYPE_UNDEFINED;
        }
        //Check types and create the new attribute
        if(type != CyAttributes.TYPE_UNDEFINED) //If attribute already exists, check it
        {
            //Check if given is a list of existing matches
            if(values.size() > 1 && type != CyAttributes.TYPE_SIMPLE_LIST)
                throw new ClassCastException("Existing attribute is not a list");
            //Check single object can be replaced
            if(replace == false && type != CyAttributes.TYPE_SIMPLE_LIST)
                throw new IllegalArgumentException("Existing attribute is not a list and can't be added onto");
            //If given a list and replace is false, add on existing values
            if(type == CyAttributes.TYPE_SIMPLE_LIST && replace == false)
            {
                List existList = attributes.getListAttribute(id, attributeName);
                Iterator existItr = existList.iterator();
                while(existItr.hasNext()) values.add(existItr.next());
            }
        }
        //If the attribute doesn't already exist create and set it
        else
        {
            if(!isList)
            {
                if(values.get(0).getClass() == Boolean.class)
                    attributes.setAttribute(id, attributeName, (Boolean)values.get(0));
                if(values.get(0).getClass() == Integer.class)
                    attributes.setAttribute(id, attributeName, (Integer)values.get(0));
                if(values.get(0).getClass() == Double.class)
                    attributes.setAttribute(id, attributeName, (Double)values.get(0));
                if(values.get(0).getClass() == String.class)
                    attributes.setAttribute(id, attributeName, (String)values.get(0));
                return;
            }
            attributes.setListAttribute(id, attributeName, values);
            return;
        }
        //Set the value of the attribute
        try {
            if(type != CyAttributes.TYPE_SIMPLE_LIST)
            {
                if(type == CyAttributes.TYPE_BOOLEAN)
                    attributes.setAttribute(id, attributeName, (Boolean)values.get(0));
                if(type == CyAttributes.TYPE_INTEGER)
                    attributes.setAttribute(id, attributeName, (Integer)values.get(0));
                if(type == CyAttributes.TYPE_FLOATING)
                    attributes.setAttribute(id, attributeName, (Double)values.get(0));
                if(type == CyAttributes.TYPE_STRING)
                    attributes.setAttribute(id, attributeName, (String)values.get(0));
            }
            else attributes.setListAttribute(id, attributeName, values);
        } catch(IllegalArgumentException e) {
            throw new ClassCastException("The given value type doesn't match the existing attribute type");
        }
    }

    /**
     * Sets the initial CyAttributes of the given CyNode. If the attribute is
     * already initialized, the attribute is not overwritten. Also, updates the
     * synonymDatabase with all synonym hash codes and database specific IDs.
     * Canceling of the given Thread Worker is supported.
     * @param databaseName Name of database class extending PathwayAccessPlugin (from
     * PathwayAccessPlugin use this.getClass().getName())
     * @param databaseID Unique ID of the node in the given database
     * @param node CyNode to initialize all attributes for
     * @param names Synonyms of the given CyNode
     * @param organism Organism containing the given CyNode
     * @param compName Compartment name of the location of the given CyNode
     * @param nodeType Type of node of the given CyNode
     * @param partType Participation type the node has in its reaction (reaction node,
     * participant, or modifier). Must use one of the constants defined in this class.
     * @throws NullPointerException When any of the given parameters are null.
     * @throws IllegalArgumentException When the given partType is not one of the
     * static constants defined in this class.
     * @see SynonymTool
     */
    protected void setNodeAttributes(String databaseName, String databaseID, CyNode node,
            List<String> names, String organism, String compName, String nodeType, String partType)
    {
        if(databaseName == null || databaseID == null || node == null ||
                names == null || compName == null || nodeType == null || partType == null)
            throw new NullPointerException("Null parameter not accepted");
        if((!partType.equals(REACTION_NODE_PARTICIPATION_TYPE)) &&
                //(!partType.equals(SOURCE_PARTICIPATION_TYPE)) &&
                //(!partType.equals(TARGET_PARTICIPATION_TYPE)) &&
                (!partType.equals(PARTICIPANT_PARTICIPATION_TYPE)) &&
                (!partType.equals(MODIFIER_PARTICIPATION_TYPE)))
            throw new IllegalArgumentException("Participation type must be set as" +
                    "one of the static constants defined in SynonymTool.");
        if(organism == null) organism = "unknown";
        if(worker.isCancelled()) return;
        addAttributes(SYNONYM_ATTRIBUTE_NAME, node, names, true, false);
        //Create hash codes for each synonym
        String cytoscapeID = node.getIdentifier();
        ArrayList<String> hash = new ArrayList();
        for(String name : names)
        {
            String hashCode = getHashCode(name, organism, compName, nodeType, partType)+"";
            hash.add(hashCode);
            //Add new synonyms to the synonyms database
            synonymsDatabase.put(hashCode, cytoscapeID);
        }
        addAttributes(HASH_ATTRIBUTE_NAME, node, hash, true, false);
        //The following are single items but need to be placed into a List in
        //order to call addAttributes()
        ArrayList<String> item = new ArrayList();
        item.add(nodeType);
        try { //Do nothing if the single item already exists
            addAttributes(NODE_TYPE_ATTRIBUTE_NAME, node, item, false, false);
        } catch(Exception e) { }
        item.clear();
        item.add(partType);
        try { //Do nothing if the single item already exists
            addAttributes(PARTICIPATION_TYPE_ATTRIBUTE_NAME, node, item, false, false);
        } catch(Exception e) { }
        item.clear();
        item.add(organism);
        try { //Do nothing if the single item already exists
            addAttributes(ORGANISM_ATTRIBUTE_NAME, node, item, false, false);
        } catch(Exception e) { }
        item.clear();
        item.add(compName);
        try { //Do nothing if the single item already exists
            addAttributes(COMPARTMENT_ATTRIBUTE_NAME, node, item, false, false);
        } catch(Exception e) { }
        item.clear();
        item.add(names.get(0));
        try { //Do nothing if the single item already exists
            addAttributes("canonicalName", node, item, false, true);
        } catch(Exception e) { }
        item.clear();
        String databaseSyn = databaseName+databaseID;
        item.add(databaseName+databaseID);
        addAttributes(DATABASE_ID_ATTRIBUTE_NAME, node, item, true, false);
        //Add to synonymsDatabase
        synonymsDatabase.put(databaseSyn, cytoscapeID);
    }

    /**
     * Sets the initial CyAttributes of the given CyEdge. If the attribute is
     * already initialized, the attribute is not overwritten.
     * Canceling of the given Thread Worker is supported.
     * @param edge CyEdge to initialize all attributes for
     * @param organism Organism containing the given CyEdge
     * @param compName Compartment name of the location of the given CyEdge
     */
    protected void setEdgeAttributes(CyEdge edge, String interactType, String organism, String compName)
    {
        if(worker.isCancelled()) return;
        ArrayList<String> item = new ArrayList();
        item.add(getHashCode(edge, interactType)+"");
        addAttributes(HASH_ATTRIBUTE_NAME, edge, item, true, false);
        item.clear();
        item.add(interactType);
        try { //Do nothing if the single item already exists
            addAttributes(INTERACTION_TYPE_ATTRIBUTE_NAME, edge, item, false, false);
        } catch(Exception e) { }
        item.clear();
        item.add(organism);
        try { //Do nothing if the single item already exists
            addAttributes(ORGANISM_ATTRIBUTE_NAME, edge, item, false, false);
        } catch(Exception e) { }
        item.clear();
        item.add(compName);
        try { //Do nothing if the single item already exists
            addAttributes(COMPARTMENT_ATTRIBUTE_NAME, edge, item, false, false);
        } catch(Exception e) { }
    }

    /**
     * Find an existing CyNode and a set of names in the model if it exists. Use
     * this when trying to lookup a species alias from a new entity with synonyms and alternate names.
     * Canceling of the given Thread Worker is supported.
     * @param databaseName Name of database class extending PathwayAccessPlugin (from
     * PathwayAccessPlugin use this.getClass().getName())
     * @param names All the possible names the alias node may use
     * @param organism Organism containing the given object
     * @param location Compartment name where given object is located
     * @param type The type of the species alias to lookup
     * @param databaseID Unique identifier from the database for this node
     * @param useSynonyms True to check for alias nodes based on synonyms
     * @param partType Participation type the node has in its reaction (reaction node,
     * participant, or modifier). Must use one of the constants defined in this class.
     * @return If a match is found, returns the match, otherwise null.
     * @throws IllegalArgumentException When the given partType is not one of the
     * static constants defined in this class.
     * @see SynonymTool
     */
    protected CyNode findNodeAlias(String databaseName, List<String> names, String organism,
            String location, String type, String databaseID, boolean useSynonyms, String partType)
    {
        if(worker.isCancelled()) return null;
        if(databaseName == null || names == null || organism == null ||
                location == null || type == null || databaseID == null)
            throw new NullPointerException("Parameter can't be null");
        if((!partType.equals(REACTION_NODE_PARTICIPATION_TYPE)) &&
                (!partType.equals(PARTICIPANT_PARTICIPATION_TYPE)) &&
                (!partType.equals(MODIFIER_PARTICIPATION_TYPE)))
            throw new IllegalArgumentException("Participation type must be set as" +
                    "one of the static constants defined in SynonymTool.");
        //If there is a known CyNode with the given ID, return that node
        if(synonymsDatabase.containsKey(databaseName+databaseID))
        {
            String nodeID = synonymsDatabase.get(databaseName+databaseID);
            CyNode node = Cytoscape.getCyNode(nodeID, false);
            return node;
        }
        else //Check if same node exists based on synonyms
        {
            if(useSynonyms)
                for(String name : names)
                {
                    if(worker.isCancelled()) return null;
                    String hash = getHashCode(name, organism, location, type, partType)+"";
                    if(synonymsDatabase.containsKey(hash))
                    {
                        //Get the alias node and add the new synonyms
                        CyNode node = Cytoscape.getCyNode(synonymsDatabase.get(hash), false);
                        //If it's a reaction node, check its participants
                        if(partType.equals(REACTION_NODE_PARTICIPATION_TYPE))
                        {
                            //TODO
                            //Need to find a way to stop nodes representing reactions from thinking
                            //there are synonyms when they are really just not enough data
                        }
                        addAttributes(SYNONYM_ATTRIBUTE_NAME, node, names, true, false);
                        return node;
                    }
                }
        }
        //If no alias nodes found, return null
        return null;
    }
}
