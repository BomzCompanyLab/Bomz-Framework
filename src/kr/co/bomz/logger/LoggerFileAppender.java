package kr.co.bomz.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

import kr.co.bomz.di.ServiceStore;


/**
 * 		�α� ������ ���Ͽ� ����ϴ� ó�� ����<p>
 * 
 * 		���۸� ����Ͽ� Ư�� �ð����� ���Ͽ� ���������� �⺻ ������ 5�ʸ���<br> 
 * 		�α׸� ���Ͽ� ���� �۾��� �����Ѵ�<p>
 * 
 * 		�ش� �ð��� �ٲٰ� ������ �������Ͽ� �߰��Ͽ� ������ �� ������ �ּ� �ð��� 1���̴�<p>
 * 
 *  	�������� ��)	<br>
 *  		- �������� ���� ���											(5�ʸ��� ����. �⺻��)<br>
 *  		- supinan.loggerFileAppender.flushTime = 3		(3�ʸ��� ����)<br>
 *  		- supinan.loggerFileAppender.flushTime = 0		(1�ʸ��� ����. �ּҰ��� 1����)<br>
 *  		- supinan.loggerFileAppender.flushTime = AB	(5�ʸ��� ����. �������� �ƴ� ��� �⺻�� ���)<br>
 * 
 * @author ���ǳ�
 * @since 1.2
 * @version 1.6
 */
public class LoggerFileAppender extends Thread{

	private static final LoggerFileAppender _this = new LoggerFileAppender();
	
	/*		���� ���� ���� �⺻ �ð� �� 	*/
	private static final long DEFAULT_FILE_FLUSH_TIME = 5L;
	
	/*
	 * 		Key		:		�α� ��ü
	 * 		Value	:		�α� ��� ��ü
	 */
	private final HashMap<Logger, LoggerFileInfo> loggerMap = new HashMap<Logger, LoggerFileInfo>();
	
	/*		loggerMap ����ȭ ó����		*/
	private final Object loggerMapLock = new Object();
	
	private long fileFlushTime;
	
	private LoggerFileAppender(){
		super.setDaemon(true);		// �ý����� ���� ��� �Բ� �������� �Ѵ�
		this.settingFileFlushTime();
		start();
	}
	
	/*		���� ���� ���� �⺻ �ð� �� 	*/
	private void settingFileFlushTime(){
		String value = ServiceStore.getResourceValue("supinan.loggerFileAppender.flushTime");
		if( value == null )		this.fileFlushTime = DEFAULT_FILE_FLUSH_TIME * 1000;
		
		try{
			long flushTime = Long.valueOf(value);
			this.fileFlushTime = (flushTime <= 0 ? 1L : flushTime) * 1000;
		}catch(Exception e){
			this.fileFlushTime = DEFAULT_FILE_FLUSH_TIME * 1000;
		}
	}
	
	static final LoggerFileAppender getInstance(){
		return _this;
	}
	
	public void run(){
		
		while( true ){
			
			try{		Thread.sleep( this.fileFlushTime );		}catch(Exception e){}
			
			synchronized( this.loggerMapLock ){
				this.allFlush();
			}
			
		}
		
	}
	
	void allFlush(){
		Iterator<LoggerFileInfo> values = this.loggerMap.values().iterator();
		while( values.hasNext() ){
			values.next().flush();		// ������ ������ ���Ϸ� ���
		}
	}
	
	/**		���� ����� ����� �α� ��ü ���	*/
	void addLogger(Logger logger, long maxFileSize){
		synchronized( this.loggerMapLock ){
			this.loggerMap.put(logger, new LoggerFileInfo(logger, maxFileSize));
		}
	}
	
	/**		���Ͽ� �α� ���� �߰�		*/
	void addLogInfo(Logger logger, String msg){
		synchronized( this.loggerMapLock ){
			LoggerFileInfo info = this.loggerMap.get(logger);
			if( info != null )		info.write(msg);
		}
	}
	
	private class LoggerFileInfo{
		private final Logger logger;
		private final File file;
		private BufferedWriter fileWriter;
		
		// ���� �ִ� ũ��
		private final long maxFileSize;
		
		private int year = -1;		// �α� ���� ���� ��
		private int month;				// �α� ���� ���� ��
		private int day;				// �α� ���� ���� ��
		private int hour;				// �α� ���� ���� �ð�
		
		private LoggerFileInfo(Logger logger, long maxFileSize){
			this.logger = logger;
			this.maxFileSize = maxFileSize;
			this.file = new File(logger.fileName);
			this.makeDirs();
		}
		
		/*		
		 * 		�α� ������ ���� ��� ���� �����ϸ�
		 * 		���� ��� �ð��� ���Ͽ� ���� �Ⱓ�� �������� �̸� ���� ��
		 * 		�ٽ� ���ο� ������ �����Ѵ� 
		 */
		private boolean createOrReplaceFile(){
			if( !this.file.isFile() ){		// ������ ���� ��� ���� ����
				return this.createNewFile();
		
			}else{		// ������ ���� ��� ������ �ð��� ���� �ֱ⸦ ���Ͽ� ó��
				if( this.year == -1 ){
					Calendar date = Calendar.getInstance();
					date.setTimeInMillis( this.file.lastModified() );
					this.settingLastModify(date);
				}
				return this.createOrReplaceFile(Calendar.getInstance());
			}
			
		}
		
		/*		���ο� �α� ���� ����		*/
		private boolean createNewFile(){
			
			this.close();
			
			try {
				if( !this.file.createNewFile() ){
					// ���ο� ���� ���� ����
					System.err.println("���ǳ������ӿ�ũ �α� ���� ���� ���� [" +  this.logger.fileName + "]");
					return false;
				}
				
				return this.createWriter();
			} catch (IOException e) {
				System.err.println("���ǳ������ӿ�ũ �α� ���� ���� ���� [" +  this.logger.fileName + "] " + e.getMessage());
				return false;
			}
		}
		
		private boolean createWriter(){
			if( this.fileWriter != null )		return true;
			
			try{
				this.fileWriter = new BufferedWriter( new FileWriter(this.file, true) );
				return true;
			}catch(IOException e){
				System.err.println("���ǳ������ӿ�ũ �α� ���� ��ü ���� ���� [" + this.logger.fileName + "] " + e.getMessage());
				return false;
			}
		}
		
		private void makeDirs(){
			String[] args = this.logger.fileName.split("/");
			int length = args.length - 1;
			
			StringBuilder buffer = new StringBuilder();
			for(int i=0; i < length; i++)
				buffer.append(args[i] + File.separatorChar);
			
			File dirFile = new File(buffer.toString());
			dirFile.mkdirs();
		}
		
		/*		�α� ������ ������ ���� �ð� ����		*/
		private void settingLastModify(Calendar date){
			this.year = date.get(Calendar.YEAR);
			this.month = date.get(Calendar.MONTH) + 1;
			this.day = date.get(Calendar.DAY_OF_MONTH);
			this.hour = date.get(Calendar.HOUR_OF_DAY);
		}
		
		/*		��¥�� ���Ͽ� ���� ����Ǿ��� ��� ���� �̸� ���� �� ���� ����		*/
		private boolean createOrReplaceFile(Calendar date){
			if( this.year != date.get(Calendar.YEAR) || this.month != (date.get(Calendar.MONTH) + 1) ||
					this.day != date.get(Calendar.DAY_OF_MONTH) ){
				// ��¥�� ����Ǿ��� ��� ���� ������ �ٸ� �̸����� ���� �� ���ο� ���� ����
				return this.replaceFile(false, date);
				
			}else{
				// ��¥�� �Ȱ��� ��� �ð����� �α� �����̵Ǿ� �ִ��� �˻�
				if(this.logger.logFilePeriod == Logger.LOG_FILE_PERIOD_DAY)		return this.createWriter(); 
				if(this.hour == date.get(Calendar.HOUR_OF_DAY))							return this.createWriter();
				
				// �ð����� �α� ������ �Ǿ��ְ� ������ �ð��� ���� �ð��� �ٸ��ٸ� ���� ������
				// �ٸ� �̸����� ����  �� ���ο� ���� ����
				return this.replaceFile(false, date);
			}
		}
		
		/*		
		 * 		���� ��� �Ⱓ�� ������ ��� ������ ����ϴ� ������ ����� ����, 
		 * 		���ϸ��� ����, ���ο� ���� ������ ������ ó���Ѵ�
		 */
		private boolean replaceFile(boolean allName, Calendar date){
			this.close();
			if( !this.file.renameTo(this.getRenameFile(allName, date)) ){
				// ���� �̸� ���� ����
				System.err.println("���ǳ������ӿ�ũ �α� ���� �̸� ���� ���� [" +  this.logger.fileName + "]");
				return false;
			}
			
			this.settingLastModify(Calendar.getInstance());
			return this.createNewFile();
		}
		
		/*		������ ��� �Ⱓ�� ������ ��� ������ ���� ���� ����		*/
		private File getRenameFile(boolean allName, Calendar date){
			return new File(
					this.logger.fileName + "." + this.year + "-" + this.month + "-" + this.day + 
					(this.logger.logFilePeriod == Logger.LOG_FILE_PERIOD_DAY ? "" : "-" + this.hour) + 
					(allName ? "-" + 
							(this.logger.logFilePeriod == Logger.LOG_FILE_PERIOD_DAY ? this.hour + "-" : "") +
							date.get(Calendar.MINUTE) + "-" + date.get(Calendar.SECOND) : "") + 
					".log" 
				);
		}
		
		private void write(String msg){
			/*
			 *  ������ ������ ��¥ ��
			 *  ������ �ֱⰡ �����ٸ� ���� ������ ���ϸ� ���� �� ���ο� ���� ����
			 *  ���ο� ���Ͽ� ����
			 */
			if( !this.createOrReplaceFile() )		return;		// �α� ������ ���� �����ϰų� ���� ������ �̸��� �����Ѵ�
			
			/*
			 * ���� ũ�� ���ؼ� ���� ũ�� �������� �Ѿ����� ���ϸ� ����
			 */
			if( !this.checkMaxFileSize() )		return;
				
			
			try {
				this.fileWriter.write(msg);
			} catch (IOException e) {
				System.err.println("���ǳ������ӿ�ũ �α� ���� ���� [" + this.logger.fileName + "] " + e.getMessage());
			}
		}
		
		/**
		 * ���� �ִ� ũ�Ⱑ ���������� ��� ���� ���� ũ�� �� ��
		 * ���� ũ�⺸�� ũ�ٸ� ���� ���� ����
		 * @return
		 */
		private boolean checkMaxFileSize(){
			// ���� �ִ� ũ�Ⱑ �����Ǿ� ���� ���� ���
			if( this.maxFileSize == Logger.NON_SETTING_FILE_SIZE )		return true;
			
			// ������ ���� �ִ� ũ�Ⱑ �� Ŭ ��� 
			if ( this.maxFileSize > this.file.length() )		return true;

			// �α����� ũ�Ⱑ ������ �ִ� ũ�⸦ �Ѱ��� ��� ���� ���� ����
			return this.replaceFile(true, Calendar.getInstance());
		}
		
		/*		���� ���� ����		*/
		private void close(){
			this.flush();
			if( this.fileWriter != null ){
				try {
					this.fileWriter.close();
				} catch (IOException e) {}
				this.fileWriter = null;
			}
		}
		
		/*		���ۿ� �ִ� ���� ���� ���		*/
		private void flush(){

			if( this.fileWriter == null )		return;
			
			try {
				this.fileWriter.flush();
			} catch (IOException e) {
				System.err.println("���ǳ������ӿ�ũ �α� ���� ���� ���� [" + this.logger.fileName + "] " + e.getMessage());
			}
		}
		
	}
	
}
