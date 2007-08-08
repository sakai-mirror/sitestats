package org.sakaiproject.sitestats.impl;

import java.util.ResourceBundle;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.ToolManager;
import org.xml.sax.Attributes;


public class RoleFactory implements ObjectCreationFactory {
	protected ResourceBundle	msgs	= ResourceBundle.getBundle("org.sakaiproject.sitestats.impl.bundle.Messages");
	private Log					LOG		= LogFactory.getLog(RoleFactory.class);
	private ToolManager			M_tm	= (ToolManager) ComponentManager.get(ToolManager.class.getName());

	public Object createObject(Attributes attributes) throws Exception {
		String id = attributes.getValue("id");

		if(id == null){ throw new Exception("Mandatory id attribute not present on role tag."); }
		String role = new String(id);
		return role;
	}

	public Digester getDigester() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDigester(Digester digester) {
		// TODO Auto-generated method stub

	}

}
