package kr.co.bomz.dbcp;

import java.sql.SQLException;

/**
 * �����ͺ��̽� �������� �߸� �Ǿ��� ��� �߻�
 * @author ���ǳ�
 * @since 1.5
 * @version 1.5
 *
 */
public class DatabasePropertyException extends SQLException{

	private static final long serialVersionUID = -30591127992185993L;

	public DatabasePropertyException(){
		super();
	}
	
	public DatabasePropertyException(String errMsg){
		super(errMsg);
	}
	
	
}
