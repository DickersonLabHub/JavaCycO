package edu.iastate.pathwayaccess.cytoscape;

import edu.iastate.pathwayaccess.common.Relation;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.FilterTable;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.PopupMessages;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.WindowManager;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.pnlDetails;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.pnlMainPanel;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.pnlNetworkTabs;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.pnlSelectNetwork;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.NetworkResultInterface;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.ResultsFilterInterface;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.SearchResultInterface;
import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author Greg Hazen <ghazen@iastate.edu>
 */
public abstract class PathwayAccessPlugin<networkType, reactionType, participantType, modifierType> extends CytoscapePlugin {

    //Location Types
    public static final String CYTOSOL = "cytosol";
    public static final String NUCLEUS = "nucleus";
    public static final String EXTRACELLULAR = "extracellular";
    public static final String UNKNOWN = "unknown";
    //Interation Types
    public static final String ADD_PRODUCT = "ADD_PRODUCT";
    public static final String ADD_REACTANT = "ADD_REACTANT";
    public static final String BOOLEAN_LOGIC_GATE_AND = "BOOLEAN_LOGIC_GATE_AND";
    public static final String BOOLEAN_LOGIC_GATE_NOT = "BOOLEAN_LOGIC_GATE_NOT";
    public static final String BOOLEAN_LOGIC_GATE_OR = "BOOLEAN_LOGIC_GATE_OR";
    public static final String BOOLEAN_LOGIC_GATE_UNKNOWN = "BOOLEAN_LOGIC_GATE_UNKNOWN";
    public static final String CATALYSIS = "CATALYSIS";
    public static final String DEGRADATION = "DEGRADATION";
    public static final String DISSOCIATION = "DISSOCIATION";
    public static final String HETERODIMER_ASSOCIATION = "HETERODIMER_ASSOCIATION";
    public static final String HOMODIMER_FORMATION = "HOMODIMER_FORMATION";
    public static final String INHIBITION = "INHIBITION";
    public static final String KNOWN_TRANSITION_OMITTED = "KNOWN_TRANSITION_OMITTED";
    public static final String MODULATION = "MODULATION";
    public static final String PHYSICAL_STIMULATION = "PHYSICAL_STIMULATION";
    public static final String STATE_TRANSITION = "STATE_TRANSITION";
    public static final String TRANSCRIPTION = "TRANSCRIPTION";
    public static final String TRANSLATION = "TRANSLATION";
    public static final String TRANSPORT = "TRANSPORT";
    public static final String TRIGGER = "TRIGGER";
    public static final String TRUNCATION = "TRUNCATION";
    public static final String UNKNOWN_CATALYSIS = "UNKNOWN_CATALYSIS";
    public static final String UNKNOWN_INHIBITION = "UNKNOWN_INHIBITION";
    public static final String UNKNOWN_TRANSITION = "UNKNOWN_TRANSITION";
    //Node Types
    public static final String ANTISENSE_RNA = "ANTISENSE_RNA";
    public static final String COMPLEX = "COMPLEX";
    public static final String DEGRADED = "DEGRADED";
    public static final String DRUG = "DRUG";
    public static final String GENE = "GENE";
    public static final String ION = "ION";
    public static final String PHENOTYPE = "PHENOTYPE";
    public static final String PROTEIN = "PROTEIN";
    public static final String PROTEIN_GENERIC = "GENERIC";
    public static final String PROTEIN_ION_CHANNEL = "ION_CHANNEL";
    public static final String PROTEIN_RECEPTOR = "RECEPTOR";
    public static final String PROTEIN_TRUNCATED = "TRUNCATED";
    public static final String RNA = "RNA";
    public static final String SIMPLE_MOLECULE = "SIMPLE_MOLECULE";

    /**
     * Stores the database access class
     */
    private PathwayAccessPlugin<networkType, reactionType, participantType, modifierType> database;
    /**
     * Stores the plugin properties specific to the current plugin
     */
    private PluginProperties properties = null;
    /**
     * This is a Relation map between the Cytoscape type strings and the type
     * strings of the datasource in use.
     * @see Relation
     */
    protected Relation typeMap;
    /**
     * A semaphore used to force multiple CyNetworkSearchClient plugins to queue
     * for the opportunity to download (a) pathway(s).  If multiple plugins were
     * allowed to download simultaneously, very bad things would happen.
     */
    protected static Semaphore download = new Semaphore(1);
    /**
     * Stores the class that adds the option to the Plugins menu in Cytoscape 
     * to run this plugin and manages the window containing the UI.
     */
    private WindowManager<networkType, reactionType, participantType, modifierType> winManager;

    /**
     * Sets up the database common type String relations, creates the plugins
     * menu item in Cytoscape, and sets the starting action for when the menu
     * item is pressed.
     * @param properties Properties class specific to the current database
     */
    public PathwayAccessPlugin(PluginProperties properties)
    {
        database = this;
        this.properties = properties;
        //Set up common type String relations with database
        try {
            database.setUpNameRelations();
        } catch(Exception e) {
            PopupMessages.databaseConnectionError(null, winManager, properties, true, true);
        }
        //Create the plugin within the plugin menu
        JMenu pluginMenu = Cytoscape.getDesktop().getCyMenus().getMenuBar().getMenu("Plugins");
        winManager = new WindowManager<networkType, reactionType, participantType, modifierType>(this, properties);
        JMenuItem menuItem = new JMenuItem(winManager);
        menuItem.setToolTipText(properties.PLUGIN_DESCRIPTION);
        pluginMenu.add(menuItem);
    }

    /**
     * Returns the window manager for this plugin's GUI
     * @return The window manager for this plugin's GUI
     */
    public WindowManager<networkType, reactionType, participantType, modifierType> getWindowManager()
    {
        return winManager;
    }

    /**
     * Option in the plugin's Organism drop-down box for choosing to search
     * all organisms in the database for the given search criteria.
     */
    public static final String ALL_ORGANISMS = "All Organisms";
    /**
     * Option in the plugin's Search Type drop-down box for choosing to search
     * all types in the database matching the given search terms.
     */
    public static final String ALL_SEARCH_TYPES = "All Types";
    /**
     * Option in the plugin's Search Type drop-down box for choosing to search
     * for pathways in the database matching the given search terms.
     */
    public static final String PATHWAY_SEARCH_TYPE = "Pathway";
    /**
     * Option in the plugin's Search Type drop-down box for choosing to search
     * for ID's in the database matching the given search terms.
     */
    public static final String ID_SEARCH_TYPE = "Unique ID";

    /**
     * Returns the properties class specific to the current database
     * @return The properties class specific to the current database
     */
    public PluginProperties getProperties()
    {
        return properties;
    }

    /**
     * Future networks to download from the database
     */
    private ArrayList<networkType> netsToDownload = new ArrayList<networkType>();
    /**
     * Sets the future networks to download from the database to the given
     * generic networks.
     */
    public void setNetworksToDownload(ArrayList<NetworkResultInterface> networks)
    {
        netsToDownload.clear();
        for(NetworkResultInterface net : networks)
            netsToDownload.add((networkType) net.getNetwork());
    }
    /**
     * Downloads the networks set by a former call to setNetworksToDownload().
     * The view for the network shouldn't be created prior to this call. This
     * method will create the view after the download is complete.
     */
    public void download(final CyNetwork network, final boolean useSynonyms)
    {
        //Create the new search task
        NewThreadWorker worker = new NewThreadWorker()
        {
            @Override
            public Void doInBackground()
            {
                //Prepare Status Bar
                this.setStatusBarProgress(0);
                this.setStatusTitle("Preparing to Download...");
                //Create download class
                DownloadTool<networkType, reactionType, participantType, modifierType>
                        downloadTool = new DownloadTool(database, winManager, properties,
                        network, useSynonyms, this);
                downloadTool.download(netsToDownload, network);
                return null;
            }
            @Override
            public void done()
            {
                Cytoscape.createNetworkView(network);
                this.setStatusTitle("Download Complete");
                winManager.close();
            }
        };
        //Create new loading bar for this task
        winManager.setStatusWorker(worker, true);
        //Begin the task
        worker.execute();
    }

    /**
     * Returns a list of names of all organisms in the database. The first item
     * in the returned array MUST be ALL_ORGANISMS as defined in the
     * PathwayAccessPlugin class. All names returned are properly formatted with
     * capitilization as they are displayed for user just as this method returns
     * them.
     *
     * @return A list of names of all organisms in the database
     */
    abstract public String[] getOrganismNames();

    /**
     * Returns all available search types including ALL_SEARCH_TYPES,
     * PATHWAY_SEARCH_TYPE, and ID_SEARCH_TYPE as defined in the PathwayAccessPlugin
     * class. All type names returned are properly formatted with
     * capitilization as they are displayed for user just as this method returns
     * them.
     *
     * @return A string array containing all available search types.
     */
    abstract public String[] getSearchTypes();

    /**
     * Searches for potentially relevent pathways in the given organism
     * using the given search term. What exactly it seaches may be
     * implemented differently for individual databases and APIs.
     *
     * @param searchTerm Term for searching for potentially relevant pathways
     * @param organism Organism in which to search for pathways
     * @param searchType The String that identities which entity type for which
     * the user would like to see results
     * @param statusBar Status panel to updated with the latest status message
     * @param worker Task, executing this method, to update the percent of progress
     * @return A two column string array with the first column containing names
     * of the pathways found and the second column containing the organism name
     * for each pathway (the two things necessary to identify a pathway).
     */
    abstract public ArrayList<SearchResultInterface> search(String searchTerm,
            boolean wholeWordOnly, String organism, String searchType,
            NewThreadWorker worker);

//    /**
//     * Allows the user to search for entities or pathways based on a search term,
//     * organism, and a search type.
//     *
//     * @param searchTerm The user-entered search term (a String).
//     * @param wholeWordOnly True to search for names with the entire search
//     *        term(s) surrounded by spaces
//     * @param organism The organism the user specifies (as a String).
//     * @param searchType The String that identities which entity type for which
//     * the user would like to see results
//     * @param statusBar Status panel to updated with the latest status message
//     * @param worker Task, executing this method, to update the percent of progress
//     * @return The array of SearchResults containing all matches found
//     * in the MetNet database
//     */
//    public ArrayList<SearchResultInterface> search(String searchTerm,
//            boolean wholeWordOnly, String organism, String searchType, NewThreadWorker worker) {
//
//        if(worker.isCancelled())
//            return null;
//        worker.setStatusTitle("Preparing search criteria...");
//        //Check for null organism or search type
//        if (organism == null)
//            organism = ALL_ORGANISMS;
//        if (searchType == null)
//            searchType = ALL_SEARCH_TYPES;
//        //Make sure lists of available organisms and entity types are set
//        String[] orgNames = getOrganismNames();
//        if(worker.isCancelled())
//            return null;
//        String[] typeNames = getSearchTypes();
//        //Sort search terms
//        String[] terms = splitSearchTerms(searchTerm);
//
//        //Create an array of the organism(s) to search
//        //Organism[] orgs = new Organism[0];
//        String[] orgs = new String[0];
//        if(organism.equals(ALL_ORGANISMS))
//            orgs = orgNames;
//        else //Get the matching organism
//        {
//            for(int i = 0; i < orgNames.length; i++)
//                if(orgNames[i].equals(organism))
//                    //organisms[i-1] to account for "All Organisms"
//                    orgs = new String[]{orgNames[i-1]};
//        }
//
//        //Create an array of the search type names to search and set booleans
//        //for whether to search based on pathways and IDs
//        boolean searchPathways, searchIds;
//        String[] searchTypeNames = new String[0];
//        if(searchType.equals(ALL_SEARCH_TYPES))
//        {
//            searchTypeNames = getSearchTypes();
//            searchPathways = true;
//            searchIds = true;
//        }
//        else if(searchType.equals(PATHWAY_SEARCH_TYPE))
//        {
//            //Leave number of types to search zero
//            searchPathways = true;
//            searchIds = false;
//        }
//        else if(searchType.equals(ID_SEARCH_TYPE))
//        {
//            //Leave number of types to search zero
//            searchPathways = false;
//            searchIds = true;
//        }
//        else //Specific entity type was specified
//        {
//            //Get the matching entity type
//            for(int i = 0; i < searchTypeNames.length; i++)
//                if(searchTypeNames[i].equals(searchType))
//                {
//                    //searchTypes[i-3] to account for "All Types" and "Pathway"
//                    String temp = searchTypeNames[i-3];
//                    searchTypeNames = new String[]{temp};
//                }
//            searchPathways = false;
//            searchIds = false;
//        }
//
//        //Stores the results matching the criteria (variable returned)
//        ArrayList<SearchResultInterface> results = new ArrayList<SearchResultInterface>();
//
//        if(worker.isCancelled())
//            return null;
//
//        //Perform search
//        Double progress = 0.0;
//        Double increment = -1.0; //10000.0 / 75000;
//        int numOrgs = 0, numPaths = 0, numTerms = 0, numTypes = 0;
//        //For each organism:
//        for(String orgName : orgs)
//        {
//            if(worker.isCancelled())
//                return null;
//            if(increment.equals(-1.0))
//                numOrgs = orgs.length;
//            //For each pathway in the organism:
//            networkType[] orgPaths = getOrganismPathways(orgName);
//            for(networkType path : orgPaths)
//            {
//                if(increment.equals(-1.0))
//                    numPaths = orgPaths.length;
//                //For each search term:
//                for(String term : terms)
//                {
//                    if(increment.equals(-1.0))
//                        numTerms = terms.length;
//                    String prog = term + " in " + orgName + ": ";
//                    worker.setStatusTitle(prog);
//                    //Search based on unique IDs
//                    if(searchIds) {
//                        if(worker.isCancelled())
//                            return null;
//                        worker.setStatusTitle(prog + "as ID");
//                        getResultsById(term, orgs, results);
//                    }
//                    //Check if pathway meets criteria
//                    if(searchPathways) {
//                        if(worker.isCancelled())
//                            return null;
//                        worker.setStatusTitle(prog + "as pathway: " + path.name);
//                        getPathways(term, wholeWordOnly, path, results);
//                    }
//                    //For each entity type, get all entities from the pathway
//                    for(EntityType type : searchTypeNames)
//                    {
//                        if(worker.isCancelled())
//                            return null;
//                        //Update Progress Monitor
//                        if(increment.equals(-1.0))
//                        {
//                            numTypes = searchTypeNames.length;
//                            increment = 100.0 / (numOrgs * numPaths * numTerms * numTypes);
//                        }
//                        progress += increment;
//                        worker.setStatusBarProgress(Math.min(progress.intValue(), 100));
//                        //Get all entities that meet the criteria
//                        getEntities(term, wholeWordOnly, path, type, results,
//                                worker, prog + "as entity: ");
//                        //Update the status bar percent complete
//                        //totalProgress += percentPerType;
//                        //worker.setStatusBarProgress((int)totalProgress);
//                    }
//                }
//            }
//        }
//
//        return results;
//    }

    /**
     * Splits the given of search term(s) into single an array of single
     * search terms (trimmed using String.trim()).
     * @param searchTerms List of search terms (multiple terms seperated by
     * commas)
     * @return An array of single search terms or null if given no search terms
     */
    public String[] splitSearchTerms(String searchTerms)
    {
        String[] ret = new String[0];
        //If there are no search terms, return null
        if(searchTerms == null)
            return ret;
        if(searchTerms.trim().equals("") ||
                searchTerms.trim().equals(pnlMainPanel.DEFAULT_SEARCH_TERM))
            return ret;
        //If only container search term, return an array of size container
        if(!searchTerms.contains(","))
        {
            ret = new String[]{searchTerms.trim()};
            return ret;
        }
        //For multiple terms, fill array with each and trim them
        ret = searchTerms.split(",");
        for(int i = 0; i < ret.length; i++)
            ret[i] = ret[i].trim();
        return ret;
    }

    /**
     * Checks if the container contains the search term (not case sensitive).
     * @param container String to check if containing the search term
     * @param searchTerm Search term to check for
     * @param wholeWordOnly True to return true only if the container contains
     *                      the search term surrounded by spaces
     * @param caseSensitive True if the check should be case sensitive,
     *                      otherwise false
     * @return True if the container contains the search term. If whole word
     *         only form, true only if it contains the term surrounded by
     *         spaces. The check is case sensitive.
     */
    public boolean wordContains(String container, String searchTerm, boolean wholeWordOnly, boolean caseSensitive)
    {
        if(!caseSensitive)
        {
            container = container.toLowerCase();
            searchTerm = searchTerm.toLowerCase();
        }
        //If not whole word only, just return if it contains the search term
        if(!wholeWordOnly)
            if(container.contains(searchTerm))
                return true;
        //If whole word only, check that it contains it with spaces around it:
        //Check if there the same
        if(container.contains(searchTerm) && container.length() == searchTerm.length())
            return true;
        //Check if it begins with it
        else if(container.startsWith(searchTerm + " "))
            return true;
        //Check if it ends with it
        else if((container.contains(" " + searchTerm)) &&
                (container.lastIndexOf(" " + searchTerm) == (container.length() - searchTerm.length() - 1)))
                return true;
        //Check if it's part of it
        else if(container.contains(" " + searchTerm + " "))
            return true;
        //The container doesn't contain the whole word surrounded by spaces
        else
            return false;
    }

    /**
     * Given a SearchResult (that the user picked from the list of searchresults)
     * this method should give back a list of all pathways that contain that
     * result.  So, given a EntitySearchResult, it should return an array of
     * all pathways that contain that entity. Given a PathwaySearchResult,
     * it should return an array with just that single pathway.
     * @param searchResult The EntitySearchResult or PathwaySearchResult for which
     * you would like to find all containing pathways.
     * @return The PathwaySearchResults that contain the given searchResult
     */
    abstract public NetworkResultInterface<networkType>[] getContainingPathways(SearchResultInterface searchResult,
            ResultsFilterInterface[] filters, String organism);

    /**
     * Returns an ArrayList of filter tables that apply to the given array of
     * networks.
     * @param networks Networks to create filters for
     * @return An ArrayList of filter tables that apply to the given array of
     *         networks.
     */
    abstract public ArrayList<FilterTable> getFilters(NetworkResultInterface[] networks,
            PropertiesInterface properties);

    /**
     * Creates a Relation map (using <code>typeMap</code>) between the common
     * type strings, supplied as static constants in this class, and the type
     * strings of the datasource in use. For example:
     * <code>
     * typeMap = new Relation();
     * //species types
     * typeMap.addMapping(PROTEIN_RECEPTOR,"polypeptide");
     * typeMap.addMapping(PROTEIN_TRUNCATED,"polypeptide");
     * //Interaction types
     * typeMap.addMapping(UNKNOWN_CATALYSIS,"Catalysis");
     * typeMap.addMapping(DISSOCIATION,"Diffusion");
     * </code>
     */
    abstract public void setUpNameRelations();

    /**
     * Returns the given network as a complete CyNetwork
     * @param network The network of the generic type given at the declaration
     *                of this class <networkType>.  The network is converted
     *                into a CyNetwork.
     */
    //abstract public void getNetwork(networkType network);

    /**
     * Get the name of a generic network object (pathway, molecule, gene, etc).
     * These are used to identify nodes in imported networks, so they should be
     * unique.
     * @param o The generic network database object
     * @return The generic network database object's name
     */
    protected abstract String getName(Object o);

    /**
     * Use available methods for a generic pathway database object to learn
     * its type in the pathway database and then convert that to a Cytoscape species or reaction type.
     * @param o the generic pathway database object
     * @return a string that best represents the generic pathway database object's
     * type as represented in the database.
     */
    protected abstract String getType(Object o);

    /**
     * Get the name of the organism containing the generic pathway database object.
     * @param o the generic pathway database object
     * @return The name of the organism containing the generic pathway database object
     */
    protected abstract String getOrganism(Object o);

    /**
     * Get the name of the subcellular location of a generic pathway database object.
     * @param o the generic pathway database object
     * @return the name of the subcellular location of the generic pathway database object
     */
    protected abstract String getLocation(Object o);

    /**
     * Get any synonyms provided by a generic pathway database for one of its objects.
     * @param o the generic pathway database object
     * @return the generic pathway database object's synonyms.
     * Return an empty ArrayList<String> if there are no synonyms.
     */
    protected abstract ArrayList<String> getSynonyms(Object o);

    /**
     * Get the generic pathway database reaction objects associated
     * with a given generic pathway database pathway.
     * @param pwy the generic pathway database pathway
     * @return the generic pathway database reaction objects associated with pwy
     */
    protected abstract Iterable<reactionType> getNetworkReactions(networkType pwy);

    /**
     * Return whether the generic pathway database reaction object is directed.
     * @param reaction The generic pathway database reaction
     * @return Whether the generic pathway database reaction object is directed.
     */
    //protected abstract boolean isDirected(reactionType reaction);

    /**
     * Get the generic pathway database objects associated as reactants/inputs to a
     * generic pathway database reaction.
     * @param reaction the generic pathway database reaction
     * @return the generic pathway database reactants
     */
    protected abstract Iterable<participantType> getEdgeSource(reactionType reaction);

    /**
     * Get the generic pathway database objects associated as products/outputs to a
     * generic pathway database reaction.
     * @param reaction the generic pathway database reaction
     * @return the generic pathway database products
     */
    protected abstract Iterable<participantType> getEdgeTarget(reactionType reaction);

    /**
     * Get the generic pathway database objects associated as modifiers/enzymes/cofactors to a
     * generic pathway database reaction.
     * @param reaction the generic pathway database reaction
     * @return the generic pathway database modifiers
     */
    protected abstract Iterable<modifierType> getEdgeModifiers(reactionType reaction);

    /**
     * Get the ID of a generic pathway database object (pathway, molecule, gene, etc).
     * These are stored as a CyAttribute in a Cytoscape model.
     * @param o the generic pathway database object
     * @return the generic pathway database object's ID
     */
    protected abstract String getId(Object o);

    /**
     * Sets the user selected highly connected entities for which should be
     * duplicated in the created CyNetwork for each instance of it in the
     * pathway being created. This method sets the entities for which a post
     * call to isSelectedHighlyConnectedEntity with a matching participant will
     * return true. Any given names that do not match names returned by a call
     * to the database properties' getHighlyConnectedEntityNames() are ignored
     * and not added to any list of selected entities.
     * @param names Names of the user selected highly connected entities.
     * @see DownloadTool
     * @see PropertiesInterface
     */
    public abstract void setSelectedHighlyConnectedEntities(String[] names);

    /**
     * Returns true if the given participant is a user selected highly connected
     * entity (set by the most recent call to setSelectedHighlyConnectedEntities)
     * for which should be duplicated in the created CyNetwork for each
     * instance of it in the pathway.
     * @param participant Participant in a pathway for which to check whether
     * it's a highly connected entity selected for duplication by the user.
     * @return True if the given participant is a user selected highly connected
     * entity, otherwise false.
     * @see DownloadTool
     */
    protected abstract boolean isSelectedHighlyConnectedEntity(/*participantType*/String participant);

        /**
     * Returns true if the main menu's progress bar is intermediate
     * @return True if the progress bar in the main menu is intermediate
     */
    public abstract boolean isIntermediate();

}


