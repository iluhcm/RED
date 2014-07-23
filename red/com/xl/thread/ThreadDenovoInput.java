package com.xl.thread;

import com.dw.denovo.*;
import com.dw.publicaffairs.DatabaseManager;
import com.dw.publicaffairs.Utilities;
import com.xl.interfaces.ProgressListener;
import com.xl.preferences.LocationPreferences;
import com.xl.preferences.REDPreferences;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Administrator on 2014/7/22.
 */
public class ThreadDenovoInput implements Runnable {
    protected final ArrayList<ProgressListener> listeners;
    private final int ALL_STEP = 8;
    protected boolean cancel = false;

    public ThreadDenovoInput() {
        listeners = new ArrayList<ProgressListener>();
    }

    @Override
    public void run() {
        DatabaseManager manager = DatabaseManager.getInstance();
        LocationPreferences locationPreferences = LocationPreferences.getInstance();
        manager.createStatement();
        manager.setAutoCommit(true);
        progressUpdated("Creating denovo database...", 1, ALL_STEP);
        manager.createDatabase("denovo");
        manager.useDatabase("denovo");
        Utilities.getInstance().createCalTable(locationPreferences.getRnaVcfFile());

        DenovoVcf df = new DenovoVcf(manager, locationPreferences.getRnaVcfFile(), "rnaVcf");
        progressUpdated("Creating RNA vcf table...", 2, ALL_STEP);
        df.establishRnaTable();
        progressUpdated("Importing RNA vcf data...", 3, ALL_STEP);
        df.rnaVcf();

        progressUpdated("Filtering sites based on quality and coverage...", 4, ALL_STEP);
        BasicFilter bf = new BasicFilter(manager, "rnaVcf", "specifictemp",
                "basictemp");
        bf.createSpecificTable();
        bf.specificf();
        bf.createBasicTable();
        // The first parameter means quality and the second means depth
        bf.basicFilter(20, 6);
        bf.distinctTable();


        RepeatFilter rf = new RepeatFilter(manager, locationPreferences.getRepeatFile(), "repeattemp",
                "referencerepeat", "basictemp");
        progressUpdated("Importing repeatmasker data...", 5, ALL_STEP);
        rf.loadrepeat();
        rf.establishrepeat();
        rf.repeatFilter();
        rf.distinctTable();

        progressUpdated("Importing RefSeq Genes data...", 6, ALL_STEP);
        ComphrehensiveFilter cf = new
                ComphrehensiveFilter(manager, locationPreferences.getRefSeqFile(), "comphrehensivetemp", "refcomphrehensive",
                "repeattemp");
        cf.loadcom();
        cf.establishCom();
        cf.comphrehensiveF(2);
        cf.distinctTable();

        progressUpdated("Importing dbSNP data...", 7, ALL_STEP);
        DbsnpFilter sf = new
                DbsnpFilter(manager, locationPreferences.getDbSNPFile(), "snptemp", "refsnp", "comphrehensivetemp");
        sf.establishsnp();
        sf.loadRefdbSnp();
        sf.snpFilter();
        sf.distinctTable();

        progressUpdated("Importing DARNED data...", 8, ALL_STEP);
        PValueFilter pv = new
                PValueFilter(manager, locationPreferences.getDarnedFile(), "pvtemp", "refHg19", "snptemp");
        pv.loadRefHg19();
//      pv.fdr(args[6]);
        manager.closeDatabase();
        REDPreferences.getInstance().setDataLoadedToDatabase(true);
        processingComplete();
    }

    /**
     * Adds a progress listener.
     *
     * @param l The listener to add
     */
    public void addProgressListener(ProgressListener l) {
        if (l == null) {
            throw new NullPointerException("DataParserListener can't be null");
        }

        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Removes a progress listener.
     *
     * @param l The listener to remove
     */
    public void removeProgressListener(ProgressListener l) {
        if (l != null && listeners.contains(l)) {
            listeners.remove(l);
        }
    }

    /**
     * Alerts all listeners to a progress update
     *
     * @param message The message to send
     * @param current The current level of progress
     * @param max     The level of progress at completion
     */
    protected void progressUpdated(String message, int current, int max) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressUpdated(message, current, max);
        }
    }

    /**
     * Alerts all listeners that an exception was received. The
     * parser is not expected to continue after issuing this call.
     *
     * @param e The exception
     */
    protected void progressExceptionReceived(Exception e) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressExceptionReceived(e);
        }
    }

    /**
     * Alerts all listeners that a warning was received.  The parser
     * is expected to continue after issuing this call.
     *
     * @param e The warning exception received
     */
    protected void progressWarningReceived(Exception e) {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressWarningReceived(e);
        }
    }

    /**
     * Alerts all listeners that the user cancelled this import.
     */
    protected void progressCancelled() {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressCancelled();
        }
    }

    /**
     * Tells all listeners that the parser has finished parsing the data
     * The list of dataSets should be the same length as the original file list.
     */
    protected void processingComplete() {
        Iterator<ProgressListener> i = listeners.iterator();
        for (; i.hasNext(); ) {
            i.next().progressComplete("database_loaded", null);
        }
    }
}
