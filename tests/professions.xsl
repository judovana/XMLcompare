<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- WARNING if this code is formated, links (to images or so on) can stop working, because triming of spaces is browser(xslt processor) dependent-->
    <xsl:param name="city"/>
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
              <xsl:if test="$cityPart"> -
                <span id="cityTitleCityPartValue">
                  <xsl:value-of select="$cityPart"/>
                </span>
              </xsl:if>
            </h3>
            <div id="services" width="100%">
              <xsl:for-each select="catalog/customer/key[../id>0 and ../data/contacts/address/city=$city and (../data/contacts/address/cityPart=$cityPart or not($cityPart))]"> 
              <xsl:variable name="currentv" select="." />
              
              <xsl:if test="count(following::customer[key=$currentv and data/contacts/address/city=$city and (data/contacts/address/cityPart=$cityPart or not($cityPart))])=0">
                  <div class="service">
                  
                  
                  
                   
                    
                    <xsl:choose>
                      <xsl:when test="$cityPart">
                        <a>
                          <xsl:attribute name="href">javascript:getLink8('<xsl:value-of select="$city"/>','<xsl:value-of select="$cityPart"/>','<xsl:value-of select="."/>')</xsl:attribute>
                          <xsl:value-of select="."/>
                        </a>
                      </xsl:when>
                      <xsl:otherwise>
                        <a>
                          <xsl:attribute name="href">javascript:getLink9('<xsl:value-of select="$city"/>','<xsl:value-of select="."/>')</xsl:attribute>
                          <xsl:value-of select="."/>
                        </a>
                      </xsl:otherwise>
                    </xsl:choose>(
                    <xsl:value-of select="count(/catalog/customer[id>0 and data/contacts/address/city=$city and (data/contacts/address/cityPart=$cityPart or not($cityPart)) and key=$currentv])"/>)
                      <xsl:value-of select="key"/>
                    </div>
                    <!-- service -->
                    </xsl:if>
                  </xsl:for-each>
                  <xsl:if test="not($cityPart)">
                   <div class="service">
                    <a class="subNavigationAddon">
                      <xsl:attribute name="href">javascript:getLink1('<xsl:value-of select="$city"/>')</xsl:attribute>Rozdělit podle čtvrtí
                    </a>
                  </div>
                 </xsl:if> 
                </div>
                <div id="backLinksSection">
                  <span id="backLinksTitle">Zpět na:</span>
                  <div id="backLinks">
                    <xsl:choose>
                      <xsl:when test="$cityPart">
                        <a id="backLinkCity" class="backLinkClass">
                          <xsl:attribute name="href">javascript:getLink1('<xsl:value-of select="$city"/>')</xsl:attribute>
                          <xsl:value-of select="$city"/>
                        </a>
                      </xsl:when>
                      <xsl:otherwise></xsl:otherwise>
                    </xsl:choose>
                    <a id="backLinkHome" class="backLinkClass">
                      <xsl:attribute name="href">../index.html</xsl:attribute>domů</a>
                  </div>
                </div>
              </div>
              <!--</body></html>--></xsl:template>
          </xsl:stylesheet>