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


/**
The Regulation class links 'regulators' to 'regulatees.'  Catalysis objects and Promoter objects (Influenceables) can be regulated.
@author John Van Hemert
*/
public class Regulation extends Frame {

	/**
	|Regulation|
	*/
	public static String GFPtype = "|Regulation|";
	
	public Regulation(JavacycConnection c, String id)
	{
		super(c, id);
	}
	
	/**
	Get the 'regulator' which somehow regulates through this Regulation.
	@return the regulator of this Regulation
	@throws PtoolsErrorException if there is no regulator in the REGULATOR slot of this Regulation object.
	*/
	public Frame getRegulator()
	throws PtoolsErrorException {
		String regID = this.getSlotValue("REGULATOR");
		if(regID==null || regID.equals("NIL") || regID.length()==0)
			//throw new PtoolsErrorException("No regulator for regulation "+ID);
			System.err.println("No regulator for regulation "+ID);
		else
			return Frame.load(conn,regID);
		return null;
	}
	
	/**
	Get the 'regulatee' which id somehow regulated through this Regulation.
	@return the regulatee of this Regulation
	@throws PtoolsErrorException if there is no regulatee in the REGULATED-ENTITY slot of this Regulation object.
	*/
	public Frame getRegulatee()
	throws PtoolsErrorException {
		String regID = this.getSlotValue("REGULATED-ENTITY");
		if(regID==null || regID.equals("NIL") || regID.length()==0)
			//throw new PtoolsErrorException("No regulatee for regulation "+ID);
			System.err.println("No regulatee for regulation "+ID);
		else
			return Frame.load(conn,regID);
		return null;
	}
	
	/**
	Get the mechanism by which this Regulation occurs.  Besides empty or null, values found in the EcoCyc PGDB include: 
	:COMPETITIVE
	:OTHER
	:IRREVERSIBLE
	:NONCOMPETITIVE
	:ALLOSTERIC
	:UNCOMPETITIVE
	:MRNA-DEGRADATION
	:TRANSLATION-BLOCKING
	@return the mechanism of this Regulation
	*/
	public String getMechanism()
	throws PtoolsErrorException {
		return this.getSlotValue("MECHANISM");
	}
	
	/**
	Get the mode by which this Regulation occurs.  Besides empty or null, values are either '+' or '-', indicating positive and negative regulation, respectively. 
	@return the mode of this Regulation
	*/
	public Boolean getMode()
	throws PtoolsErrorException {
		String m = this.getSlotValue("MODE");
		if(m==null) return null;
		else if(m.contains("+")) return true;
		else if(m.contains("-")) return false;
		else return null;
	}
	
	public void setRegulator(Frame f) throws PtoolsErrorException
	{
		this.addSlotValue("REGULATOR", f.getLocalID());
	}
	
	public void setRegulatee(Frame f) throws PtoolsErrorException
	{
		this.addSlotValue("REGULATED-ENTITY", f.getLocalID());
	}
	
	public void setMode(boolean m) throws PtoolsErrorException
	{
		this.addSlotValue("MODE", m ? "\"+\"" : "\"-\"");
	}
	
	public void setMechanism(String m) throws PtoolsErrorException
	{
		this.addSlotValue("MECHANISM", m);
	}
}
