package no.nav.brevserver.provider.itest;

import no.nav.brevserver.querydsl.TBrevlager5;
import no.nav.brevserver.querydsl.TBrevstatus;
import no.nav.brevserver.querydsl.TBrevtilgang;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;

public class BrevlagerPopulator {

	private static final String BLANK = "";
	private static final String BREVMAL = "TESTMAL";
	private static final String BRUKER = "TESTBRUKER";
	private static final String ARKIVER = "JA";
	private static final int KNAPPSTATUS = 42;

	public TBrevtilgang createBrevtilgang(String brevreferanse, String token, String systemId) {
		TBrevtilgang brevtilgang = new TBrevtilgang();
		brevtilgang.setBrevreferanse(brevreferanse);
		brevtilgang.setToken(token);
		brevtilgang.setSystemid(systemId);
		brevtilgang.setTimestamp(getNow());
		brevtilgang.setId(null);
		return brevtilgang;
	}

	public TBrevstatus createBrevstatus(String brevreferanse, String systemId, String status) {
		TBrevstatus brevstatus = new TBrevstatus();
		brevstatus.setBrevreferanse(brevreferanse);
		brevstatus.setSystemid(systemId);
		brevstatus.setStatus(status);
		brevstatus.setReturkoe(BLANK);
		brevstatus.setBrevmal(BREVMAL);
		brevstatus.setTimestamp(getNow());
		brevstatus.setBestillerbrukerid(BRUKER);
		brevstatus.setArkiver(ARKIVER);
		brevstatus.setKnappstatus(KNAPPSTATUS);
		brevstatus.setFormat(BLANK);
		return brevstatus;
	}

	public TBrevlager5 createBrevlager(String brevreferanse, String systemId, String status, String contentType, byte[] brevdata) {
		TBrevlager5 brevlager = new TBrevlager5();
		brevlager.setBlobid(null);
		brevlager.setBrevreferanse(brevreferanse);
		brevlager.setSystemid(systemId);
		brevlager.setStatus(status);
		brevlager.setBrevdata(bytesToBlob(brevdata));
		brevlager.setContenttype(contentType);
		brevlager.setTimestamp(getNow());
		brevlager.setBrukerid(BRUKER);
		return brevlager;
	}

	private Blob bytesToBlob(byte[] bytes) {
		try {
			return new SerialBlob(bytes);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Timestamp getNow() {
		return new Timestamp(System.currentTimeMillis());
	}
}
