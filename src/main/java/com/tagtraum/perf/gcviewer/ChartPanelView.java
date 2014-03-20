package com.tagtraum.perf.gcviewer;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.SwingPropertyChangeSupport;

import com.tagtraum.perf.gcviewer.imp.DataReaderException;
import com.tagtraum.perf.gcviewer.imp.DataReaderFacade;
import com.tagtraum.perf.gcviewer.log.TextAreaLogHandler;
import com.tagtraum.perf.gcviewer.model.GCModel;
import com.tagtraum.perf.gcviewer.util.LocalisationHelper;

import fr.loicmathieu.gcviewer.CreationRateChart;
import fr.loicmathieu.gcviewer.CumulativeAllocationChart;
import fr.loicmathieu.gcviewer.DurationChart;
import fr.loicmathieu.gcviewer.HeapUsageAfterGCChart;
import fr.loicmathieu.gcviewer.PercentageOfTimeInGcChart;


/**
 * This class holds all chart and model data panels and provides them to {@link GCDocument}
 * for layouting.
 *
 * @author <a href="mailto:hs@tagtraum.com">Hendrik Schreiber</a>
 * <p>Date: May 5, 2005<br/>
 * Time: 2:14:36 PM</p>
 * 
 * @author lmathieu
 */
public class ChartPanelView {

	public static final String EVENT_MINIMIZED = "minimized";

	private GCPreferences preferences;

	private ModelChartImpl modelChart;
	private ModelPanel modelPanel;
	private ModelDetailsPanel modelDetailsPanel;
	private JTabbedPane modelChartAndDetailsPanel;
	private GCModel model;
	private ViewBar viewBar;
	private boolean viewBarVisible;
	private boolean minimized;
	private SwingPropertyChangeSupport propertyChangeSupport;
	private GCDocument gcDocument;
	private TextAreaLogHandler textAreaLogHandler;
	private DataReaderFacade dataReaderFacade;

	//LMA : extra charts !!!
	private HeapUsageAfterGCChart heapUsageAfterGcChart;
	private DurationChart durationChart;
	private CumulativeAllocationChart allocationChart;
	private PercentageOfTimeInGcChart precentageOfTimeChart;
	private CreationRateChart createRateChart;

	public ChartPanelView(GCDocument gcDocument, URL url) throws DataReaderException {
		this.gcDocument = gcDocument;
		this.preferences = gcDocument.getPreferences();
		this.modelChart = new ModelChartImpl();
		this.modelPanel = new ModelPanel();
		this.modelDetailsPanel = new ModelDetailsPanel();

		//LMA initialize additional charts
		this.heapUsageAfterGcChart = new HeapUsageAfterGCChart();
		gcDocument.addChartToZoom(this.heapUsageAfterGcChart);
		this.durationChart = new DurationChart();
		gcDocument.addChartToZoom(this.durationChart);
		this.allocationChart = new CumulativeAllocationChart();
		gcDocument.addChartToZoom(this.allocationChart);
		this.precentageOfTimeChart = new PercentageOfTimeInGcChart();
		gcDocument.addChartToZoom(this.precentageOfTimeChart);
		this.createRateChart = new CreationRateChart();
		gcDocument.addChartToZoom(this.createRateChart);


		JScrollPane modelDetailsScrollPane = new JScrollPane(modelDetailsPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JScrollBar hScrollBar = modelDetailsScrollPane.getHorizontalScrollBar();
		hScrollBar.setUnitIncrement(10);
		JScrollBar vScrollBar = modelDetailsScrollPane.getVerticalScrollBar();
		vScrollBar.setUnitIncrement(10);

		this.modelChartAndDetailsPanel = new JTabbedPane();
		this.modelChartAndDetailsPanel.addTab(LocalisationHelper.getString("data_panel_tab_chart"), modelChart);
		this.modelChartAndDetailsPanel.addTab(LocalisationHelper.getString("data_panel_tab_details"), modelDetailsScrollPane);

		//LMA add aditional charts to the tabs
		this.modelChartAndDetailsPanel.addTab("HeapUsageAfterGCChart", this.heapUsageAfterGcChart);//TODO localize
		this.modelChartAndDetailsPanel.addTab("DurationChart", this.durationChart);//TODO localize
		this.modelChartAndDetailsPanel.addTab("CumulativeAllocation", this.allocationChart);//TODO localize
		this.modelChartAndDetailsPanel.addTab("PercentageOfTimeInGC", this.precentageOfTimeChart);//TODO localize
		this.modelChartAndDetailsPanel.addTab("CreationRate", this.createRateChart);//TODO localize

		this.viewBar = new ViewBar(this);
		this.propertyChangeSupport = new SwingPropertyChangeSupport(this);
		this.textAreaLogHandler = new TextAreaLogHandler();
		dataReaderFacade = new DataReaderFacade();
		final GCModel loaderModel = dataReaderFacade.loadModel(url, true, gcDocument);
		setModel(loaderModel);
	}

	/**
	 * Reloads the model displayed in this chart panel if it has changed. Using the parameter
	 * the parser error dialog can be suppressed.
	 * 
	 * @param showParserErrors if <code>true</code> parser errors will be shown
	 * @return <code>true</code>, if the file has been reloaded
	 * @throws DataReaderException if something went wrong reading the file
	 */
	public boolean reloadModel(boolean showParserErrors) throws DataReaderException {
		if (model.getURL() == null) {
			return false;
		}
		if (model.isDifferent(model.getURL())) {
			setModel(dataReaderFacade.loadModel(this.model.getURL(), showParserErrors, gcDocument));
			return true;
		}
		return false;
	}

	public void invalidate() {
		this.modelChart.invalidate();
		this.modelPanel.invalidate();
		this.modelDetailsPanel.invalidate();
		this.heapUsageAfterGcChart.invalidate();
		this.durationChart.invalidate();
		this.allocationChart.invalidate();
		this.precentageOfTimeChart.invalidate();
		this.createRateChart.invalidate();
	}

	public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
		propertyChangeSupport.addPropertyChangeListener(propertyChangeListener);
	}

	public ViewBar getViewBar() {
		return viewBar;
	}

	public JTextArea getParseLog() {
		return textAreaLogHandler.getTextArea();
	}

	public boolean isViewBarVisible() {
		return viewBarVisible;
	}

	public void setViewBarVisible(boolean viewBarVisible) {
		this.viewBarVisible = viewBarVisible;
	}

	public boolean isMinimized() {
		return minimized;
	}

	public void setMinimized(boolean minimized) {
		boolean oldValue = this.minimized;
		if (minimized != this.minimized) {
			this.minimized = minimized;
			propertyChangeSupport.firePropertyChange(EVENT_MINIMIZED, oldValue, minimized);
		}
	}

	public JTabbedPane getModelChartAndDetails() {
		return modelChartAndDetailsPanel;
	}

	public ModelChart getModelChart() {
		return modelChart;
	}

	public ModelPanel getModelPanel() {
		return modelPanel;
	}

	public GCModel getModel() {
		return model;
	}

	public void setModel(GCModel model) {
		this.model = model;
		this.modelPanel.setModel(model);
		this.modelChart.setModel(model, preferences);
		this.heapUsageAfterGcChart.setModel(model);
		this.durationChart.setModel(model);
		this.allocationChart.setModel(model);
		this.precentageOfTimeChart.setModel(model);
		this.createRateChart.setModel(model);
		this.modelDetailsPanel.setModel(model);
		this.viewBar.setTitle(model.getURL().toString());
	}

	public void close() {
		gcDocument.removeChartPanelView(this);
	}

	private static class ViewBar extends JPanel {
		private JLabel title = new JLabel();
		private ViewBarButton closeButton = new ViewBarButton("images/close.png", "images/close_selected.png");
		private MinMaxButton minimizeButton = new MinMaxButton();
		private ChartPanelView chartPanelView;

		public ViewBar(ChartPanelView chartPanelView) {
			this.chartPanelView = chartPanelView;
			setLayout(new GridBagLayout());
			this.title.setOpaque(false);
			this.title.setHorizontalAlignment(SwingConstants.LEFT);
			this.title.setFont(this.title.getFont().deriveFont(this.title.getFont().getSize2D()*0.8f));
			//minimize.set
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.weightx = 2.0;
			gridBagConstraints.anchor = GridBagConstraints.WEST;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			add(this.title, gridBagConstraints);
			gridBagConstraints.weightx = 0.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridx = 1;
			gridBagConstraints.fill = GridBagConstraints.VERTICAL;
			add(minimizeButton, gridBagConstraints);
			gridBagConstraints.gridx = 2;
			add(closeButton, gridBagConstraints);

			minimizeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					ViewBar.this.chartPanelView.setMinimized(!ViewBar.this.chartPanelView.isMinimized());
				}
			});
			closeButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					ViewBar.this.chartPanelView.close();
				}
			});
		}

		public void setTitle(String title) {
			this.title.setText(title);
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			//super.paintComponent(graphics);
			// paint background
			GradientPaint gradientPaint = new GradientPaint(0, 0, getBackground().darker(), 0, getHeight()/2.0f, getBackground().brighter(), true);
			Color color = graphics.getColor();
			final Graphics2D graphics2D = (Graphics2D)graphics;
			graphics2D.setPaint(gradientPaint);
			graphics2D.fillRect(0, 0, getWidth(), getHeight());
			graphics2D.setColor(color);
		}

		private static class ViewBarButton extends JButton {
			public ViewBarButton(String image1, String image2) {
				final ImageIcon imageIcon1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(image1)));
				final ImageIcon imageIcon2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource(image2)));
				setIcons(imageIcon1, imageIcon2);
				setMargin(new Insets(0,2,0,2));
				setRolloverEnabled(true);
				setBorderPainted(false);
				setOpaque(false);
			}

			public void setIcons(final ImageIcon imageIcon1, final ImageIcon imageIcon2) {
				setIcon(imageIcon1);
				setRolloverIcon(imageIcon2);
				setSelectedIcon(imageIcon2);
			}

		}
		private static class MinMaxButton extends JButton {
			private final ImageIcon min1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/minimize.png")));
			private final ImageIcon min2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/minimize_selected.png")));
			private final ImageIcon max1 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/maximize.png")));
			private final ImageIcon max2 = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("images/maximize_selected.png")));
			private boolean minimize = true;
			public MinMaxButton() {
				setIcons(min1, min2);
				setMargin(new Insets(0,2,0,2));
				setRolloverEnabled(true);
				setBorderPainted(false);
				setOpaque(false);
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent event) {
						if (minimize) {
							setIcons(max1, max2);
						}
						else {
							setIcons(min1, min2);
						}
						minimize = !minimize;
					}
				});
			}

			public void setIcons(final ImageIcon imageIcon1, final ImageIcon imageIcon2) {
				setIcon(imageIcon1);
				setRolloverIcon(imageIcon2);
				setSelectedIcon(imageIcon2);
			}

		}
	}

}
