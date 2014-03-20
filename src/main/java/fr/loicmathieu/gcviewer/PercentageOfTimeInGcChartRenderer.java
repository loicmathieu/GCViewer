package fr.loicmathieu.gcviewer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;


public class PercentageOfTimeInGcChartRenderer extends SimplePolygonChartRenderer {
	private static final long serialVersionUID = -1402473957733286384L;

	public static final Paint DEFAULT_LINEPAINT = Color.BLUE;

	public PercentageOfTimeInGcChartRenderer(SimpleChart chart) {
		super(chart);
		setLinePaint(DEFAULT_LINEPAINT);
		setDrawPolygon(false);
		setDrawLine(true);
	}

	@Override
	public Polygon computePolygon(SimpleChart chart, GCModel model) {
		ScaledPolygon polygon = createScaledPolygon();

		for (Iterator<GCEvent> i = model.getGCEvents(); i.hasNext();) {
			GCEvent event = i.next();
			double pause = event.getPause();
			double duration = 1; //TODO fin a better way than comparing to 1s !
			double percentage = pause / duration * 100;
			final double timestamp = event.getTimestamp() - model.getFirstPauseTimeStamp();
			polygon.addPoint(timestamp, percentage);
		}

		// dummy point to make the polygon complete
		polygon.addPoint(model.getRunningTime(), 0.0d);
		return polygon;
	}

	@Override
	protected ScaledPolygon createScaledPolygon() {
		//scale up to 100%
		return new ScaledPolygon(getChart().getScaleFactor(), getHeight()/100, getHeight());
	}
}
