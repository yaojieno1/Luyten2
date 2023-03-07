package us.deathmarine.luyten;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.*;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

import com.strobel.Procyon;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.languages.Language;
import com.strobel.decompiler.languages.Languages;

/**
 * Main menu (only MainWindow should be called from here)
 */
public class MainMenuBar extends JMenuBar {
    private static final long serialVersionUID = -7949855817172562075L;
    private final MainWindow mainWindow;
    private final Map<String, Language> languageLookup = new HashMap<String, Language>();

    private JMenu recentFiles;
    private JMenuItem clearRecentFiles;
    private JMenuItem clearTempFiles;
    private JCheckBoxMenuItem flattenSwitchBlocks;
    private JCheckBoxMenuItem forceExplicitImports;
    private JCheckBoxMenuItem forceExplicitTypes;
    private JCheckBoxMenuItem showSyntheticMembers;
    private JCheckBoxMenuItem excludeNestedTypes;
    private JCheckBoxMenuItem retainRedundantCasts;
    private JCheckBoxMenuItem unicodeReplacement;
    private JCheckBoxMenuItem debugLineNumbers;
    private JCheckBoxMenuItem showDebugInfo;
    private JCheckBoxMenuItem bytecodeLineNumbers;
    private JRadioButtonMenuItem java;
    private JRadioButtonMenuItem bytecode;
    private JRadioButtonMenuItem bytecodeAST;
    private JRadioButtonMenuItem languageEnglish;
    private JRadioButtonMenuItem languageChinese;
    private ButtonGroup languagesGroup;
    private ButtonGroup themesGroup;
    private ButtonGroup showLanguagesGroup;
    private JCheckBoxMenuItem packageExplorerStyle;
    private JCheckBoxMenuItem filterOutInnerClassEntries;
    private JCheckBoxMenuItem singleClickOpenEnabled;
    private JCheckBoxMenuItem exitByEscEnabled;
    private DecompilerSettings settings;
    private LuytenPreferences luytenPrefs;
    private Map<String, JMenuItem> showLanguagesMap = new HashMap<>();

    public MainMenuBar(MainWindow mainWnd) {
        this.mainWindow = mainWnd;
        mainWindow.setShowLanguagesMap(showLanguagesMap);
        final ConfigSaver configSaver = ConfigSaver.getLoadedInstance();
        settings = configSaver.getDecompilerSettings();
        luytenPrefs = configSaver.getLuytenPreferences();

        final JMenu fileMenu = new JMenu("File");
        showLanguagesMap.put("file", fileMenu);
        fileMenu.add(new JMenuItem("..."));
        this.add(fileMenu);
        final JMenu editMenu = new JMenu("Edit");
        showLanguagesMap.put("edit", editMenu);
        editMenu.add(new JMenuItem("..."));
        this.add(editMenu);
        final JMenu themesMenu = new JMenu("Themes");
        showLanguagesMap.put("themes", themesMenu);
        themesMenu.add(new JMenuItem("..."));
        this.add(themesMenu);
        final JMenu operationMenu = new JMenu("Operation");
        showLanguagesMap.put("operation", operationMenu);
        operationMenu.add(new JMenuItem("..."));
        this.add(operationMenu);
        final JMenu settingsMenu = new JMenu("Settings");
        showLanguagesMap.put("settings", settingsMenu);
        settingsMenu.add(new JMenuItem("..."));
        this.add(settingsMenu);
        final JMenu helpMenu = new JMenu("Help");
        showLanguagesMap.put("help", helpMenu);
        helpMenu.add(new JMenuItem("..."));
        this.add(helpMenu);

        // start quicker
        new Thread() {
            public void run() {
                try {
                    // build menu later
                    buildFileMenu(fileMenu);
                    refreshMenuPopup(fileMenu);

                    buildEditMenu(editMenu);
                    refreshMenuPopup(editMenu);

                    buildThemesMenu(themesMenu);
                    refreshMenuPopup(themesMenu);

                    buildOperationMenu(operationMenu);
                    refreshMenuPopup(operationMenu);

                    buildSettingsMenu(settingsMenu, configSaver);
                    refreshMenuPopup(settingsMenu);

                    buildHelpMenu(helpMenu);
                    refreshMenuPopup(helpMenu);

                    updateRecentFiles();
                    updateDisplayLanguage();
                } catch (Exception e) {
                    Luyten.showExceptionDialog("Exception!", e);
                }
            }

            // refresh currently opened menu
            // (if user selected a menu before it was ready)
            private void refreshMenuPopup(JMenu menu) {
                try {
                    if (menu.isPopupMenuVisible()) {
                        menu.getPopupMenu().setVisible(false);
                        menu.getPopupMenu().setVisible(true);
                    }
                } catch (Exception e) {
                    Luyten.showExceptionDialog("Exception!", e);
                }
            }
        }.start();
    }

    public void updateRecentFiles() {
        if (RecentFiles.paths.isEmpty()) {
            recentFiles.setEnabled(false);
            clearRecentFiles.setEnabled(false);
            return;
        } else {
            recentFiles.setEnabled(true);
            clearRecentFiles.setEnabled(true);
        }

        recentFiles.removeAll();
        ListIterator<String> li = RecentFiles.paths.listIterator(RecentFiles.paths.size());
        boolean rfSaveNeeded = false;

        while (li.hasPrevious()) {
            String path = li.previous();
            final File file = new File(path);

            if (!file.exists()) {
                rfSaveNeeded = true;
                continue;
            }

            JMenuItem menuItem = new JMenuItem(path);
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mainWindow.loadNewFile(file);
                }
            });
            recentFiles.add(menuItem);
        }

        if (rfSaveNeeded) RecentFiles.save();
    }

    private void updateDisplayLanguage() {
        mainWindow.changeShowLanguage(luytenPrefs.getDisplayLanguage());
    }

    private void buildFileMenu(final JMenu fileMenu) {
        fileMenu.removeAll();
        JMenuItem menuItem = new JMenuItem("Open File...");
        showLanguagesMap.put("openFile", menuItem);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindow.onOpenFileMenu();
            }
        });
        fileMenu.add(menuItem);
        fileMenu.addSeparator();

        menuItem = new JMenuItem("Close File");
        showLanguagesMap.put("closeFile", menuItem);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JTabbedPane house = mainWindow.getSelectedModel().house;

                if (e.getModifiers() != 2 || house.getTabCount() == 0)
                    mainWindow.onCloseFileMenu();
                else {
                    mainWindow.getSelectedModel().closeOpenTab(house.getSelectedIndex());
                }
            }
        });
        fileMenu.add(menuItem);
        fileMenu.addSeparator();

        menuItem = new JMenuItem("Save As...");
        showLanguagesMap.put("saveAs", menuItem);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindow.onSaveAsMenu();
            }
        });
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("Save All...");
        showLanguagesMap.put("saveAll", menuItem);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindow.onSaveAllMenu();
            }
        });
        fileMenu.add(menuItem);
        fileMenu.addSeparator();

        recentFiles = new JMenu("Recent Files");
        showLanguagesMap.put("recentFiles", recentFiles);
        fileMenu.add(recentFiles);

        clearRecentFiles = new JMenuItem("Clear Recent Files");
        showLanguagesMap.put("clearRecentFiles", clearRecentFiles);
        clearRecentFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RecentFiles.paths.clear();
                RecentFiles.save();
                updateRecentFiles();
            }
        });
        fileMenu.add(clearRecentFiles);

        clearTempFiles = new JMenuItem("Clear Temp Folder");
        showLanguagesMap.put("clearTempFolder", clearTempFiles);
        clearTempFiles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean deleteSuccess = FileUtil.deleteDir(CommonUtil.getTempRoot());
                if (deleteSuccess) {
                    JOptionPane.showMessageDialog(null, String.format("delete temporary directory success：%s ", CommonUtil.getTempRoot()));
                } else {
                    JOptionPane.showMessageDialog(null,
                            String.format("delete temporary directory fail：%s ,because some temporary files are being used , the temporary directory will be cleared automatically when the program exits", CommonUtil.getTempRoot()));
                }
            }
        });
        fileMenu.add(clearTempFiles);

        fileMenu.addSeparator();

        // Only add the exit command for non-OS X. OS X handles its close
        // automatically
        if (!Boolean.getBoolean("apple.laf.useScreenMenuBar")) {
            menuItem = new JMenuItem("Exit");
            showLanguagesMap.put("exit", menuItem);
            menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mainWindow.onExitMenu();
                }
            });
            fileMenu.add(menuItem);
        }
    }

    private void buildEditMenu(JMenu editMenu) {
        editMenu.removeAll();
        JMenuItem menuItem = new JMenuItem("Cut");
        showLanguagesMap.put("cut", menuItem);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.setEnabled(false);
        editMenu.add(menuItem);

        menuItem = new JMenuItem("Copy");
        showLanguagesMap.put("copy", menuItem);
        menuItem.addActionListener(new DefaultEditorKit.CopyAction());
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        editMenu.add(menuItem);

        menuItem = new JMenuItem("Paste");
        showLanguagesMap.put("paste", menuItem);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.setEnabled(false);
        editMenu.add(menuItem);

        editMenu.addSeparator();

        menuItem = new JMenuItem("Select All");
        showLanguagesMap.put("selectAll", menuItem);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindow.onSelectAllMenu();
            }
        });
        editMenu.add(menuItem);
        editMenu.addSeparator();

        menuItem = new JMenuItem("Find...");
        showLanguagesMap.put("find", menuItem);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindow.onFindMenu();
            }
        });
        editMenu.add(menuItem);

        menuItem = new JMenuItem("Find Next");
        showLanguagesMap.put("findNext", menuItem);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainWindow.findBox != null) mainWindow.findBox.fireExploreAction(true);
            }
        });
        editMenu.add(menuItem);

        menuItem = new JMenuItem("Find Previous");
        showLanguagesMap.put("findPrevious", menuItem);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_DOWN_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mainWindow.findBox != null) mainWindow.findBox.fireExploreAction(false);
            }
        });
        editMenu.add(menuItem);

        menuItem = new JMenuItem("Find All");
        showLanguagesMap.put("findAll", menuItem);
        menuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindow.onFindAllMenu();

            }
        });
        editMenu.add(menuItem);
    }

    private void buildThemesMenu(JMenu themesMenu) {
        themesMenu.removeAll();
        themesGroup = new ButtonGroup();
        JRadioButtonMenuItem a = new JRadioButtonMenuItem(new ThemeAction("Default", "default.xml"));
        showLanguagesMap.put("default", a);
        a.setSelected("default.xml".equals(luytenPrefs.getThemeXml()));
        themesGroup.add(a);
        themesMenu.add(a);

        a = new JRadioButtonMenuItem(new ThemeAction("Default-Alt", "default-alt.xml"));
        showLanguagesMap.put("defaultAlt", a);
        a.setSelected("default-alt.xml".equals(luytenPrefs.getThemeXml()));
        themesGroup.add(a);
        themesMenu.add(a);

        a = new JRadioButtonMenuItem(new ThemeAction("Dark", "dark.xml"));
        showLanguagesMap.put("dark", a);
        a.setSelected("dark.xml".equals(luytenPrefs.getThemeXml()));
        themesGroup.add(a);
        themesMenu.add(a);

        a = new JRadioButtonMenuItem(new ThemeAction("Eclipse", "eclipse.xml"));
        showLanguagesMap.put("eclipse", a);
        a.setSelected("eclipse.xml".equals(luytenPrefs.getThemeXml()));
        themesGroup.add(a);
        themesMenu.add(a);

        a = new JRadioButtonMenuItem(new ThemeAction("Visual Studio", "vs.xml"));
        showLanguagesMap.put("visualStudio", a);
        a.setSelected("vs.xml".equals(luytenPrefs.getThemeXml()));
        themesGroup.add(a);
        themesMenu.add(a);

        a = new JRadioButtonMenuItem(new ThemeAction("IntelliJ", "idea.xml"));
        showLanguagesMap.put("intelliJ", a);
        a.setSelected("idea.xml".equals(luytenPrefs.getThemeXml()));
        themesGroup.add(a);
        themesMenu.add(a);
    }

    private void buildOperationMenu(JMenu operationMenu) {
        operationMenu.removeAll();
        packageExplorerStyle = new JCheckBoxMenuItem("Package Explorer Style");
        showLanguagesMap.put("packageExplorerStyle", packageExplorerStyle);
        packageExplorerStyle.setSelected(luytenPrefs.isPackageExplorerStyle());
        packageExplorerStyle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                luytenPrefs.setPackageExplorerStyle(packageExplorerStyle.isSelected());
                mainWindow.onTreeSettingsChanged();
            }
        });
        operationMenu.add(packageExplorerStyle);

        filterOutInnerClassEntries = new JCheckBoxMenuItem("Filter Out Inner Class Entries");
        showLanguagesMap.put("filterOutInnerClassEntries", filterOutInnerClassEntries);
        filterOutInnerClassEntries.setSelected(luytenPrefs.isFilterOutInnerClassEntries());
        filterOutInnerClassEntries.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                luytenPrefs.setFilterOutInnerClassEntries(filterOutInnerClassEntries.isSelected());
                mainWindow.onTreeSettingsChanged();
            }
        });
        operationMenu.add(filterOutInnerClassEntries);

        singleClickOpenEnabled = new JCheckBoxMenuItem("Single Click Open");
        showLanguagesMap.put("singleClickOpen", singleClickOpenEnabled);
        singleClickOpenEnabled.setSelected(luytenPrefs.isSingleClickOpenEnabled());
        singleClickOpenEnabled.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                luytenPrefs.setSingleClickOpenEnabled(singleClickOpenEnabled.isSelected());
            }
        });
        operationMenu.add(singleClickOpenEnabled);

        exitByEscEnabled = new JCheckBoxMenuItem("Exit By Esc");
        showLanguagesMap.put("exitByEsc", exitByEscEnabled);
        exitByEscEnabled.setSelected(luytenPrefs.isExitByEscEnabled());
        exitByEscEnabled.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                luytenPrefs.setExitByEscEnabled(exitByEscEnabled.isSelected());
            }
        });
        operationMenu.add(exitByEscEnabled);
    }

    private void buildSettingsMenu(JMenu settingsMenu, ConfigSaver configSaver) {
        settingsMenu.removeAll();
        ActionListener settingsChanged = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    @Override
                    public void run() {
                        populateSettingsFromSettingsMenu();
                        mainWindow.onSettingsChanged();
                    }
                }.start();
            }
        };

        showLanguagesGroup = new ButtonGroup();
        languageEnglish = new JRadioButtonMenuItem("English");
        languageEnglish.addActionListener(showLanguageActionListener("en"));
        languageEnglish.setSelected("en".equals(luytenPrefs.getDisplayLanguage()));
        showLanguagesMap.put("english", languageEnglish);
        showLanguagesGroup.add(languageEnglish);
        settingsMenu.add(languageEnglish);
        languageChinese = new JRadioButtonMenuItem("Chinese");
        languageChinese.addActionListener(showLanguageActionListener("cn"));
        languageChinese.setSelected("cn".equals(luytenPrefs.getDisplayLanguage()));
        showLanguagesMap.put("chinese", languageChinese);
        showLanguagesGroup.add(languageChinese);
        settingsMenu.add(languageChinese);
        settingsMenu.addSeparator();

        flattenSwitchBlocks = new JCheckBoxMenuItem("Flatten Switch Blocks");
        showLanguagesMap.put("flattenSwitchBlocks", flattenSwitchBlocks);
        flattenSwitchBlocks.setSelected(settings.getFlattenSwitchBlocks());
        flattenSwitchBlocks.addActionListener(settingsChanged);
        settingsMenu.add(flattenSwitchBlocks);

        forceExplicitImports = new JCheckBoxMenuItem("Force Explicit Imports");
        showLanguagesMap.put("forceExplicitImports", forceExplicitImports);
        forceExplicitImports.setSelected(settings.getForceExplicitImports());
        forceExplicitImports.addActionListener(settingsChanged);
        settingsMenu.add(forceExplicitImports);

        forceExplicitTypes = new JCheckBoxMenuItem("Force Explicit Types");
        showLanguagesMap.put("forceExplicitTypes", forceExplicitTypes);
        forceExplicitTypes.setSelected(settings.getForceExplicitTypeArguments());
        forceExplicitTypes.addActionListener(settingsChanged);
        settingsMenu.add(forceExplicitTypes);

        showSyntheticMembers = new JCheckBoxMenuItem("Show Synthetic Members");
        showLanguagesMap.put("showSyntheticMembers", showSyntheticMembers);
        showSyntheticMembers.setSelected(settings.getShowSyntheticMembers());
        showSyntheticMembers.addActionListener(settingsChanged);
        settingsMenu.add(showSyntheticMembers);

        excludeNestedTypes = new JCheckBoxMenuItem("Exclude Nested Types");
        showLanguagesMap.put("excludeNestedTypes", excludeNestedTypes);
        excludeNestedTypes.setSelected(settings.getExcludeNestedTypes());
        excludeNestedTypes.addActionListener(settingsChanged);
        settingsMenu.add(excludeNestedTypes);

        retainRedundantCasts = new JCheckBoxMenuItem("Retain Redundant Casts");
        showLanguagesMap.put("retainRedundantCasts", retainRedundantCasts);
        retainRedundantCasts.setSelected(settings.getRetainRedundantCasts());
        retainRedundantCasts.addActionListener(settingsChanged);
        settingsMenu.add(retainRedundantCasts);

        unicodeReplacement = new JCheckBoxMenuItem("Enable Unicode Replacement");
        showLanguagesMap.put("enableUnicodeReplacement", unicodeReplacement);
        unicodeReplacement.setSelected(settings.isUnicodeOutputEnabled());
        unicodeReplacement.addActionListener(settingsChanged);
        settingsMenu.add(unicodeReplacement);

        debugLineNumbers = new JCheckBoxMenuItem("Show Debug Line Numbers");
        showLanguagesMap.put("showDebugLineNumbers", debugLineNumbers);
        debugLineNumbers.setSelected(settings.getShowDebugLineNumbers());
        debugLineNumbers.addActionListener(settingsChanged);
        settingsMenu.add(debugLineNumbers);

        JMenu debugSettingsMenu = new JMenu("Debug Settings");
        showLanguagesMap.put("debugSettings", debugSettingsMenu);
        showDebugInfo = new JCheckBoxMenuItem("Include Error Diagnostics");
        showLanguagesMap.put("includeErrorDiagnostics", showDebugInfo);
        showDebugInfo.setSelected(settings.getIncludeErrorDiagnostics());
        showDebugInfo.addActionListener(settingsChanged);

        debugSettingsMenu.add(showDebugInfo);
        settingsMenu.add(debugSettingsMenu);
        settingsMenu.addSeparator();

        languageLookup.put(Languages.java().getName(), Languages.java());
        languageLookup.put(Languages.bytecode().getName(), Languages.bytecode());
        languageLookup.put(Languages.bytecodeAst().getName(), Languages.bytecodeAst());

        languagesGroup = new ButtonGroup();
        java = new JRadioButtonMenuItem(Languages.java().getName());
        java.getModel().setActionCommand(Languages.java().getName());
        java.setSelected(Languages.java().getName().equals(settings.getLanguage().getName()));
        languagesGroup.add(java);
        settingsMenu.add(java);
        bytecode = new JRadioButtonMenuItem(Languages.bytecode().getName());
        bytecode.getModel().setActionCommand(Languages.bytecode().getName());
        bytecode.setSelected(Languages.bytecode().getName().equals(settings.getLanguage().getName()));
        languagesGroup.add(bytecode);
        settingsMenu.add(bytecode);
        bytecodeAST = new JRadioButtonMenuItem(Languages.bytecodeAst().getName());
        bytecodeAST.getModel().setActionCommand(Languages.bytecodeAst().getName());
        bytecodeAST.setSelected(Languages.bytecodeAst().getName().equals(settings.getLanguage().getName()));
        languagesGroup.add(bytecodeAST);
        settingsMenu.add(bytecodeAST);

        JMenu debugLanguagesMenu = new JMenu("Debug Languages");
        showLanguagesMap.put("debugLanguages", debugLanguagesMenu);
        for (final Language language : Languages.debug()) {
            final JRadioButtonMenuItem m = new JRadioButtonMenuItem(language.getName());
            m.getModel().setActionCommand(language.getName());
            m.setSelected(language.getName().equals(settings.getLanguage().getName()));
            languagesGroup.add(m);
            debugLanguagesMenu.add(m);
            languageLookup.put(language.getName(), language);
        }
        for (AbstractButton button : Collections.list(languagesGroup.getElements())) {
            button.addActionListener(settingsChanged);
        }
        settingsMenu.add(debugLanguagesMenu);

        bytecodeLineNumbers = new JCheckBoxMenuItem("Show Line Numbers In Bytecode");
        showLanguagesMap.put("showLineNumbersInBytecode", bytecodeLineNumbers);
        bytecodeLineNumbers.setSelected(settings.getIncludeLineNumbersInBytecode());
        bytecodeLineNumbers.addActionListener(settingsChanged);
        settingsMenu.add(bytecodeLineNumbers);
    }

    private ActionListener showLanguageActionListener(String command) {
        return e -> {
            mainWindow.changeShowLanguage(command);
        };
    }

    private void buildHelpMenu(JMenu helpMenu) {
        helpMenu.removeAll();
        JMenuItem menuItem = new JMenuItem("Legal");
        showLanguagesMap.put("legal", menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindow.onLegalMenu();
            }
        });
        helpMenu.add(menuItem);
        JMenu menuDebug = new JMenu("Debug");
        showLanguagesMap.put("debug", menuDebug);
        menuItem = new JMenuItem("List JVM Classes");
        showLanguagesMap.put("listJVMClasses", menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainWindow.onListLoadedClasses();
            }
        });
        menuDebug.add(menuItem);
        helpMenu.add(menuDebug);
        menuItem = new JMenuItem("About");
        showLanguagesMap.put("about", menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JPanel pane = new JPanel();
                pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
                JLabel title = new JLabel("Luyten " + Luyten.getVersion());
                title.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
                pane.add(title);
                pane.add(new JLabel("by Deathmarine"));
                String project = "https://github.com/deathmarine/Luyten/";
                JLabel link = new JLabel("<HTML><FONT color=\"#000099\"><U>" + project + "</U></FONT></HTML>");
                link.setCursor(new Cursor(Cursor.HAND_CURSOR));
                link.addMouseListener(new LinkListener(project, link));
                pane.add(link);
                pane.add(new JLabel("Contributions By:"));
                pane.add(new JLabel("zerdei, toonetown, dstmath"));
                pane.add(new JLabel("virustotalop, xtrafrancyz,"));
                pane.add(new JLabel("mbax, quitten, mstrobel,"));
                pane.add(new JLabel("FisheyLP, and Syquel"));
                pane.add(new JLabel(" "));
                pane.add(new JLabel("Powered By:"));
                String procyon = "https://bitbucket.org/mstrobel/procyon";
                link = new JLabel("<HTML><FONT color=\"#000099\"><U>" + procyon + "</U></FONT></HTML>");
                link.setCursor(new Cursor(Cursor.HAND_CURSOR));
                link.addMouseListener(new LinkListener(procyon, link));
                pane.add(link);
                pane.add(new JLabel("Version: " + Procyon.version()));
                pane.add(new JLabel("(c) 2018 Mike Strobel"));
                String rsyntax = "https://github.com/bobbylight/RSyntaxTextArea";
                link = new JLabel("<HTML><FONT color=\"#000099\"><U>" + rsyntax + "</U></FONT></HTML>");
                link.setCursor(new Cursor(Cursor.HAND_CURSOR));
                link.addMouseListener(new LinkListener(rsyntax, link));
                pane.add(link);
                pane.add(new JLabel("Version: 3.0.2"));
                pane.add(new JLabel("(c) 2019 Robert Futrell"));
                pane.add(new JLabel(" "));
                JOptionPane.showMessageDialog(null, pane);
            }
        });
        helpMenu.add(menuItem);
    }

    private void populateSettingsFromSettingsMenu() {
        // synchronized: do not disturb decompiler at work (synchronize every
        // time before run decompiler)
        synchronized (settings) {
            settings.setFlattenSwitchBlocks(flattenSwitchBlocks.isSelected());
            settings.setForceExplicitImports(forceExplicitImports.isSelected());
            settings.setShowSyntheticMembers(showSyntheticMembers.isSelected());
            settings.setExcludeNestedTypes(excludeNestedTypes.isSelected());
            settings.setForceExplicitTypeArguments(forceExplicitTypes.isSelected());
            settings.setRetainRedundantCasts(retainRedundantCasts.isSelected());
            settings.setIncludeErrorDiagnostics(showDebugInfo.isSelected());
            settings.setUnicodeOutputEnabled(unicodeReplacement.isSelected());
            settings.setShowDebugLineNumbers(debugLineNumbers.isSelected());
            //
            // Note: You shouldn't ever need to set this. It's only for
            // languages that support catch
            // blocks without an exception variable. Java doesn't allow this. I
            // think Scala does.
            //
            // settings.setAlwaysGenerateExceptionVariableForCatchBlocks(true);
            //

            final ButtonModel selectedLanguage = languagesGroup.getSelection();
            if (selectedLanguage != null) {
                final Language language = languageLookup.get(selectedLanguage.getActionCommand());

                if (language != null)
                    settings.setLanguage(language);
            }

            if (java.isSelected()) {
                settings.setLanguage(Languages.java());
            } else if (bytecode.isSelected()) {
                settings.setLanguage(Languages.bytecode());
            } else if (bytecodeAST.isSelected()) {
                settings.setLanguage(Languages.bytecodeAst());
            }
            settings.setIncludeLineNumbersInBytecode(bytecodeLineNumbers.isSelected());
        }
    }

    private class ThemeAction extends AbstractAction {
        private static final long serialVersionUID = -6618680171943723199L;
        private String xml;

        public ThemeAction(String name, String xml) {
            putValue(NAME, name);
            this.xml = xml;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            luytenPrefs.setThemeXml(xml);
            mainWindow.onThemesChanged();
        }
    }

    private class LinkListener extends MouseAdapter {
        String link;
        JLabel label;

        public LinkListener(String link, JLabel label) {
            this.link = link;
            this.label = label;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            try {
                Desktop.getDesktop().browse(new URI(link));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            label.setText("<HTML><FONT color=\"#00aa99\"><U>" + link + "</U></FONT></HTML>");
        }

        @Override
        public void mouseExited(MouseEvent e) {
            label.setText("<HTML><FONT color=\"#000099\"><U>" + link + "</U></FONT></HTML>");
        }

    }
}
