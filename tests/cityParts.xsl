<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- WARNING if this code is formated, links (to images or so on) can stop working, because triming of spaces is browser(xslt processor) dependent-->
    <xsl:param name="city"/>
    <xsl:param name="day"/>
    <xsl:key name="cityParts" match="cityPart" use="."/>
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
            </h3>
            <div id="services" width="100%">
              <xsl:for-each select="catalog/customer/data/contacts/address/cityPart[../../../../id>0 and ../city=$city]">
                <xsl:if test="generate-id() = generate-id(key('cityParts', normalize-space(.)))">
                  <div class="service">
                    <xsl:variable name="currentv" select="." />
                    <a>
                      <xsl:attribute name="href">javascript:getLink2('<xsl:value-of select="$city"/>','<xsl:value-of select="$currentv"/>')</xsl:attribute>
                      <xsl:value-of select="."/>
                    </a>(
                    <xsl:value-of select="count(/catalog/customer[id>0 and data/contacts/address/city=$city and data/contacts/address/cityPart=$currentv])"/>)
                      <xsl:value-of select="key"/>
                    </div>
                    <!-- service --></xsl:if>
                  </xsl:for-each>
                  <div class="service">
                    <a class="subNavigationAddon">
                      <xsl:attribute name="href">javascript:getLink3('<xsl:value-of select="$city"/>')</xsl:attribute>Vybrat celé:
                      <xsl:value-of select="$city"/>
                    </a>
                  </div>
                </div>
                <div id="backLinksSection">
                  <span id="backLinksTitle">Zpět na:</span>
                  <div id="backLinks">
                    <a id="backLinkHome" class="backLinkClass">
                      <xsl:attribute name="href">../index.html</xsl:attribute>domů</a>
                  </div>
                </div>
              </div>
              <!--</body></html>--></xsl:template>
          </xsl:stylesheet>