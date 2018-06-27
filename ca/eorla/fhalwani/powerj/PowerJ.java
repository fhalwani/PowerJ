package ca.eorla.fhalwani.powerj;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

class PowerJ extends JFrame implements WindowListener {
	private static final long serialVersionUID = 3318446746416425029L;
	private ServerSocket socket;
	private Logs logger;
	DbPowerJ dbPowerJ;
	DateUtils dateUtils;
	Defaults defaults;
	Numbers numbers;
	Statusbar statusBar;
	Variables variables;
    static String[] commandLineArgs;
	
	PowerJ() {
		super(Constants.APP_NAME);
		try {
			/* Use the System Look and Feel */
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException ignore) {
		} catch (IllegalAccessException ignore) {
		} catch (InstantiationException ignore) {
		} catch (ClassNotFoundException ignore) {
		}
		setName(Constants.APP_NAME);
		setTitle(Constants.APP_NAME);
		Thread.currentThread().setName(Constants.APP_NAME);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setLocationRelativeTo(null);	// for double monitors
		addWindowListener(this);
		variables = new Variables();
		setSocket();
	}
	
    boolean abort() {
    	return false;
    }

	int askSave(String name) {
		DlgMessage dialog = new DlgMessage(this, name);
		return dialog.choice;
	}

	/** Close a panel. */
	void close() {
	}

    /**
     * Do the work of starting the engine
     */
    void initialize() {
		numbers = new Numbers();
		for (String s: commandLineArgs) {
			s = s.trim();
			if (s.length() > 6 && s.substring(0, 6).toLowerCase().equals("--path")) {
				variables.appDir = s.substring(6).trim();
			} else if (s.length() > 11 && s.substring(0, 12).toLowerCase().equals("--logingross")) {
				// gross or grossing
				variables.userID = -222;
				variables.autoLogin = true;
				variables.userAccess[Constants.ACCESS_Gross] = true;
			} else if (s.length() > 11 && s.substring(0, 12).toLowerCase().equals("--loginhisto")) {
				// histo or histology
				variables.userID = -111;
				variables.autoLogin = true;
				variables.userAccess[Constants.ACCESS_Histology] = true;
			} else if (s.toLowerCase().equals("--offline")) {
    			variables.offLine = true;
			} else if (s.toLowerCase().equals("--debug")) {
				variables.debugMode = true;
			} else {
				System.out.println("Unknown parameter " + s);
			}
		}
		commandLineArgs = null;
		if (variables.appDir.length() == 0) {
			variables.appDir = getExecutionPath();
		} else if (!variables.appDir.endsWith(Constants.FILE_SEPARATOR)) {
			variables.appDir += Constants.FILE_SEPARATOR;
		}
		if (!variables.hasError) {
			logger = new Logs(variables.appDir);
		}
		if (!variables.hasError) {
			// Open preferences file
			defaults = new Defaults(variables);
		}
		if (!variables.hasError) {
			variables.readSetup(this);
		}
		if (!variables.hasError) {
			switch (variables.pjSystem) {
			case 0:
    			// Open local database
    			dbPowerJ = new DbDerby(this);
				break;
			case 1:
    			// Open Microsoft SQL Server database
    			dbPowerJ = new DbMSSQL(this);
				break;
			default:
    			// Open MySQL Server database
    			dbPowerJ = new DbMySQL(this);
			}
		}
		if (!variables.hasError) {
			variables.readDB(this);
		}
		if (!variables.hasError) {
			statusBar = new Statusbar();
			dateUtils = new DateUtils(variables.closingHour);
			setIconImage(Utilities.getImage(Constants.APP_NAME));
			Point p = new Point(0, 0);
			p = defaults.getPoint("main", p);
			setLocation(p);
		}
    }
    
	void log(String message) {
		logger.logInfo(message);
	}

	void log(int severity, String name, String message) {
		if (severity == JOptionPane.ERROR_MESSAGE) {
 			variables.hasError = true;
			logger.logError(message);
		} else {
			logger.logInfo(message);
		}
	}
	
	void log(int severity, String name, Throwable e) {
		if (severity == JOptionPane.ERROR_MESSAGE) {
 			variables.hasError = true;
			logger.logError(name, e);
		} else {
			log(severity, name, e);
		}
	}
	
	/** Find the folder path of the running jar file. */
	private String getExecutionPath() {
		String jarPath = "";
		try {
			File test = new File(getClass().getProtectionDomain().
					getCodeSource().getLocation().toURI().getPath());
			jarPath = test.getAbsolutePath();
			if (jarPath.length() == 0) {
				variables.hasError = true;
			} else if (jarPath.toLowerCase().contains(Constants.APP_NAME.toLowerCase() + ".jar")) {
				jarPath = jarPath.substring(0, jarPath.length()
						- (Constants.APP_NAME.length() +4));
				if (jarPath.toLowerCase().endsWith(Constants.FILE_SEPARATOR +
						"bin" + Constants.FILE_SEPARATOR)) {
					jarPath = jarPath.substring(0, jarPath.length() -4);
				} else if (!jarPath.toLowerCase().endsWith(Constants.FILE_SEPARATOR)) {
					jarPath += Constants.FILE_SEPARATOR;
				}
			} else {
				variables.hasError = true;
			}
		} catch (URISyntaxException e) {
			variables.hasError = true;
			e.printStackTrace();
		}
		return jarPath;
	}

	void quit() {
		if (dbPowerJ != null) {
			dbPowerJ.close();
		}
		if (defaults != null) {
			try {
				defaults.setPoint("main", getLocationOnScreen());
			} catch (Exception ignore) {
			}
			defaults.close();
		}
		if (logger != null) {
			logger.close();
		}
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ignore) {
			}
		}
		dispose();
		System.exit(0);
	}

	void setBusy(boolean busy) {
		variables.busy.set(busy);
		if (busy) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			statusBar.setMessage("Updating data, do not interrupt...");
			statusBar.setProgress(-1);
		} else {
			setCursor(Cursor.getDefaultCursor());
			statusBar.setMessage("");
			statusBar.setProgress(100);
		}
	}

	/** Calculate the next schedule to scan the database **/
	void setNextUpdate() {
		dateUtils.setNextUpdate(variables);
		Calendar calLast = Calendar.getInstance();
		Calendar calNext = Calendar.getInstance();
		calLast.setTimeInMillis(variables.lastUpdate);
		calNext.setTimeInMillis(variables.nextUpdate);
		if (calLast.get(Calendar.DAY_OF_YEAR) != calNext.get(Calendar.DAY_OF_YEAR)) {
			// Synchronize clients 2-10 minutes after server update
			Random rand = new Random();
			int delay = (rand.nextInt(9) + 2) * 30000;
			variables.nextUpdate += delay;
		}
	}

	private void setSocket() {
		try {
			socket = new ServerSocket(4443);
		} catch (IOException e) {
			variables.hasError = true;
			log(JOptionPane.WARNING_MESSAGE, Constants.APP_NAME,
					"Another instance of the application is running!"); 
		}
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
