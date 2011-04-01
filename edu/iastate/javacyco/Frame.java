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


import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;

/**
This class implements in Java the Frame object on which Pathway Geneome Databases and Knowledge Bases (Karp, et al) are based 
(PGDB/KBs are object-oriented databases implemented in Lisp).
Note that a Java programmer can use any approach to accessing a PGDB along with spectrum of abstract-ness and discrete-ness.
To be most discrete, you could access the PGDB only using Frame objects (this class)-- Everything is a Frame and can be treated as such.
On the other end of the spectrum, you can use the subclasses under this class, where many functions of the JavacycConnection are abstracted 
out to simple class methods.
Of course, you can also work at any point in-between these extremes, sometimes using Frames, and sometimes using the specific subclasses as needed.
@author John Van Hemert
*/
public class Frame
{
	
	/**
	All Frames maintain a pointer to the connection from which they were loaded.
	*/
	protected JavacycConnection conn;
	
	/**
	All Frames maintain a local ID.  When loaded, this is the String ID as the Frame is stored in the PGDB, ie CPD-123 or PHOS-ENXRXN.
	*/
	protected String ID;
	
	/**
	Each Frame maintains a local cache of its slots and slot values as a HashMap.  Slots are only loaded as needed because most are never even used during session.
	The COMMON_NAME slot is always loaded immediately, though.
	*/
	protected HashMap<String,ArrayList> slots;
	
	/**
	All Frame classes have a static String mapping it to its respective class name in the PGDB.  Subclasses of Frame override this static value.
	Value for Frame is OCELOT-GFP::FRAMES.
	*/
	public static String GFPtype = "OCELOT-GFP::FRAMES";
	
	/**
	Since BioCyc dbs are object-oriented, you have to ask the db for "annotations" (like stoich. coefficients), GIVEN where they appear.  
	For example, when a monomer appears in the "Component" list for a multimer, 
	you have to ask the db what is the coefficient of that monomer's entry in the multimer's object.    
	This API always loads any annotation into entity objects, DEPENDING ON where you load them from.  
	If I ask the db to just load monomer A explicitly, it will have no annotations.  
	If I ask the db to load all the monomers that make up multimer B (let's say A is a subunit of B), 
	then monomer A will be loaded with its coefficient as it appears in the multimer B as an "annotation" called "coefficient" 
	(the number 2 if B is a dimer of A).  If monomer A somehow also works alone as a catalyst in a reaction and I ask the api 
	to load that reaction's enzyme, monomer A will be loaded with the "coefficient" annotation of 1.  
	So, all entities (metabolites and genes too) are loaded with annotations (this is also how you get compartment info) 
	and the annotations depend on where you load the entities from.
	DIFFERENT INSTANCES OF AN ENTITY (FRAME) CAN HAVE DIFFERENT VALUES FOR THE ANNOTATIONS HASHMAP, DEPENDING ON HOW THE FRAMES WERE LOADED.
	*/
	public HashMap<String,String> annotations;
	
	public HashMap<String,HashMap<String,HashMap<String,ArrayList>>> slotValueAnnotations;
	
	/**
	When the method getPathways() is called, the result is also stored in the variable pathways, allowing the results to persist along with the
	frame. This is done to prevent the need for repeated calls to getPathways(), which tends to be very computationally expensive.  
	*/
	public ArrayList<Frame> pathways = new ArrayList<Frame>();
	
	/**
	Create a new, empty Frame.
	This does NOT load a frame from the PGDB.  Use Frame.load for that.
	@param c the JavacycConnection this Frame must maintain.
	@param id the ID for the new Frame.
	*/
	public Frame(JavacycConnection c,String id)
	{
		conn = c;
		ID = id;
		slots = new HashMap<String,ArrayList>();
		annotations = new HashMap<String,String>();
		slotValueAnnotations = new HashMap<String,HashMap<String,HashMap<String,ArrayList>>>();
	}
	
	/**
	 * Get this Frame's connection.
	 * @return the JavacycConnection object this Frame loads from / commits to.
	 */
	public JavacycConnection getConnection()
	{
		return conn;
	}
	
	/**
	Loads all Frames of this class' type.  This method is override by subclasses so that
	the return Frame objects are of the correct type.
	Uses the GFPtype static String variable for Frame.
	@return an ArrayList of all Frames in the PGDB of this BioCyc class type.
	*/
	public static ArrayList<Frame> allFrames(JavacycConnection c)
	throws PtoolsErrorException 
	{
		return c.getAllGFPInstances(GFPtype);
	}
	
	/**
	Retrieves from the PGDB the value for a specific annotation for a slot and slot value for this Frame.
	@param slotName The name of the slot in the Frame
	@param slotValue The value that appears in the slot slotName in this Frame
	@param annotName The name of the annotation field whose value is desired, ie "COEFFICIENT" or "COMPARTMENT"
	@return The value of the annotation annotName where slotValue falls in slotName for this Frame
	*/
	public String getAnnotation(String slotName,String slotValue,String annotName)
	throws PtoolsErrorException {
		return conn.getValueAnnot(ID, slotName, slotValue, annotName);
	}
	
	protected void loadAnnotations(Frame parent,String parentSlot) throws PtoolsErrorException
	{
		for(String label : conn.getAllAnnotLabels(parent.getLocalID(), parentSlot, ID))
		{
			this.annotations.put(label, JavacycConnection.ArrayList2LispList(conn.getValueAnnots(parent.getLocalID(), parentSlot, ID, label)));
		}
	}
	
	/**
	Retrieves from the PGDB the names of all the annotations for a slot and slot value for this Frame.
	@param slotName The name of the slot in the Frame
	@param slotValue The value that appears in the slot slotName in this Frame
	@return the names/labels of all the annotations for a slot and slot value for this Frame
	*/
	public ArrayList<String> getAllAnnotLabels(String slotName,String slotValue)
	throws PtoolsErrorException {
		return conn.getAllAnnotLabels(ID,slotName,slotValue);
	}
	
	/**
	Get the common name (like the PGDB slot "COMMON-NAME") for this Frame.
	Some Frames have null values for the COMMON-NAME slot.  In these cases, the ID is returned.  Removes enclosing quotes.
	@return The common name of this Frame as it is defined in the PGDB.  If none exists, returns this Frame's ID.
	*/
	public String getCommonName()
	throws PtoolsErrorException {
		String name = this.getSlotValue("COMMON-NAME");
		if(name == null || name.length()==0)
		{
			return ID;
		}
		if(name.startsWith("\"")) name = name.substring(1);
		if(name.endsWith("\"")) name = name.substring(0,name.length()-1);
		return name;
	}
	
	/**
	Set this Frame's common name slot.
	WHEN CHANGING SLOTS, CHANGES ARE NOT REFLECTED IN THE PGDB UNTIL YOU USE THE COMMIT() METHOD.
	@see #commit()
	@param n the new common name for this Frame
	*/
	public void setCommonName(String n)
	{
		this.putSlotValue("COMMON-NAME","\""+n+"\"");
	}
	
	/**
	Print to standard output all of this Frame's slots with slot values and annotations.
	Also print JavaCyc and PGDB class information.
	*/
	public void print()
	throws PtoolsErrorException {
		conn.showMe(ID);
		System.out.println("~CLASSIFIED AS\n\t"+this.getClass().getName());
	}
	
	/**
	Get this Frame's ID as it is stored locally (without looking it up in the PGDB).
	@return this Frame's locally stored ID.
	*/
	public String getLocalID()
	{
		return ID;
	}
	
	/**
	Set this Frame's locally stored ID, WITHOUT updating it in the PGDB (use commit() to commit changes).
	@param newID the new ID to be stored in this local Frame object
	@return the old ID
	*/
	public String setLocalID(String newID)
	{
		String oldID = ID;
		ID = newID;
		return oldID;
	}
	
	/**
	Check if this Frame exists in the PGDB/KB connected by its JavacycConnection.
	Simply asks the PGDB if a Frame with this Frame's local ID exists.
	@return true if there is a Frame with this Frame's local ID in the connected PGDB, else returns false.
	*/
	public boolean inKB()
	throws PtoolsErrorException {
		return conn.frameExists(ID);
	}
	
	/**
	Gets a HashMap representing this Frame's slots and their values.
	Locally stored slots take precedent over PGDB-stored slots.
	Loads all slots from the PGDB if they don't already exists in this local Frame Object.
	@return A HashMap representing this Frame's slots in the PGDB, where already locally cached slots (that may have been changed)
	supercede PGDB stored slots.
	*/
	public HashMap<String,ArrayList> getSlots() throws PtoolsErrorException
	{
		if(this.inKB())
		{
			ArrayList<String> slotsInKb = conn.getFrameSlots(ID);
			for(String slot : slotsInKb)
			{
				if(!slots.keySet().contains(slot))
				{
					this.loadSlot(slot);
				}
			}
		}
		return slots;
	}
	
	/**
	Get all synonyms for this Frame object.
	Simply calls getSlotValues("SYNONYMS")
	@return all synonyms for this Frame object.
	*/
	public ArrayList<String> getSynonyms()
	throws PtoolsErrorException {
		return this.getSlotValues("SYNONYMS");
	}
	
	/**
	Get the Comment entered for this Frame object.
	Simply calls getSlotValue("COMMENT");
	@return The comment entered for this Frame object.
	*/
	public String getComment()
	throws PtoolsErrorException {
		return this.getSlotValue("COMMENT");
	}
	
	/**
	Get all names for this Frame object from the NAMES slot.
	Simply calls getSlotValues("NAMES")
	@return all names for this Frame object.
	*/
	public ArrayList<String> getNames()
	throws PtoolsErrorException {
		return this.getSlotValues("NAMES");
	}
	
	/**
	Check if this Frame has a slot by a specific name.
	@param slot the name of the slot to check for
	@return true if this Frame has a slot by the name specified, else returns false
	*/
	public boolean hasSlot(String slot)
	throws PtoolsErrorException {
		return slots.containsKey(slot) || conn.slotExists(ID,slot);
	}
	
	/**
	Check if this Frame has a value for a slot by a specific name.
	@param slot the name of the slot to check for
	@return true if this Frame has a value in the slot by the name specified, else returns false
	*/
	public boolean slotIsNil(String slot)
	throws PtoolsErrorException {
		return this.hasSlot(slot) && conn.slotIsNil(ID,slot);
	}
	
	/**
	Get all values for this Frame's slot, slot.  Slots can contain lists of objects, including nested lists.
	@param slot the name of the slot whose values are to be retrieved.
	@return an ArrayList representing all values in the slot for this Frame.  Could contain nested ArrayLists.
	returns an empty ArrayList if there are no values in the slot for this Frame.
	@throws PtoolsErrorException if the slot does not exist.
	*/
	public ArrayList getSlotValues(String slot) throws PtoolsErrorException
	{
		if(!this.inKB()) return new ArrayList();
		if(!this.hasSlot(slot)) return new ArrayList();//throw new PtoolsErrorException("Slot "+slot+" for Frame "+ID+" does not exist");
		if(slots.get(slot)==null || !slots.containsKey(slot))
			this.loadSlot(slot);
		if(!slots.containsKey(slot))
			return new ArrayList();
		return slots.get(slot);
	}
	
	/**
	Get a single slot value for a slot you know has only one value for this Frame.
	@param slot the name of the slot to retrieve
	@return the value of the slot, slot for this Frame.  return null if there is no value for the slot in this Frame.
	@throws PtoolsErrorException if the slot does not exist.
	*/
	public String getSlotValue(String slot)
	throws PtoolsErrorException {
//		if(!this.hasSlot(slot)) throw new PtoolsErrorException("Slot "+slot+" for Frame "+ID+" does not exist");
//		if(slots.get(slot)==null || !slots.containsKey(slot))
//		{
//			ArrayList valAL = new ArrayList();
//			String val = conn.getSlotValue(ID,slot);
//			if(val.equals("NIL")) val = null;
//			valAL.add(val);
//			slots.put(slot,valAL);
//			
//		}
//		if(!slots.containsKey(slot))
//			return null;
//		return (String)slots.get(slot).get(0);
		
		ArrayList rst = this.getSlotValues(slot);
		if(rst.size()==0) return null;
		else return (String)rst.get(0);
	}
	
	/**
	Add a value to a growing list of values for a slot in this Frame
	Loads the slot from the PGDB if it does not exist locally.
	Does not add duplicate values.
	@param slot The name of the slot whose values are to be appended
	@param value The value to add to the list of values for the slot in this Frame
	*/
	public void addSlotValue(String slot, String value)
	throws PtoolsErrorException {
		if(!slots.containsKey(slot) || slots.get(slot) == null)
			this.loadSlot(slot);
		if(!slots.get(slot).contains(value))
			slots.get(slot).add(value);
	}
	
	/**
	Put an ArrayList of slot values in a slot for this Frame, stored locally.
	Overwrites any values already stored locally.
	@param slot The name of the slot whose values are to be overwritten
	@param values The values to place under the slot in this Frame.
	*/
	public void putSlotValues(String slot, ArrayList values)
	{
		slots.put(slot,values);
	}
	
	/**
	Put an ArrayList containing a single slot value in a slot for this Frame, stored locally.
	@param slot The name of the slot whose values are to be overwritten
	@param value The value to place under the slot in this Frame.  Creates an ArrayList containing this value and calls putSlotValues for it.
	*/
	public void putSlotValue(String slot, String value)
	{
		ArrayList al = new ArrayList();
		al.add(value);
		this.putSlotValues(slot,al);
	}
	
	/**
	Create a new Frame of a specific type both in the PGDB and locally.
	Allows the PGDB to automatically generate an ID string.
	@param c the connection to the PGDB
	@param type the PGDB class type to which the new Frame will belong.  Use any PGDB class name, or the specific class names
	of the JavaCyc classes, stored in their respected static fields, GFPtype. ie, Gene g = (Gene)Frame.create(myconnection,Gene.GFPtype);
	@return the new Frame created.  If the type fits a JavaCyc class, then you can cast the new Frame to that class.
	@throws PtoolsErrorException if you try to create a Frame of the GFPtype "|Proteins|" (javacyc.Protein.GFPtype) because 
	these do not have PGDB "template slots" and cannot be automatically created in a PGDB.  Create a Monomer or Complex (JavaCyc subclasses of Protein) instead.
	*/
	public static Frame create(JavacycConnection c,String type)
	throws PtoolsErrorException {
		return Frame.create(c, type, null);
	}
	
	/**
	Create a new Frame of a specific type both in the PGDB and locally.
	@param c the connection to the PGDB
	@param type the PGDB class type to which the new Frame will belong.  Use any PGDB class name, or the specific class names
	of the JavaCyc classes, stored in their respected static fields, GFPtype. ie, Gene g = (Gene)Frame.create(myconnection,Gene.GFPtype);
	@param id the ID to use for the new Frame
	@return the new Frame created.  If the type fits a JavaCyc class, then you can cast the new Frame to that class.
	@throws PtoolsErrorException if you try to create a Frame of the GFPtype "|Proteins|" (javacyc.Protein.GFPtype) because 
	these do not have PGDB "template slots" and cannot be automatically created in a PGDB.  Create a Monomer or Complex (JavaCyc subclasses of Protein) instead.
	*/
	public static Frame create(JavacycConnection c,String type,String id)
	throws PtoolsErrorException {
		if(type.equals("|Proteins|"))
			throw new PtoolsErrorException(type+" do not have template slots.  Try a different class.");
		System.out.println("CREATING A "+type);
		if(id==null)
		{
			return Frame.load(c,c.createInstanceWGeneratedId(type));
		}
		else
		{
			if(c.frameExists(id))
				throw new PtoolsErrorException("Frame "+id+" already exists");
			return Frame.load(c,c.createInstance(id,type));
		}
	}
	
	/**
	All JavaCyc Frame subclasses maintain a static variable, GFPtype, which maps the JavaCyc class to a PGDB/KB class type.
	Frame subclasses override this static class field.
	This method allows instances to access their respective GFPtype.
	@return the GFPtype string for this Frames JavaCyc class.
	*/
	public String getGFPtype()
	{
		//System.out.println(this.getClass().getFields()[0].get(this));
		try {
			return (String)this.getClass().getFields()[0].get(this);
		} catch (Exception e) {
			e.printStackTrace();
			return GFPtype;
		}
	}
	
	/**
	Overwrite all PGDB slots with the locally stored slots for this Frame.
	If this frame is not in the PGDB, it must have been created with a constructor and not a load method, so first create the Frame in the PGDB.
	*/
	public void commit()
	throws PtoolsErrorException {
		if(!conn.frameExists(ID))
			//conn.createInstanceWGeneratedId(GFPtype);
			Frame.create(conn,this.getGFPtype(),ID);
		Iterator<String> iter = slots.keySet().iterator();
		String key,valueList;
		ArrayList local,inDB;
		while(iter.hasNext())
		{
			key = iter.next();
			if(slots.get(key) != null)
			{
				inDB = conn.getSlotValues(ID,key);
				local = slots.get(key);
				if(!JavacycConnection.arrayListsEqual(inDB,local))
				{
					//System.out.println("committing "+key+": "+JavacycConnection.ArrayList2LispList(local));
					if(local.size()==1 && !(local.get(0) instanceof ArrayList))
					{
						conn.putSlotValue(ID,key,(String)local.get(0));
					}
					else
					{
						conn.putSlotValues(ID,key,JavacycConnection.ArrayList2LispList(local));
					}
					//this.commitAnnotations(key);
				}
			}
		}
	}
	
	public boolean isClassFrame() throws PtoolsErrorException
	{
		return conn.getFrameType(ID).toUpperCase().equals(":CLASS");
	}
	
//	protected void commitAnnotations(String slot) throws PtoolsErrorException
//	{
//		for(Object vo : slots.get(slot))
//		{
//			if(!(vo instanceof String)) continue;
//			String slotValue = (String)vo;
//			try
//			{
//				if(this.slotValueAnnotations.containsKey(slot) && this.slotValueAnnotations.get(slot).containsKey(slotValue))
//					for(String label : this.slotValueAnnotations.get(slot).get(slotValue).keySet())
//					{
//						conn.putAnnotations(ID, slot, slotValue, label, this.slotValueAnnotations.get(slot).get(slotValue).get(label));
//					}
//			}
//			catch(NullPointerException ex)
//			{
//				System.out.println("Problem with annotation for slot:"+slot+" value:"+slotValue+" for "+getLocalID()+":"+getCommonName());
//			}
//		}
//	}
	
	/**
	Change this Frame's ID both locally and in the PGDB
	@param newName the new ID/name for this Frame
	@return the old ID of this Frame
	*/
	public String renameInKB(String newName)
	throws PtoolsErrorException {
		if(!conn.frameExists(ID))
			throw new PtoolsErrorException("Frame "+ID+" not in KB.");
		String oldID = ID;
		conn.renameFrame(ID,newName);
		ID = newName;
		return ID;
	}
	
	/**
	Delete this Frame from the PGDB/KB
	The local object is untouched.
	*/
	public void deleteFromKB()
	throws PtoolsErrorException {
		conn.deleteFrame(ID);
	}
	
	/**
	Load a Frame from the PGDB into a JavaCyc object.
	@param c the connection to use to retrieve the Frame.
	@param id the ID of the Frame to retrieve
	@return a local instance of the Frame.  Instances are instantiated as their appropriate JavaCyc class according to
	the PGDB Frame's superclasses.  ie, if the PGDB stores the Frame with the specified id somewhere under "|Monomers|",
	then the return JavaCyc instances is of the class, javacyc.Monomer.
	All JavaCyc subclasses of Frame inherit this method and they all return an instance of Frame, but you can check 
	which subclass it is using the instanceof operator (or just know what you're getting when you load an id) and cast
	the returned Frame to the appropriate JavaCyc class.  ie, Gene g = (Gene)Gene.load(myconnection,"EG10650");
	If the static Frame field 'cache' is set to true, this returns the Frame in c's cache field (HashMap) keyed by the String id.
	*/
	public static Frame load(JavacycConnection c,String id)
	throws PtoolsErrorException {
		if (id == null || id.length() == 0) return null;
		if(id.startsWith("(")) id = id.replace("(","").replace(")","").replace(" ","");
		if(c.isCaching() && c.cache.containsKey(id))
		{
			c.incrementCacheHits();
			return c.cache.get(id);
		}
		else
		{
			Frame f = Frame.classifyFrame(c,id);
			if(c.isCaching())
			{
				c.cache.put(id,f);
			}
			return f;
		}
		
	}
	
	/**
	Load a set of Frames from the PGDB into an ArrayList of JavaCyc objects.
	@param c the connection to use to retrieve the Frame.
	@param ids an ArrayList of strings containing the ids of the Frames to retrieve
	@return an ArrayList of local instance of the Frames.  Instances are instantiated as their appropriate JavaCyc class according to
	the PGDB Frame's superclasses.  ie, if the PGDB stores the Frame with the specified id somewhere under "|Monomers|",
	then the return JavaCyc instances is of the class, javacyc.Monomer.
	All JavaCyc subclasses of Frame inherit this method and they all return an instance of Frame, but you can check 
	which subclass it is using the instanceof operator (or just know what you're getting when you load an id) and cast
	the returned Frame to the appropriate JavaCyc class.  ie, Gene g = (Gene)Gene.load(myconnection,"EG10650");
	*/
	public static ArrayList<Frame> load(JavacycConnection c,ArrayList<String> ids)
	throws PtoolsErrorException {
		ArrayList<Frame> rst = new ArrayList<Frame>();
		for(String id : ids)
		{
			try
			{
				rst.add(Frame.load(c,id));
			}
			catch(PtoolsErrorException e)
			{
				continue;
			}
			
		}
		return rst;
	}	
	
	/**
	Create a local instance of the appropriate JavaCyc class, according to the PGDB superclasses of the specified id.
	If the id cannot be mapped to a JavaCyc Frame subclass, it is loaded as a javacyc.Frame.
	@param c the connection to use to retrieve the information about the PGDB Frame with ID, id
	@param id the ID of the PGDB Frame to lookup and classify.
	@return an instance of javacyc.Frame or any one of its subclasses, depending on what superclass the Frame has in the PGDB.
	For example, if the Frame with the id has "|All-Genes|" or "|Polynucleotides|" somewhere in its superclasses in the PGDB, then
	a new javacyc.Gene is returned.
	*/
	public static Frame classifyFrame(JavacycConnection c,String id)
	throws PtoolsErrorException {	
		if(id==null) return null;
		
		if(!c.frameExists(id))
		{
			//throw new PtoolsErrorException("Frame "+id+" not in KB!");
			System.err.println("Frame "+id+" not in KB!");
			return new Frame(c,id);
		}

		
		HashSet<String> classes = new HashSet<String>(c.getInstanceAllTypes(id));
		
		if(c.getFrameType(id).equals(":CLASS"))
		{
			if(classes.contains(GOTerm.GFPtype))
			{
				if(classes.contains(GOCellularComponent.GFPtype))
					return new GOCellularComponent(c,id);
				if(classes.contains(GOBiologicalProcess.GFPtype))
					return new GOBiologicalProcess(c,id);
				if(classes.contains(GOMolecularFunction.GFPtype))
					return new GOMolecularFunction(c,id);
				return new GOTerm(c,id);
			}
			else if(classes.contains(CellComponent.GFPtype))
				return new CellComponent(c,id);
			else
				return new OntologyTerm(c,id);
		}
		else if(classes.contains("|Polypeptides|"))
			return new Monomer(c,id);
		else if(classes.contains("|Protein-Complexes|") || classes.contains("|Protein-Small-Molecule-Complexes|"))
			return new Complex(c,id);
		else if(classes.contains("|Proteins|"))
			return new Monomer(c,id);
		else if(classes.contains("|Transcription-Units|"))
			return new TranscriptionUnit(c,id);
		else if(classes.contains("|Enzymatic-Reactions|"))
			return new Catalysis(c,id);
		else if(classes.contains("|Promoters|"))
			return new Promoter(c,id);
		else if(classes.contains("|Chromosomes|"))
			return new Chromosome(c,id);
		else if(classes.contains("|All-Genes|") || classes.contains("|Polynucleotides|"))
			return new Gene(c,id);
		else if(classes.contains("|DNA-Reactions|"))
			return new DNAReaction(c,id);
		else if(classes.contains("|Transport-Reactions|"))
			return new TransportReaction(c,id);
		else if(classes.contains("|Small-Molecule-Reactions|"))
			return new SmallMoleculeReaction(c,id);
		else if(classes.contains("|Reactions|"))
		{
			if(!c.slotIsNil(id,"ENZYMATIC-REACTION"))
				return new EnzymeReaction(c,id);
			else
			{
				//System.out.println("");
				//c.printLists(c.getInstanceAllTypes(id));
				return new EnzymeReaction(c,id);
				//return new Reaction(c,id);
			}
		}
		else if(classes.contains("|Pathways|"))
			return new Pathway(c,id);
		else if(classes.contains("|Regulation|"))
			return new Regulation(c,id);
		else if(classes.contains("|Compounds|") || classes.contains("|Subatomic-Particles|"))
			return new Compound(c,id);
		else if(classes.contains("|Chromosomes|"))
			return new Chromosome(c,id);
		else if(classes.contains("|Organisms|"))
			return new Organism(c,id);
		else
		{
			return new Frame(c,id);
		}
	}
	
	/**
	Load all of this Frame's slot names into the local slots HashMap, with null values to be retrieved as needed.
	*/
	protected void initializeSlots()
	throws PtoolsErrorException {
		ArrayList<String> cycSlots = conn.getFrameSlots(ID);
		for(int k=0;k<cycSlots.size();k++)
		{
			String slotName = cycSlots.get(k);
			if(slotName.equals("NIL")) continue;
			//ArrayList slotValues = conn.getSlotValues(ID,slotName);
			//putSlotValue(slotName,slotValues);
			putSlotValues(slotName,(ArrayList)null);
		}
	}
	
	/**
	Load a single slot and its values for this Frame
	@param slotName the name of the slot whose values are to be loaded
	*/
	protected void loadSlot(String slotName)
	throws PtoolsErrorException {
		ArrayList values = conn.getSlotValues(ID,slotName);
		putSlotValues(slotName,values);
		HashMap<String,HashMap<String,ArrayList>> valueAnnots = new HashMap<String,HashMap<String,ArrayList>>();
		for(Object vo : values)
		{
			if(!(vo instanceof String)) continue;
			String value = (String)vo;
			HashMap<String,ArrayList> annots = new HashMap<String,ArrayList>();
			for(String label : conn.getAllAnnotLabels(ID, slotName, value))
			{
				ArrayList annotValues = conn.getValueAnnots(ID, slotName, value, label);
				annots.put(label, annotValues);
			}
			if(annots.size()>0)
				valueAnnots.put(value, annots);
		}
		if(valueAnnots.size()>0)
			this.slotValueAnnotations.put(slotName,valueAnnots);
	}
	
	protected void putLocalSlotValueAnnotations(String slotName,String value,String label,String annotValue)
	{
		if(!this.slotValueAnnotations.containsKey(slotName))
			this.slotValueAnnotations.put(slotName, new HashMap<String,HashMap<String,ArrayList>>());
		if(!this.slotValueAnnotations.get(slotName).containsKey(value))
			this.slotValueAnnotations.get(slotName).put(value,new HashMap<String,ArrayList>());
		this.slotValueAnnotations.get(slotName).get(value).put(label, new ArrayList(Arrays.asList(new String[] {annotValue})));
	}
	

	/**
	Check if the PGDB Frame with the specified id has the specified class name somewhere it its PGDB superclasses.
	@param c the connection to use
	@param id the id of the PGDB frame to lookup
	@param className the name of the PGDB class to check membership for the Frame
	*/
	public static Boolean isGFPClass(JavacycConnection c,String id,String className)
	throws PtoolsErrorException {
		if(!className.startsWith("|"))
			className = "|"+className;
		if(!className.endsWith("|"))
			className += "|";
		return c.getInstanceAllTypes(id).contains(className);
	}
	
	/**
	Instance method for the static version.
	@param className the name of the PGDB class to check membership for the Frame
	*/
	public Boolean isGFPClass(String className)
	throws PtoolsErrorException {
		return Frame.isGFPClass(conn,ID, className);
	}
	
	/**
	Gets the direct superclasses of this Frame as it is stored in the connected PGDB
	@return An ArrayList of Frames, which are PGDB/KB class Frames representing the direct superclass as this Frame is stored in the connected PGDB
	*/
	public ArrayList<Frame> getDirectSuperClasses()
	throws PtoolsErrorException {
		return Frame.load(conn,conn.getInstanceDirectTypes(ID));
	}
	
	/**
	Creates a Network object representing the PGDB/KB class hierarchy for superclasses of this Frame.
	@return a Network object representing the PGDB/KB class hierarchy for superclasses of this Frame.
	*/
	public Network getClassHierarchy()
	throws PtoolsErrorException {
		Network net = new Network(ID+"_class_hierarchy");
		Frame.buildClassHierarchy(this, net);
		return net;
	}
	
	/**
	Adds to a Network object representing the PGDB/KB class hierarchy for superclasses of the specified Frame.
	Calls self recursively as it builds the Network upward toward the root class (THINGS).
	@param f The Frame whose sup
	*/
	public static void buildClassHierarchy(Frame f,Network net)
	throws PtoolsErrorException {
		for(Frame parent : f.getDirectSuperClasses())
		{
			net.addEdge(parent,f,"");
			Frame.buildClassHierarchy(parent, net);
		}
	}
	
	/**
	This method will get all pathways in which this object participates.
	@return all pathways for this Frame object.
	*/
	public ArrayList<Frame> getPathways()
	throws PtoolsErrorException {
		System.out.println("getPathways called on a Frame of a subclass of Frame for which the function was not overwritten: " + this.getClass().getName() + " :: " + this.ID);
		pathways = new ArrayList<Frame>();
		return pathways;
	}

	/**
	A shallow test of equality. Test the frame ID of two frames for equality. Does not compare frame slots.
	@return true if both frames have the same ID. 
	*/
	@Override public boolean equals(Object aThat) {
		//Based on example at http://www.javapractices.com/topic/TopicAction.do?Id=17
		
	    //Check for self-comparison
	    if (this == aThat) return true;

	    //Check for similar class
	    if (!(aThat instanceof Frame)) return false;
	    
	    //Cast to native type
	    Frame that = (Frame)aThat;

	    //Compare frame IDs
	    return this.getLocalID().equals(that.getLocalID());
	  }

	@Override public int hashCode() {
		return this.ID.hashCode();
	  }
}
