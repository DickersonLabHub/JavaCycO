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

import java.util.HashMap;

/**
A HashMap with abstracted get methods.  Not a Frame.
@author John Van Hemert
*/
@SuppressWarnings("serial")
public class OrgStruct extends HashMap<String,String>
{
	
	public OrgStruct()
	{

	}

	static OrgStruct load(JavacycConnection c, String orgID)
	throws PtoolsErrorException {
		OrgStruct org = new OrgStruct();
		org.put(":ORGANISM", orgID);
		String orgName = c.callFuncString("org-name :org '"+orgID,false);
		org.put(":SPECIES-NAME", orgName);
		return org;
	}
	
	public String getSpecies()
	{
		String name = this.get(":SPECIES-NAME");
		if(name.startsWith("\"")) name = name.substring(1);
		if(name.endsWith("\"")) name = name.substring(0,name.length()-1);
		return name;
	}
	
	public String getLocalID()
	{
		return this.get(":ORGANISM");
	}
	
	public void print()
	{
		for(String o : this.keySet())
		{
			System.out.println(o+" --> "+this.get(o));
		}
		System.out.println("");
	}
	
}