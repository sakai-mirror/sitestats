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
package edu.ufp.sakai.tool.statstool.ui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.data.general.DefaultPieDataset;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;

import edu.ufp.sakai.tool.statstool.api.StatsManager;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class OverviewBean extends BaseBean implements Serializable {
	private static final long	serialVersionUID		= 1L;

	/** Our log (commons). */
	private static Log			LOG						= LogFactory.getLog(OverviewBean.class);

	/** Bean members */
	private int					totalVisits				= -1;
	private double				lastWeekVisitsAverage	= -1;
	private double				lastMonthVisitsAverage	= -1;
	private List				weekVisits				= null;
	private List				weekUniqueVisitors		= null;
	private List				weekActivity			= null;
	private String				siteId					= null;
	private boolean				refresh					= true;

	/** Statistics Manager object */
	private StatsManager		sm						= (StatsManager) ComponentManager.get(StatsManager.class.getName());
	private ToolManager			M_tm					= (ToolManager) ComponentManager.get(ToolManager.class.getName());
	
	// ######################################################################################
	// Main methods
	// ######################################################################################
	public void init() {
		LOG.debug("OverviewBean.init()");
		
		if(isAllowed() && refresh){
			totalVisits = -1;
			lastWeekVisitsAverage = -1;
			lastMonthVisitsAverage = -1;
			weekVisits = null;
			weekActivity = null;
			refresh = false;
		}

	}
	
	public DefaultPieDataset getPieDataSet() {
		DefaultPieDataset pieDataSet = new DefaultPieDataset();
		pieDataSet.setValue("A",52);
		pieDataSet.setValue("B", 18);
		pieDataSet.setValue("C", 30);
		return pieDataSet;
	}
	
	public String getChartURL(){
		//String context = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		//return context + "/chartServlet";
		return "/chartservlet?id=1&type=2";
	}

	// ######################################################################################
	// Action/ActionListener methods
	// ######################################################################################

	// ######################################################################################
	// Generic get/set methods
	// ######################################################################################
	public String getSiteId() {
		if(siteId == null){
			Placement placement = M_tm.getCurrentPlacement();
			siteId = placement.getContext();
		}
		return siteId;
	}

	public int getTotalVisits() {
		if(totalVisits == -1){
			totalVisits = sm.getSiteVisits(getSiteId());
		}
		return totalVisits;
	}

	public double getLastWeekVisitsAverage() {
		if(lastWeekVisitsAverage == -1){
			Date now = new Date();
			// week = 1000ms * 60s * 60m * 24h * 7d;
			long weekDiff = 604800000l;
			Date lastWeek = new Date(now.getTime() - weekDiff);
			double weekVisitors = (double) sm.getSiteVisits(getSiteId(), lastWeek, now);
			lastWeekVisitsAverage = round(weekVisitors / 7.0, 2);
		}
		return lastWeekVisitsAverage;
	}

	public double getLastMonthVisitsAverage() {
		if(lastMonthVisitsAverage == -1){
			Date now = new Date();
			// year = 1000ms * 60s * 60m * 24h * 30d;
			long monthDiff = 2592000000l;
			Date lastMonth = new Date(now.getTime() - monthDiff);
			double monthVisitors = (double) sm.getSiteVisits(getSiteId(), lastMonth, now);
			lastMonthVisitsAverage = round(monthVisitors / 30.0, 2);
		}
		return lastMonthVisitsAverage;
	}

	public List getWeekVisits() {
		if(weekVisits == null){
			weekVisits = new ArrayList();
			weekUniqueVisitors = new ArrayList();
			Date finalDate = getToday();
			Date initialDate = getSundayOfWeek(finalDate);

			Map wv = sm.getSiteVisitsPerDay(getSiteId(), initialDate, finalDate);

			// fill w/ 0
			for(int i = 0; i < 7; i++){
				weekVisits.add(new Integer(0));
				weekUniqueVisitors.add(new Integer(0));
			}

			Iterator v = wv.keySet().iterator();
			Calendar curr = Calendar.getInstance();
			int keyDayOfWeek = 1;
			while (v.hasNext()){
				Date key = (Date) v.next();
				curr.setTime(key);
				keyDayOfWeek = curr.get(Calendar.DAY_OF_WEEK);
				Vector data = (Vector) wv.get(key);
				Integer val1 = (Integer) data.get(0);
				Integer val2 = (Integer) data.get(1);
				weekVisits.set(keyDayOfWeek - 1, val1);
				weekUniqueVisitors.set(keyDayOfWeek - 1, val2);
				// LOG.info("Adding: "+val+" for day_of_week: "+keyDayOfWeek + "
				// sql date: "+key.toString());
			}
		}
		return weekVisits;
	}

	public List getWeekActivity() {
		if(weekActivity == null){
			weekActivity = new ArrayList();
			Date finalDate = getToday();
			Date initialDate = getSundayOfWeek(finalDate);

			Map wa = sm.getSiteActivityPerDay(getSiteId(), initialDate, finalDate);

			// fill w/ 0
			for(int i = 0; i < 7; i++)
				weekActivity.add(new Integer(0));

			Iterator a = wa.keySet().iterator();
			Calendar curr = Calendar.getInstance();
			int keyDayOfWeek = 1;
			while (a.hasNext()){
				Date key = (Date) a.next();
				curr.setTime(key);
				keyDayOfWeek = curr.get(Calendar.DAY_OF_WEEK);
				Integer val = (Integer) wa.get(key);
				weekActivity.set(keyDayOfWeek - 1, val);
				// LOG.info("Adding: "+val+" for day_of_week: "+keyDayOfWeek + "
				// sql date: "+key.toString());
			}
		}
		return weekActivity;
	}

	public List getWeekUniqueVisitors() {
		if(weekUniqueVisitors == null){
			getWeekVisits();
		}
		return weekUniqueVisitors;
	}

	public Integer getWeekOfYear() {
		Calendar c = Calendar.getInstance();
		c.setTime(getToday());
		return new Integer(c.get(Calendar.WEEK_OF_YEAR));
	}

	// ######################################################################################
	// Private methods
	// ######################################################################################
	private Date getToday() {
		// prepare Calendar
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		return c.getTime();
	}

	private Date getSundayOfWeek(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		c.add(Calendar.DATE, -1);
		return c.getTime();
	}

	private static double round(double val, int places) {
		long factor = (long) Math.pow(10, places);

		// Shift the decimal the correct number of places
		// to the right.
		val = val * factor;

		// Round to the nearest integer.
		long tmp = Math.round(val);

		// Shift the decimal the correct number of places
		// back to the left.
		return (double) tmp / factor;
	}

}
