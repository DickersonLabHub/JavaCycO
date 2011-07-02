package edu.iastate.pathwayaccess.cytoscape.biocyc;

import edu.iastate.javacyco.OrgStruct;
import edu.iastate.javacyco.PtoolsErrorException;
import edu.iastate.javacyco.Pathway;
import edu.iastate.javacyco.Complex;
import edu.iastate.javacyco.Catalysis;
import edu.iastate.javacyco.EnzymeReaction;
import edu.iastate.javacyco.Protein;
import edu.iastate.javacyco.TransportReaction;
import edu.iastate.javacyco.Frame;
import edu.iastate.javacyco.Reaction;
import edu.iastate.javacyco.OntologyTerm;
import edu.iastate.javacyco.Compound;
import edu.iastate.javacyco.Gene;
import edu.iastate.javacyco.TranscriptionUnit;
import edu.iastate.javacyco.GOCellularComponent;
import edu.iastate.pathwayaccess.cytoscape.ClientUI.FilterTable;
import edu.iastate.pathwayaccess.cytoscape.PathwayAccessPlugin;
import edu.iastate.pathwayaccess.cytoscape.ResultListItems.*;
import edu.iastate.pathwayaccess.cytoscape.biocyc.ResultListItems.*;
import edu.iastate.pathwayaccess.cytoscape.NewThreadWorker;
import edu.iastate.pathwayaccess.common.Prompt;
import edu.iastate.pathwayaccess.cytoscape.PropertiesInterface;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import edu.iastate.javacyco.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides all access to the BioCyc database to the user interface.
 * Any methods that are specific to the database are called via this class from
 * the user interface.
 * @author Greg Hazen, ghazen@iastate.edu
 */
public class BioCycAccess extends PathwayAccessPlugin<Pathway, Reaction, Frame, Frame> {

    /**
     * Stores whether the user has logged into the database
     */
    boolean loggedIn = false; //this is only used in login() for login purposes but not the funtionality itself
    /**
     * True if the connection to the database is good
     */
    boolean connected = false; //this is only used in login()  
    /**
     * Stores the connect to the BioCyc database
     */
    JavacycConnection cyc; 
    /**
     * Stores all organism names in the database
     */
    ArrayList<String> organisms = null;
    /**
     * Stores all the organism IDs
     */
    ArrayList<String> organismsID = null;
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
    
    /**
     * The constructor Sets up the database common type String relations, creates the plugins
     * menu item in Cytoscape, and sets the starting action for when the menu
     * item is pressed. 
     */
    public BioCycAccess()
    {
        //The constructor of PathwayAccessPlugin is called since all the plugins are constructed similarly,
        //the difference is in the properties which is taken care of by passing in BioCycPropertie. 
        super(new BioCycProperties());
    }

    /**
     * Requests the BioCyc ip/name and port to connect to and logs the user into
     * the BioCyc database. If an error occurs, an error message is shown to the
     * user and the plugin is not logged into the database. If a connection has
     * already been previously established, the connect is tested and the
     * success of the test is returned.
     * @return True if connecting to BioCyc is successful, otherwise false.
     */
    private boolean login()
    {
        //Check if already connected, if the user is already logged in and connected, makes sure the connection is still good and returns at this point
        if(loggedIn && connected)
        {
            //Test that the connection is still good
            try {
                cyc.testConnection();
                return true;
            } catch (Exception e) {
                loggedIn = false;
                connected = false;
                Prompt.errorMessage("Connection Problem", "There was a problem " +
                        "connecting to the BioCyc server. \nSomething about a " +
                        e.getClass().getName()+": "+e.getMessage()+"...");
                return false;
            }
        }
        
        //If the user isn't already logged in, log into BioCyc database
        String server = Prompt.userEnter("Connect to...","Enter BioCyc server ip/name"); 
        int port = Integer.parseInt(Prompt.userEnter("Connect to...","Enter port"));
        cyc = new JavacycConnection(server,port); //creates a connection to the BioCyc database using the given server name and port number
        cyc.selectOrganism("META");
        
        /**
         * This is temporary, John is working on making the cache work so that it gets the results faster
         * but now it was getting the results correctly for the first search but not the searches after that 
         */
        cyc.setIsCaching(false);
        
        //makes sure everything went through successfully 
        try {
                cyc.testConnection();
                connected = true;
                loggedIn = true;
                return true;
        } catch (Exception e) {
                loggedIn = false;
                connected = false;
                Prompt.errorMessage("Connection Problem", "There was a problem " +
                        "connecting to "+server+":"+port+". \nSomething about a " +
                        e.getClass().getName()+": "+e.getMessage()+"...");
                return false;
        }
    }

    @Override
    public String[] getOrganismNames() {
        //Connect to the database if not already connected
        if(!login()) return null; //no where does it check if it returns null and makes a connection?????? here and getsearchType

        organisms = new ArrayList<String>();
        organismsID = new ArrayList<String>();
////Temporarily hard-code the organism names
//        organisms.add("ECOLI");
//        organisms.add("META");

//TODO: The method cyc.allOrgs() isn't working but John is trying to fix this.
//Once the method is fixed this code should be un-commented and the above hard-coding
//of ECOLI and META should be removed.
        
        Iterator<OrgStruct> itr = null;
        try {
            //allOrgs() is a method in javacyc that returns a list of all organism
//            for(OrgStruct orgstruct : cyc.allOrgs()) 
//            orgstruct.print();

            ArrayList<OrgStruct> orgs = new ArrayList<OrgStruct>();
            orgs = cyc.allOrgs();
            itr = orgs.iterator();
        } catch (PtoolsErrorException ex) {
            return null;
        }
        while(itr.hasNext())
        {
            OrgStruct next = itr.next();
            //An orgstruct includes the organism name which is retrieved by the .getSpecies method and
            //the organism id which is retrieved by the .getLocalID method
            organisms.add(next.getSpecies());
            organismsID.add(next.getLocalID());
        }

        //Get all organism names into an array
        String[] ret = new String[organisms.size()+1];
        ret[0] = ALL_ORGANISMS;
        Iterator<String> validNames = organisms.iterator();
        for(int i = 1; i < ret.length; i++)
        {
            ret[i] = validNames.next();
        }
        return ret;
    }

    @Override
    public String[] getSearchTypes() {
        //Connect to the database if not already connected
        if(!login()) return null;

        //Only access the database once to save time
        if (searchTypeNames != null)
            return searchTypeNames;

        
        //SeachTypeNames were hardcoded because it took too long to get them through Frame.allFrames
        
        //Store a string of all entity type names
        searchTypeNames = new String[22];//searchTypes.length + 2];
        searchTypeNames[0] = ALL_SEARCH_TYPES;
        searchTypeNames[1] = PATHWAY_SEARCH_TYPE;
        //searchTypeNames[2] = ID_SEARCH_TYPE;
        searchTypeNames[2] = Catalysis.GFPtype;
        searchTypeNames[3] = CellComponent.GFPtype;
        searchTypeNames[4] = Chromosome.GFPtype;
        searchTypeNames[5] = Complex.GFPtype;
        searchTypeNames[6] = Compound.GFPtype;
        searchTypeNames[7] = DNAReaction.GFPtype;
        searchTypeNames[8] = EnzymeReaction.GFPtype;
        searchTypeNames[9] = GOBiologicalProcess.GFPtype;
        searchTypeNames[10] = GOCellularComponent.GFPtype;
        searchTypeNames[11] = GOMolecularFunction.GFPtype;
        searchTypeNames[12] = GOTerm.GFPtype;
        searchTypeNames[13] = Gene.GFPtype;
        searchTypeNames[14] = Monomer.GFPtype;
        searchTypeNames[15] = edu.iastate.javacyco.Promoter.GFPtype;
        searchTypeNames[16] = edu.iastate.javacyco.Protein.GFPtype;
        searchTypeNames[17] = edu.iastate.javacyco.Reaction.GFPtype;
        searchTypeNames[18] = edu.iastate.javacyco.Regulation.GFPtype;
        searchTypeNames[19] = edu.iastate.javacyco.SmallMoleculeReaction.GFPtype;
        searchTypeNames[20] = edu.iastate.javacyco.TranscriptionUnit.GFPtype;
        searchTypeNames[21] = edu.iastate.javacyco.TransportReaction.GFPtype;

        //Original code:
        /*
        //Removed because Frame.allFrames takes significantly long
        try {
            searchTypes = (Frame[]) Frame.allFrames(cyc).toArray();
        } catch (PtoolsErrorException ex) {
            return null;
        }
        //Get the Frame corresponding to each search type
        searchTypes = new Frame[searchTypeNames.length];
        for(int i = 0; i < searchTypeNames.length; i++)
        {
            try {
                searchTypes[i] = Frame.load(cyc, searchTypeNames[i]);
            } catch (PtoolsErrorException ex) {
                return null;
            }
        }*/

        return searchTypeNames;
    }

    @Override
    public ArrayList<SearchResultInterface> search(String searchTerm, boolean wholeWordOnly, String organism, String searchType, NewThreadWorker worker) {
        //Connect to the database if not already connected
        if(!login()) return null;

//PART I: checks and translates the user input 
        //checks throughout if the user has canceled
        if(worker.isCancelled())
            return null;
        worker.setStatusTitle("Preparing search criteria...");
       
   //<--checks passed in values or any faults-->
        //Check for null organism or search type 
        //If the passed in organism or search type was null, it functions as if the user chose "All organisms"/"All search types"
        if (organism == null)
            organism = ALL_ORGANISMS;
        if (searchType == null)
            searchType = ALL_SEARCH_TYPES; 
        
        //Make sure lists of available organisms and entity types are set 
        if (organisms == null)
            getOrganismNames();
        
        if(worker.isCancelled())
            return null;
        
        //checking entity type like organism 
        if (searchTypeNames == null)
            getSearchTypes();
        
     //<--translates passed in values for the search-->  
        
        //Sort search terms the user passed in and stores it in an array 
        String[] terms = splitSearchTerms(searchTerm);

        
   
        
        //Create an array of the organism(s) to search 
        //orgs here will store the IDs of the organisms instead of the name since we select them using ID
        String[] orgs = new String[0]; 
      
        //If the user selects "All organism", orgs will contain all IDs of the organisms 
        if(organism.equals(ALL_ORGANISMS)) 
        {
            //oNames = (String[]) organisms.toArray();
            orgs = new String[organismsID.size()]; 
            Iterator<String> itr = organismsID.iterator();
            for(int i = 0; itr.hasNext(); i++)
                orgs[i] = itr.next();
        }
        else //Get the matching organism ID if the user chooses any other organism 
        {
            Iterator<String> orgNames = organisms.iterator();
            Iterator<String> orgIDs = organismsID.iterator();
            String name = "";
            String id = "";
            String toUpper = organism.trim().toUpperCase();
            while(orgNames.hasNext())
            {
                name = orgNames.next();
                id = orgIDs.next();
                if(name.trim().toUpperCase().equals(toUpper)) //if the name we're on matches the name passed in,
                    orgs = new String[]{id}; //add the corresponding organism id to orgs
            }
        }

        //if the user passes in "All search types", searchType is assigned the corresponding type that JavaCyco recognizes going in its search method 
        if(searchType.equals(ALL_SEARCH_TYPES))
        {
            searchType = Frame.GFPtype; 
        }else if(searchType.equals(PATHWAY_SEARCH_TYPE)){ //if user passes in "Pathway", search Type is assigned the corresponding type 
            searchType = Pathway.GFPtype;
        }
        
//PART TWO: searching 
        //Stores the results matching the criteria (variable returned)
        ArrayList<SearchResultInterface> results = new ArrayList<SearchResultInterface>();
         
         String nameOrg="";
        
        if(worker.isCancelled())
            return null;

        //Perform search
        Double progress = 0.0;
        Double increment = -1.0; //10000.0 / 75000;
        int numOrgs = 0, numPaths = 0, numTerms = 0, numTypes = 0;
   
        //for each organism that contains pathways  
        for(String org : orgs)
        {
            if(worker.isCancelled())
                return null;
            if(increment.equals(-1.0))
                numOrgs = orgs.length;
            //Set to the current organism  
            cyc.selectOrganism(org); //allows us from now to search within this specific organism by passing in the id of the organism 
       /*
         cyc.selectOrganism("VITI");   
         */
            
            
            //For each search term:
            for(String term : terms)
            {
                if(increment.equals(-1.0))
                        numTerms = terms.length;
                
                for(int i= 0; i<organisms.size();i++)
                    if(org.equalsIgnoreCase(organismsID.get(i)))
                        nameOrg = organisms.get(i);
                String prog = term + " in " + nameOrg + ": ";//need to change this//
                worker.setStatusTitle(prog);
                
                //The results cyc.search returns are Frames (Frames is an overarching type for everything)
                ArrayList<Frame> curResults = null; 
                try {
                    curResults = cyc.search(term, searchType); //what about whole words only?
                    /*
                     cyc.search("h2o", "OCELOT-GFP::FRAMES");     for the "All types choice"
                     cyc.search("h20", "|Enzymatic-Reactions|");     for the Enzymatic-Reactions 
                     
                     */
                } catch (PtoolsErrorException ex) { 
                    return null;
                }
               for(Frame fr : curResults)
                {
                    //if the frame is a pathway, create the appropriate result 
                    if(fr instanceof Pathway) 
                    {
                        PathwaySearchResult res = new PathwaySearchResult((Pathway)fr);
                        results.add(res);
                    }
                    else if(!(fr instanceof Reaction))
                    {
                        EntitySearchResult res = new EntitySearchResult(fr); 
                        results.add(res);
                    }
                }
                
            }
            
        }
        return results;
    }

    @Override
    public NetworkResultInterface<Pathway>[] getContainingPathways(SearchResultInterface searchResult, ResultsFilterInterface[] filters, String organism) {
        //Connect to the database if not already connected
        if(!login()) return null;

        if (searchResult == null) {
            return null;
        } else if (searchResult.getClass().equals(PathwaySearchResult.class)) { //Pathway
            //return (PathwaySearchResult[]) new SearchResult[]{searchResult};
            PathwayNetworkResult pathRes = new PathwayNetworkResult((Pathway)searchResult.getResult());
            return new NetworkResultInterface[]{pathRes};
        } else { //Entity
            //Get the specific entity
            //Entity entity = new Entity(searchResult.getID());
            Frame entity = (Frame)searchResult.getResult();

            //Get the organism(s) to use
            if (organism == null)
                organism = ALL_ORGANISMS;
            ArrayList<String> orgsToUse = new ArrayList<String>();
            if(organism.equals(ALL_ORGANISMS)) //Use pathways from all organisms
                orgsToUse = organismsID;
            else //Use only pathways from the specific organism
            {
                for(int i=0; i<organisms.size(); i++){
                    if(organisms.get(i).equals(organism))
                        orgsToUse.add(organismsID.get(i));
                }
                    
//                for(String orgName : organisms)
//                    for(String orgID: organismsID)
//                        if(orgName.equals(organism))
//                             orgsToUse.add(orgID);
            }

            ArrayList<PathwayNetworkResult> results = new ArrayList<PathwayNetworkResult>(); //Stores containing pathways
            
            
            //ArrayList<PathwayNetworkResult> results = new ArrayList<PathwayNetworkResult>(); //Stores containing pathways
            //Get pathways from each organism to use
            ArrayList<Pathway> pathways= new ArrayList<Pathway>();
            for(String org : orgsToUse)
            {
                cyc.selectOrganism(org);
                //Search for all pathways containing the entitiy
                //ArrayList<Frame> paths = new ArrayList<Frame>();
                
                try {
                   ArrayList<Frame> paths = entity.getPathways(); //.search(ent).toArray(); ?????????????
                    
                    for (Frame f: paths){
                        pathways.add((Pathway)f);
                    }
                } catch (PtoolsErrorException ex) {
                    return null;
                }
               
               
                
                for(Pathway path : pathways)
                {
                    
                        
                    //The entity does exists in this pathway
                    PathwayNetworkResult pathRes = new PathwayNetworkResult(path);
                    if(!results.contains(pathRes)) results.add(pathRes);
                       
                   
                }
            }
            PathwayNetworkResult[] returned = new PathwayNetworkResult[results.size()];
            int i=0;
            for(PathwayNetworkResult network: results){
                returned[i]=network;
                i++;
            }

//            PathwayNetworkResult pathRes = new PathwayNetworkResult((Pathway)searchResult.getResult());
//            return new NetworkResultInterface[]{pathRes};
            //NetworkResultInterface<Pathway>[] returned = (NetworkResultInterface<Pathway>[]) (results.toArray());
            return returned;
                    //return (NetworkResultInterface<Pathway>[]) (results.toArray());
//return pathways;
            
            
//            //Get pathways from each organism to use
//            for(String org : orgsToUse)
//            {
//                cyc.selectOrganism(org);
//                //Search for all pathways containing the entitiy
//                ArrayList<Pathway> paths = new ArrayList<Pathway>();
//                try {
//                    paths = (Pathway)entity.getPathways(); //.search(ent).toArray();
//                } catch (PtoolsErrorException ex) {
//                    return null;
//                }
////                for(Pathway path : paths)
////                {
////                    ArrayList<String> entIDs = new ArrayList<String>();
////                    try {
////                        entIDs = path.getEntityIDs();
////                    } catch (PtoolsErrorException ex) {
////                        return null;
////                    }
////                    for(String id : entIDs)
////                    {
////                        if(id.equals(entity.getLocalID()))
////                        {
////                            //The entity does exists in this pathway
////                            PathwayNetworkResult pathRes = new PathwayNetworkResult(path);
////                            if(!results.contains(pathRes)) results.add(pathRes);
////                        }
////                    }
////                }
//            }

    
            
        }




    }

    @Override
    public ArrayList<FilterTable> getFilters(NetworkResultInterface[] networks, PropertiesInterface properties) {
        ArrayList<FilterTable> ret = new ArrayList<FilterTable>();
        return ret;
    }

    @Override
    public void setUpNameRelations() {
         //This method is specific to metNetAccess. This functionality is done in the getType method
        return;
    }

    @Override
    protected String getName(Object o) {
        //Connect to the database if not already connected
        if(!login()) return null;

        try {
            if(o instanceof OrgStruct)
                return ((OrgStruct)o).getSpecies();
            else if(o instanceof String)
                return cyc.getSlotValue((String)o, "COMMON-NAME").replace("\"","");
            else
                return ((Frame)o).getCommonName();
        } catch (PtoolsErrorException e) {
            return null;
        }
    }

    @Override
    protected String getType(Object o) {
        if(o instanceof TranscriptionUnit) return GENE;
        if(o instanceof Gene) return RNA;
        if(o instanceof Complex) return COMPLEX;
        if(o instanceof Protein) return PROTEIN;
        if(o instanceof Compound) return SIMPLE_MOLECULE;
        if(o instanceof Reaction) return STATE_TRANSITION;
        if(o instanceof Catalysis) return CATALYSIS;
        if(o instanceof TransportReaction)return TRANSPORT;
        return UNKNOWN;
    }

    @Override
    protected String getOrganism(Object o) {
        //Connect to the database if not already connected
        if(!login()) return null;

        if(o instanceof Frame)
        {
            try {
                return ((Frame) o).getSlotValue("ORGANISM");
            } catch (PtoolsErrorException ex) {
                return UNKNOWN;
            }
        }
        else 
            return UNKNOWN;
    }

    @Override
    protected String getLocation(Object o) {
        //Connect to the database if not already connected
        if(!login()) return null;

        Frame f = (Frame)o;
        String locId = f.annotations.get("COMPARTMENT");
        String loc = null;
        try
        {
            if(locId!=null && locId.length()==0)
            {
                if(cyc.frameExists(locId))
                    loc = Frame.load(this.cyc,locId).getCommonName();
                else loc = locId;
            }
            if(loc==null)
            {
                if(f instanceof Protein)
                {
                    ArrayList<OntologyTerm> terms = new ArrayList<OntologyTerm>();
                    terms.addAll(((Protein)f).getCellComponents());
                    OntologyTerm l = OntologyTerm.getLowestCommonChild(cyc,terms);
                    if(l==null)
                    {
                        terms = new ArrayList<OntologyTerm>();
                        for(OntologyTerm t : (((Protein)f).getGOterms()))
                        {
                            if(t instanceof GOCellularComponent)
                                terms.add(t);
                        }
                        l = OntologyTerm.getLowestCommonChild(cyc,terms);
                    }
                    if(l != null) loc = l.getCommonName();
                }
            }
            if(loc==null || loc.equals("cytoplasm"))
            {
                loc = CYTOSOL;
            }
            loc = loc.replace("\"","");
            if(!loc.startsWith("extracell"))
            {
                if(loc.equals("unknown space"))
                {
                        loc = CYTOSOL;
                }
                //if((f instanceof Compound))
                //	loc = "cytosol";
                //System.out.println(f.getCommonName()+" IN "+loc);
            }
        }
        catch(Exception ex)
        {
                return null;
        }
        if(loc==null) loc = CYTOSOL;
        return loc;
    }

    @Override
    protected ArrayList<String> getSynonyms(Object o) {
        //Connect to the database if not already connected
        if(!login()) return null;

        ArrayList<String> rst = new ArrayList<String>();
        try {
            for(String s : ((Frame)o).getSynonyms()) rst.add(s.replace("\"",""));
        } catch (PtoolsErrorException e) {
            return null;
        }
        finally
        {
            return rst;
        }
    }

    @Override
    protected Iterable<Reaction> getNetworkReactions(Pathway pwy) {
        //Connect to the database if not already connected
        if(!login()) return null;

        try {
            return pwy.getReactions();
        } catch (PtoolsErrorException e) {
            return null;
        }
    }

    @Override
    protected Iterable<Frame> getEdgeSource(Reaction reaction) {
        //Connect to the database if not already connected
        if(!login()) return null;

        try {
            return reaction.getReactants();
        } catch (PtoolsErrorException e) {
            return null;
        }
    }

    @Override
    protected Iterable<Frame> getEdgeTarget(Reaction reaction) {
        //Connect to the database if not already connected
        if(!login()) return null;

        try {
            return reaction.getProducts();
        } catch (PtoolsErrorException e) {
            return null;
        }
    }

    @Override
    protected Iterable<Frame> getEdgeModifiers(Reaction reaction) {
        //Connect to the database if not already connected
        if(!login()) return null;

        ArrayList<Frame> rst = new ArrayList<Frame>();
        HashSet<String> enzDone = new HashSet<String>();
        try
        {
            if(reaction instanceof EnzymeReaction)
            {
                for(Frame f : ((EnzymeReaction)reaction).getCatalysis())
                {
                    Catalysis c = (Catalysis)f;

                    Protein p = c.getEnzyme();
                    if(p==null || enzDone.contains(p.getCommonName())) continue;
                    enzDone.add(p.getCommonName());
                    rst.add(c);//new Frame(p,PluginReactionSymbolType.CATALYSIS,c.getKm()));

                    for(Frame f2 : c.getCofactors())
                    {
                        rst.add(f2);//new Frame(f2,PluginReactionSymbolType.PHYSICAL_STIMULATION));
                    }
                    for(Frame f2 : c.getProstheticGroups())
                    {
                        rst.add(f2);//new Frame(f2,PluginReactionSymbolType.PHYSICAL_STIMULATION));
                    }
                    for(Frame f2 : c.getActivators())
                    {
                        rst.add(f2);//new Frame(f2,PluginReactionSymbolType.TRIGGER));
                    }
                    for(Frame f2 : c.getInhibitors())
                    {
                        rst.add(f2);//new Frame(f2,PluginReactionSymbolType.INHIBITION));
                    }
                }
            }
        }
        catch(Exception ex)
        {
            return null;
        }
        return rst;
    }

    @Override
    protected String getId(Object o) {
        if(o instanceof Frame) return ((Frame)o).getLocalID();
        else return null;
    }

    @Override
    public void setSelectedHighlyConnectedEntities(String[] names) {
        //Remove previous entities
        selhighlyConEnts.clear();
        //Store the entities corresponding to each selected name
        if(names != null)
            for(String name : names) selhighlyConEnts.add(name);
    }

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
    /*
     * This method was added for the main menu's progress bar. For BioCycAcess, since the progress can't be tracked,	
     * the progress bar needs to be intermediate. 	
     * @return true if the main menu's progress bar is intermediate 	
     */	
    @Override
    public boolean isIntermediate(){
	
        return true;
	
    }
    
}
