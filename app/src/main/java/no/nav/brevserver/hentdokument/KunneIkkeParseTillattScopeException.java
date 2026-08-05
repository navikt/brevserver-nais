package no.nav.brevserver.hentdokument;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class KunneIkkeParseTillattScopeException extends RuntimeException {
	public KunneIkkeParseTillattScopeException() {
		super();
	}

	public KunneIkkeParseTillattScopeException(String message) {
		super(message);
	}
}
