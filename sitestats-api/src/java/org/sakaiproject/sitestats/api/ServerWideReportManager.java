package org.sakaiproject.sitestats.api;

import java.util.List;

public interface ServerWideReportManager {
	
	// ################################################################
	// Server-wide activity related methods
	// ################################################################	
	/**
	 * Get site login activity statistics grouped by week.
	 * @return a list of login statistics where date is the Monday's of the week.
	 * 	format: Date, Total Logins, Total Unique Logins
	 */	
	public List<StatsRecord> getWeeklyLogin ();
	
	/**
	 * Get site login activity statistics grouped by day.
	 * @return a list of login statistics.
	 * 	format: Date, Total Logins, Total Unique Logins
	 */	
	public List<StatsRecord> getDailyLogin ();
	
	
	/**
	 * Get top 20 activities in the last 7/30/365 daily average.
	 * @return a list of activities. format: event, last 7, last 30, last 365 average
	 * 	sorted by last 7
	 */	
	public List<StatsRecord> getTop20Activities ();
	
	
	/**
	 * Get regular users by week
	 * @return format: Date, number of users login 5+ in the week, 4, 3, 2, 1
	 */	
	public List<StatsRecord> getWeeklyRegularUsers ();
	
	/**
	 * Get session start in the last 30 days
	 * @return format: Date, hour, number of logins
	 */	
	public List<StatsRecord> getHourlyUsagePattern ();
	
	
	

}
