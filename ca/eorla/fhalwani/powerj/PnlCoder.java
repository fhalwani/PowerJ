package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

class PnlCoder extends PnlMain {
	private static final long serialVersionUID = -6023212164334489551L;
	private final byte DATA_CODE_NAME = 0;
	// private final byte DATA_CODE_DESCR = 1;
	private final byte DATA_CODE_RULE = 2;
	private final byte DATA_CODE_COUNT = 3;
	private final byte DATA_CODE_VALUE1 = 4;
	private final byte DATA_CODE_VALUE2 = 5;
	private final byte DATA_CODE_VALUE3 = 6;
	private HashMap<Short, String> mapCodes = new HashMap<Short, String>();
	private ArrayList<ClassData> list = new ArrayList<ClassData>();
	private JTableEditor tblData;

	PnlCoder(PowerJ parent, byte coderID) {
		super(parent);
		setName(parent.variables.codersName[coderID -1]);
		mapCodes = DataRules.setMap(parent);
		parent.dbPowerJ.prepareCoder(coderID);
		readTable(coderID);
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
			parent.dbPowerJ.closeStms();
			list.clear();
			mapCodes.clear();
			super.close();
		}
		return !altered;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		tblData = new JTableEditor(parent, new ModelData());
		TableColumn column = null;
		for (int i = DATA_CODE_VALUE3; i >= 0; i--) {
			column = tblData.getColumnModel().getColumn(i);
			switch (i) {
			case DATA_CODE_RULE:
				JComboBox cboEditor = new JComboBox(
						new DefaultComboBoxModel(setItems()));
				cboEditor.setName("cboEditor");
				cboEditor.setFont(Constants.APP_FONT);
				cboEditor.setEditable(false);
				column.setCellEditor(new DefaultCellEditor(cboEditor));
				break;
			default:
				// Use defaults created in class JTableEditor()
			}
		}
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
		add(scrollPane, BorderLayout.CENTER);
		parent.statusBar.setMessage("No rows " + list.size());
	}
	
	private void readTable(byte coderID) {
		ResultSet rst = parent.dbPowerJ.getCoder(coderID);
		ClassData thisRow = new ClassData();
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.ID = rst.getShort("ID");
				thisRow.count = rst.getByte("COUNT");
				thisRow.value1 = rst.getDouble("VALUE1");
				thisRow.value2 = rst.getDouble("VALUE2");
				thisRow.value3 = rst.getDouble("VALUE3");
				thisRow.name = rst.getString("NAME");
				thisRow.description = rst.getString("DESCR");
				thisRow.rule = new DataItem(rst.getShort("RULEID"),
						mapCodes.get(rst.getShort("RULEID")));
				list.add(thisRow);
			}
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
		short newID = 0;
        int noUpdates = 0;
		ClassData thisRow = new ClassData();
		PreparedStatement stm = null;
		try {
			for (int i = 0; i < list.size(); i++) {
				thisRow = list.get(i);
				if (newID <= thisRow.ID) {
					newID = (short) (thisRow.ID +1);
				}
				if (thisRow.altered) {
					thisRow.description = thisRow.description.trim();
					if (thisRow.description.length() > 128) {
						thisRow.description = thisRow.description.substring(0, 128);
					}
					thisRow.name = thisRow.name.trim();
					if (thisRow.name.length() > 16) {
						thisRow.name = thisRow.name.substring(0, 16);
					}
					if (thisRow.count > Byte.MAX_VALUE) {
						thisRow.count = Byte.MAX_VALUE;
					}
					if (thisRow.newRow) {
						thisRow.ID = newID;
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
					stm.setInt(1, thisRow.rule.getValue());
					stm.setByte(2, thisRow.count);
					stm.setDouble(3, thisRow.value1);
					stm.setDouble(4, thisRow.value2);
					stm.setDouble(5, thisRow.value3);
					stm.setString(6, thisRow.name);
					stm.setString(7, thisRow.description);
					stm.setShort(8, thisRow.ID);
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
	
	private DataItem[] setItems() {
		int i = 0;
		DataItem[] items = new DataItem[mapCodes.size()];
		for (Map.Entry<Short, String> entry : mapCodes.entrySet()) {
			items[i] = new DataItem(entry.getKey(), entry.getValue());
			i++;
		}
		return items;
	}
	
	private class ClassData {
		boolean altered = false;
		boolean newRow = false;
		short ID = 0;
		DataItem rule = new DataItem(0, "");
		byte count = 0;
		double value1 = 0;
		double value2 = 0;
		double value3 = 0;
		String name = "";
		String description = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = 8045499089754301496L;
		private final String[] columns = {"NAME", "DESCR", "RULE", "COUNT",
			"VALUE1", "VALUE2", "VALUE3"};

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
					case DATA_CODE_VALUE1:
						objValue = thisRow.value1;
						break;
					case DATA_CODE_VALUE2:
						objValue = thisRow.value2;
						break;
					case DATA_CODE_VALUE3:
						objValue = thisRow.value3;
						break;
					case DATA_CODE_COUNT:
						objValue = thisRow.count;
						break;
					case DATA_CODE_RULE:
						objValue = thisRow.rule;
						break;
					case DATA_CODE_NAME:
						objValue = thisRow.name;
						break;
					default:
						objValue = thisRow.description;
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return objValue;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_CODE_VALUE1:
			case DATA_CODE_VALUE2:
			case DATA_CODE_VALUE3:
				return Double.class;
			case DATA_CODE_COUNT:
				return Byte.class;
			case DATA_CODE_RULE:
				return DataItem.class;
			default:
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				ClassData thisRow = list.get(row);
				switch (col) {
				case DATA_CODE_VALUE1:
					thisRow.value1 = (Double) value;
					break;
				case DATA_CODE_VALUE2:
					thisRow.value2 = (Double) value;
					break;
				case DATA_CODE_VALUE3:
					thisRow.value3 = (Double) value;
					break;
				case DATA_CODE_COUNT:
					thisRow.count = (Byte) value;
					break;
				case DATA_CODE_RULE:
					thisRow.rule = (DataItem) value;
					break;
				case DATA_CODE_NAME:
					thisRow.name = (String) value;
					break;
				default:
					thisRow.description = (String) value;
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
