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
package edu.ufp.sakai.jsf;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ResourceLoader;


public class VBarChartComponent extends UIComponentBase {
	/** Our log (commons). */
	private static Log				LOG				= LogFactory.getLog(VBarChartComponent.class);
	/** Resource bundle */
	protected static ResourceLoader	msgs			= new ResourceLoader("edu.ufp.sakai.tool.statstool.bundle.Messages");

	private String[]				weekDays		= { "day_sun", "day_mon", "day_tue", "day_wed", "day_thu", "day_fri", "day_sat" };
	private String[]				visitsClasses	= { "sun1", "mon1", "tue1", "wed1", "thu1", "fri1", "sat1" };
	private String[]				activityClasses	= { "sun2", "mon2", "tue2", "wed2", "thu2", "fri2", "sat2" };
	private String					type;
	private List					column1;
	private List					column2;
	private List					column3;
	private Integer					weekOfYear;

	// private String col1Image = "vk.png";
	// private String col2Image = "vu.png";
	// private String col3Image = "vv.png";
	private String					col1Image		= "vu1.png";
	private String					col2Image		= "vu2.png";
	private String					col3Image		= "vu3.png";

	public void encodeBegin(FacesContext context) throws IOException {
		ResponseWriter writer = context.getResponseWriter();
		type = (String) getAttributes().get("type");
		column1 = (List) getAttributes().get("column1");
		column2 = (List) getAttributes().get("column2");
		column3 = (List) getAttributes().get("column3");

		if(type.equals("week")){
			Object o = getAttributes().get("weekOfYear");
			if(o != null) weekOfYear = (Integer) o;
			encodeWeekChart(writer);
		}else if(type.equals("month")) encodeMonthChart(writer);
		else if(type.equals("year")) encodeYearChart(writer);
	}

	// awstats like
	private void encodeWeekChart(ResponseWriter writer) throws IOException {
		int width = 12;
		int maximumHeight = 105;
		String weekColor = "#EAEAEA";
		String weekEndColor = "#AAAAAA";
		String mainDiv = "div id=\"vbartbl\"";
		String table = "table width=\"340px\"";
		String tr = "tr valign=\"bottom\"";
		String td = "td valign=\"bottom\"";
		String br = "br";
		String idiv = "div id=\"ivbartbl\"";
		String center = "center";
		String itable = "table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" width=\"0\"";
		String itr = "tr id=\"ivbartbl\" valign=\"bottom\"";
		String itd = "td id=\"ivbartbl\" valign=\"bottom\"";

		writer.startElement("div style=\"width: 340px\"", this);
		writer.startElement(mainDiv, this);
		writer.startElement(table, this);

		int max = getMaximumValue();
		int visitsSize = column1.size();
		int activitySize = column2.size();
		int col3Size = column3.size();
		// <tr>: Data
		writer.startElement(tr, this);
		for(int i = 0; i < 7; i++){
			writer.startElement(td, this);
			writer.startElement(idiv, this);
			writer.startElement(center, this);
			writer.startElement(itable, this);
			writer.startElement(itr, this);

			// column1
			Integer nV = new Integer(0);
			writer.startElement(itd, this);
			if(i < visitsSize) nV = (Integer) (column1.get(i));
			int height = max == 0 ? 1 : (maximumHeight * nV.intValue()) / max;
			height = height == 0 ? 1 : height;
			String tooltip = msgs.getString("legend_visits") + ": " + nV.toString();
			if(nV.intValue() > 0){
				writer.writeText(nV.toString(), null);
				writer.startElement(br, this);
				writer.endElement(br);
			}
			String element = "img align=\"bottom\" src=\"sitestats/images/" + col1Image + "\" height=\"" + height + "\" width=\"" + width + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);
			writer.endElement(itd);

			// column2
			Integer nA = new Integer(0);
			writer.startElement(itd, this);
			if(i < activitySize) nA = (Integer) (column2.get(i));
			height = max == 0 ? 1 : (maximumHeight * nA.intValue()) / max;
			height = height == 0 ? 1 : height;
			tooltip = msgs.getString("legend_activity") + ": " + nA.toString();
			if(nA.intValue() > 0){
				writer.writeText(nA.toString(), null);
				writer.startElement(br, this);
				writer.endElement(br);
			}
			element = "img align=\"bottom\" src=\"sitestats/images/" + col2Image + "\" height=\"" + height + "\" width=\"" + width + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);
			writer.endElement(itd);

			// column3
			Integer n3 = new Integer(0);
			writer.startElement(itd, this);
			if(i < col3Size) n3 = (Integer) (column3.get(i));
			height = max == 0 ? 1 : (maximumHeight * n3.intValue()) / max;
			height = height == 0 ? 1 : height;
			tooltip = msgs.getString("legend_unique_visitors") + ": " + n3.toString();
			if(n3.intValue() > 0){
				writer.writeText(n3.toString(), null);
				writer.startElement(br, this);
				writer.endElement(br);
			}
			element = "img align=\"bottom\" src=\"sitestats/images/" + col3Image + "\" height=\"" + height + "\" width=\"" + width + "\" alt='" + tooltip + "' title='" + tooltip + "'";
			writer.startElement(element, this);
			writer.endElement(element);
			writer.endElement(itd);

			writer.endElement(itr);
			writer.endElement(itable);
			writer.endElement(center);
			writer.endElement(idiv);
			writer.endElement(td);
		}
		writer.endElement(tr);

		// <tr>: Labels
		String td_week = "td bgcolor=\"" + weekColor + "\" valign=\"middle\" align=\"center\" height=\"10px\"";
		String td_weekend = "td bgcolor=\"" + weekEndColor + "\" valign=\"middle\" align=\"center\" height=\"10px\"";
		writer.startElement(tr, this);
		for(int i = 0; i < 7; i++){
			boolean isWeekEnd = (i == 0 || i == 6);
			writer.startElement(isWeekEnd ? td_weekend : td_week, this);
			writer.writeText(msgs.getString(weekDays[i]), null);
			writer.endElement(isWeekEnd ? td_weekend : td_week);
		}
		writer.endElement(tr);

		writer.endElement(table);
		writer.endElement(mainDiv);

		// <table>: Legend
		encodeWeekChartLegend(writer, weekEndColor);
		writer.endElement("div style=\"width: 340px\"");
	}

	private void encodeWeekChartLegend(ResponseWriter writer, String borderColor) throws IOException {
		String table = "table id=\"vbartbl\" width=\"100%\"";
		String trS = "tr height=\"5px\"";
		String trL1 = "tr";
		String trL2 = "tr height=\"12px\"";
		String tdLeft = "td style=\"text-align: left; white-space: nowrap; font-weight: bold; width: 50%;\"";
		String tdRight = "td style=\"text-align: right; white-space: nowrap; font-weight: bold; width: 50%;\"";
		String tdLegend = "td style=\"width: 100%\" colspan=\"2\"";

		// calc dates if weekNumber != null
		String iDate = new String();
		String fDate = new String();
		if(weekOfYear != null){
			Calendar c = Calendar.getInstance();
			c.set(Calendar.WEEK_OF_YEAR, weekOfYear.intValue());
			c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			c.add(Calendar.DATE, -1);
			// sunday
			iDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
			c.add(Calendar.DATE, 6);
			// monday
			fDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);

			// if(c.getFirstDayOfWeek() == Calendar.SUNDAY){
			// c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
			// iDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1)
			// + "-" + c.get(Calendar.DAY_OF_MONTH);
			// c.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
			// fDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1)
			// + "-" + c.get(Calendar.DAY_OF_MONTH);
			// }else if(c.getFirstDayOfWeek() == Calendar.MONDAY){
			// c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			// c.add(Calendar.DATE, -1);
			// iDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1)
			// + "-" + c.get(Calendar.DAY_OF_MONTH);
			// c.add(Calendar.DATE, 6);
			// fDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1)
			// + "-" + c.get(Calendar.DAY_OF_MONTH);
			// }
		}

		writer.startElement(table, this);

		// tr: spacer line
		writer.startElement(trS, this);
		writer.startElement(tdLeft, this);
		writer.endElement(tdLeft);
		writer.startElement(tdRight, this);
		writer.endElement(tdRight);
		writer.endElement(trS);

		// tr: LINE1: START
		writer.startElement(trL1, this);
		// left date
		writer.startElement(tdLeft, this);
		writer.writeText(iDate, null);
		writer.endElement(tdLeft);
		// right date
		writer.startElement(tdRight, this);
		writer.writeText(fDate, null);
		writer.endElement(tdRight);
		// tr: LINE1 END
		writer.endElement(trL1);

		// tr: LINE2: START
		writer.startElement(trL2, this);
		writer.startElement(tdLegend, this);

		// legend: column1
		String img = "img src=\"sitestats/images/" + col1Image + "\" height=\"8\" width=\"12\" style=\"border: 1px solid " + borderColor + "; padding: 1px;\"";
		writer.startElement(img, this);
		writer.endElement(img);
		writer.writeText(" " + msgs.getString("legend_visits"), null);

		writer.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");

		// legend: column2
		img = "img src=\"sitestats/images/" + col2Image + "\" height=\"8\" width=\"12\" style=\"border: 1px solid " + borderColor + "; padding: 1px;\"";
		writer.startElement(img, this);
		writer.endElement(img);
		writer.writeText(" " + msgs.getString("legend_activity"), null);

		writer.write("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");

		// legend: column3
		img = "img src=\"sitestats/images/" + col3Image + "\" height=\"8\" width=\"12\" style=\"border: 1px solid " + borderColor + "; padding: 1px;\"";
		writer.startElement(img, this);
		writer.endElement(img);
		writer.writeText(" " + msgs.getString("legend_unique_visitors"), null);

		// tr: LINE2 END
		writer.endElement(tdLegend);
		writer.endElement(trL2);

		writer.endElement(table);
	}

	// apples-to-oranges.com like
	private void __encodeWeekChart(ResponseWriter writer) throws IOException {
		writer.startElement("div id=\"vertgraph\"", this);
		writer.startElement("ul", this);

		int max = getMaximumValue();
		for(int i = 0; i < 7; i++){
			// column1
			Integer nV = (Integer) (column1.get(i));
			int height = max == 0 ? 0 : (150 * nV.intValue()) / max;
			String element = "li class=\"" + visitsClasses[i] + "\" style=\"height: " + height + "px;\"";
			writer.startElement(element, this);
			if(nV.intValue() > 0) writer.writeText(nV.toString(), null);
			writer.endElement(element);

			// column2
			Integer nA = (Integer) (column2.get(i));
			height = max == 0 ? 0 : (150 * nA.intValue()) / max;
			element = "li class=\"" + activityClasses[i] + "\" style=\"height: " + height + "px;\"";
			writer.startElement(element, this);
			if(nA.intValue() > 0) writer.writeText(nA.toString(), null);
			writer.endElement(element);
		}

		writer.endElement("ul");
		writer.endElement("div id=\"vertgraph\"");
	}

	private void encodeMonthChart(ResponseWriter writer) throws IOException {

	}

	private void encodeYearChart(ResponseWriter writer) throws IOException {

	}

	private void encodeGraph(ResponseWriter writer) throws IOException {
		writer.startElement("table cellspacing=\"0\" cellpadding=\"0\" id=\"vbar\"", this);
		// caption
		writer.startElement("caption align=\"top\"", this);
		writer.writeText("Current week visits", null);
		writer.endElement("caption align=\"top\"");
		// header
		writer.startElement("tr", this);
		writer.startElement("th scope=\"col\"", this);
		writer.endElement("th scope=\"col\"");
		writer.startElement("th scope=\"col\"", this);
		writer.endElement("th scope=\"col\"");
		writer.endElement("tr");

		// first data row
		writer.startElement("tr", this);
		writer.startElement("td class=\"first\"", this);
		writer.writeText("Monday", null);
		writer.endElement("td class=\"first\"");
		writer.startElement("td class=\"value first\"", this);
		writer.startElement("div style=\"background: #a4a4a4\" width=\"200\" height=\"16\"", this);
		// writer.startElement("img src=\"sitestats/images/bar.png\" alt=\"\"
		// width=\"200\" height=\"16\"", this);
		writer.writeText("17.12", null);
		// writer.endElement("img src=\"sitestats/images/bar.png\" alt=\"\"
		// width=\"200\" height=\"16\"");
		writer.endElement("div style=\"background: #a4a4a4\" width=\"200\" height=\"16\"");
		writer.endElement("td class=\"value first\"");
		writer.endElement("tr");

		// data rows
		// for(int i=0; i<3; i++){
		writer.startElement("tr", this);
		writer.startElement("td", this);
		writer.writeText("Tuesday", null);
		writer.endElement("td");
		writer.startElement("td class=\"value\"", this);
		writer.startElement("img src=\"sitestats/images/bar.png\" alt=\"\" width=\"104\" height=\"16\"", this);
		writer.writeText("8.88", null);
		writer.endElement("img src=\"sitestats/images/bar.png\" alt=\"\" width=\"200\" height=\"16\"");
		writer.endElement("td class=\"value\"");
		writer.endElement("tr");
		// }

		writer.endElement("table cellspacing=\"0\" cellpadding=\"0\" id=\"vbar\"");

		/*
		 * <caption align="top">Top banana importers 1998 (value of banana
		 * imports in millions of US dollars per million people)<br /><br /></caption>
		 * <tr> <th scope="col"><span class="auraltext">Country</span> </th>
		 * <th scope="col"><span class="auraltext">Millions of US dollars per
		 * million people</span> </th> </tr> <tr> <td class="first">Sweden</td>
		 * <td class="value first"><img src="bar.png" alt="" width="200"
		 * height="16" />17.12</td> </tr> <tr> <td>United&nbsp;Kingdom</td>
		 * <td class="value"><img src="bar.png" alt="" width="104" height="16"
		 * />8.88</td> </tr> <tr>
		 */

	}

	private void encodeGraph2(ResponseWriter writer) throws IOException {
		writer.startElement("div id=\"vertgraph\"", this);
		writer.startElement("ul", this);

		writer.startElement("li class=\"sun1\" style=\"height: 40px;\"", this);
		writer.writeText("2", null);
		writer.endElement("li class=\"sun1\" style=\"height: 40px;\"");
		writer.startElement("li class=\"sun2\" style=\"height: 40px;\"", this);
		writer.writeText("2", null);
		writer.endElement("li class=\"sun2\" style=\"height: 40px;\"");

		writer.startElement("li class=\"mon1\" style=\"height: 150px;\"", this);
		writer.writeText("22", null);
		writer.endElement("li class=\"mon1\" style=\"height: 150px;\"");
		writer.startElement("li class=\"mon2\" style=\"height: 150px;\"", this);
		writer.writeText("22", null);
		writer.endElement("li class=\"mon2\" style=\"height: 150px;\"");

		writer.startElement("li class=\"tue1\" style=\"height: 80px;\"", this);
		writer.writeText("7", null);
		writer.endElement("li class=\"tue1\" style=\"height: 80px;\"");
		writer.startElement("li class=\"tue2\" style=\"height: 80px;\"", this);
		writer.writeText("7", null);
		writer.endElement("li class=\"tue2\" style=\"height: 80px;\"");

		writer.startElement("li class=\"wed1\" style=\"height: 50px;\"", this);
		writer.writeText("3", null);
		writer.endElement("li class=\"wed1\" style=\"height: 50px;\"");
		writer.startElement("li class=\"wed2\" style=\"height: 50px;\"", this);
		writer.writeText("3", null);
		writer.endElement("li class=\"wed2\" style=\"height: 50px;\"");

		writer.startElement("li class=\"thu1\" style=\"height: 90px;\"", this);
		writer.writeText("8", null);
		writer.endElement("li class=\"thu1\" style=\"height: 90px;\"");
		writer.startElement("li class=\"thu2\" style=\"height: 90px;\"", this);
		writer.writeText("8", null);
		writer.endElement("li class=\"thu2\" style=\"height: 90px;\"");

		writer.startElement("li class=\"fri1\" style=\"height: 40px;\"", this);
		writer.writeText("2", null);
		writer.endElement("li class=\"fri1\" style=\"height: 40px;\"");
		writer.startElement("li class=\"fri2\" style=\"height: 40px;\"", this);
		writer.writeText("2", null);
		writer.endElement("li class=\"fri2\" style=\"height: 40px;\"");

		writer.startElement("li class=\"sat1\" style=\"height: 40px;\"", this);
		writer.writeText("2", null);
		writer.endElement("li class=\"sat1\" style=\"height: 40px;\"");
		writer.startElement("li class=\"sat2\" style=\"height: 40px;\"", this);
		writer.writeText("2", null);
		writer.endElement("li class=\"sat2\" style=\"height: 40px;\"");

		writer.endElement("ul");
		writer.endElement("div id=\"vertgraph\"");
	}

	private int getMaximumValue() {
		int max = 1;
		Iterator v = column1.iterator();
		while (v.hasNext()){
			int val = ((Integer) v.next()).intValue();
			if(val > max) max = val;
		}
		Iterator a = column2.iterator();
		while (a.hasNext()){
			int val = ((Integer) a.next()).intValue();
			if(val > max) max = val;
		}
		return max;
	}

	public String getFamily() {
		return "SiteStatsFamily";
	}
}
