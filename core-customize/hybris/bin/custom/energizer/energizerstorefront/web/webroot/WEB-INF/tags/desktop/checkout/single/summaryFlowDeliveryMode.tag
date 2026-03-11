<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="deliveryMode" required="true" type="de.hybris.platform.commercefacades.order.data.DeliveryModeData" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
 <jsp:useBean id="DateTimeUtil"  class="com.energizer.storefront.util.EnergizerDateTimeUtil" />  

<spring:url value="/checkout/single/summary/getDeliveryModes.json" var="getDeliveryModesUrl"/>
<spring:url value="/checkout/single/summary/setDeliveryMode.json" var="setDeliveryModeUrl"/>


 <link href="${commonResourcePath}/css/cal.css" rel="stylesheet">
 <script src="${commonResourcePath}/js/jquery-1.7.2.min.js"></script>
 <script src="${commonResourcePath}/js/jquery-ui.js"></script>

      <!-- here create code for calendar -->
      <script>
      </script>
<c:set value="${not empty deliveryMode}" var="deliveryModeOk"/>

<c:choose>
	<c:when test="${siteUid eq 'personalCare'}">
		<c:set var="summaryDeliveryAddressClass" value="summaryDeliveryAddressLATAM" />
		<c:set var="summarySectionClass" value="summarySectionLATAM" />
		<c:set var="orderCommentsClass" value="orderCommentsLATAM" />
	</c:when>
	<c:otherwise>
		<c:set var="summaryDeliveryAddressClass" value="" />
		<c:set var="summarySectionClass" value="" />
		<c:set var="orderCommentsClass" value="" />
	</c:otherwise>
</c:choose>

<div class="summaryDeliveryMode summarySection ${summarySectionClass} ${deliveryModeOk ? 'complete' : ''}"   data-get="${getDeliveryModesUrl}" data-set="${setDeliveryModeUrl}" >

	<script id="deliveryModeSummaryTemplate" class="sectionTemplate" type="text/x-jquery-tmpl">
 
		<div class="headline"><span class="number">3</span><spring:theme code="checkout.summary.deliveryMode.header" htmlEscape="false"/></div>

			<ul id="checkout_summary_deliverymode_ul">
				{{if data.deliveryMode}}
					<li>{{= data.deliveryMode.name}} ({{= data.deliveryMode.code}})</li>
					<li>{{= data.deliveryMode.description}} - {{= data.deliveryMode.deliveryCost.formattedValue}}</li>
				{{else}}
					<li><spring:theme code="checkout.summary.deliveryMode.noneSelected"/></li>
				{{/if}}
			</ul>
		<button type="button" class="positive editButton"><spring:theme code="checkout.summary.deliveryMode.editDeliveryMethod"/></button>

	</script>

	<script id="deliveryModesTemplate" class="colorboxTemplate" type="text/x-jquery-tmpl">
 
		<div class="headline"><spring:theme code="checkout.summary.deliveryMode.selectDeliveryMethod"/></div>
		<div class="description"><spring:theme code="checkout.summary.deliveryMode.selectDeliveryMethodForOrder"/></div>

		<form>
			<ycommerce:testId code="checkout_deliveryModes">
				<div class="content">
			 		{{if data}}
			 			<ul id="delivery_modes_ul">
			 			{{each data}}
			 				<li>
			 					<label for='{{= code}}'>
								{{if code == selectedCode}}
			 						<input type="radio" name="delivery" value="{{= code}}"  id="{{= code}}" checked="checked" />
								{{else}}
									<input type="radio" name="delivery" value="{{= code}}"  id="{{= code}}" />
								{{/if}}
								
			 					
			 					{{= name}} - {{= description}} - {{= deliveryCost.formattedValue}}</label>
			 				</li>
			 			{{/each}}
			 			</ul>
					{{else}}
						<spring:theme code="text.checkout.noDeliveryModes"/>
			 		{{/if}}
					
				</div>
			</ycommerce:testId>
			<div class="form-actions" id="delivery_modes_button">
				{{if data}}
					<ycommerce:testId code="checkout_chooseSelectedDeliveryMethod">
						<button class="positive saveButton" ><spring:theme code="checkout.summary.deliveryMode.useThisDeliveryMethod"/></button>
					</ycommerce:testId>
				{{/if}}
			</div>
		</form>
	</script>
	
	<div class="headline"></span><spring:theme code="checkout.summary.deliveryDate.header" htmlEscape="false"/><span class="mandatory"><img width="5" height="6" alt="Required" title="Required" src="/_ui/desktop/common/images/mandatory.gif"></span></div>
	<spring:theme code="basket.your.shopping.estimatedleadtime"/> <%-- <div  id="leadTimeId">  ${cartData.leadTime} </br>  </div> --%>
	<div  id="leadTimeIdEMEA">  ${cartData.leadTime} </br>  </div>
	<!-- <input type="hidden" value="" id="requestedDeliveryDay" /> -->
	<input type="hidden" value="" id="requestedDeliveryDayEMEA" />
	<c:if test="${not empty cartData.requestedDeliveryDate}" >
		<input type="hidden" value="${DateTimeUtil.getTimeInMilliSeconds(cartData.requestedDeliveryDate)}" id="initialRequestedDeliveryDateEMEA" />
	</c:if>
	<spring:theme code="product.product.details.future.date"/>
	
	<input type="hidden" id="siteUid" value="${siteUid}" />
	
	<div id="deliveryDateId">
	<c:choose>
		<c:when test="${siteUid eq 'personalCare'}">
			<c:if test="${not empty cartData.requestedDeliveryDate }">
				${DateTimeUtil.displayDate(cartData.requestedDeliveryDate)}
			</c:if>
		</c:when>
		<c:otherwise>
			<c:if test="${not empty cartData.requestedDeliveryDate }">
				${DateTimeUtil.displayRequestedDeliveryDate(cartData.requestedDeliveryDate,'dd/MM/yyyy')}
			</c:if>
		</c:otherwise>
	</c:choose>
	
	</div>     
	<!-- <div class="contentSection"></div> -->
	<!-- <input type="text" id="datepicker-2"  placeholder="mm-dd-yyyy" style="width: 152px;margin-top: 10px;"/> -->
	<input type="text" id="datepicker-2"  placeholder="dd-mm-yyyy" readonly="readonly" style="width: 152px;margin-top: 10px;"/>
	
	<%-- <div style="display:none">
		<div id="summaryDeliveryModeOverlay" class="summaryOverlay ">
			<div class="headline"><spring:theme code="checkout.summary.deliveryMode.selectDeliveryMethod"/></div>
			<div class="description"><spring:theme code="checkout.summary.deliveryMode.selectDeliveryMethodForOrder"/></div>
		
			<form>
				<ycommerce:testId code="checkout_deliveryModes">
					<div class="content">
						available delivery modes for the cart
					</div>
				</ycommerce:testId>
				<div class="form-actions" id="delivery_modes_button">
					delivery mode select button
				</div>
			</form>
		
		</div>
	</div> --%>
</div>
