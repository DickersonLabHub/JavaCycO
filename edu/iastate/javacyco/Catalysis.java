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

/**
This class maps to the PGDB class |Enzymatic-Reactions|.  It relates Reactions to their respective enzymes, cofactors, activators, and inhibitors.
@author John Van Hemert
*/
@SuppressWarnings({"unchecked"})
public class Catalysis extends Influenceable
{
	/**
	|Enzymatic-Reactions|
	*/
	public static String GFPtype = "|Enzymatic-Reactions|";

	public Catalysis(JavacycConnection c, String id)
	{
		super(c, id);
		//GFPtype = "|Enzymatic-Reactions|";
	}
	
	/**
	Get the Km of the reaction.
	@return the Km of the reaction.
	*/
	public Double getKm()
	throws PtoolsErrorException {
		try
		{
			return Double.parseDouble(conn.getSlotValue(ID,"KM"));
		}
		catch(Exception e)
		{
			if(e instanceof PtoolsErrorException)
				throw (PtoolsErrorException)e;
			else
				return null;
		}
	}
	
	/**
	Set the Km of the reaction.
	@param n the new Km of the reaction.
	*/
	public void setKm(double n)
	{
		this.putSlotValue("KM",""+n);
	}
	
	/**
	Get the Reaction object associated with this Catalysis object.
	@return the Reaction associated with this Catalysis event.
	*/
	public ArrayList<Frame> getReactions()
	throws PtoolsErrorException {
		return Reaction.load(conn, this.getSlotValues("REACTION"));
	}
	
	/**
	Get the cofactors which enable this Catalysis.
	@return An ArrayList of cofactors, usually Compounds.
	*/
	public ArrayList<Frame> getCofactors()
	throws PtoolsErrorException {
		ArrayList<String> cfIDs = this.getSlotValues("COFACTORS");
		ArrayList<Frame> rst = new ArrayList<Frame>();
		for(String cfID : cfIDs)
		{
			rst.add(Frame.load(conn,cfID));
			//rst.add((Compound)Compound.load(conn,cfID));
		}
		return rst;
	}
	
	/**
	Get the prosthetic groups which enable this Catalysis.
	@return An ArrayList of prosthetic groups, usually Compounds.
	*/
	public ArrayList<Frame> getProstheticGroups()
	throws PtoolsErrorException {
		ArrayList<String> cfIDs = this.getSlotValues("PROSTHETIC-GROUPS");
		ArrayList<Frame> rst = new ArrayList<Frame>();
		for(String cfID : cfIDs)
		{
			rst.add(Frame.load(conn,cfID));
		}
		return rst;
	}
	
	/**
	Get the enzyme associated with this Catalysis.
	@return the Catalysis enzyme, a Protein.
	*/
	public Protein getEnzyme()
	throws PtoolsErrorException {
		return getSlotValues("ENZYME").size()>0 ? (Protein)Protein.load(conn,(String)this.getSlotValues("ENZYME").get(0)) : null;
	}
	
	/**
	Set the enzyme associated with this Catalysis.
	@param p the new enzyme for this Catalysis.
	*/
	public void setEnzyme(Protein p)
	throws PtoolsErrorException {
		this.putSlotValue("ENZYME",p.getLocalID());
		p.addSlotValue("CATALYZES",ID);
	}
	
	/**
	Get all Pathways of Reactions which this Catalysis object participates.
	@return all Pathways with which this Reaction is associated.
	*/
	public ArrayList<Frame> getPathways()
	throws PtoolsErrorException {
		ArrayList<Frame> pways = new ArrayList<Frame>();
		for (Frame rxn : this.getReactions()) {
			for (Frame pway : rxn.getPathways())
                            if (!pways.contains(pway)) 
                                pways.add(pway);
		}
		pathways = pways;
		return pathways;
	}

}
