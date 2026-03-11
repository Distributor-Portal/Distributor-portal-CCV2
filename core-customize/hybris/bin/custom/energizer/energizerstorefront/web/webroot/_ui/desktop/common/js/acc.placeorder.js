var spinnerLoader = '<div class="spinnerDiv"><img class="spinnerImg" src="/_ui/desktop/common/images/spinner.gif"/></div>';

ACC.placeorder = {
		
	bindAll: function ()
	{
		this.bindPlaceOrder();
		this.updatePlaceOrderButton();
		this.bindSecurityCodeWhatIs();
		this.getEstimatedDeliveryDate();
		//this.refreshEstimatedDeliveryDate();
	},

	bindPlaceOrder: function ()
	{
		$(".placeOrderWithSecurityCode").on("click", function ()
		{
			ACC.common.blockFormAndShowProcessingMessage($(this));
			$(".securityCodeClass").val($("#SecurityCode").val());
			$("#placeOrderForm1").submit();
		});
	},
	
	showAndHideLoader: function(flag){    
        //use flag value 'block' to show loader
        //use flag value 'none' to hide loader
        
        $(".spinnerDiv").css("display",flag);
        //console.log('spinner function called ...' + flag);    
	},
  
	updatePlaceOrderButton: function ()
	{
		
		$(".place-order").removeAttr("disabled");
		// need rewrite /  class changes
	},

	bindSecurityCodeWhatIs: function ()
	{
		$('.security_code_what').bt($("#checkout_summary_payment_div").data("securityWhatText"),
				{
					trigger: 'click',
					positions: 'bottom',
					fill: '#efefef',
					cssStyles: {
						fontSize: '11px'
					}
				});
	},
	
getEstimatedDeliveryDate : function(){
    	
		var leadTime = $('#leadTime').val();
		var requestedDeliveryDate = $('#requestedDeliveryDay').val();
		
    	document.leadTime=$('#leadTime').val();
        document.leadTime = document.leadTime +" Days";
        
        if($('#leadTimeId').length){
        	document.getElementById("leadTimeId").innerHTML = document.leadTime;	
        }
        
      datePickerTextBox=$("#datepicker-2");
        
      if(requestedDeliveryDate != null && requestedDeliveryDate > 0)
        {
          leadTimeDate = new Date(Number(requestedDeliveryDate));
          
          datePart = (leadTimeDate.getDate().length < 2)?'0'+leadTimeDate.getDate():leadTimeDate.getDate();
          monthPart = (leadTimeDate.getMonth().length < 2)?'0'+(leadTimeDate.getMonth()+1):(leadTimeDate.getMonth()+1);
          yearPart = leadTimeDate.getFullYear().toString();
          
          if(datePart <= 9){
        	  datePart = '0'+datePart;
          }
          if(monthPart <= 9 ){
        	  monthPart = '0'+monthPart;
          }
          
          var siteUid = $('#siteUid').val();
          if(siteUid.indexOf("EMEA") != -1){
              
        	  datePickerTextBox.val(datePart+"/"+monthPart+"/"+yearPart);
              
              var requestedDeliveryDateInFormat = yearPart+"-"+monthPart+"-"+datePart;
              var requestedDeliveryDate = new Date(requestedDeliveryDateInFormat); 
              
              $('#requestedDeliveryDay').val(Number(requestedDeliveryDate.getTime()));
              
          } else {
        	  datePickerTextBox.val(monthPart+"-"+datePart+"-"+yearPart);
        	  
        	  var requestedDeliveryDateInFormat = yearPart+"-"+monthPart+"-"+datePart;
              var requestedDeliveryDate = new Date(requestedDeliveryDateInFormat); 

              $('#requestedDeliveryDay').val(Number(requestedDeliveryDate.getTime()));
          }
        }
      else
        {
          if(document.leadTime != null && document.leadTime > 0)
            {
              leadTimeDate = new Date(new Date().getTime()+(86400000*document.leadTime));
              datePart = (leadTimeDate.getDate().length < 2)?'0'+leadTimeDate.getDate():leadTimeDate.getDate();
              monthPart = (leadTimeDate.getMonth().length < 2)?'0'+(leadTimeDate.getMonth()+1):(leadTimeDate.getMonth()+1);
              yearPart = leadTimeDate.getFullYear().toString();
              
              var siteUid = $('#siteUid').val();
              
              if(datePart <= 9){
            	  datePart = '0'+datePart;
              }
              if(monthPart <= 9 ){
            	  monthPart = '0'+monthPart;
              }
              
              if(siteUid.indexOf("EMEA") != -1){
                  datePickerTextBox.val(datePart+"/"+monthPart+"/"+yearPart);
              } else {
            	  datePickerTextBox.val(monthPart+"-"+datePart+"-"+yearPart);
              }
            }
        }
      
        var reqDevdate=new Date(requestedDeliveryDate);
        var curr_date = reqDevdate.getDate();
        var curr_month = reqDevdate.getMonth();
        var curr_year = reqDevdate.getFullYear();
        var reqDeliverDate = curr_month+"/"+curr_date+"/"+curr_year;
        document.assignLeadTimeToDatePickerLATAM();
        prepopulatedDate = datePickerTextBox.val();
        //alert()
      //  ACC.checkoutB2B.refresh(data);
        $.colorbox.close();
    	
    }

};

document.assignLeadTimeToDatePickerLATAM = function() {
    if ($("#datepicker-2")) {
          $("#datepicker-2").datepicker("destroy");
    }
    
    var leadTime = document.leadTime;
    var siteUid = $('#siteUid').val();
    var firstDayOfWeek = 0;
    var requestedDeliveryDay = $('#requestedDeliveryDay').val();
	  var deliveryDate = new Date(Number(requestedDeliveryDay));
	  leadTime = deliveryDate;
	  var displayDays = $.datepicker.noWeekends;
	  
    if(siteUid.indexOf("EMEA") != -1){
  	  firstDayOfWeek = 1;
    } else if(siteUid == 'personalCare'){
  	  firstDayOfWeek = 0;
    }  
    // Start date from initial estimated delivery day irrespective of user choosing any date in the calendar 
    leadTime = new Date(Number($('#initialRequestedDeliveryDate').val()));
    document.checkoutdeliveryDatePicker = $("#datepicker-2").datepicker({
          dateFormat : 'mm-dd-yy',
          minDate : leadTime,
          beforeShowDay: displayDays,
          showOn : "button",
          buttonImage : "/_ui/desktop/common/images/calendar.gif",
          buttonImageOnly : true,
          buttonText : "Select Date",
          firstDay: firstDayOfWeek,
          onSelect : function() {
                //document.getElementById("checkoutPlaceOrder").disabled = false;
                //document.getElementById("checkoutPlaceOrder2").disabled = false;
        	  	//document.getElementById("simulatePlaceOrderId1").disabled = false;  
        	  
                var deliveryDate = $('#datepicker-2').val();
                var siteUid = $('#siteUid').val();
                
                /*if(siteUid.indexOf("EMEA") != -1){
              	  var dateArray = deliveryDate.split('-');
                    var dateInDDMMYYYY = dateArray[1] + '/' + dateArray[0] + '/' + dateArray[2];
                    document.getElementById("deliveryDateId").innerHTML = dateInDDMMYYYY;
                } else{
              	  document.getElementById("deliveryDateId").innerHTML = deliveryDate;
                }*/
                
                var deliveryDateArray = deliveryDate.split('-');
                var requestedDeliveryDateInFormat = deliveryDateArray[2]+"-"+deliveryDateArray[0]+"-"+deliveryDateArray[1];
                //var requestedDeliveryDateSelected = new Date(requestedDeliveryDateInFormat);
                var requestedDeliveryDateSelected = new Date(deliveryDateArray[2], Number(deliveryDateArray[0])-1, deliveryDateArray[1]);  
                
                $('#requestedDeliveryDay').val(Number(requestedDeliveryDateSelected.getTime()));

                $.ajax({
                      type : "POST",
                      url : "/checkout/single/summary/setDeliveryDate.json",
                      data : {
                            deliveryDate : deliveryDate
                      },
                      success : function(data) {
                    	  ACC.placeorder.bindAll();
                      },
                      error : function(data){
                      }
                });
          }
    });
};

$(document).ready(function ()
{
	ACC.placeorder.bindAll();
	
	/* Added for Delivery Notes Feature - START */
	
	// Upload Delivery Notes file
	$(document).on("click", "#uploadDeliveryNotesFileButton", function(e){
		//stop submit the form, we will post it manually.
	    e.preventDefault();

	    var selectedLanguage = $("#lang_selector_LangCurrencyComponent").find(":selected").val();
		
	    // Get form
	   	var $form = $('#uploadDeliveryNotesFileForm')[0];     
	    var cartID = $('#cartId').val();
	    var file = $('#file')[0].files[0];
	    //Regex for Valid Characters i.e. Alphabets(A-Z, a-z), Numbers(0-9), Hyphen(-), Underscore(_) and Dot(.). 
        var regex = /^[A-Za-z0-9-_.]+$/; 
          
	    // Call File upload method only if there is a valid file.
	    if (null != file){
	    	$('#deliveryNotesFileEmpty').hide();
        	$('#deliveryNotesFileEmptyText').hide();  
        	
	    	var fileName = file.name;
	    	//console.log('fileName : ' + fileName); 
	    	var extension = fileName.substr( (fileName.lastIndexOf('.') +1) );
	    	//console.log('extension : ' + extension);  
	    	var fileNameWithoutExtension = fileName.replace("." + extension, "");
	    	
	    	//console.log('fileNameWithoutExtension : ' + fileNameWithoutExtension + ", Length : " + fileNameWithoutExtension.length); 
	    	var isValid = regex.test(fileNameWithoutExtension);   
            if (!isValid) {
                //console.log("File name with only Alphabets(A-Z, a-z), Numbers(0-9), Hyphen(-), Underscore(_) and Dot(.) are allowed.");  
                $('#deliveryNotesFileRegexError').show();
            	$('#deliveryNotesFileRegexErrorText').show();
            	$('#deliveryNotesFileLengthError').hide();
            	$('#deliveryNotesFileLengthErrorText').hide(); 
            } else if(fileNameWithoutExtension.length > 30){ 
            	//console.log("File name cannot exceed 30 characters."); 
            	$('#deliveryNotesFileRegexError').hide();
            	$('#deliveryNotesFileRegexErrorText').hide();
            	$('#deliveryNotesFileLengthError').show();
            	$('#deliveryNotesFileLengthErrorText').show();
            } else {
            	$('#deliveryNotesFileRegexError').hide();
             	$('#deliveryNotesFileRegexErrorText').hide();
             	$('#deliveryNotesFileLengthError').hide();
            	$('#deliveryNotesFileLengthErrorText').hide();
            	
            	// Create an FormData object 
    		    var data = new FormData($form);  
    		    
    		    // Add cartId as an additional field
    		    //data.append("cartId", $('#cartId').val());
    		     
    		    // disabled the submit button
    		    $("#uploadDeliveryNotesFileButton").prop("disabled", true);

    		    $.ajax({
    		        type: "POST",
    		        enctype: 'multipart/form-data',
    		        url: "/checkout/single/uploadDeliveryNotesFile",
    		        data: data, 
    		        processData: false, // very important to set it as false, else it will be serialized as query string
    		        contentType: false,
    		        cache: false, 
    		        timeout: 600000,
    		        success: function (data) {
    		            if(data == 'FILE EMPTY'){
    		            	$('#deliveryNotesFileEmpty').show();
    		            	$('#deliveryNotesFileEmptyText').show(); 
    		            } else if(data == 'NEW FILE UPLOAD SUCCESS'){ //NEW FILE UPLOAD SUCCESS 
    		            	//console.log('New file upload successful ...'); // Display the uploaded new file in UI in addition to the existing file list
    		            	
    		            	var newFileDisplayDiv = "<div class='displayDeliveryNote' id='"+ cartID +"_"+ fileName +"'>" +
    							            			"<p class='displayDeliveryNoteInner' id='openFile_"+ cartID +"_"+ fileName +"' title='Download "+ fileName +"'>"+ fileName +"</p>" +
    							            			"<a class='removeDeliveryNoteFile' id='removeFile_"+ cartID +"_"+ fileName +"' style='cursor: pointer;'>" +
    							    						"<img alt='Remove' title='Remove' src='/_ui/desktop/common/images/facet-remove.png'>" +
    							    					"</a>" +
    							    				"</div>";
    		            	
    		            	// Adding the new file div to the existing div  
    		            	$('.deliveryNotesFilesDiv').append(newFileDisplayDiv);  
    		            	
    		            	// Increase box height css	            	
    		            	var existingNoOfDivs = $(".displayDeliveryNote").length;     
    		            	var existingNoOfRows = parseInt(existingNoOfDivs/4);
    		            	  
    		            	if((existingNoOfDivs % 4 == 1) && (existingNoOfRows >= 3)){    
    		            		var existingHeight = $(".orderBoxes").height();  
    		            		var additonalHeight = 32;
    		            		var newHeight = existingHeight + additonalHeight; 
    		            		
    		            		$(".orderBox").css('height', (newHeight) + 'px');  
    		                    $(".orderBoxes").css('height', (newHeight) + 'px');   
    		            	}
    		            	  
    		            	// Handle the 'No Files to display' error message	
    		            	if(existingNoOfDivs > 0){
    		            		$('#noFilesToDisplayDiv').css('display', 'none');
    		            		$('.noOfFilesDiv').text(" (" + existingNoOfDivs + ")");   
    		            		$('.noOfFilesDiv').show();  
    		            	} else {
    		            		$('#noFilesToDisplayDiv').css('display', 'block');
    		            		$('.noOfFilesDiv').hide();
    		            	}
    		            	
    		            	// Display success message
    		            	$('#deliveryNotesFileUploadSuccess').show();
    		            	$('#deliveryNotesFileUploadSuccessText').show();
    		            	$('#deliveryNotesFileUploadSuccess').delay(3000).hide(100);  
    		            	
    		            	$('#deliveryNotesFileEmpty').hide();
    		            	$('#deliveryNotesFileUploadFailure').hide(); 
    		            	$('#uploadDeliveryNotesFileForm').trigger("reset");
    		            	 
    		            } else if(data == 'EXISTING FILE UPDATE SUCCESS'){
    		            	//console.log('Existing file update successful ...'); // DO NOT add the file to the existing file list
    		            	// Display success message
    		            	$('#deliveryNotesFileUploadSuccess').show();
    		            	$('#deliveryNotesFileUploadSuccessText').show(); 
    		            	$('#deliveryNotesFileUploadSuccess').delay(3000).hide(100);   
    		            	
    		            	$('#deliveryNotesFileEmpty').hide();  
    		            	$('#deliveryNotesFileUploadFailure').hide(); 
    		            	$('#uploadDeliveryNotesFileForm').trigger("reset");
    		            }
    		            $("#uploadDeliveryNotesFileButton").prop("disabled", false);
    		        },
    		        error: function (e) {
    		            if(data == 'UPLOAD FAILURE'){
    		            	//console.log('File upload failed ...'); // Don't display the file in UI, since the upload failed. 
    		            	// Display error message 
    		            	$('#deliveryNotesFileUploadFailure').show();
    		            	$('#deliveryNotesFileUploadFailureText').show(); 
    		            	$('#uploadDeliveryNotesFileForm').trigger("reset");
    		            }
    		            $("#uploadDeliveryNotesFileButton").prop("disabled", false);
    		        }
    		    });
    		    $('#deliveryNotesFileEmpty').hide();
            }
		    
	    } else {
	    	$('#deliveryNotesFileEmpty').show();
        	$('#deliveryNotesFileEmptyText').show(); 
        	
        	$('#deliveryNotesFileRegexError').hide();
         	$('#deliveryNotesFileRegexErrorText').hide();
         	$('#deliveryNotesFileLengthError').hide();
        	$('#deliveryNotesFileLengthErrorText').hide(); 
	    }
	    
	    $('#deliveryNotesFileUploadSuccess').hide(); 
		$('#deliveryNotesFileUploadFailure').hide();
		$('#deliveryNotesFileRemovalFailure').hide(); 
		$('#uploadDeliveryNotesFileForm').trigger("reset"); 
		ACC.placeorder.showAndHideLoader("none"); 
	});

	// Download Delivery Notes file
	/*$(document).on("click", ".displayDeliveryNoteInner", function(e){
		var thisID = $(this).attr("id");
		var cartID = thisID.split("_")[1];
		var thisIDTemp = thisID;
		var fileName = thisIDTemp.replace("openFile_" + cartID + "_" , ""); 
		var removeButtonId = thisID.replace("openFile_", "removeFile_"); 
		console.log('removeButtonId : ' + removeButtonId);
		$('#removeButtonId').hide(); 
		window.open('/checkout/single/downloadDeliveryNoteFile?cartID=' + cartID + '&fileName=' + fileName + '&inline=false', fileName);   
		//location.reload(true);
		window.location.reload(true) 
		$('#removeButtonId').hide();  
		console.log('removeButtonId : ' + removeButtonId);  
		setTimeout(function() { 
			//ACC.placeorder.showAndHideLoader("block"); 
		}, 10000); 
		//setTimeout('startDownload()', 5000); 
		window.close();   
		//$('#removeButtonId').show();    
	});*/
	 
	// Remove Delivery Notes file
	$(document).on("click", ".removeDeliveryNoteFile", function(e){
		e.preventDefault();
		
		var removeConfirmed = confirm("Do you really want to remove this file from Hybris ?");
		//console.log('removeConfirmed : ' + removeConfirmed);
		
		if(removeConfirmed){
			setTimeout(function(){
				//do something special
			}, 5000); 		
			
			ACC.placeorder.showAndHideLoader("block");
			var selectedLanguage = $("#lang_selector_LangCurrencyComponent").find(":selected").val();
			
			$('#deliveryNotesFileEmpty').hide();
	    	$('#deliveryNotesFileEmptyText').hide();  
	    	
			var thisID = $(this).attr("id");
			
			var cartID = thisID.split("_")[1];
			var thisIDTemp = thisID;
			var fileName = thisIDTemp.replace("removeFile_" + cartID + "_" , "");  
			var parentDiv = $(this).parent('.displayDeliveryNote');

			$.ajax({ 
		        url: '/checkout/single/removeDeliveryNoteFile',
		        data : {'cartID' : cartID, 'fileName' : fileName},
		        type: 'POST',    
		        cache: false, 
		        success: function(data) {
		        	if(data == "SUCCESS"){	
		        		$(parentDiv).remove(); // Remove the file div from the UI    
		        		 
		        		// Handle the 'No Files to display' error message  	
		        		var noOfFileDivs = $('.displayDeliveryNote').length;
		            	if(noOfFileDivs == 0){  
		            		$('.noOfFilesDiv').hide(); 
		            		$('#noFilesToDisplayDiv').css('display', 'block');	            			
		            		$(".orderBox").css('height', $('#defaultHeight').val() + 'px');   
		                    $(".orderBoxes").css('height', $('#defaultHeight').val() + + 'px');  
		            	} else {
		            		$('.noOfFilesDiv').text(" (" + noOfFileDivs + ")");    
		            		$('.noOfFilesDiv').show();   
		            		$('#noFilesToDisplayDiv').hide();  
		            		$('#noFilesToDisplayDiv').css('display', 'none');
		            	}  
		            	$('#deliveryNotesFileRemovalFailureText_en').hide(); 
		            	$('#deliveryNotesFileRemovalFailureText_es').hide();
		        		$('#deliveryNotesFileRemovalFailure').hide(); 
		        	}
		        	if(data == "ERROR"){
		        		if(selectedLanguage == 'en'){
		        			$('#deliveryNotesFileRemovalFailureText_en').text("Error removing the file '" + fileName + "'. Please refresh the browser and try removing again. If the situation continues, please send an email to B2B_IT_Support@Edgewell.com for support.");
		        			$('#deliveryNotesFileRemovalFailureText_en').show();
		        			$('#deliveryNotesFileRemovalFailureText_es').hide(); 
		        		} else if(selectedLanguage == 'es'){
		        			$('#deliveryNotesFileRemovalFailureText_es').text("Error al eliminar el archivo '" + fileName + "'. Actualice el navegador e intente eliminarlo nuevamente. Si la situación continúa, envíe un correo electrónico a B2B_IT_Support@Edgewell.com para obtener asistencia.");
		        			$('#deliveryNotesFileRemovalFailureText_es').show();
		        			$('#deliveryNotesFileRemovalFailureText_en').hide();
		        		} 
		        		$('#deliveryNotesFileRemovalFailure').show();
		        	}
		        }
			});
			ACC.placeorder.showAndHideLoader("none"); 
		}
	});
	
	/* Added for Delivery Notes Feature - END */ 
});


