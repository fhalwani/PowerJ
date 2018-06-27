package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

class PnlPathologists extends PnlMain implements KeyListener {
	private static final long serialVersionUID = 3984436325392435249L;
	private final byte DATA_NAME = 0;
	private final byte DATA_CASES = 1;
	private final byte DATA_SLIDES = 2;
	private final byte DATA_VALUE1 = 3;
	private final byte DATA_VALUE2 = 4;
	private final byte DATA_VALUE3 = 5;
	private final byte DATA_VALUE4 = 6;
	private final byte DATA_TAT = 7;
	private String infoUpdate = "No rows ";
	private JTreeTable treeTable;
	private JTableColumnModel columnModel = new JTableColumnModel();
	private ModelPath treeTableModel;
	private TreeTableNode nodeRoot = new TreeTableNode("Total");
	private TreePath treePath;

	PnlPathologists(PowerJ parent) {
		super(parent);
		setName("Pathologists");
		readTable();
		createPanel();
	}

	boolean close() {
		super.close();
		return true;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		treeTableModel = new ModelPath(nodeRoot);
		treeTable = new JTreeTable(parent, treeTableModel);
		treeTable.setColumnModel(columnModel);
		treeTable.createDefaultColumnsFromModel();
		treeTable.setFocusable(true);
		treeTable.addKeyListener(this);
		treeTable.tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath newPath = e.getNewLeadSelectionPath();
				if (newPath != null) {
					if (treePath == null || !treePath.equals(newPath)) {
						treePath = newPath;
					}
				}
			}
		});
		JScrollPane scrollPane = new JScrollPane(treeTable,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		scrollPane.setBorder(border);
		scrollPane.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 771027087739168338L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		add(scrollPane, BorderLayout.CENTER);
	}

	public void keyPressed(KeyEvent e) {
		if (treePath == null) return;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ADD: // 107
		case KeyEvent.VK_PLUS: // 521
			if (e.isControlDown()) {
				treeTable.expandAll();
			} else if (e.isAltDown()) {
				treeTable.expandAllUnder(treePath);
			} else {
				treeTable.expandPath(treePath);
			}
			break;
		case KeyEvent.VK_SUBTRACT: // 109
		case KeyEvent.VK_MINUS: // 45
			if (e.isControlDown()) {
				treeTable.collapseAll();
			} else if (e.isAltDown()) {
				treeTable.collapseAllUnder(treePath);
			} else {
				treeTable.collapsePath(treePath);
			}
			break;
		default:
			// Ignore rest
		}
	}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	private void readTable() {
		boolean exists = false;
		long timeFrom = 0;
		long timeTo = 0;
		Calendar calMax = Calendar.getInstance();
		Calendar calMin = Calendar.getInstance();
		java.sql.Date start = new java.sql.Date(0);
		java.sql.Date end = new java.sql.Date(0);
		ArrayList<DataRow> dataRows = new ArrayList<DataRow>();
		PreparedStatement stm = null;
		ResultSet rst = null;
		DataRow dataRow = new DataRow();
		try {
			calMax.set(Calendar.HOUR_OF_DAY, 0);
			calMax.set(Calendar.MINUTE, 0);
			calMax.set(Calendar.SECOND, 0);
			calMax.set(Calendar.MILLISECOND, 0);
			timeTo = calMax.getTimeInMillis();
			calMin.add(Calendar.YEAR, -1);
			calMin.set(Calendar.HOUR_OF_DAY, 0);
			calMin.set(Calendar.MINUTE, 0);
			calMin.set(Calendar.SECOND, 0);
			calMin.set(Calendar.MILLISECOND, 0);
			timeFrom = calMin.getTimeInMillis();
			if (timeFrom < parent.variables.minWorkloadDate) {
				timeFrom = parent.variables.minWorkloadDate;
			}
			start = new java.sql.Date(timeFrom);
			end = new java.sql.Date(timeTo);
			parent.dbPowerJ.prepareSummary();
			stm = parent.dbPowerJ.getStatement(0);
			stm.setDate(1, start);
			stm.setDate(2, end);
			rst = stm.executeQuery();
			while (rst.next()) {
				dataRow = new DataRow();
				dataRow.spyID = rst.getShort("SPYID");
				dataRow.subID = rst.getShort("SUBID");
				dataRow.perID = rst.getShort("FINALID");
				dataRow.noCases = rst.getInt("NOCASES");
				dataRow.noSlides = rst.getInt("NOSLIDES");
				dataRow.value1 = rst.getDouble("VALUE1");
				dataRow.value2 = rst.getDouble("VALUE2");
				dataRow.value3 = rst.getDouble("VALUE3");
				dataRow.value4 = rst.getDouble("VALUE4");
				dataRow.spyName = rst.getString("SPYNAME").trim();
				dataRow.subName = rst.getString("SUBINIT").trim();
				dataRow.perName = rst.getString("INITIALS").trim();
				dataRows.add(dataRow);
			}
			rst.close();
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			stm = parent.dbPowerJ.getStatement(1);
			stm.setDate(1, start);
			stm.setDate(2, end);
			rst = stm.executeQuery();
			while (rst.next()) {
				exists = false;
				for (int i = 0; i < dataRows.size(); i++) {
					dataRow = dataRows.get(i);
					if (dataRow.spyID == rst.getShort("SPYID")
							&& dataRow.subID == rst.getShort("SUBID")
							&& dataRow.perID == rst.getShort("PERID")) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					dataRow = new DataRow();
					dataRow.spyID = rst.getShort("SPYID");
					dataRow.subID = rst.getShort("SUBID");
					dataRow.perID = rst.getShort("PERID");
					dataRow.spyName = rst.getString("SPYNAME").trim();
					dataRow.subName = rst.getString("SUBINIT").trim();
					dataRow.perName = rst.getString("INITIALS").trim();
					dataRows.add(dataRow);
				}
				dataRow.noCases += rst.getInt("NOCASES");
				dataRow.noSlides += rst.getInt("NOSLIDES");
				dataRow.value1 += rst.getDouble("VALUE1");
				dataRow.value2 += rst.getDouble("VALUE2");
				dataRow.value3 += rst.getDouble("VALUE3");
				dataRow.value4 += rst.getDouble("VALUE4");
			}
			rst.close();
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			stm = parent.dbPowerJ.getStatement(2);
			stm.setDate(1, start);
			stm.setDate(2, end);
			rst = stm.executeQuery();
			while (rst.next()) {
				exists = false;
				for (int i = 0; i < dataRows.size(); i++) {
					dataRow = dataRows.get(i);
					if (dataRow.spyID == rst.getShort("SPYID")
							&& dataRow.subID == rst.getShort("SUBID")
							&& dataRow.perID == rst.getShort("PERID")) {
						exists = true;
						break;
					}
				}
				if (!exists) {
					dataRow = new DataRow();
					dataRow.spyID = rst.getShort("SPYID");
					dataRow.subID = rst.getShort("SUBID");
					dataRow.perID = rst.getShort("PERID");
					dataRow.spyName = rst.getString("SPYNAME").trim();
					dataRow.subName = rst.getString("SUBINIT").trim();
					dataRow.perName = rst.getString("INITIALS").trim();
					dataRows.add(dataRow);
				}
				dataRow.value1 += rst.getDouble("VALUE1");
				dataRow.value2 += rst.getDouble("VALUE2");
				dataRow.value3 += rst.getDouble("VALUE3");
				dataRow.value4 += rst.getDouble("VALUE4");
			}
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
			rst = parent.dbPowerJ.getTATSum(timeFrom, timeTo);
			while (rst.next()) {
				for (int i = 0; i < dataRows.size(); i++) {
					dataRow = dataRows.get(i);
					if (dataRow.spyID == rst.getShort("SPYID")
							&& dataRow.subID == rst.getShort("SUBID")
							&& dataRow.perID == rst.getShort("FINALID")) {
						dataRow.tat += rst.getInt("FINAL");
						break;
					}
				}
			}
			structureData(timeFrom, timeTo, dataRows);
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			infoUpdate = "No rows " + dataRows.size();
			parent.statusBar.setMessage(infoUpdate);
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStms();
			parent.dbPowerJ.closeStm();
		}
	}

	private void structureData(long timeFrom, long timeTo, ArrayList<DataRow> dataRows) {
		short id = 0;
		short ids[] = new short[3];
		int rowNos[] = new int[3];
		int noRows = dataRows.size();
		String name = "";
		DataArray dataRoot = new DataArray();
		DataArray data1 = new DataArray();
		DataArray data2 = new DataArray();
		DataArray data3 = new DataArray();
		DataRow dataRow = new DataRow();
		for (int i = 0; i < ids.length; i++) {
			// 0 = FSE
			ids[i] = -1;
		}
		for (int x = 0; x < noRows; x++) {
			dataRow = dataRows.get(x);
			// Match 1st node
			if (dataRow.perID == parent.variables.userID) {
				name = dataRow.perName;
			} else {
				name = "";
			}
			if (ids[0] != dataRow.perID) {
				ids[0] = dataRow.perID;
				ids[1] = -1;
				ids[2] = -1;
				rowNos[0] = -1;
				rowNos[1] = -1;
				rowNos[2] = -1;
				for (int i = 0; i < dataRoot.children.size(); i++) {
					data1 = dataRoot.children.get(i);
					if (data1.id == ids[0]) {
						rowNos[0] = i;
						break;
					}
				}
				if (rowNos[0] < 0) {
					rowNos[0] = dataRoot.children.size();
					data1 = new DataArray();
					data1.id = ids[0];
					data1.name = name;
					dataRoot.children.add(data1);
				}
			}
			if (dataRow.perID == parent.variables.userID) {
				// Match specialty
				id = dataRow.spyID;
				name = dataRow.spyName;
				if (ids[1] != id) {
					ids[1] = id;
					ids[2] = -1;
					rowNos[1] = -1;
					rowNos[2] = -1;
					for (int i = 0; i < data1.children.size(); i++) {
						data2 = data1.children.get(i);
						if (data2.id == ids[1]) {
							rowNos[1] = i;
							break;
						}
					}
					if (rowNos[1] < 0) {
						rowNos[1] = data1.children.size();
						data2 = new DataArray();
						data2.id = ids[1];
						data2.name = name;
						data1.children.add(data2);
					}
				}
				// Match subspecialty
				id = dataRow.subID;
				name = dataRow.subName;
				if (ids[2] != id) {
					ids[2] = id;
					rowNos[2] = -1;
					for (int i = 0; i < data2.children.size(); i++) {
						data3 = data2.children.get(i);
						if (data3.id == ids[2]) {
							rowNos[2] = i;
							break;
						}
					}
					if (rowNos[2] < 0) {
						rowNos[2] = data2.children.size();
						data3 = new DataArray();
						data3.id = ids[2];
						data3.name = name;
						data2.children.add(data3);
					}
				}
				data3.noCases += dataRow.noCases;
				data3.noSlides += dataRow.noSlides;
				data3.tat += dataRow.tat;
				data3.fte1 += dataRow.value1;
				data3.fte2 += dataRow.value2;
				data3.fte3 += dataRow.value3;
				data3.fte4 += dataRow.value4;
				data2.noCases += dataRow.noCases;
				data2.noSlides += dataRow.noSlides;
				data2.tat += dataRow.tat;
				data2.fte1 += dataRow.value1;
				data2.fte2 += dataRow.value2;
				data2.fte3 += dataRow.value3;
				data2.fte4 += dataRow.value4;
			}
			data1.noCases += dataRow.noCases;
			data1.noSlides += dataRow.noSlides;
			data1.tat += dataRow.tat;
			data1.fte1 += dataRow.value1;
			data1.fte2 += dataRow.value2;
			data1.fte3 += dataRow.value3;
			data1.fte4 += dataRow.value4;
			dataRoot.noCases += dataRow.noCases;
			dataRoot.noSlides += dataRow.noSlides;
			dataRoot.tat += dataRow.tat;
			dataRoot.fte1 += dataRow.value1;
			dataRoot.fte2 += dataRow.value2;
			dataRoot.fte3 += dataRow.value3;
			dataRoot.fte4 += dataRow.value4;
		}
		dataRows.clear();
		try {
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException e) {
		}
		// Calculate FTE's equivalent for # of worked days
		int noDays = parent.dateUtils.getNoDays(timeFrom, timeTo);
		double fte1 = 0, fte2 = 0, fte3 = 0, fte4 = 0;
		fte1 = 1.0 * noDays * parent.variables.codersFTE[0] / 365;
		fte2 = 1.0 * noDays * parent.variables.codersFTE[1] / 365;
		fte3 = 1.0 * noDays * parent.variables.codersFTE[2] / 365;
		fte4 = 1.0 * noDays * parent.variables.codersFTE[3] / 365;
		if (fte1 == 0) {
			fte1 = 1.0;
		}
		if (fte2 == 0) {
			fte2 = 1.0;
		}
		if (fte3 == 0) {
			fte3 = 1.0;
		}
		if (fte4 == 0) {
			fte4 = 1.0;
		}
		dataRoot.fte1 = dataRoot.fte1 / fte1;
		dataRoot.fte2 = dataRoot.fte2 / fte2;
		dataRoot.fte3 = dataRoot.fte3 / fte3;
		dataRoot.fte4 = dataRoot.fte4 / fte4;
		if (dataRoot.noCases > 0) {
			dataRoot.tat = dataRoot.tat / dataRoot.noCases;
		}
		setTotals(dataRoot, fte1, fte2, fte3, fte4);
		sortChildren(dataRoot);
		try {
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException e) {
		}
		setModel(dataRoot);
	}

	private void setModel(DataArray dataRoot) {
		String name = "";
		TreeTableNode node1 = new TreeTableNode("node");
		TreeTableNode node2 = new TreeTableNode("node");
		TreeTableNode node3 = new TreeTableNode("node");
		DataArray data1 = new DataArray();
		DataArray data2 = new DataArray();
		DataArray data3 = new DataArray();
		nodeRoot.id = dataRoot.id;
		nodeRoot.noCases = dataRoot.noCases;
		nodeRoot.noSlides = dataRoot.noSlides;
		nodeRoot.tat = dataRoot.tat;
		nodeRoot.fte1 = dataRoot.fte1;
		nodeRoot.fte2 = dataRoot.fte2;
		nodeRoot.fte3 = dataRoot.fte3;
		nodeRoot.fte4 = dataRoot.fte4;
		nodeRoot.children = new TreeTableNode[dataRoot.children.size()];
		for (int i = 0; i < dataRoot.children.size(); i++) {
			data1 = dataRoot.children.get(i);
			name = data1.name.length() == 0 ? "P"+(i+1) : data1.name;
			node1 = new TreeTableNode(name);
			node1.id = data1.id;
			node1.noCases = data1.noCases;
			node1.noSlides = data1.noSlides;
			node1.tat = data1.tat;
			node1.fte1 = data1.fte1;
			node1.fte2 = data1.fte2;
			node1.fte3 = data1.fte3;
			node1.fte4 = data1.fte4;
			node1.children = new TreeTableNode[data1.children.size()];
			for (int j = 0; j < data1.children.size(); j++) {
				data2 = data1.children.get(j);
				node2 = new TreeTableNode(data2.name);
				node2.id = data2.id;
				node2.noCases = data2.noCases;
				node2.noSlides = data2.noSlides;
				node2.tat = data2.tat;
				node2.fte1 = data2.fte1;
				node2.fte2 = data2.fte2;
				node2.fte3 = data2.fte3;
				node2.fte4 = data2.fte4;
				node2.children = new TreeTableNode[data2.children.size()];
				for (int k = 0; k < data2.children.size(); k++) {
					data3 = data2.children.get(k);
					node3 = new TreeTableNode(data3.name);
					node3.id = data3.id;
					node3.noCases = data3.noCases;
					node3.noSlides = data3.noSlides;
					node3.tat = data3.tat;
					node3.fte1 = data3.fte1;
					node3.fte2 = data3.fte2;
					node3.fte3 = data3.fte3;
					node3.fte4 = data3.fte4;
					node3.children = new TreeTableNode[data3.children.size()];
					node2.children[k] = node3;
				}
				data2.children.clear();
				node1.children[j] = node2;
			}
			data1.children.clear();
			nodeRoot.children[i] = node1;
		}
		dataRoot.children.clear();
		try {
			Thread.sleep(Constants.SLEEP_TIME);
		} catch (InterruptedException e) {
		}
	}

	private void setTotals(DataArray master, double fte1, double fte2,
			double fte3, double fte4) {
		DataArray child = new DataArray();
		for (int i = master.children.size() -1; i >= 0; i--) {
			child = master.children.get(i);
			if (child.noCases > 0) {
				child.tat = child.tat / child.noCases;
			}
			child.fte1 = child.fte1 / fte1;
			child.fte2 = child.fte2 / fte2;
			child.fte3 = child.fte3 / fte3;
			child.fte4 = child.fte4 / fte4;
			if (child.children.size() > 0) {
				setTotals(child, fte1, fte2, fte3, fte4);
			}
		}
	}

	private void sortChildren(DataArray master) {
		Collections.sort(master.children, new Comparator<DataArray>() {
			public int compare(DataArray o1, DataArray o2) {
				return o1.noSlides - o2.noSlides;
			}
		});
		DataArray child = new DataArray();
		for (int i = 0; i < master.children.size(); i++) {
			child = master.children.get(i);
			if (child.children.size() > 0) {
				sortChildren(child);
			}
		}
	}

	class ModelPath extends AbstractTreeTableModel implements TreeTableModel {
		private final String[] cNames = {"Name", "Cases", "Slides",
				parent.variables.codersName[0],
				parent.variables.codersName[1],
				parent.variables.codersName[2],
				parent.variables.codersName[3], "TAT"};
		private final Class<?>[]  cTypes = {TreeTableModel.class, Integer.class,
				Integer.class, Double.class, Double.class, Double.class,
				Double.class, Integer.class};

		public ModelPath(Object nodeRoot) {
			super(nodeRoot);
		}

		public Object getChild(Object node, int element) {
			return ((TreeTableNode)node).children[element];
		}

		public int getChildCount(Object node) {
			Object[] children = getChildren(node); 
			return (children == null) ? 0 : children.length;
		}

		protected Object[] getChildren(Object node) {
			return ((TreeTableNode)node).children;
		}

		public int getColumnCount() {
			return cNames.length;
		}

		public String getColumnName(int column) {
			return cNames[column];
		}

		public Class<?> getColumnClass(int column) {
			return cTypes[column];
		}

		public Object getValueAt(Object node, int column) {
			TreeTableNode data = (TreeTableNode) node;
			switch(column) {
			case DATA_NAME:
				return data.title;
			case DATA_CASES:
				return data.noCases;
			case DATA_SLIDES:
				return data.noSlides;
			case DATA_VALUE1:
				return data.fte1;
			case DATA_VALUE2:
				return data.fte2;
			case DATA_VALUE3:
				return data.fte3;
			case DATA_VALUE4:
				return data.fte4;
			case DATA_TAT:
				return data.tat;
			}
			return null;
		}
	}
	class DataArray {
		short id = 0;
		int noCases = 0;
		int noSlides = 0;
		int tat = 0;
		double fte1 = 0;
		double fte2 = 0;
		double fte3 = 0;
		double fte4 = 0;
		String name = "";
		ArrayList<DataArray> children = new ArrayList<DataArray>();
	}

	class DataRow {
		short spyID = 0;
		short subID = 0;
		short perID = 0;
		int noCases = 0;
		int noSlides = 0;
		int tat = 0;
		double value1 = 0;
		double value2 = 0;
		double value3 = 0;
		double value4 = 0;
		String spyName = "";
		String subName = "";
		String perName = "";
	}

	class TreeTableNode {
		short id = 0;
		int noCases = 0;
		int noSlides = 0;
		int tat = 0;
		double fte1 = 0;
		double fte2 = 0;
		double fte3 = 0;
		double fte4 = 0;
		String title = "";
		Object[] children; 

		TreeTableNode(String name) {
			this.title = name;
		}

		public String toString() {
			return this.title;
		}
	}
}
