package no.nav.brevserver.joark;

import lombok.extern.slf4j.Slf4j;
import no.nav.brevserver.consumer.DokarkivConsumer;
import no.nav.brevserver.consumer.SettBrevdataResponse;
import no.nav.brevserver.consumer.saf.GraphQLRequest;
import no.nav.brevserver.consumer.saf.SafConsumer;
import no.nav.brevserver.consumer.saf.SafDokument;
import no.nav.brevserver.consumer.saf.SafJournalpost;
import no.nav.brevserver.core.exception.BrevFinnesIkkeException;
import no.nav.brevserver.core.exception.BrevserverTechnicalException;
import no.nav.brevserver.core.vo.BrevVO;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.singletonMap;
import static no.nav.brevserver.core.vo.FilType.PDF;

@Qualifier("dokarkivService")
@Service
@Slf4j
public class DokarkivServiceImpl implements JoarkService {
	private static final List<String> JOURNALFOERT_VARIANT_FORMAT_PRIORITET = List.of("SLADDET", "ARKIV");
	private static final String JOURNALSTATUS_AVBRUTT = "AVBRUTT";
	private static final String JOURNALSTATUS_UNDER_ARBEID = "UNDER_ARBEID";
	private static final Set<String> ENDELIGE_JOURNALSTATUSER = Set.of("JOURNALFOERT", "FERDIGSTILT", "EKSPEDERT");
	public static final String VARIANT_FORMAT_PRODUKSJON = "PRODUKSJON";
	private final SafConsumer safConsumer;
	private final DokarkivConsumer dokarkivConsumer;
	private final byte[] PDF_MED_FORKLARING;


	public DokarkivServiceImpl(SafConsumer safConsumer,
							   DokarkivConsumer dokarkivConsumer) throws IOException {
		PDF_MED_FORKLARING = new ClassPathResource("/static/rtf-konvertering-sanert-forklaring.pdf").getInputStream().readAllBytes();
		this.safConsumer = safConsumer;
		this.dokarkivConsumer = dokarkivConsumer;
	}

	@Override
	public void lagreDokument(String brevreferanse, String contentType, byte[] brevdata) {
		settBrevdata(brevreferanse, contentType, brevdata);
	}

	@Override
	public void lagreFerdigstiltDokument(String brevreferanse, BrevVO redBrevVO, BrevVO pdfBrevVO) {
		settBrevdata(brevreferanse, redBrevVO.getContentType(), redBrevVO.getBrevdata());
		settBrevdata(brevreferanse, pdfBrevVO.getContentType(), pdfBrevVO.getBrevdata());
	}

	private void settBrevdata(String brevreferanse, String contentType, byte[] brevdata) {
		SettBrevdataResponse settBrevdataResponse = dokarkivConsumer.settBrevdata(Long.parseLong(brevreferanse), contentType, brevdata);
		log.info("Lagret dokument på brevreferanse={}. filUuid={}, contentType={}, filstørrelse={}",
				brevreferanse, settBrevdataResponse.filUuid(), contentType, settBrevdataResponse.filstoerrelse());
	}

	@Override
	public BrevVO hentDokument(String brevreferanse) {
		SafJournalpost safJournalpost = safConsumer.performQuery(graphQLRequest(brevreferanse));
		if (JOURNALSTATUS_AVBRUTT.equals(safJournalpost.getJournalstatus())) {
			return statiskPdfMedForklaring(brevreferanse);
		}

		SafJournalpost.DokumentInfo hoveddokument = safJournalpost.getDokumenter().stream().findFirst()
				.orElseThrow(() -> new BrevserverTechnicalException("Finner ikke hoveddokument for journalpostId " + brevreferanse));
		String dokumentInfoId = hoveddokument.getDokumentInfoId();
		Optional<String> variantFormat = mapVariantFormat(safJournalpost.getJournalstatus(), hoveddokument);
		if(variantFormat.isPresent()) {
			SafDokument safDokument = safConsumer.hentDokument(brevreferanse, dokumentInfoId, variantFormat.get());
			return mapBrevVo(brevreferanse, safDokument);
		} else {
			throw new BrevFinnesIkkeException(brevreferanse);
		}
	}

	private BrevVO mapBrevVo(String brevreferanse, SafDokument safDokument) {
		BrevVO brevVO = new BrevVO();
		brevVO.setBrevreferanse(brevreferanse);
		brevVO.setContentType(mapContentType(safDokument.contentType()));
		brevVO.setBrevdata(safDokument.dokument());
		return brevVO;
	}

	private static @NonNull String mapContentType(@NonNull MediaType contentType) {
		if(contentType.toString().equals("application/rtf")) {
			return "text/rtf";
		}
		return contentType.toString();
	}

	protected static Optional<String> mapVariantFormat(String journalstatus, SafJournalpost.DokumentInfo hoveddokument) {
		Set<String> variantformater = hoveddokument.getDokumentvarianter().stream()
				.map(SafJournalpost.DokumentInfo.Dokumentvariant::getVariantformat)
				.collect(Collectors.toSet());

		if(JOURNALSTATUS_UNDER_ARBEID.equals(journalstatus)) {
			return Optional.of(VARIANT_FORMAT_PRODUKSJON);
		} else if(ENDELIGE_JOURNALSTATUSER.contains(journalstatus)) {
			return JOURNALFOERT_VARIANT_FORMAT_PRIORITET.stream()
					.filter(variantformater::contains)
					.findFirst();
		}
		return Optional.empty();
	}

	private GraphQLRequest graphQLRequest(String journalpostId) {
		return GraphQLRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(singletonMap("journalpostId", journalpostId))
				.build();
	}

	private BrevVO statiskPdfMedForklaring(String brevreferanse) {
		BrevVO brevVO = new BrevVO();
		brevVO.setBrevreferanse(brevreferanse);
		brevVO.setContentType(PDF.getContentType());
		brevVO.setBrevdata(PDF_MED_FORKLARING);
		return brevVO;
	}

	private static final String JOURNALPOST_QUERY = """
			query journalpost($journalpostId: String!) {
			  journalpost(journalpostId: $journalpostId) {
			    journalpostId
			    journalstatus
			    dokumenter {
			      dokumentInfoId
			      dokumentvarianter {
			        variantformat
			      }
			    }
			  }
			}
			""";
}
