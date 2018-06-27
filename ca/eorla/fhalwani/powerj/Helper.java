package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

class Helper extends JDialog implements WindowListener {
	private static final long serialVersionUID = 6262853246308278794L;
	int level = 0;
	private JEditorPane txtPane;
	private PJClient parent;

	Helper(PJClient parent) {
		super();
		this.parent = parent;
		setName("Help");
		createAndShowGUI();
	}

	/** Create and set up the main window **/
	private void createAndShowGUI() {
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		setLayout(new BorderLayout());
		setResizable(true);
		Image image = Utilities.getImage("help");
		setIconImage(image);
		// Set Frame Size & Bounds
		Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
		Rectangle r = new Rectangle(sz.width *2/3, 100, sz.width /3, sz.height - 200);
		parent.defaults.getRectangle("helpbounds", r);
		if (r != null && r.height > 50) {
			if (r.width > sz.width)
				r.width = sz.width;
			if (r.height > sz.height)
				r.height = sz.height;
			setBounds(r);
			Dimension d = new Dimension(r.width, r.height);
			setPreferredSize(d);
			setMaximumSize(d);
			txtPane = new JEditorPane() {
				private static final long serialVersionUID = 8873617488282541498L;
				// Disable cut/copy/paste
				public void copy(){}
				public void cut(){}
				public void paste(){}
			};
			txtPane.setContentType("text/html");
			txtPane.setEditable(false);
			txtPane.addHyperlinkListener(new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent e) {
					// Display Linked Form
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						setText(e.getDescription());
					}
				}
			});
			// Create the display pane
			JScrollPane scrollPane = new JScrollPane(txtPane,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			Border border = BorderFactory.createEmptyBorder(10,10,10,10);
			scrollPane.setBorder(border);
			add(scrollPane);
			// Set the cascading style script
			setStyle();
			String fileName = "";
			switch (parent.panelID) {
			case Constants.PANEL_ACCESSION:
				fileName = "index.html";
				break;
			case Constants.PANEL_ADDITIONAL:
				fileName = "index.html";
				break;
			case Constants.PANEL_CASES:
				fileName = "index.html";
				break;
			case Constants.PANEL_CODER1:
				fileName = "index.html";
				break;
			case Constants.PANEL_CODER2:
				fileName = "index.html";
				break;
			case Constants.PANEL_CODER3:
				fileName = "index.html";
				break;
			case Constants.PANEL_CODER4:
				fileName = "index.html";
				break;
			case Constants.PANEL_DASH:
				fileName = "index.html";
				break;
			case Constants.PANEL_ERRORS:
				fileName = "index.html";
				break;
			case Constants.PANEL_FACILITY:
				fileName = "index.html";
				break;
			case Constants.PANEL_FROZENS:
				fileName = "index.html";
				break;
			case Constants.PANEL_GROUPS:
				fileName = "index.html";
				break;
			case Constants.PANEL_HISTO:
				fileName = "index.html";
				break;
			case Constants.PANEL_ORDERS:
				fileName = "index.html";
				break;
			case Constants.PANEL_PATHOLOGISTS:
				fileName = "index.html";
				break;
			case Constants.PANEL_PERSONNEL:
				fileName = "index.html";
				break;
			case Constants.PANEL_SCANNING:
				fileName = "index.html";
				break;
			case Constants.PANEL_RULES:
				fileName = "rules.html";
				break;
			case Constants.PANEL_SPECIALTY:
				fileName = "index.html";
				break;
			case Constants.PANEL_SPECIMEN:
				fileName = "index.html";
				break;
			case Constants.PANEL_SUBSPECIAL:
				fileName = "index.html";
				break;
			case Constants.PANEL_STATS:
				fileName = "index.html";
				break;
			case Constants.PANEL_STATSUM:
				fileName = "index.html";
				break;
			case Constants.PANEL_TEMPLATES:
				fileName = "index.html";
				break;
			case Constants.PANEL_TRACKER:
				fileName = "index.html";
				break;
			case Constants.PANEL_TURNAROUND:
				fileName = "index.html";
				break;
			case Constants.PANEL_VARIABLES:
				fileName = "index.html";
				break;
			case Constants.PANEL_WORKFLOW:
				fileName = "index.html";
				break;
			case Constants.PANEL_WLSUMMARY:
				fileName = "index.html";
				break;
			default:
				fileName = "main.html";
			}
			setText(fileName);
			pack();
		}
	}

	private void setStyle() {
		try {
			String fileName = "help/" + "screen.css";
			URL url = ClassLoader.getSystemClassLoader().getResource(fileName);
			StyleSheet stylesheet = new StyleSheet();
			if (url != null) {
				stylesheet.importStyleSheet(url);
				HTMLEditorKit kit = (HTMLEditorKit) txtPane
						.getEditorKitForContentType("text/html");
				kit.setStyleSheet(stylesheet);
				txtPane.setEditorKit(kit);
			}
		} catch (NullPointerException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}

	private void setText(String fileName) {
		try {
			fileName = "help/" + fileName;
			InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
			if (is != null) {
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(ir);
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				ir.close();
				is.close();
				txtPane.setText(sb.toString());
				txtPane.setCaretPosition(0);
			}
		} catch (FileNotFoundException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} catch (NullPointerException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		} catch (IOException e) {
			parent.log(JOptionPane.ERROR_MESSAGE, getName(), e);
		}
	}

	public void windowClosing(WindowEvent evt) {
		// Save last used bounds
		Rectangle r = getBounds();
		parent.defaults.setRectangle("helpbounds", r);
		dispose();
	}

	public void windowOpened(WindowEvent ignore) {}
	public void windowClosed(WindowEvent ignore) {}
	public void windowIconified(WindowEvent ignore) {}
	public void windowDeiconified(WindowEvent ignore) {}
	public void windowActivated(WindowEvent ignore) {}
	public void windowDeactivated(WindowEvent ignore) {}
}
