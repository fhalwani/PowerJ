package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;

class PJClient extends PowerJ {
	private static final long serialVersionUID = -2327073173098386790L;
	byte panelID = 0;
	private Dimension defaultSize;
	private PnlMain currentPanel;

	PJClient() {
		super();
	}
	
	private void about() {
		new DlgMessage(this, JOptionPane.PLAIN_MESSAGE,
				"About " + Constants.APP_NAME, null);
	}

	void close() {
		if (closePanel()) {
			setPreferredSize(defaultSize);
			invalidate();
			pack();
		}
	}

	private boolean closePanel() {
		boolean closed = true;
		if (currentPanel != null) {
			if (((PnlMain) currentPanel).close()) {
				// Cannot separate Point from Dimension in an ascii file
				Point p = new Point(getSize().width, getSize().height);
				defaults.setPoint("panel" + panelID, p);
				remove(currentPanel);
				statusBar.clear();
				currentPanel = null;
				panelID = 0;
			} else {
				// Error saving
				closed = false;
			}
		}
		return closed;
	}

	private static void createAndShowGUI() {
		PJClient aFrame = new PJClient();
		aFrame.initialize();
		aFrame.createMain();
	}

	/** Create and set up the main window **/
	private void createMain() {
		UIManager.put("ToolTip.font", new FontUIResource(Constants.APP_FONT));
		// default BorderLayout used
		setLayout(new BorderLayout());
		// add a status bar to the bottom of the JFrame
		add(statusBar, BorderLayout.SOUTH);
		if (variables.userID > 0) {
			setJMenuBar(createMenu());
			JToolBar toolBar = createToolBar();
			add(toolBar, BorderLayout.NORTH);
		} else if (variables.userID == -111) {
			JToolBar toolBar = createToolBarHisto();
			add(toolBar, BorderLayout.NORTH);
		}
		pack();
		setVisible(true);
		defaultSize = getPreferredSize();
		Point location = defaults.getPoint("main", getLocationOnScreen());
		setLocation(location);
		if (variables.userID < 0) {
			setView(Constants.PANEL_DASH);
		}
	}

	private JMenuBar createMenu() {
		// Setup the menu bar
		int noItems = 0;
		JMenuBar menuBar = new JMenuBar();
		// File Menu
		JMenu mainItem = new JMenu("File");
		mainItem.setMnemonic(KeyEvent.VK_F);
		ImageIcon image = Utilities.getIcon("save");
		JMenuItem menuItem = new JMenuItem(new ActionSave(image));
		mainItem.add(menuItem);
		image = Utilities.getIcon("close");
		menuItem = new JMenuItem(new ActionClose(image));
		mainItem.add(menuItem);
		mainItem.addSeparator();
		image = Utilities.getIcon("xls");
		menuItem = new JMenuItem(new ActionXls(image));
		mainItem.add(menuItem);
		image = Utilities.getIcon("pdf");
		menuItem = new JMenuItem(new ActionPdf(image));
		mainItem.add(menuItem);
		mainItem.addSeparator();
		image = Utilities.getIcon("quit");
		menuItem = new JMenuItem(new ActionQuit(image));
		mainItem.add(menuItem);
		menuBar.add(mainItem);
		noItems = 0;
		mainItem = new JMenu("Dashboard");
		mainItem.setMnemonic(KeyEvent.VK_D);
		if (variables.userAccess[Constants.ACCESS_Gross]) {
			image = Utilities.getIcon("dashboard");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_DASH, KeyEvent.VK_D, "Dashboard",
					"Open Cases Dashboard", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_Histology]) {
			image = Utilities.getIcon("histology");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_HISTO, KeyEvent.VK_H, "Histology",
					"Open Histology Workflow", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_Diagnosis]) {
			image = Utilities.getIcon("workflow");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_WORKFLOW, KeyEvent.VK_W, "Workflow",
					"Open Pathologists Workflow", image));
			mainItem.add(menuItem);
			noItems++;
			image = Utilities.getIcon("pathologists");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_PATHOLOGISTS, KeyEvent.VK_O, "Workload",
					"Open Pathologists Workload", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (noItems > 1) {
			menuBar.add(mainItem);
		} else if (noItems == 1) {
			menuBar.add(menuItem);
		}
		noItems = 0;
		mainItem = new JMenu("Reports");
		mainItem.setMnemonic(KeyEvent.VK_R);
		if (variables.userAccess[Constants.ACCESS_REP_SCAN]) {
			image = Utilities.getIcon("scanner");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_SCANNING, KeyEvent.VK_S, "Scanner",
					"Open Scanner Report", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_REP_STATS]) {
			image = Utilities.getIcon("stats");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_STATSUM, KeyEvent.VK_T, "Stats",
					"Open Statistics Report", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_REP_TAT]) {
			image = Utilities.getIcon("tat");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_TURNAROUND, KeyEvent.VK_U, "Turnaround",
					"Open Turnaround Time Report", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_REP_TRACK]) {
			image = Utilities.getIcon("tracker");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_TRACKER, KeyEvent.VK_R, "Tracker",
					"Open Tracker Report", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_REP_WL]) {
			image = Utilities.getIcon("workload");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_WLSUMMARY, KeyEvent.VK_W, "Workload",
					"Open Workload Summary Report", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (noItems > 1) {
			menuBar.add(mainItem);
		} else if (noItems == 1) {
			menuBar.add(menuItem);
		}
		if (variables.userAccess[Constants.ACCESS_SetupDash]
				|| variables.userAccess[Constants.ACCESS_SetupWorkload]
				|| variables.userAccess[Constants.ACCESS_SetupSchedule]) {
			mainItem = new JMenu("Setup");
			mainItem.setMnemonic(KeyEvent.VK_S);
			menuBar.add(mainItem);
			if (variables.userAccess[Constants.ACCESS_SetupWorkload]) {
				//a submenu
				JMenu subMenu = new JMenu("PowerPath");
				subMenu.setMnemonic(KeyEvent.VK_P);
				mainItem.add(subMenu);
				image = Utilities.getIcon("accession");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_ACCESSION, KeyEvent.VK_A, "Accession",
						"Open Accession Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("facility");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_FACILITY, KeyEvent.VK_F, "Facilities",
						"Open Facilities Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("orders");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_ORDERS, KeyEvent.VK_O, "Orders",
						"Open Orders Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("personnel");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_PERSONNEL, KeyEvent.VK_P, "Personnel",
						"Open Personnel Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("templates");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_TEMPLATES, KeyEvent.VK_T, "Templates",
						"Open Templates Setup", image));
				subMenu.add(menuItem);
				//a submenu
				subMenu = new JMenu("PowerJ");
				subMenu.setMnemonic(KeyEvent.VK_J);
				mainItem.add(subMenu);
				image = Utilities.getIcon("coder1");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_CODER1, 0, variables.codersName[0],
						"Open " + variables.codersName[0] + " Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("coder2");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_CODER2, 0, variables.codersName[1],
						"Open " + variables.codersName[1] + " Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("coder3");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_CODER3, 0, variables.codersName[2],
						"Open " + variables.codersName[2] + " Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("coder4");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_CODER4, 0, variables.codersName[3],
						"Open " + variables.codersName[3] + " Setup", image));
				subMenu.add(menuItem);
				subMenu.addSeparator();
				image = Utilities.getIcon("orders");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_GROUPS, KeyEvent.VK_G, "Groups",
						"Open Order Groups Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("rules");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_RULES, KeyEvent.VK_R, "Rules",
						"Open Rules Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("setup");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_SPECIALTY, KeyEvent.VK_S, "Specialties",
						"Open Specialties Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("setup");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_SUBSPECIAL, KeyEvent.VK_U, "Subspecialties",
						"Open Subspecialties Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("powerj");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_VARIABLES, KeyEvent.VK_V, "Variables",
						"Open System Variables Setup", image));
				subMenu.add(menuItem);
				// Schedule Schedule submenu
				subMenu = new JMenu("Schedule");
				subMenu.setMnemonic(KeyEvent.VK_S);
				mainItem.add(subMenu);
				image = Utilities.getIcon("contract");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_CONTRACTS, KeyEvent.VK_C, "Contracts",
						"Open Contracts Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("skill");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_SKILLS, KeyEvent.VK_K, "Skills",
						"Open Skills Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("workload");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_STAFF, KeyEvent.VK_T, "Staff",
						"Open Staff Setup", image));
				subMenu.add(menuItem);
				image = Utilities.getIcon("shift");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_SHIFTS, KeyEvent.VK_H, "Shifts",
						"Open Shifts Setup", image));
				subMenu.add(menuItem);
				if (variables.userID == 375) {	// my userID
					// Data Import/Export submenu
					mainItem.addSeparator();
					subMenu = new JMenu("Data");
					subMenu.setMnemonic(KeyEvent.VK_D);
					mainItem.add(subMenu);
					menuItem = new JMenuItem(new ActionExport());
					subMenu.add(menuItem);
					menuItem = new JMenuItem(new ActionImport());
					subMenu.add(menuItem);
				}
			} else if (variables.userAccess[Constants.ACCESS_SetupSchedule]) {
				image = Utilities.getIcon("contract");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_CONTRACTS, KeyEvent.VK_C, "Contracts",
						"Open Contracts Setup", image));
				mainItem.add(menuItem);
				image = Utilities.getIcon("skill");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_SKILLS, KeyEvent.VK_K, "Skills",
						"Open Skills Setup", image));
				mainItem.add(menuItem);
				image = Utilities.getIcon("workload");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_STAFF, KeyEvent.VK_T, "Staff",
						"Open Staff Setup", image));
				mainItem.add(menuItem);
				image = Utilities.getIcon("shift");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_SHIFTS, KeyEvent.VK_H, "Shifts",
						"Open Shifts Setup", image));
				mainItem.add(menuItem);
			} else {
				image = Utilities.getIcon("personnel");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_PERSONDASH, KeyEvent.VK_P, "Personnel",
						"Open Personnel Setup", image));
				mainItem.add(menuItem);
				image = Utilities.getIcon("templates");
				menuItem = new JMenuItem(new ActionView(
						Constants.PANEL_TEMPLATEDASH, KeyEvent.VK_T, "Templates",
						"Open Templates Setup", image));
				mainItem.add(menuItem);
			}
		}
		noItems = 0;
		mainItem = new JMenu("Workload");
		mainItem.setMnemonic(KeyEvent.VK_W);
		if (variables.userAccess[Constants.ACCESS_Additionals]) {
			image = Utilities.getIcon("additional");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_ADDITIONAL, KeyEvent.VK_A, "Additional",
					"Open Additional Work Auditor", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_Cases]) {
			image = Utilities.getIcon("edit");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_CASES, KeyEvent.VK_C, "Case Editor",
					"Open Case Editor", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_Errors]) {
			image = Utilities.getIcon("errors");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_ERRORS, KeyEvent.VK_E, "Errors",
					"Open Workload Errors Auditor", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_Frozen]) {
			image = Utilities.getIcon("frozen");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_FROZENS, KeyEvent.VK_F, "Frozens",
					"Open Frozen Section Auditor", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_Specimens]) {
			image = Utilities.getIcon("review");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_SPECIMEN, KeyEvent.VK_S, "Specimens",
					"Open Specimens Auditor", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_STATS]) {
			image = Utilities.getIcon("stats");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_STATS, KeyEvent.VK_T, "Stats",
					"Open Statistics Auditor", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (variables.userAccess[Constants.ACCESS_Workload]) {
			image = Utilities.getIcon("audit");
			menuItem = new JMenuItem(new ActionView(
					Constants.PANEL_WORKLOAD, KeyEvent.VK_W, "Workload",
					"Open Workload Auditor", image));
			mainItem.add(menuItem);
			noItems++;
		}
		if (noItems > 1) {
			menuBar.add(mainItem);
		} else if (noItems == 1) {
			menuBar.add(menuItem);
		}
		mainItem = new JMenu("Help");
		mainItem.setMnemonic(KeyEvent.VK_H);
		menuBar.add(mainItem);
		image = Utilities.getIcon("help");
		menuItem = new JMenuItem(new ActionHelp(image));
		mainItem.add(menuItem);
		image = Utilities.getIcon("about");
		menuItem = new JMenuItem(new ActionAbout(image));
		mainItem.add(menuItem);
		return menuBar;
	}

	private JToolBar createToolBar() {
		int noButtons = 0;
		Dimension size = new Dimension(32, 32);
		ImageIcon image = Utilities.getIcon("save");
		JToolBar toolBar = new JToolBar();
		JButton button = new JButton(new ActionSave(image));
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setAlignmentY(TOP_ALIGNMENT);
		if (button.getIcon() != null)
			// set as an icon-only button
			button.setText("");
		toolBar.add(button);
		image = Utilities.getIcon("close");
		button = new JButton(new ActionClose(image));
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setAlignmentY(TOP_ALIGNMENT);
		if (button.getIcon() != null)
			// set as an icon-only button
			button.setText("");
		toolBar.add(button);
		toolBar.addSeparator();
		image = Utilities.getIcon("xls");
		button = new JButton(new ActionXls(image));
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setAlignmentY(TOP_ALIGNMENT);
		if (button.getIcon() != null)
			// set as an icon-only button
			button.setText("");
		toolBar.add(button);
		image = Utilities.getIcon("pdf");
		button = new JButton(new ActionPdf(image));
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setAlignmentY(TOP_ALIGNMENT);
		if (button.getIcon() != null)
			// set as an icon-only button
			button.setText("");
		toolBar.add(button);
		toolBar.addSeparator();
		if (variables.userAccess[Constants.ACCESS_Gross]
				|| variables.userAccess[Constants.ACCESS_Histology]
				|| variables.userAccess[Constants.ACCESS_Diagnosis]) {
			image = Utilities.getIcon("dashboard");
			button = new JButton(new ActionView(
					Constants.PANEL_DASH, KeyEvent.VK_D, "Dashboard",
					"Open Dashboard", image));
			button.setMinimumSize(size);
			button.setPreferredSize(size);
			button.setAlignmentY(TOP_ALIGNMENT);
			if (button.getIcon() != null)
				// set as an icon-only button
				button.setText("");
			toolBar.add(button);
			noButtons++;
			if (variables.userAccess[Constants.ACCESS_Histology]) {
				image = Utilities.getIcon("histology");
				button = new JButton(new ActionView(
						Constants.PANEL_HISTO, KeyEvent.VK_H, "Histology",
						"Open Histology workflow", image));
				button.setMinimumSize(size);
				button.setPreferredSize(size);
				button.setAlignmentY(TOP_ALIGNMENT);
				if (button.getIcon() != null)
					button.setText("");
				toolBar.add(button);
				noButtons++;
			}
			if (variables.userAccess[Constants.ACCESS_Diagnosis]) {
				image = Utilities.getIcon("workflow");
				button = new JButton(new ActionView(
						Constants.PANEL_WORKFLOW, KeyEvent.VK_W, "Workflow",
						"Open Pathologists Workflow", image));
				button.setMinimumSize(size);
				button.setPreferredSize(size);
				button.setAlignmentY(TOP_ALIGNMENT);
				if (button.getIcon() != null)
					button.setText("");
				toolBar.add(button);
				noButtons++;
				image = Utilities.getIcon("pathologists");
				button = new JButton(new ActionView(
						Constants.PANEL_PATHOLOGISTS, KeyEvent.VK_O, "Workload",
						"Open Pathologists Workload", image));
				button.setMinimumSize(size);
				button.setPreferredSize(size);
				button.setAlignmentY(TOP_ALIGNMENT);
				if (button.getIcon() != null)
					button.setText("");
				toolBar.add(button);
				noButtons++;
			}
		}
		if (noButtons > 0) {
			toolBar.addSeparator();
		}
		noButtons = 0;
		if (variables.userAccess[Constants.ACCESS_Workload]) {
			image = Utilities.getIcon("audit");
			button = new JButton(new ActionView(
					Constants.PANEL_WORKLOAD, KeyEvent.VK_W, "Workload",
					"Open Workload Auditor", image));
			button.setMinimumSize(size);
			button.setPreferredSize(size);
			button.setAlignmentY(TOP_ALIGNMENT);
			if (button.getIcon() != null)
				button.setText("");
			toolBar.add(button);
			noButtons++;
		}
		if (variables.userAccess[Constants.ACCESS_Specimens]) {
			image = Utilities.getIcon("review");
			button = new JButton(new ActionView(
					Constants.PANEL_SPECIMEN, KeyEvent.VK_S, "Specimens",
					"Open Specimens Auditor", image));
			button.setMinimumSize(size);
			button.setPreferredSize(size);
			button.setAlignmentY(TOP_ALIGNMENT);
			if (button.getIcon() != null)
				button.setText("");
			toolBar.add(button);
			noButtons++;
		}
		if (variables.userAccess[Constants.ACCESS_Frozen]) {
			image = Utilities.getIcon("frozen");
			button = new JButton(new ActionView(
					Constants.PANEL_FROZENS, KeyEvent.VK_F, "Frozens", 
					"Open Frozen Section Auditor", image));
			button.setMinimumSize(size);
			button.setPreferredSize(size);
			button.setAlignmentY(TOP_ALIGNMENT);
			if (button.getIcon() != null)
				button.setText("");
			toolBar.add(button);
			noButtons++;
		}
		if (variables.userAccess[Constants.ACCESS_Cases]) {
			image = Utilities.getIcon("edit");
			button = new JButton(new ActionView(
					Constants.PANEL_CASES, KeyEvent.VK_E, "Edit",
					"Open Case Editor", image));
			button.setMinimumSize(size);
			button.setPreferredSize(size);
			button.setAlignmentY(TOP_ALIGNMENT);
			if (button.getIcon() != null)
				button.setText("");
			toolBar.add(button);
			noButtons++;
		}
		if (noButtons > 0) {
			toolBar.addSeparator();
		}
		image = Utilities.getIcon("help");
		button = new JButton(new ActionHelp(image));
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setAlignmentY(TOP_ALIGNMENT);
		button.getInputMap().put(KeyStroke.getKeyStroke("F1"), "doHelp");
		button.getActionMap().put("doHelp", new ActionHelp(image));
		if (button.getIcon() != null)
			button.setText("");
		toolBar.add(button);
		image = Utilities.getIcon("about");
		button = new JButton(new ActionAbout(image));
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setAlignmentY(TOP_ALIGNMENT);
		if (button.getIcon() != null)
			button.setText("");
		toolBar.add(button);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		toolBar.setVisible(true);
		return toolBar;
	}

	private JToolBar createToolBarHisto() {
		Dimension size = new Dimension(32, 32);
		ImageIcon image = Utilities.getIcon("dashboard");
		JToolBar toolBar = new JToolBar();
		JButton button = new JButton(new ActionView(
				Constants.PANEL_DASH, KeyEvent.VK_D, "Dashboard",
				"Open Dashboard", image));
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setAlignmentY(TOP_ALIGNMENT);
		if (button.getIcon() != null)
			// set as an icon-only button
			button.setText("");
		toolBar.add(button);
		image = Utilities.getIcon("histology");
		button = new JButton(new ActionView(
				Constants.PANEL_HISTO, KeyEvent.VK_H, "Histology",
				"Open Histology workflow", image));
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setAlignmentY(TOP_ALIGNMENT);
		if (button.getIcon() != null)
			button.setText("");
		toolBar.add(button);
		toolBar.addSeparator();
		image = Utilities.getIcon("help");
		button = new JButton(new ActionHelp(image));
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setAlignmentY(TOP_ALIGNMENT);
		if (button.getIcon() != null)
			button.setText("");
		toolBar.add(button);
		image = Utilities.getIcon("about");
		button = new JButton(new ActionAbout(image));
		button.setMinimumSize(size);
		button.setPreferredSize(size);
		button.setAlignmentY(TOP_ALIGNMENT);
		if (button.getIcon() != null)
			button.setText("");
		toolBar.add(button);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		toolBar.setVisible(true);
		return toolBar;
	}

	private void exporter() {
		ExportManager manager = new ExportManager(this);
		if (manager.getDir()) {
			manager.exportSetup();
			manager.exportDash();
			manager.exportWorkload();
			manager.exportStats();
		}
	}
	
	private String getFilePath(String def, FileNameExtensionFilter filter) {
		String fileName = "";
		final JFileChooser fc = new JFileChooser();
		try {
			String dataDir = defaults.getString("datadir", System.getProperty("user.home"));
			if (!dataDir.endsWith(Constants.FILE_SEPARATOR)) {
				dataDir += Constants.FILE_SEPARATOR;
			}
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fc.setFileFilter(filter);
			fc.setSelectedFile(new File(dataDir + def));
			int val = fc.showOpenDialog(this);
			if (val == JFileChooser.APPROVE_OPTION) {
				fileName = fc.getSelectedFile().getAbsolutePath();
				if (!dataDir.equals(fc.getCurrentDirectory().getAbsolutePath())) {
					dataDir = fc.getCurrentDirectory().getAbsolutePath();
					defaults.setString("datadir", dataDir);
				}
			}
		} catch (HeadlessException ignore) {}
		return fileName;
	}

	String getFilePdf(String def) {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF files", "pdf");
		String fileName = getFilePath(def, filter);
		if (fileName.trim().length() > 2) {
			int i = fileName.lastIndexOf('.');
			if (i > 0) {
				String extension = fileName.substring(i+1).trim().toLowerCase();
			    if (!extension.equals("pdf")) {
					fileName = fileName.substring(0, i).trim() + ".pdf";
			    }
			} else {
				fileName += ".pdf";
			}
		}
		File file = new File(fileName);
		if (file.exists()) {
			String message = String.format("A file named '%s' already exists. Do you want to replace it?", fileName);
			String[] choices = {"Cancel", "Replace"};
			int[] mnemonics = {KeyEvent.VK_C, KeyEvent.VK_R};
			DlgMessage dialog = new DlgMessage(this,
					JOptionPane.QUESTION_MESSAGE, "Export", message, choices, mnemonics, 0);
			if (dialog.choice == 0) {
				fileName = "";
			}
		}
		return fileName;
	}
	
	String getFileXls(String def) {
		FileNameExtensionFilter filter = new FileNameExtensionFilter("XLS files", "xls", "xlsx");
		String fileName = getFilePath(def, filter);
		if (fileName.trim().length() > 2) {
			int i = fileName.lastIndexOf('.');
			if (i > 0) {
				String extension = fileName.substring(i+1).trim().toLowerCase();
			    if (!extension.equals("xlsx")) {
					fileName = fileName.substring(0, i).trim() + ".xlsx";
			    }
			} else {
				fileName += ".xlsx";
			}
		}
		File file = new File(fileName);
		if (file.exists()) {
			String message = String.format("A file named '%s' already exists. Do you want to replace it?", fileName);
			String[] choices = {"Cancel", "Replace"};
			int[] mnemonics = {KeyEvent.VK_C, KeyEvent.VK_R};
			DlgMessage dialog = new DlgMessage(this,
					JOptionPane.QUESTION_MESSAGE, "Export", message, choices, mnemonics, 0);
			if (dialog.choice == 0) {
				fileName = "";
			}
		}
		return fileName;
	}

	private void help() {
		Helper dialog = new Helper(this);
		dialog.setVisible(true);
	}

	private void importer() {
		ExportManager manager = new ExportManager(this);
		if (manager.getDir()) {
			//manager.importSetup();
			//manager.importDash();
			//manager.importWorkload();
			manager.importStats();
		}
	}
	
    void initialize() {
		super.initialize();
		if (!variables.hasError) {
			if (variables.offLine && variables.debugMode) {
				variables.userID = 375;	// my userID
				if (!validateLogin()) {
					variables.hasError = true;
				}
			} else if (!variables.autoLogin) {
				DlgLogin login = new DlgLogin(this);
				if (login.cancel) {
					variables.hasError = true;
				} else if (!login.validateLogin()) {
					variables.hasError = true;
				} else if (!validateLogin()) {
					variables.hasError = true;
				}
				// Kill Dialog
				login.dispose();
			}
			if (variables.hasError) {
				new DlgMessage(this, JOptionPane.WARNING_MESSAGE, Constants.APP_NAME, "Access denied.");
			}
		}
		if (variables.hasError) {
			quit();
		}
    }
    
	void log(int severity, String name, String message) {
		super.log(severity, name, message);
		new DlgMessage(this, severity, name, message);
	}
	
	void log(int severity, String name, Throwable e) {
		super.log(severity, name, e);
		new DlgMessage(this, severity, name, e.getLocalizedMessage());
	}
	
	public static void main(String[] args) {
        // store the args
        commandLineArgs = args;
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	void quit() {
		if (closePanel()) {
			super.quit();
		}
	}

	private void setView(byte panel_id) {
		variables.hasError = false;
		if (panelID == panel_id) {
			return;
		} else if (!closePanel()) {
			// Error saving
			return;
		}
		switch (panel_id) {
		case Constants.PANEL_ACCESSION:
			currentPanel = new PnlAccessions(this);
			break;
		case Constants.PANEL_ADDITIONAL:
			currentPanel = new PnlAdditionals(this);
			break;
		case Constants.PANEL_CASES:
			currentPanel = new PnlCases(this);
			break;
		case Constants.PANEL_CODER1:
			currentPanel = new PnlCoder(this, (byte)1);
			break;
		case Constants.PANEL_CODER2:
			currentPanel = new PnlCoder(this, (byte)2);
			break;
		case Constants.PANEL_CODER3:
			currentPanel = new PnlCoder(this, (byte)3);
			break;
		case Constants.PANEL_CODER4:
			currentPanel = new PnlCoder(this, (byte)4);
			break;
		case Constants.PANEL_DASH:
			currentPanel = new PnlDashboard(this);
			break;
		case Constants.PANEL_ERRORS:
			currentPanel = new PnlErrors(this);
			break;
		case Constants.PANEL_FACILITY:
			currentPanel = new PnlFacilities(this);
			break;
		case Constants.PANEL_FROZENS:
			currentPanel = new PnlFrozens(this);
			break;
		case Constants.PANEL_GROUPS:
			currentPanel = new PnlGroups(this);
			break;
		case Constants.PANEL_HISTO:
			currentPanel = new PnlHistology(this);
			break;
		case Constants.PANEL_ORDERS:
			currentPanel = new PnlOrders(this);
			break;
		case Constants.PANEL_PATHOLOGISTS:
			currentPanel = new PnlPathologists(this);
			break;
		case Constants.PANEL_PERSONDASH:
			currentPanel = new PnlPersonDash(this);
			break;
		case Constants.PANEL_PERSONNEL:
			currentPanel = new PnlPersonnel(this);
			break;
		case Constants.PANEL_RULES:
			currentPanel = new PnlRules(this);
			break;
		case Constants.PANEL_SCANNING:
			currentPanel = new PnlScanning(this);
			break;
		case Constants.PANEL_SPECIALTY:
			currentPanel = new PnlSpecialties(this);
			break;
		case Constants.PANEL_SPECIMEN:
			currentPanel = new PnlSpecimens(this);
			break;
		case Constants.PANEL_SUBSPECIAL:
			currentPanel = new PnlSubspecials(this);
			break;
		case Constants.PANEL_STATS:
			currentPanel = new PnlStats(this);
			break;
		case Constants.PANEL_STATSUM:
			currentPanel = new PnlStSummary(this);
			break;
		case Constants.PANEL_TEMPLATEDASH:
			currentPanel = new PnlTemplateDash(this);
			break;
		case Constants.PANEL_TEMPLATES:
			currentPanel = new PnlTemplates(this);
			break;
		case Constants.PANEL_TRACKER:
			currentPanel = new PnlTracker(this);
			break;
		case Constants.PANEL_TURNAROUND:
			currentPanel = new PnlTat(this);
			break;
		case Constants.PANEL_VARIABLES:
			currentPanel = new PnlVariables(this);
			break;
		case Constants.PANEL_WORKFLOW:
			currentPanel = new PnlWorkflow(this);
			break;
		case Constants.PANEL_WLSUMMARY:
			currentPanel = new PnlWlSummary(this);
			break;
		case Constants.PANEL_CONTRACTS:
			currentPanel = new PnlContracts(this);
			break;
		case Constants.PANEL_SKILLS:
			currentPanel = new PnlSkills(this);
			break;
		case Constants.PANEL_STAFF:
			currentPanel = new PnlStaff(this);
			break;
		case Constants.PANEL_SHIFTS:
			currentPanel = new PnlShifts(this);
			break;
		default:
			currentPanel = new PnlAuditor(this);
		}
		setTitle(Constants.APP_NAME + " - " + currentPanel.getName());
		panelID = panel_id;
		add(currentPanel, BorderLayout.CENTER);
		// Set Frame Size
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		Point p = new Point(dim.width-100, dim.height-100);
		p = defaults.getPoint("panel" + panelID, p);
		dim.setSize(p.x, p.y);
		setPreferredSize(dim);
		validate();
		pack();
	}
	
	/** Check login access to PowerJ & set userAccess value **/
	private boolean validateLogin() {
		boolean success = false;
		ResultSet rst = dbPowerJ.getAccessLevel();
		try {
			while (rst.next()) {
				if (rst.getInt("ACCESS") != 0) {
					// Else no access whatsoever
					variables.userAccess = numbers.toBits(rst.getInt("ACCESS"));
					success = true;
				}
			}
		} catch (SQLException e) {
			log(JOptionPane.ERROR_MESSAGE, Constants.APP_NAME, e);
		} finally {
			dbPowerJ.closeRst(rst);
			dbPowerJ.closeStm();
			// TODO DELETE
			variables.userAccess = numbers.toBits(-939524097);
			success = true;
		}
		return success;
	}

	class ActionAbout extends AbstractAction {
		private static final long serialVersionUID = 3384951287597368083L;

		ActionAbout(ImageIcon image) {
			super("About", image);
			putValue(SHORT_DESCRIPTION, "About" + Constants.APP_NAME);
			putValue(MNEMONIC_KEY, KeyEvent.VK_A);
		}

		public void actionPerformed(ActionEvent e) {
			about();
		}
	}

	class ActionClose extends AbstractAction {
		private static final long serialVersionUID = -113942801206459537L;

		ActionClose(ImageIcon image) {
			super("Close", image);
			putValue(SHORT_DESCRIPTION, "Close Form");
			putValue(MNEMONIC_KEY, KeyEvent.VK_C);
		}

		public void actionPerformed(ActionEvent e) {
			close();
		}
	}
	
	class ActionExport extends AbstractAction {
		private static final long serialVersionUID = -7547156676139164034L;

		ActionExport() {
			super("Export");
			putValue(SHORT_DESCRIPTION, "Export PowerJ Data");
			putValue(MNEMONIC_KEY, KeyEvent.VK_E);
		}

		public void actionPerformed(ActionEvent e) {
			exporter();
		}
	}

	class ActionHelp extends AbstractAction {
		private static final long serialVersionUID = -8897181437379841064L;

		ActionHelp(ImageIcon image) {
			super("Help", image);
			putValue(SHORT_DESCRIPTION, "Help Contents");
			putValue(MNEMONIC_KEY, KeyEvent.VK_H);
		}

		public void actionPerformed(ActionEvent e) {
			help();
		}
	}

	class ActionImport extends AbstractAction {
		private static final long serialVersionUID = 3109066001185319752L;

		ActionImport() {
			super("Import");
			putValue(SHORT_DESCRIPTION, "Import PowerJ Data");
			putValue(MNEMONIC_KEY, KeyEvent.VK_I);
		}

		public void actionPerformed(ActionEvent e) {
			importer();
		}
	}

	class ActionPdf extends AbstractAction {
		private static final long serialVersionUID = 941407224743339019L;

		ActionPdf(ImageIcon image) {
			super("Pdf", image);
			putValue(SHORT_DESCRIPTION, "Save data to PDF file");
			putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPanel != null) {
				(currentPanel).pdf();
			}
		}
	}

	class ActionQuit extends AbstractAction {
		private static final long serialVersionUID = -4148918619987392556L;

		ActionQuit(ImageIcon image) {
			super("Quit", image);
			putValue(SHORT_DESCRIPTION, "Quit" + Constants.APP_NAME);
			putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4,
					ActionEvent.ALT_MASK));
		}

		public void actionPerformed(ActionEvent e) {
			quit();
		}
	}

	class ActionSave extends AbstractAction {
		private static final long serialVersionUID = 941407224743339019L;

		ActionSave(ImageIcon image) {
			super("Save", image);
			putValue(SHORT_DESCRIPTION, "Save changes");
			putValue(MNEMONIC_KEY, KeyEvent.VK_S);
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPanel != null) {
				(currentPanel).save();
				if (!currentPanel.altered) {
					close();
				}
			}
		}
	}

	class ActionView extends AbstractAction {
		private static final long serialVersionUID = -967869723940264934L;
		byte id = 0;
		
		ActionView(byte panel, int mnemonic, String name,
				String description, ImageIcon image) {
			super(name, image);
			id = panel;
			putValue(SHORT_DESCRIPTION, description);
			putValue(MNEMONIC_KEY, mnemonic);
		}
		
		public void actionPerformed(ActionEvent e) {
			setView(id);
		}
	}

	class ActionXls extends AbstractAction {
		private static final long serialVersionUID = 941407224743339019L;

		ActionXls(ImageIcon image) {
			super("Excel", image);
			putValue(SHORT_DESCRIPTION, "Save data to Excel file");
			putValue(MNEMONIC_KEY, KeyEvent.VK_X);
		}

		public void actionPerformed(ActionEvent e) {
			if (currentPanel != null) {
				(currentPanel).xls();
			}
		}
	}
}
