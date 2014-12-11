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

package com.xl.database;

import com.xl.datatypes.DataSet;
import com.xl.datatypes.sites.SiteSet;
import com.xl.display.dialog.DataImportDialog;
import com.xl.display.dialog.TypeColourRenderer;
import com.xl.exception.REDException;
import com.xl.main.REDApplication;
import com.xl.utils.FontManager;
import com.xl.utils.ListDefaultSelector;
import com.xl.utils.namemanager.MenuUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Xing Li on 2014/11/15.
 * <p/>
 * A dialog to select the database and sample relative to a data set. It is worth to say that data sets must be imported first to RED and user should know what
 * they should select with a given data set.
 */
public class DatabaseSelector extends JDialog implements ListSelectionListener, TreeSelectionListener, ActionListener {

    /**
     * The application.
     */
    private REDApplication application;

    /**
     * The database tree.
     */
    private JTree databaseTree;

    /**
     * The data set list.
     */
    private JList dataSetList;

    /**
     * The selected data set.
     */
    private DataSet selectedDataSet;

    /**
     * The ok button.
     */
    private JButton okButton;

    /**
     * Instantiates a new database selector.
     *
     * @param application the application
     */
    public DatabaseSelector(REDApplication application) {
        super(application, "Select Sample From Database...");
        this.application = application;
        setSize(600, 300);
        setLocationRelativeTo(application);
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        getContentPane().setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        topPanel.add(new JLabel("WARNINGS", JLabel.CENTER), BorderLayout.NORTH);

        JTextArea textField = new JTextArea();
        textField.setText("The data set selected in the left panel and the sample selected in the right panel must be consistent!");
        textField.setFont(FontManager.DIALOG_FONT);
        textField.setEditable(false);
        textField.setLineWrap(true);
        textField.setWrapStyleWord(true);
        textField.setBackground(getBackground());
        topPanel.add(textField, BorderLayout.CENTER);
        getContentPane().add(topPanel, BorderLayout.NORTH);

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        leftPanel.add(new JLabel("Data Sets", JLabel.CENTER), BorderLayout.NORTH);

        DefaultListModel dataModel = new DefaultListModel();

        DataSet[] sets = application.dataCollection().getAllDataSets();
        for (DataSet set : sets) {
            dataModel.addElement(set);
        }
        dataSetList = new JList(dataModel);
        selectedDataSet = (DataSet) ListDefaultSelector.selectDefaultStore(dataSetList);
        dataSetList.setCellRenderer(new TypeColourRenderer());
        dataSetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataSetList.addListSelectionListener(this);
        leftPanel.add(new JScrollPane(dataSetList), BorderLayout.CENTER);
        getContentPane().add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        DatabaseTreeModel treeModel = new DatabaseTreeModel();
        databaseTree = new JTree(treeModel);
        databaseTree.addTreeSelectionListener(this);
        databaseTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        rightPanel.add(new JScrollPane(databaseTree));
        getContentPane().add(rightPanel, BorderLayout.CENTER);

        // Create the buttons at the bottom.
        JPanel buttonPanel = new JPanel();

        JButton cancelButton = new JButton(MenuUtils.CANCEL_BUTTON);
        cancelButton.setActionCommand(MenuUtils.CANCEL_BUTTON);
        cancelButton.addActionListener(this);
        buttonPanel.add(cancelButton);

        JButton deleteButton = new JButton(MenuUtils.DELETE_BUTTON);
        deleteButton.setActionCommand(MenuUtils.DELETE_BUTTON);
        deleteButton.addActionListener(this);
        buttonPanel.add(deleteButton);

        JButton importButton = new JButton(MenuUtils.IMPORT_BUTTON);
        importButton.setActionCommand(MenuUtils.IMPORT_BUTTON);
        importButton.addActionListener(this);
        buttonPanel.add(importButton);

        okButton = new JButton(MenuUtils.OK_BUTTON);
        okButton.setActionCommand(MenuUtils.OK_BUTTON);
        okButton.setEnabled(false);
        okButton.addActionListener(this);
        getRootPane().setDefaultButton(okButton);
        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equals(MenuUtils.OK_BUTTON)) {
            setVisible(false);
            try {
                TableNode node = (TableNode) databaseTree.getSelectionPath().getLastPathComponent();
                String parentNode = databaseTree.getSelectionPath().getParentPath().getLastPathComponent().toString();
                DatabaseManager.getInstance().databaseChanged(parentNode, node.getSampleName());
                SiteSet siteSet = Query.getSiteSetFromDatabase(node.getSampleName());
                siteSet.setDataStore(selectedDataSet);
                selectedDataSet.setSiteSet(siteSet);
                dispose();
            } catch (REDException e) {
                e.printStackTrace();
            }
        } else if (ae.getActionCommand().equals(MenuUtils.IMPORT_BUTTON)) {
            setVisible(false);
            new DataImportDialog(application);
            dispose();
        } else if (ae.getActionCommand().equals(MenuUtils.DELETE_BUTTON)) {
            TableNode lastComponent = (TableNode) databaseTree.getSelectionPath().getLastPathComponent();
            String parentNode = databaseTree.getSelectionPath().getParentPath().getLastPathComponent().toString();
            DatabaseManager.getInstance().deleteTableAndFilters(parentNode, lastComponent.getSampleName());
            dispose();
            new DatabaseSelector(application);
        } else if (ae.getActionCommand().equals(MenuUtils.CANCEL_BUTTON)) {
            setVisible(false);
            dispose();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (databaseTree.getSelectionPath() != null && databaseTree.getSelectionPath().getLastPathComponent() instanceof TableNode && dataSetList != null &&
                dataSetList.getSelectedValue() instanceof DataSet) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(true);
        }
    }


    @Override
    public void valueChanged(TreeSelectionEvent e) {
        if (databaseTree.getSelectionPath() != null && databaseTree.getSelectionPath().getLastPathComponent() instanceof TableNode && dataSetList != null &&
                dataSetList.getSelectedValue() instanceof DataSet) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }


}