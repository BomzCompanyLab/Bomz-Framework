package kr.co.bomz.dbcp;

import java.sql.SQLException;

/**
 * Ʈ������� ���۵��� �ʾҴµ� Ŀ���̳� �ѹ��۾��� ��û�Ͽ��� ��� �߻�
 * 
 * @author ���ǳ�
 * @since 1.5
 * @version 1.5
 *
 */
public class NonTransactionException extends SQLException{

	private static final long serialVersionUID = -8342462174629627707L;

	public NonTransactionException(){}
}
