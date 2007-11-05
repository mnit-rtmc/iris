<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="meter_state">
    <xsl:choose><xsl:when test="@id='M77N82'">
    <td><xsl:value-of select="@Q" /></td>
    <td><xsl:value-of select="@O" /></td>
    <td><xsl:value-of select="@P" /></td>
    <td><xsl:value-of select="@R_acc" /></td>
    <td><xsl:value-of select="@N" /></td>
    <td><xsl:value-of select="@T" /></td>
    <td><xsl:value-of select="@R_min" /></td>
    <td><xsl:value-of select="@D" /></td>
    <td><xsl:value-of select="@R" /></td>
    <td><xsl:value-of select="@Z" /></td>
    </xsl:when></xsl:choose>
  </xsl:template>

  <xsl:template match="interval">
    <tr>
      <td><xsl:value-of select="@time" /></td>
      <xsl:apply-templates select="meter_state" />
    </tr>
  </xsl:template>

  <xsl:template match="stratified_plan_log">
    <html>
      <head>
      <link href="analysis.css" rel="stylesheet" type="text/css" />
      <title>Ramp Meter Analysis</title></head>
      <body>
        <table cellspacing='0' cellpadding='0'>
        <caption>M77N82: T.H.13 EB to T.H.77 NB</caption>
        <th>Time</th>
        <th>Q</th>
        <th>O</th>
        <th>P</th>
        <th>R_acc</th>
        <th>N</th>
        <th>T</th>
        <th>R_min</th>
        <th>D</th>
        <th>R</th>
        <th>Z</th>
        <xsl:apply-templates select="interval" />
        </table>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
