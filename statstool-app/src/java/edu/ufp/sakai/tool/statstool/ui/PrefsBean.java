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
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;

import edu.ufp.sakai.tool.statstool.api.StatsManager;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class PrefsBean extends BaseBean implements Serializable {
	private static final long	serialVersionUID		= 1L;

	/** Our log (commons). */
	private static Log			LOG						= LogFactory.getLog(PrefsBean.class);

	/** Bean members */
	private Map					eventNames				= null;
	private List				availableEvents			= null;
	private String[]			configuredEvents		= null;
	private String[]			tempConfiguredEvents	= null;

	/** Statistics Manager object */
	private String				message;
	private boolean				updatedEvents			= false;
	private boolean				noEventsSelected		= false;
	private StatsManager		sm						= (StatsManager) ComponentManager.get(StatsManager.class.getName());
	private ToolManager			M_tm					= (ToolManager) ComponentManager.get(ToolManager.class.getName());
	private Collator			collator				= Collator.getInstance();

	// ######################################################################################
	// Main methods
	// ######################################################################################
	public void init() {
		LOG.debug("PrefsBean.init()");

		if(isAllowed()){
			eventNames = getEventNames();
			availableEvents = getAvailableEvents();
			configuredEvents = getConfiguredEvents();

			// show message
			if(updatedEvents){
				FacesContext fc = FacesContext.getCurrentInstance();
				fc.addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
				updatedEvents = false;
			}else if(noEventsSelected){
				FacesContext fc = FacesContext.getCurrentInstance();
				fc.addMessage("msg", new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
				noEventsSelected = false;
			}

			Collections.sort(availableEvents, getComboItemsComparator(collator));
		}
	}

	// ######################################################################################
	// Action/ActionListener methods
	// ######################################################################################
	public String update() {
		int size = tempConfiguredEvents.length;
		if(size == 0){
			noEventsSelected = true;
			message = msgs.getString("prefs_noeventsselected");
		}else{
			List newPrefs = new ArrayList();
			configuredEvents = new String[size];
			for(int i = 0; i < size; i++){
				configuredEvents[i] = tempConfiguredEvents[i];
				newPrefs.add(tempConfiguredEvents[i]);
			}
			sm.setSiteConfiguredEventIds(getSiteId(), newPrefs);
			updatedEvents = true;
			message = msgs.getString("prefs_updated");
		}
		tempConfiguredEvents = null;
		return "prefs";
	}

	public String cancel() {
		tempConfiguredEvents = null;
		return "prefs";
	}

	// ######################################################################################
	// Generic get/set methods
	// ######################################################################################
	public List getAvailableEvents() {
		if(availableEvents == null){
			availableEvents = new ArrayList();
			List l = sm.getRegisteredEventIds();
			Iterator i = l.iterator();
			while (i.hasNext()){
				String eId = (String) i.next();
				availableEvents.add(new SelectItem(eId, (String) eventNames.get(eId)));
			}
		}
		return availableEvents;
	}

	public String[] getConfiguredEvents() {
		if(tempConfiguredEvents == null){
			List l = sm.getSiteConfiguredEventIds(getSiteId());
			int size = l.size();
			if(size == 0){
				l = sm.getRegisteredEventIds();
				size = l.size();
			}
			configuredEvents = new String[size];
			tempConfiguredEvents = new String[size];
			Iterator i = l.iterator();
			int n = 0;
			while (i.hasNext()){
				configuredEvents[n] = (String) i.next();
				tempConfiguredEvents[n] = configuredEvents[n];
				n++;
			}
		}
		return tempConfiguredEvents;
	}

	public void setConfiguredEvents(String[] events) {
		this.tempConfiguredEvents = events;
	}

	public String getSiteId() {
		Placement placement = M_tm.getCurrentPlacement();
		return placement.getContext();
	}

	public Map getEventNames() {
		if(eventNames == null) eventNames = sm.getEventNameMap();
		return eventNames;
	}

	public static final Comparator getComboItemsComparator(final Collator collator) {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				if(o1 instanceof SelectItem && o2 instanceof SelectItem){
					SelectItem r1 = (SelectItem) o1;
					SelectItem r2 = (SelectItem) o2;
					return collator.compare(r1.getLabel().toLowerCase(), r2.getLabel().toLowerCase());
				}
				return 0;
			}
		};
	}
}
