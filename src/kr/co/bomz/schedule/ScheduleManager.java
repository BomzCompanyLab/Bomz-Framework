package kr.co.bomz.schedule;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import kr.co.bomz.custom.util.SupinanThread;
import kr.co.bomz.custom.util.SupinanThreadPool;
import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;


/**
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.4.1
 *
 */
public class ScheduleManager extends Thread{
	
	private static final Logger logger = Logger.getRootLogger();
	
	private final long SCHEDULE_SLEEP_TIME;
	
	private SupinanThreadPool scheduleThreadPool = new SupinanThreadPool(kr.co.bomz.schedule.ScheduleTimeCheck.class, this);	
	
	private Queue<ScheduleObject> scheduleQueue = new LinkedBlockingQueue<ScheduleObject>();
	private ArrayList<Long> deleteScheduleIdList = new ArrayList<Long>();
	
	private boolean close = false;
	
	ScheduleManager(long scheduleSleepTime){
		this.SCHEDULE_SLEEP_TIME = scheduleSleepTime;
		logger.log(Level.DEBUG, "[������ �Ŵ��� ����] SleepTime=", this.SCHEDULE_SLEEP_TIME);
	}
	
	boolean insertSchedule(ScheduleObject object){
		return this.scheduleQueue.offer( object );
	}
	
	boolean removeSchedule(long scheduleId){
		logger.log(Level.DEBUG, "[������ ����] ��������̵�=", scheduleId);
		return this.deleteScheduleIdList.add(scheduleId);
	}
	
	public void run(){
		
		int queueSize;
		ScheduleObject scheduleObject;
		long scheduleId;
		SupinanThread supinanThread;
		
		while( !this.close ){
			
			queueSize = scheduleQueue.size();
			
			for(int i=0; i < queueSize; i++){
				scheduleObject = this.scheduleQueue.poll();
				
				if( scheduleObject == null )		break;
				scheduleId = scheduleObject.getScheduleId();
				
				if( this.deleteScheduleIdList.indexOf(scheduleId) != -1 ){
					this.deleteScheduleIdList.remove(scheduleId);
					scheduleObject.removeSchedule();
				}
				
				supinanThread = this.scheduleThreadPool.getThread();
				supinanThread.start(scheduleObject);
			}
			
			try {
				Thread.sleep( SCHEDULE_SLEEP_TIME );
			} catch (InterruptedException e) {}
			
		}
		
		// ������ ���� �� ó��
		this.scheduleThreadPool.closeThreadPool();
		this.scheduleThreadPool = null;
		this.scheduleQueue = null;
		this.deleteScheduleIdList = null;
	}
	
	/**		������ �Ŵ��� ����		*/
	void closeScheduleManager(){
		this.close = true;
	}
}
