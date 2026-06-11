package no.nav.brevserver.consumer.saf;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SafJournalpost {

	String journalpostId;
	String journalstatus;

	List<DokumentInfo> dokumenter = new ArrayList<>();

	@Data
	public static class DokumentInfo {
		String dokumentInfoId;
		List<Dokumentvariant> dokumentvarianter = new ArrayList<>();

		@Data
		public static class Dokumentvariant {
			String variantformat;
		}
	}
}
