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

class PnlGroups extends PnlMain {
	private static final long serialVersionUID = 2273064315966859315L;
	private final byte DATA_GROUP = 0;
	private final byte DATA_TYPE = 1;
	private final byte DATA_CODER1 = 2;
	private final byte DATA_CODER2 = 3;
	private final byte DATA_CODER3 = 4;
	private final byte DATA_CODER4 = 5;
	private ModelData mdlData = new ModelData();
	private JTableEditor tblData;
	private ArrayList<ClassData> list = new ArrayList<ClassData>();

	PnlGroups(PowerJ parent) {
		super(parent);
		setName("Groups");
		parent.dbPowerJ.prepareGroups();
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
		byte coderID = 1; 
		setLayout(new BorderLayout());
		setOpaque(true);
		CboTypes cboTypes = new CboTypes(parent);
		CboCoder cboCoder1 = new CboCoder(parent, true, coderID++);
		CboCoder cboCoder2 = new CboCoder(parent, true, coderID++);
		CboCoder cboCoder3 = new CboCoder(parent, true, coderID++);
		CboCoder cboCoder4 = new CboCoder(parent, true, coderID++);
		tblData = new JTableEditor(parent, mdlData);
		TableColumn column = null;
		for (int i = DATA_TYPE; i <= DATA_CODER3; i++) {
			column = tblData.getColumnModel().getColumn(i);
			switch (i) {
			case DATA_CODER1:
				column.setCellEditor(new DefaultCellEditor(cboCoder1));
				break;
			case DATA_CODER2:
				column.setCellEditor(new DefaultCellEditor(cboCoder2));
				break;
			case DATA_CODER3:
				column.setCellEditor(new DefaultCellEditor(cboCoder3));
				break;
			case DATA_CODER4:
				column.setCellEditor(new DefaultCellEditor(cboCoder4));
				break;
			default:
				column.setCellEditor(new DefaultCellEditor(cboTypes));
			}
		}
		JScrollPane scrollPane = new JScrollPane(tblData,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		scrollPane.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 8334954865866544843L;
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
		ResultSet rst = parent.dbPowerJ.getGroups(0);
		ClassData thisRow = new ClassData();
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.groupID = rst.getShort("ID");
				thisRow.name = rst.getString("NAME");
				thisRow.type = new DataItem(rst.getByte("GRP"),
						DataOrderType.TYPES[rst.getByte("GRP")]);
				thisRow.coder1 = new DataItem(rst.getShort("CODE1"),
						rst.getString("NAME1"));
				thisRow.coder2 = new DataItem(rst.getShort("CODE2"),
						rst.getString("NAME2"));
				thisRow.coder3 = new DataItem(rst.getShort("CODE3"),
						rst.getString("NAME3"));
				thisRow.coder4 = new DataItem(rst.getShort("CODE4"),
						rst.getString("NAME4"));
				list.add(thisRow);
			}
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
					thisRow.name = thisRow.name.trim();
			        if (thisRow.name.length() > 8) {
			        	thisRow.name = thisRow.name.substring(0, 8);
			        }
					if (thisRow.newRow) {
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
					stm.setInt(1, thisRow.type.getValue());
					stm.setInt(2, thisRow.coder1.getValue());
					stm.setInt(3, thisRow.coder2.getValue());
					stm.setInt(4, thisRow.coder3.getValue());
					stm.setInt(5, thisRow.coder4.getValue());
		            stm.setString(6, thisRow.name);
		            stm.setInt(7, thisRow.groupID);
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
	
	private class ClassData {
		boolean altered = false;
		boolean newRow = false;
		short groupID = 0;
		String name = "";
		DataItem type = new DataItem(0, "");
		DataItem coder1 = new DataItem(0, "");
		DataItem coder2 = new DataItem(0, "");
		DataItem coder3 = new DataItem(0, "");
		DataItem coder4 = new DataItem(0, "");
	}
	
	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = -7204394647519992737L;
		private final String[] columns = {"GROUP", "TYPE",
				parent.variables.codersName[0],
				parent.variables.codersName[1],
				parent.variables.codersName[2],
				parent.variables.codersName[3]};
		
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
					case DATA_TYPE:
						objValue = thisRow.type;
						break;
					case DATA_CODER1:
						objValue = thisRow.coder1;
						break;
					case DATA_CODER2:
						objValue = thisRow.coder2;
						break;
					case DATA_CODER3:
						objValue = thisRow.coder3;
						break;
					case DATA_CODER4:
						objValue = thisRow.coder4;
						break;
					default:
						objValue = thisRow.name;
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return objValue;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_GROUP:
				return String.class;
			default:
				return DataItem.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				ClassData thisRow = list.get(row);
				switch (col) {
				case DATA_GROUP:
					thisRow.name = (String) value;
					break;
				case DATA_TYPE:
					thisRow.type = (DataItem) value;
					break;
				case DATA_CODER1:
					thisRow.coder1 = (DataItem) value;
					break;
				case DATA_CODER2:
					thisRow.coder2 = (DataItem) value;
					break;
				case DATA_CODER3:
					thisRow.coder3 = (DataItem) value;
					break;
				default:
					thisRow.coder4 = (DataItem) value;
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
