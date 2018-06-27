package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
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

class PnlScanning extends PnlMain {
	private static final long serialVersionUID = -9196274136686335449L;
	private final byte DATA_COL_NO = 0;
	private final byte DATA_CASES = 1;
	private final byte DATA_DATES = 2;
	private final byte DATA_NAMES = 3;
	private long timeFrom = 0;
	private long timeTo = 0;
	private String infoUpdate = "No rows ";
	private ArrayList<DataRow> dataRows = new ArrayList<DataRow>();
	private JTableEditor tblCases;
	private DbAPIS dbAP;

	PnlScanning(PowerJ parent) {
		super(parent);
		setName("Scanning");
		createPanel();
		if (!parent.variables.offLine) {
			dbAP = new DbAPIS(parent);
			if (dbAP.connected) {
				dbAP.prepareUnscanned();
			}
		}
	}

	boolean close() {
		super.close();
		dataRows.clear();
		if (!parent.variables.offLine) {
			if (dbAP.connected) {
				dbAP.close();
			}
		}
		return true;
	}

	private void createPanel() {
		setLayout(new BorderLayout());
		setOpaque(true);
		tblCases = new JTableEditor(parent, new DataModel());
		tblCases.setName("tblCases");
		TableColumn column = tblCases.getColumnModel().getColumn(DATA_COL_NO);
		column.setMinWidth(50);
		column.setMaxWidth(50);
		tblCases.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int row = ((JTable) e.getSource()).rowAtPoint(new Point(e.getX(), e.getY()));
		        displayCase(row);
			}
		});
		JScrollPane scrollCases = new JScrollPane(tblCases,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollCases.setColumnHeader(new JViewport() {
			private static final long serialVersionUID = -9024296715239285516L;
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 24;
				return d;
			}
		});
		add(createToolbar(), BorderLayout.NORTH);
		add(scrollCases, BorderLayout.CENTER);
	}
	
	private JPanel createToolbar() {
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		Calendar calMin = Calendar.getInstance();
		calMin.set(Calendar.YEAR, 2011);
		calMin.set(Calendar.DAY_OF_YEAR, 1);
		calMin.set(Calendar.HOUR_OF_DAY, 0);
		calMin.set(Calendar.MINUTE, 0);
		calMin.set(Calendar.SECOND, 0);
		calMin.set(Calendar.MILLISECOND, 1);
		Calendar calMax = Calendar.getInstance();
		// Must include today to include all cases till midnight yesterday
		calMax.set(Calendar.HOUR_OF_DAY, 0);
		calMax.set(Calendar.MINUTE, 0);
		calMax.set(Calendar.SECOND, 0);
		calMax.set(Calendar.MILLISECOND, 0);
		Calendar calStart = Calendar.getInstance();
		calStart.add(Calendar.MONTH, -1);
		calStart.set(Calendar.DAY_OF_MONTH, 1);
		calStart.set(Calendar.HOUR_OF_DAY, 0);
		calStart.set(Calendar.MINUTE, 0);
		calStart.set(Calendar.SECOND, 0);
		calStart.set(Calendar.MILLISECOND, 1);
		Calendar calEnd = Calendar.getInstance();
		calEnd.set(Calendar.DAY_OF_MONTH, 1);
		calEnd.set(Calendar.HOUR_OF_DAY, 0);
		calEnd.set(Calendar.MINUTE, 0);
		calEnd.set(Calendar.SECOND, 0);
		calEnd.set(Calendar.MILLISECOND, 1);
		timeFrom = calStart.getTimeInMillis();
		timeTo = calEnd.getTimeInMillis();
		CboDate cboStart = new CboDate(calStart, calMin, calMax);
		cboStart.setName("cboStart");
		//  This class handles the ancestorAdded event and invokes the requestFocusInWindow() method
		cboStart.addAncestorListener(new RequestFocusListener());
		cboStart.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CboDate cbo = (CboDate)e.getSource();
					Calendar cal = cbo.getValue();
					if (timeFrom != cal.getTimeInMillis()) {
						timeFrom = cal.getTimeInMillis();
						altered = true;
					}
				}
			}

		});
		JLabel label = new JLabel("From:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_F);
		label.setLabelFor(cboStart);
		panel.add(label);
		panel.add(cboStart);
		CboDate cboEnd = new CboDate(calEnd, calMin, calMax);
		cboEnd.setName("cboEnd");
		cboEnd.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CboDate cbo = (CboDate)e.getSource();
					Calendar cal = cbo.getValue();
					if (timeTo != cal.getTimeInMillis()) {
						timeTo = cal.getTimeInMillis();
						altered = true;
					}
				}
			}

		});
		label = new JLabel("To:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_T);
		label.setLabelFor(cboEnd);
		panel.add(label);
		panel.add(cboEnd);
		JButton btnGo = new JButton("Go");
		btnGo.setMnemonic(KeyEvent.VK_G);
		btnGo.setIcon(Utilities.getIcon("go"));
		btnGo.setFocusable(true);
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (altered) {
					setData();
				}
			}
		});
		panel.add(btnGo);
		return panel;
	}
	
	private void displayCase(int row) {
		if (parent.variables.offLine) return;
		if (parent.variables.busy.get()) return;
        if (row > -1) {
			// else, Selection got filtered away.
        	// Convert to model
			row = tblCases.convertRowIndexToModel(row);
			if (row > -1 && row < dataRows.size()) {
				new DlgCase(parent,
						dataRows.get(row).caseID,
						dataRows.get(row).caseNo);
			}
        }
	}

	void pdf() {
		String fileName = ((PJClient)parent).getFilePdf("scanner.pdf").trim();
		if (fileName.trim().length() == 0) return;
		final String[] headers = {"Case", "Date", "Staff"};
		float[] widths = {1, 1, 2};
		PdfFonts pdfLib = new PdfFonts();
		HashMap<String, Font> fonts = pdfLib.getFonts();
		Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);
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
            String str = "Cases Signed out without Scanning Slides";
            paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.add(new Chunk(str));
			document.add(paragraph);
			str = parent.dateUtils.formatter(timeFrom, parent.dateUtils.FORMAT_DATE) + " - "
					+ parent.dateUtils.formatter(timeTo, parent.dateUtils.FORMAT_DATE);
			paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font12"));
			paragraph.setAlignment(Element.ALIGN_CENTER);
			paragraph.add(new Chunk(str));
			document.add(paragraph);
            document.add(Chunk.NEWLINE);
			PdfPTable table = new PdfPTable(headers.length);
			table.setWidths(widths);
			for (int i = 0; i < headers.length; i++) {
	            paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(headers[i]));
				cell = new PdfPCell(paragraph);
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			DataRow dataRow;
			int row = 0;
			for (int j = 0; j < tblCases.getRowCount(); j++) {
				row = tblCases.convertRowIndexToModel(j);
				dataRow = dataRows.get(row);
				for (int i = 0; i < headers.length; i++) {
		            paragraph = new Paragraph();
					paragraph.setFont(fonts.get("Font10n"));
					paragraph.setAlignment(Element.ALIGN_LEFT);
					switch (i) {
					case 0:
						str = dataRow.caseNo;
						break;
					case 1:
						paragraph.setAlignment(Element.ALIGN_RIGHT);
						str = parent.dateUtils.formatter(dataRow.calDate, parent.dateUtils.FORMAT_DATETIME);
						break;
					default:
						str = dataRow.staff;
					}
					paragraph.add(new Chunk(str));
					cell = new PdfPCell(paragraph);
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

	private void setData() {
		if (parent.variables.busy.get()) return;
		parent.setBusy(true);
		// Must initialize a new instance each time
		DataWorker worker = new DataWorker();
		worker.execute();
	}

	void xls() {
		String fileName = ((PJClient)parent).getFileXls("scanner.xlsx").trim();
		if (fileName.trim().length() == 0) return;
		try {
			final String[] headers = {"Case", "Date", "Staff"};
			XSSFWorkbook wb = new XSSFWorkbook();
			XlsStyles xlsLib = new XlsStyles(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			XSSFSheet sheet = wb.createSheet("Scanner");
			// title row
			Row row = sheet.createRow(0);
			row.setHeightInPoints(45);
			Cell cell = row.createCell(0);
			cell.setCellValue("Cases Signed out without Scanning Slides");
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$A$1:$C$1"));
			row = sheet.createRow(1);
			row.setHeightInPoints(45);
			cell = row.createCell(0);
			cell.setCellValue(parent.dateUtils.formatter(timeFrom, parent.dateUtils.FORMAT_DATE) + " - "
					+ parent.dateUtils.formatter(timeTo, parent.dateUtils.FORMAT_DATE));
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(CellRangeAddress.valueOf("$A$2:$C$2"));
			// header row
			row = sheet.createRow(2);
			row.setHeightInPoints(40);
			for (int i = 0; i < headers.length; i++) {
				cell = row.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(styles.get("header"));
				switch (i) {
				case 0:
					sheet.setColumnWidth(i, 15 * 256); // 15 characters
					break;
				case 1:
					sheet.setColumnWidth(i, 20 * 256); // 20 characters
					break;
				default:
					sheet.setColumnWidth(i, 30 * 256); // 30 characters
				}
			}
			ColumnHelper colHelper = sheet.getColumnHelper();
			colHelper.setColDefaultStyle(0, styles.get("text"));
			colHelper.setColDefaultStyle(1, styles.get("datetime"));
			colHelper.setColDefaultStyle(2, styles.get("text"));
			int rownum = 3;
			int x = 0;
			CellStyle dateStyle = styles.get("datetime");
			DataRow dataRow;
			for (int i = 0; i < tblCases.getRowCount(); i++) {
				x = tblCases.convertRowIndexToModel(i);
				dataRow = dataRows.get(x);
				row = sheet.createRow(rownum++);
				cell = row.createCell(0);
				cell.setCellValue(dataRow.caseNo);
				cell = row.createCell(1);
				cell.setCellValue(dataRow.calDate.getTime());
				cell.setCellStyle(dateStyle);
				cell = row.createCell(2);
				cell.setCellValue(dataRow.staff);
			}
			sheet.createFreezePane(1, 3);
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

	class DataRow {
		int caseID = 0;
		String caseNo = "";
		String staff = "";
		Calendar calDate = Calendar.getInstance();
	}

	class DataModel extends AbstractTableModel {
		private static final long serialVersionUID = -3013135183204427318L;
		private final String[] columHeaders = {"No", "Case", "Date", "Staff"};

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
			DataRow dataRow = dataRows.get(row);
			Object returnValue = Object.class;
			switch (col) {
			case DATA_COL_NO:
				if (tblCases != null) {
					returnValue = tblCases.convertRowIndexToView(row) +1;
				}
				break;
			case DATA_NAMES:
				returnValue = dataRow.staff;
				break;
			case DATA_CASES:
				returnValue = dataRow.caseNo;
				break;
			case DATA_DATES:
				returnValue = dataRow.calDate;
				break;
			}
			return returnValue;
		}

		public Class<?> getColumnClass(int col) {
			switch (col) {
			case DATA_COL_NO:
				return Integer.class;
			case DATA_DATES:
				return Calendar.class;
			default:
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

	private class DataWorker extends SwingWorker<Void, Void> {
		protected Void doInBackground() throws Exception {
			setName("ScanWorker");
			if (!parent.variables.offLine) {
				if (dbAP.connected) {
					if (altered && timeTo > timeFrom) {
						readTable();
					}
				}
			}
			return null;
		}
		
		private void readTable() {
			DataRow dataRow = new DataRow();
			ResultSet rst = null;
			PreparedStatement stm = null;
			try {
				dataRows.clear();
				stm = dbAP.getStatement(0);
				stm.setTimestamp(1, new Timestamp(timeFrom));
				stm.setTimestamp(2, new Timestamp(timeTo));
				stm.setTimestamp(3, new Timestamp(timeFrom));
				stm.setTimestamp(4, new Timestamp(timeTo));
				rst = stm.executeQuery();
				while (rst.next()) {
					dataRow = new DataRow();
					dataRow.caseID = rst.getInt("id");
					dataRow.caseNo = rst.getString("accession_no");
					dataRow.staff = rst.getString("first_name").trim() +
							" " + rst.getString("last_name").trim();
					dataRow.calDate.setTimeInMillis(rst.getTimestamp("completed_date").getTime());
					dataRows.add(dataRow);
				}
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
			} catch (SQLException e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			} finally {
				infoUpdate = "No rows " + dataRows.size();
				parent.statusBar.setMessage(infoUpdate);
				dbAP.closeRst(rst);
			}
		}

		public void done() {
			altered = false;
			parent.setBusy(false);
			// Display results
			((AbstractTableModel) tblCases.getModel()).fireTableDataChanged();
		}
	}
}
