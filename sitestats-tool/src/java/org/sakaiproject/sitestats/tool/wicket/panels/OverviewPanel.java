package org.sakaiproject.sitestats.tool.wicket.panels;

import java.util.Date;

import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;


/**
 * @author Nuno Fernandes
 */
public class OverviewPanel extends Panel {
	private static final long		serialVersionUID	= 1L;

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade	facade;

	/** Site ID */
	private String					siteId				= null;
	private String					currentSiteId		= null;
	
	// UI Components
	private VisitsPanel				visitsPanel			= null;
	private ActivityPanel			activityPanel		= null;
	
	private AbstractDefaultAjaxBehavior chartSizeBehavior = null;

	/**
	 * Default constructor.
	 * @param id The wicket:id
	 */
	public OverviewPanel(String id) {
		this(id, null);
	}

	/**
	 * Constructor for data relative to a given site.
	 * @param id The wicket:id
	 * @param siteId The related site id
	 */
	public OverviewPanel(String id, String siteId) {
		super(id);
		// site id
		this.siteId = siteId;
		currentSiteId = facade.getToolManager().getCurrentPlacement().getContext();
		if(this.siteId == null){
			this.siteId = currentSiteId;
		}
		renderBody();
		renderAjaxBehavior();
	}

	@Override
	public void renderHead(HtmlHeaderContainer container) {
		container.getHeaderResponse().renderJavascriptReference("/library/js/jquery.js");
		container.getHeaderResponse().renderJavascriptReference("/sakai-sitestats-tool/script/common.js");
		super.renderHead(container);
	}
	
	@SuppressWarnings("serial")
	private void renderAjaxBehavior() {		
		chartSizeBehavior = new AbstractDefaultAjaxBehavior() {
			@Override
			protected void respond(AjaxRequestTarget target) {
				// get chart size
		    	Request req = RequestCycle.get().getRequest();
		    	int width;
		    	int height;
		    	int maxwidth;
		    	int maxheight;
				try{
					width = (int) Float.parseFloat(req.getParameter("width"));					
				}catch(NumberFormatException e){
					e.printStackTrace();
					width = 400;
				}
				try{
					height = (int) Float.parseFloat(req.getParameter("height"));
				}catch(NumberFormatException e){
					e.printStackTrace();
					height = 200;
				}
				try{
					maxwidth = (int) Float.parseFloat(req.getParameter("maxwidth"));
				}catch(NumberFormatException e){
					e.printStackTrace();
					maxwidth = 640;
				}
				try{
					maxheight = (int) Float.parseFloat(req.getParameter("maxheight"));
				}catch(NumberFormatException e){
					e.printStackTrace();
					maxheight = 300;
				}
				
				// set visits chart size
				if(visitsPanel != null) {
					visitsPanel.setChartSize(width, height, maxwidth, maxheight);
				}
				// set activity chart size
				if(activityPanel != null) {
					activityPanel.setChartSize(width, height, maxwidth, maxheight);
				}
			}		   
		};
		add(chartSizeBehavior);
		
		WebMarkupContainer js = new WebMarkupContainer("jsWicketChartSize");
		js.setOutputMarkupId(true);
		add(js);
		WebMarkupContainer jsCall = new WebMarkupContainer("jsWicketChartSizeCall") {
			@Override
			protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag) {
				StringBuilder buff = new StringBuilder();
				buff.append("jQuery(document).ready(function() {");
				buff.append("  var chartSizeCallback = '" + chartSizeBehavior.getCallbackUrl() + "'; ");
				buff.append("  setWicketChartSize(chartSizeCallback);");
				buff.append("});");
				replaceComponentTagBody(markupStream, openTag, buff.toString());
			}	
		};
		jsCall.setOutputMarkupId(true);
		add(jsCall);
	}

	/** Render body. */
	private void renderBody() {		
		// SiteStats services
		StatsManager statsManager = facade.getStatsManager();
		StatsUpdateManager statsUpdateManager = facade.getStatsUpdateManager();
		
		// Last job run
		final WebMarkupContainer lastJobRun = new WebMarkupContainer("lastJobRun");
		boolean lastJobRunVisible = !statsUpdateManager.isCollectThreadEnabled() && statsManager.isLastJobRunDateVisible(); 
		lastJobRun.setVisible(lastJobRunVisible);
		add(lastJobRun);
		final Label lastJobRunDate = new Label("lastJobRunDate");
		if(lastJobRunVisible) {
			try{
				Date d = statsUpdateManager.getEventDateFromLatestJobRun();
				String dStr = facade.getTimeService().newTime(d.getTime()).toStringLocalFull();
				lastJobRunDate.setModel(new Model(dStr));
			}catch(RuntimeException e) {
				lastJobRunDate.setModel(new Model());
			}catch(Exception e){
				lastJobRunDate.setModel(new Model());
			}
		}
		lastJobRun.add(lastJobRunDate);
		
		// Visits
		final WebMarkupContainer visits = new WebMarkupContainer("visits");
		boolean visitsVisible = statsManager.isEnableSiteVisits() && statsManager.isVisitsInfoAvailable();
		visits.setVisible(visitsVisible);
		if(visitsVisible) {
			visitsPanel = new VisitsPanel("visitsPanel", siteId);
			visits.add(visitsPanel);
		}else{
			WebMarkupContainer panel = new WebMarkupContainer("visitsPanel");
			visits.add(panel);
		}
		add(visits);
		
		// Activity
		final WebMarkupContainer activity = new WebMarkupContainer("activity");
		boolean activityVisible = statsManager.isEnableSiteActivity();
		activity.setVisible(activityVisible);
		if(activityVisible) {
			activityPanel = new ActivityPanel("activityPanel", siteId);
			activity.add(activityPanel);
		}else{
			WebMarkupContainer panel = new WebMarkupContainer("activityPanel");
			activity.add(panel);
		}
		add(activity);		
	}
}
