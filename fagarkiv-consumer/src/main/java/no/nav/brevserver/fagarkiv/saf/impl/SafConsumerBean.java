package no.nav.brevserver.fagarkiv.saf.impl;

import no.nav.brevserver.dokarkiv.SafJournalpostQueryService;
import no.nav.brevserver.dokarkiv.converter.HentDokumentToBrevVoConverter;
import no.nav.brevserver.dokarkiv.journalpost.Journalpost;
import no.nav.brevserver.dokarkiv.kodeverk.Variantformat;
import no.nav.brevserver.fagarkiv.reststs.StsRestConsumer;
import no.nav.brevserver.fagarkiv.saf.SafConsumer;
import no.nav.brevserver.fagarkiv.saf.hentdokument.HentDokumentConsumer;
import no.nav.brevserver.fagarkiv.saf.hentdokument.HentDokumentResponseTo;
import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.vo.BrevVO;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.brevserver.dokarkiv.constants.Constants.BEARER_PREFIX;
import static no.nav.brevserver.dokarkiv.kodeverk.Variantformat.ARKIV;
import static no.nav.brevserver.dokarkiv.kodeverk.Variantformat.PRODUKSJON;

@Service
public class SafConsumerBean implements SafConsumer {

	private final StsRestConsumer stsRestConsumer;
	private final SafJournalpostQueryService safJournalpostQueryService;
	private final HentDokumentConsumer hentDokumentConsumer;
	private final HentDokumentToBrevVoConverter converter;

	@Autowired
	public SafConsumerBean(StsRestConsumer stsRestConsumer, SafJournalpostQueryService safJournalpostQueryService, HentDokumentConsumer hentDokumentConsumer, HentDokumentToBrevVoConverter converter) {
		this.stsRestConsumer = stsRestConsumer;
		this.safJournalpostQueryService = safJournalpostQueryService;
		this.hentDokumentConsumer = hentDokumentConsumer;
		this.converter = converter;
	}

	@Override
	public BrevVO hentDokument(String brevreferanse) throws BrevFunctionalException {
		Journalpost journalpost = safJournalpostQueryService.hentJournalpost(brevreferanse, getAuthorizationHeader());
		Pair<String, Variantformat> dokumentInfoPair = hentDokumentInfoIdPrioritert(journalpost);
		HentDokumentResponseTo dokument = hentDokumentConsumer.hentDokument(brevreferanse, dokumentInfoPair.getLeft(), dokumentInfoPair.getRight().name());
		return converter.convert(dokument);
	}

	private Pair<String, Variantformat> hentDokumentInfoIdPrioritert(Journalpost journalpost) throws BrevFunctionalException {
		List<Journalpost.DokumentInfo> dokumentInfoIdList = journalpost.getDokumenter().stream().filter(d -> hasVariantKode(d.getDokumentvarianter(), ARKIV.toString())).collect(Collectors.toList());
		if(dokumentInfoIdList.size()>0){
			return Pair.of(dokumentInfoIdList.get(0).getDokumentInfoId(), ARKIV);
		}
		dokumentInfoIdList = journalpost.getDokumenter().stream().filter(d -> hasVariantKode(d.getDokumentvarianter(), PRODUKSJON.toString())).collect(Collectors.toList());
		if(dokumentInfoIdList.size()>0){
			return Pair.of(dokumentInfoIdList.get(0).getDokumentInfoId(), PRODUKSJON);
		}
		throw new BrevFunctionalException("Ingen dokumenter funnet");
	}

	private boolean hasVariantKode(List<Journalpost.Dokumentvariant> dokumentvarianter, String variantKode) {
		return dokumentvarianter.stream().filter(d -> d.getVariantformat()!=null&&d.getVariantformat().name().equals(variantKode)).findFirst().isPresent();
	}


	private String getAuthorizationHeader() {
		return BEARER_PREFIX + stsRestConsumer.getOidcToken();
	}
}
