package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.Menu;
import org.sakaiproject.sitestats.tool.wicket.panels.PreferencesPanel;

/**
 * @author Nuno Fernandes
 */
public class PreferencesPage extends BasePage {

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade facade;
	
	public PreferencesPage() {
		this(null);
	}

	public PreferencesPage(PageParameters params) {
		String siteId = facade.getToolManager().getCurrentPlacement().getContext();
		boolean allowed = facade.getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			renderBody();
		}else{
			redirectToInterceptPage(new NotAuthorizedPage());
		}
	}
	
	private void renderBody() {
		add(new Menu("menu"));
		
		add(new PreferencesPanel("preferences"));
	}
}

