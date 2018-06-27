package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

class PnlErrors extends PnlMain {
	private static final long serialVersionUID = -3020865416989906895L;
	// private final byte DATA_SPEC_ID = 0;
	private final byte DATA_SPEC_CODE = 0;
	private final byte DATA_SPEC_DESCR = 1;
	private boolean canEdit = false;
	private long lngCaseID = 0;
	private JTableEditor tblCases, tblSpecimens;
	private JTextArea txtComment;
	private ModelCases mdlCases;
	private ModelSpecimens mdlSpecimens;
	private DbAPIS dbAP;
	private ArrayList<ClassCases> cases = new ArrayList<ClassCases>();
	private ArrayList<ClassSpecimen> specimens = new ArrayList<ClassSpecimen>();

	PnlErrors(PowerJ parent) {
		super(parent);
		setName("Errors");
		readTable();
		if (!parent.variables.offLine) {
			dbAP = new DbAPIS(parent);
			if (dbAP.connected) {
				dbAP.prepareSpecimens();
				if (!parent.variables.hasError) {
					parent.dbPowerJ.prepareErrors();
					if (!parent.variables.hasError) {
						canEdit = true;
					}
				}
			}
		}
		createPanel();
	}

	boolean close() {
		specimens.clear();
		cases.clear();
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
		// Layout 2 panels from top to bottom.
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(createPanelCases());
		boxPanel.add(createPanelSpecimens());
		add(boxPanel, BorderLayout.CENTER);
		if (cases.size() > 0) {
			// Display results
			mdlCases.fireTableDataChanged();
			tblCases.setRowSelectionInterval(0,0);
			lngCaseID = cases.get(0).caseID;
			readSpecimens();
		}
		parent.statusBar.setMessage("No rows " + cases.size());
	}
	
	private JPanel createPanelCases() {
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Cases");
		title.setTitleJustification(TitledBorder.CENTER);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(title);
		panel.setName("Cases");
		panel.setOpaque(true);
		mdlCases = new ModelCases();
		tblCases = new JTableEditor(parent, mdlCases);
		tblCases.setName("tblCases");
        tblCases.getSelectionModel().addListSelectionListener(
        		new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages
		        if (e.getValueIsAdjusting()) return;
				if (parent.variables.offLine) return;
				if (!dbAP.connected) return;
		        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) return;
		        int viewRow = lsm.getMinSelectionIndex();
		        if (viewRow > -1) {
					// else, Selection got filtered away.
					int modelRow = tblCases.convertRowIndexToModel(viewRow);
					if (lngCaseID != cases.get(modelRow).caseID) {
						lngCaseID = cases.get(modelRow).caseID;
						readSpecimens();
					}
		        }
			}
        });
		JScrollPane scrollCases = new JScrollPane(tblCases,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollCases.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 2714270694970413738L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		panel.add(scrollCases);
		return panel;
	}
	
	private JPanel createPanelSpecimens() {
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Specimens");
		title.setTitleJustification(TitledBorder.CENTER);
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(title);
		panel.setName("Specimens");
		panel.setOpaque(true);
		CboMaster cboEditor = new CboMaster(parent, true);
		mdlSpecimens = new ModelSpecimens();
		tblSpecimens = new JTableEditor(parent, mdlSpecimens);
		// detect row selection
		tblSpecimens.setName("tblSpecimens");
		TableColumn column = tblSpecimens.getColumnModel().getColumn(DATA_SPEC_CODE);
		column.setCellEditor(new DefaultCellEditor(cboEditor));
		JScrollPane scroller = new JScrollPane(tblSpecimens,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -7170776227218552293L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		Border borderEmpty = BorderFactory.createEmptyBorder(2, 5, 2, 5);
		scroller.setBorder(borderEmpty);
		panel.add(scroller);
		txtComment = new JTextArea();
		txtComment.setEditable(false);
		txtComment.setMargin(new Insets(5, 5, 5, 5));
		txtComment.setFont(Constants.APP_FONT);
		txtComment.setLineWrap(true);
		txtComment.setWrapStyleWord(true);
		JScrollPane scrollComment = new JScrollPane(txtComment,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollComment.setBorder(borderEmpty);
		scrollComment.setPreferredSize(new Dimension(200, 500));
		panel.add(scrollComment);
		return panel;
	}
	
	private void readSpecimens() {
		String comment = "";
		ClassSpecimen specimen = new ClassSpecimen();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			specimens.clear();
			stm = dbAP.getStatement(1);
			stm.setLong(1, lngCaseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				specimen = new ClassSpecimen();
				specimen.specID = rst.getLong("id");
				specimen.description = rst.getString("description");
				specimen.master = new DataItem(rst.getInt("tmplt_profile_id"),
						rst.getString("code"));
				specimens.add(specimen);
			}
			rst.close();
			stm = parent.dbPowerJ.getStatement(0);
			stm.setLong(1, lngCaseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				comment = rst.getString("COMMENT");
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			txtComment.setText(comment);
			mdlSpecimens.fireTableDataChanged();
			if (specimens.size() > 0) {
				// Else, index out of range error
				tblSpecimens.setRowSelectionInterval(0,0);
			}
		}
	}
	
	private void readTable() {
		ResultSet rst = parent.dbPowerJ.getErrors(1);
		ClassCases thisRow = new ClassCases();
		try {
			while (rst.next()) {
				thisRow = new ClassCases();
				thisRow.caseID = rst.getLong("CASEID");
				thisRow.errID = rst.getByte("ERRID");
				thisRow.caseNo = rst.getString("CASENO");
				cases.add(thisRow);
			}
			parent.statusBar.setMessage("No rows " + cases.size());
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
		}
	}

	void save(int row) {
        int noUpdates = 0;
        ClassSpecimen thisRow = specimens.get(row);
		PreparedStatement stmInsert = null;
		PreparedStatement stmUpdate = null;
		try {
	        if (thisRow.master.getValue() > 0
	        		&& thisRow.specID > 0 && lngCaseID > 0) {
				stmInsert = parent.dbPowerJ.getStatement(1);
				stmInsert.setLong(1, lngCaseID);
				stmInsert.setByte(2, (byte) 0);
				stmInsert.setString(3, "");
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
	
	private class ClassCases {
		long caseID = 0;
		byte errID = 0;
		String caseNo = "";
	}

	private class ModelCases extends AbstractTableModel {
		private static final long serialVersionUID = -8056462671866797141L;
		private final String[] columns = {"CASE", "ERROR"};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return cases.size();
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Object getValueAt(int row, int col) {
			Object objValue = Object.class;
			try {
				if (row < cases.size()) {
					ClassCases thisRow = cases.get(row);
					if (col == 0) {
						objValue = thisRow.caseNo;
					} else {
						objValue = thisRow.errID;
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return objValue;
		}

		public Class<?> getColumnClass(int col) {
			if (col == 0) {
				return String.class;
			}
			return Byte.class;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Table is not editable
		}
	}
	
	private class ClassSpecimen {
		long specID = 0;
		String description = "";
		DataItem master = new DataItem(0, "");
	}

	private class ModelSpecimens extends AbstractTableModel {
		private static final long serialVersionUID = 8045499089754301496L;
		private final String[] columns = {"CODE", "DESCR"};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return specimens.size();
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Object getValueAt(int row, int col) {
			Object objValue = Object.class;
			try {
				if (row < specimens.size()) {
					ClassSpecimen thisSpecimen = specimens.get(row);
					switch (col) {
					case DATA_SPEC_DESCR:
						objValue = thisSpecimen.description;
						break;
					default:
						objValue = thisSpecimen.master;
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
				specimens.get(row).master = (DataItem) value;
				save(row);
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}
}
