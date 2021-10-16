package kr.co.bomz.dbcp;

import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 
 * java.sql.ResultSet �ڿ��ݳ��� �����ʾ� ����� �޸� ������ �������� Ŭ���� 
 * 
 * @author ���ǳ�
 * @since 1.5
 * @version 1.5
 *
 */
public class AutoCloseResult {

	private Statement statement;
	
	private ResultSet resultSet;
	
	private long autoCloseTime;
	
	AutoCloseResult(Statement statement, ResultSet resultSet, long autoCloseTime){
		this.statement = statement;
		this.resultSet = resultSet;
		this.autoCloseTime = autoCloseTime;
	}
	
	void closeResult(){
		if( this.resultSet != null ){
			try{		this.resultSet.close();		}catch(Exception e){}
			this.resultSet = null;
		}
		
		if( this.statement != null ){
			try{		this.statement.close();		}catch(Exception e){}
			this.statement = null;
		}
	}

	public long getAutoCloseTime() {
		return autoCloseTime;
	}

	public ResultSet getResultSet() {
		return resultSet;
	}
	
}
