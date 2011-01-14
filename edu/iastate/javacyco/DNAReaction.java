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
A Reaction in which DNA plays a role.
@author John Van Hemert
*/
public class DNAReaction extends EnzymeReaction {

	/**
	|DNA-Reactions|
	*/
	public static String GFPtype = "|DNA-Reactions|";
	
	public DNAReaction(JavacycConnection c, String id) {
		super(c, id);
		// TODO Auto-generated constructor stub
	}
	
	/**
	Get all the DNA Reactions in the PGDB.
	@return an ArrayList of all Reactions which are DNA Reactions.
	*/
	static public ArrayList<Reaction> all(JavacycConnection c)
	throws PtoolsErrorException {
		return Reaction.all(c,Reaction.DNA);
	}

}
