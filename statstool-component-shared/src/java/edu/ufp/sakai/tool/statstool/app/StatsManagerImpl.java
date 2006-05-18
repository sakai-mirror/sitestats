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
package edu.ufp.sakai.tool.statstool.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Tool;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import edu.ufp.sakai.tool.statstool.api.PrefsEntry;
import edu.ufp.sakai.tool.statstool.api.StatsEntry;
import edu.ufp.sakai.tool.statstool.api.StatsManager;


public class StatsManagerImpl extends HibernateDaoSupport implements StatsManager {
	private static final String		SEPARATOR		= " / ";
	private static Log				LOG				= LogFactory.getLog(StatsManagerImpl.class);

	/** Spring bean members */
	private Properties				eventsBean;
	private boolean					showAdminEvents	= false;

	
	/** Controller fields */
	private List					registeredEvents;
	private Map						eventNameMap;

	/** Sakai services */
	private SecurityService			M_as			= (SecurityService) ComponentManager.get(SecurityService.class.getName());
	private SqlService				M_sql			= (SqlService) ComponentManager.get(SqlService.class.getName());
	
	
	// ################################################################
	// Tool access
	// ################################################################
	public boolean isUserAllowed(String userId, Site site, Tool tool) {
		if(userId == null) return false;
		if(M_as.isSuperUser()) return true;
		Role r = site.getUserRole(userId);
		if(r != null) return isToolAllowedForRole(tool, r.getId());
		else return false;
	}

	/**
	 * Checks if a given user has permissions access a tool in a site. This is
	 * checked agains the following tags in tool xml file:<br>
	 * <configuration name="roles.allow" value="maintain,Instructor" /><br>
	 * <configuration name="roles.deny" value="access,guest" /> <br>
	 * Both, one or none of this configuration tags can be specified. By
	 * default, an user has permissions to see the tool in site.<br>
	 * Permissions are checked in the order: Allow, Deny.
	 * @param tool Tool to check permissions on.
	 * @param roleId Current user's role.
	 * @return Whether user has permissions to this tool in this site.
	 */
	private boolean isToolAllowedForRole(Tool tool, String roleId) {
		String TOOL_CFG_ROLES_ALLOW = "roles.allow";
		String TOOL_CFG_ROLES_DENY = "roles.deny";
		Properties roleConfig = tool.getRegisteredConfig();
		String toolTitle = tool.getTitle();
		boolean allowRuleSpecified = roleConfig.containsKey(TOOL_CFG_ROLES_ALLOW);
		boolean denyRuleSpecified = roleConfig.containsKey(TOOL_CFG_ROLES_DENY);

		// allow by default, when no config keys are present
		if(!allowRuleSpecified && !allowRuleSpecified) return true;

		boolean allowed = true;
		if(allowRuleSpecified){
			allowed = false;
			boolean found = false;
			String[] result = roleConfig.getProperty(TOOL_CFG_ROLES_ALLOW).split("\\,");
			for(int x = 0; x < result.length; x++){
				if(result[x].trim().equals(roleId)){
					found = true;
					break;
				}
			}
			if(found){
				LOG.debug("Tool config '" + TOOL_CFG_ROLES_ALLOW + "' allowed access to '" + roleId + "' in " + toolTitle);
				allowed = true;
			}
		}
		if(denyRuleSpecified){
			if(!allowRuleSpecified) allowed = true;
			boolean found = false;
			String[] result = roleConfig.getProperty(TOOL_CFG_ROLES_DENY).split("\\,");
			for(int x = 0; x < result.length; x++){
				if(result[x].trim().equals(roleId)){
					found = true;
					break;
				}
			}
			if(found){
				LOG.debug("Tool config '" + TOOL_CFG_ROLES_DENY + "' denied access to '" + roleId + "' in " + toolTitle);
				allowed = false;
			}
		}else if(!allowRuleSpecified) allowed = true;
		LOG.debug("Allowed access to '" + roleId + "' in " + toolTitle + "? " + allowed);
		return allowed;
	}

	
	// ################################################################
	// Spring bean methods
	// ################################################################
	public void setEvents(Properties events) {
		this.eventsBean = events;
	}

	public void setShowAdminEvents(boolean value) {
		this.showAdminEvents = value;
	}


	// ################################################################
	// Registered/configured events
	// ################################################################
	public List getRegisteredEventIds() {
		if(registeredEvents == null){
			registeredEvents = new ArrayList();
			Enumeration e = eventsBean.propertyNames();
			while (e.hasMoreElements()){
				registeredEvents.add((String) e.nextElement());
			}
		}
		return registeredEvents;
	}

	public List getSiteConfiguredEventIds(final String siteId) {
		if (siteId == null){
	      throw new IllegalArgumentException("Null siteId");
	    }else{
	    	HibernateCallback hcb = new HibernateCallback(){                
	    		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    			List prefs = session.createCriteria(PrefsEntryImpl.class).add(Expression.eq("siteId", siteId)).list();
	    			List eventIds = new ArrayList();
	    			Iterator i = prefs.iterator();
	    			while (i.hasNext()){
	    				PrefsEntry p = (PrefsEntry) i.next();
	    				eventIds.add(p.getEventId());
	    			}
	    			return eventIds;
	    		}
	    	};
	    	return (List) getHibernateTemplate().execute(hcb);
	    }
	}

	public void setSiteConfiguredEventIds(final String siteId, final List eventIds) {
		if (siteId == null || eventIds == null){
			throw new IllegalArgumentException("Null siteId or eventIds");
		}else{
			HibernateCallback hcb = new HibernateCallback(){                
	    		public Object doInHibernate(Session session) throws HibernateException, SQLException {
	    			// 1. build new config list (PrefsEntry objects)
	    			List newPrefs = new ArrayList();
	    			Iterator i = eventIds.iterator();
	    			while (i.hasNext()){
	    				PrefsEntry p = new PrefsEntryImpl();
	    				p.setSiteId(siteId);
	    				p.setEventId((String) i.next());
	    				newPrefs.add(p);
	    			}
	    			
	    			// 2. build current config list
	    			List currPrefs = session.createCriteria(PrefsEntryImpl.class).add(Expression.eq("siteId", siteId)).list();

	    			// 3. iterate current config:
	    			// - remove entries not in new config from db
	    			// - remove entries in new and current config from new config list
	    			Iterator c = currPrefs.iterator();
	    			while (c.hasNext()){
	    				PrefsEntry p = (PrefsEntry) c.next();
	    				if(!newPrefs.contains(p)){
	    					// remove from db
	    					session.delete(p);
	    				}else{
	    					// remove from new cfg
	    					newPrefs.remove(p);
	    				}
	    			}
	    			
	    			// 4. add new cfg (new entries only)
	    			Iterator n = newPrefs.iterator();
	    			while (n.hasNext()){
	    				PrefsEntry p = (PrefsEntry) n.next();
	    				session.save(p);
	    			}
	    			
	    			return null;
	    		}
			};
			getHibernateTemplate().execute(hcb);
		}
	}

	// ################################################################
	// Maps
	// ################################################################
	public String getEventName(String eventId) {
		return eventsBean.getProperty(eventId, eventId);
	}

	public Map getEventNameMap() {
		if(eventNameMap == null){
			eventNameMap = new HashMap();
			if(registeredEvents == null) getRegisteredEventIds();
			Iterator i = registeredEvents.iterator();
			while (i.hasNext()){
				String eId = (String) i.next();
				eventNameMap.put(eId, getEventName(eId));
			}

		}
		return eventNameMap;
	}

	public List getSiteResources(String siteId) {
		List list = new ArrayList();
		try{
			Connection c = M_sql.borrowConnection();
			String sql = "select DISTINCT REF from SAKAI_EVENT where EVENT='content.read' and REF like ? ORDER BY REF;";
			PreparedStatement pst = c.prepareStatement(sql);
			int paramIndex = 1;
			// siteId/resourceId params
			pst.setString(paramIndex++, "/content/group/" + siteId + "/%");
			ResultSet rs = pst.executeQuery();
			while (rs.next()){
				String tmp[] = rs.getString("REF").split("\\/");
				String fileName = "";
				for(int i = 4; i < tmp.length - 1; i++)
					fileName += tmp[i] + SEPARATOR;
				fileName += tmp[tmp.length - 1];
				list.add(fileName);
			}
			rs.close();
			pst.close();
			M_sql.returnConnection(c);
		}catch(SQLException e){
			LOG.error("SQL error occurred while retrieving list of site resources: " + e.getMessage());
		}
		return list;
	}

	// ################################################################
	// Statistical information
	// ################################################################
	public Date getInitialActivityDate(String siteId) {
		Date date = Calendar.getInstance().getTime();
		try{
			Connection c = M_sql.borrowConnection();
			String sql = "select CREATEDON from SAKAI_SITE where SITE_ID=?;";
			PreparedStatement pst = c.prepareStatement(sql);
			pst.setString(1, siteId);
			ResultSet rs = pst.executeQuery();
			while (rs.next()){
				date = rs.getDate("CREATEDON");
				if(date == null) date = Calendar.getInstance().getTime();
			}
			rs.close();
			pst.close();
			M_sql.returnConnection(c);
		}catch(SQLException e){
			LOG.error("SQL error occurred while retrieving getInitialActivityDate(): " + e.getMessage());
		}
		return date;
	}

	public List getEventStats(String siteId, List events, String searchKey, Date iDate, Date fDate) {
		List list = new ArrayList();
		// work on a copy of events list
		List _events = new ArrayList();
		Iterator it = events.iterator();
		while (it.hasNext())
			_events.add(it.next());
		// event filter
		if(_events.size() == 0) _events = getRegisteredEventIds();
		// event filter
		String eventsQuestion = new String();
		eventsQuestion = " and EVENT IN (?";
		for(int i = 1; i < _events.size(); i++){
			eventsQuestion += ",?";
		}
		eventsQuestion += ") ";
		// search user id/name
		String userInc1 = new String();
		String userInc2 = new String();
		String userQuestion = new String();
		if(searchKey == null){
			userQuestion = " and SESSION_USER IS NOT NULL ";
		}else{
			userInc1 = " SAKAI_USER join ";
			userInc2 = " SAKAI_USER.USER_ID=S.SESSION_USER and ";
			userQuestion = " and SAKAI_USER.USER_ID=SESSION_USER and (SESSION_USER like ? or SAKAI_USER.FIRST_NAME like ? or SAKAI_USER.LAST_NAME like ?) ";
		}
		if(!showAdminEvents) userQuestion += " and SESSION_USER!='admin' ";

		// date filtering
		String iDateQuestion = new String();
		if(iDate != null) iDateQuestion = " and EVENT_DATE >= ? ";
		String fDateQuestion = new String();
		if(fDate != null) fDateQuestion = " and EVENT_DATE <= ? ";
		try{
			Connection c = M_sql.borrowConnection();
			String sql = "select count(EVENT_ID) as T,max(" + getDATEforSqlVendor("EVENT_DATE") + ") as LAST_DATE,EVENT,SESSION_USER from " + userInc1
					+ " SAKAI_EVENT as E left join SAKAI_SESSION as S on " + userInc2 + " E.SESSION_ID=S.SESSION_ID " + "where REF like ? " + userQuestion + eventsQuestion + iDateQuestion
					+ fDateQuestion + " group by SESSION_USER,EVENT order by SESSION_USER,EVENT;";
			PreparedStatement pst = c.prepareStatement(sql);
			int paramIndex = 1;
			// siteId param
			pst.setString(paramIndex++, "%" + siteId + "%");
			// userId param
			if(searchKey != null){
				for(int i = 0; i < 3; i++)
					pst.setString(paramIndex++, "%" + searchKey + "%");
			}
			// events param
			for(int i = 0; i < _events.size(); i++){
				pst.setString(paramIndex++, (String) _events.get(i));
			}
			// date params
			if(iDate != null){
				java.sql.Date iD = new java.sql.Date(iDate.getTime());
				pst.setDate(paramIndex++, iD);
			}
			if(fDate != null){
				long fDms = fDate.getTime() + (1000 * 60 * 60 * 24);
				java.sql.Date fD = new java.sql.Date(fDms);
				;
				pst.setDate(paramIndex++, fD);
			}
			// execute query
			ResultSet rs = pst.executeQuery();
			while (rs.next()){
				StatsEntry entry = new StatsEntryImpl();
				entry.setUserId(rs.getString("SESSION_USER"));
				entry.setEventId(rs.getString("EVENT"));
				entry.setTotal(rs.getInt("T"));
				entry.setDate(rs.getDate("LAST_DATE"));
				list.add(entry);
			}
			rs.close();
			pst.close();
			M_sql.returnConnection(c);
		}catch(SQLException e){
			LOG.error("SQL error occurred while retrieving event stats: " + e.getMessage());
		}
		return list;
	}

	public List getEventStats(String siteId, List events) {
		return getEventStats(siteId, events, null, getInitialActivityDate(siteId), null);
	}

	public List getResourceStats(String siteId, String searchKey, Date iDate, Date fDate) {
		List list = new ArrayList();
		// search user id/name
		String userInc1 = new String();
		String userInc2 = new String();
		String userQuestion = new String();
		if(searchKey == null){
			userQuestion = " and SESSION_USER IS NOT NULL ";
		}else{
			userInc1 = " SAKAI_USER join ";
			userInc2 = " SAKAI_USER.USER_ID=S.SESSION_USER and ";
			userQuestion = " and SAKAI_USER.USER_ID=SESSION_USER and (SESSION_USER like ? or SAKAI_USER.FIRST_NAME like ? or SAKAI_USER.LAST_NAME like ?) ";
		}
		if(!showAdminEvents) userQuestion += " and SESSION_USER!='admin' ";

		// date filtering
		String iDateQuestion = new String();
		if(iDate != null) iDateQuestion = " and EVENT_DATE >= ? ";
		String fDateQuestion = new String();
		if(fDate != null) fDateQuestion = " and EVENT_DATE <= ? ";
		try{
			Connection c = M_sql.borrowConnection();
			String sql = "select count(EVENT_ID) as T,SESSION_USER,REF," + getDATEforSqlVendor("EVENT_DATE") + " as LAST_DATE from " + userInc1 + " SAKAI_EVENT as E left join SAKAI_SESSION as S on "
					+ userInc2 + " E.SESSION_ID=S.SESSION_ID " + " where EVENT='content.read' " + " and REF like ? " + userQuestion + iDateQuestion + fDateQuestion
					+ " GROUP BY SESSION_USER,REF ORDER BY SESSION_USER;";
			PreparedStatement pst = c.prepareStatement(sql);
			int paramIndex = 1;
			// siteId param
			pst.setString(paramIndex++, "/content/group/" + siteId + "/%");
			// userId param
			if(searchKey != null){
				for(int i = 0; i < 3; i++)
					pst.setString(paramIndex++, "%" + searchKey + "%");
			}
			// date params
			if(iDate != null){
				java.sql.Date iD = new java.sql.Date(iDate.getTime());
				pst.setDate(paramIndex++, iD);
			}
			if(fDate != null){
				long fDms = fDate.getTime() + (1000 * 60 * 60 * 24);
				java.sql.Date fD = new java.sql.Date(fDms);
				pst.setDate(paramIndex++, fD);
			}
			// execute query
			ResultSet rs = pst.executeQuery();
			while (rs.next()){
				StatsEntry entry = new StatsEntryImpl();
				entry.setUserId(rs.getString("SESSION_USER"));
				String tmp[] = rs.getString("REF").split("\\/");
				String fileName = "";
				for(int i = 4; i < tmp.length - 1; i++)
					fileName += tmp[i] + SEPARATOR;
				fileName += tmp[tmp.length - 1];
				entry.setRefId(fileName);
				entry.setTotal(rs.getInt("T"));
				entry.setDate(rs.getDate("LAST_DATE"));
				list.add(entry);
			}
			rs.close();
			pst.close();
			M_sql.returnConnection(c);
		}catch(SQLException e){
			LOG.error("SQL error occurred while retrieving resource stats: " + e.getMessage());
		}
		return list;
	}

	public List getResourceStats(String siteId) {
		return getResourceStats(siteId, null, getInitialActivityDate(siteId), null);
	}

	public int getSiteVisits(String siteId, Date iDate, Date fDate) {
		int total = 0;
		// date filtering
		String iDateQuestion = new String();
		if(iDate != null) iDateQuestion = " and EVENT_DATE >= ? ";
		String fDateQuestion = new String();
		if(fDate != null) fDateQuestion = " and EVENT_DATE <= ? ";
		String adminJoin = new String();
		String adminQuestion = new String();
		if(!showAdminEvents){
			adminJoin = " left join SAKAI_SESSION on SAKAI_EVENT.SESSION_ID=SAKAI_SESSION.SESSION_ID ";
			adminQuestion = " and SESSION_USER!='admin' ";
		}
		try{
			Connection c = M_sql.borrowConnection();
			String sql = "select count(EVENT_ID) as TOTAL from SAKAI_EVENT " + adminJoin + " where EVENT='pres.begin' and REF=? " + iDateQuestion + fDateQuestion + adminQuestion;
			PreparedStatement pst = c.prepareStatement(sql);
			int paramIndex = 1;
			// site id
			pst.setString(paramIndex++, "/presence/" + siteId + "-presence");
			// date params
			if(iDate != null){
				java.sql.Date iD = new java.sql.Date(iDate.getTime());
				pst.setDate(paramIndex++, iD);
			}
			if(fDate != null){
				long fDms = fDate.getTime() + (1000 * 60 * 60 * 24);
				java.sql.Date fD = new java.sql.Date(fDms);
				pst.setDate(paramIndex++, fD);
			}
			ResultSet rs = pst.executeQuery();
			if(rs.next()){
				total = rs.getInt("TOTAL");
			}
			rs.close();
			pst.close();
			M_sql.returnConnection(c);
		}catch(SQLException e){
			LOG.error("SQL error occurred while getting site visit count: " + e.getMessage());
		}
		return total;
	}

	public int getSiteVisits(String siteId) {
		return getSiteVisits(siteId, getInitialActivityDate(siteId), null);
	}

	public List getSiteVisitsPerUser(String siteId, String searchKey, Date iDate, Date fDate) {
		List list = new ArrayList();
		// search user id/name
		String userInc1 = new String();
		String userInc2 = new String();
		String userQuestion = new String();
		if(searchKey == null){
			userQuestion = " and SESSION_USER IS NOT NULL ";
		}else{
			userInc1 = " SAKAI_USER join ";
			userInc2 = " SAKAI_USER.USER_ID=S.SESSION_USER and ";
			userQuestion = " and SAKAI_USER.USER_ID=SESSION_USER and (SESSION_USER like ? or SAKAI_USER.FIRST_NAME like ? or SAKAI_USER.LAST_NAME like ?) ";
		}
		if(!showAdminEvents) userQuestion += " and SESSION_USER!='admin' ";
		// date filtering
		String iDateQuestion = new String();
		if(iDate != null) iDateQuestion = " and EVENT_DATE >= ? ";
		String fDateQuestion = new String();
		if(fDate != null) fDateQuestion = " and EVENT_DATE <= ? ";
		try{
			Connection c = M_sql.borrowConnection();
			String sql = "select count(EVENT_ID) as T,SESSION_USER,max(EVENT_DATE) as LAST_DATE from " + userInc1 + " SAKAI_EVENT as E left join SAKAI_SESSION as S on " + userInc2
					+ " E.SESSION_ID=S.SESSION_ID " + " where EVENT='pres.begin' and REF=? " + userQuestion + iDateQuestion + fDateQuestion + " GROUP BY SESSION_USER ORDER BY SESSION_USER";
			PreparedStatement pst = c.prepareStatement(sql);
			int paramIndex = 1;
			// site id
			pst.setString(paramIndex++, "/presence/" + siteId + "-presence");
			// date params
			if(iDate != null){
				java.sql.Date iD = new java.sql.Date(iDate.getTime());
				pst.setDate(paramIndex++, iD);
			}
			if(fDate != null){
				long fDms = fDate.getTime() + (1000 * 60 * 60 * 24);
				java.sql.Date fD = new java.sql.Date(fDms);
				pst.setDate(paramIndex++, fD);
			}
			ResultSet rs = pst.executeQuery();
			while (rs.next()){
				StatsEntry entry = new StatsEntryImpl();
				entry.setUserId(rs.getString("SESSION_USER"));
				entry.setEventId("pres.begin");
				entry.setTotal(rs.getInt("T"));
				entry.setDate(rs.getDate("LAST_DATE"));
				list.add(entry);
			}
			rs.close();
			pst.close();
			M_sql.returnConnection(c);
		}catch(SQLException e){
			LOG.error("SQL error occurred while getting site visit count per user: " + e.getMessage());
		}
		return list;
	}

	public Map getSiteVisitsPerDay(String siteId, Date iDate, Date fDate) {
		Map map = new HashMap();
		// date filtering
		String iDateQuestion = new String();
		if(iDate != null) iDateQuestion = " and SESSION_START >= ? ";
		String fDateQuestion = new String();
		if(fDate != null) fDateQuestion = " and SESSION_END <= ? ";
		String adminQuestion = new String();
		if(!showAdminEvents) adminQuestion = " and SESSION_USER!='admin' ";
		try{
			Connection c = M_sql.borrowConnection();
			String sql = "select count(EVENT_ID) as TOTAL, count(DISTINCT SESSION_USER) AS UNIQUE_VISITORS, " + getDATEforSqlVendor("SESSION_START")
					+ " as DATE from SAKAI_EVENT LEFT JOIN SAKAI_SESSION ON SAKAI_EVENT.SESSION_ID=SAKAI_SESSION.SESSION_ID where " + "EVENT='pres.begin' and REF=? " + iDateQuestion + fDateQuestion
					+ adminQuestion + "GROUP BY " + getDATEforSqlVendor("SESSION_START") + ";";
			PreparedStatement pst = c.prepareStatement(sql);
			int paramIndex = 1;
			// site id
			pst.setString(paramIndex++, "/presence/" + siteId + "-presence");
			// date params
			if(iDate != null){
				java.sql.Date iD = new java.sql.Date(iDate.getTime());
				pst.setDate(paramIndex++, iD);
			}
			if(fDate != null){
				long fDms = fDate.getTime() + (1000 * 60 * 60 * 24);
				java.sql.Date fD = new java.sql.Date(fDms);
				pst.setDate(paramIndex++, fD);
			}
			ResultSet rs = pst.executeQuery();
			while (rs.next()){
				Date date = new Date(rs.getDate("DATE").getTime());
				Vector v = new Vector(2);
				Integer total = new Integer(rs.getInt("TOTAL"));
				Integer unique = new Integer(rs.getInt("UNIQUE_VISITORS"));
				v.add(total);
				v.add(unique);
				map.put(date, v);
			}
			rs.close();
			pst.close();
			M_sql.returnConnection(c);

		}catch(SQLException e){
			LOG.error("SQL error occurred while getting site visit count per day: " + e.getMessage());
		}
		return map;
	}

	public Map getSiteActivityPerDay(String siteId, Date iDate, Date fDate) {
		Map map = new HashMap();
		// date filtering
		String iDateQuestion = new String();
		if(iDate != null) iDateQuestion = " and EVENT_DATE >= ? ";
		String fDateQuestion = new String();
		if(fDate != null) fDateQuestion = " and EVENT_DATE <= ? ";
		String adminQuestion = new String();
		if(!showAdminEvents) adminQuestion = " and SESSION_USER!='admin' ";
		try{
			Connection c = M_sql.borrowConnection();
			String sql = "select count(EVENT_ID) AS TOTAL, " + getDATEforSqlVendor("SESSION_START")
					+ " as DATE from SAKAI_EVENT LEFT JOIN SAKAI_SESSION ON SAKAI_EVENT.SESSION_ID=SAKAI_SESSION.SESSION_ID where " + "REF like ? " + " and SESSION_USER IS NOT NULL " + adminQuestion
					+ " and EVENT IN ('mail.new','chat.new','disc.new','content.read','asn.submit.submission','asn.save.submission') " + iDateQuestion + fDateQuestion + "GROUP BY "
					+ getDATEforSqlVendor("SESSION_START") + ";";
			PreparedStatement pst = c.prepareStatement(sql);
			int paramIndex = 1;
			// site id
			pst.setString(paramIndex++, "%" + siteId + "%");
			// date params
			if(iDate != null){
				java.sql.Date iD = new java.sql.Date(iDate.getTime());
				pst.setDate(paramIndex++, iD);
			}
			if(fDate != null){
				long fDms = fDate.getTime() + (1000 * 60 * 60 * 24);
				java.sql.Date fD = new java.sql.Date(fDms);
				pst.setDate(paramIndex++, fD);
			}
			ResultSet rs = pst.executeQuery();
			while (rs.next()){
				Date date = new Date(rs.getDate("DATE").getTime());
				Integer total = new Integer(rs.getInt("TOTAL"));
				map.put(date, total);
			}
			rs.close();
			pst.close();
			M_sql.returnConnection(c);
		}catch(SQLException e){
			LOG.error("SQL error occurred while getting site activity count per day: " + e.getMessage());
		}
		return map;
	}

	private String getDATEforSqlVendor(String date) {
		String vendor = M_sql.getVendor();
		if(vendor.equalsIgnoreCase("mysql")){
			return " DATE(" + date + ") ";
		}else if(vendor.equalsIgnoreCase("oracle")){
			return " TO_DATE(TO_CHAR(" + date + ",'YYYY-MM-DD'),'YYYY-MM-DD') ";
		}else /* hsqldb */{
			return " CAST(CONCAT(YEAR(" + date + "), CONCAT('-',CONCAT(MONTH(" + date + "),CONCAT('-',DAYOFMONTH(" + date + "))))) as DATE) ";
		}
	}
}
