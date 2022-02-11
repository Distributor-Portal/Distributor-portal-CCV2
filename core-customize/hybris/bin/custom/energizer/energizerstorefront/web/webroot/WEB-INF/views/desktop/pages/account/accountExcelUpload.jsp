<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template" %>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme" %>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/desktop/nav" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="breadcrumb" tagdir="/WEB-INF/tags/desktop/nav/breadcrumb" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<template:page pageTitle="${pageTitle}">

	<div id="breadcrumb" class="breadcrumb">
		<breadcrumb:breadcrumb breadcrumbs="${breadcrumbs}"/>
	</div>
	<div id="globalMessages">
		<common:globalMessages/>
	</div>
	<nav:accountNav selected="excelfileupload" />

		<div class="column accountContentPane clearfix ">
		<div class="headline"><spring:theme code="text.account.excelFileUpload"/></div>
	
  	<c:url value="/my-cart/excelFileToUpload" var="encodedUrl"  scope="page" />
    <form:form  method="POST" action="${encodedUrl}" enctype="multipart/form-data">
   
       <div style="width:50%;float:left;">
	       	<span style="float:left;position:relative;top:15px;margin-right:10px;">
	       		<spring:theme code="text.account.excelFileToUpload"/>  
	       	</span>
	       	<input style="background:none;width:220px; color:#000" type="file" class="button" name="file">
       </div> 
       <div style="width:50%;float:left;"> 
	       	<input type="submit" class="button"  value="<spring:theme code='text.account.click.upload'/>" > 
	       	<span style="float:right;position:relative;top:15px;margin-right:10px;">
	       <%-- 	<spring:theme code="text.account.click.excelFileUpload"/>	  --%>  
	       	</span>
       	</div>
   	</form:form>	
</div>

</template:page>

