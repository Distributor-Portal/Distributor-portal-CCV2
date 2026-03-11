ACC.autocomplete = {

	bindAll: function ()
	{
		this.bindSearchAutocomplete();
	},

	bindSearchAutocomplete: function ()
	{
		var $search = $("#search");
		var option  = $search.data("options");
		var cache   = {};

		if (option)
		{
            $search.autocomplete({
                minLength: option.minCharactersBeforeRequest,
                delay:     option.waitTimeBeforeRequest,
                appendTo:  ".siteSearch",
                source:    function(request, response) {

                    var term = request.term.toLowerCase();

                    if (term in cache) {
                        return response(cache[term]);
                    }

                    $.postJSON(option.autocompleteUrl, {term: request.term}, function(data) {
                        var autoSearchData = [];
                        if(data.suggestions != null){
                            $.each(data.suggestions, function (i, obj)
                            {
                                autoSearchData.push(
                                        {value: obj.term,
                                            url: ACC.config.contextPath + "/search?text=" + obj.term,
                                            type: "autoSuggestion"});
                            });
                        }
                        if(data.products != null){
                            $.each(data.products, function (i, obj)
                            {
                                                            
                                autoSearchData.push(
                                        {value: obj.name,
                                            code: obj.code,
                                            ean:obj.ean,
                                            desc: obj.description,
                                            manufacturer: obj.manufacturer,
                                            url: ACC.config.contextPath + obj.url,
                                            price: (obj.price!=null && obj.price.formattedValue!=undefined)?obj.price.formattedValue:undefined,
                                            type: "productResult",
                                            searchtext:term,
                                            image: (obj.images!=null && obj.images!=undefined)?obj.images[0].url:undefined});
                            });
                        }
                        cache[term] = autoSearchData;
                        return response(autoSearchData);
                    });
                },
                focus: function (event, ui)
                {
                    return false;
                },
                select: function (event, ui)
                {
                    window.location.href = ui.item.url;
                }
            }).data("autocomplete")._renderItem = function (ul, item)
            {
                if (item.type == "autoSuggestion")
                {
                    renderHtml = "<a href='?q=" + item.value + "' class='clearfix'>" + item.value + "</a>";
                    return $("<li class='suggestions'>")
                            .data("item.autocomplete", item)
                            .append(renderHtml)
                            .appendTo(ul);
                }
                if (item.type == "productResult")
                {
                    var renderHtml = "<a href='" + ACC.config.contextPath + item.url + "' class='product clearfix'>";
                    /* need to show the products with images */
                    if($('#isSalesRepLoggedIn').val() == "false")
                    {
	                    if (item.image != null && item.image != undefined)
	                    {
	                    	
	                        renderHtml += "<span class='thumb'><img src='" + item.image + "' /></span><span class='desc clearfix'>";
	                    }
                    }
                    if(item.value!=undefined && item.value!=null && item.price!=null && item.price!=undefined )
                    	{
                    	 renderHtml += "<span class='title'>" + item.value +
                         "</span><span class='price'>" + item.price + "</span></span>" +
                         "</a>";
                    	}
                    
                    if(item.value!=undefined && item.value!=null && (item.price==null || item.price==undefined ))
                	{
                    	
                    	if(item.searchtext != null && item.searchtext != undefined && item.ean != null && item.ean != undefined && item.ean.match(item.searchtext.replace(/[*]/g, '')) != null )
                    	{
                    		console.log('ean no');
                        	var searchText = item.searchtext;
                        	var eanNo = item.ean.split(item.searchtext.replace(/[*]/g, ''));
                    		if(item.code != null && item.code != undefined && item.code.match(item.searchtext.toUpperCase().replace(/[*]/g, '')) != null )
                    		{
                    			var code = item.code.split(item.searchtext.toUpperCase().replace(/[*]/g, ''));
                    			 renderHtml += "<span class='title'>" + code[0] +  "<span style='background-color: #F8FF84'>" + searchText.replace(/[*]/g, '') + "</span>" + code[1]+ " - " + eanNo[0] + "<span style='background-color: #F8FF84'>" + searchText.replace(/[*]/g, '') + "</span>" + eanNo[1] + " - " +item.value +
                                 "</span><span class='price'> </span></span>" +
                                 "</a>";
                    			
                    		}
                    		else
                    		{
                    		 renderHtml += "<span class='title'>" + item.code + " - " + eanNo[0] + "<span style='background-color: #F8FF84'>" + searchText.replace(/[*]/g, '') + "</span>" + eanNo[1] + " - " +item.value +
                             "</span><span class='price'> </span></span>" +
                             "</a>";
                    		}
                    	}
                    	else if(item.searchtext != null && item.searchtext != undefined && item.code != null && item.code != undefined && item.ean != null && item.ean != undefined && item.code.match(item.searchtext.toUpperCase().replace(/[*]/g, '')) != null)
                    	{
                    		console.log('code with ean');
                    		var searchText = item.searchtext;
                    		var code = item.code.split(item.searchtext.toUpperCase().replace(/[*]/g, ''));
                    		 renderHtml += "<span class='title'>" + code[0] +  "<span style='background-color: #F8FF84'>" + searchText.replace(/[*]/g, '') + "</span>" + code[1]+ " - "+ item.ean+ " - " +item.value +
                             "</span><span class='price'> </span></span>" +
                             "</a>";
                    	}
                    	else if(item.searchtext != null && item.searchtext != undefined && item.code != null && item.code != undefined && (item.ean == null || item.ean == undefined) && item.code.match(item.searchtext.toUpperCase().replace(/[*]/g, '')) != null)
                    	{
                    		console.log('code without ean ');
                    		var searchText = item.searchtext;
                    		var code = item.code.split(item.searchtext.toUpperCase().replace(/[*]/g, ''));
                    		 renderHtml += "<span class='title'>" + code[0] + "<span style='background-color: #F8FF84'>" + searchText.replace(/[*]/g, '') + "</span>"  + code[1] + " - " +item.value +
                             "</span><span class='price'> </span></span>" +
                             "</a>";
                    	}
                    	else if(item.searchtext != null && item.searchtext != undefined && item.value != null && item.value != undefined && item.value.toUpperCase().replace(/[*]/g, '').match(item.searchtext.toUpperCase().replace(/[*]/g, '')) != null)
                    	{
                    		var name =item.value.toUpperCase().replace(item.searchtext.toUpperCase().replace(/[*]/g, ''), "<span style='background-color: #F8FF84'>"+ item.searchtext.toUpperCase().replace(/[*]/g, '') +"</span>");
                    		if(item.ean == null || item.ean == undefined)
    						{
                    			renderHtml += "<span class='title'>" + item.code + " - " + name +"</span><span class='price'> </span></span>" + "</a>";
    						}
                    		else
                    		{
                    			renderHtml += "<span class='title'>" + item.code +" - "+item.ean + " - " + name +"</span><span class='price'> </span></span>" + "</a>";
                    		}
                    		
                    	}
                    	else
                    	{
                    		console.log('name');
                    		if(item.searchtext != null && item.searchtext != undefined && item.value != null && item.value != undefined)
                    		{
                    			var nameArray= item.searchtext.toUpperCase().replace(/[*]/g, '').split(' ');
                    			if(nameArray != null && nameArray != undefined && nameArray.length > 0)
                    			{
                    				var productName = null;
                    					for (var i=0; i< nameArray.length; i++)
                    					{
                    						if(item.value.toUpperCase().replace(/[*]/g, '').match(nameArray[i]) != null && productName == null)
                    						{
                    							if(item.ean == null || item.ean == undefined)
                        						{
                    							productName =item.value.toUpperCase().replace(nameArray[i], "<span style='background-color: #F8FF84'>"+ nameArray[i] +"</span>");
	                    							renderHtml += "<span class='title'>" + item.code + " - " + productName +
	                          	                     "</span><span class='price'> </span></span>" +
	                          	                     "</a>";
                        						}
                    							else
                    							{
                    								productName =item.value.toUpperCase().replace(nameArray[i], "<span style='background-color: #F8FF84'>"+ nameArray[i] +"</span>");
	                    							renderHtml += "<span class='title'>" + item.code + " - "+item.ean+ " - " + productName +
	                          	                     "</span><span class='price'> </span></span>" +
	                          	                     "</a>";
                    							}
                    							break; 
                    						}
                    						
                    					}
                    					if(productName == null)
                						{
                    						if(item.ean == null || item.ean == undefined)
                    						{
                                   			 renderHtml += "<span class='title'>"+ item.code + " - " +item.value +
                       	                     "</span><span class='price'> </span></span>" +
                       	                     "</a>";
                    						}
                    						else
                                   			{
                                   			 renderHtml += "<span class='title'>"+ item.code + " - "+item.ean+ " - " +item.value +
                       	                     "</span><span class='price'> </span></span>" +
                       	                     "</a>";
                                   			}
                							
                						}
                    			}
                    			
                    			
                    		}
                    		//OOTB functionality -Start
                    		/*if(item.ean == null || item.ean == undefined){
                    			 renderHtml += "<span class='title'>" + item.code + " - " +item.value +
        	                     "</span><span class='price'> </span></span>" +
        	                     "</a>";
                    		}
                    		else
                    			{
                    			 renderHtml += "<span class='title'>" + item.code + " - "+item.ean + " - " +item.value +
        	                     "</span><span class='price'> </span></span>" +
        	                     "</a>";
                    			}*/
                    		//OOTB functionality -End
                    	}
                	}
                   
                    return $("<li class='product'>").data("item.autocomplete", item).append(renderHtml).appendTo(ul);
                }
            };
		}
	}
};

$(document).ready(function ()
{
	ACC.autocomplete.bindAll();
});