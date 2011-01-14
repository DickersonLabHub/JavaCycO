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
import java.util.HashMap;
import java.util.TreeMap;

/**
The Pathway class.  Groups Reactions together into meaningful processes.
@author John Van Hemert
*/
public class Pathway extends Frame
{
	/**
	|Pathways|
	*/
	public static String GFPtype = "|Pathways|";
	
	public Pathway(JavacycConnection c,String id)
	{
		super(c,id);
	}
	
	/**
	@return true if falls under |Super-Pathways| in PGDB class hierarchy. else returns false.
	*/
	public boolean isSuperPathway()
	throws PtoolsErrorException {
		return this.getClassHierarchy().containsNode("|Super-Pathways|");
	}
	
	/**
	@return all Pathways in the PGDB
	@param c the JavacycConnection to use.
	*/
	static public ArrayList<Pathway> all(JavacycConnection c)
	throws PtoolsErrorException {
		ArrayList<String> pwyIDs = c.allPathways();
		ArrayList<Pathway> rst = new ArrayList<Pathway>();
		for(String pwyID : pwyIDs)
		{
			Pathway p = (Pathway)load(c,pwyID);
			//System.out.println(p.getID());
			rst.add(p);
			
		}
		return rst;
	}
	
	/**
	@return all Reactions in this Pathway
	*/
	public ArrayList<Reaction> getReactions()
	throws PtoolsErrorException {
		ArrayList<Frame> fs = Reaction.load(conn,this.getReactionIDs());
		ArrayList<Reaction> rst = new ArrayList<Reaction>();
		for(Frame f : fs)
		{
			if (f instanceof Pathway) {
				for (Reaction rxn : ((Pathway)f).getReactions()) {
					rst.add(rxn);
				}
			} else {
				rst.add((Reaction)f);
			}
		}
		return rst;
	}
	
	/**
	Attach a Reaction to this Pathway
	@param r the Reaction to attach
	*/
	public void addReaction(Reaction r)
	throws PtoolsErrorException {
		//System.out.println("ADDING REACTION "+r.getClass().getName()+":"+r.getCommonName()+" to "+this.getCommonName());
		this.addSlotValue("REACTION-LIST",r.getLocalID());
		r.addSlotValue("IN-PATHWAY",ID);
		if(r instanceof EnzymeReaction)
		{
			EnzymeReaction erxn = (EnzymeReaction)r;
			for(Protein p : erxn.getEnzymes())
			{
				addMember(p);
				for(Gene g : p.getGenes())
				{
					addMember(g);
				}
			}
		}
		for(Frame f : r.getReactants())
		{
			addMember(f);
		}
		for(Frame f : r.getProducts())
		{
			addMember(f);
		}
		r.commit();
		this.commit();
	}
	
	private void addMember(Frame f)
	throws PtoolsErrorException {
		//System.out.println("ADDING MEMBER "+f.getCommonName()+" to "+this.getCommonName());
		if(f instanceof Protein) addSlotValue("ENZYMES-OF-PATHWAY",f.getLocalID());
		else if(f instanceof Gene) addSlotValue("GENES-OF-PATHWAY",f.getLocalID());
		else if(f instanceof Compound) addSlotValue("COMPOUNDS-OF-PATHWAY",f.getLocalID());
		f.commit();
	}
	@Override
	public void commit() throws PtoolsErrorException
	{
		//super.commit();
		//conn.saveKB();
		Network rxnsNet = this.getReactionNetwork();
		
		this.putSlotValues("PREDECESSORS",rxnsNet.toPredecessorsList());
//		System.out.println("PUT PREDS "+JavacycConnection.ArrayList2LispList(rxnsNet.toPredecessorsList()));
//		System.out.println("rxnNet: "+rxnsNet.getNodes().size()+", "+rxnsNet.getEdges().size());
//		System.out.println("REACTION-LIST: "+JavacycConnection.ArrayList2LispList(this.getReactionIDs()));
//		System.out.println("LOCAL-REACTION-LIST: "+JavacycConnection.ArrayList2LispList(this.slots.get("REACTION-LIST")));
//		System.out.println("PREDECESSORS: "+JavacycConnection.ArrayList2LispList(this.getSlotValues("PREDECESSORS")));
		super.commit();
	}
	
	public void clear()
	{
		this.putSlotValues("REACTION-LIST", new ArrayList());
		this.putSlotValues("ENZYMES-OF-PATHWAY", new ArrayList());
		this.putSlotValues("GENES-OF-PATHWAY", new ArrayList());
		this.putSlotValues("COMPOUNDS-OF-PATHWAY", new ArrayList());
		this.putSlotValues("PREDECESSORS", new ArrayList());
	}
	
	/**
	@return all enzymes in this Pathway
	*/
	public ArrayList<Frame> getEnzymes()
	throws PtoolsErrorException {
		return Protein.load(conn,conn.enzymesOfPathway(ID));
	}
	
	/**
	@return all Genes in this Pathway
	*/
	public ArrayList<Frame> getGenes()
	throws PtoolsErrorException {
		return Gene.load(conn,conn.genesOfPathway(ID));
	}
	
	/**
	@return all Reaction IDs in this Pathway without loading them
	*/
	public ArrayList<String> getReactionIDs()
	throws PtoolsErrorException {
		//if(slots.containsKey("REACTION-LIST")) return slots.get("REACTION-LIST");
		return this.getSlotValues("REACTION-LIST");
	}
	
	/**
	@return the number of Reactions in this Pathway without loading them
	*/
	public int numReactions()
	throws PtoolsErrorException {
		return (new HashSet<String>(getReactionIDs())).size();
	}
	
	/**
	@return the number of Genes in this Pathway without loading them
	*/
	public int numGenes()
	throws PtoolsErrorException {
		return (new HashSet<String>(conn.genesOfPathway(ID))).size();
	}
	
	/**
	@return the number of Enzymes in this Pathway without loading them
	*/
	public int numEnzymes()
	throws PtoolsErrorException {
		return (new HashSet<String>(conn.enzymesOfPathway(ID))).size();
	}
	
	/**
	@return the number of Compounds in this Pathway without loading them
	*/
	public int numCompounds()
	throws PtoolsErrorException {
		return (new HashSet<String>(conn.compoundsOfPathway(ID))).size();
	}
	
	/**
	@return all Reactions common to this Pathway and another Pathway
	@param b the other Pathway to search for common Reactions
	*/
	public ArrayList<Reaction> commonReactions(Pathway b)
	throws PtoolsErrorException {
		HashSet<String> ids = new HashSet<String>(this.getReactionIDs());
		ids.retainAll(new HashSet<String>(b.getReactionIDs()));
		ArrayList<Reaction> rst = new ArrayList<Reaction>();
		for(String id : ids)
		{
			rst.add((Reaction)Reaction.load(conn,id));
		}
		return rst;
	}
	
	/**
	@return all entities common to this Pathway and another Pathway
	@param b the other Pathway to search for common entities
	*/
	public ArrayList<Frame> commonEntities(Pathway b)
	throws PtoolsErrorException {
		HashSet<String> ids = new HashSet<String>(this.getEntityIDs());
		ids.retainAll(new HashSet<String>(b.getEntityIDs()));
		ArrayList<Frame> rst = new ArrayList<Frame>();
		for(String id : ids)
		{
			rst.add(Frame.load(conn,id));
		}
		return rst;
	}
	
	/**
	@return all entity IDs in this Pathway without loading them
	*/
	public ArrayList<String> getEntityIDs()
	throws PtoolsErrorException {
		ArrayList<String> rst = new ArrayList<String>();
		for(Object o : conn.substratesOfPathway(ID))
		{
			if(o instanceof String)
				rst.add((String)o);
			else
			{
				for(Object so : (ArrayList)o)
				{
					rst.add((String)so);
				}
			}
		}
		return rst;
	}
	
	/**
	@return all entities in this Pathway
	*/
	public ArrayList<Frame> getEntitys()
	throws PtoolsErrorException {
		return Frame.load(conn,this.getEntityIDs());
	}
	
	/**
	 * List all Pathway ID's and names.
	@return a TreeMap mapping all Pathway ID's to their respective common names.
	@param c The connection to use
	*/
	public static TreeMap<String,String> list(JavacycConnection c)
	throws PtoolsErrorException {
		TreeMap<String,String> rst = new TreeMap<String,String>();
		for(Object o : c.allPathways())
		{
			String id = (String)o;
			String name = c.getSlotValue(id,"COMMON-NAME");
			if(name.startsWith("\"")) name = name.substring(1);
			if(name.endsWith("\"")) name = name.substring(0,name.length()-1);
			rst.put(id,name);
		}
		return rst;
	}
	
	/**
	Build a Network where Reactions are nodes and the Edges represent substrates.
	@return the Network of Reactions from this Pathway.
	*/
	public Network getReactionNetwork()
	throws PtoolsErrorException {
		Network rst = new Network(ID+"_reaction_network");
		ArrayList<Reaction> rxns = this.getReactions();
		for(Reaction i : rxns)
		{
			ArrayList<Frame> productsi = i.getProducts();
			//System.out.println(i.getLocalID()+" has "+productsi.size()+" products");
			for(Reaction j : rxns)
			{
				ArrayList<Frame> reactantsj = j.getReactants();
				//System.out.println("\t"+j.getLocalID()+" has "+reactantsj.size()+" reactants");
				boolean hit = false;
				for(Frame p : productsi)
				{
					for(Frame r : reactantsj)
					{
						if(p.getLocalID().equals(r.getLocalID()))
						{
							rst.addEdge(i,j,"");
							hit = true;
							break;
						}
					}
					if(hit)
						break;
				}
			}
		}
		return rst;
	}
	
	/**
	Builds a Network object representing all interactions in this Pathway name by this Pathway's ID 
	with a prefix of "pathways/" so that it is printed to a file in a directory named pathways.
	@return the Network representing this metabolic Pathway
	*/
	public Network getNetwork()
	throws PtoolsErrorException {
		Network rst = new Network("pathways/"+ID);
		conn.buildNetwork(this,rst,true);
		return rst;
	}
	
	/**
	Returns self.
	@return all Pathways with which this Pathway is associated.
	*/
	public ArrayList<Frame> getPathways()
	throws PtoolsErrorException {
		ArrayList<Frame> pways = new ArrayList<Frame>();
		pways.add(this);
		pathways = pways;
		return pathways;
	}
}
