package ca.eorla.fhalwani.powerj;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

class PnlAdditionals extends PnlMain {
	private static final long serialVersionUID = -4708253749011295831L;
	private final byte DATA_CASENO = 0;
	private final byte DATA_DATE = 1;
	private final byte DATA_CODE = 2;
	private final byte DATA_STAFF = 3;
	private final byte DATA_CODE1 = 4;
	private final byte DATA_CODE2 = 5;
	private final byte DATA_CODE3 = 6;
	private final byte DATA_CODE4 = 7;
	private final byte FILTER_FACILITY = 0;
	private final byte FILTER_SPECIALTY = 1;
	private final byte FILTER_SUBSPECIAL = 2;
	private int[] filters = {0, 0, 0};
	private JTableEditor tblData;
	private ArrayList<DataRow> dataRows = new ArrayList<DataRow>();

	PnlAdditionals(PowerJ parent) {
		super(parent);
		setName("Additionals");
		readTable();
		createPanel();
	}

	boolean close() {
		super.close();
		dataRows.clear();
		return true;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		add(createToolbar(), BorderLayout.NORTH);
		tblData = new JTableEditor(parent, new ModelData());
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
		parent.statusBar.setMessage("No rows " + dataRows.size());
	}

	private JPanel createToolbar() {
		// Setup 3 JComboBox and fill with their data
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
		return panel;
	}

	void pdf() {
		String fileName = ((PJClient)parent).getFilePdf("additional.pdf").trim();
		if (fileName.trim().length() == 0) return;
        final String[] headers = {"CASE", "DATE", "CODE", "STAFF",
				parent.variables.codersName[0], parent.variables.codersName[1],
				parent.variables.codersName[2], parent.variables.codersName[3]};
		final String[] codes = {"FSEC", "AMND", "ADDN", "CORR", "REVW"};
		final float[] widths = {2, 2, 1, 1, 1, 1, 1, 1};
        String str = "Additional Work";
		PdfFonts pdfLib = new PdfFonts();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document();
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
			DataRow dataRow;
			int row = 0;
			for (int j = 0; j < tblData.getRowCount(); j++) {
				row = tblData.convertRowIndexToModel(j);
				dataRow = dataRows.get(row);
				for (int i = 0; i < headers.length; i++) {
		            paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					switch (i) {
					case DATA_CASENO:
						str = dataRow.caseNo;
						break;
					case DATA_DATE:
						str = parent.dateUtils.formatter(dataRow.date, parent.dateUtils.FORMAT_DATE);
						break;
					case DATA_CODE:
						str = codes[dataRow.codeID];
						break;
					case DATA_CODE1:
						str = parent.numbers.formatDouble(2, dataRow.value1);
						break;
					case DATA_CODE2:
						str = parent.numbers.formatDouble(2, dataRow.value2);
						break;
					case DATA_CODE3:
						str = parent.numbers.formatDouble(2, dataRow.value3);
						break;
					case DATA_CODE4:
						str = parent.numbers.formatDouble(2, dataRow.value4);
						break;
					default:
						str = dataRow.staff;
					}
					paragraph.add(new Chunk(str));
					cell = new PdfPCell();
					switch (i) {
					case DATA_DATE:
					case DATA_CODE1:
					case DATA_CODE2:
					case DATA_CODE3:
					case DATA_CODE4:
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
		dataRows.clear();
		// Must initialize a new instance each time
		DataWorker worker = new DataWorker();
		worker.execute();
	}
	
	private void readTable() {
		parent.setBusy(true);
		DataRow row = new DataRow();
		ResultSet rst = parent.dbPowerJ.getAdditionals(filters);
		try {
			while (rst.next()) {
				row = new DataRow();
				if (rst.getShort("CODEID") > 4) {
					// Reviews
					row.codeID = 4;
				} else {
					row.codeID = rst.getByte("CODEID");
				}
				row.value1 = rst.getDouble("VALUE1");
				row.value2 = rst.getDouble("VALUE2");
				row.value3 = rst.getDouble("VALUE3");
				row.value4 = rst.getDouble("VALUE4");
				row.caseNo = rst.getString("CASENO");
				row.staff = rst.getString("INITIALS");
				row.date = rst.getTimestamp("FINALED");
				dataRows.add(row);
			}
		} catch (SQLException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} finally {
			parent.dbPowerJ.closeRst(rst);
			parent.dbPowerJ.closeStm();
			parent.setBusy(false);
		}
	}

	void xls() {
		String fileName = ((PJClient)parent).getFileXls("additional.xlsx").trim();
		if (fileName.trim().length() == 0) return;
		final String[] headers = {"CASE", "DATE", "CODE", "STAFF",
				parent.variables.codersName[0], parent.variables.codersName[1],
				parent.variables.codersName[2], parent.variables.codersName[3]};
		final String[] codes = {"FSEC", "AMND", "ADDN", "CORR", "REVW"};
		try {
			XSSFWorkbook wb = new XSSFWorkbook();
			XlsStyles xlsLib = new XlsStyles(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			XSSFSheet sheet = wb.createSheet("Additional");
			// title row
			Row row = sheet.createRow(0);
			row.setHeightInPoints(45);
			Cell cell = row.createCell(0);
			cell.setCellValue("Additional Work");
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$H$1"));
			// header row
			row = sheet.createRow(1);
			row.setHeightInPoints(30);
			for (int i = 0; i < headers.length; i++) {
				cell = row.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(styles.get("header"));
				sheet.setColumnWidth(i, 13 * 256); // 13 characters
			}
			ColumnHelper colHelper = sheet.getColumnHelper();
			colHelper.setColDefaultStyle(0, styles.get("text"));
			colHelper.setColDefaultStyle(1, styles.get("date"));
			colHelper.setColDefaultStyle(2, styles.get("text"));
			colHelper.setColDefaultStyle(3, styles.get("text"));
			colHelper.setColDefaultStyle(4, styles.get("data_double"));
			colHelper.setColDefaultStyle(5, styles.get("data_double"));
			colHelper.setColDefaultStyle(6, styles.get("data_double"));
			colHelper.setColDefaultStyle(7, styles.get("data_double"));
			// data rows
			int rownum = 2;
			int i = 0;
			DataRow dataRow;
			CellStyle dateStyle = styles.get("date");
			for (int j = 0; j < tblData.getRowCount(); j++) {
				i = tblData.convertRowIndexToModel(j);
				dataRow = dataRows.get(i);
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue(dataRow.caseNo);
				cell = row.createCell(1);
				cell.setCellValue(dataRow.date);
				cell.setCellStyle(dateStyle);
				cell = row.createCell(2);
				cell.setCellValue(codes[dataRow.codeID]);
				cell = row.createCell(3);
				cell.setCellValue(dataRow.staff);
				cell = row.createCell(4);
				cell.setCellValue(dataRow.value1);
				cell = row.createCell(5);
				cell.setCellValue(dataRow.value2);
				cell = row.createCell(6);
				cell.setCellValue(dataRow.value3);
				cell = row.createCell(7);
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

	private class DataRow {
		byte codeID = 0;
		double value1 = 0;
		double value2 = 0;
		double value3 = 0;
		double value4 = 0;
		Date date = new Date();
		String caseNo = "";
		String staff = "";
	}

	private class ModelData extends AbstractTableModel {
		private static final long serialVersionUID = 915332841823693483L;
		private final String[] columHeaders = {"CASE", "DATE", "CODE", "STAFF",
				parent.variables.codersName[0], parent.variables.codersName[1],
				parent.variables.codersName[2], parent.variables.codersName[3]};
		private final String[] codes = {"FSEC", "AMND", "ADDN", "CORR", "REVW"};

		public int getColumnCount() {
			return columHeaders.length;
		}

		public int getRowCount() {
			return dataRows.size();
		}

		public String getColumnName(int col) {
			return columHeaders[col];
		}

		public Object getValueAt(int row, int col) {
			Object value = Object.class;
			try {
				if (row < dataRows.size()) {
					DataRow thisRow = dataRows.get(row);
					switch (col) {
					case DATA_CASENO:
						value = thisRow.caseNo;
						break;
					case DATA_DATE:
						value = thisRow.date;
						break;
					case DATA_CODE:
						value = codes[thisRow.codeID];
						break;
					case DATA_STAFF:
						value = thisRow.staff;
						break;
					case DATA_CODE1:
						value = thisRow.value1;
						break;
					case DATA_CODE2:
						value = thisRow.value2;
						break;
					case DATA_CODE3:
						value = thisRow.value3;
						break;
					default:
						value = thisRow.value4;
					}
				}
			} catch (Exception e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			}
			return value;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_DATE:
				return Date.class;
			case DATA_CODE1:
			case DATA_CODE2:
			case DATA_CODE3:
			case DATA_CODE4:
				return Double.class;
			default:
				return String.class;
			}
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		public void setValueAt(Object value, int row, int col) {
			// Ignore
		}
	}

	private class DataWorker extends SwingWorker<Void, Void> {

		protected Void doInBackground() throws Exception {
			readTable();
			return null;
		}
		
		public void done() {
			parent.statusBar.setMessage("No rows " + dataRows.size());
			((AbstractTableModel) tblData.getModel()).fireTableDataChanged();
		}
	}
}
