package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.Menu;

/**
 * @author Nuno Fernandes
 */
public class ReportsPage extends BasePage {

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade facade;

	public ReportsPage() {
		this(null);
	}

	public ReportsPage(PageParameters params) {
		String siteId = facade.getToolManager().getCurrentPlacement().getContext();
		boolean allowed = facade.getStatsAuthz().isUserAbleToViewSiteStats(siteId);
		if(allowed) {
			renderBody();
		}else{
			redirectToInterceptPage(new NotAuthorizedPage());
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference("/library/js/jquery.js");
		response.renderJavascriptReference("/sakai-sitestats-tool/script/common.js");
		super.renderHead(response);
	}
	
	private void renderBody() {
		final Menu menu = new Menu("menu");
		add(menu);
	}
}

