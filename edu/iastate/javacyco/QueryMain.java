package edu.iastate.javacyco;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

public class QueryMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JavacycConnection connection = new JavacycConnection("vitis.student.iastate.edu",4444);//"vitis.student.iastate.edu",4444);
		JavacycConnection metacyc = new JavacycConnection("vitis.student.iastate.edu",4444);
		metacyc.selectOrganism("META");
		String org = args.length>0 ? args[0] : "ECOLI";
		String listFilename = args.length>1 ? args[1] : "";
		boolean writeTable =  args.length>2;
		connection.selectOrganism(org);
		try
		{
			
			BufferedReader listFileReader = new BufferedReader(new FileReader(listFilename));
			String line = null;
			ArrayList<Pathway> pwys = new ArrayList<Pathway>();
			HashSet<String> ids = new HashSet<String>();
			while((line=listFileReader.readLine()) != null)
			{
				Frame f = Frame.load(connection,line);
				if(f!=null)
				{
					if(writeTable)
					{
						String desc = f.getComment();
						if(desc == null && metacyc.frameExists(f.getLocalID()))
						{
							Frame f2 = Frame.load(connection,f.getLocalID());
							desc = Frame.load(metacyc,f.getLocalID()).getComment();
						}
						if(desc == null) desc = "No summary";
						System.out.println(latexize(f.getCommonName())+" & "+latexize(desc)+" \\\\ \\hline");
					}
					else
					{
						for(Frame p : f.getPathways())
						{
							if(!ids.contains(p.getLocalID()))
							{
								pwys.add((Pathway)p);
								ids.add(p.getLocalID());
							}
						}
					}
				}
			}
			listFileReader.close();
			if(!writeTable)
			{
				Network net = new Network(listFilename+".pathways");
				for(Pathway p : pwys)
				{
					net.importNetwork(p.getNetwork());
				}
				net.writeGML(new PrintStream(new FileOutputStream(listFilename+".pathways.gml")),true,true,true,false);
			}
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private static String latexize(String s)
	{
		return s.replace("&alpha;","$\\alpha$").replace("&beta;","$\\beta$").replace("<i>","\\emph{").replace("</i>","}").replace("<b>","{\\bf ").replace("</b>","}").replace("<B>","{\\bf ").replace("</B>","}").replace("%","$\\%$").replace("\"","").replaceAll("CITS:\\[.*\\]","").replace("|","");
	}

}
