package no.nav.brevserver.fagarkiv.dokarkiv.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Klasse som definerer JSON-objektet som inneholder NAV-brukeren relatert til journalposten.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bruker {

    private String id;

    private String idType;
}