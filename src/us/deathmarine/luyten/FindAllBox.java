package us.deathmarine.luyten;

import com.strobel.assembler.metadata.TypeDefinition;
import com.strobel.assembler.metadata.TypeReference;
import com.strobel.core.StringUtilities;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import org.apache.commons.lang3.StringUtils;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;

/**
 *
 * this is the Find All Dialog
 * <p>
 * Change with 1.1
 * Adjust the find all box width
 * </p>
 *
 * @author clevertension
 * @version 1.1
 */
public class FindAllBox extends JDialog {
	private static final long serialVersionUID = -4125409760166690462L;
	private static final int MIN_WIDTH = 640;
	private boolean searching;

	private JFindButton findButton;
	private JTextField textField;
	private JCheckBox mcase;
	private JCheckBox regex;
	private JCheckBox wholew;
	private JCheckBox classname;
	private JList<String> list;
	private JProgressBar progressBar;
	boolean locked;

	private JLabel statusLabel = new JLabel("");

	private DefaultListModel<String> classesList = new DefaultListModel<String>();

	private Thread tmp_thread;

	private MainWindow mainWindow;

	public FindAllBox(final MainWindow mainWindow) {
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);
		this.setHideOnEscapeButton();

		progressBar = new JProgressBar(0, 100);
		this.mainWindow = mainWindow;

		JLabel label = new JLabel("Find What:");
		mainWindow.addOtherPageMap("findWhatOnKeyboard", label);
		textField = new JTextField();
		findButton = new JFindButton("Find", "Find");
		mainWindow.addOtherPageMap("findOnKeyboard", findButton);
		findButton.addActionListener(new FindButtonAction());

		mcase = new JCheckBox("Match Case");
		mainWindow.addOtherPageMap("matchCaseOnKeyboard", mcase);
		regex = new JCheckBox("Regex");
		mainWindow.addOtherPageMap("regexOnKeyboard", regex);
		wholew = new JCheckBox("Whole Words");
		mainWindow.addOtherPageMap("wholeWordsOnKeyboard", wholew);
		classname = new JCheckBox("Classnames");
		mainWindow.addOtherPageMap("classnamesOnKeyboard", classname);

		this.getRootPane().setDefaultButton(findButton);

		list = new JList<String>(classesList);
		list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL_WRAP);
		list.setVisibleRowCount(-1);
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				@SuppressWarnings("unchecked")
				JList<String> list = (JList<String>) evt.getSource();
				if (evt.getClickCount() == 2) {
					int index = list.locationToIndex(evt.getPoint());
					String entryName = (String) list.getModel().getElementAt(index);
					String[] array = entryName.split("/");
					if (entryName.toLowerCase().endsWith(".class")) {
						String internalName = StringUtilities.removeRight(entryName, ".class");
						TypeReference type = Model.metadataSystem.lookupType(internalName);
						try {
							mainWindow.getSelectedModel().extractClassToTextPane(type, array[array.length - 1], entryName,
									null);
						} catch (Exception e) {
							Luyten.showExceptionDialog("Exception!", e);
						}

					} else {
						try {
							JarFile jfile = new JarFile(mainWindow.getSelectedModel().getOpenedFile());
							mainWindow.getSelectedModel().extractSimpleFileEntryToTextPane(
									jfile.getInputStream(jfile.getEntry(entryName)), array[array.length - 1],
									entryName);
							jfile.close();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			}
		});
		list.setLayoutOrientation(JList.VERTICAL);
		JScrollPane listScroller = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = (int) (screenSize.width * 0.35);
		if (width < MIN_WIDTH) {
		    width = MIN_WIDTH;
		}
		final Dimension center = new Dimension(width, 500);
		final int x = (int) (center.width * 0.2);
		final int y = (int) (center.height * 0.2);
		this.setBounds(x, y, center.width, center.height);
		this.setResizable(false);

		GroupLayout layout = new GroupLayout(getRootPane());
		getRootPane().setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		layout.setHorizontalGroup(
				layout.createSequentialGroup().addComponent(label)
						.addGroup(
								layout.createParallelGroup(Alignment.LEADING)
										.addComponent(statusLabel)
										.addComponent(textField)
										.addGroup(layout.createSequentialGroup()
												.addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(mcase))
												.addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(wholew))
												.addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(regex))
												.addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(classname)))
										.addGroup(layout.createSequentialGroup()
												.addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(listScroller)
														.addComponent(progressBar))))
						.addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(findButton))

		);

		layout.linkSize(SwingConstants.HORIZONTAL, findButton);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(label).addComponent(textField)
						.addComponent(findButton))
				.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(mcase).addComponent(wholew)
						.addComponent(regex).addComponent(classname))
				.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(listScroller))))
				.addGroup(layout.createParallelGroup(Alignment.LEADING)).addComponent(statusLabel)
				.addComponent(progressBar));
		this.adjustWindowPositionBySavedState();
		this.setSaveWindowPositionOnClosing();

		this.setName("Find All");
		this.setTitle("Find All");
		mainWindow.addOtherDialogMap("findAllOnKeyboard", this);
	}

	private class JFindButton extends JButton {
		private String flag;

		public JFindButton(String flag) {
			this.flag = flag;
		}

		public JFindButton(Icon icon, String flag) {
			super(icon);
			this.flag = flag;
		}

		public JFindButton(String text, String flag) {
			super(text);
			this.flag = flag;
		}

		public JFindButton(Action a, String flag) {
			super(a);
			this.flag = flag;
		}

		public JFindButton(String text, Icon icon, String flag) {
			super(text, icon);
			this.flag = flag;
		}

		public String getFlag() {
			return flag;
		}

		public void setFlag(String flag) {
			this.flag = flag;
		}
	}

	private class FindButtonAction extends AbstractAction {
		private static final long serialVersionUID = 75954129199541874L;

		@Override
		public void actionPerformed(ActionEvent event) {
			tmp_thread = new Thread() {
				public void run() {
					if (findButton.getFlag().equals("Stop")) {
						if (tmp_thread != null)
							tmp_thread.interrupt();
						setStatus("Stopped.");
						String findOnKeyboard = mainWindow.getShowLanguageText("findOnKeyboard");
						findButton.setText(StringUtils.isEmpty(findOnKeyboard) ? "Find" : findOnKeyboard);
						findButton.setFlag("Find");
						locked = false;
					} else {
						String stopOnKeyboard = mainWindow.getShowLanguageText("stopOnKeyboard");
						findButton.setText(StringUtils.isEmpty(stopOnKeyboard) ? "Stop" : stopOnKeyboard);
						findButton.setFlag("Stop");
						classesList.clear();
						ConfigSaver configSaver = ConfigSaver.getLoadedInstance();
						DecompilerSettings settings = configSaver.getDecompilerSettings();
						File inFile = mainWindow.getSelectedModel().getOpenedFile();
						boolean filter = ConfigSaver.getLoadedInstance().getLuytenPreferences()
								.isFilterOutInnerClassEntries();
						try {
							JarFile jfile = new JarFile(inFile);
							Enumeration<JarEntry> entLength = jfile.entries();
							initProgressBar(Collections.list(entLength).size());
							Enumeration<JarEntry> ent = jfile.entries();
							while (ent.hasMoreElements() && findButton.getFlag().equals("Stop")) {
								JarEntry entry = ent.nextElement();
								String name = entry.getName();
								setStatus(name);
								if (filter && name.contains("$"))
									continue;
								if(locked || classname.isSelected()){
									locked = true;
									if(search(entry.getName()))
										addClassName(entry.getName());
								}else{
									if (entry.getName().endsWith(".class")) {
										synchronized (settings) {
											String internalName = StringUtilities.removeRight(entry.getName(), ".class");
											TypeReference type = Model.metadataSystem.lookupType(internalName);
											TypeDefinition resolvedType = null;
											if (type == null || ((resolvedType = type.resolve()) == null)) {
												throw new Exception("Unable to resolve type.");
											}
											StringWriter stringwriter = new StringWriter();
											DecompilationOptions decompilationOptions;
											decompilationOptions = new DecompilationOptions();
											decompilationOptions.setSettings(settings);
											decompilationOptions.setFullDecompilation(true);
											PlainTextOutput plainTextOutput = new PlainTextOutput(stringwriter);
											plainTextOutput.setUnicodeOutputEnabled(
													decompilationOptions.getSettings().isUnicodeOutputEnabled());
											settings.getLanguage().decompileType(resolvedType, plainTextOutput,
													decompilationOptions);
											if (search(stringwriter.toString()))
												addClassName(entry.getName());
										}
									} else {

										StringBuilder sb = new StringBuilder();
										long nonprintableCharactersCount = 0;
										try (InputStreamReader inputStreamReader = new InputStreamReader(
												jfile.getInputStream(entry));
												BufferedReader reader = new BufferedReader(inputStreamReader);) {
											String line;
											while ((line = reader.readLine()) != null) {
												sb.append(line).append("\n");

												for (byte nextByte : line.getBytes()) {
													if (nextByte <= 0) {
														nonprintableCharactersCount++;
													}
												}

											}
										}
										if (nonprintableCharactersCount < 5 && search(sb.toString()))
											addClassName(entry.getName());
									}
								}
							}
							setSearching(false);
							if (findButton.getFlag().equals("Stop")) {
								setStatus("Done.");
								String findOnKeyboard = mainWindow.getShowLanguageText("findOnKeyboard");
								findButton.setText(StringUtils.isEmpty(findOnKeyboard) ? "Find" : findOnKeyboard);
								findButton.setFlag("Find");
								locked = false;
							}
							jfile.close();
							locked = false;
						} catch (Exception e) {
							Luyten.showExceptionDialog("Exception!", e);
						}

					}
				}
			};
			tmp_thread.start();

		}

	}

	private boolean search(String bulk) {
		String a = textField.getText();
		String b = bulk;
		if (regex.isSelected())
			return Pattern.matches(a, b);
		if (wholew.isSelected())
			a = " " + a + " ";
		if (!mcase.isSelected()) {
			a = a.toLowerCase();
			b = b.toLowerCase();
		}
		if (b.contains(a))
			return true;
		return false;
	}

	private void setHideOnEscapeButton() {
		Action escapeAction = new AbstractAction() {
			private static final long serialVersionUID = 6846566740472934801L;

			@Override
			public void actionPerformed(ActionEvent e) {
				FindAllBox.this.setVisible(false);
			}
		};

		KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
		this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
		this.getRootPane().getActionMap().put("ESCAPE", escapeAction);
	}

	private void adjustWindowPositionBySavedState() {
		WindowPosition windowPosition = ConfigSaver.getLoadedInstance().getFindWindowPosition();

		if (windowPosition.isSavedWindowPositionValid()) {
			this.setLocation(windowPosition.getWindowX(), windowPosition.getWindowY());
		}
	}

	private void setSaveWindowPositionOnClosing() {
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDeactivated(WindowEvent e) {
				WindowPosition windowPosition = ConfigSaver.getLoadedInstance().getFindWindowPosition();
				windowPosition.readPositionFromDialog(FindAllBox.this);
			}
		});
	}

	public void showFindBox() {
		this.setVisible(true);
		this.textField.requestFocus();
	}

	public void hideFindBox() {
		this.setVisible(false);
	}

	public void setStatus(String text) {
		if (text.length() > 25) {
			this.statusLabel.setText("Searching in file: ..." + text.substring(text.length() - 25));
		} else {
			this.statusLabel.setText("Searching in file: " + text);
		}

		progressBar.setValue(progressBar.getValue() + 1);
	}

	public void addClassName(String className) {
		this.classesList.addElement(className);
	}

	public void initProgressBar(Integer length) {
		progressBar.setMaximum(length);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
	}

	public boolean isSearching() {
		return searching;
	}

	public void setSearching(boolean searching) {
		this.searching = searching;
	}
}
