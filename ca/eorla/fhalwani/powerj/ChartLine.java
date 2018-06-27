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

class ChartLine extends JComponent {
	private static final long serialVersionUID = 7963731380154282123L;
	private boolean hasImage = false;
	private String title = "";
	private BufferedImage image = null;
	private ArrayList<DataSlides> dataList = new ArrayList<DataSlides>();

	ChartLine() {
		super();
		image = new BufferedImage(1000, 200, BufferedImage.TYPE_INT_RGB);
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
			DataSlides dataItem = new DataSlides();
			try {
				for (int i = 0; i < dataList.size(); i++) {
					dataItem = dataList.get(i);
			    	if (dataItem.region.contains(p)) {
			    		message = dataItem.name + ": in " + dataItem.noIn
			    				+ ", out: " + dataItem.noOut
			    				+ ", pending: " + dataItem.noPending + " slides";
			    		break;
			    	}
				}
			} catch (NullPointerException ignore) {
				// Occasionally fires premature before initialization
			}
		}
		return message;
	}

	private ArrayList<Integer> getTicks(int noTicks) {
	    int lowest = 0;
	    int highest = 0;
	    ArrayList<Integer> yTicks = new ArrayList<Integer>();
		DataSlides dataItem = new DataSlides();
		for (int i = 0; i < dataList.size(); i++) {
			dataItem = dataList.get(i);
	        if (highest < dataItem.noIn) {
	        	highest = dataItem.noIn;
	        }
	        if (highest < dataItem.noOut) {
	        	highest = dataItem.noOut;
	        }
	        if (highest < dataItem.noPending) {
	        	highest = dataItem.noPending;
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
		DataSlides dataItem = new DataSlides();
		// Create a pretty Y-axis scale
		ArrayList<Integer> yTicks = getTicks(noTicks);
		noTicks = yTicks.size();
		dataSize = dataList.size();
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
		Rectangle2D.Double rect = new Rectangle2D.Double(padXL, padYT, xInc, area.height -padYB);
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
		Line2D.Double lineIn = new Line2D.Double(0, 0, 1, 1);
		Line2D.Double lineOut = new Line2D.Double(0, 0, 1, 1);
		Line2D.Double linePending = new Line2D.Double(0, 0, 1, 1);
		Ellipse2D.Double circle = new Ellipse2D.Double(0, 0, 1, 1);
		for (int i = 0; i < dataSize; i++) {
			dataItem = dataList.get(i);
	        if (dataItem.noIn > 0 || dataItem.noOut > 0 || dataItem.noPending > 0) {
				label = dataItem.name;
				x1 = (int) (padXL + (xInc * (x0 + 1)) - (xInc / 3) - fm.stringWidth(label));
				y1 = area.height - 3;
				g2d.setColor(Color.BLACK);
				g2d.drawString(label, x1, y1);
				// Draw the noIn line/circles
				g2d.setColor(Color.GREEN);
				x1 = (int) (xInc * x0 + padXL + (xInc /2));
				if (dataItem.noIn > yTicks.get(0)) {
					barHeight = (dataItem.noIn - yTicks.get(0)) * barRatio + yInc;
				} else {
					// 1st tick is always dwarfed
					barHeight = dataItem.noIn * yInc / yTicks.get(0);
				}
				y1 = (int) (area.height -padYB -barHeight);
				circle.setFrame(x1-2, y1-2, 4, 4);
				g2d.fill(circle);
				if (x0 > 0) {
					// First point has no line, all others get a line drawn to the previous point as well
					lineIn.x2 = x1;
					lineIn.y2 = y1;
					g2d.draw(lineIn);
				}
				// Save it for next line
				lineIn.x1 = x1;
				lineIn.y1 = y1;
				// Draw the noOut line/circles
				g2d.setColor(Constants.AMBER);
				if (dataItem.noOut > yTicks.get(0)) {
					barHeight = (dataItem.noOut - yTicks.get(0)) * barRatio + yInc;
				} else {
					// 1st tick is always dwarfed
					barHeight = dataItem.noOut * yInc / yTicks.get(0);
				}
				y1 = (int) (area.height -padYB -barHeight);
				circle.setFrame(x1-2, y1-2, 4, 4);
				g2d.fill(circle);
				if (x0 > 0) {
					// First point has no line, all others get a line drawn to the previous point as well
					lineOut.x2 = x1;
					lineOut.y2 = y1;
					g2d.draw(lineOut);
				}
				// Save it for next line
				lineOut.x1 = x1;
				lineOut.y1 = y1;
				g2d.setColor(Color.RED);
				// Draw the noPending line/circles
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
					linePending.x2 = x1;
					linePending.y2 = y1;
					g2d.draw(linePending);
				}
				// Save it for next line
				linePending.x1 = x1;
				linePending.y1 = y1;
				// save this rectangle as a hot spot
				dataItem.region = rect.getBounds();
				// Next X step
				x0++;
				rect.x += xInc;
	        }
		}
		// Draw title on top of finished chart
		g2d.setColor(Color.BLACK);
		g2d.setFont(Constants.FONT_LARGE);
		fm = g2d.getFontMetrics();
		x2 = (area.width - fm.stringWidth(title)) /2;
		int y2 = padYT / 2;
		g2d.drawString(title, x2, y2);
		// Draw legend on top of finished chart
		if (yInc > 25) {
			// Shrink the label size
			yInc = 25;
		}
		g2d.setColor(Color.GREEN);
		circle.setFrame(3, 3, 6, 6);
		g2d.fill(circle);
		lineIn.x1 = 0;
		lineIn.y1 = 5;
		lineIn.x2 = 10;
		lineIn.y2 = 5;
		g2d.draw(lineIn);
		g2d.setColor(Constants.AMBER);
		circle.setFrame(3, (int)(yInc + 6), 6, 6);
		g2d.fill(circle);
		lineIn.y1 += yInc + 4;
		lineIn.y2 = lineIn.y1;
		g2d.draw(lineIn);
		g2d.setColor(Color.RED);
		circle.setFrame(3, (int)((yInc + 6) * 2), 6, 6);
		g2d.fill(circle);
		lineIn.y1 += yInc + 4;
		lineIn.y2 = lineIn.y1;
		g2d.draw(lineIn);
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

	void setData(ArrayList<DataSlides> alist, String s) {
		dataList = alist;
		title = s;
		hasImage = true;
		paintImage();
	}
}
