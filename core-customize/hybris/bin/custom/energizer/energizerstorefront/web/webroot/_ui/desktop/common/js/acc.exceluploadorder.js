
$(document).ready(function(){
	
	$(".excelOrderSubmitButton").attr("disabled",true);

	$(".shippingPointRadioButton").change(function(){
		
		if($(".shippingPointRadioButton:checked").length>0)
			{
			$(".excelOrderSubmitButton").attr("disabled",false);	
			}
		else
			{
				$(".excelOrderSubmitButton").attr("disabled",true);
			}		
	});
	
		$("input.excelOrderModifiedQuantity").blur(function(){
			
			 try{
				 qty=$(this).val();
				 if(qty != null && qty>=0)
					{
					 qty=parseInt(qty);
						$.ajax({
							url:'/my-cart/excelUpload/updateOrderQuantity',
							data:{'quantity':$(this).val(),'erpMaterialCode':$(this).prev().val()},
							success:function()
							{
							}
						})
					}
				 }
			 catch(err){

			 }			 
	});
	
});

