/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * About.java
 *
 * Created on Apr 12, 2011, 10:22:58 PM
 */

package edu.iastate.pathwayaccess.cytoscape.ClientUI;

import edu.iastate.pathwayaccess.cytoscape.PathwayAccessPlugin;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Greg
 */
public class About extends javax.swing.JFrame {

    /**
     * Stores access to the database
     */
    private PathwayAccessPlugin database;
    /**
     * Stores the frame to align the window to
     */
    private JFrame frame;
    /**
     * URL of the Pathway Access project URL
     */
    private final String projectURL = "http://metnet.vrac.iastate.edu/MetNet_fcmodeler.htm";

    /**
     * Creates new form About with position relative to the given frame and
     * using properties from the given database
     * @param db Database to use properties from
     * @param frame Frame to set position relative to
     */
    public About(PathwayAccessPlugin db, JFrame frame) {
        if(db == null) throw new NullPointerException("The database cannot be null");
        database = db;
        if(frame == null) throw new NullPointerException("The frame cannot be null");
        this.frame = frame;
        initComponents();

        //Check that the Pathway Access project URL doesn't exceed 60 characters
        if(lblProjectURL.getText().length() > 60)
        {
            String url = lblProjectURL.getText();
            url = url.substring(0, 57) + "...";
            lblProjectURL.setText(url);
        }
        //Check that the database URL doesn't exceed 60 characters
        if(lblDbProjectURL.getText().length() > 60)
        {
            String url = lblDbProjectURL.getText();
            url = url.substring(0, 57) + "...";
            lblDbProjectURL.setText(url);
        }

        this.setLocationRelativeTo(frame);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
        // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
        private void initComponents() {

                lblPluginName = new javax.swing.JLabel();
                lblVersion = new javax.swing.JLabel();
                lblDistributor = new javax.swing.JLabel();
                sepPluginAndDbPlugin = new javax.swing.JSeparator();
                lblDbPluginName = new javax.swing.JLabel();
                lblDbPluginVersion = new javax.swing.JLabel();
                lblDbPluginDistributor = new javax.swing.JLabel();
                lblProjectURLTitle = new javax.swing.JLabel();
                lblProjectURL = new javax.swing.JLabel();
                lblDbProjectURLTitle = new javax.swing.JLabel();
                lblDbProjectURL = new javax.swing.JLabel();

                setTitle("About Pathway Access");
                setResizable(false);

                lblPluginName.setFont(new java.awt.Font("Tahoma", 1, 14));
                lblPluginName.setText("Pathway Access");

                lblVersion.setText("Version: 0.2");

                lblDistributor.setText("Distributor: Virtual Reality Application Center, ISU");

                lblDbPluginName.setFont(new java.awt.Font("Tahoma", 1, 14));
                lblDbPluginName.setText(database.getProperties().getDatabaseName().trim() + " Pathway Access");

                lblDbPluginVersion.setText("Version: " + database.getProperties().getVersion().trim());

                lblDbPluginDistributor.setText("Distributor: " + database.getProperties().getDistributor().trim());

                lblProjectURLTitle.setText("Project URL: ");

                //Check that the Pathway Access project URL doesn't exceed 60 characters
                String url = projectURL.trim();
                if(url.length() > 60) url = url.substring(0, 57) + "...";
                lblProjectURL.setForeground(new java.awt.Color(51, 51, 255));
                lblProjectURL.setText(url);
                lblProjectURL.setToolTipText("Opens the link in the default browser (" + projectURL.trim() + ")");
                lblProjectURL.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                lblProjectURLMouseClicked(evt);
                        }
                });

                lblDbProjectURLTitle.setText("Project URL: ");

                //Check that the Pathway Access project URL doesn't exceed 60 characters
                url = database.getProperties().projectURL().trim();
                if(url.length() > 60) url = url.substring(0, 57) + "...";
                lblDbProjectURL.setForeground(new java.awt.Color(51, 51, 255));
                lblDbProjectURL.setText(url);
                lblDbProjectURL.setToolTipText("Opens the link in the default browser (" + database.getProperties().projectURL().trim() + ")");
                lblDbProjectURL.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                                lblDbProjectURLMouseClicked(evt);
                        }
                });

                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
                getContentPane().setLayout(layout);
                layout.setHorizontalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblPluginName)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(lblProjectURLTitle)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lblProjectURL))
                                        .addComponent(lblVersion)
                                        .addComponent(lblDistributor)
                                        .addComponent(sepPluginAndDbPlugin, javax.swing.GroupLayout.DEFAULT_SIZE, 435, Short.MAX_VALUE)
                                        .addComponent(lblDbPluginName)
                                        .addComponent(lblDbPluginVersion)
                                        .addComponent(lblDbPluginDistributor)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(lblDbProjectURLTitle)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(lblDbProjectURL)))
                                .addContainerGap())
                );
                layout.setVerticalGroup(
                        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblPluginName)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblVersion)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblDistributor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblProjectURLTitle)
                                        .addComponent(lblProjectURL))
                                .addGap(18, 18, 18)
                                .addComponent(sepPluginAndDbPlugin, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblDbPluginName)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblDbPluginVersion)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblDbPluginDistributor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblDbProjectURLTitle)
                                        .addComponent(lblDbProjectURL))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                );

                pack();
        }// </editor-fold>//GEN-END:initComponents

    /**
     * When the user clicks the project URL link, this attempts to open in the
     * link default the default browser
     * @param evt Link click
     */
    private void lblProjectURLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblProjectURLMouseClicked
        //Open default browser to the project URL
        openURI(projectURL);
    }//GEN-LAST:event_lblProjectURLMouseClicked

    /**
     * When the user clicks the database project URL link, this attempts to open
     * in the link default the default browser
     * @param evt Link click
     */
    private void lblDbProjectURLMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDbProjectURLMouseClicked
        //Open default browser to the project URL
        openURI(database.getProperties().projectURL());
}//GEN-LAST:event_lblDbProjectURLMouseClicked

    /**
     * Attempts to create a URI out of the given string and open it in the
     * default browser. Any errors are shown to the user via an appropirate
     * message ("Link Error"/"Unable to open the link." or
     * "Browser Error"/"Unable to open the link.").
     * @param link Link to attempt to open in the defaul browser
     */
    private void openURI(String link) {
        URI url = null;
        try {
            url = new URI(link);
        } catch (URISyntaxException ex) {
            JOptionPane.showMessageDialog(frame,
                "Unable to open the link.",
                "Link Error",
                JOptionPane.ERROR_MESSAGE);
        }
        try {
            //OpenBrowser.openURL(evt.getURL().toString());
            java.awt.Desktop.getDesktop().browse(url);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame,
                "Unable to open the link.",
                "Browser Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JLabel lblDbPluginDistributor;
        private javax.swing.JLabel lblDbPluginName;
        private javax.swing.JLabel lblDbPluginVersion;
        private javax.swing.JLabel lblDbProjectURL;
        private javax.swing.JLabel lblDbProjectURLTitle;
        private javax.swing.JLabel lblDistributor;
        private javax.swing.JLabel lblPluginName;
        private javax.swing.JLabel lblProjectURL;
        private javax.swing.JLabel lblProjectURLTitle;
        private javax.swing.JLabel lblVersion;
        private javax.swing.JSeparator sepPluginAndDbPlugin;
        // End of variables declaration//GEN-END:variables

}