/**
 *  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.iastate.pathwayaccess.celldesigner;

import java.util.HashMap;

/**
Stores relations or 2-way mappings between different strings.  It is basically a two-way hashmap.
For example, if your datasource uses the word 'polypeptide' to describe the same things as CellDesigner's PluginSpeciesSymbolType.PROTEIN, 
add a Relation record to typeMap of the pair in either order.  Then, refer to typeMap to convert datasource type strings to CellDesigner type strings 
and vice versa; typeMap.get("polypeptide") returns PluginSpeciesSymbolType.PROTEIN and typeMap.get(PluginSpeciesSymbolType.PROTEIN) returns "polypeptide".
 */
public class Relation extends HashMap<String,String> {

	/**
	A default strings to return if no match is found in the Relation.
	 */
	private String deft;
	
	/**
	Initializes deft to an empty string, ""
	 */
	public Relation()
	{
		deft = "";
	}
	
	/**
	Stores 2-way mapping.
	@param f 'from,' one string of the mapping
	@param t 'to,' the other string
	 */
	public void addMapping(String f,String t)
	{
		this.put(f,t);
		this.put(t,f);
	}
	
	/**
	Set the default string to be returned with no mapping match is found.
	@param d the new default string.
	 */
	public void setDefault(String d)
	{
		deft = d;
	}
	
	/**
	Gets the mapped stringed from a string.
	@param key the string whose mapping parnter is to be looked up.
	@return the mapped string if key exists in the relation.  else returns deft, the default string.
	 */
	public String get(String key)
	{
		if(this.containsKey(key))
			return super.get(key);
		else
			return deft;
	}

}
