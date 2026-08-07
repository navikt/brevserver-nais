package no.nav.brevserver.hentdokument;

public class UgyldigSystemForHentDokumentException extends RuntimeException {

	public UgyldigSystemForHentDokumentException(String system) {
		super("System %s not found. Must be oebs or bisys".formatted(system));
	}
}
