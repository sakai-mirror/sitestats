package org.sakaiproject.sitestats.tool.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.CharacterConverter;

import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;


public class UserIdEidConverter extends CharacterConverter {
	/** Resource Bundle */
	private String 			bundleName 	= FacesContext.getCurrentInstance().getApplication().getMessageBundle();
	private ResourceLoader	msgs		= new ResourceLoader(bundleName);

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		String userEid = null;
		if (value != null && value instanceof String) {
      String userId = (String) value;
			if(("-").equals(userId) || ("?").equals(userId)) {
				userEid = "-";
			}else{
				try{
					userEid = UserDirectoryService.getUser(userId).getDisplayId();
				}catch(UserNotDefinedException e1){
					userEid = userId;
				}
			}
		}else{
			userEid = msgs.getString("user_unknown");
		}
    return super.getAsString(context, component, (Object) userEid);
	}

}
