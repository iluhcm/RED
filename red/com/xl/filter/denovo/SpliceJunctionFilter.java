/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.filter.denovo;

import com.xl.database.DatabaseManager;
import com.xl.display.dialog.ProgressDialog;
import com.xl.display.dialog.REDProgressBar;
import com.xl.utils.Timer;

import java.sql.SQLException;

/**
 * The Class SpliceJunctionFilter is a rule-based filter. Variants that were within+/-k bp (e.g., k = 2) of the splice junction, which were supposed to be
 * unreliable, were excluded based on the gene annotation file.
 */
public class SpliceJunctionFilter {
    /**
     * The database manager.
     */
    private DatabaseManager databaseManager;
    /**
     * The progress bar for loading repeat regions file from RepeatMasker.
     */
    private REDProgressBar progressBar = REDProgressBar.getInstance();

    /**
     * Initiate a new splice junction filter.
     *
     * @param databaseManager the database manager
     */
    public SpliceJunctionFilter(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Check whether the gene annotation file has been loaded into database by calculating the row count of this table.
     *
     * @param spliceJunctionTable The gene annotation file table name, it is constant.
     * @return true if gene annotation data exists in the database.
     */
    public boolean hasEstablishedSpliceJunctionTable(String spliceJunctionTable) {
        return databaseManager.getRowCount(spliceJunctionTable) > 0;
    }

    /**
     * Load gene annotation file into database.
     *
     * @param spliceJunctionTable The gene annotation file table name, it is constant.
     * @param spliceJunctionPath  The gene annotation file path.
     */
    public void loadSpliceJunctionTable(String spliceJunctionTable, String spliceJunctionPath) {
        System.out.println("Start loading SpliceJunctionTable..." + Timer.getCurrentTime());
        progressBar.addProgressListener(new ProgressDialog("Import gene annotation file into database..."));
        progressBar.progressUpdated("Start loading gene annotation file from " + spliceJunctionPath + " to " + spliceJunctionTable, 0, 0);
        if (!hasEstablishedSpliceJunctionTable(spliceJunctionTable)) {
            try {
                progressBar.progressUpdated("Importing gene annotation file from " + spliceJunctionPath + " to " + spliceJunctionTable + " table", 0, 0);
                databaseManager.executeSQL("load data local infile '" + spliceJunctionPath + "' into table " + spliceJunctionTable + " fields terminated" +
                        " by '\t' lines terminated by '\n'");
            } catch (SQLException e) {
                System.err.println("Error execute sql clause in " + SpliceJunctionFilter.class.getName() + ":loadSpliceJunctionTable()..");
                e.printStackTrace();
            }
        }
        progressBar.progressComplete("splicejunction_loaded", null);

        System.out.println("End loading SpliceJunctionTable..." + Timer.getCurrentTime());
    }

    /**
     * Perform splice junction filter as user's preference. Variants that were within +/-k bp (e.g., k = 2) of the splice junction, which were supposed to be
     * unreliable, were excluded based on the gene annotation file.
     *
     * @param spliceJunctionTable       The gene annotation file table name, it is constant.
     * @param spliceJunctionResultTable The result table
     * @param previousTable             The previous table
     * @param splicejunction            The threshold of splice junction
     */
    public void executeSpliceJunctionFilter(String spliceJunctionTable, String spliceJunctionResultTable, String previousTable, int splicejunction) {
        System.out.println("Start executing SpliceJunctionFilter..." + Timer.getCurrentTime());
        try {
            databaseManager.executeSQL("insert into " + spliceJunctionResultTable + " select * from " + previousTable + " where not exists (select chrom from "
                    + spliceJunctionTable + " where (" + spliceJunctionTable + ".type='CDS' and " + spliceJunctionTable + ".chrom=" + previousTable + ".chrom" +
                    " and ((" + spliceJunctionTable + ".begin<" + previousTable + ".pos+" + splicejunction + " and " + spliceJunctionTable + ".begin>" + previousTable + "" +
                    ".pos-" + splicejunction + ") or (" + spliceJunctionTable + ".end<" + previousTable + ".pos+" + splicejunction + " and " + spliceJunctionTable + ".end>"
                    + previousTable + ".pos-" + splicejunction + "))))");
        } catch (SQLException e) {
            System.err.println("Error execute sql clause in" + SpliceJunctionFilter.class.getName() + ":executeSpliceJunctionFilter()");
            e.printStackTrace();
        }
        System.out.println("End executing SpliceJunctionFilter..." + Timer.getCurrentTime());
    }

}