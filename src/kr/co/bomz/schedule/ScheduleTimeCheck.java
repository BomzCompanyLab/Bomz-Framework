package kr.co.bomz.schedule;

import java.util.Calendar;

import kr.co.bomz.custom.util.SupinanThread;
import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;


/**
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
public final class ScheduleTimeCheck extends SupinanThread{
	
	private static final Logger logger = Logger.getRootLogger();
	
	private ScheduleManager scheduleManager;
	
	private ScheduleObject scheduleObject;
	
	private Calendar calendar;
	
	public ScheduleTimeCheck(){}
	
	@Override
	protected void execute() throws Exception {
		
		if( this.scheduleObject == null )								return;
		if( this.scheduleObject.getSchedule() == null )			return;
		
		if( this.scheduleObject.getScheduleState() == ScheduleState.STOP ){
			// �����ڿ� ���� ������ �������� ����Ǿ��� ���
			this.scheduleObject.getSchedule().destroy();
			return;
		}
		
		if( _execute() ){
			// ���� ������ ������ ���� ������Ŵ����� ���
			this.scheduleManager.insertSchedule( this.scheduleObject );
		}else{
			// ������ ����
			// �ڿ� ����
			this.scheduleObject.getSchedule().destroy();
		}
		
		
		
//		System.out.print(this.scheduleObject.getRepeatType() + " /  " );
//		int[][] aa = this.scheduleObject.getHourAndMinute();
//		for(int i=0; i < aa.length; i++){
//			System.out.print(aa[i][0] + ":" + aa[i][1] +"  " );
//		}
//		System.out.println();
		
	}
	
	private boolean _execute(){
		
		this.calendar = Calendar.getInstance();
		
		// ������ ���� ��¥�� �������� �˻�
		if( !this.checkStartDate() )			return true;
		// ������ ���� ��¥�� �Ǿ����� �˻�
		if( !this.checkEndDate() )			return false;
		// ������ ������ ���� ��¥�� �´��� �˻�
		// ���� ��¥�� �´ٸ� ���� �ð��� �´����� �˻�
		try{
			// ��¥ �˻�
			if( !this.checkRepeat() )			return true;
			// �ð� �˻�
			if( !checkHourAndMinute() )		return true;
		}catch(Exception e){
			// ���� �߻� �� �ݺ� �ֱ� �˻縦 ���� ��ü�� ���̰ų� �߸��Ǿ��� �����
			// �̷� ��� �������� ������
			logger.log(Level.WARN, e, "������ ���� ��¥ �� �ð� �˻� �� ���� �߻�");
			return false;
		}
		
		// ��� ���ǿ� �����Ƿ� ������ ����
		this.scheduleObject.getSchedule().execute();
		
		return true;
	}
	
	private boolean checkHourAndMinute() throws Exception{
		int[][] hourAndMinute = this.scheduleObject.getHourAndMinute();
		if( hourAndMinute == null ){
			logger.log(Level.WARN, "[������ ���� ���� �ð� ���� NULL ���̹Ƿ� �������� �����մϴ�] ��������̵� : ", this.scheduleObject.getScheduleId());
			throw new Exception();
		}

		int nowHour = this.calendar.get(Calendar.HOUR_OF_DAY);
		int nowMinute = this.calendar.get(Calendar.MINUTE);
		
		int length = hourAndMinute.length;
		
		for(int i=0; i < length; i++){
			if( hourAndMinute[i][0] == nowHour && hourAndMinute[i][1] == nowMinute ){
				// ���� �ð��� ���� �ð��� ���� ���
				if( this.scheduleObject.checkExecuteHourAndMinute(nowHour, nowMinute) ){
					// ���� ������� ���� �ð��̹Ƿ� ������ ����
					this.scheduleObject.addHourAndMinute(nowHour, nowMinute);
					return true;
					
				}else{
					// �̹� �����ߴ� �ð��̹Ƿ� �������� �������� ����
					break;
				}
				
			}
		}
		
		return false;
	}
	
	/**
	 * 	������ �ݺ� �ֱ� �˻縦 �����Ͼ��ϴ��� Ȯ���Ѵ�
	 */
	private boolean checkRepeat() throws Exception{
		int nowDay = this.calendar.get(Calendar.DAY_OF_MONTH);
		// ������ �̹� ���� �ݺ� ���θ� �˻��Ͽ��ٸ� ���Ӱ� �˻��� �ʿ䰡 ����
		if( this.scheduleObject.getBeforeDay() == nowDay )		return this.scheduleObject.isCheckRepeat();
		
		// ������ �����ߴ� ������ �ð��� �ʱ�ȭ�Ѵ�.
		this.scheduleObject.clearHourAndMinute();
		
		boolean result = this.checkRepeat(this.scheduleObject.getRepeatType());
		this.scheduleObject.setCheckRepeat( result );
		
		// ���� �˻� ��¥�� ���� ��¥�� ����
		this.scheduleObject.setBeforeDay( nowDay );
		
		return result;
	}
	
	private boolean checkRepeat(ScheduleRepeatType repeatType) throws Exception{
		// �ݺ� �ֱ� ��Ŀ� ���� �б�
		switch( repeatType ){
		case REPEAT_DAY:		// ������ �ֱ�� ����
			return this.checkRepeatDay();
			
		case DAYS:						// Ư�� ������ ����
			return this.checkDays();
			
		case WEEKS:					// Ư�� ���Ͽ��� ����
			return this.checkWeeks();
			
		default:
			// ���������� ������ ��� �̰����� ���� �ʾƾ� ��. ������ ������ ���� ó��
			// �α״� ���� �߻� ��ġ���� ���
			logger.log(Level.WARN, "[������ �ݺ� ������ ���� �������� ��� NULL ���̹Ƿ� �������� �����մϴ�] ��������̵� : ", this.scheduleObject.getScheduleId());
			throw new Exception();
		}
	}
	
	/**
	 * 	������ �ֱ�� �������� ������ ��� �������� ������ ���� �Ǿ����� �˻��Ѵ�.
	 * 	������ ���� �� ������ ������ ���� ��¥�� �����Ѵ�
	 */
	private boolean checkRepeatDay() throws Exception{
		Integer repeatDay = this.scheduleObject.getRepeatDay();
		if( repeatDay == null ){
			logger.log(Level.WARN, "[������ ���� ���� ���� �� ���� �Ⱓ���� ���� ������ NULL ������ �Ǿ� �־� �������� �����մϴ�] ��������̵� : ", this.scheduleObject.getScheduleId());
			throw new Exception();
		}
		
		int nextRepeatDay = this.scheduleObject.getNextRepeatDay();
		
		if( nextRepeatDay == -1 || 
				( 
						nextRepeatDay == this.calendar.get(Calendar.DAY_OF_MONTH) &&
						this.scheduleObject.getNextRepeatYear() == this.calendar.get(Calendar.YEAR) && 
						this.scheduleObject.getNextRepeatMonth() == (this.calendar.get(Calendar.MONTH) + 1)
				)
				){
			// ù �������̰ų� ���࿹������ ���� ��¥�� ���� ��� 
			// ���� ���� ��¥�� �����Ѵ�.
			Calendar c = Calendar.getInstance();
			// 86400000 �� �Ϸ� 24�ð��� �и�������� ǥ���� ��
			c.setTimeInMillis( this.calendar.getTimeInMillis() + (86400000*repeatDay) );
			this.scheduleObject.setNextRepeatDay(c.get(Calendar.YEAR), c.get(Calendar.MONTH + 1), c.get(Calendar.DAY_OF_MONTH) );
			
			return true;
		}else{
			// ���� ������ ���� ��¥�� ���� �ʾ���.
			return false;
		}
		
	}
	
	/**
	 * 	�������� ������ ������ �Ǿ����� �˻��Ѵ�
	 */
	private boolean checkWeeks() throws Exception{
		// ���� ������ �����´�. 
		// -1 �� �ϴ� ������ enum �� ���� 0 ���� �����ϱ� ����
		// ���ϰ��� 1 ���� �����ϸ� �Ͽ����� ������
		int nowWeek = this.calendar.get(Calendar.DAY_OF_WEEK) - 1;
		
		ScheduleWeek[] weeks = this.scheduleObject.getWeeks();
		if( weeks == null ){
			logger.log(Level.WARN, "[������ ���� ���� ���� �� ���Ϸ� ���� ������ NULL ������ �Ǿ� �־� �������� �����մϴ�] ��������̵� : ", this.scheduleObject.getScheduleId());
			throw new Exception();
		}
		
		for(ScheduleWeek week : weeks){
			if( nowWeek == week.ordinal() )		return true;
		}
		
		return false;
	}
	
	/**
	 * 	�������� ������ ��¥�� �Ǿ����� �˻��Ѵ�
	 */
	private boolean checkDays() throws Exception{
		int nowDay = this.calendar.get(Calendar.DAY_OF_MONTH);
		int[] days = this.scheduleObject.getDays();
		
		if( days == null ){
			logger.log(Level.WARN, "[������ ���� ���� ���� �� ��¥�� ���� ������ NULL ������ �Ǿ� �־� �������� �����մϴ�] ��������̵� : ", this.scheduleObject.getScheduleId());
			throw new Exception();
		}
		
		for(int day : days){
			if( day == nowDay )	return true;
		}
		
		return false;
	}
	
	/**
	 * 		�������� ���� ��¥�� �������� �˻��Ѵ�
	 */
	private boolean checkStartDate(){
		
		if( this.scheduleObject.isCheckStartDate() )		return true;
		
		if( this.calendar.getTimeInMillis() >= this.scheduleObject.getStartTime() ){
			this.scheduleObject.changeCheckStartDate();
			return true;
		}else{
			return false;
		}
		
	}
	
	/**
	 * 		�������� ���� ��¥�� �Ǿ����� �˻��Ѵ�
	 */
	private boolean checkEndDate(){
		
		long value = this.scheduleObject.getEndTime();
		if( value == -1 )		return true;
		
		if( this.calendar.getTimeInMillis() >= value )		return false;
		else																			return true;
	}
	
	@Override
	protected void startInit(Object... parameters) throws Exception {
		this.scheduleManager = (ScheduleManager)parameters[0];
	}

	@Override
	protected void close() {
		this.calendar = null;
	}
	
	@Override
	protected void initParameter(Object... parameters) throws Exception {
		this.scheduleObject = (ScheduleObject)parameters[0];
	}

}
