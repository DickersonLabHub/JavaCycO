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
//		JavacycConnection connection = new JavacycConnection("tht.vrac.iastate.edu",4444);
//		connection.selectOrganism("ECOLI");
		
//		JavacycConnection connection = new JavacycConnection("localhost", 4444);
//		connection.selectOrganism("MAIZE");
		
		JavacycConnection connection = new JavacycConnection("localhost", 4444);
//		connection.selectOrganism("CORN");
		connection.selectOrganism("MAIZE");
//		connection.selectOrganism("ECOLI");
		try {
//			connection.getAllGFPInstances("|All-Genes|");
//			connection.getClassAllInstances("|All-Genes|");
//			System.out.println("FrameID\tCommonName\tACCESSION-1\tACCESSION-2\tABBREV-NAME\tSYNONYMS");
//			for (Frame f : connection.getAllGFPInstances("|All-Genes|")) {
//				Gene gene = (Gene)f;
////				gene.print();
//				
//				System.out.print(gene.getLocalID() + "\t" + gene.getCommonName() + "\t" + gene.getSlotValue("ACCESSION-1") + "\t" + gene.getSlotValue("ACCESSION-2") + "\t" + gene.getSlotValue("ABBREV-NAME") + "\t");
//				String syn = "";
//				for (String value : (ArrayList<String>) gene.getSlotValues("SYNONYMS")) syn += value + ",";
//				if (syn.length() > 0) System.out.print(syn.substring(0, syn.length()-1));
//				System.out.println();
////				if (gene.getProducts().size() == 0) System.err.println(gene.getLocalID() + " : No product.");
////				else {
////					if (gene.getProducts().size() > 1) {
////						System.err.println(gene.getLocalID() + " : More than one product.");
////						String s = "";
////						for (Frame product : gene.getProducts()) s += product.getLocalID() + ", ";
////						System.err.println("\t" + s.substring(0, s.length()-2));
////					}
////					Frame product = Frame.load(connection, gene.getProducts().get(0).getLocalID());
////					if (!product.isGFPClass("|Proteins|")) System.err.println(gene.getLocalID() + " : Product not a protein.");
////				}
//			}
			
			
//			String encodedDate = connection.callFuncString("encode-universal-time 34 17 13 6 10 2011", false);
//			System.out.println(encodedDate);
//			System.out.println(connection.callFuncString("multiple-value-list (decode-universal-time " + encodedDate + ")", false));
			
//			for (Frame f : connection.getAllGFPInstances("|Genes|")) {
//				System.out.println(f.getLocalID() + " :: " + f.getCommonName());
//			}
//			
//			
//			Frame.load(connection, "GDQC-107985").print();
//			Frame.load(connection, "GDQC-107985-MONOMER").print();
			
//			Frame.load(connection, "GRMZM2G000278_T01").print();
//			Frame.load(connection, "GRMZM2G000278_P01").print();
			
			
			
			
			
			
			
			
			
			
			//updateFrame(connection);
//			connection.close();
//			connection.testConnection();
//			Frame frame = Frame.load(connection, "|Proteins|");
//			Frame frame = Frame.load(connection, "|Peptides|");
			
			
			
//			ArrayList<String> go = new ArrayList<String>();
//			go.add("|GO:0001906|");
//			go.add("|GO:0000036|");
//			go.add("|GO:0004022|");
//			connection.putSlotValues("GDQC-106529-MONOMER", "GO-TERMS", JavacycConnection.ArrayList2LispList(go));
//			connection.importGOTerms(go);
//			ArrayList<String> values = new ArrayList<String>();
//			values.add("format nil \"~{~A~#[~:;:~]~}\" '(2987807 EV-EXP-IMP 3501259540 Jesse)");
//			values.add("\"149128:EV-COMP:3501259540:keseler\"");
//			values.add("2987807\\:EV-COMP\\:3501259540\\:Jesse");
//			connection.putAnnotation("GDQC-106529-MONOMER", "GO-TERMS", "|GO:0001906|", "CITATIONS", "\"19131630:EV-EXP-IDA:19131630:keseler\"");
//			connection.putAnnotation("GDQC-106529-MONOMER", "GO-TERMS", "|GO:0000036|", "CITATIONS", "\"149128:EV-COMP:3501259540:keseler\"");
//			connection.putAnnotation("GDQC-106529-MONOMER", "GO-TERMS", "|GO:0004022|", "CITATIONS", "\"6328449:EV-COMP:3501259540:keseler\"");
//			Frame frame = Frame.load(connection, "CPLX0-7715");
//			frame.print();
			
//			for (Frame f : connection.getAllGFPInstances("|Terminators|")) System.out.println(f.getLocalID());
					
//			for (Frame f : connection.getAllGFPInstances("|Polypeptides|")) System.out.println(f.getLocalID() + " :: " + f.getSlotValue("DNA-FOOTPRINT-SIZE"));
//			for (Frame f : connection.getAllGFPInstances("|Protein-Complexes|")) System.out.println(f.getLocalID() + " :: " + f.getSlotValue("DNA-FOOTPRINT-SIZE"));
//			for (Frame f : connection.getAllGFPInstances("|Protein-Small-Molecule-Complexes|")) System.out.println(f.getLocalID() + " :: " + f.getSlotValue("DNA-FOOTPRINT-SIZE"));
					
//			for (Frame fr : connection.getAllGFPInstances("|Genes|")) {
//				try {
//					if (connection.getClassAllSupers(fr.getLocalID()).contains("|Unclassified-Genes|")) System.out.println(fr.getLocalID());
//				} catch (Exception ex) {
//					
//				}
//			}
			
			
//			System.out.println(Frame.load(connection, "|D-Glucose|").isClassFrame());
//			frame = Frame.load(connection, "NADH-DEHYDROG-A-RXN");
//			frame.print();
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
}
