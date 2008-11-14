package org.sakaiproject.sitestats.tool.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.CharacterConverter;

import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

public class UserIdNameConverter extends CharacterConverter {
	/** Resource Bundle */
	private String 			bundleName 	= FacesContext.getCurrentInstance().getApplication().getMessageBundle();
	private ResourceLoader	msgs		= new ResourceLoader(bundleName);
	
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String name = null;
		if (value != null && value instanceof String) {
      String userId = (String) value;
			if(("-").equals(userId)) {      
				name = msgs.getString("user_anonymous");
			}else if(("?").equals(userId)) {
        name = msgs.getString("user_anonymous_access");
			}else{
				try{
					name = UserDirectoryService.getUser(userId).getDisplayName();
				}catch(UserNotDefinedException e1){
          name = msgs.getString("user_unknown");
				}
			}
		}else{
			name = msgs.getString("user_unknown");
		}
    return super.getAsString(context, component, (Object)name);
	}

}
