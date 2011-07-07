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
Compounds or metabolites.
@author John Van Hemert
*/
public class Compound extends Frame
{
	/**
	|Compounds|
	*/
	public static String GFPtype = "|Compounds|";
	
	public Compound(JavacycConnection c,String id)
	{
		super(c,id);
	}
	
	/**
	Get the InChi code for this compound.  InChi codes are machine-readable codes for the molecular structure and makeup of a compound.
	@return this Compound's InChi code.
	*/
	public String getInChi()
	throws PtoolsErrorException {
		return this.getSlotValue("INCHI");
	}
	
	public void setInChi(String n)
	{
		this.putSlotValue("INCHI",n);
	}
	
	/**
	Get the Reactions in which this Compound is a reactant.  Reactant meaning the input into the Reaction to make the products.
	@return An ArrayList of Reactions in which this Compound is a reactant.
	*/
	public ArrayList<Reaction> reactantIn()
	throws PtoolsErrorException {
		ArrayList<Reaction> rst = new ArrayList<Reaction>();
		for(Frame f : Reaction.load(conn,this.getSlotValues("APPEARS-IN-LEFT-SIDE-OF")))
			rst.add((Reaction)f);
		return rst;
	}
	
	/**
	Get the Reactions in which this Compound is a product.  product meaning the output of the Reaction.
	@return An ArrayList of Reactions in which this Compound is a product.
	*/
	public ArrayList<Reaction> productOf()
	throws PtoolsErrorException {
		ArrayList<Reaction> rst = new ArrayList<Reaction>();
		for(Frame f : Reaction.load(conn,this.getSlotValues("APPEARS-IN-RIGHT-SIDE-OF")))
			rst.add((Reaction)f);
		return rst;
	}
	
	/**
	Get the SMILES code for this compound.  SMILES codes are machine-readable codes for the molecular structure and makeup of a compound.
	@return this Compound's SMILES code.
	*/
	public String getSmiles()
	throws PtoolsErrorException {
		return this.getSlotValue("SMILES");
	}
	
	
	/**
	 * 
	 */
	public ArrayList<Frame> cofactorOf()
	throws PtoolsErrorException {
		return Protein.load(conn, this.getSlotValues("Cofactors-Of"));
	}
	
	/**
	 * 
	 */
	public ArrayList<Frame> prostheticGroupOf()
	throws PtoolsErrorException {
		return Protein.load(conn, this.getSlotValues("Prosthetic-Groups-Of"));
	}
	
	/**
	Return the Pathways of Reactions with which this Compound is associated with as a reactant, a product, a cofactor, or a prosthetic group.
	@return all Pathways with which this Compound is associated.
	*/
	public ArrayList<Frame> getPathways()
	throws PtoolsErrorException {
		if(pathways==null || pathways.size()==0)
		{
			pathways = new ArrayList<Frame>();
			HashSet<String> done = new HashSet<String>();
			ArrayList rst = this.conn.callFuncArray("WITH-ORGANISM (:ORG-ID '"+conn.getOrganismID()+") (MAPCAR #'OBJECT-NAME (LOOP FOR RXN IN (APPEND (WITH-ORGANISM (:ORG-ID '"+conn.getOrganismID()+")(MAPCAR #'OBJECT-NAME (GET-SLOT-VALUES 'WATER 'APPEARS-IN-LEFT-SIDE-OF))) (WITH-ORGANISM (:ORG-ID '"+conn.getOrganismID()+")(MAPCAR #'OBJECT-NAME (GET-SLOT-VALUES 'WATER 'APPEARS-IN-RIGHT-SIDE-OF))) (WITH-ORGANISM (:ORG-ID '"+conn.getOrganismID()+")(MAPCAR #'OBJECT-NAME (GET-SLOT-VALUES 'WATER 'COFACTOR-OF))) (WITH-ORGANISM (:ORG-ID '"+conn.getOrganismID()+")(MAPCAR #'OBJECT-NAME (GET-SLOT-VALUES 'WATER 'PROSTHETIC-GROUPS-OF)))) FOR PWY = (WITH-ORGANISM (:ORG-ID '"+conn.getOrganismID()+")(MAPCAR #'OBJECT-NAME (GET-SLOT-VALUES RXN 'IN-PATHWAY))) UNLESS (FEQUAL PWY NIL) COLLECT PWY))", true);
			for(Object obj : rst)
			{
				ArrayList objList = (ArrayList)obj;
				for(Object obj2 : objList)
				{
					String s = (String)obj2;
					if(!done.contains(s))
					{
						Frame pwy = Frame.load(conn, s);
						if(pwy instanceof Pathway)
						{
							pathways.add(pwy);
							done.add(s);
						}
					}
				}
			}
		}
		return pathways;
	}
	
}
