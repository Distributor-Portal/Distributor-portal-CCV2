$(document).ready(function() {

$( "#j_username" ).change(function() {
	 var user = $("#j_username").val();
	 $('#j_password').removeAttr('value');
	 var siteUid = $('#siteUid').val();
	 // Getting distributor list ONLY for Sales Rep Login for LATAM region, NOT for EMEA
	 //if(siteUid == 'personalCare'){
		if(user != 'null' && user != undefined && user != ''){
	 	console.log('Check if the user is valid ...');
	 	 $.ajax({
			 url: '/login/isValidLoginUser',
			 type: 'POST',
		     data:{'user' : user.trim()},
		     success : function(response){
			     if(response == 'VALID_USER'){   
				 console.log('Valid user');
				 $('#j_password').prop("disabled", false);
				 $('#login_bt').prop("disabled", false);
				 $('#showErrorMessageForInvalidUser').css('display', 'none');
		         $('#showErrorMessageForInvalidUser').hide(); 
		         $('#globalMessages').css('display', 'none');  
		         $('#globalMessages').hide(); // Hide the password validation message, if any.
			     if(siteUid == 'personalCare'){
				 console.log('Fetching B2B Unit List');         		
				 $.ajax({
					 url: '/login/getb2bunitlist',
					 type: 'POST',
				     data:{'user' : user.trim()},
				     success : function(distributorsList){   
			           var distributorsListSize= Object.keys(distributorsList).length;
			           //console.log('map size ::: ' + distributorsListSize);
			            $('#b2bUnitList').find('option').remove().end().append('<option disabled selected value>   </option>');
			        	$('#b2bUnitList').find('option').remove().end();
			        	//$("#location option:selected").attr("<custom attribute>")
			        	var sessionLanguage = $('#lang_selector_LangCurrencyComponent option:selected').val();  
			        	//console.log('sessionLanguage ::: ' + sessionLanguage);
			        	var selectPlaceholderText = null;
			        	if(sessionLanguage == 'es'){ 
			        		selectPlaceholderText = 'Selecciona tu cliente';
			        	} else {
			        		selectPlaceholderText = 'Select your customer';
			        	}
			        	
			        	if(distributorsListSize > 0)
			        	{
			        		$(".changedB2BunitDropdown").hide()
			        		$(".dropdown").show()
			        		$("#b2bUnitList").append("<option disabled='disabled' selected >----- " + selectPlaceholderText + " -----</option>");
			        		$.each( distributorsList, function( key, val ) {
			          		  //console.log( key + ": " + val ); 
			          		  $('#b2bUnitList').append($('<option >', { 
			                        //value: key,
			                        //text : value
			          			  	value: val,
			                        text : key  
			                    }));
			          		});
			        	}
			        	else
			    		{
			    			$(".dropdown").hide()
			    			$(".changedB2BunitDropdown").hide()
			    			console.log('distributor map is empty ... ');
			    		}
			        } ,
			        error: function()
			        {
			       	   console.log('Error while fetching distributor map ...');
			        }  
				 });
				 }
		         } else {
		         	console.log('Invalid username entered ..');
		         	$('#showErrorMessageForInvalidUser').css('display', 'inline-block');
		         	$('#showErrorMessageForInvalidUser').show();
					$('#j_password').prop("disabled", true);
					$('#login_bt').prop("disabled", true);
					$('#globalMessages').css('display', 'none');  
					$('#globalMessages').hide(); // Hide the password validation message, if any.
					
					$(".dropdown").hide()
			    	$(".changedB2BunitDropdown").hide(); // Hide the customer dropdown list if the username is invalid - for WESELL
		         }
	        } ,
	        error: function()
	        {
	       	   console.log('failure');
	        }  
		 });
		}
	});

$('#b2bUnitList').on('change', function(){
	var selectedValue = $('#b2bUnitList').val();
	 var user = $("#j_username").val();
	console.log('salesRepName selected :: '+ user)
	console.log('distributor selected :: ' + selectedValue);
	$.ajax({
		 url: '/login/setb2bunitinsession',
		 type: 'POST',
	     data:{ 'b2bUnit' : selectedValue,
	    	 	'salesRepUser': user
	     	},
	     success : function(response){   
          console.log('response ::: ' + response);
       } ,
       error: function()
       {
      	   console.log('failure');
       }  
	 });
});

$('#changeB2BUnit').on('change', function(){
	var selectedValue = $('#changeB2BUnit').val();
	 var user = $("#j_username").val();
	console.log('salesRepName selected :: '+ user)
	console.log('distributor selected :: ' + selectedValue);
	$.ajax({
		 url: '/login/setb2bunitinsession',
		 type: 'POST',
	     data:{ 'b2bUnit' : selectedValue,
	    	 	'salesRepUser': user
	     	},
	     success : function(response){   
          console.log('response ::: ' + response);
       } ,
       error: function()
       {
      	   console.log('failure');
       }  
	 });
});

/*$('#b2bUnit').on('change', function(){
	var selectedValue = $('#b2bUnit').val();
	console.log('distributor selected :: ' + selectedValue);
	$.ajax({
		 url: '/cart/setb2bunit',
		 type: 'POST',
	     data:{ 'b2bUnit' : selectedValue},
	     success : function(response){   
          console.log('response ::: ' + response);
       } ,
       error: function()
       {
      	   console.log('failure');
       }  
	 });
});*/

var isSelectedDistributor = $('#isSelectedDistributor').val();
if(isSelectedDistributor == 'true')
{
	$("#changeB2BUnit").css("border-color", "#c90400");
}

});