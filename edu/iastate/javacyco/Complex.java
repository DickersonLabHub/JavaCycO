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
Protein Complexes.
@author John Van Hemert
*/
public class Complex extends Protein
{
	/**
	|Protein-Complexes|
	*/
	public static String GFPtype = "|Protein-Complexes|";
	
	public Complex(JavacycConnection c, String id)
	{
		super(c, id);
	}
	
	/**
	Get the components which make up this protein Complex.
	@return An ArrayList of Frame which are usually Complexes and Proteins.  Annotations for COEFFICIENT are loaded.  Access them 
	with component.annotations.get("COEFFICIENT");
	*/
	public ArrayList<Frame> getComponents()
	throws PtoolsErrorException {
		
		//return Protein.load(conn, this.getSlotValues("COMPONENTS"));
		
		ArrayList<Frame> rst = new ArrayList<Frame>();
		ArrayList<String> componentsList = this.getSlotValues("COMPONENTS");

		for(String proteinID : componentsList)
		{
			//System.out.println(proteinID);
			Frame f = Frame.load(conn,proteinID);
			String coef = this.getAnnotation("COMPONENTS", proteinID, "COEFFICIENT");
			if(!coef.equals("NIL")) f.annotations.put("COEFFICIENT",coef);
			rst.add(f);
		}
		return rst;
	}
	
	/**
	Get all Pathways of reactions which this Complex catalyzes and Pathways of Genes this Complex regulates.
	@return all Pathways with which this Reaction is associated.
	*/
	public ArrayList<Frame> getPathways()
	throws PtoolsErrorException {
		ArrayList<Frame> pways = new ArrayList<Frame>();
		
		// Get pathways for which this complex is an enzyme
		for (Frame catalysis : this.getCatalysis()) {
			if (catalysis != null) {
				pways = ((Catalysis)catalysis).getPathways();
			}
		}
		
		// Get pathways of the genes this protein regulates
		for (Gene gene : genesRegulatedByProtein()) {
			ArrayList<String> regulators = conn.regulatorsOfGeneTranscription(gene.ID);
			if (regulators.contains(ID)) {
				for (Frame pway : gene.getPathways()) if (!pways.contains(pway)) pways.add(pway);
			}
		}
		
		// Get pathways of the genes this protein is a sigma factor for.
		for (Gene gene : genesRegulatedBySigmaFactor()) {
			for (Frame pway : gene.getPathways()) if (!pways.contains(pway)) pways.add(pway);
		}
		
		pathways = pways;
		return pathways;
	}
}
