package org.sakaiproject.sitestats.api;

import java.util.List;

public interface ServerWideReportManager {
	
	// ################################################################
	// Server-wide activity related methods
	// ################################################################	
	/**
	 * Get site login activity statistics grouped by week.
	 * @return a list of login statistics where date is the Monday's of the week
	 */	
	public List<Logins> getWeeklyLogin ();
	
	

}
