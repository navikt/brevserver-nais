package no.nav.brevserver.hentdokument;

public record Bilag(
		byte[] brevdata,
		String contentType
) {
}
