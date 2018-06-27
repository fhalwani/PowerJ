package ca.eorla.fhalwani.powerj;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JComponent;

class ChartLoad extends JComponent {
	private static final long serialVersionUID = 5595937741842821349L;
	private boolean hasImage = false;
	private String title = "";
	private BufferedImage image = null;
	protected ArrayList<DataWorkload> list = new ArrayList<DataWorkload>();
	
	ChartLoad() {
		super();
		image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				paintImage();
			}
		});
	}

	/** Check if mouse was clicked on which personnel bar. */
	String getMessage(int x, int y) {
		String message = "";
		if (hasImage) {
			Point p = new Point(x,y);
			DataWorkload dataItem = new DataWorkload();
			try {
				for (int i = 0; i < list.size(); i++) {
					dataItem = list.get(i);
			    	if (dataItem.region.contains(p)) {
			    		message = dataItem.name + " today: " + dataItem.casesToday
			    				+ ", yesterday: " + dataItem.casesYesterday
			    				+ ", before: " + dataItem.casesOld;
			    		break;
			    	}
				}
			} catch (NullPointerException ignore) {
				// Occasionally fires premature before initialization
			}
		}
		return message;
	}

	/** Check if mouse was clicked on which personnel bar. */
	int getPersonID(int x, int y) {
		int prsID = 0;
		if (hasImage) {
			Point p = new Point(x,y);
			DataWorkload dataItem = new DataWorkload();
			for (int i = 0; i < list.size(); i++) {
				dataItem = list.get(i);
		    	if (dataItem.region.contains(p)) {
		    		prsID = dataItem.personID;
		    		break;
		    	}
			}
		}
		return prsID;
	}

	private ArrayList<Integer> getTicks(int noTicks) {
	    int lowest = 0;
	    int highest = 0;
	    ArrayList<Integer> yTicks = new ArrayList<Integer>();
		DataWorkload dataItem = new DataWorkload();
		for (int i = 0; i < list.size(); i++) {
			dataItem = list.get(i);
	        if (highest < dataItem.casesToday + dataItem.casesYesterday + dataItem.casesOld) {
	        	highest = dataItem.casesToday + dataItem.casesYesterday + dataItem.casesOld;
	        }
		}
		if (lowest >= highest) {
			highest = lowest + 10;
		}
		double range = highest - lowest;
		double unroundedTickSize = range/(noTicks-1);
		double x = Math.ceil(Math.log10(unroundedTickSize)-1);
		double pow10x = Math.pow(10, x);
		double roundedTickSize = Math.ceil(unroundedTickSize / pow10x) * pow10x;
		lowest = (int)roundedTickSize;
		yTicks.add(lowest);
		for (int i = 1; i < noTicks; i++) {
			lowest += (int)roundedTickSize;
			yTicks.add(lowest);
			if (lowest > highest) {
				// No need for the other extra ticks
				// Otherwise, we get 2 extra unnecessary ticks that dwarf the graph
				break;
			}
		}
		return yTicks;
	}

	/** Display BufferedImage */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
	}
	
	/** Draws BufferedImage */
	void paintImage() {
		if (!hasImage) return;
		int padXL = 120, padXR = 35, padYT = 25, padYB = 20;
		int x0 = 0, x1 = 0, x2 = 0, y1 = 0;
		int noTicks = 6, dataSize = 0;
		double xInc = 0, yInc = 0, barRatio = 0, barHeight = 0;
		String label = "0";
		Rectangle area = new Rectangle(0, 0, getWidth(), getHeight());
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, 1, 1);
		DataWorkload dataItem = new DataWorkload();
		// Create a pretty Y-axis scale
		ArrayList<Integer> yTicks = getTicks(noTicks);
		noTicks = yTicks.size();
		image = new BufferedImage(area.width, area.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, area.width, area.height);
		// Draw 2 ordinates (left and right)
		g2d.setColor(Color.BLACK);
		g2d.draw(new Line2D.Double(padXL, padYT, padXL, area.height -padYB));
		g2d.draw(new Line2D.Double(area.width - padXR, padYT, area.width - padXR, area.height -padYB));
		// Draw abscissa
		g2d.draw(new Line2D.Double(padXL-3, area.height -padYB, area.width -padXR +3, area.height -padYB));
		// Draw scale
		dataSize = list.size();
		if (dataSize > 0) {
			if (dataSize > 25) {
				// Limit to top 25 workers
				dataSize = 25;
			}
			xInc = (double) (area.width - padXL - padXR) / dataSize;
			if (xInc > 70) {
				xInc = 70;
			}
		}
		if (noTicks > 0) {
			yInc = (double) ((area.height - padYT - padYB) / noTicks);
			barRatio = (yInc * (noTicks-1)) / (yTicks.get(noTicks-1) - yTicks.get(0));
		}
		g2d.setFont(Constants.FONT_SMALL);
		FontMetrics fm = g2d.getFontMetrics();
		for (int i = 0; i < noTicks; i++) {
			// Draw vertical tick above 0
			x1 = padXL -3;
			x2 = area.width -padXR -3;
			y1 = (int) (area.height -padYB - (yInc * (i+1)));
			g2d.draw(new Line2D.Double(x1, y1, x1 +6, y1));
			g2d.draw(new Line2D.Double(x2, y1, x2 +6, y1));
			// Draw vertical label above 0
			label = ""+ yTicks.get(i);
			x1 = padXL - fm.stringWidth(label) -5;
			x2 = area.width -padXR +5;
			g2d.drawString(label, x1, y1);
			g2d.drawString(label, x2, y1);
		}
		for (int i = 0; i < dataSize; i++) {
			dataItem = list.get(i);
	    	if (dataItem.casesToday + dataItem.casesYesterday + dataItem.casesOld > 0) {
				label = dataItem.name;
				x1 = (int) (padXL + (xInc * (x0 + 1)) - (xInc / 3) - fm.stringWidth(label));
				y1 = area.height - 3;
				g2d.setColor(Color.BLACK);
				g2d.drawString(label, x1, y1);
				// Draw vertical bar of all cases grossed, using today color
				barHeight = (dataItem.casesToday + dataItem.casesYesterday + dataItem.casesOld) * barRatio;
				rect.setRect(xInc * x0 + padXL, area.height -padYB -barHeight, xInc, barHeight);
				g2d.setColor(Constants.COLOR_LIGHT_BLUE);
				g2d.fill(rect);
				// save this rectangle as a hot spot
				dataItem.region = rect.getBounds();
				// Draw vertical bar of yesterday+old cases grossed
				barHeight = (dataItem.casesYesterday +dataItem.casesOld) * barRatio;
				rect.setRect(xInc * x0 + padXL, area.height -padYB -barHeight, xInc, barHeight);
				g2d.setColor(Constants.COLOR_AZURE_BLUE);
				g2d.fill(rect);
				// Draw vertical bar of old cases grossed
				barHeight = dataItem.casesOld * barRatio;
				rect.setRect(xInc * x0 + padXL, area.height -padYB -barHeight, xInc, barHeight);
				g2d.setColor(Constants.COLOR_DARK_BLUE);
				g2d.fill(rect);
				x0++;
			}
		}
		// Draw header on top of finished chart
		g2d.setColor(Color.BLACK);
		g2d.setFont(Constants.FONT_LARGE);
		fm = g2d.getFontMetrics();
		x1 = (area.width - fm.stringWidth(title)) / 2;
		y1 = padYT / 2;
		g2d.drawString(title, x1, y1);
		// Draw color labels on top of finished chart
		g2d.setPaint(Constants.COLOR_LIGHT_BLUE);
		g2d.fillRect(2, 2, (int)yInc, (int)yInc);
		g2d.setPaint(Constants.COLOR_AZURE_BLUE);
		g2d.fillRect(2, (int)(yInc +4), (int)yInc, (int)yInc);
		g2d.setPaint(Constants.COLOR_DARK_BLUE);
		g2d.fillRect(2, (int)(yInc*2 +6), (int)yInc, (int)yInc);
		// Draw labels
		g2d.setColor(Color.BLACK);
		g2d.setFont(Constants.FONT_SMALL);
		label = "Today";
		g2d.drawString(label, (int) (yInc +4), (int) (yInc));		
		label = "Yesterday";
		g2d.drawString(label, (int) (yInc +4), (int) (yInc *2));		
		label = "Beforeday";
		g2d.drawString(label, (int) (yInc +4), (int) (yInc *3));		
		g2d.dispose();
		repaint();
	}
	
	void setData(ArrayList<DataWorkload> data, int status) {
		list = data;
		switch (status) {
		case Constants.STATUS_Accession:
			title = "Grossing Workload";
			break;
		case Constants.STATUS_Gross:
			title = "Embedding Workload";
			break;
		case Constants.STATUS_Embed:
			title = "Microtomy Workload";
			break;
		case Constants.STATUS_Microtomy:
			title = "Staining Workload";
			break;
		case Constants.STATUS_Slides:
			title = "Routing Workload";
			break;
		case Constants.STATUS_Routed:
			title = "Diagnosis Workload";
			break;
		case Constants.STATUS_Histology:
			title = "Histology Workload";
			break;
		default:
			title = "Cases Workload";
		}
		hasImage = true;
		paintImage();
	}
}
