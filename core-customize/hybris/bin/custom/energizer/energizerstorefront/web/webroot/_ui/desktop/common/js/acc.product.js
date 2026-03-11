ACC.product = {
	
	initQuickviewLightbox:function(){
		
		this.bindToAddToCartForm();
	},
	
	
	enableAddToCartButton: function ()
	{
		$('#addToCartButton').removeClass("display_none");
		$('.addToCartButton').removeClass("display_none");
		
	},

	bindToAddToCartForm: function ()
	{
		var addToCartForm = $('.add_to_cart_form');
		addToCartForm.ajaxForm({url:addToCartForm.attr("action_data"),success: ACC.product.displayAddToCartPopup,error: ACC.product.displayAddToCartPopupError});

	},

	displayAddToCartPopupError: function ()
	{
		 $('#showErrorMessageForaddToCart').show();
		 $("html, body").animate({ scrollTop: 0 }, 50);
	},

	displayAddToCartPopup: function (cartResult, statusText, xhr, formElement)
	{
		
		ACC.common.$globalMessages.html(cartResult.cartGlobalMessagesHtml);
		
		var errLen =  cartResult.cartGlobalMessagesHtml;
		if(errLen.length > 0)		
		{
			$("html, body").animate({ scrollTop: 0 }, 50);
		}
		
		$('#addToCartLayer').remove();
		
		if (typeof ACC.minicart.refreshMiniCartCount == 'function')
		{
			ACC.minicart.refreshMiniCartCount();
		}
		
		$("#header").append(cartResult.addToCartLayer);
		

		$('#addToCartLayer').fadeIn(function(){
			$.colorbox.close();
			if (typeof timeoutId != 'undefined')
			{
				clearTimeout(timeoutId);
			}
			timeoutId = setTimeout(function ()
			{
				$('#addToCartLayer').fadeOut(function(){
			 	   $('#addToCartLayer').remove();
					
				});
			}, 5000);
			
		});
		
		var productCode = $('[name=productCodePost]', formElement).val();
		var quantityField = $('[name=qty]', formElement).val();

		var quantity = 1;
		if (quantityField != undefined)
		{
			quantity = quantityField;
		}

		ACC.track.trackAddToCart(productCode, quantity, cartResult.cartData);
		
		// if it is orderForm, disable add to cart button in the end
		if($('#orderFormAddToCart').length > 0) {
			$('#addToCartBtn').attr('disabled','disabled');
		}
	}

};

$(document).ready(function ()
{
	with(ACC.product)
	{
		bindToAddToCartForm();
		enableAddToCartButton();
	}
});

