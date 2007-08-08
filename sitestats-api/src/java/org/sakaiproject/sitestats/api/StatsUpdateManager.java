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
package org.sakaiproject.sitestats.api;

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
	
	/**
	 * Collect (process) a Sakai event into SiteStats tables.
	 * This method is called by the default quartz job implementation and should be called for every other custom quartz
	 * job implemented to this task (collect events).
	 */
	public void collectEvent(Event e);
	/** 
	 * Collect (process) Sakai events into SiteStats tables.
	 * This method is called by the default quartz job implementation and should be called for every other custom quartz
	 * job implemented to this task (collect events).
	 */
	public void collectEvents(List<Event> events);
	/** 
	 * Collect (process) Sakai events into SiteStats tables.
	 * This method is called by the default quartz job implementation and should be called for every other custom quartz
	 * job implemented to this task (collect events).
	 */
	public void collectEvents(Event[] events);
	
}
