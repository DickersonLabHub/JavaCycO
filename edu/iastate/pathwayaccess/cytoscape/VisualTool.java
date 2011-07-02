/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.iastate.pathwayaccess.cytoscape;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.ArrowShape;
import cytoscape.visual.CalculatorCatalog;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.BoundaryRangeValues;
import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.Interpolator;
import cytoscape.visual.mappings.LinearNumberToColorInterpolator;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.PassThroughMapping;
import java.awt.Color;
import java.util.Set;

/**
 *
 * @author Greg
 */
public class VisualTool {

    /**
     * Name of the visual style created by a call to getNetworkVisualStyles()
     */
    public static final String VISUAL_STYLE_NAME = "CyNetworkSearchClient Visual Style";

    /**
     * Returns a Set of names of the available visual styles applicable to the 
     * existing CyNetworks.
     * @return A Set of names of the available visual styles applicable to the 
     * existing CyNetworks
     */
    public static Set<String> getNetworkVisualStyles()
    {
        VisualMappingManager vmm = Cytoscape.getVisualMappingManager();
        return vmm.getCalculatorCatalog().getVisualStyleNames();
    }

    /**
     * Creates the default CyNetworkSearchClient network visual style and
     * applies it to the given CyNetwork. This method supports canceling of the
     * given task.
     * @param network CyNetwork to apply the visual style to
     * @return The default CyNetworkSearchClient visual style created or null if
     * the task is canceled.
     */
    public static VisualStyle createDefaultVisualStyle(CyNetwork network, NewThreadWorker worker)
    {

        if(network == null || worker == null)
            throw new NullPointerException("Null parameter");
        if(worker.isCancelled()) return null;
        //Check to see if a visual style with this name already exists
        CalculatorCatalog catalog = Cytoscape.getVisualMappingManager().getCalculatorCatalog();
        VisualStyle vs = catalog.getVisualStyle(VISUAL_STYLE_NAME);
        if(vs != null) return vs;

        NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();
        EdgeAppearanceCalculator edgeAppCalc = new EdgeAppearanceCalculator();
        GlobalAppearanceCalculator globalAppCalc = new GlobalAppearanceCalculator();

        // Passthrough Mapping - set node label
        PassThroughMapping pm = new PassThroughMapping(new String(), SynonymTool.SYNONYM_ATTRIBUTE_NAME);
        Calculator nlc = new BasicCalculator("CyNetworkSearchClient Node Label Calculator",
                pm, VisualPropertyType.NODE_LABEL);
        nodeAppCalc.setCalculator(nlc);

        // Discrete Mapping - set node shapes
        DiscreteMapping disMapping = new DiscreteMapping(NodeShape.RECT,
                ObjectMapping.NODE_MAPPING);
        disMapping.setControllingAttributeName(SynonymTool.NODE_TYPE_ATTRIBUTE_NAME, network, false);
        disMapping.putMapValue(PathwayAccessPlugin.GENE, NodeShape.RECT);
        disMapping.putMapValue(PathwayAccessPlugin.RNA, NodeShape.RECT);
        disMapping.putMapValue(PathwayAccessPlugin.ANTISENSE_RNA, NodeShape.RECT);
        disMapping.putMapValue(PathwayAccessPlugin.PROTEIN, NodeShape.DIAMOND);
        disMapping.putMapValue(PathwayAccessPlugin.PROTEIN_GENERIC, NodeShape.DIAMOND);
        disMapping.putMapValue(PathwayAccessPlugin.PROTEIN_ION_CHANNEL, NodeShape.DIAMOND);
        disMapping.putMapValue(PathwayAccessPlugin.PROTEIN_RECEPTOR, NodeShape.DIAMOND);
        disMapping.putMapValue(PathwayAccessPlugin.PROTEIN_TRUNCATED, NodeShape.DIAMOND);
        disMapping.putMapValue(PathwayAccessPlugin.COMPLEX, NodeShape.DIAMOND);

        Calculator shapeCalculator = new BasicCalculator("CyNetworkSearchClient Node Shape Calculator",
                disMapping,
                VisualPropertyType.NODE_SHAPE);
        nodeAppCalc.setCalculator(shapeCalculator);

//Need to edit here on: Probably need to change node color mapping to discrete mapping instead of
//        continuous. Use the node shape mapping above as an example. This will allow colors to
//        be mapped to specific compartment names instead.
        // Continuous Mapping - set node color
        ContinuousMapping continuousMapping = new ContinuousMapping(Color.WHITE,
                ObjectMapping.NODE_MAPPING);
        continuousMapping.setControllingAttributeName(SynonymTool.COMPARTMENT_ATTRIBUTE_NAME, network, false);

        Interpolator numToColor = new LinearNumberToColorInterpolator();
        continuousMapping.setInterpolator(numToColor);
        
        Color underColor = Color.GRAY;
        Color minColor = Color.RED;
        Color midColor = Color.WHITE;
        Color maxColor = Color.GREEN;
        Color overColor = Color.BLUE;

        // Create boundary conditions                     less than, equals, greater than
        BoundaryRangeValues bv0 = new BoundaryRangeValues(underColor, minColor, minColor);
        BoundaryRangeValues bv1 = new BoundaryRangeValues(midColor, midColor, midColor);
        BoundaryRangeValues bv2 = new BoundaryRangeValues(maxColor, maxColor, overColor);

        // Set the attribute point values associated with the boundary values
        continuousMapping.addPoint(0.0, bv0);
        continuousMapping.addPoint(1.0, bv1);
        continuousMapping.addPoint(2.0, bv2);

        Calculator nodeColorCalculator = new BasicCalculator("Example Node Color Calc",
                continuousMapping,
                VisualPropertyType.NODE_FILL_COLOR);
        nodeAppCalc.setCalculator(nodeColorCalculator);

        // Discrete Mapping - Set edge target arrow shape
        DiscreteMapping arrowMapping = new DiscreteMapping(ArrowShape.NONE,
                ObjectMapping.EDGE_MAPPING);
        arrowMapping.setControllingAttributeName("interaction", network, false);
        arrowMapping.putMapValue("pp", ArrowShape.ARROW);
        arrowMapping.putMapValue("pd", ArrowShape.CIRCLE);

        Calculator edgeArrowCalculator = new BasicCalculator("Example Edge Arrow Shape Calculator",
                arrowMapping, VisualPropertyType.EDGE_TGTARROW_SHAPE);
        edgeAppCalc.setCalculator(edgeArrowCalculator);

        // Create the visual style
        VisualStyle visualStyle = new VisualStyle(VISUAL_STYLE_NAME, nodeAppCalc, edgeAppCalc, globalAppCalc);
        return visualStyle;
    }

    /**
     * Applies the default CyNetworkSearchClient network visual style to the
     * given CyNetwork. This method supports canceling of the given task.
     * @param network CyNetwork to apply the visual style to
     * @return The default CyNetworkSearchClient visual style created or null if
     * the task is canceled.
     */
    public static VisualStyle applyDefaultLayout(CyNetwork network, NewThreadWorker worker)
    {
        if(network == null || worker == null)
            throw new NullPointerException("Null parameter");
        if(worker.isCancelled()) return null;
        VisualStyle visualStyle;// = new VisualStyle(VISUAL_STYLE_NAME, nodeAppCalc, edgeAppCalc, globalAppCalc);
        //Check to see if a visual style with this name already exists
        CalculatorCatalog catalog = Cytoscape.getVisualMappingManager().getCalculatorCatalog();
        VisualStyle vs = catalog.getVisualStyle(VISUAL_STYLE_NAME);
        if(vs == null)
        {
            visualStyle = createDefaultVisualStyle(network, worker);
            catalog.addVisualStyle(visualStyle);
            VisualStyle testStyle;
            testStyle = Cytoscape.getVisualMappingManager().getCalculatorCatalog().getVisualStyle(VISUAL_STYLE_NAME);
            int i = 0;
        }
        else visualStyle = vs;
        //Apply the visual style to the given network
        CyNetworkView netView = Cytoscape.getNetworkView(network.getIdentifier());
        netView.setVisualStyle(visualStyle.getName());
        netView.redrawGraph(true, true);

        return visualStyle;
    }

    /**
     *
     */
    public static void applyLayout()
    {

    }


}
