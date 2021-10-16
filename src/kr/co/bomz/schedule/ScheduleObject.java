package kr.co.bomz.schedule;

/**
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
public class ScheduleObject {

	// ���� �������� ���� ����. removeSchedule ȣ�� �� STOP ���� ����ȴ�
	private ScheduleState scheduleState = ScheduleState.START;
	
	private final long scheduleId;
	private final Schedule schedule;
	
	private Integer repeatDay; 
	private int[] days;
	private ScheduleWeek[] weeks;
	
	private int[][] hourAndMinute;
	
	private ScheduleRepeatType repeatType;
	
	private long startTime;
	
	/*
	 * 		-1 �� ��� ���� ��¥�� ����,
	 */
	private long endTime;
	
	private boolean checkStartDate = false;
	private int beforeDay;
	private boolean checkRepeat = false;
	
	/**
	 * 		�ش� ������ ������ �ݺ� Ÿ���� RepeatDay �� �����Ǿ� ���� ���� ���ȴ�
	 * 		�������� �����Ϸ��� ��¥�� �ش� ������ ��¥�� ������ �������� �����ϸ�,
	 * 		�ش� ���� ���� ���� ��¥�� �����Ѵ�
	 */
	private int nextRepeatYear, nextRepeatMonth, nextRepeatDay = -1;	
	
	/**
	 * 	�̹� ����� �ð��� ����ȴ�.
	 *  �Ϸ翡 ������ 
	 */
	private int executeHourAndMinuteIndex;
	private int[][] executeHourAndMinute;
	
	ScheduleObject(Schedule schedule, long scheduleId){
		 this.schedule = schedule;
		 this.scheduleId = scheduleId;
	 }
	 
	 void removeSchedule(){
		 this.scheduleState = ScheduleState.STOP;
	 }
	
	long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	long getEndTime() {
		return endTime;
	}

	void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	void setRepeatDay(Integer repeatDay) {
		this.repeatDay = repeatDay;
		if( this.repeatDay != null )	this.repeatType = ScheduleRepeatType.REPEAT_DAY;
	}

	void setWeeks(ScheduleWeek[] weeks) {
		this.weeks = weeks;
		if( this.weeks != null )	this.repeatType = ScheduleRepeatType.WEEKS;
	}

	void setDays(int[] days) {
		this.days = days;
		if( this.days != null )	this.repeatType = ScheduleRepeatType.DAYS;
	}

	void setHourAndMinute(int[][] hourAndMinute) {
		this.hourAndMinute = hourAndMinute;
	}
	
	public long getScheduleId(){
		return this.scheduleId;
	}
	
	ScheduleState getScheduleState(){
		return this.scheduleState;
	}
	
	public Schedule getSchedule(){
		return this.schedule;
	}
	
	boolean isCheckStartDate(){
		return this.checkStartDate;
	}
	
	void changeCheckStartDate(){
		this.checkStartDate = true;
	}

	public ScheduleRepeatType getRepeatType() {
		return repeatType;
	}

	Integer getRepeatDay() {
		return repeatDay;
	}

	int[] getDays() {
		return days;
	}

	ScheduleWeek[] getWeeks() {
		return weeks;
	}

	int[][] getHourAndMinute() {
		return hourAndMinute;
	}

	int getBeforeDay() {
		return beforeDay;
	}

	void setBeforeDay(int beforeDay) {
		this.beforeDay = beforeDay;
	}

	boolean isCheckRepeat() {
		return checkRepeat;
	}

	void setCheckRepeat(boolean checkRepeat) {
		this.checkRepeat = checkRepeat;
	}
	
	int getNextRepeatYear() {
		return nextRepeatYear;
	}

	int getNextRepeatMonth() {
		return nextRepeatMonth;
	}

	int getNextRepeatDay() {
		return nextRepeatDay;
	}

	void setNextRepeatDay(int nextRepeatYear, int nextRepeatMonth, int nextRepeatDay) {
		this.nextRepeatYear = nextRepeatYear;
		this.nextRepeatMonth = nextRepeatMonth;
		this.nextRepeatDay = nextRepeatDay;
	}
	
	void addHourAndMinute(int hour, int minute){
		try{
			this.executeHourAndMinute[executeHourAndMinuteIndex][0] = hour;
			this.executeHourAndMinute[executeHourAndMinuteIndex++][1] = minute;
		}catch(Exception e){
			// ���� ������ �ʰ����� ������ ���� ó��.
		}
	}
	
	void clearHourAndMinute(){
		this.executeHourAndMinuteIndex = 0;
		this.executeHourAndMinute = new int[this.hourAndMinute.length][2];
	}
	
	boolean checkExecuteHourAndMinute(int hour, int minute){
		for(int i=0; i < this.executeHourAndMinuteIndex; i++){
			if( this.executeHourAndMinute[i][0] == hour && this.executeHourAndMinute[i][1] == minute )	return false;
		}
		
		return true;
	}
}
