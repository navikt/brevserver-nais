package no.nav.brevserver.dokarkiv.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidMappingToEnumFunctionalException extends AbstractDokdistfordelingFunctionalException {
	public InvalidMappingToEnumFunctionalException(String message) {
		super(message);
	}
}
