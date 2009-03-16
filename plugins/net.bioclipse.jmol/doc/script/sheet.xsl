<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="xml" omit-xml-declaration="yes"/>

    <xsl:template match="document">
        <html>
        <xsl:comment><xsl:text> Last updated: </xsl:text> <xsl:value-of select="./@lastupdated"/><xsl:text> </xsl:text></xsl:comment>
        <xsl:text>
</xsl:text>
        <xsl:comment><xsl:text> Automatically created from documentation on http://www.stolaf.edu/people/hansonr/jmol/docs/?xml
     as described in the README in the JmolScriptDocumentation module in CVS. </xsl:text></xsl:comment>
        <xsl:apply-templates select="cmdlist"/>
        </html>
    </xsl:template>

    <xsl:template match="jmolcmd">
        <xsl:element name="a">
            <xsl:attribute name="name"><xsl:value-of select="cmdname/a/@id"/></xsl:attribute>
        </xsl:element>
            <h1>
                <xsl:value-of select="cmdname/a"/>
            </h1>
            <ul>
            <p>
                <xsl:value-of select="cmddescription"/>
            </p>
            <xsl:apply-templates select="cmdexamples"/>
            <xsl:apply-templates select="cmddefinitions"/>
            <xsl:apply-templates select="cmdxref"/>
            <xsl:apply-templates select="cmdscriptlist"/>
            </ul>
    </xsl:template>
						
    <xsl:template match="cmdexamples">
        <xsl:if test="cmdexample">
            <h3>Syntax</h3>
                <table>
                <xsl:for-each select="cmdexample">
                    <tr>
                      <td>
                        <b><xsl:value-of select="cmdoption"/></b>
                      </td>
                      <td>
                        <ul>
                          <li><i><xsl:value-of select="cmdlistidescription"/></i></li>
                            <xsl:for-each select="cmdscript">
                                <li>
                                  <b><xsl:value-of select="../cmdoption"/></b>
                                  <pre>
                                  <xsl:apply-templates select="."/>
                                  </pre>
                                </li>
                            </xsl:for-each>
                        </ul>
                      </td>
                    </tr>
                </xsl:for-each>
            </table>
        </xsl:if>
    </xsl:template>
						
    <xsl:template match="cmddefinitions">
        <h3>Definitions</h3>
            <table>
            <xsl:for-each select="cmddef">
                <tr>
                    <td>
                        <b><xsl:value-of select="defkey"/></b>
                    </td>
                    <td>
                        <p>
                            <xsl:value-of select="defdata"/>
                        </p>
                    </td>
                </tr>
            </xsl:for-each>
            </table>
    </xsl:template>

    <xsl:template match="cmdxref">
      <h3>See also</h3>
      <xsl:for-each select="seealso/a">
        <xsl:element name="a">
          <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
          <xsl:value-of select="substring(@href,2)"/>
        </xsl:element>
        <xsl:text>, </xsl:text>
      </xsl:for-each>
    </xsl:template>

    <xsl:template match="cmdscriptlist">
        <h3>Examples</h3>
        <pre>
          <xsl:for-each select="cmdscript">
            <xsl:value-of select="normalize-space(.)"/><xsl:text>
</xsl:text>
          </xsl:for-each>
        </pre>
    </xsl:template>

</xsl:stylesheet>


