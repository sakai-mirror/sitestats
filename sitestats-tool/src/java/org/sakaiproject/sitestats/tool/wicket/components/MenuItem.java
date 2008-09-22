package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;


/**
 * @author Nuno Fernandes
 */
public class MenuItem extends Panel {
	private static final long	serialVersionUID	= 1L;

	public MenuItem(String id, IModel itemText, Class itemPageClass) {
		super(id);

		// link version
		final BookmarkablePageLink menuItemLink = new BookmarkablePageLink("menuItemLink", itemPageClass);
		final Label menuLinkText = new Label("menuLinkText", itemText);
		menuLinkText.setRenderBodyOnly(true);
		menuItemLink.add(menuLinkText);
		add(menuItemLink);

		// span version
		final Label menuItemLabel = new Label("menuItemLabel", itemText);
		menuItemLabel.setRenderBodyOnly(true);
		add(menuItemLabel);
		
		// determine current page
		Class currentPageClass = getRequestCycle().getResponsePageClass();
		if(itemPageClass.equals(currentPageClass)) {
			menuItemLink.setVisible(false);
			menuItemLabel.setVisible(true);
		}else{
			menuItemLink.setVisible(true);
			menuItemLabel.setVisible(false);
		}
	}
}
