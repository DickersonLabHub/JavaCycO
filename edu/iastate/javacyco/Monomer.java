package edu.iastate.javacyco;

import java.util.ArrayList;
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


/**
The Monomer class.  These may act alone or as components in Complexes.
@author John Van Hemert
*/
public class Monomer extends Protein
{
	/**
	|Polypeptides|
	*/
	public static String GFPtype = "|Polypeptides|";
	
	public Monomer(JavacycConnection c, String id)
	{
		super(c, id);
	}
	
	/**
	@return the isoelectric point of this Monomer.
	*/
	public double getPi()
	throws PtoolsErrorException {
		return Double.parseDouble(this.getSlotValue("PI"));
	}

	/**
	@return this Monomer's Gene's translated sequence.
	*/
	public String getSequence()
	throws PtoolsErrorException {
		return this.getGenes().size() == 0 ? "" : getGenes().get(0).getTranslatedSequence();
	}
	
	/**
	Calls the Frame.print() method and then prints this Monomer's protein sequence.
	*/
	public void print()
	throws PtoolsErrorException {
		super.print();
		System.out.println("~SEQUENCE:\n\t"+this.getSequence());
	}
	
	public void setPi(double n)
	{
		this.putSlotValue("PI",""+n);
	}
	
	/**
	Add a Gene to this Monomer's list of Genes from which it is translated.
	@param g the gene to add.
	*/
	public void addGene(Gene g)
	throws PtoolsErrorException {
		if(!((ArrayList<String>)conn.genesOfProtein(ID)).contains(g.getLocalID()))
		{
			this.addSlotValue("GENE",g.getLocalID());
			g.putSlotValue("PRODUCT",ID);
		}
	}

	/**
	Get the complexes for which this monomer can be a component.
	@return An ArrayList of Frame which are usually Complexes and Proteins.
	*/
	public ArrayList<Frame> getComponentOf()
	throws PtoolsErrorException {
		return Complex.load(conn, this.getSlotValues("COMPONENT-OF"));
	}
	
	/**
	Get all Pathways with which this Monomer is associated as an enzyme and Pathways of Genes this monomer regulates.
	Include the pathways of any complexes this monomer can form.
	@return all Pathways with which this Reaction is associated.
	*/
	public ArrayList<Frame> getPathways()
	throws PtoolsErrorException {
		ArrayList<Frame> pways = new ArrayList<Frame>();
		
//		// Get pathways for which this monomer is an enzyme
//		for (Frame catalysis : this.getCatalysis()) {
//			if (catalysis != null) {
//				pways = ((Catalysis)catalysis).getPathways();
//			}
//		}
//		
//		// Get pathways of the genes this protein regulates
//		for (Gene gene : genesRegulatedByProtein()) {
//			ArrayList<String> regulators = conn.regulatorsOfGeneTranscription(gene.ID);
//			if (regulators.contains(ID)) {
//				for (Frame pway : gene.getPathways()) if (!pways.contains(pway)) pways.add(pway);
//			}
//		}
//		
//		// Get pathways of the genes this protein is a sigma factor for.
//		for (Gene gene : genesRegulatedBySigmaFactor()) {
//			for (Frame pway : gene.getPathways()) if (!pways.contains(pway)) pways.add(pway);
//		}
//		
//		// Get pathways of complexes this monomer is a component of
//		//TODO just call complex.getpathways()?  Do I really want to include complex pathways when considering a monomer?
//		for (Frame complex : this.getComponentOf()) {
//			if (!(complex instanceof Complex)) {
//				System.out.println(this.ID);
//			}
//			for (Frame catalysis : ((Complex)complex).getCatalysis()) {
//				if (catalysis != null) {
//					for (Frame pway : ((Catalysis)catalysis).getPathways()) if (!pways.contains(pway)) pways.add(pway);
//				}
//			}
//		}
		
		for(Gene g : this.getGenes())
		{
			pways.addAll(g.getPathways());
		}
		
		pathways = pways;
		return pathways;
	}
}
