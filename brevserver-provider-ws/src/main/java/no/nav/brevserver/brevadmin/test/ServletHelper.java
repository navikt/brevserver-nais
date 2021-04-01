package no.nav.brevserver.brevadmin.test;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

/**
 * @author Rune Røren, Accenture
 * @version $Revision: 2145 $ $Author: t133126 $ $Date: 2013-07-23 12:06:48 +0200 (ti, 23 jul 2013) $
 */
public class ServletHelper {

	public static String getRequestValueAndSetSession(HttpServletRequest request, String varName, String defaultValue,
			boolean useSession) {
		String result = request.getParameter(varName);

		if (useSession) {
			result = (result == null) ? (String) request.getSession().getAttribute(varName) : result;
		}
		result = (result == null) ? defaultValue : result.trim();

		if (useSession) {
			request.getSession().setAttribute(varName, result);
		}

		return result;
	}

	// Retrieve the version number of the project which has been written to the manifest
	public static String getVersionNumber(PageContext pageContext) {
		Manifest manifest = new Manifest();
		try {
			manifest.read(pageContext.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Attributes attributes = manifest.getMainAttributes();
		return attributes.getValue("Implementation-Version");
	}

}
