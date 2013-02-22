package edu.iastate.javacyco;

import java.util.ArrayList;

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
A Promoter is a associated with a TranscriptionUnit.  It is Influeneable.
@author John Van Hemert
*/
@SuppressWarnings({"rawtypes"})
public class Promoter extends Influenceable {

	/**
	|Promoters|
	*/
	public static String GFPtype = "|Promoters|";
	
	public Promoter(JavacycConnection c, String id)
	{
		super(c, id);
	}
	
	/**
	Get the Protein which acts as the sigma factor for transcription initiation
	@return the Protein which acts as the sigma factor for transcription initiation
	*/
	public Protein getSigmaFactor()
	throws PtoolsErrorException {
		return (Protein)Frame.load(conn,this.getSlotValue("BINDS-SIGMA-FACTOR"));
	}
	
	/**
	Get the Protein which acts as the sigma factor for transcription initiation
	@return the Protein which acts as the sigma factor for transcription initiation
	*/
	public ArrayList transcriptionUnitsOfPromoter()
	throws PtoolsErrorException {
		return conn.transcriptionUnitsOfPromoter(ID);
	}
	
}
