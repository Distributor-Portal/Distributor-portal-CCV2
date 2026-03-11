ACC.productDetail = {

	
	initPageEvents: function ()
	{
		
		
		$('.productImageGallery .jcarousel-skin').jcarousel({
			vertical: true
		});
		
		
		$(document).on("click","#imageLink, .productImageZoomLink",function(e){
			e.preventDefault();
			
			$.colorbox({
				href:$(this).attr("href"),
				height:555,
				onComplete: function() {
				    ACC.common.refreshScreenReaderBuffer();
					
					$('#colorbox .productImageGallery .jcarousel-skin').jcarousel({
						vertical: true
					});
					
				},
				onClosed: function() {
					ACC.common.refreshScreenReaderBuffer();
				}
			});
		});
		
		
		
		$(".productImageGallery img").click(function(e) {
			$(".productImagePrimary img").attr("src", $(this).attr("data-primaryimagesrc"));
			$("#zoomLink, #imageLink").attr("href",$("#zoomLink").attr("data-href")+ "?galleryPosition="+$(this).attr("data-galleryposition"));
			$(".productImageGallery .thumb").removeClass("active");
			$(this).parent(".thumb").addClass("active");
		});


		$(document).on("click","#colorbox .productImageGallery img",function(e) {
			$("#colorbox  .productImagePrimary img").attr("src", $(this).attr("data-zoomurl"));
			$("#colorbox .productImageGallery .thumb").removeClass("active");
			$(this).parent(".thumb").addClass("active");
		});
		
		
		
		$("body").on("keyup", "input[name=qtyInput]", function(event) {
  			var input = $(event.target);
		  	var value = input.val();
		  	var qty_css = 'input[name=qty]';
  			while(input.parent()[0] != document) {
 				input = input.parent();
 				if(input.find(qty_css).length > 0) {
  					input.find(qty_css).val(value);
  					return;
 				}
  			}
		});
		
	


		$("#Size").change(function () {
			var url = "";
			var selectedIndex = 0;
			$("#Size option:selected").each(function () {
				url = $(this).attr('value');
				selectedIndex = $(this).attr("index");
			});
			if (selectedIndex != 0) {
				window.location.href=url;
			}
		});

		$("#variant").change(function () {
			var url = "";
			var selectedIndex = 0;
		
			$("#variant option:selected").each(function () {
				url = $(this).attr('value');
				selectedIndex = $(this).attr("index");
			});
			if (selectedIndex != 0) {
				window.location.href=url;
			}
		});


		$(".selectPriority").change(function () {
			var url = "";
			var selectedIndex = 0;

			url = $(this).attr('value');
			selectedIndex = $(this).attr("index");

			if (selectedIndex != 0) {
				window.location.href=url;
			}
		});


		$(".addToCartButtonPDP").click(function(e){
			e.preventDefault();
			var dataCode=this.id;
			var errorMsg = $('#errorMsg').val();
			var emptyUomErrorMsg = $('#emptyUomErrorMsg').val();
			var siteId = $("#siteIdInPDP").val();
			if(siteId == 'personalCareEMEA')
			{
			                $('#addToCartForm').submit();
			}
			else 
			{

			$("#error"+dataCode).text('');
			if($("#uomSelector_"+dataCode).val() == 'Select' || $("#uomSelector_"+dataCode).val() == 'select' || $("#uomSelector_"+dataCode).val() =="" || $("#uomSelector_"+dataCode).val() == undefined){
				$("#error"+dataCode).css("color","red");
				if(emptyUomErrorMsg == undefined)
				{
					$("#error"+dataCode).text('* Please select an UOM to add the product to cart.');
				}
				else
				{
					$("#error"+dataCode).text(emptyUomErrorMsg);
				}
				return false;
			}
			// Set productCode & selectedUom in hidden parameters and serialize the form to pass it to the controller
			var $validateUOMForm = $('#selectedUomForm');
			$("#productCodePDP").val(dataCode);
			$("#selectedUomPDP").val($("#uomSelector_"+dataCode).val());
			$("#selectedUom_"+dataCode).val($("#uomSelector_"+dataCode).val());
			var validateUOMMethod = $validateUOMForm.attr("method") ? $validateUOMForm.attr("method").toUpperCase() : "GET";
			var validateUOMUrl = "/validateUOM";
			
			$.ajax({
			                url: validateUOMUrl,
			                data: $validateUOMForm.serialize(),
			                type: "POST",
			                success: function(data) {
			                	if(data == "Success"){	
			                		validatePBG(dataCode);
			                	}
			                	if(data == "Error"){
			                		$("#error"+dataCode).css("color","red");
			                		if(errorMsg == undefined)
			                		{
			                			$("#error"+dataCode).text("* Same product available in cart with different UOM data.");
			                		}
			                		else
			                		{
			                			$("#error"+dataCode).text(errorMsg);
			                		}
			                		
			                	}
			                }
			});
		}
		});
		
		validatePBG = function(dataCode){
			
			// Validate PBG (or) Non-PBG
			var $validatePBGForm = $('#validatePBGForm_'+dataCode);
			$("#productCode_"+dataCode).val(dataCode);
			$("#isPBGPDP_"+dataCode).val($("#isPBG_"+dataCode).val());
			var validatePBGMethod = $validatePBGForm.attr("method") ? $validatePBGForm.attr("method").toUpperCase() : "GET";
			var validatePBGUrl = "/validatePBG";
			
			$.ajax({
	            url: validatePBGUrl,
	            data: $validatePBGForm.serialize(),
	            type: "POST",
	            success: function(data) {
	            	if(data == "Success"){
	            		$('#addToCartForm').submit();
	            	}
	            	if(data == "Error"){
	            		$("#error"+dataCode).css("color","red");
	            		$("#error"+dataCode).text("* Cannot order Private Label and National Branded items on the same PO !");
	            	}
	            }
			});
		}
		
		$('.uomSelectorPDP').change(function(){
			var dropDownId=$(this).attr("id");
			$("#productCodePDP").val(dropDownId.split("_")[1]);
			$("#selectedUomPDP").val($(this).val());
			$("#error"+dropDownId.split("_")[1]).text('');
		});

	}


};

$(document).ready(function ()
{

	with(ACC.productDetail)
	{
		initPageEvents();
	}
});

