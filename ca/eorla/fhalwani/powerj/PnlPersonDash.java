package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

class PnlPersonDash extends PnlMain {
	private static final long serialVersionUID = 87628136758074677L;
	private final byte DATA_FIRSTNAME = 0;
	private final byte DATA_LASTNAME = 1;
	private final byte DATA_ROLE = 2;
	private final byte DATA_INITIALS = 3;
	private String strFilter = "";
	private ClassData thisRow = new ClassData();
	private ArrayList<ClassData> list = new ArrayList<ClassData>();
	private JTableEditor tblData;

	PnlPersonDash(PowerJ parent) {
		super(parent);
		setName("Personnel");
		parent.dbPowerJ.preparePersonDash();
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
		tblData = new JTableEditor(parent, new ModelData());
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JScrollPane scrollPane = new JScrollPane(tblData,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(border);
		scrollPane.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 8755059126979273544L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		add(createToolbar(), BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		parent.statusBar.setMessage("No rows " + list.size());
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
		ResultSet rst = parent.dbPowerJ.getPersonnel(1);
		try {
			while (rst.next()) {
				if (!rst.getString("CODE").equalsIgnoreCase("PT")) {
					thisRow = new ClassData();
					thisRow.ID = rst.getShort("PERID");
					thisRow.firstname = rst.getString("PFIRST");
					thisRow.lastname = rst.getString("PLAST");
					thisRow.initials = rst.getString("INITIALS");
					thisRow.position = rst.getString("CODE");
					list.add(thisRow);
				}
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	void save() {
		boolean failed = false;
        int noUpdates = 0;
		PreparedStatement stm = null;
		try {
			stm = parent.dbPowerJ.getStatement(0);
			for (int i = 0; i < list.size(); i++) {
				thisRow = list.get(i);
				if (thisRow.altered) {
					thisRow.initials = thisRow.initials.trim().toUpperCase();
			        if (thisRow.initials.length() > 3) {
			        	thisRow.initials = thisRow.initials.substring(0, 3);
			        }
					stm.setString(1, thisRow.initials);
					stm.setShort(2, thisRow.ID);
					noUpdates = stm.executeUpdate();
					if (noUpdates > 0) {
						thisRow.altered = false;
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

	private class ClassData {
		boolean altered = false;
		short ID = 0;
		String lastname = "";
		String firstname = "";
		String position = "";
		String initials = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = 7118376276607650658L;
		private final String[] columns = {"First", "Last", "Role", "Initials"};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return list.size();
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			try {
				if (row < list.size()) {
					thisRow = list.get(row);
					switch (col) {
					case DATA_FIRSTNAME:
						value = thisRow.firstname;
						break;
					case DATA_LASTNAME:
						value = thisRow.lastname;
						break;
					case DATA_ROLE:
						value = thisRow.position;
						break;
					default:
						value = thisRow.initials;
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return value;
		}

		public Class<?> getColumnClass(int col) {
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			return (col == DATA_INITIALS);
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				if (col == DATA_INITIALS) {
					thisRow.initials = (String) value;
					thisRow.altered = true;
					altered = true;
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}
}
