package edu.iastate.javacyco;


import java.util.ArrayList;

/**
PGDBs have an extensive class hierarchy which is basically an ontology.  Therefore, if a Frame is a class Frame and not classified 
by the Frame loader as any specific javaCyc class, then the Frame is an OntologyTerm with super classes and subclasses and instances.
*/
public class OntologyTerm extends Frame {

	public OntologyTerm(JavacycConnection c, String id) {
		super(c, id);
	}
	
	public ArrayList<Frame> getPathways()
	throws PtoolsErrorException {
		ArrayList<Frame> pways = new ArrayList<Frame>();
		for(OntologyTerm child : this.getChildren())
		{
			pways.addAll(child.getPathways());
		}
		pathways = pways;
		return pathways;
	}
	
	/**
	@return the defintion of this term.
	*/
	public String getDefinition()
	throws PtoolsErrorException {
		return (String)(this.getSlotValues("DEFINITION").get(0));
	}
	
	/**
	@return the subclasses of this term.
	*/
	public ArrayList<OntologyTerm> getChildren()
	throws PtoolsErrorException {
		ArrayList<OntologyTerm> rst = new ArrayList<OntologyTerm>();
		for(Object fido : this.getSlotValues("OCELOT-GFP::SUBS"))
		{
			String fid = (String)fido;
			rst.add((OntologyTerm)Frame.load(conn,fid));
			
		}
		return rst;
	}

	/**
	@return the superclasses of this term.
	*/
	public ArrayList<OntologyTerm> getParents()
	throws PtoolsErrorException {
		ArrayList<OntologyTerm> rst = new ArrayList<OntologyTerm>();
		if(ID.equals("|Gene-Ontology-Terms|")) return rst;
		for(Object fido : this.getSlotValues("OCELOT-GFP::PARENTS"))
		{
			String fid = (String)fido;
			rst.add((OntologyTerm)Frame.load(conn,fid));
			
		}
		return rst;
	}
	
	/**
	@return the instances (leaves) of this term.
	*/
	public ArrayList<Frame> getInstances()
	throws PtoolsErrorException {
		ArrayList<Frame> rst = new ArrayList<Frame>();
		for(Object fido : this.getSlotValues("OCELOT-GFP::INSTANCES"))
		{
			String fid = (String)fido;
			rst.add(Frame.load(conn,fid));
			
		}
		return rst;
	}
	
	/**
	Find the lowest common child of a set of OntologyTerms.
	@param c the JavacycConnection to use.
	@param terms the set of OntologyTerms to search for a common child term.
	@return the OntologyTerm which is the lowest common child of the terms.  Lowest means the farthest from the root Frame, FRAMES.
	returns null if none exists.
	*/
	public static OntologyTerm getLowestCommonChild(JavacycConnection c,ArrayList<OntologyTerm> terms)
	throws PtoolsErrorException {
		if(terms.size()==0) return null;
		if(terms.size()==1) return terms.get(0);
		ArrayList<OntologyTerm> candidates = new ArrayList<OntologyTerm>();
		for(int i=0;i<terms.size()-1;i+=2)
		{
			OntologyTerm cand = terms.get(i).getLowestCommonChild(terms.get(i+1));
			if(cand != null) candidates.add(cand);
		}
		return OntologyTerm.getLowestCommonChild(c,candidates);
	}
	
	/**
	Find the lowest common child between this term and another term.
	@param term the other term.
	@return the OntologyTerm which is the lowest common child of the terms.  Lowest means the farthest from the root Frame, FRAMES.
	returns null if none exists.
	*/
	public OntologyTerm getLowestCommonChild(OntologyTerm term)
	throws PtoolsErrorException {
		if(ID.equals(term.getLocalID())) return this;
		int myChildrenSize = this.getChildren().size();
		int termsChildrenSize = term.getChildren().size();
		if(myChildrenSize==0 && termsChildrenSize==0) return null;
		ArrayList<OntologyTerm> candidates = new ArrayList<OntologyTerm>();
		if(myChildrenSize==0)
		{
			for(OntologyTerm t : term.getChildren())
			{
				OntologyTerm cand = this.getLowestCommonChild(t);
				if(cand != null) candidates.add(cand);
			}
		}
		else if(termsChildrenSize==0)
		{
			for(OntologyTerm t : this.getChildren())
			{
				OntologyTerm cand = term.getLowestCommonChild(t);
				if(cand != null) candidates.add(cand);
			}
		}
		else
		{
			for(OntologyTerm t : this.getChildren())
			{
				for(OntologyTerm t2 : term.getChildren())
				{
					OntologyTerm cand = t.getLowestCommonChild(t2);
					if(cand != null) candidates.add(cand);
				}
			}
		}
		
		return OntologyTerm.getDeepestOntologyTerm(conn,candidates);
	}
	
	/**
	Find the term from a set of terms which is farthest from the root term (FRAMES).
	@param c the JavacycConnection to use.
	@param terms the set of OntologyTerms to search.
	@return the OntologyTerm which is the farthest from the root term.
	*/
    public static OntologyTerm getDeepestOntologyTerm(JavacycConnection c,ArrayList<OntologyTerm> terms)
    throws PtoolsErrorException {
    	if(terms.size()==0) return null;
    	OntologyTerm rst = terms.get(0);
    	int maxParents = c.getInstanceAllTypes(rst.getLocalID()).size();
    	for(int i=1; i<terms.size(); i++)
    	{
    		int test = c.getInstanceAllTypes(terms.get(i).getLocalID()).size();
    		if(test > maxParents)
    		{
    			rst = terms.get(i);
    			maxParents = test;
    		}
    	}
    	return rst;
    }
}
