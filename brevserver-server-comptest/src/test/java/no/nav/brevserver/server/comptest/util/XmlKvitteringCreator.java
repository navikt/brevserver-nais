package no.nav.brevserver.server.comptest.util;

import no.nav.brevserver.server.common.config.Konstanter;

import org.apache.commons.lang.StringUtils;

public class XmlKvitteringCreator {

	private final int headerLength = Konstanter.MELDING_HEADER_LENGTH;
	private final char padChar = ' ';
	
	private String brevreferanse;
	private String systemId;
	private String contentType;
	private int feilniva;
	private int feilkode;

	public XmlKvitteringCreator brevreferanse(String value) {this.brevreferanse = value; return this;}
	public XmlKvitteringCreator systemId(String value) {this.systemId = value; return this;}
	public XmlKvitteringCreator contentType(String value) {this.contentType = value; return this;}
	public XmlKvitteringCreator feilniva(int value) {this.feilniva = value; return this;}
	public XmlKvitteringCreator feilkode(int value) {this.feilkode = value; return this;}
	
	public String create() {
		StringBuilder builder = new StringBuilder("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
		builder.append("<rtv-brevkvitt>");
		builder.append("<brevref>").append(brevreferanse).append("</brevref>");
		builder.append("<sysid>").append(systemId).append("</sysid>");
		builder.append("<type>").append(contentType).append("</type>");
		builder.append("<feilniva>").append(feilniva).append("</feilniva>");
		builder.append("<feilkode>").append(feilkode).append("</feilkode>");
		builder.append("</rtv-brevkvitt>");
		return StringUtils.rightPad(builder.toString(), headerLength, padChar);
	}
	
}
