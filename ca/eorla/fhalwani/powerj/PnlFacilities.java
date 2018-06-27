package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;

class PnlFacilities extends PnlMain {
	private static final long serialVersionUID = -3914001578914268242L;
	private final byte DATA_NAME = 0;
	private final byte DATA_CODE = 1;
	private final byte DATA_DASHBOARD = 2;
	private final byte DATA_WORKLOAD = 3;
	private ArrayList<ClassData> list = new ArrayList<ClassData>();
	
	PnlFacilities(PowerJ parent) {
		super(parent);
		setName("Facilities");
		parent.dbPowerJ.prepareFacilities();
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
		JTableEditor tblData = new JTableEditor(parent, new ModelData());
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
		ResultSet rst = parent.dbPowerJ.getFacilities();
		ClassData thisRow = new ClassData();
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.ID = rst.getShort("FACID");
				thisRow.name = rst.getString("NAME");
				thisRow.code = rst.getString("CODE");
				thisRow.dashboard = (rst.getString("DASH").equalsIgnoreCase("Y"));
				thisRow.workload = (rst.getString("WLOAD").equalsIgnoreCase("Y"));
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
		ResultSet rst = dbAP.getFacilities(lastID);
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.newRow = true;
				thisRow.altered = true;
				thisRow.ID = rst.getShort("id");
				thisRow.name = rst.getString("name").trim();
				thisRow.code = rst.getString("code").trim();
				if (thisRow.code.length() > 4) {
					thisRow.code = thisRow.code.substring(0, 4);
				}
				list.add(thisRow);
				noInserts++;
			}
			if (noInserts > 0) {
				save();
				parent.log(JOptionPane.INFORMATION_MESSAGE, getName(),
						"Found " + noInserts + " new facilities in APIS.");
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
        String dashboard = "", workload = "";
        ClassData thisRow = new ClassData();
		PreparedStatement stm = null;
		try {
			for (int i = 0; i < list.size(); i++) {
		        thisRow = list.get(i);
		        if (thisRow.altered) {
					dashboard = (thisRow.dashboard ? "Y" : "N");
					workload = (thisRow.workload ? "Y" : "N");
					thisRow.name = thisRow.name.trim();
					thisRow.code = thisRow.code.trim();
			        if (thisRow.ID <= 0) {
			        	// Generate a duplicate error (1 already exists)
			        	thisRow.ID = 1;
			        }
			        if (thisRow.name.length() > 80) {
			        	thisRow.name = thisRow.name.substring(0, 80);
			        }
			        if (thisRow.code.length() > 4) {
			        	thisRow.code = thisRow.code.substring(0, 4);
			        }
					if (thisRow.newRow) {
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
					stm.setString(1, dashboard);
					stm.setString(2, workload);
					stm.setString(3, thisRow.code);
					stm.setString(4, thisRow.name);
					stm.setInt(5, thisRow.ID);
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
		boolean dashboard = false;
		boolean workload = false;
		boolean newRow = false;
		short ID = 0;
		String code = "";
		String name = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = 3533343898478482087L;
		private final String[] columns = {"NAME", "CODE", "DASHBOARD", "WORKLOAD"};
		
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
						objValue = thisRow.code;
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
				case DATA_DASHBOARD:
					thisRow.dashboard = (Boolean) value;
					break;
				case DATA_WORKLOAD:
					thisRow.workload = (Boolean) value;
					break;
				case DATA_CODE:
					thisRow.code = (String) value;
					break;
				default:
					thisRow.name = (String) value;
				}
				thisRow.altered = true;
				altered = true;
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}
}
