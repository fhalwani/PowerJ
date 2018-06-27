package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

class PnlSubspecials extends PnlMain {
	private static final long serialVersionUID = 6726234073783722650L;
	private final byte DATA_NAME = 0;
	private final byte DATA_INIT = 1;
	private final byte DATA_SPY = 2;
	private byte newID = 0;
	private ArrayList<ClassData> list = new ArrayList<ClassData>();

	PnlSubspecials(PowerJ parent) {
		super(parent);
		setName("Subspecialties");
		parent.dbPowerJ.prepareSubspecialties();
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
		CboSpecialties cboSpecialties = new CboSpecialties(parent, true);
		JTableEditor tblData = new JTableEditor(parent, new ModelData());
		TableColumn column = tblData.getColumnModel().getColumn(DATA_SPY);
		column.setCellEditor(new DefaultCellEditor(cboSpecialties));
		JScrollPane scrollPane = new JScrollPane(tblData,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		scrollPane.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 8755059126979273544L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		add(scrollPane, BorderLayout.CENTER);
		parent.statusBar.setMessage("No rows " + list.size());
	}
	
	private void readTable() {
		ResultSet rst = parent.dbPowerJ.getSubspecialties(1);
		ClassData thisRow = new ClassData ();
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.ID = rst.getByte("SUBID");
				thisRow.initials = rst.getString("SUBINIT");
				thisRow.name = rst.getString("SUBNAME");
				thisRow.specialty = new DataItem(rst.getShort("SPYID"),
						rst.getString("SPYNAME"));
				list.add(thisRow);
				if (newID < thisRow.ID) {
					newID = thisRow.ID;
				}
			}
			newID++;
			parent.statusBar.setMessage("No rows " + list.size());
			// Add a blank row
			thisRow = new ClassData();
			thisRow.newRow = true;
			list.add(thisRow);
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
		ClassData thisRow = new ClassData();
		PreparedStatement stm = null;
		try {
			for (int i = 0; i < list.size(); i++) {
				thisRow = list.get(i);
				if (thisRow.altered) {
					thisRow.initials = thisRow.initials.trim();
					if (thisRow.initials.length() > 3) {
						thisRow.initials = thisRow.initials.substring(0, 3);
					}
					thisRow.name = thisRow.name.trim();
					if (thisRow.name.length() > 30) {
						thisRow.name = thisRow.name.substring(0, 30);
					}
					if (thisRow.newRow) {
						thisRow.ID = newID;
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
					stm.setInt(1, thisRow.specialty.getValue());
		            stm.setString(2, thisRow.initials);
		            stm.setString(3, thisRow.name);
		            stm.setByte(4, thisRow.ID);
					noUpdates = stm.executeUpdate();
					if (noUpdates > 0) {
						thisRow.altered = false;
						if (thisRow.newRow) {
							thisRow.newRow = false;
							newID++;
						}
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
		boolean newRow = false;
		byte ID = 0;
		String initials = "";
		String name = "";
		// SpyID + SpyName
		DataItem specialty = new DataItem(0, "");
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = 3478621953527581073L;
		private final String[] columns = {"NAME", "SHORT", "SPECIALTY"};
		
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
			Object objValue = Object.class;
			try {
				if (row < list.size()) {
					ClassData thisRow = list.get(row);
					switch (col) {
					case DATA_NAME:
						objValue = thisRow.name;
						break;
					case DATA_SPY:
						objValue = thisRow.specialty;
						break;
					default:
						objValue = thisRow.initials;
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return objValue;
		}

		public Class<?> getColumnClass(int col) {
			return String.class;
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				ClassData thisRow = list.get(row);
				switch (col) {
				case DATA_INIT:
					thisRow.initials = (String) value;
					break;
				case DATA_SPY:
					thisRow.specialty = (DataItem) value;
					break;
				default:
					thisRow.name = (String) value;
				}
				thisRow.altered = true;
				altered = true;
				if (row == list.size()) {
					// Add a blank row
					thisRow = new ClassData();
					thisRow.newRow = true;
					list.add(thisRow);
					fireTableDataChanged();
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}
}
