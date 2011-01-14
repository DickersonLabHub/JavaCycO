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


import java.util.ArrayList;
import java.util.HashSet;

/**
The Reaction class is a general super class of the more specific Reaction types.
@author John Van Hemert
*/
public class Reaction extends Frame 
{
	/**
	Reactions
	*/
	public static String GFPtype = "|Reactions|";
	
	/**
	all
	*/
	public static final String ALL = "all";
	
	/**
	enzyme
	*/
	public static final String ENZYME = "enzyme";
	
	/**
	small-molecule
	*/
	public static final String SMALLMOLECULE = "small-molecule";
	
	/**
	transport
	*/
	public static final String TRANSPORT = "transport";
	
	/**
	dna
	*/
	public static final String DNA = "dna";
	
	/**
	{ENZYME,SMALLMOLECULE,TRANSPORT,DNA}
	*/
	public static final String[] ALLTYPES = {ENZYME,SMALLMOLECULE,TRANSPORT,DNA};
	
	public Reaction(JavacycConnection c,String id)
	{
		super(c,id);
	}

	/**
	Get the Enzyme Commission number (n.n.n.n) associated with this Reaction.
	@return the Enzyme Commission number (n.n.n.n) associated with this Reaction.
	*/
	public String getEC()
	throws PtoolsErrorException {
		return this.getSlotValue("EC-NUMBER");
	}
	
	/**
	Set the Enzyme Commission number (n.n.n.n) associated with this Reaction.
	@param n the new Enzyme Commission number (n.n.n.n) associated with this Reaction.
	*/
	public void setEC(String n)
	{
		this.putSlotValue("EC-NUMBER",n);
	}
	
	
	/**
	Get all Reactions in the PGDB.
	@return an ArrayList of all Reactions in the PGDB.
	*/
	static public ArrayList<Reaction> all(JavacycConnection c)
	 throws PtoolsErrorException {
		
		//return Reaction.load(c, c.allRxns("all"));
		
//		ArrayList<Reaction> rst = new ArrayList<Reaction>();
//		HashSet<String> rxns = new HashSet<String>();
//		
//		String[] types = {"enzyme","small-molecule","transport","dna"};
//		for(String type : types)
//		{
//			ArrayList<String> IDs = c.allRxns(type);
//			for(String ID : IDs)
//			{
//				if(!rxns.contains(ID))
//					rst.add((EnzymeReaction)EnzymeReaction.load(c,ID));
//			}
//		}
//		return rst;
		
		ArrayList<String> IDs = c.allRxns(ALL);
		ArrayList<Reaction> rst = new ArrayList<Reaction>();
		for(String ID : IDs)
		{
			rst.add((Reaction)load(c,ID));
		}
		return rst;
	}
	
	/**
	Get all Reactions in the PGDB of a specific Reaction type.  Use the static Reaction fields ENZYME,SMALLMOLECULE,TRANSPORT,DNA.
	@param c the JavacycConnection to use.
	@param type the type of the Reactions to retrieve.  Use Reaction.-one of ENZYME,SMALLMOLECULE,TRANSPORT,DNA.
	@return an ArrayList of all Reactions in the PGDB of the given type.
	*/
	static public ArrayList<Reaction> all(JavacycConnection c,String type)
	 throws PtoolsErrorException {
		
		//return Reaction.load(c, c.allRxns(type));
		
		ArrayList<String> IDs = c.allRxns(type);
		ArrayList<Reaction> rst = new ArrayList<Reaction>();
		for(String ID : IDs)
		{
			rst.add((Reaction)load(c,ID));
		}
		return rst;
	}
	
	/**
	Get all Pathways with which this Reaction is associated.  Accesses the IN-PATHWAY slot.
	@return all Pathways with which this Reaction is associated.
	*/
	public ArrayList<Frame> getPathways()
	throws PtoolsErrorException {
		//Sometimes the IN-PATHWAY slot of a reaction holds another reaction.  Recursively get pathways on these reactions.
		//No attempt is made to detect circular references
		ArrayList<Frame> pways = new ArrayList<Frame>();
		for (Frame pway : Pathway.load(conn,getSlotValues("IN-PATHWAY"))) {
			if (pway instanceof Reaction) pways.addAll(pway.getPathways());
			else pways.add(pway);
		}
		pathways = pways;
		return pathways;
	}
	
	protected ArrayList<Frame> checkParticipants(ArrayList reactantsAndProducts,int which)
	 throws PtoolsErrorException {
		ArrayList<Frame> rst = new ArrayList<Frame>();
		String slotName = which==0 ? "LEFT" : "RIGHT";
		if(reactantsAndProducts.size()<2) return rst;
		try
		{
			ArrayList<String> reactantIDs = (ArrayList<String>)reactantsAndProducts.get(which);
			for(String reactantID : reactantIDs)
			{
				Frame f = Frame.load(conn,reactantID);
				f.loadAnnotations(this, slotName);
				rst.add(f);
			}
		}
		catch(ClassCastException e)
		{
			e.printStackTrace();
		}
		return rst;
	}
	

	
	protected ArrayList<Frame> getParticipants(int which,Pathway pwy)
	 throws PtoolsErrorException {
		return checkParticipants(conn.reactionReactantsAndProducts(ID,pwy.getLocalID()),which);
	}
	
	protected ArrayList<Frame> getParticipants(int which)
	 throws PtoolsErrorException {
		ArrayList<Frame> rst = new ArrayList<Frame>();//checkParticipants(conn.reactionReactantsAndProducts(ID),which);
		if(rst.size()==0)
		{
			String slotName = which==0 ? "LEFT" : "RIGHT";
			for(Frame f : Frame.load(conn,this.getSlotValues(slotName)))
			{
				f.loadAnnotations(this, slotName);
				rst.add(f);
			}
		}
		return rst;
	}
	
	public boolean isReversible()
	 throws PtoolsErrorException {
		if (getSlotValue("REACTION-DIRECTION") == null) return false;
		return getSlotValue("REACTION-DIRECTION").equals("REVERSIBLE");
	}
	
	/**
	Get the reactants which are input into this Reaction.  Use the default direction for this Reaction without Pathway context.
	@return the Frames which are reactants for this Reaction.
	*/
	public ArrayList<Frame> getReactants()
	 throws PtoolsErrorException {
		return getParticipants(0);
	}
	
	/**
	Get the products which are output of this Reaction. Use the default direction for this Reaction without Pathway context.
	@return the Frames which are products of this Reaction.
	*/
	public ArrayList<Frame> getProducts()
	 throws PtoolsErrorException {
		return getParticipants(1);
	}
	
	/**
	Get the reactants which are input into this Reaction.  Use the direction of this Reaction in the context of a Pathway.
	@return the Frames which are reactants for this Reaction in a specific Pathway.
	@param pwy the Pathway whose context to use for the direction of this Reaction.
	*/
	public ArrayList<Frame> getReactants(Pathway pwy)
	 throws PtoolsErrorException {
		return getParticipants(0,pwy);
	}
	
	/**
	Get the products which are output of this Reaction. Use the direction of this Reaction in the context of a Pathway.
	@return the Frames which are products of this Reaction in a specific Pathway.
	@param pwy the Pathway whose context to use for the direction of this Reaction.
	*/
	public ArrayList<Frame> getProducts(Pathway pwy)
	 throws PtoolsErrorException {
		return getParticipants(1,pwy);
	}
	
	/**
	Count the number of Genes coding for enzymes involved with this Reaction without loading them.  Calls the genes-of-reaction Lisp function on the PGDB.
	@return the number of Genes coding for enzymes involved with this Reaction.
	*/
	public int numGenes()
	 throws PtoolsErrorException {
		return conn.genesOfReaction(ID).size();
	}
	
	protected int numParticipants(int which)
	 throws PtoolsErrorException {
		ArrayList rsAndPs = conn.reactionReactantsAndProducts(ID);
		if(rsAndPs.size() != 2) return 0;
		if(rsAndPs.get(which) instanceof ArrayList)
			return ((ArrayList)rsAndPs.get(which)).size();
		else
			return 0;
	}
	
	/**
	Count the number of reactants for Reaction without loading them.
	@return the number of reactants for Reaction.
	*/
	public int numReactants()
	 throws PtoolsErrorException {
		return numParticipants(0);
	}
	
	/**
	Count the number of products of Reaction without loading them.
	@return the number of products of Reaction.
	*/
	public int numProducts()
	 throws PtoolsErrorException {
		return numParticipants(1);
	}	
	
	/**
	Add a reactant to this Reaction
	@param f the new reactant to add
	*/
	public void addReactant(Frame f,int coef)
	throws PtoolsErrorException {
		this.addSlotValue("LEFT",f.getLocalID());
		this.putLocalSlotValueAnnotations("LEFT", f.getLocalID(), "COEFFICIENT", coef+"");
		this.addSlotValue("SUBSTRATES",f.getLocalID());
	}
	
	/**
	Add a product to this Reaction
	@param f the new product to add
	*/
	public void addProduct(Frame f,int coef)
	throws PtoolsErrorException {
		this.addSlotValue("RIGHT",f.getLocalID());
		this.putLocalSlotValueAnnotations("RIGHT", f.getLocalID(), "COEFFICIENT", coef+"");
		this.addSlotValue("SUBSTRATES",f.getLocalID());
	}
	
	/**
	Attach a Catalysis object to this Reaction
	@param c the Catalysis object to attach
	*/
	public void addCatalysis(Catalysis c)
	throws PtoolsErrorException {
		this.addSlotValue("ENZYMATIC-REACTION",c.getLocalID());
		c.addSlotValue("REACTION",ID);
	}
	
	public void clear() throws PtoolsErrorException
	{
		this.putSlotValues("LEFT", new ArrayList());
		this.putSlotValues("RIGHT", new ArrayList());
		this.putSlotValues("SUBSTRATES", new ArrayList());
		if(this.hasSlot("ENZYMATIC-REACTION")) putSlotValues("ENZYMATIC-REACTION",new ArrayList());
	}
}
