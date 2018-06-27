package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

class PnlVariables extends PnlMain implements DocumentListener,
	FocusListener, ItemListener {
	private static final long serialVersionUID = 322217491528284624L;
	private final byte DATA_STRING = 0;
	private final byte DATA_BOOLEAN = 1;
	private final byte DATA_SHORT = 2;
	private final byte DATA_BYTE = 3;
	private final byte DATA_LONG = 4;
	private final byte STP_SERVER = 1;
	private final byte STP_PORT = 2;
	private final byte STP_DATABASE = 3;
	private final byte STP_LOGIN = 4;
	private final byte STP_PASSWORD = 5;
	private final byte STP_SAT_OFF = 6;
	private final byte STP_SUN_OFF = 7;
	private final byte STP_OPENING = 8;
	private final byte STP_CLOSING = 9;
	private final byte STP_TIMER = 10;
	private final byte STP_UPDATER = 11;
	private final byte STP_CODER1_NAME = 12;
	private final byte STP_CODER1_ACTIVE = 13;
	private final byte STP_CODER2_NAME = 14;
	private final byte STP_CODER2_ACTIVE = 15;
	private final byte STP_CODER3_NAME = 16;
	private final byte STP_CODER3_ACTIVE = 17;
	private final byte STP_CODER4_NAME = 18;
	private final byte STP_CODER4_ACTIVE = 19;
	private final byte STP_MIN_WL_DATE = 20;
	private final byte STP_CODER1_FTE = 21;
	private final byte STP_CODER2_FTE = 22;
	private final byte STP_CODER3_FTE = 23;
	private final byte STP_CODER4_FTE = 24;
	private final byte STP_BUSINESS_DAYS = 25;
	private final byte STP_COUNT = 26;
	private ArrayList<DataDefault> lstData = new ArrayList<DataDefault>();

	PnlVariables(PowerJ parent) {
		super(parent);
		setName("Variable");
		parent.dbPowerJ.prepareSetup();
		programmaticChange = true;
		readTable();
		createPanel();
		programmaticChange = false;
	}

	boolean close() {
		if (altered) {
			int option = parent.askSave(getName());
			switch (option) {
			case Utilities.OPTION_YES:
				save();
				break;
			case Utilities.OPTION_NO:
				altered = false;
				break;
			default:
				// Cancel close
			}
		}
		if (!altered) {
			lstData.clear();
			parent.dbPowerJ.closeStms();
			super.close();
		}
		return !altered;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		//Lay out 3 panels from top to bottom.
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(createPanelServer());
		boxPanel.add(createPanelDashboard());
		boxPanel.add(createPanelWorkload());
		add(boxPanel, BorderLayout.CENTER);
	}
	
	private JPanel createPanelDashboard() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Dashboard");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Login");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Update every (min): ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_U);
		Utilities.addComponent(label, 0, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		// Range 10-120 minutes (4 columns)
		JIntegerField integerField = new JIntegerField(parent, 10, 120);
		integerField.setName("Updater");
		integerField.getDocument().addDocumentListener(this);
		integerField.getDocument().putProperty("RowID", STP_UPDATER);
		integerField.setText(getText(STP_UPDATER));
		label.setLabelFor(integerField);
		Utilities.addComponent(integerField, 1, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Wake up every (min): ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_W);
		Utilities.addComponent(label, 2, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		// Range 1-10 minutes (3 columns)
		integerField = new JIntegerField(parent, 1, 10);
		integerField.setName("Timer");
		integerField.getDocument().addDocumentListener(this);
		integerField.getDocument().putProperty("RowID", STP_TIMER);
		integerField.setToolTipText("");
		integerField.setText(getText(STP_TIMER));
		label.setLabelFor(integerField);
		Utilities.addComponent(integerField, 3, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Opening hour: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_O);
		Utilities.addComponent(label, 0, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		// Range 1-12 o'clock (3 columns)
		integerField = new JIntegerField(parent, 1, 12);
		integerField.setName("Opening");
		integerField.getDocument().addDocumentListener(this);
		integerField.getDocument().putProperty("RowID", STP_OPENING);
		integerField.setText(getText(STP_OPENING));
		label.setLabelFor(integerField);
		Utilities.addComponent(integerField, 1, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Closing hour: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_C);
		Utilities.addComponent(label, 2, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		// Range 12-24 o'clock (3 columns)
		integerField = new JIntegerField(parent, 12, 24);
		integerField.setName("Closing");
		integerField.getDocument().addDocumentListener(this);
		integerField.getDocument().putProperty("RowID", STP_CLOSING);
		integerField.setText(getText(STP_CLOSING));
		label.setLabelFor(integerField);
		Utilities.addComponent(integerField, 3, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		JCheckBox checkBox = new JCheckBox();
		checkBox.setName("Saturday");
		checkBox.setText("Saturday Closed: ");
		checkBox.setMnemonic(KeyEvent.VK_T);
		checkBox.setSelected((getText(STP_SAT_OFF).equalsIgnoreCase("Y")));
		checkBox.addItemListener(this);
		Utilities.addComponent(checkBox, 0, 2, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Sunday");
		checkBox.setText("Sunday Closed: ");
		checkBox.setMnemonic(KeyEvent.VK_U);
		checkBox.setSelected((getText(STP_SUN_OFF).equalsIgnoreCase("Y")));
		checkBox.addItemListener(this);
		Utilities.addComponent(checkBox, 2, 2, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		return panel;
	}
	
	private JPanel createPanelServer() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Server");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Server");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Server: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_E);
		Utilities.addComponent(label, 0, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		JStringField textField = new JStringField(3, 50);
		textField.setName("Server");
		textField.getDocument().addDocumentListener(this);
		textField.getDocument().putProperty("RowID", STP_SERVER);
		textField.setText(getText(STP_SERVER));
		label.setLabelFor(textField);
		Utilities.addComponent(textField, 1, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Database: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_A);
		Utilities.addComponent(label, 2, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		textField = new JStringField(3, 50);
		textField.setName("Database");
		textField.getDocument().addDocumentListener(this);
		textField.getDocument().putProperty("RowID", STP_DATABASE);
		textField.setText(getText(STP_DATABASE));
		label.setLabelFor(textField);
		Utilities.addComponent(textField, 3, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Port: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_O);
		Utilities.addComponent(label, 4, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		JIntegerField intField = new JIntegerField(parent, 1000, 9999);
		intField.setName("Port");
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("RowID", STP_PORT);
		intField.setText(getText(STP_PORT));
		label.setLabelFor(intField);
		Utilities.addComponent(intField, 5, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Login: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_L);
		Utilities.addComponent(label, 0, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		textField = new JStringField(3, 50);
		textField.setName("Login");
		textField.getDocument().addDocumentListener(this);
		textField.getDocument().putProperty("RowID", STP_LOGIN);
		textField.setText(getText(STP_LOGIN));
		label.setLabelFor(textField);
		Utilities.addComponent(textField, 1, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Password: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		Utilities.addComponent(label, 2, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		JPasswordField textPassword = new JPasswordField(20);
		textPassword.setName("Password");
		textPassword.addFocusListener(this);
		textPassword.setToolTipText("Password at least 8 characters");
		textPassword.setText(getText(STP_PASSWORD));
		label.setLabelFor(textPassword);
		Utilities.addComponent(textPassword, 3, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		return panel;
	}
	
	private JPanel createPanelWorkload() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Workload");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Workload");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Coder 1: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_1);
		Utilities.addComponent(label, 0, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		JStringField textField = new JStringField(2, 15);
		textField.setName("Coder1Name");
		textField.getDocument().addDocumentListener(this);
		textField.getDocument().putProperty("RowID", STP_CODER1_NAME);
		textField.setText(getText(STP_CODER1_NAME));
		label.setLabelFor(textField);
		Utilities.addComponent(textField, 1, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		JCheckBox checkBox = new JCheckBox();
		checkBox.setName("Coder1Active");
		checkBox.setText("Active: ");
		checkBox.setSelected((getText(STP_CODER1_ACTIVE).equalsIgnoreCase("Y")));
		checkBox.addItemListener(this);
		Utilities.addComponent(checkBox, 2, 0, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("FTE: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		Utilities.addComponent(label, 4, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		JIntegerField intField = new JIntegerField(parent, 1000, 9999);
		intField.setName("FTE1");
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("RowID", STP_CODER1_FTE);
		intField.setText(getText(STP_CODER1_FTE));
		label.setLabelFor(intField);
		Utilities.addComponent(intField, 5, 0, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Coder 2: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_2);
		Utilities.addComponent(label, 0, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		textField = new JStringField(2, 15);
		textField.setName("Coder2Name");
		textField.getDocument().addDocumentListener(this);
		textField.getDocument().putProperty("RowID", STP_CODER2_NAME);
		textField.setText(getText(STP_CODER2_NAME));
		label.setLabelFor(textField);
		Utilities.addComponent(textField, 1, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Coder2Active");
		checkBox.setText("Active: ");
		checkBox.setSelected((getText(STP_CODER2_ACTIVE).equalsIgnoreCase("Y")));
		checkBox.addItemListener(this);
		Utilities.addComponent(checkBox, 2, 1, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("FTE: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		Utilities.addComponent(label, 4, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		intField = new JIntegerField(parent, 1000, 9999);
		intField.setName("FTE2");
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("RowID", STP_CODER2_FTE);
		intField.setText(getText(STP_CODER2_FTE));
		label.setLabelFor(intField);
		Utilities.addComponent(intField, 5, 1, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Coder 3: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_3);
		Utilities.addComponent(label, 0, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		textField = new JStringField(2, 15);
		textField.setName("Coder3Name");
		textField.getDocument().addDocumentListener(this);
		textField.getDocument().putProperty("RowID", STP_CODER3_NAME);
		textField.setText(getText(STP_CODER3_NAME));
		label.setLabelFor(textField);
		Utilities.addComponent(textField, 1, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Coder3Active");
		checkBox.setText("Active: ");
		checkBox.setSelected(getText(STP_CODER3_ACTIVE).equalsIgnoreCase("Y"));
		checkBox.addItemListener(this);
		Utilities.addComponent(checkBox, 2, 2, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("FTE: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		Utilities.addComponent(label, 4, 2, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		intField = new JIntegerField(parent, 1000, 9999);
		intField.setName("FTE3");
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("RowID", STP_CODER3_FTE);
		intField.setText(getText(STP_CODER3_FTE));
		label.setLabelFor(intField);
		Utilities.addComponent(intField, 5, 2, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Coder 4: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_4);
		Utilities.addComponent(label, 0, 3, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		textField = new JStringField(2, 15);
		textField.setName("Coder4Name");
		textField.getDocument().addDocumentListener(this);
		textField.getDocument().putProperty("RowID", STP_CODER4_NAME);
		textField.setText(getText(STP_CODER4_NAME));
		label.setLabelFor(textField);
		Utilities.addComponent(textField, 1, 3, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		checkBox = new JCheckBox();
		checkBox.setName("Coder4Active");
		checkBox.setText("Active: ");
		checkBox.setSelected(getText(STP_CODER4_ACTIVE).equalsIgnoreCase("Y"));
		checkBox.addItemListener(this);
		Utilities.addComponent(checkBox, 2, 3, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("FTE: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		Utilities.addComponent(label, 4, 3, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		intField = new JIntegerField(parent, 1000, 9999);
		intField.setName("FTE4");
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("RowID", STP_CODER4_FTE);
		intField.setText(getText(STP_CODER4_FTE));
		label.setLabelFor(intField);
		Utilities.addComponent(intField, 5, 3, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Minimum Date: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_M);
		Utilities.addComponent(label, 0, 4, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		Calendar calMin = Calendar.getInstance();
		Calendar calMax = Calendar.getInstance();
		Calendar calStart = Calendar.getInstance();
		calMin.setTimeInMillis(0);
		calStart.setTimeInMillis(parent.numbers.parseLong(getText(STP_MIN_WL_DATE)));
		CboDate cboStart = new CboDate(calStart, calMin, calMax);
		cboStart.setName("cboStart");
		cboStart.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CboDate cbo = (CboDate)e.getSource();
					Calendar cal = cbo.getValue();
					DataDefault thisRow = lstData.get(STP_MIN_WL_DATE -1);
					thisRow.value = "" + cal.getTimeInMillis();
					thisRow.altered = true;
					altered = true;
				}
			}

		});
		label.setLabelFor(cboStart);
		Utilities.addComponent(cboStart, 1, 4, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Business Days: ");
		label.setDisplayedMnemonic(KeyEvent.VK_B);
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		Utilities.addComponent(label, 4, 4, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		intField = new JIntegerField(parent, 100, 356);
		intField.setName("Business");
		intField.getDocument().addDocumentListener(this);
		intField.getDocument().putProperty("RowID", STP_BUSINESS_DAYS);
		intField.setText(getText(STP_BUSINESS_DAYS));
		label.setLabelFor(intField);
		Utilities.addComponent(intField, 5, 4, 2, 1, 0.2, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		return panel;
	}
	
	private String getText(byte row){
		return lstData.get(row-1).value;
	}
	
	private void readTable() {
		byte key = 0;
		String value = "";
		DataDefault thisRow = new DataDefault();
		ResultSet rst = null;
		try {
			rst = parent.dbPowerJ.getSetup();
			while (rst.next()) {
				key = rst.getByte("STPID");
				if (rst.getString("STPVAL") == null) {
					value = "";
				} else {
					value = rst.getString("STPVAL").trim();
				}
				thisRow = new DataDefault();
				thisRow.ID = key;
				thisRow.value = value;
				switch (key) {
				case STP_SERVER:
				case STP_DATABASE:
				case STP_LOGIN:
				case STP_PASSWORD:
				case STP_CODER1_NAME:
				case STP_CODER2_NAME:
				case STP_CODER3_NAME:
				case STP_CODER4_NAME:
					thisRow.dataType = DATA_STRING;
					break;
				case STP_PORT:
				case STP_CODER1_FTE:
				case STP_CODER2_FTE:
				case STP_CODER3_FTE:
				case STP_CODER4_FTE:
				case STP_BUSINESS_DAYS:
					thisRow.dataType = DATA_SHORT;
					break;
				case STP_OPENING:
				case STP_CLOSING:
				case STP_TIMER:
				case STP_UPDATER:
					thisRow.dataType = DATA_BYTE;
					break;
				case STP_MIN_WL_DATE:
					thisRow.dataType = DATA_LONG;
					break;
				default:
					thisRow.dataType = DATA_BOOLEAN;
				}
				lstData.add(thisRow);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
		}
	}
	
	void save() {
		if (!altered) return;
		boolean valid = true;
		int noUpdates = 0;
		long dataLong = 0;
		DataDefault thisRow;
		// No insert here
		PreparedStatement stm = null;
		try {
			stm = parent.dbPowerJ.getStatement(0);
			for (int i = 1; i < STP_COUNT; i++) {
				thisRow = lstData.get(i-1);
				if (thisRow.altered) {
					valid = true;
					switch (thisRow.dataType) {
					case DATA_STRING:
						if (thisRow.value.length() < 3) {
							valid = false;
						} else if (thisRow.value.length() > 64) {
							thisRow.value = thisRow.value.substring(0, 64);
						}
						break;
					case DATA_SHORT:
						dataLong = numbers.parseLong(thisRow.value);
						if (dataLong < 0 || dataLong > Short.MAX_VALUE) {
							valid = false;
						}
						break;
					case DATA_BYTE:
						dataLong = numbers.parseLong(thisRow.value);
						if (dataLong < 0 || dataLong > Byte.MAX_VALUE) {
							valid = false;
						}
						break;
					case DATA_LONG:
						dataLong = numbers.parseLong(thisRow.value);
						if (dataLong < 0) {
							valid = false;
						}
						break;
					default:
						if (thisRow.value.equals("y")) {
							thisRow.value = "Y";
						} else if (!thisRow.value.equals("Y")) {
							thisRow.value = "N";
						}
					}
					if (valid) {
						switch (i) {
						case STP_PASSWORD:
							if (thisRow.value.length() < 8) {
								valid = false;
							}
							break;
						case STP_OPENING:
							if (dataLong > 12 || dataLong < 0) {
								valid = false;
							}
							break;
						case STP_CLOSING:
							if (dataLong > 24 || dataLong < 12) {
								valid = false;
							}
							break;
						case STP_TIMER:
							if (dataLong > 10 || dataLong < 1) {
								valid = false;
							}
							break;
						case STP_UPDATER:
							if (dataLong > 180 || dataLong < 5) {
								valid = false;
							}
							break;
						default:
							// Rest do not need validation
							valid = true;
						}
					}
					if (valid) {
						stm.setString(1, thisRow.value);
						stm.setShort(2, thisRow.ID);
						noUpdates = stm.executeUpdate();
			            if (noUpdates > 0) {
				        	thisRow.altered = false;
			            }
					}
				}
			}
			altered = false;
			parent.variables.readDB(parent);
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} catch (NumberFormatException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}
	
	private void trackCheckboxes(Boolean selected, String name) {
		byte row = 0;
        String text = (selected ? "Y" : "N");
        if (name.equals("Saturday")) {
        	row = STP_SAT_OFF;
        } else if (name.equals("Sunday")) {
        	row = STP_SUN_OFF;
        } else if (name.equals("Coder1Active")) {
        	row = STP_CODER1_ACTIVE;
        } else if (name.equals("Coder2Active")) {
        	row = STP_CODER2_ACTIVE;
        } else if (name.equals("Coder3Active")) {
        	row = STP_CODER3_ACTIVE;
        } else {
        	row = STP_CODER4_ACTIVE;
        }
		DataDefault thisRow = lstData.get(row -1);
		thisRow.value = text;
		thisRow.altered = true;
		altered = true;
	}
	
	private void trackDocument(DocumentEvent e) {
		try {
			Document doc = (Document)e.getDocument();
			int length = doc.getLength();
			byte row = (Byte) doc.getProperty("RowID");
			DataDefault thisRow = lstData.get(row -1);
			thisRow.value = doc.getText(0, length);
			thisRow.altered = true;
			altered = true;
		} catch (BadLocationException ignore) {
		}
	}
	
	class DataDefault {
		boolean altered = false;
		byte ID = 0;
		byte dataType = DATA_STRING;
		String value = "";
	}

	public void focusGained(FocusEvent e) {
		Component c = e.getComponent();
		if (c != null) {
			if (c instanceof JTextComponent) {
				((JTextComponent)c).selectAll();
			}
		}
	}

	public void focusLost(FocusEvent e) {
		Component c = e.getComponent();
		if (c != null) {
			if (c instanceof JTextComponent) {
				((JTextComponent)c).setCaretPosition(0);
				((JTextComponent)c).setSelectionStart(0);
				((JTextComponent)c).setSelectionEnd(0);
				if (c instanceof JPasswordField) {
					char[] input = ((JPasswordField) c).getPassword();
					DataDefault thisRow = lstData.get(STP_PASSWORD -1);
					String password = String.copyValueOf(input).trim();
					if (!thisRow.value.equals(password)) {
						thisRow.value = password;
						thisRow.altered = true;
						altered = true;
					}
				}
			}
		}
	}

	public void itemStateChanged(ItemEvent e) {
		if (!programmaticChange) {
			JCheckBox checkBox = (JCheckBox)e.getSource();
	        trackCheckboxes(checkBox.isSelected(),
	        		checkBox.getName());
		}
	}

	public void insertUpdate(DocumentEvent e) {
		if (!programmaticChange) {
			trackDocument(e);
		}
	}

	public void removeUpdate(DocumentEvent e) {
		if (!programmaticChange) {
			trackDocument(e);
		}
	}

	public void changedUpdate(DocumentEvent e) {
		if (!programmaticChange) {
			trackDocument(e);
		}
	}
}
