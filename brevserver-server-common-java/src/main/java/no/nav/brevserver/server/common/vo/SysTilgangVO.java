package no.nav.brevserver.server.common.vo;

/**
 * @author Rune Røren, Accenture
 * @version $Revision: 1991 $ $Author: t133126 $ $Date: 2012-06-14 11:20:26 +0200 (to, 14 jun 2012) $
 */
public class SysTilgangVO {
	private String sysId = null;
	private String pwd = null;

	/**
	 * @return
	 */
	public String getPwd() {
		return pwd;
	}

	/**
	 * @return
	 */
	public String getSysId() {
		return sysId;
	}

	/**
	 * @param string
	 */
	public void setPwd(String string) {
		pwd = string;
	}

	/**
	 * @param string
	 */
	public void setSysId(String string) {
		sysId = string;
	}

	public Object clone() {
		SysTilgangVO result = new SysTilgangVO();
		
		result.sysId = sysId;
		result.pwd = pwd;
		
		return result;
	}

}
