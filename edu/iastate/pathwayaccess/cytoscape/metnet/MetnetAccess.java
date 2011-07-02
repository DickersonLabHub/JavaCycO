package edu.iastate.pathwayaccess.cytoscape.metnet;

import edu.iastate.pathwayaccess.cytoscape.metnet.ResultListItems.EntitySearchResult;
import edu.iastate.pathwayaccess.cytoscape.metnet.ResultListItems.PathwaySearchResult;
import edu.iastate.pathwayaccess.cytoscape.metnet.ResultListItems.PathwayNetworkResult;
import edu.iastate.pathwayaccess.common.*;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.FilterTable;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.pnlMainPanel;
import edu.iastate.pathwayaccess.cytoscape.*;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.*;
import edu.iastate.pathwayaccess.cytoscape.metnet.ResultListItems.*;
import edu.iastate.pathwayaccess.cytoscape.metnet.ResultListItems.InteractionFilter;
import edu.iastate.metnet.Entity;
import edu.iastate.metnet.EntityType;
import edu.iastate.metnet.Interaction;
import edu.iastate.metnet.LocalEntity;
import edu.iastate.metnet.Organism;
import edu.iastate.metnet.Pathway;
import edu.iastate.metnet.util.EntityVector;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Greg
 */
public class MetnetAccess extends PathwayAccessPlugin<Pathway, Interaction, LocalEntity, LocalEntity> {

    /*
     * The list of all organisms
     */
    private Organism[] organisms = null;
    /*
     * The list of all organisms names
     */
    private String[] organismNames = null;
    /**
     * List of search types
     */
    private EntityType[] searchTypes = null;
    /**
     * List of search types names
     */
    private String[] searchTypeNames = null;
    /**
     * Stores the selected highly connected entities for which should be
     * duplicated in the created CyNetwork for each instance of it in the
     * pathway being created.
     */
    private ArrayList<String/*Entity*/> selhighlyConEnts = new ArrayList<String/*Entity*/>();

    public MetnetAccess()
    {
        super(new MetNetProperties());
    }

    @Override
    public void setUpNameRelations()
    {
        typeMap = new Relation();
        //Entity types
        typeMap.addMapping(PROTEIN_RECEPTOR,"polypeptide");
        typeMap.addMapping(PROTEIN_TRUNCATED,"polypeptide");
        typeMap.addMapping(PROTEIN_GENERIC,"polypeptide");
        typeMap.addMapping(PROTEIN_ION_CHANNEL,"polypeptide");
        typeMap.addMapping(PROTEIN,"polypeptide");
        typeMap.addMapping(COMPLEX,"protein complex");
        typeMap.addMapping(GENE,"Cis-element");
        typeMap.addMapping(GENE,"gene");
        typeMap.addMapping(ANTISENSE_RNA,"RNA");
        typeMap.addMapping(RNA,"RNA");
        typeMap.addMapping(DEGRADED,"metabolite");
        typeMap.addMapping(DRUG,"metabolite");
        typeMap.addMapping(ION,"metabolite");
        typeMap.addMapping(SIMPLE_MOLECULE,"metabolite");
        typeMap.addMapping(PHENOTYPE,"environment");
        //Interaction types
        typeMap.addMapping(UNKNOWN_CATALYSIS,"Catalysis");
        typeMap.addMapping(DISSOCIATION,"Diffusion");
        typeMap.addMapping(STATE_TRANSITION,"Composition-OR");
        typeMap.addMapping(INHIBITION,"Negative regulation");
        typeMap.addMapping(PHYSICAL_STIMULATION,"Positive regulation");
        typeMap.addMapping(STATE_TRANSITION,"Others");
        typeMap.addMapping(STATE_TRANSITION,"Catalysis");
        typeMap.addMapping(STATE_TRANSITION,"Composition-AND");
        typeMap.addMapping(STATE_TRANSITION,"Enzymatic reaction");
        typeMap.addMapping(TRANSCRIPTION,"Transcription");
        typeMap.addMapping(TRANSLATION,"Translation");
        typeMap.addMapping(TRANSPORT,"Transport");
        typeMap.addMapping(TRIGGER,"Positive regulation");
    }

    /*
     * Gets a list of names of all organisms in the database. Adds "All
     * Organisms" as the first item in the list. Only accesses the
     * database once.
     *
     * @return A list of names of all organisms in the database
     */
    @Override
    public String[] getOrganismNames() {
        //Only access the database once to save time
        if(organismNames != null)
            return organismNames;
        //Get all organisms from the database
        organisms = Organism.search();

        //Store a string of all organism names
        organismNames = new String[organisms.length + 1];
        organismNames[0] = ALL_ORGANISMS;
        for (int i = 1; i < organisms.length + 1; i++) {
            organismNames[i] = organisms[i-1].name;
        }
        return organismNames;
    }

    /**
     * Gets a list of names of all entity types in the database. Adds "All
     * Types" and "Pathway" as the first items in the list. Only accesses the
     * database once.
     *
     * @return A list of names of all entity types in the database
     */
    @Override
    public String[] getSearchTypes() {
        //Only access the database once to save time
        if (searchTypeNames != null)
            return searchTypeNames;
        //Get all entity types from the database
        searchTypes = EntityType.search();
        //Store a string of all entity type names
        searchTypeNames = new String[searchTypes.length + 3];
        searchTypeNames[0] = ALL_SEARCH_TYPES;
        searchTypeNames[1] = PATHWAY_SEARCH_TYPE;
        searchTypeNames[2] = ID_SEARCH_TYPE;
        for (int i = 3; i < searchTypes.length + 3; i++) {
            searchTypeNames[i] = searchTypes[i-3].name;
        }
        return searchTypeNames;
    }

    /**
     * Allows the user to search for entities or pathways based on a search term,
     * organism, and a search type.
     *
     * @param searchTerm The user-entered search term (a String).
     * @param wholeWordOnly True to search for names with the entire search
     *        term(s) surrounded by spaces
     * @param organism The organism the user specifies (as a String).
     * @param searchType The String that identities which entity type for which
     * the user would like to see results
     * @param statusBar Status panel to updated with the latest status message
     * @param worker Task, executing this method, to update the percent of progress
     * @return The array of SearchResults containing all matches found
     * in the MetNet database
     */
    @Override
    public ArrayList<SearchResultInterface> search(String searchTerm, boolean wholeWordOnly,
            String organism, String searchType, NewThreadWorker worker) {
//PART I: translates the user input

        if(worker.isCancelled())
            return null;
        worker.setStatusTitle("Preparing search criteria...");

        //Check for null organism or search type
        if (organism == null)
            organism = ALL_ORGANISMS; //redefines organismName that was passed it for a more accurate name
        if (searchType == null) //Same as organism name
            searchType = ALL_SEARCH_TYPES;

        //Make sure lists of available organisms and entity types are set
        if (organisms == null) //organisms may be full already because of having to be filled for the menu that the user chooses from before searching
            //how could this not happen??? You need to get this everytime the menu pops up anyways????
            getOrganismNames(); //this fills up organisms and organismNames


        if(worker.isCancelled())
            return null;


        if (searchTypes == null)
            getSearchTypes();


        //Sort search terms the user passed in and stores it in an array
        String[] terms = splitSearchTerms(searchTerm);

        //Create an array of the organism(s) to search (the size is either one or size of the list of all organisms)
            //Matches the name of the organism passed in by the user to the organism itself
        Organism[] orgs = new Organism[0];//an array of zero organisms just to initialize
        //orgs is an array because of the choice of "All organisms", other choices will make this array a size 1 containing just one organism
        if(organism.equals(ALL_ORGANISMS))
            orgs = organisms; //If the organism equals the value we redefined earlier, orgs becomes the list of all organisms (organisms are filled earlier)
        else //Get the matching organism (the user passes in the organism name but we need to find the actual organism that matches that name)
        {
            for(int i = 0; i < organismNames.length; i++) //could have started i at 1
                if(organismNames[i].equals(organism))
                    //organisms[i-1] to account for "All Organisms"
                    orgs = new Organism[]{organisms[i-1]};
        }

        //Create an array of the search type(s) to search and set booleans
        //booleans for whether to search based on pathways and IDs
        boolean searchPathways, searchIds; //why do we only care about pathways and ids and not the other types??
        EntityType[] types = new EntityType[0]; //same as organism
        if(searchType.equals(ALL_SEARCH_TYPES))
        {
            types = searchTypes;
            searchPathways = true;
            searchIds = true;
        }
        else if(searchType.equals(PATHWAY_SEARCH_TYPE))
        {
            //Leave number of types to search zero
            searchPathways = true;
            searchIds = false;
        }
        else if(searchType.equals(ID_SEARCH_TYPE))
        {
            //Leave number of types to search zero
            searchPathways = false;
            searchIds = true;
        }
        else //Specific entity type was specified
        {
            //Get the matching entity type
            for(int i = 0; i < searchTypeNames.length; i++)
                if(searchTypeNames[i].equals(searchType))
                    //searchTypes[i-3] to account for "All Types", "Pathway", and "UniqueID" which were already taken care of
                    types = new EntityType[]{searchTypes[i-3]};
            searchPathways = false;
            searchIds = false;
        }

        /* Setup status bar progess percentages */
        /*
        double percentPerType = 0;
        double totalProgress = 0;
        //For each organism determine how many times getEntities will run
        for(Organism org : orgs)
        {
            Pathway[] orgPaths = org.getPathways().toArray();
            for(Pathway path : orgPaths)
            {
                for(String term : terms)
                {
                    for(EntityType type : types)
                    {
                        percentPerType += path.count(type);
                    }
                }
            }
        }
        //Divide the % of the task this search will take by the total # of times
        //getEntities will run to get the percent of progress to add to the
        //status bar each time getEntities runs
        percentPerType = 95 / percentPerType;
         *
         */

//PART TWO: searching

        //Stores the results matching the criteria (variable returned)
        ArrayList<SearchResultInterface> results = new ArrayList<SearchResultInterface>();

        if(worker.isCancelled())
            return null;

        //Perform search
        Double progress = 0.0;
        Double increment = -1.0; //10000.0 / 75000;
        int numOrgs = 0, numPaths = 0, numTerms = 0, numTypes = 0;

        //For each organism:
        for(Organism org : orgs)
        {
            if(worker.isCancelled())
                return null;
            if(increment.equals(-1.0))
                numOrgs = orgs.length;
            //For each pathway in the organism:
            Pathway[] orgPaths = org.getPathways().toArray();
            for(Pathway path : orgPaths)
            {
                if(increment.equals(-1.0))
                    numPaths = orgPaths.length;
                //For each search term:
                for(String term : terms)
                {
                    if(increment.equals(-1.0))
                        numTerms = terms.length;
                    String prog = term + " in " + org.name + ": ";
                    worker.setStatusTitle(prog);
                    //Search based on unique IDs
                    if(searchIds) {
                        if(worker.isCancelled())
                            return null;
                        worker.setStatusTitle(prog + "as ID");
                        getResultsById(term, orgs, results);
                    }
                    //Check if pathway meets criteria
                    if(searchPathways) {
                        if(worker.isCancelled())
                            return null;
                        worker.setStatusTitle(prog + "as pathway: " + path.name);
                        getPathways(term, wholeWordOnly, path, results);
                    }
                    //For each entity type, get all entities from the pathway
                    for(EntityType type : types) //only goes here when it's not a pathway search or an Id search
                    {
                        if(worker.isCancelled())
                            return null;
                        //Update Progress Monitor
                        if(increment.equals(-1.0))
                        {
                            numTypes = types.length;
                            increment = 100.0 / (numOrgs * numPaths * numTerms * numTypes);
                        }
                        progress += increment;
                        worker.setStatusBarProgress(Math.min(progress.intValue(), 100));
                        //Get all entities that meet the criteria
                        getEntities(term, wholeWordOnly, path, type, results,
                                worker, prog + "as entity: ");
                        //Update the status bar percent complete
                        //totalProgress += percentPerType;
                        //worker.setStatusBarProgress((int)totalProgress);
                    }
                }
            }
        }

        return results;
    }

//    /**
//     * Splits the given of search term(s) into single an array of single
//     * search terms (trimmed using String.trim()).
//     * @param searchTerms List of search terms (multiple terms seperated by
//     * commas)
//     * @return An array of single search terms or null if given no search terms
//     */
//    private String[] splitSearchTerms(String searchTerms)
//    {
//        String[] ret = new String[0];
//        //If there are no search terms, return null
//        if(searchTerms == null)
//            return ret;
//        if(searchTerms.trim().equals("") ||
//                searchTerms.trim().equals(pnlMainPanel.DEFAULT_SEARCH_TERM))
//            return ret;
//        //If only container search term, return an array of size container
//        if(!searchTerms.contains(","))
//        {
//            ret = new String[]{searchTerms.trim()};
//            return ret;
//        }
//        //For multiple terms, fill array with each and trim them
//        ret = searchTerms.split(",");
//        for(int i = 0; i < ret.length; i++)
//            ret[i] = ret[i].trim();
//        return null;
//    }

//    /**
//     * Checks if the container contains the search term (case sensitive).
//     * @param container String to check if containing the search term
//     * @param searchTerm Search term to check for
//     * @param wholeWordOnly True to return true only if the container contains
//     *                      the search term surrounded by spaces
//     * @return True if the container contains the search term. If whole word
//     *         only form, true only if it contains the term surrounded by
//     *         spaces. The check is case sensitive.
//     */
//    private boolean wordContains(String container, String searchTerm, boolean wholeWordOnly)
//    {
//        //If not whole word only, just return if it contains the search term
//        if(!wholeWordOnly)
//            if(container.contains(searchTerm))
//                return true;
//        //If whole word only, check that it contains it with spaces around it:
//        //Check if there the same
//        if(container.contains(searchTerm) && container.length() == searchTerm.length())
//            return true;
//        //Check if it begins with it
//        else if(container.startsWith(searchTerm + " "))
//            return true;
//        //Check if it ends with it
//        else if((container.contains(" " + searchTerm)) &&
//                (container.lastIndexOf(" " + searchTerm) == (container.length() - searchTerm.length() - 1)))
//                return true;
//        //Check if it's part of it
//        else if(container.contains(" " + searchTerm + " "))
//            return true;
//        //The container doesn't contain the whole word surrounded by spaces
//        else
//            return false;
//    }

    /**
     * Searches the database for pathways and entities with a unique ID of the
     * given search term and is part of a given organism. Adds any result found
     * to the given list of results.
     * @param term Search term to match as a unique ID
     * @param orgs Organisms to include in the search
     * @param results List of search results to add to
     * @return
     */
    private void getResultsById(String term, Organism[] orgs,
            ArrayList<SearchResultInterface> results)
    {
        boolean ofOrg = false;
        //Convert term to an integer
        int termToInt;
        try {
            termToInt = Integer.parseInt(term.trim());
        } catch(NumberFormatException e) {
            return;
        }

        //Search for entities
        Entity ent = new Entity(termToInt);
        if(ent != null)
        {
            //Check that its part of an organism specified
            for(Organism org : orgs)
            {
                if(ent.organism.equals(org.name))
                    ofOrg = true;
            }
            if(ofOrg)
            {
                SearchResultInterface result = new EntitySearchResult(ent);
                if(!results.contains(result))
                    results.add(result);
            }
        }
        //Search for pathways
        else
        {
            Pathway path = new Pathway(termToInt);
            if(path != null)
            {
                //Check that its part of an organism specified
                for(Organism org : orgs)
                {
                    if(ent.organism.equals(org.name))
                        ofOrg = true;
                }
                if(ofOrg)
                {
                    SearchResultInterface result = new PathwaySearchResult(path);
                    if(!results.contains(result))
                        results.add(result);
                }
            }
        }
    }

    /**
     * Adds the given pathway to the given results list if the pathway name
     * matces the search term
     * @param term Search term to match
     * @param wholeWordOnly True to only include results whose names include the
     *          search term surrounded by spaces
     * @param path Pathway to determine if meets the search criteria
     * @param results List of search results to add to
     * @return Pathways matching the search criteria
     */
    private void getPathways(String term, boolean wholeWordOnly,
            Pathway path, ArrayList<SearchResultInterface> results)
    {
        //If the path name matches search criteria, add it
        if(wordContains(path.name, term, wholeWordOnly, false))
        {
            SearchResultInterface pathResult = new PathwaySearchResult(path);
            //Add it if it's not already in the results
            if(!results.contains(pathResult))
                results.add(pathResult);
        }
    }

    /**
     * Searches the database for entities matching the search criteria. Any
     * results found are added to the given results list. The purpose is to
     * split the search into smaller groups to avoid the OutOfMemoryError and
     * since the MetNet API is incomplete, its useful methods don't work so
     * this replaces those broken methods.
     * @param term Search term to match
     * @param wholeWordOnly True to only include results whose names include the
     *          search term surrounded by spaces
     * @param path Pathway in which to search for entities
     * @param type Entity type to include in the search
     * @param results List of search results to add to
     * @param worker Status bar to update with current process
     * @param progress String to add entity name to and set to current status
     * @return Entities matching the search criteria
     */
    private void getEntities(String term,
            boolean wholeWordOnly, Pathway path, EntityType type,
            ArrayList<SearchResultInterface> results, NewThreadWorker worker,
            String progress)
    {
        EntityVector entities = new EntityVector();
        //Get all entities in the pathway
        Entity[] pathEnts;
        try {
            pathEnts = path.getEntities(type).toArray();
        }
        catch(NullPointerException e)
        {
            return;
        }
        //For each entitiy, filter based on criteria
        for(Entity ent : pathEnts) //actual metnet entities
        {
            //Add entities if their name includes the search term
            if(!entities.contains(ent)) //Doesn't work in the MetNet API
            {
                worker.setStatusTitle(progress + ent.name);
                if(wordContains(ent.name, term, wholeWordOnly, false))
                    entities.add(ent);
            }
        }

        //Add filtered entities to the results list
        for(int i = 0; i < entities.count(); i++) //convert it to our entities search result (Specific to us)
        {
            //Create the results object
            EntitySearchResult ent = new EntitySearchResult(entities.get(i));
            //Add it if it's not already in the results
            if(!results.contains(ent))
                results.add(ent);
        }
    }

    /**
     * Creates filter tables corresponding to the given networks. Filter types
     * considered include: Common Entities, Interactions
     * @param networks Networks to create corresponding filter tables for
     * @return filter tables corresponding to the given networks
     */
    public ArrayList<FilterTable> getFilters(NetworkResultInterface[] networks,
            PropertiesInterface properties) {

        ArrayList<FilterTable> ret = new ArrayList<FilterTable>();

//        //Get interactions from each network
//        ArrayList<InteractionFilter> interactions = new ArrayList<InteractionFilter>();
//        for(NetworkResultInterface net : networks)
//        {
//            //Get all interactions in the current network
//            Pathway path = (Pathway)net.getNetwork();
//            Interaction[] curInteractions = path.getInteractions().toArray();
//            //For each interaction, if there's already a filter for it, add the
//            //current network to its list of filtered networks
//            for(int i = 0; i < curInteractions.length; i++)
//            {
//                //Create the new filter
//                InteractionFilter curFilter = new InteractionFilter(curInteractions[i]);
//                PathwayNetworkResult pathNet = new PathwayNetworkResult(path);
//                curFilter.addFiltered(pathNet);
//                //If the filter doesn't already exist, add it
//                if(!interactions.contains(curFilter))
//                    interactions.add(curFilter);
//                //If the filter exists, add the pathway to its list of filtered
//                else
//                {
//                    InteractionFilter interac = (InteractionFilter)interactions
//                            .get(interactions.indexOf(curFilter));
//                    interac.addFiltered(pathNet);
//                }
//            }
//        }
//        //Create the table of interaction filters
//        InteractionFilter[] conv_filters = new InteractionFilter[interactions.size()];
//        int i = 0;
//        for(InteractionFilter interaction : interactions)
//        {
//            conv_filters[i] = interaction;
//        }
//        FilterTable interactionTable = new FilterTable(this.getWindowManager(), properties, conv_filters);
//
//        //Combine all of the filter tables found
//        ret.add(interactionTable);
        return ret;
    }

    /**
     * This method finds pathways that contain every interaction as passed
     * in in an Array
     *
     * @param interactions The desired interactions.
     * @param pathways The possible pathways.
     * @return The set of Pathways that contain all the given interactions.
     */
    public Pathway[] containsInteractions(Interaction[] interactions, Pathway[] pathways) {

        ArrayList<Pathway> subset = new ArrayList<Pathway>();
        for (int i = 0; i < pathways.length; i++) {
            for (int j = 0; j < interactions.length; j++) {
                if (!pathways[i].contains(interactions[j])) {
                    break;
                }
                if (j == interactions.length && pathways[i].contains(interactions[j])) {
                    subset.add(pathways[i]);
                }
            }
        }
        return (Pathway[]) subset.toArray();

    }

    /**
     * Given a SearchResult (that the user picked from the list of searchresults)
     * this method should give back a list of all pathways that contain that
     * result.  So, given a EntitySearchResult, it should return an array of
     * all pathways that contain that entity. Given a PathwaySearchResult,
     * it should return an array with just that single pathway.
     *
     * @param searchResult The EntitySearchResult or PathwaySearchResult for which
     * you would like to find all containing pathways.
     *
     * @return The PathwaySearchResults that contain the given searchResult
     */
    public NetworkResultInterface[] getContainingPathways(SearchResultInterface searchResult,
            ResultsFilterInterface[] filters, String organism) {
        if (searchResult == null) {
            return null;
        } else if (searchResult.getClass().equals(PathwaySearchResult.class)) { //Pathway
            //return (PathwaySearchResult[]) new SearchResult[]{searchResult};
            PathwayNetworkResult pathRes = new PathwayNetworkResult((Pathway)searchResult.getResult());
            return new NetworkResultInterface[]{pathRes};
        } else { //Entity
            //Get the specific entity
            //Entity entity = new Entity(searchResult.getID());
            Entity entity = (Entity)searchResult.getResult();
            //Create a vector to send to the Pathway.search method
            EntityVector ent = new EntityVector();
            ent.add(entity);
            //Search for all pathways containing the entitiy
            Pathway[] paths = Pathway.search(ent).toArray();

            //Get the organisms to use
            if (organism == null)
                organism = ALL_ORGANISMS;

            //Get only pathways in the given organism
            ArrayList<Pathway> validPaths = new ArrayList<Pathway>();
            if(organism.equals(ALL_ORGANISMS)) //Use all pathways
            {
                for(Pathway p : paths) validPaths.add(p);
            }
            else //Use only pathways from the specific organism
            {
                for(Pathway p : paths)
                    if(p.organism.trim().toLowerCase().equals(organism.trim().toLowerCase()))
                        validPaths.add(p);
            }
            //Convert the vector to an array of PathwaySearchResults
            PathwayNetworkResult[] results = new PathwayNetworkResult[validPaths.size()];
            Iterator<Pathway> itr = validPaths.iterator();
            int i = 0;
            while(itr.hasNext())
            {
                PathwayNetworkResult pathRes = new PathwayNetworkResult(itr.next());
                results[i] = pathRes;
                i++;
            }
            return results;
        }
    }

    @Override
    protected String getName(Object o) {
        if(o instanceof Pathway) return ((Pathway)o).name;
        if(o instanceof Interaction) return ((Interaction)o).name;
        if(o instanceof Entity) return ((Entity)o).name;
        if(o instanceof LocalEntity) return ((LocalEntity)o).name;
        if(o instanceof Organism) return ((Organism)o).name;
        return null;
    }

    @Override
    protected Iterable<Interaction> getNetworkReactions(Pathway pwy) {
        return Arrays.asList(((Pathway)pwy).getInteractions().toArray());
    }

    @Override
    protected Iterable<LocalEntity> getEdgeSource(Interaction rxn) {
        ArrayList<LocalEntity> rst = new ArrayList<LocalEntity>();
        for(LocalEntity m : ((Interaction)rxn).getReactants().toArray())
            rst.add(m);
        return rst;
    }

    @Override
    protected Iterable<LocalEntity> getEdgeTarget(Interaction rxn) {
        ArrayList<LocalEntity> rst = new ArrayList<LocalEntity>();
        for(LocalEntity m : ((Interaction)rxn).getProducts().toArray())
            rst.add(m);
        return rst;
    }

    @Override
    protected String getType(Object o) {
        String metNetType = "";
        if(o instanceof LocalEntity)
            metNetType = ((LocalEntity)o).getEntity().type;
        else if(o instanceof Entity)
            metNetType = ((Entity)o).type;
        else if(o instanceof Interaction)
            metNetType = ((Interaction)o).type;
        //Convert to Cytoscape type if known
        if(typeMap.containsKey(metNetType))
            return typeMap.get(metNetType);
    	return UNKNOWN;
    }

    @Override
    protected String getLocation(Object o) {
        String rst = null;
        if(o instanceof LocalEntity) rst = ((LocalEntity)o).cellLocation;
        if(o instanceof Interaction) rst = ((Interaction)o).cellLocation;
        if(rst==null || rst.equals("not assigned"))
            rst = CYTOSOL;
        return rst;
    }

    @Override
    protected ArrayList<String> getSynonyms(Object o) {
        String[] synonyms = new String[0];
        if(o instanceof LocalEntity)
            synonyms = ((LocalEntity)o).getEntity().getSynonyms();
        else if(o instanceof Entity)
            synonyms = ((Entity)o).getSynonyms();

        ArrayList<String> lst = new ArrayList();
        for(String syn : synonyms)
            lst.add(syn);
        return lst;
    }

    @Override
    protected Iterable<LocalEntity> getEdgeModifiers(Interaction rxn) {
        ArrayList<LocalEntity> rst = new ArrayList<LocalEntity>();
        for(LocalEntity m : ((Interaction)rxn).getModifiers().toArray())
            rst.add(m);
        return rst;
    }

    @Override
    protected String getId(Object o) {
        if(o instanceof Pathway) return ((Pathway)o).id+"";
        if(o instanceof Interaction) return ((Interaction)o).id+"";
        if(o instanceof Entity) return ((Entity)o).id+"";
        if(o instanceof LocalEntity) return ((LocalEntity)o).id+"";
        if(o instanceof Organism) return ((Organism)o).id+"";
        return null;
    }

    @Override
    protected String getOrganism(Object o) {
        if(o instanceof Pathway) return ((Pathway)o).organism;
        if(o instanceof Interaction) return ((Interaction)o).organism;
        if(o instanceof LocalEntity) return ((LocalEntity)o).getEntity().organism;
        if(o instanceof Entity) return ((Entity)o).organism;
        if(o instanceof Organism) return ((Organism)o).name;
        return null;
    }

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
    @Override
    public void setSelectedHighlyConnectedEntities(String[] names) {
        //Remove previous entities
        selhighlyConEnts.clear();
        //Store the entities corresponding to each selected name
        if(names != null)
            for(String name : names) selhighlyConEnts.add(name);
    }

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
    @Override
    protected boolean isSelectedHighlyConnectedEntity(String participant) {
        //Local Entities are part of an entity so get the containing entity
        //Entity ent = participant.getEntity();
        //Compare each selected entity to the participant's containing entity
        Iterator<String/*Entity*/> itr = selhighlyConEnts.iterator();
        for( ; itr.hasNext(); )
        {
            String/*Entity*/ curParent = itr.next();
            if(/*ent*/participant.equals(curParent)) return true; //TODO: Do we need to compare ids instead?
        }
        return false;
    }

    /* Not used
    @Override
    protected boolean isDirected(Interaction edge) {
        if(edge.proreversal == ?)
            return true;
        return false;
    }
    */

    /**
     * This method was added for the main menu's progress bar. For MetnetAccess, since the progress can be tracked
     * using the number of pathways, the progress bar doesn't need to be intermediate.
     * @return false if the main menu's progress bar is not intermediate
     */
    @Override
    public boolean isIntermediate(){
        return false;
    }

}