<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                version="1.0">

  <!-- $Author: egonw $
       $Date: 2005-06-22 13:31:24 +0200 (Wed, 22 Jun 2005) $
       $Revision: 152 $ -->

  <xsl:output method="xml" indent="yes"
    omit-xml-declaration="no" encoding="utf-8"
    doctype-public="-//W3C//DTD XHTML 1.1 plus MathML 2.0 plus SVG 1.1//EN" 
    doctype-system="http://www.w3.org/2002/04/xhtml-math-svg/xhtml-math-svg.dtd"/>

  <xsl:template match="text()">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="/">
<html xml:lang="en">
<head>
  <title><xsl:value-of select=".//*[name(.)='dictionary']/@title"/> [<xsl:value-of select=".//*[name(.)='dictionary']/@namespace"/>]</title>
  <link rel="stylesheet" href="../../PRODUCT_PLUGIN/narrow_book.css" type="text/css" />
</head>
<body>
<h1><xsl:value-of select=".//*[name(.)='dictionary']/@title"/></h1>
<p>
  <xsl:for-each select=".//*[name(.)='contributor']">
    <xsl:if test="position()=last() and last()!=1"><xsl:text> and </xsl:text></xsl:if>
    <i><xsl:value-of select="."/></i>
    <xsl:if test="position() &lt; (last()-1)"><xsl:text>, </xsl:text></xsl:if>
  </xsl:for-each>
</p>
<p>
  <xsl:for-each select=".//*[name(.)='annotation']//*[name(.)='appinfo']">
    <xsl:value-of select="."/>
  </xsl:for-each>
</p>
<p>
  <xsl:apply-templates select=".//*[name(.)='dictionary']/*[name(.)='description']"/>
</p>
<xsl:if test="/*[name(.)='dictionary']/@id='qsar-descriptors'">
</xsl:if>
<h2><a name="Entries">Entries</a></h2>
<xsl:for-each select=".//*[name(.)='entry']">
  <xsl:sort select="./@term" order="ascending"/>
  <xsl:apply-templates select="."/>
</xsl:for-each>
</body>
</html>
  </xsl:template>

  <xsl:template match="*[name(.)='entry']">
<h3><xsl:element name="a">
      <xsl:attribute name="id"><xsl:value-of select="./@id"/></xsl:attribute>
      <xsl:value-of select="./@term"/> (<xsl:value-of select="./@id"/>)
    </xsl:element>
</h3>
<ul>
<li><p>
  <xsl:if test="./*[name(.)='definition']">
    <b>Definition</b><br/>
    <xsl:apply-templates select="./*[name(.)='definition']"/>
  </xsl:if>
</p>
<p>
  <xsl:if test="./*[name(.)='description']">
    <b>Description</b><br/>
    <xsl:apply-templates select="./*[name(.)='description']"/>
  </xsl:if>
</p>
</li>
</ul>
  </xsl:template>

</xsl:stylesheet>
