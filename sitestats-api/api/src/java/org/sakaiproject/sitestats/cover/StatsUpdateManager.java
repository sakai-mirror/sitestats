package org.sakaiproject.sitestats.cover;

import java.util.Date;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.event.api.Event;

/**
 * <p>
 * StatsUpdateManager is a static Cover for the {@link org.sakaiproject.sitestats.api.StatsUpdateManger StatsUpdateManager}; see that interface for usage details.
 * </p>
 */
public class StatsUpdateManager
{
	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.sitestats.api.StatsUpdateManager getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.sitestats.api.StatsUpdateManager) ComponentManager
						.get(org.sakaiproject.sitestats.api.StatsUpdateManager.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.sitestats.api.StatsUpdateManager) ComponentManager
					.get(org.sakaiproject.sitestats.api.StatsUpdateManager.class);
		}
	}

	private static org.sakaiproject.sitestats.api.StatsUpdateManager m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.sitestats.api.StatsUpdateManager.SERVICE_NAME;

	public static void processBatchEvent(Event e, String userId, String siteId, Date eventDate)
	{
		org.sakaiproject.sitestats.api.StatsUpdateManager service = getInstance();
		if (service == null) return;

		service.processBatchEvent(e, userId, siteId, eventDate);
	}
	
}
