<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="cartData" required="true" type="de.hybris.platform.commercefacades.order.data.CartData" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
 
<div class="orderBox" style="width:910px;padding-bottom: 20px; height: auto;">       
	<div class="headline" style="font-size: 1.2em !important;">   
		<spring:theme code="text.deliveryNotes" text="Delivery Notes"/>
		<span class="noOfFilesDiv">
			<c:if test="${not empty cartData.deliveryNoteFiles}">
				(${fn:length(cartData.deliveryNoteFiles)}) 
			</c:if>	
		</span> 
	</div>
	<!-- Delivery Notes File Upload - START -->
	<div class="clearfix">
		<c:url value="/checkout/single/uploadDeliveryNotesFile" var="encodedUrl" scope="page" />
	    <form:form  method="POST" action="${encodedUrl}" enctype="multipart/form-data" id="uploadDeliveryNotesFileForm">
	       <div>
		       	<span style="float:left;position:relative;top:15px;margin-right:10px;">
		       		<spring:theme code="text.account.excelFileToUpload"/>  
		       	</span>
		       	<input style="background:none;width:250px; color:#000;overflow: hidden" type="file" class="button" name="file" id="file"> 
	       </div> 
	       <div style="width:20px;float:left;margin-top: 2px;"> 
		       	<input id="uploadDeliveryNotesFileButton" type="button" class="button" value="<spring:theme code='text.account.click.upload'/>" > 
	       	</div>
	       	 <input type="hidden" name="cartId" id="cartId" value="${cartData.code}" />    
	       	<input type="hidden" id="csrfToken" name="${_csrf.parameterName}" value="${_csrf.token}"/>
	       	<input type="hidden" id="escapeCSRFForDeliveryNotes" value="true" />
	   	</form:form>      
	</div>
	
	<!-- Delivery Notes File Upload  - END -->
	<!-- Delivery Note Files Display - START -->
	<c:set var="displayFlag" value="block" />
	<div class="clearfix deliveryNotesFilesDiv" style="margin-top: 10px;">
		<c:if test="${not empty cartData.deliveryNoteFiles}">
		<c:set var="displayFlag" value="none" />
			<c:forEach var="deliveryNoteFile" items="${cartData.deliveryNoteFiles}">
				<div class="displayDeliveryNote" id="${cartData.code}_${deliveryNoteFile.fileName}">  
					<%-- <p class="displayDeliveryNoteInner" onclick="openDeliveryNoteFile('${cartData.code}','${deliveryNoteFile.fileName}')" title="Download ${deliveryNoteFile.fileName}">${deliveryNoteFile.fileName}</p> --%>
					<%-- <p class="displayDeliveryNoteInner" id="openFile_${cartData.code}_${deliveryNoteFile.fileName}" title="Click to download ${deliveryNoteFile.fileName}">${deliveryNoteFile.fileName}</p> --%>
					<p class="displayDeliveryNoteInner"  style="text-decoration: none;" id="openFile_${cartData.code}_${deliveryNoteFile.fileName}">${deliveryNoteFile.fileName}</p>   
					<a class="removeDeliveryNoteFile" id="removeFile_${cartData.code}_${deliveryNoteFile.fileName}" style="cursor: pointer;"> 
						<img alt="Remove" title="Remove" src="/_ui/desktop/common/images/facet-remove.png">
					</a>  <!-- style="cursor: pointer;" -->  
				</div>    
			</c:forEach>
		</c:if> 
		
		<div id="noFilesToDisplayDiv" style="display:${displayFlag}">   
			<span id="noFilesToDisplay" style="color: red;">
				<spring:theme code="cart.delivery.notes.file.upload.no.file"/> 
			</span> 
		</div>
	</div>
	<!-- Delivery Note Files Display - END -->
</div>
