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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JComponent;

class ChartFlow extends JComponent {
	private static final long serialVersionUID = -1726535700534450471L;
	private boolean hasImage = false;
	private String title = "";
	private BufferedImage image = null;
	protected ArrayList<DataWorkflow> list = new ArrayList<DataWorkflow>();
	
	ChartFlow() {
		super();
		image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				paintImage();
			}
		});
	}

	/** Check if mouse was clicked on one of the bars */
	String getMessage(int x, int y) {
		String message = "";
		if (hasImage) {
			DataWorkflow dataItem = new DataWorkflow();
			try {
				Point p = new Point(x,y);
				for (int i = 0; i < list.size(); i++) {
					dataItem = list.get(i);
			    	if (dataItem.region.contains(p)) {
			    		message = dataItem.date + " in: " + dataItem.noIn
			    				+ ", out: " + dataItem.noOut
			    				+ ", pending: " + dataItem.noPending;
			    		break;
			    	}
				}
			} catch (NullPointerException ignore) {
				// Occasionally fires premature before initialization
			}
		}
		return message;
	}

	private ArrayList<Integer> getTicksLeft(int noTicks) {
	    int low = 9999999;
	    int high = 0;
		DataWorkflow dataItem = new DataWorkflow();
		ArrayList<Integer> yTicks = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++) {
			dataItem = list.get(i);
	        if (low > dataItem.noIn) {
	        	low = dataItem.noIn;
	        }
	        if (high < dataItem.noIn) {
	        	high = dataItem.noIn;
	        }
	        if (low > dataItem.noOut) {
	        	low = dataItem.noOut;
	        }
	        if (high < dataItem.noOut) {
	        	high = dataItem.noOut;
	        }
		}
		if (low < 0) {
			low = 0;
		}
		if (low >= high) {
			high = low + 10;
		}
		double range = high - low;
		double unroundedTickSize = range/(noTicks-1);
		double x = Math.ceil(Math.log10(unroundedTickSize)-1);
		double pow10x = Math.pow(10, x);
		double roundedTickSize = Math.ceil(unroundedTickSize / pow10x) * pow10x;
		int tickSize = (int)roundedTickSize;
		if (low <= 0) {
			low = tickSize;
		} else if (tickSize > 0) {
			int first = tickSize;
			while (first + tickSize < low) {
				first += tickSize;
			}
			low = first;
		}
		yTicks.add(low);
		for (int i = 1; i < noTicks; i++) {
			low += tickSize;
			yTicks.add(low);
		}
		return yTicks;
	}

	private ArrayList<Integer> getTicksRight(int noTicks) {
	    int low = 9999999;
	    int high = 0;
		ArrayList<Integer> yTicks = new ArrayList<Integer>();
		DataWorkflow dataItem = new DataWorkflow();
		for (int i = 0; i < list.size(); i++) {
			dataItem = list.get(i);
	        if (low > dataItem.noPending) {
	        	low = dataItem.noPending;
	        }
	        if (high < dataItem.noPending) {
	        	high = dataItem.noPending;
	        }
		}
		if (low < 0) {
			low = 0;
		}
		if (high <= low) {
			high = low + 10;
		}
		double range = high - low;
		double unroundedTickSize = range/(noTicks-1);
		double x = Math.ceil(Math.log10(unroundedTickSize)-1);
		double pow10x = Math.pow(10, x);
		double roundedTickSize = Math.ceil(unroundedTickSize / pow10x) * pow10x;
		int tickSize = (int)roundedTickSize;
		if (low <= 0) {
			low = tickSize;
		} else if (tickSize > 0) {
			int first = tickSize;
			while (first + tickSize < low) {
				first += tickSize;
			}
			low = first;
		}
		yTicks.add(low);
		for (int i = 1; i < noTicks; i++) {
			low += tickSize;
			yTicks.add(low);
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
		final int padXL = 80, padXR = 40, padYT = 25, padYB = 20;
		int x0 = 0, x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		int noTicks = 0;
		double xInc = 0, yInc = 0, barRatio = 0, barHeight = 0;
		Rectangle area = new Rectangle(0, 0, getWidth(), getHeight());
		String label = "";
		DataWorkflow dataItem = new DataWorkflow();
		// Create left Y-axis scale
		ArrayList<Integer> yTicks = getTicksLeft(6);
		noTicks = yTicks.size();
		image = new BufferedImage(area.width, area.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(Color.WHITE);
		g2d.fillRect(area.x, area.y, area.width, area.height);
		// Draw left ordinate
		g2d.setColor(Color.BLACK);
		g2d.draw(new Line2D.Double(padXL, padYT, padXL, area.height -padYB));
		// Draw abscissa
		g2d.draw(new Line2D.Double(padXL-3, area.height -padYB, area.width -padXR +3, area.height -padYB));
		// Draw scale
		xInc = (double) (area.width - padXL - padXR) / list.size();
		if (xInc > 70) {
			xInc = 70;
		}
		yInc = (double) (area.height - padYT - padYB) / noTicks;
		barRatio = (yInc * (noTicks-1)) / (yTicks.get(noTicks-1) - yTicks.get(0));
		g2d.setFont(Constants.FONT_SMALL);
		FontMetrics fm = g2d.getFontMetrics();
		for (int i = 0; i < noTicks; i++) {
			// Draw vertical tick above 0
			x1 = padXL -3;
			y1 = (int) (area.height -padYB - (yInc * (i+1)));
			g2d.draw(new Line2D.Double(x1, y1, x1 +6, y1));
			// Draw vertical label above 0
			label = yTicks.get(i).toString();
			x1 = padXL - fm.stringWidth(label) -5;
			g2d.drawString(label, x1, y1);
		}
		Rectangle2D.Double rect = new Rectangle2D.Double(0, 0, 1, 1);
		for (int i = 0; i < list.size(); i++) {
			dataItem = list.get(i);
			// Draw label at bottom consisting of a short date
			x1 = (int) (padXL + (xInc * (x0+1)) -fm.stringWidth(dataItem.date));
			y1 = area.height - 3;
			g2d.setColor(Color.BLACK);
			g2d.drawString(dataItem.date, x1, y1);
			if (dataItem.noIn > dataItem.noOut) {
				// Draw the taller vertical bar of new cases first, then superimpose the smaller bar of cases grossed
				if (dataItem.noIn > yTicks.get(0)) {
					barHeight = (dataItem.noIn - yTicks.get(0)) * barRatio + yInc;
				} else {
					// 1st tick is always dwarfed
					barHeight = dataItem.noIn * yInc / yTicks.get(0);
				}
				rect.setRect(xInc * x0 + padXL, area.height -padYB -barHeight, xInc, barHeight);
				g2d.setColor(Constants.COLOR_LIGHT_BLUE);
				g2d.fill(rect);
				// save this rectangle as a hot spot
				dataItem.region = rect.getBounds();
				// Draw the shorter bar of cases out
				if (dataItem.noOut > 0) {
					if (dataItem.noOut > yTicks.get(0)) {
						barHeight = (dataItem.noOut - yTicks.get(0)) * barRatio + yInc;
					} else {
						// 1st tick is always dwarfed
						barHeight = dataItem.noOut * yInc / yTicks.get(0);
					}
					rect.setRect(xInc * x0 + padXL, area.height -padYB -barHeight, xInc, barHeight);
					g2d.setColor(Constants.COLOR_AZURE_BLUE);
					g2d.fill(rect);
				}
			} else {
				// Draw the taller vertical bar of grossed cases first, then superimpose the smaller bar of new cases 
				if (dataItem.noOut > yTicks.get(0)) {
					barHeight = (dataItem.noOut - yTicks.get(0)) * barRatio + yInc;
				} else {
					// 1st tick is always dwarfed
					barHeight = dataItem.noOut * yInc / yTicks.get(0);
				}
				rect.setRect(xInc * x0 + padXL, area.height -padYB -barHeight, xInc, barHeight);
				g2d.setColor(Constants.COLOR_AZURE_BLUE);
				g2d.fill(rect);
				// save this rectangle as a hot spot
				dataItem.region = rect.getBounds();
				// Draw the shorter bar of new cases
				if (dataItem.noIn > 0) {
					if (dataItem.noIn > yTicks.get(0)) {
						barHeight = (dataItem.noIn - yTicks.get(0)) * barRatio + yInc;
					} else {
						// 1st tick is always dwarfed
						barHeight = dataItem.noIn * yInc / yTicks.get(0);
					}
					rect.setRect(xInc * x0 + padXL, area.height -padYB -barHeight, xInc, barHeight);
					g2d.setColor(Constants.COLOR_LIGHT_BLUE);
					g2d.fill(rect);
				}
			}
			x0++;
		}
		// Calculate the scale for the pending cases separately
		yTicks = getTicksRight(6);
		noTicks = yTicks.size();
		// Draw right ordinate
		g2d.setColor(Color.BLACK);
		g2d.draw(new Line2D.Double(area.width - padXR, padYT, area.width - padXR, area.height -padYB));
		// Draw scale
		barRatio = (yInc * (noTicks-1)) / (yTicks.get(noTicks-1) - yTicks.get(0));
		g2d.setFont(Constants.FONT_SMALL);
		fm = g2d.getFontMetrics();
		for (int i = 0; i < noTicks; i++) {
			// Draw vertical tick above 0
			x1 = area.width - padXR -3;
			y1 = (int) (area.height -padYB - (yInc * (i+1)));
			g2d.draw(new Line2D.Double(x1, y1, x1 +6, y1));
			// Draw vertical label above 0
			label = yTicks.get(i).toString();
			x1 += 8;
			g2d.drawString(label, x1, y1);
		}
		Line2D.Double line = new Line2D.Double(0, 0, 1, 1);
		Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, 1, 1);
		g2d.setColor(Constants.COLOR_DARK_BLUE);
		x0 = 0;
		for (int i = 0; i < list.size(); i++) {
			dataItem = list.get(i);
			// Draw the cases pending line/circles
			x1 = (int) (xInc * x0 + padXL + (xInc /2));
			if (dataItem.noPending > yTicks.get(0)) {
				barHeight = (dataItem.noPending - yTicks.get(0)) * barRatio + yInc;
			} else {
				// 1st tick is always dwarfed
				barHeight = dataItem.noPending * yInc / yTicks.get(0);
			}
			y1 = (int) (area.height -padYB -barHeight);
			circle.setFrame(x1-2, y1-2, 4, 4);
			g2d.fill(circle);
			if (x0 > 0) {
				// First point has no line, all others get a line drawn to the previous point as well
				line.x2 = x1;
				line.y2 = y1;
				g2d.draw(line);
			}
			// Save it for next line
			line.x1 = x1;
			line.y1 = y1;
			x0++;
		}
		// Draw title on top of finished chart
		g2d.setColor(Color.BLACK);
		g2d.setFont(Constants.FONT_LARGE);
		fm = g2d.getFontMetrics();
		x2 = (area.width - fm.stringWidth(title)) /2;
		y2 = padYT / 2;
		g2d.drawString(title, x2, y2);
		// Draw legend on top of finished chart
		if (yInc > 25) {
			// Shrink the label size
			yInc = 25;
		}
		g2d.setPaint(Constants.COLOR_LIGHT_BLUE);
		g2d.fillRect(2, 2, (int)yInc, (int)yInc);
		g2d.setPaint(Constants.COLOR_AZURE_BLUE);
		g2d.fillRect(2, (int)(yInc +4), (int)yInc, (int)yInc);
		g2d.setPaint(Constants.COLOR_DARK_BLUE);
		// Draw a line with circles for the Totals legend
		line.x1 = 2;
		line.y1 = (int)(yInc*2.5 +6);
		line.x2 = line.x1 +(int)yInc;
		line.y2 = line.y1;
		g2d.draw(line);
		circle.setFrame(line.x1 -2, line.y1 -2, 4, 4);
		g2d.fill(circle);
		circle.setFrame(line.x2-2, line.y2-2, 4, 4);
		g2d.fill(circle);
		// Draw labels
		g2d.setColor(Color.BLACK);
		g2d.setFont(Constants.FONT_SMALL);
		label = "In";
		g2d.drawString(label, (int) (yInc +6), (int) (yInc -10));
		label = "Out";
		g2d.drawString(label, (int) (yInc +6), (int) (yInc *2 -10));
		label = "Pend";
		g2d.drawString(label, (int) (yInc +6), (int) (yInc *3 -10));
		g2d.dispose();
		repaint();
	}
	
	void setData(ArrayList<DataWorkflow> data, int status) {
		list = data;
		switch (status) {
		case Constants.STATUS_Accession:
			title = "Grossing Workflow";
			break;
		case Constants.STATUS_Gross:
			title = "Embedding Workflow";
			break;
		case Constants.STATUS_Embed:
			title = "Microtomy Workflow";
			break;
		case Constants.STATUS_Microtomy:
			title = "Staining Workflow";
			break;
		case Constants.STATUS_Slides:
			title = "Routing Workflow";
			break;
		case Constants.STATUS_Routed:
			title = "Diagnosis Workflow";
			break;
		case Constants.STATUS_Histology:
			title = "Histology Workflow";
			break;
		default:
			title = "Cases Workflow";
		}
		hasImage = true;
		paintImage();
	}
}
