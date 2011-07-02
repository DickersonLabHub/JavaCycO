package edu.iastate.pathwayaccess.cytoscape;

import javax.swing.SwingWorker;

/**
 * <p>
 * This is a simplified class extending SwingWorker<Void,Void>, therefore all methods
 * inside SwingWorker can be implemented. The purpose of this class being to
 * perform tasks in a dedicated thread in the background allowing tasks to
 * perform at without freezing up the GUI. <b>Also note below, the convenient
 * use of a pre-built status bar along side this class which simplifies much
 * of the task of communicating between GUI waiting components and long tasks.<b>
 *
 * <p>
 * For the most simple application of this class, an override method of
 * <i>protected Void doInBackground()</i> and optionally <i>public void done()</i>
 * are all that are needed to perform tasks in the background. For more details
 * on other methods and functionality, please refer to JavaDoc for SwingWorker.
 * There is a large amount of support online for working with multi-threaded
 * applications; the JavaDoc for SwingWorker may be the best place to begin.
 *
 * <p>
 * Tasks when searching databases for biological data can take significant
 * time and therefore should be performed in a seperate thread from the GUI.
 * Communication between multiple threads can be difficult, therefore the
 * tasks of using loading bars and waiting statuses have been <b>simplified
 * using this class and a pre-built status bar class</b>. This JavaDoc will
 * cover the simplified use of this class, however a more in depth use of
 * SwingWorker can be found by refering to JavaDoc for SwingWorker.
 *
 * <p>
 * <b>Sample Usage:</b> The following example creates a new task that calls a
 * method in a new thread using <code>NewThreadWorker</code>.
 *
 * <pre>
 * private JPanel layoutStatusBar = new JPanel();
 * private void executeSomething() {
        //Create the new task
        NewThreadWorker worker = new NewThreadWorker()
        {
            <code>@Override</code>
            public Void doInBackground()
            {
                //Prepare Status Bar
                this.setStatusBarProgress(0);
                this.setStatusTitle("Preparing to Search...");
                //Call the search method
                doSomething(this);
                return null;
            }
            <code>@Override</code>
            public void done()
            {
                this.setStatusTitle("Search Complete");
            }
        };
        //Create new loading bar for this task
        pnlStatusBar statusBar = new pnlStatusBar(false, worker);
        layoutStatusBar.removeAll(); //Remove old loading bar
        layoutStatusBar.add(statusBar, "STATUS"); //Add new loading bar
        //Begin the task
        worker.execute();
   }
 * doSomething(NewThreadWorker worker) {
 *
 * }
 * </pre>
 *
 * @author Greg Hazen <ghazen@iastate.edu>
 */
public class NewThreadWorker extends SwingWorker<Void,Void> {

    /**
     * Creates an empty NewThreadWorker
     */
    public NewThreadWorker()
    {

    }
/*
    public void cancel()
    {

    }
*/
    /**
     * Computes a result, or throws an exception if unable to do so.
     * Note that this method is executed only once.
     *
     * Note: this method is executed in a background thread.
     * @return Nothing (Void)
     * @throws Exception if unable to compute a result
     */
    @Override
    protected Void doInBackground() throws Exception {
        //Does nothing, must override
        return null;
    }

    /**
     * NOTE: pnlStatusBar will automatically set the task title to "Task
     * Complete" if completed or "Task Canceled" if canceled before completed.
     * This text can only be changed by calling the setStatusTitle() method in
     * this class after completion.
     *
     * Executed on the Event Dispatch Thread after the doInBackground method is
     * finished. The default implementation does nothing. Subclasses may
     * override this method to perform completion actions on the Event Dispatch
     * Thread. Note that you can query status inside the implementation of this
     * method to determine the result of this task or whether this task has
     * been cancelled.
     */
    @Override
    public void done()
    {
        //Does nothing, may override
    }

    /**
     * If the status bar assoctiated with this thread worker is
     * non-indeterminate, set during initialization of pnlStatusBar, the loading
     * bar percent complete is set to the given progress amount.
     * @param progress Percent of the task completed (0-100)
     */
    public void setStatusBarProgress(int progress)
    {
        //How this method works:
        //Sets the progress of the task within the SwingWorker. The pnlStatusBar
        //will register this change through the EventListener and automatically
        //update the status bar.
        this.setProgress(progress);
    }

    /**
     * Used to show what the current task is: changes the label next to the
     * progress bar to the given string. If the string is longer than 40
     * characters, the first 37 characters are used and "..." is added to the
     * end.  If the given value is null, the title is set to blank.
     * @param title String to change the status title to.
     */
    public void setStatusTitle(String title)
    {
        //How this method works:
        //Creates a PropertyChangeEvent in the SwingWorker with the new title.
        //The pnlStatusBar will register this change through the EventListener
        //and automatically update the title.
        firePropertyChange("task string", null, title);
    }

}
