package kr.co.bomz.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 * 
 * @author ���ǳ�
 * @since 1.2
 * @version 1.4
 *
 */
public class LoggerManager {

	private static LoggerManager _this = null;
	
	static final String SYSTEM_LOGGER_NAME = "supinan";
	
	/**
	 * 		�⺻ �α� ���� ���ϸ�
	 * 		���� ��ġ : Ŭ�����н� ��� / logger.properties 
	 */
	public static final String DEFAULT_LOGGER_CONFIGURE_FILE = "classpath:logger.properties";
	
	/**		�α� ���� ����		*/
	static String logConfigureFile = DEFAULT_LOGGER_CONFIGURE_FILE;
	
	/*		
	 * 		Key		:		�α׸�
	 * 		Value	:		�α� ��ü
	 */
	private HashMap<String, Logger> loggerInfoMap = new HashMap<String, Logger>(1);
	
	/**		�α� ���� ���� ���� ��		*/
	final HashMap<String, String> propertyMap = new HashMap<String, String>();
	
	private LoggerManager(){
		Runtime.getRuntime().addShutdownHook(new LoggerShutdown());
	}
	
	private void initLoggerManager(){
		this.readPropertyFile();
		loggerInfoMap.put(SYSTEM_LOGGER_NAME, new Logger(SYSTEM_LOGGER_NAME));
	}
	
	/*		
	 * 		���� ���Ͽ� ���� ��Ʈ���� ��´�.
	 * 		���� ������ ���� ��� NULL ����
	 */
	private InputStream getPropertyFileStream(){
		if( logConfigureFile.equals(DEFAULT_LOGGER_CONFIGURE_FILE) ){
			// �⺻ ���� ����
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			
			if (classLoader!=null) {
				// �տ� classpath: �� �����Ѵ�
				return classLoader.getResourceAsStream( logConfigureFile.substring(10) );
			}else{
				return null;
			}
			
		}else{
			// ����� ���� ���� ����
			File file = new File(logConfigureFile);
			if( !file.isFile() )		return null;
			
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				return null;
			}
		}

	}
	
	/*		�α� ���� ���� �м�		*/
	void readPropertyFile(){
		
		InputStream stream = this.getPropertyFileStream();
		
		if( stream == null )		return;		// ���� ������ ã�� �� ���� ���

		Properties properties =  new Properties();
		try {
			properties.load(stream);
			Object key;
			Iterator<Object> keys = properties.keySet().iterator();
			while( keys.hasNext() ){
				key = keys.next();
				
				this.propertyMap.put(key.toString(), properties.get(key).toString());
			}
			
		} catch (IOException e) {
			System.err.println("Supinan framework log configure file error");
		}
	}
	
	/**		�α� ���� ���� �˻�		*/
	String getProperty(String propertyName){
		if( propertyName == null )		return null;
		
		return this.propertyMap.get(propertyName);
	}
	
	synchronized static final LoggerManager getInstance(){		
		
		if( _this == null ){
			_this = new LoggerManager();
			LoggerFileAppender.getInstance();
			_this.initLoggerManager();
		}
		return _this;
	}
	
	/**		�α��ۼ� �缳��		*/
	void resetting(){
		Iterator<String> keys = loggerInfoMap.keySet().iterator();
		while( keys.hasNext() ){
			Logger logger = loggerInfoMap.get(keys.next());
			logger.init();
		}
		
	}
	
	/**
	 * 		�ý��� �α� ��ü�� ����
	 */
	final Logger getRootLogger(){
		return getLogger(SYSTEM_LOGGER_NAME);
	}
	
	/**
	 * 		�α׸��� �ش��ϴ� �α� ��ü�� ����
	 * 
	 * 		@param loggerName  	�α׸�.  ������ ���õȴ�
	 * 		@return		�α� ��ü. �α׸��� NULL �Ǵ� ������ ��� NULL ����
	 */
	final Logger getLogger(String loggerName){
		
		if( loggerName == null )		return null;
		
		loggerName = loggerName.trim();
		if( loggerName.equals("") )		return null;
		
		synchronized( _this.loggerInfoMap ){
			Logger logger = _this.loggerInfoMap.get(loggerName);
			if( logger == null ){
				logger = new Logger(loggerName);
				_this.loggerInfoMap.put(loggerName, logger);
			}
			return logger;
		}
		 
	}
	
	/**
	 * 	���ۿ� �ִ� �α� ������ ���Ϸ� ����Ѵ�
	 * @throws LogSecurityException		�������� ���� ����ڰ� ȣ�� �� �߻�
	 */
	public static void LogFlush() throws LogSecurityException{
		
		StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
		if( stacks.length < 3 )		throw new LogSecurityException();
		
		// �ƹ��������� ȣ���� �� ������ ���� ó��
		if( !stacks[2].getClassName().equals("kr.co.bomz.plugin.tool.console.CtrConsole") )		throw new LogSecurityException();
		
		_this.logAllFlush();
	}
	
	private void logAllFlush(){
		try{
			LoggerFileAppender.getInstance().allFlush();
		}catch(Throwable e){}		// ����� ���ܰ� ��µ��� �ʵ��� ó��
	}
	
	class LoggerShutdown extends Thread{
		public void run(){
			logAllFlush();
		}
	}
}
