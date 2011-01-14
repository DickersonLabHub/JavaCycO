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
Small molecule reactions.
@author John Van Hemert
*/
public class SmallMoleculeReaction extends EnzymeReaction 
{
	/**
	|Small-Molecule-Reactions|
	*/
	public static String GFPtype = "|Small-Molecule-Reactions|";

	public SmallMoleculeReaction(JavacycConnection c, String id) {
		super(c, id);
	}
	
	/**
	Get all the Reactions of the small molecule reaction type.
	@return all the Reactions of the small molecule reaction type
	*/
	static public ArrayList<Reaction> all(JavacycConnection c)
	throws PtoolsErrorException {
		return Reaction.all(c,Reaction.SMALLMOLECULE);
	}

}
