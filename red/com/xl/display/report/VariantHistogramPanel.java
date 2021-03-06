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

package com.xl.display.report;

import com.xl.datatypes.DataStore;
import com.xl.datatypes.sites.Site;
import com.xl.display.dialog.SiteListViewer;
import com.xl.main.RedApplication;
import com.xl.utils.AxisScale;
import com.xl.utils.ColourScheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Class VariantHistogramPanel displays an interactive histogram of variant types.
 */
public class VariantHistogramPanel extends JPanel implements Runnable {
    /**
     * The data.
     */
    private Site[] sites;
    /**
     * The main histogram panel.
     */
    private MainHistogramPanel mainHistogramPanel;
    /**
     * The histogram categories for all variant types.
     */
    private Map<String, Integer> histogramCategories = new LinkedHashMap<String, Integer>(12);
    /**
     * The status panel.
     */
    private StatusPanel statusPanel;
    /**
     * Current histogram category.
     */
    private Map.Entry<String, Integer> currentHistogram;
    /**
     * The max data value.
     */
    private int maxCount;

    /**
     * Instantiates a new histogram panel.
     *
     * @param dataStore the data store.
     */
    public VariantHistogramPanel(DataStore dataStore) {

        this.sites = dataStore.siteSet().getActiveList().getAllSites();

        setLayout(new BorderLayout());
        JPanel textPanel = new JPanel();
        JTextArea textField = new JTextArea("Variant Type Distribution Histogram");
        textField.setEditable(false);
        textPanel.add(textField, BorderLayout.CENTER);
        add(textPanel, BorderLayout.NORTH);

        mainHistogramPanel = new MainHistogramPanel();
        add(mainHistogramPanel, BorderLayout.CENTER);

        statusPanel = new StatusPanel();
        add(statusPanel, BorderLayout.SOUTH);

        calculateCategories();
    }

    /**
     * Main histogram panel.
     *
     * @return the panel
     */
    public JPanel mainHistogramPanel() {
        return mainHistogramPanel;
    }

    public void exportData(File file) throws IOException {
        mainHistogramPanel.exportData(file);
    }

    /**
     * Calculate categories.
     */
    private void calculateCategories() {
        Thread t = new Thread(this);
        t.start();
    }


    @Override
    public void run() {

        for (Site site : sites) {
            String refToAlt = String.valueOf(site.getRefBase()).toUpperCase() + " to " + String.valueOf(site.getAltBase()).toUpperCase();
            if (!histogramCategories.containsKey(refToAlt)) {
                histogramCategories.put(refToAlt, 1);
            } else {
                histogramCategories.put(refToAlt, histogramCategories.get(refToAlt) + 1);
            }
        }
        Collection<Integer> coll = histogramCategories.values();
        for (int max : coll) {
            if (max > maxCount) {
                maxCount = max;
            }
        }
    }

    /**
     * The Class MainHistogramPanel.
     */
    private class MainHistogramPanel extends JPanel implements MouseListener, MouseMotionListener {
        /**
         * The space of x axis.
         */
        private static final int X_AXIS_SPACE = 50;
        /**
         * The space of y axis.
         */
        private static final int Y_AXIS_SPACE = 30;

        /**
         * Instantiates a new main histogram panel.
         */
        public MainHistogramPanel() {
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        /**
         * Export the count of variant type to a single text file.
         *
         * @param file the file to export to.
         * @throws IOException If file does not exist, then throw this exception.
         */
        public void exportData(File file) throws IOException {

            PrintWriter pr = new PrintWriter(file);

            pr.println("Variant Type\tCount");
            for (Map.Entry<String, Integer> entry : histogramCategories.entrySet()) {
                pr.println(entry.getKey() + "\t" + entry.getValue());
            }
            pr.close();
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            // We want a white background
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            // Draw the graph axes first.  We leave a border on all sides
            g.setColor(Color.BLACK);

            g.drawLine(X_AXIS_SPACE, 5, X_AXIS_SPACE, getHeight() - Y_AXIS_SPACE);
            g.drawLine(X_AXIS_SPACE, getHeight() - Y_AXIS_SPACE, getWidth() - 5, getHeight() - Y_AXIS_SPACE);

            // If we don't have any data we can stop here
            if (histogramCategories == null) return;

            // We need the scaling factor for the y-axis
            double yScale;

            yScale = (double) (getHeight() - (5 + Y_AXIS_SPACE)) / maxCount;

            // Now draw the scale on the y axis
            AxisScale yAxisScale = new AxisScale(0, maxCount);

            double currentYValue = yAxisScale.getStartingValue();

            while (currentYValue < maxCount) {

                double yHeight = currentYValue * yScale;
                g.drawString(yAxisScale.format(currentYValue), 2, (int) ((getHeight() - Y_AXIS_SPACE) - yHeight) + (g.getFontMetrics().getAscent() / 2));

                // Put a line across the plot
                if (currentYValue != 0) {
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawLine(X_AXIS_SPACE, (int) ((getHeight() - Y_AXIS_SPACE) - yHeight), getWidth() - 5, (int) ((getHeight() - Y_AXIS_SPACE) - yHeight));
                    g.setColor(Color.BLACK);
                }
                currentYValue += yAxisScale.getInterval();
            }

            // Now draw the scale on the x axis
            double interval = (double) (getWidth() - X_AXIS_SPACE) / histogramCategories.size();
            int index = 0;
            for (Map.Entry<String, Integer> entry : histogramCategories.entrySet()) {
                g.setColor(Color.BLACK);
                g.drawString(entry.getKey(), (int) (X_AXIS_SPACE + interval * index + interval / 2 - g.getFontMetrics().stringWidth(entry.getKey()) / 2),
                        getHeight() - Y_AXIS_SPACE + 15);
                if (entry == currentHistogram) {
                    g.setColor(ColourScheme.HIGHLIGHTED_HISTOGRAM_BAR);
                } else {
                    g.setColor(ColourScheme.HISTOGRAM_BAR);
                }
                g.fillRect((int) (X_AXIS_SPACE + interval * index), getHeight() - Y_AXIS_SPACE - getYWidth(entry.getValue()), (int) interval, getYWidth(entry.getValue()));
                // Draw a box around it
                g.setColor(Color.BLACK);
                g.drawRect((int) (X_AXIS_SPACE + interval * index), getHeight() - Y_AXIS_SPACE - getYWidth(entry.getValue()), (int) interval, getYWidth(entry.getValue()));
                index++;
            }
        }

        /**
         * Get the y pixel width by a given count relative to the maximum count.
         *
         * @param count the count of variant type
         * @return pixel of y width.
         */
        private int getYWidth(int count) {
            int y = (int) ((double) (count) / maxCount * (getHeight() - Y_AXIS_SPACE));
            if (y <= 5) {
                return y;
            } else {
                return y - 5;
            }
        }

        /**
         * Given a x position, we calculate the histogram where the mouse is in.
         *
         * @param xPosition the x position.
         * @return the variant type name.
         */
        private String getHistogram(int xPosition) {
            String[] variants = histogramCategories.keySet().toArray(new String[0]);
            return variants[(int) ((double) (xPosition - X_AXIS_SPACE) / (getWidth() - X_AXIS_SPACE) * variants.length)];
        }


        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent me) {

//            If we're outside the main plot area we don't need to worry about it
            if (me.getX() < 5 || me.getX() > getWidth() - 5 || me.getY() < 5 || me.getY() > getHeight() - Y_AXIS_SPACE - 5) {
                if (currentHistogram != null) {
                    currentHistogram = null;
                    statusPanel.setSelectedCategory(null);
                    repaint();
                }
                return;
            }
            for (Map.Entry<String, Integer> entry : histogramCategories.entrySet()) {
                if (entry.getKey().equals(getHistogram(me.getX()))) {
                    currentHistogram = entry;
                    statusPanel.setSelectedCategory(currentHistogram);
                    repaint();
                }
            }
        }

        @Override
        public void mouseClicked(MouseEvent arg0) {
            if (arg0.getClickCount() == 2 && currentHistogram != null) {
                String refToAlt = currentHistogram.getKey();
                char ref = refToAlt.charAt(0);
                char alt = refToAlt.charAt(refToAlt.length() - 1);
                java.util.List<Site> siteList = new ArrayList<Site>();
                for (Site site : sites) {
                    if (site.getAltBase() == alt && site.getRefBase() == ref) {
                        siteList.add(site);
                    }
                }
                new SiteListViewer(siteList.toArray(new Site[0]), ref + " to " + alt, "Focus on " + ref + " to " + alt, RedApplication.getInstance());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent me) {
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
        }

        @Override
        public void mouseExited(MouseEvent arg0) {
            currentHistogram = null;
            repaint();
        }

    }

    /**
     * The Class StatusPanel.
     */
    private class StatusPanel extends JPanel {

        /**
         * The label.
         */
        private JLabel label;

        /**
         * Instantiates a new status panel.
         */
        public StatusPanel() {
            setBackground(Color.WHITE);
            setOpaque(true);
            label = new JLabel("No selected category", JLabel.LEFT);
            setLayout(new BorderLayout());
            add(label, BorderLayout.WEST);
            setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
        }

        /**
         * Sets the selected category.
         *
         * @param currentHistogram the new selected category
         */
        public void setSelectedCategory(Map.Entry<String, Integer> currentHistogram) {
            if (currentHistogram == null) {
                label.setText("No selected Category");
            } else {
                label.setText("Variant Type: " + currentHistogram.getKey() + ", RNA Editing Sites of " + currentHistogram.getKey() + ":" + currentHistogram.getValue());
            }
        }

    }
}
