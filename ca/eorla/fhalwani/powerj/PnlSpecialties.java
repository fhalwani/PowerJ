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

class PnlSpecialties extends PnlMain {
	private static final long serialVersionUID = -483112467247611771L;
	private final byte DATA_NAME = 0;
	private final byte DATA_DASHBOARD = 1;
	private final byte DATA_WORKLOAD = 2;
	private final byte DATA_SPECIMEN = 3;
	private ArrayList<ClassData> list = new ArrayList<ClassData>();

	PnlSpecialties(PowerJ parent) {
		super(parent);
		setName("Specialties");
		parent.dbPowerJ.prepareSpecialties();
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
		TableColumn column = tblData.getColumnModel().getColumn(DATA_NAME);
		JStringField txtEditor = new JStringField(1, 16);
		column.setCellEditor(new DefaultCellEditor(txtEditor));
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

	private void readTable() {
		ResultSet rst = parent.dbPowerJ.getSpecialties(1);
		ClassData thisRow = new ClassData ();
		try {
			while (rst.next()) {
				thisRow = new ClassData ();
				thisRow.ID = rst.getByte("SPYID");
				thisRow.dashboard = (rst.getString("DASH").equalsIgnoreCase("Y"));
				thisRow.workload = (rst.getString("WLOAD").equalsIgnoreCase("Y"));
				thisRow.codeSpec = (rst.getString("CODESPEC").equalsIgnoreCase("Y"));
				thisRow.name = rst.getString("SPYNAME");
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
		byte newID = 0;
        int noUpdates = 0;
        String dashboard = "", workload = "", codeSpec = "";
		ClassData thisRow = new ClassData();
		PreparedStatement stm = null;
		try {
			for (int i = 0; i < list.size(); i++) {
				thisRow = list.get(i);
				if (newID <= thisRow.ID) {
					newID = (byte) (thisRow.ID +1);
				}
				if (thisRow.altered) {
					dashboard = (thisRow.dashboard ? "Y" : "N");
					workload = (thisRow.workload ? "Y" : "N");
					codeSpec = (thisRow.codeSpec ? "Y" : "N");
					thisRow.name = thisRow.name.trim();
					if (thisRow.name.length() > 16) {
						thisRow.name = thisRow.name.substring(0, 16);
					}
					if (thisRow.newRow) {
						thisRow.ID = newID;
					}
					if (thisRow.newRow) {
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
		            stm.setString(1, dashboard);
		            stm.setString(2, workload);
		            stm.setString(3, codeSpec);
		            stm.setString(4, thisRow.name);
		            stm.setByte(5, thisRow.ID);
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
		boolean dashboard = false;
		boolean workload = false;
		boolean codeSpec = false;
		byte ID = 0;
		String name = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = 2383465019273296085L;
		private final String[] columns = {"NAME", "DASHBOARD", "WORKLOAD", "SPECIMEN"};

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
					case DATA_DASHBOARD:
						objValue = thisRow.dashboard;
						break;
					case DATA_WORKLOAD:
						objValue = thisRow.workload;
						break;
					case DATA_SPECIMEN:
						objValue = thisRow.codeSpec;
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
			case DATA_NAME:
				return String.class;
			default:
				return Boolean.class;
			}
		}
	
		public boolean isCellEditable(int row, int col) {
			return true;
		}
	
		public void setValueAt(Object value, int row, int col) {
			try {
				ClassData thisRow = list.get(row);
				switch (col) {
				case DATA_NAME:
					thisRow.name = (String) value;
					break;
				case DATA_SPECIMEN:
					thisRow.codeSpec = (Boolean) value;
					break;
				case DATA_WORKLOAD:
					thisRow.workload = (Boolean) value;
					break;
				default:
					thisRow.dashboard = (Boolean) value;
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
