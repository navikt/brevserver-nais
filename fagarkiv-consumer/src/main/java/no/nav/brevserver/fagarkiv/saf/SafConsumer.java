package no.nav.brevserver.fagarkiv.saf;

import no.nav.brevserver.server.common.exception.BrevFunctionalException;
import no.nav.brevserver.server.common.vo.BrevVO;

public interface SafConsumer {
	BrevVO hentDokument(String brevreferanse) throws BrevFunctionalException;
}
