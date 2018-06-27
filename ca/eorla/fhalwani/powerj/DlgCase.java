package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

class DlgCase extends JDialog implements WindowListener {
	private static final long serialVersionUID = -1505835672335807387L;
	private final byte COL_SPEC_LABEL = 0;
	private final byte COL_SPEC_COLLECTED = 1;
	private final byte COL_SPEC_RECEIVED = 2;
	private final byte COL_SPEC_CODE = 3;
	private final byte COL_SPEC_DESCRIPTION = 4;
	private final byte COL_ROW_NO = 0;
	private final byte COL_EVNT_TIME = 1;
	private final byte COL_EVNT_MATERIAL = 2;
	private final byte COL_EVNT_LOCATION = 3;
	// private final byte COL_EVNT_DESCRIPTION = 4;
	private long caseID = 0;
	private PowerJ parent;
	private DbAPIS dbAP;
	private JTableEditor tblSpecimens;
	private JTableEditor tblEvents;
	private ArrayList<DataSpecimen> dataSpecimens = new ArrayList<DataSpecimen>();
	private ArrayList<DataEvent> dataEvents = new ArrayList<DataEvent>();
	
	DlgCase(PowerJ parent, long caseID, String caseNo) {
		super();
		this.parent = parent;
		this.caseID = caseID;
		if (!parent.variables.offLine) {
			dbAP = new DbAPIS(parent);
			if (dbAP.connected) {
				dbAP.prepareCase();
				readSpecimens();
				readEvents();
				dbAP.close();
			}
		}
		createDialog(caseNo);
	}

	private void createDialog(String caseNo) {
		setName("CaseDetails");
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setIconImage(Utilities.getImage(Constants.APP_NAME));
		setTitle(Constants.APP_NAME + " - " + caseNo);
		setLayout(new BorderLayout());
		addWindowListener(this);
		tblSpecimens = new JTableEditor(parent, new SpecimensModel());
		tblSpecimens.setName("tblSpecimens");
		TableColumn column = tblSpecimens.getColumnModel().getColumn(COL_SPEC_LABEL);
		column.setMinWidth(50);
		column.setMaxWidth(50);
		JScrollPane scrollSpecimens = new JScrollPane(tblSpecimens,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollSpecimens.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		scrollSpecimens.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -5301105545782919740L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		Utilities.addComponent(scrollSpecimens, 0, 0, 1, 1, 1, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, panel);
		// Model must be created before the table
		EventsModel modelEvents = new EventsModel();
		tblEvents = new JTableEditor(parent, modelEvents);
		tblEvents.setName("tblEvents");
		column = tblEvents.getColumnModel().getColumn(COL_ROW_NO);
		column.setMinWidth(50);
		column.setMaxWidth(50);
		JScrollPane scrollEvents = new JScrollPane(tblEvents,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollEvents.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		scrollEvents.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -3956958185030299002L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		Utilities.addComponent(scrollEvents, 0, 1, 1, 1, 1, 0.75,
				GridBagConstraints.BOTH, GridBagConstraints.SOUTHEAST, panel);
		add(panel, BorderLayout.CENTER);
		Rectangle r = new Rectangle(0, 0, 400, 600);
		r = parent.defaults.getRectangle("casebounds", r);
		Dimension dim = new Dimension(r.width, r.height);
		setLocation(r.x, r.y);
		setPreferredSize(dim);
		validate();
		pack();
		setVisible(true);
	}
	
	private void readEvents() {
		String strMaterial = "";
		DataEvent event = new DataEvent();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
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
			parent.log(JOptionPane.ERROR_MESSAGE, "Case Dialog", e);
		} finally {
			dbAP.closeRst(rst);
		}
	}

	private void readSpecimens() {
		DataSpecimen specimen = new DataSpecimen();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			stm = dbAP.getStatement(1);
			stm.setLong(1, caseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				specimen = new DataSpecimen();
				specimen.label = rst.getInt("specimen_label");
				specimen.code = rst.getString("code");
				specimen.description = rst.getString("description");
				specimen.received.setTimeInMillis(rst.getTimestamp("recv_date").getTime());
				if (rst.getTimestamp("collection_date") != null) {
					specimen.collected.setTimeInMillis(rst.getTimestamp("collection_date").getTime());
				} else {
					specimen.collected.setTimeInMillis(rst.getTimestamp("recv_date").getTime());
				}
				dataSpecimens.add(specimen);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, "Case Dialog", e);
		} finally {
			dbAP.closeRst(rst);
		}
	}

	class SpecimensModel extends AbstractTableModel {
		private static final long serialVersionUID = 780468192448864671L;
		private final String[] columHeaders = {"Label", "Collected", "Received", "Code", "Description"};
		
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
			case COL_SPEC_COLLECTED:
				returnValue = specimen.collected;
				break;
			case COL_SPEC_RECEIVED:
				returnValue = specimen.received;
				break;
			case COL_SPEC_CODE:
				returnValue = specimen.code;
				break;
			case COL_SPEC_DESCRIPTION:
				returnValue = specimen.description;
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
			// Not editable
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Ignore, not editable
		}
	}

	class EventsModel extends AbstractTableModel {
		private static final long serialVersionUID = 8607320495699389072L;
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
			case COL_ROW_NO:
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
		String code = "";
		String description = "";
		Calendar collected = Calendar.getInstance();
		Calendar received = Calendar.getInstance();
	}

	public void windowClosing(WindowEvent e) {
		// Save last used bounds
		Rectangle r = getBounds();
		parent.defaults.setRectangle("casebounds", r);
		dispose();
	}

	public void windowOpened(WindowEvent ignore) {}
	public void windowClosed(WindowEvent ignore) {}
	public void windowIconified(WindowEvent ignore) {}
	public void windowDeiconified(WindowEvent ignore) {}
	public void windowActivated(WindowEvent ignore) {}
	public void windowDeactivated(WindowEvent ignore) {}
}
