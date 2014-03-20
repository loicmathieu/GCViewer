package fr.loicmathieu.gcviewer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Polygon;
import java.util.Iterator;

import com.tagtraum.perf.gcviewer.model.GCEvent;
import com.tagtraum.perf.gcviewer.model.GCModel;


public class CumulativeAllocationChartRenderer extends SimplePolygonChartRenderer {

	private static final long serialVersionUID = 4747298804857212792L;

	public static final Paint DEFAULT_LINEPAINT = Color.BLUE;

	public CumulativeAllocationChartRenderer(SimpleChart chart) {
		super(chart);
		setLinePaint(DEFAULT_LINEPAINT);
		setDrawPolygon(false);
		setDrawLine(true);
	}

	@Override
	public Polygon computePolygon(SimpleChart chart, GCModel model) {
		ScaledPolygon polygon = createScaledPolygon();
		long allocation = 0;
		int postPost = 0;

		for (Iterator<GCEvent> i = model.getGCEvents(); i.hasNext();) {
			GCEvent event = i.next();
			if (event.getPreUsed() > 0) {
				allocation += event.getPreUsed() - postPost;
				final double timestamp = event.getTimestamp() - model.getFirstPauseTimeStamp();
				polygon.addPoint(timestamp, allocation / 1024);
			}

			postPost = event.getPostUsed();
		}

		// dummy point to make the polygon complete
		polygon.addPoint(model.getRunningTime(), 0.0d);
		return polygon;
	}

	@Override
	protected ScaledPolygon createScaledPolygon() {
		return new ScaledPolygon(getChart().getScaleFactor(), getHeight()/(getChart().getTotalAllocation()), getHeight());
	}
}
