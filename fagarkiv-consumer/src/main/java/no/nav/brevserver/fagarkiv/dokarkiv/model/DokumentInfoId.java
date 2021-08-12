package no.nav.brevserver.fagarkiv.dokarkiv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class DokumentInfoId {

	private String dokumentInfoId; // NOSONAR Tillat samme navn som klassen.
}