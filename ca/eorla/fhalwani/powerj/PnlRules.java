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

class PnlRules extends PnlMain {
	private static final long serialVersionUID = 9005498724117271781L;
	private final byte DATA_NAME = 0;
	// private final byte DATA_DESCR = 2;
	private ClassData thisRow = new ClassData();
	private ArrayList<ClassData> list = new ArrayList<ClassData>();
	private JTableEditor tblData;

	PnlRules(PowerJ parent) {
		super(parent);
		setName("Rules");
		parent.dbPowerJ.prepareRules();
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
			parent.dbPowerJ.closeStms();
			list.clear();
			super.close();
		}
		return !altered;
	}

	private void createPanel() {
		setName(getName());
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
		add(scrollPane, BorderLayout.CENTER);
		parent.statusBar.setMessage("No rows " + list.size());
	}

	private void readTable() {
		ResultSet rst = parent.dbPowerJ.getRules();
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.ID = rst.getShort("ID");
				thisRow.name = rst.getString("NAME");
				thisRow.description = rst.getString("DESCR");
				list.add(thisRow);
			}
			parent.statusBar.setMessage("No rows " + list.size());
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
					thisRow.description = thisRow.description.trim();
					if (thisRow.description.length() > 256) {
						thisRow.description = thisRow.description.substring(0, 256);
					}
					thisRow.name = thisRow.name.trim();
					if (thisRow.name.length() > 32) {
						thisRow.name = thisRow.name.substring(0, 32);
					}
					stm.setString(1, thisRow.name);
					stm.setString(2, thisRow.description);
					stm.setShort(3, thisRow.ID);
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
		String name = "";
		String description = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = 8045499089754301496L;
		private final String[] columns = {"NAME", "DESCR"};

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
					case DATA_NAME:
						value = thisRow.name;
						break;
					default:
						value = thisRow.description;
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
			return true;
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				thisRow = list.get(row);
				switch (col) {
				case DATA_NAME:
					thisRow.name = (String) value;
					break;
				default:
					thisRow.description = (String) value;
				}
				thisRow.altered = true;
				altered = true;
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}
}
