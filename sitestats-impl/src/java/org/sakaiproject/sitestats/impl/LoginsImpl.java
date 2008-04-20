package org.sakaiproject.sitestats.impl;

import java.io.Serializable;
import java.util.Date;

import org.sakaiproject.sitestats.api.Logins;

public class LoginsImpl implements Logins, Serializable {
	private static final long serialVersionUID	= 1L;

	private Date date;
	private long totalLogins;
	private long totalUnique;	

	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}

	public long getTotalLogins() {
		return totalLogins;
	}

	public void setTotalLogins(long totalLogins) {
		this.totalLogins = totalLogins;
	}

	public long getTotalUnique() {
		return totalUnique;
	}

	public void setTotalUnique(long totalUnique) {
		this.totalUnique = totalUnique;
	}

}
