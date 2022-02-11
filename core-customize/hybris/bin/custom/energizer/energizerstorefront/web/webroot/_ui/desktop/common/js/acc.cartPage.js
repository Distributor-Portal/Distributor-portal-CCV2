
function submitForm() {
    document.getElementById("form").submit();
}

function getPackingOptionChange(elem){
	
	var contHeight = elem.value;
	var packingOption = '${packingOptionList}';
	
	var packOptionList2 = ["1 SLIP SHEET AND 1 WOODEN BASE","2 SLIP SHEETS","2 WOODEN BASE"];	
	
		
	if(contHeight == '20FT'){
		$('#packingTypeForm option:contains("2 SLIP SHEETS")').remove();
		
	}
	else if(contHeight == '40FT'){
		/** $("#packingTypeForm").append('<option value="option6">option6</option>'); **/
		$("#packingTypeForm").empty();
		
		$.each(packOptionList2, function(val, text) {
			$("#packingTypeForm").append(
		        $('<option></option>').html(text)
		    );
		});
	}
}
function onUOMChange(prodId,selectedUOM)
{
      console.log("prodId in cartPage"+prodId);
      console.log('selectedUOM in cartpage'+selectedUOM);
      var receivedId = prodId.split("_");
      var productId = receivedId[1];
      var entryId = receivedId[2];
      $("#selectedUomCart_"+productId).val(selectedUOM);
      console.log("Product id is------"+productId);
      var $form = $('#getSelectedUomCartPage' + productId);
      $form.submit();
}

	
	



