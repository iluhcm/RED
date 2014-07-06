/*
 * Created by JFormDesigner on Tue Nov 12 11:20:42 GMT 2013
 */

package com.xl.menu;

import com.xl.dialog.*;
import com.xl.dialog.gotodialog.GotoDialog;
import com.xl.dialog.gotodialog.GotoWindowDialog;
import com.xl.help.HelpDialog;
import com.xl.main.REDApplication;
import com.xl.panel.ToolbarPanel;
import com.xl.panel.WelcomePanel;
import com.xl.parsers.annotationparsers.AnnotationParserRunner;
import com.xl.parsers.annotationparsers.UCSCRefGeneParser;
import com.xl.parsers.dataparsers.BAMFileParser;
import com.xl.parsers.dataparsers.FastaFileParser;
import com.xl.preferences.REDPreferences;
import com.xl.utils.imagemanager.ImageSaver;
import com.xl.utils.namemanager.MenuUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Vector;

/**
 * @author Xing Li
 */
public class REDMenu extends JMenuBar implements ActionListener {
    /**
     *
     */
    private REDApplication redApplication;
    private ToolbarPanel toolbarPanel;
    private REDToolbar[] redToolbar;

    private JMenu fileMenu;
    private JMenuItem newProject;
    private JMenuItem openProject;
    private JMenuItem saveProject;
    private JMenuItem saveProjectAs;
    private JMenu importData;
    private JMenuItem rna_cDNA;
    private JMenuItem dna_gDNA;
    private JMenuItem loadGenome;
    private JMenuItem annotation;
    private JMenuItem exportImage;
    private JMenuItem exit;
    private JMenu editMenu;

    private JCheckBoxMenuItem showToolbar;

    private JMenu showPanel;
    private JCheckBoxMenuItem[] showPanels;

    private JMenuItem setDataTracks;
    private JMenuItem find;
    private JMenuItem preference;
    private JMenu viewMenu;
    private JMenuItem zoomIn;
    private JMenuItem zoomOut;
    private JMenuItem setZoomLevel;
    private JMenuItem moveLeft;
    private JMenuItem moveRight;
    private JMenu gotoMenuItem;
    private JMenuItem gotoPosition;
    private JMenuItem gotoWindow;
    private JMenu filterMenu;
    private JMenuItem basicFilter;
    private JMenuItem knownSNVsFilter;
    private JMenuItem rnadnaFilter;
    private JMenuItem repetitiveFilter;
    private JMenuItem comprehensiveFilter;
    private JMenuItem statisticalFilter;
    private JMenu reportsMenu;
    private JMenuItem variantDistribution;
    private JMenuItem barChart;
    private JMenuItem filterReports;
    private JMenu helpMenu;
    private JMenuItem welcome;
    private JMenuItem helpContents;
    private JMenuItem checkForUpdates;
    private JMenuItem aboutRED;

    public REDMenu(REDApplication redApplication) {
        this.redApplication = redApplication;
        initComponents();
    }

    private void initComponents() {
        toolbarPanel = new ToolbarPanel();
        redToolbar = new REDToolbar[]{new MainREDToolbar(this),};

        fileMenu = new JMenu();
        newProject = new JMenuItem();
        openProject = new JMenuItem();
        saveProject = new JMenuItem();
        saveProjectAs = new JMenuItem();
        importData = new JMenu();
        rna_cDNA = new JMenuItem();
        dna_gDNA = new JMenuItem();
        loadGenome = new JMenuItem();
        annotation = new JMenuItem();
        exportImage = new JMenuItem();
        exit = new JMenuItem();
        editMenu = new JMenu();
        showToolbar = new JCheckBoxMenuItem(redToolbar[0].name(),
                redToolbar[0].shown());
        showPanels = new JCheckBoxMenuItem[4];
        showPanel = new JMenu();
        gotoMenuItem = new JMenu();
        gotoPosition = new JMenuItem();
        gotoWindow = new JMenuItem();
        find = new JMenuItem();
        preference = new JMenuItem();
        viewMenu = new JMenu();
        zoomIn = new JMenuItem();
        zoomOut = new JMenuItem();
        setZoomLevel = new JMenuItem();
        setDataTracks = new JMenuItem();
        moveLeft = new JMenuItem();
        moveRight = new JMenuItem();
        filterMenu = new JMenu();
        basicFilter = new JMenuItem();
        knownSNVsFilter = new JMenuItem();
        rnadnaFilter = new JMenuItem();
        repetitiveFilter = new JMenuItem();
        comprehensiveFilter = new JMenuItem();
        statisticalFilter = new JMenuItem();
        reportsMenu = new JMenu();
        variantDistribution = new JMenuItem();
        barChart = new JMenuItem();
        filterReports = new JMenuItem();
        helpMenu = new JMenu();
        welcome = new JMenuItem();
        helpContents = new JMenuItem();
        checkForUpdates = new JMenuItem();
        aboutRED = new JMenuItem();

        // ======== fileMenu ========
        {
            fileMenu.setText(MenuUtils.FILE_MENU);
            fileMenu.setMnemonic('F');

            addJMenuItem(fileMenu, newProject, MenuUtils.NEW_PROJECT,
                    KeyEvent.VK_N, false);
            addJMenuItem(fileMenu, openProject, MenuUtils.OPEN_PROJECT,
                    KeyEvent.VK_O, false);
            addJMenuItem(fileMenu, saveProject, MenuUtils.SAVE_PROJECT,
                    KeyEvent.VK_S, false);
            addJMenuItem(fileMenu, saveProjectAs, MenuUtils.SAVE_PROJECT_AS,
                    KeyEvent.VK_W, false);
            fileMenu.addSeparator();
            // ======== importData ========
            {
                importData.setText(MenuUtils.IMPORT_DATA);
                addJMenuItem(importData, rna_cDNA, MenuUtils.RNA,
                        KeyEvent.VK_R, true);
                addJMenuItem(importData, dna_gDNA, MenuUtils.DNA,
                        KeyEvent.VK_D, true);
                addJMenuItem(importData, annotation, MenuUtils.ANNOTATION,
                        KeyEvent.VK_A, true);
                fileMenu.add(importData);
                importData.setEnabled(false);
            }
            addJMenuItem(fileMenu, loadGenome, MenuUtils.LOAD_GENOME,
                    KeyEvent.VK_L, false);
            addJMenuItem(fileMenu, exportImage, MenuUtils.EXPORT_IMAGE,
                    KeyEvent.VK_E, false);
            fileMenu.addSeparator();

            String[] recentPaths = REDPreferences.getInstance()
                    .getRecentlyOpenedFiles();
            for (int i = 0; i < recentPaths.length; i++) {
                File f = new File(recentPaths[i]);
                if (f.exists()) {
                    JMenuItem menuItem2 = new JMenuItem(f.getName());
                    menuItem2.addActionListener(new FileOpener(redApplication,
                            f));
                    fileMenu.add(menuItem2);
                }
            }
            addJMenuItem(fileMenu, exit, MenuUtils.EXIT, KeyEvent.VK_Q, true);
        }
        add(fileMenu);

        // ======== editMenu ========
        {
            editMenu.setText(MenuUtils.EDIT_MENU);
            showToolbar.setActionCommand(MenuUtils.SHOW_TOOLBAR);
            showToolbar.addActionListener(this);
            showToolbar.setEnabled(false);
            editMenu.add(showToolbar);
            {
                for (int i = 0; i < 4; i++) {
                    showPanels[i] = new JCheckBoxMenuItem(
                            MenuUtils.SHOW_PANELS[i], true);
                    showPanels[i].setActionCommand(MenuUtils.SHOW_PANELS[i]);
                    showPanels[i].addActionListener(this);
                    showPanel.add(showPanels[i]);
                }
                showPanel.setText(MenuUtils.SHOW_PANEL);
                showPanel.setActionCommand(MenuUtils.SHOW_PANEL);
                showPanel.addActionListener(this);
                editMenu.add(showPanel);
                showPanel.setEnabled(false);
            }

            editMenu.addSeparator();
            addJMenuItem(editMenu, setDataTracks, MenuUtils.SET_DATA_TRACKS,
                    -1, false);
            addJMenuItem(editMenu, find, MenuUtils.FIND, KeyEvent.VK_F, false);
            editMenu.addSeparator();
            addJMenuItem(editMenu, preference, MenuUtils.PREFERENCES,
                    KeyEvent.VK_P, true);
        }
        add(editMenu);

        // ======== viewMenu ========
        {
            viewMenu.setText("View");
            addJMenuItem(viewMenu, zoomIn, MenuUtils.ZOOM_IN, KeyEvent.VK_I);
            addJMenuItem(viewMenu, zoomOut, MenuUtils.ZOOM_OUT, KeyEvent.VK_O);
            addJMenuItem(viewMenu, setZoomLevel, MenuUtils.SET_ZOOM_LEVEL, -1);
            viewMenu.addSeparator();
            addJMenuItem(viewMenu, moveLeft, MenuUtils.MOVE_LEFT, -1);
            addJMenuItem(viewMenu, moveRight, MenuUtils.MOVE_RIGHT, -1);
            viewMenu.addSeparator();
            {
                gotoMenuItem.setText(MenuUtils.GOTO);
                addJMenuItem(gotoMenuItem, gotoPosition,
                        MenuUtils.GOTO_POSITION, -1);
                addJMenuItem(gotoMenuItem, gotoWindow, MenuUtils.GOTO_WINDOW,
                        -1);
                viewMenu.add(gotoMenuItem);
            }
        }
        add(viewMenu);
        viewMenu.setEnabled(false);

        // ======== filterMenu ========
        {
            filterMenu.setText(MenuUtils.FILTER_MENU);
            addJMenuItem(filterMenu, basicFilter, MenuUtils.BASIC_FILTER, -1);
            addJMenuItem(filterMenu, knownSNVsFilter,
                    MenuUtils.KNOWN_SNVS_FILTER, -1);
            addJMenuItem(filterMenu, rnadnaFilter, MenuUtils.RNA_DNA_FILTER, -1);
            addJMenuItem(filterMenu, repetitiveFilter,
                    MenuUtils.REPEATED_FILTER, -1);
            addJMenuItem(filterMenu, comprehensiveFilter,
                    MenuUtils.COMPREHENSIVE_FILTER, -1);
            addJMenuItem(filterMenu, statisticalFilter,
                    MenuUtils.STATISTICAL_FILTER, -1);
        }
        add(filterMenu);
        filterMenu.setEnabled(false);

        // ======== plotsFilter ========
        {
            reportsMenu.setText(MenuUtils.REPORTS_MENU);
            addJMenuItem(reportsMenu, variantDistribution,
                    MenuUtils.VARIANT_DISTRIBUTION, -1);
            addJMenuItem(reportsMenu, barChart, MenuUtils.BAR_CHART, -1);
            addJMenuItem(reportsMenu, filterReports, MenuUtils.FILTER_REPORTS,
                    -1);
        }
        add(reportsMenu);
        reportsMenu.setEnabled(false);

        // ======== helpMenu ========
        {
            helpMenu.setText(MenuUtils.HELP_MENU);
            addJMenuItem(helpMenu, welcome, MenuUtils.WELCOME, -1);
            addJMenuItem(helpMenu, helpContents, MenuUtils.HELP_CONTENTS, -1);
            addJMenuItem(helpMenu, checkForUpdates,
                    MenuUtils.CHECK_FOR_UPDATES, -1);
            addJMenuItem(helpMenu, aboutRED, MenuUtils.ABOUT_RED, -1);
        }
        add(helpMenu);
    }

    /**
     * A brief way to add menu item to the menu.
     *
     * @param jMenu     The menu which should add to.
     * @param jMenuItem The menu item to be added to the jMenu.
     * @param text      The menu item name, including the action command name.
     * @param mnemonic  The keyboard shortcuts. Call Java API when using the shortcut
     *                  letter.
     */
    private void addJMenuItem(JMenu jMenu, JMenuItem jMenuItem, String text,
                              int mnemonic) {
        if (text != null) {
            jMenuItem.setText(text);
            jMenuItem.setActionCommand(text);
        }
        if (mnemonic != -1) {
            jMenuItem.setMnemonic(mnemonic);
            jMenuItem.setAccelerator(KeyStroke.getKeyStroke(mnemonic,
                    ActionEvent.CTRL_MASK));
        }
        jMenuItem.addActionListener(REDMenu.this);
        jMenu.add(jMenuItem);
    }

    /**
     * A brief way to add menu item to the menu.
     *
     * @param jMenu     The menu which should add to.
     * @param jMenuItem The menu item to be added to the jMenu.
     * @param text      The menu item name, including the action command name.
     * @param mnemonic  The keyboard shortcuts. Call Java API when using the shortcut
     *                  letter.
     * @param isEnable  Set the item enable or not.
     */
    private void addJMenuItem(JMenu jMenu, JMenuItem jMenuItem, String text,
                              int mnemonic, boolean isEnable) {
        if (text != null) {
            jMenuItem.setText(text);
            jMenuItem.setActionCommand(text);
        }
        if (mnemonic != -1) {
            jMenuItem.setMnemonic(mnemonic);
            jMenuItem.setAccelerator(KeyStroke.getKeyStroke(mnemonic,
                    ActionEvent.CTRL_MASK));
        }
        jMenuItem.addActionListener(REDMenu.this);
        jMenu.add(jMenuItem);
        jMenuItem.setEnabled(isEnable);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        String action = arg0.getActionCommand();
        // --------------------FileMenu--------------------
        if (action.equals(MenuUtils.NEW_PROJECT)) {
            redApplication.startNewProject();
        } else if (action.equals(MenuUtils.OPEN_PROJECT)) {
            redApplication.loadProject();
        } else if (action.equals(MenuUtils.SAVE_PROJECT)) {
            redApplication.saveProject();
        } else if (action.equals(MenuUtils.SAVE_PROJECT_AS)) {
            redApplication.saveProjectAs();
        } else if (action.equals(MenuUtils.RNA)) {
            redApplication.importData(new BAMFileParser(redApplication
                    .dataCollection()));
        } else if (action.equals(MenuUtils.DNA)) {

        } else if (action.equals(MenuUtils.ANNOTATION)) {
            AnnotationParserRunner.RunAnnotationParser(redApplication,
                    new UCSCRefGeneParser(redApplication.dataCollection()
                            .genome()));
        } else if (action.equals(MenuUtils.LOAD_GENOME)) {
            redApplication.importData(new FastaFileParser(redApplication.dataCollection()));
        } else if (action.equals(MenuUtils.EXPORT_IMAGE)) {
            ImageSaver.saveImage(redApplication.chromosomeViewer());
        } else if (action.equals(MenuUtils.EXIT)) {
            redApplication.dispose();
        }
        // --------------------EditMenu--------------------
        else if (action.equals(MenuUtils.SHOW_TOOLBAR)) {
            toolbarPanel.setVisible(!toolbarPanel.isVisible());
        } else if (action.equals(MenuUtils.SHOW_PANELS[0])) {
            redApplication.dataViewer().setVisible(
                    !redApplication.dataViewer().isVisible());
            if (redApplication.genomeViewer().isVisible()) {
                if (redApplication.dataViewer().isVisible()) {
                    redApplication.topPane().setDividerLocation(0.125);
                } else {
                    redApplication.topPane().setDividerLocation(0);
                }
            }
        } else if (action.equals(MenuUtils.SHOW_PANELS[1])) {
            redApplication.genomeViewer().setVisible(
                    !redApplication.genomeViewer().isVisible());
            if (redApplication.dataViewer().isVisible()) {
                if (redApplication.genomeViewer().isVisible()) {
                    redApplication.topPane().setDividerLocation(0.125);
                } else {
                    redApplication.topPane().setDividerLocation(0.99);
                }
            }
        } else if (action.equals(MenuUtils.SHOW_PANELS[2])) {
            redApplication.chromosomeViewer().setVisible(
                    !redApplication.chromosomeViewer().isVisible());
            if (redApplication.chromosomeViewer().isVisible()) {
                redApplication.mainPanel().setDividerLocation(0.5);
            } else {
                redApplication.mainPanel().setDividerLocation(0.99);
            }
        } else if (action.equals(MenuUtils.SHOW_PANELS[3])) {
            redApplication.statusPanel().setVisible(
                    !redApplication.statusPanel().isVisible());
        } else if (action.equals(MenuUtils.SET_DATA_TRACKS)) {
            new DataTrackSelector(redApplication);
        } else if (action.equals(MenuUtils.FIND)) {
            new FindFeatureDialog(redApplication.dataCollection());
        } else if (action.equals(MenuUtils.PREFERENCES)) {
            new EditPreferencesDialog();
        }
        // --------------------ViewMenu--------------------
        else if (action.equals(MenuUtils.ZOOM_IN)) {
            redApplication.chromosomeViewer().zoomIn();
        } else if (action.equals(MenuUtils.ZOOM_OUT)) {
            redApplication.chromosomeViewer().zoomOut();
        } else if (action.equals(MenuUtils.SET_ZOOM_LEVEL)) {
            new DataZoomSelector(redApplication);
        } else if (action.equals(MenuUtils.MOVE_LEFT)) {
            redApplication.chromosomeViewer().moveLeft();
        } else if (action.equals(MenuUtils.MOVE_RIGHT)) {
            redApplication.chromosomeViewer().moveRight();
        } else if (action.equals(MenuUtils.GOTO_POSITION)) {
            new GotoDialog(redApplication);
        } else if (action.equals(MenuUtils.GOTO_WINDOW)) {
            new GotoWindowDialog(redApplication);
        }
        // --------------------FilterMenu--------------------
        else if (action.equals(MenuUtils.BASIC_FILTER)) {

        } else if (action.equals(MenuUtils.KNOWN_SNVS_FILTER)) {

        } else if (action.equals(MenuUtils.REPEATED_FILTER)) {

        } else if (action.equals(MenuUtils.RNA_DNA_FILTER)) {

        } else if (action.equals(MenuUtils.COMPREHENSIVE_FILTER)) {

        } else if (action.equals(MenuUtils.STATISTICAL_FILTER)) {

        }
        // --------------------ReportsMenu------------------
        else if (action.equals(MenuUtils.VARIANT_DISTRIBUTION)) {

        } else if (action.equals(MenuUtils.BAR_CHART)) {

        } else if (action.equals(MenuUtils.FILTER_REPORTS)) {

        }
        // --------------------HelpMenu---------------------
        else if (action.equals(MenuUtils.WELCOME)) {
            new WelcomePanel(redApplication);
        } else if (action.equals(MenuUtils.HELP_CONTENTS)) {
            new HelpDialog(new File(ClassLoader.getSystemResource("Help")
                    .getFile().replaceAll("%20", " ")));
        } else if (action.equals(MenuUtils.CHECK_FOR_UPDATES)) {

        } else if (action.equals(MenuUtils.ABOUT_RED)) {
            new AboutDialog();
        }
    }

    public void cacheFolderChecked() {
        newProject.setEnabled(true);
        openProject.setEnabled(true);
        loadGenome.setEnabled(true);
    }

    /**
     * Data loaded.
     */
    public void dataLoaded() {
        genomeLoadedMenu();
        viewMenu.setEnabled(true);
        filterMenu.setEnabled(true);
        setDataTracks.setEnabled(true);
        reportsMenu.setEnabled(true);
    }

    /**
     * Genome loaded.
     */
    public void genomeLoadedMenu() {
        updateVisibleToolBars();
        importData.setEnabled(true);
        viewMenu.setEnabled(true);
        saveProject.setEnabled(true);
        saveProjectAs.setEnabled(true);
        find.setEnabled(true);
        gotoMenuItem.setEnabled(true);
        exportImage.setEnabled(true);
    }

    /**
     * Resets the menu availability to its default state. Should be called when
     * a new dataset is loaded.
     */
    public void resetMenus() {

        saveProject.setEnabled(false);
        saveProjectAs.setEnabled(false);
        importData.setEnabled(false);
        exportImage.setEnabled(false);

        viewMenu.setEnabled(false);
        filterMenu.setEnabled(false);
        reportsMenu.setEnabled(false);

    }

    public JPanel toolbarPanel() {
        return toolbarPanel;
    }

    private void updateVisibleToolBars() {
        Vector<JToolBar> visibleToolBars = new Vector<JToolBar>();

        for (int i = 0; i < redToolbar.length; i++) {
            if (redToolbar[i].shown()) {
                visibleToolBars.add(redToolbar[i]);
            }
        }

        toolbarPanel.setToolBars(visibleToolBars.toArray(new JToolBar[0]));
    }

    /**
     * The Class FileOpener.
     */
    private class FileOpener implements ActionListener {

        /**
         * The application.
         */
        private final REDApplication application;

        /**
         * The file.
         */
        private final File file;

        /**
         * Instantiates a new file opener.
         *
         * @param application the application
         * @param file        the file
         */
        public FileOpener(REDApplication application, File file) {
            this.application = application;
            this.file = file;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        public void actionPerformed(ActionEvent e) {
            application.loadProject(file);
        }
    }
}
