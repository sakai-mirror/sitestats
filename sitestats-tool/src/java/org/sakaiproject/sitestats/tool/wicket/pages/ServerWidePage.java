package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;

/**
 * @author Nuno Fernandes
 */
public class ServerWidePage extends BasePage {

	@SpringBean
	private transient SakaiFacade facade;

	public ServerWidePage() {
		this(null);
	}

	public ServerWidePage(PageParameters params) {
		String siteId = facade.getToolManager().getCurrentPlacement().getContext();
		boolean allowed = facade.getStatsAuthz().isUserAbleToViewSiteStatsAdmin(siteId);
		if(allowed) {
			renderBody();
		}else{
			redirectToInterceptPage(new NotAuthorizedPage());
		}
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		//response.renderJavascriptReference("/library/js/jquery.js");
		super.renderHead(response);
	}
	
	private void renderBody() {
		
	}
}

