package fr.loicmathieu.gcviewer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.text.NumberFormat;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;


public class CreationRateCharteRenderer extends SimplePolygonChartRenderer {
	private static final long serialVersionUID = -1967642328721480847L;

	public static final Paint DEFAULT_LINEPAINT = Color.BLUE;

	public CreationRateCharteRenderer(SimpleChart chart) {
		super(chart);
		setLinePaint(DEFAULT_LINEPAINT);
		setDrawPolygon(false);
		setDrawLine(true);
	}

	@Override
	public Polygon computePolygon(SimpleChart chart, GCModel model) {
		ScaledPolygon polygon = createScaledPolygon();
		int postPost = 0;
		double postEventTimestamp = model.getFirstPauseTimeStamp();
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(2);

		for (Iterator<GCEvent> i = model.getGCEvents(); i.hasNext();) {
			GCEvent event = i.next();
			if (event.getPreUsed() > 0 && event.getPreUsed() > postPost) {//limit to avoid negative number
				int allocation = (event.getPreUsed() - postPost) / 1024;
				double allocationTime = event.getTimestamp() - postEventTimestamp;
				double allocationRate = allocation / allocationTime;
				//System.out.println("Allocation : " + allocation + ", duration : " + allocationTime + " -> " + format.format(allocationRate) + "Mb/s");
				if(allocationRate > 2000){//limit to scale
					final double timestamp = event.getTimestamp() - model.getFirstPauseTimeStamp();
					polygon.addPoint(timestamp, allocationRate);
				}
			}

			postEventTimestamp = event.getTimestamp();
			postPost = event.getPostUsed();
		}

		// dummy point to make the polygon complete
		polygon.addPoint(model.getRunningTime(), 0.0d);
		return polygon;
	}

	@Override
	protected ScaledPolygon createScaledPolygon() {
		return new ScaledPolygon(getChart().getScaleFactor(), getHeight()/2000, getHeight());//TODO scale using a real number (not 2Go)
	}
}
