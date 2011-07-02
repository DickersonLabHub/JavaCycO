package edu.iastate.pathwayaccess.cytoscape.metnet;

import edu.iastate.pathwayaccess.cytoscape.PluginProperties;

/**
 *
 * @author Greg
 */
public class MetNetProperties extends PluginProperties {

    public MetNetProperties()
    {
        super("Pathway Access - MetNet",
                "Searching and opening of MetNet Database pathways.");
    }

    public String getNetworkTypeName() {
        return "Pathway";
    }

    public String databaseDescription() {
        return "<html><body>" +
            "<h2>About <a href=\"http://metnetonline.org\">MetNet</a> Pathway Access</h2>" +
            "<p>Pathway Access for MetNet provides convenient access to the " +
            "<a href=\"http://metnetonline.org\">MetNet</a> Database. Using the " +
            "search features you may search the database based on entity names, pathway names, " +
            "and the organisms they are in. Once a valid search has been performed, results " +
            "will be shown. You can then explore information about the different entities " +
            "and pathways. It will also allow filtering of the resulting pathways. Once a " +
            "pathway has been chosen, a Cytoscape network of the pathway can be created. " +
            "Multiple pathways can be combined from any database set up with Pathway Access.</p>" +
            "<h2>More About the <a href=\"http://metnetonline.org\">MetNet</a> Database</h2>" +
            "<p><a href=\"http://metnetonline.org\">MetNet</a> is publicly available software " +
            "for analysis of genome-wide mRNA, protein, and metabolite profiling data. The software " +
            "is designed to enable the biologist to visualize, statistically analyze, and model " +
            "a metabolic and regulatory network map of Arabidopsis, combined with gene expression " +
            "profiling data.</p>" +
            "<p><a href=\"http://metnetonline.org\">MetNet</a> will provide a framework for the " +
            "formulation of testable hypotheses regarding the function of specific genes, and in " +
            "the long term will provide the basis for identification of metabolic and regulatory " +
            "networks that control plant composition and development.</p>" +
            "</body></html>";
    }
    
    /**
     * Returns an array of the names of all highly connected entities in the
     * database.
     * @return An array of the names of all highly connected entities in the
     * database.
     */
    public String[] getHighlyConnectedEntityNames()
    {
        return new String[] {
            "2-KETOGLUTARATE",
            "Acceptor",
            "ACETYL-COA",
            "ADP",
            "AMMONIA",
            "AMP",
            "ASCORBATE",
            "ATP",
            "BETA-GALACTOSIDASE",
            "CARBON-DIOXIDE",
            "CO-A",
            "CO2",
            "CoA",
            "CYTOCHROME P450",
            "Donor-H2",
            "E-",
            "GLN",
            "GLT",
            "H+",
            "H2O",
            "H2O2",
            "HCO3",
            "HYDROGEN-PEROXIDE",
            "MDHA",
            "NAD",
            "NAD+",
            "NADH",
            "NADP",
            "NAD(P)",
            "NADPH",
            "O2",
            "O2-",
            "OXYGEN-MOLECULE",
            "PECTINESTERASE",
            "PEROXIDASE",
            "phosphate",
            "Pi",
            "POLYGALACTURONASE",
            "PROTON",
            "PYRUVATE",
            "S-ADENOSYLMETHIONINE",
            "UDP",
            "UDP-GLUCOSE"
        };
    }

    public String getDatabaseName() {
        return "MetNet";
    }
    
    public String getVersion() {
        return "0.2";
    }

    public String getDistributor() {
        return "Virtual Reality Application Center, ISU";
    }

    public String projectURL() {
        return "http://metnet.vrac.iastate.edu/MetNet_fcmodeler.htm";
    }
}
