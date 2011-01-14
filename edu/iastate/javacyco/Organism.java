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
Not used much.  The comment for the PGDB class:
"The Organisms class is used in different ways in organism-specific PGDBs versus in multiorganism PGDBs such as MetaCyc. The next paragraph discusses what is common to both types of PGDBs. Subsequent paragraphs describe the differences. In all PGDBssubclasses of Organisms define biological taxaat all possible taxonomic levels. Class-subclass relationships between subclasses of Organisms describe their taxonomic relationshipssincefor examplethe class Bacteria includes as a subclass the class Alphaproteobacteria. Generallymost of the taxonomic groups under Organisms correspond to entries from the NCBI Taxonomy databasewhich is stored in its entirety in a separate Ocelot KB. But in addition to taxa from NCBI Taxonomya PGDB can contain subclasses for additional taxa that are not present in the NCBI Taxonomy. Organism-specific PGDBs: In an organism-specific PGDBthe only frames that exist as children of Organisms are those frames needed to describe the taxonomic lineage of the organism described by the PGDB. An organism-specific PGDB contains a single instance frame that describes information about the PGDB itself. A parent class P of that instance must exist to describe the lowest taxonomic group defined for the organism. Additional parent classes exist as parents of P and children of Organisms that describe the other known taxonomic parents of P. No other children of Organisms exist in the PGDB. Multiorganism PGDBs such as MetaCyc: Multiorganism PGDBs contain no instances of class Organismsbut only subclasses of this class. Those subclasses define each of the different organisms for which MetaCycfor exampledefines pathways and enzymes. For economy of storageonly those taxaand their parent taxaactually referenced in the PGDB are stored in the PGDBso that only a subset of the NCBI Taxonomy is replicated in the PGDB. There is only one instance of Multi-Organism-Groupings that describes the properties of the PGDB."
@author John Van Hemert
*/
public class Organism extends Frame {

	/**
	|Organisms|
	*/
	public static String GFPtype = "|Organisms|";
	
	public Organism(JavacycConnection c, String id) {
		super(c, id);
	}
	
	/**
	 Prints all pathways for the current organism to tab delimited files. Also calls loadNetworkPathwayInfo
	 @throws PtoolsErrorException
	 */
	public void printPathwayNetwork() throws PtoolsErrorException {
		Network net = new Network("all_pathways_net");
		conn.selectOrganism(ID);
        for(Pathway p : Pathway.all(conn)) net.importNetwork(p.getNetwork());
        net.loadNetworkPathwayInfo();
        net.printTab();
	}

}
