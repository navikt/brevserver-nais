package no.nav.brevserver.core.vo;

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
