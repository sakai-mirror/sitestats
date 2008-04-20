package org.sakaiproject.sitestats.api;

import java.util.Date;

public interface Logins {

	public Date getDate();
	public void setDate(Date date);

	public long getTotalLogins();
	public void setTotalLogins(long totalLogins);

	public long getTotalUnique();
	public void setTotalUnique(long totalUnique);

}
