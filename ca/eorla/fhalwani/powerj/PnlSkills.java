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
import javax.swing.table.TableColumn;

class PnlSkills extends PnlMain {
	private static final long serialVersionUID = -2722737793251656453L;
	private final byte DATA_INITS = 0;
	private final byte DATA_NAME = 1;
	private ArrayList<ClassSkill> list = new ArrayList<ClassSkill>();

	public PnlSkills(PowerJ parent) {
		super(parent);
		setName("Skills");
		parent.dbPowerJ.prepareSkills();
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
		JTableEditor tblData = new JTableEditor(parent, new ModelSkills());
		JStringEditor editor = new JStringEditor(2, 64);
		tblData.getColumnModel().getColumn(DATA_NAME).setCellEditor(editor);
		editor = new JStringEditor(2, 3);
		TableColumn column = tblData.getColumnModel().getColumn(DATA_INITS);
		column.setCellEditor(editor);
		column.setMinWidth(75);
		column.setMaxWidth(75);
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
		ResultSet rst = parent.dbPowerJ.getSkills();
		ClassSkill thisRow = new ClassSkill();
		try {
			while (rst.next()) {
				thisRow = new ClassSkill();
				thisRow.skillID = rst.getShort("ID");
				thisRow.initials = rst.getString("INITIALS");
				thisRow.descr = rst.getString("DESCR");
				list.add(thisRow);
			}
			// Add a blank row
			thisRow = new ClassSkill();
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
		ClassSkill thisRow = new ClassSkill();
		PreparedStatement stm = null;
		try {
			for (int i = 0; i < list.size(); i++) {
				thisRow = list.get(i);
				if (newID <= thisRow.skillID) {
					newID = (byte) (thisRow.skillID +1);
				}
				if (thisRow.altered) {
					if (thisRow.newRow) {
						thisRow.skillID = newID;
					}
					if (thisRow.newRow) {
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
		            stm.setString(1, thisRow.initials);
		            stm.setString(2, thisRow.descr);
		            stm.setShort(3, thisRow.skillID);
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

	private class ClassSkill {
		boolean altered = false;
		boolean newRow = false;
		short skillID = 0;
		String initials = "";
		String descr = "";
	}

	private class ModelSkills extends AbstractTableModel {
		private static final long serialVersionUID = 3164804118232893352L;
		private final String[] columns = {"SHORT", "DESCR"};

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
					ClassSkill thisRow = list.get(row);
					switch (col) {
					case DATA_INITS:
						objValue = thisRow.initials;
						break;
					case DATA_NAME:
						objValue = thisRow.descr;
						break;
					default:
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
				ClassSkill thisRow = list.get(row);
				switch (col) {
				case DATA_INITS:
					thisRow.initials = (String) value;
					thisRow.initials = thisRow.initials.trim().toUpperCase();
					if (thisRow.initials.length() > 3) {
						thisRow.initials = thisRow.initials.substring(0, 3);
					}
					break;
				case DATA_NAME:
					thisRow.descr = (String) value;
					thisRow.descr = thisRow.descr.trim();
					if (thisRow.descr.length() > 64) {
						thisRow.descr = thisRow.descr.substring(0, 64);
					}
					break;
				default:
				}
				thisRow.altered = true;
				altered = true;
				if (row == list.size()) {
					// Add a blank row
					thisRow = new ClassSkill();
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
