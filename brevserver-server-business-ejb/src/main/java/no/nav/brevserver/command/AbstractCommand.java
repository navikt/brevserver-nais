package no.nav.brevserver.command;

import no.nav.brevserver.server.common.exception.BrevException;
import no.nav.brevserver.server.common.log.Log;
import no.nav.brevserver.server.common.vo.MessageVO;

/**
 * Definerer en Command. Det inneholder tilstanden til en operasjon, data og navn på operasjon. Navnet er brukt for å mappe en
 * Command til den rette business handler.
 *
 * @author Morten Lileng, Cap Gemini Ernst & Young Copyright (c) Trygdeetaten, 2002
 */
public abstract class AbstractCommand {
	protected MessageVO messageVo = null;
	protected Log log = new Log(getClass());

	protected AbstractCommand(MessageVO message) {
		messageVo = message;
	}

	/**
	 * Eksekverer en Command. Implementasjonen har logikk for oppkobling og sending av et kommandoobjekt eller tilsvarende.
	 *
	 * @throws BrevException
	 */
	public abstract void execute() throws BrevException;

	/**
	 * Brukes for å hente ut resultatet av en operasjon
	 *
	 * @return Object
	 */
	public abstract Object getResult();

	protected void notEmpty(String name, String value, boolean checkIfValidNumber) throws BrevException {
		if (value == null) {
			return;
		} else if (value.equals("")) {
			throw new BrevException(name + " er blank (ikke null)");
		} else if (checkIfValidNumber) {
			try {
				Integer.parseInt(value);
			} catch (NumberFormatException e) {
				throw new BrevException(name + " er ikke et gyldig tall", e);
			}
		}
	}
}