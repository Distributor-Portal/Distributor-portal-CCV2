<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>

<c:set value="${component.styleClass} ${dropDownLayout}"
	var="bannerClasses" />
<%-- 
<li
	class="La ${bannerClasses} <c:if test="${not empty component.navigationNode.children}"> parent</c:if>">
	<cms:component component="${component.link}" evaluateRestriction="true" />
	<c:if test="${not empty component.navigationNode.children}">
		<ul class="Lb">
			<c:forEach items="${component.navigationNode.children}" var="child">
				<c:if test="${child.visible}">
					<li class="Lb"><span class="nav-submenu-title">${child.title}</span>
						<c:forEach items="${child.links}" step="${component.wrapAfter}"
							varStatus="i">
							<ul class="Lc ${i.count < 2 ? 'left_col' : 'right_col'}">
								<c:forEach items="${child.links}" var="childlink"
									begin="${i.index}" end="${i.index + component.wrapAfter - 1}">
									<cms:component component="${childlink}"
										evaluateRestriction="true" element="li"
										class="Lc ${i.count < 2 ? 'left_col' : 'right_col'}" />
								</c:forEach>
							</ul>
						</c:forEach>
						</li>
				</c:if>
			</c:forEach>
		</ul>
	</c:if>
</li> 
--%>


	
	<li>
		<cms:component component="${component.link}" evaluateRestriction="true" />
		<c:if test="${not empty component.navigationNode.children}">
		<!--   2nd Level category Start -->
			<ul>
				<c:forEach items="${component.navigationNode.children}" var="child" >
					<c:if test="${child.visible}">
						<li>
							<c:forEach items="${child.links}" step="${component.wrapAfter}" varStatus="i">
								
									<c:forEach items="${child.links}" var="childlink" begin="${i.index}" end="${i.index + component.wrapAfter - 1}">
										<a href="${childlink.url }">														
											<c:set var="arr"  value="${fn:split(childlink.url, '-')}" />
											<c:out value="${ arr[1]}"/>
										</a>
									</c:forEach>
								
							</c:forEach>						
							<!--   Third Level category Start -->
							<ul>
								<c:forEach items="${child.children}" var="child1" >
									<c:if test="${child1.visible}">
										<li>
											<c:forEach items="${child1.links}" step="${component.wrapAfter}" varStatus="j">											
												<c:forEach items="${child1.links}" var="childlink1" begin="${j.index}" end="${j.index + component.wrapAfter - 1}">
													<c:set var="currentUrl" value="${childlink1.url }"/>
													<a href="${currentUrl }">
														<c:set var="arr1"  value="${fn:split(currentUrl, '-')}" />
														<c:choose>
															<c:when test="${fn:contains(arr1[2], '|')}">
																<c:set var="linkName" value="${fn:replace(arr1[2], '|', '/')}" /> 
																<c:out value="${linkName}"/>
															</c:when>
															<c:otherwise>
																<c:out value="${arr1[2]}"/>
															</c:otherwise>
														</c:choose>
													</a>  
												</c:forEach>
											</c:forEach>								
										</li>   
									</c:if>
								</c:forEach>	
							</ul>
							<!--   Third Level category end  -->
						</li>
					</c:if>
					
				</c:forEach>
			</ul>
			<!--   2nd Level category End -->
		</c:if>
	</li>
	
