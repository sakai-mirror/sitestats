package org.sakaiproject.sitestats.tool.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.CharacterConverter;


public class UserIdEidConverter extends CharacterConverter {

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String userEid = null;
		if(value == null){
			userEid = "";
		}else{
			if(value instanceof String){
				userEid = (String) value;
			}
			userEid = super.getAsString(context, component, (Object) userEid);
		}

		return userEid;
	}

}
