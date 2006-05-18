<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<% 
	response.setContentType("text/html; charset=UTF-8");
	response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
	response.addDateHeader("Last-Modified", System.currentTimeMillis());
	response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	response.addHeader("Pragma", "no-cache");
%>
<f:loadBundle basename="edu.ufp.sakai.tool.statstool.bundle.Messages" var="msgs"/>

<f:view>
<sakai:view title="#{msgs.tool_title}">
	<sakai:flowState bean="#{EventsBean}"/>

	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>	
	
	<style type="text/css">
		@import url("sitestats/css/sitestats.css");
	</style>	
	        
		<h:panelGroup rendered="#{BaseBean.allowed}">
			<t:aliasBean  alias="#{viewName}" value="EventsBean">
				<f:subview id="menu">
					<%@include file="inc/navmenu.jsp"%>
				</f:subview>
	        </t:aliasBean>
	    </h:panelGroup>    
	    
		<h3><h:outputText value="#{msgs.menu_events}" rendered="#{BaseBean.allowed}"/></h3>
		
		<t:div style="width:100%" rendered="#{BaseBean.allowed}">
		<t:aliasBean  alias="#{bean}" value="#{EventsBean}">
			<f:subview id="filtering">
				<%@include file="inc/filtering.jsp"%>
		</f:subview>
	    </t:aliasBean>
	    <%/*<t:aliasBeansScope>
		    <t:aliasBean alias="#{bean}" value="#{EventsBean}"/>
		    <t:aliasBean alias="#{bundle}" value="#{msgs}"/>
		    <f:subview id="filtering">*/%>
		        <%/*@include file="inc/filtering.jsp"*/%>
		    <%/*</f:subview>
		</t:aliasBeansScope>*/%>
	    </t:div>

	<h:form rendered="#{BaseBean.allowed}">			
	<t:div style="width:100%">
		<t:dataTable
			value="#{EventsBean.events}"
			var="row"
			styleClass="listHier narrowTable"
			columnClasses="left,left,left,right"
			sortColumn="#{EventsBean.sortColumn}" 
            sortAscending="#{EventsBean.sortAscending}"
            first="#{EventsBean.firstItem}"
            rows="#{EventsBean.pageSize}">
			<h:column>
				<f:facet name="header">	 
		            <t:commandSortHeader columnName="id" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.th_id}"/>		                
		            </t:commandSortHeader>               
		        </f:facet>
		        <h:outputText value="#{row.userId}"/>
			</h:column>
			<h:column>
				<f:facet name="header">	 
		            <t:commandSortHeader columnName="user" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.th_user}"/>		                
		            </t:commandSortHeader>               
		        </f:facet>
		        <h:outputText value="#{EventsBean.userNames[row.userId]}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <t:commandSortHeader columnName="event" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.th_event}"/>		                
		            </t:commandSortHeader>   	                
		        </f:facet>
		        <h:outputText value="#{EventsBean.eventNames[row.eventId]}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <t:commandSortHeader columnName="date" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.th_date}"/>		                
		            </t:commandSortHeader>   	  	                
		        </f:facet>		  
		        <t:outputText value="#{row.dateAsString}"/>    
			</h:column>
			<h:column >
				<f:facet name="header">
		            <t:commandSortHeader columnName="total" immediate="true" arrow="true">
		                <h:outputText value="#{msgs.th_total}" styleClass="center"/>		                
		            </t:commandSortHeader>  		                
		        </f:facet>		  
		        <h:outputText value="#{row.total}" style="text-align: right;"/>    
			</h:column>
		</t:dataTable>
		<p class="instruction">
			<h:outputText value="#{msgs.no_data}" rendered="#{EventsBean.emptyList}" />
		</p>
		
		<p class="act">
			<h:commandButton id="exportXls" actionListener="#{EventsBean.exportEventsXls}" value="#{msgs.bt_export_excel}" rendered="#{!EventsBean.emptyList}" />
			<h:commandButton id="exportCsv" actionListener="#{EventsBean.exportEventsCsv}" value="#{msgs.bt_export_csv}" rendered="#{!EventsBean.emptyList}" />
		</p>
		</t:div>
	</h:form>
		
</sakai:view>
</f:view>
