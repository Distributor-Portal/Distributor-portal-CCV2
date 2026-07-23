<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="nav" tagdir="/WEB-INF/tags/desktop/nav" %>

<c:if test="${navigationNode.visible}">
    <div class="accountNav">
        <div class="headline">
                ${navigationNode.title}
        </div>
        <ul class="facet_block indent">
            <c:forEach items="${navigationNode.links}" var="link">
                <c:set value="${ requestScope['jakarta.servlet.forward.servlet_path'] == link.url ? 'active':'' }" var="selected"/>
                <cms:component component="${link}" evaluateRestriction="true" element="li" class=" ${selected}"/>
            </c:forEach>
        </ul>
    </div>
</c:if>
