package org.sakaiproject.sitestats.tool.bean;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;

import org.ajax4jsf.framework.resource.ImageRenderer;
import org.ajax4jsf.framework.resource.PngRenderer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.Week;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.Authz;
import org.sakaiproject.sitestats.api.Logins;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

public class ServerWideReportBean
{
    /** Our log (commons). */
    private static Log LOG = LogFactory.getLog (ServerWideReportBean.class);

    /** Resource bundle */
    private static String bundleName = FacesContext.getCurrentInstance ()
	    .getApplication ().getMessageBundle ();
    private static ResourceLoader msgs = new ResourceLoader (bundleName);

    /** Manager APIs */
    private transient ServiceBean serviceBean = null;
    private transient StatsManager SST_sm = null;
    private SiteService M_ss = null;
    private Authz SST_authz = null;
    private ServerWideReportManager serverWideReportManager = null;

    /** Other */
    private PrefsData prefsdata = null;
    private long prefsLastModified = 0;

    private XYDataset loginsDataset = null;

    // ######################################################################################
    // ManagedBean property methods
    // ######################################################################################
    public void setServiceBean (ServiceBean serviceBean)
    {
	this.serviceBean = serviceBean;
	this.SST_sm = serviceBean.getSstStatsManager ();
	this.SST_authz = serviceBean.getSstAuthz ();
	this.M_ss = serviceBean.getSiteService ();
    }

    public void setServerWideReportManager (
	    ServerWideReportManager serverWideReportManager)
    {
	this.serverWideReportManager = serverWideReportManager;
    }

    public boolean isAllowed ()
    {
	boolean allowed = SST_authz.isUserAbleToViewSiteStatsAdmin (ToolManager
		.getCurrentPlacement ().getContext ());

	if (!allowed) {
	    FacesContext fc = FacesContext.getCurrentInstance ();
	    fc.addMessage ("allowed", new FacesMessage (
		    FacesMessage.SEVERITY_FATAL, msgs
			    .getString ("unauthorized"), null));
	}
	return allowed;
    }

    public void generateReportChart (OutputStream out, Object data)
	    throws IOException
    {
	ChartParamsBean params = null;
	if (data instanceof ChartParamsBean)
	    params = (ChartParamsBean) data;
	else {
	    LOG.warn ("data NOT instanceof ChartParamsBean!");
	    return;
	}

	boolean useSmallFontInDomainAxis = true;
	if (params.getSelectedReportType ().equals (
		ChartParamsBean.LOGIN_REPORT))
	{
	    XYDataset dataset = getLoginsDataSet (params);
	    if (dataset != null) {
		generateLineChart (dataset, params, useSmallFontInDomainAxis,
			out);
	    } else {
		generateNoDataChart (params, out);
	    }
	} else {
	    // TODO
	}
    }

    private XYDataset getLoginsDataSet (ChartParamsBean params)
    {
	// LOG.info("Generating activityWeekBarDataSet");
	List<Logins> loginList = serverWideReportManager.getWeeklyLogin ();
	if (loginList == null)
	    return null;

	TimeSeries s1 = new TimeSeries (msgs.getString ("legend_logins"),
		Week.class);
	TimeSeries s2 = new TimeSeries (
		msgs.getString ("legend_unique_logins"), Week.class);
	for (Logins login : loginList) {
	    Week week = new Week (login.getDate ());
	    s1.add (week, login.getTotalLogins ());
	    s2.add (week, login.getTotalUnique ());
	}

	TimeSeriesCollection dataset = new TimeSeriesCollection ();
	dataset.addSeries (s1);
	dataset.addSeries (s2);

	loginsDataset = dataset;

	return loginsDataset;
    }

    private void generateLineChart (XYDataset dataset, ChartParamsBean params,
	    boolean useSmallFontInDomainAxis, OutputStream out)
	    throws IOException
    {
	JFreeChart chart = ChartFactory.createTimeSeriesChart (
		null, // title
		null, // x-axis label
		null, // y-axis label
		dataset, // data
		true, // create legend?
		true, // generate tooltips?
		false // generate URLs?
		);

	XYPlot plot = (XYPlot) chart.getPlot ();

	// set transparency
	// plot.setForegroundAlpha(getPrefsdata(params.getSiteId()).getChartTransparency());

	// set background
	chart.setBackgroundPaint (OverviewBean.parseColor (SST_sm
		.getChartBackgroundColor ()));

	// set chart border
	chart.setPadding (new RectangleInsets (10, 5, 5, 5));
	chart.setBorderVisible (true);
	chart.setBorderPaint (OverviewBean.parseColor ("#cccccc"));

	// set anti alias
	chart.setAntiAlias (true);

        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));
	axis.setLabelAngle(Math.PI * 1);	

	// set domain axis font size
	if (useSmallFontInDomainAxis) {
	    axis.setTickLabelFont (new Font ("SansSerif", Font.PLAIN, 8));
	}

	BufferedImage img = chart.createBufferedImage (params.getChartWidth (),
		params.getChartHeight ());
	try {
	    ImageIO.write (img, "png", out);
	}
	catch (Exception e) {
	    // Load canceled by user
	    // Do nothing.
	    LOG.warn ("Data transfer aborted by client.");
	}
    }

    private void generateNoDataChart (ChartParamsBean params, OutputStream out)
	    throws IOException
    {
	ImageRenderer imgR = new PngRenderer ();
	BufferedImage img = imgR.createImage (params.getChartWidth (), params
		.getChartHeight ());
	Graphics2D g2d = img.createGraphics ();

	g2d.setBackground (OverviewBean.parseColor (SST_sm
		.getChartBackgroundColor ()));
	g2d.clearRect (0, 0, params.getChartWidth () - 1, params
		.getChartHeight () - 1);
	g2d.setColor (OverviewBean.parseColor ("#cccccc"));
	g2d.drawRect (0, 0, params.getChartWidth () - 1, params
		.getChartHeight () - 1);
	Font f = new Font ("SansSerif", Font.PLAIN, 12);
	g2d.setFont (f);
	FontMetrics fm = g2d.getFontMetrics (f);
	String noData = msgs.getString ("no_data");
	int noDataWidth = fm.stringWidth (noData);
	int noDataHeight = fm.getHeight ();
	g2d.setColor (OverviewBean.parseColor ("#555555"));
	g2d.drawString (noData, params.getChartWidth () / 2 - noDataWidth / 2,
		params.getChartHeight () / 2 - noDataHeight / 2 + 2);

	try {
	    ImageIO.write (img, "png", out);
	}
	catch (Exception e) {
	    // Load canceled by user
	    // Do nothing.
	    LOG.warn ("Data transfer aborted by client.");
	}
    }
}
