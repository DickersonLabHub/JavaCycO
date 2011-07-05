package edu.iastate.javacyco;
/**
 *  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import java.io.*;
import java.util.*;
import java.net.Socket;
import java.net.SocketException;




/**
	This class can be a connection to either a local BioCyc server via a Unix domain socket, or to a remote 
	BioCyc server running a JavacycServer remote socket listener.
	It is backwards compatible with the original Javacyc class from TAIR.
	See http://bioinformatics.ai.sri.com/ptools/ptools-fns.html for the PathwayTool Lisp functions.
	The PathwayTools software locks up when queried for entities with "&alpha;" in the name, so these are always skipped.
	@author Thomas Yan
	@author John Van Hemert (edited, added to)
	@see <a href='http://bioinformatics.ai.sri.com/ptools/ptools-fns.html'>PathwayTools functions</a>
	
*/
public class JavacycConnection {
	private Boolean remote;
	private String server;
	private int port;
	private Socket socket;
	
    private UnixDomainSocket uds; // J-BUDS Unix domain socket
    private String socketName; // name of the socket
    private String organism; // name of the organism
    private PrintWriter out; // output to the Pathway Tools server
    private BufferedReader in; // input from the Pathway Tools server
    
	/**
	If this flag is set to true, all build and use a cache of Frame objects that have been loaded from the PGDB.
	*/
	private boolean caching = true;
	private int cacheHits = 0;
    
    private ArrayList<Long> waits;
    
    /**
     * A map of hierarchically indented pathway class names -> pathway class frame ids that can caches the current organisms 
     * pathway ontology.
     */
    public LinkedHashMap<String,String> pathwayOntologyCache;
    
    /**
     * A map of Frame id's to cached Frame objects in memory.  Used by the Frame's load method.  Only used when the static flag cache in Frame is set to true;
     */
    public HashMap<String,Frame> cache;
	
    /**
       Constructor for a local JavacycConnection using a Unix Domain Socket.
       Attempts to connect to local UnixDomainSocket for PathwayTool API communication.
    */
    public JavacycConnection()
    {
    	socketName = "/tmp/ptools-socket";
    	remote = false;
    	commonSetup();
    }
    
    public boolean isCaching()
    {
    	return caching;
    }
    
    public void setIsCaching(boolean b)
    {
    	caching = b;
    }
    
    public void incrementCacheHits()
    {
    	cacheHits++;
    }
    
	/**
	Constructor for a remote JavacycConnection using a java.net.Socket.
	Note: this requires that the remote server is running a socket listener.  See JavacycServer.
	@param server The hostname or IP address of the remote BioCyc server to connect to.
	@param port  The port that the server is listening on.
	*/
    public JavacycConnection(String server, int port)
    {
    	this.remote = true;
    	this.server = server;
    	this.port = port;
    	commonSetup();
    }
    
    private void commonSetup()
    {
    	waits = new ArrayList<Long>();
    	pathwayOntologyCache = new LinkedHashMap<String,String>();
    	cache = new HashMap<String,Frame>();
    }
    
    public void testConnection() throws Exception
    {
    	if(remote)
    	{
			Socket s = new Socket(server,port);
			s.close();
    	}
    	else
    	{
		    // Create socket and connect to the server
    		UnixDomainSocket u = new UnixDomainSocket(socketName);
    		u.close();
    	}
    }

    /**
       Get a socket connection with Pathway Tools using a Unix domain socket or with a remote BioCyc server.
    */
    private void makeSocket()
    {
    	if(remote)
    	{
    		try
    		{
    			socket = new Socket(server,port);
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(),true);
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    			throw new RuntimeException("Problem connecting to remote socket"); 
    		}
    	}
    	else
    	{
			try {
			    // Create socket and connect to the server
			    uds = new UnixDomainSocket(socketName);
			    out = new PrintWriter(uds.getOutputStream(), true);
			    in = new BufferedReader(
						    new InputStreamReader(uds.getInputStream()));
			} catch (IOException e) { 
			    e.printStackTrace();
			    throw new RuntimeException("Problem connecting to local socket"); 
			}
    	}
    }

    /**
       Close the socket connection with Pathway Tools.
       @throws IOException if the socket connection cannot be closed
    */
    private void closeSocket() {
    	//System.out.println("closing socket...");
	try {
	    if(uds != null)
	    	uds.close();
	    if(socket != null)
	    	socket.close();
	    if(out != null)
	    	out.close();
	    if(in != null)
	    	in.close();
	} catch (IOException e) {
	    e.printStackTrace();
	    throw new RuntimeException(); 
	}
    }


    // Methods that call the GFP Functions
    
    /**
    Calls the GFP function, get-kb-root-classes.
 	*/
    public ArrayList getKbRootClasses()
     throws PtoolsErrorException {
    	ArrayList rst = callFuncArrayNoWrap("OBJECT-NAME (get-kb-root-classes :type :all)");
    	return rst;
    	
    }
    
    /**
    Calls the GFP function, get-kb-frames.
 	*/
    public ArrayList getKbFrames()
     throws PtoolsErrorException {
    	ArrayList rst = callFuncArray("get-kb-frames");
    	return rst;
    }
    
    /**
    Calls the GFP function, get-facet-value
 	*/
    public String getFacetValue(String frame,String slot,String facet)
     throws PtoolsErrorException {
    	String rst = callFuncString("get-facet-value '"+frame+" '"+slot+" '"+facet);
    	return rst;
    }
    
    /**
    Calls the GFP function, get-frame-labeled
 	*/
    public ArrayList getFrameLabeled(String label)
     throws PtoolsErrorException {
    	return callFuncArray("get-frame-labeled '"+label);
    }
    
    
    /**
    Calls the GFP function, get-frame-type
    @param frame the ID of the frame to lookup
    @return :class if frame is a PGDB/KB class and :instance if it is an instance of a PGDB/KB class
 	*/
    public String getFrameType(String frame)
     throws PtoolsErrorException {
    	return callFuncString("get-frame-type '"+frame);
    }
    
    /**
    Calls the GFP function, get-value-annot
 	*/
    public String getValueAnnot(String frame,String slot,String value,String label)
     throws PtoolsErrorException {
    	String rst = callFuncString("get-value-annot '"+frame+" '"+slot+" '"+value+" '"+label);
    	return rst;
    }
    
    /**
    Calls the GFP function, get-value-annots
 	*/
    public ArrayList getValueAnnots(String frame,String slot,String value,String label)
     throws PtoolsErrorException {
    	ArrayList rst = callFuncArray("get-value-annots '"+frame+" '"+slot+" '"+value+" '"+label);
    	return rst;
    }
    
    /**
    Calls the GFP function, get-all-annots
 	*/
    public ArrayList<String> getAllAnnotLabels(String frame,String slot,String value)
     throws PtoolsErrorException {
    	//if(slot.equals("COMMENT") || (value.contains("&") && value.contains(";"))) return new ArrayList();
    	//value = value.replace(";","\\;");
    	ArrayList rst = callFuncArray("get-all-annots '"+frame+" '"+slot+" '"+value);
    	return rst;
    }
    
    public void addAnnotation(String frame,String slot,String value,String annotLabel,String annotValue) throws PtoolsErrorException
    {
    	callFuncArray("add-value-annot '"+frame+" '"+slot+" '"+value+" '"+annotLabel+" '"+annotValue);
    }
    
    public void addAnnotations(String frame,String slot,String value,String annotLabel,ArrayList annotValues) throws PtoolsErrorException
    {
    	callFuncArray("add-value-annots '"+frame+" '"+slot+" '"+value+" '"+annotLabel+" '"+ArrayList2LispList(annotValues));
    }
    
    public void putAnnotation(String frame,String slot,String value,String annotLabel,String annotValue) throws PtoolsErrorException
    {
    	callFuncArray("put-value-annot '"+frame+" '"+slot+" '"+value+" '"+annotLabel+" '"+annotValue);
    }
    
    public void putAnnotations(String frame,String slot,String value,String annotLabel,ArrayList annotValues) throws PtoolsErrorException
    {
    	callFuncArray("put-value-annots '"+frame+" '"+slot+" '"+value+" '"+annotLabel+" '"+ArrayList2LispList(annotValues));
    }
    
    public boolean hasAnnotatedValue(String frame,String slot) throws PtoolsErrorException
    {
    	for(Object vo : this.getSlotValues(frame, slot))
    	{
    		String value = (String)vo;
    		if(this.getAllAnnotLabels(frame, slot, value).size()>0) return true;
    	}
    	return false;
    }
    
    /**
    Calls the GFP function, get-facet-values
 	*/
    public ArrayList getFacetValues(String frame,String slot,String facet)
     throws PtoolsErrorException {
    	ArrayList rst = callFuncArray("get-facet-values '"+frame+" '"+slot+" '"+facet);
    	return rst;
    }
    
    /**
    Calls the GFP function, get-slot-facets
 	*/
    public ArrayList getSlotFacets(String frame,String slot)
     throws PtoolsErrorException {
    	ArrayList rst = callFuncArray("get-slot-facets '"+frame+" '"+slot);
    	return rst;
    }
    
    /**
    Calls the GFP function, create-instance-w-generated-id.
    Note: Classes must be encapsulated by pipes (||).
 	*/
    public String createInstanceWGeneratedId(String className)
     throws PtoolsErrorException {
    	String rst = callFuncString("create-instance-w-generated-id '"+className);
    	return rst;
    }
    
    /**
    Calls the GFP function, create-instance.
    Note: Classes must be encapsulated by pipes (||) and multiple classes must be in proper lisp list format.
    (use JavacycConnection.ArrayList2LispList(aList) to convert an ArrayList to this format).
 	*/
    public String createInstance(String id,String className)
     throws PtoolsErrorException {
    	String rst = callFuncString("create-instance '"+id+" '"+className);
    	return rst;
    }
    
    /**
    Calls the GFP function, rename-frame.
    Changes the name of frame to be the symbol new-frame.
 	*/
    public String renameFrame(String oldName,String newName)
     throws PtoolsErrorException {
    	String rst = callFuncString("rename-frame '"+oldName+" '"+newName);
    	return rst;
    }
 
    /**
    Calls the GFP function, delete-frame.
    Deletes frame from kb.
 	*/
    public String deleteFrame(String id)
     throws PtoolsErrorException {
    	String rst = callFuncString("delete-frame '"+id);
    	return rst;
    }
    
    /**
    Calls the GFP function, get-kb-frames.
 	*/
    public ArrayList<String> getKbClasses()
     throws PtoolsErrorException {
    	ArrayList<String> frames = getKbFrames();
    	ArrayList<String> rst = new ArrayList<String>();
    	for(String f : frames)
    	{
    		if(f.startsWith("|") && f.endsWith("|"))
    			rst.add(f);
    	}
    	return rst;
    }
    
    private ArrayList callFuncArrayNoWrap(String func)
    {
		makeSocket();
		try {
		    String query = "("+func+")";//wrapQuery(func);
		    sendQuery(query);
		    ArrayList results = retrieveResultsArray();
		    return results;
		} finally {
		    closeSocket();
		}
    }
    
    /**
    Calls the GFP function slot-has-value-p.
    @param frame a frame id or object
    @param slotName a slot name
    @return true iff own slot slot of frame has some value, local or nonlocal.
 */
	 public Boolean slotIsNil(String frame, String slotName)
	  throws PtoolsErrorException {
		return !callFuncBool("slot-has-value-p '" + frame + " '" + slotName);
	 }
	 
	    /**
	    Calls the GFP function slot-p.
	    @param frame a frame id or object
	    @param slotName a slot name
	    @return true iff slotName is a valid slot in frame.
	 */
		 public Boolean slotExists(String frame, String slotName)
		  throws PtoolsErrorException {
			 if(!this.frameExists(frame)) return false;
			return callFuncBool("slot-p '" + frame + " '" + slotName);
		 }

    /**
       Calls the GFP function, get-slot-values.
       @param frame a frame id or object
       @param slotName a slot name
       @return an ArrayList of all values of slot of frame
    */
    public ArrayList getSlotValues(String frame, String slotName)
     throws PtoolsErrorException {
    	ArrayList rst = callFuncArray("get-slot-values '" + frame + " '" + slotName);
    	//JavacycConnection.printListsNewLines(rst, "", System.out);
    	return rst;
    	
    }

    /**
       Calls the GFP function get-slot-value.
       @param frame a frame id or object
       @param slotName a slot name
       @return the first value of slot of frame
    */
    public String getSlotValue(String frame, String slotName)
     throws PtoolsErrorException {
	return callFuncString("get-slot-value '" + frame + " '" + slotName);
    }
    
	public static String ArrayList2LispList(ArrayList aList)
	{
		String rst = "(";
		for(Object item : aList)
		{
			if(item instanceof String)
				rst += (String)item+" ";
			else if(item instanceof ArrayList)
				rst += ArrayList2LispList((ArrayList)item);
		}
		return rst+") ";
	}
    
    /**
    Compares two ArrayLists by first converting them to potentially nested Lisp lists
     */
    public static boolean arrayListsEqual(ArrayList a,ArrayList b)
    {
    	return ArrayList2LispList(a).equals(ArrayList2LispList(b));
    }
    
    /**
    Checks if a frame exists.
     */
    public boolean frameExists(String id)
     throws PtoolsErrorException {
    	return this.coercibleToFrameP(id);//!(getSlotValue(id,"COMMON-NAME").equals("NIL"));
    }

    /**
       Calls the GFP function, get-class-slot-slotvalue.
       @param className the class
       @param slotName a slot name
       @param slotValue a slot value
       @return an ArrayList with the object names returned by 
       get-class-slot-slotvalue
    */
    public ArrayList getClassSlotSlotvalue(String className, String slotName,String slotValue)
     throws PtoolsErrorException {
	return callFuncArray("get-class-slot-slotvalue '" + className + " '" 
			     + slotName + " '" + slotValue);
    }

    /**
       Calls the GFP function, get-class-all-instances
       @param classFrame a class frame
       @return an ArrayList of all frames that are direct or indirect instances
       of classFrame
    */
    public ArrayList getClassAllInstances(String classFrame)
     throws PtoolsErrorException {
	return callFuncArray("get-class-all-instances '" + classFrame);
    }
    
    /**
    Calls the GFP function, get-class-all-instances
    @param classes a list of classes
    @return an ArrayList of all frames that are direct or indirect instances
    of classFrame
 */
 public ArrayList getClassAllInstances(Iterable<String> classes)
  throws PtoolsErrorException {
	 ArrayList rst = new ArrayList();
	 for(String classFrame : classes)
	 {
		 rst.addAll(callFuncArray("get-class-all-instances '" + classFrame));
	 }
	 return rst;
 }
    
    /**
    Calls getClassAllInstances and loads the Frames referenced by the return list of ids.
    @param GFPtype the id of the KB frame type to get all instances of this type.  Use the JavaCyc classes' static fields GFPtype, ie Compound.GFPtype.
    @return an ArrayList of all frames that are direct or indirect instances of the type GFPtype.
 */
    public ArrayList<Frame> getAllGFPInstances(String GFPtype)
    throws PtoolsErrorException {
    	return Frame.load(this,this.getClassAllInstances(GFPtype));
    }

    /**
       Calls the GFP function, instance-all-instance-of-p
       @param classFrame  a class frame
       @param instance an instance frame
       @return true if instance is a direct or indirect child of class
    */
    public boolean instanceAllInstanceOfP(String classFrame, String instance)
     throws PtoolsErrorException {
	return callFuncBool("instance-all-instance-of-p '" + instance + " '"
			    + classFrame);
    }

    /**
       Calls the GFP function, member-slot-value-p
       @param frame a frame id or object
       @param slot a slot name
       @param value a slot value 
       @return true if value is one of the values of slot of frame
    */
    public boolean memberSlotValueP(String frame, String slot, String value)
     throws PtoolsErrorException {
	return callFuncBool("member-slot-value-p '" + frame + " '" + slot 
			    + " '" + value);
    }

    /**
       Calls the GFP function, current-kb
       @return the currently selected KB
    */
    public String getOrganismID()
    {
    	return organism;
    }
    
    public Organism getOrganism()
    throws PtoolsErrorException {
    	return (Organism)Organism.load(this,getOrganismID());
    }

    /**
       Calls the GFP function, put-slot-values
       @param frame a frame id or object
       @param slot a slot name
       @param values a set of values
       @return any results from the server in an ArrayList
    */
    public ArrayList putSlotValues(String frame, String slot, String values)
     throws PtoolsErrorException {
	return callFuncArray("put-slot-values '" + frame + " '" + slot
			     + " '" + values);
    }

    /**
       Calls the GFP function, put-slot-value
       @param frame a frame id or object
       @param slot a slot name
       @param value a value
       @return any results from the server in an ArrayList
    */
    public ArrayList putSlotValue(String frame, String slot, String value)
     throws PtoolsErrorException {
	return callFuncArray("put-slot-value '" + frame + " '" + slot 
			     + " '" + value);
    }

    /**
       Calls the GFP function, add-slot-value
       @param frame a frame id or object
       @param slot a slot name
       @param value a value
       @return any results from the server in an ArrayList
    */
    public ArrayList addSlotValue(String frame, String slot, String value)
     throws PtoolsErrorException {
	return callFuncArray("add-slot-value '" + frame + " '" + slot
			     + " '" + value);
    }

    /**
       Calls the GFP function, replace-slot-value
       @param frame a frame id or object
       @param slot a slot name
       @param oldValue the value to be replaced
       @param newValue the value to replace oldValue with
       @return any results from the server in an ArrayList
    */
    public ArrayList replaceSlotValue(String frame, String slot, 
				      String oldValue, String newValue)
     throws PtoolsErrorException {
	return callFuncArray("replace-slot-value '" + frame + " '" + slot
			     + " '" + oldValue + " '" + newValue);
    }

    /**
       Calls the GFP function, remove-slot-value
       @param frame a frame id or object
       @param slot a slot name
       @return any results from the server in an ArrayList
    */
    public ArrayList removeSlotValue(String frame, String slot)
     throws PtoolsErrorException {
	return callFuncArray("remove-slot-value '" + frame + " '" + slot);
    }

    /**
       Calls the GFP function, coercible-to-frame-p
       @param thing a thing
       @return true if thing is a frame object, the name of a frame in kb, or
       handle of frame in kb
    */
    public boolean coercibleToFrameP(String thing)
     throws PtoolsErrorException {
	return callFuncBool("coercible-to-frame-p '" + thing);
    }

    /**
       Calls the GFP function, class-all-type-of-p
       @param classFrame a class frame
       @param instance an instance
       @return true if instance is an all-instance of classFrame
    */
    public boolean classAllTypeOfP(String classFrame, String instance)
     throws PtoolsErrorException {
	return callFuncBool("class-all-type-of-p '" + classFrame + " '" 
			    + instance);
    }

    /**
       Calls the GFP function, get-instance-direct-types
       @param instance an instance
       @return an ArrayList of the direct types of instance
    */
    public ArrayList getInstanceDirectTypes(String instance)
     throws PtoolsErrorException {
    	ArrayList rst = callFuncArray("get-instance-direct-types '" + instance);
//    	//this.printLists(rst);
//    	for(int i=0;i<rst.size();i++)
//    	{
//    		
//    		if(instance.equals((String)rst.get(i)))
//    		{
//    			rst.remove(i);
//    			break;
//    		}
//    	}
    	return rst;
    }

    /**
       Calls the GFP function, get-instance-all-types
       @param instance an instance
       @return an ArrayList of all-types of instance
    */
    public ArrayList getInstanceAllTypes(String instance)
	 throws PtoolsErrorException {
	    if(this.getFrameType(instance).equals(":CLASS")) return callFuncArray("get-class-all-supers '" + instance);
	    else return callFuncArray("get-instance-all-types '" + instance);
    }
    


    /**
       Calls the GFP function, get-frame-slots
       @param frame a frame id or object
       @return an ArrayList of instance or template slots associated with frame
    */
    public ArrayList getFrameSlots(String frame)
     throws PtoolsErrorException {
		ArrayList rst =  callFuncArray("get-frame-slots '" + frame);
		if(rst.size() == 0)
		{
			throw new PtoolsErrorException("No slots for frame "+frame);
		}
		return rst;
    }

    /**
       Calls the GFP function, put-instance-types
       @param instance an instance
       @param newTypes the classes that instances becomes an instance of
       @return the results from the server in an ArrayList
    */
    public ArrayList putInstanceTypes(String instance, String newTypes)
     throws PtoolsErrorException {
	return callFuncArray("put-instance-types '" + instance + " '" 
			     + newTypes);
    }

    /**
       Calls the GFP function, save-kb
       @return the results from the server in an ArrayList
    */
    public ArrayList saveKB()
     throws PtoolsErrorException {
	return callFuncArray("save-kb");
    }

    /**
       Calls the GFP function, revert-kb
       @return the results from the server in an ArrayList
    */
    public ArrayList revertKB()
     throws PtoolsErrorException {
	return callFuncArray("revert-kb");
    }

    /**
       Calls the GFP function, find-indexed-frame
       @param datum a datum
       @param className a class
       @return the results from the server in an ArrayList.  Some of the
       elements in the returned ArrayList may be ArrayLists themselves.
    */
    public ArrayList findIndexedFrame(String datum, String className)
     throws PtoolsErrorException {
	return callFuncArray("multiple-value-list (find-indexed-frame ' "
			     + datum + " '" + className);
    }

    // Methods that call Pathway-Tools internal lisp (PTIL) functions

    /**
       Changes the organism.  Does not make a call to the select-organism
       lisp function in Pathway-Tools.  Does not make any calls to 
       Pathway-Tools functions.  The organism is prefixed to every query
       sent to the socket server.
       @param newOrganism the new organism
    */
    public void selectOrganism(String newOrganism)
    {
    	organism = newOrganism;
    	pathwayOntologyCache.clear();
    }

    /**
       Calls PTIL function, all-pathways
       @return an ArrayList containing all pathways in the current organism
    */
    public ArrayList allPathways()
     throws PtoolsErrorException {
    	
    	return callFuncArray("all-pathways");
    }
    
    public ArrayList oldAllOrgs()
    throws PtoolsErrorException {
    	return callFuncArray("all-orgs",false);
    }

    /**
       Calls PTIL function, all-orgs
       @return an ArrayList of orgkb-defstructs for all organisms currently 
       known to the Pathway Tools
    */
    public ArrayList<OrgStruct> allOrgs()
     throws PtoolsErrorException {
    	try{this.callFuncArray("select-organism :org-id 'meta",false);}catch(Exception ex){System.out.println("Corrected socket sync");}
    	
    	ArrayList<OrgStruct> orgs = new ArrayList<OrgStruct>();

	ArrayList<String> orgIDs = callFuncArray("mapcar #'kb-orgid (all-orgs nil)",false);
	for(String orgID : orgIDs)
	{
		orgs.add(OrgStruct.load(this, orgID));
	}

	return orgs;
    }
    
    /**
    @param list An ArrayList of Strings or ArrayLists.
    @return an OrgStruct that the ArrayList represents.
 */
    public static OrgStruct arrayListToOrgStruct(ArrayList list)
    {
    	//JavacycConnection.printListsNewLines(list,"", System.out);System.out.flush();
//    	ArrayList remove = new ArrayList();
//    	remove.add("#");
//    	remove.add("S");
//    	remove.add("P");
//    	list.removeAll(remove);
    	OrgStruct org = new OrgStruct();
//    	String key = "TYPE";
//    	String value = (String)list.get(0);
//    	org.put(key,value);
    	//System.out.println(key+"\t"+value);
//		for (int i = 1; i < list.size()-1; i+=2)
//		{
//			key = (String)list.get(i);
//			value = (String)list.get(i+1);
//			//System.out.println(key+"\t"+value);
//			org.put(key, value);
//		}
    	String lastKey = "";
		for (Object i : list)
		{
			String s = (String)i;
			if(s.startsWith(":"))
				lastKey = s;
			else if(lastKey.length()>0)
				org.put(lastKey,s);
		}
		return org;
    }

    /**
       Calls PTIL function, all-rxns
       (calls allRxns("all"))
       @return an ArrayList of reactions in the current organism
    */
    public ArrayList<String> allRxns()
     throws PtoolsErrorException {
    	//return callFuncArray("all-rxns");
    	return allRxns("all");
    }
    
    /**
     * @param type Can be one of: all = All reactions. 
     * smm = All reactions whose substrates are all small molecules, plus all reactions that are members of pathways of small- molecule metabolism. Note that smm will often return more reactions than does small-molecule because some pathways of small-molecule metabolism contain reactions involving macromolecules, e.g., ACP. 
     * small-molecule = All reactions whose substrates are all small molecules, as opposed to macromolecules. 
     * enzyme = All enzyme-catalyzed reactions (instances of classes EC-Reactions or Unclassified-Reactions). 
     * transport = All transport reactions. 
     * dna = All DNA Binding Reactions. 
     * @return IDs of all the reactions requested.
     */
    public ArrayList<String> allRxns(String type)
     throws PtoolsErrorException {
    	return callFuncArray("all-rxns :"+type);
    }

    /**
       Calls the PTIL function, genes-of-reaction
       @param rxn a reaction frame
       @return an ArrayList of all genes that code for enzymes that catalyze
       the reaction rxn
    */
    public ArrayList genesOfReaction(String rxn)
     throws PtoolsErrorException {
    	return callFuncArray("genes-of-reaction '" + rxn);
    }
    
    /**
    Calls the PTIL function, genes-regulating-gene
    */
    public ArrayList genesRegulatingGene(String g)
    throws PtoolsErrorException {
   	return callFuncArray("genes-regulating-gene '" + g);
   }
    
    /**
    Calls the PTIL function, genes-regulated-by-gene
    */
    public ArrayList genesRegulatedByGene(String g)
    throws PtoolsErrorException {
   	return callFuncArray("genes-regulated-by-gene '" + g);
   }

    /**
    Calls the PTIL function, genes-regulated-by-protein
    @param p a protein frame
    @return Return an ArrayList of all genes for which the given protein, or its modified form, acts as a regulator. 
    the reaction rxn
    */
    public ArrayList genesRegulatedByProtein(String p)
    throws PtoolsErrorException {
   	return callFuncArray("genes-regulated-by-protein '" + p);
   }
    
    /**
    Calls the PTIL function, regulators-of-gene-transcription
    @param p a protein frame
    @return Returns an ArrayList of proteins that are regulators of the given gene 
    the reaction rxn
    */
    public ArrayList regulatorsOfGeneTranscription(String g)
    throws PtoolsErrorException {
   	return callFuncArray("regulators-of-gene-transcription '" + g);
   }
    
    /**
       Calls the PTIL function, substrates-of-reaction
       @param rxn a reaction frame
       @return an ArrayList of all substrates of the reaction rxn
    */
    public ArrayList substratesOfReaction(String rxn)
     throws PtoolsErrorException {
	return callFuncArray("substrates-of-reaction '" + rxn);
    }

    /**
       Calls the PTIL function, products-of-reaction
       This is a hypothetical function that may not exist.
       @param rxn a reaction frame
       @return an ArrayList of all products of the reaction rxn
    */
    public ArrayList productsOfReaction(String rxn)
     throws PtoolsErrorException {
	return callFuncArray("products-of-reaction '" + rxn);
    }

    /**
       Calls the PTIL function, enzymes-of-reaction
       @param rxn a reaction frame
       @return an ArrayList of all enzymes that catalyze the reaction rxn
    */
    public ArrayList enzymesOfReaction(String rxn)
     throws PtoolsErrorException {
	return callFuncArray("enzymes-of-reaction '" + rxn);
    }
    
    public ArrayList specificFormsOfReaction(String rxn)
    throws PtoolsErrorException {
	return callFuncArray("specific-forms-of-rxn '" + rxn);
   }

    /**
       Calls the PTIL function, reaction-reactants-and-products
       @param rxn a reaction frame
       @param pwy a pathway frame
       @return an ArrayList containing the reactants of rxn and the products
       of rxn.  Some of the elements of the returned ArrayList may be 
       ArrayLists themselves.
    */
    public ArrayList reactionReactantsAndProducts(String rxn, String pwy)
     throws PtoolsErrorException {
	return callFuncArray("multiple-value-list (" +
			     "reaction-reactants-and-products '" + rxn +
			     " :pwy '" + pwy + ")");
//	return callFuncArray("multiple-value-list (" +
//    	"reaction-reactants-and-products '" + rxn + ")");
    }
    
    /**
    Calls the PTIL function, reaction-reactants-and-products using the L2R direction keyword.
    @param rxn a reaction frame
    @return an ArrayList containing the reactants of rxn and the products
    of rxn.  Some of the elements of the returned ArrayList may be 
    ArrayLists themselves.
 */
    public ArrayList reactionReactantsAndProducts(String rxn)
     throws PtoolsErrorException {
	return callFuncArray("multiple-value-list (" +
			     "reaction-reactants-and-products '" + rxn +
			     " :direction :L2R)");
//	return callFuncArray("multiple-value-list (" +
//    	"reaction-reactants-and-products '" + rxn + ")");
    }
    
    /**
    Calls the PTIL function, direct-activators
    @param frame the frame id
    @return an ArrayList containing the ids of the frames which directly activate frame.
     */
    public ArrayList directActivators(String frame)
     throws PtoolsErrorException {
    	return callFuncArray("direct-activators '" + frame);
    }
    
    /**
    Calls the PTIL function, direct-inhibitors
    @param frame the frame id
    @return an ArrayList containing the ids of the frames which directly inhibit frame.
     */
    public ArrayList directInhibitors(String frame)
     throws PtoolsErrorException {
    	return callFuncArray("direct-inhibitors '" + frame);
    }

    /**
       Calls the PTIL function, get-predecessors
       @param rxn a reaction frame
       @param pwy a pathway frame
       @return an ArrayList of all reactions that are direct predecessors
       of rxn in pwy
    */
    public ArrayList getPredecessors(String rxn, String pwy)
     throws PtoolsErrorException {
	return callFuncArray("get-predecessors '" + rxn + " '" + pwy);
    }

    /**
       Calls the PTIL function, get-successors
       @param rxn a reaction frame
       @param pwy a pathway frame
       @return an ArrayList of all reactions that are direct successors of
       rxn in pwy
    */
    public ArrayList getSuccessors(String rxn, String pwy)
     throws PtoolsErrorException {
	return callFuncArray("get-successors '" + rxn + " '" + pwy);
    }

    /**
       Calls the PTIL function, get-reaction-list
       @param pwy a pathway frame
       @return an ArrayList of the reactions in pwy
    */
    public ArrayList getReactionList(String pwy)
     throws PtoolsErrorException {
	return callFuncArray("get-reaction-list '" + pwy);
    }

    /**
       Calls the PTIL function, genes-of-pathway
       @param pwy a pathway frame
       @return an ArrayList of all genes that code for enzymes that catalyze a
       reaction in the pathway pwy
    */
    public ArrayList genesOfPathway(String pwy)
     throws PtoolsErrorException {
	return callFuncArray("genes-of-pathway '" + pwy);
    }

    /**
       Calls the PTIL function, enzymes-of-pathway
       @param pwy a pathway frame
       @return an ArrayList of all enzymes that catalyze a reaction in pwy
    */
    public ArrayList enzymesOfPathway(String pwy)
     throws PtoolsErrorException {
	return callFuncArray("enzymes-of-pathway '" + pwy);
    }

    /**
       Calls the PTIL function, compounds-of-pathway
       @param pwy a pathway frame
       @return an ArrayList of of all substrates of reactions of pwy, with 
       duplicates removed
    */
    public ArrayList compoundsOfPathway(String pwy)
     throws PtoolsErrorException {
	return callFuncArray("compounds-of-pathway '" + pwy);
    }

    /**
       Calls the PTIL function, substrates-of-pathway
       @param pwy a pathway frame
       @return an ArrayList of ArrayLists that contain the values returned
       by Pathway Tools
    */
    public ArrayList substratesOfPathway(String pwy)
     throws PtoolsErrorException {
	return callFuncArray("multiple-value-list (substrates-of-pathway '" 
			     + pwy + ")");
    }

    /**
       Calls the PTIL function, all-transcription-factors
       @return all transcription factors in the current organism
    */
    public ArrayList allTranscriptionFactors()
     throws PtoolsErrorException {
	return callFuncArray("all-transcription-factors");
    }
    


    /**
       Calls the PTIL function, transcription-factor?
       @param protein a protein
       @return true if protein is a trascription factor in the current
       organism
    */
    public boolean isTranscriptionFactor(String protein)
     throws PtoolsErrorException {
	return callFuncBool("transcription-factor? '" + protein);
    }

    /**
       Calls the PTIL function, all-cofactors
       @return an ArrayList of all cofactors used by enzymes in the current
       organism
    */
    public ArrayList allCofactors()
     throws PtoolsErrorException {
	return callFuncArray("all-cofactors");
    }

    /**
       Calls the PTIL function, all-modulators
       @return an ArrayList of all modulators that enzymes in the current 
       organism are sensitive to
    */
    public ArrayList allModulators()
     throws PtoolsErrorException {
	return callFuncArray("all-modulators");
    }

    /**
       Calls the PTIL function, monomers-of-protein
       @param protein a protein
       @return an ArrayList of monomers that are subunits of protein
    */
    public ArrayList monomersOfProtein(String protein)
     throws PtoolsErrorException {
	return callFuncArray("monomers-of-protein '" + protein);
    }

    /**
       Calls the PTIL function, components-of-protein
       @param protein a protein
       @return an ArrayList of components and their coefficients.  Some of 
       the elements in the returned ArrayList may be ArrayLists themselves.
    */
    public ArrayList componentsOfProtein(String protein)
     throws PtoolsErrorException {
	return callFuncArray("multiple-value-list (components-of-protein '" 
			     + protein + ")");
    }

    /**
       Calls the PTIL function, genes-of-protein
       @param protein a protein
       @return an ArrayList of genes that code for protein and all of the
       subunits of protein
    */
    public ArrayList genesOfProtein(String protein)
     throws PtoolsErrorException {
	return callFuncArray("genes-of-protein '" + protein);
    }

    /**
       Calls the PTIL function, reactions-of-enzyme
       @param enzyme an enzyme
       @return an ArrayList of all reactions that enzyme is linked to via
       enzymatic reactions
    */
    public ArrayList reactionsOfEnzyme(String enzyme)
     throws PtoolsErrorException {
	return callFuncArray("reactions-of-enzyme '" + enzyme);
    }

    /**
       Calls the PTIL function, enzyme?
       @param protein a protein
       @return true if the specified protein is an enzyme
    */
    public boolean isEnzyme(String protein)
     throws PtoolsErrorException {
	return callFuncBool("enzyme? '" + protein);
    }

    /**
       Calls the PTIL function, transporter?
       @param protein a protein
       @return true if the specified protein is a transporter
    */
    public boolean isTransporter(String protein)
     throws PtoolsErrorException {
	return callFuncBool("transporter? '" + protein);
    }

    /**
       Calls the PTIL function, containers-of
       @param protein a protein
       @return a list of all containers of protein, including itself
    */
    public ArrayList containersOf(String protein)
     throws PtoolsErrorException {
	return callFuncArray("containers-of '" + protein);
    }

    /**
       Calls the PTIL function, modified-forms
       @param protein a protein
       @return a list of modified forms of protein, including itself
    */
    public ArrayList modifiedForms(String protein)
     throws PtoolsErrorException {
	return callFuncArray("modified-forms '" + protein);
    }

    /**
       Calls the PTIL function, modified-containers
       @param protein a protein
       @return a list of all containers of a protein including itself and all
       modified forms of a protein
    */
    public ArrayList modifiedContainers(String protein)
     throws PtoolsErrorException {
	return callFuncArray("modified-containers '" + protein);
    }    

    /**
       Calls the PTIL function, top-containers
       @param protein a protein
       @return a list of all containers of protein that have no containers
    */
    public ArrayList topContainers(String protein)
     throws PtoolsErrorException {
	return callFuncArray("top-containers '" + protein);
    }

    /**
       Calls the PTIL function, reactions-of-protein
       @param protein a protein
       @return an ArrayList of all reactions catalyzed by protein or subuinits
       of protein
    */
    public ArrayList reactionsOfProtein(String protein)
     throws PtoolsErrorException {
	return callFuncArray("reactions-of-protein '" + protein);
    }

    /**
       Calls the PTIL function, regulon-of-protein
       @param protein a protein
       @return an ArrayList of transcription units regulated by any modified
       or unmodified form of protein
    */
    public ArrayList regulonOfProtein(String protein)
     throws PtoolsErrorException {
	return callFuncArray("regulon-of-protein '" + protein);
    }

    /**
       Calls the PTIL function, transcription-units-of-protein
       @param protein a protein
       @return an ArrayList of transcripton units activated or inhibited by
       the supplied protein or modified protein frame
    */
    public ArrayList transcriptionUnitsOfProtein(String protein)
     throws PtoolsErrorException {
	return callFuncArray("transcription-units-of-protein '" + protein);
    }

    /**
       Calls the PTIL function, regulator-proteins-of-transcription-unit
       @param tu a transcription unit
       @return an ArrayList of proteins that bind to binding sites within tu
    */
    public ArrayList regulatorProteinsOfTranscriptionUnit(String tu)
     throws PtoolsErrorException {
	return callFuncArray("regulator-proteins-of-transcription-unit '"
			     + tu);
    }

    /**
       Calls the PTIL function, enzymes-of-gene
       @param gene a gene
       @return an ArrayList of all enzymes coded for by gene
    */
    public ArrayList enzymesOfGene(String gene)
     throws PtoolsErrorException {
	return callFuncArray("enzymes-of-gene '" + gene);
    }

    /**
       Calls the PTIL function, all-products-of-gene
       @param gene a gene
       @return an ArrayList of all gene products of gene including those that
       are not enzymes
    */
    public ArrayList allProductsOfGene(String gene)
     throws PtoolsErrorException {
	return callFuncArray("all-products-of-gene '" + gene);
    }

    /**
       Calls the PTIL function, reactions-of-gene
       @param gene a gene
       @return an ArrayList of all reactions catalyzed by proteins that are 
       products of gene
    */
    public ArrayList reactionsOfGene(String gene)
     throws PtoolsErrorException {
	return callFuncArray("reactions-of-gene '" + gene);
    }

    /**
       Calls the PTIL function, pathways-of-gene
       @param gene a gene
       @return an ArrayList of all pathways containing reactions that are
       catalyzed by proteins that are products of gene
    */
    public ArrayList pathwaysOfGene(String gene)
     throws PtoolsErrorException {
	return callFuncArray("pathways-of-gene '" + gene);
    }

    /**
       Calls the PTIL function, chromosome-of-gene
       @param gene a gene
       @return a String containing the chromosome on which gene resides
    */
    public String chromosomeOfGene(String gene)
     throws PtoolsErrorException {
	return callFuncString("chromosome-of-gene '" + gene);
    }

    /**
       Calls the PTIL function, transcription-units-of-gene
       @param gene a gene
       @return an ArrayList of all transcription units that form the operon
       containing gene
    */
    public ArrayList geneTranscriptionUnits(String gene)
     throws PtoolsErrorException {
	return callFuncArray("gene-transcription-units '" + gene);
    }
    
    /**
    Calls the PTIL function, get-gene-sequence
    @param gene a gene
    @return the gene's nucleotide sequence
    */
    public String getGeneSequence(String gene)
     throws PtoolsErrorException {
	return callFuncString("get-gene-sequence '"+gene);
    }
    
    /**
    Calls the PTIL function, nucleotide->protein-sequence
    @param seq a nucleotide sequence
    @return the translated protein sequence
    */
    public String translate(String seq)
     throws PtoolsErrorException {
	return callFuncString("nucleotide->protein-sequence '"+seq);
    }

    /**
       Calls the PTIL function, transcription-unit-promoter
       @param tu a transcription unit
       @return a string containing the promoter of tu
    */
    public String transcriptionUnitPromoter(String tu)
     throws PtoolsErrorException {
	return callFuncString("transcription-unit-promoter '" + tu);
    }
    
    /**
    Calls the PTIL function, transcription-units-of-promoter
    @param pm a promoter
    @return an ArrayList containing the tus of pm
	*/
	public ArrayList transcriptionUnitsOfPromoter(String pm)
	 throws PtoolsErrorException {
	return callFuncArray("transcription-units-of-promoter '" + pm);
	}

    /**
       Calls the PTIL function, transcription-unit-genes
       @param tu a transcription unit
       @return an ArrayList of genes within the transcription unit
    */
    public ArrayList transcriptionUnitGenes(String tu)
     throws PtoolsErrorException {
    	return callFuncArray("transcription-unit-genes '" + tu);
    }

    /**
       Calls the PTIL function, transcription-unit-binding-sites
       @param tu a transcription unit
       @return an ArrayList of DNA binding sites within the transcriptional
       unit
    */
    public ArrayList transcriptionUnitBindingSites(String tu)
     throws PtoolsErrorException {
    	return callFuncArray("transcriptional-unit-binding-sites '" + tu);
    }

    /**
       Calls the PTIL function, transcription-unit-transcription-factors
       @param tu a transcription unit
       @return an ArrayList of the transcription factors that control the
       transcription unit tu
    */
    public ArrayList transcriptionUnitTranscriptionFactors(String tu)
     throws PtoolsErrorException {
    	return callFuncArray("transcription-unit-transcription-factors '"
			     + tu);
    }

    /**
       Calls the PTIL function, transcription-unit-terminators
       @param tu a transcription unit
       @return an ArrayList of the transcription terminators(s) within the
       transcription unit
    */
    public ArrayList transcriptionUnitTerminators(String tu)
     throws PtoolsErrorException {
    	return callFuncArray("transcription-unit-terminators '" + tu);
    }

    /**
       Calls the PTIL function, all-transported-chemicals
       @return an ArrayList of chemicals that are transported by the set of all
       defined transport reactions in current organism
    */
    public ArrayList allTransportedChemicals()
     throws PtoolsErrorException {
    	return callFuncArray("all-transported-chemicals");
    }

    /**
       Calls the PTIL function, reactions-of-compound
       @param cpd a chemical
       @return an ArrayList of the reactions in which cpd occurs as a 
       reactant or a product
    */
    public ArrayList reactionsOfCompound(String cpd)
     throws PtoolsErrorException {
    	return callFuncArray("reactions-of-compound '" + cpd);
    }

    /**
       Calls the PTIL function, full-enzyme-name
       @param enzyme an enzyme
       @return the full name of the enzyme
    */
    public String fullEnzymeName(String enzyme)
     throws PtoolsErrorException {
    	return callFuncString("full-enzyme-name '" + enzyme);
    }

    /**
       Calls the PTIL function, enzyme-activity-name
       @param enzyme an enzyme
       @return the enzyme activity name
    */
    public String enzymeActivityName(String enzyme)
     throws PtoolsErrorException {
    	return callFuncString("enzyme-activity-name '" + enzyme);
    }

    // Private methods for querying, retrieving results, calling functions,
    // and lisp list parsing
    
    public ArrayList callFuncArray(String func) throws PtoolsErrorException
    {
    	return callFuncArray(func,true);
    }

    /**
       Private method to call a Pathway Tools function that returns a list.
       @param func the Pathway Tools function to call
       @return an ArrayList representation of the lisp list returned by Pathway Tools
       @throws PtoolsErrorException if the API returns an error code (":error").  
       See the PathwayTools terminal windows for details when this happens.
       The PtoolsErrorException will contain a message giving the query that caused the error.
    */
    public ArrayList callFuncArray(String func,boolean wrap) throws PtoolsErrorException
    {
		makeSocket();
		String query = "";
		if(wrap)
		{
			query = wrapQuery(func);
		}
		else if(func.startsWith("***"))
		{
			query = func;
		}
		else
		{
			query = "("+func+")";
		}

		ArrayList results = null;
		try
		{
		    
		    Long start = System.currentTimeMillis();
		    sendQuery(query);
		    results = retrieveResultsArray();
		    Long stop = System.currentTimeMillis();
		    waits.add(stop-start);
		} 
		finally
		{
		    closeSocket();
		}
		if(results.size()==1)
		    if(results.get(0) instanceof String && ((String)results.get(0)).equals(":error"))
		    {
		    	throw new PtoolsErrorException("The server returned :error for the query "+query);
		    }
		return results;
    }
    
    private String callFuncString(String func) throws PtoolsErrorException
    {
	    return callFuncString(func,true);
    }

    private String wrapStringQuery(String func)
    {
	   return  "(with-organism (:org-id '" + organism + ") (object-name (" + func + ")))";

    }

    /**
       Private method to call a Pathway Tools function that returns a string.
       @param func the Pathway Tools function to call
       @return string returned by Pathway Tools function call
       @throws PtoolsErrorException if the API returns an error code (":error").  
       See the PathwayTools terminal windows for details when this happens.
       The PtoolsErrorException will contain a message giving the query that caused the error.
    */
    public String callFuncString(String func, boolean wrap) throws PtoolsErrorException
    {
		makeSocket();
		String results = "";
		String query = wrap ? wrapStringQuery(func) : "("+func+")";
		try {
		    Long start = System.currentTimeMillis();
		    sendQuery(query);
		    results = retrieveResultsString();
		    Long stop = System.currentTimeMillis();
		    waits.add(stop-start);
		} 
		finally {
		    closeSocket();
		}
	    if(results!=null && results.equals(":error"))
	    	throw new PtoolsErrorException("The server returned :error for the query "+query);
	    return results;

    }

    /**
       Private method to call a Pathway Tools function that returns a boolean.
       @param func the Pathway Tools function to call
       @return true if the result of the function call is true
    */
    private boolean callFuncBool(String func) throws PtoolsErrorException
    {
		String result = callFuncString(func);
		if (result==null || result.equals("NIL"))
		{
		    return false;
		}
		else
		{
		    return true;
		}
    }

    /**
       Private method that wraps a query.
       @param func the function call to wrap in a query
       @return a query
    */
    private String wrapQuery(String func)
    {
	return "(with-organism (:org-id '" + organism +
	    ") (mapcar #'object-name (" + func + ")))";
    }

    /**
       Private method to send a query to Pathway Tools.
       @param query the query to send to Pathway Tools
    */
    private void sendQuery(String query)
    {
    	//System.out.println("javacyc sending query: "+query);
	if(query==null)
	{
		Exception ex = new Exception("Null query");
		ex.printStackTrace();
	}
	else
		out.println(query.replace(";", "\\;"));
    		
    }

    /**
       Private method to retrieve a string result.
       @return the string result or null if the result is "NIL" from the PGDB
    */
    private String retrieveResultsString()
    {
		try
		{
		    ArrayList<String> results = new ArrayList();
		    String readStr = "";

	    	readStr = in.readLine();
		    while (readStr != null)
			{
			    // DEBUG ONLY
			    //System.out.println(readStr);
	
			    results.add(readStr);
			    readStr = in.readLine();
			}
		    
		    if(results.size()==0) return null;
		    
		    String retStr = (String)results.get(0);
	
		    // DEBUG
		    //System.out.println("0th element: " + (String)results.get(0));
	
		    // If retStr is surrounded by quotation marks, remove them
//		    if ((retStr.startsWith("\"")) && (retStr.endsWith("\"")) && retStr.length()>1)
//			{
//			    int endIndex = retStr.length() - 1;
//			    return retStr.substring(1, endIndex);
//			}
//		    else
//			{
//			    return retStr;
//			}
		    return retStr;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null; // if an IOException has occured
    }

    /**
       Private method to retrieve an ArrayList result.
       This method is like retrieve_results in perlcyc, not perlcyc's 
       retrieve_results_array subroutine.
       @return the ArrayList result
    */
    private ArrayList retrieveResultsArray()
    {
    	//I have no idea why, but things break if I don't do this...
//    	try {
//			in.mark(1024);
//			String s = in.readLine();
//			System.out.println("RESPONSE: "+s);
//			in.reset();
//		} catch (IOException e) {
//			//e.printStackTrace();
//		}
    	
    	
		LinkedList tokens = tokenize();
//		System.out.println(tokens.size());
//		for(Object token : tokens)
//		{
//			System.out.println("a token: "+(String)token);
//		}
		ArrayList rst =  parseExpr(tokens);
		return rst;
    }
    
    /**
    private method to tokenize a lisp expression.
    @return an LinkedList containing the tokens of the lisp expression
    */
    private LinkedList tokenize()
    {
    	
		LinkedList tokens = new LinkedList();
		try
		{

		    StreamTokenizer tokenizer;
		    tokenizer = new StreamTokenizer(in);
		    
		    
		    tokenizer.resetSyntax();
		    tokenizer.eolIsSignificant(false);
		    tokenizer.wordChars('a', 'z');
		    tokenizer.wordChars('A', 'Z');
		    tokenizer.wordChars('&', '&');
		    tokenizer.wordChars('<', '<');
		    tokenizer.wordChars('>', '>');
		    tokenizer.wordChars(';', ';');
		    tokenizer.wordChars('\'', '\'');
		    tokenizer.wordChars('\u00A0', '\u00FF');
		    tokenizer.wordChars('0', '9');
		    tokenizer.wordChars('.', '.');
		    tokenizer.wordChars('+', '+');
		    tokenizer.wordChars('?', '?');
		    tokenizer.wordChars(':', ':');
		    tokenizer.wordChars('_', '_');
		    tokenizer.wordChars('-', '-');
		    tokenizer.wordChars('/', '/');
		    tokenizer.wordChars('\'', '\'');
		    //tokenizer.wordChars('\\', '\\');
		    tokenizer.whitespaceChars(' ', ' ');
		    tokenizer.whitespaceChars('\n', '\n'); 
		    //tokenizer.quoteChar('"');
		    int lastType = 0;
		    int type=StreamTokenizer.TT_EOF;
			try {
				type = tokenizer.nextToken();
			} catch (Exception e) {
				return tokens;
			}
		    while (type != StreamTokenizer.TT_EOF)
		    {
		    	//System.out.println(lastType+" "+(char)lastType+" "+type+" "+(char)type);System.out.flush();
				if (type == StreamTokenizer.TT_NUMBER)
				{
				    tokens.add(Double.toString(tokenizer.nval));
				}
				else if (type == StreamTokenizer.TT_WORD)
				{
				    tokens.add(tokenizer.sval);
				}
				else if (type == '(')
				{
				    tokens.add("(");
				}
				else if (type == '"' && lastType!='\\')
				{
				    //tokens.add(tokenizer.sval);
					StringBuffer buffer = new StringBuffer();
				    buffer.append("\"");
				    lastType = type;
				    type = tokenizer.nextToken();
				    while(!(type == '"' && lastType!='\\') && type != StreamTokenizer.TT_EOF)
				    {
				    	if (type == StreamTokenizer.TT_WORD)
				    	{
				    		if(lastType==StreamTokenizer.TT_WORD || lastType==StreamTokenizer.TT_NUMBER)
				    			buffer.append(' ');
				    		buffer.append(tokenizer.sval);
				    	}
				    	else if (type == StreamTokenizer.TT_NUMBER)
				    	{
				    		if(lastType==StreamTokenizer.TT_WORD || lastType==StreamTokenizer.TT_NUMBER)
				    			buffer.append(' ');
				    		buffer.append(tokenizer.nval);
				    	}
				    	else if (type == '|' || type == '[' || type == ']')
				    	{
				    		buffer.append((char)type);
				    	}
				    	lastType = type;
				    	type = tokenizer.nextToken();
				    }
				    //System.out.println(lastType+" "+(char)lastType+" "+(char)type);System.out.flush();
				    buffer.append("\"");
				    tokens.add(buffer.toString());
				}
				else if (type == ')')
				{
				    tokens.add(")");
				}
				else if (type == '|' && tokenizer.ttype != '"')
				{
				    StringBuffer buffer = new StringBuffer();
				    buffer.append("|");
				    int typeLastTok = tokenizer.ttype;
				    lastType = type;
				    type = tokenizer.nextToken();
				    
				    while (type != '|' && type != StreamTokenizer.TT_EOF)
				    {
						if (type == StreamTokenizer.TT_WORD)
						{
						    if ((typeLastTok == StreamTokenizer.TT_WORD) ||
							(typeLastTok == StreamTokenizer.TT_NUMBER) ||
							(typeLastTok == '"'))
						    {
						    	buffer.append(" ");
						    }
						    buffer.append(tokenizer.sval);
						}
						else if (type == StreamTokenizer.TT_NUMBER)
						{
						    if ((typeLastTok == StreamTokenizer.TT_WORD) ||
							(typeLastTok == StreamTokenizer.TT_NUMBER) ||
							(typeLastTok == '"'))
						    {
						    	buffer.append(" ");
						    }
						    buffer.append(Double.toString(tokenizer.nval));
						}
						else if (type == '"')
						{
						    if ((typeLastTok == StreamTokenizer.TT_WORD) ||
							(typeLastTok == StreamTokenizer.TT_NUMBER) ||
							(typeLastTok == '"'))
						    {
						    	buffer.append(" ");
						    }
						    buffer.append("\"");
						    buffer.append(tokenizer.sval);
						    buffer.append("\"");
						}
						else
						{
						    buffer.append((char)tokenizer.ttype);
						}
						typeLastTok = type;
						lastType = type;
						type = tokenizer.nextToken();
				    }
				    buffer.append("|");
				    tokens.add(buffer.toString());
				}
				else
				{
				    Character c = new Character((char)tokenizer.ttype);
				    tokens.add(c.toString());
				}
				lastType = type;
				type = tokenizer.nextToken();
		    }   	
		}
		catch (IOException e)
		{
		    e.printStackTrace();
		    //makeSocket();
		}
		
		return tokens;
    }

    /**
       Public parser method.
       @param tokens a LinkedList containing the tokens of a lisp expression
       @return an ArrayList representation of the lisp list
    */
    public ArrayList parseExpr(LinkedList tokens)
    {

    	ArrayList listElements = new ArrayList();
    	if(tokens.size()==0) return listElements;
    	String first = (String)tokens.getFirst();
    	if (first.equals("("))
		{
			
		    tokens.removeFirst();
		    
		    //System.out.println(tokens.getFirst());
		    while (tokens.size()>0 && !((String)tokens.getFirst()).equals(")"))
		    {
				String temp = (String)tokens.getFirst();
				if (temp.equals("("))
				{
				    listElements.add(parseExpr(tokens)); // add an inner list
				}
				else if (temp.equals("NIL"))
				{
				    //listElements.add("NIL"); 
				    tokens.removeFirst();
				}
				else
				{
				    listElements.add(temp);
				    tokens.removeFirst();
				}
				//if(tokens.size()==0) break;
		    }
		    if(tokens.size()>0) 
		    	tokens.removeFirst(); // remove a )
		    //return listElements;
		}
    	else if (first.equals(":error")) {
    		listElements.add(tokens.removeFirst());
    	}
		else
		{
			//System.out.println(tokens.size());
//			String rst = "";
//			for(Object token : tokens)
//			{
//				if(!(((String)token).equals("NIL")))
//					rst += (String)token + " ";
//			}
//			listElements.add(rst);
		}
    	return listElements;
    }
    
    /**
    Recursively print everything in an ArrayList.  Should contain (ArrayLists of) strings or Frames.
    Prints items within "[]" all on one line.
    @param list An ArrayList of ArrayLists, Strings, or Frames to print
 */
    public static void printLists(ArrayList list)
    {
		for (int i = 0; i < list.size(); i++)
		{
			System.out.print("[");
		    Object obj = list.get(i);
		    if (obj instanceof String)
		    {
				String str = (String)obj;
				System.out.print(str);
		    }
		    else if (obj instanceof ArrayList)
		    {
				
				ArrayList aList = (ArrayList)obj;
				printLists(aList);
		    }
		    else if (obj instanceof Frame)
		    {
		    	Frame f = (Frame)obj;
		    	System.out.print(f.getLocalID());
		    }
		    else
		    {
		    	System.out.println("WARNING THIS SHOULD NOT HAPPEN!");
		    }
		    System.out.print("]");
		}
    }
    
    /**
    Recursively print everything in an ArrayList.  Should contain (ArrayLists of) strings or Frames.
    Prints items on new lines, keeping track of recursion depth and printing tabs accordingly.
    @param list An ArrayList of ArrayLists, Strings, or Frames to print
    @param tabs The number of tabs to begin printing (call it with 0, it will be used in recursion)
    @param o The PrintStream to print to.  Could be for a file or just System.out
 */
    public static void printListsNewLines(ArrayList list,String tabs,PrintStream o)
    {
		for (int i = 0; i < list.size(); i++)
		{
		    Object obj = list.get(i);
		    if (obj instanceof String)
		    {
				String str = (String)obj;
				o.println(tabs+str);
		    }
		    else if (obj instanceof ArrayList)
		    {
				
				ArrayList aList = (ArrayList)obj;
				printListsNewLines(aList,tabs+"\t",o);
				
		    }
		    else if (obj instanceof Frame)
		    {
		    	Frame f = (Frame)obj;
		    	System.out.print(f.getLocalID());
		    }
		    else
		    {
		    	System.out.println("WARNING THIS SHOULD NOT HAPPEN!");
		    }
		}
    }
    
    /**
    Recursively count everything in an ArrayList.
    @param list An ArrayList of anything.
    @return the number of items in the ArrayList (recursively counts ArrayLists within the ArrayList)
 */
    public static int countLists(ArrayList list)
    {
    	int rst = 0;
		for (int i = 0; i < list.size(); i++)
		{
		    Object obj = list.get(i);
		    if (obj instanceof String)
		    {
				rst++;
		    }
		    else if (obj instanceof ArrayList)
		    {
				ArrayList aList = (ArrayList)obj;
				rst += countLists(aList);
		    }
		    else
		    {
		    	System.out.println("WARNING THIS SHOULD NOT HAPPEN! (countLists)");
		    }
		}
		return rst;
    }
    

    
    /**
    Prints all the slots for a frame that are currently stored in the PGDB.
    @param item the frame id to print.
 */
	public void printAllSlots(String item)
	 throws PtoolsErrorException {
		ArrayList slots = this.getFrameSlots(item);
		for(int k=0;k<slots.size();k++)
		{
			String slotName = (String)slots.get(k);
			ArrayList slotValues = this.getSlotValues(item,slotName);
			System.out.println(slotName+" ("+JavacycConnection.countLists(slotValues)+")");
			for(Object slotValue : slotValues)
			{
				if(slotValue instanceof String)
				{
					System.out.println("\t"+(String)slotValue);
					ArrayList<String> annots = this.getAllAnnotLabels(item, slotName, (String)slotValue);
					for(String annotName : annots)
					{
						System.out.println("\t\t--"+annotName+"\t"+this.getValueAnnot(item, slotName, (String)slotValue, annotName));
					}
				}
				else
				{
					System.out.println("\t"+this.ArrayList2LispList(slotValues));
					break;
				}
			}
//			String slotValue = this.ArrayList2LispList(this.getSlotValues(item,slotName));
//			System.out.println(slotName+"\t"+slotValue);	
//			ArrayList<String> annots = this.getAllAnnots(item, slotName, slotValue);
//			for(String annotName : annots)
//			{
//				System.out.println("\t--"+annotName+"\t"+this.getValueAnnot(item, slotName, slotValue, annotName));
//			}
		}
	}
	
    /**
    Creates a Network object representing the entire PGDB that the JavacycConnection is currently connected to.
    @return the Network object representing the entire PGDB that the JavacycConnection is currently connected to.
    */
	public Network getNetwork()
	throws PtoolsErrorException {
		return getNetwork(false);
	}
	
	/**
    Creates a Network object representing the entire PGDB that the JavacycConnection is currently connected to and if specified, 
    does not connect Promoters to their respective activators/inhibitors-- it stops at the Promoter level.
    @param stop if true, stops pathway traversal at the Promoter level, leavig Promoters as "leaves" or "sinks"
    @return the Network object representing the entire PGDB that the JavacycConnection is currently connected to
    */
	public Network getNetwork(boolean stop)
	 throws PtoolsErrorException {
		String stopped = stop ? "_stopped" : "";
		Network net = new Network(this.organism+stopped);
		int count = 0;
		ArrayList<Pathway> allPwys = Pathway.all(this);
		for(Frame pwy : allPwys)
		{
			count ++;
			System.out.println(pwy.getLocalID()+" "+count+"/"+allPwys.size());
			this.buildNetwork((Pathway)pwy, net,stop);
		}
		return net;
	}
	
	/**
    Build onto an existing Network object the network representing a specific pathway.
    @param pwy The Pathway to add to the Network
    @param net The Network to add the Pathway to.  (can be a new, empty Network).
    @param stop if true, stops pathway traversal at the Promoter level, leavig Promoters as "leaves" or "sinks"
    */
	public void buildNetwork(Pathway pwy,Network net,boolean stop)
	throws PtoolsErrorException {
		for(Reaction rxn : pwy.getReactions())
		{
			//System.out.println(rxn.getLocalID());
			//rxn.print();
			net.addNode(rxn);
			//System.out.println("\t reactants");
			for(Frame r : rxn.getReactants())
			{
				if(r==null) continue;
				//System.out.println(r.getLocalID());
				backtracePathways(r,net,stop);
				String coef = r.annotations.get("COEFFICIENT");
				if(coef==null) coef="1";
				String comp = r.annotations.get("COMPARTMENT");
				if(comp==null) comp="not assigned";
				else comp = Frame.load(this,comp).getCommonName();
				net.addEdge(r, rxn, Network.REACTANT+"\t"+coef+"\t"+comp);
			}
			//System.out.println("\t products");
			for(Frame p : rxn.getProducts())
			{
				//System.out.println(p.getLocalID());
				if(p==null) continue;
				backtracePathways(p,net,stop);
				String coef = p.annotations.get("COEFFICIENT");
				if(coef==null) coef="1";
				String comp = p.annotations.get("COMPARTMENT");
				if(comp==null) comp="not assigned";
				else comp = Frame.load(this,comp).getCommonName();
				net.addEdge(rxn, p, Network.PRODUCT+"\t"+coef+"\t"+comp);
			}
			if(rxn instanceof EnzymeReaction)
			{
				//System.out.println("\t enzymes");
				HashSet<String> enzDone = new HashSet<String>();
				for(Frame f : ((EnzymeReaction)rxn).getCatalysis())
				{
					Catalysis c = (Catalysis)f;
					//System.out.println(c.getLocalID());
					//net.addEdge(c,rxn,Network.CATALYSIS);
					Protein p = c.getEnzyme();
					enzDone.add(p.getLocalID());
					//System.out.println(p.getLocalID());
					backtracePathways(p,net,stop);
					//net.addEdge(p,c,Network.CATALYSIS);
					net.addEdge(p,rxn,Network.CATALYSIS);
					for(Frame f2 : c.getCofactors())
					{
						backtracePathways(f2,net,stop);
						//net.addEdge(f2,c,Network.COFACTOR);
						net.addEdge(f2,rxn,Network.COFACTOR);
					}
					for(Frame f2 : c.getProstheticGroups())
					{
						backtracePathways(f2,net,stop);
						//net.addEdge(f2,c,Network.PROSTHETICGROUP);
						net.addEdge(f2,rxn,Network.PROSTHETICGROUP);
					}
//					for(Frame f2 : c.getActivators())
//					{
//						if(f2==null) continue;
//						backtracePathways(f2,net,stop);
//						net.addEdge(f2,c,Network.CATALYSISACTIVATION);
//					}
//					for(Frame f2 : c.getInhibitors())
//					{
//						if(f2==null) continue;
//						backtracePathways(f2,net,stop);
//						net.addEdge(f2,c,Network.CATALYSISINHIBITION);
//					}
				}
//				for(Frame f : ((EnzymeReaction)rxn).getEnzymes())
//				{
//					if(!enzDone.contains(f.getLocalID()));
//					{
//						backtracePathways(f,net,stop);
//						net.addEdge(f,rxn,Network.CATALYSIS);
//					}
//				}
	
			}
		}
	}

    /**
    Adds a specific Frame along with its direct connections to a growing Network object.
    Traverses from Complexes -> Monomers -> Genes -> Transcription Units -> Promoters -> Affectors(Complexes or Monomers)
    Recursively calls itself.
    @param f The frame to add to the Network
    @param net The Network to add the Frame and its connections to.
    @param stop if true, stops pathway traversal at the Promoter level, leaving Promoters as "leaves" or "sinks"
    */
	public void backtracePathways(Frame f,Network net,boolean stop)
	throws PtoolsErrorException {
		//System.out.println("\t"+"\t"+f.getLocalID()+"\t"+f.getClass().getName());
		Exception e = new Exception();
		//e.printStackTrace();
		if(net.containsNode(f))
		{
			//System.out.println("already done");
			return;
		}
		net.addNode(f);
		if(f instanceof Compound) return;
		if(f instanceof Complex)
		{
			//System.out.println("HELLO");
			String formationName = f.getLocalID()+"-formation";
			Reaction formation = new Reaction(this,formationName);
			formation.setCommonName(formationName);
			net.addEdge(formation,f,Network.COMPLEX_FORMATION+"\t1\t-");
			//net.addNode(formation);
			for(Frame c : ((Complex)f).getComponents())
			{
				backtracePathways(c,net,stop);
				String coef = c.annotations.get("COEFFICIENT");
				if(coef==null) coef="1";
				net.addEdge(c,formation,Network.COMPLEX_FORMATION+"\t"+coef+"\t-");
			}
		}
		else if(f instanceof Monomer)
		{
			for(Gene g : ((Monomer)f).getGenes())
			{
				backtracePathways(g,net,stop);
				net.addEdge(g,f,Network.TRANSLATION);
			}
		}
		else if(f instanceof Protein)
		{
			return;
		}
		else if(f instanceof Gene)
		{
			for(TranscriptionUnit tu : ((Gene)f).getTranscriptionUnits())
			{
				//backtracePathways(tu,net,stop);
				//net.addEdge(tu,f,"TRANSCRIPTION");
				Promoter pm = tu.getPromoter();
				if(pm != null)
				{
					for(Frame a : pm.getActivators())
					{
						if(!stop) backtracePathways(a,net,stop);
						net.addEdge(a,f,Network.PROMOTERACTIVATION);
					}
					for(Frame i : pm.getInhibitors())
					{
						if(!stop) backtracePathways(i,net,stop);
						net.addEdge(i,f,Network.PROMOTERINHIBITION);
					}
					Frame sigma = pm.getSigmaFactor();
					if(sigma != null)
					{
						if(!stop) backtracePathways(sigma,net,stop);
						net.addEdge(sigma,f,Network.SIGMAFACTOR);
					}
				}
			}
		}
//		else if(f instanceof TranscriptionUnit)
//		{
//			Promoter pm = ((TranscriptionUnit)f).getPromoter();
//			if(pm != null)
//			{
//				backtracePathways(pm,net,stop);
//				net.addEdge(pm,f,Network.PROMOTER);
//			}
//
//		}
//		else if(f instanceof Promoter)
//		{
//			for(Frame a : ((Promoter)f).getActivators())
//			{
//				if(!stop) backtracePathways(a,net,stop);
//				net.addEdge(a,f,Network.PROMOTERACTIVATION);
//			}
//			for(Frame i : ((Promoter)f).getInhibitors())
//			{
//				if(!stop) backtracePathways(i,net,stop);
//				net.addEdge(i,f,Network.PROMOTERINHIBITION);
//			}
//			Frame sigma = ((Promoter)f).getSigmaFactor();
//			if(sigma != null)
//			{
//				if(!stop) backtracePathways(sigma,net,stop);
//				net.addEdge(sigma,f,Network.SIGMAFACTOR);
//			}
//		}
		else
		{
//			this.printLists(this.getInstanceAllTypes(f.getLocalID()));
//			System.err.println("UNACCOUNTED FOR:");
//			f.print();
		}
	}
	
    /**
    Creates a Network of Pathways; Pathways are nodes and they are connected if they share a common Reaction.
    @param multipleEdges if true, creates an edge for each common reaction between two pathways, 
    else creates one edges between two pathways sharing at least one reaction that is weighted by the number of common reactions.
 	@return the Network representing the network of pathways
    */
	public Network getPathwayNetwork(boolean multipleEdges)
	throws PtoolsErrorException {
		String me = multipleEdges ? "_multipe_edges" : "_weighted_edges";
		Network net = new Network(this.organism+me);
		ArrayList<Pathway> pwys = Pathway.all(this);
		HashSet<String> done = new HashSet<String>();
		for(int i = 0; i<pwys.size(); i++)
		{
			Pathway a = (Pathway)pwys.get(i);
			System.out.println(i+"/"+pwys.size());
			for(Frame b : pwys)
			{
				if(b.getLocalID().equals(a.getLocalID()) || done.contains(b.getLocalID())) continue;
				ArrayList<Reaction> commonRxns = a.commonReactions((Pathway)b);
				if(commonRxns.size()==0) continue;
				if(multipleEdges)
				{
					for(Reaction r : commonRxns)
					{
						net.addEdge(a,b,r.getCommonName());
					}
				}
				else
				{
					net.addEdge(a,b,""+commonRxns.size());
				}
			}
			done.add(a.getLocalID());
		}
		return net;
	}
	
    /**
    Prints slots and class information for a frame id.
    @param id the id of the frame to print
    */
	public void showMe(String id)
	 throws PtoolsErrorException {
		System.out.println("\n"+id);
		try
		{
			this.printAllSlots(id);
		}
		catch(PtoolsErrorException e)
		{
			e.printStackTrace();
		}
		if(!this.getFrameType(id).toUpperCase().equals(":CLASS"))
		{
			System.out.println("~~THE FOLLOWING ARE NOT SLOTS~~\n~GFP SUPERCLASSES: ");
			for(Object t : this.getInstanceAllTypes(id))
			{
				System.out.println("\t"+(String)t);
			}
			System.out.println("~DIRECT GFP SUPERCLASSES: ");
			for(Object t : this.getInstanceDirectTypes(id))
			{
				System.out.println("\t"+(String)t);
			}
		}
	}

	/**
    Builds a Network representing the entire class hierarchy of the connected PGDB as a tab-seperated file representing a network.
    @param includeInstances if true, also prints instances of classes, else only prints class frames.
	return The Network of classes in the PGDB.
    */
	public Network getClassHierarchy(boolean includeInstances)
	 throws PtoolsErrorException {
		Network net = new Network(this.organism+"_class_heirarchy");
		ArrayList<String> ids = this.getClassAllInstances("OCELOT-GFP::FRAMES");
		int count = 0;
		HashSet<String> classes = new HashSet<String>();
		
		if(!includeInstances)
		{
			for(String id : ids)
			{
				ArrayList<String> directClasses = this.getInstanceDirectTypes(id);
				for(String c : directClasses)
				{
					classes.add(c);
				}
			}
			for(String c : classes)
			{
				count ++;
				Frame f = Frame.load(this,c);
				System.out.println(f.getLocalID()+" ("+count+"/"+classes.size()+")");
				buildClassHierarchy(f,net);
			}
		}
		else
		{
			for(String id : ids)
			{
				count ++;
				Frame f = Frame.load(this,id);
				System.out.println(f.getLocalID()+" ("+count+"/"+ids.size()+")");
				buildClassHierarchy(f,net);
			}
		}
		return net;
	}
	
    /**
    Builds a Network representing the entire class hierarchy of the connected PGDB as a tab-seperated file representing a network.
    Used by getClassHierarchy.
    Calls self recursively.
    @param f the frame to begin building the Network from (moving 'backward' through the metabolic network)
	@param net The Network to build on.
    */
	public void buildClassHierarchy(Frame f,Network net)
	throws PtoolsErrorException {
		if(net.containsNode(f)) return;
		for(Frame sup : f.getDirectSuperClasses())
		{
			buildClassHierarchy(sup,net);
			net.addEdge(sup,f,"");
		}
	}
    
    /**
    Closes the connection to the PGDB.
    Prints query wait time statistics to standard output.
    */
	public void close()
	{
		//System.out.println("closing connection");
		closeSocket();
		if(waits.size()>0)
		{
			System.out.println("\nWait time ("+waits.size()+" queries):");
			System.out.println("Min: "+min(waits)+"ms");
			System.out.println("Avg: "+1.0*sum(waits)/waits.size()+"ms");
			System.out.println("Max: "+max(waits)+"ms");
			System.out.println("Var: "+var(waits)+"ms");
			System.out.println("SD: "+Math.sqrt(var(waits))+"ms");
		}
		System.out.println(cacheHits+" cache hits");
	}
	
	private long min(ArrayList<Long> a)
	{
		if(a.size()==0)return 0L;
		Long rst = a.get(0);
		for(Long v : a)
		{
			if(v<rst) rst = v;
		}
		return rst;
	}
	
	private long max(ArrayList<Long> a)
	{
		if(a.size()==0)return 0L;
		Long rst = a.get(0);
		for(Long v : a)
		{
			if(v>rst) rst = v;
		}
		return rst;
	}
	
	private long sum(ArrayList<Long> a)
	{
		if(a.size()==0)return 0L;
		Long rst = 0L;
		for(Long v : a)
		{
			rst += v;
		}
		return rst;
	}
	
	private double var(ArrayList<Long> a)
	{
		if(a.size()==0)return 0L;
		double mean = 1.0*sum(a)/a.size();
		double rst = 0.0;
		for(Long v : a)
		{
			rst += (1.0*v-mean)*(1.0*v-mean);
		}
		return rst/(a.size()-1);
	}
	
	/**
    Makes sure the connection is closed before disposal.
    */
	protected void finalize() throws Throwable
	{
		closeSocket();
		super.finalize();
	}
	
	/**
	 * Obtain a map of hierarchically indented pathway class names to pathway class frame ids.
	 * @param here if true, query all the data to the local machine and build the map, else ask the remote server to do it.
	 * @return a map of hierarchically indented pathway class names to pathway class frame ids
	 * @throws PtoolsErrorException
	 */
	public LinkedHashMap<String,String> getPathwayOntology(boolean here) throws PtoolsErrorException
	{
		return this.getPathwayOntology(here, "+");
	}

	public LinkedHashMap<String,String> getPathwayOntology(boolean here, String tab) throws PtoolsErrorException
	{
		if(this.pathwayOntologyCache.size()>0) return this.pathwayOntologyCache;
		LinkedHashMap<String,String> classMap = new LinkedHashMap<String,String>();
		if(!here)
		{
			ArrayList al = this.callFuncArray("***PO:"+organism, false);
			for(Object o : al)
			{
				//classMap.put((String)o, (String)o);
				String[] pair = ((String)o).split(":");
				classMap.put(pair[0].replace("\"",""),pair[1].replace("\"", ""));
			}
		}
		else
		{
			putChildren((OntologyTerm)Frame.load(this,Pathway.GFPtype),classMap,"",tab);
		}
		this.pathwayOntologyCache = classMap;
		return classMap;
	}
	
	private void putChildren(OntologyTerm ont,LinkedHashMap<String,String> classMap,String tabs,String tab) throws PtoolsErrorException
	{
		ArrayList<OntologyTerm> children = ont.getChildren();
		if(children.size()>0)
		{
			classMap.put(tabs+ont.getCommonName(),ont.getLocalID());
			TreeMap<String,OntologyTerm> sortMap = new TreeMap<String,OntologyTerm>(String.CASE_INSENSITIVE_ORDER);
			for(OntologyTerm child : ont.getChildren())
				sortMap.put(child.getCommonName(),child);
			for(String name : sortMap.keySet())
			{
				putChildren(sortMap.get(name),classMap,tabs+tab,tab);
			}
		}
	}
	
	/**
	* Search for a string in a certain frame type.
	* @param search The string to search for
	* @param type The GFP type of objects to search.  Use JavaCyc class' GFPtype static fields, ie Compound.GFPtype to search all Compounds, 
	* or Gene.GFPtype to search all Genes.
	* @return An ArrayList of Frames containing the Frames matching the search string
	*/
	public ArrayList<Frame> search(String search,String type)
	throws PtoolsErrorException
	{
		//return search(search,type,false);
		if(type==null) type = "NIL";
		else type = "'"+type;
		ArrayList rst = this.callFuncArray("substring-search "+type+" \""+search+"\" :insert-html-tags? NIL");
		//this.printLists(rst);
		ArrayList<Frame> ret = new ArrayList<Frame>();
		for(Object obj : rst)
		{
			ArrayList hit = (ArrayList)obj;
			for(Object obj2 : hit)
			{
				String s = (String)obj2;
				if(!s.startsWith("\"") && !s.equals("."))
				{
					ret.add(Frame.load(this, s));
				}
			}
		}
		return ret;
	}
	
	
    /**
    Search for any Frame in the PGDB using the GFP function, get-frame-labeled.
    Note: Matches WHOLE WORD.  If you are searching for a part of a string, use search
    @param label The string to search for in label-designated slots
    @return an ArrayList of Frames which had matching labels
    @see #search(String, String)
 	*/
    public ArrayList<Frame> searchLabels(String label)
     throws PtoolsErrorException {
    	ArrayList<Frame> rst = new ArrayList<Frame>();
    	for(Object obj : this.getFrameLabeled(label))
    	{
    		rst.add(Frame.load(this,(String)obj));
    	}
    	return rst;
    }
    
    /**
    Calls the GFP function, pathways-of-gene
 	*/
    public ArrayList getPathwaysOfGene(String geneID)
     throws PtoolsErrorException {
    	return callFuncArray("pathways-of-gene '"+geneID);
    }
    
    /**
     * Write a tab-delimited file with all reactions and their participants listed
     * @param o the PrintStream to write to.
     * @param prefix the value of a first column of output.  (used for compatability with my scripts)
     * @throws PtoolsErrorException
     */
    public void writeReactionNeighbors(PrintStream o,String prefix) throws PtoolsErrorException
    {
    	for(Reaction r : Reaction.all(this))
    	{
    		o.print(prefix+r.getLocalID()+"--"+r.getCommonName());
    		for(Frame f : r.getReactants())
    			o.print("\t"+f.getLocalID());
    		for(Frame f : r.getProducts())
    			o.print("\t"+f.getLocalID());
    		if(r instanceof EnzymeReaction)
    		{
    			for(Frame f : ((EnzymeReaction)r).getEnzymes())
    				o.print("\t"+f.getLocalID());
    		}
    		o.println("");
    	}
    }
    
    public void writeReactions(PrintStream o,String prefix) throws PtoolsErrorException
    {
    	for(Reaction r : Reaction.all(this))
    	{
    		o.println(prefix+r.getLocalID()+"--"+r.getCommonName()+"\t"+r.getLocalID());
    	}
    }

}
