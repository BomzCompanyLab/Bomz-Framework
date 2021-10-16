package kr.co.bomz.dbcp;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

/**
 * 
 * @author ���ǳ�
 * @since 1.5
 * @version 1.5
 *
 */
public class DatabaseConnection implements CloseEvent{

	/**		Ŀ�ؼ� ���� ���̵�		*/
	private final long id;
	
	/**		�����ͺ��̽� Ŀ�ؼ�		*/
	private Connection conn;
	
	/**		Ʈ����� ������		*/
	private Savepoint savepoint = null;
		
	/**		������ Ŀ�ؼ� ȣ�� �ð�		*/
	private long lastCallTime = System.currentTimeMillis();
	
	DatabaseConnection(long id, Connection conn){
		this.id = id;
		this.conn = conn;
	}
	
	@Override
	public void close() {
		if( this.conn == null )		return;
		
		// ���� �����̹Ƿ� ���� Ʈ������� �����Ǿ� ���� ��� �ѹ� ó��
		if( this.savepoint != null ){
			try{
				this.conn.rollback(this.savepoint);
			}catch(Exception e){}
		}
		
		// �����ͺ��̽� ���� ����
		try{
			this.conn.close();
		}catch(Exception e){}
		
		this.savepoint = null;
	}

	@Override
	public long getId() {
		return this.id;
	}

	public boolean isStartTransaction() {
		return this.savepoint != null;
	}
	
	void startTransaction() throws SQLException{
		this.conn.setAutoCommit(false);
		this.savepoint = this.conn.setSavepoint();
	}
	
	/**
	 * �����ͺ��̽� Ŀ��
	 * @throws NonTransactionException		Ʈ������� ������� ���� ���¿��� ȣ�� �� �߻�
	 * @throws SQLException						�����ͺ��̽� ó�� �� ���� 
	 */
	void commit() throws NonTransactionException, SQLException{
		if( this.savepoint == null )		throw new NonTransactionException();
		try{
			this.conn.commit();
		}finally{
			this.savepoint = null;
			if( !this.conn.getAutoCommit() )
				try{		this.conn.setAutoCommit(true);	}catch(Exception e){}
		}
	}

	/**
	 * �����ͺ��̽� �ѹ�
	 * @throws NonTransactionException		Ʈ������� ������� ���� ���¿��� ȣ�� �� �߻�
	 * @throws SQLException						�����ͺ��̽� ó�� �� ���� 
	 */
	void rollback() throws NonTransactionException, SQLException{
		if( this.savepoint == null )		throw new NonTransactionException();
		try{
			this.conn.rollback(this.savepoint);
		}finally{
			this.savepoint = null;
			if( !this.conn.getAutoCommit() )
				try{		this.conn.setAutoCommit(true);	}catch(Exception e){}
		}
	}
	
	public boolean isClosed() throws SQLException{
		return this.conn.isClosed();
	}
	
	public Statement getStatement(String sql, StatementType type) throws SQLException{
		// ������ ȣ�� �ð� ����
		this.lastCallTime = System.currentTimeMillis();
		
		switch(type){
		case STATEMENT :						return this.conn.createStatement();
		case PREPARED_STATEMENT : return this.conn.prepareStatement(sql);
		case CALLABLE_STATEMENT :	return this.conn.prepareCall(sql);
		default :										throw new QueryTypeException(type.name());
		}
		
	}
		
	public long getLastCallTime(){
		return this.lastCallTime;
	}
}
