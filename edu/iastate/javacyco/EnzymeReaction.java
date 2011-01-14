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
A Reaction catalyzed by an enzyme.
@author John Van Hemert
*/
public class EnzymeReaction extends Reaction
{
	/**
	|Small-Molecule-Reactions| |Chemical-Reactions|
	*/
	public static String GFPtype = "|Small-Molecule-Reactions|";// |Chemical-Reactions|";
	
	public EnzymeReaction(JavacycConnection c, String id) {
		super(c, id);
		//GFPtype = "|Small-Molecule-Reactions|";
	}

	/**
	Get all the Enzyme Reactions in the PGDB.
	@return an ArrayList of all Reactions which are Enzyme Reactions.
	*/
	static public ArrayList<Reaction> all(JavacycConnection c)
	 throws PtoolsErrorException {
		return Reaction.all(c,Reaction.ENZYME);
	}
	
	/**
	Get the Catalysis object associated with this Enzyme Reaction.
	@return the Catalysis object associated with this Enzyme Reaction.
	*/
	public ArrayList<Catalysis> getCatalysis()
	throws PtoolsErrorException {
		ArrayList<Catalysis> rst = new ArrayList<Catalysis>();
		for(Object o : this.getSlotValues("ENZYMATIC-REACTION"))
		{
			String id = (String)o;
			rst.add((Catalysis)Frame.load(conn,id));
		}
		return rst;
	}
	
	/**
	Get the Protein(s) which catalyze this EnzymeReaction.  This is a "short cut" which avoids going through the Catalysis object via 
	getCatalysis().  
	@return the Catalysis object associated with this Enzyme Reaction.
	@see #getCatalysis()
	*/
	public ArrayList<Protein> getEnzymes()
	 throws PtoolsErrorException {
		ArrayList<String> enzymeIDs = conn.enzymesOfReaction(ID);
		ArrayList<Protein> rst = new ArrayList<Protein>();

		for(String enzymeID : enzymeIDs)
		{
			rst.add((Protein)Protein.load(conn,enzymeID));
		}
		return rst;
	}
	
	/**
	Count the number of enzymes which catalyze this Reaction without loading them.
	@return the number of enzymes which catalyze this Reaction.
	*/
	public int numEnzymes()
	 throws PtoolsErrorException {
		return conn.enzymesOfReaction(ID).size();
	}
}
