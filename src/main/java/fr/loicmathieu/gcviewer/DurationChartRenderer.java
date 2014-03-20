package fr.loicmathieu.gcviewer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;


public class DurationChartRenderer extends SimplePolygonChartRenderer {
	private static final long serialVersionUID = 8721311087593866113L;

	public static final Paint DEFAULT_LINEPAINT = Color.BLUE;

	public DurationChartRenderer(SimpleChart chart) {
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
			if (event.getPause() > 0) {
				final double timestamp = event.getTimestamp() - model.getFirstPauseTimeStamp();
				polygon.addPoint(timestamp, event.getPause() * 1000);
			}
		}

		// dummy point to make the polygon complete
		polygon.addPoint(model.getRunningTime(), 0.0d);
		return polygon;
	}

	@Override
	protected ScaledPolygon createScaledPolygon() {
		return new ScaledPolygon(getChart().getScaleFactor(), getHeight()/(getChart().getMaxDuration() * 1000), getHeight());
	}
}
