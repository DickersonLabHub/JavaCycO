package edu.iastate.javacyco;

import java.util.ArrayList;

public class AnnotationMapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JavacycConnection connection = new JavacycConnection("vitis.student.iastate.edu",4444);//"vitis.student.iastate.edu",4444);
		String org = args.length>0 ? args[0] : "META";
		try {
			connection.selectOrganism(org);
			//for(Frame f : connection.getAllGFPInstances(Gene.GFPtype))
			Frame f = Frame.load(connection,"JGVV0.241");
			f.print();
			{
				Gene g = (Gene)f;
				if(g.getBNumber() != null && g.getBNumber().length()>0) System.out.println(g.getBNumber().replace("\"","")+" "+g.getLocalID());
				if(g.getECK() != null && g.getECK().length()>0) System.out.println(g.getECK().replace("\"","")+" "+g.getLocalID());
				for(Object dblObj : g.getSlotValues("DBLINKS"))
				{
					ArrayList dbl = (ArrayList)dblObj;
					System.out.println(dbl.get(1).toString().replace("\"","")+" "+g.getLocalID());
				}
			}
		} catch (PtoolsErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
