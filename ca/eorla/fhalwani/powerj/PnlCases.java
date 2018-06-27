package ca.eorla.fhalwani.powerj;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

class PnlCases extends PnlMain {
	private static final long serialVersionUID = 6117514492533214181L;
	private final byte COL_SPEC_LABEL = 0;
	private final byte COL_SPEC_CODE = 1;
	private final byte COL_SPEC_DESCRIPTION = 2;
	private final byte COL_SPEC_COLLECTED = 3;
	private final byte COL_SPEC_RECEIVED = 4;
	private final byte COL_EVNT_ROW_NO = 0;
	private final byte COL_EVNT_TIME = 1;
	private final byte COL_EVNT_MATERIAL = 2;
	private final byte COL_EVNT_LOCATION = 3;
	// private final byte COL_EVNT_DESCRIPTION = 4;
	private long caseID = 0;
	private String caseNo = "";
	private DbAPIS dbAP;
	private JTableEditor tblSpecimens;
	private JTableEditor tblEvents;
	private ArrayList<DataSpecimen> dataSpecimens = new ArrayList<DataSpecimen>();
	private ArrayList<DataEvent> dataEvents = new ArrayList<DataEvent>();

	PnlCases(PowerJ parent) {
		super(parent);
		setName("Cases");
		createPanel();
		if (!parent.variables.offLine) {
			dbAP = new DbAPIS(parent);
			if (dbAP.connected) {
				dbAP.prepareCase();
				parent.dbPowerJ.prepareCase();
			}
		}
	}

	boolean close() {
		super.close();
		dataSpecimens.clear();
		dataEvents.clear();
		if (!parent.variables.offLine) {
			if (dbAP.connected) {
				dbAP.close();
				parent.dbPowerJ.closeStms();
			}
		}
		return true;
	}

	private void createPanel() {
		setLayout(new GridBagLayout());
		setOpaque(true);
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		JLabel label = new JLabel("Case No: ");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_N);
		Utilities.addComponent(label, 0, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, this);
		JStringField textField = new JStringField(3, 15);
		textField.setName("CaseNo");
		textField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				try {
					Document doc = (Document)e.getDocument();
					caseNo = doc.getText(0, doc.getLength());
					altered = true;
				} catch (BadLocationException ignore) {}
			}

			public void insertUpdate(DocumentEvent e) {
				try {
					Document doc = (Document)e.getDocument();
					caseNo = doc.getText(0, doc.getLength());
					altered = true;
				} catch (BadLocationException ignore) {}
			}

			public void removeUpdate(DocumentEvent e) {
				try {
					Document doc = (Document)e.getDocument();
					caseNo = doc.getText(0, doc.getLength());
					altered = true;
				} catch (BadLocationException ignore) {}
			}
			
		});
		textField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				Component c = e.getComponent();
				((JTextComponent)c).selectAll();
			}

			public void focusLost(FocusEvent e) {
				Component c = e.getComponent();
				JTextComponent tc = (JTextComponent) c;
				tc.setCaretPosition(0);
				tc.setSelectionStart(0);
				tc.setSelectionEnd(0);
				if (altered) {
					setData();
				}
			}
			
		});
		label.setLabelFor(textField);
		Utilities.addComponent(textField, 1, 0, 1, 1, 0.1, 0,
				GridBagConstraints.HORIZONTAL,
				GridBagConstraints.EAST, this);
		tblSpecimens = new JTableEditor(parent, new SpecimensModel());
		tblSpecimens.setName("tblData");
		// Define the editor of the specimens templates
		CboMaster cboEditor = new CboMaster(parent, true);
		TableColumn column = tblSpecimens.getColumnModel().getColumn(COL_SPEC_CODE);
		column.setCellEditor(new DefaultCellEditor(cboEditor));
		column = tblSpecimens.getColumnModel().getColumn(COL_SPEC_LABEL);
		column.setMinWidth(50);
		column.setMaxWidth(50);
		JScrollPane scrollSpecimens = new JScrollPane(tblSpecimens,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollSpecimens.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -3872050641581161330L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		Utilities.addComponent(scrollSpecimens, 0, 1, 4, 3, 1.0, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, this);
		// Model must be created before the table
		EventsModel modelEvents = new EventsModel();
		tblEvents = new JTableEditor(parent, modelEvents);
		tblEvents.setName("tblEvents");
		column = tblEvents.getColumnModel().getColumn(COL_EVNT_ROW_NO);
		column.setMinWidth(50);
		column.setMaxWidth(50);
		JScrollPane scrollEvents = new JScrollPane(tblEvents,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollEvents.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		scrollEvents.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -1756203248071862612L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		Utilities.addComponent(scrollEvents, 0, 4, 4, 3, 1.0, 0.75,
				GridBagConstraints.BOTH, GridBagConstraints.SOUTHEAST, this);
	}
	
	void save(int row) {
        int noUpdates = 0;
		DataSpecimen specimen = dataSpecimens.get(row);
		PreparedStatement stmInsert = null;
		PreparedStatement stmUpdate = null;
		PreparedStatement stmSelect = null;
		try {
	        if (specimen.master.getValue() > 0
	        		&& specimen.specID > 0 && caseID > 0) {
				stmUpdate = dbAP.getStatement(3);
				stmUpdate.setInt(1, specimen.master.getValue());
				stmUpdate.setLong(2, specimen.specID);
				noUpdates = stmUpdate.executeUpdate();
		        if (noUpdates > 0) {
					stmSelect = parent.dbPowerJ.getStatement(0);
					stmSelect.setLong(1, caseID);
					ResultSet rst = parent.dbPowerJ.getResultSet(stmSelect);
					while (rst.next()) {
						if (caseNo.equals(rst.getString("CASENO"))) {
							stmInsert = parent.dbPowerJ.getStatement(1);
							stmInsert.setLong(1, caseID);
							stmInsert.setInt(2, 0);
							stmInsert.setString(3, caseNo);
							stmInsert.setString(4, "User updated specimen.");
							noUpdates = stmInsert.executeUpdate();
						}
					}
					rst.close();
		        }
	        }
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}

	private void setData() {
		if (parent.variables.busy.get()) return;
		parent.setBusy(true);
		// Must initialize a new instance each time
		DataWorker worker = new DataWorker();
		worker.execute();
	}
	
	class SpecimensModel extends AbstractTableModel {
		private static final long serialVersionUID = -2286087721046002667L;
		private final String[] columHeaders = {"Label", "Code", "Description", "Collected", "Received"};
		
		public int getColumnCount() {
			return columHeaders.length;
		}

		public int getRowCount() {
			return dataSpecimens.size();
		}

		public String getColumnName(int col) {
			return columHeaders[col];
		}

		public Object getValueAt(int row, int col) {
			DataSpecimen specimen = dataSpecimens.get(row);
			Object returnValue = Object.class;
			switch (col) {
			case COL_SPEC_LABEL:
				returnValue = specimen.label;
				break;
			case COL_SPEC_CODE:
				returnValue = specimen.master;
				break;
			case COL_SPEC_DESCRIPTION:
				returnValue = specimen.description;
				break;
			case COL_SPEC_COLLECTED:
				returnValue = specimen.collected;
				break;
			case COL_SPEC_RECEIVED:
				returnValue = specimen.received;
				break;
			default:
				// Ignore, return object
			}
			return returnValue;
		}

		public Class<?> getColumnClass(int column) {
			if (column == COL_SPEC_LABEL) {
				return Integer.class;
			} else if (column == COL_SPEC_COLLECTED
					|| column == COL_SPEC_RECEIVED) {
				return Calendar.class;
			} else {
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			// Only if connected to update in PowerPath
			return (col == COL_SPEC_CODE);
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				DataSpecimen specimen = dataSpecimens.get(row);
				specimen.master = (DataItem) value;
				save(row);
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}

	class EventsModel extends AbstractTableModel {
		private static final long serialVersionUID = -2593817631960512511L;
		private final String[] columHeaders = {"Row", "Time", "Material", "Location", "Description"};
		
		public int getColumnCount() {
			return columHeaders.length;
		}

		public int getRowCount() {
			return dataEvents.size();
		}

		public String getColumnName(int col) {
			return columHeaders[col];
		}

		public Object getValueAt(int row, int col) {
			DataEvent event = dataEvents.get(row);
			Object returnValue = Object.class;
			switch (col) {
			case COL_EVNT_ROW_NO:
				if (tblEvents != null) {
					returnValue = tblEvents.convertRowIndexToView(row) +1;
				}
				break;
			case COL_EVNT_TIME:
				returnValue = event.calDate;
				break;
			case COL_EVNT_MATERIAL:
				returnValue = event.material;
				break;
			case COL_EVNT_LOCATION:
				returnValue = event.location;
				break;
			default:
				returnValue = event.description;
			}
			return returnValue;
		}

		public Class<?> getColumnClass(int column) {
			if (column == COL_EVNT_TIME) {
				return Calendar.class;
			} else {
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			// Not editable
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Ignore, not editable
		}
	}
	
	private class DataEvent {
		String material = "";
		String location = "";
		String description = "";
		Calendar calDate = Calendar.getInstance();
	}
	
	private class DataSpecimen {
		int label = 0;
		long specID = 0;
		DataItem master = new DataItem(0, "");
		String description = "";
		Calendar collected = Calendar.getInstance();
		Calendar received = Calendar.getInstance();
	}

	private class DataWorker extends SwingWorker<Void, Void> {
		protected Void doInBackground() throws Exception {
			setName("TrackerWorker");
			if (!parent.variables.offLine) {
				if (dbAP.connected) {
					if (altered) {
						readCase();
						altered = false;
					}
				}
			}
			return null;
		}

		private void readCase() {
			ResultSet rst = null;
			PreparedStatement stm = null;
			try {
				stm = dbAP.getStatement(0);
				stm.setString(1, caseNo);
				rst = stm.executeQuery();
				while (rst.next()) {
					caseID = rst.getLong("id");
				}
				if (caseID > 0) {
					readSpecimens();
					readEvents();
				}
			} catch (SQLException e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			} finally {
				dbAP.closeRst(rst);
			}
		}
		
		private void readSpecimens() {
			DataSpecimen specimen = new DataSpecimen();
			PreparedStatement stm = null;
			ResultSet rst = null;
			try {
				dataSpecimens.clear();
				stm = dbAP.getStatement(1);
				stm.setLong(1, caseID);
				rst = stm.executeQuery();
				while (rst.next()) {
					specimen = new DataSpecimen();
					specimen.specID = rst.getLong("id");
					specimen.label = rst.getInt("specimen_label");
					specimen.description = rst.getString("description");
					specimen.received.setTimeInMillis(rst.getTimestamp("recv_date").getTime());
					if (rst.getTimestamp("collection_date") != null) {
						specimen.collected.setTimeInMillis(rst.getTimestamp("collection_date").getTime());
					} else {
						specimen.collected.setTimeInMillis(rst.getTimestamp("recv_date").getTime());
					}
					specimen.master = new DataItem(rst.getInt("tmplt_profile_id"), rst.getString("CODE"));
					dataSpecimens.add(specimen);
				}
			} catch (SQLException e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			} finally {
				dbAP.closeRst(rst);
			}
		}

		private void readEvents() {
			String strMaterial = "";
			DataEvent event = new DataEvent();
			PreparedStatement stm = null;
			ResultSet rst = null;
			try {
				dataEvents.clear();
				stm = dbAP.getStatement(2);
				stm.setLong(1, caseID);
				rst = stm.executeQuery();
				while (rst.next()) {
					strMaterial = rst.getString("source_rec_type").trim();
					if (strMaterial.equals("S")) {
						strMaterial = "Specimen ";
					} else if (strMaterial.equals("B")) {
						strMaterial = "Block ";
					} else if (strMaterial.equals("L")) {
						if (rst.getString("event_type").trim().
								toLowerCase().equals("folder_scanned")) {
							// Skip these, meaningless
							continue;
						}
						strMaterial = "Slide ";
					} else {
						strMaterial += " ";
					}
					strMaterial += rst.getString("material_label").trim();
					event = new DataEvent();
					event.material = strMaterial;
					event.calDate.setTimeInMillis(rst.getTimestamp("event_date").getTime());
					event.location = rst.getString("event_location");
					event.description = rst.getString("event_description");
					dataEvents.add(event);
				}
			} catch (SQLException e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			} finally {
				dbAP.closeRst(rst);
			}
		}

		public void done() {
			parent.setBusy(false);
			// Display results
			((AbstractTableModel) tblSpecimens.getModel()).fireTableDataChanged();
			((AbstractTableModel) tblEvents.getModel()).fireTableDataChanged();
		}
	}
}
