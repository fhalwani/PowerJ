package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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

class PnlFrozens extends PnlMain implements ListSelectionListener {
	private static final long serialVersionUID = 209147342323432692L;
	private final byte DATA_CASE_NO = 0;
	private final byte DATA_CASE_DATE = 1;
	private final byte DATA_CASE_SUBSPECIALTY = 2;
	private final byte DATA_CASE_PROCEDURE = 3;
	private final byte DATA_CASE_STAFF = 4;
	private final byte DATA_CASE_NOSPECS = 5;
	private final byte DATA_CASE_NOFS = 6;
	private final byte DATA_CASE_NOBLOCKS = 7;
	private final byte DATA_CASE_NOSLIDES = 8;
	private final byte DATA_CASE_CODE1 = 9;
	private final byte DATA_CASE_CODE2 = 10;
	private final byte DATA_CASE_CODE3 = 11;
	private final byte DATA_CASE_CODE4 = 12;
	private final byte DATA_SPEC_CODE = 0;
	private final byte DATA_SPEC_DESCR = 1;
	private final byte DATA_SPEC_NOBLOCKS = 2;
	private final byte DATA_SPEC_NOSLIDES = 3;
	private final byte DATA_SPEC_CODE1 = 4;
	private final byte DATA_SPEC_CODE2 = 5;
	private final byte DATA_SPEC_CODE3 = 6;
	private final byte DATA_SPEC_CODE4 = 7;
	private boolean canEdit = false;
	private int[] filters = {0, 0, 0, 0};
	private long lngCaseID = 0;
	private JTableEditor tblCases, tblSpecimens;
	private ModelCases mdlCases;
	private ModelSpecimens mdlSpecimens;
	private DbAPIS dbAP;
	private ArrayList<ClassCase> cases = new ArrayList<ClassCase>();
	private ArrayList<ClassSpecimen> specimens = new ArrayList<ClassSpecimen>();
	
	PnlFrozens(PowerJ parent) {
		super(parent);
		setName("Frozens");
		readTable();
		parent.dbPowerJ.prepareFrozens();
		if (!parent.variables.hasError) {
			if (!parent.variables.offLine) {
				dbAP = new DbAPIS(parent);
				if (dbAP.connected) {
					dbAP.prepareSpecimens();
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
		add(createToolbar(), BorderLayout.NORTH);
		add(boxPanel, BorderLayout.CENTER);
		parent.statusBar.setMessage("No rows " + cases.size());
		if (cases.size() > 0) {
			// Display results
			mdlCases.fireTableDataChanged();
			tblCases.setRowSelectionInterval(0,0);
			lngCaseID = cases.get(0).caseID;
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
		mdlCases = new ModelCases();
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
					if (lngCaseID != cases.get(modelRow).caseID) {
						lngCaseID = cases.get(modelRow).caseID;
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
		for (int i = DATA_SPEC_CODE4; i >= DATA_SPEC_CODE1; i--) {
			column = tblSpecimens.getColumnModel().getColumn(i);
			if (!parent.variables.codersActive[i - DATA_SPEC_CODE1]) {
				tblSpecimens.removeColumn(column);
			}
		}
		JScrollPane scroller = new JScrollPane(tblSpecimens,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroller.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = 8334954865866544843L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		panel.add(scroller);
		return panel;
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
		CboSubspecial cboSubspecial = new CboSubspecial(parent, false);
		cboSubspecial.addItemListener(new ItemListener() {
	        public void itemStateChanged(ItemEvent e) {
	    		if (e.getStateChange() == ItemEvent.SELECTED) {
	    			JComboBox cb = (JComboBox)e.getSource();
	    			DataItem item = (DataItem) cb.getSelectedItem();
	    			filters[1] = item.getValue();
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
		label = new JLabel("Subspecialty:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_B);
		label.setLabelFor(cboSubspecial);
		panel.add(label);
		panel.add(cboSubspecial);
		return panel;
	}

	void pdf() {
		String fileName = ((PJClient)parent).getFilePdf("frozens.pdf").trim();
		if (fileName.trim().length() == 0) return;
        final String[] headers = {"CASENO", "DATE", "SUB",
    			"PROC", "STAFF", "SPECS", "FS", "BLCK", "SLD",
    			parent.variables.codersName[0], parent.variables.codersName[1],
    			parent.variables.codersName[2], parent.variables.codersName[3]};
		final float[] widths = {2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
		int row = 0;
        String str = "Frozen Sections";
		PdfFonts pdfLib = new PdfFonts();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER.rotate(), 36, 36, 36, 36);
        Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		ClassCase dataRow;
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
			for (int j = 0; j < tblCases.getRowCount(); j++) {
				row = tblCases.convertRowIndexToModel(j);
				dataRow = cases.get(row);
				for (int i = 0; i < headers.length; i++) {
		            paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					switch (i) {
					case DATA_CASE_NO:
						str = dataRow.caseNo;
						break;
					case DATA_CASE_DATE:
						str = parent.dateUtils.formatter(dataRow.date, parent.dateUtils.FORMAT_DATE);
						break;
					case DATA_CASE_SUBSPECIALTY:
						str = dataRow.subspecialty;
						break;
					case DATA_CASE_PROCEDURE:
						str = dataRow.procedure;
						break;
					case DATA_CASE_STAFF:
						str = dataRow.staff;
						break;
					case DATA_CASE_NOSPECS:
						str = "" + dataRow.noSpecimens;
						break;
					case DATA_CASE_NOFS:
						str = "" + dataRow.noFS;
						break;
					case DATA_CASE_NOBLOCKS:
						str = "" + dataRow.noBlocks;
						break;
					case DATA_CASE_NOSLIDES:
						str = "" + dataRow.noSlides;
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
					default:
						str = parent.numbers.formatDouble(2, dataRow.value4);
					}
					paragraph.add(new Chunk(str));
					cell = new PdfPCell();
					switch (i) {
					case DATA_CASE_DATE:
					case DATA_CASE_NOSPECS:
					case DATA_CASE_NOBLOCKS:
					case DATA_CASE_NOSLIDES:
					case DATA_CASE_NOFS:
					case DATA_CASE_CODE1:
					case DATA_CASE_CODE2:
					case DATA_CASE_CODE3:
					case DATA_CASE_CODE4:
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
		cases.clear();
		// Must initialize a new instance each time
		DataWorker worker = new DataWorker();
		worker.execute();
	}
	
	private void readSpecimens() {
		ClassSpecimen specimen = new ClassSpecimen();
		PreparedStatement stm = null;
		ResultSet rst = null;
		try {
			specimens.clear();
			stm = parent.dbPowerJ.getStatement(0);
			stm.setLong(1, lngCaseID);
			rst = stm.executeQuery();
			while (rst.next()) {
				specimen = new ClassSpecimen();
				specimen.specID = rst.getLong("SPECID");
				specimen.noBlocks = rst.getShort("NOBLOCKS");
				specimen.noSlides = rst.getShort("NOSLIDES");
				specimen.value1 = rst.getDouble("VALUE1");
				specimen.value2 = rst.getDouble("VALUE2");
				specimen.value3 = rst.getDouble("VALUE3");
				specimen.value4 = rst.getDouble("VALUE4");
				specimen.description = rst.getString("DESCR");
				specimen.master = new DataItem(rst.getInt("MSID"),
						rst.getString("CODE"));
				specimens.add(specimen);
			}
			mdlSpecimens.fireTableDataChanged();
			if (specimens.size() > 0) {
				// Else, index out of range error
				tblSpecimens.setRowSelectionInterval(0,0);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
		}
	}
	
	private void readTable() {
		parent.setBusy(true);
		ClassCase thisCase = new ClassCase();
		ResultSet rst = parent.dbPowerJ.getFrozens(filters);
		try {
			while (rst.next()) {
				thisCase = new ClassCase();
				thisCase.caseID = rst.getLong("CASEID");
				thisCase.noSpecimens = rst.getShort("NOSPECS");
				thisCase.noBlocks = rst.getShort("NOBLOCKS");
				thisCase.noSlides = rst.getShort("NOSLIDES");
				thisCase.noFS = rst.getShort("NOFS");
				thisCase.value1 = rst.getDouble("VALUE1");
				thisCase.value2 = rst.getDouble("VALUE2");
				thisCase.value3 = rst.getDouble("VALUE3");
				thisCase.value4 = rst.getDouble("VALUE4");
				thisCase.date = rst.getTimestamp("ACCESSED");
				thisCase.caseNo = rst.getString("CASENO");
				thisCase.staff = rst.getString("INITIALS");
				thisCase.subspecialty = rst.getString("SUBINIT");
				thisCase.procedure = DataProcedure.NAMES[rst.getShort("PROID")];
				cases.add(thisCase);
			}
			parent.statusBar.setMessage("No rows " + cases.size());
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
			parent.setBusy(false);
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
	
	public void valueChanged(ListSelectionEvent e) {
        //Ignore extra messages
        if (e.getValueIsAdjusting()) return;
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (lsm.isSelectionEmpty()) return;
        int viewRow = lsm.getMinSelectionIndex();
        if (viewRow > -1) {
			// else, Selection got filtered away.
			int modelRow = tblCases.convertRowIndexToModel(viewRow);
			if (lngCaseID != cases.get(modelRow).caseID) {
				lngCaseID = cases.get(modelRow).caseID;
				if (lngCaseID > 0) {
			        readSpecimens();
				}
			}
        }
	}

	void xls() {
		String fileName = ((PJClient)parent).getFileXls("frozens.xlsx").trim();
		if (fileName.trim().length() == 0) return;
        final String[] headers = {"CASENO", "DATE", "SUB",
    			"PROC", "STAFF", "SPECS", "FS", "BLCK", "SLD",
    			parent.variables.codersName[0], parent.variables.codersName[1],
    			parent.variables.codersName[2], parent.variables.codersName[3]};
		ClassCase dataRow;
		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			XlsStyles xlsLib = new XlsStyles(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			XSSFSheet sheet = wb.createSheet("Frozens");
			// title row
			Row row = sheet.createRow(0);
			row.setHeightInPoints(45);
			Cell cell = row.createCell(0);
			cell.setCellValue("Frozen Sections");
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$M$1"));
			// header row
			row = sheet.createRow(1);
			row.setHeightInPoints(30);
			for (int i = 0; i < headers.length; i++) {
				cell = row.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(styles.get("header"));
				sheet.setColumnWidth(i, 15 * 256); // 15 characters
			}
			ColumnHelper colHelper = sheet.getColumnHelper();
			colHelper.setColDefaultStyle(0, styles.get("text"));
			colHelper.setColDefaultStyle(1, styles.get("date"));
			colHelper.setColDefaultStyle(2, styles.get("text"));
			colHelper.setColDefaultStyle(3, styles.get("text"));
			colHelper.setColDefaultStyle(4, styles.get("text"));
			colHelper.setColDefaultStyle(10, styles.get("data_int"));
			colHelper.setColDefaultStyle(11, styles.get("data_int"));
			colHelper.setColDefaultStyle(12, styles.get("data_int"));
			colHelper.setColDefaultStyle(13, styles.get("data_int"));
			colHelper.setColDefaultStyle(6, styles.get("data_double"));
			colHelper.setColDefaultStyle(7, styles.get("data_double"));
			colHelper.setColDefaultStyle(8, styles.get("data_double"));
			colHelper.setColDefaultStyle(9, styles.get("data_double"));
			// data rows
			int rownum = 2;
			int i = 0;
			CellStyle dateStyle = styles.get("date");
			for (int j = 0; j < tblCases.getRowCount(); j++) {
				i = tblCases.convertRowIndexToModel(j);
				dataRow = cases.get(i);
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue(dataRow.caseNo);
				cell = row.createCell(1);
				cell.setCellValue(dataRow.date);
				cell.setCellStyle(dateStyle);
				cell = row.createCell(2);
				cell.setCellValue(dataRow.subspecialty);
				cell = row.createCell(3);
				cell.setCellValue(dataRow.procedure);
				cell = row.createCell(4);
				cell.setCellValue(dataRow.staff);
				cell = row.createCell(5);
				cell.setCellValue(dataRow.noSpecimens);
				cell = row.createCell(6);
				cell.setCellValue(dataRow.noFS);
				cell = row.createCell(7);
				cell.setCellValue(dataRow.noBlocks);
				cell = row.createCell(8);
				cell.setCellValue(dataRow.noSlides);
				cell = row.createCell(9);
				cell.setCellValue(dataRow.value1);
				cell = row.createCell(10);
				cell.setCellValue(dataRow.value2);
				cell = row.createCell(11);
				cell.setCellValue(dataRow.value3);
				cell = row.createCell(12);
				cell.setCellValue(dataRow.value4);
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
		short noSpecimens = 0;
		short noBlocks = 0;
		short noSlides = 0;
		short noFS = 0;
		long caseID = 0;
		double value1 = 0;
		double value2 = 0;
		double value3 = 0;
		double value4 = 0;
		Date date = new Date();
		String caseNo = "";
		String staff = "";
		String subspecialty = "";
		String procedure = "";
	}

	private class ModelCases extends AbstractTableModel {
		private static final long serialVersionUID = -5335300411076285900L;
		private final String[] columns = {"CASENO", "DATE", "SUB",
    			"PROC", "STAFF", "SPECS", "FS", "BLCKS", "SLIDES",
			parent.variables.codersName[0], parent.variables.codersName[1],
			parent.variables.codersName[2], parent.variables.codersName[3]};

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
					ClassCase thisCase = cases.get(row);
					switch (col) {
					case DATA_CASE_CODE4:
						objValue = thisCase.value4;
						break;
					case DATA_CASE_CODE3:
						objValue = thisCase.value3;
						break;
					case DATA_CASE_CODE2:
						objValue = thisCase.value2;
						break;
					case DATA_CASE_CODE1:
						objValue = thisCase.value1;
						break;
					case DATA_CASE_NOSLIDES:
						objValue = thisCase.noSlides;
						break;
					case DATA_CASE_NOBLOCKS:
						objValue = thisCase.noBlocks;
						break;
					case DATA_CASE_NOFS:
						objValue = thisCase.noFS;
						break;
					case DATA_CASE_NOSPECS:
						objValue = thisCase.noSpecimens;
						break;
					case DATA_CASE_STAFF:
						objValue = thisCase.staff;
						break;
					case DATA_CASE_PROCEDURE:
						objValue = thisCase.procedure;
						break;
					case DATA_CASE_SUBSPECIALTY:
						objValue = thisCase.subspecialty;
						break;
					case DATA_CASE_DATE:
						objValue = thisCase.date;
						break;
					default:
						objValue = thisCase.caseNo;
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return objValue;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_CASE_DATE:
				return Date.class;
			case DATA_CASE_CODE4:
			case DATA_CASE_CODE3:
			case DATA_CASE_CODE2:
			case DATA_CASE_CODE1:
				return Double.class;
			case DATA_CASE_NOBLOCKS:
			case DATA_CASE_NOSLIDES:
			case DATA_CASE_NOFS:
			case DATA_CASE_NOSPECS:
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
		double value1 = 0;
		double value2 = 0;
		double value3 = 0;
		double value4 = 0;
		DataItem master = new DataItem(0, "");
		String description = "";
	}

	private class ModelSpecimens extends AbstractTableModel {
		private static final long serialVersionUID = 8045499089754301496L;
		private final String[] columns = {" CODE ", " DESCR ", " BLCKS ", " SLIDES ",
				parent.variables.codersName[0], parent.variables.codersName[1],
				parent.variables.codersName[2], parent.variables.codersName[3]};

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
					case DATA_SPEC_CODE4:
						objValue = thisSpecimen.value4;
						break;
					case DATA_SPEC_CODE3:
						objValue = thisSpecimen.value3;
						break;
					case DATA_SPEC_CODE2:
						objValue = thisSpecimen.value2;
						break;
					case DATA_SPEC_CODE1:
						objValue = thisSpecimen.value1;
						break;
					case DATA_SPEC_NOBLOCKS:
						objValue = thisSpecimen.noBlocks;
						break;
					case DATA_SPEC_NOSLIDES:
						objValue = thisSpecimen.noSlides;
						break;
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
			case DATA_SPEC_CODE4:
			case DATA_SPEC_CODE3:
			case DATA_SPEC_CODE2:
			case DATA_SPEC_CODE1:
				return Double.class;
			case DATA_SPEC_NOBLOCKS:
			case DATA_SPEC_NOSLIDES:
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
				specimens.get(row).master = (DataItem) value;
				save(row);
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
			specimens.clear();
			parent.statusBar.setMessage("No rows " + cases.size());
			if (cases.size() > 0) {
				// Else, index out of range error
				// Display results
				tblCases.setRowSelectionInterval(0,0);
				lngCaseID = cases.get(0).caseID;
				readSpecimens();
			}
			mdlCases.fireTableDataChanged();
			mdlSpecimens.fireTableDataChanged();
		}
	}
}
