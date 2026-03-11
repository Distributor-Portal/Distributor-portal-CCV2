ACC.minicart = {
	
	$layer:$('#miniCartLayer'),

	bindMiniCart: function ()
	{
		$(document).on('mouseenter', '.miniCart', this.showMiniCart);
		$(document).on('mouseleave', '.miniCart', this.hideMiniCart);
	},
	
	showMiniCart: function ()
	{

		if(ACC.minicart.$layer.data("hover"))
		{
			return;
		}
		
		if(ACC.minicart.$layer.data("needRefresh") != false){
			ACC.minicart.getMiniCartData(function(){
				ACC.minicart.$layer.fadeIn(function(){
					ACC.minicart.$layer.data("hover", true);
					ACC.minicart.$layer.data("needRefresh", false);
				});
			})
		}
		
		ACC.minicart.$layer.fadeIn(function(){
			ACC.minicart.$layer.data("hover", true);
		});
	},
	
	hideMiniCart: function ()
	{
		ACC.minicart.$layer.fadeOut(function(){
			ACC.minicart.$layer.data("hover", false);
		});
	},
	
	getMiniCartData : function(callback)
	{
		$.ajax({
			url: ACC.minicart.$layer.attr("data-rolloverPopupUrl"),
			cache: false,
			type: 'POST',
			success: function (result)
			{
				ACC.minicart.$layer.html(result);
				callback();
			}
		});	
	},

	refreshMiniCartCount : function()
	{
		$.ajax({
			dataType: "json",
			url: ACC.minicart.$layer.attr("data-refreshMiniCartUrl") + Math.floor(Math.random() * 101) * (new Date().getTime()),
			success: function (data)
			{
				
				$(".miniCart .count").html(data.miniCartCount);
				
				// Added for WeSell Implementation - Hide the minicart price during quantity update ONLY for Sales Rep
				if($('#isSalesRepLoggedIn').val() == 'true'){
					/*console.log('mini cart sales rep ');    
					var miniCartPrice = data.miniCartPrice;
					var miniCartCurrencyIso = data.currencyIso;
					var currencyIsoPriceValue = "";
					console.log('miniCartPrice :: ' + miniCartPrice + ' , miniCartCurrencyIso ::: ' + miniCartCurrencyIso);
					if(null != miniCartPrice && miniCartPrice.indexOf('$') != -1){ 
						currencyIsoPriceValue = miniCartCurrencyIso + (miniCartPrice.substring(1));
						console.log('currencyIsoPriceValue ::: ' + currencyIsoPriceValue);
						$(".miniCart .price").html(currencyIsoPriceValue);   
					} else {
						$(".miniCart .price").html(data.miniCartPrice);
					}*/
					//console.log('mini cart sales rep ');
					console.log($('#isSalesRepLoggedIn').val());
					$(".miniCart .price").hide();
				} else {
					console.log('mini cart non sales rep ');
					console.log($('#isSalesRepLoggedIn').val());
					$(".miniCart .price").html(data.miniCartPrice);
				}
				
				ACC.minicart.$layer.data("needRefresh", true);
			}
		});
	}
};

$(document).ready(function ()
{
	ACC.minicart.bindMiniCart();
});

