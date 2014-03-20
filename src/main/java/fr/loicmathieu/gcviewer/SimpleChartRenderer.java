package fr.loicmathieu.gcviewer;

import javax.swing.*;
import java.awt.*;

/**
 * ChartRenderer.
 *
 * @author lmathieu
 */
public abstract class SimpleChartRenderer extends JComponent {
	private static final long serialVersionUID = -142935619537238623L;
	
	private SimpleChart chart;
    private boolean drawLine;
    private Paint linePaint;

    public SimpleChartRenderer(SimpleChart chart) {
        this.chart = chart;
    }

    public void setLinePaint(Paint linePaint) {
        this.linePaint = linePaint;
    }

    public void setDrawLine(boolean drawLine) {
        this.drawLine = drawLine;
    }

	public SimpleChart getChart() {
		return chart;
	}
	
	public void setChart(SimpleChart chart) {
		this.chart = chart;
	}

	public boolean isDrawLine() {
        return drawLine;
    }

    public Paint getLinePaint() {
        return linePaint;
    }

    @Override
	protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        Paint oldPaint = g2d.getPaint();
        Object oldAAHint = g2d.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        if (chart.isAntiAlias()) g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setPaint(getLinePaint());
        paintComponent(g2d);
        g2d.setPaint(oldPaint);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAAHint);
    }

    public abstract void paintComponent(Graphics2D g2d);


}
