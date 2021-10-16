package kr.co.bomz.dbcp;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import kr.co.bomz.custom.util.SupinanBlockingQueue;

/**
 * 
 * @author ���ǳ�
 * @since 1.5
 * @version 1.5
 *
 */
public class DatabasePool {
	
	/**	
	 * ��� Ŀ�ؼ��� ��� ���� ��� ����� �� �ִ� Ŀ�ؼ��� ��ȯ�� �� ���� ��� �ð� (ms)
	 */
	private long returnConnectionWaitTime = 5000;
	
	/**
	 * ���� java.sql.ResultSet ��ü�� �ڵ� �ڿ��ݳ� ó�� �ð�
	 * �⺻ �� = 15��
	 */
	private static final long AUTO_CLOSE_RESULTSET_TIME = 15000L;
	
	/**		Ŀ�ؼ� �ּ� ���� ��		*/
	private int minConnectionNumber = 5;
	/**		Ŀ�ؼ� �ִ� ���� ��		*/
	private int maxConnectionNumber = 15;
	
	/**
	 * ��� ��� ���� Ŀ�ؼ� ���� ť
	 */
	private Queue<DatabaseConnection> connectionQueue = new SupinanBlockingQueue<DatabaseConnection>();
	
	/**
	 * �����ͺ��̽� Ʈ����� ���� ��
	 * KEY : Thread.currentThread.getId()
	 * VALUE : ���̵� ���� Ŀ�ؼ� ���� ��ü
	 */
	private Map<Long, DatabaseConnection> transactionMap = new HashMap<Long, DatabaseConnection>();
	
	/**		�����ͺ��̽� ���� URL		*/
	private String databaseUrl;
	/**		�����ͺ��̽� ���� ���̵�		*/
	private String databaseId;
	/**		�����ͺ��̽� ���� ��ȣ		*/
	private String databasePassword;
	
	/**		�����ͺ��̽� Ŀ�ؼ� ���� ���̵� ������		*/
	private final IdGenerator idGenerator = new IdGenerator();
	
	/**		�������� ���� ���� ó���� ����ϴ� DBCP �Ŵ���		*/
	private final DatabasePoolManager manager = new DatabasePoolManager(this);
	
	/**		�������� �����ͺ��̽� ���� ������ ���� ���� ����		*/
	private String validationQuery = null;
	
	/**		validationQuery �˻� �ֱ� (�⺻�� : 1�ð�)		*/
	private long validationQueryTimeout = 3600000L;

	public DatabasePool(String url, String id, String pw) throws NullPointerException{
		if( url == null || url.trim().equals("") )		throw new NullPointerException("url is null");
		if( id == null || id.trim().equals("") )		throw new NullPointerException("id is null");
		if( pw == null || pw.trim().equals("") )		throw new NullPointerException("password is null");
		
		this.databaseUrl = url.trim();
		this.databaseId = id.trim();
		this.databasePassword = pw.trim();
	}
	
	/**
	 * �����ͺ��̽� Ǯ �ʱ�ȭ �۾�<br>
	 * �ش� �޼ҵ�� ���� �����ϸ�, �̸� �����ͺ��̽��� �ּ� ���� ����ŭ ���� �� �ش� Ǯ�� ����ϰ� ���� �� ���ȴ�
	 * 
	 * @throws SQLException							���� ���� �� �߻�
	 * @throws DatabasePropertyException		DB URL, ���̵�, ��ȣ�� �������� �ʾ��� ��� �߻�
	 */
	public void init() throws SQLException, DatabasePropertyException{
		for(int i=0; i < this.minConnectionNumber; i++){
			if( this.connectionQueue.size() >= this.minConnectionNumber )		break;
			
			this.returnConnection( this.newConnection() );
		}
	}
		
	/**		�������� �����ͺ��̽� ���� ������ ���� ���� ����		*/
	public void setValidationQuery(String validationQuery){
		this.validationQuery = validationQuery;
	}
	
	/**
	 * Ŀ�ؼ� �ּ� ���� ���� �ִ� ���� �� ����
	 * @param minConnectionNumber		�ּ� �� 1
	 * @param maxConnectionNumber		minConnectionNumber ���� ���� ��� minConnectionNumber + 1
	 */
	public void setConnectionNumber(int minConnectionNumber, int maxConnectionNumber) {
		if( minConnectionNumber <= 0 )		minConnectionNumber = this.minConnectionNumber;
		if( maxConnectionNumber <= minConnectionNumber )	maxConnectionNumber = minConnectionNumber + 1;
		
		this.minConnectionNumber = minConnectionNumber;
		this.maxConnectionNumber = maxConnectionNumber;
	}

	/**
	 * ��ȿ�� �˻� ���� �˻� �ֱ�<br>
	 * �⺻�� : 1�ð�
	 * �ּҰ� : 10��
	 * @param validationQueryTimeout ����:�� (��:10��=10)
	 */
	public void setValidationQueryTimeout(long validationQueryTimeout) {
		if( validationQueryTimeout <= 0 )		return;
		if( validationQueryTimeout < 10 )		validationQueryTimeout = 10;
		this.validationQueryTimeout = validationQueryTimeout * 60000;
	}

	/**		�����ͺ��̽� Ŀ�ؼ� �ݳ�		*/
	private void returnConnection(DatabaseConnection dc){
		if( dc == null )		return;
		
		if( this.idGenerator.getNowId() == dc.getId() ){
			// �������� �ʴ� ���̵� ���� ���
			
			if( dc.isStartTransaction() )		this.transactionMap.put(Thread.currentThread().getId(), dc);
			else											this.connectionQueue.offer(dc);
		}
//		else{
//			�����ؾ� �ϴ� ���̵� ���� ���
//			������ ó���� �ʿ� ������ DbcpManager ���� �ڵ� ���� ��Ų��	
//		}
	}
	
	/**
	 * DatabasePoolManager ���� ȣ���Ѵ�</br>
	 * �������� Ŀ�ؼ� �� ���� �ð��� ���� Ŀ�ؼ��� validationQuery �� ���� ��ȿ�� Ȯ��
	 */
	void checkConnectionValidation(){
		// ���� �ð����� �˻��� �ð��� �����Ѵ�
		long checkTime = System.currentTimeMillis() - this.validationQueryTimeout;
		CheckType checkType = null;
		
		// connectionQueue �� �������� Ŀ�ؼ� �˻�
		int size = this.connectionQueue.size();
		DatabaseConnection dc;
		
		for(int i=0; i < size; i++){
			dc = this.connectionQueue.poll();
			checkType = this.checkConnectionValidation( checkTime, dc, true );
			
			if( checkType == CheckType.SUCCESS ) 	this.returnConnection(dc);		// �˻� �Ϸ� Ŀ�ؼ��� �ݳ�
			else if( checkType == CheckType.FAIL )		break;
			else  	 dc.close();		// return is CheckType.PASS
		}
		
		// false �� �����Ǿ��� ���� ������ �����Ǿ��� �����
		if( checkType != null && checkType == CheckType.FAIL )		return;
		
		// transactionMap �� �������� Ŀ�ؼ� �˻�
		try{		// �����忡 ���� ó���̱⶧���� Map ���� ���� ó���� ������ �������� ����
			java.util.Iterator<DatabaseConnection> connections = this.transactionMap.values().iterator();
			while( connections.hasNext() ){
				// fail ���� �� ������ �����Ǿ��� �����
				if( this.checkConnectionValidation( checkTime, connections.next(), false ) == CheckType.FAIL)		break;
			}
		}catch(Exception e){}		// Ư���� ���� ó���� ����
	}
	
	/**		
	 * 		�ֱ������� ����� Ŀ�ؼ��� ������ ��� �ð��� Ȯ�� �� �����ð��̻�
	 * 		������ ��� validationQuery�� �����Ͽ� ���� ���� ������ ����
	 * @param checkTime		���� �ð� - validationQueryTimeout
	 * @param dc					Ŀ�ؼ� ����
	 * @param pass				�˻� �н� ����
	 * @return						���� �߻� �� false
	 */
	private CheckType checkConnectionValidation(long checkTime, DatabaseConnection dc, boolean pass){
		if( dc == null )		return CheckType.SUCCESS;
		
		// ������ ��� �ð��� �˻��� �ð��� ������ �ʾ��� ���
		if( checkTime <= dc.getLastCallTime() )		return CheckType.SUCCESS;
		
		try{
			if( pass && (this.minConnectionNumber < this.transactionMap.size() + this.connectionQueue.size())){
				// ���� �������� Ŀ�ؼ� ���� �ּ� ���� ������ ���� ��� �ش� Ŀ�ؼǸ� ���Ḧ ���� PASS ����
				return CheckType.PASS;
			}
			this.testValidationQuery(dc);
			return CheckType.SUCCESS;
		}catch(SQLException e){
			// ������ ���� ��� ���� ���� ó��
//			this.connectionQueue.offer(dc);		// �ٽ� ť�� ���� �ʴ´�. DatabasePoolManager �� ���������Ŵ
			this.disconnectDatabase();
			return CheckType.FAIL;
		}
	}
	
	/**		�����ͺ��̽� Ŀ�ؼ� ��û		*/
	private DatabaseConnection requestConnection() throws SQLException{
		
		// ������� Ʈ����� Ŀ�ؼ� ����
		DatabaseConnection resultConn = this.transactionMap.remove(Thread.currentThread().getId());
		if( resultConn != null )		return resultConn;
				
		// ���� ��� Ŀ�ؼ� ����
		while( true ){
			resultConn = this.connectionQueue.poll();
			if( resultConn == null )		break;		// ���� �����̳� ��� ���� Ŀ�ؼ� �ݳ� ���
			if( resultConn.getId() == this.idGenerator.getNowId() ){
				// ���� ���� Ŀ�ؼ� ��ü�� �ƴ� ���
				return resultConn;
			}
			// ���� ���� Ŀ�ؼ����� ��ϵǾ��ִ� ��� poll() �� �����͸� ������ ���� ���� ��ȯ
		}
		
		// ���� ���� Ŀ�ؼ� ���� �ִ�ġ�� ���� �ʾҴٸ� ���� �����Ͽ� ����
		if( this.transactionMap.size() + this.connectionQueue.size() < this.maxConnectionNumber )
			return this.newConnection();		// ���⼭ ���� �߻� ����
				
		// �ִ�ġ���� Ŀ�ؼ� �������̶�� ������ �ð� ���� ��� �� ��ȯ�� Ŀ�ؼ� ����
		resultConn = this.waitToReturnConnection();
		if( resultConn != null )		return resultConn; 
		
		// ����� �� �ִ� Ŀ�ؼ��� ���� ��� ���� �߻�
		throw new NotConnectionCanUseException();
	}
	
	
	/**		����� �� �ִ� Ŀ�ؼ��� ��ȯ�� ������ �����ð����� ����ϸ鼭 ��ȯ Ŀ�ؼ� Ȯ��		*/
	private DatabaseConnection waitToReturnConnection(){
		int repeatNumber = (int)this.returnConnectionWaitTime / 100;
		
		DatabaseConnection result;
		
		for(int i=0; i < repeatNumber; i++){
			try{		Thread.sleep(100);		}catch(Exception e){}
			result = this.connectionQueue.poll();
			if( result != null )		return result;
		}
		
		return null;
	}
	
	/**		
	 * ���ο� �����ͺ��̽� Ŀ�ؼ� ����
	 * 
	 * @return	�����ͺ��̽� Ŀ�ؼ� ��ü
	 * @throws SQLException	�����ͺ��̽� ���� ���� �� �߻�
	 * @throws DatabasePropertyException		�����ͺ��̽� ���� ������ �������� �ʾ��� ��� �߻�
	 */
	private DatabaseConnection newConnection() throws SQLException, DatabasePropertyException{
		// ���� �� �˻�
		if( this.databaseUrl == null || this.databaseUrl.equals("") )		throw new DatabasePropertyException("�����ͺ��̽� ���� URL ���� �������� �ʾҽ��ϴ�");
		if( this.databaseId == null || this.databaseId.equals("") )		throw new DatabasePropertyException("�����ͺ��̽� ���� ���̵� ���� �������� �ʾҽ��ϴ�");
		if( this.databasePassword == null || this.databasePassword.equals("") )		throw new DatabasePropertyException("�����ͺ��̽� ���� ��ȣ ���� �������� �ʾҽ��ϴ�");

		try{
			Connection conn = DriverManager.getConnection(this.databaseUrl, this.databaseId, this.databasePassword);
						
			DatabaseConnection result = new DatabaseConnection(this.idGenerator.getNowId(), conn);
			this.testValidationQuery(result);	// ���� ���� ���� ����
			
			this.manager.addCloseEvent(result);		// ���������� �̺�Ʈ ó���� �� �ֵ��� �Ŵ����� ���
			
			return result;
		}catch(SQLException e){
			// �� �κп��� ���ܰ� �߻����� ��� �����ͺ��̽� ������ ����Ȱɷ� �����Ѵ�
			this.disconnectDatabase();
			throw e;
		}
	}
	
	/**		���ο� ���� �� �׽�Ʈ ���� ����		*/
	private void testValidationQuery(DatabaseConnection dc) throws SQLException{
		if( this.validationQuery == null )		return;
		
		Statement st = null;
		ResultSet rs = null;
		try{
			st = dc.getStatement(null, StatementType.STATEMENT);
			rs = st.executeQuery(this.validationQuery);
		}catch(SQLException e){
			throw new SQLException("Validation Query : " + this.validationQuery, e);
		}finally{
			if( rs != null ){		try{		rs.close();		}catch(Exception e){}	}
			if( st != null ){		try{		st.close();		}catch(Exception e){}	}
		}
	}
	
	/**		�����ͺ��̽� ���� ����		*/
	private void disconnectDatabase(){
		
		long id = this.idGenerator.getNowId();
		
		// ���� �����ų ���̵� �Ŵ����� ���
		this.manager.runCloseEvent(id);
		
		// ���� ���̵� �� ����
		this.idGenerator.next();
	}
	
	/**
	 * Ʈ����� ����<br>
	 * �ش� �޼ҵ带 ȣ�������� �ݵ�� commit() �Ǵ� rollback() �� ȣ���ؾ� ��
	 * @throws SQLException Ŀ�ؼ��� ������ �� ���ų� Ʈ����� ���� �� �߻� ����
	 */
	public void startTransaction() throws SQLException{
		DatabaseConnection dc = this.requestConnection();
		dc.startTransaction();
		this.transactionMap.put(Thread.currentThread().getId(), dc);
	}
	
	/**
	 * �����ͺ��̽� Ŀ��
	 * @throws NonTransactionException		Ʈ������� ������� ���� ���¿��� ȣ�� �� �߻�
	 * @throws SQLException						�����ͺ��̽� ó�� �� ���� 
	 */
	public void commit() throws NonTransactionException, SQLException{
		DatabaseConnection dc = this.requestConnection();
		dc.commit();
		this.returnConnection(dc);
	}
	
	/**
	 * �����ͺ��̽� �ѹ�
	 * @throws NonTransactionException		Ʈ������� ������� ���� ���¿��� ȣ�� �� �߻�
	 * @throws SQLException						�����ͺ��̽� ó�� �� ���� 
	 */
	public void rollback() throws NonTransactionException, SQLException{
		DatabaseConnection dc = this.requestConnection();
		dc.rollback();
		this.returnConnection(dc);
	}
	
	/**
	 * java.sql.Statement �� �̿��� UPDATE / INSERT / DELETE ���� ����
	 * @param sql		����
	 * @return			���༺�� �ο� ��
	 * @throws SQLException
	 */
	public int queryToStatement(String sql) throws SQLException{
		return (int)this.executeQuery(StatementType.STATEMENT, false, sql);
	}
	
	/**
	 * java.sql.PraparedStatement �� �̿��� UPDATE / INSERT / DELETE ���� ����
	 * @param sql			����
	 * @param param		���� ���� �Ķ����
	 * @return				���༺�� �ο� ��
	 * @throws SQLException
	 */
	public int queryToPreparedStatement(String sql, Object ... param) throws SQLException{
		return (int)this.executeQuery(StatementType.PREPARED_STATEMENT, false, sql, param);
	}
	
	/**
	 * java.sql.CallableStatement �� �̿��� UPDATE / INSERT / DELETE ���� ����
	 * @param sql			����
	 * @param param		���� ���� �Ķ����
	 * @return				���༺�� �ο� ��
	 * @throws SQLException
	 */
	public int queryToCallableStatement(String sql, Object ... param) throws SQLException{
		return (int)this.executeQuery(StatementType.CALLABLE_STATEMENT, false, sql, param);
	}
	
	/**
	 * java.sql.Statement �� �̿��� ����Ʈ ���� ����
	 * @param sql			����
	 * @param param		���� ���� �Ķ����
	 * @return				ResutSet
	 * @throws SQLException
	 */
	public ResultSet selectQueryToStatement(String sql) throws SQLException{
		return this.selectQuery(StatementType.STATEMENT, sql);
	}

	/**
	 * java.sql.PreparedStatement �� �̿��� ����Ʈ ���� ����
	 * @param sql			����
	 * @param param		���� ���� �Ķ����
	 * @return				ResutSet
	 * @throws SQLException
	 */
	public ResultSet selectQueryToPreparedStatement(String sql, Object ... param) throws SQLException{
		return this.selectQuery(StatementType.PREPARED_STATEMENT, sql, param);
	}
	
	/**
	 * java.sql.CallableStatement �� �̿��� ����Ʈ ���� ����
	 * @param sql			����
	 * @param param		���� ���� �Ķ����
	 * @return				ResutSet
	 * @throws SQLException
	 */	
	public ResultSet selectQueryToCallableStatement(String sql, Object ... param) throws SQLException{
		return this.selectQuery(StatementType.CALLABLE_STATEMENT, sql, param);
	}
	
	/**		����Ʈ ���� ����		*/
	private ResultSet selectQuery(StatementType sType, String sql, Object ... param) throws SQLException{
		try{
			return ((AutoCloseResult)this.executeQuery(sType, true, sql, param)).getResultSet();
		}catch(DatabaseDisconnectException e){
			// ���� ���� ���� �� �ѹ� �� �õ��Ͽ� ������ ó�� �� �˻������� ������ �� �ְ� ��
			return ((AutoCloseResult)this.executeQuery(sType, true, sql, param)).getResultSet();
		}
	}
	
	/**		���� ����		*/
	private Object executeQuery(StatementType sType, boolean select, String sql, Object ... param) throws SQLException{
		DatabaseConnection dc = this.requestConnection();

		Statement st = dc.getStatement(sql, sType);
		
		Object result;
		
		try{
			if( sType == StatementType.STATEMENT ){
				// statement
				if( select )		result = new AutoCloseResult(st, st.executeQuery(sql), System.currentTimeMillis() + AUTO_CLOSE_RESULTSET_TIME);		// SELECT
				else				result = st.executeUpdate(sql);	// INSERT, UPDATE, DELETE
			}else{
				// preparedStatement or callableStatement
				PreparedStatement pst = (PreparedStatement)st;
				// �Ķ���� ����
				this.settingParameter(pst, param);
				// ���� ����
				if( select )		result = new AutoCloseResult(st, pst.executeQuery(), System.currentTimeMillis() + AUTO_CLOSE_RESULTSET_TIME);		// SELECT
				else				result = pst.executeUpdate();		// INSERT, UPDATE, DELETE
			}
			
			/*
			 * ����ڰ� resultSet.close() , st.close() �� ���� ���� ��� �޸𸮰� ���� �����ϹǷ�
			 * �ڵ����� close() �� ȣ���ϱ� ���� �Ŵ����� ��Ͻ�Ų��
			 */
			if( select )		this.manager.addAutoCloseResult((AutoCloseResult)result);
			
			return result;
		}catch(SQLException e){
			// ���� �߻� �� ���� �������� �������� �������� Ȯ���Ѵ�
			try{		st.close();		}catch(Exception e1){}		// ���� ���� ��� �ڿ��ݳ�
			if( this.checkConnectionClosed(dc) )		throw new DatabaseDisconnectException();		// ���������� ������ ������ ��� ó��
			else			throw e;		// �ٸ� ������ ���� ���
		}finally{
			if( !select )		try{		st.close();		}catch(Exception e1){}		// ResultSet ������ �ƴ� ��� �ڿ��ݳ�
			this.returnConnection(dc);			// Ŀ�ؼ� �ݳ�
		}
	}
	
	/**		�����ͺ��̽� ���� ���� �˻�		*/
	private boolean checkConnectionClosed(DatabaseConnection dc){
		try{
			if( !dc.isClosed() )		return false;
			
			// �����ͺ��̽� ������ ������ ���
			this.disconnectDatabase();
			
			return true;
		}catch(Exception e){
			// ������ �� ��쿡�� ������ ����ɷ� ó��
			this.disconnectDatabase();
			return false;
		}
	}
	
	/**
	 * �Ķ���� Ÿ�Կ� �´� setMethod�� ȣ���Ͽ� ������ ����
	 * @param pst			�����ͺ��̽� ��ü
	 * @param param		������ ���� �Ķ����
	 * @throws SQLException		setMethod ȣ�� �� �߻� ����
	 */
	private void settingParameter(PreparedStatement pst, Object ... param) throws SQLException{
		int length = param.length;
		for(int i=0; i < length; i++){
			if( param[i] == null )							pst.setString(i+1, null);
			else if( param[i] instanceof String )	pst.setString(i+1, (String)param[i]);
			else if( param[i] instanceof Integer)	pst.setInt(i+1, (int)param[i]);
			else if( param[i] instanceof Boolean)	pst.setBoolean(i+1,  (boolean)param[i]);
			else if( param[i] instanceof Float)	pst.setFloat(i+1,  (float)param[i]);
			else if( param[i] instanceof Long)		pst.setLong(i+1,  (long)param[i]);
			else if( param[i] instanceof Double)	pst.setDouble(i+1,  (Double)param[i]);
			else if( param[i] instanceof Short)	pst.setShort(i+1,  (Short)param[i]);
			else if( param[i] instanceof Byte)		pst.setByte(i+1,  (byte)param[i]);
			else if( param[i] instanceof Byte[])	pst.setBytes(i+1,  (byte[])param[i]);
			else if( param[i] instanceof Date)		pst.setDate(i+1,  (Date)param[i]);
			else if( param[i] instanceof Time)		pst.setTime(i+1,  (Time)param[i]);
			else if( param[i] instanceof Timestamp)		pst.setTimestamp(i+1,  (Timestamp)param[i]);
			else if( param[i] instanceof Object)	pst.setObject(i+1,  param[i]);
			else if( param[i] instanceof URL)		pst.setURL(i+1,  (URL)param[i]);
			else if( param[i] instanceof SQLXML)		pst.setSQLXML(i+1,  (SQLXML)param[i]);
			else if( param[i] instanceof Array)	pst.setArray(i+1,  (Array)param[i]);
			else if( param[i] instanceof BigDecimal)		pst.setBigDecimal(i+1,  (BigDecimal)param[i]);
			else if( param[i] instanceof Blob)		pst.setBlob(i+1,  (Blob)param[i]);
			else if( param[i] instanceof Clob)		pst.setClob(i+1,  (Clob)param[i]);
			else if( param[i] instanceof Ref)		pst.setRef(i+1,  (Ref)param[i]);
			else if( param[i] instanceof RowId)	pst.setRowId(i+1,  (RowId)param[i]);
			else if( param[i] instanceof NClob)	pst.setNClob(i+1,  (NClob)param[i]);
			else													pst.setObject(i+1, param[i]);
		}
	}
			
}
