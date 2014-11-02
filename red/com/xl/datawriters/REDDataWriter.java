/**
 * Copyright Copyright 2007-13 Simon Andrews
 *
 *    This file is part of SeqMonk.
 *
 *    SeqMonk is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    SeqMonk is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with SeqMonk; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.xl.datawriters;

import com.xl.datatypes.DataCollection;
import com.xl.datatypes.DataGroup;
import com.xl.datatypes.DataSet;
import com.xl.datatypes.DataStore;
import com.xl.datatypes.annotation.AnnotationSet;
import com.xl.datatypes.annotation.CoreAnnotationSet;
import com.xl.datatypes.feature.Feature;
import com.xl.datatypes.genome.Genome;
import com.xl.datatypes.genome.GenomeDescriptor;
import com.xl.datatypes.sequence.Location;
import com.xl.datatypes.sites.Site;
import com.xl.datatypes.sites.SiteList;
import com.xl.datatypes.sites.SiteSet;
import com.xl.exception.REDException;
import com.xl.interfaces.Cancellable;
import com.xl.interfaces.ProgressListener;
import com.xl.main.REDApplication;
import com.xl.preferences.DisplayPreferences;
import com.xl.preferences.REDPreferences;
import com.xl.utils.ParsingUtils;
import com.xl.utils.Strand;

import java.io.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

/**
 * The Class SeqMonkDataWriter serialises a SeqMonk project to a single file.
 */
public class REDDataWriter implements Runnable, Cancellable {

    /**
     * The Constant DATA_VERSION.
     */
    public static final int DATA_VERSION = 1;

    // If you make ANY changes to the format written by this class
    // you MUST increment this value to stop older parsers from
    // trying to parse it. Once you have updated the parser to
    // read the new format you can then update the corresponding
    // value in the parser so that it will work.

	/*
     * TODO: Some of these data sets take a *long* time to save due to the
	 * volume of data. Often when people are saving they're just saving display
	 * preferences. In these cases it would be nice to have a mode where the
	 * display preferences were just appended to the end of an existing file,
	 * rather than having to put out the whole thing again. Since the size of
	 * the preferences section is pretty small it won't affect overall file size
	 * much.
	 * 
	 * If the data (sites, groups or quantitation) changes then we'll have to
	 * do a full rewrite.
	 */

    /**
     * The listeners.
     */
    private Vector<ProgressListener> listeners = new Vector<ProgressListener>();

    /**
     * The data.
     */
    private DataCollection data;

    /**
     * The genome.
     */
    private Genome genome;

    /**
     * The final file to save to file.
     */
    private File file;

    /**
     * The temporary file to work with
     */
    private File tempFile;

    /**
     * The visible stores.
     */
    private DataStore[] visibleStores;

    /**
     * Whether to cancel
     */
    private boolean cancel = false;

    /**
     * Instantiates a new seq monk data writer.
     */
    public REDDataWriter() {
    }

    /**
     * Adds the progress listener.
     *
     * @param l the l
     */
    public void addProgressListener(ProgressListener l) {
        if (l != null && !listeners.contains(l))
            listeners.add(l);
    }

    /**
     * Removes the progress listener.
     *
     * @param l the l
     */
    public void removeProgressListener(ProgressListener l) {
        if (l != null && listeners.contains(l))
            listeners.remove(l);
    }

    /**
     * Write data.
     *
     * @param application the application
     * @param file        the file
     */
    public void writeData(REDApplication application, File file) {
        data = application.dataCollection();
        // System.out.println(this.getClass().getDisplayName()+":"+data.getGenome().species());
        this.genome = data.genome();
        this.file = file;
        visibleStores = application.drawnDataStores();
        Thread t = new Thread(this);
        t.start();
    }

    public void cancel() {
        cancel = true;
    }

    private void cancelled(PrintStream p) throws IOException {
        p.close();

        if (!tempFile.delete()) {
            throw new IOException("Couldn't delete temp file");
        }
        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().progressCancelled();
        }
    }

    public void run() {
        try {
            // Generate a temp file in the same directory as the final destination
            tempFile = File.createTempFile("red", ".temp", file.getParentFile());

            BufferedOutputStream bos;

            if (REDPreferences.getInstance().compressOutput()) {
                bos = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(tempFile), 2048));
            } else {
                bos = new BufferedOutputStream(new FileOutputStream(tempFile));
            }
            PrintStream p = new PrintStream(bos);

            printDataVersion(p);

            printGenome(p);

            DataSet[] dataSets = data.getAllDataSets();
            DataGroup[] dataGroups = data.getAllDataGroups();

            if (!printDataSets(dataSets, p)) {
                return; // They cancelled
            }

            printDataGroups(dataSets, dataGroups, p);

            AnnotationSet[] annotationSets = data.genome()
                    .getAnnotationCollection().anotationSets();
            for (int a = 0; a < annotationSets.length; a++) {
                if (annotationSets[a] instanceof CoreAnnotationSet) {
                    continue;
                }
                if (!printAnnotationSet(annotationSets[a], p)) {
                    // They cancelled
                    return;
                }
            }

            Site[] sites = null;

            if (data.siteSet() != null) {
                sites = data.siteSet().getAllSites();
            }

            if (sites != null) {
                if (!printSiteSet(data.siteSet(), sites, p)) {
                    return; // They cancelled
                }
            }

            printVisibleDataStores(dataSets, dataGroups, p);

            if (sites != null) {
                if (!printSiteLists(p)) {
                    return; // They cancelled
                }
            }

            printDisplayPreferences(p);

            p.close();

            // We can now overwrite the original file
            if (file.exists()) {
                if (!file.delete()) {
                    throw new IOException(
                            "Couldn't delete old project file when making new one");
                }

            }

            if (!tempFile.renameTo(file)) {
                throw new IOException("Failed to rename temporary file");
            }

            Enumeration<ProgressListener> e = listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().progressComplete("data_written", null);
            }
        } catch (Exception ex) {
            Enumeration<ProgressListener> e = listeners.elements();
            while (e.hasMoreElements()) {
                e.nextElement().progressExceptionReceived(ex);
            }
            ex.printStackTrace();
        }

    }

    /**
     * Prints the data version.
     *
     * @param p the p
     */
    private void printDataVersion(PrintStream p) {
        // The first line of the file will be the version of the data
        // format we're using. This will help us out should we need
        // to update the format in the future.
        p.println(ParsingUtils.RED_DATA_VERSION + "\t" + DATA_VERSION);
    }

    /**
     * Prints the assembly.
     *
     * @param p the p
     */
    private void printGenome(PrintStream p) {
        // The next thing we need to do is to output the details of the genome
        // we're using
        p.println(ParsingUtils.GENOME_INFORMATION_START);
        p.println(GenomeDescriptor.getInstance().toString());
        p.println(ParsingUtils.GENOME_INFORMATION_END);
    }

    /**
     * Prints the data sets.
     *
     * @param dataSets the data sets
     * @param p        the p
     * @return false if cancelled, else true
     */
    private boolean printDataSets(DataSet[] dataSets, PrintStream p) throws IOException, REDException {
        p.println(ParsingUtils.SAMPLES + "\t" + dataSets.length);
        for (DataSet dataSet : dataSets) {
            p.println(dataSet.name() + "\t" + dataSet.fileName() + "\t" + dataSet.isStandardChromosomeName());
            p.println(dataSet.getTotalReadCount() + "\t" + dataSet.getTotalReadLength() + "\t" + dataSet.getForwardReadCount() + "\t" + dataSet.getReverseReadCount());
        }
        return true;
    }

    /**
     * Prints the data groups.
     *
     * @param dataSets   the data sets
     * @param dataGroups the data groups
     * @param p          the p
     */
    private void printDataGroups(DataSet[] dataSets, DataGroup[] dataGroups, PrintStream p) {

        p.println(ParsingUtils.DATA_GROUPS + "\t" + dataGroups.length);
        for (int i = 0; i < dataGroups.length; i++) {
            DataSet[] groupSets = dataGroups[i].dataSets();

            // We used to use the name of the dataset to populate the group
            // but this caused problems when we had duplicated dataset names
            // we therefore have to figure out the index of each dataset in
            // each group

            StringBuffer b = new StringBuffer();
            b.append(dataGroups[i].name());
            for (int j = 0; j < groupSets.length; j++) {
                for (int d = 0; d < dataSets.length; d++) {
                    if (groupSets[j] == dataSets[d]) {
                        b.append("\t");
                        b.append(d);
                    }
                }
            }

            p.println(b);
        }
    }

    /**
     * Prints the annotation set.
     *
     * @param a the a
     * @param p the p
     * @return false if cancelled, else true;
     */
    private boolean printAnnotationSet(AnnotationSet a, PrintStream p)
            throws IOException {
        List<Feature> features = a.getAllFeatures();
        p.println(ParsingUtils.ANNOTATION + "\t" + a.name() + "\t" + features.size());

        Enumeration<ProgressListener> e = listeners.elements();
        while (e.hasMoreElements()) {
            e.nextElement().progressUpdated("Writing annotation set " + a.name(), 0, 1);
        }

        for (Feature feature : features) {

            if (cancel) {
                cancelled(p);
                return false;
            }
            String name = feature.getName();
            String chr = feature.getChr();
            String strand = Strand.parseStrand(feature.getStrand());
            String aliasName = feature.getAliasName();
            List<Location> allLocations = feature.getAllLocations();
            p.println(name + "\t" + chr + "\t" + strand + "\t" + aliasName);
            p.println(allLocations);
        }
        return true;
    }

    /**
     * Prints the site set.
     *
     * @param siteSet the site set
     * @param sites   the sites
     * @param p       the p
     * @return false if cancelled, else true
     */
    private boolean printSiteSet(SiteSet siteSet, Site[] sites, PrintStream p) throws IOException {

        // We need the saved string to be linear so we replace the line breaks
        // with ` (which we've replaced with ' in the
        // comment. We put back the line breaks when we load the comments back.

        String comments = siteSet.comments().replaceAll("[\\r\\n]", "`");

        p.println(ParsingUtils.SITES + "\t" + sites.length + "\t" + siteSet.getTableName() + "\t" + siteSet
                .justDescription() + "\t" + comments);

        // Next we print out the data

        for (int i = 0; i < sites.length; i++) {

            if (cancel) {
                cancelled(p);
                return false;
            }

            if (i % 1000 == 0) {
                Enumeration<ProgressListener> e = listeners.elements();
                while (e.hasMoreElements()) {
                    e.nextElement().progressUpdated(
                            "Written data for " + i + " sites out of "
                                    + sites.length, i, sites.length);
                }
            }

            p.println(sites[i].getChr() + "\t" + sites[i].getStart() + "\t" + sites[i].getRefBase() + "\t" + sites[i].getAltBase());
        }
        return true;
    }

    /**
     * Prints the visible data stores.
     *
     * @param dataSets   the data sets
     * @param dataGroups the data groups
     * @param p          the p
     */
    private void printVisibleDataStores(DataSet[] dataSets,
                                        DataGroup[] dataGroups, PrintStream p) {
        // Now we can put out the list of visible stores
        // We have to refer to these by position rather than name
        // since names are not guaranteed to be unique.
        p.println(ParsingUtils.VISIBLE_STORES + "\t" + visibleStores.length);
        for (int i = 0; i < visibleStores.length; i++) {
            if (visibleStores[i] instanceof DataSet) {
                for (int s = 0; s < dataSets.length; s++) {
                    if (visibleStores[i] == dataSets[s]) {
                        p.println(s + "\t" + "set");
                    }
                }
            } else if (visibleStores[i] instanceof DataGroup) {
                for (int g = 0; g < dataGroups.length; g++) {
                    if (visibleStores[i] == dataGroups[g]) {
                        p.println(g + "\t" + "group");
                    }
                }
            }
        }
    }

    /**
     * Prints the site lists.
     *
     * @param p the p
     */
    private boolean printSiteLists(PrintStream p) throws REDException, IOException {
        // Now we print out the list of site lists

		/*
         * We rely on this list coming in tree order, that is to say that when
		 * we see a node at depth n we assume that all subsequent nodes at depth
		 * n+1 are children of the first node, until we see another node at
		 * depth n.
		 * 
		 * This should be how the nodes are created anyway.
		 */
        SiteList[] lists = data.siteSet().getAllSiteLists();

        // We start at the second list since the first list will always
        // be "All sites" which we'll sort out some other way.

        p.println(ParsingUtils.LISTS + "\t" + (lists.length - 1));

        for (int i = 1, len = lists.length; i < len; i++) {
            String listComments = lists[i].comments().replaceAll("[\\r\\n]", "`");
            Site[] currentSiteLists = lists[i].getAllSites();
            int siteLength = currentSiteLists.length;
            p.println(getListDepth(lists[i]) + "\t" + lists[i].name() + "\t" + lists[i].description() + "\t" +
                    lists[i].getTableName() + "\t" + siteLength + "\t" + listComments);

            for (int j = 0; j < siteLength; j++) {
                if (j % 1000 == 0) {
                    if (cancel) {
                        cancelled(p);
                        return false;
                    }
                    Enumeration<ProgressListener> e = listeners.elements();
                    while (e.hasMoreElements()) {
                        e.nextElement().progressUpdated(
                                "Written lists for " + j + " sites out of "
                                        + siteLength, j, siteLength);
                    }
                }
                p.println(currentSiteLists[j].getChr() + "\t" + currentSiteLists[j].getStart() + "\t" + currentSiteLists[j].getRefBase() + "\t" +
                        currentSiteLists[j].getAltBase());
            }
        }

        return true;
    }

    /**
     * Prints the display preferences.
     *
     * @param p the print stream to write the preferences to
     */
    private void printDisplayPreferences(PrintStream p) {
        // Now write out some display preferences
        DisplayPreferences.getInstance().writeConfiguration(p);
    }

    /**
     * Gets the list depth.
     *
     * @param p the p
     * @return the list depth
     */
    private int getListDepth(SiteList p) {
        int depth = 0;

        while (p.getParent() != null) {
            depth++;
            p = p.getParent();
        }
        return depth;
    }
}
