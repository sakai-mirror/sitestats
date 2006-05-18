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
	<sakai:flowState bean="#{SiteListBean}"/>

	<h:panelGroup>
        <t:aliasBean alias="#{viewName}" value="SiteListBean">
            <%@include file="inc/navmenu.jsp"%>
        </t:aliasBean>
    </h:panelGroup>

	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>
	
</sakai:view>
</f:view>
