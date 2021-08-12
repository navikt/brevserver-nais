package no.nav.brevserver.fagarkiv.dokarkiv.model;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OppdaterJournalpostRequest {

	private String tittel;

	private String tema;

	private String behandlingstema;

	private AvsenderMottaker avsenderMottaker;

	private Bruker bruker;

	private Sak sak;

	private DokumentInfo[] dokumenter;
}