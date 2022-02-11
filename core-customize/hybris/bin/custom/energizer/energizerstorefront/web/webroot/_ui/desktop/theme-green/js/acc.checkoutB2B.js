ACC.checkoutB2B = {
      cartData:"",
      pageInitDone:false,
      settingDefaultCostCenter: function()
      {
            var costCenter = $(".constCenterSelect").val();
            
            $.ajax({
                  type : "POST",
                  url : "/checkout/single/summary/setCostCenter.json",
                  data : {
                        costCenterId : costCenter
                  },
                  success : function() {
                  }
            });
      },
      
      PaymentType: function ()
      {


            if($(".summaryPaymentType").length>0){
                  var $element = $(".summaryPaymentType")
                  var config = $element.data()


                  $(".summaryPaymentType input[name='PaymentType']").on("change",function(){
                        $.postJSON(config.setPaymentTypeUrl, {paymentType: $(this).val()}, function(data){     
                              ACC.checkoutB2B.refresh(data);      
                              $(".summarySection").removeClass("complete")
                              $(".summaryPaymentType").addClass("complete")
                              $(".summaryCostCenter .constCenterSelect").val("")

                        });
                        var globalMessage = document.getElementById("globalMessages");
                        while (globalMessage.firstChild) {
                              globalMessage.removeChild(globalMessage.firstChild);
                        }
                  })



                  $("#PurchaseOrderNumber").blur(function(){
                	  
                	  var poNumber = $(this).val();    
                      var poNumberPattern =new RegExp('^'+$('#poNumPatternId').val());
                      if(poNumber && !new RegExp('^\\s{1,}$').test(poNumber) && poNumber!=''){
                    	 if(poNumberPattern.test(poNumber)){
                    		 $('#globalMessages').empty();
                              $.postJSON(config.setPurchaseOrderNumberUrl, {purchaseOrderNumber: $(this).val()}, function(data){
                                    ACC.checkoutB2B.refresh(data);            
                              });
                              
                    	 }else{
                   		  var html = "<div class='alert negative'>Purchase Order Number is not valid.</div>";
                          $('#globalMessages').html(html);
                          $("html, body").animate({ scrollTop: 0 }, 50);
                          return false; 
                	  }
                    }
                        
                  });

            }
      
      },
            
      costCenter: function ()
      {
            
            if($(".summaryCostCenter").length>0){
                  var config = $(".summaryCostCenter").data()
                  $(".summaryCostCenter .constCenterSelect").on("change",function(){
                        if($(this).val()!=""){
                              $.postJSON(config.setCostCenterUrl, {costCenterId: $(this).val()}, function(data){
                                    ACC.checkoutB2B.refresh(data);
                                    $(".summaryCostCenter").addClass("complete")
                              });
                        }else{
                              $(".summaryCostCenter").removeClass("complete")
                        }
                  })
            }

      },
      
      
      createUpdatePaymentForm: function ()
      {

            $(document).on("change", "#cardType", ACC.checkoutB2B.displayStartDateIssueNum);

            if($('#differentAddress:checked').length<1){
                  $("#newBillingAddressFields :input").attr('disabled', 'disabled');
            }
            $('.create_update_payment_form').each(function ()
            {

                  ACC.checkoutB2B.displayStartDateIssueNum()

                  var options = {
                        type: 'POST',
                        beforeSubmit: function ()
                        {
                              
                        },
                        success: function (data)
                        {
                              $('#summaryPaymentOverlayEnterNew').html(data);
                              ACC.checkoutB2B.createUpdatePaymentForm();
                              if ($('#paymentDetailsForm').hasClass('Success'))
                              {
                                    ACC.checkoutB2B.refresh();
                                    
                                    parent.$.colorbox.close();
                              }
                              else
                              {
                                    
                                    $.colorbox.resize();
                              }
                        },
                        error: function (xht, textStatus, ex)
                        {
                              alert("Failed to create/update payment details. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                        },
                        complete: function ()
                        {
                              
      
                        }
                  };

                  $(this).ajaxForm(options);

            });
      },
      payment: function ()
      {
            
            $(document).ready(function(){
                  $("#securityCode").val("");
            });
            
            $(document).on("focusout",'.summaryPayment #SecurityCodePayment', function (e){
                  $("#securityCode").val($(this).val())
            });
            
            
            
            
            $(document).on("click",'.summaryPayment .security_code_what', function (e){
                  $('.security_code_what').bt($(".summaryPayment").data('security-what-text'), {
                        trigger: 'click',
                        positions: 'bottom',
                        fill: '#efefef',
                        cssStyles: {
                              fontSize: '11px'
                        }
                  });
            });
            

            $(document).on("click",'#summaryOverlayViewSavedPaymentMethods', function (){
                  $("#summaryPaymentOverlay").show();
                  $("#summaryPaymentOverlayEnterNew").remove();
                  $.colorbox.resize();
            });
            
            
            $(document).on("click",'#summaryPaymentOverlay .useCard', function (e){
                  e.preventDefault();
                  var paymentId = $(this).data('payment');
                  $.postJSON(setPaymentDetailsUrl, {paymentId: paymentId}, function(data){
                        ACC.checkoutB2B.refresh(data);
                        $.colorbox.close();
                  });
            });
            
            
            
            
            
            $(document).on("click",'#summaryPaymentOverlay .enterNewPayment', function (){
                  $("#summaryPaymentOverlay").hide();
                  $("#summaryPaymentOverlay").after($("#summaryPaymentOverlayEnterNew").clone());
                  ACC.checkoutB2B.createUpdatePaymentForm();      
                  $.colorbox.resize();
            });
            
            
            $(document).on("click",'.summaryPayment .editButton', function (){

                  $("#summaryPaymentOverlayContainer").html("");
                  $("#summaryPaymentOverlayContainer").append($("#summaryPaymentOverlayEnterNew").clone()).append($("#summaryPaymentOverlay").clone());



                              $.colorbox({
                                    inline: true,
                                    href: "#summaryPaymentOverlayContainer",
                                    height: false,
                                    overlayClose: false,
                                    onComplete: function ()
                                    {
                                          ACC.common.refreshScreenReaderBuffer();
                                                $.colorbox.resize();
                                                ACC.checkoutB2B.createUpdatePaymentForm();      

                                                $.ajax({
                                                      url: $(".summaryPayment").data("getSavedCardsUrl"),
                                                      type: 'POST',
                                                      cache: false,
                                                      dataType: 'json',
                                                      success: function (data)
                                                      {
                                                            $('#summaryPaymentOverlay').html($('#savedCardsTemplate').tmpl({savedCards: data})).hide();
                                                            
                                                      },
                                                      error: function (xht, textStatus, ex)
                                                      {
                                                            alert("Failed to get saved cards. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                                                      }
                                                });
                  
                                    },
                                    onClosed: function ()
                                    {
                                          ACC.common.refreshScreenReaderBuffer();
                                    }
                              });

            });
            
            
            
            $(document).on("change",'#differentAddress', function (){
                  var newAddress = $('#differentAddress').attr("checked");
                  if(newAddress)
                  {
                        $("#newBillingAddressFields :input").removeAttr('disabled');
                  }
                  else
                  {
                        $("#newBillingAddressFields :input").attr('disabled', 'disabled');
                  }
            });
            
      },
      deliveryAddress: function (section)
      {
            
                  
            $(document).on("click",'#summaryDeliveryAddressOverlay #summaryOverlayViewAddressForm', function (){
                  $("#summaryDeliveryAddressForm").show();
                  $("#summaryDeliveryAddressBook").hide();
                  $.colorbox.resize();
            });
            
            $(document).on("click",'#summaryDeliveryAddressOverlay #summaryOverlayViewAddressBook', function (){
                  $("#summaryDeliveryAddressBook").show();
                  $("#summaryDeliveryAddressForm").hide();
                  $.colorbox.resize();
            });         
            
            $(document).on("click",'.summaryDeliveryAddress .editButton', function (){



                  if(ACC.checkoutB2B.cartData  != null ){

                        $.postJSON($(".summaryDeliveryAddress").data('get'), function(data){
                              section= ".summaryDeliveryAddress";
                              $("#summaryDeliveryAddressBook").html($(section+' .colorboxTemplate').tmpl({addresses: data,editable: false}));
                              $("#summaryDeliveryAddressBook").show();
                              $("#summaryDeliveryAddressForm").hide();


                              $.colorbox({
                                    inline: true,
                                    href: "#summaryDeliveryAddressOverlay",
                                    height: false,
                                    overlayClose: false,
                                    onComplete: function ()
                                    {
                                          ACC.common.refreshScreenReaderBuffer();
                                                $.colorbox.resize();
                                    },
                                    onClosed: function ()
                                    {

                                          ACC.common.refreshScreenReaderBuffer();
                                    }
                              });


                        });

                        


                        return;
                  }



                  $("#summaryDeliveryAddressBook").hide();
                  var changeFormUrl= $(".summaryDeliveryAddress").data('get-form')+"?createUpdateStatus=1&addressId="+$(this).data("address");
                  $.get(changeFormUrl, function(data){
                        $("#summaryDeliveryAddressForm").remove();
                        $("#summaryDeliveryAddressOverlay").prepend(data);
                        
                  
                        $.postJSON($(".summaryDeliveryAddress").data('get'), function(data){
                              section= ".summaryDeliveryAddress";
                              $("#summaryDeliveryAddressBook").html($(section+' .colorboxTemplate').tmpl({addresses: data,editable: true}));
                        });
                  
                        
                        ACC.checkoutB2B.createUpdateAddressForm();
                        
                        $.colorbox({
                              inline: true,
                              href: "#summaryDeliveryAddressOverlay",
                              height: false,
                              overlayClose: false,
                              onComplete: function ()
                              {
                                    ACC.common.refreshScreenReaderBuffer();
                                          $.colorbox.resize();
                              },
                              onClosed: function ()
                              {
                                    ACC.common.refreshScreenReaderBuffer();
                              }
                        });
                  });
            });
            
            
            $(document).on("click",'#summaryDeliveryAddressOverlay .useAddress', function (e){
                  e.preventDefault();
                  var addressId = $(this).data('address');
                  $.postJSON($('.summaryDeliveryAddress').data("set"), {addressId: addressId}, function(data){
                	  
                	  var siteUid = $('#siteUid').val();
                	  // Set Requested Delivery Date only for EMEA in Checkout Page - START
                	  if(siteUid.indexOf("EMEA") != -1){
                		  document.leadTime=data.leadTime;
    	                  document.leadTime = document.leadTime +" Days";
    	                  document.getElementById("leadTimeIdEMEA").innerHTML = document.leadTime;
                          datePickerTextBox=$("#datepicker-2");
                          if(data.requestedDeliveryDate!=null && data.requestedDeliveryDate>0)
                            {
                        	  //alert("if .. lead time "+data.leadTime);
                        	  // alert("data.requestedDeliveryDate "+data.requestedDeliveryDate);
                              leadTimeDate=new Date(data.requestedDeliveryDate);
                              // alert("leadTimeDate "+leadTimeDate);
                              datePart=(leadTimeDate.getDate().length < 2)?'0'+leadTimeDate.getDate():leadTimeDate.getDate();
                              monthPart=(leadTimeDate.getMonth().length < 2)?'0'+(leadTimeDate.getMonth()+1):(leadTimeDate.getMonth()+1);
                              yearPart=leadTimeDate.getFullYear().toString();
                              datePickerTextBox.val(monthPart+"-"+datePart+"-"+yearPart);
                            }
                          else
                            {
                        	  //alert("else .. lead time "+data.leadTime);
                              if(document.leadTime!=null && document.leadTime>0)
                                {
                                  leadTimeDate=new Date(new Date().getTime()+(86400000*document.leadTime));
                                  datePart=(leadTimeDate.getDate().length < 2)?'0'+leadTimeDate.getDate():leadTimeDate.getDate();
                                  monthPart=(leadTimeDate.getMonth().length < 2)?'0'+(leadTimeDate.getMonth()+1):(leadTimeDate.getMonth()+1);
                                  yearPart=leadTimeDate.getFullYear().toString();
                                  datePickerTextBox.val(monthPart+"-"+datePart+"-"+yearPart);
                                }
                            }
                          
                            var reqDevdate=new Date(data.requestedDeliveryDate);
                            var curr_date = reqDevdate.getDate();
                            var curr_month = reqDevdate.getMonth();
                            var curr_year = reqDevdate.getFullYear();
                            var reqDeliverDate = curr_month+"/"+curr_date+"/"+curr_year;
                            document.assignLeadTimeToDatePicker();
                            prepopulatedDate = datePickerTextBox.val();
                            //alert()
                           /* ACC.checkoutB2B.refresh(data);
                            $.colorbox.close();*/
                	  } /*else {
                		  console.log('close the box !'); 
                		  ACC.checkoutB2B.refresh(data);
                		  $.colorbox.close();
                	  }*/
                	  ACC.checkoutB2B.refresh(data);
            		  $.colorbox.close();
                	// Set Requested Delivery Date only for EMEA in Checkout Page - END
                  });
                  
            });
            
            
            $(document).on("click",'#summaryDeliveryAddressOverlay #addressForm button', function (){
                  var saveAddressChecked = $(this).prop('checked');
                  $('#defaultAddress').prop('disabled', !saveAddressChecked);
                  if (!saveAddressChecked) {
                        $('#defaultAddress').prop('checked', false);
                  }
            });




            $(document).on("click",'#summaryDeliveryAddressBook .addressEntry .edit', function (e){
                  e.preventDefault();
                  
                  //var changeFormUrl= $(".summaryDeliveryAddress").data('get-form')+"?createUpdateStatus=1&addressId="+$(this).data("address");
                  $.get(changeFormUrl, function(data){

                        $("#summaryDeliveryAddressForm").remove();
                        $("#summaryDeliveryAddressOverlay").prepend(data);
                        $("#summaryDeliveryAddressBook").hide();
                        $("#summaryDeliveryAddressForm").show();
                        $.colorbox.resize();

                        ACC.checkoutB2B.createUpdateAddressForm();

                  });
            });


            /*$(document).on("click",'#summaryDeliveryAddressOverlay button.default', function (e){
                  e.preventDefault();
                  var addressId = $(this).data('address');
                  $.postJSON($('.summaryDeliveryAddress').data("setDefault"), {addressId: addressId}, function(data){
                        section= ".summaryDeliveryAddress";
                        $("#summaryDeliveryAddressBook").html($(section+' .colorboxTemplate').tmpl({addresses: data}));
                  });
                  
            });*/
       
            

            
      },
      createUpdateAddressForm: function ()
      {
            $('.create_update_address_form').each(function ()
            {
                  var options = {
                        type: 'POST',
                        beforeSubmit: function ()
                        {
                              
                        },
                        success: function (data)
                        {
                              
                              $('#summaryDeliveryAddressForm').html(data);
                              ACC.checkoutB2B.createUpdateAddressForm();
                              if ("Success" == $(".create_update_address_id").data('status'))
                              {
                                    ACC.checkoutB2B.refresh();
                                    
                                    parent.$.colorbox.close();
                              }
                              else
                              {
                                    
                                    $.colorbox.resize();
                              }
                        },
                        error: function (xht, textStatus, ex)
                        {
                              alert("Failed to update cart. Error details [" + xht + ", " + textStatus + ", " + ex + "]");
                        },
                        complete: function ()
                        {
                              
                        }
                  };

                  $(this).ajaxForm(options);
            });
      },
      deliveryMode: function ()
      {
            
            $(document).on("click",'#summaryDeliveryModeOverlay .saveButton', function (e){
                  e.preventDefault();
                  var setDeliveryModeUrl = $(".summaryDeliveryMode").data("set");
                  var selectedCode = $('input:radio[name=delivery]:checked').val();
                  if(selectedCode)
                  {
                        $.postJSON(setDeliveryModeUrl, {modeCode: selectedCode}, function(data){
                              ACC.checkoutB2B.refresh(data);
                        });
                  }
                  $.colorbox.close();
            });
      
      
            $(document).on("click",'.summaryDeliveryMode .editButton', function (){
                  section= ".summaryDeliveryMode";
                  $.postJSON($(section).data('get'), function(data){
                        if (ACC.checkoutB2B.cartData.deliveryMode != null)
                              $("#summaryDeliveryModeOverlay").html($(section+' .colorboxTemplate').tmpl({data: data, selectedCode: ACC.checkoutB2B.cartData.deliveryMode.code}));
                        else
                              $("#summaryDeliveryModeOverlay").html($(section+' .colorboxTemplate').tmpl({data: data, selectedCode: 'standard-net'}));
                  });
                  
      
                  $.colorbox({
                        inline: true,
                        href: "#summaryDeliveryModeOverlay",
                        height: false,
                        overlayClose: false,
                        onComplete: function ()
                        {
                              ACC.common.refreshScreenReaderBuffer();
                                    $.colorbox.resize();
                        },
                        onClosed: function ()
                        {
                              ACC.common.refreshScreenReaderBuffer();
                        }
                  });
      
            });
      },
      displaySection: function (section)
      {
            var cartData = ACC.checkoutB2B.cartData;
            
            /*if($(".summaryPaymentType input[name=PaymentType]:checked").length < 1){
                  $("#PaymentTypeSelection_"+cartData.paymentType.code).attr("checked",true);
            }*/

            /*if(cartData.paymentType.code=="ACCOUNT"){
                  $(".summaryCostCenter").show();
                  $(".summaryPayment").hide();
                  $("#deliverySectionNum").text("3");
            }else{
                  $(".summaryCostCenter").hide();
                  $(".summaryPayment").show();
                  $("#deliverySectionNum").text("2");
                  
            }*/
      },
      refreshSection: function (section)
      {
            var cartData = ACC.checkoutB2B.cartData;
            $(section+" .contentSection").html($(section+' .sectionTemplate').tmpl({data: cartData}));
            ACC.checkoutB2B.displaySection();
      },
      refresh: function(data){
            if(data==undefined)
            {
                  $.postJSON($("#checkoutContentPanel").data("checkoutCartUrl"), function(data){
                        ACC.checkoutB2B.refreshSections(data);
                  });
            }
            else
            {
                  ACC.checkoutB2B.refreshSections(data);
            }
      },
      refreshSections: function(data){
            
            ACC.checkoutB2B.cartData=data;
            ACC.checkoutB2B.refreshCart(data);
            if(data.deliveryAddress!=null)
            	{
            	
            ACC.checkoutB2B.getLeadTime(data);
            	}
            // ACC.checkoutB2B.refreshSection(".summaryPaymentType");
            // ACC.checkoutB2B.refreshSection(".summaryCostCenter");
            ACC.checkoutB2B.refreshSection(".summaryPayment");
            ACC.checkoutB2B.refreshSection(".summaryDeliveryAddress");
            if(data.deliveryAddress==null)
            {
                  li=document.createElement("li");
                  $(li).html(noDeliveryAddressMesage);
                  $(".summaryDeliveryAddress ul li").remove();
                  $(".summaryDeliveryAddress ul").append(li);
                  
                 
            }
            
            ACC.checkoutB2B.refreshSection(".summaryDeliveryMode");

            if (data.deliveryMode == null || data.deliveryAddress == null || (data.paymentType.code == "CARD" && data.paymentInfo == null)) {
                  $(".placeOrderButton").attr("disabled", "disabled")
                  $(".scheduleReplenishmentButton").attr("disabled", "disabled")
                  $(".requestQuoteButton").attr("disabled", "disabled")
            } else {
                  if ($('#replenishmentSchedule').is(':visible')) {
                        $(".placeOrderButton").attr("disabled", "disabled")
                        $(".requestQuoteButton").attr("disabled", "disabled")
                        $(".scheduleReplenishmentButton").removeAttr("disabled")
                        
                  } else if ($('#negotiateQuote').is(':visible')) {
                        $(".placeOrderButton").attr("disabled", "disabled")
                        $(".scheduleReplenishmentButton").attr("disabled", "disabled")
                        $(".requestQuoteButton").removeAttr("disabled")
                        
                  } else {
                        $(".placeOrderButton").removeAttr("disabled")
                        $(".scheduleReplenishmentButton").removeAttr("disabled")

                        if (data.paymentType.code == "CARD" || !data.quoteAllowed) {
                              $(".requestQuoteButton").attr("disabled", "disabled")
                        } else {
                              $(".requestQuoteButton").removeAttr("disabled")
                        }
                  }
            }

      },
      refreshCart: function(data){
            $("#ajaxCart").html($("#cartTotalsTemplate").tmpl({data: data}))
            $("#ajaxCartItems").html($("#cartItemsTemplate").tmpl({data: data}))          
      },
      
      
      scheduleReplenishment: function(data){
                  
                  $(document).on("click",'#placeOrder .scheduleReplenishmentButton', function (e){
                        e.preventDefault();

                        if (ACC.checkoutB2B.cartData.quoteAllowed)
                              $("#placeOrder .requestQuoteButton").attr('disabled', 'disabled');
                        $(".placeOrderButton").attr('disabled', 'disabled');
                        $("#placeOrder .scheduleReplenishmentButton").addClass('pressed');
                        $('#replenishmentSchedule').show();
                        ACC.checkoutB2B.replenishmentInit();
                  });

                  $(document).on("click",'#replenishmentSchedule #placeReplenishmentOrder', function (e){
                        e.preventDefault();
                        $(".replenishmentOrderClass").val(true);
                        var securityCode = $("#SecurityCodePayment").val();
                        $(".securityCodeClass").val(securityCode);
                        $("#placeOrderForm1").submit();
                  });

                  $(document).on("click",'#replenishmentSchedule #cancelReplenishmentOrder', function (e){
                        e.preventDefault();
                        if (ACC.checkoutB2B.cartData.quoteAllowed && ACC.checkoutB2B.cartData.paymentType.code != "CARD")
                              $("#placeOrder .requestQuoteButton").removeAttr('disabled');
                        $(".placeOrderButton").removeAttr('disabled');
                        $("#placeOrder .scheduleReplenishmentButton").removeClass('pressed');
                        $('#replenishmentSchedule').hide();
                        $(".replenishmentOrderClass").val(false);
                  });

                  $(document).on("click",'#replenishmentSchedule .replenishmentfrequencyD, #replenishmentSchedule .replenishmentfrequencyW, #replenishmentSchedule .replenishmentfrequencyM', function (e){
                        $('.scheduleform').removeClass('selected');
                        $(this).parents('.scheduleform').addClass('selected');
                  });   

                  var placeOrderFormReplenishmentOrder = $('#replenishmentSchedule').data("placeOrderFormReplenishmentOrder")

                  if(placeOrderFormReplenishmentOrder){
                        $('#placeOrder .scheduleReplenishmentButton').click()
                  }
            
      },

      replenishmentInit: function ()
      {
            var dateForDatePicker = $('#replenishmentSchedule').data("dateForDatePicker");
            var placeOrderFormReplenishmentRecurrence = $('#replenishmentSchedule').data("placeOrderFormReplenishmentRecurrence");
            var placeOrderFormNDays = $('#replenishmentSchedule').data("placeOrderFormNDays");
            var placeOrderFormNthDayOfMonth = $('#replenishmentSchedule').data("placeOrderFormNthDayOfMonth");
            var placeOrderFormNegotiateQuote = $('#replenishmentSchedule').data("placeOrderFormNegotiateQuote");
            var placeOrderFormReplenishmentOrder = $('#replenishmentSchedule').data("placeOrderFormReplenishmentOrder");
            
            $("input:radio[name='replenishmentRecurrence'][value=" + placeOrderFormReplenishmentRecurrence + "]").attr('checked', true);
            $("input:radio[name='replenishmentRecurrence'][value=" + placeOrderFormReplenishmentRecurrence + "]").parents('.scheduleform').addClass('selected');;
            $("#nDays option[value=" + placeOrderFormNDays + "]").attr('selected', 'selected');
            $("#daysoFWeek option[value=" + placeOrderFormNthDayOfMonth + "]").attr('selected', 'selected');
            $("#replenishmentStartDate").datepicker({dateFormat: dateForDatePicker});
            $("#replenishmentStartDate").datepicker("option", "appendText", dateForDatePicker);     
      },
      
      
      negotiateQuote: function(data){
            
                  $(document).on("click",'#placeOrder .requestQuoteButton', function (e){
                        e.preventDefault();
                        $("#placeOrder .scheduleReplenishmentButton").attr('disabled', 'disabled');
                        $(".placeOrderButton").attr('disabled', 'disabled');
                        $("#placeOrder .requestQuoteButton").addClass('pressed');
                        $('#negotiateQuote').show();
                        
                  });
                  
                  $(document).on("click",'#negotiateQuote #placeNegotiateQuote', function (e){
                        e.preventDefault();
                        $(".negotiateQuoteClass").val(true);
                        var securityCode = $("#SecurityCodePayment").val();
                        $(".securityCodeClass").val(securityCode);
                        $("#placeOrderForm1").submit();
                  });

                  $(document).on("click",'#negotiateQuote #cancelNegotiateQuote', function (e){
                        e.preventDefault();
                        $("#placeOrder .scheduleReplenishmentButton").removeAttr('disabled');
                        $(".placeOrderButton").removeAttr('disabled');
                        $("#placeOrder .requestQuoteButton").removeClass('pressed');
                        $('#negotiateQuote').hide();
                        $(".negotiateQuoteClass").val(false);
                  });


                  var placeOrderFormNegotiateQuote = $('#replenishmentSchedule').data("placeOrderFormNegotiateQuote");

                  if(placeOrderFormNegotiateQuote){
                        $('#placeOrder .requestQuoteButton').click()
                  }
      },
      
      
      placeOrder: function(data){
                 
                  $(document).on("click",'.placeOrderButton', function (e){
                        e.preventDefault();
                        var securityCode = $("#SecurityCodePayment").val();
                        $(".securityCodeClass").val(securityCode);
                        $("#placeOrderForm1").submit();
                  });
                  
                  
                  $(document).on("click",'#Terms1,#Terms2', function (e){
                        if($(this).attr('checked')){
                              $('#Terms1,#Terms2').attr('checked','checked')
                        }else{
                              $('#Terms1,#Terms2').removeAttr('checked')
                        }
                  });
                  

                  
      },

      displayStartDateIssueNum: function()
      {
            
            var cardType = $("#cardType").val();
                        
      if (cardType == 'maestro' || cardType == 'switch')
        {
            $('#startDate, #issueNum').show("fast",'linear', function() {
                        parent.$.colorbox.resize()
                  });
        }
        else
        {
            $('#startDate, #issueNum').hide('fast','linear', function() {
                        parent.$.colorbox.resize()
                  });
            
          }
            
                                    
    },
    
getLeadTime:function(data){
	
		var siteUid = $('#siteUid').val();
		 // Set Requested Delivery Date only for EMEA in Checkout Page - START
		if(siteUid.indexOf("EMEA") != -1){
			document.leadTime=data.leadTime;
	        document.leadTime = document.leadTime +" Days";
	        document.getElementById("leadTimeIdEMEA").innerHTML = document.leadTime;
	      datePickerTextBox=$("#datepicker-2");
	      if(data.requestedDeliveryDate!=null && data.requestedDeliveryDate>0)
	        {
	    	  //alert("if .. lead time "+data.leadTime);
	    	  // alert("data.requestedDeliveryDate "+data.requestedDeliveryDate);
	          leadTimeDate=new Date(data.requestedDeliveryDate);
	          // alert("leadTimeDate "+leadTimeDate);
	          datePart=(leadTimeDate.getDate().length < 2)?'0'+leadTimeDate.getDate():leadTimeDate.getDate();
	          monthPart=(leadTimeDate.getMonth().length < 2)?'0'+(leadTimeDate.getMonth()+1):(leadTimeDate.getMonth()+1);
	          yearPart=leadTimeDate.getFullYear().toString();
	          
	          var siteUid = $('#siteUid').val();
	          if(siteUid.indexOf("EMEA") != -1){
	        	  if(datePart <= 9){
	            	  datePart = '0'+datePart;
	              }
	              if(monthPart <= 9 ){
	            	  monthPart = '0'+monthPart;
	              }
	              datePickerTextBox.val(datePart+"/"+monthPart+"/"+yearPart);
	              
	              var requestedDeliveryDateInFormat = yearPart+"-"+monthPart+"-"+datePart;
	              var requestedDeliveryDate = new Date(requestedDeliveryDateInFormat); 
	              
	              $('#requestedDeliveryDayEMEA').val(requestedDeliveryDate);
	              
	          } else {
	        	  datePickerTextBox.val(monthPart+"-"+datePart+"-"+yearPart);
	        	  
	        	  var requestedDeliveryDateInFormat = yearPart+"-"+monthPart+"-"+datePart;
	              var requestedDeliveryDate = new Date(requestedDeliveryDateInFormat); 
	              
	              $('#requestedDeliveryDayEMEA').val(requestedDeliveryDate);
	          }
	        }
	      else
	        {
	    	  //alert("else .. lead time "+data.leadTime);
	          if(document.leadTime!=null && document.leadTime>0)
	            {
	              leadTimeDate=new Date(new Date().getTime()+(86400000*document.leadTime));
	              datePart=(leadTimeDate.getDate().length < 2)?'0'+leadTimeDate.getDate():leadTimeDate.getDate();
	              monthPart=(leadTimeDate.getMonth().length < 2)?'0'+(leadTimeDate.getMonth()+1):(leadTimeDate.getMonth()+1);
	              yearPart=leadTimeDate.getFullYear().toString();
	              
	              var siteUid = $('#siteUid').val();
	              if(siteUid.indexOf("EMEA") != -1){
	            	  if(datePart <= 9){
	                	  datePart = '0'+datePart;
	                  }
	                  if(monthPart <= 9 ){
	                	  monthPart = '0'+monthPart;
	                  }
	                  datePickerTextBox.val(datePart+"/"+monthPart+"/"+yearPart);
	              } else {
	            	  datePickerTextBox.val(monthPart+"-"+datePart+"-"+yearPart);
	              }
	            }
	        }
	      
	        var reqDevdate=new Date(data.requestedDeliveryDate);
	        var curr_date = reqDevdate.getDate();
	        var curr_month = reqDevdate.getMonth();
	        var curr_year = reqDevdate.getFullYear();
	        var reqDeliverDate = curr_month+"/"+curr_date+"/"+curr_year;
	        document.assignLeadTimeToDatePicker();
	        prepopulatedDate = datePickerTextBox.val();
	        //alert()
	      //  ACC.checkoutB2B.refresh(data);
	        //$.colorbox.close();
		}
		// Set Requested Delivery Date only for EMEA in Checkout Page - END
		$.colorbox.close();
    },

    bindTermsAndConditionsLink: function() {
      $(document).on("click",'.termsAndConditionsLink', function(e) {
            
                  e.preventDefault();
                  $.colorbox({
                        href: getTermsAndConditionsUrl,
                        onComplete: function() {
                              ACC.common.refreshScreenReaderBuffer();
                        },
                        onClosed: function() {
                              ACC.common.refreshScreenReaderBuffer();
                        }
                  });
            });
      }
      
      
      
      
      
      
      
      
      
      

};

$(document).ready(function ()
{
      var prepopulatedDate = "";
      $('#leadTimeId').empty();
      $('#deliveryDateId').empty();
      if($("body").hasClass("page-cartPage"))
      {
            $('.checkoutButton').click(function (){
                  var checkoutUrl = $(this).data("checkoutUrl");
                  window.location = checkoutUrl;
                  
            });
      }
      
      
      
      if($("body").hasClass("page-singleStepCheckoutSummaryPage"))
      {
            with(ACC.checkoutB2B){
                  
                  
                  refresh();
                  settingDefaultCostCenter();
                  PaymentType();
                  costCenter();
                  payment();
                  deliveryAddress();
                  
                  
                  deliveryMode();
                  scheduleReplenishment();
                  negotiateQuote();
                  placeOrder();
                  bindTermsAndConditionsLink();
                  
            }
      }
});

document.assignLeadTimeToDatePicker = function() {
	
	var siteUid = $('#siteUid').val();
	// Set Requested Delivery Date only for EMEA in Checkout Page - START
	if(siteUid.indexOf("EMEA") != -1){
		if ($("#datepicker-2")) {
            $("#datepicker-2").datepicker("destroy");
      }
      
      var leadTime = document.leadTime;
      var siteUid = $('#siteUid').val();
      var firstDayOfWeek = 0;
      
      var requestedDeliveryDay = $('#requestedDeliveryDayEMEA').val();
	  var deliveryDate = new Date(requestedDeliveryDay);
	  leadTime = deliveryDate;
	  var displayDays = $.datepicker.noWeekends;
	  
      if(siteUid.indexOf("EMEA") != -1){
    	  firstDayOfWeek = 1;
      } else if(siteUid == 'personalCare'){
    	  firstDayOfWeek = 0;
      }  
      
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
                  document.getElementById("checkoutPlaceOrder").disabled = false;
                  document.getElementById("checkoutPlaceOrder2").disabled = false;
                  var deliveryDate = $('#datepicker-2').val();
                  
                  var siteUid = $('#siteUid').val();
                  if(siteUid.indexOf("EMEA") != -1){
                	  var dateArray = deliveryDate.split('-');
                      var dateInDDMMYYYY = dateArray[1] + '/' + dateArray[0] + '/' + dateArray[2];
                      document.getElementById("deliveryDateId").innerHTML = dateInDDMMYYYY;
                  } else{
                	  document.getElementById("deliveryDateId").innerHTML = deliveryDate;
                  }
                 
                  $.ajax({
                        type : "POST",
                        url : "/checkout/single/summary/setDeliveryDate.json",
                        data : {
                              deliveryDate : deliveryDate
                        },
                        success : function(data) {

                              ACC.checkoutB2B.refresh(data);   
                        }
                  });
            }
      });
	}
	// Set Requested Delivery Date only for EMEA in Checkout Page - END
};

$('#checkoutPlaceOrder').click(function(){
	     
      var poNumber = $("#PurchaseOrderNumber").val();
      var siteUid = $('#siteUid').val();
	  // Set Requested Delivery Date only for EMEA in Checkout Page - START
      var selectedDate = "";
	  if(siteUid.indexOf("EMEA") != -1){
		  selectedDate = $('#datepicker-2').val();    
	  }
      var poNumberPattern =new RegExp('^'+$('#poNumPatternId').val());
      if(poNumber && !new RegExp('^\\s{1,}$').test(poNumber) && poNumber!=''){
    	 if(poNumberPattern.test(poNumber)){
    		
           
                  if($(".summaryDeliveryAddress li").text() != "None selected"){
                	  if(siteUid.indexOf("EMEA") != -1){
                		  if(isDate(selectedDate)){
                         	 if($("#Terms1").prop('checked')){
                               var html = "";
                               $('#globalMessages').html(html);
                               $.postJSON(config.setPurchaseOrderNumberUrl, {purchaseOrderNumber: poNumber}, function(data){
                                   ACC.checkoutB2B.refresh(data);            
                             });
                         return true;
                         	 }
                         	 else{
                                  var html = "<div class='alert negative'>Please accept terms & conditions before submitting your order.</div>";
                                  $('#globalMessages').html(html);
                                  $("html, body").animate({ scrollTop: 0 }, 50);
                                  return false;
                            }//end
                         }else
                         {
                               
                               //var html = "<div class='alert negative'>Expected delivery date is not valid. Enter a valid date.</div>";
                        	   var html = "<div class='alert negative'>Expected delivery date is not valid. Please select a date from the date picker.</div>";
                               $('#globalMessages').html(html);
                               $("html, body").animate({ scrollTop: 0 }, 50);
                               return false;
                         }
                	  }
                  }
                  else{
                        var html = "<div class='alert negative'>Please select Delivery Address</div>";
                        $('#globalMessages').html(html);
                        $("html, body").animate({ scrollTop: 0 }, 50);
                        return false;
                  }
                  
            
            
    	  }else{
    		  var html = "<div class='alert negative'>Purchase Order Number is not valid.</div>";
              $('#globalMessages').html(html);
              $("html, body").animate({ scrollTop: 0 }, 50);
              return false; 
    	  }
      
      }else{
            
      
      var html = "<div class='alert negative'>Purchase Order Number is Mandatory</div>";
      $('#globalMessages').html(html);
      $("html, body").animate({ scrollTop: 0 }, 50);
      return false;
            
      }
                              
      });
$('#checkoutPlaceOrder2').click(function(){
    
      var poNumber = $("#PurchaseOrderNumber").val();
      
      var siteUid = $('#siteUid').val();
	  // Set Requested Delivery Date only for EMEA in Checkout Page - START
      var selectedDate = "";
	  if(siteUid.indexOf("EMEA") != -1){
		  selectedDate = $('#datepicker-2').val();    
	  }
	  
      var poNumberPattern =new RegExp('^'+$('#poNumPatternId').val());
      if(poNumber && !new RegExp('^\\s{1,}$').test(poNumber) && poNumber!=''){
    	  if(poNumberPattern.test(poNumber)){
           
                  if($(".summaryDeliveryAddress li").text() != "None selected"){
                	  if(siteUid.indexOf("EMEA") != -1){

                          if(isDate(selectedDate)){
                          	 if($("#Terms1").prop('checked')){
                                var html = "";
                                $('#globalMessages').html(html);
                                $.postJSON(config.setPurchaseOrderNumberUrl, {purchaseOrderNumber: poNumber}, function(data){
                                    ACC.checkoutB2B.refresh(data);            
                              });
                                return true;
                          	 }
                          	 else{
                                   var html = "<div class='alert negative'>Please accept terms & conditions before submitting your order.</div>";
                                   $('#globalMessages').html(html);
                                   $("html, body").animate({ scrollTop: 0 }, 50);
                                   return false;
                             }//end
                        
                          }else
                          {
                                
                                //var html = "<div class='alert negative'>Expected delivery date is not valid. Enter a valid date.</div>";
                        	  	var html = "<div class='alert negative'>Expected delivery date is not valid. Please select a date from the date picker.</div>";
                                $('#globalMessages').html(html);
                                $("html, body").animate({ scrollTop: 0 }, 50);
                                return false;
                          }
                      }
                  }
                  else{
                        var html = "<div class='alert negative'>Please select Delivery Address.</div>";
                        $('#globalMessages').html(html);
                        $("html, body").animate({ scrollTop: 0 }, 50);
                        return false;
                  }
       
           
    	  }else{
    		  var html = "<div class='alert negative'>Purchase Order Number is not valid.</div>";
              $('#globalMessages').html(html);
              $("html, body").animate({ scrollTop: 0 }, 50);
              return false; 
    	  }
      }else{
            
      
      var html = "<div class='alert negative'>Purchase Order Number is Mandatory</div>";
      $('#globalMessages').html(html);
      $("html, body").animate({ scrollTop: 0 }, 50);
      return false;
            
      }
                              
});
function isDate(txtDate)
{
   var currVal = txtDate;
   if(currVal == '')
       return false;

   var rxDatePattern = /^(\d{1,2})(\/|-)(\d{1,2})(\/|-)(\d{4})$/; //Declare Regex
   var dtArray = currVal.match(rxDatePattern); // 
   if (dtArray == null) 
       return false;

   //Checks for mm/dd/yyyy format.
  // Added for B2BEMEA - 215
	var siteUid = $('#siteUid').val();
	
	if(siteUid.indexOf("EMEA") != -1){
		dtMonth = parseInt(dtArray[3],10);
		dtDay= parseInt(dtArray[1],10);
		dtYear = parseInt(dtArray[5],10);
	} else {
		dtMonth = parseInt(dtArray[1],10);
		dtDay= parseInt(dtArray[3],10);
		dtYear = parseInt(dtArray[5],10);
	}

   if (dtMonth < 1 || dtMonth > 12) {
	  
       return false;}
   else if (dtDay < 1 || dtDay> 31) {
	  
       return false;}
   else if ((dtMonth==4 || dtMonth==6 || dtMonth==9 || dtMonth==11) && dtDay ==31) {
	  
       return false;}
   else if (dtMonth == 2) 
   {
       var isleap = (dtYear % 4 == 0 && (dtYear % 100 != 0 || dtYear % 400 == 0));
       if (dtDay> 29 || (dtDay ==29 && !isleap)) 
    	  
               return false;
   }  
   var preDtArray = prepopulatedDate.match(rxDatePattern);
   if(siteUid.indexOf("EMEA") != -1){
    	preDtMonth = parseInt(preDtArray[3],10);
        preDtDay = parseInt(preDtArray[1],10);
        preDtYear = parseInt(preDtArray[5],10);
    } else {
    	preDtMonth = parseInt(preDtArray[1],10);
        preDtDay = parseInt(preDtArray[3],10);
        preDtYear = parseInt(preDtArray[5],10);
    }
      if( dtMonth < preDtMonth){
    	  if(dtYear <= preDtYear){
    		  return false;
    	  }
      }
      if( dtDay < preDtDay){
    	  if(dtYear <= preDtYear && dtMonth <= preDtMonth){
    		  return false;
    	  }
      }
      if( dtYear < preDtYear){
    		  return false;
      }
   return true;
}
$('#Terms1').click(function(){
      if($("#Terms1").prop('checked')){
      $('#globalMessages').html("");
      }
});
$('#Terms2').click(function(){
      if($("#Terms2").prop('checked')){
      $('#globalMessages').html("");
      }
});

$(document).on("click",'#Terms1PlaceOrder,#Terms2PlaceOrder', function (e){
      if($(this).attr('checked')){
            $('#Terms1PlaceOrder,#Terms2PlaceOrder').attr('checked','checked')
      }else{
            $('#Terms1PlaceOrder,#Terms2PlaceOrder').removeAttr('checked')
      }
});
$('#simulatePlaceOrderId1').click(function(){
	 var siteUid = $('#siteUid').val();
     var selectedDate = $('#datepicker-2').val(); 
     if($('#isSalesRepLoggedIn').val() == "false"){
      if($("#Terms1PlaceOrder").prop('checked')){
    	  if(siteUid == 'personalCare'){
        	  if(!isDate(selectedDate)){
        		  var html = "<div class='alert negative'>Expected delivery date is not valid. Please select a date from the date picker.</div>";
                  $('#globalMessages').html(html);
                  $("html, body").animate({ scrollTop: 0 }, 50);
                  return false;
        	  } else {
        		  return true;
        	  }
          }
      }
      else{
            var html = "<div class='alert negative'>Please accept terms & conditions before submitting your order.</div>";
            $('#globalMessages').html(html);
            $("html, body").animate({ scrollTop: 0 }, 50);
            return false;
      }
     }else
    	 {
    	 if(siteUid == 'personalCare'){
       	  if(!isDate(selectedDate)){
       		  var html = "<div class='alert negative'>Expected delivery date is not valid. Please select a date from the date picker.</div>";
                 $('#globalMessages').html(html);
                 $("html, body").animate({ scrollTop: 0 }, 50);
                 return false;
       	  } else {
       		  return true;
       	  }
         }

    	 }
    	 
});
$('#simulatePlaceOrderId2').click(function(){
	 if($('#isSalesRepLoggedIn').val() == "false"){
      if($("#Terms1PlaceOrder").prop('checked')){
            return true;
      }
      else{
            var html = "<div class='alert negative'>Please accept terms & conditions before submitting your order.</div>";
            $('#globalMessages').html(html);
            $("html, body").animate({ scrollTop: 0 }, 50);
            return false;
      }
	 }
	 else
		 {
		 return true;
		 }
});

$('#orderComments').focusout(function(){
	$('#orderCommentsHiddenVar').val(this.value);
	//alert('Hidden variable value ::: ' + $('#orderCommentsHiddenVar').val());
});

/*-$("#orderComments").keyup(function(event) {
	var orderComment = $('#orderComments').val();
	if(orderComment !== null && orderComment !== '' && orderComment.length > 264){
		var html = "<div class='alert negative'>Order comments cannot exceed 264 characters.</div>";
	     $('#globalMessages').html(html);
	     $("html, body").animate({ scrollTop: 0 }, 50);
	     $("#checkoutPlaceOrder").attr("disabled", "disabled")
	      $("#checkoutPlaceOrder2").attr("disabled", "disabled")
	      console.log('OrderComment text lenght :: ' + orderComment.length);
	}else
		{
		 $('#globalMessages').html("");
		 $("#checkoutPlaceOrder").removeAttr("disabled")
		  $("#checkoutPlaceOrder2").removeAttr("disabled")
		  console.log('OrderComment text lenght :: ' + orderComment.length);
		}
	
});*/