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
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

class PnlAccessions extends PnlMain {
	private static final long serialVersionUID = -5535662021829058861L;
	private final byte DATA_NAME = 0;
	private final byte DATA_SPECIALTY = 1;
	private final byte DATA_DASHBOARD = 2;
	private final byte DATA_WORKLOAD = 3;
	private ArrayList<ClassData> list = new ArrayList<ClassData>();
	
	PnlAccessions(PowerJ parent) {
		super(parent);
		setName("Accessions");
		parent.dbPowerJ.prepareAccessions();
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
		CboSpecialties cboEditor = new CboSpecialties(parent, true);
		JTableEditor tblData = new JTableEditor(parent, new ModelData());
		TableColumn column = tblData.getColumnModel().getColumn(DATA_SPECIALTY);
		column.setCellEditor(new DefaultCellEditor(cboEditor));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JScrollPane scrollPane = new JScrollPane(tblData,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBorder(border);
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
		short lastID = 0;
		ResultSet rst = parent.dbPowerJ.getAccTypes();
		ClassData thisRow = new ClassData();
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.ID = rst.getShort("ACCID");
				thisRow.name = rst.getString("ACCNAME");
				thisRow.dashboard = (rst.getString("DASH").equalsIgnoreCase("Y"));
				thisRow.workload = (rst.getString("WLOAD").equalsIgnoreCase("Y"));
				thisRow.master = new DataItem(rst.getInt("SPYID"),
						rst.getString("SPYNAME").trim());
				list.add(thisRow);
				if (lastID < thisRow.ID) {
					lastID = thisRow.ID;
				}
			}
			if (!parent.variables.offLine) {
				readUpdates(lastID);
			}
			parent.statusBar.setMessage("No rows " + list.size());
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
		ClassData thisRow = new ClassData();
		ResultSet rst = dbAP.getAccTypes(lastID);
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.newRow = true;
				thisRow.altered = true;
				thisRow.ID = rst.getShort("id");
				thisRow.name = rst.getString("name");
				thisRow.master = new DataItem(1, "Ignore");
				list.add(thisRow);
				noInserts++;
			}
			if (noInserts > 0) {
				save();
				parent.log(JOptionPane.INFORMATION_MESSAGE, getName(),
						"Found " + noInserts + " new item(s) in APIS.");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(),
					e.getLocalizedMessage());
		} finally {
			dbAP.closeRst(rst);
			dbAP.closeStm();
			dbAP.close();
		}
	}
	
	void save() {
		boolean failed = false;
        int noUpdates = 0;
        String dashboard = "", workload = "";
        ClassData thisRow = new ClassData();
		PreparedStatement stm = null;
		try {
			for (int i = 0; i < list.size(); i++) {
		        thisRow = list.get(i);
		        if (thisRow.altered) {
		        	thisRow.name = thisRow.name.trim();
					if (thisRow.name.length() > 30) {
						thisRow.name = thisRow.name.substring(0, 30);
					}
					dashboard = (thisRow.dashboard ? "Y" : "N");
					workload = (thisRow.workload ? "Y" : "N");
					if (thisRow.newRow) {
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
					stm.setInt(1, thisRow.master.getValue());
					stm.setString(2, dashboard);
					stm.setString(3, workload);
					stm.setString(4, thisRow.name);
					stm.setShort(5, thisRow.ID);
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
		boolean dashboard = false;
		boolean workload = false;
		short ID = 0;
		// SpecialtyID + SpecialtyName
		DataItem master = new DataItem(0, "");
		String name = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = -8992088360660048158L;
		private final String[] columns = {"NAME", "SPECIALTY", "DASHBOARD", "WORKLOAD"};
		
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
					case DATA_DASHBOARD:
						objValue = thisRow.dashboard;
						break;
					case DATA_WORKLOAD:
						objValue = thisRow.workload;
						break;
					default:
						objValue = thisRow.master;
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return objValue;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_DASHBOARD:
				return Boolean.class;
			case DATA_WORKLOAD:
				return Boolean.class;
			case DATA_SPECIALTY:
				return DataItem.class;
			default:
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			return (col != DATA_NAME);
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				ClassData thisRow = list.get(row);
				switch (col) {
				case DATA_NAME:
					thisRow.name = (String) value;
					break;
				case DATA_DASHBOARD:
					thisRow.dashboard = (Boolean) value;
					break;
				case DATA_WORKLOAD:
					thisRow.workload = (Boolean) value;
					break;
				default:
					thisRow.master = (DataItem) value;
				}
				thisRow.altered = true;
				altered = true;
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}
}
