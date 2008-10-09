function checkWhatSelection() {
	var visitsRadio = jQuery('.what-visits');
    var eventsRadio = jQuery('.what-events');
    var resourcesRadio = jQuery('.what-resources');
	if(visitsRadio.attr('checked')) {
		jQuery('#what-events-by-selectionRadio').hide();
		jQuery('#what-tools-select').hide();
		jQuery('#what-events-select').hide();
		jQuery('#what-resources-options').hide();
		jQuery('#what-resources-select').hide();
	}else if(eventsRadio.attr('checked')) {
		jQuery('#what-events-by-selectionRadio').show();
		if(jQuery('.what-events-bytool').attr('checked')) {
			jQuery('#what-tools-select').show();
			jQuery('#what-events-select').hide();
		}else{
			jQuery('#what-tools-select').hide();
			jQuery('#what-events-select').show();
		}
		jQuery('#what-resources-options').hide();
		jQuery('#what-resources-select').hide();
	}else{
		jQuery('#what-events-by-selectionRadio').hide();
		jQuery('#what-tools-select').hide();
		jQuery('#what-events-select').hide();
		if(jQuery('.whatLimitedAction').attr('checked')) {
			jQuery('.whatResourceAction').removeAttr('disabled');
		}else{
			jQuery('.whatResourceAction').attr('disabled','disabled');
		}
		if(jQuery('.whatLimitedResourceIds').attr('checked')) {
			jQuery('.whatResourceIds').removeAttr('disabled');
			jQuery('.whatResourceIds').width('auto');
		}else{
			jQuery('.whatResourceIds').attr('disabled','disabled');
			jQuery('.whatResourceIds').width('304px');
		}
		jQuery('#what-resources-options').show();
		jQuery('#what-resources-select').show();
	}
	//resourcesSelectOnDivScroll();
	setMainFrameHeightNoScroll(window.name);
}

function checkWhenSelection() {
	if(jQuery('.when-custom').attr('checked')){
		jQuery('#when-customPanel').show();
		setMainFrameHeightNoScroll(window.name);
	}else{
		jQuery('#when-customPanel').hide();
	}	
}

function checkWhoSelection() {
	if(jQuery('.who-all').attr('checked')){
		jQuery('.who-role-select').hide();
		jQuery('.who-group-select').hide();
		jQuery('.who-custom-select').hide();
	}else if(jQuery('.who-role').attr('checked')){
		jQuery('.who-role-select').show();
		jQuery('.who-group-select').hide();
		jQuery('.who-custom-select').hide();
	}else if(jQuery('.who-group').attr('checked')){
		jQuery('.who-role-select').hide();
		jQuery('.who-group-select').show();
		jQuery('.who-custom-select').hide();
	}else if(jQuery('.who-custom').attr('checked')){
		jQuery('.who-role-select').hide();
		jQuery('.who-group-select').hide();
		jQuery('.who-custom-select').show();
	}else{
		jQuery('.who-role-select').hide();
		jQuery('.who-group-select').hide();
		jQuery('.who-custom-select').hide();
	}
	setMainFrameHeightNoScroll(window.name);
}

// Resources select related functions
function resourcesSelectOnDivScroll() {
	var selectResources = document.getElementsByName("reportsForm:what-resources-select")[0];

    //The following two points achieves two things while scrolling
    //a) On horizontal scrolling: To avoid vertical
    //   scroll bar in select box when the size of 
    //   the selectbox is 8 and the count of items
    //   in selectbox is greater than 8.
    //b) On vertical scrolling: To view all the items in selectbox

    //Check if items in selectbox is greater than 8, 
    //if so then making the size of the selectbox to count of
    //items in selectbox,so that vertival scrollbar
    // won't appear in selectbox
    if (selectResources.options.length > 8){
        selectResources.size=selectResources.options.length;
    }else{
        selectResources.size=8;
    }
}

function resourcesSelectOnFocus() {
	//On focus of Selectbox, making scroll position 
    //of DIV to very left i.e 0 if it is not. The reason behind
    //is, in this scenario we are fixing the size of Selectbox 
    //to 8 and if the size of items in Selecbox is greater than 8 
    //and to implement downarrow key and uparrow key 
    //functionality, the vertical scrollbar in selectbox will
    //be visible if the horizontal scrollbar of DIV is exremely right.
    if (document.getElementById("what-resources-select-container").scrollLeft != 0){
        document.getElementById("what-resources-select-container").scrollLeft = 0;
    }

    var selectResources = document.getElementsByName("reportsForm:what-resources-select")[0];
    //Checks if count of items in Selectbox is greater 
    //than 8, if yes then making the size of the selectbox to 8.
    //So that on pressing of downarrow key or uparrowkey, 
    //the selected item should also scroll up or down as expected.
    if( selectResources.options.length > 8){
        selectResources.focus();
        selectResources.size=8;
    }
}
