package fr.loicmathieu.gcviewer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.tagtraum.perf.gcviewer.model.GCModel;

/**
 * Graphical chart of the gc file. It contains the chart and all rulers surrounding it but not
 * the model details on the right side.
 *
 * @author lmathieu
 */
public class HeapUsageAfterGCChart extends SimpleChart implements ChangeListener{

	private static final long serialVersionUID = -6292915549045177205L;

	private Ruler metricRuler;
	private HeapUsageAfterGCChartRenderer heapUsageAfterGCRenderer;
	private int lastViewPortWidth = 0;

	public HeapUsageAfterGCChart() {
		super(new GCModel(true));

		// order of the renderers determines what is painted first and last
		// we start with what's painted last
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.weightx = 2;
		gridBagConstraints.weighty = 2;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 1;

		heapUsageAfterGCRenderer = new HeapUsageAfterGCChartRenderer(this);
		chart.add(heapUsageAfterGCRenderer, gridBagConstraints);

		setViewportView(chart);
		// This would make scrolling slower, but eliminates flickering...
		//getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);

		getViewport().addChangeListener(this);
		lastViewPortWidth = getViewport().getWidth();

		// set scrolling speed
		horizontalScrollBar = getHorizontalScrollBar();
		horizontalScrollBar.setUnitIncrement(50);
		horizontalScrollBar.setBlockIncrement(getViewport().getWidth());

		JPanel rowHeaderPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		rowHeaderPanel.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.VERTICAL;
		constraints.weightx = 2;
		constraints.weighty = 1;
		constraints.gridheight = 2;
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.metricRuler = new Ruler(true, 0, getModel().getFootprint() / 1024, "M");
		layout.setConstraints(metricRuler, constraints);
		rowHeaderPanel.add(metricRuler);
		constraints.gridx = 1;
		setRowHeaderView(rowHeaderPanel);
		setCorner(UPPER_LEFT_CORNER, new JPanel());
		setCorner(LOWER_LEFT_CORNER, new JPanel());


		getViewport().addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent e) {
				chart.setSize(chart.getPreferredSize());
				metricRuler.setSize((int)metricRuler.getPreferredSize().getWidth(), e.getComponent().getHeight());
				timestampRuler.setSize((int)chart.getPreferredSize().getWidth(), (int)timestampRuler.getPreferredSize().getHeight());
			}
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
		});

	}


	@Override
	public void setRunningTime(double runningTime) {
		heapUsageAfterGCRenderer.invalidate();

		super.setRunningTime(runningTime);
	}

	@Override
	public void setModel(GCModel model) {
		super.setModel(model);

		this.metricRuler.setMaxUnit(model.getFootprint()  / 1024);
		this.metricRuler.repaint();
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		JViewport viewPort = (JViewport)e.getSource();
		if (lastViewPortWidth != viewPort.getWidth()) {
			lastViewPortWidth = viewPort.getWidth();
			horizontalScrollBar.setBlockIncrement(lastViewPortWidth);
		}
	}

}
