function setupDatePickerHandlers(elemName) {
    var element = $('[name="' + elemName + '"]');
    var datePicker = element.datepicker({
        format: 'yyyy-mm-dd',
        todayHighlight: true
    });
    
    // We don't want to get in a event handling loop.
    var eventInProgress = false;

    element.change(function(evt) {
    	if (eventInProgress) {
    		return;
    	}
    	
    	console.log("change called");
    	eventInProgress = true;
	        setDatePickerValue(datePicker, $(this).val());
        eventInProgress = false;
    });

    setDatePickerValue(datePicker, element.val());
    datePicker.on("changeDate", function(evt) {
    	if (eventInProgress) {
    		return;
    	}
    	
    	eventInProgress = true;
        	element.attr("value", getDateString(evt.date));
        eventInProgress = false;
    });
}

function setDatePickerValue(datePicker, value) {
    if (value != '') {
        datePicker.datepicker("setValue", value);
    }
}

function getDateString(date) {
    return date.getFullYear() + '-' + ('0' + (date.getMonth()+1)).slice(-2) + '-' + ('0' + date.getDate()).slice(-2);
}
