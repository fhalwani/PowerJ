package ca.eorla.fhalwani.powerj;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JOptionPane;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;

public class PJServer extends PowerJ implements Daemon, Runnable  {
	private static final long serialVersionUID = 2305941538818083085L;
	private static AtomicBoolean stopped = new AtomicBoolean(false);
	private static AtomicBoolean doWorkload = new AtomicBoolean(false);
	private static PJServer launcher = new PJServer();
	private Thread engine;
    
	PJServer() {
		super();
	}

	boolean abort() {
		return stopped.get();
	}

	public void destroy() {
		terminate();
	}

	private void getLastUpdate() {
		ResultSet rst = null;
		try {
			rst = dbPowerJ.getLastDash();
			while (rst.next()) {
				if (rst.getTimestamp("accession") != null) {
					variables.lastUpdate = rst.getTimestamp("accession").getTime();
				}
			}
		} catch (SQLException e) {
			log(JOptionPane.ERROR_MESSAGE, "PJServer", e);
		} finally {
			dbPowerJ.closeRst(rst);
			dbPowerJ.closeStm();
			variables.nextUpdate = variables.lastUpdate + variables.updateInterval;
		}
	}

	// Implementing the Daemon interface is not required for Windows but is for Linux
	public void init(DaemonContext ctx) throws Exception {
		// store the args
		commandLineArgs = ctx.getArguments();
	}

	/**
	* Do the work of starting the engine
	*/
	void initialize() {
		if (!stopped.get()) {
			// automatic login to APIS
			variables.autoLogin = true;
			super.initialize();
			if (variables.hasError) {
				quit();
			}
			getLastUpdate();
			// start the thread
			engine = new Thread(this);
			engine.setName("PJServer");
			engine.start();
		}
	}

	/**
	* The Java entry point.
	*/
	public static void main(String[] args) {
		// store the args
		commandLineArgs = args;
		// the main routine is only here so I can also run the app from the command line
		launcher.initialize();
		Scanner sc = new Scanner(System.in);
		// wait until receive stop command from keyboard
		System.out.printf("Enter 'stop' to halt: ");
		while (!sc.nextLine().toLowerCase().equals("stop"));
			if (!stopped.get()) {
				launcher.terminate();
		}
		sc.close();
	}

	public void run() {
		while (!stopped.get()) {
			if (!variables.busy.get()) {
				if (!variables.offLine) {
					// Reset error flag
					variables.hasError = false;            		
					variables.busy.set(true);
					if (doWorkload.compareAndSet(true, false)) {
						new WLManager(this);
						try {
							Thread.sleep(60000);
						} catch (InterruptedException e) {}
						new StatsManager(this);
					} else if (variables.nextUpdate - System.currentTimeMillis() < variables.timerInterval) {
						new DashManager(this);
						setNextUpdate();
						// Refresh variables
						variables.readDB(this);
					}
					variables.busy.set(false);
				}
			}
			try {
				// Wait for the next signal from the timer
				synchronized (this) {
					wait(variables.timerInterval);
				}
			} catch (InterruptedException ignore) {}
		}
	}

	void setNextUpdate() {
		Calendar calLast = Calendar.getInstance();
		Calendar calNext = Calendar.getInstance();
		dateUtils.setNextUpdate(variables);
		calLast.setTimeInMillis(variables.lastUpdate);
		calNext.setTimeInMillis(variables.nextUpdate);
		if (calLast.get(Calendar.DAY_OF_YEAR) != calNext.get(Calendar.DAY_OF_YEAR)) {
			// Run the workload update at the end of the workday
			doWorkload.set(true);
		}
		log("Next update: " + dateUtils.formatter(variables.nextUpdate, dateUtils.FORMAT_DATETIME));
	}

	public void start() throws Exception {
		launcher.initialize();
	}

	public static void start(String[] args) {
		// store the args
		commandLineArgs = args;
		// the main routine is only here so I can also run the app from the command line
		launcher.initialize();
	}

	public void stop() throws Exception {
		launcher.terminate();
	}

	public static void stop(String[] args) {
		launcher.terminate();
	}

	/**
	* Cleanly stop the engine.
	*/
	void terminate() {
		if (stopped.compareAndSet(false, true)) {
			synchronized (this) {
				notifyAll();
			}
			for (Thread thread : Thread.getAllStackTraces().keySet()) {
				try {
					if (thread.getName().equals("PJServer")) {
						// Wait for the thread to close
						log(JOptionPane.INFORMATION_MESSAGE,
							"Terminate", "Waiting for task to complete.");
						thread.join();
					}
				} catch (InterruptedException ignore) {
				}
			}
		}
		super.quit();
	}
}
