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
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
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
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

class PnlOrders extends PnlMain {
	private static final long serialVersionUID = 6503134239979660832L;
	private final byte DATA_NAME = 0;
	private final byte DATA_DESCR = 1;
	private final byte DATA_GROUP = 2;
	private final byte DATA_TYPE = 3;
	private final byte DATA_CODER1 = 4;
	private final byte DATA_CODER2 = 5;
	private final byte DATA_CODER3 = 6;
	private final byte DATA_CODER4 = 7;
	private int groupID = 0;
	private ModelData mdlData = new ModelData();
	private JTableEditor tblData;
	private HashMap<Integer, ClassGroup> groups = new HashMap<Integer, ClassGroup>();
	private ArrayList<ClassData> list = new ArrayList<ClassData>();

	PnlOrders(PowerJ parent) {
		super(parent);
		setName("Orders");
		parent.dbPowerJ.prepareOrders();
		readGroups();
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
		CboGroups cboGroups = new CboGroups(parent, true);
		tblData = new JTableEditor(parent, mdlData);
		TableColumn column = tblData.getColumnModel().getColumn(DATA_GROUP);
		column.setCellEditor(new DefaultCellEditor(cboGroups));
		for (int i = DATA_CODER4; i >= DATA_CODER1; i--) {
			column = tblData.getColumnModel().getColumn(i);
			if (!parent.variables.codersActive[i - DATA_CODER1]) {
				tblData.removeColumn(column);
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
		add(createToolbar(), BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);
		parent.statusBar.setMessage("No rows " + list.size());
	}

	private JPanel createToolbar() {
		CboGroups cboGroups = new CboGroups(parent, false);
		cboGroups.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			groupID = item.getValue();
	    			readFilters();
	    		}
	        }
	    });
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Group:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_G);
		label.setLabelFor(cboGroups);
		panel.add(label);
		panel.add(cboGroups);
		return panel;
	}
	
	private void readFilters() {
		int noRows = list.size();
		RowFilter<AbstractTableModel, Integer> rowFilter = null;
		if (groupID > 0) {
			rowFilter = new RowFilter<AbstractTableModel, Integer>() {
				public boolean include(Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					int row = entry.getIdentifier();
					return (list.get(row).group.getValue() == groupID);
				}
			};
			// Count filtered rows
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).group.getValue() != groupID) {
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
	
	private void readGroups() {
		ResultSet rst = parent.dbPowerJ.getGroups(0);
		ClassGroup thisGroup = new ClassGroup();
		try {
			while (rst.next()) {
				thisGroup = new ClassGroup();
				thisGroup.type = DataOrderType.TYPES[rst.getByte("GRP")];
				thisGroup.coder1 = rst.getString("NAME1");
				thisGroup.coder2 = rst.getString("NAME2");
				thisGroup.coder3 = rst.getString("NAME3");
				thisGroup.coder4 = rst.getString("NAME4");
				groups.put(rst.getInt("ID"), thisGroup);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	private void readTable() {
		short lastID = 0;
		ResultSet rst = parent.dbPowerJ.getMasterOrders();
		ClassData thisRow = new ClassData();
		try {
			while (rst.next()) {
				lastID = rst.getShort("ID");
				thisRow = new ClassData();
				thisRow.masterID = lastID;
				thisRow.typeID = rst.getByte("GRP");
				thisRow.name = rst.getString("CODE");
				thisRow.descr = rst.getString("DESCR");
				thisRow.coder1 = rst.getString("NAME1");
				thisRow.coder2 = rst.getString("NAME2");
				thisRow.coder3 = rst.getString("NAME3");
				thisRow.coder4 = rst.getString("NAME4");
				thisRow.typeName = DataOrderType.TYPES[thisRow.typeID];
				thisRow.group = new DataItem(rst.getShort("GRPID"),
						rst.getString("NAMEG"));
				list.add(thisRow);
			}
			if (!parent.variables.offLine) {
				readUpdates(lastID);
			}
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
		ResultSet rst = dbAP.getMasterOrders(lastID);
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.newRow = true;
				thisRow.altered = true;
				thisRow.masterID = rst.getShort("id");
				thisRow.name = rst.getString("code").trim();
				thisRow.descr = rst.getString("description").trim();
				thisRow.coder1 = DataOrderType.TYPES[0];
				thisRow.coder2 = DataOrderType.TYPES[0];
				thisRow.coder3 = DataOrderType.TYPES[0];
				thisRow.coder4 = DataOrderType.TYPES[0];
				thisRow.typeName = DataOrderType.TYPES[0];
				thisRow.group = new DataItem(0, "");
				list.add(thisRow);
				noInserts++;
			}
			if (noInserts > 0) {
				save();
				parent.log(JOptionPane.INFORMATION_MESSAGE, getName(),
						"Found " + noInserts + " new orders in APIS.");
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
        ClassData thisRow = new ClassData();
		PreparedStatement stm = null;
		try {
			for (int i = 0; i < list.size(); i++) {
		        thisRow = list.get(i);
		        if (thisRow.altered) {
					thisRow.name = thisRow.name.trim();
					thisRow.descr = thisRow.descr.trim();
			        if (thisRow.name.length() > 15) {
			        	thisRow.name = thisRow.name.substring(0, 15);
			        }
			        if (thisRow.descr.length() > 80) {
			        	thisRow.descr = thisRow.descr.substring(0, 80);
			        }
					if (thisRow.newRow) {
						stm = parent.dbPowerJ.getStatement(0);
					} else {
						stm = parent.dbPowerJ.getStatement(1);
					}
					stm.setInt(1, thisRow.group.getValue());
		            stm.setString(2, thisRow.name);
		            stm.setString(3, thisRow.descr);
		            stm.setInt(4, thisRow.masterID);
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
		byte typeID = 0;
		short masterID = 0;
		String name = "";
		String descr = "";
		String typeName = "";
		String coder1 = "";
		String coder2 = "";
		String coder3 = "";
		String coder4 = "";
		// GroupID + GroupName
		DataItem group = new DataItem(0, "");
	}
	
	private class ClassGroup {
		String type = "";
		String coder1 = "";
		String coder2 = "";
		String coder3 = "";
		String coder4 = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = -7109959335903712947L;
		private final String[] columns = {"NAME", "DESCR", "GROUP", "TYPE",
				parent.variables.codersName[0], parent.variables.codersName[1],
				parent.variables.codersName[2], parent.variables.codersName[3]};
		
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
					case DATA_DESCR:
						objValue = thisRow.descr;
						break;
					case DATA_TYPE:
						objValue = thisRow.typeName;
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
						objValue = thisRow.group;
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
				return DataItem.class;
			default:
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			switch (col) {
			case DATA_NAME:
			case DATA_DESCR:
			case DATA_GROUP:
				return true;
			default:
				return false;
			}
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				ClassData thisRow = list.get(row);
				switch (col) {
				case DATA_NAME:
					thisRow.name = (String) value;
					break;
				case DATA_DESCR:
					thisRow.descr = (String) value;
					break;
				default:
					thisRow.group = (DataItem) value;
					ClassGroup thisGroup = groups.get(thisRow.group.getValue());
					if (thisGroup != null) {
						thisRow.typeName = thisGroup.type;
						thisRow.coder1 = thisGroup.coder1;
						thisRow.coder2 = thisGroup.coder2;
						thisRow.coder3 = thisGroup.coder3;
						thisRow.coder4 = thisGroup.coder4;
						mdlData.fireTableRowsUpdated(row, row);
					}
				}
				thisRow.altered = true;
				altered = true;
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}
}
