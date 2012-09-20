package edu.iastate.javacyco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;


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

	@SuppressWarnings("static-access")
	public static void main(String[] args) {
//		JavacycConnection connection = new JavacycConnection("tht.vrac.iastate.edu",4444);
//		connection.selectOrganism("ECOLI");
		
//		JavacycConnection connection = new JavacycConnection("localhost",4444);
//		connection.selectOrganism("MAIZE");
		
		JavacycConnection connection = new JavacycConnection("localhost",4444);
		connection.selectOrganism("CBIRC");
		try {
			
			//updateFrame(connection);
			
//			Frame frame = Frame.load(connection, "|Proteins|");
//			Frame frame = Frame.load(connection, "|Peptides|");
			Frame frame = Frame.load(connection, "GLC-6-P");
			frame.print();
//			for (Frame f : frame.getDirectSuperClasses()) {
//				System.out.println(frame.getLocalID());
//			}
//			for (Object f : connection.callFuncArray("get-class-all-supers '" + frame.getLocalID())) {
//				System.out.println(f.toString());
//			}
//			for (Object f : connection.callFuncArray("get-class-direct-supers '" + frame.getLocalID())) {
//				System.out.println(f.toString());
//			}
			
			
			// Create a frame to have multiple inheritance
//			Frame frame = Frame.load(connection, "EG10700");
//			frame.print();
//			ArrayList test = new ArrayList();
//			test.add("TestValue");
//			frame.putLocalSlotValueAnnotations("LEFT-END-POSITION", "1804394", "TEST", test);
//			frame.commit();
//			frame.print();
//			ArrayList<String> types = new ArrayList<String>();
//			types.add("BC-1.1.1");
//			types.add("BC-7.1");
//			types.add("|Publications|");
//			connection.callFuncArray("put-instance-types '" + frame.getLocalID() + " '" + connection.ArrayList2LispList(types));
//			frame.print();

			
			
			
			// Where are citations stored (EcoCyc)
//			ArrayList<String> fs = connection.getClassAllInstances("|Publications|");
//			for (String f : fs) {
//				Frame frame = Frame.load(connection, f);
//				System.out.println(frame.getSlotValue("PUBMED-ID"));
//				try {
//					if (frame.getSlotValue("PUBMED-ID").toString().equalsIgnoreCase("\"6235149\"")) frame.print();
//				} catch (Exception e) {
//					//ignore
//				}
//			}
			
			
			
			
			// Gathering GO terms for MaizeGDB group
//			System.out.println("Genes\tProteinID\tProteinCommonName\tGO-TERMS");
//			ArrayList<String> proteinNames = connection.getClassAllInstances("|Polypeptides|");
//			for (String proteinName : proteinNames) {
//				String evidence = "";
//				Frame protein = Frame.load(connection, proteinName);
//				for (Object value : protein.getSlotValues("GO-TERMS")) {
//					String goTerm = value.toString();
//					evidence += goTerm + ":" + protein.getAnnotation("GO-TERMS", goTerm, "CITATIONS") + "\t";
//				}
//				System.out.println(protein.getSlotValues("GENE").toString() + "\t" + protein.getLocalID() + "\t" + protein.getCommonName() + "\t" + evidence);
//			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Caught a "+e.getClass().getName()+". Shutting down...");
		}
		finally {
			connection.close();
		}
	}
	
	public void submitItem(String fileName) {
		File tfLinks = new File(fileName);
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(tfLinks));
			String text = null;
			
			// Ignore Headers
			reader.readLine();
			
			while ((text = reader.readLine()) != null) {
				String[] line = text.split("\t");
				
//				updateFrame(connection);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
 	public static void updateFrame(JavacycConnection conn) throws PtoolsErrorException {
 		String frameID = "EG10700";
 		String geneFullName = "";
 		String CurrentGeneModelName = "";
 		String goId = "";
 		String goTerm = "";
 		String goEvidenceCode = "";
 		String pubmedId = "";
 		String citation = "";
 		String validation = "";
 		
 		if (!conn.frameExists(frameID)) {
 			System.err.println("Cannot update frame " + frameID + ". Frame does not exist.");
 			return;
 		}
 		
 		Gene gene = (Gene) Gene.load(conn, frameID);
 		gene.print();
 		
 		gene.putSlotValue("COMMON-NAME", "Hello");
// 		putSlotValue(slot, value);
// 		putSlotValue(slot, value);
// 		putSlotValue(slot, value);
// 		putSlotValue(slot, value);
 		
 		gene.commit();
 		
 		gene.print();
// 		"pfkB"
 	}
}
