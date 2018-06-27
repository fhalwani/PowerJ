package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
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

class PnlTat extends PnlMain implements KeyListener {
	private static final long serialVersionUID = 4916901844306050502L;
	//private final byte DATA_NAME = 0;
	private final byte DATA_CASES = 1;
	private final byte DATA_TAT_GROSS = 2;
	private final byte DATA_TAT_HISTO = 3;
	private final byte DATA_TAT_DIAGN = 4;
	private final byte DATA_TAT_TOTAL = 5;
	private AtomicBoolean datesChanged = new AtomicBoolean(true);
	private AtomicBoolean rowsChanged = new AtomicBoolean(false);
	private int[] rowsView = {Constants.ROW_FACILITY, Constants.ROW_SPECIALTY,
			Constants.ROW_SUBSPECIALTY, Constants.ROW_PROCEDURE, Constants.ROW_STAFF};
	private long timeFrom = 0;
	private long timeTo = 0;
	private String infoUpdate = "No rows ";
	private ChartGauge chartGross;
	private ChartGauge chartHisto;
	private ChartGauge chartDiagnosis;
	private ChartGauge chartTotal;
	private JTreeTable treeTable;
	private TatModel treeTableModel;
	private ArrayList<DataRow> dataRows = new ArrayList<DataRow>();
	private TreePath treePath;

	PnlTat(PowerJ parent) {
		super(parent);
		setName("TAT");
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
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		TreeTableNode nodeRoot = new TreeTableNode("Total");
		treeTableModel = new TatModel(nodeRoot);
		treeTable = new JTreeTable(parent, treeTableModel);
		treeTable.setFocusable(true);
		treeTable.addKeyListener(this);
		treeTable.tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath newPath = e.getNewLeadSelectionPath();
				if (newPath != null) {
					if (treePath == null || !treePath.equals(newPath)) {
						treePath = newPath;
						setCharts();
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
		Utilities.addComponent(scrollPane, 0, 0, 4, 3, 1.0, 0.75,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		chartGross = new ChartGauge();
		chartGross.setBorder(border);
		Utilities.addComponent(chartGross, 0, 3, 1, 1, 0.25, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		chartHisto = new ChartGauge();
		chartHisto.setBorder(border);
		Utilities.addComponent(chartHisto, 1, 3, 1, 1, 0.25, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		chartDiagnosis = new ChartGauge();
		chartDiagnosis.setBorder(border);
		Utilities.addComponent(chartDiagnosis, 2, 3, 1, 1, 0.25, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		chartTotal = new ChartGauge();
		chartTotal.setBorder(border);
		Utilities.addComponent(chartTotal, 3, 3, 1, 1, 0.25, 0.25,
				GridBagConstraints.BOTH, GridBagConstraints.EAST, panel);
		add(createToolbar(), BorderLayout.NORTH);
		add(panel, BorderLayout.CENTER);
	}
	
	private JPanel createToolbar() {
		JPanel panel = new JPanel();
		panel.setName("Toolbar");
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		Border border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
		timeFrom = parent.variables.minWorkloadDate;
		Calendar calMin = Calendar.getInstance();
		calMin.setTimeInMillis(timeFrom);
		Calendar calMax = Calendar.getInstance();
		// Must include today to include all cases till midnight yesterday
		calMax.set(Calendar.HOUR_OF_DAY, 0);
		calMax.set(Calendar.MINUTE, 0);
		calMax.set(Calendar.SECOND, 0);
		calMax.set(Calendar.MILLISECOND, 0);
		Calendar calStart = Calendar.getInstance();
		calStart.setTimeInMillis(calMin.getTimeInMillis());
		timeTo = calMax.getTimeInMillis();
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
						datesChanged.set(true);
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
		Calendar calEnd = Calendar.getInstance();
		calEnd.setTimeInMillis(calMax.getTimeInMillis());
		CboDate cboEnd = new CboDate(calEnd, calMin, calMax);
		cboEnd.setName("cboEnd");
		cboEnd.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CboDate cbo = (CboDate)e.getSource();
					Calendar cal = cbo.getValue();
					if (timeTo != cal.getTimeInMillis()) {
						timeTo = cal.getTimeInMillis();
						datesChanged.set(true);
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
		CboRows cboRows = new CboRows(rowsView);
		cboRows.setName("cboRows");
		cboRows.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					CboRows cbo = (CboRows)e.getSource();
					int[] newValue = cbo.getValue();
					if (!rowsView.equals(newValue)) {
						for (int i = 0; i < rowsView.length; i++) {
							rowsView[i] = newValue[i];
						}
						rowsChanged.set(true);
					}
				}
			}

		});
		label = new JLabel("Rows:");
		label.setFont(Constants.APP_FONT);
		label.setBorder(border);
		label.setDisplayedMnemonic(KeyEvent.VK_R);
		label.setLabelFor(cboRows);
		panel.add(label);
		panel.add(cboRows);
		JButton btnGo = new JButton("Go");
		btnGo.setMnemonic(KeyEvent.VK_G);
		btnGo.setIcon(Utilities.getIcon("go"));
		btnGo.setFocusable(true);
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (datesChanged.get() || rowsChanged.get()) {
					setData();
				}
			}
		});
		panel.add(btnGo);
		return panel;
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

	public void keyReleased(KeyEvent ignore) {}
	public void keyTyped(KeyEvent ignore) {}

	void pdf() {
		String fileName = ((PJClient)parent).getFilePdf("turnaround.pdf").trim();
		if (fileName.trim().length() == 0) return;
		final String[] header = {"Cases", "Gross", "Histo", "Report", "Total"};
        String str = "Turnaround Time";
		ArrayList<XlsCol> xlsCols = new ArrayList<XlsCol>();
		XlsCol xlsCol;
		try {
			for (int i = 0; i < rowsView.length; i++) {
				if (rowsView[i] > 0) {
					switch (rowsView[i]) {
					case Constants.ROW_FACILITY:
						str = "Fac";
						break;
					case Constants.ROW_SPECIALTY:
						str = "Spy";
						break;
					case Constants.ROW_SUBSPECIALTY:
						str = "Sub";
						break;
					case Constants.ROW_PROCEDURE:
						str = "Proc";
						break;
					default:
						str = "Staff";
					}
					xlsCol = new XlsCol(i, 0, str);
					xlsCols.add(xlsCol);
				}
			}
			int noColNames = xlsCols.size();
			for (int i = 0; i < header.length; i++) {
				xlsCol = new XlsCol(noColNames+i, 1, header[i]);
				xlsCols.add(xlsCol);
			}
			int noCols = xlsCols.size();
			float[] widths = new float[noCols];
			for (int i = 0; i < noCols; i++) {
				widths[i] = 1f;
			}
			PdfFonts pdfLib = new PdfFonts();
			HashMap<String, Font> fonts = pdfLib.getFonts();
			Document document = new Document(PageSize.LETTER.rotate(), 36, 36, 36, 36);
			if (noCols < 11) {
				// Switch from Landscape to Portrait
				document = new Document(PageSize.LETTER, 36, 36, 36, 36);
			}
	        Paragraph paragraph = new Paragraph();
			PdfPCell cell = new PdfPCell();
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
	        str = "Workload Summary";
			paragraph.add(new Chunk(str));
            document.add(paragraph);
			str = parent.dateUtils.formatter(timeFrom, parent.dateUtils.FORMAT_DATE) + " - "
					+ parent.dateUtils.formatter(timeTo, parent.dateUtils.FORMAT_DATE);
			paragraph.add(new Chunk(str));
            document.add(paragraph);
            document.add(Chunk.NEWLINE);
			PdfPTable table = new PdfPTable(noCols);
			table.setWidthPercentage(100);
			table.setWidths(widths);
			// header row
			for (int i = 0; i < noCols; i++) {
				xlsCol = xlsCols.get(i);
				str = xlsCol.name;
	            paragraph = new Paragraph();
				paragraph.setFont(fonts.get("Font10b"));
				paragraph.setAlignment(Element.ALIGN_CENTER);
				paragraph.add(new Chunk(str));
				cell = new PdfPCell();
				cell.setHorizontalAlignment(Element.ALIGN_CENTER);
				cell.setBackgroundColor(BaseColor.YELLOW);
				cell.addElement(paragraph);
				table.addCell(cell);
			}
			table.setHeaderRows(1);
			// data rows
			TreeTableNode root = (TreeTableNode) treeTableModel.getRoot();
			pdfAddRow(-1, noColNames, root, table, fonts, xlsCols);
			document.add(table);
            document.close();
        } catch (DocumentException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
        } catch (FileNotFoundException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
        }
	}

	private void pdfAddRow(int iter, int noColNames, TreeTableNode node,
			PdfPTable table, HashMap<String, Font> fonts, ArrayList<XlsCol> xlsCols) {
        String str = "";
		XlsCol xlsCol;
        Paragraph paragraph = new Paragraph();
		PdfPCell cell = new PdfPCell();
		for (int i = 0; i < xlsCols.size(); i++) {
			str = "";
			xlsCol = xlsCols.get(i);
			if (i == 0 && iter < 0) {
				str = node.title;
			} else if (i == iter) {
				str = node.title;
			} else if (i > noColNames -1) {
				switch (xlsCol.id - noColNames +1) {
				case DATA_CASES:
					str = parent.numbers.formatNumber(node.noCases);
					break;
				case DATA_TAT_GROSS:
					str = parent.numbers.formatNumber(node.tatGross);
					break;
				case DATA_TAT_HISTO:
					str = parent.numbers.formatNumber(node.tatHisto);
					break;
				case DATA_TAT_DIAGN:
					str = parent.numbers.formatNumber(node.tatFinal);
					break;
				default:
					str = parent.numbers.formatNumber(node.tatTotal);
				}
			}
            paragraph = new Paragraph();
			paragraph.setFont(fonts.get("Font10n"));
			paragraph.add(new Chunk(str));
			cell = new PdfPCell();
			if (i < noColNames) {
				paragraph.setAlignment(Element.ALIGN_LEFT);
				cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			} else {
				paragraph.setAlignment(Element.ALIGN_RIGHT);
				cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			}
			cell.addElement(paragraph);
			table.addCell(cell);
		}
		if (node.children != null) {
			iter++;
			TreeTableNode child;
			for (int i = 0; i < node.children.length; i++) {
				child = (TreeTableNode) node.children[i];
				pdfAddRow(iter, noColNames, child, table, fonts, xlsCols);
			}
		}
	}

	private void setCharts() {
		if (parent.variables.busy.get()) return;
		parent.setBusy(true);
		if (treePath != null) {
			TreeTableNode node = (TreeTableNode) treePath.getPathComponent(treePath.getPathCount() -1);
			if (node != null) {
				chartGross.setData(node.tatGross, Constants.STATUS_Accession);
				chartHisto.setData(node.tatHisto, Constants.STATUS_Histology);
				chartDiagnosis.setData(node.tatFinal, Constants.STATUS_Routed);
				chartTotal.setData(node.tatTotal, Constants.STATUS_Final);
			}
		}
		parent.setBusy(false);
	}

	private void setData() {
		if (parent.variables.busy.get()) return;
		parent.setBusy(true);
		// Must initialize a new instance each time
		DataWorker worker = new DataWorker();
		worker.execute();
	}

	void xls() {
		String fileName = ((PJClient)parent).getFileXls("turnaround.xlsx").trim();
		if (fileName.trim().length() == 0) return;
		final String[] header = {"Cases", "Gross", "Histo", "Diagnosis", "Total"};
		String str = "";
		ArrayList<XlsCol> xlsCols = new ArrayList<XlsCol>();
		XlsCol xlsCol;
		try {
			for (int i = 0; i < rowsView.length; i++) {
				if (rowsView[i] > 0) {
					switch (rowsView[i]) {
					case Constants.ROW_FACILITY:
						str = "Fac";
						break;
					case Constants.ROW_SPECIALTY:
						str = "Spy";
						break;
					case Constants.ROW_SUBSPECIALTY:
						str = "Sub";
						break;
					case Constants.ROW_PROCEDURE:
						str = "Proc";
						break;
					default:
						str = "Staff";
					}
					xlsCol = new XlsCol(i, 0, str);
					xlsCols.add(xlsCol);
				}
			}
			int noColNames = xlsCols.size();
			for (int i = 0; i < header.length; i++) {
				xlsCol = new XlsCol(noColNames + i, 1, header[i]);
				xlsCols.add(xlsCol);
			}
			XSSFWorkbook wb = new XSSFWorkbook();
			XlsStyles xlsLib = new XlsStyles(wb);
			HashMap<String, CellStyle> styles = xlsLib.getStyles();
			XSSFSheet sheet = wb.createSheet("Summary");
			// title row
			Row row = sheet.createRow(0);
			row.setHeightInPoints(45);
			Cell cell = row.createCell(0);
			cell.setCellValue("Turnaround Time");
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, xlsCols.size() -1));
			row = sheet.createRow(1);
			row.setHeightInPoints(45);
			cell = row.createCell(0);
			cell.setCellValue(parent.dateUtils.formatter(timeFrom, parent.dateUtils.FORMAT_DATE) + " - "
					+ parent.dateUtils.formatter(timeTo, parent.dateUtils.FORMAT_DATE));
			cell.setCellStyle(styles.get("title"));
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, xlsCols.size() -1));
			// header row
			row = sheet.createRow(2);
			ColumnHelper colHelper = sheet.getColumnHelper();
			for (int i = 0; i < xlsCols.size(); i++) {
				xlsCol = xlsCols.get(i);
				cell = row.createCell(i);
				cell.setCellValue(xlsCol.name);
				cell.setCellStyle(styles.get("header"));
				switch (xlsCol.type) {
				case 1:
					colHelper.setColDefaultStyle(i, styles.get("data_int"));
					break;
				case 2:
					colHelper.setColDefaultStyle(i, styles.get("data_double"));
					break;
				default:
					colHelper.setColDefaultStyle(i, styles.get("text"));
				}
			}
			int rownum = 3;
			TreeTableNode root = (TreeTableNode) treeTableModel.getRoot();
			xlsAddRow(-1, rownum, noColNames, root, xlsCols, sheet);
			sheet.createFreezePane(noColNames, 3);
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

	private int xlsAddRow(int iter, int rownum, int noColNames, TreeTableNode node,
			ArrayList<XlsCol> xlsCols, XSSFSheet sheet) {
		Row row = sheet.createRow(rownum++);
		Cell cell;
		XlsCol xlsCol;
		for (int i = 0; i < xlsCols.size(); i++) {
			cell = row.createCell(i);
			xlsCol = xlsCols.get(i);
			if (i < noColNames) {
				if (i == 0 && iter < 0) {
					xlsCol.name = node.title;
				} else if (i == iter) {
					xlsCol.name = node.title;
					for (int j = i+1; j < noColNames; j++) {
						xlsCol = xlsCols.get(j);
						xlsCol.name = "";
					}
					xlsCol = xlsCols.get(i);
				}
				cell.setCellValue(xlsCol.name);
				continue;
			}
			switch (xlsCol.id - noColNames +1) {
			case DATA_CASES:
				cell.setCellValue(node.noCases);
				break;
			case DATA_TAT_GROSS:
				cell.setCellValue(node.tatGross);
				break;
			case DATA_TAT_HISTO:
				cell.setCellValue(node.tatHisto);
				break;
			case DATA_TAT_DIAGN:
				cell.setCellValue(node.tatFinal);
				break;
			default:
				cell.setCellValue(node.tatTotal);
			}
		}
		if (node.children != null) {
			iter++;
			TreeTableNode child;
			for (int i = 0; i < node.children.length; i++) {
				child = (TreeTableNode) node.children[i];
				rownum = xlsAddRow(iter, rownum, noColNames, child, xlsCols, sheet);
			}
		}
		return rownum;
	}

	private class DataWorker extends SwingWorker<Void, Void> {
		protected Void doInBackground() throws Exception {
			setName("TATWorker");
			if (datesChanged.get() && timeTo > timeFrom) {
				readTable();
			} else if (rowsChanged.get()) {
				structureData();
			}
			return null;
		}
		
		private void readTable() {
			DataRow dataRow = new DataRow();
			ResultSet rst = null;
			try {
				dataRows.clear();
				rst = parent.dbPowerJ.getTATSum(timeFrom, timeTo);
				while (rst.next()) {
					dataRow = new DataRow();
					dataRow.facID = rst.getShort("FACID");
					dataRow.spyID = rst.getShort("SPYID");
					dataRow.subID = rst.getShort("SUBID");
					dataRow.perID = rst.getShort("FINALID");
					dataRow.proID = rst.getShort("PROID");
					dataRow.noCases = rst.getInt("NOCASES");
					dataRow.tatGross = rst.getInt("GROSS");
					dataRow.tatHisto = rst.getInt("ROUTE");
					dataRow.tatFinal = rst.getInt("FINAL");
					dataRow.tatTotal = rst.getInt("TOTAL");
					dataRow.facName = rst.getString("FACI").trim();
					dataRow.spyName = rst.getString("SPYNAME").trim();
					dataRow.subName = rst.getString("SUBINIT").trim();
					dataRow.perName = rst.getString("INITIALS").trim();
					dataRow.proName = DataProcedure.NAMES[dataRow.proID];
					dataRows.add(dataRow);
				}
				try {
					Thread.sleep(Constants.SLEEP_TIME);
				} catch (InterruptedException e) {
				}
				structureData();
			} catch (SQLException e) {
				parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
			} finally {
				infoUpdate = "No rows " + dataRows.size();
				parent.statusBar.setMessage(infoUpdate);
				parent.dbPowerJ.closeRst(rst);
				parent.dbPowerJ.closeStm();
			}
		}

		private void structureData() {
			short id = 0;
			short ids[] = new short[rowsView.length];
			int rowNos[] = new int[rowsView.length];
			int noRows = dataRows.size();
			String name = "";
			DataArray dataRoot = new DataArray();
			DataArray data1 = new DataArray();
			DataArray data2 = new DataArray();
			DataArray data3 = new DataArray();
			DataArray data4 = new DataArray();
			DataArray data5 = new DataArray();
			DataRow dataRow = new DataRow();
			for (int x = 0; x < noRows; x++) {
				dataRow = dataRows.get(x);
				// Match 1st node
				switch (rowsView[0]) {
				case Constants.ROW_FACILITY:
					id = dataRow.facID;
					name = dataRow.facName;
					break;
				case Constants.ROW_SPECIALTY:
					id = dataRow.spyID;
					name = dataRow.spyName;
					break;
				case Constants.ROW_SUBSPECIALTY:
					id = dataRow.subID;
					name = dataRow.subName;
					break;
				case Constants.ROW_STAFF:
					id = dataRow.perID;
					name = dataRow.perName;
					break;
				case Constants.ROW_PROCEDURE:
					id = dataRow.proID;
					name = dataRow.proName;
					break;
				default:
					id = 0;
				}
				if (ids[0] != id) {
					ids[0] = id;
					ids[1] = 0;
					ids[2] = 0;
					ids[3] = 0;
					ids[4] = 0;
					rowNos[0] = -1;
					rowNos[1] = -1;
					rowNos[2] = -1;
					rowNos[3] = -1;
					rowNos[4] = -1;
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
						data1.title = name;
						dataRoot.children.add(data1);
					}
				}
				// Match 2nd node
				switch (rowsView[1]) {
				case Constants.ROW_FACILITY:
					id = dataRow.facID;
					name = dataRow.facName;
					break;
				case Constants.ROW_SPECIALTY:
					id = dataRow.spyID;
					name = dataRow.spyName;
					break;
				case Constants.ROW_SUBSPECIALTY:
					id = dataRow.subID;
					name = dataRow.subName;
					break;
				case Constants.ROW_STAFF:
					id = dataRow.perID;
					name = dataRow.perName;
					break;
				case Constants.ROW_PROCEDURE:
					id = dataRow.proID;
					name = dataRow.proName;
					break;
				default:
					id = 0;
				}
				if (ids[1] != id) {
					ids[1] = id;
					ids[2] = 0;
					ids[3] = 0;
					ids[4] = 0;
					rowNos[1] = -1;
					rowNos[2] = -1;
					rowNos[3] = -1;
					rowNos[4] = -1;
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
						data2.title = name;
						data1.children.add(data2);
					}
				}
				// Match 3rd node
				switch (rowsView[2]) {
				case Constants.ROW_FACILITY:
					id = dataRow.facID;
					name = dataRow.facName;
					break;
				case Constants.ROW_SPECIALTY:
					id = dataRow.spyID;
					name = dataRow.spyName;
					break;
				case Constants.ROW_SUBSPECIALTY:
					id = dataRow.subID;
					name = dataRow.subName;
					break;
				case Constants.ROW_STAFF:
					id = dataRow.perID;
					name = dataRow.perName;
					break;
				case Constants.ROW_PROCEDURE:
					id = dataRow.proID;
					name = dataRow.proName;
					break;
				default:
					id = 0;
				}
				if (ids[2] != id) {
					ids[2] = id;
					ids[3] = 0;
					ids[4] = 0;
					rowNos[2] = -1;
					rowNos[3] = -1;
					rowNos[4] = -1;
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
						data3.title = name;
						data2.children.add(data3);
					}
				}
				// Match 4th node
				switch (rowsView[3]) {
				case Constants.ROW_FACILITY:
					id = dataRow.facID;
					name = dataRow.facName;
					break;
				case Constants.ROW_SPECIALTY:
					id = dataRow.spyID;
					name = dataRow.spyName;
					break;
				case Constants.ROW_SUBSPECIALTY:
					id = dataRow.subID;
					name = dataRow.subName;
					break;
				case Constants.ROW_STAFF:
					id = dataRow.perID;
					name = dataRow.perName;
					break;
				case Constants.ROW_PROCEDURE:
					id = dataRow.proID;
					name = dataRow.proName;
					break;
				default:
					id = 0;
				}
				if (ids[3] != id) {
					ids[3] = id;
					ids[4] = 0;
					rowNos[3] = -1;
					rowNos[4] = -1;
					for (int i = 0; i < data3.children.size(); i++) {
						data4 = data3.children.get(i);
						if (data4.id == ids[3]) {
							rowNos[3] = i;
							break;
						}
					}
					if (rowNos[3] < 0) {
						rowNos[3] = data3.children.size();
						data4 = new DataArray();
						data4.id = ids[3];
						data4.title = name;
						data3.children.add(data4);
					}
				}
				// Match 5th node
				switch (rowsView[4]) {
				case Constants.ROW_FACILITY:
					id = dataRow.facID;
					name = dataRow.facName;
					break;
				case Constants.ROW_SPECIALTY:
					id = dataRow.spyID;
					name = dataRow.spyName;
					break;
				case Constants.ROW_SUBSPECIALTY:
					id = dataRow.subID;
					name = dataRow.subName;
					break;
				case Constants.ROW_STAFF:
					id = dataRow.perID;
					name = dataRow.perName;
					break;
				case Constants.ROW_PROCEDURE:
					id = dataRow.proID;
					name = dataRow.proName;
					break;
				default:
					id = 0;
				}
				if (ids[4] != id) {
					ids[4] = id;
					rowNos[4] = -1;
					for (int i = 0; i < data4.children.size(); i++) {
						data5 = data4.children.get(i);
						if (data5.id == ids[3]) {
							rowNos[4] = i;
							break;
						}
					}
					if (rowNos[4] < 0) {
						rowNos[4] = data4.children.size();
						data5 = new DataArray();
						data5.id = ids[4];
						data5.title = name;
						data4.children.add(data5);
					}
				}
				data5.noCases += dataRow.noCases;
				data4.noCases += dataRow.noCases;
				data3.noCases += dataRow.noCases;
				data2.noCases += dataRow.noCases;
				data1.noCases += dataRow.noCases;
				dataRoot.noCases += dataRow.noCases;
				data5.tatGross += dataRow.tatGross;
				data4.tatGross += dataRow.tatGross;
				data3.tatGross += dataRow.tatGross;
				data2.tatGross += dataRow.tatGross;
				data1.tatGross += dataRow.tatGross;
				dataRoot.tatGross += dataRow.tatGross;
				data5.tatHisto += dataRow.tatHisto;
				data4.tatHisto += dataRow.tatHisto;
				data3.tatHisto += dataRow.tatHisto;
				data2.tatHisto += dataRow.tatHisto;
				data1.tatHisto += dataRow.tatHisto;
				dataRoot.tatHisto += dataRow.tatHisto;
				data5.tatFinal += dataRow.tatFinal;
				data4.tatFinal += dataRow.tatFinal;
				data3.tatFinal += dataRow.tatFinal;
				data2.tatFinal += dataRow.tatFinal;
				data1.tatFinal += dataRow.tatFinal;
				dataRoot.tatFinal += dataRow.tatFinal;
				data5.tatTotal += dataRow.tatTotal;
				data4.tatTotal += dataRow.tatTotal;
				data3.tatTotal += dataRow.tatTotal;
				data2.tatTotal += dataRow.tatTotal;
				data1.tatTotal += dataRow.tatTotal;
				dataRoot.tatTotal += dataRow.tatTotal;
			}
			sortChildren(dataRoot);
			setModel(dataRoot);
		}
		
		private void setModel(DataArray dataRoot) {
			TreeTableNode nodeRoot = (TreeTableNode) treeTableModel.getRoot();
			TreeTableNode node1 = new TreeTableNode("node");
			TreeTableNode node2 = new TreeTableNode("node");
			TreeTableNode node3 = new TreeTableNode("node");
			TreeTableNode node4 = new TreeTableNode("node");
			TreeTableNode node5 = new TreeTableNode("node");
			DataArray data1 = new DataArray();
			DataArray data2 = new DataArray();
			DataArray data3 = new DataArray();
			DataArray data4 = new DataArray();
			DataArray data5 = new DataArray();
			nodeRoot.noCases = dataRoot.noCases;
			nodeRoot.tatGross = 0;
			nodeRoot.tatHisto = 0;
			nodeRoot.tatFinal = 0;
			nodeRoot.tatTotal = 0;
			if (dataRoot.noCases > 0) {
				nodeRoot.tatGross = dataRoot.tatGross / dataRoot.noCases;
				nodeRoot.tatHisto = dataRoot.tatHisto / dataRoot.noCases;
				nodeRoot.tatFinal = dataRoot.tatFinal / dataRoot.noCases;
				nodeRoot.tatTotal = dataRoot.tatTotal / dataRoot.noCases;
			}
			nodeRoot.children = new TreeTableNode[dataRoot.children.size()];
			for (int i = 0; i < dataRoot.children.size(); i++) {
				data1 = dataRoot.children.get(i);
				node1 = new TreeTableNode(data1.title);
				node1.noCases = data1.noCases;
				if (data1.noCases > 0) {
					node1.tatGross = data1.tatGross / data1.noCases;
					node1.tatHisto = data1.tatHisto / data1.noCases;
					node1.tatFinal = data1.tatFinal / data1.noCases;
					node1.tatTotal = data1.tatTotal / data1.noCases;
				}
				node1.children = new TreeTableNode[data1.children.size()];
				for (int j = 0; j < data1.children.size(); j++) {
					data2 = data1.children.get(j);
					node2 = new TreeTableNode(data2.title);
					node2.noCases = data2.noCases;
					if (data2.noCases > 0) {
						node2.tatGross = data2.tatGross / data2.noCases;
						node2.tatHisto = data2.tatHisto / data2.noCases;
						node2.tatFinal = data2.tatFinal / data2.noCases;
						node2.tatTotal = data2.tatTotal / data2.noCases;
					}
					node2.children = new TreeTableNode[data2.children.size()];
					for (int k = 0; k < data2.children.size(); k++) {
						data3 = data2.children.get(k);
						node3 = new TreeTableNode(data3.title);
						node3.noCases = data3.noCases;
						if (data3.noCases > 0) {
							node3.tatGross = data3.tatGross / data3.noCases;
							node3.tatHisto = data3.tatHisto / data3.noCases;
							node3.tatFinal = data3.tatFinal / data3.noCases;
							node3.tatTotal = data3.tatTotal / data3.noCases;
						}
						node3.children = new TreeTableNode[data3.children.size()];
						for (int l = 0; l < data3.children.size(); l++) {
							data4 = data3.children.get(l);
							node4 = new TreeTableNode(data4.title);
							node4.noCases = data4.noCases;
							if (data4.noCases > 0) {
								node4.tatGross = data4.tatGross / data4.noCases;
								node4.tatHisto = data4.tatHisto / data4.noCases;
								node4.tatFinal = data4.tatFinal / data4.noCases;
								node4.tatTotal = data4.tatTotal / data4.noCases;
							}
							node4.children = new TreeTableNode[data4.children.size()];
							for (int m = 0; m < data4.children.size(); m++) {
								data5 = data4.children.get(m);
								node5 = new TreeTableNode(data5.title);
								node5.noCases = data5.noCases;
								if (data5.noCases > 0) {
									node5.tatGross = data5.tatGross / data5.noCases;
									node5.tatHisto = data5.tatHisto / data5.noCases;
									node5.tatFinal = data5.tatFinal / data5.noCases;
									node5.tatTotal = data5.tatTotal / data5.noCases;
								}
								node4.children[m] = node5;
							}
							data4.children.clear();
							node3.children[l] = node4;
						}
						data3.children.clear();
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

		private void sortChildren(DataArray aParent) {
			DataArray child = new DataArray();
			for (int i = 0; i < aParent.children.size(); i++) {
				child = aParent.children.get(i);
				if (child.children.size() > 0) {
					sortChildren(child);
				}
			}
			Collections.sort(aParent.children, new Comparator<DataArray>() {
				public int compare(DataArray o1, DataArray o2) {
					return o1.noCases - o2.noCases;
				}
			});
		}

		public void done() {
			datesChanged.set(false);
			rowsChanged.set(false);
			parent.setBusy(false);
			// Display results
			TreeTableNode nodeRoot = (TreeTableNode) treeTableModel.getRoot();
			treeTableModel = new TatModel(nodeRoot);
			treeTable.setTreeTableModel(treeTableModel);
		}
	}
	
	class DataArray {
		short id = 0;
		int index = 0;
		int noCases = 0;
		int tatGross = 0;
		int tatHisto = 0;
		int tatFinal = 0;
		int tatTotal = 0;
		String title = "";
		ArrayList<DataArray> children = new ArrayList<DataArray>();
	}

	class DataRow {
		short facID = 0;
		short spyID = 0;
		short subID = 0;
		short perID = 0;
		short proID = 0;
		int noCases = 0;
		int tatGross = 0;
		int tatHisto = 0;
		int tatFinal = 0;
		int tatTotal = 0;
		String facName = "";
		String spyName = "";
		String subName = "";
		String perName = "";
		String proName = "";
	}

	class TreeTableNode {
		int noCases = 0;
		int tatGross = 0;
		int tatHisto = 0;
		int tatFinal = 0;
		int tatTotal = 0;
		String title = "";
		Object[] children = new Object[0];

		TreeTableNode(String name) {
			this.title = name;
		}

		public String toString() {
			return this.title;
		}
	}

	class TatModel extends AbstractTreeTableModel implements TreeTableModel {
		private final String[] cNames = {"Name", "Cases", "Gross", "Histo", "Report", "Total"};
		private final Class<?>[]  cTypes = {TreeTableModel.class, Integer.class,
				Integer.class, Integer.class, Integer.class, Integer.class};

		TatModel(Object nodeRoot) {
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
			case DATA_CASES:
				return data.noCases;
			case DATA_TAT_GROSS:
				return data.tatGross;
			case DATA_TAT_HISTO:
				return data.tatHisto;
			case DATA_TAT_DIAGN:
				return data.tatFinal;
			case DATA_TAT_TOTAL:
				return data.tatTotal;
			default:
				return data.title;
			}
		}
	}
}
