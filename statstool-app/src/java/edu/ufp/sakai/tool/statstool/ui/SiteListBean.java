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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ResourceLoader;

import edu.ufp.sakai.tool.statstool.jsf.InitializableBean;


/**
 * @author <a href="mailto:nuno@ufp.pt">Nuno Fernandes</a>
 */
public class SiteListBean extends InitializableBean implements Serializable {
	private static final long		serialVersionUID	= 1L;

	/** Our log (commons). */
	private static Log				LOG					= LogFactory.getLog(SiteListBean.class);

	/** Resource bundle */
	protected static ResourceLoader	msgs				= new ResourceLoader("edu.ufp.sakai.tool.statstool.bundle.Messages");

	// ######################################################################################
	// Main methods
	// ######################################################################################
	public void init() {
		LOG.debug("SiteListsBean.init()");
	}

	// ######################################################################################
	// ActionListener methods
	// ######################################################################################

	// ######################################################################################
	// Generic get/set methods
	// ######################################################################################

}
