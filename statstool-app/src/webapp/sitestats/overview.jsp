<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sitestats" prefix="sst" %>
<% 
	response.setContentType("text/html; charset=UTF-8");
	response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
	response.addDateHeader("Last-Modified", System.currentTimeMillis());
	response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	response.addHeader("Pragma", "no-cache");	/*prepEventsForm();*/
%>
<f:loadBundle basename="edu.ufp.sakai.tool.statstool.bundle.Messages" var="msgs"/>

<f:view>
<sakai:view title="#{msgs.tool_title}">
	<sakai:flowState bean="#{OverviewBean}"/>

	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>	
	
	<style type="text/css">
		@import url("sitestats/css/sitestats.css");
	</style>	

	<h:form id="overviewForm" rendered="#{BaseBean.allowed}">
		<h:panelGroup>
	        <t:aliasBean alias="#{viewName}" value="OverviewBean">
	            <f:subview id="menu">
					<%@include file="inc/navmenu.jsp"%>
				</f:subview>
	        </t:aliasBean>
	    </h:panelGroup>
	    
		<h3><h:outputText value="#{msgs.menu_overview}" /></h3>

		<f:subview id="allowed">
			<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
		</f:subview>
	
	<t:div style="width:100%">
		 <h:panelGrid styleClass="sectionContainerNav" style="width:100%" columns="2" columnClasses="sst,sst">               
			<t:div style="text-align: left; white-space: nowrap; vertical-align:top;">   
            	<f:verbatim><h4></f:verbatim><h:outputText value="#{msgs.overview_title1}"/><f:verbatim></h4></f:verbatim>
					<sst:vbarchart 
						type="week" 
						weekOfYear="#{OverviewBean.weekOfYear}" 
						column1="#{OverviewBean.weekVisits}" 
						column2="#{OverviewBean.weekActivity}" 
						column3="#{OverviewBean.weekUniqueVisitors}"
					/>
			</t:div> 
            
            <t:div style="text-align: left; white-space: nowrap; vertical-align:top;">            
                        <f:verbatim><h4></f:verbatim><h:outputText value="#{msgs.overview_title2}" style="text-align: left;"/><f:verbatim></h4></f:verbatim>
                        <h:panelGrid  styleClass="sectionContainerNav" style="width: 100%;" columns="2">
                                <t:div style="text-align: left; white-space: nowrap;">
                                        <h:panelGrid styleClass="sectionContainerNav" style="width: 100%;" columns="1">
                                                <h:outputText value="#{msgs.overview_total_visits}" style="font-weight: bold;"/>            
                                                <h:outputText value="#{msgs.overview_last_week_visits_average}" style="font-weight: bold;"/>
                                                <h:outputText value="#{msgs.overview_last_month_visits_average}" style="font-weight: bold;"/>          
                                        </h:panelGrid>
                                </t:div>
                                <t:div style="text-align: right; white-space: nowrap;">
                                        <h:panelGrid styleClass="sectionContainerNav" style="width: 100%;" columns="1">
                                                <h:outputText value="#{OverviewBean.totalVisits}"/>            
                                                <h:outputText value="#{OverviewBean.lastWeekVisitsAverage}"/>          
                                                <h:outputText value="#{OverviewBean.lastMonthVisitsAverage}"/>         
                                        </h:panelGrid>
                                </t:div>
                        </h:panelGrid>

			</t:div>
		</h:panelGrid>
	</t:div>
		
		
		<%/*<c:chart datasource="#{OverviewBean.lineDataSet}" id="chart1" type="line">
		</c:chart>
		
		<sst:vbarchart type="pie"/> 
		
		<t:graphicImage url="#{OverviewBean.chartURL}"/>
		
		<c:chart 
			id="chart1" 
			datasource="#{OverviewBean.pieDataSet}" 
			type="pie" is3d="true" background="gray" antialias="true" 
			title="Example Chart" xlabel="X Label" ylabel="Y Label" 
			height="300" width="400">
		</c:chart>*/%>
		
	
	</h:form>
</sakai:view>
</f:view>