package no.nav.brevserver.consumer.saf;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GraphQLResponse {

	private SafJournalpostQueryData data;

	public SafJournalpost getJournalpost() {
		if (data == null) {
			return null;
		}
		return data.getJournalpost();
	}
}
