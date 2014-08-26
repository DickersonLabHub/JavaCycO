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


public class Test {

	public static void main(String[] args) {
		Long start = System.currentTimeMillis();
		JavacycConnection conn = new JavacycConnection("localhost", 4444);
		conn.selectOrganism("ECOLI");
		try {
			Pathway pwy = (Pathway) Pathway.load(conn, "GLYCOLYSIS-TCA-GLYOX-BYPASS");
			for (Reaction rxn : pwy.getReactions()) {
				System.out.println(rxn.getLocalID());
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Caught a "+e.getClass().getName()+". Shutting down...");
		}
		finally {
			conn.close();
		}
		
		Long stop = System.currentTimeMillis();
		Long runtime = (stop - start) / 1000;
		System.out.println("Runtime is " + runtime + " seconds.");
	}
}
