ACC.productlisting = {

	infiniteScrollingConfig: {offset: '100%'},
	currentPage:             0,
	numberOfPages:           Number.MAX_VALUE,
	showMoreResultsArea:    $('#footer'),
	baseQuery:               $("#resultsList").attr("data-current-query")   || "",
	isOrderForm:             $("#resultsList").attr("data-isOrderForm")      || false,
	isOnlyProductIds:        $("#resultsList").attr("data-isOnlyProductIds") || false,
	searchPath:			 	 $("#resultsList").attr("data-current-path"),
	isCreateOrderForm:		 $("#isCreateOrderForm").val() || false,
	lastDataHtml:			 "",
	inCall: false,

	triggerLoadMoreResults: function() {
		if (ACC.productlisting.currentPage < ACC.productlisting.numberOfPages) {
			// show the page loader
			ACC.common.showSpinnerById('spinner');
			ACC.productlisting.loadMoreResults(parseInt(ACC.productlisting.currentPage) + 1);
		}
	},

	scrollingHandler: function(direction) {
		if (direction === "down") {
			/* ACC.productlisting.triggerLoadMoreResults(); */
		}
	},

	loadMoreResults: function(page) {
		skuIndex = "0";

		if ($("#skuIndexSavedValue").attr("data-sku-index") !== undefined) {
			skuIndex = $("#skuIndexSavedValue").attr("data-sku-index");
		}
		
		searchResultType = $("[name='searchResultType']:radio:checked").val() || "";

		if(ACC.productlisting.inCall == false) {
			ACC.productlisting.inCall = true;
		
			$.ajax({
				url: ACC.productlisting.searchPath + "/results?q=" + ACC.productlisting.baseQuery + "&page=" + page + "&isOnlyProductIds=" + ACC.productlisting.isOnlyProductIds +
					"&isOrderForm=" + ACC.productlisting.isOrderForm + "&skuIndex=" + skuIndex + "&isCreateOrderForm=" + ACC.productlisting.isCreateOrderForm + "&searchResultType=" + searchResultType,
					success: function (data) {
						if (data.pagination !== undefined) {
							if ($("#resultsList").length > 0 && ACC.productlisting.lastDataHtml !== data.productListerHtml) { //Product List Page
								ACC.productlisting.lastDataHtml = data.productListerHtml;
								$("#resultsList").append(data.productListerHtml);
								// rebind the add-to-cart ajaxForms
								ACC.product.bindToAddToCartForm({enforce: true});
								if ($("#skuIndexSavedValue").attr("data-sku-index") !== undefined){
									$("#skuIndexSavedValue").attr("data-sku-index", data.skuIndex);
								}
							}

							ACC.productlisting.updatePaginationInfos(data.pagination);
							ACC.common.hideSpinnerById('spinner');
							ACC.productlisting.showMoreResultsArea.waypoint(ACC.productlisting.infiniteScrollingConfig); // reconfigure waypoint eventhandler
						}
						else {
							ACC.common.hideSpinner();
						}
						ACC.productlisting.inCall = false;
					},
					error: function (request, status, error) {
						alert(error);
						ACC.productlisting.inCall = false;
					}
			});
		}
	},

	updatePaginationInfos: function(paginationInfo) {
		ACC.productlisting.currentPage   = parseInt(paginationInfo.currentPage);
		ACC.productlisting.numberOfPages = parseInt(paginationInfo.numberOfPages);
	},

	bindShowMoreResults: function(showMoreResultsArea) {
		showMoreResultsArea.live("click", function() {
			ACC.productlisting.triggerLoadMoreResults();
		});

		showMoreResultsArea.waypoint(ACC.productlisting.scrollingHandler,
									 ACC.productlisting.infiniteScrollingConfig);
	},

	bindSortingSelector: function() {
		$('#sort_form1, #sort_form2').change(function() {
			this.submit();
		});
		$(".addToCartButtonPLP").click(function(e){
			e.preventDefault();
			var dataCode=this.id;
			var siteId = $("#siteIdInPLP").val();
			var errorMsg = $('#errorMsg').val();
			var emptyUomErrorMsg = $('#emptyUomErrorMsg').val();
			if(siteId == 'personalCareEMEA')
			{
				$('#addToCartForm'+dataCode).submit();
			}
			else
			{

				$("#error"+dataCode).text('');
				if($("#uomSelector_"+dataCode).val() == 'Select' || $("#uomSelector_"+dataCode).val() =="" || $("#uomSelector_"+dataCode).val() == undefined){
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
				// Validate UOM
				$("#productCodePLP_"+dataCode).val(dataCode);
				var uom = $("#uomSelector_"+dataCode).val();
				$("#selectedUomPLP_"+dataCode).val(uom);
				$("#selectedUom_"+dataCode).val(uom);
				//console.log('dataCode ::: ' + dataCode + ' , uom ::: ' + uom);
				$.ajax({
				                url: '/validateUOM',
				                data : {'productCode' : dataCode, 'uom' : uom},
				                type: 'POST',    
				                success: function(data) {
				                	if(data=="Success"){	
				                		validatePBG(dataCode);
				                	}
				                	if(data=="Error"){
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
			$("#isPBGPLP_"+dataCode).val($("#isPBG_"+dataCode).val());
			var validatePBGMethod = $validatePBGForm.attr("method") ? $validatePBGForm.attr("method").toUpperCase() : "GET";
			var validatePBGUrl = "/validatePBG";
			
			$.ajax({
	            url: validatePBGUrl,
	            data: $validatePBGForm.serialize(),
	            type: 'POST',
	            success: function(data) {
	            	if(data=="Success"){
	            		$('#addToCartForm'+dataCode).submit();
	            	}
	            	if(data=="Error"){
	            		$("#error"+dataCode).css("color","red");
	            		$("#error"+dataCode).text("* Cannot order Private Label and National Branded items on the same PO !");
	            	}
	            }
			});
		}
		
		$('.uomSelector').change(function(){
			var dropDownId=$(this).attr("id");
			$("#productCodePLP").val(dropDownId.split("_")[1]);
			$("#selectedUomPLP").val($(this).val());
			$("#error"+dropDownId.split("_")[1]).text('');
		});
		
	},

	initialize: function() {
		with(ACC.productlisting) {
			bindShowMoreResults(showMoreResultsArea);
			bindSortingSelector();
		}
	}
};

$(document).ready(function() {
	ACC.productlisting.initialize();
});