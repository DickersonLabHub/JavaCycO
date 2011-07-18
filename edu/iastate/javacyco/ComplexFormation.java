package edu.iastate.javacyco;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Frames of this type don't actually exist in PGDBs, but they are used as connectors in client-side networks.
 * It overrides some Reaction and Frame methods to keep them from attempting to query the PGDB.
 * @author jlv
 */
public class ComplexFormation extends Reaction
{

	public static String suffix = "-formation";

	public ComplexFormation(Complex comp)
	{
		super(comp.getConnection(),comp.getLocalID()+suffix);
		setCommonName(comp.getLocalID()+suffix);
		pathways = new ArrayList<Frame>();
	}

	@Override
	public HashMap<String,ArrayList> getSlots() throws PtoolsErrorException
	{
		return slots;
	}

	@Override
	public ArrayList<Frame> getPathways()
	throws PtoolsErrorException {
		return pathways;
	}

	@Override
	public boolean isReversible()
	 throws PtoolsErrorException {
		return true;
	}

}
