/**
 * 
 */
package org.sakaiproject.sitestats.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.sitestats.api.Logins;
import org.sakaiproject.sitestats.api.ServerWideReportManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.UsageSessionService;

/**
 * @author u4330369
 *
 */
public class ServerWideReportManagerImpl implements ServerWideReportManager {

	/** Our log (commons). */
	private static Log LOG = LogFactory.getLog(ServerWideReportManagerImpl.class);
	
	/** Dependency: SqlService */
	private SqlService m_sqlService = null;
	
	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		m_sqlService = service;
	}

	
	/** Dependency: UsageSessionService */
	private UsageSessionService	m_usageSessionService;
	
	public void setUsageSessionService(UsageSessionService usageSessionService){
		this.m_usageSessionService = usageSessionService;
	}
	

	public void init(){
	}

	

	/* (non-Javadoc)
	 * @see org.sakaiproject.sitestats.api.ServerWideReportManager#getWeeklyLogin()
	 */
	public List<Logins> getWeeklyLogin() {
		String mySql = "select STR_TO_DATE(concat(date_format(SESSION_START, '%x-%v'), ' Monday'),'%x-%v %W') as week_start,"
			+ " count(*) as user_logins, count(distinct SESSION_USER) as unique_users"
			+ " from SAKAI_SESSION"
			+ " group by 1";
		
		List result = m_sqlService.dbRead(mySql, null, new SqlReader()
		{
			public Object readSqlResultRecord (ResultSet result)
			{
				Logins info = new LoginsImpl ();
				try {
					info.setDate(result.getDate(1));
					info.setTotalLogins(result.getLong(2));
					info.setTotalUnique(result.getLong(3));
				} catch (SQLException e) {
					return null;
				}
				return info;
			}
		});
		
		return result;
	}
}
