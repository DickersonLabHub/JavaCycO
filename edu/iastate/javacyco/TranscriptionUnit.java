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
TranscriptionUnits are the polycistronic groups of genes which are regulated together by the same promoter and/or enhancers.
@author John Van Hemert
*/
public class TranscriptionUnit extends Influenceable {

	/**
	|Transcription-Units|
	*/
	public static String GFPtype = "|Transcription-Units|";
	
	public TranscriptionUnit(JavacycConnection c, String id)
	{
		super(c, id);
	}

	/**
	Get the Genes which make up this TranscriptinoUnit.
	@return this TranscriptionUnit's Genes.
	*/
	public ArrayList<Gene> getGenes()
	throws PtoolsErrorException {
		ArrayList<Gene> rst = new ArrayList<Gene>();
		ArrayList<String> ids = conn.transcriptionUnitGenes(ID);
		for(String id : ids)
		{
			rst.add((Gene)Gene.load(conn,id));
		}
		return rst;
	}
	
	/**
	Get the Promoter which controls transcription of this TranscriptionUnit.
	@return this TranscriptionUnit's Promoter.
	*/
	public Promoter getPromoter()
	throws PtoolsErrorException {
		String id = conn.transcriptionUnitPromoter(ID);
		return id.equals("NIL") ? null : (Promoter)Promoter.load(conn,id);
	}
	
	/**
	Get the Frames which affect transcription of this TranscriptionUnit.
	@return this TranscriptionUnit's transcription factors.
	*/
	public ArrayList<Frame> getTranscriptionFactors()
	throws PtoolsErrorException {
		ArrayList<Frame> rst = new ArrayList<Frame>();
		ArrayList<String> ids = conn.transcriptionUnitTranscriptionFactors(ID);
		for(String id : ids)
		{
			rst.add(Frame.load(conn,id));
		}
		return rst;
	}
}
