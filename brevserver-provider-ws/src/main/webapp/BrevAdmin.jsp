<%@page import="no.nav.brevserver.brevadmin.consumer.DokumentBehandlingPingService"%>
<%@page import="no.nav.brevserver.consumer.joark.factory.JoarkServiceBeanFactory"%>
<%@page import="no.nav.brevserver.consumer.joark.JoarkServiceBi"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="no.nav.brevserver.server.common.config.Konstanter"%>
<%@page import="no.nav.brevserver.server.common.exception.BrevException"%>
<%@page import="no.nav.brevserver.server.common.exception.BrevTechnicalException"%>
<%@page import="no.nav.brevserver.server.common.config.ConfigManager"%>
<%@page import="no.nav.brevserver.server.common.jms.JMSAccessor"%>
<%@page import="no.nav.brevserver.server.common.cache.CacheManager"%>
<%@page import="no.nav.brevserver.server.common.log.LogCache"%>
<%@page import="no.nav.brevserver.server.common.log.Log"%>
<%@page import="no.nav.brevserver.brevadmin.test.DialogueStub"%>
<%@page import="no.nav.brevserver.brevadmin.test.DialogueStubData"%>
<%@page import="no.nav.brevserver.server.common.vo.SysTilgangVO"%>
<%@page import="no.nav.brevserver.service.brevserver.BrevserverServiceFactory"%>
<%@page import="no.nav.brevserver.service.brevserver.BrevserverService"%>
<%@page import="no.nav.brevserver.server.common.utility.PerformanceLogger"%>
<%@page import="no.nav.brevserver.service.search.SearchFactory"%>
<%@page import="no.nav.brevserver.service.search.SearchService"%>
<%@page import="no.nav.brevserver.brevadmin.test.ServletHelper"%>
<%
 
	Log log = new Log(this.getClass());

	// Get config
	boolean utvidetAdmin = ConfigManager.getInstance().getBool("BrevAdmin.utvidetAdministrasjon", false);
	String defaultDokId = ConfigManager.getInstance().getString("BrevAdmin.dokid", "50000000038");
	
	// Get request variables
	String dokid = ServletHelper.getRequestValueAndSetSession(request, "dokid", defaultDokId, true);
	String cmd = ServletHelper.getRequestValueAndSetSession(request, "cmd", "", false);
	String fil = ServletHelper.getRequestValueAndSetSession(request, "fil", "", true);
	String system = ServletHelper.getRequestValueAndSetSession(request, "system", "BI12", true);	
	String token = ServletHelper.getRequestValueAndSetSession(request, "token", "123", false);	
	
	// Some variables
	String msg = "";
	String caller = request.getRemoteAddr();
	Exception ukjentException = null;
	List myList = new ArrayList();	
	List unicenterList = null;
	boolean useCounter = false;
	List connectionMessages = new ArrayList();
	List connectionExceptions = new ArrayList();	
	String brevUrl=null;
	String id = "BrevAdmin("+caller+", "+cmd+")";
	
	PerformanceLogger p = new PerformanceLogger(id);
	
	try {
		// TEST INSTALLASJON
		if ("Test".equalsIgnoreCase(cmd)) {
			int feilteller = 0;
			unicenterList = new ArrayList();
			
			// Teste database
			try {
					BrevserverService brevService = BrevserverServiceFactory.getInstance().createBrevserverService();
					SysTilgangVO tilgang = brevService.hentTilgang("BI01", false);
					
					connectionMessages.add("Databasen er tilgjengelig");
					connectionExceptions.add(null);
					unicenterList.add("DB2_OK");
					
			} catch (BrevException e) {
					feilteller++;
					unicenterList.add("DB_NOT_OK");
					connectionMessages.add("Databasen er ikke tilgjengelig");
					connectionExceptions.add(e);
			}
			
			try {
				new DokumentBehandlingPingService().ping();
				connectionMessages.add("Dokumentbehandling er tilgjengelig");
				unicenterList.add("DOKBEH_OK");
				connectionExceptions.add(null);
			} catch (RuntimeException e) {
				feilteller++;
				connectionMessages.add("Dokumentbehandling er ikke tilgjengelig");
				unicenterList.add("DOKBEH_NOT_OK");
				connectionExceptions.add(e);
			}
			
			// Teste kobling mot Joark
			try {
				JoarkServiceBi joarkService = JoarkServiceBeanFactory.getInstance().getJoarkService();	
				joarkService.isJournalpost("0");
				
				connectionMessages.add("Joark er tilgjengelig");
				connectionExceptions.add(null);
				unicenterList.add("JOARK_OK");
					
			} catch (Exception e) {
					feilteller++;
					unicenterList.add("JOARK_NOT_OK");
					connectionMessages.add("Joark er ikke tilgjengelig");
					connectionExceptions.add(e);
			}

			// Teste MQ
			String[] queues = { Konstanter.KONF_MOTTAK_SAKSBEH_ONLINE_BI,
								Konstanter.KONF_MOTTAK_DIALOGUE_ONLINE_BI,
								Konstanter.KONF_MOTTAK_DIALOGUE_ARKIV_BI, 
								Konstanter.KONF_DEAD_LETTER_BI,
								Konstanter.KONF_MOTTAK_SAKSBEH_ONLINE_PE,
								Konstanter.KONF_MOTTAK_DIALOGUE_ONLINE_PE,
								Konstanter.KONF_MOTTAK_DIALOGUE_ARKIV_PE,  
								Konstanter.KONF_DEAD_LETTER_PE,									 };

			for (int i=0; i<queues.length; i++) {

				JMSAccessor accessor = null;
				BrevException ex = null;
				String str = "kø " + queues[i] + " feilet. Sjekk WAS oppsettet og MQ.";
				
				try {
					accessor = JMSAccessor.getAccessorUsingQueueJndiName(queues[i]);
					str = accessor.testQueue();
					unicenterList.add(queues[i].toUpperCase()+"_OK");
					
				} catch (BrevException e) {
					feilteller++;
					ex = e;
					unicenterList.add(queues[i].toUpperCase()+"_NOT_OK");
					
				} finally {
					JMSAccessor.close(accessor);
				}
				
				connectionMessages.add(str);
				connectionExceptions.add(ex);
			}
			
			
			// Teste Lyttere
			String[][] listener = { { "Brevserver", Konstanter.KONF_MOTTAK_SAKSBEH_ONLINE_BI },
								    { "Brevserver", Konstanter.KONF_MOTTAK_DIALOGUE_ONLINE_BI },
								    { "Brevserver", Konstanter.KONF_MOTTAK_DIALOGUE_ARKIV_BI }, 
								    { "Brevserver Pensjon", Konstanter.KONF_MOTTAK_SAKSBEH_ONLINE_PE },
								    { "Brevserver Pensjon", Konstanter.KONF_MOTTAK_DIALOGUE_ONLINE_PE },
								    { "Brevserver Pensjon", Konstanter.KONF_MOTTAK_DIALOGUE_ARKIV_PE }, 
								  };
	
			for (int i=0; i<listener.length; i++) {
	
				JMSAccessor accessor = null;
				BrevException ex = null;
				String str = "Sjekk av "+listener[i][0] + " sin lytter på " + listener[i][1] + " feilet. Sjekk WAS oppsettet.";
				
				try {
					accessor = JMSAccessor.getAccessorUsingQueueJndiName(listener[i][1]);
					if (accessor.isListenerRunning()) {
						unicenterList.add("LYTTER_"+listener[i][0].toUpperCase()+"_"+listener[i][1].toUpperCase()+"_OK");
						str = listener[i][0] + " ser ut tilålytte på " + accessor.getQueueName();
						
					} else {
						unicenterList.add("LYTTER_"+listener[i][0].toUpperCase()+"_"+listener[i][1].toUpperCase()+"_NOT_OK");
						str = listener[i][0] + " ser *ikke* ut tilålytte på " + accessor.getQueueName();
						feilteller++;
					}
					
				} catch (BrevException e) {
					feilteller++;
					ex = e;
					
				} finally {
					JMSAccessor.close(accessor);
				}
				
				connectionMessages.add(str);
				connectionExceptions.add(ex);
			}
			
			if (feilteller==0) {
				msg = "Brevløsningen er oppe!";
				unicenterList.add(0, "BREVLOSNING_OK");
				
			} else {
				unicenterList.add(0, "BREVLOSNING_NOT_OK");
				if (feilteller==1) {
					msg = "Brevløsningen feilet på ett punkt";
				} else {
					msg = "Brevløsningen feilet på " + Integer.toString(feilteller) + " punkter";
				}
			}

		// VIS FEILMELDINGER
		} else if ("VisLoggError".equalsIgnoreCase(cmd)) {	
			List logList = LogCache.getLastErrorMessages();
				
			if (logList.size()==0) {
				msg = "Loggen er tom!";
			} else {
				for (int i=0; i<logList.size(); i++) {
					String logMsg = (String) logList.get(i);
					myList.add(logMsg.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
				}
			}

		// VIS SUKSESS-MELDINGER
		} else if ("VisLoggSuccess".equalsIgnoreCase(cmd)) {	
			List logList = LogCache.getLastSuccessMessages();
				
			if (logList.size()==0) {
				msg = "Loggen er tom!";
			} else {
				for (int i=0; i<logList.size(); i++) {
					String logMsg = (String) logList.get(i);
					myList.add(logMsg.replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
				}
			}
				
		// FINN DOKUMENT
		} else if ("FindDok".equalsIgnoreCase(cmd)) {
			try {
				Long lDokid = new Long(Long.parseLong(dokid));
				
				SearchService searchService = SearchFactory.getService();
				List searchObjectes = searchService.findDocument(system, dokid);
				myList = searchService.analyze(searchObjectes);
				useCounter = true;
				
			} catch (NumberFormatException nfe) {
				msg = "Legg inn gyldig dokumentid";
				ukjentException = nfe;
			} 

		// OPPRETT BREV FRA FIL
		} else if ("OpprettBrevFraFil".equalsIgnoreCase(cmd) && utvidetAdmin) {
			DialogueStubData data = new DialogueStubData();
			data.filnavn = fil;
			data.brevref = dokid;
			data.system = system;
			
			String queueName = DialogueStub.opprettBrev(data);
			
			msg = "Filen er lagt på " + queueName + " med korrekt XML";

		// BESTILL BREV
		} else if ("BestillBrev".equalsIgnoreCase(cmd) && utvidetAdmin) {
			DialogueStubData data = new DialogueStubData();
			data.system = system;
			data.filnavn = fil;
			data.brevref = dokid;
			
			String queueName = DialogueStub.bestillBrev(data);
			
			msg = "Bestilling er lagt på " + queueName ;

		// GI TILGANG TIL DOKUMENT
		} else if ("GiTilgang".equalsIgnoreCase(cmd) && utvidetAdmin) {
			DialogueStubData data = new DialogueStubData();
			data.brevref = dokid;
			data.token = token;
			data.system = system;

			String queueStr = DialogueStub.giTilgangTilBrevet(data);
			
			msg = "Tilgangsforespårsel er sendt med token "+data.token+" til kø "+queueStr;
			
		// ÅPNE BREV
		} else if ("AapneBrev".equalsIgnoreCase(cmd) ) {

			// Hvis det er default dok så gi tilgang
			if (defaultDokId.equals(dokid)) {
				DialogueStubData data = new DialogueStubData();
				data.brevref = defaultDokId;
				data.token = token;
				data.system = system;
	
				String queueStr = DialogueStub.giTilgangTilBrevet(data);
			}
		
			brevUrl = "StartBrevKlient.jsp?dokid="+dokid+"&systemid="+system+"&token="+token+"&width=400";

		// VIS KONFIG
		} else if ("VisProperties".equalsIgnoreCase(cmd) && utvidetAdmin ) {
			myList = ConfigManager.getInstance().getAllProperties();

		// TØM CACHE OG LAST KONFIG påNYTT
		} else if ("ClearCache".equalsIgnoreCase(cmd) ) {
			CacheManager.clearCache();
			msg = ConfigManager.getInstance().clearConfig()?"Cache ble tømt og konfigurasjon ble lastet på nytt":"Cache ble tømt men konfigurasjon ble *ikke* lastet pånytt";
			System.gc();
		} 
	} catch (Exception e) {
		msg = e.getMessage();
		ukjentException = e;
	}
%>
<%@ include file="./maven_look/Header.jsp" %>
<%@ include file="./maven_look/Menu.jsp" %>
<H2><a href="./BrevAdmin.jsp">BrevServer - Admin : <%=ServletHelper.getVersionNumber(pageContext)%></a></H2>
<p>
<%
	if (utvidetAdmin) {
		out.println("Du har utvidede administrasjonsrettigheter :-)<br />");
	}
	out.flush();	
%>
<p>
<FORM>
<TABLE>
	<TR>
		<TD colspan="2"><FONT color="red"><I><%= msg %></I></FONT></TD>
	</TR>
	<TR>
		<TD>Brev fra</TD>
<%
	String[] systems = { "BI12", "OB05", "PE2", "HT01" };

	String[] systems_default = new String[systems.length];
	for (int i=0; i<systems.length; i++) {
		if (systems[i].equals(system)) {
			systems_default[i] = "SELECTED";
		} else {
			systems_default[i] = "";
		}
	}
%>
		<TD><SELECT NAME="system">
				<OPTION VALUE="<%= systems[0] %>" <%= systems_default[0] %>>Bidrag</OPTION>
				<OPTION VALUE="<%= systems[1] %>" <%= systems_default[1] %>>Innkreving</OPTION>
				<OPTION VALUE="<%= systems[2] %>" <%= systems_default[2] %>>Pensjon</OPTION>
				<OPTION VALUE="<%= systems[3] %>" <%= systems_default[3] %>>Frikort</OPTION>
		</SELECT>
		</TD>
	</TR>
	<TR>
		<TD WIDTH="10%">Dokid</TD>
		<TD><INPUT NAME="dokid" TYPE="TEXT"
			VALUE="<%= (String) request.getSession().getAttribute("dokid") %>"></TD>
	</TR>
<% if  (utvidetAdmin) {%>
	<TR>
		<TD WIDTH="10%">Filnavn</TD>
		<TD><INPUT NAME="fil" TYPE="TEXT"
			VALUE="<%= (String) request.getSession().getAttribute("fil") %>"></TD>
	</TR>
	
<% } 

	String[] cmds = { "FindDok", "Test", "UTGÅTT", "VisLoggSuccess",
		"VisLoggError", "UTGÅTT", "AapneBrev", "UTGÅTT",
		"OpprettBrevFraFil", "UTGÅTT", "UTGÅTT",
		"BestillBrev", "GiTilgang", "UTGÅTT", "ClearCache", "VisProperties",
		"UTGÅTT", "UTGÅTT", "UTGÅTT", "UTGÅTT" };

	String[] cmd_default = new String[cmds.length];
	for (int i=0; i<cmds.length; i++) {
		if (cmds[i].equals(cmd)) {
			cmd_default[i] = "SELECTED";
		} else {
			cmd_default[i] = "";
		}
	}

	String separator = "----------------------------------------------------";
%>
	<TR>
	<TD COLSPAN="2">
	<SELECT NAME="cmd">
	<OPTION VALUE="<%= cmds[0] %>"  <%= cmd_default[0]  %>>Finn brev (DokId)</OPTION>
	<OPTION VALUE="<%= cmds[1] %>"  <%= cmd_default[1]  %>>Test installasjon</OPTION>
	<OPTION VALUE="<%= cmds[6] %>"  <%= cmd_default[6]  %>>åpne brev (DokId)</OPTION>
	<OPTION VALUE="sep"><%= separator %></OPTION>
	<OPTION VALUE="<%= cmds[3] %>"  <%= cmd_default[3]  %>>Vis siste suksess-meldinger</OPTION>
	<OPTION VALUE="<%= cmds[4] %>"  <%= cmd_default[4]  %>>Vis siste feilmeldinger</OPTION>
	<OPTION VALUE="<%= cmds[14] %>" <%= cmd_default[14] %>>Tøm cache og last konfigurasjon på nytt</OPTION>
	
<% if  (utvidetAdmin) { %>
	<OPTION VALUE="sep"><%= separator %></OPTION>
	<OPTION VALUE="<%= cmds[8] %>" <%= cmd_default[8] %>>Send fil til arkivering (Fil, DokId)</OPTION>
	<OPTION VALUE="<%= cmds[11] %>" <%= cmd_default[11] %>>Send bestilling til Dialogue (Fil, DokId)</OPTION>
	<OPTION VALUE="<%= cmds[12] %>" <%= cmd_default[12] %>>Gi tilgang til brev (DokId)</OPTION>
	<OPTION VALUE="<%= cmds[15] %>" <%= cmd_default[15] %>>Vis konfigurasjon</OPTION>
<% } %>
	</SELECT>
	<INPUT TYPE="SUBMIT" VALUE="Kjør">
	<a href="<%= dokURL %>brev/drift/brevadmin.html"> ?</a>
	</TD>
	</TR>
</TABLE>
</FORM>

<BR />

<PRE style="font-family:'courier new'">
<%
	if (ukjentException != null) {
		out.flush();
		ukjentException.printStackTrace(response.getWriter());
		log.error("BrevAdmin.jsp", ukjentException.getMessage(), ukjentException);
	}

	for (int i=0; i<connectionMessages.size(); i++) {
		String str = " " + Integer.toString(i+1)+". " + (String) connectionMessages.get(i);
		
		Exception e = (Exception) connectionExceptions.get(i);
		if (e != null) {
			log.error("BrevAdmin.jsp", e.getMessage(), e);
		    String ex_id = "e"+(i+1);
			out.print("<a href=\"javascript:toggleVisibility('"+ex_id+"');\">");
			out.print("<img alt=\"Feil!\"  src=\"./maven_look/images/redball.gif\">");
			out.print("</a>");
			out.println(str);
			
			out.print("<div id=\""+ex_id+"\" style=\"visibility:hidden;display:none\">");
			out.flush();
			e.printStackTrace(response.getWriter());
			out.print("</div>");
			out.flush();
			
		} else {
			if (str.indexOf("*ikke*") > -1) {
				out.print("<img alt=\"Feil!\"  src=\"./maven_look/images/yellowball.gif\">");
			} else {
				out.print("<img alt=\"Ok!\"  src=\"./maven_look/images/greenball.gif\">");
			}
			
			out.println(str);
			out.flush();
		}
	}
	
	if (myList.size()>0) {
		out.flush();
		
		for (int i=0; i<myList.size(); i++) {
			String str = (String) myList.get(i);
			str = str.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
			
			if (str != null) {
				if (useCounter) 
					out.println(Integer.toString(i+1)+". "+str);
				else 
					out.println(str);
					
				out.flush();
			}
			
			if (str.indexOf("Brevet ble funnet i loggen")>=0) {
				useCounter = false;
			}
		}
	}
		
	if (unicenterList != null) {
		out.println("<DIV style=\"visibility:hidden;display:none\">");
		for (int i=0; i<unicenterList.size(); i++) {
			String unicenterCode = (String) unicenterList.get(i);
			out.println("<!--UNICENTER:"+unicenterCode+":-->");
		}
		out.println("</DIV>");
	}
		
	if (brevUrl!=null) { 
%>
		<iframe src="<%=brevUrl%>" width="450" height="70"></iframe>
<%	}
	p.stop();
%>
</PRE>

<%@ include file="./maven_look/Footer.jsp" %>
