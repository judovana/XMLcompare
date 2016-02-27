<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- WARNING if this code is formated, links (to images or so on) can stop working, because triming of spaces is browser(xslt processor) dependent-->
  <xsl:param name="city"/>
  <xsl:param name="key"/>
  <xsl:param name="day"/>
  <xsl:param name="cityPart"/>
  <xsl:template match="/">
    <!--<html>
      <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                    
   </head>
      <body>-->
          <div id="sheetBackground">
         <h2 id="titleHeaderHeader" >
          <a id="titleHeader" href="../index.html" align="center" >Služby kolem Vás</a>
        </h2>
    
        <h3 id="cityTitle">
        <span id="cityTitleCityValue">
          <xsl:value-of select="$city"/>
        </span>  
          <xsl:if test="$cityPart">
            - <span id="cityTitleCityPartValue"><xsl:value-of select="$cityPart"/></span>
          
        </xsl:if>
        </h3>
        
        
        
        <h4 id="keyTitle">
          <span id="keyTitleText">Služba: </span><span id="keyTitleValue"><xsl:value-of select="$key"/></span>
        </h4>
      
      <div id="services" width="100%">
          <xsl:for-each select="catalog/customer[id>0 and key=$key and data/contacts/address/city=$city and (./data/contacts/address/cityPart=$cityPart or not($cityPart))]">
          <xsl:sort select="data/title"/>
              
          <div class="service">
                <a>
                 <xsl:attribute name="name"><xsl:value-of select="id"/>
                  </xsl:attribute>
                </a>  
              <div class="serviceTitle">
              <a class="serviceTitleSelfLink">
                   <xsl:choose>
          <xsl:when test="$cityPart">
           
              <xsl:attribute name="href">javascript:getLink6('<xsl:value-of select="id"/>','<xsl:value-of select="$city"/>','<xsl:value-of select="$cityPart"/>','<xsl:value-of select="$key"/>')</xsl:attribute>
                     
           
            </xsl:when>
            <xsl:otherwise>
                         <xsl:attribute name="href">javascript:getLink7('<xsl:value-of select="id"/>','<xsl:value-of select="$city"/>','<xsl:value-of select="$key"/>')</xsl:attribute>
           
                     
            </xsl:otherwise>
            </xsl:choose>
                <xsl:value-of select="data/title"/>
                </a>
              </div>
              <div class="address">
                <span class="addressStreet"><xsl:value-of select="data/contacts/address/street"/></span>
                &#160;
                <span class="addressHause"> <xsl:value-of select="data/contacts/address/hause"/></span>
                    <xsl:if test="data/contacts/address/cityPart and not($cityPart)">
                       -&#160;<span class="addressCityPart"><xsl:value-of select="data/contacts/address/cityPart"/></span>
                  </xsl:if>
              </div>
              
              <div class="todayHoursBlock">
               <span class="todayHoursTitle">dnes: </span><span class="todayHoursValue">
               <xsl:choose>
                        <xsl:when test="data/open/period[1]/day[@which=$day]='CLOSED'">
                          <span style="color:red">zavřeno</span>
                        </xsl:when>
                        <xsl:otherwise>
                        <span style="color:green"><xsl:value-of select="data/open/period[1]/day[@which=$day]"/></span>
                        </xsl:otherwise>
                      </xsl:choose></span>
              </div>
              <div class="moreInfo">
                <a class="moreInfoLink">
                 <xsl:choose>
          <xsl:when test="$cityPart">
           
              <xsl:attribute name="href">javascript:getLink4('<xsl:value-of select="id"/>','<xsl:value-of select="$city"/>','<xsl:value-of select="$cityPart"/>','<xsl:value-of select="$key"/>')</xsl:attribute>
                     
           
            </xsl:when>
            <xsl:otherwise>
                         <xsl:attribute name="href">javascript:getLink5('<xsl:value-of select="id"/>','<xsl:value-of select="$city"/>' ,'<xsl:value-of select="$key"/>')</xsl:attribute>
           
                     
            </xsl:otherwise>
            </xsl:choose>
                 více informací</a>
              </div>
               
           </div><!-- service -->
           
          </xsl:for-each>
         </div> 
        <div id="backLinksSection">  
        <span id="backLinksTitle">Zpět na: </span>
         <div id="backLinks">
        <xsl:choose>
          <xsl:when test="$cityPart">
            <a id="backLinkCityParts" class="backLinkClass">
              <xsl:attribute name="href">javascript:getLink2('<xsl:value-of select="$city"/>','<xsl:value-of select="$cityPart"/>')</xsl:attribute>
              <xsl:value-of select="$city"/> -
              <xsl:value-of select="$cityPart"/>
            </a>
          
            
            <a id="backLinkCity" class="backLinkClass">
              <xsl:attribute name="href">javascript:getLink1('<xsl:value-of select="$city"/>')</xsl:attribute>
              <xsl:value-of select="$city"/>
            </a>        
            
            </xsl:when>
            <xsl:otherwise>
            <a id="backLinkCity" class="backLinkClass">
              <xsl:attribute name="href">javascript:getLink3('<xsl:value-of select="$city"/>')</xsl:attribute>
              <xsl:value-of select="$city"/>
            </a>           
            </xsl:otherwise>
            </xsl:choose>
            <a id="backLinkHome" class="backLinkClass">
              <xsl:attribute name="href">../index.html</xsl:attribute>
              domů
            </a>
            </div> 
            </div>         
          
              
           
        </div>  
      <!--</body>
    </html>-->
  </xsl:template>
</xsl:stylesheet>