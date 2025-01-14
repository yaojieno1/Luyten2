package us.deathmarine.luyten;

import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * Dispatcher
 */
public class MainWindow extends JFrame {
    private static final long serialVersionUID = 5265556630724988013L;

    private static final String TITLE = "Luyten";
    private static final String DEFAULT_TAB = "#DEFAULT";

    private JProgressBar bar;
    private JLabel label;
    FindBox findBox;
    private FindAllBox findAllBox;
    private ConfigSaver configSaver;
    private WindowPosition windowPosition;
    private LuytenPreferences luytenPrefs;
    private FileDialog fileDialog;
    private FileSaver fileSaver;
    private JTabbedPane jarsTabbedPane;
    private Map<String, Model> jarModels;
    public MainMenuBar mainMenuBar;
    private Map<String, String> jarAndNameMap;
    private Map<String, AtomicInteger> jarNameIndexMap;
    private Map<String, Set<TitledBorder>> borderMap;
    private Map<String, JMenuItem> showLanguagesMap;
    private Map<String, Set<JComponent>> otherPageMap;
    private Map<String, Set<Dialog>> otherDialogMap;

    public MainWindow(File fileFromCommandLine) {
        configSaver = ConfigSaver.getLoadedInstance();
        windowPosition = configSaver.getMainWindowPosition();
        luytenPrefs = configSaver.getLuytenPreferences();

        borderMap = new HashMap<>();
        otherPageMap = new LinkedHashMap<>();
        otherDialogMap = new LinkedHashMap<>();
        showLanguagesMap = new LinkedHashMap<>();
        jarModels = new HashMap<String, Model>();
        jarAndNameMap = new HashMap<>();
        jarNameIndexMap = new HashMap<>();
        mainMenuBar = new MainMenuBar(this);
        this.setJMenuBar(mainMenuBar);

        this.adjustWindowPositionBySavedState();
        this.setHideFindBoxOnMainWindowFocus();
        this.setShowFindAllBoxOnMainWindowFocus();
        this.setQuitOnWindowClosing();
        this.setTitle(TITLE);
        this.setIconImage(new ImageIcon(
                Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/Luyten.png"))).getImage());

        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel();
        label.setHorizontalAlignment(JLabel.LEFT);
        panel1.setBorder(new BevelBorder(BevelBorder.LOWERED));
        panel1.setPreferredSize(new Dimension(this.getWidth() / 2, 20));
        panel1.add(label);

        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bar = new JProgressBar();

        bar.setStringPainted(true);
        bar.setOpaque(false);
        bar.setVisible(false);
        panel2.setPreferredSize(new Dimension(this.getWidth() / 3, 20));
        panel2.add(bar);

        jarsTabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        jarsTabbedPane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tab_placement, int run_count, int max_tab_height) {
                if (jarsTabbedPane.indexOfTab(DEFAULT_TAB) == -1)
                    return super.calculateTabAreaHeight(tab_placement, run_count, max_tab_height);
                else
                    return 0;
            }
        });
        jarsTabbedPane.addTab(DEFAULT_TAB, new Model(this));
        this.getContentPane().add(jarsTabbedPane);

        JSplitPane spt = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panel1, panel2) {
            private static final long serialVersionUID = 2189946972124687305L;
            private final int location = 400;

            {
                setDividerLocation(location);
            }

            @Override
            public int getDividerLocation() {
                return location;
            }

            @Override
            public int getLastDividerLocation() {
                return location;
            }
        };
        spt.setBorder(new BevelBorder(BevelBorder.LOWERED));
        spt.setPreferredSize(new Dimension(this.getWidth(), 24));
        this.add(spt, BorderLayout.SOUTH);
        Model jarModel = null;
        if (fileFromCommandLine != null) {
            jarModel = loadNewFile(fileFromCommandLine);
        }

        try {
            DropTarget dt = new DropTarget();
            dt.addDropTargetListener(new DropListener(this));
            this.setDropTarget(dt);
        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        }

        fileDialog = new FileDialog(this);
        fileSaver = new FileSaver(bar, label);

        if (jarModel != null) {
            this.setExitOnEscWhenEnabled(jarModel);
        }

        if (jarModel != null && (fileFromCommandLine.getName().toLowerCase().endsWith(".jar")
                || fileFromCommandLine.getName().toLowerCase().endsWith(".zip")
                || fileFromCommandLine.getName().toLowerCase().endsWith(".war")
                || fileFromCommandLine.getName().toLowerCase().endsWith(".ear"))) {
            jarModel.startWarmUpThread();
        }

        if (RecentFiles.load() > 0) mainMenuBar.updateRecentFiles();
    }

    public Map<String, Set<TitledBorder>> getBorderMap() {
        return borderMap;
    }

    public Map<String, Set<JComponent>> getOtherPageMap() {
        return otherPageMap;
    }

    public Map<String, Set<Dialog>> getOtherDialogMap() {
        return otherDialogMap;
    }

    public Map<String, JMenuItem> getShowLanguagesMap() {
        return showLanguagesMap;
    }

    public void addBorderMap(String key, TitledBorder border) {
        if (borderMap.containsKey(key)) {
            borderMap.get(key).add(border);
        } else {
            Set<TitledBorder> titledBorders = new LinkedHashSet<>();
            titledBorders.add(border);
            borderMap.put(key, titledBorders);
        }
    }

    public void addOtherPageMap(String key, JComponent component) {
        if (otherPageMap.containsKey(key)) {
            otherPageMap.get(key).add(component);
        } else {
            Set<JComponent> components = new LinkedHashSet<>();
            components.add(component);
            otherPageMap.put(key, components);
        }
    }

    public void addOtherDialogMap(String key, Dialog dialog) {
        if (otherDialogMap.containsKey(key)) {
            otherDialogMap.get(key).add(dialog);
        } else {
            Set<Dialog> dialogs = new LinkedHashSet<>();
            dialogs.add(dialog);
            otherDialogMap.put(key, dialogs);
        }
    }

    public void setShowLanguagesMap(Map<String, JMenuItem> showLanguagesMap) {
        this.showLanguagesMap = showLanguagesMap;
    }

    public String getShowLanguageText(String key) {
        Map<?, ?> showLanguageSetting = CommonUtil.getShowLanguageSetting();
        return CommonUtil.getShowLanguageInfo(showLanguageSetting, key, luytenPrefs.getDisplayLanguage());
    }

    public void updateDisplayLanguage() {
        changeShowLanguage(luytenPrefs.getDisplayLanguage());
    }

    public void changeShowLanguage(String command) {
        luytenPrefs.setDisplayLanguage(command);
        Map<?, ?> showLanguageSetting = CommonUtil.getShowLanguageSetting();
        showLanguagesMap.forEach((key, value) -> {
            String showLanguageInfo = CommonUtil.getShowLanguageInfo(showLanguageSetting, key, command);
            if (showLanguageInfo != null) {
                value.setText(showLanguageInfo);
            }
        });
        getBorderMap().forEach((key, value) -> {
            String showLanguageInfo = CommonUtil.getShowLanguageInfo(showLanguageSetting, key, command);
            if (showLanguageInfo != null) {
                value.forEach(item -> {
                    item.setTitle(showLanguageInfo);
                });
            }
        });
        getOtherDialogMap().forEach((key, value) -> {
            String showLanguageInfo = CommonUtil.getShowLanguageInfo(showLanguageSetting, key, command);
            if (showLanguageInfo != null) {
                value.forEach(item -> {
                    item.setTitle(showLanguageInfo);
                });
            }
        });
        getOtherPageMap().forEach((key, value) -> {
            String showLanguageInfo = CommonUtil.getShowLanguageInfo(showLanguageSetting, key, command);
            if (showLanguageInfo != null) {
                value.forEach(item -> {
                    Class<? extends JComponent> aClass = item.getClass();
                    try {
                        Method setText = aClass.getMethod("setText", String.class);
                        setText.invoke(item, showLanguageInfo);
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                });
            }
        });
    }

    private void createDefaultTab() {
        jarsTabbedPane.addTab(DEFAULT_TAB, new Model(this));
    }

    private void removeDefaultTab() {
        jarsTabbedPane.remove(jarsTabbedPane.indexOfTab(DEFAULT_TAB));
    }

    public void onOpenFileMenu(File selectedFile, boolean isAppendHistory) {
        if (selectedFile != null) {
            if (!isAppendHistory) {
                RecentFiles.setNowNotAddHistoryPath(selectedFile.getAbsolutePath());
            }
            System.out.println("[Open]: Opening " + selectedFile.getAbsolutePath());
            this.loadNewFile(selectedFile);
        }
    }

    public void onOpenFileMenu() {
        File selectedFile = fileDialog.doOpenDialog();
        if (selectedFile != null) {
            System.out.println("[Open]: Opening " + selectedFile.getAbsolutePath());
            this.loadNewFile(selectedFile);
        }
    }

    public Model loadNewFile(final File file) {
        // In case we open the same file again
        // we remove the old entry to force a refresh
        if (jarModels.containsKey(file.getAbsolutePath())) {
            jarModels.remove(file.getAbsolutePath());
            String name = jarAndNameMap.get(file.getAbsolutePath());
            int index = jarsTabbedPane.indexOfTab(name);
            jarsTabbedPane.remove(index);
            jarAndNameMap.remove(file.getAbsolutePath());
            if (jarAndNameMap.keySet().stream().noneMatch(item -> FileUtil.getFile(item).getName().equals(file.getName()))) {
                jarNameIndexMap.remove(file.getName());
            }
        }

        String name = file.getName();
        if (jarNameIndexMap.containsKey(name)) {
            name = "[" + jarNameIndexMap.get(name).getAndIncrement() + "]" + name;
        } else {
            jarNameIndexMap.put(name, new AtomicInteger(1));
        }
        jarAndNameMap.put(file.getAbsolutePath(), name);

        Model jarModel = new Model(this);
        jarModel.loadFile(file);
        jarModels.put(file.getAbsolutePath(), jarModel);
        jarsTabbedPane.addTab(name, jarModel);
        jarsTabbedPane.setSelectedComponent(jarModel);

        final String tabName = name;
        int index = jarsTabbedPane.indexOfTab(tabName);
        Model.Tab tabUI = new Model.Tab(tabName, new Callable<Void>() {
            @Override
            public Void call() {
                Model model = jarModels.get(file.getAbsolutePath());
                if (model != null) {
                    borderMap.values().forEach(itemSet -> {
                        List<TitledBorder> titledBorders = model.getTitledBorders();
                        titledBorders.forEach(itemSet::remove);
                    });
                }
                int index = jarsTabbedPane.indexOfTab(tabName);
                jarModels.remove(file.getAbsolutePath());
                jarsTabbedPane.remove(index);
                jarAndNameMap.remove(file.getAbsolutePath());
                if (jarAndNameMap.keySet().stream().noneMatch(item -> FileUtil.getFile(item).getName().equals(file.getName()))) {
                    jarNameIndexMap.remove(file.getName());
                }

                if (jarsTabbedPane.getTabCount() == 0) {
                    createDefaultTab();
                }
                return null;
            }
        });
        jarsTabbedPane.setTabComponentAt(index, tabUI);
        if (jarsTabbedPane.indexOfTab(DEFAULT_TAB) != -1 && jarsTabbedPane.getTabCount() > 1) {
            removeDefaultTab();
        }
        return jarModel;
    }

    public void onCloseFileMenu() {
        this.getSelectedModel().closeFile();
        jarModels.remove(getSelectedModel());
    }

    public void onSaveAsMenu() {
        RSyntaxTextArea pane = this.getSelectedModel().getCurrentTextArea();
        if (pane == null)
            return;
        String tabTitle = this.getSelectedModel().getCurrentTabTitle();
        if (tabTitle == null)
            return;

        String recommendedFileName = tabTitle.replace(".class", ".java");
        File selectedFile = fileDialog.doSaveDialog(recommendedFileName);
        if (selectedFile != null) {
            fileSaver.saveText(pane.getText(), selectedFile);
        }
    }

    public void onSaveAllMenu() {
        File openedFile = this.getSelectedModel().getOpenedFile();
        if (openedFile == null)
            return;

        String fileName = openedFile.getName();
        if (fileName.endsWith(".class")) {
            fileName = fileName.replace(".class", ".java");
        } else if (fileName.toLowerCase().endsWith(".jar")
                || fileName.toLowerCase().endsWith(".zip")
                || fileName.toLowerCase().endsWith(".war")
                || fileName.toLowerCase().endsWith(".ear")) {
            fileName = fileName
                    .replaceAll("\\.zip", ".src.zip")
                    .replaceAll("\\.[jJwWeE][aA][rR]", ".src.zip");
        } else {
            fileName = "saved-" + fileName;
        }

        File selectedFileToSave = fileDialog.doSaveAllDialog(fileName);
        if (selectedFileToSave != null) {
            fileSaver.saveAllDecompiled(openedFile, selectedFileToSave);
        }
    }

    public void onExitMenu() {
        quit();
    }

    public void onSelectAllMenu() {
        try {
            RSyntaxTextArea pane = this.getSelectedModel().getCurrentTextArea();
            if (pane != null) {
                pane.requestFocusInWindow();
                pane.setSelectionStart(0);
                pane.setSelectionEnd(pane.getText().length());
            }
        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
    }

    public void onFindMenu() {
        try {
            RSyntaxTextArea pane = this.getSelectedModel().getCurrentTextArea();
            if (pane != null) {
                if (findBox == null) {
                    findBox = new FindBox(this);
                    updateDisplayLanguage();
                }
                findBox.showFindBox();
            }
        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
    }

    public void onFindAllMenu() {
        try {
            if (findAllBox == null) {
                findAllBox = new FindAllBox(this);
                updateDisplayLanguage();
            }
            findAllBox.showFindBox();

        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
    }

    public void onLegalMenu() {
        new Thread() {
            public void run() {
                try {
                    bar.setVisible(true);
                    bar.setIndeterminate(true);
                    String legalStr = getLegalStr();
                    getSelectedModel().showLegal(legalStr);
                } finally {
                    bar.setIndeterminate(false);
                    bar.setVisible(false);
                }
            }
        }.start();
    }

    public void onListLoadedClasses() {
        try {
            StringBuilder sb = new StringBuilder();
            ClassLoader myCL = Thread.currentThread().getContextClassLoader();
            bar.setVisible(true);
            bar.setIndeterminate(true);
            while (myCL != null) {
                sb.append("ClassLoader: " + myCL + "\n");
                for (Iterator<?> iter = list(myCL); iter.hasNext(); ) {
                    sb.append("\t" + iter.next() + "\n");
                }
                myCL = myCL.getParent();
            }
            this.getSelectedModel().show("Debug", sb.toString());
        } finally {
            bar.setIndeterminate(false);
            bar.setVisible(false);
        }
    }

    private static Iterator<?> list(ClassLoader CL) {
        Class<?> CL_class = CL.getClass();
        while (CL_class != java.lang.ClassLoader.class) {
            CL_class = CL_class.getSuperclass();
        }
        java.lang.reflect.Field ClassLoader_classes_field;
        try {
            ClassLoader_classes_field = CL_class.getDeclaredField("classes");
            ClassLoader_classes_field.setAccessible(true);
            Vector<?> classes = (Vector<?>) ClassLoader_classes_field.get(CL);
            return classes.iterator();
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
        return null;
    }

    private String getLegalStr() {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/distfiles/Procyon.License.txt")));
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
            sb.append("\n\n\n\n\n");
            reader = new BufferedReader(
                    new InputStreamReader(getClass().getResourceAsStream("/distfiles/RSyntaxTextArea.License.txt")));
            while ((line = reader.readLine()) != null)
                sb.append(line).append("\n");
        } catch (IOException e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
        return sb.toString();
    }

    public void onThemesChanged() {
        for (Model jarModel : jarModels.values()) {
            jarModel.changeTheme(luytenPrefs.getThemeXml());
            luytenPrefs.setFont_size(jarModel.getTheme().baseFont.getSize());
        }
    }

    public void onSettingsChanged() {
        for (Model jarModel : jarModels.values()) {
            jarModel.updateOpenClasses();
        }
    }

    public void onTreeSettingsChanged() {
        for (Model jarModel : jarModels.values()) {
            jarModel.updateTree();
        }
    }

    public void onFileDropped(File file) {
        if (file != null) {
            this.loadNewFile(file);
        }
    }

    public void onFileLoadEnded(File file, boolean isSuccess) {
        try {
            if (file != null && isSuccess) {
                this.setTitle(TITLE + " - " + file.getName());
            } else {
                this.setTitle(TITLE);
            }
        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        }
    }

    public void onNavigationRequest(String uniqueStr) {
        this.getSelectedModel().navigateTo(uniqueStr);
    }

    private void adjustWindowPositionBySavedState() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (!windowPosition.isSavedWindowPositionValid()) {
            final Dimension center = new Dimension((int) (screenSize.width * 0.75), (int) (screenSize.height * 0.75));
            final int x = (int) (center.width * 0.2);
            final int y = (int) (center.height * 0.2);
            this.setBounds(x, y, center.width, center.height);

        } else if (windowPosition.isFullScreen()) {
            int heightMinusTray = screenSize.height;
            if (screenSize.height > 30)
                heightMinusTray -= 30;
            this.setBounds(0, 0, screenSize.width, heightMinusTray);
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);

            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    if (MainWindow.this.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                        windowPosition.setFullScreen(false);
                        if (windowPosition.isSavedWindowPositionValid()) {
                            MainWindow.this.setBounds(windowPosition.getWindowX(), windowPosition.getWindowY(),
                                    windowPosition.getWindowWidth(), windowPosition.getWindowHeight());
                        }
                        MainWindow.this.removeComponentListener(this);
                    }
                }
            });

        } else {
            this.setBounds(windowPosition.getWindowX(), windowPosition.getWindowY(), windowPosition.getWindowWidth(),
                    windowPosition.getWindowHeight());
        }
    }

    private void setHideFindBoxOnMainWindowFocus() {
        this.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (findBox != null && findBox.isVisible()) {
                    findBox.setVisible(false);
                }
            }
        });
    }

    private void setShowFindAllBoxOnMainWindowFocus() {
        this.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (findAllBox != null && findAllBox.isVisible()) {
                    findAllBox.setVisible(false);
                }
            }
        });
    }

    private void setQuitOnWindowClosing() {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
    }

    private void quit() {
        try {
            windowPosition.readPositionFromWindow(this);
            configSaver.saveConfig();
        } catch (Exception e) {
            Luyten.showExceptionDialog("Exception!", e);
        } finally {
            try {
                this.dispose();
            } finally {
                jarModels.values().forEach(Model::closeFile);
                FileUtil.deleteDir(CommonUtil.getTempRoot());
                System.exit(0);
            }
        }
    }

    private void setExitOnEscWhenEnabled(JComponent mainComponent) {
        Action escapeAction = new AbstractAction() {
            private static final long serialVersionUID = -3460391555954575248L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (luytenPrefs.isExitByEscEnabled()) {
                    quit();
                }
            }
        };
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        mainComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(escapeKeyStroke, "ESCAPE");
        mainComponent.getActionMap().put("ESCAPE", escapeAction);
    }

    public Model getSelectedModel() {
        return (Model) jarsTabbedPane.getSelectedComponent();
    }

    public JProgressBar getBar() {
        return bar;
    }

    public JLabel getLabel() {
        return label;
    }
}
