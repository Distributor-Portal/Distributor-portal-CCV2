<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>

<div id="nav_main" class="nav_main">
	<cms:pageSlot position="NavigationBar" var="component" element="ul" class="clear_fix">
		<cms:component component="${component}"/>
	</cms:pageSlot>
</div>