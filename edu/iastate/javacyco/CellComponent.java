package edu.iastate.javacyco;


import java.util.ArrayList;

/**
This class implements SRI's Cell Component Ontology.  These terms are usually associated with a GOCellularCompoennt term.
@author John Van Hemert
*/
@SuppressWarnings({"unchecked"})
public class CellComponent extends OntologyTerm {

	/**
	|CCO|
	*/
	public static String GFPtype = "CCO";
	
	public CellComponent(JavacycConnection c, String id) {
		super(c, id);
	}
	
	public GOTerm getGOTerm()
	throws PtoolsErrorException {
		return (GOTerm)Frame.load(conn,"|"+this.getSlotValue("GOID")+"|");
	}
	
	/**
	Get the term's container.  Like asking 'what is this term a component of?'
	@return the CellComponent which contains this CellComponent.
	*/
	public CellComponent getContainer()
	throws PtoolsErrorException {
		return (CellComponent)Frame.load(conn,(String)this.getSlotValue("COMPONENT-OF"));
	}
	
	/**
	Get the CellComponents which are contained within this one.
	@return CellComponents found inside this one.
	*/
	public ArrayList<CellComponent> getComponents()
	throws PtoolsErrorException {
		ArrayList<CellComponent> rst = new ArrayList<CellComponent>();
		for(Frame f : Frame.load(conn, this.getSlotValues("COMPONENTS")))
		{
			rst.add((CellComponent)f);
		}
		return rst;
	}
	
	/**
	Get the CellComponent which surrounds this one.
	@return the CellComponent which surrounds this one.
	*/
	public ArrayList<CellComponent> surroundedBy()
	throws PtoolsErrorException {
		ArrayList<CellComponent> rst = new ArrayList<CellComponent>();
		for(Frame f : Frame.load(conn, this.getSlotValues("SURROUNDED-BY")))
		{
			rst.add((CellComponent)f);
		}
		return rst;
	}
	
	/**
	Get the CellComponents which this one surrounds.
	@return the CellComponents which this one surrounds.
	*/
	public ArrayList<CellComponent> surrounds()
	throws PtoolsErrorException {
		ArrayList<CellComponent> rst = new ArrayList<CellComponent>();
		for(Frame f : Frame.load(conn, this.getSlotValues("SURROUNDS")))
		{
			rst.add((CellComponent)f);
		}
		return rst;
	}

}
