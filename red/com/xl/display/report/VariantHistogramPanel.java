package com.xl.display.report;

import com.xl.datatypes.probes.Probe;
import com.xl.dialog.ProbeListViewer;
import com.xl.main.REDApplication;
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
 * The Class HistogramPanel displays an interactive histogram from
 * any linear set of data.
 */
public class VariantHistogramPanel extends JPanel implements Runnable {

    /**
     * The data.
     */
    private Probe[] probes;

    /**
     * The main histogram panel.
     */
    private MainHistogramPanel mainHistogramPanel;

    private Map<String, Integer> histogramCategories = new LinkedHashMap<String, Integer>(12);
    /**
     * The status panel.
     */
    private StatusPanel statusPanel;

    private Map.Entry<String, Integer> currentHistogram;

    /**
     * The max data value.
     */
    private int maxCount;

    /**
     * Instantiates a new histogram panel.
     *
     * @param probes the probes
     */
    public VariantHistogramPanel(Probe[] probes) {

        this.probes = probes;

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

        calcuateCategories();

    }

    /**
     * Main histogram panel.
     *
     * @return the j panel
     */
    public JPanel mainHistogramPanel() {
        return mainHistogramPanel;
    }

    public void exportData(File file) throws IOException {
        mainHistogramPanel.exportData(file);
    }

    /**
     * Calcuate categories.
     */
    private void calcuateCategories() {
        Thread t = new Thread(this);
        t.start();
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {

        for (Probe probe : probes) {
            String refalt = String.valueOf(probe.getRefBase()).toUpperCase() + " to " + String.valueOf(probe.getAltBase()).toUpperCase();
            if (!histogramCategories.containsKey(refalt)) {
                histogramCategories.put(refalt, 1);
            } else {
                histogramCategories.put(refalt, histogramCategories.get(refalt) + 1);
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

        private static final int X_AXIS_SPACE = 50;
        private static final int Y_AXIS_SPACE = 30;

        /**
         * Instantiates a new main histogram panel.
         */
        public MainHistogramPanel() {
            addMouseListener(this);
            addMouseMotionListener(this);
        }

        public void exportData(File file) throws IOException {

            PrintWriter pr = new PrintWriter(file);

            pr.println("Variant Type\tCount");
            for (Map.Entry<String, Integer> entry : histogramCategories.entrySet()) {
                pr.println(entry.getKey() + "\t" + entry.getValue());
            }
            pr.close();
        }

        /* (non-Javadoc)
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
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
                    g.drawLine(X_AXIS_SPACE, (int) ((getHeight() - Y_AXIS_SPACE) - yHeight), getWidth() - 5,
                            (int) ((getHeight() - Y_AXIS_SPACE) - yHeight));
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

        private int getYWidth(int count) {
            int y = (int) ((double) (count) / maxCount * (getHeight() - Y_AXIS_SPACE));
            if (y <= 5) {
                return y;
            } else {
                return y - 5;
            }
        }

        private String getHistogram(int xPosition) {
            String[] variants = histogramCategories.keySet().toArray(new String[0]);
            return variants[(int) ((double) (xPosition - X_AXIS_SPACE) / (getWidth() - X_AXIS_SPACE) * variants.length)];
        }


        @Override
        public void mouseDragged(MouseEvent e) {

        }

        /* (non-Javadoc)
                 * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
                 */
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

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(MouseEvent arg0) {
            if (arg0.getClickCount() == 2 && currentHistogram != null) {
                String refalt = currentHistogram.getKey();
                char ref = refalt.charAt(0);
                char alt = refalt.charAt(refalt.length() - 1);
                java.util.List<Probe> probeList = new ArrayList<Probe>();
                for (Probe probe : probes) {
                    if (probe.getAltBase() == alt && probe.getRefBase() == ref) {
                        probeList.add(probe);
                    }
                }
                new ProbeListViewer(probeList.toArray(new Probe[0]), ref + " to " + alt, "Focus on " + ref + " to " + alt, REDApplication.getInstance());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        public void mouseEntered(MouseEvent arg0) {
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        public void mouseExited(MouseEvent arg0) {
            currentHistogram = null;
            repaint();
        }


        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(MouseEvent me) {

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