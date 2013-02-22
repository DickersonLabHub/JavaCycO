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
The Protein class.  All PGDB instances are actually mapped to either javacyc.Monomer or javacyc.Complex, which are subclasses of 
javacyc.Protein.  Do not create a new PGDB frame of this type as there are no template slots for it.  Instead, either create new 
PGDB frames of GFP type Complex.GFPtype or Monomer.GFPtype.
@author John Van Hemert
*/
@SuppressWarnings({"unchecked"})
public abstract class Protein extends Frame {

	/**
	|Proteins| --Do not create a new PGDB frame of this type as there are no template slots for it.  Instead, either create new 
PGDB frames of GFP type Complex.GFPtype or Monomer.GFPtype.  Use this static field as for loading all PGDB Proteins.
@see JavacycConnection#getAllGFPInstances(String)
	*/
	public static String GFPtype = "|Proteins|";
	
	public Protein(JavacycConnection c, String id)
	{
		super(c, id);
	}
	
	/**
	Get all Gene Ontology terms assigned to this Protein object.
	Simply calls getSlotValues("GO-TERMS")
	@return all synonyms for this Protein object.
	*/
	public ArrayList<GOTerm> getGOterms()
	throws PtoolsErrorException {
		ArrayList<GOTerm> rst = new ArrayList<GOTerm>();
		for(Object obj : this.getSlotValues("GO-TERMS"))
		{
			rst.add((GOTerm)Frame.load(conn,(String)obj));
		}
		return rst;
	}

	/**
	Get the Protein which is complex containing this Protein as a subunit.
	@return all synonyms for this Protein object.
	*/
	public Complex getContainer()
	throws PtoolsErrorException {
		return (Complex)Frame.load(conn, this.getSlotValue("COMPONENT-OF"));
	}
	
	/**
	Get the CellComponents (of the SRI Cellular Componenent Ontology) associated with this Protein.  Loads CellComponents from the LOCATIONS slot.
	@return all CellComponents ontology terms associates with this Protein.
	*/
	public ArrayList<CellComponent> getCellComponents()
	throws PtoolsErrorException {
		ArrayList<CellComponent> rst = new ArrayList<CellComponent>();
		for(Frame f : Frame.load(conn,this.getSlotValues("LOCATIONS")))
		{
			if(f instanceof CellComponent)
				rst.add((CellComponent)f);
			else
				System.out.println(f.getLocalID()+" is not a CellComponent--it's a "+f.getClass().getName());
		}
		return rst;
	}
	
	/**
	Get the Catalysis object associate with this Protein.
	@return the Catalysis which this Protein causes.
	*/
	public ArrayList<Frame> getCatalysis()
	throws PtoolsErrorException {
		return Frame.load(conn, this.getSlotValues("CATALYZES"));
	}
	
	/**
	Get the Regulation objects where this Protein is the regulator.
	@return all Regulation objects where this Protein is the regulator.
	*/
	public ArrayList<Regulation> getRegulations()
	throws PtoolsErrorException {
		ArrayList<Regulation> rst = new ArrayList<Regulation>();
		for(Frame f : Frame.load(conn, this.getSlotValues("REGULATES")))
		{
			rst.add((Regulation)f);
		}
		return rst;
	}
	
	/**
	Get the Genes which code for this Protein.  If this Protein is a Monomer, the result is usually one Gene.
	If this Protein is a Complex, the result is all the Genes which code for all the Monomers which make up this Complex.
	@return the Genes which code for this Protein and/or its subunits.
	*/
	public ArrayList<Gene> getGenes()
	throws PtoolsErrorException {
		ArrayList<Gene> rst = new ArrayList<Gene>();
		ArrayList<String> ids = conn.genesOfProtein(ID);

		for(String id : ids)
		{
			//System.out.println(Frame.load(conn,id).getClass().getName());
			rst.add((Gene)Gene.load(conn,id));
		}
		return rst;
	}
	
	/**
	Get the genes which this protein regulates. Does not capture sigma factor regulation.
	@return the genes which this protein regulates.
	*/
	public ArrayList<Gene> genesRegulatedByProtein()
	throws PtoolsErrorException {
		ArrayList<Gene> rst = new ArrayList<Gene>();
		ArrayList<String> ids = conn.genesRegulatedByProtein(ID);

		for(String id : ids) rst.add((Gene)Gene.load(conn,id));
		
		return rst;
	}
	
	/**
	Get the genes which this sigma factor regulates.
	@return the genes which this sigma factor regulates or empty list if this protein is not a sigma factor.
	*/
	public ArrayList<Gene> genesRegulatedBySigmaFactor()
	throws PtoolsErrorException {
		ArrayList<Gene> genes = new ArrayList<Gene>();
		
		// This works when the slot RECOGNIZED-PROMOTERS is accessible (EcoCyc v14.0)
		if(this.isGFPClass("|Sigma-Factors|")) {
			for (String s : (ArrayList<String>)this.getSlotValues("RECOGNIZED-PROMOTERS")) {
				Promoter pm = (Promoter)Promoter.load(conn, s);
				for (String tu : (ArrayList<String>)pm.transcriptionUnitsOfPromoter()) {
					for (Gene g : ((TranscriptionUnit)TranscriptionUnit.load(conn, tu)).getGenes()) {
						if (!genes.contains(g)) genes.add(g);
					}
				}
			}
		}
		
//		// This works when we can't get to the RECOGNIZED-PROMOTERS slot of the sigma factor (EcoCyc v13.5)
//		if(this.isGFPClass("|Sigma-Factors|")) {
//			for (Frame promoter : conn.getAllGFPInstances("|Promoters|")) {
//				if (((Promoter)promoter).getSigmaFactor() != null && ID.equals(((Promoter)promoter).getSigmaFactor().getLocalID())) {
//					for (String tu : (ArrayList<String>)((Promoter)promoter).transcriptionUnitsOfPromoter()) {
//						for (Gene g : ((TranscriptionUnit)TranscriptionUnit.load(conn, tu)).getGenes()) {
//							if (!genes.contains(g)) genes.add(g);
//						}
//					}
//				}
//			}
//		}

		return genes;
	}
	
	/**
	Get all Pathways with which this Protein is associated as an enzyme.
	@return all Pathways with which this Reaction is associated.
	*/
	public ArrayList<Frame> getPathways()
	throws PtoolsErrorException {
		System.out.println("getPathways called on a Protein of a subclass of Protein for which the function was not overwritten: " + this.getClass().getName());
		pathways = new ArrayList<Frame>();
		return pathways;
	}
}
