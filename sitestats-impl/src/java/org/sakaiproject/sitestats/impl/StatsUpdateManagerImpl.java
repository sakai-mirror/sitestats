/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package org.sakaiproject.sitestats.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventParserTip;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SiteActivity;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.StatsUpdateManager;
import org.sakaiproject.sitestats.api.ToolInfo;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class StatsUpdateManagerImpl extends HibernateDaoSupport implements Runnable, StatsUpdateManager, Observer {
	private Log						LOG							= LogFactory.getLog(StatsUpdateManagerImpl.class);
	private final static String		PRESENCE_SUFFIX				= "-presence";
	private final static int		PRESENCE_SUFFIX_LENGTH		= PRESENCE_SUFFIX.length();

	/** Spring bean members */
	private boolean					collectThreadEnabled		= true;
	public long						collectThreadUpdateInterval	= 4000L;
	private boolean						collectAdminEvents						= false;
	private boolean						collectEventsForSiteWithToolOnly		= true;

	/** Sakai services */
	private StatsManager			M_sm;
	private SiteService				M_ss;
	private UsageSessionService		M_uss;
	private EventTrackingService	M_ets;

	/** Collect Thread and Semaphore */
	private Thread					collectThread;
	private List<Event>				collectThreadQueue			= new ArrayList<Event>();
	private Object					collectThreadSemaphore		= new Object();
	private boolean					collectThreadRunning		= true;

	private List<String>			registeredEvents			= null;
	private Map<String, ToolInfo>		eventIdToolMap				= null;


	
	// ################################################################
	// Spring related methods
	// ################################################################	
	public void setCollectThreadEnabled(boolean enabled) {
		this.collectThreadEnabled = enabled;
	}
	
	public boolean isCollectThreadEnabled() {
		return collectThreadEnabled;
	}
	
	public void setCollectThreadUpdateInterval(long dbUpdateInterval){
		this.collectThreadUpdateInterval = dbUpdateInterval;
	}
	
	public long getCollectThreadUpdateInterval(){
		return collectThreadUpdateInterval;
	}	
	
	public void setCollectAdminEvents(boolean value){
		this.collectAdminEvents = value;
	}

	public boolean isCollectAdminEvents(){
		return collectAdminEvents;
	}

	public void setCollectEventsForSiteWithToolOnly(boolean value){
		this.collectEventsForSiteWithToolOnly = value;
	}
	
	public boolean isCollectEventsForSiteWithToolOnly(){
		return collectEventsForSiteWithToolOnly;
	}
	
	public void setStatsManager(StatsManager mng){
		this.M_sm = mng;
	}
	
	public void setSiteService(SiteService ss){
		this.M_ss = ss;
	}
	
	public void setEventTrackingService(EventTrackingService ets){
		this.M_ets = ets;
	}
	
	public void setUsageSessionService(UsageSessionService uss){
		this.M_uss = uss;
	}
	
	public void init(){
		// get all registered events
		registeredEvents = M_sm.getAllToolEventIds();
		// add site visit event
		registeredEvents.add(M_sm.getSiteVisitEventId());
		// get eventId -> ToolInfo map
		eventIdToolMap = M_sm.getEventIdToolMap();
		
		logger.info("init(): - collect thread enabled: " + collectThreadEnabled);
		if(collectThreadEnabled) {
			logger.info("init(): - collect thread db update interval: " + collectThreadUpdateInterval +" ms");
			logger.info("init(): - collect administrator events: " + collectAdminEvents);
			logger.info("init(): - collect events only for sites with SiteStats: " + collectEventsForSiteWithToolOnly);
			
			// start update thread
			startUpdateThread();
			
			// add this as EventInfo observer
			//EventTrackingService.addObserver(this);
			M_ets.addLocalObserver(this);
		}
	}
	
	public void destroy(){
		if(collectThreadEnabled) {
			// remove this as EventInfo observer
			M_ets.deleteObserver(this);	
			
			// stop update thread
			stopUpdateThread();
		}
	}

	
	// ################################################################
	// Public methods
	// ################################################################
//	public static EventInfo buildEvent(String event, String resource, Date date, String contextId, String userId) {
//		return new DetailedEvent();
//	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#collectEvent(org.sakaiproject.event.api.Event)
	 */
	public synchronized void collectEvent(Event e) {
		String userId = e.getUserId();
		e = fixMalFormedEvents(e);
		if(registeredEvents.contains(e.getEvent()) && isValidEvent(e)){
			
			// site check
			String siteId = parseSiteId(e);
			if(siteId == null || M_ss.isUserSite(siteId) || M_ss.isSpecialSite(siteId)){
				return;
			}
			if(isCollectEventsForSiteWithToolOnly()){
				try {
					if(M_ss.getSite(siteId).getToolForCommonId(StatsManager.SITESTATS_TOOLID) == null)
						return;
				}catch(Exception ex) {
					// not a valid site
					return;
				}
			}
			
			// user check
			if(userId == null) userId = M_uss.getSession(e.getSessionId()).getUserId();
			if(!isCollectAdminEvents() && userId.equals("admin")){
				return;
			}

			// aggregate event...
			doUpdate(e, userId, siteId);
			//LOG.info("Statistics updated for '"+e.getEvent()+"' ("+e.toString()+") USER_ID: "+userId);
		}//else LOG.info("EventInfo ignored:  '"+e.toString()+"' ("+e.toString()+") USER_ID: "+userId);
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#collectEvents(java.util.List)
	 */
	public synchronized void collectEvents(List<Event> events) {
		if(events != null) {
			Iterator<Event> iE = events.iterator();
			while(iE.hasNext()) {
				Event e = iE.next();
				collectEvent(e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.StatsUpdateManager#collectEvents(org.sakaiproject.event.api.Event[])
	 */
	public synchronized void collectEvents(Event[] events) {
		for(int i=0; i<events.length; i++)
			collectEvent(events[i]);
	}
	

	// ################################################################
	// Update thread related methods
	// ################################################################	
	/** Method called whenever an new event is generated from EventTrackingService: do not call this method! */
	public void update(Observable obs, Object o) {		
		if(o instanceof Event){
			collectThreadQueue.add((Event) o);
		}
	}
	
	/** Update thread: do not call this method! */
	public void run(){
		try{
			while(collectThreadRunning){
				// do update job
				while(collectThreadQueue.size() > 0){
					collectEvent(collectThreadQueue.remove(0));
				}
				
				// sleep if no work to do
				if(!collectThreadRunning) break;
				try{
					synchronized (collectThreadSemaphore){
						collectThreadSemaphore.wait(collectThreadUpdateInterval);
					}
				}catch(InterruptedException e){
					LOG.warn("Failed to sleep statistics update thread",e);
				}
			}
		}catch(Throwable t){
			LOG.debug("Failed to execute statistics update thread",t);
		}finally{
			if(collectThreadRunning){
				// thread was stopped by an unknown error: restart
				LOG.debug("Statistics update thread was stoped by an unknown error: restarting...");
				startUpdateThread();
			}else
				LOG.info("Finished statistics update thread");
		}
	}

	/** Start the update thread */
	private void startUpdateThread(){
		collectThreadRunning = true;
		collectThread = null;
		collectThread = new Thread(this, "org.sakaiproject.sitestats.impl.StatsUpdateManagerImpl");
		collectThread.start();
	}
	
	/** Stop the update thread */
	private void stopUpdateThread(){
		collectThreadRunning = false;
		synchronized (collectThreadSemaphore){
			collectThreadSemaphore.notifyAll();
		}
	}
	

	// ################################################################
	// Update methods
	// ################################################################
	private synchronized void doUpdate(Event e, final String userId, final String siteId){		
		// event details
		final Date date = getToday();
		final String event = e.getEvent();
		final String resource = e.getResource();
		if(resource.trim().equals("")) return;
		
		
		// update		
		if(registeredEvents.contains(event)){			
			doUpdateEventStat(event, resource, userId, siteId, date, registeredEvents);
			if(!event.equals("pres.begin")){
				doUpdateSiteActivity(event, siteId, date, registeredEvents);
			}
		}	
		if(event.startsWith("content.")){
			doUpdateResourceStat(event, resource, userId, siteId, date);
		}else if(event.equals("pres.begin")){
			doUpdateSiteVisits(userId, siteId, date);
		}
	}
	
	private synchronized void doUpdateSiteVisits(final String userId, final String siteId, final Date date) {
		final String hql = "select s.userId " + 
				"from EventStatImpl as s " +
				"where s.siteId = :siteid " +
				"and s.eventId = 'pres.begin' " +
				"and s.date = :idate " +
				//"and s.userId != :userid " +
				"group by s.siteId, s.userId";
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					// do the work		
					Criteria c = session.createCriteria(SiteVisitsImpl.class);
					c.add(Expression.eq("siteId", siteId));
					c.add(Expression.eq("date", date));
					SiteVisits entryV = null;
					try{
						entryV = (SiteVisits) c.uniqueResult();
					}catch(Exception ex){
						LOG.debug("More than 1 result when unique result expected.", ex);
						entryV = (SiteVisits) c.list().get(0);
					}
					if(entryV == null) entryV = new SiteVisitsImpl();
					
					Query q = session.createQuery(hql);
					q.setString("siteid", siteId);
					q.setDate("idate", getToday());
					//q.setString("userid", userId);
					long uniqueVisitors = q.list().size();// + 1;
					
					entryV.setSiteId(siteId);
					entryV.setTotalVisits(entryV.getTotalVisits() + 1);
					entryV.setTotalUnique(uniqueVisitors);
					entryV.setDate(date);
					// save & commit
					session.saveOrUpdate(entryV);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
				}
				return null;
			}
		});
	}

	private synchronized void doUpdateResourceStat(final String event, final String ref, final String userId, final String siteId, final Date date) {
		final String fileName = ref;
		
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					// do the work
					String resourceAction = null;
					try{
						resourceAction = event.split("\\.")[1];
					}catch(ArrayIndexOutOfBoundsException e){
						resourceAction = event;
					}
					Criteria c = session.createCriteria(ResourceStatImpl.class);
					c.add(Expression.eq("siteId", siteId));
					c.add(Expression.eq("resourceRef", fileName));
					c.add(Expression.eq("resourceAction", resourceAction));
					c.add(Expression.eq("userId", userId));
					c.add(Expression.eq("date", date));
					ResourceStat entryR = null;
					try{
						entryR = (ResourceStat) c.uniqueResult();
					}catch(Exception ex){
						LOG.debug("More than 1 result when unique result expected.", ex);
						entryR = (ResourceStat) c.list().get(0);
					}
					if(entryR == null) entryR = new ResourceStatImpl();
					entryR.setSiteId(siteId);
					entryR.setUserId(userId);
					entryR.setResourceRef(fileName);
					entryR.setResourceAction(resourceAction);
					entryR.setCount(entryR.getCount() + 1);
					entryR.setDate(date);
					// save & commit
					session.saveOrUpdate(entryR);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
				}
				return null;
			}
		});
	}

	private synchronized void doUpdateSiteActivity(final String event, final String siteId, final Date date, List<String> registeredEvents) {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					// do the work
					Criteria c = session.createCriteria(SiteActivityImpl.class);
					c.add(Expression.eq("siteId", siteId));
					c.add(Expression.eq("eventId", event));
					c.add(Expression.eq("date", date));
					SiteActivity entryA = null;
					try{
						entryA = (SiteActivity) c.uniqueResult();
					}catch(Exception ex){
						LOG.debug("More than 1 result when unique result expected.", ex);
						entryA = (SiteActivity) c.list().get(0);
					}
					if(entryA == null) entryA = new SiteActivityImpl();
					entryA.setSiteId(siteId);
					entryA.setEventId(event);
					entryA.setCount(entryA.getCount() + 1);
					entryA.setDate(date);
					// save & commit
					session.saveOrUpdate(entryA);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
				}
				return null;
			}
		});
	}

	private synchronized void doUpdateEventStat(final String event, String resource, final String userId, final String siteId, final Date date, List<String> registeredEvents) {
		getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Transaction tx = null;
				try{
					tx = session.beginTransaction();
					// do the work
					Criteria c = session.createCriteria(EventStatImpl.class);
					c.add(Expression.eq("siteId", siteId));
					c.add(Expression.eq("eventId", event));
					c.add(Expression.eq("userId", userId));
					c.add(Expression.eq("date", date));
					EventStat entryE = null;
					try{
						entryE = (EventStat) c.uniqueResult();
					}catch(Exception ex){
						LOG.debug("More than 1 result when unique result expected.", ex);
						entryE = (EventStat) c.list().get(0);
					}
					if(entryE == null) entryE = new EventStatImpl();
					entryE.setSiteId(siteId);
					entryE.setUserId(userId);
					entryE.setEventId(event);
					entryE.setCount(entryE.getCount() + 1);
					entryE.setDate(date);
					// save & commit
					session.saveOrUpdate(entryE);
					tx.commit();
				}catch(Exception e){
					if(tx != null) tx.rollback();
					LOG.warn("Unable to commit transaction: ", e);
				}
				return null;
			}
		});
	}
	

	// ################################################################
	// Utility methods
	// ################################################################	
	private synchronized boolean isValidEvent(Event e) {
		if(e.getEvent().startsWith("content")){
			String ref = e.getResource();			
			try{
				String parts[] = ref.split("\\/");		
				if(parts[2].equals("user")){
					// workspace (ignore)
					return false;
				}else if(parts[2].equals("attachment") && parts.length < 6){
					// ignore mail attachments (no reference to site)
					return false;
				}else if(parts[2].equals("group")){
					// resources
					if(parts.length <= 4) return false;	
				}else if(parts[2].equals("group-user")){
					// drop-box
					if(parts.length <= 5) return false;
				}
			}catch(Exception ex){
				return false;
			}
		}
		return true;
	}
	
	private Event fixMalFormedEvents(Event e){
		String event = e.getEvent();
		String resource = e.getResource();
		
		// OBSOLETE: fix bad reference (resource) format
		// => Use <eventParserTip> instead
			//if(!resource.startsWith("/"))
			//	resource = '/' + resource;
		
		// MessageCenter (OLD) CASE: Handle old MessageCenter events */
		if(event.startsWith("content.") && resource.startsWith("MessageCenter")) {
			resource = resource.replaceFirst("MessageCenter::", "/MessageCenter/site/");
			resource = resource.replaceAll("::", "/");
			return M_ets.newEvent(
					event.replaceFirst("content.", "msgcntr."), 
					resource, 
					false);
		}

		return e;
	}
	
	private String parseSiteId(Event e){
		String eventId = e.getEvent();
		String eventRef = e.getResource();
		
		try{
			if(eventId.equals(StatsManager.SITEVISIT_EVENTID)) {
				
				// presence (site visit) syntax (/presence/SITE_ID-presence)
				String[] parts = eventRef.split("/");
				if(parts[2].endsWith(PRESENCE_SUFFIX))
					return parts[2].substring(0, parts[2].length() - PRESENCE_SUFFIX_LENGTH);
				else
					return null;	
				
			}else {

				// use <eventParserTip>
				ToolInfo toolInfo = eventIdToolMap.get(eventId);
				EventParserTip parserTip = toolInfo.getEventParserTip();
				if(parserTip != null && parserTip.getFor().equals(StatsManager.PARSERTIP_FOR_CONTEXTID)) {
					int index = Integer.parseInt(parserTip.getIndex());
					return eventRef.split(parserTip.getSeparator())[index];
					
				}else {
					// try with most common syntax (/abc/cde/SITE_ID/...)
					return eventRef.split("/")[3];
				}
			}
		}catch(Exception ex){
			LOG.warn("Unable to parse contextId from event: " + eventId + " | " + eventRef, ex);
		}
		return null;
	}
	
	/*
	private String parseSiteId_Old(String ref){
		try{
			String[] parts = ref.split("/");
			if(parts == null)
				return null;
			if(parts.length == 1){
				// try with OLD MessageCenter syntax (MessageCenter::SITE_ID::...)
				parts = ref.split("::");
				return parts.length > 1 ? parts[1] : null;
			}
			if(parts[0].equals("MessageCenter")){
				// MessageCenter without initial '/'
				return parts[2];
			}
			if(parts[0].equals("")){
				if(parts[1].equals("presence"))
					// try with presence syntax (/presence/SITE_ID-presence)
					if(parts[2].endsWith("-presence"))
						return parts[2].substring(0,parts[2].length()-9);
					else
						return null;
				else if(parts[1].equals("syllabus"))
					// try with Syllabus syntax (/syllabus/SITE_ID/...)
					return parts[2];
				else if(parts[1].equals("site"))
					// try with Section Info syntax (/site/SITE_ID/...)
					return parts[2];
				else if(parts[1].equals("gradebook"))
					// try with Gradebook syntax (/gradebook/SITE_ID/...)
					return parts[2];
				else if(parts[1].equals("tasklist") || parts[1].equals("todolist"))
					// try with Tasklist/TodoList syntax (/tasklist/SITE_ID/...)
					return parts[2];
				else
					// try with most common syntax (/abc/cde/SITE_ID/...)
					return parts[3];
			}
		}catch(Exception e){
			LOG.debug("Unable to parse site ID from "+ref, e);
		}
		return null;
	}
	*/
	
	private Date getToday() {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
	}
	
}
