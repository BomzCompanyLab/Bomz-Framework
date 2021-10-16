package kr.co.bomz.custom.util;

import java.lang.Thread.State;

import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;


/**
 * ������Ǯ�� �̿��� ������� �ش� Ŭ������ ��ӹ޾� �����ؾ� �Ѵ�.<br>
 * 
 * execute() �� ������ ������ �����ϰ� start() �� ȣ���ϸ� �ȴ�.
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.4.1
 */
public abstract class SupinanThread{

	private static final Logger logger = Logger.getRootLogger();
	
	private SupinanThread _this;
	private Thread thread;
	private boolean run = false;
	protected SupinanThreadPool pool;
		
	// ���� ���� ����
	private boolean coercionClose = false;
	
	public SupinanThread(){
		this._this = this;
		
		this.thread = new Thread( new Runnable(){
			public void run(){
								
				while(true){
					
					if( coercionClose )		break;
					
					try {
						synchronized(thread){
							thread.wait();
						}
					} catch (InterruptedException e) {}
							
					if( coercionClose )		break;
					
					run = true;
					
					try {
						execute();
					} catch (Exception e) {
						logger.log(Level.WARN, e);
					}finally{
						threadClose();
					}
						
					if( pool == null )		break;
				}
				
			}
		});
		
		this.thread.start();
	}
	
	private final void threadClose(){
		this.close();
		this.poolEnqueue();		
	}
	
	// ������Ǯ�� �ش� �����带 ������ �����ϵ��� �ݳ��Ѵ�
	private void poolEnqueue(){
		if( pool != null ){
			run = false;
			pool.enqueue(_this);
		}
	}
	
	public final boolean start(Object ... parameters){
		if( this.run )			return false;
		
		try {
			this.initParameter(parameters);
		} catch (Exception e1) {
			// �ش� �����带 �ٽ� ť�� �߰�
			this.threadClose();
			logger.log(Level.WARN, e1, "�Ķ���� ���� �� ������ �߻��Ͽ����ϴ�");
			return false;
		}
		
		while( true ){
			if( thread.getState() == State.WAITING )	break;
			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {}
		}
		
		synchronized(thread){
			thread.notify();
		}
		
		return true;
	}
	
	final void setThreadPool(SupinanThreadPool pool){
		if( pool != null )	this.pool = pool;
	}
	
	/**
	 * SupinanThread ���� Ŭ���� new �� ���� ������ �ѹ��� ȣ��ȴ�.<p>
	 * 
	 * new SupinanThreadPool(SupinanThread.class , param1, param2, param3, ...)
	 * ���� param1 ~ ... ������ �ʱ�ȭ �� �� ���ȴ�
	 */
	protected void startInit(Object ... parameters) throws Exception{}
	
	/**
	 * ���� �ڵ�
	 */
	protected abstract void execute() throws Exception;
	
	/**
	 * �ڿ� �ݳ�
	 */
	protected void close(){}
	
	/**
	 * ������ ������ ���� �Ķ���� ����
	 * @param parameters
	 * @throws Exception	����ȯ ���н� ���� �߻�
	 */
	protected void initParameter(Object ... parameters) throws Exception{}
	
	/**
	 * SupinanThreadPool.close() ȣ�� �� �������� �������� ���� ���Ḧ ���� ȣ��
	 * @see kr.co.bomz.custom.util.SupinanThreadPool
	 */
	void coercionClose(boolean isSleep){
		this.coercionClose = true;		// ���� ���� ����
		
		if( isSleep ){
			// �����ִ� �����带 �����
			synchronized(thread){
				thread.notify();
			}
		}
	}
}
