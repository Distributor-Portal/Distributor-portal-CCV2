<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>

<c:if test="${cmsPageRequestContextData.liveEdit}">
	<div class="yCmsComponentEmpty">
		Empty ${component.itemtype}: ${component.name}
	</div>
</c:if>
