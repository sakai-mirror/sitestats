package org.sakaiproject.sitestats.tool.wicket.pages;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;

/**
 * @author Nuno Fernandes
 */
public class OverviewPage extends BasePage {

	public OverviewPage() {
		this(null);
	}

	public OverviewPage(PageParameters params) {
		renderBody();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference("/library/js/jquery.js");
		super.renderHead(response);
	}
	
	private void renderBody() {
		final Label test = new Label("test", new ResourceModel("tool_title"));
		add(test);
	}
}

