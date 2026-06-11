package no.nav.brevserver.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

import static no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId.BREVREFERANSE_LENGTH;
import static no.nav.brevserver.core.domain.entities.id.BrevreferanseSystemCompositeId.SYSTEMID_LENGTH;

@Entity
@Table(name = "T_BREVLAGER_HISTORIKK")
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BrevlagerHistorikk {

	private static final String BREVLAGER_HISTORIKK_SEQ = "brevlager_historikk_seq";

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = BREVLAGER_HISTORIKK_SEQ)
	@SequenceGenerator(name = BREVLAGER_HISTORIKK_SEQ, sequenceName = BREVLAGER_HISTORIKK_SEQ, allocationSize = 1)
	@Column(name = "BREVLAGER_HISTORIK_ID", columnDefinition = "NUMBER(19,0)")
	private Long id;

	@Column(name = "BREVREFERANSE", length = BREVREFERANSE_LENGTH, nullable = false)
	private String brevreferanse;

	@Column(name = "SYSTEMID", length = SYSTEMID_LENGTH, nullable = false)
	private String systemId;

	@Column(name = "BRUKERID", length = 18)
	private String brukerId;

	@Column(name = "STATUS", length = 8, nullable = false)
	private String status;

	@Column(name = "CONTENTTYPE", length = 64)
	private String contentType;

	@Column(name = "BREVDATA", nullable = false)
	@Lob
	private byte[] brevdata;

	@Column(name = "VASKET", nullable = false)
	private Character vasket;

	@Column(name = "TIMESTAMP", nullable = false)
	private Timestamp endret;
}
