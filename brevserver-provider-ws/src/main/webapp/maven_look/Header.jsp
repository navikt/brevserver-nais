<%@page import="no.nav.brevserver.server.common.config.Konstanter"%>
<%@page import="no.nav.brevserver.server.common.config.ConfigManager"%>
<% 
	String dokURL = ConfigManager.getInstance().getString("Menu.dokURL", ""); 
	String zipUrl = ConfigManager.getInstance().getString("Menu.zipURL", "");
	String siteName = "Brev&Arkiv";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta name="generator" content="HTML Tidy for Linux/x86 (vers 1st November 2002), see www.w3.org" />
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
    <title>Brevserver</title>
    <style type="text/css" media="all">
    /*<![CDATA[*/
          @import url("./maven_look/maven-base.css");
          @import url("./maven_look/maven-theme.css");
    /*]]>*/
    </style>
    <link rel="stylesheet" href="./maven_look/print.css" type="text/css" media="print" />

<script>
	function toggleVisibility(id){
		if (document.getElementById(id).style.visibility=='hidden') {
			document.getElementById(id).style.visibility='visible';
			document.getElementById(id).style.display='block';
		} else {
			document.getElementById(id).style.visibility='hidden';
			document.getElementById(id).style.display='none';
		}
	}
</script>
    
    
  </head>
  <body class="composite">
    <div id="banner">
      <a href="<%=dokURL %>" id="organizationLogo">
        <img alt="NAV" src="./maven_look/images/logo.gif" />
      </a>
      <a href="<%=dokURL %>brev" id="projectLogo"><span><%=siteName %></span></a>
      <div class="clear">
        <hr />
      </div>
    </div>
    <div id="breadcrumbs">
      <div class="xleft">
      </div>
      <div class="clear">
        <hr />
      </div>
    </div>