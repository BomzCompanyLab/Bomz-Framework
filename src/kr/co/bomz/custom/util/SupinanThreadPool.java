package kr.co.bomz.custom.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;


/**
 *
 * 	������ Ǯ <p>
 * 
 *  SupinanThread �� ��ӹ��� Ŭ������ �����ڿ� ���ڰ����� �Ѱ��ָ� �ȴ�<p>
 *  
 *  ���� ������ Ǯ�� ũ�⸦ �������� �ʾҴٸ� DEFAULT_POOL_MAX_SIZE ������<p>
 *  
 *  ������ ��ü�� �����ȴ�<p>
 *  
 *  ����� ���� getThread() �޼ҵ带 ���� ������ ��ü�� �Ѱܹ��� �� ������<p>
 *  
 *  ��� ������ �����尡 ���� ���� ��� ������ �����尡 ���涧���� ����Ѵ�
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.4.1
 *
 */
public class SupinanThreadPool {
	
	private static final Logger logger = Logger.getRootLogger();
	
	protected static final int DEFAULT_POOL_MAX_SIZE = 5;
	private int poolMaxSize;
	private SupinanQueue<SupinanThread> queue;

	private final Class<? extends SupinanThread> clazz;
	private final Object[] parameters;
	
	private boolean nonWait = true;
	
	// �������� ���� ����
	private boolean close = false;
	
	/*
	 * ���� ������� ������ ��ü
	 * close() ȣ�� �� ���� ���� �����带 ���� ������Ű�� ���� ���ȴ�
	 */
	private Set<SupinanThread> runningThread = Collections.synchronizedSet(new HashSet<SupinanThread>());
	
	public SupinanThreadPool(Class<? extends SupinanThread> clazz, Object ... parameters) throws RuntimeException{
		this(DEFAULT_POOL_MAX_SIZE, clazz, parameters);
	}

	public SupinanThreadPool(int poolMaxSize, Class<? extends SupinanThread> clazz, Object ... parameters) throws RuntimeException{
		this.clazz = clazz;
		this.parameters = parameters;
		
		this.init(poolMaxSize);
		this.execute(clazz, parameters);
	}
	
	private void init(int poolMaxSize){
		this.poolMaxSize = (poolMaxSize <= 0) ? DEFAULT_POOL_MAX_SIZE : poolMaxSize; 
		
		this.queue = new SupinanQueue<SupinanThread>(poolMaxSize);
	}
	
	private void execute(Class<? extends SupinanThread> clazz, Object ... parameters) throws RuntimeException{		
		for(int i=0; i < poolMaxSize; i++){
			try {
				this.queue.offer( this.createThread(true, clazz, parameters) );
			} catch (Exception e) {
				logger.log(Level.WARN, e, "������ ���� ���� ����");
				throw new RuntimeException();
			}
		}
	}
	
	private SupinanThread createThread(boolean isInit, Class<? extends SupinanThread> clazz, Object ... parameters) throws Exception{
		SupinanThread th = clazz.newInstance();
		if( parameters.length != 0 )			th.startInit(parameters);
		
		if( isInit )		th.setThreadPool(this);
		
		return th;
	}
	
	final synchronized void enqueue(SupinanThread thread){
		
		// �������� ������ ����
		this.runningThread.remove(thread);
		
		if( this.close )		return;		// ���� ������Ǯ�� ���� ������ ���
		
		if( thread != null ){
			queue.offer(thread);
			notify();
		}else{
			logger.log(Level.WARN, "NULL ���� ������ ��ü �߰��� ��û");
		}
	}
	
	/**
	 * 
	 * ������Ǯ���� ���� ���� ������ ��û<p>
	 * 
	 * ��� �����尡 ��� ���� ��� �⺻������ ���ο� �����带 �����Ͽ� �����ϸ�<p>
	 * 
	 * setNonWait(false) �� ȣ���Ͽ��� ��� ��� ���� �����尡 �ݳ��� ������ ����Ѵ�<p><p>
	 * 
	 * close() ȣ�� �� SupinanThreadPoolCloseException �� �߻��ϸ�<p>
	 * 
	 * ������Ǯ�� �ٽ� ����� ��� reset() �� ȣ���Ͽ� ������Ǯ ������ �˷����Ѵ�<p>
	 * 
	 * @return ������ ��ü
	 * @throws SupinanThreadPoolCloseException close() ȣ�� �� �ش� �޼ҵ� ȣ�� ��
	 * @throws RuntimeException ������ ���� ����
	 */
	public final synchronized SupinanThread getThread() throws SupinanThreadPoolCloseException, RuntimeException{
		
		// ���� �����ε� ������ ��û���� ��� ���� �߻�
		if( this.close )		throw new SupinanThreadPoolCloseException();
		
		SupinanThread thread;
		
		if( this.nonWait ){
			thread  = queue.poll();
			
			if( thread == null ){
				try {
					thread = createThread(false, this.clazz, this.parameters);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
		}else{
			while(true){
				if( queue.size() <= 0 ){
					// �ڿ��� ���� ��� enqueue �� ���� �ڿ��� �߰��ɶ����� ���
					try {
						if( queue.size() <= 0 )		wait();
					} catch (InterruptedException e) {}
				}
				
				thread  = queue.poll();
				if( thread  != null )	break;
			}
			
		}
		
		this.runningThread.add(thread);
		return thread;
	}
	
	public int size(){
		return queue.size();
	}
	
	/**		
	 * 		��� �����尡 ������� �� ��û�� ���� �������� �����带 ��ٸ��� �ƴϸ�
	 * 		���ο� �����带 �������� ����
	 * 		 
	 * @return	true �� ��� ���ο� �����带 ����
	 */
	public boolean isNonWait(){
		return this.nonWait;
	}
	
	/**		
	 * 		��� �����尡 ������� �� ��û�� ���� �������� �����带 ��ٸ��� �ƴϸ�
	 * 		���ο� �����带 �������� ����
	 * 
	 * 		true �� ���� �� ��� �����尡 ������̶�� ���ο� �����带 �����Ѵ�
	 * 		�⺻�� true
	 * 		 
	 */
	public void setNonWait(boolean nonWait){
		this.nonWait = nonWait;
	}
	
	/**
	 * 		������Ǯ���� �����ϴ� ��� �����带 �����Ų��
	 */
	public void closeThreadPool(){
		this.close = true;
		// �������� ������ ���� ���� �۾� ����
		java.util.Iterator<SupinanThread> iter = this.runningThread.iterator();
		while( iter.hasNext() ){
			iter.next().coercionClose(false);		// ���� ����
		}
		
		// ������� ������ ���� ���� �۾� ����
		while( !this.queue.isEmpty() ){
			this.queue.poll().coercionClose(true);
		}
	}
	
	/**
	 * close() �� ���� ����� �����Ǿ��� ������Ǯ�� ����� ȣ��
	 * 
	 * @throws SupinanThreadPoolCloseException close ���� ���� ���¿��� ȣ�� �� ���ܰ� �߻��Ѵ�
	 */
	public void reset() throws SupinanThreadPoolCloseException{
		// close() �� ȣ��Ǿ������� Ȯ���Ѵ�
		if( !this.close )		throw new SupinanThreadPoolCloseException();
				
		this.execute(this.clazz, this.parameters);
		
		this.close = false;		// ���� ���� ����
	}
	
	public boolean isClose(){
		return this.close;
	}
}
