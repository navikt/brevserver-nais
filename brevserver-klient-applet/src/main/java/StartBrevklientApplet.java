import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Applet for å starte brevklienten fra IE.
 * <p/>
 * Parametre :
 * <APPLET code="StartBrevklientApplet.class" height="50" width="250">
 * <PARAM NAME="SERVER" VALUE="<%= server %>">
 * <PARAM NAME="SYSTEMID" VALUE="<%= systemid %>">
 * <PARAM NAME="TOKEN" VALUE="<%= token %>">
 * <PARAM NAME="DOKID" VALUE="<%= dokid %>">
 * <PARAM NAME="VARIANT" VALUE="<%= variant %>">
 * <PARAM NAME="ENVIRONMENT" VALUE="<%= env %>">
 * </APPLET>
 * <p/>
 * For at appletten skal kunne starte brevklienten og logge til fil må følgende tillatelse gis.
 * <p/>
 * grant {
 * permission java.io.FilePermission "C:\\programfiler\\datasentral\\brev\\StartBrevklient.bat", "execute";
 * permission java.util.PropertyPermission "user.home", "read";
 * permission java.io.FilePermission "${user.home}${/}brevklient.log", "write, read";
 * };
 * <p/>
 * Dette er definert i brevklient/src/resources/brev.policy
 * I tillegg må java.security-filen som benyttes linkes opp til denne filen igjen.
 * <p/>
 * Denne .class-filen må legges i samme katalog som jsp/html-filen med <APPLET> koden
 * <p/>
 * Tips : Feilmeldinger havner i javakonsollet i IE (Verktøy / Sun Java Console )
 *
 * @author Rune Røren, Accenture
 * @version $Id: StartBrevklientApplet.java 928 2006-03-02 11:17:28Z skb2930 $
 */
public class StartBrevklientApplet extends Applet {
	private static final long serialVersionUID = -8667593014766998127L;
	// Disse må være i java.policy-filen til Java VM'et IE benytter
	private static final String PRG_FILE_LOCATION = "C:\\Progra~1\\datasentral\\brev\\StartBrevklient.bat";
	private static final String LOG_FILE_LOCATION = "C:\\Progra~1\\datasentral\\brev\\logs\\brevklient_";
	private static String logFileLocation = null;

	static final String[] PARAMETERS = {"SERVER", "SYSTEMID", "TOKEN", "DOKID"};
	private static final String PARAMETER_SEPARATOR = ":";

	static final String RUN_OK = "Brevklient kjører...";
	static final String RUN_NOT_OK = "Brevklient startet ikke.";
	static final String RUN_FINISHED = "Brevklient har avsluttet.";
	static final String RUN_NO_ACCESS = "Ikke tilgang til Brevklient.";

	String msgToUser = null;
	String[] cmd = {};
	boolean runProgram = false;
	int paramErrors = 0;

	/**
	 * Henter alle parametre som klienten trenger som input.
	 */
	public void init() {
		try {
			if (logFileLocation == null) {
				logFileLocation = LOG_FILE_LOCATION + System.getProperty("user.name", "ukjent") + ".log";
			}

			slettGammelLoggFil();

		} catch (AccessControlException ace) {
			messageToUser(RUN_NO_ACCESS);
			log("StartBrevklientApplet.init()", "Ikke tilgang", ace);
			return;
		}

		messageToUser("Laster brevklient...");

		// Finn  parameterne
		ArrayList<String> list = new ArrayList<String>();
		for (String param : PARAMETERS) {
			String value = getParameter(param);

			if (value != null && !value.equals("null")) {
				String tmp = param + PARAMETER_SEPARATOR + value;
				list.add(tmp);
			}
		}

		// Konverter ArrayList til String[]
		cmd = new String[list.size() + 1];
		cmd[0] = PRG_FILE_LOCATION;

		for (int i = 0; i < list.size(); i++) {
			cmd[i + 1] = list.get(i);
		}

		// Marker at kjøring er ok.
		if (paramErrors == 0) {
			runProgram = true;
		} else {
			messageToUser(RUN_NOT_OK);
		}
	}

	/**
	 * Kjør brevklienten
	 */
	public void start() {
		try {
			if (runProgram) {

				// Logging
				for (int i = 0; i < cmd.length; i++) {
					String cmdArg = "cmd[" + Integer.toString(i) + "] " + cmd[i];
					log("StartBrevKlientApplet.start()", cmdArg, null);
				}

				// Starte brevklienten
				Process proc = Runtime.getRuntime().exec(cmd);
				messageToUser(RUN_OK);

				// Logge hva som kommer ut av brevklienten 
				ProcessListener listener = new ProcessListener(proc, this);
				listener.start();

			}
		} catch (AccessControlException e) {
			messageToUser(RUN_NO_ACCESS);
			log("StartBrevklientApplet.start()", "Ikke tilgang", e);

		} catch (Exception e) {
			messageToUser(RUN_NOT_OK);
			log("StartBrevklientApplet.start()", "Feil i brev applet", e);
		}
	}

	public void paint(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		//g.setColor(Color.GRAY);
		//g.drawRect(0,0, this.getWidth(), this.getHeight());

		g.setColor(Color.BLUE);
		g.drawString(msgToUser, 5, 15);
	}

	/**
	 * Logger meldinger til konsollet
	 *
	 * @param msg Melding
	 */
	void messageToUser(String msg) {
		log("StartBrevKlientApplet.messageToUser()", msg, null);

		msgToUser = msg;
		repaint();
	}

	static boolean append2File = true;

	private static String[] sensitiveOrd = {"token:", "token="};

	private static String finnSensitiveOrd(String msg) {
		if (msg == null) {
			return null;
		}

		String msgLowerCase = msg.toLowerCase();

		for (String sensitivtOrd : sensitiveOrd) {
			if (msgLowerCase.contains(sensitivtOrd)) {
				return "(" + sensitivtOrd + ")";
			}
		}

		return null;
	}

	static synchronized void log(String who, String msg, Exception e) {

		String forbudtOrd = finnSensitiveOrd(msg);
		if (forbudtOrd != null) {
			msg = "Meldingen innholdt sensitiv informasjon " + forbudtOrd + " og blir ikke logget.";
		}

		Date now = new Date();

		SimpleDateFormat fmt = (SimpleDateFormat) DateFormat.getInstance();
		fmt.setLenient(false);
		fmt.applyLocalizedPattern("dd.MM.yyyy HH:mm:ss");

		String logMsg = fmt.format(now) + " : " + who + " - " + msg + (char) 13 + (char) 10;

		if (logFileLocation == null) {
			logToSystemOut(logMsg, e);

			return;
		}

		FileWriter fw;

		try {
			fw = new FileWriter(logFileLocation, append2File);
			append2File = true;
			PrintWriter pw = new PrintWriter(fw);

			pw.write(logMsg);

			if (e != null) {
				e.printStackTrace(pw);
			}

			pw.close();
			fw.close();

		} catch (AccessControlException ace) {
			logToSystemOut(logMsg, e);
			logToSystemOut(logMsg, ace);

		} catch (IOException ioe) {
			logToSystemOut(logMsg, e);
			logToSystemOut(logMsg, ioe);
		}
	}

	private static void logToSystemOut(String msg, Exception e) {
		System.out.println(msg);

		if (e != null) {
			e.printStackTrace(System.out);
		}
	}

	/**
	 * Sletter loggfilen hvis den ikke er i fra i dag.
	 */
	private void slettGammelLoggFil() {
		try {
			File fil = new File(logFileLocation);

			if (fil.exists()) {
				Calendar now = Calendar.getInstance();

				Calendar lastModified = Calendar.getInstance();
				lastModified.setTime(new Date(fil.lastModified()));

				if (now.get(Calendar.DAY_OF_MONTH) != lastModified.get(Calendar.DAY_OF_MONTH)) {
					append2File = false;
				}
			}
		} catch (AccessControlException e) {
			messageToUser(RUN_NO_ACCESS);
		}
	}


	/**
	 * Indre klasse for å skrive ut output fra StartBrevklient.bat
	 */
	class ProcessListener extends Thread {

		private static final String WHO_AM_I = "StartBrevklientApplet.ProcessListener";

		Process proc = null;
		StartBrevklientApplet mom = null;

		ProcessListener(Process proc, StartBrevklientApplet mom) {
			this.proc = proc;
			this.mom = mom;
		}

		public void run() {
			InputStream stream = proc.getInputStream();
			StringBuilder msg = new StringBuilder();

			try {

				int c;
				while ((c = stream.read()) != -1) {
					msg.append((char) c);
				}

				mom.messageToUser(StartBrevklientApplet.RUN_FINISHED);

			} catch (Exception e) {
				mom.messageToUser(StartBrevklientApplet.RUN_NOT_OK);
				StartBrevklientApplet.log(WHO_AM_I, "ERROR:" + e.getMessage(), e);
			}
		}
	}

}
