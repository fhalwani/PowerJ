package ca.eorla.fhalwani.powerj;

class PJTest extends PowerJ {
	private static final long serialVersionUID = 8896446011297810565L;

	PJTest() {
		super();
		variables.autoLogin = true;
		if (variables.hasError) {
			quit();
		}
	}

    /**
     * Do the work of starting the engine
     */
    void initialize() {
		super.initialize();
		if (!variables.hasError) {
			new Scheduler(this);
//			int noCycles = 0;
//			while (noCycles < 10) {
//				// new WLManager(this, 0, "QCS-18-02484");
//				new StatsManager(this);
//				noCycles++;
//				System.out.println("Cycle: " + noCycles);
//				try {
//					Thread.sleep(15000);
//				} catch (InterruptedException e) {}
//			}
		}
		quit();
    }
    
	void log(int severity, String name, String message) {
		System.out.println("PJTest: " + name + ", " + message);
	}
	
	void log(int severity, String name, Throwable e) {
		System.out.println("PJTest: " + name + ", " + e.getLocalizedMessage());
	}
	
	public static void main(String[] args) {
        // store the args
        commandLineArgs = args;
		PJTest aFrame = new PJTest();
		aFrame.initialize();
	}

	void quit() {
		super.quit();
	}
}
