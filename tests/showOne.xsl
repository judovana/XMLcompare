<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!-- WARNING if this code is formated, links (to images or so on) can stop working, because triming of spaces is browser(xslt processor) dependent-->
    <!-- defausts only for debug?-->
      <xsl:param name="id"/>
      <xsl:param name="city"/>
      <xsl:param name="key"/>
      <xsl:param name="day"/>
      <xsl:param name="cityPart"/>
      <xsl:template match="/">
        <!-- <html>
          <head>
            <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
          </head>
          <body>-->
            <div id="sheetBackground">
              <h2 id="titleHeaderHeader">
                <a id="titleHeader" href="../index.html" align="center" >Služby kolem Vás</a>
              </h2>
              <div id="haderInfoDelimiter1">
              </div>
                   <div id="vizitkaHeaderHeader">
                                          <div id="vizitkaSpacer"></div>
                   <div id="vizitkaLiner">
                <span id="vizitkaHeader" >Vizitka</span>
              </div>
              </div>
              <div id="haderInfoDelimiter2">
              </div>
              <xsl:for-each select="/catalog/customer[id=$id]">
              <div id="customerBody">
              <div id="titleTextSectionBody">
               <xsl:if test="data/logo">
                    <img id="imageLogo" width="50" height="50"><xsl:attribute name="src">data/images/logos/<xsl:value-of select="data/logo"/></xsl:attribute>
                    </img>
                  </xsl:if>
                  <div id="titleTextSection">
                <small id="smallProffesion">
                  <xsl:value-of select="key"/>
                </small >
                <h2 id="customerTitleHeader">
                 <span id="customerTitleHeaderValue">
                    <xsl:value-of select="data/title"/>
                  </span></h2>
                  </div>
                  </div> <!-- title sectio body -->
                  <div id="breaker1" class="breaker">
                  </div>
<div id="column1" class="columns">                  
<div id="addressSection" class="infoSections">
<div id="addressTitle">Kontaktní adresa: </div>                  
<div id="addressSctreetSection">
<span id="addressStreet" class="address">  <xsl:value-of select="data/contacts/address/street"/></span>
&#160;
<span id="addressHause" class="address">  <xsl:value-of select="data/contacts/address/hause"/></span>
</div>
<div id="addressPost" class="address">  <xsl:value-of select="data/contacts/address/post"/></div>
<div id="addressCity" class="address">  <xsl:value-of select="data/contacts/address/city"/></div>
<div id="addressCityPart" class="address">  <xsl:value-of select="data/contacts/address/cityPart"/></div>
</div>  
<a id="mapImageLink" target="_blank">
  <xsl:attribute name="href"><xsl:value-of select="data/seznamMaplink"/></xsl:attribute>
  <img id="mapImage" ><xsl:attribute name="src">data/images/mapImages/<xsl:value-of select="data/mapImage"/></xsl:attribute>
  </img>
</a>
</div><!--column1 -->

<div id="column2" class="columns">                  
<div id="moreContactsSection" class="infoSections">
<div id="moreContactsTitle">Kontakty: </div> 
<xsl:if test="data/contacts/tele">                 
<div id="teleSection" class="moreContacts">
<span id="teleTitle" class="moreContactsTitle">Tel.: </span>
<span id="teleValue" class="moreContactsValue">  <xsl:value-of select="data/contacts/tele"/></span>
</div>
</xsl:if>
<xsl:if test="data/contacts/email">                   
<div id="emailSection" class="moreContacts">
<span id="emailTitle" class="moreContactsTitle">Email: </span>
<span id="emailValue" class="moreContactsValue">  <xsl:value-of select="data/contacts/email"/></span>
</div>
</xsl:if>
<xsl:if test="data/contacts/webPage">                   
<div id="webPageSection" class="moreContacts">
<span id="webPageTitle" class="moreContactsTitle">Web: </span>                                 
<span id="webPageValue" class="moreContactsValue">
<a  target="_blank">
<xsl:attribute name="href"><xsl:value-of select="data/contacts/webPage"/></xsl:attribute>
  <xsl:value-of select="data/contacts/webPage"/>
</a>
</span>
</div>
</xsl:if>
 </div>  

<div id="openSections" class="infoSections">       
<div id="openSection" >
<div id="openTitle">Otevřeno: </div>

<xsl:for-each select="data/open/period">
                    <div class="openDelimiter"></div>
                    <div class="openPeriod">
                    <div class="openComment">
                      <xsl:value-of select="comment"/>
                    </div>
                    
                    <xsl:for-each select="day">
                    <div class="openDayInfo">
                   
                       <span class="dayTitle">
                        <xsl:choose>
                    
                        <xsl:when test="@which='mo'">Pondělí: </xsl:when>
                        <xsl:when test="@which='tu'">Úterý: </xsl:when>
                        <xsl:when test="@which='we'">Středa: </xsl:when>
                        <xsl:when test="@which='th'">Čtvrtek: </xsl:when>
                        <xsl:when test="@which='fr'">Pátek: </xsl:when>
                        <xsl:when test="@which='sa'">Sobota:</xsl:when>
                        <xsl:when test="@which='su'">Neděle: </xsl:when>
                        <xsl:otherwise>neznamy den</xsl:otherwise>
                        
                      </xsl:choose>
                       </span><!--dayTitle-->  
                      <span class="dayValue">
                      <xsl:choose>
                        <xsl:when test=".='CLOSED'">
                          <span class="openDayValue" style="color:red">zavřeno</span>
                        </xsl:when>
                        <xsl:otherwise>
                        <span class="closedDayValue" style="color:green">
                          <xsl:value-of select="."/>
                          </span>
                        </xsl:otherwise>
                      </xsl:choose>
                        </span> <!--dayValue-->
                         <xsl:if test="@which=$day"><div class="todayMarker"></div></xsl:if>
                       </div><!--openDayInfo-->                                   
                    </xsl:for-each>  <!--day>-->
                    <div class="openHints">
                    <xsl:for-each select="hint">
                      <div class="openHint">
                        <xsl:value-of select="."/>
                      </div>
                    </xsl:for-each>             <!--hint-->
                    </div>                       <!--hints-->
                    </div>
                </xsl:for-each>          <!--period-->
</div>  <!--openSection-->
</div>  <!--openSections-->
       
</div><!--column2 -->

<div id="breaker2" class="breaker">
   </div>
   
<xsl:if test="not(count(data/notes/paragraph)=0)">   
<div id="column3" class="columns">   
<div id="notesSections" class="infoSections">
<xsl:for-each select="data/notes/paragraph">
<p class="notesParagraph">  
  <xsl:value-of select="."/>
</p>
</xsl:for-each>             <!--paragraph-->
</div> <!--notes-->
</div><!--column 3-->   
</xsl:if>

<xsl:if test="not(count(data/images/image)=0)">
<div id="column4" class="columns">   
<div id="imagesSections" class="infoSections">
  <xsl:for-each select="data/images/image">
                  <a class="imgHref">
                    <xsl:attribute name="href">data/images/fotos/<xsl:value-of select="."/>-BIG.jpg</xsl:attribute>
                    <img class="imgPicture" width="50" height="50"><xsl:attribute name="src">data/images/fotos/<xsl:value-of select="."/>-SMALL.jpg</xsl:attribute>
                    </img>
                  </a>
</xsl:for-each>             <!--image-->
</div> <!--images-->
</div><!--column 4-->
</xsl:if>             
                </div><!-- customer body-->
                
              </xsl:for-each>
              
              
              <div id="backLinksSection">
                <span id="backLinksTitle">Zpět na:</span>
                <div id="backLinks">
                  <xsl:choose>
                    <xsl:when test="$cityPart">
                     <a id="backLinkCityPartsProfession" class="backLinkClass">
                       <xsl:attribute name="href">javascript:getLink6('<xsl:value-of select="$id"/>','<xsl:value-of select="$city"/>','<xsl:value-of select="$cityPart"/>','<xsl:value-of select="$key"/>')</xsl:attribute>
                        <xsl:value-of select="$city"/>-
                        <xsl:value-of select="$cityPart"/>-
                        <xsl:value-of select="$key"/>
                      </a>
                      <a id="backLinkCityParts" class="backLinkClass">
                        <xsl:attribute name="href">javascript:getLink2('<xsl:value-of select="$city"/>','<xsl:value-of select="$cityPart"/>')</xsl:attribute>
                        <xsl:value-of select="$city"/>-
                        <xsl:value-of select="$cityPart"/>
                      </a>   
                      <a id="backLinkCity" class="backLinkClass">
                        <xsl:attribute name="href">javascript:getLink1('<xsl:value-of select="$city"/>')</xsl:attribute>
                        <xsl:value-of select="$city"/>
                      </a>
                    </xsl:when>
                    <xsl:otherwise>
                     <a id="backLinkCityProfession" class="backLinkClass">
                       <xsl:attribute name="href">javascript:getLink7('<xsl:value-of select="$id"/>','<xsl:value-of select="$city"/>','<xsl:value-of select="$key"/>')</xsl:attribute>
                        <xsl:value-of select="$city"/>-
                        <xsl:value-of select="$key"/>
                      </a>
                      <a id="backLinkCity" class="backLinkClass">
                        <xsl:attribute name="href">javascript:getLink3('<xsl:value-of select="$city"/>')</xsl:attribute>
                        <xsl:value-of select="$city"/>
                      </a>
                    </xsl:otherwise>
                  </xsl:choose>
                  <a id="backLinkHome" class="backLinkClass">
                    <xsl:attribute name="href">../index.html</xsl:attribute>domů</a>
                </div>
              

              </div>
            </div>
            <!-- </body></html>--></xsl:template>
        </xsl:stylesheet>
