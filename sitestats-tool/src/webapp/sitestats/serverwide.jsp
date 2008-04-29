
<%/* #####  TAGLIBS, BUNDLE, Etc  ##### */%>
<%@include file="inc/common.jsp"%>


<f:view>
<sakai:view title="#{msgs.tool_title}">

	<%/* #####  CSS  ##### */%>
	<style type="text/css">
		@import url("/sakai-sitestats-tool/sitestats/css/sitestats.css");
	</style>
	
	<%/* #####  JAVASCRIPT  ##### */%>
	<sakai:script path="/sitestats/script/common.js"/>

	<%/* #####  FACES MESSAGES  ##### */%>
	<f:subview id="allowed" rendered="#{!ServiceBean.allowed}">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>

	<a4j:form id="serverWideReportForm" rendered="#{ServerWideReportBean.allowed}">
	    
		<%/* #####  MENU  ##### */%>
		<h:panelGroup>
	        <t:aliasBean alias="#{viewName}" value="ServerWideReportBean">
	            <f:subview id="menu">
					<%@include file="inc/navmenu.jsp"%>
				</f:subview>
	        </t:aliasBean>
        </h:panelGroup>
		
		<%/* #####  TITLE  ##### */%>
		<t:htmlTag value="h2">
			<h:outputText value="#{msgs.menu_serverwide}"/>
			<a4j:status id="reportChartStatus" startText="..." stopText=" " startStyleClass="ajaxLoading"/>	            		
		</t:htmlTag>
		<sakai:instruction_message value="#{msgs.instructions_serverwide}" />
		
		<%/* #####  PAGE CONTENT  ##### */%>
		<t:div style="width:100%">

			<%/* #####  REPORTS  ##### */%>
			<h:panelGrid id="reportMainArea" styleClass="sectionContainerNav" style="width:100%" columns="2" columnClasses="left30,left70">
				<t:div styleClass="left30">		
					<a4j:region id="reportListRegion">
						<%/* #####  REPORT SELECTORS  ##### */%>
						<a4j:outputPanel id="reportSelectors">					
							<a4j:commandLink id="reportLoginSel" value="#{msgs.submenu_login_report}" actionListener="#{ChartParams.selectLoginReportType}" rendered="#{ChartParams.selectedReportType ne 'login'}"
			                	status="reportChartStatus" reRender="reportSelectors,reportChartPanel" styleClass="selector"
	                			oncomplete="setReportChartRenderFalse()"/>
							<t:outputText id="reportLoginLbl" value="#{msgs.submenu_login_report}" rendered="#{ChartParams.selectedReportType eq 'login'}" styleClass="selector"/>
							
		                    <t:htmlTag value="br"/>
							
							<a4j:commandLink id="reportToolSel" value="#{msgs.submenu_tool_report}" actionListener="#{ChartParams.selectToolReportType}" rendered="#{ChartParams.selectedReportType ne 'tool'}"
			                	status="reportChartStatus" reRender="reportSelectors,reportChartPanel" styleClass="selector"
	                			oncomplete="setReportChartRenderFalse()"/>
							<t:outputText id="reportToolLbl" value="#{msgs.submenu_tool_report}" rendered="#{ChartParams.selectedReportType eq 'tool'}" styleClass="selector"/>
							
		                    <t:htmlTag value="br"/>
							
							<a4j:commandLink id="reportUserSiteSel" value="#{msgs.submenu_user_site_report}" actionListener="#{ChartParams.selectUserSiteReportType}" rendered="#{ChartParams.selectedReportType ne 'user-site'}"
			                	status="reportChartStatus" reRender="reportSelectors,reportChartPanel" styleClass="selector"
	                			oncomplete="setReportChartRenderFalse()"/>
							<t:outputText id="reportUserSiteLbl" value="#{msgs.submenu_user_site_report}" rendered="#{ChartParams.selectedReportType eq 'user-site'}" styleClass="selector"/>
						</a4j:outputPanel>						
		            </a4j:region>
				</t:div>	           
	            
				<t:div styleClass="left70">		
					<a4j:region id="reportChartRegion">									
						<%/* #####  ACTIVITY CHART  ##### */%>
						<t:outputText id="chosenReportLbl" value="Chosen Report"/>
	                    <t:htmlTag value="br"/>
	            		<a4j:outputPanel id="reportChartPanel">
	            			<t:commandLink action="maximize" title="#{msgs.click_to_max}" actionListener="#{ChartParams.selectMaximizedReport}">
		            			<a4j:mediaOutput 
									id="reportChart"
									element="img" cacheable="false"
									createContent="#{ServerWideReportBean.generateReportChart}" 
									value="#{ChartParams}"
									mimeType="image/png"
			                        rendered="#{ChartParams.renderReportChart}"
								/>
							</t:commandLink>
		                    <t:htmlTag value="br"/>
	                    </a4j:outputPanel>
		            </a4j:region>	                 
				</t:div>	           
            </h:panelGrid>	

            <t:htmlTag value="br"/>
			
		</t:div>		    
		
		<a4j:jsFunction name="renderReportChart"
			actionListener="#{ChartParams.renderReportChart}"
		    reRender="reportSelectors,reportChartPanel" status="reportChartStatus"
		    immediate="true" oncomplete="setMainFrameHeightNoScroll(window.name, 640);">  
		    <a4j:actionparam name="chartWidth"/>
		   	<a4j:actionparam name="chartHeight"/>
		    <a4j:actionparam name="backgroundColor"/>          
        </a4j:jsFunction>
		    
		<%/* #####  Set chart params in bean ##### */%>
		<a4j:jsFunction name="setChartParameters"
			actionListener="#{ChartParams.setChartParameters}" immediate="true" >
		   	<a4j:actionparam name="chartWidth"/>
		   	<a4j:actionparam name="chartHeight"/>
		    <a4j:actionparam name="backgroundColor"/>
		</a4j:jsFunction>
		
		<%/* #####  Set chart and summary tables render flags to false on startup ##### */%>
		<a4j:jsFunction name="setChartsRenderFalse" actionListener="#{ChartParams.setAllRenderFalse}" immediate="true"/>
		<a4j:jsFunction name="setReportChartRenderFalse" actionListener="#{ChartParams.setReportRenderFalse}" immediate="true"/>		    
		
		
	    
	</a4j:form>
	
	<f:verbatim>
       	<script type="text/javascript">
       		function getMainAreaWidth(){
       			return document.getElementById('serverWideReportForm:reportMainArea').offsetWidth - 10;
       		}
       		function getChartWidth(){
       			//return document.getElementById('serverWideReportForm:left').offsetWidth;
       			return (getMainAreaWidth() / 2);
       		}
       		function getChartHeight(){
       			return 200;
       		}
       		function getBodyBackgroundColor(){
       			var bgColor;
				if(document.body.currentStyle){
					// IE based
					if(window.name && parent.document.getElementById(window.name)){
						bgColor = parent.document.getElementById(window.name).currentStyle["backgroundColor"];
					}else{
                   		bgColor = document.body.currentStyle["backgroundColor"];
                   	}
                   }else if(window.getComputedStyle){
                   	// Mozilla based
                   	var elstyle;
                   	if(parent.document.getElementById(window.name)){
                   		elstyle = window.getComputedStyle(parent.document.getElementById(window.name), "");
                   	}else{
                   		elstyle = window.getComputedStyle(document.body, "");
                   	}
                       bgColor = elstyle.getPropertyValue("background-color");
                   }
                   if(bgColor == 'transparent') bgColor = "white";
                   return bgColor;
       		}
       	</script>
	</f:verbatim>		
	
	<f:subview id="reportPartialLoader">
		<f:verbatim>
        	<script type="text/javascript">
                 	renderReportChart(getChartWidth(), getChartHeight(), 'white');
        	</script>
		</f:verbatim>
	</f:subview>
   
	
	
</sakai:view>
</f:view>
