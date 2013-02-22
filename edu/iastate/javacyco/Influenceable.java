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
Catalysis and Promoter object are influencable by activators and inhibitors.
These methods are not pathway-specific and may contain references to Frames which are not actually in the PGDB.
@author John Van Hemert
*/
@SuppressWarnings({"unchecked"})
public abstract class Influenceable extends Frame
{

	public Influenceable(JavacycConnection c, String id)
	{
		super(c, id);
	}
	
	/**
	@return the entities which inhibit this object.
	*/
	public ArrayList<Frame> getInhibitors()
	throws PtoolsErrorException {
		return Frame.load(conn,conn.directInhibitors(ID));
	}
	
	/**
	@return the entities which activate this object.
	*/
	public ArrayList<Frame> getActivators()
	throws PtoolsErrorException {
		return Frame.load(conn,conn.directActivators(ID));
	}
	
	/**
	@return the Regulation objects which link this object to its activators and inhibitors.
	*/
	public ArrayList<Frame> getRegulations()
	throws PtoolsErrorException {
		return Frame.load(conn,this.getSlotValues("REGULATED-BY"));
	}
	
	public void addRegulation(Regulation reg) throws PtoolsErrorException
	{
		this.addSlotValue("REGULATED-BY", reg.getLocalID());
	}
	
	/**
	Add an activator (non-Protein object- usually a compound like Mg2+).
	@param f the Frame which is the activator.
	*/
	public Regulation addActivator(Frame f)
	throws PtoolsErrorException {
		return this.addRegulator(f,true);
	}
	
	/**
	Add an inhibitor (non-Protein object- usually a compound like Mg2+).
	@param f the Frame which is the activator.
	*/
	public Regulation addInhibitor(Frame f)
	throws PtoolsErrorException {
		return this.addRegulator(f,false);
	}
	
	private Regulation addRegulator(Frame f,boolean mode) throws PtoolsErrorException
	{
		Regulation reg = (Regulation)Frame.create(conn,Regulation.GFPtype);
		reg.setMode(mode);
		reg.setRegulator(f);
		reg.setRegulatee(this);
		f.addSlotValue("REGULATES", reg.getLocalID());
		this.addRegulation(reg);
		return reg;
	}
}
