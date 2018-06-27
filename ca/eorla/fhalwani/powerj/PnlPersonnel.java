package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

class PnlPersonnel extends PnlMain implements ItemListener {
	private static final long serialVersionUID = 9036100389934847044L;
	private volatile boolean programmaticChange = true;
	private String strFilter = "";
	private final String[] strAccess = {
			"Gross",      "Histo",       "Diagnosis",  "View Names",
			"Workload",   "Specimens",   "Frozens",    "Errors",
			"Additional", "Case Editor", "Statistics", "",
			"Scanner",    "Statistics",  "Turnaround", "Tracker",
			"Workload",   "",            "",           "",
			"",           "",            "",           "",
			"",           "",            "",           "",
			"",           "Schedule",    "Dashboard",  "Workload"};
	private JStringField txtInitials, txtLastname, txtFirstname;
	private CboPersonnel cboPosition;
	private ArrayList<JCheckBox> checkboxes = new ArrayList<JCheckBox>();
	private ArrayList<ClassData> list = new ArrayList<ClassData>();
	private ClassData thisRow = new ClassData();
	private JTableEditor tblData;

	PnlPersonnel(PowerJ parent) {
		super(parent);
		setName("Personnel");
		parent.dbPowerJ.preparePersonnel();
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
			list.clear();
			super.close();
		}
		return !altered;
	}
	
	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		for (int i = 0; i < 32; i++) {
			JCheckBox checkBox = new JCheckBox();
			checkBox.setName(strAccess[i]);
			checkBox.setText(strAccess[i]);
			checkBox.addItemListener(this);
			checkboxes.add(checkBox);
		}
		// Layout 5 panels from top to bottom.
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(createPanelNames());
		boxPanel.add(createPanelDashboard());
		boxPanel.add(createPanelWorkload());
		boxPanel.add(createPanelReports());
		boxPanel.add(createPanelSetup());
		add(createToolbar(), BorderLayout.NORTH);
		add(boxPanel, BorderLayout.EAST);
		// Layout List panel on left side
		add(createPanelList(), BorderLayout.WEST);
		if (list.size() > 0) {
			tblData.setRowSelectionInterval(0,0);
			updateRow(0);
		}
		tblData.requestFocusInWindow();
		parent.statusBar.setMessage("No rows " + list.size());
	}
	
	private JPanel createPanelDashboard() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Dashboard");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Dashboard");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		for (int i = 0; i < 4; i++) {
			if (strAccess[i].length() > 0) {
				Utilities.addComponent(checkboxes.get(i),
						(i % 2 == 0 ? 0 : 1),
						(i / 2), 1, 1, 0.5, 0,
						GridBagConstraints.HORIZONTAL,
						GridBagConstraints.EAST, panel);
			}
		}
		return panel;
	}

	private JPanel createPanelList() {
		ModelData mdlData = new ModelData();
		tblData = new JTableEditor(parent, mdlData);
		// detect row selection
		tblData.setName("tblPersonnel");
        tblData.getSelectionModel().addListSelectionListener(
        		new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages
		        if (e.getValueIsAdjusting()) return;
		        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) return;
		        int viewRow = lsm.getMinSelectionIndex();
		        if (viewRow > -1) {
					// else, Selection got filtered away.
					int modelRow = tblData.convertRowIndexToModel(viewRow);
					updateRow(modelRow);
		        }
			}
        });
		JScrollPane scrollPane = new JScrollPane(tblData,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Border borderEmpty = BorderFactory.createEmptyBorder(2, 5, 2, 5);
		scrollPane.setBorder(borderEmpty);
		scrollPane.setPreferredSize(new Dimension(320, 600));
		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.add(scrollPane);
		return panel;
	}
	
	private JPanel createPanelNames() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Personnel");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Personnel");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("First Name: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_F);
		label.setLabelFor(txtFirstname);
		Utilities.addComponent(label, 0, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		txtFirstname = new JStringField(2, 30);
		txtFirstname.setName("Firstname");
		txtFirstname.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					JStringField source = (JStringField) e.getSource();
					if (source.altered()) {
						thisRow.altered = true;
						altered = true;
					}
				}
			}
		});
		Utilities.addComponent(txtFirstname, 1, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Last Name: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_L);
		label.setLabelFor(txtLastname);
		Utilities.addComponent(label, 2, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		txtLastname = new JStringField(2, 30);
		txtLastname.setName("Lastname");
		txtLastname.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					JStringField source = (JStringField) e.getSource();
					if (source.altered()) {
						thisRow.altered = true;
						altered = true;
					}
				}
			}
		});
		Utilities.addComponent(txtLastname, 3, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Initials: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_I);
		label.setLabelFor(txtInitials);
		Utilities.addComponent(label, 0, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		txtInitials= new JStringField(2, 3);
		txtInitials.setName("Initials");
		txtInitials.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					JStringField source = (JStringField) e.getSource();
					if (source.altered()) {
						thisRow.altered = true;
						altered = true;
					}
				}
			}
		});
		Utilities.addComponent(txtInitials, 1, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		label = new JLabel("Position: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		label.setLabelFor(cboPosition);
		Utilities.addComponent(label, 2, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		cboPosition = new CboPersonnel(true);
		cboPosition.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (!programmaticChange) {
		    		if (e.getStateChange() == ItemEvent.SELECTED) {
		    			JComboBox cb = (JComboBox)e.getSource();
		    			thisRow.position = (String) cb.getSelectedItem();
		    			thisRow.altered = true;
		    			altered = true;
		    		}
	    		}
	        }
	    });
		Utilities.addComponent(cboPosition, 3, 1, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, panel);
		return panel;
	}
	
	private JPanel createPanelReports() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Reports");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Reports");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		// 12-29
		for (int i = 12; i < 29; i++) {
			if (strAccess[i].length() > 0) {
				Utilities.addComponent(checkboxes.get(i),
						(i % 2 == 0 ? 0 : 1),
						((i-12) / 2), 1, 1, 0.5, 0,
						GridBagConstraints.HORIZONTAL,
						GridBagConstraints.EAST, panel);
			}
		}
		return panel;
	}
	
	private JPanel createPanelSetup() {
		JPanel panel = new JPanel();
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Setup");
		title.setTitleJustification(TitledBorder.CENTER);
		panel.setBorder(title);
		panel.setName("Setup");
		panel.setLayout(new GridBagLayout());
		panel.setOpaque(true);
		border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		for (int i = 29; i < 32; i++) {
			if (strAccess[i].length() > 0) {
				Utilities.addComponent(checkboxes.get(i),
						(i % 2 == 0 ? 0 : 1),
						((i-29) / 2), 1, 1, 0.5, 0,
						GridBagConstraints.HORIZONTAL,
						GridBagConstraints.EAST, panel);
			}
		}
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
		// Workload Audit Panels 4-11
		for (int i = 4; i < 11; i++) {
			if (strAccess[i].length() > 0) {
				Utilities.addComponent(checkboxes.get(i),
						(i % 2 == 0 ? 0 : 1),
						((i-4) / 2), 1, 1, 0.5, 0,
						GridBagConstraints.HORIZONTAL,
						GridBagConstraints.EAST, panel);
			}
		}
		return panel;
	}

	private JPanel createToolbar() {
		CboPersonnel cboPersonnel = new CboPersonnel(false);
		cboPersonnel.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			strFilter = (String) cb.getSelectedItem();
	    			readFilters();
	    		}
	        }
	    });
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Position:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_G);
		label.setLabelFor(cboPersonnel);
		panel.add(label);
		panel.add(cboPersonnel);
		return panel;
	}
	
	private void readFilters() {
		int noRows = list.size();
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		if (strFilter.trim().length() > 0 && 
				!strFilter.equals("* All *")) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).position.equals(strFilter));
				}
			};
			// Count filtered rows
			for (int i = 0; i < list.size(); i++) {
				if (!list.get(i).position.equals(strFilter)) {
					noRows--;
				}
			}
		}
		@SuppressWarnings("unchecked")
		TableRowSorter<ModelData> sorter = (TableRowSorter<ModelData>) tblData.getRowSorter();
		sorter.setRowFilter(rowFilter);
		sorter.sort();
		parent.statusBar.setMessage("No rows " + noRows);
	}
	
	private void readTable() {
		short lastID = 0;
		ResultSet rst = parent.dbPowerJ.getPersonnel(1);
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.ID = rst.getShort("PERID");
				thisRow.access = rst.getInt("ACCESS");
				thisRow.position = rst.getString("CODE");
				thisRow.initials = rst.getString("INITIALS");
				thisRow.lastname = rst.getString("PLAST");
				thisRow.firstname = rst.getString("PFIRST");
				thisRow.bits = parent.numbers.toBits(thisRow.access);
				list.add(thisRow);
				if (lastID < thisRow.ID) {
					lastID = thisRow.ID;
				}
			}
			if (!parent.variables.offLine) {
				readUpdates(lastID);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	private void readUpdates(short lastID) {
		DbAPIS dbAP = new DbAPIS(parent);
		if (!dbAP.connected) {
			return;
		}
		int noInserts = 0;
		ResultSet rst = dbAP.getPersons(lastID);
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.newRow = true;
				thisRow.altered = true;
				thisRow.ID = rst.getShort("id");
				thisRow.position = rst.getString("persnl_class_id").trim();
				thisRow.lastname = rst.getString("last_name").trim();
				thisRow.firstname = rst.getString("first_name").trim();
				thisRow.initials = thisRow.firstname.toUpperCase().substring(0, 1) +
						thisRow.lastname.toUpperCase().substring(0, 2);
				list.add(thisRow);
				noInserts++;
			}
			if (noInserts > 0) {
				save();
				parent.log(JOptionPane.INFORMATION_MESSAGE, getName(),
						"Found " + noInserts + " new personnel in Powerpath.");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			dbAP.closeRst(rst);
			dbAP.closeStm();
			dbAP.close();
		}
	}

	void save() {
		boolean failed = false;
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			for (int i = 0; i < list.size(); i++) {
				thisRow = list.get(i);
		        if (thisRow.altered) {
					thisRow.position = thisRow.position.trim().toUpperCase();
					thisRow.initials = thisRow.initials.trim().toUpperCase();
					thisRow.lastname = thisRow.lastname.trim();
					thisRow.firstname = thisRow.firstname.trim();
			        if (thisRow.position.length() > 2) {
			        	thisRow.position = thisRow.position.substring(0, 2);
			        }
			        if (thisRow.initials.length() > 3) {
			        	thisRow.initials = thisRow.initials.substring(0, 3);
			        }
			        if (thisRow.lastname.length() > 30) {
			        	thisRow.lastname = thisRow.lastname.substring(0, 30);
			        }
			        if (thisRow.firstname.length() > 30) {
			        	thisRow.firstname = thisRow.firstname.substring(0, 30);
			        }
					if (thisRow.newRow) {
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
		            stm.setInt(1, thisRow.access);
		            stm.setString(2, thisRow.position);
		            stm.setString(3, thisRow.initials);
		            stm.setString(4, thisRow.lastname);
		            stm.setString(5, thisRow.firstname);
		            stm.setShort(6, thisRow.ID);
					noUpdates = stm.executeUpdate();
		            if (noUpdates > 0) {
			        	thisRow.altered = false;
			        	thisRow.newRow = false;
					} else {
						failed = true;
		            }
		        }
			}
			if (!failed) {
				altered = false;
			}
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}
	
	private void updateRow(int row) {
		if (txtFirstname.altered()) {
			thisRow.firstname = txtFirstname.getText();
			thisRow.altered = true;
		}
		if (txtLastname.altered()) {
			thisRow.lastname = txtLastname.getText();
			thisRow.altered = true;
		}
		if (txtInitials.altered()) {
			thisRow.initials = txtInitials.getText();
			thisRow.altered = true;
		}
		if (thisRow.altered) {
			for (int i = 0; i < 32; i++) {
				thisRow.bits[i] = checkboxes.get(i).isSelected();
			}
			thisRow.access = parent.numbers.toInt(thisRow.bits);
			// Save it later
			altered = true;
		}
		// Move to desired row
		programmaticChange = true;
		thisRow = list.get(row);
		txtFirstname.setText(thisRow.firstname);
		txtLastname.setText(thisRow.lastname);
		txtInitials.setText(thisRow.initials);
		cboPosition.setSelectedItem(thisRow.position);
		for (int i = 0; i < 32; i++) {
			checkboxes.get(i).setSelected(thisRow.bits[i]);
		}
		programmaticChange = false;
	}
	
	public void itemStateChanged(ItemEvent e) {
		if (!programmaticChange) {
			thisRow.altered = true;
			altered = true;
		}
	}

	private class ClassData {
		boolean altered = false;
		boolean newRow = false;
		boolean[] bits = new boolean[32];
		short ID = 0;
		int access = 0;
		String position = "";
		String initials = "";
		String lastname = "";
		String firstname = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = 4481642348680201074L;

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return list.size();
		}

		public String getColumnName(int col) {
			return "Personnel";
		}

		public Object getValueAt(int row, int col) {
			if (row < list.size()) {
				ClassData item = list.get(row);
				return item.lastname + ", " + item.firstname;
			}
			return "";
		}

		public Class<?> getColumnClass(int col) {
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			// This table is not editable
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Ignore, table not editable
		}
	}
}
