package edu.iastate.javacyco;


import java.util.ArrayList;

/**
Terms from the Gene Ontology fall into this super class.  They have 'subsets' (like goslim) and 'members', which are instances assigned to a GO term.
@author John Van Hemert
*/
@SuppressWarnings({"unchecked"})
public class GOTerm extends OntologyTerm {
	
	/**
	|Gene-Ontology-Terms|
	*/
	public static String GFPtype = "|Gene-Ontology-Terms|";

	public GOTerm(JavacycConnection c, String id) {
		super(c, id);
	}
	
	/**
	@return the subsite (such as goslim) which this GO term falls into.
	*/
	public ArrayList<String> getSubsets()
	throws PtoolsErrorException {
		return this.getSlotValues("SUBSET");
	}
	
	/**
	@return the entities which are annotated with this GO term.
	*/
	public ArrayList<Frame> getMembers()
	throws PtoolsErrorException {
		return Frame.load(conn,this.getSlotValues("TERM-MEMBERS"));
	}

}
