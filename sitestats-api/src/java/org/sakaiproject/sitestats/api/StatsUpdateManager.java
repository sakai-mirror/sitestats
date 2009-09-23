/**
 * $URL:$
 * $Id:$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.api;

import java.util.Date;
import java.util.List;

import org.sakaiproject.event.api.Event;


public interface StatsUpdateManager {
	/**
	 * Enable/disable collect thread (collect sakai events in real time at a specified (small) time interval).
	 * For medium-large installation (more than 8000-10000 users) it is recommended to use a quartz job instead of the collect thread.
	 */
	public void setCollectThreadEnabled(boolean enabled);	
	/** Check whether collect thread is enabled.  */
	public boolean isCollectThreadEnabled();
	
	/**
	 * Time interval at which the collect thread will run and process all the events in queue since the last run.
	 * Please notice that this value cannot be set to high due to memory consumption. For medium-large installation
	 * (more than 8000-10000 users) it is recommended to use a quartz job instead of the collect thread.
	 */
	public void setCollectThreadUpdateInterval(long dbUpdateInterval);
	/** Get the collect thread sleep interval. */
	public long getCollectThreadUpdateInterval();
	

	/** Collect administrator events */
	public boolean isCollectAdminEvents();
	public void setCollectAdminEvents(boolean value);
	
	/** Collect events ONLY for sites with SiteStats tool? */
	public boolean isCollectEventsForSiteWithToolOnly();
	public void setCollectEventsForSiteWithToolOnly(boolean value);
	
	/**
	 * Collect (process) a Sakai event into SiteStats tables.
	 * This method is called by the default quartz job implementation and should be called for every other custom quartz
	 * job implemented to this task (collect events).
	 * @param e An Event (can be built from sql fields using the CustomEventImpl class)
	 * @return True if event was successfully processed and persisted.
	 */
	public boolean collectEvent(Event e);
	/** 
	 * Collect (process) Sakai events into SiteStats tables.
	 * This method is called by the default quartz job implementation and should be called for every other custom quartz
	 * job implemented to this task (collect events).
	 * @param e A List of Event (can be built from sql fields using the CustomEventImpl class)
	 * @return True if events were successfully processed and persisted.
	 */
	public boolean collectEvents(List<Event> events);
	/** 
	 * Collect (process) Sakai events into SiteStats tables.
	 * This method is called by the default quartz job implementation and should be called for every other custom quartz
	 * job implemented to this task (collect events).
	 * @param e An array of Event (can be built from sql fields using the CustomEventImpl class)
	 * @return True if events were successfully processed and persisted.
	 */
	public boolean collectEvents(Event[] events);
	
	/**
	 * Construct a new Event object using specified arguments. Useful for building Events read from SAKAI_EVENT and SAKAI_SESSION table.
	 * @param date The SAKAI_EVENT.EVENT_DATE field
	 * @param event The SAKAI_EVENT.EVENT field
	 * @param ref The SAKAI_EVENT.REF field
	 * @param sessionUser The SAKAI_SESSION.SESSION_USER field
	 * @param sessionId The SAKAI_SESSION.SESSION_ID field
	 * @return An Event object
	 */
	public Event buildEvent(Date date, String event, String ref, String sessionUser, String sessionId);
	
	/**
	 * Construct a new Event object using specified arguments. Useful for building Events read from SAKAI_EVENT and SAKAI_SESSION table.
	 * @param date The SAKAI_EVENT.EVENT_DATE field
	 * @param event The SAKAI_EVENT.EVENT field
	 * @param ref The SAKAI_EVENT.REF field
	 * @param context The SAKAI_EVENT.CONTEXT field
	 * @param sessionUser The SAKAI_SESSION.SESSION_USER field
	 * @param sessionId The SAKAI_SESSION.SESSION_ID field
	 * @return An Event object
	 */
	public Event buildEvent(Date date, String event, String ref, String context, String sessionUser, String sessionId);
	
	public boolean saveJobRun(JobRun jobRun);
	public JobRun getLatestJobRun() throws Exception;
	public Date getEventDateFromLatestJobRun() throws Exception;
}
