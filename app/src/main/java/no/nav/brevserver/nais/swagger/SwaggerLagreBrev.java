package no.nav.brevserver.nais.swagger;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiOperation(
		value = "Oppretter brev i midlertidig brevlager")
@ApiResponses(value = {
		@ApiResponse(code = 201, message = "Created"),
		@ApiResponse(code = 400, message = "* Kan ikke opprette journalpost"),
		@ApiResponse(code = 401, message = "* Mangler tilgang til å opprette ny journalpost.\n* Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er UTGÅTT."),
		@ApiResponse(code = 403, message = "Bruker mangler tilgang til å opprette journalpost på tema"),
		@ApiResponse(code = 409, message = "Journalpost med angitt eksternReferanseId finnes allerede for angitt kanal.\nGjelder bare kanal SKAN_IM og HELSENETTET"),
		@ApiResponse(code = 500, message = "Internal server error")})
public @interface SwaggerLagreBrev {


}
