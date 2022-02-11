<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ attribute name="order" required="true"
	type="de.hybris.platform.commercefacades.order.data.AbstractOrderData"%>
<%@ attribute name="isOrderDetailsPage" type="java.lang.Boolean"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="product" tagdir="/WEB-INF/tags/desktop/product"%>
<%@ taglib prefix="theme" tagdir="/WEB-INF/tags/shared/theme"%>
<%@ taglib prefix="format" tagdir="/WEB-INF/tags/shared/format"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="orderList">
	<div class="headline"><spring:theme code="text.account.orderHistory.loadingDetails" /></div>
	<table class="orderListTable orderListApprovTbl-cls">
		
		<tbody>
		   
		    <tr>
		        <td>
		            <p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.containerType"/>&nbsp;:&nbsp;
		               <c:if test="${not empty order.containerHeight }">
		                 ${order.containerHeight}
		               </c:if>
		            </p>
		        </td>
		        <td>
		            <p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.fullPallets"/>&nbsp;:&nbsp;
		                <c:if test="${not empty order.totalPalletCount }">
		                  ${order.totalPalletCount}
		                </c:if>
		            </p>
		        </td>
		    </tr> 
		    
		     <tr>
		        <td>
		             <p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.containerPackingMaterial"/>&nbsp;:&nbsp;
		                 <c:if test="${not empty order.containerPackingType }">
		                    ${order.containerPackingType}
		                 </c:if>
		             </p>
		        </td>
		        <td>
		              <p class="reduce_space_cls"><spring:theme code="text.account.orderHistory.partialPallets"/>&nbsp;:&nbsp;
		                   <c:if test="${not empty order.partialPalletCount }">
		                     ${order.partialPalletCount}
		                   </c:if>
		              </p>
		        </td>
		    </tr>   
		
		
		</tbody>
	</table>
	<br/>
	<br/>
	
	<c:if test="${not empty order.palStackData}">
	<table style="height:200px;width:700px; position:relative;background-color: #ffd799;margin-left:25px;float:left;margin-right:100px;border-color:Black;border-collapse:collapse; table-layout:fixed; border :1px solid ;">
	   <tr style="height:100px;">
        <c:forEach items="${order.palStackData}" var="item">
           <td  style="border-right: 1px solid #000000;border-bottom: 1px solid #000000;">
              <c:forEach items="${item.value}" var="productList" varStatus="stat">
                <c:choose>
                   <c:when test="${stat.count eq 2 && not fn:containsIgnoreCase(productList, 'NA')}">
                      <div style="font-size:12px;color: black;transform: rotate(-90deg);white-space: nowrap;margin-top:30px;">
                          <spring:theme code="${productList}"/>
                       </div>
                   </c:when>
                  </c:choose>
               </c:forEach>
           </td>
         </c:forEach> 
                 <td style="border-bottom: 1px solid #000000;background-color: white; width:60px;border:0;">
                        &nbsp;<spring:theme code="palStack.secondStack"/>
                </td>
        </tr>  
              
         <tr style="height:100px;">
        <c:forEach items="${order.palStackData}" var="item">
           <td style="border-right: 1px solid #000000;border-bottom: 1px solid #000000;">
              <c:forEach items="${item.value}" var="productList" varStatus="stat">
                <c:choose>
                   <c:when test="${stat.count eq 1 && not fn:containsIgnoreCase(productList, 'NA')}">
                      <div style="font-size:12px;color: black;transform: rotate(-90deg);;white-space: nowrap;margin-top:30px;">
                          <spring:theme code="${productList}"/>
                       </div>
                    </c:when>
                  </c:choose>
               </c:forEach>
           </td>
         </c:forEach> 
                <td style="border-bottom: 1px solid #000000;background-color: white; width:60px;border:0;">
                      &nbsp;<spring:theme code="palStack.firstStack"/>
                </td>
        </tr>  
    </table>

    <table style="width:640px;table-layout: fixed; margin-left:25px;float:left;margin-right:100px;">
        <tr style="height:100%;">
          <c:forEach items="${order.palStackData}" var="item" varStatus="stat">
          <td style="text-align: center;">
              <div style="font-size:12px;white-space: nowrap;">
                          <spring:theme code="${stat.count}"/>
                </div>
                </td>
           </c:forEach>
        </tr>
    </table>
  
 
           <p  align="Center"><spring:theme code="basket.your.shopping.container.utilization.floorSpace"/><br/><br/>
	   </c:if>      
	
	<div align="left" style="font-size:11px;color: blue; ">
        <spring:theme code="text.account.orderHistory.loadingDetails.disclaimer"/>
    </div>  
</div>

   