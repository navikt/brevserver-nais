package no.nav.brevserver.fagarkiv.mapper;

import no.nav.brevserver.dokarkiv.journalpost.Journalpost;
import no.nav.brevserver.fagarkiv.dokarkiv.model.AvsenderMottaker;
import no.nav.brevserver.fagarkiv.dokarkiv.model.Bruker;
import no.nav.brevserver.fagarkiv.dokarkiv.model.DokumentInfo;
import no.nav.brevserver.fagarkiv.dokarkiv.model.OppdaterJournalpostRequest;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class OppdaterJournalRequestMapper implements Converter<Journalpost, OppdaterJournalpostRequest> {


	@Override
	public OppdaterJournalpostRequest convert(Journalpost journalpost) {
		return OppdaterJournalpostRequest.builder()
				.dokumenter(journalpost.getDokumenter()!=null?journalpost.getDokumenter().toArray(new DokumentInfo[journalpost.getDokumenter().size()]):null)
				//.journalfoerendeEnhet(journalpost.)
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(journalpost.getAvsenderMottaker().getId())
						.idType(journalpost.getAvsenderMottaker().getType()!=null?journalpost.getAvsenderMottaker().getType().name():null)
						.navn(journalpost.getAvsenderMottaker().getNavn())
						.build())
				.bruker(Bruker.builder()
						.id(journalpost.getBruker().getId())
						.idType(journalpost.getBruker().getType().name())
						.build())
				.tema(journalpost.getTema())
				//.behandlingstema()
				.tittel(journalpost.getTittel())
				//.sak
				.build();
	}
}
