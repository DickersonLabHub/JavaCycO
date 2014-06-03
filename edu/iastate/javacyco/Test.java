package edu.iastate.javacyco;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.swing.table.DefaultTableModel;

import org.w3c.dom.Element;

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

	@SuppressWarnings({ "unchecked", "unused" })
	public static void main(String[] args) {
		Long start = System.currentTimeMillis();
//		JavacycConnection connection = new JavacycConnection("tht.vrac.iastate.edu",4444);
//		connection.selectOrganism("ECOLI");
		
//		JavacycConnection connection = new JavacycConnection("localhost", 4444);
//		connection.selectOrganism("MAIZE");
		
		JavacycConnection conn = new JavacycConnection("jrwalsh.student.iastate.edu", 4444);
//		connection.selectOrganism("CORN");
//		connection.selectOrganism("MAIZE");
//		connection.selectOrganism("Meta");
//		conn.selectOrganism("ECOLI");
		conn.selectOrganism("MANUALTEST");
		try {
			conn.selectOrganism("Meta");
			
			conn.callFuncText("biovelo \"[x^names:x<-corn^^genes]\"", false);
			
//			conn.search("geranylgeranyl hydrogenase", "|Proteins|");
//			for (Frame fra : conn.search("geranylgeranyl hydrogenase", "|Proteins|")) {
//				System.out.println(fra.getLocalID());
//			}
			
//			Frame fr = Frame.load(conn, "|ODQC-1|");
//			conn.callFuncArray("add-slot-value '" + "GDQC-114480-MONOMER" + " '" + "COMMENT" + " '" + "\"Hello World\"");
//			conn.addSlotValue("GDQC-114480-MONOMER", "COMMENT", "\"Hello World\"");
//			System.out.println(conn.getSlotValues("GDQC-114480-MONOMER", "CREDITS"));
//			conn.addSlotValue("GDQC-114480-MONOMER", "CREDITS", fr.getLocalID());
//			conn.putAnnotation("GDQC-114480-MONOMER", "CREDITS", fr.getLocalID(), "REVISED", "3536671559");
			//conn.addCredit("GDQC-114480-MONOMER", "|sen|", "|ODQC-1|");
//			conn.addCredit("ENZRXNDQC-140169", "", "|ODQC-1|");
//			Frame fr = Frame.load(conn, "GDQC-114480-MONOMER");
//			Frame fr = Frame.load(conn, "CPLX0-7877");
//			Frame cur = Frame.load(conn, "|tran|");
//			conn.callFuncString("add-revised-credit '" + fr.getLocalID() + " :author '" + cur.getLocalID() + " :force? 'T", true);
			if (true) return;
			
			HashMap<String, String> regulatees = new HashMap<String, String>();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new FileReader("C:\\Users\\Jesse\\Desktop\\NewLinksMapRegulatees.csv"));
				String text = null;
				while ((text = reader.readLine()) != null) {
					try {
						String[] textSplit = text.split(",");
						regulatees.put(textSplit[0], textSplit[1]);
//						if (textSplit.length == 3) {
//							regulatees.put(textSplit[1], textSplit[3]);
//						}
					} catch (Exception e) {
						System.err.println("Error");
					}
				}
			} catch (FileNotFoundException exception) {
				exception.printStackTrace();
			} catch (IOException exception) {
				exception.printStackTrace();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			finally {
				try {
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
			
			HashMap<String, String> regulators = new HashMap<String, String>();
			BufferedReader reader2 = null;
			try {
				reader2 = new BufferedReader(new FileReader("C:\\Users\\Jesse\\Desktop\\NewLinksMapRegulators.csv"));
				String text = null;
				while ((text = reader2.readLine()) != null) {
					try {
						String[] textSplit = text.split(",");
						regulators.put(textSplit[0], textSplit[1]);
					} catch (Exception e) {
						System.err.println("Error");
					}
				}
			} catch (FileNotFoundException exception) {
				exception.printStackTrace();
			} catch (IOException exception) {
				exception.printStackTrace();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			finally {
				try {
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				try {
					if (reader2 != null) {
						reader2.close();
					}
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
			
			BufferedReader reader3 = null;
			try {
				reader3 = new BufferedReader(new FileReader("C:\\Users\\Jesse\\Desktop\\NewLinks.txt"));
				String text = null;
				while ((text = reader3.readLine()) != null) {
					try {
						String[] textSplit = text.split("\t");
						String regulator = regulators.get(textSplit[0]);
						String regulatee = regulatees.get(textSplit[1]);
						String mode = textSplit[2];
						
						Gene regulatorGene = (Gene) Gene.load(conn, Protein.load(conn, regulator).getSlotValue("Gene"));
						ArrayList<String> regulatedGenes = (ArrayList<String>) conn.callFuncArray("genes-regulated-by-gene '" + regulatorGene.getLocalID());
						if (regulatedGenes.contains(regulatee)) System.out.println("Match regulator " + regulator + " to regulatee " + regulatee);
						else System.out.println("No match for regulator " + regulator + " to regulatee " + regulatee);
						
					} catch (Exception e) {
						System.err.println("Error");
					}
				}
			} catch (FileNotFoundException exception) {
				exception.printStackTrace();
			} catch (IOException exception) {
				exception.printStackTrace();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
			finally {
				try {
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				try {
					if (reader3 != null) {
						reader3.close();
					}
				} catch (IOException exception) {
					exception.printStackTrace();
				}
			}
			
			/* Commit new regulation
			String regulationFrameID = conn.callFuncString("create-instance-w-generated-id '" + "|Regulation-of-Transcription|"); 
			Frame regulation = Frame.load(conn, regulationFrameID);
			//Frame regulator = Frame.load(conn, "G7326-MONOMER");
			Frame regulator = Frame.load(conn, "G7326");
			Frame regulatee = Frame.load(conn, "EG11530");
			regulation.putSlotValue("regulator", regulator.getLocalID());
			regulation.putSlotValue("regulated-entity", regulatee.getLocalID());
			regulation.putSlotValue("mode", "\"+\"");
			
			regulation.commit();
			regulator.commit();
			regulatee.commit();
			*/
			
			/* Gene deletion strategy... needs proof that it takes care of regulation as well.
			Gene gene = (Gene) Gene.load(conn, "EG11530");
			for (Object enzymeName : conn.enzymesOfGene("EG11530")) {
				Frame enzyme = Frame.load(conn, enzymeName.toString());
				for (Object catalyzedReaction : enzyme.getSlotValues("CATALYZES")) {
					conn.deleteFrameAndDependents(catalyzedReaction.toString());
				}
			}
			for (Object product: conn.allProductsOfGene("EG11530")) {
				if (conn.frameExists(product.toString())) conn.deleteFrameAndDependents(product.toString());
				else System.out.println("Frame already deleted: " + product.toString());
			}
			conn.deleteFrameAndDependents(gene.getLocalID());
			*/
			
			
			
			
//			JavacycConnection conn = new JavacycConnection("jrwalsh.student.iastate.edu", 4444);
//			conn.selectOrganism("Ecoli");
//			
//			for (Object frame : conn.allPathways()) {
////				Pathway pwy = (Pathway) Pathway.load(conn, "GLYCOLYSIS-TCA-GLYOX-BYPASS");
//				Pathway pwy = (Pathway) Pathway.load(conn, (String) frame);
//				for (Reaction rxn : pwy.getReactions()) {
//					System.out.println(rxn.getLocalID() + "," + frame);
//				}
//			}		
			
			
			
			
			
			
//			getTwoModeAdjacencyMatrix(conn.getReactionList("GLYCOLYSIS-TCA-GLYOX-BYPASS"), conn);
			
			
//			for (Object s : connection.callFuncArray("get-value-annots 'GBWI-69554-MONOMER 'GO-TERMS '|GO:0003899| 'CITATIONS"))
//				System.out.println(s.toString());
			
			
			
			// Lets test creating new frames
//			Frame f = Frame.load(connection, "GDQC-104328-MONOMER");
//			Frame newFrame = new Frame(connection, "GDQC-104328-MONOMERS");
//			newFrame.create(connection, "|Polypeptides|", newFrame.getLocalID());
			//
			
//			System.out.println(connection.callFuncString("do-frame-slots (slot GLC-6-P) (setf len (+ len 1))", false));
			
			
//			Frame f = Frame.load(connection, "GLC-6-P");
//			f.getSlotValue("COMMON-NAME");
//			f.print();
//			f.update();
//			f.loadFromKB();
			
			//Try Array
//			String result = "";
//			ArrayList<String> resultArray = connection.callFuncArray("with-output-to-string (*standard-output*) (print-frame 'GLC-6-P)", false);
//			for (String s : resultArray) result += s;
//			System.out.println();
			
			//Try String
//			System.out.println(connection.callFuncString("with-output-to-string (*standard-output*) (print-frame 'GLC-6-P)", false));
			
			//Try Text
//			ArrayList<String> results = connection.callFuncText("with-output-to-string (*standard-output*) (print-frame 'GLC-6-P)", false);
//			for (String result : results) System.out.println(result);
			
//			ArrayList<String> results = connection.callFuncText("with-output-to-string (*standard-output*) (print-frame 'GDQC-104328-MONOMER)", true);
//			for (String result : results) System.out.println(result);
			
			
//			Frame f2 = f.copy(f.getLocalID());
//			
//			ArrayList<String> list = new ArrayList<String>();
//			list.add("testValue");
//			f.putLocalSlotValueAnnotations("COMMON-NAME", f.getCommonName(), "test", list);
//			
//			ArrayList<String> list2 = new ArrayList<String>();
//			list2.add("test2Value");
//			f2.putLocalSlotValueAnnotations("COMMON-NAME", f2.getCommonName(), "test", list2);
//			System.out.println(f.equalBySlotValues(f2));
			
//			System.out.println(connection.isCurrentKBModified());
			
			
//			String string = "ENZRXNDQC-131607$ENZRXNDQC-131606";
//			String pat = "$";
//			String pattern = Pattern.quote(pat);
//			String[] array = string.split(pattern);
//			for (String s : array) {
//				System.out.println(s);
//			}
//			JavacycConnection conn = new JavacycConnection("jrwalsh.student.iastate.edu", 4444);
//			conn.selectOrganism("Ecoli");
//			getSMatrix(conn.getReactionList("GLYCOLYSIS-TCA-GLYOX-BYPASS"), conn);
//			getSMatrix(conn.allRxns(), conn);
//			getMetaboliteAdjacencyMatrix(conn.getReactionList("GLYCOLYSIS-TCA-GLYOX-BYPASS"), conn, false);
//			getMetaboliteAdjacencyMatrix(conn.allRxns(), conn);
			
			
//			compare();
			
			
			
			
//			Frame f = Frame.load(connection, "CHEMICAL-FORMULA");
//			f.print();
//			3-OXOACYL-ACP-SYNTH-RXN
//			3-OXOACYL-ACP-REDUCT-RXN
//			ENOYL-ACP-REDUCT-NADH-RXN
//			ENOYL-ACP-REDUCT-NADPH-RXN
//			3-HYDROXYDECANOYL-ACP-DEHYDR-RXN
			
			
//			System.out.println(connection.search("Glucose", Reaction.GFPtype).size());
			
//			for (Frame f : connection.search("Glycolysis",  Pathway.GFPtype)) {
//				System.out.println(f.getCommonName());
//			}
			
//			printFramesToCSV();
			
			
//			connection.getClassHierarchy(true).printStructureTab();
			
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
			conn.close();
		}
		
		Long stop = System.currentTimeMillis();
		Long runtime = (stop - start) / 1000;
		System.out.println("Runtime is " + runtime + " seconds.");
	}
	
	// Creates a matrix with rows/columns as metabolites.  For each reaction, make an edge from reactant to all products.  If directed = false, also make an edge
	// from products back to reactants.  This does not do anything to eliminate hub node issues.  Consider pruning edges.
	private static void getMetaboliteAdjacencyMatrix(ArrayList<String> reactions, JavacycConnection conn, boolean directed) throws PtoolsErrorException {
		HashMap<String, Integer> metaboliteList = new HashMap<String, Integer>();
		int meaboliteListIndex = 1;
		
		System.out.println("MetaboliteID\tMetaboliteID\tAdjacent");
		
		for (String reactionLabel : reactions) {
			Reaction reaction = (Reaction) Reaction.load(conn,  reactionLabel);
			
			for (Frame metabolite : reaction.getReactants()) {
				if (metaboliteList.containsKey(metabolite.getLocalID())) {
				} else {
					metaboliteList.put(metabolite.getLocalID(), meaboliteListIndex);
					meaboliteListIndex++;
				}
			}
			for (Frame metabolite : reaction.getProducts()) {
				if (metaboliteList.containsKey(metabolite.getLocalID())) {
				} else {
					metaboliteList.put(metabolite.getLocalID(), meaboliteListIndex);
					meaboliteListIndex++;
				}
			}
			
			for (Frame reactant : reaction.getReactants()) {
				for (Frame product : reaction.getProducts()) {
					System.out.println(metaboliteList.get(reactant.getLocalID()) + "\t" + metaboliteList.get(product.getLocalID()) + "\t" + 1);
					if (!directed)
						System.out.println(metaboliteList.get(product.getLocalID()) + "\t" + metaboliteList.get(reactant.getLocalID()) + "\t" + 1);
				}
			}
		}
		
		System.out.println("\n\nMetabolite\tID");
		for (String key : metaboliteList.keySet()) {
			System.out.println(key + "\t" + metaboliteList.get(key));
		}
		
	}
	
	// Create S matrix in normal (not sparse) form
	private static void getTwoModeAdjacencyMatrix(ArrayList<String> reactions, JavacycConnection conn) throws PtoolsErrorException {
		ArrayList<String> reactionList = new ArrayList<String>();
		ArrayList<String> metaboliteList = new ArrayList<String>();
		
		// Just collect all reaction/metabolite labels so we know the size of our matrix and the column/row names
		for (String reactionLabel : reactions) {
			reactionList.add(reactionLabel);
			Reaction reaction = (Reaction) Reaction.load(conn,  reactionLabel);
			for (Frame metabolite : reaction.getReactants()) {
				if (!metaboliteList.contains(metabolite.getLocalID())) metaboliteList.add(metabolite.getLocalID());
			}
			
			for (Frame metabolite : reaction.getProducts()) {
				if (!metaboliteList.contains(metabolite.getLocalID())) metaboliteList.add(metabolite.getLocalID());
			}
		}
		
		for (String reactionLabel : reactionList) {
			Reaction reaction = (Reaction) Reaction.load(conn,  reactionLabel);
			
			ArrayList<String> reactants = new ArrayList<String>();
			for (Frame metabolite : reaction.getReactants()) {
				reactants.add(metabolite.getLocalID());
			}
			
			ArrayList<String> products = new ArrayList<String>();
			for (Frame metabolite : reaction.getProducts()) {
				products.add(metabolite.getLocalID());
			}
			
			System.out.print(reactionLabel);
			for (String metaboliteLabel : metaboliteList) {
				System.out.print("\t");
				if (reactants.contains(metaboliteLabel)) System.out.print(-1);
				else if (products.contains(metaboliteLabel)) System.out.print(1);
				else System.out.print(0);
			}
			System.out.println();
		}
	}
		
	// Creates a stroichiometric matrix for the given reactions.  Reactions as rows, metabolites as columns.  When a reaction contains a reactant, a negative value equal to the
	// metabolite coefficient is used.  When reaction contains a product, a positive value equal to the coefficient is used.  Does not yet handle reversible reactions, which should
	// be done be creating a duplicate version of the reaction.
	@SuppressWarnings("unchecked")
	private static void getSMatrix(ArrayList<String> reactions, JavacycConnection conn) throws PtoolsErrorException {
		HashMap<String, Integer> metaboliteList = new HashMap<String, Integer>();
		HashMap<String, Integer> reactionList = new HashMap<String, Integer>();
		int meaboliteListIndex = 0;
		int reactionListIndex = 0;
		
		System.out.println("MetaboliteID\tReactionID\tCoefficient");
		
		for (String reactionLabel : reactions) {
			Reaction reaction = (Reaction) Reaction.load(conn,  reactionLabel);
			reactionList.put(reactionLabel, new Integer(reactionListIndex));
			reactionListIndex++;
			
			for (Frame metabolite : reaction.getReactants()) {
				int metabolitePosition = 0;
				if (metaboliteList.containsKey(metabolite.getLocalID())) metabolitePosition = metaboliteList.get(metabolite.getLocalID());
				else {
					metaboliteList.put(metabolite.getLocalID(), meaboliteListIndex);
					metabolitePosition = meaboliteListIndex;
					meaboliteListIndex++;
				}
				int coefficient = 1;
				try {
					coefficient = Integer.parseInt(conn.getValueAnnot(reactionLabel, "LEFT", metabolite.getLocalID(), "COEFFICIENT"));
				} catch (NumberFormatException e) {
				}
				System.out.println(metabolitePosition + "\t" + reactionListIndex + "\t" + coefficient);
			}
			for (Frame metabolite : reaction.getProducts()) {
				int metabolitePosition = 0;
				if (metaboliteList.containsKey(metabolite.getLocalID())) metabolitePosition = metaboliteList.get(metabolite.getLocalID());
				else {
					metaboliteList.put(metabolite.getLocalID(), meaboliteListIndex);
					metabolitePosition = meaboliteListIndex;
					meaboliteListIndex++;
				}
				int coefficient = 1;
				try {
					coefficient = Integer.parseInt(conn.getValueAnnot(reactionLabel, "RIGHT", metabolite.getLocalID(), "COEFFICIENT"));
				} catch (NumberFormatException e) {
				}
				System.out.println(metabolitePosition + "\t" + reactionListIndex + "\t" + (0-coefficient));
			}
		}
		
		System.out.println("\n\nMetabolite\tID");
		for (String key : metaboliteList.keySet()) {
			System.out.println(key + "\t" + metaboliteList.get(key));
		}
		System.out.println("\n\nReaction\tID");
		for (String key : reactionList.keySet()) {
			System.out.println(key + "\t" + reactionList.get(key));
		}
	}
	
	
	private static void compare() throws PtoolsErrorException {
		JavacycConnection conn = new JavacycConnection("jrwalsh.student.iastate.edu", 4444);
		
		String org1 = "CORN";
		String org2 = "MAIZE";
		String frameID = "GDQC-104328-MONOMER";
		conn.selectOrganism(org1);
		Frame reaction1 = Frame.load(conn, frameID);
		conn.selectOrganism(org2);
		Frame reaction2 = Frame.load(conn, "GBWI-69920-MONOMER");
		
		conn.selectOrganism(org1);
		HashSet<String> slotLabels1 = new HashSet<String>(reaction1.getSlotLabels());
		conn.selectOrganism(org2);
		HashSet<String> slotLabels2 = new HashSet<String>(reaction2.getSlotLabels());
		
		if (!slotLabels1.containsAll(slotLabels2) || !slotLabels2.containsAll(slotLabels1))
			System.out.println("Slot labels are not equal");
		
		for (String slotLabel : slotLabels1) {
			conn.selectOrganism(org1);
			HashSet<String> slotValues1 = new HashSet<String>(reaction1.getSlotValues(slotLabel));
			conn.selectOrganism(org2);
			HashSet<String> slotValues2 = new HashSet<String>(reaction2.getSlotValues(slotLabel));
			
			if (!slotValues1.containsAll(slotValues2) || !slotValues2.containsAll(slotValues1))
				System.out.println("Slot values for slot " + slotLabel + " are equal: " + slotValues1.containsAll(slotValues2) + " " + slotValues2.containsAll(slotValues1));
			
			for (String value : slotValues1) {
				conn.selectOrganism(org1);
				HashSet<String> annotationLabels1 = new HashSet<String>(reaction1.getAllAnnotLabels(slotLabel, value));
				conn.selectOrganism(org2);
				HashSet<String> annotationLabels2 = new HashSet<String>(reaction2.getAllAnnotLabels(slotLabel, value));
				
				if (!annotationLabels1.containsAll(annotationLabels2) || !annotationLabels2.containsAll(annotationLabels1))
					System.out.println("Annotation labels are not equal");
				
				for (String annotationLabel : annotationLabels1) {
					conn.selectOrganism(org1);
					HashSet<String> annotationValues1 = new HashSet<String>(reaction1.getAnnotations(slotLabel, value, annotationLabel));
					conn.selectOrganism(org2);
					HashSet<String> annotationValues2 = new HashSet<String>(reaction2.getAnnotations(slotLabel, value, annotationLabel));
				
					if (!annotationValues1.containsAll(annotationValues2) || !annotationValues2.containsAll(annotationValues1))
						System.out.println("Annotation values for slot:" + slotLabel + " value:" + value + " annotation:" + annotationLabel + " are equal: " + annotationValues1.containsAll(annotationValues2) + " " + annotationValues2.containsAll(annotationValues1));
				}
			}
		}
	}

	public static void printFramesToCSV() throws PtoolsErrorException {
		JavacycConnection conn = new JavacycConnection("jrwalsh.student.iastate.edu", 4444);
		conn.selectOrganism("META");
		
		String slotDelimiter = "\t";
		String valueDelimiter = "$";
		
//		Frame frame = Frame.load(conn,  "G-14659");
		
		ArrayList<Frame> frames = new ArrayList<Frame>();
		frames.add(Frame.load(conn,  "G-14659"));
		frames.add(Frame.load(conn,  "G-14659"));
		frames.add(Frame.load(conn,  "G-14659"));
		frames.add(Frame.load(conn,  "G-14659"));
		
		TreeSet<String> slots = new TreeSet<String>();
		for (Frame frame : frames) {
			for (String slotLabel : frame.getSlots().keySet()) slots.add(slotLabel);
		}
		
		ArrayList<ArrayList<String>> rows = new ArrayList<ArrayList<String>>();
		for (Frame frame : frames) {
			ArrayList<String> row = new ArrayList<String>();
			
			// Format to CSV
			row.add(frame.getLocalID());
			
			for (String slotLabel : slots) {
				ArrayList<Object> slotValueObjects = frame.getSlotValues(slotLabel);
				String slotValue = "";
				for (Object slotValueObject : slotValueObjects) {
					if (slotValueObject instanceof ArrayList) {
						ArrayList<String> valueArray = (ArrayList<String>)slotValueObject;
						String valueString = "(";
						for (String value : valueArray) {
							valueString += value + " ";
						}
						if (valueArray.size() > 0) valueString = valueString.substring(0, valueString.length()-1);
						valueString += ")";
						row.add(valueString);
					} else {
						String value = (String) slotValueObject;
						slotValue += value + valueDelimiter;
					}	
				}
				if (slotValue.endsWith(valueDelimiter)) slotValue = slotValue.substring(0, slotValue.length()-1);
				row.add(slotValue);
			}
			rows.add(row);
		}
		
		System.out.print("FrameID\t");
		for (String slotLabel : slots) {
			System.out.print(slotLabel + "\t");
		}
		System.out.println();
		for (ArrayList<String> row : rows) {
			for (String string : row) {
				System.out.print(string + "\t");
			}
			System.out.println();
		}
	}
}
