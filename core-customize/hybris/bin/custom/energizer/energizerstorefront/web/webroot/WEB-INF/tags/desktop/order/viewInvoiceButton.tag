<!-- Content may get change When actual View Invoice functionality will Work -->
<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="orderData" required="true" type="de.hybris.platform.commercefacades.order.data.OrderData" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<script>
function openInvoicePDF(orderid){
window.open('/my-account/invoice/invoicePdfDisplay?orderCode=${orderData.code}&inline=true','invoicepdf','scrollbars=yes,width=500, height=900')
}
</script>

<c:choose>
	<c:when test="${siteUid eq 'personalCare'}">
		<spring:url value="/my-account/invoice/${orderData.code}?inline=true" var="viewinvoiceUrl" />
		<a class="positive right pad_right re-order invoicePdf"  
		style=" text-align:center ; background-color: #169e08;  padding-top:14px; width:95px; 
		border-color: #169e08; color: #fff; font-weight: bold; text-transform: uppercase; border-color: #000;  
		height: 26px;padding-left: 10px;padding-right: 10px;" href="javascript:openInvoicePDF('${orderData.code}')" >
		<spring:theme code="text.order.viewInvoice" text="viewInvoice"/></a>
	</c:when>
	<c:otherwise>
		<c:set var="inline" value="true" />
		<a class="positive right pad_right re-order invoicePdf"  
		style=" text-align:center ; background-color: #169e08;  padding-top:14px; width:95px; 
		border-color: #169e08; color: #fff; font-weight: bold; text-transform: uppercase; border-color: #000;  
		height: 26px;padding-left: 10px;padding-right: 10px; width: 143px !important;" href="javascript:openInvoicePDF('${orderData.code}')">
		<spring:theme code="text.order.downloadInvoice" text="Download Invoice"/></a>
	</c:otherwise>
</c:choose>

