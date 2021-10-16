package kr.co.bomz.logger;

import java.util.Calendar;

/**
 * 
 * 		�ܼ� �α� ��� �� ���� ��� ��� ����<br>
 * 		�α� ����� ������ �������� ���� ��� DEBUG ������� �����ȴ�<p>
 * 
 * 		�α� ���<br>
 * 			Level.DEBUG , Level.INFO , Level.WARN , Level.ERROR , Level.FATAL<p>
 * 
 * 		��� ��)<br>
 * 			���� :	private static final Logger logger = Logger.getLogger("testLogger");<br>
 * 			��� :	<br>	
 * 			logger.log(Level.INFO, "�α�", "�׽�", "Ʈ. ");<br>
 * 			logger.log(Level.WARN, exception);<br>
 * 			logger.log(Level.WARN, exception, "�����α��Դϴ�");<br>
 * 			logger.log(Level.WARN, exception, "�����α�", "�Դϴ�");<p>
 * 
 * 		���� ��)<br>
 * 			supinan.logger = WARN						(WARN �̻� �ܼ� �α� ���)<br> 
 * 			supinan.logger.testLogger = INFO		(INFO �̻� �ܼ� �α� ���)<br>
 * 			supinan.logger.testLogger.file = log/MyTest.log		(log/MyTest.log ���Ͽ� ���)<br>
 * 			supinan.logger.testLogger.file.level = WARN			(WARN �̻� ���Ͽ� �α����)<br>
 * 			supinan.logger.testLogger.file.size = 3					(3MB ������ �α� ���� ����)<br>
 * 			supinan.logger.testLogger.file.period = DAY or HOUR	(�� �Ǵ� �ð� ������ �α� ���� ����)<br>
 * 
 * @author ���ǳ�
 * @since 1.2
 * @version 1.6
 */
public class Logger {
	
	/*		�α׸�		*/
	private final String loggerName;
	
	/*		�ܼ� ��� �α� ���		*/
	private Level consolLoggerLevel;
	
	/*		���� ��� �α� ���		*/
	private Level fileLoggerLevel;
		
	/*		�α� ���ϸ�		*/
	String fileName;
	
	private static final String CLASS_NAME = Logger.class.getName();
	
	/**		�α� ���� �з� �Ⱓ (�ϴ���)		*/
	static final int LOG_FILE_PERIOD_DAY = 105;
	/**		�α� ���� �з� �Ⱓ (�ð� ����)	*/
	static final int LOG_FILE_PERIOD_HOUR = 106;
		
	/**		
	 * 		�α� ���� �з� �Ⱓ. <br>
	 * 		������ �������� ���� ��� �� ������ �α� ���Ͽ� ����ȴ�<p>
	 * 		���� ��)<br>
	 * 			supinan.logger.�α׸�.file.period = �Ⱓ ���� ��<br>
	 * 			supinan.logger.testLogger.file.period = DAY<br>
	 * 			supinan.logger.testLogger.file.period = HOUR<br>
	 */
	int logFilePeriod = LOG_FILE_PERIOD_DAY;
	
	public static final long NON_SETTING_FILE_SIZE = -1;
	
	/**
	 * 		�α� ���� ũ�� ���� (�⺻���� MB)<br/>
	 * 		������ ��� �ش� ũ�⿡ �°� �α������� �����Ѵ�<p>
	 * 		���� ��)<br>
	 * 			supinan.logger.�α׸�.file.size = 30<br>
	 */
	private long maxFileSize = NON_SETTING_FILE_SIZE;
	
	
	
	Logger(String loggerName){
		this.loggerName = loggerName;
		this.init();
	}
	
	/*
	 * 		�α� ��� �� ���� ���� ���� �ʱ�ȭ
	 */
	void init(){
		this.initConsolLoggerLevel();
		this.initLoggerFile();
	}
		
	/*		�ش� �αװ� �⺻�α׸��� ���� �ƴҰ�� �ٸ��� �̸��� ����		*/
	private String getLoggerKeyName(){
		return "supinan.logger" + (this.loggerName.equals(LoggerManager.SYSTEM_LOGGER_NAME) ? "" : "." + this.loggerName);
	}
	
	/*		�α׸� ���Ͽ� ����� ��� ���� ���� �ʱ�ȭ		*/ 
	private void initLoggerFile(){
		// �ý��� �α��ϰ�� ���� ó���� ���� ����
		// 1.3 �������� ������. �ý��� �α׵� ����ó�� ����
//		if( this.loggerName.equals(LoggerManager.SYSTEM_LOGGER_NAME) )		return;
		
		this.fileName = LoggerManager.getInstance().getProperty(this.getLoggerKeyName() + ".file");

		if( this.fileName != null ){
			this.fileLoggerLevel = this.getLoggerLevel(this.getLoggerKeyName() + ".file.level");
			this.logFilePeriod = this.getLoggerPeriod();
			this.maxFileSize = this.getMaxFileSize(this.getLoggerKeyName() + ".file.size");
			LoggerFileAppender.getInstance().addLogger(this, this.maxFileSize);
		}
	}
	
	/**
	 * �������Ͽ� �Է��� �α����� �ִ� ũ�⸦ �����´�
	 * @param fileMaxSizeValue		����ڰ� ������ �α����� �ִ� ũ��
	 * @return
	 */
	private long getMaxFileSize(String fileMaxSize){
		String fileMaxSizeValue = LoggerManager.getInstance().getProperty(fileMaxSize);
		if( fileMaxSizeValue == null )		return NON_SETTING_FILE_SIZE;
		try{
			long result = Long.parseLong(fileMaxSizeValue.trim());
			return result <= 0 ? NON_SETTING_FILE_SIZE : result * 1024 * 1024;
		}catch(Exception e){
			// �߸��� ���� ��� -1 ����
			return NON_SETTING_FILE_SIZE;
		}
	}
	
	private int getLoggerPeriod(){
		String period = LoggerManager.getInstance().getProperty(this.getLoggerKeyName() + ".file.period");
		if( period == null )		return LOG_FILE_PERIOD_DAY;
		
		if( period.equalsIgnoreCase("DAY") )						return LOG_FILE_PERIOD_DAY;
		else if( period.equalsIgnoreCase("HOUR") )			return LOG_FILE_PERIOD_HOUR;
		else																		return LOG_FILE_PERIOD_DAY;
	}
	
	/*		�α� ��� �ʱ�ȭ		*/
	private void initConsolLoggerLevel(){
		this.consolLoggerLevel = this.getLoggerLevel(this.getLoggerKeyName());
	}
	
	/*		�ΰŸ��� �´� �α� ��� �Ǵ�		*/
	private Level getLoggerLevel(String loggerName){
		String level = LoggerManager.getInstance().getProperty(loggerName);
		
		if( level == null && loggerName.endsWith(".file.level") ){
			// ���� �α� ���� ������ �ȵǾ����� ��� �ܼ� �α� ������ ����
			level = LoggerManager.getInstance().getProperty(loggerName.substring(0, loggerName.length() - 11));
		}
		
		return level == null ? Level.DEBUG : this.checkLoggerLevel(level.trim());	
	}
	
	/*		�α� ������ �´� Level ��ü ��ȯ		*/
	private Level checkLoggerLevel(String level){
		if( level.equalsIgnoreCase(Level.DEBUG.name()) ){
			return Level.DEBUG;
		}else if( level.equalsIgnoreCase(Level.INFO.name()) ){
			return Level.INFO;
		}else if( level.equalsIgnoreCase(Level.WARN.name()) ){
			return Level.WARN;
		}else if( level.equalsIgnoreCase(Level.ERROR.name()) ){
			return Level.ERROR;
		}else if( level.equalsIgnoreCase(Level.FATAL.name()) ){
			return Level.FATAL;
		}else{
			System.err.println(
					"���ǵ��� ���� �α� ��� [�α׸�=" + this.loggerName + " , ���=" + level + "]\n" +
					"��� ������ ��� [DEBUG , INFO , WARN , ERROR , FATAL]"
				);
			return Level.DEBUG;
		}
	}
	
	/**		�α� ���� ��� ����	*/
	public static final void setLogConfigureFile(String logConfigureFile){
		if( logConfigureFile == null )		return;

		LoggerManager.logConfigureFile = logConfigureFile;
		LoggerManager.getInstance().readPropertyFile();
		LoggerManager.getInstance().resetting();
	}
	
	/**
	 * 		�ý��� �α� ��ü�� ����
	 */
	public static final Logger getRootLogger(){
		return LoggerManager.getInstance().getRootLogger();
	}
	
	/**
	 * 		�α׸��� �ش��ϴ� �α� ��ü�� ����
	 * 
	 * 		@param loggerName  	�α׸�.  ������ ���õȴ�
	 * 		@return		�α� ��ü. �α׸��� NULL �Ǵ� ������ ��� NULL ����
	 */
	public static final Logger getLogger(String loggerName){
		return LoggerManager.getInstance().getLogger(loggerName);
	}
	
	public void log(Level level, Object ... msg){
		this.log(level, null, msg);
	}
	
	public void log(Level level, Throwable err){
		this.log(level, err, new Object[]{});
	}
	
	public void log(Level level, Throwable err, Object ... msg){
		if( level == null )		return;
		
		// �ܼ� �� ���� ��� ��� ����� �� ���� ��� ó������ ����
		if( !this.isRequestLevelCompetency(this.consolLoggerLevel, level) && 
			(this.fileName == null || !this.isRequestLevelCompetency(this.fileLoggerLevel, level))
		)		return;			// �α� ��� �˻�
		
		Thread thread = Thread.currentThread();
		StackTraceElement stackTrace = this.getCallStackTraceElement( thread.getStackTrace() );
				
		StringBuilder buffer = new StringBuilder(
				this.getNowTime() + level.name() + "] " + stackTrace.toString() + " "
			);
		
		if( msg != null )		for(Object m : msg)		buffer.append(m);
		
		if( err != null ){
			buffer.append("\n");
			StackTraceElement[] trace = err.getStackTrace();
			buffer.append(err.toString());
			for(StackTraceElement t : trace)	buffer.append("\n\tat " + t);
		}
		
		buffer.append("\n");
		
		String logMsg = buffer.toString();
		
		// �ܼ� ��¿��� �˻�
		if( this.isRequestLevelCompetency(this.consolLoggerLevel, level) ){
			System.out.print(logMsg);
		}
		
		// ���� ��� ���� �˻�
		if( this.fileName != null && this.isRequestLevelCompetency(this.fileLoggerLevel, level) ){
			LoggerFileAppender.getInstance().addLogInfo(this, logMsg);
		}
				
	}
	
	/*		�α׸� ȣ���� ���� ������ ����		*/
	private StackTraceElement getCallStackTraceElement(StackTraceElement[] stack){
		for(int i=1; i < stack.length; i++){
			if( !stack[i].getClassName().equals(CLASS_NAME) )		return stack[i];
		}
		
		// ���ϴ� ������ ã�� ������ ���
		return stack[stack.length -1];
	}
	
	
	/*		�α� ��� ���� �Ǵ�. true ���� �� �α� ���		*/
	private boolean isRequestLevelCompetency(Level systemLevel, Level userLevel){
		switch( systemLevel ){
		case DEBUG:
			return true;
		case INFO:
			return userLevel != Level.DEBUG;
		case WARN:
			return userLevel != Level.DEBUG && userLevel != Level.INFO;
		case ERROR:
			return userLevel == Level.ERROR || userLevel == Level.FATAL;
		case FATAL:
			return userLevel == Level.FATAL;
		}
		
		return false;
	}
	
	private String getNowTime(){
		Calendar cal = Calendar.getInstance();
		return
				"[" +
				cal.get(Calendar.YEAR) + "-" +
				(cal.get(Calendar.MONTH) + 1) + "-" +
				cal.get(Calendar.DAY_OF_MONTH) + " " +
				cal.get(Calendar.HOUR_OF_DAY) + ":" +
				cal.get(Calendar.MINUTE) + ":" +
				cal.get(Calendar.SECOND) +
				"][";
	}
}
