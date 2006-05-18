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

import java.io.Serializable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.ufp.sakai.tool.statstool.api.PrefsEntry;

/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class PrefsEntryImpl implements PrefsEntry, Serializable {
	private static final long	serialVersionUID	= 1L;
	private String siteId;
	private String eventId;

	public boolean equals(Object o) {
		if(!(o instanceof PrefsEntryImpl)) return false;
		PrefsEntryImpl other = (PrefsEntryImpl) o;
		boolean sameSite = siteId.equals(other.getSiteId());
		boolean sameEvent = eventId.equals(other.getEventId());
		return (sameSite && sameEvent);
	}

	public int hashCode() {
		HashCodeBuilder b = new HashCodeBuilder();
		b.append(siteId);
		b.append(eventId);
		return b.toHashCode();
	}

	/* (non-Javadoc)
	 * @see edu.ufp.sakai.tool.statstool.api.PrefsEntry#getSiteId()
	 */
	public String getSiteId() {
		return this.siteId;
	}

	/* (non-Javadoc)
	 * @see edu.ufp.sakai.tool.statstool.api.PrefsEntry#setSiteId(java.lang.String)
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/* (non-Javadoc)
	 * @see edu.ufp.sakai.tool.statstool.api.PrefsEntry#getEventId()
	 */
	public String getEventId() {
		return eventId;
	}

	/* (non-Javadoc)
	 * @see edu.ufp.sakai.tool.statstool.api.PrefsEntry#setEventId(java.lang.String)
	 */
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

}
