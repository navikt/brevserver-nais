package no.nav.brevserver.server.common.file;

import no.nav.brevserver.server.common.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Rune Røren, Accenture
 * @version $Revision: 868 $ $Author: rra2920 $ $Date: 2005-11-21 08:24:54 +0100 (ma, 21 nov 2005) $
 */
public class FileUtil {

	static Log log = new Log(FileUtil.class);

	public static void copy(File src, File dest) throws IOException {
		String methSig = "copy(" + src.getAbsoluteFile() + ", " + dest.getAbsoluteFile() + ")";

		InputStream in = null;
		OutputStream out = null;

		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dest);

			byte[] buf = new byte[1024];
			int len = -1;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					log.error(methSig, "Fikk ikke lukket output-stream", e);
				}
			}

			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.error(methSig, "Fikk ikke lukket input-stream", e);
				}
			}
		}
	}


}
