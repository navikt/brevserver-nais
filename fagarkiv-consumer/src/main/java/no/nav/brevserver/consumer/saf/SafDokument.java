package no.nav.brevserver.consumer.saf;

import org.springframework.http.MediaType;

public record SafDokument(byte[] dokument, MediaType contentType) {
}
