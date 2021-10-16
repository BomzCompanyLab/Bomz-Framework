package kr.co.bomz.timer;

import kr.co.bomz.custom.util.SupinanThread;
import kr.co.bomz.custom.util.SupinanThreadPool;
import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;


/**
 * 
 * 		���� �ð��� �ֱ�� �ݺ� ȣ���ϴ� Ÿ�̸�
 * 
 * @author ���ǳ�
 * @since 1.2
 * @version 1.4.1
 *
 */
public class SupinanTimer{

	private static final Logger logger = Logger.getRootLogger();
	
	/*		Ÿ�̸Ӹ� �����ϱ� ���� ���̵�		*/
	private long timerId = 281L;
	
	private SupinanTimerRun threadObj;
	private java.util.Map<Long, Timer> timerInfoMap;
	
	private SupinanThreadPool timerThreadPool;
	
	private final long TIMER_SLEEP_TIME;
	
	private final Object lock = new Object();
	
	
	public SupinanTimer(){
		this(800);
	}
	
	/**
	 * Ÿ�̸� �ݺ� �˻� �ֱ⸦ ������ �� �ִ�
	 * �и�����Ʈ �����̸� 500���� ���� ��� �⺻ �� 500���� �����ȴ�
	 * 
	 * @param repeatCheckTime		������ �˻� ��� �ð�
	 */
	public SupinanTimer(long repeatCheckTime){
		this.TIMER_SLEEP_TIME = repeatCheckTime < 500 ? 500 : repeatCheckTime;
	}
	
	private final synchronized long getTimerId(){
		if( this.threadObj == null ){
			this.threadObj = new SupinanTimerRun();
			this.threadObj.start();
		}
		
		if( this.timerInfoMap == null )		this.timerInfoMap = new java.util.HashMap<Long, Timer>();
		
		if( this.timerThreadPool == null ){		// ù ���� �� ����� ����
			this.timerThreadPool = new SupinanThreadPool(3, kr.co.bomz.timer.TimerRunning.class, this);
			this.timerThreadPool.setNonWait(false);
		}else	 if( this.timerThreadPool.isClose() ){			// close ���¿��� ��� ����� 
			this.timerThreadPool.reset();
		}
		
		return this.timerId++;
	}
		
	/**		Ÿ�̸� �߰�		*/
	public long addTimer(Timer timer){
		synchronized( this.lock ){
			long timerId = this.getTimerId();
			timer.setTimerId( timerId );
			this.timerInfoMap.put(timerId, timer);
			
			logger.log(Level.DEBUG, "add supinan timer is " , timerId);
			return timerId;
		}
	}
	
	/**		Ÿ�̸� ����		*/
	void removeTimer(Timer timer){
		synchronized( this.lock ){
			this.timerInfoMap.remove(timer.getTimerId());
		}
		
		timer.stopTimer(TimerStopType.STOP_SYSTEM);
	}
	
	/**		Ÿ�̸� ����		*/
	public void removeTimer(long timerId){
		Timer timer;
		synchronized( this.lock ){
			timer = this.timerInfoMap.remove(timerId);		
		}
		
		if( timer != null ){
			logger.log(Level.DEBUG, "remove supinan timer is ", timerId);
			timer.stopTimer(TimerStopType.STOP_USER);
		}
	}
	
	/**
	 * Ÿ�̸� ����
	 */
	public void closeTimer(){
		synchronized( lock ){
			if( this.threadObj != null ){
				this.threadObj.run = false;
				this.threadObj = null;
			}
						
			if( this.timerInfoMap != null ){
				java.util.Iterator<Timer> iter = this.timerInfoMap.values().iterator();
				while( iter.hasNext() ){
					iter.next().stopTimer(TimerStopType.STOP_CLOSE);
				}
				this.timerInfoMap.clear();
			}
			
			if( this.timerThreadPool != null )	this.timerThreadPool.closeThreadPool();
		}
		
	}
	
	class SupinanTimerRun extends Thread{
				
		private boolean run = true;
		
		public void run(){
			
			Timer timer;
			SupinanThread timerThread;
			java.util.Iterator<Timer> timers;
			
			while( this.run ){
							
				synchronized( lock ){
					timers = timerInfoMap.values().iterator();
					while( timers.hasNext() ){
						timer = timers.next();
						if( timer.isRunningTime() ){
							timerThread = timerThreadPool.getThread();
							timerThread.start(timer);
						}
					}
				}
				
				// �������̶�� ��� ����
				if( this.run )			try{		Thread.sleep(TIMER_SLEEP_TIME);		}catch(Exception e){}
			}
			
			
			
		}
	}
}
