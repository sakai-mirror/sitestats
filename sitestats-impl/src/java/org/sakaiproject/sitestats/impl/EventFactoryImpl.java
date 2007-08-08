package org.sakaiproject.sitestats.impl;

import java.util.ResourceBundle;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sitestats.api.EventInfo;
import org.sakaiproject.sitestats.api.EventFactory;
import org.xml.sax.Attributes;

public class EventFactoryImpl implements EventFactory, ObjectCreationFactory {
	protected ResourceBundle	msgs	= ResourceBundle.getBundle("org.sakaiproject.sitestats.impl.bundle.Messages");
	private Log					LOG		= LogFactory.getLog(EventFactoryImpl.class);

	public EventInfo createEvent(String eventId) {
		return new EventInfoImpl(eventId);
	}
	
	public Object createObject(Attributes attributes) throws Exception {
		String eventId = attributes.getValue("eventId");
		String selected = attributes.getValue("selected");

		if(eventId == null){ throw new Exception("Mandatory eventId attribute not present on event tag."); }
		EventInfo eventInfo;
		try{
			eventInfo = new EventInfoImpl(eventId, msgs.getString(eventId.trim()));
			eventInfo.setSelected(Boolean.parseBoolean(selected));
		}catch(RuntimeException e){
			eventInfo = new EventInfoImpl(eventId, eventId);
			LOG.warn("No translation found for eventId: "+eventId.trim()+". Please specify it in sitestats/sitestats-impl/impl/src/bundle/org/sakaiproject/sitestats/impl/bundle/");
		}
		return eventInfo;
	}

	public Digester getDigester() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDigester(Digester arg0) {
		// TODO Auto-generated method stub

	}

}
