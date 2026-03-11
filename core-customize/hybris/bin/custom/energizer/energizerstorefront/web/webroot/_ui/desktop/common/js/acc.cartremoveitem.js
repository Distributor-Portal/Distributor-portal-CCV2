var isContainerfull;
var spinnerLoader = '<div class="spinnerDiv"><img class="spinnerImg" src="/_ui/desktop/common/images/spinner.gif"/></div>';
ACC.cartremoveitem = {
            isOrderBlocked: false,
            showAndHideLoader: function(flag){    
                  //use flag value 'block' to show loader
                  //use flag value 'none' to hide loader
                  
                  $(".spinnerDiv").css("display",flag);
            },
            highlightQtyInputBox: function ()
            {     
                  $(".prdCode").each(function(){
                        if(ACC.cartremoveitem.checkProductID($(this).text().trim())){
                              $($(this).parent().find(".quantity .qty")).css("border","1px solid #ff0000");
                        }
                  });
            },
            checkProductID:function(prodId){
                  var isMatching = false;
                  $(".prod_det").each(function(){
                  //$(".productsNotAdded tbody td:first").each(function(){
                        if($(this).text().trim() == prodId ){
                              isMatching = true;
                              return false;
                        }
                  });
                  return isMatching;
            },
            bindAll: function (e)
            {     
                  this.bindCartRemoveProduct(e);
            },
            bindCartData : function()
            {
                  ACC.cartremoveitem.getCartData();
            },
            
            bindCartRemoveProduct: function (e)
            {
                  
                  $('.submitRemoveProduct').on("click", function ()
                  {   
                        ACC.cartremoveitem.showAndHideLoader("block");
                        var entryNum = $(this).attr('id').split("_")[1];
                        var $form = $('#updateCartForm' + entryNum);
                        var initialCartQuantity = $form.find('input[name=initialQuantity]');
                        var cartQuantity = $form.find('input[name=quantity]');
                        var productCode = $form.find('input[name=productCode]').val(); 

                        cartQuantity.val(0);
                        initialCartQuantity.val(0);

                        ACC.track.trackRemoveFromCart(productCode, initialCartQuantity, cartQuantity.val());

                        var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
                        $.ajax({
                              url: $form.attr("action"),
                              data: $form.serialize(),
                              type: method,
                              success: function(data) 
                              {
                                  ACC.cartremoveitem.refreshCartData(data, entryNum, productCode, 0);
                                  location.reload(true);  
                              },
                              error: function(xht, textStatus, ex) 
                              {
                                    alert("Failed to remove quantity. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                              }

                        });

                              });
                  //this function is doing the same as onBlur()         
            /*$('.qty').on("change", function ()
                  {     
                        alert(" Testing ");
                        var entryNum = $(this).attr('id').split("_")[1];                        
                        var $form = $('#updateCartForm' + entryNum);                      
                        var cartQuantity = $form.find('input[name=quantity]').val();                        
                        var productCode = $form.find('input[name=productCode]').val();                                                            
                                                            
                                                            
                        var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
                       $.ajax({
                              url: $form.attr("action"),
                              data: $form.serialize(),
                              type: method,
                              success: function(data) 
                              {     
                                    ACC.cartremoveitem.getErrors(data, entryNum, productCode, cartQuantity);
                                    
                              },
                              error: function() 
                              {
                                    alert("Failed to remove quantity. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                              }

                        });
                  });*/

                  $('.updateQuantityProduct').on("click", function (event)
                              { 
                        event.preventDefault();

                        var prodid  = $(this).attr('id').split("_"); 
                        var form    = $('#updateCartForm' + prodid[1]);
                        var productCode = form.find('input[name=productCode]').val(); 
                        var grid = $('#grid_' + prodid[1]);
                        grid.addClass("cboxGrid");

                        var strSubEntries = grid.data("sub-entries");
                        var arrSubEntries= strSubEntries.split(',');          
                        var firstVariantCode = arrSubEntries[0].split(':')[0];

                        var mapCodeQuantity = new Object();
                        
                        for (var i = 0; i < arrSubEntries.length; i++)
                        {
                              var arrValue = arrSubEntries[i].split(":");
                              mapCodeQuantity[arrValue[0]] = arrValue[1];
                        }

                        var method = "GET";
                        $.ajax({
                              url: ACC.config.contextPath + '/cart/getProductVariantMatrix',
                              data: {productCode: firstVariantCode},
                              type: method,
                              success: function(data) 
                              {
                                    grid.html(data);

                                    var $gridContainer = grid.find(".product-grid-container");      
                                    var numGrids = $gridContainer.length;

                                    for (var i = 0; i < numGrids; i++)
                                    {
                                          ACC.cartremoveitem.getProductQuantity($gridContainer.eq(i), mapCodeQuantity);
                                    }

                                    $.colorbox({
                                          html:      grid.clone(true).show(),
                                          scroll:    true,
                                          //width:     "80%",
                                          //height:    "80%",
                                          onCleanup: function() { 
                                                // remove the cloned grid
                                                grid.empty(); 

                                                strSubEntries = '';
                                                $.each(mapCodeQuantity, function(key, value) {
                                                      if (value != undefined)
                                                      {
                                                            strSubEntries = strSubEntries + key + ":"+ value+",";
                                                      }
                                                });

                                                grid.data('sub-entries', strSubEntries);
                                          }
                                    });         

                                    ACC.cartremoveitem.coreTableActions(prodid[1], mapCodeQuantity);
                              },
                              error: function(xht, textStatus, ex) 
                              {
                                    alert("Failed to get variant matrix. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                              }

                        });         

                        //grid.show();          
                              });

                  $('.qty').on("keypress", function (e)
                  { 
           
		            var $input = $(this);
		            if ((e.keyCode || e.which) == 13) // Enter was hit
		            {     
		                  e.preventDefault();
		                  $input.blur();
		            }
            
                  });                  
                  $('.qty').on("blur", function ()
                    {   
                        var parentClass = $(this).parent().parent().attr("class");
                       /* if(parentClass == "quantity"){
                              ACC.cartremoveitem.showAndHideLoader("block"); 
                        }*/
                        var entryNum = $(this).parent().find('input[name=entryNumber]').val();                             
                        var $form = $('#updateCartForm' + entryNum);  
                        
                        //console.log('isSalesRepLoggedIn ::: ' + $('#isSalesRepLoggedIn').val());
                        
                        var initialCartQuantity = $form.find('input[name=initialQuantity]').val();
                        var newCartQuantity = $form.find('input[name=quantity]').val(); 
                        var productCode = $form.find('input[name=productCode]').val();       
                        var moq =    $form.find('input[name=moq]').val();           
                        if(initialCartQuantity != newCartQuantity)
                        {     
                              if(parentClass == "quantity"){
                                ACC.cartremoveitem.showAndHideLoader("block"); 
							  }
                              ACC.track.trackUpdateCart(productCode, initialCartQuantity, newCartQuantity);
                              var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
                             
                              $.ajax({
                                    url: $form.attr("action"),
                                    data: $form.serialize(),
                                    type: method,
                                    success: function(data) 
                                    {   
                                    	if (!$.trim(data)){ 
                                    		location.reload();
                                    	}
                                    	else{   
                                    	    ACC.cartremoveitem.refreshCartData(data, entryNum, productCode, newCartQuantity);
                                            if(newCartQuantity % moq == 0)
                                            {      
                                            initialCartQuantity = newCartQuantity;
                                            $form.find('input[name=initialQuantity]').val(initialCartQuantity);
                                            }    
                                    	}
                                       
                                    },
                                    error: function(xht, textStatus, ex) 
                                    {
                                          
                                          alert("Failed to update quantity. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                                    }

                              });
                        }
                     
                        if($('#isSalesRepLoggedIn').val() == "true"){
                        	$('#checkoutButton_top').prop('disabled', true);
                        	$('#checkoutButton_bottom').prop('disabled', true);
                        	$('#itemPriceDiv_'+entryNum).hide();
                        	$('#totalPriceDiv_'+entryNum).hide();
                        	$('#gotPriceFromSAP').val('false');
                        } 
                     });

 
            	 
            $('.unitPrice').on("blur", function ()
                    {       
            			var cartEntriesSize = $('#cartEntriesSize').val();
                        var parentClass = $(this).parent().parent().attr("class");
                        //console.log(parentClass);
                        var entryNum = $(this).parent().find('input[name=entryNumber]').val();                             
                        var $form = $('#updateCartExpectedPriceForm' + entryNum);                   
                        var initialExpectedUnitPrice = $form.find('input[name=initialExpectedUnitPrice]').val();
                        var productCode = $form.find('input[name=productCode]').val();       
                        var newExpectedUnitPrice = $form.find('input[name=expectedUnitPrice]').val();
                       // newExpectedUnitPrice=newExpectedUnitPrice.toFixed(2);
                        var itemEachPrice = $('#eachUnitPrice_'+entryNum).val();
                    	 var validationCheck;
                         //var rx= /^[1-9]\d{0,9}(\.\d{2})?%?$/;
                         //var rx= /^\.*[0-9]\d{0,9}(\.\d{2})?%?$/;
                         //var rx= /[0-9]+\.[0-9]\d{0,2}(\.\d{2})?%?$/;
                         
                         /* The regex below allows 
                         ^            # Start of string
                         \s*          # Optional whitespace
                         (?=.*[1-9])  # Assert that at least one digit > 0 is present in the string
                         \d*          # integer part (optional)
                         (?:          # decimal part:
                          \.          # dot
                          \d{1,2}     # plus one or two decimal digits
                         )?           # (optional)
                         \s*          # Optional whitespace
                         $            # End of string */
                         
                         var rx = /^\s*(?=.*[1-9])\d*(?:\.\d{1,2})?\s*$/;
                         
                         if(rx.test(newExpectedUnitPrice))
                     	 {
                     		validationCheck= true; 
                         }
                         else if(newExpectedUnitPrice == null || newExpectedUnitPrice == undefined || newExpectedUnitPrice == '')
                         { 
                         	validationCheck= true;
                     	 } else {
                     		validationCheck= false;
                     	 }
                         
                         if(!validationCheck)
                     	 {
                     		$('#showErrorMessageForEdgewellPriceValidation').show();
                     		// Modify border css added for validation
        	            	$('#expectedPrice_'+entryNum).css('border', '1px solid red');
        	            	$('#agreeEdgewellUnitPrice_'+entryNum).css('outline-color', 'red');
        	            	$('#agreeEdgewellUnitPrice_'+entryNum).css('outline-style', 'solid');
        	            	$('#agreeEdgewellUnitPrice_'+entryNum).css('border-radius', 'none');
                    		
                     	 } else {
                     		$('#showErrorMessageForEdgewellPriceValidation').hide();
                     	 }
                       
                        
                        //console.log(initialExpectedUnitPrice + " , " + newExpectedUnitPrice);
                        //console.log($('#expectedPrice_'+entryNum).css('border-color'));   
                        if(/*initialExpectedUnitPrice != newExpectedUnitPrice && */validationCheck)
                        	{
                        	$('#showErrorMessageForEdgewellPriceValidation').hide();

                        	  if(parentClass == "expectedPrice"){
                                ACC.cartremoveitem.showAndHideLoader("block"); 
							  }
                              var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
                             
                              $.ajax({
                                    url: $form.attr("action"),
                                    data: $form.serialize(),
                                    type: method,
                                    success: function(data) 
                                    {   
                                    	initialExpectedUnitPrice=newExpectedUnitPrice;
                                    	ACC.cartremoveitem.showAndHideLoader("none"); 
                                    	// Remove highlighted error css
                                    	if(null != newExpectedUnitPrice && newExpectedUnitPrice != undefined && newExpectedUnitPrice != ''){
                                    		// Modify border css added for validation
                        	            	$('#expectedPrice_'+entryNum).css('border', '1px solid #777');
                        	            	$('#agreeEdgewellUnitPrice_'+entryNum).css('outline-color', 'none');
                        	            	$('#agreeEdgewellUnitPrice_'+entryNum).css('outline-style', 'none');
                        	            	$('#agreeEdgewellUnitPrice_'+entryNum).css('border-radius', 'none');
                                    	}
                                    	// If customer expected price == each unit price, then agree edgewell price is also checked
                                    	if(itemEachPrice == newExpectedUnitPrice){
                                    		$('#agreeEdgewellUnitPrice_'+entryNum).prop('checked', true);
                                    		$('#expectedPrice_'+entryNum).prop('disabled', true);
                                    	}
                                    	
                                    	// Check if the customer agrees to edgewell's price for all the products and if it is true, then set the header level 'agree' flag to 'true'
                                    	var agreeAllCount = 0;
                        	            for(var i = 0; i < cartEntriesSize; i++){
                        	            	if($('#agreeEdgewellUnitPrice_'+i).attr('checked')){
                        	            		//console.log('checkbox for product ' + $('#productCode_'+i).val() + " is checked !");
                        	            		agreeAllCount = agreeAllCount + 1;
                        	            	} else {
                        	            		//console.log('checkbox for product ' + $('#productCode_'+i).val() + " is unchecked !");
                        	            	}
                        	            }
                        	            //console.log('agreeAllCount ::: ' + agreeAllCount);
                        	            if(agreeAllCount == cartEntriesSize){
                        	            	$('#agreeEdgewellPriceAll').prop('checked', true);
                        	            	$('#agreeEdgewellPriceForAllProducts_'+entryNum).val('true');
                        	            	// Hide validation message
                         	            	$('#showErrorMessageForAgreeEdgewellPrice').hide();
                        	            } else {
                        	            	$('#agreeEdgewellPriceAll').prop('checked', false);
                        	            	$('#agreeEdgewellPriceForAllProducts_'+entryNum).val('false');
                        	            }
                        	            
                                    	
                                    },
                                    error: function(xht, textStatus, ex) 
                                    {
                                          alert("Failed to update customer expected price. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                                    }

                              });
                            	} else if(initialExpectedUnitPrice == newExpectedUnitPrice){
                            		// Modify border css added for validation
                	            	/*$('#expectedPrice_'+entryNum).css('border', '1px solid #777');
                	            	$('#agreeEdgewellUnitPrice_'+entryNum).css('outline-color', 'none');
                	            	$('#agreeEdgewellUnitPrice_'+entryNum).css('outline-style', 'none');
                	            	$('#agreeEdgewellUnitPrice_'+entryNum).css('border-radius', 'none');
                	            	
                	            	$('#agreeEdgewellUnitPrice_'+entryNum).prop('checked', true);
                            		$('#expectedPrice_'+entryNum).prop('disabled', true);*/
                            	}	            
                              });
            
            $('.agreeEdgewellUnitPrice').on("change", function (e)
	        {       
            	e.stopImmediatePropagation();
            	var cartEntriesSize = $('#cartEntriesSize').val();
            	//console.log("cartEntriesSize ::: " + cartEntriesSize);
	            var parentClass = $(this).parent().parent().attr("class");
	           
	            //console.log('parentClass ::: ' + parentClass);
	            
	            var entryNum = $(this).parent().find('input[name=entryNumber]').val();                             
	            var $form = $('#updateCartAgreeEdgewellUnitPriceForm' + entryNum);                   
	            var initialAgreeEdgewellUnitPrice = $form.find('input[name=initialAgreeEdgewellUnitPrice]').val();
	            var productCode = $form.find('input[name=productCode]').val();       
	            //var newAgreeEdgewellPrice = $form.find('input[name=agreeEdgewellPrice]').val();
	            var newAgreeEdgewellUnitPrice = false;
	            var currencySymbol = $('.currencySymbol').val();
	            
            	
	            if(this.checked){
	            	// Copy the unit price to customer expected price input box if customer agrees to the edgewell price
	            	//console.log('checked');
	            	var itemEachPrice = $('#eachUnitPrice_'+entryNum).val();
	            	//console.log('itemEachPrice ::: ' + itemEachPrice);
	            	
	            	newAgreeEdgewellUnitPrice = true;
	            	$('#expectedPrice_'+entryNum).val(itemEachPrice);
	            	$('#expectedUnitPrice_'+entryNum).val(itemEachPrice);
	            	//$('#initialExpectedUnitPrice_'+entryNum).val(copyItemPrice);
	            	$('#expectedPrice_'+entryNum).prop('disabled', true);
	            	//$('#expectedPrice_'+entryNum).prop('readonly', 'readonly');
	            	
	            	// Modify border css added for validation
	            	$('#expectedPrice_'+entryNum).css('border', '1px solid #777');
	            	$(this).css('outline-color', 'none');
	            	$(this).css('outline-style', 'none');
	            	$(this).css('border-radius', 'none');
	            	$('#showErrorMessageForEdgewellPriceValidation').hide();
	            } else {
	            	// Reset to initial expected price value to the customer expected price input box if customer does not agrees to the edgewell price
	            	//console.log('unchecked');
	            	//var initialExpectedUnitPrice = $('#initialExpectedUnitPrice_'+entryNum).val();
	            	var initialExpectedUnitPrice = "";
	            	//var copyInitialExpectedUnitPrice = initialExpectedUnitPrice_.split(currencySymbol)[1];
	            	//console.log(currencySymbol + " , " + initialExpectedUnitPrice);
	            	
	            	newAgreeEdgewellUnitPrice = false;
	            	$('#expectedPrice_'+entryNum).val(initialExpectedUnitPrice);
	            	$('#expectedUnitPrice_'+entryNum).val(initialExpectedUnitPrice);
	            	$('#expectedPrice_'+entryNum).prop('disabled', false);
	            	//$('#expectedPrice_'+entryNum).removeAttr('readonly');
	            	$('#agreeEdgewellPriceForAllProducts_'+entryNum).val('false');
	            	//console.log('Agree all value ::: ' + $('#agreeEdgewellPriceForAllProducts_'+entryNum).val());
	            }
	            
	            var agreeAllCount = 0;
	            for(var i = 0; i < cartEntriesSize; i++){
	            	if($('#agreeEdgewellUnitPrice_'+i).attr('checked')){
	            		//console.log('checkbox for product ' + $('#productCode_'+i).val() + " is checked !");
	            		agreeAllCount = agreeAllCount + 1;
	            	} else {
	            		//console.log('checkbox for product ' + $('#productCode_'+i).val() + " is unchecked !");
	            	}
	            }
	            //console.log('agreeAllCount ::: ' + agreeAllCount);
	            if(agreeAllCount == cartEntriesSize){
	            	$('#agreeEdgewellPriceAll').prop('checked', true);
	            	$('#agreeEdgewellPriceForAllProducts_'+entryNum).val('true');
	            	// Hide validation message
 	            	$('#showErrorMessageForAgreeEdgewellPrice').hide();
	            } else {
	            	$('#agreeEdgewellPriceAll').prop('checked', false);
	            	$('#agreeEdgewellPriceForAllProducts_'+entryNum).val('false');
	            }
	            
	            //console.log("ExpectedUnitPrice in hidden variable ::: " + $('#expectedUnitPrice_'+entryNum).val());
	            //console.log(entryNum + " , " + initialAgreeEdgewellUnitPrice + " , " + productCode + " , " + newAgreeEdgewellUnitPrice);
	            //console.log('agreeAll value ::: ' + $('#agreeEdgewellPriceForAllProducts_'+entryNum).val());
	            
	            if(initialAgreeEdgewellUnitPrice != newAgreeEdgewellUnitPrice)
	        	{
	              if(parentClass == "agreeEdgewellPrice"){
	                ACC.cartremoveitem.showAndHideLoader("block"); 
				  }
	              var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
	             
	              $.ajax({
	                    url: $form.attr("action"),
	                    data: $form.serialize(),
	                    type: method,
	                    success: function(data) 
	                    {   
	                    	console.log('success');
	                    	initialAgreeEdgewellUnitPrice=newAgreeEdgewellUnitPrice;
	                    	ACC.cartremoveitem.showAndHideLoader("none");                                            
	                    },
	                    error: function(xht, textStatus, ex) 
	                    {
	                          console.log("Failed to update Agree Edgewell Price. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
	                    }
	
	              });
	            }	
	        });
            
            $('#agreeEdgewellPriceAll').on("change", function (e){
            	
            	var cartEntriesSize = $('#cartEntriesSize').val();
            	var currencySymbol = $('.currencySymbol').val();
            	var agreeAllCount = 0;
            	var initialAgreeEdgewellPriceForAllProducts = $('#initialAgreeEdgewellUnitPriceForAllProducts').val();
            	var newAgreeEdgewellPriceForAllProducts = this.checked;
            	var parentClass = $(this).parent().attr("class");
	            //console.log('parentClass ::: ' + parentClass);
	            
            	if(this.checked){
            		//console.log('Select all the checkboxes');
            		$('.agreeEdgewellUnitPrice').prop('checked', true);
            		$('.unitPrice').prop('disabled', true);
            		$('#agreeEdgewellUnitEachPriceForAllProducts').val('true');
            		
            		// Copy the unit price to customer expected price input box if customer agrees to the edgewell price
                	for(var i = 0; i < cartEntriesSize; i++){
                		
                		var itemEachPrice = $('#eachUnitPrice_'+i).val();
                    	
                    	//console.log(currencySymbol + " , " + itemEachPrice);
                    	
                		$('#expectedPrice_'+i).val(itemEachPrice);
    	            	$('#expectedUnitPrice_'+i).val(itemEachPrice);
    	            	//$('#expectedPrice_'+i).prop('disabled', true);
    	            }
                	// Hide validation message
 	            	$('#showErrorMessageForAgreeEdgewellPrice').hide();
 	            	
	            	// Modify border css added for validation
	            	$('.unitPrice').css('border', '1px solid #777');
	            	$('.agreeEdgewellUnitPrice').css('outline-color', 'none');
	            	$('.agreeEdgewellUnitPrice').css('outline-style', 'none');
	            	$('.agreeEdgewellUnitPrice').css('border-radius', 'none');

	            	
                	// Save it to db only if checked
            	} else {
            		//console.log('unselect all the checkboxes');
            		$('.agreeEdgewellUnitPrice').prop('checked', false);
            		$('.unitPrice').prop('disabled', false);
            		$('#agreeEdgewellUnitEachPriceForAllProducts').val('false');
            		
            		// Copy the initial expected price value to the customer expected price input box if customer does not agrees to the edgewell price
            		for(var i = 0; i < cartEntriesSize; i++){
                		
                		//var initialExpectedUnitPrice = $('#initialExpectedUnitPrice_'+i).val();
            			var initialExpectedUnitPrice = "";
                		if(initialExpectedUnitPrice != "" && initialExpectedUnitPrice != undefined){
                			//console.log(currencySymbol + " , " + initialExpectedUnitPrice);
                		} else {
                			//console.log("Initial Customer Expected Unit Price is empty for this product " + $('#productCode_'+i).val() + "!")
                		}
                    	
                		$('#expectedPrice_'+i).val(initialExpectedUnitPrice);
    	            	$('#expectedUnitPrice_'+i).val(initialExpectedUnitPrice);
    	            	//$('#expectedPrice_'+i).prop('disabled', true);
    	            }
            	}
            	
            	var $form = $('#updateCartAgreeEdgewellUnitPriceForAllProductsForm');
            	// Save it to db
            	if(initialAgreeEdgewellPriceForAllProducts != newAgreeEdgewellPriceForAllProducts)
 	        	{
 	              if(parentClass == "agreeEdgewellPriceAll"){
 	                ACC.cartremoveitem.showAndHideLoader("block"); 
 				  }
 	              var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
 	             
 	              //console.log('Agree all value ::: ' + $('#agreeEdgewellUnitEachPriceForAllProducts').val());
 	              
 	              $.ajax({
 	                    url: $form.attr("action"),
 	                    data: $form.serialize(),
 	                    type: method,
 	                    success: function(data) 
 	                    {   
 	                    	console.log('success');
 	                    	initialAgreeEdgewellPriceForAllProducts = newAgreeEdgewellPriceForAllProducts;
 	                    	ACC.cartremoveitem.showAndHideLoader("none");                                            
 	                    },
 	                    error: function(xht, textStatus, ex) 
 	                    {
 	                          console.log("Failed to update Agree Edgewell Price. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
 	                    }
 	
 	              });
 	            }
            	
            });
            
            // Cart Page On click checkout button - START
            
            /*$('#checkoutButton_top, #checkoutButton_bottom').on("click", function (event){
            	
            	event.stopImmediatePropagation();
            	
            	var cartEntriesSize = $('#cartEntriesSize').val();
            	//console.log("cart entries size ::: " + cartEntriesSize);
	            var errorCounter = 0;
	            
            	for(var i = 0; i < cartEntriesSize; i++){
            		
            		var expectedUnitPrice = $('#expectedPrice_'+i).val();
                	var agreeEdgewellUnitPrice = $('#agreeEdgewellUnitPrice_'+i).is(':checked');
                	//console.log(expectedUnitPrice + " , " + agreeEdgewellUnitPrice);
                	if((null == expectedUnitPrice || expectedUnitPrice == undefined || expectedUnitPrice == '') && agreeEdgewellUnitPrice == false){
                		console.log('Error at entry number ' + i + ' product ' + $('#productCode_'+i).val());
                		// Add border css
                		$('#expectedPrice_'+i).css('border', '1px solid red');
                		$('#agreeEdgewellUnitPrice_'+i).css('outline-color', 'red');
                		$('#agreeEdgewellUnitPrice_'+i).css('outline-style', 'solid');
                		//$('#agreeEdgewellUnitPrice_'+i).css('outline-width', 'thin');  
                		
                		errorCounter = errorCounter + 1;
                	}
	            }
            	
            	//console.log("errorCounter ::: " + errorCounter);   
            	var $form = $('#checkoutButtonForm');
            	if(errorCounter > 0)
 	        	{
            		//event.stopImmediatePropagation();
            		//console.log('There are ' + errorCounter + " more errors to fix. Please fix them before proceeding to checkout page !");
            		
            		$('html, body').animate({
            	        'scrollTop' : $("#breadcrumb").position().top
            	    });
            		
 	            } else {
 	            	ACC.cartremoveitem.showAndHideLoader("block"); 
	 	            var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
	 	            $form.submit();   
 	            }
            });*/

         // Cart Page On click checkout button - END
            
            redirectToCheckout = function(event){
            	//console.log('calling function from jsp');
            	
            	event.stopImmediatePropagation();
            	
            	var cartEntriesSize = $('#cartEntriesSize').val();
            	//console.log("cart entries size ::: " + cartEntriesSize);
	            var errorCounter = 0;
	            
            	for(var i = 0; i < cartEntriesSize; i++){
            		
            		var expectedUnitPrice = $('#expectedPrice_'+i).val();
                	var agreeEdgewellUnitPrice = $('#agreeEdgewellUnitPrice_'+i).is(':checked');
                	//console.log(expectedUnitPrice + " , " + agreeEdgewellUnitPrice);
                	if((null == expectedUnitPrice || expectedUnitPrice == undefined || expectedUnitPrice == '') && agreeEdgewellUnitPrice == false){
                		//console.log('Error at entry number ' + i + ' product ' + $('#productCode_'+i).val());
                		
                		// Display error message
                		$('#showErrorMessageForAgreeEdgewellPrice').show();
                		
                		// Add border css
                		$('#expectedPrice_'+i).css('border', '1px solid red');
                		$('#agreeEdgewellUnitPrice_'+i).css('outline-color', 'red');
                		$('#agreeEdgewellUnitPrice_'+i).css('outline-style', 'solid');
                		//$('#agreeEdgewellUnitPrice_'+i).css('outline-width', 'thin');  
                		
                		errorCounter = errorCounter + 1;
                	}
	            }
            	
            	//console.log("errorCounter ::: " + errorCounter);   
            	var $form = $('#checkoutButtonForm');
            	if(errorCounter > 0)
 	        	{
            		//event.stopImmediatePropagation();
            		//console.log('There are ' + errorCounter + " more errors to fix. Please fix them before proceeding to checkout page !");
            		
            		$('html, body').animate({
            	        'scrollTop' : $("#breadcrumb").position().top
            	    });
            		
 	            } else {
 	            	// Hide validation message
 	            	$('#showErrorMessageForAgreeEdgewellPrice').hide();
 	            	ACC.cartremoveitem.showAndHideLoader("block"); 
	 	            var method = $form.attr("method") ? $form.attr("method").toUpperCase() : "GET";
	 	            $form.submit();   
 	            }
            }
},
			
            getProductQuantity: function(gridContainer, mapData) 
            {
                  var skus          = jQuery.map(gridContainer.find("input[type='hidden'].sku"), function(o) {return o.value});
                  var quantities    = jQuery.map(gridContainer.find("input[type='textbox'].sku-quantity"), function(o) {return o});

                  var totalPrice = 0.0;
                  var totalQuantity = 0.0;

                  $.each(skus, function(index, skuId) 
                              { 
                        var quantity = mapData[skuId];
                        if (quantity != undefined)
                        {
                              quantities[index].value = quantity;
                              totalQuantity += parseFloat(quantity);

                              var indexPattern = "[0-9]+";
                              var currentIndex = parseInt(quantities[index].id.match(indexPattern));

                              var currentPrice = $("input[id='productPrice["+currentIndex+"]']").val();
                              totalPrice += parseFloat(currentPrice) * parseInt(quantity);
                        }
                              });

                  var subTotalValue = Currency.formatMoney(Number(totalPrice).toFixed(2), Currency.money_format[ACC.common.currentCurrency]);
                  var avgPriceValue = 0.0;
                  if (totalQuantity > 0)
                  {
                        avgPriceValue = Currency.formatMoney(Number(totalPrice/totalQuantity).toFixed(2), Currency.money_format[ACC.common.currentCurrency]);
                  }

                  gridContainer.parent().find('#quantity').html(totalQuantity);
                  gridContainer.parent().find("#avgPrice").html(avgPriceValue)
                  gridContainer.parent().find("#subtotal").html(subTotalValue);

                  var $inputQuantityValue = gridContainer.parent().find('#quantityValue');
                  var $inputAvgPriceValue = gridContainer.parent().find('#avgPriceValue');
                  var $inputSubtotalValue = gridContainer.parent().find('#subtotalValue');

                  $inputQuantityValue.val(totalQuantity);
                  $inputAvgPriceValue.val(Number(totalPrice/totalQuantity).toFixed(2));
                  $inputSubtotalValue.val(Number(totalPrice).toFixed(2));

            }, 

            coreTableActions: function(productCode, mapCodeQuantity)  
            {
                  var skuQuantityClass = '.sku-quantity';

                  var quantityBefore = 0;
                  var quantityAfter = 0;

                  var grid = $('#grid_' + productCode);

                  grid.on('click', skuQuantityClass, function(event) {
                        $(this).select();
                  });

                  grid.on('focusin', skuQuantityClass, function(event) {
                        quantityBefore = jQuery.trim(this.value);
                        if (quantityBefore == "") {
                              quantityBefore = 0;
                              this.value = 0;
                        }
                  });

                  grid.on('focusout', skuQuantityClass, function(event) {
                        var indexPattern           = "[0-9]+";
                        var currentIndex           = parseInt($(this).attr("id").match(indexPattern));
                        var $gridGroup             = $(this).parents('.orderForm_grid_group');
                        var $closestQuantityValue  = $gridGroup.find('#quantityValue');
                        var $closestAvgPriceValue  = $gridGroup.find('#avgPriceValue');
                        var $closestSubtotalValue  = $gridGroup.find('#subtotalValue');

                        var currentQuantityValue   = $closestQuantityValue.val();
                        var currentSubtotalValue   = $closestSubtotalValue.val();

                        var currentPrice = $("input[id='productPrice["+currentIndex+"]']").val();
                        var variantCode = $("input[id='cartEntries["+currentIndex+"].sku']").val();

                        quantityAfter = jQuery.trim(this.value);

                        if (isNaN(jQuery.trim(this.value))) {
                              this.value = 0;
                        }

                        if (quantityAfter == "") {
                              quantityAfter = 0;
                              this.value = 0;
                        }

                        if (quantityBefore == 0) {
                              $closestQuantityValue.val(parseInt(currentQuantityValue) + parseInt(quantityAfter));
                              $closestSubtotalValue.val(parseFloat(currentSubtotalValue) + parseFloat(currentPrice) * parseInt(quantityAfter));
                        } else {
                              $closestQuantityValue.val(parseInt(currentQuantityValue) + (parseInt(quantityAfter) - parseInt(quantityBefore)));
                              $closestSubtotalValue.val(parseFloat(currentSubtotalValue) + parseFloat(currentPrice) * (parseInt(quantityAfter) - parseInt(quantityBefore)));
                        }

                        if (parseInt($closestQuantityValue.val()) > 0) {
                              $closestAvgPriceValue.val(parseFloat($closestSubtotalValue.val()) / parseInt($closestQuantityValue.val()));
                        } else {
                              $closestAvgPriceValue.val(0);
                        }

                        $closestQuantityValue.parent().find('#quantity').html($closestQuantityValue.val());
                        $closestAvgPriceValue.parent().find('#avgPrice').html(ACC.productorderform.formatTotalsCurrency($closestAvgPriceValue.val()));
                        $closestSubtotalValue.parent().find('#subtotal').html(ACC.productorderform.formatTotalsCurrency($closestSubtotalValue.val()));

                        if (quantityBefore != quantityAfter)
                        {
                              var method = "POST";
                              $.ajax({
                                    url: ACC.config.contextPath + '/cart/update',
                                    data: {productCode: variantCode, quantity: quantityAfter, entryNumber: -1},
                                    type: method,
                                    success: function(data) 
                                    {
                                          ACC.cartremoveitem.refreshCartData(data, -1, productCode, null);
                                          mapCodeQuantity[variantCode] = quantityAfter;
                                    },
                                    error: function(xht, textStatus, ex) 
                                    {
                                          alert("Failed to get variant matrix. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                                    }

                              });
                        }

                  }); 

            },
            
            getErrors: function(cartData, entryNum, productCode, quantity)
            {                 
                  if (cartData.entries.length == 0)
                  {
                        location.reload();
                  }
                  else
                  {     
                        var errorsDiv = $('#businesRuleErrors');
                        
                        errorsDiv.html('');
                        $("#businesRuleErrors").show();           
                        var isContainerFullFlag = cartData.isContainerFull;                     
                        $('#isContainerFull').val(isContainerFullFlag);
                        
                        ACC.cartremoveitem.fillThis();
                        
                        var errorSize = cartData.businesRuleErrors.length;                      
                        var errors ="";                                             
                        if(errorSize > 0)
                {
                      $('#businesRuleErrors').fadeIn("fast");
                      for (var i = 0; i < errorSize; i++)
                      {
                            var error = cartData.businesRuleErrors[i];
                            
                            errors = errors + error + "<br/>";  
                            
                      }
                      errorsDiv.html(errors); 
                      errorsDiv.addClass("alert negative");
                      
                      $("html, body").animate({ scrollTop: 0 }, 50);
                      //errorsDiv.fadeOut(3000);
                    //Disable checkout buttons
                      //console.log('disabling checkout buttons ...');
                      $("#checkoutButton_top").prop('disabled', true);
                      $("#checkoutButton_bottom").prop('disabled', true);    
                }else{
                      $('#businesRuleErrors').fadeOut(1000);  
                      errorsDiv.removeClass("alert negative");
                    //Enable checkout buttons
                      //console.log('enabling checkout buttons ...');
                      $("#checkoutButton_top").prop('disabled', false);
                      $("#checkoutButton_bottom").prop('disabled', false);  
                }
                $('#validationErrors').fadeOut(5000);

                  }           
            },
                  

            refreshCartData: function(cartData, entryNum, productCode, quantity) 
            {                 
                  //alert("refreshCartData: "+cartData.entries.length);
                  $('#containerHeightLine').text(cartData.containerHeight);                  
                  // if cart is empty, we need to reload the whole page
                  if (cartData.entries.length == 0)
                  {
                        location.reload();
                  }
                  else
                  {                         
                        var form;   
                        var removeItem = false;
                        var totalProductWeightInPercent = cartData.totalProductWeightInPercent;
                        var totalProductVolumeInPercent = cartData.totalProductVolumeInPercent;
						var availableVolume = cartData.availableVolume;
                        var availableWeight = cartData.availableWeight;
                        ACC.cartremoveitem.isOrderBlocked =cartData.isOrderBlocked;
                                                
                        if (entryNum == -1) // grouped item
                        {   
                              var editLink = $('#QuantityProduct_' + productCode);
                              form = editLink.closest('form');

                              var quantity = 0;
                              var entryPrice = 0;
                              for (var i = 0; i < cartData.entries.length; i++)
                              {
                                    var entry = cartData.entries[i];
                                    if (entry.product.code == productCode)
                                    {                 
                                          quantity = entry.quantity;
                                          entryPrice = entry.totalPrice;
                                          break;
                                    }
                              }

                              if (quantity == 0)
                              {
                                    removeItem = true;
                                    form.parent().parent().remove();
                              }
                              else
                              {
                              
                                    form.find(".qty").html(quantity);
                                    form.parent().parent().find(".total").html(entryPrice.formattedValue);

                                    $('#weight_txt').val(totalProductWeightInPercent);
                                    $('#volume_txt').val(totalProductVolumeInPercent);   
                                    $('#availableVolume_txt').val(availableVolume);
                                    $('#availableWeight_txt').val(availableWeight);							  
                                    
                                    var isContainerFullFlag = cartData.isContainerFull;                                    
                                    $('#isContainerFull').val(isContainerFullFlag);                                           

                                    ACC.cartremoveitem.fillThis();

                              }

                        }
                        else //ungrouped item
                        {     
                              form = $('#updateCartForm' + entryNum);

                              if (quantity == 0)
                              {
                                    removeItem = true;
                                    form.parent().parent().remove();
                              }
                              else
                              {
                                    for (var i = 0; i < cartData.entries.length; i++)
                                    {
                                          var entry = cartData.entries[i];
                                          if (entry.entryNumber == entryNum)
                                          {   
                                        	  console.log('update cartform itemlevel total price div else condition' );
                                                form.find('input[name=quantity]').val(entry.quantity);
                                                
                                                if($('#isSalesRepLoggedIn').val() == "true")
                                                {
                                                	 var totalPriceDiv = "<div id='totalPriceDiv_" + entryNum + '>' + entry.totalPrice.formattedValue + "</div>";
                                                     form.parent().parent().find(".total").html(totalPriceDiv);
                                                }
                                                else
                                                {
                                                	form.parent().parent().find(".total").html(entry.totalPrice.formattedValue);
                                                }
                                                $('#weight_txt').val(totalProductWeightInPercent);
                                                $('#volume_txt').val(totalProductVolumeInPercent);
                                                $('#availableVolume_txt').val(availableVolume);
                                                $('#availableWeight_txt').val(availableWeight);  												

                                                var isContainerFullFlag = cartData.isContainerFull;                                                     
                                                $('#isContainerFull').val(isContainerFullFlag);
                        
                                                ACC.cartremoveitem.fillThis();

                                          }
                                    }
                              }
                        }

                        // remove item, need to update other items' entry numbers
                        if (removeItem === true)
                        {     
                              $('.cartItem').each(function(index)
                                          {
                                    form = $(this).find('.quantity').children().first();
                                    var productCode = form.find('input[name=productCode]').val(); 

                                    for (var i = 0; i < cartData.entries.length; i++)
                                    {
                                          var entry = cartData.entries[i];
                                          if (entry.product.code == productCode)
                                          {                       
                                                form.find('input[name=entryNumber]').val(entry.entryNumber);
                                                break;
                                          }
                                    }
                                          });
                        }

                        // refresh mini cart    
                        ACC.minicart.refreshMiniCartCount();
                       
                        $('#orderTotals').next().remove();
                        $('#orderTotals').remove();
                        $("#ajaxCart").html($("#cartTotalsTemplate").tmpl({data: cartData}));      
                        
                        ACC.cartremoveitem.getErrors(cartData, entryNum, productCode, quantity);
                        //if($(".form-actions .positive").length !== 0){
                             // $(".form-actions .positive").click(); 
                        //} else {
                        	//$('#containeroptimization').submit();
                        //}                       
                  }
                  
                  $('#weight_txt').val(totalProductWeightInPercent);
                  $('#volume_txt').val(totalProductVolumeInPercent); 
                  $('#availableVolume_txt').val(availableVolume);
                  $('#availableWeight_txt').val(availableWeight);				  
                  
                  // Refresh the pallet counts
                  $('#totalPalletCount').html(cartData.totalPalletCount);
                  $('#partialPalletCount').html(cartData.virtualPalletCount);
                  
                  var isContainerFullFlag = cartData.isContainerFull;                                 
                  $('#isContainerFull').val(isContainerFullFlag);                                           

                  ACC.cartremoveitem.fillThis();
                  
                 ACC.cartremoveitem.showAndHideLoader("none");   
                 
                 var errorSize = cartData.businesRuleErrors.length;                      
                 var errors ="";                                             
                 if(errorSize > 0)
		         {
                	//Disable checkout buttons
                     //console.log('disabling checkout buttons ...');
                     $("#checkoutButton_top").prop('disabled', true);
                     $("#checkoutButton_bottom").prop('disabled', true); 	 
		         }
                 if(!cartData.isFloorSpaceFull){
                	 $('#palletsCountInfo').css('color', 'blue');
                 } else {
                	 $('#palletsCountInfo').css('color', 'red');
                 }
                 
                 // reloading the whole page to refresh the container floor space graphics
                 location.reload(true);     
            
            },
            getCartData : function()
            {
            var contHeight = $("#volume_cont").height();
            var isContainerFull = $('#isContainerFull').val();
            var isOrderBlocked = $('#isOrderBlocked').val();
            var getVolTxt = $("#volume_txt").val();
            var getWeightTxt = $("#weight_txt").val();
            var weightCont = $("#weight_cont").height(); 
            var getavailableVolTxt = $("#availableVolume_txt").val();
            var getavailableWeightTxt = $("#availableWeight_txt").val();			
           // var percentageSign = $("#percentageSign").val();	
            var errorsDiv = $('#businesRuleErrors').show(); 
            var errorMsg = "";
            
            if(isOrderBlocked=='true'){
            	
            	$("#checkoutButton_top").attr("disabled", true);
                $("#checkoutButton_bottom").attr("disabled",true);
                errorMsg =errorMsg + " Dear Customer, You order has been blocked. Please contact Customer Care <br>"

          }
            
            if(isContainerFull == 'true')
            {
                  
                  if(getVolTxt < 100){
                        contHeight = (contHeight*getVolTxt)/100;
                        $("#volume_utilization").css('background-color', '#33cc33'); 
                        $("#utl_vol").text(getavailableVolTxt);
                        $("#volumePercentageSign").text("%");
                  }
                  else{
                        $("#volume_utilization").css('background-color', '#FF5757');       
                        $("#utl_vol").text("Volume Exceeded");
                        $("#volumePercentageSign").text("");
                  }                             
                  
                  if(getWeightTxt <100){
                        weightCont = (weightCont*getWeightTxt)/100;
                        $("#weight_utilization").css('background-color', '#33cc33');       
                        $("#utl_wt").text(getavailableWeightTxt);
                        $("#weightPercentageSign").text("%");
                  }
                  else{
                        $("#weight_utilization").css('background-color', '#FF5757');       
                        $("#utl_wt").text("Weight Exceeded");
                        $("#weightPercentageSign").text("");
                  }                             
                  
                        
            
                  $("#weight_utilization").css('height', weightCont);         
                  $("#volume_utilization").css('height', contHeight); 
            
                  $("#checkoutButton_top").attr("disabled", true);
                  $("#checkoutButton_bottom").attr("disabled",true);    
                  $("#continueButton_bottom").attr("disabled",true);    
                  
                  errorMsg = "Dear Customer, your order will not fit in one container. Please, adjust the cart and/or place multiple orders. <br>";
            }     
            
            

            
        if(errorMsg==""){
            errorsDiv.hide()
            errorsDiv.removeClass("alert negative");
        }else{
            errorsDiv.html(errorMsg); 
            errorsDiv.addClass("alert negative");
            $("html, body").animate({ scrollTop: 0 }, 50);
        }
            
            
            if(isContainerFull == 'false')
            { 
                   $("#volume_utilization").css('background-color', '#33cc33'); 
                   $("#weight_utilization").css('background-color', '#33cc33'); 
                  if(getVolTxt == 100)
                    {                     
                       $("#volume_utilization").css('height', contHeight);       
                       $("#volumePercentageSign").text("%");
                    }
                    if(getWeightTxt == 100)
                    {
                    $("#weight_utilization").css('height', contHeight);    
                    $("#weightPercentageSign").text("%");
                    }
					
					if(getVolTxt > 100){
                  	    contHeight = (contHeight*getVolTxt)/100;
                  	    $("#volume_utilization").css('height', contHeight);
                  	    $("#volume_utilization").css('background-color', '#FF5757');       
                        $("#utl_vol").text("Volume Exceeded");
                        $("#volumePercentageSign").text("");
                     }
                                               
                    if(getWeightTxt > 100){
                	    weightCont = (weightCont*getWeightTxt)/100;
                	    $("#weight_utilization").css('height', weightCont);
                  	    $("#weight_utilization").css('background-color', '#FF5757');       
                        $("#utl_wt").text("Weight Exceeded");
                        $("#weightPercentageSign").text("");
                  }

            }
                        
            },
            
            fillThis: function() {  
                  var contHeight = $("#volume_cont").height(); 
                  var volUtl= $("#volume_utilization").height();
                  var volCont = $("#volume_cont").height(); 
                  var weightUtl= $("#weight_utilization").height();
                  var weightCont = $("#weight_cont").height();                
                  var isContainerFull = $('#isContainerFull').val();
                  isContainerfull = isContainerFull;

                  if(null !=contHeight && null != volCont  ){

                        var getVolTxt = $("#volume_txt").val();
                        var getWeightTxt = $("#weight_txt").val();
						var getavailableVolTxt = $("#availableVolume_txt").val();
                        var getavailableWeightTxt = $("#availableWeight_txt").val();
                        
                        if(isContainerFull == 'true')
                        { 

                              if(getVolTxt < 100){
                                    contHeight = (contHeight*getVolTxt)/100;
                                    $("#volume_utilization").css('background-color', '#33cc33'); 
                                    $("#utl_vol").text(getavailableVolTxt);
                                    $("#volumePercentageSign").text("%");
                              }
                              else{
                                    $("#volume_utilization").css('background-color', '#FF5757');       
                                    $("#utl_vol").text("Volume Exceeded");
                                    $("#volumePercentageSign").text("");
                              }                             
                              if(getWeightTxt <100){
                                    weightCont = (weightCont*getWeightTxt)/100;
                                    $("#weight_utilization").css('background-color', '#33cc33'); 
                                    $("#utl_wt").text(getavailableWeightTxt);
                                    $("#weightPercentageSign").text("%");
                              }
                              else{
                                    $("#weight_utilization").css('background-color', '#FF5757');       
                                    $("#utl_wt").text("Weight Exceeded");
                                    $("#weightPercentageSign").text("");
                              }                             
                              
                                                      
                              $("#weight_utilization").css('height', weightCont);         
                              $("#volume_utilization").css('height', contHeight);                     
                         
                               //Disable checkout buttons
                              $("#checkoutButton_top").attr("disabled", true);
                              $("#checkoutButton_bottom").attr("disabled",true);   
                              $("#continueButton_bottom").attr("disabled",true);   
                        }
                        
                        if(isContainerFull == 'false')
                        {
                        if(ACC.cartremoveitem.isOrderBlocked != true)  {
                              $("#checkoutButton_top").attr("disabled", false);                      
                               $("#checkoutButton_bottom").attr("disabled",false);
                              $("#continueButton_bottom").attr("disabled",false);  
                        }
                       // $("#volume_utilization").css('background-color', '#33cc33'); 
                        // $("#weight_utilization").css('background-color', '#33cc33'); 
                         
                         //$("#utl_vol").text(getVolTxt);
                       // $("#utl_wt").text(getWeightTxt);
                        if(getVolTxt == 100)
                          {     
                             $("#volume_utilization").css('background-color', '#33cc33');						  
                             $("#volume_utilization").css('height', contHeight); 
                             $("#utl_vol").text(getavailableVolTxt);
                             $("#volumePercentageSign").text("%");
                          }
                          if(getWeightTxt == 100)
                          {
						       $("#weight_utilization").css('background-color', '#33cc33');
                               $("#weight_utilization").css('height', contHeight);    
                               $("#utl_wt").text(getavailableWeightTxt);
                               $("#weightPercentageSign").text("%");
                          }
						  
						   if(getVolTxt > 100)
						  {
                        	  $("#volume_utilization").css('background-color', '#FF5757');       
                              $("#utl_vol").text("Volume Exceeded");
                              $("#volumePercentageSign").text("");
                          }
                                                     
                        if(getWeightTxt > 100)
						{
                        	$("#weight_utilization").css('background-color', '#FF5757');       
                            $("#utl_wt").text("Weight Exceeded");
                            $("#weightPercentageSign").text("");
                        }
                          
                          ACC.common.$globalMessages.html('<div id="businesRuleErrors"></div>');
                                               
                        }
                        
                        if(getVolTxt <100){
                              //console.log('contHeight ::: ' + contHeight)
                              $("#volume_utilization").css('height', contHeight * getVolTxt / 100); 
                              var volUtlBar = document.getElementById("volume_utilization").style.height;
                              volUtlBar = volUtlBar.replace('px', '');
                              $("#volume_utilization").css('background-color', '#33cc33');
                              $("#utl_vol").text(getavailableVolTxt);
                              $("#volumePercentageSign").text("%");
							  if(volUtlBar > contHeight) { callBackIfExceeds(volUtl, volCont); } 
                        }
                        
                        if(getWeightTxt <100)
                        {           
                              $("#weight_utilization").css('height', contHeight * getWeightTxt / 100);
                              var weightUtlBar = document.getElementById("weight_utilization").style.height;
                              weightUtlBar = weightUtlBar.replace('px', ''); 
                              $("#weight_utilization").css('background-color', '#33cc33');
							  $("#utl_wt").text(getavailableWeightTxt);
							  $("#weightPercentageSign").text("%");
							  if(weightUtlBar > contHeight) { callBackIfExceeds(weightUtl, weightCont); } 
                        
                        }
                        
                        if(isContainerFull == 'true')
                        {
                        $("#weight_utilization").css('height', weightCont);                          
                        $("#volume_utilization").css('height', contHeight); 
                        
                        }
                  }
            }
}


$(document).ready(function (){ 
            $("body").append(spinnerLoader);
      ACC.cartremoveitem.bindCartData();        
      ACC.cartremoveitem.bindAll();      
      if($("#productsNotAddedToCart").css('display') !== 'none'){
        ACC.cartremoveitem.highlightQtyInputBox();
      }
      
});
