package com.xl.display.report;

/**
 * Created by Administrator on 2014/9/18.
 */

import com.xl.datatypes.DataCollection;
import com.xl.interfaces.Cancellable;
import com.xl.interfaces.OptionsListener;
import com.xl.interfaces.ProgressListener;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * An abstract class to represent an output report.
 */
public abstract class Report implements Runnable, Cancellable {

    protected DataCollection collection;

    /**
     * A flag to say if we should cancel the report generation
     */
    protected boolean cancel = false;

    /**
     * A set of listeners for the progress of the report generation
     */
    private ArrayList<ProgressListener> listeners = new ArrayList<ProgressListener>();

    /**
     * A set of listeners listening to changes in the report options
     */
    private ArrayList<OptionsListener> optionsListeners = new ArrayList<OptionsListener>();

    /**
     * Instantiates a new report.
     *
     * @param collection Data Collection to use for the report
     */
    public Report(DataCollection collection) {
        this.collection = collection;
    }

    /**
     * Data collection.
     *
     * @return The dataCollection
     */
    public DataCollection dataCollection() {
        return collection;
    }

    /* (non-Javadoc)
     * @see uk.ac.babraham.SeqMonk.Dialogs.Cancellable#cancel()
     */
    public void cancel() {
        /*
         * Sets the cancel flag so that if the report is being
		 * generated it can spot this and bail out
		 */
        cancel = true;
    }

    /**
     * Name.
     *
     * @return The name of the report
     */
    public abstract String name();

    /**
     * Gets a panel which both displays and allows the user to
     * set whatever options the report has.
     *
     * @return A JPanel which displays all report options.
     */
    public abstract JPanel getOptionsPanel();

    /**
     * Actually starts the generation of the report.
     */
    public abstract void generateReport();

    /**
     * Says if enough options have been set to allow the report
     * to be generated.
     *
     * @return true, if the report is ready to be generated
     */
    public abstract boolean isReady();

    /**
     * Add a progress listener for the generation of the report
     *
     * @param l The listener to add
     */
    public void addProgressListener(ProgressListener l) {
        if (l == null) {
            throw new NullPointerException("ProgressListener can't be null");
        }

        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Remove a progress listener.
     *
     * @param l The listener to remove
     */
    public void removeProgressListener(ProgressListener l) {
        if (l != null && listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    /**
     * Adds a listener to the changing options for the report.
     *
     * @param l The listener to add
     */
    public void addOptionsListener(OptionsListener l) {
        if (l == null) {
            throw new NullPointerException("OptionsListener can't be null");
        }

        if (!optionsListeners.contains(l)) {
            optionsListeners.add(l);
        }
    }

    /**
     * Removes an options listener.
     *
     * @param l The listener to remove
     */
    public void removeOptionsListener(OptionsListener l) {
        if (l != null && optionsListeners.contains(l)) {
            optionsListeners.remove(l);
        }
    }


    public boolean canExportGFF() {
        return false;
    }

    public int chromosomeColumn() {
        return 0;
    }

    public int startColumn() {
        return 0;
    }

    public int endColumn() {
        return 0;
    }

    public int strandColumn() {
        return 0;
    }


    /**
     * Passes on an update message to all progress listeners
     *
     * @param message The message text to display
     * @param current The current progress value
     * @param total   The progress value at completion
     */
    protected void progressUpdated(String message, int current, int total) {
        Iterator<ProgressListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().progressUpdated(message, current, total);
        }
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    /**
     * Passes on a message to all options listeners
     */
    protected void optionsChanged() {
        Iterator<OptionsListener> i = optionsListeners.iterator();
        while (i.hasNext()) {
            i.next().optionsChanged();
        }
    }

    /**
     * Passes on an exception to all progress listeners
     *
     * @param e The exception to pass on.
     */
    protected void progressExceptionReceived(Exception e) {
        Iterator<ProgressListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().progressExceptionReceived(e);
        }
    }

    /**
     * Passes on a cancellation message to all listeners.
     */
    protected void progressCancelled() {
        Iterator<ProgressListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().progressCancelled();
        }
        cancel = false;
    }

    /**
     * Passes on a report completion message to all listeners
     */
    protected void reportComplete(TableModel report) {
        Iterator<ProgressListener> i = listeners.iterator();
        while (i.hasNext()) {
            i.next().progressComplete("report_generated", report);
        }
    }


}