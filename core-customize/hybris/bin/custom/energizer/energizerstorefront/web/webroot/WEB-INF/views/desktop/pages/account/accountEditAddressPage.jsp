<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/desktop/nav" %>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/desktop/formElement"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common" %>
<%@ taglib prefix="breadcrumb" tagdir="/WEB-INF/tags/desktop/nav/breadcrumb" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>

<spring:url value="/my-account/address-book"
			var="cancelUrl">
	
</spring:url>

<template:page pageTitle="${pageTitle}">

	<div id="breadcrumb" class="breadcrumb">
		<breadcrumb:breadcrumb breadcrumbs="${breadcrumbs}"/>
	</div>

	<div id="globalMessages">
		<common:globalMessages/>
	</div>
	<nav:accountNav selected="address-book" />
	
	
	<div class="column accountContentPane clearfix">
		<div class="headline"><spring:theme code="text.account.addressBook.addressDetails" text="Address Details"/></div>
		<div class="required right"><spring:theme code="form.required" text="Fields marked * are required"/></div>
		<div class="description"><spring:theme code="text.account.addressBook.addEditform" text="Please use this form to add/edit an address."/></div>
		<form:form action="add-address" method="post" modelAttribute="addressForm">
			<form:hidden path="addressId"/>
			<!-- Commented below code and added new lines to fix alignment issues-->
			<!-- <div style="float:left;width:305px;margin-right:25px;">
			<formElement:formSelectBox idKey="address.title" labelKey="address.title" path="titleCode" mandatory="true" skipBlank="false" skipBlankMessageKey="address.title.pleaseSelect" items="${titleData}"  selectedValue="${addressForm.titleCode}"/>
			<formElement:formInputBox idKey="address.firstName" labelKey="address.firstName" path="firstName" inputCSS="text" mandatory="true"/>
			<formElement:formInputBox idKey="address.surname" labelKey="address.surname" path="lastName" inputCSS="text" mandatory="true"/>
			<formElement:formInputBox idKey="address.line1" labelKey="address.line1" path="line1" inputCSS="text" mandatory="true"/>
			</div>
			<div style="float:left;width:305px;">
			<formElement:formInputBox idKey="address.line2" labelKey="address.line2" path="line2" inputCSS="text" mandatory="false"/>
			<formElement:formInputBox idKey="address.townCity" labelKey="address.townCity" path="townCity" inputCSS="text" mandatory="true"/>
			<formElement:formInputBox idKey="address.postcode" labelKey="address.postcode" path="postcode" inputCSS="text" mandatory="true"/>
			<formElement:formSelectBox idKey="address.country" labelKey="address.country" path="countryIso" mandatory="true" skipBlank="false" skipBlankMessageKey="address.selectCountry" items="${countryData}" itemValue="isocode" selectedValue="${addressForm.countryIso}"/>
			</div>
				-->
				
			<table cellpadding="0" cellspacing="0" border="0" style="width:90%;">
				<tr>
					<td style="width:305px%;"><formElement:formSelectBox idKey="address.title" labelKey="address.title" path="titleCode" mandatory="true" skipBlank="false" skipBlankMessageKey="address.title.pleaseSelect" items="${titleData}"  selectedValue="${addressForm.titleCode}"/><td>
					<td><formElement:formInputBox idKey="address.firstName" labelKey="address.firstName" path="firstName" inputCSS="text" mandatory="true"/></td>
				</tr>
				<tr>
					<td style="width:305px%;"><formElement:formInputBox idKey="address.surname" labelKey="address.surname" path="lastName" inputCSS="text" mandatory="true"/><td>
					<td><formElement:formInputBox idKey="address.line1" labelKey="address.line1" path="line1" inputCSS="text" mandatory="true"/></td>
				</tr>
				<tr>
					<td style="width:305px%;"><formElement:formInputBox idKey="address.line2" labelKey="address.line2" path="line2" inputCSS="text" mandatory="false"/><td>
					<td><formElement:formInputBox idKey="address.townCity" labelKey="address.townCity" path="townCity" inputCSS="text" mandatory="true"/></td>
				</tr>
				<tr>
					<td style="width:305px%;"><formElement:formInputBox idKey="address.postcode" labelKey="address.postcode" path="postcode" inputCSS="text" mandatory="true"/><td>
					<td><formElement:formSelectBox idKey="address.country" labelKey="address.country" path="countryIso" mandatory="true" skipBlank="false" skipBlankMessageKey="address.selectCountry" items="${countryData}" itemValue="isocode" selectedValue="${addressForm.countryIso}"/></td>
				</tr>		
			</table>
			<c:if test="${not addressBookEmpty && not addressForm.defaultAddress}">
				<div class="form-additionals">
					<formElement:formCheckbox idKey="address.default" labelKey="address.default" path="defaultAddress" inputCSS="add-address-left-input" labelCSS="add-address-left-label" mandatory="false"/>
				</div>
			</c:if>
			
			<div id="addressform_button_panel" class="form-actions">
				<button class="form"><spring:theme code="text.account.addressBook.saveAddress" text="Save address"/></button>
			</div>
			<!-- Cancel button added as part of Account Management implementation-->
			
			<ycommerce:testId code="UnitAddress_Cancel_button">
						<a href="${cancelUrl}" class="button cancel"><spring:theme code="text.company.manage.unit.address.cancelButton" text="Cancel"/></a>
				</ycommerce:testId>
		</form:form>
	
	</div>
	

</template:page>

