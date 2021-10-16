package kr.co.bomz.dbcp;

import java.sql.SQLException;

/**
 * ���ǵ��� ���� QueryType ���� ���Ǿ��� ��� �߻�
 * 
 * @author ���ǳ�
 * @since 1.5
 * @version 1.5
 *
 */
public class QueryTypeException extends SQLException{

	private static final long serialVersionUID = -5980858742103847200L;

	public QueryTypeException(String errMsg){
		super(errMsg);
	}
}
