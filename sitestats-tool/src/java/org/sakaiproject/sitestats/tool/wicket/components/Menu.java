package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.pages.AdminPage;
import org.sakaiproject.sitestats.tool.wicket.pages.OverviewPage;
import org.sakaiproject.sitestats.tool.wicket.pages.PreferencesPage;
import org.sakaiproject.sitestats.tool.wicket.pages.ReportsPage;
import org.sakaiproject.sitestats.tool.wicket.pages.ServerWidePage;


/**
 * @author Nuno Fernandes
 */
public class Menu extends Panel {
	private static final long	serialVersionUID	= 1L;

	@SpringBean
	private transient SakaiFacade facade;

	/**
	 * Default constructor.
	 * @param id The wicket:id
	 */
	public Menu(String id) {
		super(id);
		setRenderBodyOnly(true);
		renderBody();
	}
	
	/**
	 * Render Sakai Menu
	 */
	@SuppressWarnings("unchecked")
	private void renderBody() {
		// site id
		String siteId = facade.getToolManager().getCurrentPlacement().getContext();
		Class currentPageClass = getRequestCycle().getResponsePageClass();
				
		// --------- ADMIN SECTION ---------
		
		// Admin page
		boolean adminPageVisible = 
			facade.getStatsAuthz().isUserAbleToViewSiteStatsAdmin(siteId);
		MenuItem adminPage = new MenuItem("adminPage", new ResourceModel("menu_overview"), AdminPage.class);
		adminPage.setVisible(adminPageVisible);
		if(adminPageVisible) {
			adminPage.add(new AttributeModifier("class", true, new Model("firstToolBarItem")));
		}
		add(adminPage);
		
		// Admin ServerWide page
		boolean serverWidePageVisible = 
			adminPageVisible
			&&
			facade.getStatsManager().isServerWideStatsEnabled();
		MenuItem serverWidePage = new MenuItem("serverWidePage", new ResourceModel("menu_serverwide"), ServerWidePage.class);
		serverWidePage.setVisible(serverWidePageVisible);
		add(serverWidePage);
		
		// --------- USER SECTION ---------

		// Overview
		boolean overviewVisible = 
			!AdminPage.class.equals(currentPageClass)
			&&
			!ServerWidePage.class.equals(currentPageClass)			
			&&
			(facade.getStatsManager().isEnableSiteVisits() || facade.getStatsManager().isEnableSiteActivity());
		MenuItem overview = new MenuItem("overview", new ResourceModel("menu_overview"), OverviewPage.class);
		overview.setVisible(overviewVisible);
		if(!adminPageVisible) {
			overview.add(new AttributeModifier("class", true, new Model("firstToolBarItem")));
		}
		add(overview);

		// Reports
		boolean reportsVisible = 
			!AdminPage.class.equals(currentPageClass)
			&&
			!ServerWidePage.class.equals(currentPageClass);
		MenuItem reports = new MenuItem("reports", new ResourceModel("menu_reports"), ReportsPage.class);
		reports.setVisible(reportsVisible);
		add(reports);

		// Preferences
		boolean preferencesVisible = 
			!AdminPage.class.equals(currentPageClass)
			&&
			!ServerWidePage.class.equals(currentPageClass);
		MenuItem preferences = new MenuItem("preferences", new ResourceModel("menu_prefs"), PreferencesPage.class);
		preferences.setVisible(preferencesVisible);
		add(preferences);
		
	}

	/* (non-Javadoc)
	 * @see org.apache.wicket.markup.html.panel.Panel#onComponentTag(org.apache.wicket.markup.ComponentTag)
	 */
	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		checkComponentTag(tag, "menu");
	}

}
