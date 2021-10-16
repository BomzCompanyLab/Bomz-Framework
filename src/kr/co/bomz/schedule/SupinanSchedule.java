package kr.co.bomz.schedule;

import java.util.ArrayList;
import java.util.Calendar;

import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;


/**
 * 
 *  ���ǿ� �´� �ð��� �Ǹ� Schedule �������̽��� �����ϴ� �����췯<p>
 *  
 *  �����쿡 ��밡���� ���ǿ��� ������ �͵��� �ִ�<p>
 *  
 *  1. Ư�� ��¥ Ư�� �ð����� ����<p>
 *  2. Ư�� ���� Ư�� �ð����� ����<p>
 *  3. ������ ��� ������ �������� Ư�� ��/�ð� �ֱ�� ����<p>
 *  4. ������ ��� ������ �������� Ư�� �� �ֱ�� ����<p>
 *  5. ��� ������ ���� ��¥�� ���� ��¥�� ����<p>
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.4.1
 *
 */
public final class SupinanSchedule{

	private static final Logger logger = Logger.getRootLogger();
	
	private ScheduleManager scheduleManager = null;
	
	// ��ϵ� �������� Ű ��
	// �⺻���� 2472182�� ������ ������ ����.
	private volatile long scheduleId = 2472182L;
	
	// �������� ���� ��� �ݺ� �ֱ�
	private long scheduleSleepTime;
	
	/*
	 *  �Ϸ絿�� ��� �ݺ��� ��� �Ϸ� �ִ� �ݼ� ���� ���Ѵ�.
	 *  �ִ� �ʴ� �ѹ��� ����ǹǷ� �Ϸ縦 �ʷ� ����ϸ� 86400
	 *  ������ ���� 90000 ���� ���� 
	 */
	private static final int MAX_REPEAT_SIZE = 90000;

	private final Object lock = new Object();
	
	public SupinanSchedule(){
		this(500);
	}
	
	/**
	 * ������ �ݺ� �˻� �ֱ⸦ ������ �� �ִ�
	 * �и�����Ʈ �����̸� 500���� ���� ��� �⺻ �� 500���� �����ȴ�
	 * 
	 * @param repeatCheckTime		������ �˻� ��� �ð�
	 */
	public SupinanSchedule(long repeatCheckTime){
		this.scheduleSleepTime = (scheduleSleepTime < 500) ? 500 : scheduleSleepTime ;
	}
		
	private long getScheduleId(){

		if( this.scheduleManager == null ){
			this.scheduleManager = new ScheduleManager( this.scheduleSleepTime );
			this.scheduleManager.start();
		}
		return this.scheduleId++;
	}
	
	/**
	 * 	�ش� ������ Ű���� ����ϴ� �������� �����췯���� �����Ѵ�
	 */
	public boolean removeSchedule(long scheduleKey){
		return this.scheduleManager.removeSchedule(scheduleKey);
	}
	
	private long addSchedule(Schedule schedule, int[][] hourAndMinute, 
			Integer repeatDay, int[] days, ScheduleWeek[] weeks,
			int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay){
		
		// start validate
		if( schedule == null ){
			logger.log(Level.WARN, "[������ ������ ���� ��ü�� NULL �Դϴ�]");
			return -1;
		}
			
		if( hourAndMinute == null ){
			logger.log(Level.WARN, "[������ ������ ���� �ð� �� �� ������ NULL �� �Դϴ�]");
			return -1;
		}
		
		if( hourAndMinute.length <= 0 ){
			logger.log(Level.WARN, "[�������� ������ �� �ִ� �ð� ������ �����ϴ�]");
			return -1;
		}
		
		int checkCount =0;
		
		if( repeatDay != null  ){
			if( repeatDay > 0 )			checkCount++;
			else{
				logger.log(Level.WARN, "[������ �ݺ� ������ ���� repeatDay ���� ������ 1���� Ŀ���մϴ�] ���簳�� : ", repeatDay);
				return -1;
			}
		}
		
		if( days != null ){
			int daysLength = days.length;
			if( daysLength > 31 || daysLength <= 0 ){
				logger.log(Level.WARN, "[������ �ݺ� ������ ���� days ���� ������ 1���� 31���̿��� �մϴ�] ���簳�� : ", daysLength);
				return -1;
			}else 
				checkCount++;
		}
		
		if( weeks != null ){
			int weeksLength = weeks.length;
			if( weeksLength > 7 || weeksLength <= 0 ){
				logger.log(Level.WARN, "[������ �ݺ� ������ ���� weeks ���� ������ 1���� 7���̿��� �մϴ�] ���簳�� : ", weeksLength);
				return -1;
			}else
				checkCount++;
		}
		
		if( checkCount != 1 ){
			logger.log(Level.WARN, "[�������� �����ϱ� ���� ������ �߸��Ǿ����ϴ�]");
			return -1;
		}
		
		// end validate
		
		// closeSchedule() �߰��� ���� ���� �߻� ������ ���� ����ȭ
		synchronized( this.lock ){
			long scheduleId = this.getScheduleId();
			
			final ScheduleObject object = new ScheduleObject(schedule, scheduleId);
			
			object.setRepeatDay(repeatDay);
			object.setDays(days);
			object.setWeeks(weeks);
			
			object.setHourAndMinute(hourAndMinute);
			
			long value = this.getTime(startYear, startMonth, startDay, true);
			if( value <= 0 )	return -1;
			object.setStartTime( value );
			
			if( endYear <= 0 && endMonth <= 0 && endDay <= 0 ){
				// ������ ���� �ð��� ���� ���
				object.setEndTime( -1 );
			}else{
				// ������ ���� �ð��� ���� ���
				value = this.getTime(endYear, endMonth, endDay, false);
				if( value <= 0 )	return -1;
				object.setEndTime( value );
			}
					
			logger.log(Level.DEBUG, "[������ ���] ��������̵�=", scheduleId);
			this.scheduleManager.insertSchedule( object );
		}
		return scheduleId;
	}
	
	private long getTime(int year, int month, int day, boolean startTime){
		if( year < 1900 ){
			logger.log(Level.WARN, "[��¥�� �⵵�� 1900 �� ������ �� �� �����ϴ�] �Է°� : ", year);
			return -1;
		}
		
		if( month < 1 || month > 12 ){
			logger.log(Level.WARN, "[��¥�� ���� 1���� 12 ������ ���̿��� �մϴ�] �Է°� : ", month);
			return -1;
		}
		
		if( day < 1 || day > 31 ){
			logger.log(Level.WARN, "[��¥�� ���� 1���� 31 ������ ���̿��� �մϴ�] �Է°� : ", day);
			return -1;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, (month -1) );
		calendar.set(Calendar.DAY_OF_MONTH, day);
		calendar.set(Calendar.HOUR_OF_DAY, startTime ? 0 : 23);
		calendar.set(Calendar.MINUTE, startTime ? 0 : 59);
		calendar.set(Calendar.SECOND, startTime ? 0 : 59);
		
		if( calendar.getActualMaximum(Calendar.DAY_OF_MONTH) < day ){
			logger.log(Level.WARN, "[����� �� ���� ��¥�� �Է��Ͽ����ϴ�] �Է°� : ", year, " �� ", month, " �� ", day, " ��");
			return -1;
		}
		
		calendar.set(Calendar.DAY_OF_MONTH, day);
		
		long result = calendar.getTimeInMillis();
		
		if( result <= 0 ){
			logger.log(Level.WARN, "[����� �� ���� ��¥�� �Է��Ͽ����ϴ�] �Է°� : ", year, " �� ", month, " �� ", day, " ��");
			return -1;
		}
		
		return result;
	}
	
	/**
	 * 	Ư�� �ð�, �и��� ������ ���� ����<p>
	 * repeatDay ���� 3 �̸� 3�Ͽ� �ѹ��� ����Ǵ�<p>
	 * ������ �������� ������ ��¥���� �����Ͽ�  ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, 
			int startHour, int startMinute, int repeatDay,
			int startYear, int startMonth, int startDay, 
			int endYear, int endMonth, int endDay){
	
		if( repeatDay <= 0 ){
			logger.log(Level.WARN, "[������ �ݺ� ������ ���� repeatDay ���� 0 ���� Ŀ���մϴ�] �Է°� : ", repeatDay);
			return -1;
		}
		
		return this.addSchedule(schedule, 
				new int[][]{{startHour, startMinute}}, 
				repeatDay, null, null, 
				startYear, startMonth, startDay, endYear, endMonth, endDay);
	}
	
	/**
	 * 	Ư�� ��¥�� Ư�� �ð�(�ð�, ��)�� �������� ������ ���<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, 
			int[] days, int[][] hourAndMinute, 
			int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay){
					
		return this.addSchedule(schedule,
				hourAndMinute,
				null, days, null,
				startYear, startMonth, startDay, endYear, endMonth, endDay);	
	}
	
	/**
	 * 	Ư�� ��¥�� Ư���ð�, ���� ���������ؼ� ������ �������� �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, 
			int[] days, int startHour,	int startMinute, int repeatTime,  
			int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay){
		
		int[][] hourAndMinute = this.getHourAndMinute(startHour, startMinute, repeatTime, MAX_REPEAT_SIZE);
		
		return this.addSchedule(schedule,
				days, hourAndMinute,
				startYear, startMonth, startDay, endYear, endMonth, endDay);
	}
	
	/**
	 * 	Ư�� ��¥�� Ư���ð�, ���� ���������ؼ� ������ �������� ���� Ƚ����ŭ �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, 
			int[] days, int startHour,	int startMinute, int repeatTime, int repeatSize,
			int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay){
	
		if( repeatSize <= 0 ){
			logger.log(Level.WARN, "[������ �ݺ� ������ ���� repeatSize ���� 0 ���� Ŀ���մϴ�] �Է°� : ", repeatSize);
			return -1;
		}
		
		int[][] hourAndMinute = this.getHourAndMinute(startHour, startMinute, repeatTime, repeatSize);

		return this.addSchedule(schedule,
				days, hourAndMinute,
				startYear, startMonth, startDay, endYear, endMonth, endDay);
	}
	
	private int[][] getHourAndMinute(int startHour, int startMinute, int repeatTime, int repeatSize){
		
		if( repeatTime <= 0 )	return null;
		
		ArrayList<Integer> hourList = new ArrayList<Integer>();
		ArrayList<Integer> minuteList = new ArrayList<Integer>();
		
		int count = 0;
		while(repeatSize > 0 &&  count++ < repeatSize){
			if( startHour >= 24 )			break;
			if( startMinute >= 60 )		break;
			
			hourList.add(startHour);
			minuteList.add(startMinute);
			startMinute += repeatTime;		// �ð��� ���Ѵ�
			
			startHour += startMinute / 60;
			startMinute = startMinute % 60;

		}
		
		int scheduleSize = hourList.size();
		
		if( scheduleSize <= 0 )	return new int[][]{};
		
		int[][] result = new int[scheduleSize][2];
		for(int i=0; i < scheduleSize; i++){
			result[i][0] = hourList.get(i);
			result[i][1] = minuteList.get(i);
		}
		
		return result;
	}
	
	/**
	 * 	Ư�� ���ϰ� Ư�� �ð�(�ð�, ��)�� �������� ������ ���<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, 
			ScheduleWeek[] weeks, int[][] hourAndMinute, 
			int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay){
	
		return this.addSchedule(schedule, hourAndMinute, 
				null, null, weeks, 
				startYear, startMonth, startDay, endYear, endMonth, endDay);
	}
	
	/**
	 * 	Ư�� ���ϰ� Ư���ð�, ���� ���������ؼ� ������ �������� �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule,
			ScheduleWeek[] weeks, int startHour, int startMinute, int repeatTime, 
			int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay){

		return this.addSchedule(schedule, this.getHourAndMinute(startHour, startMinute, repeatTime, MAX_REPEAT_SIZE), 
				null, null, weeks, 
				startYear, startMonth, startDay, endYear, endMonth, endDay);
	}
	
	/**
	 * 	Ư�� ���ϰ� Ư���ð�, ���� ���������ؼ� ������ �������� ���� Ƚ����ŭ �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, 
			ScheduleWeek[] weeks, int startHour, int startMinute, int repeatTime, int repeatSize,
			int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay){
	
		return this.addSchedule(schedule, this.getHourAndMinute(startHour, startMinute, repeatTime, repeatSize), 
				null, null, weeks, 
				startYear, startMonth, startDay, endYear, endMonth, endDay);
	}
	
	/**
	 * 	Ư�� ��¥�� Ư�� �ð�(�ð�, ��)�� �������� ������ ���<p>
	 * 	������ ��� ��û ��� �����ϸ� ������ ���� ��û�� �ö����� ��� �ݺ��Ѵ�<p>
	 */
	public long addSchedule(Schedule schedule, int[] days, int[][] hourAndMinute){
		return this.addSchedule(schedule, days, hourAndMinute, Calendar.getInstance() );
	}
	
	/**
	 * 	Ư�� ��¥�� Ư�� �ð�(�ð�, ��)�� �������� ������ ���<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ö����� ��� �ݺ��Ѵ�
	 */
	public long addSchedule(Schedule schedule, int[] days, int[][] hourAndMinute, 
			Calendar startDate){
		
		return this.addSchedule(schedule, days, hourAndMinute, startDate.get(Calendar.YEAR), 
				startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	/**
	 * 	Ư�� ��¥�� Ư�� �ð�(�ð�, ��)�� �������� ������ ���<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ö����� ��� �ݺ��Ѵ�
	 */
	public long addSchedule(Schedule schedule, int[] days, int[][] hourAndMinute, 
			int startYear, int startMonth, int startDay){
		
		return this.addSchedule(schedule, days, hourAndMinute, startYear, startMonth, startDay, 0, 0, 0);
	}
	
	/**
	 * 	Ư�� ��¥�� Ư�� �ð�(�ð�, ��)�� �������� ������ ���<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, int[] days, int[][] hourAndMinute, 
			Calendar startDate, Calendar endDate){
		
		return this.addSchedule(schedule, days, hourAndMinute,
				startDate.get(Calendar.YEAR), 	startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH),
				endDate.get(Calendar.YEAR), 	endDate.get(Calendar.MONTH) + 1, endDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	
	
	/**
	 * 	Ư�� ��¥�� Ư���ð�, ���� ���������ؼ� ������ �������� �ݺ��Ѵ�<p>
	 * 	������ ��� ��û�� ��� �����ϸ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�
	 * 
	 *  @param		repeatTime �д���
	 */
	public long addSchedule(Schedule schedule, int[] days, int startHour, 
			int startMinute, int repeatTime){
		
		return this.addSchedule(schedule, days, startHour, startMinute, repeatTime,  Calendar.getInstance() );
	}
	
	/**
	 * 	Ư�� ��¥�� Ư���ð�, ���� ���������ؼ� ������ �������� �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�
	 * 
	 * @param		repeatTime �д���	
	 */
	public long addSchedule(Schedule schedule, int[] days, int startHour, 
			int startMinute, int repeatTime, Calendar startDate){
		
		return this.addSchedule(schedule, days, startHour, startMinute, repeatTime, 
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	/**
	 * 	Ư�� ��¥�� Ư���ð�, ���� ���������ؼ� ������ �������� �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�	
	 * 
	 * @param		repeatTime �д���
	 */
	public long addSchedule(Schedule schedule, int[] days, int startHour, 
			int startMinute, int repeatTime,  int startYear, int startMonth, int startDay){
		
		return this.addSchedule(schedule, days, startHour, startMinute, repeatTime, 
				startYear, startMonth, startDay, 0, 0, 0);
	}
	
	/**
	 * 	Ư�� ��¥�� Ư���ð�, ���� ���������ؼ� ������ �������� �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ�
	 * 
	 *  @param		repeatTime �д���
	 */
	public long addSchedule(Schedule schedule, int[] days, int startHour, 
			int startMinute, int repeatTime,  Calendar startDate, Calendar endDate){

		return this.addSchedule(schedule, days, startHour, startMinute, repeatTime, 
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH),
				endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH) + 1, endDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	
	
	/**
	 * 	Ư�� ��¥�� Ư���ð�, ���� ���������ؼ� ������ �������� ���� Ƚ����ŭ �ݺ��Ѵ�<p>
	 * 	������ ��� ��û�� ��� �����ϸ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�
	 * 
	 * @param		repeatTime �д���	
	 */
	public long addSchedule(Schedule schedule, int[] days, int startHour,
			int startMinute, int repeatTime, int repeatSize){
		
		return this.addSchedule(schedule, days, startHour, startMinute, repeatTime, repeatSize, Calendar.getInstance());
	}
	
	/**
	 * 	Ư�� ��¥�� Ư���ð�, ���� ���������ؼ� ������ �������� ���� Ƚ����ŭ �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�
	 * 
	 * @param		repeatTime �д���	
	 */
	public long addSchedule(Schedule schedule, int[] days, int startHour,
			int startMinute, int repeatTime, int repeatSize, Calendar startDate){
		
		return this.addSchedule(schedule, days, startHour, startMinute, repeatTime, repeatSize,
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	/**
	 * 	Ư�� ��¥�� Ư���ð�, ���� ���������ؼ� ������ �������� ���� Ƚ����ŭ �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�
	 * 
	 * @param		repeatTime �д���	
	 */
	public long addSchedule(Schedule schedule, int[] days, int startHour,
			int startMinute, int repeatTime, int repeatSize,
			int startYear, int startMonth, int startDay){
		
		return this.addSchedule(schedule, days, startHour, startMinute, repeatTime, repeatSize,
				startYear, startMonth, startDay, 0, 0, 0);
	}

	/**
	 * 	Ư�� ��¥�� Ư���ð�, ���� ���������ؼ� ������ �������� ���� Ƚ����ŭ �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, int[] days, int startHour,
			int startMinute, int repeatTime, int repeatSize,
			Calendar startDate, Calendar endDate){
		
		return this.addSchedule(schedule, days, startHour, startMinute, repeatTime, repeatSize,
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH),
				endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH) + 1, endDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	/**
	 * 	Ư�� �ð�, �и��� ������ ���� ����<p>
	 * repeatDay ���� 3 �̸� 3�Ͽ� �ѹ��� ����Ǵ�<p>
	 * ������ ��� ��û�� ��� �����ϸ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�	
	 */
	public long addSchedule(Schedule schedule, int startHour, int startMinute, int repeatDay){
		return this.addSchedule(schedule, startHour, startMinute, repeatDay, Calendar.getInstance() );
	}
	
	/**
	 * 	Ư�� �ð�, �и��� ������ ���� ����<p>
	 * repeatDay ���� 3 �̸� 3�Ͽ� �ѹ��� ����Ǵ�<p>
	 * ������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�	
	 */
	public long addSchedule(Schedule schedule, int startHour, int startMinute, int repeatDay,
			Calendar startDate){
		return this.addSchedule(schedule, startHour, startMinute, repeatDay,
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	/**
	 * 	Ư�� �ð�, �и��� ������ ���� ����<p>
	 * repeatDay ���� 3 �̸� 3�Ͽ� �ѹ��� ����Ǵ�<p>
	 * ������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�	
	 */
	public long addSchedule(Schedule schedule, int startHour, int startMinute, int repeatDay,
			int startYear, int startMonth, int startDay){
		return this.addSchedule(schedule, startHour, startMinute, repeatDay, startYear, startMonth, startDay, 0, 0, 0);
	}
	
	/**
	 * 	Ư�� �ð�, �и��� ������ ���� ����<p>
	 * repeatDay ���� 3 �̸� 3�Ͽ� �ѹ��� ����Ǵ�<p>
	 * ������ �������� ������ ��¥���� �����Ͽ�  ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, int startHour, int startMinute, int repeatDay,
			Calendar startDate, Calendar endDate){
		return this.addSchedule(schedule, startHour, startMinute, repeatDay,
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH),
				endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH) + 1, endDate.get(Calendar.DAY_OF_MONTH));
	}
	

	
	
	/*
	 * 
	 * 
	 *		���Ϸ� ���� 
	 * 
	 */
	
	
	/**
	 * 	Ư�� ���ϰ� Ư�� �ð�(�ð�, ��)�� �������� ������ ���<p>
	 * 	������ ��� ��û ��� �����ϸ� ������ ���� ��û�� �ö����� ��� �ݺ��Ѵ�
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int[][] hourAndMinute){
		return this.addSchedule(schedule, weeks, hourAndMinute, Calendar.getInstance() );
	}

	/**
	 * 	Ư�� ���ϰ� Ư�� �ð�(�ð�, ��)�� �������� ������ ���<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ö����� ��� �ݺ��Ѵ�
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int[][] hourAndMinute, 
			Calendar startDate){
		
		return this.addSchedule(schedule, weeks, hourAndMinute,
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	/**
	 * 	Ư�� ���ϰ� Ư�� �ð�(�ð�, ��)�� �������� ������ ���<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ö����� ��� �ݺ��Ѵ�
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int[][] hourAndMinute, 
			int startYear, int startMonth, int startDay){
		
		return this.addSchedule(schedule, weeks, hourAndMinute,
				startYear, startMonth, startDay, 0, 0, 0);
	}
	
	
	/**
	 * 	Ư�� ���ϰ� Ư�� �ð�(�ð�, ��)�� �������� ������ ���<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int[][] hourAndMinute, 
			Calendar startDate, Calendar endDate){
		
		return this.addSchedule(schedule, weeks, hourAndMinute,
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH),
				endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH) + 1, endDate.get(Calendar.DAY_OF_MONTH));
	}
	
	/**
	 * 	Ư�� ���ϰ� Ư���ð�, ���� ���������ؼ� ������ �������� �ݺ��Ѵ�<p>
	 * 	������ ��� ��û�� ��� �����ϸ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�	
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int startHour, 
			int startMinute, int repeatTime){
		
		return this.addSchedule(schedule, weeks, startHour, startMinute, repeatTime, Calendar.getInstance() );
	}
	
	/**
	 * 	Ư�� ���ϰ� Ư���ð�, ���� ���������ؼ� ������ �������� �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�	
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int startHour, 
			int startMinute, int repeatTime, Calendar startDate){
		
		return this.addSchedule(schedule, weeks, startHour, startMinute, repeatTime, 
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	/**
	 * 	Ư�� ���ϰ� Ư���ð�, ���� ���������ؼ� ������ �������� �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�	
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int startHour, 
			int startMinute, int repeatTime,  int startYear, int startMonth, int startDay){
		
		return this.addSchedule(schedule, weeks, startHour, startMinute, repeatTime, 
				startYear, startMonth, startDay, 0, 0, 0);
	}
	
	/**
	 * 	Ư�� ���ϰ� Ư���ð�, ���� ���������ؼ� ������ �������� �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int startHour, 
			int startMinute, int repeatTime,  Calendar startDate, Calendar endDate){

		return this.addSchedule(schedule, weeks, startHour, startMinute, repeatTime, 
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH),
				endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH) + 1, endDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	/**
	 * 	Ư�� ���ϰ� Ư���ð�, ���� ���������ؼ� ������ �������� ���� Ƚ����ŭ �ݺ��Ѵ�<p>
	 * 	������ ��� ��û�� ��� �����ϸ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�	
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int startHour,
			int startMinute, int repeatTime, int repeatSize){
	
		return this.addSchedule(schedule, weeks, startHour, startMinute, repeatTime, repeatSize, Calendar.getInstance() );
	}
	
	/**
	 * 	Ư�� ���ϰ� Ư���ð�, ���� ���������ؼ� ������ �������� ���� Ƚ����ŭ �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�	
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int startHour,
			int startMinute, int repeatTime, int repeatSize,
			Calendar startDate){
		
		return this.addSchedule(schedule, weeks, startHour, startMinute, repeatTime, repeatSize, 
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	/**
	 * 	Ư�� ���ϰ� Ư���ð�, ���� ���������ؼ� ������ �������� ���� Ƚ����ŭ �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ���� ��û�� �ô���� �ݺ��Ѵ�	
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int startHour,
			int startMinute, int repeatTime, int repeatSize,
			int startYear, int startMonth, int startDay){
		
		return this.addSchedule(schedule, weeks, startHour, startMinute, repeatTime, repeatSize, 
				startYear, startMonth, startDay, 0, 0, 0 );
	}

	
	/**
	 * 	Ư�� ���ϰ� Ư���ð�, ���� ���������ؼ� ������ �������� ���� Ƚ����ŭ �ݺ��Ѵ�<p>
	 * 	������ �������� ������ ��¥���� �����Ͽ� ������ ����� ������ ��¥�� ������ �� ����ȴ� 
	 */
	public long addSchedule(Schedule schedule, ScheduleWeek[] weeks, int startHour,
			int startMinute, int repeatTime, int repeatSize,
			Calendar startDate, Calendar endDate){
		
		return this.addSchedule(schedule, weeks, startHour, startMinute, repeatTime, repeatSize, 
				startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH) + 1, startDate.get(Calendar.DAY_OF_MONTH),
				endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH) + 1, endDate.get(Calendar.DAY_OF_MONTH) );
	}
	
	
	/**
	 * �����췯 ����
	 */
	public void closeSchedule(){
		synchronized( this.lock ){
			this.scheduleManager.closeScheduleManager();
			this.scheduleManager = null;
		}
	}
}
