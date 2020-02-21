package ca.eorla.fhalwani.powerj;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import com.microsoft.sqlserver.jdbc.SQLServerException;

class PJInstall extends JFrame implements ActionListener, DocumentListener,
		FocusListener, WindowListener {
	private static final long serialVersionUID = 4001032210349636712L;
	private final byte STP_SERVER = 0;
	private final byte STP_ADDRESS = 1;
	private final byte STP_PORT = 2;
	private final byte STP_DATABASE = 3;
	private final byte STP_LOGIN = 4;
	private final byte STP_PASSWORD = 5;
	private final byte TASK_DERBY = 1;
	private final byte TASK_MSSQL = 2;
	private final byte TASK_MYSQL = 3;
	private byte task = TASK_DERBY;
	private static String path = "";
	private final String[] data = {"mysql", "localhost", "3306", "PowerJ", "PJClient", "password"};
	private Connection con = null;
	private JStringField txtServer, txtPort, txtDatabase, txtLogin;
	private JPasswordField txtPassword;
	
	PJInstall() {
		super(Constants.APP_NAME);
		if (path.length() == 0) {
			System.out.println("Must pass mandatory argument (" +
					"Install.jar path--/path/to/app/directory)");
		} else {
			if (!path.substring(path.length()-1).
					equals(Constants.FILE_SEPARATOR)) {
				path += Constants.FILE_SEPARATOR;
			}
		}
	}

	public static void main(String[] args) {
		for (String s: args) {
			s = s.trim();
			if (s.length() > 6) {
				if (s.substring(0, 6).toLowerCase().equals("--path")) {
					path = s.substring(6);
					break;
				}
			}
		}
		// Schedule a job for the event dispatch thread
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	private void closeDerby() {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException ignore) {}
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException ignore) {}
		con = null;
	}

	private void closeServer() {
		try {
			if (con != null) {
				con.close();
			}
		} catch (SQLException ignore) {}
		con = null;
	}

	private static void createAndShowGUI() {
		PJInstall aFrame = new PJInstall();
		aFrame.createMain();
	}
	
	private boolean createDerby() {
		boolean success = false;
		try {
			// Define physical location of Derby Database
			System.out.println("Creating Database.");
			Properties p = System.getProperties();
			p.setProperty("derby.system.home", path + "db" +
					Constants.FILE_SEPARATOR);
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			con = DriverManager.getConnection("jdbc:derby:PJWL;create=true;");
			success = true;
			try {
				Thread.sleep(Constants.SLEEP_TIME);
			} catch (InterruptedException e) {
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return success;
	}
	
	private void createMain() {
		setName(Constants.APP_NAME);
		setTitle(Constants.APP_NAME);
		Thread.currentThread().setName(Constants.APP_NAME);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		try {
			/* Use the System Look and Feel */
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException ignore) {
		} catch (IllegalAccessException ignore) {
		} catch (InstantiationException ignore) {
		} catch (ClassNotFoundException ignore) {
		}
		setIconImage(Utilities.getImage(Constants.APP_NAME));
		setLayout(new BorderLayout());
		JRadioButton btnDerby = new JRadioButton("Desktop");
		btnDerby.setMnemonic(KeyEvent.VK_D);
		btnDerby.setActionCommand("Derby");
		btnDerby.setSelected(true);
		btnDerby.addActionListener(this);
	    JRadioButton btnMSSQL = new JRadioButton("MSQL Server");
	    btnMSSQL.setMnemonic(KeyEvent.VK_M);
	    btnMSSQL.setActionCommand("MSSQL");
		btnMSSQL.addActionListener(this);
	    JRadioButton btnMySQL = new JRadioButton("MySQL Server");
	    btnMySQL.setMnemonic(KeyEvent.VK_Y);
	    btnMySQL.setActionCommand("MYSQL");
		btnMySQL.addActionListener(this);
	    ButtonGroup group = new ButtonGroup();
	    group.add(btnDerby);
	    group.add(btnMSSQL);
	    group.add(btnMySQL);
        JPanel radioPanel = new JPanel(new GridLayout(0, 3));
        radioPanel.add(btnDerby);
        radioPanel.add(btnMSSQL);
        radioPanel.add(btnMySQL);
        add(radioPanel, BorderLayout.LINE_START);
		JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));
		JLabel label = new JLabel("MS SQL Address: ");
		label.setFont(Constants.APP_FONT);
		label.setDisplayedMnemonic(KeyEvent.VK_A);
		panel.add(label);
		txtServer = new JStringField(3, 50);
		txtServer.setName("Server");
		txtServer.getDocument().addDocumentListener(this);
		txtServer.getDocument().putProperty("RowID", STP_SERVER);
		txtServer.setEnabled(false);
		label.setLabelFor(txtServer);
		panel.add(txtServer);
		label = new JLabel("Server Port: ");
		label.setFont(Constants.APP_FONT);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		panel.add(label);
		txtPort = new JStringField(3, 5);
		txtPort.setName("Port");
		txtPort.getDocument().addDocumentListener(this);
		txtPort.getDocument().putProperty("RowID", STP_PORT);
		txtPort.setEnabled(false);
		label.setLabelFor(txtPort);
		panel.add(txtPort);
		label = new JLabel("Database Name: ");
		label.setFont(Constants.APP_FONT);
		label.setDisplayedMnemonic(KeyEvent.VK_N);
		panel.add(label);
		txtDatabase = new JStringField(3, 50);
		txtDatabase.setName("Database");
		txtDatabase.getDocument().addDocumentListener(this);
		txtDatabase.getDocument().putProperty("RowID", STP_DATABASE);
		txtDatabase.setEnabled(false);
		label.setLabelFor(txtDatabase);
		panel.add(txtDatabase);
		label = new JLabel("Login Name: ");
		label.setFont(Constants.APP_FONT);
		label.setDisplayedMnemonic(KeyEvent.VK_L);
		panel.add(label);
		txtLogin = new JStringField(3, 50);
		txtLogin.setName("Login");
		txtLogin.getDocument().addDocumentListener(this);
		txtLogin.getDocument().putProperty("RowID", STP_LOGIN);
		txtLogin.setEnabled(false);
		label.setLabelFor(txtLogin);
		panel.add(txtLogin);
		label = new JLabel("Password: ");
		label.setFont(Constants.APP_FONT);
		label.setDisplayedMnemonic(KeyEvent.VK_P);
		panel.add(label);
		txtPassword = new JPasswordField(20);
		txtPassword.setName("Password");
		txtPassword.addFocusListener(this);
		txtPassword.setToolTipText("Password at least 8 characters");
		txtPassword.setEnabled(false);
		label.setLabelFor(txtPassword);
		panel.add(txtPassword);
		JButton btnGo = new JButton(Utilities.getIcon("go"));
		btnGo.setAlignmentY(TOP_ALIGNMENT);
		if (btnGo.getIcon() == null)
			btnGo.setText("Go");
		btnGo.setActionCommand("Save");
		btnGo.addActionListener(this);
		panel.add(btnGo);
		JButton btnCancel = new JButton(Utilities.getIcon("quit"));
		btnCancel.setAlignmentY(TOP_ALIGNMENT);
		if (btnCancel.getIcon() == null)
			btnCancel.setText("Quit");
		btnCancel.setActionCommand("Cancel");
		btnCancel.addActionListener(this);
		panel.add(btnCancel);
        add(panel, BorderLayout.CENTER);
		pack();
		setVisible(true);
	}

	private boolean createMSSql() {
		boolean success = false;
		SQLServerDataSource ds = new SQLServerDataSource();
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			ds.setIntegratedSecurity(false);
			ds.setLoginTimeout(2);
			Integer port = Integer.valueOf(data[STP_PORT]);
			ds.setPortNumber(port);
			ds.setServerName(data[STP_ADDRESS]);
			ds.setDatabaseName(Constants.APP_NAME);
			ds.setUser(data[STP_LOGIN]);
			ds.setPassword(data[STP_PASSWORD]);
			con = ds.getConnection();
			success = true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLServerException e) {
			e.printStackTrace();
		}
		return success;
	}

	private boolean createMySql() {
		boolean success = false;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			con = DriverManager.getConnection("jdbc:mysql://" +
					data[STP_ADDRESS] + ":" +
					data[STP_PORT] + "/" +
					data[STP_DATABASE] +
					"?autoReconnect=true&useSSL=false",
					data[STP_LOGIN],
					data[STP_PASSWORD]);
			success = true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return success;
	}

	private void enableText() {
		boolean enable = false;
		if (task == TASK_MSSQL
				|| task == TASK_MYSQL) {
			enable = true;
		}
		txtServer.setEnabled(enable);
		txtPort.setEnabled(enable);
		txtDatabase.setEnabled(enable);
		txtLogin.setEnabled(enable);
		txtPassword.setEnabled(enable);
	}
	
	private void execute() {
		switch (task) {
		case TASK_DERBY:
			if (createDerby()) {
				closeDerby();
			}
			break;
		case TASK_MSSQL:
			if (createMSSql()) {
				Crypto crypto = new Crypto(path);
				if (crypto.setFile(data)) {
					closeServer();
				}
			}
			break;
		case TASK_MYSQL:
			if (createMySql()) {
				Crypto crypto = new Crypto(path);
				if (crypto.setFile(data)) {
					closeServer();
				}
			}
			break;
		default:
			// do nothing
		}
	}

	private void getText(DocumentEvent e) {
		try {
			Document doc = (Document)e.getDocument();
			int length = doc.getLength();
			byte row = (Byte) doc.getProperty("RowID");
			data[row] = doc.getText(0, length);
		} catch (BadLocationException ignore) {
		}
	}
	
	private void quit() {
		if (con != null) {
			try {
				if (!con.isClosed())
					con.close();
			} catch (Exception ignore) {}
		}
		dispose();
		System.exit(0);
	}

	public void actionPerformed(ActionEvent e) {
		String name = e.getActionCommand();
		if (name.equals("Derby")) {
			task = TASK_DERBY;
			enableText();
		} else if (name.equals("MSSQL")) {
			task = TASK_MSSQL;
			enableText();
		} else if (name.equals("MYSQL")) {
			task = TASK_MYSQL;
			enableText();
		} else if (name.equals("Save")) {
			execute();
		} else {
			quit();
		}
	}

	public void focusGained(FocusEvent e) {
		Component c = e.getComponent();
		if (c != null) {
			if (c instanceof JTextComponent) {
				((JTextComponent)c).selectAll();
			}
		}
	}

	public void focusLost(FocusEvent e) {
		Component c = e.getComponent();
		if (c != null) {
			if (c instanceof JTextComponent) {
				((JTextComponent)c).setCaretPosition(0);
				((JTextComponent)c).setSelectionStart(0);
				((JTextComponent)c).setSelectionEnd(0);
				if (c instanceof JPasswordField) {
					char[] input = ((JPasswordField) c).getPassword();
					data[STP_PASSWORD] = String.copyValueOf(input).trim();
				}
			}
		}
	}

	public void changedUpdate(DocumentEvent e) {
		getText(e);
	}

	public void insertUpdate(DocumentEvent e) {
		getText(e);
	}

	public void removeUpdate(DocumentEvent e) {
		getText(e);
	}

	public void windowClosing(WindowEvent e) {
		quit();
	}
	public void windowActivated(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowOpened(WindowEvent e) {}
}
