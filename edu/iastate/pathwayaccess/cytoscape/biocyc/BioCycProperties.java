/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.iastate.pathwayaccess.cytoscape.biocyc;

import edu.iastate.pathwayaccess.cytoscape.PluginProperties;

/**
 *
 * @author Greg Hazen ghazen@iastate.edu
 */
public class BioCycProperties extends PluginProperties {

    public BioCycProperties()
    {
        super("BioCyc - MetNet",
                "Searching and opening of BioCyc Database pathways.");
    }

    public String getDatabaseName() {
        return "BioCyc";
    }

    public String getVersion() {
        return "0.1";
    }

    public String getDistributor() {
        return "Virtual Reality Application Center, ISU";
    }

    public String projectURL() {
        return "http://metnet.vrac.iastate.edu/MetNet_fcmodeler.htm";
    }

    public String getNetworkTypeName() {
        return "Pathway";
    }

    public String databaseDescription() {
        return "<html><body>" +
            "<h2>About <a href=\"http://metnetonline.org\">MetNet</a> BioCyc Access</h2>" +
            "<p>Pathway Access for BioCyc provides convenient access to the " +
            "<a href=\"http://www.biocyc.org/\">BioCyc</a> Database. Using the " +
            "search features you may search the database based on entity names, pathway names, " +
            "and the organisms they are in. Once a valid search has been performed, results " +
            "will be shown. You can then explore information about the different entities " +
            "and pathways. It will also allow filtering of the resulting pathways. Once a " +
            "pathway has been chosen, a Cytoscape network of the pathway can be created. " +
            "Multiple pathways can be combined from any database set up with Pathway Access.</p>" +
            "<h2>More About the <a href=\"http://www.biocyc.org\">BioCyc</a> Database</h2>" +
            "<p><a href=\"http://www.biocyc.org\">BioCyc</a> is a collection of Pathway/Genome " +
            "Databases. Each database in the BioCyc collection describes the genome and metabolic " +
            "pathways of a single organism.</p>" +
            "</body></html>";
    }

    public String[] getHighlyConnectedEntityNames() {
        return new String[] {
            "|Acceptor|",
            "|Demethylated-methyl-acceptors|",
            "|Donor-H2|",
            "|Fatty-Acids|",
            "|Methylated-methyl-acceptors|",
            "|Oxidized-ferredoxins|",
            "|Pi|",
            "|Reduced-ferredoxins|",
            "2-KETO-ISOVALERATE",
            "2-KETOGLUTARATE",
            "5-10-METHENYL-THF",
            "ACET",
            "ACETYL-COA",
            "ACP",
            "ADENINE",
            "ADENOSINE",
            "ADENOSYL-HOMO-CYS",
            "ADP",
            "AMMONIA",
            "AMP",
            "ASCORBATE",
            "ATP",
            "CAFFEOYL-COA",
            "CARBON-DIOXIDE",
            "CMP",
            "CO-A",
            "CTP",
            "CYS",
            "DELTA3-ISOPENTENYL-PP",
            "DIACYLGLYCEROL",
            "DIHYDROXY-ACETONE-PHOSPHATE",
            "DOLICHOLP",
            "FAD",
            "FADH2",
            "FORMATE",
            "FRUCTOSE-6P",
            "FUM",
            "GAP",
            "GDP",
            "GDP-MANNOSE",
            "GERANYL-PP",
            "GLC",
            "GLC-1-P",
            "GLC-6-P",
            "GLN",
            "GLT",
            "GLUTATHIONE",
            "GLY",
            "GLYCEROL-3P",
            "GTP",
            "HCO3",
            "HOMO-CYS",
            "HYDROGEN-PEROXIDE",
            "HYPOXANTHINE",
            "INDOLE_ACETATE_AUXIN",
            "L-ALPHA-ALANINE",
            "L-ASPARTATE",
            "L-ORNITHINE",
            "MAL",
            "MALONYL-ACP",
            "MALONYL-COA",
            "MET",
            "METHYLENE-THF",
            "MYO-INOSITOL",
            "NAD",
            "NAD-P-OR-NOP",
            "NADH",
            "NADH-P-OR-NOP",
            "NADP",
            "NADPH",
            "OXALACETIC_ACID",
            "OXYGEN-MOLECULE",
            "P-COUMAROYL-COA",
            "PHOSPHATIDYLCHOLINE",
            "PHOSPHO-ENOL-PYRUVATE",
            "PPI",
            "PROTON",
            "PRPP",
            "PYRUVATE",
            "RIBOSE",
            "RIBOSE-5P",
            "RIBULOSE-5P",
            "S-ADENOSYLMETHIONINE",
            "SER",
            "SUC",
            "THF",
            "UDP",
            "UDP-GALACTOSE",
            "UDP-GLUCOSE",
            "UDP-GLUCURONATE",
            "UTP",
            "WATER",
            "XYLULOSE-5-PHOSPHATE"  
        };
        
    }

}
