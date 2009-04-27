package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.LastJobRun;
import org.sakaiproject.sitestats.tool.wicket.components.Menus;
import org.sakaiproject.sitestats.tool.wicket.widget.ActivityWidget;
import org.sakaiproject.sitestats.tool.wicket.widget.ResourcesWidget;
import org.sakaiproject.sitestats.tool.wicket.widget.VisitsWidget;

/**
 * @author Nuno Fernandes
 */
public class OverviewPage extends BasePage {
	private static final long			serialVersionUID	= 1L;

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade		facade;

	private String						realSiteId;
	private String						siteId;
	
	
	public OverviewPage() {
		this(null);
	}

	public OverviewPage(PageParameters pageParameters) {
		realSiteId = getFacade().getToolManager().getCurrentPlacement().getContext();
		if(pageParameters != null) {
			siteId = pageParameters.getString("siteId");
		}
		if(siteId == null){
			siteId = realSiteId;
		}
		boolean allowed = getFacade().getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			renderBody();
			getFacade().getStatsManager().logEvent(null, StatsManager.LOG_ACTION_VIEW, siteId, true);
		}else{
			setResponsePage(NotAuthorizedPage.class);
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.renderJavascriptReference(JQUERYSCRIPT);
		response.renderJavascriptReference("/sakai-sitestats-tool/script/jquery.ifixpng2.js");
		StringBuilder onDomReady = new StringBuilder();
		onDomReady.append("jQuery.ifixpng('/sakai-sitestats-tool/images/transparent.gif');");
		response.renderOnDomReadyJavascript(onDomReady.toString());
	}
	
	private void renderBody() {
		setRenderBodyOnly(true);
		add(new Menus("menu", siteId));
		
		// SiteStats services
		StatsManager statsManager = getFacade().getStatsManager();
		
		// Last job run
		add(new LastJobRun("lastJobRun", siteId));
		
		// Widgets ----------------------------------------------------
		
		// Visits
		boolean visitsVisible = statsManager.isEnableSiteVisits() && statsManager.isVisitsInfoAvailable();
		if(visitsVisible) {
			add(new VisitsWidget("visitsWidget", siteId));
		}else{
			add(new WebMarkupContainer("visitsWidget").setRenderBodyOnly(true));
		}
		
		// Activity
		boolean activityVisible = statsManager.isEnableSiteActivity();
		if(activityVisible) {
			add(new ActivityWidget("activityWidget", siteId));
		}else{
			add(new WebMarkupContainer("activityWidget").setRenderBodyOnly(true));
		}
		
		// Resources
		boolean resourcesVisible = false;
		try{
			resourcesVisible = statsManager.isEnableResourceStats() &&
								(getFacade().getSiteService().getSite(siteId).getToolForCommonId(StatsManager.RESOURCES_TOOLID) != null);
		}catch(Exception e) {
			resourcesVisible = false;
		}
		if(resourcesVisible) {
			add(new ResourcesWidget("resourcesWidget", siteId));
		}else{
			add(new WebMarkupContainer("resourcesWidget").setRenderBodyOnly(true));
		}
	}
	
	private SakaiFacade getFacade() {
		if(facade == null) {
			InjectorHolder.getInjector().inject(this);
		}
		return facade;
	}
}

