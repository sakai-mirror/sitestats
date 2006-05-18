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
package edu.ufp.sakai.tool.statstool.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.Tool;


public interface StatsManager {
	
	// ################################################################
	// Tool access 
	// ################################################################
	public boolean isUserAllowed(String userId, Site site, Tool tool);
	
	// ################################################################
	// Registered/configured events 
	// ################################################################
	/** Get all registered event ids. Event list is registered on tool xml file. */
	public List getRegisteredEventIds();
	
	/** Get all site configured event ids. This is a sublist from registered events configured per site. */
	public List getSiteConfiguredEventIds(String siteId);
	
	/** Configure site event ids. This is a sublist from registered events configured per site. */
	public void setSiteConfiguredEventIds(String siteId, List eventIds);
	
	// ################################################################
	// Maps
	// ################################################################	
	/** Get the event name for a given event id. */
	public String getEventName(String eventId);
	
	/** Get the event name mapping (id <-> name mapping). */
	public Map getEventNameMap();
	
	/** Get a resources list of a given site */
	public List getSiteResources(String siteId);
	
	// ################################################################
	// Statistical information
	// ################################################################
	public Date getInitialActivityDate(String siteId);
	
	/**
	 * Gets event statistics grouped by user and event, eg:
	 * <pre>
	 * +-----+---------------------+--------------+----------+
	 * | T   | LAST_DATE           | EVENT        | USER     |
	 * +-----+---------------------+--------------+----------+
	 * |   1 | 2006-01-03 16:26:36 | chat.new     | admin    |
	 * |   1 | 2006-01-03 16:26:36 | mail.new     | admin    |
	 * |  35 | 2006-03-04 11:49:11 | pres.begin   | lmbg     |
	 * |   1 | 2006-03-15 11:01:35 | content.read | nuno     |
	 * |  31 | 2006-03-15 10:53:07 | pres.begin   | nuno     |
	 * +-----+---------------------+--------------+----------+
	 * </pre> 
	 */
	public List getEventStats(String siteId, List events);
	
	public List getEventStats(String siteId, List events, String searchKey, Date iDate, Date fDate);
	
	/**
	 * Gets resource statistics grouped by user, eg:
	 * <pre>
	 * +---+----------+-------------------------------------------------------------------------+---------------------+
	 * | T | USER     | REF                                                                     | LAST_DATE           |
	 * +---+----------+-------------------------------------------------------------------------+---------------------+
	 * | 1 | fribeiro | /content/group/e1ba6966-b182-411f-00f8-679756bbcd24/Cópia DAV.txt       | 2006-02-18 15:44:08 |
	 * | 2 | fribeiro | /content/group/e1ba6966-b182-411f-00f8-679756bbcd24/cUpia Upload.txt    | 2006-02-20 10:39:05 |
	 * | 1 | nuno     | /content/group/e1ba6966-b182-411f-00f8-679756bbcd24/win2.log            | 2006-03-15 11:01:35 |
	 * +---+----------+-------------------------------------------------------------------------+---------------------+
	 * </pre>
	 */
	public List getResourceStats(String siteId);
	
	public List getResourceStats(String siteId, String searchKey, Date iDate, Date fDate);

	/**
	 * Get total site visits on a specific date interval.
	 * @param siteId Site identifier
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return Total visits.
	 */
	public int getSiteVisits(String siteId, Date iDate, Date fDate);
	
	public int getSiteVisits(String siteId);
	/**
	 * Get total site visits on a specific date interval, grouped by user.
	 * @param siteId Site identifier
	 * @param searchKey Part of user name or id
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return Total visits.
	 */
	public List getSiteVisitsPerUser(String siteId, String searchKey, Date iDate, Date fDate);
	
	/**
	 * Get site visits on a specific date interval, grouped by day.
	 * @param siteId Site identifier
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return A map with total visits: key is date (java.util.Date) and value is a Vector with: total (java.lang.Integer) and unique_visitors (java.lang.Integer)
	 */
	public Map getSiteVisitsPerDay(String siteId, Date iDate, Date fDate);
	
	/**
	 * Get site activity (based on events) on a specific date interval, grouped by day.
	 * @param siteId Site identifier
	 * @param iDate Initial date
	 * @param fDate Final date
	 * @return A map with total activity: key is date (java.util.Date) and value is total (java.lang.Integer)
	 */
	public Map getSiteActivityPerDay(String siteId, Date iDate, Date fDate);
}
