package com.xl.display.report;

import com.dw.publicaffairs.Query;
import com.xl.datatypes.DataCollection;
import com.xl.datatypes.probes.ProbeBean;
import com.xl.datatypes.probes.ProbeList;
import com.xl.display.dataviewer.DataTreeRenderer;
import com.xl.display.dataviewer.ProbeSetTreeModel;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

/**
 * Created by Administrator on 2014/10/5.
 */
public class FilterReports extends Report implements MouseListener, TreeSelectionListener {
    private JPanel optionsPanel = null;
    private JTree probeSetTree;
    private Object currentProbeList;

    /**
     * Instantiates a new report.
     *
     * @param collection Data Collection to use for the report
     */
    public FilterReports(DataCollection collection) {
        super(collection);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public String name() {
        return "Filter Reporter";
    }

    @Override
    public JPanel getOptionsPanel() {
        if (optionsPanel != null) return optionsPanel;
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BorderLayout());

        JPanel probeViewer = new JPanel();
        probeViewer.setLayout(new GridBagLayout());
        probeViewer.setBackground(Color.WHITE);
        GridBagConstraints con = new GridBagConstraints();
        con.gridx = 0;
        con.gridy = 0;
        con.weightx = 0.1;
        con.weighty = 0.01;
        con.fill = GridBagConstraints.HORIZONTAL;
        con.anchor = GridBagConstraints.FIRST_LINE_START;
        ProbeSetTreeModel probeModel = new ProbeSetTreeModel(collection);
        probeSetTree = new UnfocusableTree(probeModel);
        probeSetTree.addMouseListener(this);
        probeSetTree.addTreeSelectionListener(this);
        probeSetTree.setCellRenderer(new DataTreeRenderer());
        probeViewer.add(probeSetTree, con);
        // This nasty bit just makes the trees squash up to the top of the display area.
        con.gridy++;
        con.weighty = 1;
        con.fill = GridBagConstraints.BOTH;
        probeViewer.add(new JLabel(" "), con);

        optionsPanel.add(probeViewer, BorderLayout.CENTER);

        return optionsPanel;
    }

    @Override
    public void generateReport() {
        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public boolean isReady() {
        return currentProbeList != null;
    }

    @Override
    public void run() {
        ProbeList selectedProbeList = (ProbeList) currentProbeList;
        Vector<ProbeBean> probes = Query.queryAllEditingInfo(selectedProbeList.getTableName());
        ProbeBeanTableModel model = new ProbeBeanTableModel(probes.toArray(new ProbeBean[0]));
        reportComplete(model);
    }

    @Override
    public void valueChanged(TreeSelectionEvent tse) {
        if (tse.getSource() == probeSetTree) {
            if (probeSetTree.getSelectionPath() == null) {
                currentProbeList = null;
            } else {
                currentProbeList = probeSetTree.getSelectionPath().getLastPathComponent();
            }
            optionsChanged();
        }
    }

    /**
     * An extension of JTree which is unable to take keyboard focus.
     * <p/>
     * This class is needed to make sure the arrow key navigation
     * always works in the chromosome view.  If either of the JTrees
     * can grab focus they will intercept the arrow key events and
     * just move the selections on the tree.
     */
    private class UnfocusableTree extends JTree {

        // This class is needed to make sure the arrow key navigation
        // always works in the chromosome view.  If either of the JTrees
        // can grab focus they will intercept the arrow key events and
        // just move the selections on the tree.

        /**
         * Instantiates a new unfocusable tree.
         *
         * @param m
         */
        public UnfocusableTree(TreeModel m) {
            super(m);
            this.setFocusable(false);
        }

    }

    /**
     * A TableModel representing the results of the AnnotatedListReport..
     */
    private class ProbeBeanTableModel extends AbstractTableModel {

        private ProbeBean[] probeBeans;

        /**
         * Instantiates a new annotation table model.
         *
         * @param probeBeans The starting probe list
         */
        public ProbeBeanTableModel(ProbeBean[] probeBeans) {
            this.probeBeans = probeBeans;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public int getRowCount() {
            return probeBeans.length;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount() {
            return 9;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        public String getColumnName(int c) {
            switch (c) {
                case 0:
                    return "Chromosome";
                case 1:
                    return "Position";
                case 2:
                    return "ID";
                case 3:
                    return "Reference";
                case 4:
                    return "Alternative";
                case 5:
                    return "Quality";
                case 6:
                    return "Level";
                case 7:
                    return "P-value";
                case 8:
                    return "FDR";
                default:
                    return null;
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        public Class getColumnClass(int c) {
            switch (c) {
                case 0:
                    return String.class;
                case 1:
                    return Integer.class;
                case 2:
                    return String.class;
                case 3:
                    return Character.class;
                case 4:
                    return Character.class;
                case 5:
                    return Double.class;
                case 6:
                    return Double.class;
                case 7:
                    return Double.class;
                case 8:
                    return Double.class;
                default:
                    return null;
            }
        }


        /* (non-Javadoc)
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int r, int c) {
            switch (c) {
                case 0:
                    return probeBeans[r].getChr();
                case 1:
                    return probeBeans[r].getPos();
                case 2:
                    return probeBeans[r].getId();
                case 3:
                    return probeBeans[r].getRef();
                case 4:
                    return probeBeans[r].getAlt();
                case 5:
                    return probeBeans[r].getQual();
                case 6:
                    return probeBeans[r].getLevel();
                case 7:
                    return probeBeans[r].getPvalue();
                case 8:
                    return probeBeans[r].getFdr();
                default:
                    return null;
            }
        }

    }
}