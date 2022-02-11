<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="order" required="true" type="de.hybris.platform.commercefacades.order.data.AbstractOrderData" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<div class="orderBox" style="width: 746px; padding-bottom: 20px; height: auto;">  
	<div class="headline">     
		<spring:theme code="text.deliveryNotes" text="Delivery Notes"/>
		<c:if test="${not empty orderData.deliveryNoteFiles}" >
			<c:out value=" (" /> ${fn:length(orderData.deliveryNoteFiles)} <c:out value=")" />
		</c:if>	 
	</div>   
	<!-- Delivery Notes File Upload - START -->
	<c:if test="${siteUid eq 'personalCareEMEA'}">
		<div class="clearfix">
			<c:url value="/my-account/uploadDeliveryNotesFile" var="encodedUrl" scope="page" />
		    <form:form  method="POST" action="${encodedUrl}" enctype="multipart/form-data">
		   	   <input type="hidden" name="orderCode" id="orderCode" value="${orderData.code}" />
		       <div>
			       	<span style="float:left;position:relative;top:15px;margin-right:10px;">
			       		<spring:theme code="text.account.excelFileToUpload"/>  
			       	</span>
			       	<input style="background:none;width:220px;color:#000;overflow: hidden" type="file" class="button" name="file">  
		       </div> 
		       <div style="width:20px;float:left;margin-top: 2px;"> 
			       	<input type="submit" class="button"  value="<spring:theme code='text.account.click.upload'/>" > 
		       	</div>
		   	</form:form>   
		</div>
	</c:if> 
	<!-- Delivery Notes File Upload  - END -->
	<!-- Delivery Note Files Display - START -->
	<div class="clearfix" style="margin-top: 10px;">
		<c:choose>
			<c:when test="${not empty orderData.deliveryNoteFiles}">
				<c:forEach var="deliveryNoteFile" items="${orderData.deliveryNoteFiles}">
					<div class="displayDeliveryNote" id="${orderData.code}_${deliveryNoteFile.fileName}">  
						<p class="displayDeliveryNoteInner" onclick="javascript:openDeliveryNoteFile('${orderData.code}','${deliveryNoteFile.fileName}')" title="Click to download ${deliveryNoteFile.fileName}">${deliveryNoteFile.fileName}</p> 
						<c:if test="${siteUid eq 'personalCareEMEA'}">
							<a href="javascript:removeDeliveryNoteFile('${orderData.code}','${deliveryNoteFile.fileName}')">
								<img alt="Remove" title="Remove" src="/_ui/desktop/common/images/facet-remove.png">
							</a>
						</c:if>
					</div>  
				</c:forEach>
			</c:when>
			<c:otherwise>
				<div>
					<span style="color: red;">No Files to display, please upload one !</span>
				</div>
			</c:otherwise>
		</c:choose>
	</div>
	<!-- Delivery Note Files Display - END -->
</div>
