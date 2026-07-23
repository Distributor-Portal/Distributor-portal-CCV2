<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>


<cms:pageSlot position="Footer" var="feature" element="div" class="footer">
	<cms:component component="${feature}"/>
</cms:pageSlot>
