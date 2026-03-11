<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/desktop/template"%>
<%@ taglib prefix="formElement"
	tagdir="/WEB-INF/tags/desktop/formElement"%>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="common" tagdir="/WEB-INF/tags/desktop/common"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<cms:pageSlot position="SideContent" var="feature" element="div"
	class="span-4 side-content-slot cms_disp-img_slot">
	<cms:component component="${feature}" />
</cms:pageSlot>

<spring:url value="/login" var="cancelUrl" />

<div class="span-20 last">
	<div class="item_container_holder">
	
		<c:if test="${ empty forgotPwdConfMsgs}">
			<div class="title_holder">
				<h2>
					<spring:theme code="forgottenPwd.title" />
				</h2>
			</div>
			<div class="item_container">
				<p>
					<spring:theme code="forgottenPwd.description" />
				</p>
				<p class="required">
					<spring:theme code="form.required" />
				</p>
				<form:form method="post" modelAttribute="forgottenPwdForm">
					<div class="form_field-elements">
						<div class="form_field-input">
							<formElement:formInputBox idKey="forgottenPwd.email"
								labelKey="forgottenPwd.email" path="email" inputCSS="text"
								mandatory="true" />
								
								     <%-- <formElement:formSelectBox skipBlank="false" skipBlankMessageKey="form.select.empty" idKey="profile.passwordQuestion" labelKey="profile.passwordQuestion" path="passwordQuestion" mandatory="true" items="${passwordQuestionsList}"/>
			  		   
			  						 <formElement:formInputBox idKey="profile.passwordAnswer" labelKey="profile.passwordAnswer" path="passwordAnswer" inputCSS="text" mandatory="true"/> --%>
			  						 <c:if test="${hasError == true}">
			  						 	
			  						 </c:if>
			  						 <c:choose>
			  						 	<c:when test="${hasError == true}">
			  						 		<div style="padding-top: 10px;">
			  						 	</c:when>
			  						 	<c:otherwise>
			  						 		<div style="padding-top: 45px;">
			  						 	</c:otherwise>
			  						 </c:choose>
			  						 
											<button class="positive" type="submit">
												<spring:theme code="forgottenPwd.submit" />
											</button>
										<ycommerce:testId code="User_Cancel_button">
											<a href="${cancelUrl}" class="button forgotpasswordcancel"><spring:theme
													code="b2bcustomer.cancel" text="Cancel" /></a>
										</ycommerce:testId>
								</div>
						</div>
					</div>
				</form:form>
			</div>
		</c:if>
		
		
	</div>
</div>