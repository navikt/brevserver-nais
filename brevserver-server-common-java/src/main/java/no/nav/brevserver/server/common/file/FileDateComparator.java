package no.nav.brevserver.server.common.file;

import java.io.File;
import java.util.Comparator;

public class FileDateComparator implements Comparator<File>{
	public int compare(File f1, File f2) {
		if (f1.lastModified() > f2.lastModified()) {
			return 1;
		} else {
			return -1;
		}
	}
}
