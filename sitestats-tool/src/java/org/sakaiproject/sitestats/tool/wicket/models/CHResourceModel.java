/**
 * 
 */
package org.sakaiproject.sitestats.tool.wicket.models;

import org.apache.wicket.model.IModel;

public class CHResourceModel implements IModel {
	private static final long	serialVersionUID	= 1L;
	
	String resourceId = null;
	String resourceName = null;
	
	public CHResourceModel(String resourceId, String resourceName) {
		this.resourceId = resourceId;
		this.resourceName = resourceName;
	}

	public Object getObject() {
		return resourceId;
	}

	public void setObject(Object object) {
		resourceId = resourceId;
	}
	
	public String getResourceId() {
		return resourceId;
	}
	
	public String getResourceName() {
		return resourceName;
	}

	public void detach() {
		resourceId = null;
		resourceName = null;
	}
	
}