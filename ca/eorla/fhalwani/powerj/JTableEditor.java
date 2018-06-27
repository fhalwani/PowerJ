package ca.eorla.fhalwani.powerj;
import java.awt.Component;
//import java.awt.event.ComponentAdapter;
//import java.awt.event.ComponentEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.text.JTextComponent;

class JTableEditor extends JTable {
	private static final long serialVersionUID = -5458412581330855577L;

	JTableEditor(PowerJ parent, TableModel tm) {
		super(tm);
		setRowHeight(24);
		setShowGrid(true);
		setAutoCreateRowSorter(true);
		setFillsViewportHeight(true);
		setCellSelectionEnabled(true);
		setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setFont(Constants.APP_FONT);
		getTableHeader().setFont(Constants.APP_FONT);
		// Render background color in alternating rows
		setDefaultRenderer(String.class, new RendererString());
		setDefaultRenderer(Long.class, new RendererInteger(parent));
		setDefaultRenderer(Integer.class, new RendererInteger(parent));
		setDefaultRenderer(Short.class, new RendererInteger(parent));
		setDefaultRenderer(Byte.class, new RendererInteger(parent));
		setDefaultRenderer(Double.class, new RendererDouble(parent, 3));
		setDefaultRenderer(Boolean.class, new RendererBoolean());
		setDefaultRenderer(Date.class, new RendererDate(parent));
		setDefaultRenderer(Calendar.class, new RendererDateTime(parent));
		setDefaultRenderer(DataItem.class, new RendererItem());
		setDefaultRenderer(Object.class, new RendererString());
	}
	
	public boolean editCellAt(int row, int column, EventObject evnt) {
		boolean result = super.editCellAt(row, column, evnt);
		final Component editor = getEditorComponent();
		if (editor != null && editor instanceof JTextComponent) {
			((JTextComponent) editor).selectAll();
			((JTextComponent) editor).setFont(Constants.APP_FONT);
		}
		return result;
	}
}
