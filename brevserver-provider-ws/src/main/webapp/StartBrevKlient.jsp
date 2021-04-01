<%@page import="java.util.ResourceBundle" %>
<%@page import="java.util.MissingResourceException" %>
<%@page import="no.nav.brevserver.server.common.config.ConfigManager" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
<HEAD>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<TITLE>Start av Brevklient</TITLE>
</HEAD>
<BODY>
<P>
<%  // Get parameters to applet
	ConfigManager cfg = ConfigManager.getInstance();
	
	String server = cfg.getString(ConfigManager.BREVSERVER_URL, null);
	String token = request.getParameter("token");
	String systemid = request.getParameter("systemid");
	String dokid = request.getParameter("dokid");
	
	String width = request.getParameter("width");
	if (width == null) {
		width = "800";
	}

	String height = request.getParameter("height");
	if (height == null) {
		height = "30";
	}
	
	boolean authOk = (systemid != null && token != null);
	
	if (!authOk || dokid == null || server == null) {
%>
		Programmet er ikke korrekt konfigurert mot brevserveren. Kontakt brukerhjelpen.
<%
		return;
	};
%> 
	<APPLET codebase="/brevweb" archive="StartBrevklientApplet.jar" code="StartBrevklientApplet.class" height="<%= height %>" width="<%= width %>">
		<PARAM NAME="SERVER" VALUE="<%= server %>">
		<PARAM NAME="SYSTEMID" VALUE="<%= systemid %>">
		<PARAM NAME="TOKEN" VALUE="<%= token %>">
		<PARAM NAME="DOKID" VALUE="<%= dokid %>">
	</APPLET>
</P>
</BODY>
</HTML>
