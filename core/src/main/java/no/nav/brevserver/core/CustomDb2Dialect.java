package no.nav.brevserver.core;

import org.hibernate.dialect.DB2Dialect;

public class CustomDb2Dialect extends DB2Dialect {

	public String getSequenceNextValString(String sequenceName) {
		return "SELECT NEXT VALUE FOR " + sequenceName + " FROM SYSIBM.SYSDUMMY1";
	}

}