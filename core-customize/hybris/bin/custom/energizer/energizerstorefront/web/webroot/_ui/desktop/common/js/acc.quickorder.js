ACC.quickorder = {
		
	bindValidation:function()
	{
		
		$("input.orderEntryQuantity").change(function(){
				qty=$(this).val();
			try
			{
				if(qty.match('\\d\\.{1,}')!=null)
					{
						//$(this).parent().children("span").text(enterWholeNumber).removeClass("display_none");
						ACC.quickorder.addErrorMsg($(this).attr("id"),enterWholeNumber);
						$(this).addClass("red_border_input");
						ACC.quickorder.enableOrDisableCheckoutButton();
						return;
					}
				qty=parseInt(qty);
				if(qty<=0)
					{
						//$(this).parent().children("span").text(enterPositiveNumber).removeClass("display_none");
						ACC.quickorder.addErrorMsg($(this).attr("id"),enterPositiveNumber);
						$(this).addClass("red_border_input");
						ACC.quickorder.enableOrDisableCheckoutButton();
						return;
					}
				else
					{
						//$(this).parent().children("span").text("").addClass("display_none");
						ACC.quickorder.removeErrorMsg($(this).attr("id"));
						$(this).removeClass("red_border_input");
						ACC.quickorder.enableOrDisableCheckoutButton();
					}
				if(ACC.quickorder.isValidUOM(qty,parseInt($(this).next("input").val())))
					{
						//$(this).parent().children("span").text("").addClass("display_none");
						ACC.quickorder.removeErrorMsg($(this).attr("id"));
						$(this).removeClass("red_border_input");
						ACC.quickorder.updateOrderedQty($(this).val(),$(this).prev().val())
					}
				else
					{
						//$(this).parent().children("span").text(enterMOQMultiples+" - "+$(this).next("input").val()).removeClass("display_none");
						ACC.quickorder.addErrorMsg($(this).attr("id"),enterMOQMultiples+" - "+$(this).next("input").val());
						$(this).addClass("red_border_input");
						ACC.quickorder.enableOrDisableCheckoutButton();
					}
			}
			catch(err)
			{
				//$(this).parent().children("span").text(inputValidNumber).removeClass("display_none");
				ACC.quickorder.addErrorMsg($(this).attr("id"),inputValidNumber);
				$(this).addClass("red_border_input");
				ACC.quickorder.enableOrDisableCheckoutButton();
			}
			
			
		});
	},

	isValidUOM:function(val,uom)
	{
		if(val==uom || val%uom==0)
			{
				return true;
			}
		else
			{
				return false;
			}
		
	},
	enableOrDisableCheckoutButton:function()
	{
		if($(".red_border_input").length==0)
			{
			$(".quickOrderSubmitButton").attr("disabled",false);
			}
		else
			{
			$(".quickOrderSubmitButton").attr("disabled",true);
			}
		
	},
	
	updateOrderedQty:function(qty,productCode)
	{
		$.ajax({
			url:'/my-account/quickorder/qtyAjaxUpdate',
			data:{'qty':qty,'productCode':productCode},
			
			success:function()
			{
				ACC.quickorder.enableOrDisableCheckoutButton();
			}
		})
		
	},
	
	addErrorMsg:function(productCode,msg)
	{
		if($("div.alert[id='"+productCode+"']").length==0)
			{
			div=document.createElement("div");
			$(div).attr("id",productCode+"msg").text(msg).addClass("alert").addClass("negative");
			$("#globalMessages").append(div);
			$("html, body").animate({ scrollTop: 0 }, 50);
			}
		else
			{
			$("div.alert[id='"+productCode+"msg']").text(msg);
			}
		
		
	},
	
	removeErrorMsg:function(productCode)
	{
		$("div.alert[id='"+productCode+"msg']").remove();
		
	}
};


$(document).ready(function(){
	
	if($("body.page-quickorderpage"))
		{
		 ACC.quickorder.bindValidation();
		}
	
	
});
