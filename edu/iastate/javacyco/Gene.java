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
The Gene class.  In PGDBs, Genes can actually represent DNA or pre- or mRNA, depending on what is store in the PGDB.
@author John Van Hemert
*/
@SuppressWarnings({"unchecked"})
public class Gene extends Frame {

	private String seq;
	
	/**
	|Genes|
	*/
	public static String GFPtype = "|All-Genes|";
	
	public Gene(JavacycConnection c, String id)
	{
		super(c, id);
	}
	
	/**
	Get this Gene's chromosome.
	@return the Gene's chromosome. 
	*/
	public Chromosome getChromosome()
	throws PtoolsErrorException {
		return (Chromosome)Chromosome.load(conn,conn.chromosomeOfGene(ID));
	}
	
	/**
	Load this Gene's sequence into local memory.
	*/
	public void loadSequence()
	throws PtoolsErrorException {
		if(seq == null)
		{
			seq = conn.getGeneSequence(ID);
			if(seq.equals("NIL"))
				seq = "";
		}
	}
	
	/**
	Load this Gene's sequence if necessary and return it.
	@return this Gene's nucleotide sequence.
	*/
	public String getSequence()
	throws PtoolsErrorException {
		this.loadSequence();
		return seq;
	}
	
	/**
	Load this Gene's sequence if necessary and return its translated protein sequence.
	@return this Gene's translated nucleotide sequence.
	*/
	public String getTranslatedSequence()
	throws PtoolsErrorException {
		this.loadSequence();
		return conn.translate(seq);
	}
	
	/**
	Get the protein enzymes of this gene.  Enzymes catalyze at least one reaction.
	@return an ArrayList of this gene's enzyme products.
	*/
	public ArrayList<Protein> getEnzymes()
	throws PtoolsErrorException {
		ArrayList<Protein> rst = new ArrayList<Protein>();
		for(Object o : conn.enzymesOfGene(ID))
		{
			rst.add((Protein)(Protein.load(conn, (String)o)));
		}
		return rst;
	}
	
	/**
	Get the protein products of this gene.  These include enzymes and products that do not catalyze any reactions.
	@return an ArrayList of this gene's protein products.
	*/
	public ArrayList<Protein> getProducts()
	throws PtoolsErrorException {
		ArrayList<Protein> rst = new ArrayList<Protein>();
		for(Object o : conn.allProductsOfGene(ID))
		{
			rst.add((Protein)(Protein.load(conn, (String)o)));
		}
		return rst;
	}
	
	/**
	Call the Frame.print() method and then print this gene's sequence.
	*/
	public void print()
	throws PtoolsErrorException {
		super.print();
		System.out.println("~SEQUENCE:\n\t"+this.getSequence());
	}
	
	/**
	Get this Gene's TranscriptionUnit.  A Gene comes from one TranscriptionUnit.
	@return this Gene's TranscriptionUnit.
	*/
	public ArrayList<TranscriptionUnit> getTranscriptionUnits()
	throws PtoolsErrorException {
		ArrayList<TranscriptionUnit> rst = new ArrayList<TranscriptionUnit>();
		ArrayList<String> tuIDs = conn.geneTranscriptionUnits(ID);
		for(String tuID : tuIDs)
		{
			rst.add((TranscriptionUnit)TranscriptionUnit.load(conn,tuID));
		}
		return rst;
	}
	
	
	public void addTransciptionUnit(TranscriptionUnit tu)
	throws PtoolsErrorException {
		if(!((ArrayList<String>)conn.geneTranscriptionUnits(ID)).contains(tu.getLocalID()))
		{
			ArrayList<String> conts = conn.getSlotValues(ID,"COMPONENTS-OF");
			if(conts.size()>0)
				this.putSlotValue("COMPONENT-OF",conts.get(0));
			this.addSlotValue("COMPONENT-OF",tu.getLocalID());
			tu.addSlotValue("COMPONENTS",ID);
		}
	}
	
	/**
	Get the Genes which regulate this Gene.
	@return an ArrayList of Frames (can be cast to Genes) which regulate this Gene.
	*/
	public ArrayList<Frame> getRegulatingGenes()
	throws PtoolsErrorException {
		return Gene.load(conn,conn.genesRegulatingGene(ID));
	}
	
	/**
	Get the Genes which this Gene regulates.
	@return an ArrayList of Frames (can be cast to Genes) which this Gene regulates.
	*/
	public ArrayList<Frame> getRegulatedGenes()
	throws PtoolsErrorException {
		return Gene.load(conn,conn.genesRegulatedByGene(ID));
	}
	
	/**
	Only for ECOCYC.
	@return the E Coli genome project's B-Number for this gene.  
	*/
	public String getBNumber()
	throws PtoolsErrorException {
		for(String syn : this.getSynonyms())
			if(syn.matches("\"[b|B][0-9]{4}\"")) return syn;
		for(String syn : this.getNames())
			if(syn.matches("\"[b|B][0-9]{4}\"")) return syn;
		return "";
	}
	
	/**
	Only for ECOCYC.
	@return the E Coli genome project's ECK ID for this gene.  
	*/
	public String getECK()
	throws PtoolsErrorException {
		for(String syn : this.getSynonyms())
			if(syn.matches("\"ECK[0-9]{4}\"")) return syn;
		for(String syn : this.getNames())
			if(syn.matches("\"ECK[0-9]{4}\"")) return syn;
		return "";
	}

	/**
	Get all Pathways of Reactions for which this Gene produces an enzyme which catalyzes the Reaction.
	@return all Pathways with which this Reaction is associated.
	*/
	public ArrayList<Frame> getPathways()
	throws PtoolsErrorException {
//		ArrayList<Frame> pways = new ArrayList<Frame>();
//		for (Frame enzyme : this.getEnzymes()) {
//			for (Frame pway : enzyme.getPathways()) if (!pways.contains(pway)) pways.add(pway);
//		}
//		return pways;
		pathways = Pathway.load(conn, conn.getPathwaysOfGene(this.ID));
		return pathways;
	}
}
