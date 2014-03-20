package fr.loicmathieu.gcviewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import com.tagtraum.perf.gcviewer.GCPreferences;
import com.tagtraum.perf.gcviewer.TimeOffsetPanel;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.TimeFormat;


public abstract class SimpleChart extends JScrollPane {
	private static final long serialVersionUID = 3851618227540047720L;

	protected Chart chart;
	protected Ruler timestampRuler;

	private boolean antiAlias;
	private GCModel model;
	private double scaleFactor = 1;
	private double runningTime;
	private TimeOffsetPanel timeOffsetPanel;


	public SimpleChart(GCModel model) {
		super();
		this.model = model;
		this.chart = new Chart();
		this.chart.setPreferredSize(new Dimension(0, 0));

		setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);
		setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_NEVER);

		DateFormat dateFormatter = new TimeFormat();
		this.timestampRuler = new Ruler(false, 0, getModel().getRunningTime(), "", dateFormatter);
		setColumnHeaderView(timestampRuler);

		// timestamp menu
		final JPopupMenu popupMenu = new JPopupMenu();
		timeOffsetPanel = new TimeOffsetPanel(popupMenu);
		popupMenu.add(timeOffsetPanel);
		final JPopupMenu timestampRulerPopup = popupMenu;
		Action setOffsetAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (timeOffsetPanel.isOffsetSet()) {
					timestampRuler.setOffset(timeOffsetPanel.getDate().getTime()/1000);
				}
				else {
					timestampRuler.setOffset(0);
				}
				timestampRuler.revalidate();
				timestampRuler.repaint();
			}
		};
		timeOffsetPanel.setOkAction(setOffsetAction);
		this.timestampRuler.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e) {
				maybePopup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				maybePopup(e);
			}

			public void maybePopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					if (timestampRuler.getOffset() != 0) {
						timeOffsetPanel.setDate(new Date((long)timestampRuler.getOffset()*1000));
						timeOffsetPanel.setOffsetSet(true);
					}
					else {
						long suggestedStartDate = getModel().getLastModified();
						if (getModel().hasDateStamp()) {
							suggestedStartDate = getModel().getFirstDateStamp().getTime();
						}
						else if (getModel().hasCorrectTimestamp()) {
							suggestedStartDate -= (long)(getModel().getRunningTime() * 1000.0d);
						}
						timeOffsetPanel.setDate(new Date(suggestedStartDate));
						timeOffsetPanel.setOffsetSet(false);
					}
					timestampRulerPopup.show(e.getComponent(), e.getX(),  e.getY());
					timeOffsetPanel.requestFocus();
				}
			}
		});
	}

	public double getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(double runningTime) {
		this.runningTime = runningTime;

		this.timestampRuler.setMaxUnit(runningTime);
		getRowHeader().revalidate();
		chart.revalidate();
	}

	public boolean isAntiAlias() {
		return antiAlias;
	}

	public void setAntiAlias(boolean antiAlias) {
		this.antiAlias = antiAlias;
	}


	public GCModel getModel() {
		return model;
	}


	public void setModel(GCModel model) {
		this.model = model;
	}

	public double getFootprint() {
		return getModel().getFootprint();
	}

	public double getMaxDuration() {
		return getModel().getPause().getMax();
	}

	public double getTotalAllocation() {
		//freed memory is an aproximation of total allocation
		return getModel().getFreedMemory() / 1024;
	}


	public double getScaleFactor() {
		return scaleFactor;
	}

	public void autoSetScaleFactor() {
		double autoScaleFactor = getViewport().getWidth() / model.getRunningTime();
		setScaleFactor(autoScaleFactor);
	}

	public void setScaleFactor(double scaleFactor) {
		this.scaleFactor = scaleFactor;
		chart.setSize(chart.getPreferredSize());
		chart.resetPolygons();
		timestampRuler.setSize((int)(getViewport().getWidth()*getScaleFactor()), (int)timestampRuler.getPreferredSize().getHeight());

		repaint();
	}


	public void applyPreferences(GCPreferences preferences) {
		setAntiAlias(preferences.getGcLineProperty(GCPreferences.ANTI_ALIAS));
	}

	/**
	 * Resets the internal cache of the chart.
	 */
	public void resetPolygonCache() {
		chart.resetPolygons();
	}

	protected class Chart extends JPanel implements ComponentListener {

		public Chart() {
			setBackground(Color.white);
			setLayout(new GridBagLayout());
			addComponentListener(this);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(scaleX(getRunningTime()), getViewport().getHeight());
		}

		private int scaleX(double d) {
			return (int) (d * getScaleFactor());
		}

		/**
		 * Reset the cached polygons of all {@link SimplePolygonChartRenderer}s stored in this chart.
		 */
		public void resetPolygons() {
			for (Component component : getComponents()) {
				if (component instanceof SimplePolygonChartRenderer) {
					((SimplePolygonChartRenderer)component).resetPolygon();
				}
			}
		}

		@Override
		public void componentResized(ComponentEvent e) {
			resetPolygons();
		}

		@Override
		public void componentMoved(ComponentEvent e) {
			// not interested
		}

		@Override
		public void componentShown(ComponentEvent e) {
			// not interested
		}

		@Override
		public void componentHidden(ComponentEvent e) {
			// not interested
		}

	}

	protected class Ruler extends JPanel {
		private boolean vertical;
		private double minUnit;
		private double maxUnit;
		private final double log10 = Math.log(10);
		private Font font;
		private Format formatter;
		private String longestString;
		private String unitName;
		private int minHalfDistance;
		private double offset;

		public Ruler(boolean vertical, double minUnit, double maxUnit, String unitName) {
			this(vertical,  minUnit, maxUnit, unitName, NumberFormat.getInstance());
		}

		public Ruler(boolean vertical, double minUnit, double maxUnit, String unitName, Format formatter) {
			setUnitName(unitName);
			this.formatter = formatter;
			setVertical(vertical);
			setMinUnit(minUnit);
			setMaxUnit(maxUnit);
			font = new Font("sans-serif", Font.PLAIN, 10);
		}

		@Override
		public void setSize(int width, int height) {
			super.setSize(width, height);
			configureFormatter();
		}

		@Override
		public Dimension getPreferredSize() {
			FontMetrics fm = getToolkit().getFontMetrics(font);
			configureFormatter();
			int minWidth = fm.stringWidth(longestString) + 5;
			Dimension bestSize = null;
			if (isVertical()) {
				bestSize = new Dimension(minWidth, getHeight());
			} else {
				bestSize = new Dimension((int) (getRunningTime() * getScaleFactor()), fm.getHeight());
				minHalfDistance = minWidth;
			}
			return bestSize;
		}

		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponents(g);
			configureFormatter();
			double lineDistance = getLineDistance();
			Rectangle clip = g.getClipBounds();
			g.clearRect(clip.x, clip.y, clip.width, clip.height);
			g.setColor(Color.black);
			if (isVertical()) {
				double halfLineDistance = lineDistance / 2.0d;
				for (double line = getHeight() - (minUnit % lineDistance); line > 0; line -= lineDistance) {
					g.drawLine(0, (int) line, getWidth(), (int) line);
				}
				for (double line = getHeight() - ((minUnit - halfLineDistance) % lineDistance); line > 0; line -= lineDistance) {
					int inset = 3;
					g.drawLine(inset, (int) line, getWidth() - inset, (int) line);
				}
				String number = null;
				for (double line = getHeight() - (minUnit % lineDistance); line > 0; line -= lineDistance) {
					g.setFont(font);
					String newNumber = format((getHeight() - line) / getPixelsPerUnit()) + getUnitName();
					if (!newNumber.equals(number)) {
						g.drawString(newNumber, 2, (int) line - 2);
					}
					number = newNumber;
				}
			} else {
				double halfLineDistance = lineDistance / 2.0d;
				for (double line = (minUnit % lineDistance); line < getWidth(); line += lineDistance) {
					g.drawLine((int) line, 0, (int) line, getHeight());
				}
				for (double line = (minUnit - halfLineDistance) % lineDistance; line < getWidth(); line += lineDistance) {
					int inset = 3;
					g.drawLine((int) line, inset, (int) line, getHeight() - inset);
				}
				String number = null;
				for (double line = (minUnit % lineDistance); line < getWidth(); line += lineDistance) {
					g.setFont(font);
					String newNumber = format(line / getPixelsPerUnit()) + getUnitName();
					if (!newNumber.equals(number)) {
						g.drawString(newNumber, ((int) line) + 3, getHeight() - 2);
					}
					number = newNumber;
				}
			}
		}

		public double getOffset() {
			return offset;
		}

		public void setOffset(double offset) {
			this.offset = offset;
		}

		private String format(final double val) {
			final double offsetValue = val + offset;
			String s = null;
			if (formatter instanceof NumberFormat) {
				s = ((NumberFormat)formatter).format(offsetValue);
			}
			else if (formatter instanceof DateFormat) {
				final Date date = new Date(Math.round(offsetValue) * 1000);
				s = ((DateFormat)formatter).format(date);
			}
			return s;
		}

		private double getLineDistance() {
			if (formatter instanceof NumberFormat) {
				return getNumberLineDistance();
			}
			else if (formatter instanceof DateFormat) {
				return getDateLineDistance();
			}
			return 0.0d;
		}

		private double getDateLineDistance() {
			double lineDistance = getPixelsPerUnit();
			if (isVertical()) {
				if (lineDistance < 20)
				{
					lineDistance *= 10.0d; // 10sec
				}
				if (lineDistance < 20)
				{
					lineDistance *= 3.0d; // 30sec
				}
				if (lineDistance < 20)
				{
					lineDistance *= 2.0d; // 1min
				}
				if (lineDistance < 20)
				{
					lineDistance *= 2.0d; // 2min
				}
				if (lineDistance < 20)
				{
					lineDistance *= 5.0d; // 10min
				}
				if (lineDistance < 20)
				{
					lineDistance *= 2.0d; // 20min
				}
				if (lineDistance < 20)
				{
					lineDistance *= 3.0d; // 1h
				}
				if (lineDistance < 20) {
					double oneHourDistance = lineDistance;
					while (lineDistance < 20) {
						lineDistance += oneHourDistance;
					}
				}
			} else {
				if (lineDistance < minHalfDistance * 2)
				{
					lineDistance *= 10.0d; // 10sec
				}
				if (lineDistance < minHalfDistance * 2)
				{
					lineDistance *= 3.0d; // 30sec
				}
				if (lineDistance < minHalfDistance * 2)
				{
					lineDistance *= 2.0d; // 1min
				}
				if (lineDistance < minHalfDistance * 2)
				{
					lineDistance *= 2.0d; // 2min
				}
				if (lineDistance < minHalfDistance * 2)
				{
					lineDistance *= 5.0d; // 10min
				}
				if (lineDistance < minHalfDistance * 2)
				{
					lineDistance *= 2.0d; // 20min
				}
				if (lineDistance < minHalfDistance * 2)
				{
					lineDistance *= 3.0d; // 1h
				}
				if (lineDistance < minHalfDistance * 2) {
					double oneHourDistance = lineDistance;
					while (lineDistance < minHalfDistance * 2) {
						lineDistance += oneHourDistance;
					}
				}
			}
			return lineDistance;
		}

		private double getNumberLineDistance() {
			double log10PixelPerUnit = Math.log(getPixelsPerUnit()) / log10;
			double lineDistance = getPixelsPerUnit() * Math.pow(10, Math.ceil(-log10PixelPerUnit) + 1);
			if (isVertical()) {
				while (lineDistance < 20) {
					lineDistance *= 10.0d;
				}
			} else {
				while (lineDistance < minHalfDistance * 2) {
					lineDistance *= 10.0d;
				}
			}
			return lineDistance;
		}

		private double getPixelsPerUnit() {
			double pixelPerUnit = (isVertical()?getHeight()/(maxUnit - minUnit):(getRunningTime() * getScaleFactor() / (maxUnit - minUnit)));
			return pixelPerUnit;
		}

		public void setMinUnit(double minUnit) {
			this.minUnit = minUnit;
			configureFormatter();
		}

		public void setMaxUnit(double maxUnit) {
			this.maxUnit = maxUnit;
			configureFormatter();
		}

		public void configureFormatter() {
			if (formatter instanceof NumberFormat) {
				double digits = Math.log(maxUnit) / log10;
				if (digits < 1) {
					((NumberFormat)formatter).setMaximumFractionDigits((int) Math.abs(digits) + 2);
					((NumberFormat)formatter).setMinimumFractionDigits((int) Math.abs(digits) + 2);
				} else {
					((NumberFormat)formatter).setMaximumFractionDigits(0);
					((NumberFormat)formatter).setMinimumFractionDigits(0);
				}
			}
			longestString = format(maxUnit);
			if (unitName != null) {
				longestString += unitName;
			}
			invalidate();
		}

		public boolean isVertical() {
			return vertical;
		}

		public void setVertical(boolean vertical) {
			this.vertical = vertical;
		}

		public String getUnitName() {
			return unitName;
		}

		public void setUnitName(String unitName) {
			this.unitName = unitName;
		}
	}

}
