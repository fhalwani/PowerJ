package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

class PnlContracts extends PnlMain {
	private static final long serialVersionUID = 1232486465946784856L;
	private final byte CONTRACT_FULLTIME = 1;
	private final byte CONTRACT_TYPE_WEEKDAYS = 2;
	private final byte CONTRACT_TYPE_DAILY = 3;
	private final byte CONTRACT_TYPE_WEEKLY = 4;
	private ButtonGroup contractGroup = new ButtonGroup();
	private JCheckBox chkMonday, chkTuesday, chkWednesday, chkThursday, chkFriday;
	private JIntegerField txtDaysOn, txtDaysOutOf, txtWeeksOn, txtWeeksOutOf;
	private JStringField txtDescr;
	private JTableEditor tblData;
	private ArrayList<ClassData> list = new ArrayList<ClassData>();
	private ClassData thisRow = new ClassData();

	PnlContracts(PowerJ parent) {
		super(parent);
		setName("Contracts");
		parent.dbPowerJ.prepareContract();
		readTable();
		createPanel();
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
		// Layout List panel on left side
		add(createPanelList(), BorderLayout.WEST);
		add(createPanelNames(), BorderLayout.CENTER);
		tblData.requestFocusInWindow();
		parent.statusBar.setMessage("No rows " + list.size());
	}

	private JPanel createPanelList() {
		ModelData mdlData = new ModelData();
		tblData = new JTableEditor(parent, mdlData);
		// detect row selection
		tblData.setName("tblData");
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
		scrollPane.setPreferredSize(new Dimension(200, 500));
		JPanel panel = new JPanel();
		panel.setOpaque(true);
		panel.add(scrollPane);
		return panel;
	}

	private JPanel createPanelNames() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		add(panel, BorderLayout.CENTER);
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Name: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_N);
		label.setLabelFor(txtDescr);
		Utilities.addComponent(label, 0, 0, 1, 1, 0, 0,
				GridBagConstraints.NONE, GridBagConstraints.WEST, panel);
		txtDescr = new JStringField(2, 64);
		txtDescr.setName("Descr");
		//  This class handles the ancestorAdded event and invokes the requestFocusInWindow() method
		txtDescr.addAncestorListener(new RequestFocusListener());
		txtDescr.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					JStringField source = (JStringField) e.getSource();
					if (source.altered()) {
						thisRow.descr = source.getText();
						setAltered();
					}
				}
			}
		});
		Utilities.addComponent(txtDescr, 1, 0, 4, 1, 1, 0, 
				GridBagConstraints.HORIZONTAL, GridBagConstraints.EAST, panel);
		JPanel radioPanel = new JPanel();
		radioPanel.setLayout(new BoxLayout(radioPanel, BoxLayout.X_AXIS));
		JRadioButton btnWeekdays = new JRadioButton("Weekdays");
		btnWeekdays.setMnemonic(KeyEvent.VK_W);
		btnWeekdays.setActionCommand("WEEKDAYS");
		btnWeekdays.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!programmaticChange) {
					thisRow.contractType = CONTRACT_TYPE_WEEKDAYS;
					setAltered();
					setData();
				}
			}
		});
		contractGroup.add(btnWeekdays);
		radioPanel.add(btnWeekdays);
		JRadioButton btnDaily = new JRadioButton("Daily");
		btnDaily.setMnemonic(KeyEvent.VK_D);
		btnDaily.setActionCommand("DAILY");
		btnDaily.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!programmaticChange) {
					thisRow.contractType = CONTRACT_TYPE_DAILY;
					setAltered();
					setData();
				}
			}
		});
		contractGroup.add(btnDaily);
		radioPanel.add(btnDaily);
		JRadioButton btnWeekly = new JRadioButton("Weekly");
		btnWeekly.setMnemonic(KeyEvent.VK_K);
		btnWeekly.setActionCommand("WEEKLY");
		btnWeekly.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!programmaticChange) {
					thisRow.contractType = CONTRACT_TYPE_WEEKLY;
					setAltered();
					setData();
				}
			}
		});
		contractGroup.add(btnWeekly);
		radioPanel.add(btnWeekly);
		Utilities.addComponent(radioPanel, 0, 1, 3, 1, 0, 0,
				GridBagConstraints.NONE, GridBagConstraints.WEST, panel);
		chkMonday = new JCheckBox("Monday");
		chkMonday.setEnabled(false);
		chkMonday.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisRow.no1 = (byte) (source.isSelected() ? 1: 0);
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkMonday, 0, 2, 1, 1, 0, 0,
				GridBagConstraints.NONE, GridBagConstraints.WEST, panel);
		chkTuesday = new JCheckBox("Tuesday");
		chkTuesday.setEnabled(false);
		chkTuesday.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisRow.no2 = (byte) (source.isSelected() ? 1: 0);
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkTuesday, 1, 2, 1, 1, 0, 0,
				GridBagConstraints.NONE, GridBagConstraints.WEST, panel);
		chkWednesday = new JCheckBox("Wednesday");
		chkWednesday.setEnabled(false);
		chkWednesday.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisRow.no3 = (byte) (source.isSelected() ? 1: 0);
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkWednesday, 2, 2, 1, 1, 0, 0,
				GridBagConstraints.NONE, GridBagConstraints.WEST, panel);
		chkThursday = new JCheckBox("Thursday");
		chkThursday.setEnabled(false);
		chkThursday.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisRow.no4 = (byte) (source.isSelected() ? 1: 0);
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkThursday, 3, 2, 1, 1, 0, 0,
				GridBagConstraints.NONE, GridBagConstraints.WEST, panel);
		chkFriday = new JCheckBox("Friday");
		chkFriday.setEnabled(false);
		chkFriday.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!programmaticChange) {
					JCheckBox source = (JCheckBox) e.getSource();
					thisRow.no1 = (byte) (source.isSelected() ? 1: 0);
					setAltered();
				}
			}
		});
		Utilities.addComponent(chkFriday, 4, 2, 1, 1, 0, 0,
				GridBagConstraints.NONE, GridBagConstraints.WEST, panel);
		txtDaysOn = new JIntegerField(parent, 1, 9);
		txtDaysOn.setEnabled(false);
		txtDaysOn.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					JIntegerField source = (JIntegerField) e.getSource();
					if (source.altered()) {
						thisRow.no1 = (byte) source.getInt();
						setAltered();
					}
				}
			}
		});
		Utilities.addComponent(txtDaysOn, 0, 3, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		label = new JLabel("Every");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		Utilities.addComponent(label, 1, 3, 1, 1, 0, 0,
				GridBagConstraints.NONE, GridBagConstraints.WEST, panel);
		txtDaysOutOf = new JIntegerField(parent, 2, 10);
		txtDaysOutOf.setEnabled(false);
		txtDaysOutOf.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					JIntegerField source = (JIntegerField) e.getSource();
					if (source.altered()) {
						thisRow.no2 = (byte) source.getInt();
						setAltered();
					}
				}
			}
		});
		Utilities.addComponent(txtDaysOutOf, 2, 3, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		label = new JLabel("Days");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		Utilities.addComponent(label, 3, 3, 1, 1, 0, 0,
				GridBagConstraints.NONE, GridBagConstraints.WEST, panel);
		txtWeeksOn = new JIntegerField(parent, 1, 9);
		txtWeeksOn.setEnabled(false);
		txtWeeksOn.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					JIntegerField source = (JIntegerField) e.getSource();
					if (source.altered()) {
						thisRow.no1 = (byte) source.getInt();
						setAltered();
					}
				}
			}
		});
		Utilities.addComponent(txtWeeksOn, 0, 4, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		label = new JLabel("Every");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		Utilities.addComponent(label, 1, 4, 1, 1, 0, 0,
				GridBagConstraints.NONE, GridBagConstraints.WEST, panel);
		txtWeeksOutOf = new JIntegerField(parent, 2, 10);
		txtWeeksOutOf.setEnabled(false);
		txtWeeksOutOf.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (!programmaticChange) {
					JIntegerField source = (JIntegerField) e.getSource();
					if (source.altered()) {
						thisRow.no2 = (byte) source.getInt();
						setAltered();
					}
				}
			}
		});
		Utilities.addComponent(txtWeeksOutOf, 2, 4, 1, 1, 0.3, 0,
				GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST, panel);
		label = new JLabel("Weeks");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		Utilities.addComponent(label, 3, 4, 1, 1, 0, 0,
				GridBagConstraints.NONE, GridBagConstraints.WEST, panel);
		Utilities.addComponent(Box.createVerticalGlue(), 0, 5, 1, 1, 1, 1, 
				GridBagConstraints.VERTICAL, GridBagConstraints.NORTH, panel);
		return panel;
	}

	private boolean inspectRow() {
		boolean valid = true;
		if (thisRow.descr.length() > 64) {
			thisRow.descr = thisRow.descr.substring(0, 64);
		} else if (thisRow.descr.length() < 2) {
			valid = false;
			new DlgMessage(parent, JOptionPane.WARNING_MESSAGE, getName(),
					"Invalid contract name.");
		}
		switch (thisRow.contractType) {
		case CONTRACT_TYPE_WEEKDAYS:
			if (thisRow.no1 + thisRow.no2 + thisRow.no3 + thisRow.no4 + thisRow.no5 < 1) {
				valid = false;
				new DlgMessage(parent, JOptionPane.WARNING_MESSAGE, getName(),
						"The contract must include at least 1 day of the week.");
			}
			break;
		case CONTRACT_TYPE_DAILY:
			thisRow.no3 = 0;
			thisRow.no4 = 0;
			thisRow.no5 = 0;
			if (thisRow.no1 < 1 || thisRow.no2 <= thisRow.no1) {
				valid = false;
				new DlgMessage(parent, JOptionPane.WARNING_MESSAGE, getName(),
						"Invalid daily contract syntax, consider editing (1 of 2, 3 of 4, etc).");
			}
			break;
		case CONTRACT_TYPE_WEEKLY:
			thisRow.no3 = 0;
			thisRow.no4 = 0;
			thisRow.no5 = 0;
			if (thisRow.no1 < 1 || thisRow.no2 <= thisRow.no1) {
				valid = false;
				new DlgMessage(parent, JOptionPane.WARNING_MESSAGE, getName(),
						"Invalid weekly contract syntax, consider editing (1 of 2, 3 of 4, etc).");
			}
			break;
		default:
			thisRow.contractType = CONTRACT_FULLTIME;
			thisRow.no1 = 0;
			thisRow.no2 = 0;
			thisRow.no3 = 0;
			thisRow.no4 = 0;
			thisRow.no5 = 0;
		}
		return valid;
	}

	private void readTable() {
		short lastID = 1;
		ResultSet rst = parent.dbPowerJ.getContracts();
		try {
			while (rst.next()) {
				if (rst.getShort("ID") == 1) {
					// Locked; cannot be edited or deleted
					continue;
				}
				thisRow = new ClassData();
				thisRow.newRow = false;
				thisRow.contractID = rst.getShort("ID");
				thisRow.contractType = rst.getShort("COTYPE");
				thisRow.no1 = rst.getByte("CONO1");
				thisRow.no2 = rst.getByte("CONO2");
				thisRow.no3 = rst.getByte("CONO3");
				thisRow.no4 = rst.getByte("CONO4");
				thisRow.no5 = rst.getByte("CONO5");
				thisRow.descr = rst.getString("DESCR");
				list.add(thisRow);
				if (lastID < thisRow.contractID) {
					lastID = thisRow.contractID;
				}
			}
			// Add a blank
			lastID++;
			thisRow = new ClassData();
			thisRow.newRow = true;
			thisRow.contractID = lastID;
			list.add(thisRow);
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	private void setAltered() {
		if (!thisRow.altered) {
			thisRow.altered = true;
			altered = true;
			if (thisRow.newRow) {
				// Add another new row
				short lastID = thisRow.contractID;
				lastID++;
				ClassData anotherRow = new ClassData();
				anotherRow.newRow = true;
				anotherRow.contractID = lastID;
				list.add(anotherRow);
				AbstractTableModel mdl = (AbstractTableModel) tblData.getModel();
				mdl.fireTableRowsInserted(list.size()-1, list.size()-1);
			}
		}
	}

	private void setData() {
		programmaticChange = true;
		switch (thisRow.contractType) {
		case CONTRACT_TYPE_WEEKDAYS:
			chkMonday.setEnabled(true);
			chkTuesday.setEnabled(true);
			chkWednesday.setEnabled(true);
			chkThursday.setEnabled(true);
			chkFriday.setEnabled(true);
			txtDaysOn.setEnabled(false);
			txtDaysOutOf.setEnabled(false);
			txtWeeksOn.setEnabled(false);
			txtWeeksOutOf.setEnabled(false);
			break;
		case CONTRACT_TYPE_DAILY:
			chkMonday.setEnabled(false);
			chkTuesday.setEnabled(false);
			chkWednesday.setEnabled(false);
			chkThursday.setEnabled(false);
			chkFriday.setEnabled(false);
			txtDaysOn.setEnabled(true);
			txtDaysOutOf.setEnabled(true);
			txtWeeksOn.setEnabled(false);
			txtWeeksOutOf.setEnabled(false);
			chkMonday.setSelected(false);
			chkTuesday.setSelected(false);
			chkWednesday.setSelected(false);
			chkThursday.setSelected(false);
			chkFriday.setSelected(false);
			break;
		case CONTRACT_TYPE_WEEKLY:
			chkMonday.setEnabled(false);
			chkTuesday.setEnabled(false);
			chkWednesday.setEnabled(false);
			chkThursday.setEnabled(false);
			chkFriday.setEnabled(false);
			txtDaysOn.setEnabled(false);
			txtDaysOutOf.setEnabled(false);
			txtWeeksOn.setEnabled(true);
			txtWeeksOutOf.setEnabled(true);
			break;
		default:
			chkMonday.setEnabled(false);
			chkTuesday.setEnabled(false);
			chkWednesday.setEnabled(false);
			chkThursday.setEnabled(false);
			chkFriday.setEnabled(false);
			txtDaysOn.setEnabled(false);
			txtDaysOutOf.setEnabled(false);
			txtWeeksOn.setEnabled(false);
			txtWeeksOutOf.setEnabled(false);
		}
		programmaticChange = false;
	}

	void save() {
		boolean failed = false;
		int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			for (int i = 0; i < list.size(); i++) {
				thisRow = list.get(i);
				if (!thisRow.altered) {
					if (txtDescr.altered()) {
						thisRow.altered = true;
						thisRow.descr = txtDescr.getText().trim();
					}
				}
				if (thisRow.altered) {
					if (!inspectRow()) {
						failed = true;
						continue;
					} else if (thisRow.newRow) {
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
					stm.setShort(1, thisRow.contractType);
					stm.setByte(2, thisRow.no1);
					stm.setByte(3, thisRow.no2);
					stm.setByte(4, thisRow.no3);
					stm.setByte(5, thisRow.no4);
					stm.setByte(6, thisRow.no5);
					stm.setString(7, thisRow.descr);
					stm.setShort(8, thisRow.contractID);
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
		if (row > list.size() -1) {
			// Called from an empty list; clear text fields
			thisRow = new ClassData();
		} else {
			thisRow = list.get(row);
		}
		setData();
		programmaticChange = true;
		txtDescr.setText(thisRow.descr);
		Enumeration<AbstractButton> elements = contractGroup.getElements();
		switch (thisRow.contractType) {
		case CONTRACT_TYPE_WEEKDAYS:
			while (elements.hasMoreElements()) {
				AbstractButton button = (AbstractButton)elements.nextElement();
				if (button.getActionCommand().equals("WEEKDAYS")) {
					button.setSelected(true);
				}
			}
			chkMonday.setSelected(thisRow.no1 > 0);
			chkTuesday.setSelected(thisRow.no2 > 0);
			chkWednesday.setSelected(thisRow.no3 > 0);
			chkThursday.setSelected(thisRow.no4 > 0);
			chkFriday.setSelected(thisRow.no5 > 0);
			txtDaysOn.setInt(0);
			txtDaysOutOf.setInt(0);
			txtWeeksOn.setInt(0);
			txtWeeksOutOf.setInt(0);
			break;
		case CONTRACT_TYPE_DAILY:
			while (elements.hasMoreElements()) {
				AbstractButton button = (AbstractButton)elements.nextElement();
				if (button.getActionCommand().equals("DAILY")) {
					button.setSelected(true);
				}
			}
			chkMonday.setSelected(false);
			chkTuesday.setSelected(false);
			chkWednesday.setSelected(false);
			chkThursday.setSelected(false);
			chkFriday.setSelected(false);
			txtDaysOn.setInt(thisRow.no1);
			txtDaysOutOf.setInt(thisRow.no2);
			txtWeeksOn.setInt(0);
			txtWeeksOutOf.setInt(0);
			break;
		case CONTRACT_TYPE_WEEKLY:
			while (elements.hasMoreElements()) {
				AbstractButton button = (AbstractButton)elements.nextElement();
				if (button.getActionCommand().equals("WEEKLY")) {
					button.setSelected(true);
				}
			}
			chkMonday.setSelected(false);
			chkTuesday.setSelected(false);
			chkWednesday.setSelected(false);
			chkThursday.setSelected(false);
			chkFriday.setSelected(false);
			txtDaysOn.setInt(0);
			txtDaysOutOf.setInt(0);
			txtWeeksOn.setInt(thisRow.no1);
			txtWeeksOutOf.setInt(thisRow.no2);
			break;
		default:
			contractGroup.clearSelection();
			chkMonday.setSelected(false);
			chkTuesday.setSelected(false);
			chkWednesday.setSelected(false);
			chkThursday.setSelected(false);
			chkFriday.setSelected(false);
			txtDaysOn.setInt(0);
			txtDaysOutOf.setInt(0);
			txtWeeksOn.setInt(0);
			txtWeeksOutOf.setInt(0);
		}
		programmaticChange = false;

	}

	private class ClassData {
		boolean altered = false;
		boolean newRow = false;
		short contractID = 0;
		short contractType = 0;
		byte no1 = 0;
		byte no2 = 0;
		byte no3 = 0;
		byte no4 = 0;
		byte no5 = 0;
		String descr = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = -3614866125235585646L;

		public int getColumnCount() {
			return 1;
		}

		public int getRowCount() {
			return list.size();
		}

		public String getColumnName(int col) {
			return "Contracts";
		}

		public Object getValueAt(int row, int col) {
			if (row < list.size()) {
				return list.get(row).descr;
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
