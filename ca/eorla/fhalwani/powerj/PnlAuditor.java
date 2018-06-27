package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.helpers.ColumnHelper;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class PnlAuditor extends PnlMain implements ListSelectionListener {
	private static final long serialVersionUID = -5791923912796027975L;
	private final byte DATA_CASE_NO = 0;
	private final byte DATA_CASE_FINAL_DATE = 1;
	private final byte DATA_CASE_SPECIALTY = 2;
	private final byte DATA_CASE_SUBSPECIALTY = 3;
	private final byte DATA_CASE_PROCEDURE = 4;
	private final byte DATA_CASE_STAFF = 5;
	private final byte DATA_CASE_CODE1 = 6;
	private final byte DATA_CASE_CODE2 = 7;
	private final byte DATA_CASE_CODE3 = 8;
	private final byte DATA_CASE_CODE4 = 9;
	private final byte DATA_CASE_NOSPECS = 10;
	private final byte DATA_CASE_NOBLOCKS = 11;
	private final byte DATA_CASE_NOSLIDES = 12;
	private final byte DATA_CASE_NOSYNOPTICS = 13;
	private final byte DATA_CASE_NOFS = 14;
	private final byte DATA_SPEC_CODE = 0;
	private final byte DATA_SPEC_DESCR = 1;
	private final byte DATA_SPEC_CODE1 = 2;
	private final byte DATA_SPEC_CODE2 = 3;
	private final byte DATA_SPEC_CODE3 = 4;
	private final byte DATA_SPEC_CODE4 = 5;
	private final byte DATA_SPEC_NOBLOCKS = 6;
	private final byte DATA_SPEC_NOSLIDES = 7;
	private final byte DATA_SPEC_NOFRAGS = 8;
	// private final byte DATA_ORDER_CODE = 0;
	private final byte DATA_ORDER_QTY = 1;
	private final byte DATA_ORDER_CODE1 = 2;
	private final byte DATA_ORDER_CODE2 = 3;
	private final byte DATA_ORDER_CODE3 = 4;
	private final byte DATA_ORDER_CODE4 = 5;
	// private final byte DATA_ADDL_CODE = 0;
	private final byte DATA_ADDL_STAFF = 1;
	private final byte DATA_ADDL_DATE = 2;
	private final byte DATA_ADDL_NOSPECS = 3;
	private final byte DATA_ADDL_NOBLOCKS = 4;
	private final byte DATA_ADDL_NOSLIDES = 5;
	private final byte DATA_ADDL_CODE1 = 6;
	private final byte DATA_ADDL_CODE2 = 7;
	private final byte DATA_ADDL_CODE3 = 8;
	private final byte DATA_ADDL_CODE4 = 9;
	private final byte FILTER_FACILITY = 0;
	private final byte FILTER_SPECIALTY = 1;
	private final byte FILTER_SUBSPECIAL = 2;
	private final byte FILTER_PROCEDURE = 3;
	private boolean canEdit = false;
	private int[] filters = {0, 0, 0, 0};
	private long lngCaseID = 0;
	private long lngSpecID = 0;
	private JTableEditor tblCases, tblSpecimens, tblOrders, tblAdditionals;
	private JTextArea txtComment;
	private ArrayList<ClassCase> dataCases = new ArrayList<ClassCase>();
	private ArrayList<ClassSpecimen> dataSpecs = new ArrayList<ClassSpecimen>();
	private ArrayList<ClassOrder> dataOrders = new ArrayList<ClassOrder>();
	private ArrayList<ClassAdditional> dataAdditional = new ArrayList<ClassAdditional>();
	private DbAPIS dbAP;
	
	PnlAuditor(PowerJ parent) {
		super(parent);
		setName("Auditor");
		parent.dbPowerJ.prepareAuditor();
		if (!parent.variables.offLine) {
			dbAP = new DbAPIS(parent);
			if (dbAP.connected) {
				dbAP.prepareSpecimens();
				canEdit = true;
			}
		}
		readTable();
		createPanel();
	}

	boolean close() {
		dataOrders.clear();
		dataSpecs.clear();
		dataCases.clear();
		if (!parent.variables.offLine) {
			if (dbAP.connected) {
				dbAP.closeStms();
				dbAP.close();
			}
			super.close();
		}
		return true;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		// Layout 3 panels from top to bottom.
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));
		boxPanel.add(createPanelCases());
		boxPanel.add(createPanelSpecimens());
		boxPanel.add(createPanelBottom());
		add(createToolbar(), BorderLayout.NORTH);
		add(boxPanel, BorderLayout.CENTER);
		if (dataCases.size() > 0) {
			// Display results
			((AbstractTableModel) tblCases.getModel()).fireTableDataChanged();
			tblCases.setRowSelectionInterval(0,0);
			lngCaseID = dataCases.get(0).caseID;
			readSpecimens();
		}
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
		ModelCases mdlCases = new ModelCases();
		tblCases = new JTableEditor(parent, mdlCases);
		tblCases.setName("tblCases");
        tblCases.getSelectionModel().addListSelectionListener(
        		new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages
		        if (e.getValueIsAdjusting()) return;
		        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) return;
		        int viewRow = lsm.getMinSelectionIndex();
		        if (viewRow > -1) {
					// else, Selection got filtered away.
					int modelRow = tblCases.convertRowIndexToModel(viewRow);
					if (lngCaseID != dataCases.get(modelRow).caseID) {
						lngCaseID = dataCases.get(modelRow).caseID;
				        readSpecimens();
					}
		        }
			}
        });
		TableColumn column = new TableColumn();
		for (int i = DATA_CASE_CODE4; i >= DATA_CASE_CODE1; i--) {
			column = tblCases.getColumnModel().getColumn(i);
			if (!parent.variables.codersActive[i - DATA_CASE_CODE1]) {
				tblCases.removeColumn(column);
			}
		}
		JScrollPane scrollCases = new JScrollPane(tblCases,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollCases.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 7929845594115898238L;
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
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setName("Specimens");
		panel.setOpaque(true);
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Specimens");
		title.setTitleJustification(TitledBorder.CENTER);
		ModelSpecimens mdlSpecimens = new ModelSpecimens();
		tblSpecimens = new JTableEditor(parent, mdlSpecimens);
		// detect row selection
		tblSpecimens.setName("tblSpecimens");
        tblSpecimens.getSelectionModel().addListSelectionListener(
        		new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
		        //Ignore extra messages
		        if (e.getValueIsAdjusting()) return;
		        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
		        if (lsm.isSelectionEmpty()) return;
		        int viewRow = lsm.getMinSelectionIndex();
		        if (viewRow > -1) {
					// else, Selection got filtered away.
					int modelRow = tblSpecimens.convertRowIndexToModel(viewRow);
					if (lngSpecID != dataSpecs.get(modelRow).specID) {
						lngSpecID = dataSpecs.get(modelRow).specID;
				        readOrders();
					}
		        }
			}
        	
        });
		TableColumn column = tblSpecimens.getColumnModel().getColumn(DATA_SPEC_CODE);
		CboMaster cboEditor = new CboMaster(parent, true);
		column.setCellEditor(new DefaultCellEditor(cboEditor));
		for (int i = DATA_SPEC_CODE4; i >= DATA_SPEC_CODE1; i--) {
			column = tblSpecimens.getColumnModel().getColumn(i);
			if (!parent.variables.codersActive[i - DATA_SPEC_CODE1]) {
				tblSpecimens.removeColumn(column);
			}
		}
		JScrollPane scrollSpecimens = new JScrollPane(tblSpecimens,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollSpecimens.setBorder(title);
		scrollSpecimens.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 8334954865866544843L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		panel.add(scrollSpecimens);
		title = BorderFactory.createTitledBorder(border, "Comment");
		title.setTitleJustification(TitledBorder.CENTER);
		txtComment = new JTextArea();
		txtComment.setEditable(false);
		txtComment.setMargin(new Insets(5, 5, 5, 5));
		txtComment.setFont(Constants.APP_FONT);
		txtComment.setLineWrap(true);
		txtComment.setWrapStyleWord(true);
		JScrollPane scrollComment = new JScrollPane(txtComment,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		Dimension minimumSize = new Dimension(300, 200);
		scrollComment.setMinimumSize(minimumSize);
		scrollComment.setPreferredSize(minimumSize);
		scrollComment.setBorder(title);
		panel.add(scrollComment);
		return panel;
	}
	
	private JPanel createPanelBottom() {
		// Layout 2 horizontal panels
		JPanel boxPanel = new JPanel();
		boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.X_AXIS));
		boxPanel.setOpaque(true);
		// Titled borders
		Border border = BorderFactory.createLineBorder(Color.black);
		TitledBorder title = BorderFactory.createTitledBorder(border, "Orders");
		title.setTitleJustification(TitledBorder.CENTER);
		ModelOrders mdlOrders = new ModelOrders();
		tblOrders = new JTableEditor(parent, mdlOrders);
		TableColumn column = new TableColumn();
		for (int i = DATA_ORDER_CODE4; i >= DATA_ORDER_CODE1; i--) {
			column = tblOrders.getColumnModel().getColumn(i);
			if (!parent.variables.codersActive[i - DATA_ORDER_CODE1]) {
				tblOrders.removeColumn(column);
			}
		}
		JScrollPane scrollOrders = new JScrollPane(tblOrders,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollOrders.setBorder(title);
		scrollOrders.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -5988770619616488422L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		boxPanel.add(scrollOrders);
		title = BorderFactory.createTitledBorder(border, "Additionals");
		title.setTitleJustification(TitledBorder.CENTER);
		ModelAdditional mdlAdditional = new ModelAdditional();
		tblAdditionals = new JTableEditor(parent, mdlAdditional);
		tblAdditionals.setName("tblAdditionals");
		column = tblAdditionals.getColumnModel().getColumn(DATA_ADDL_STAFF);
		CboPathologists cboPathologists = new CboPathologists(parent, true);
		column.setCellEditor(new DefaultCellEditor(cboPathologists));
		for (int i = DATA_ADDL_CODE4; i >= DATA_ADDL_CODE1; i--) {
			column = tblAdditionals.getColumnModel().getColumn(i);
			if (!parent.variables.codersActive[i - DATA_ADDL_CODE1]) {
				tblAdditionals.removeColumn(column);
			}
		}
		JScrollPane scrollAdditional = new JScrollPane(tblAdditionals,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollAdditional.setBorder(title);
		scrollAdditional.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -2244910731274696096L;

			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		boxPanel.add(scrollAdditional);
		return boxPanel;
	}

	private JPanel createToolbar() {
		// Setup 4 JComboBox and fill with their data
		CboFacilities cboFacilities = new CboFacilities(parent);
		cboFacilities.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[FILTER_FACILITY] = item.getValue();
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
	    			filters[FILTER_SPECIALTY] = item.getValue();
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
	    			filters[FILTER_SUBSPECIAL] = item.getValue();
	    			readData();
	    		}
	        }
	    });
		CboProcedures cboProcs = new CboProcedures(parent, false);
		cboProcs.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[FILTER_PROCEDURE] = item.getValue();
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
		label = new JLabel("Procedure:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		label.setDisplayedMnemonicIndex(1);
		label.setLabelFor(cboProcs);
		panel.add(label);
		panel.add(cboProcs);
		return panel;
	}

	void pdf() {
		String fileName = ((PJClient)parent).getFilePdf("workload.pdf").trim();
		if (fileName.trim().length() == 0) return;
        final String[] headers = {"CASENO", "FINAL", "SPY", "SUB",
				"PROC", "STAFF", parent.variables.codersName[0], parent.variables.codersName[1],
				parent.variables.codersName[2], parent.variables.codersName[3],
				"SPECS", "BLK", "SLD", "SYNOP", "FSEC"};
		final float[] widths = {2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        String str = "Workload";
		PdfFonts pdfLib = new PdfFonts();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER.rotate(), 36, 36, 36, 36);
        Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		try {
			// Write the output to a file
			FileOutputStream fos = new FileOutputStream(fileName);
            PdfWriter.getInstance(document, fos);
            document.open();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.add(new Chunk(Constants.LAB_NAME));
            document.add(paragraph);
            paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.add(new Chunk(str));
            document.add(paragraph);
            document.add(Chunk.NEWLINE);
			PdfPTable table = new PdfPTable(headers.length);
			table.setWidthPercentage(100);
			table.setWidths(widths);
			// header row
			for (int i = 0; i < headers.length; i++) {
	            paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(headers[i]));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			// data rows
			ClassCase dataRow;
			int row = 0;
			for (int j = 0; j < tblCases.getRowCount(); j++) {
				row = tblCases.convertRowIndexToModel(j);
				dataRow = dataCases.get(row);
				for (int i = 0; i < headers.length; i++) {
		            paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					switch (i) {
					case DATA_CASE_NO:
						str = dataRow.caseNo;
						break;
					case DATA_CASE_FINAL_DATE:
						str = parent.dateUtils.formatter(dataRow.calFinal, parent.dateUtils.FORMAT_DATETIME);
						break;
					case DATA_CASE_SPECIALTY:
						str = dataRow.specialty;
						break;
					case DATA_CASE_SUBSPECIALTY:
						str = dataRow.subspecialty;
						break;
					case DATA_CASE_PROCEDURE:
						str = dataRow.procedure;
						break;
					case DATA_CASE_CODE1:
						str = parent.numbers.formatDouble(2, dataRow.value1);
						break;
					case DATA_CASE_CODE2:
						str = parent.numbers.formatDouble(2, dataRow.value2);
						break;
					case DATA_CASE_CODE3:
						str = parent.numbers.formatDouble(2, dataRow.value3);
						break;
					case DATA_CASE_CODE4:
						str = parent.numbers.formatDouble(2, dataRow.value4);
						break;
					case DATA_CASE_NOSPECS:
						str = "" + dataRow.noSpecimens;
						break;
					case DATA_CASE_NOBLOCKS:
						str = "" + dataRow.noBlocks;
						break;
					case DATA_CASE_NOSLIDES:
						str = "" + dataRow.noSlides;
						break;
					case DATA_CASE_NOSYNOPTICS:
						str = "" + dataRow.noSynoptics;
						break;
					case DATA_CASE_NOFS:
						str = "" + dataRow.noFS;
						break;
					default:
						str = dataRow.staff;
					}
					paragraph.add(new Chunk(str));
					cell = new PdfPCell();
					switch (i) {
					case DATA_CASE_FINAL_DATE:
					case DATA_CASE_CODE1:
					case DATA_CASE_CODE2:
					case DATA_CASE_CODE3:
					case DATA_CASE_CODE4:
					case DATA_CASE_NOSPECS:
					case DATA_CASE_NOBLOCKS:
					case DATA_CASE_NOSLIDES:
					case DATA_CASE_NOSYNOPTICS:
					case DATA_CASE_NOFS:
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
						break;
					default:
						paragraph.setAlignment(Element.ALIGN_LEFT);
						cell.setHorizontalAlignment(Element.ALIGN_LEFT);
					}
					cell.addElement(paragraph);
					table.addCell(cell);
				}
			}
			document.add(table);
            document.close();
        } catch (DocumentException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
        } catch (FileNotFoundException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
        }
	}

	private void readData() {
		if (parent.variables.busy.get()) return;
		dataCases.clear();
		txtComment.setText("");
		// Must initialize a new instance each time
		DataWorker worker = new DataWorker();
		worker.execute();
	}
	
	private void readOrders() {
		ClassOrder order = new ClassOrder();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			dataOrders.clear();
			stm = parent.dbPowerJ.getStatement(1);
			stm.setLong(1, lngSpecID);
			rst = stm.executeQuery();
			while (rst.next()) {
				order = new ClassOrder();
				order.qty = rst.getShort("QTY");
				order.value1 = rst.getDouble("VALUE1");
				order.value2 = rst.getDouble("VALUE2");
				order.value3 = rst.getDouble("VALUE3");
				order.value4 = rst.getDouble("VALUE4");
				order.name = rst.getString("NAME");
				dataOrders.add(order);
			}
			((AbstractTableModel) tblOrders.getModel()).fireTableDataChanged();
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
		}
	}
	
	private void readSpecimens() {
		String comment = "";
		ClassSpecimen specimen = new ClassSpecimen();
		ClassAdditional additional = new ClassAdditional();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			dataSpecs.clear();
			lngSpecID = 0;
			stm = parent.dbPowerJ.getStatement(2);
			stm.setLong(1, lngCaseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				specimen = new ClassSpecimen();
				specimen.specID = rst.getLong("SPECID");
				specimen.noBlocks = rst.getShort("NOBLOCKS");
				specimen.noSlides = rst.getShort("NOSLIDES");
				specimen.noFrags = rst.getShort("NOFRAGS");
				specimen.value1 = rst.getDouble("VALUE1");
				specimen.value2 = rst.getDouble("VALUE2");
				specimen.value3 = rst.getDouble("VALUE3");
				specimen.value4 = rst.getDouble("VALUE4");
				specimen.description = rst.getString("DESCR");
				specimen.master = new DataItem(rst.getInt("MSID"),
						rst.getString("CODE"));
				dataSpecs.add(specimen);
			}
			rst.close();
			((AbstractTableModel) tblSpecimens.getModel()).fireTableDataChanged();
			if (dataSpecs.size() > 0) {
				// Else, index out of range error
				tblSpecimens.setRowSelectionInterval(0,0);
				lngSpecID = dataSpecs.get(0).specID;
			}
			dataAdditional.clear();
			stm = parent.dbPowerJ.getStatement(3);
			stm.setLong(1, lngCaseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				additional = new ClassAdditional();
				additional.codeID = 0;
				additional.noSpecimens = rst.getShort("NOSPECS");
				additional.noBlocks = rst.getShort("NOBLOCKS");
				additional.noSlides = rst.getShort("NOSLIDES");
				additional.value1 = rst.getDouble("VALUE1");
				additional.value2 = rst.getDouble("VALUE2");
				additional.value3 = rst.getDouble("VALUE3");
				additional.value4 = rst.getDouble("VALUE4");
				additional.staff = new DataItem(rst.getInt("PERID"), rst.getString("INITIALS"));
				additional.date.setTime(rst.getTimestamp("ACCESSED").getTime());
				dataAdditional.add(additional);
			}
			rst.close();
			stm = parent.dbPowerJ.getStatement(4);
			stm.setLong(1, lngCaseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				additional = new ClassAdditional();
				if (rst.getShort("CODEID") > 4) {
					// Reviews
					additional.codeID = 4;
				} else {
					additional.codeID = rst.getByte("CODEID");
				}
				additional.value1 = rst.getDouble("VALUE1");
				additional.value2 = rst.getDouble("VALUE2");
				additional.value3 = rst.getDouble("VALUE3");
				additional.value4 = rst.getDouble("VALUE4");
				additional.staff = new DataItem(rst.getInt("PERID"), rst.getString("INITIALS"));
				additional.date.setTime(rst.getTimestamp("FINALED").getTime());
				dataAdditional.add(additional);
			}
			rst.close();
			((AbstractTableModel) tblAdditionals.getModel()).fireTableDataChanged();
			if (dataAdditional.size() > 0) {
				// Else, index out of range error
				tblAdditionals.setRowSelectionInterval(0,0);
			}
			stm = parent.dbPowerJ.getStatement(0);
			stm.setLong(1, lngCaseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				if (rst.getString("COMMENT") != null) {
					// deleted after a year
					comment = rst.getString("COMMENT");
				}
			}
			rst.close();
			txtComment.setText(comment);
			txtComment.setCaretPosition(0);
			readOrders();
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
		}
	}
	
	private void readTable() {
		parent.setBusy(true);
		ClassCase thisRow = new ClassCase();
		ResultSet rst = parent.dbPowerJ.getFinals(filters);
		try {
			while (rst.next()) {
				thisRow = new ClassCase();
				thisRow.caseID = rst.getLong("CASEID");
				thisRow.noSynoptics = rst.getByte("NOSYNOPT");
				thisRow.noSpecimens = rst.getShort("NOSPECS");
				thisRow.noBlocks = rst.getShort("NOBLOCKS");
				thisRow.noSlides = rst.getShort("NOSLIDES");
				thisRow.noFS = rst.getShort("NOFS");
				thisRow.value1 = rst.getDouble("VALUE1");
				thisRow.value2 = rst.getDouble("VALUE2");
				thisRow.value3 = rst.getDouble("VALUE3");
				thisRow.value4 = rst.getDouble("VALUE4");
				thisRow.calFinal.setTimeInMillis(rst.getTimestamp("FINALED").getTime());
				thisRow.caseNo = rst.getString("CASENO");
				thisRow.staff = rst.getString("INITIALS");
				thisRow.specialty = rst.getString("SPYNAME");
				thisRow.subspecialty = rst.getString("SUBINIT");
				thisRow.procedure = DataProcedure.NAMES[rst.getShort("PROID")];
				dataCases.add(thisRow);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
			parent.setBusy(false);
		}
	}

	void saveSpecimen(int row) {
        int noUpdates = 0;
        ClassSpecimen thisRow = dataSpecs.get(row);
		PreparedStatement stmInsert = null;
		PreparedStatement stmUpdate = null;
		try {
	        if (thisRow.master.getValue() > 0
	        		&& thisRow.specID > 0 && lngCaseID > 0) {
				stmInsert = parent.dbPowerJ.getStatement(6);
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
	
	void saveStaff(int row) {
        ClassAdditional thisRow = dataAdditional.get(row);
		PreparedStatement stmUpdate = null;
		try {
	        if (thisRow.staff.getValue() > 0 && lngCaseID > 0) {
				stmUpdate = parent.dbPowerJ.getStatement(5);
				stmUpdate.setInt(1, thisRow.staff.getValue());
				stmUpdate.setLong(2, lngCaseID);
				stmUpdate.executeUpdate();
	        }
 		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}
	
	public void valueChanged(ListSelectionEvent e) {
        //Ignore extra messages
        if (e.getValueIsAdjusting()) return;
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (lsm.isSelectionEmpty()) return;
        int viewRow = lsm.getMinSelectionIndex();
        if (viewRow > -1) {
			// else, Selection got filtered away.
			int modelRow = tblCases.convertRowIndexToModel(viewRow);
			if (lngCaseID != dataCases.get(modelRow).caseID) {
				lngCaseID = dataCases.get(modelRow).caseID;
				if (lngCaseID > 0) {
			        readSpecimens();
				}
			}
        }
	}

	void xls() {
		String fileName = ((PJClient)parent).getFileXls("workload.xlsx").trim();
		if (fileName.trim().length() == 0) return;
		final String[] headers = {"CASENO", "FINAL", "SPY", "SUB",
				"PROC", "STAFF", parent.variables.codersName[0], parent.variables.codersName[1],
				parent.variables.codersName[2], parent.variables.codersName[3],
				"SPECS", "BLK", "SLD", "SYNOP", "FSEC"};
		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			XlsStyles xlsLib = new XlsStyles(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			XSSFSheet sheet = wb.createSheet("Workload");
			// title row
			Row row = sheet.createRow(0);
			row.setHeightInPoints(45);
			Cell cell = row.createCell(0);
			cell.setCellValue("Workload");
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$O$1"));
			// header row
			row = sheet.createRow(1);
			row.setHeightInPoints(30);
			for (int i = 0; i < headers.length; i++) {
				cell = row.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(styles.get("header"));
				if (i == 1) {
					sheet.setColumnWidth(i, 25 * 256); // 25 characters
				} else {
					sheet.setColumnWidth(i, 15 * 256); // 15 characters
				}
			}
			ColumnHelper colHelper = sheet.getColumnHelper();
			colHelper.setColDefaultStyle(0, styles.get("text"));
			colHelper.setColDefaultStyle(1, styles.get("datetime"));
			colHelper.setColDefaultStyle(2, styles.get("text"));
			colHelper.setColDefaultStyle(3, styles.get("text"));
			colHelper.setColDefaultStyle(4, styles.get("text"));
			colHelper.setColDefaultStyle(5, styles.get("text"));
			colHelper.setColDefaultStyle(6, styles.get("data_double"));
			colHelper.setColDefaultStyle(7, styles.get("data_double"));
			colHelper.setColDefaultStyle(8, styles.get("data_double"));
			colHelper.setColDefaultStyle(9, styles.get("data_double"));
			colHelper.setColDefaultStyle(10, styles.get("data_int"));
			colHelper.setColDefaultStyle(11, styles.get("data_int"));
			colHelper.setColDefaultStyle(12, styles.get("data_int"));
			colHelper.setColDefaultStyle(13, styles.get("data_int"));
			colHelper.setColDefaultStyle(14, styles.get("data_int"));
			// data rows
			int rownum = 2;
			int i = 0;
			CellStyle dateStyle = styles.get("datetime");
			ClassCase dataRow;
			for (int j = 0; j < tblCases.getRowCount(); j++) {
				i = tblCases.convertRowIndexToModel(j);
				dataRow = dataCases.get(i);
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue(dataRow.caseNo);
				cell = row.createCell(1);
				cell.setCellValue(dataRow.calFinal);
				cell.setCellStyle(dateStyle);
				cell = row.createCell(2);
				cell.setCellValue(dataRow.specialty);
				cell = row.createCell(3);
				cell.setCellValue(dataRow.subspecialty);
				cell = row.createCell(4);
				cell.setCellValue(dataRow.procedure);
				cell = row.createCell(5);
				cell.setCellValue(dataRow.staff);
				cell = row.createCell(6);
				cell.setCellValue(dataRow.value1);
				cell = row.createCell(7);
				cell.setCellValue(dataRow.value2);
				cell = row.createCell(8);
				cell.setCellValue(dataRow.value3);
				cell = row.createCell(9);
				cell.setCellValue(dataRow.value4);
				cell = row.createCell(10);
				cell.setCellValue(dataRow.noSpecimens);
				cell = row.createCell(11);
				cell.setCellValue(dataRow.noBlocks);
				cell = row.createCell(12);
				cell.setCellValue(dataRow.noSlides);
				cell = row.createCell(13);
				cell.setCellValue(dataRow.noSynoptics);
				cell = row.createCell(14);
				cell.setCellValue(dataRow.noFS);
			}
			sheet.createFreezePane(1, 2);
			// Write the output to a file
			FileOutputStream out = new FileOutputStream(fileName);
			wb.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} catch (IOException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} catch (Exception e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}

	private class ClassCase {
		byte noSynoptics = 0;
		short noSpecimens = 0;
		short noBlocks = 0;
		short noSlides = 0;
		short noFS = 0;
		long caseID = 0;
		double value1 = 0;
		double value2 = 0;
		double value3 = 0;
		double value4 = 0;
		Calendar calFinal = Calendar.getInstance();
		String caseNo = "";
		String staff = "";
		String specialty = "";
		String subspecialty = "";
		String procedure = "";
	}

	private class ModelCases extends AbstractTableModel {
		private static final long serialVersionUID = -5335300411076285900L;
		private final String[] columns = {"CASENO", "FINAL", "SPY", "SUB",
			"PROC", "STAFF", parent.variables.codersName[0], parent.variables.codersName[1],
			parent.variables.codersName[2], parent.variables.codersName[3],
			"SPECS", "BLK", "SLD", "SYNOP", "FSEC"};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return dataCases.size();
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			ClassCase thisRow = new ClassCase();
			if (row < dataCases.size()) {
				thisRow = dataCases.get(row);
			}
			switch (col) {
			case DATA_CASE_CODE4:
				value = thisRow.value4;
				break;
			case DATA_CASE_CODE3:
				value = thisRow.value3;
				break;
			case DATA_CASE_CODE2:
				value = thisRow.value2;
				break;
			case DATA_CASE_CODE1:
				value = thisRow.value1;
				break;
			case DATA_CASE_NOBLOCKS:
				value = thisRow.noBlocks;
				break;
			case DATA_CASE_NOSLIDES:
				value = thisRow.noSlides;
				break;
			case DATA_CASE_NOSPECS:
				value = thisRow.noSpecimens;
				break;
			case DATA_CASE_NOSYNOPTICS:
				value = thisRow.noSynoptics;
				break;
			case DATA_CASE_NOFS:
				value = thisRow.noFS;
				break;
			case DATA_CASE_SPECIALTY:
				value = thisRow.specialty;
				break;
			case DATA_CASE_SUBSPECIALTY:
				value = thisRow.subspecialty;
				break;
			case DATA_CASE_PROCEDURE:
				value = thisRow.procedure;
				break;
			case DATA_CASE_STAFF:
				value = thisRow.staff;
				break;
			case DATA_CASE_FINAL_DATE:
				value = thisRow.calFinal;
				break;
			default:
				value = thisRow.caseNo;
			}
			return value;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_CASE_FINAL_DATE:
				return Calendar.class;
			case DATA_CASE_CODE4:
			case DATA_CASE_CODE3:
			case DATA_CASE_CODE2:
			case DATA_CASE_CODE1:
				return Double.class;
			case DATA_CASE_NOBLOCKS:
			case DATA_CASE_NOSLIDES:
			case DATA_CASE_NOSPECS:
			case DATA_CASE_NOSYNOPTICS:
			case DATA_CASE_NOFS:
				return Integer.class;
			default:
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			// This table is not editable
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Ignore, table not editable
		}
	}

	private class ClassSpecimen {
		long specID = 0;
		short noBlocks = 0;
		short noSlides = 0;
		short noFrags = 0;
		double value1 = 0;
		double value2 = 0;
		double value3 = 0;
		double value4 = 0;
		DataItem master = new DataItem(0, "");
		String description = "";
	}

	private class ModelSpecimens extends AbstractTableModel {
		private static final long serialVersionUID = 8045499089754301496L;
		private final String[] columns = {"CODE", "DESCR",
				parent.variables.codersName[0], parent.variables.codersName[1],
				parent.variables.codersName[2], parent.variables.codersName[3],
				"BLK", "SLD", "FRG"};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return dataSpecs.size();
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			ClassSpecimen thisRow = new ClassSpecimen();
			try {
				if (row < dataSpecs.size()) {
					thisRow = dataSpecs.get(row);
				}
				switch (col) {
				case DATA_SPEC_CODE4:
					value = thisRow.value4;
					break;
				case DATA_SPEC_CODE3:
					value = thisRow.value3;
					break;
				case DATA_SPEC_CODE2:
					value = thisRow.value2;
					break;
				case DATA_SPEC_CODE1:
					value = thisRow.value1;
					break;
				case DATA_SPEC_NOBLOCKS:
					value = thisRow.noBlocks;
					break;
				case DATA_SPEC_NOSLIDES:
					value = thisRow.noSlides;
					break;
				case DATA_SPEC_NOFRAGS:
					value = thisRow.noFrags;
					break;
				case DATA_SPEC_DESCR:
					value = thisRow.description;
					break;
				default:
					value = thisRow.master;
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return value;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_SPEC_CODE4:
			case DATA_SPEC_CODE3:
			case DATA_SPEC_CODE2:
			case DATA_SPEC_CODE1:
				return Double.class;
			case DATA_SPEC_NOBLOCKS:
			case DATA_SPEC_NOSLIDES:
			case DATA_SPEC_NOFRAGS:
				return Integer.class;
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
				if (!dataSpecs.get(row).master.equals((DataItem) value)) {
					dataSpecs.get(row).master = (DataItem) value;
					saveSpecimen(row);
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
		}
	}

	private class ClassOrder {
		short qty = 0;
		double value1 = 0;
		double value2 = 0;
		double value3 = 0;
		double value4 = 0;
		String name = "";
	}

	private class ModelOrders extends AbstractTableModel {
		private static final long serialVersionUID = 8045499089754301496L;
		private final String[] columns = {"CODE", "QTY",
				parent.variables.codersName[0], parent.variables.codersName[1],
				parent.variables.codersName[2], parent.variables.codersName[3]};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return dataOrders.size();
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			ClassOrder thisRow = new ClassOrder();
			try {
				if (row < dataOrders.size()) {
					thisRow = dataOrders.get(row);
				}
				switch (col) {
				case DATA_ORDER_CODE4:
					value = thisRow.value4;
					break;
				case DATA_ORDER_CODE3:
					value = thisRow.value3;
					break;
				case DATA_ORDER_CODE2:
					value = thisRow.value2;
					break;
				case DATA_ORDER_CODE1:
					value = thisRow.value1;
					break;
				case DATA_ORDER_QTY:
					value = thisRow.qty;
					break;
				default:
					value = thisRow.name;
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return value;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_ORDER_CODE4:
			case DATA_ORDER_CODE3:
			case DATA_ORDER_CODE2:
			case DATA_ORDER_CODE1:
				return Double.class;
			case DATA_ORDER_QTY:
				return Short.class;
			default:
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Table is not editable
		}
	}
	
	private class ClassAdditional {
		byte codeID = 0;
		short noSpecimens = 0;
		short noBlocks = 0;
		short noSlides = 0;
		double value1 = 0;
		double value2 = 0;
		double value3 = 0;
		double value4 = 0;
		DataItem staff = new DataItem(0, "");
		Date date = new Date();
	}

	private class ModelAdditional extends AbstractTableModel {
		private static final long serialVersionUID = -3684428960681314315L;
		private final String[] columns = {"CODE", "STAFF", "DATE", "SPECS", "BLK", "SLD",
				parent.variables.codersName[0], parent.variables.codersName[1],
				parent.variables.codersName[2], parent.variables.codersName[3]};
		private final String[] codes = {"FSEC", "AMND", "ADDN", "CORR", "REVW"};

		public int getColumnCount() {
			return columns.length;
		}

		public int getRowCount() {
			return dataAdditional.size();
		}

		public String getColumnName(int col) {
			return columns[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			ClassAdditional thisRow = new ClassAdditional();
			try {
				if (row < dataAdditional.size()) {
					thisRow = dataAdditional.get(row);
				}
				switch (col) {
				case DATA_ADDL_CODE4:
					value = thisRow.value4;
					break;
				case DATA_ADDL_CODE3:
					value = thisRow.value3;
					break;
				case DATA_ADDL_CODE2:
					value = thisRow.value2;
					break;
				case DATA_ADDL_CODE1:
					value = thisRow.value1;
					break;
				case DATA_ADDL_NOSPECS:
					value = thisRow.noSpecimens;
					break;
				case DATA_ADDL_NOBLOCKS:
					value = thisRow.noBlocks;
					break;
				case DATA_ADDL_NOSLIDES:
					value = thisRow.noSlides;
					break;
				case DATA_ADDL_DATE:
					value = thisRow.date;
					break;
				case DATA_ADDL_STAFF:
					value = thisRow.staff;
					break;
				default:
					value = codes[thisRow.codeID];
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return value;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_ADDL_CODE4:
			case DATA_ADDL_CODE3:
			case DATA_ADDL_CODE2:
			case DATA_ADDL_CODE1:
				return Double.class;
			case DATA_ADDL_NOSPECS:
			case DATA_ADDL_NOBLOCKS:
			case DATA_ADDL_NOSLIDES:
				return Short.class;
			case DATA_ADDL_DATE:
				return Date.class;
			default:
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			ClassAdditional thisRow = new ClassAdditional();
			if (row < dataAdditional.size()) {
				thisRow = dataAdditional.get(row);
			}
			// Only Staff name is editable if connected to update in PowerPath & FSEC row
			return (canEdit && col == DATA_ADDL_STAFF && thisRow.codeID == 0);
		}

		public void setValueAt(Object value, int row, int col) {
			try {
				if (!dataAdditional.get(row).staff.equals((DataItem) value)) {
					dataAdditional.get(row).staff = (DataItem) value;
					saveStaff(row);
				}
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
			lngCaseID = 0;
			dataSpecs.clear();
			dataOrders.clear();
			((AbstractTableModel) tblCases.getModel()).fireTableDataChanged();
			((AbstractTableModel) tblSpecimens.getModel()).fireTableDataChanged();
			((AbstractTableModel) tblOrders.getModel()).fireTableDataChanged();
			((AbstractTableModel) tblAdditionals.getModel()).fireTableDataChanged();
			parent.statusBar.setMessage("No rows " + dataCases.size());
			if (dataCases.size() > 0) {
				// Else, index out of range error
				// Display results
				tblCases.setRowSelectionInterval(0,0);
				lngCaseID = dataCases.get(0).caseID;
				readSpecimens();
			}
		}
	}
}
