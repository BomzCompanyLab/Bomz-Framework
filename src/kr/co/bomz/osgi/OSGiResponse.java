package kr.co.bomz.osgi;

/**
 * 		OSGi ���� ��û�� ���� ó�� ���
 * 
 * @author ���ǳ�
 * @since 1.4
 * @version 1.4
 *
 */
public class OSGiResponse {

	/**		OSGi ���� ��û ó�� ���� ����		*/
	private final boolean success;
	/**		OSGi ���� ��û ó�� ���� �� ���� ����		*/
	private final String errMsg;
	
	public OSGiResponse(){
		this.success = true;
		this.errMsg = null;
	}
	
	public OSGiResponse(String errMsg){
		this.success = false;
		this.errMsg = errMsg;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getErrMsg() {
		return errMsg;
	}
		
}
