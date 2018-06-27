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
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

class PnlSpecimens extends PnlMain {
	private static final long serialVersionUID = -296603149158353106L;
	private final byte DATA_CASE_NO = 0;
	private final byte DATA_SPEC_CODE = 1;
	private final byte DATA_SPEC_TEMPLATE = 2;
	private final byte DATA_SPEC_DESCR = 3;
	private boolean canEdit = false;
	private int intRowNo = 0;
	private int[] filters = {0, 0, 0, 0};
	private JTableEditor tblData;
	private ModelData mdlData;
	private DbAPIS dbAP;
	private ArrayList<ClassData> list = new ArrayList<ClassData>();

	PnlSpecimens(PowerJ parent) {
		super(parent);
		setName("Specimens");
		readTable();
		if (!parent.variables.offLine) {
			dbAP = new DbAPIS(parent);
			if (dbAP.connected) {
				dbAP.prepareSpecimens();
				if (!parent.variables.hasError) {
					parent.dbPowerJ.prepareSpecimens();
					if (!parent.variables.hasError) {
						canEdit = true;
					}
				}
			}
		}
		createPanel();
	}

	boolean close() {
		list.clear();
		if (!parent.variables.offLine) {
			if (dbAP.connected) {
				dbAP.closeStms();
				dbAP.close();
			}
		}
		super.close();
		return true;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		add(createToolbar(), BorderLayout.NORTH);
		CboMaster cboEditor = new CboMaster(parent, true);
		mdlData = new ModelData();
		tblData = new JTableEditor(parent, mdlData);
		TableColumn column = tblData.getColumnModel().getColumn(DATA_SPEC_CODE);
		// Define the editor of the specimens templates
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

	private JPanel createToolbar() {
		// Setup 4 JComboBox and fill with their data
		CboFacilities cboFacilities = new CboFacilities(parent);
		cboFacilities.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[0] = item.getValue();
	    			readData();
	    		}
	        }
	    });
		CboSpecialties cboSpecialties = new CboSpecialties(parent, false);
		cboSpecialties.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[1] = item.getValue();
	    			readData();
	    		}
	        }
	    });
		CboSubspecial cboSubspecial = new CboSubspecial(parent, false);
		cboSubspecial.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[2] = item.getValue();
	    			readData();
	    		}
	        }
	    });
		CboMaster cboMaster = new CboMaster(parent, false);
		cboMaster.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[3] = item.getValue();
	    			readData();
	    		}
	        }
	    });
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Facility:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_F);
		label.setLabelFor(cboFacilities);
		panel.add(label);
		panel.add(cboFacilities);
		label = new JLabel("Specialty:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_S);
		label.setLabelFor(cboSpecialties);
		panel.add(label);
		panel.add(cboSpecialties);
		label = new JLabel("Subspecialty:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_B);
		label.setLabelFor(cboSubspecial);
		panel.add(label);
		panel.add(cboSubspecial);
		label = new JLabel("Specimen:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		label.setDisplayedMnemonicIndex(1);
		label.setLabelFor(cboMaster);
		panel.add(label);
		panel.add(cboMaster);
		return panel;
	}

	private void readData() {
		if (parent.variables.busy.get()) return;
		list.clear();
		// Must initialize a new instance each time
		DataWorker worker = new DataWorker();
		worker.execute();
	}

	private void readTable() {
		parent.setBusy(true);
		ResultSet rst = parent.dbPowerJ.getSpecimens(filters);
		ClassData thisRow = new ClassData();
		try {
			while (rst.next()) {
				thisRow = new ClassData();
				thisRow.specID = rst.getLong("SPECID");
				thisRow.caseID = rst.getLong("CASEID");
				thisRow.caseNo = rst.getString("CASENO");
				thisRow.template = rst.getString("MDESCR");
				thisRow.description = rst.getString("DESCR");
				thisRow.master = new DataItem(rst.getInt("MSID"),
						rst.getString("CODE"));
				list.add(thisRow);
			}
			parent.statusBar.setMessage("No rows " + list.size());
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
			parent.setBusy(false);
		}
	}

	void save() {
        int noUpdates = 0;
        ClassData thisRow = list.get(intRowNo);
		PreparedStatement stmInsert = null;
		PreparedStatement stmUpdate = null;
		try {
	        if (thisRow.master.getValue() > 0
	        		&& thisRow.caseID > 0) {
				stmInsert = parent.dbPowerJ.getStatement(0);
				stmInsert.setLong(1, thisRow.caseID);
				stmInsert.setInt(2, 0);
				stmInsert.setString(3, thisRow.caseNo);
				stmInsert.setString(4, "User updated specimen.");
				noUpdates = stmInsert.executeUpdate();
		        if (noUpdates > 0) {
					stmUpdate = dbAP.getStatement(0);
					stmUpdate.setInt(1, thisRow.master.getValue());
					stmUpdate.setLong(2, thisRow.specID);
					noUpdates = stmUpdate.executeUpdate();
		        }
	        }
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}

	private class ClassData {
		long specID = 0;
		long caseID = 0;
		DataItem master = new DataItem(0, "");
		String caseNo = "";
		String template = "";
		String description = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = 8045499089754301496L;
		private final String[] columns = {"CASE", "CODE", "TEMPLATE", "DESCR"};

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
					case DATA_SPEC_DESCR:
						objValue = thisRow.description;
						break;
					case DATA_SPEC_TEMPLATE:
						objValue = thisRow.template;
						break;
					case DATA_SPEC_CODE:
						objValue = thisRow.master;
						break;
					case DATA_CASE_NO:
						objValue = thisRow.caseNo;
						break;
					default:
						objValue = Object.class;
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return objValue;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_SPEC_CODE:
				return DataItem.class;
			default:
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			// Only if connected to update in PowerPath
			return (canEdit && col == DATA_SPEC_CODE);
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				ClassData thisRow = list.get(row);
				thisRow.master = (DataItem) value;
				intRowNo = row;
				save();
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}

	private class DataWorker extends SwingWorker<Void, Void> {

		protected Void doInBackground() throws Exception {
			readTable();
			return null;
		}
		
		public void done() {
			// Display results
			mdlData.fireTableDataChanged();
			parent.statusBar.setMessage("No rows " + list.size());
			if (list.size() > 0) {
				tblData.setRowSelectionInterval(0,0);
			}
		}
	}
}
