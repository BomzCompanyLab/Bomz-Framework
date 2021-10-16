package kr.co.bomz.di;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;


/**
 * 
 * ������ ���� ���Ͽ� ȯ�漳������ ������ ��� ���ȴ�
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.0
 *
 */
public class ResourceBundle {

	private static final Logger logger = Logger.getRootLogger();
	
	private InputStream[] inputStream;
	private Reader[] reader;
		
	/**
	 * 		Ư�� ���ϸ����� ���ҽ� �߰�
	 * @param resourceFile		���ҽ� ���� ��
	 */
	public ResourceBundle(String resourceFile){
		this.addResourceFile(resourceFile);
	}
	
	/**
	 * 		Ư�� ������ ���ҽ��� ���
	 * @param resourceFile		���ҽ� ���� ��
	 */
	public ResourceBundle(File resourceFile){
		this.addResourceFile(resourceFile.getPath());
	}
	
	/**
	 * 		Ư�� ��ο� �ִ� ��� ���� �� �̸� ������ �´� ��� ���� ���
	 * 		
	 * 		���� ����
	 * 			.*-sample.conf		:	���ϸ��� '-sample' �� ������ Ȯ���ڰ� conf �� ��� ����
	 * 			.*-.*.conf				:	���ϸ� �߰��� '-' �� ���ԵǾ� �ְ� Ȯ���ڰ� conf �� ��� ����
	 * 			project-.*..*			:	���ϸ��� 'project-' �� �����ϴ� ��� Ȯ������ ����
	 * 			[a-c]-sample.*		:	'-sample' �պκ��� 'a' �Ǵ� 'b' �Ǵ� 'd' �θ� �̷�����ִ� ��� Ȯ������ ����
	 * 											��)	aaa-sample.conf (O)
	 * 													bbb-sample.properties (X)
	 * 													ads-sample.conf (X)
	 * 			
	 * 
	 * @param resourceDir		���� ���
	 * @param filePattern		���� �̸� ����
	 */
	public ResourceBundle(String resourceDir, String filePattern){
		if( resourceDir == null || filePattern == null ){
			logger.log(Level.WARN, "ResourceBundle ���� ���� NULL �Դϴ� (dir=", resourceDir, " , filePattern=", filePattern, ")");
			return;
		}
				
		File fileDir = new File(resourceDir);
		if( !fileDir.isDirectory() ){
			logger.log(Level.WARN, "�߸��� ResourceBundle ���� �� (dir=", resourceDir, ")");
			try{	System.out.println("--" + fileDir.getCanonicalPath());	}catch(Exception e){}
			return;
		}
		
		ArrayList<String> filePathList = new ArrayList<String>();
		
		Pattern pattern = Pattern.compile(filePattern);
		for(File file : fileDir.listFiles()){
			if( pattern.matcher( file.getName() ).find() ){		// ���ϸ� ���� �˻�
				filePathList.add( file.getPath() );		// ���Ͽ� �´� ������ ��� �߰�
			}
		}
		
		// ���Ͽ� �´� ������ �߰�
		this.addResourceFile( filePathList.toArray(new String[filePathList.size()]) );	
	}
		
	public ResourceBundle(InputStream inputStream){
		this.inputStream = new InputStream[]{ inputStream };
	}
	
	public ResourceBundle(Reader reader){
		this.reader = new Reader[]{ reader };
	}
	
	private void addResourceFile(String ... resourceFile){
		ArrayList<FileReader> fileReaderList = new ArrayList<FileReader>(resourceFile.length);
		
		for(String resource : resourceFile){
			
			try {
				fileReaderList.add( new FileReader(resource) );
			} catch (FileNotFoundException e) {
				logger.log(Level.WARN, e, "���ҽ� ������ ã�� �� �����ϴ� (path=", resourceFile, ")");
			}
			
		}
		
		int length = fileReaderList.size();
		this.reader = new Reader[ length ];
		
		for(int i=0; i < length; i++)	this.reader[i] = new BufferedReader( fileReaderList.get(i) );
	}
	
	void load(){
		this.parsing(
				this.readResourceFile().toString().split("\n")
			);
	}
	
	private StringBuilder readResourceFile(){
		int size;
		char[] readerData = null;
		byte[] streamData = null;
		
		int length;
		if( this.inputStream != null ){
			streamData = new byte[100];
			length = this.inputStream.length;
		}else{
			readerData = new char[100];
			length = this.reader.length;
		}
		
		StringBuilder buffer = new StringBuilder();

		for(int i=0; i < length; i++){
			try {
				while( true ){
				
					size = (this.inputStream != null ) ? this.inputStream[i].read(streamData) : this.reader[i].read(readerData);
					if( size == -1 || size == 0 )		break;
					
					if( this.inputStream != null )
						buffer.append( new String( streamData, 0, size) );
					else
						buffer.append( new String(readerData, 0, size) );
					
				}
				
			} catch (Exception e) {
				logger.log(Level.WARN, e, "���ҽ� ������ �д� �� ������ �߻��Ͽ����ϴ�");
			}finally{
				if( this.inputStream != null ){
					try{		this.inputStream[i].close();		}catch(Exception e){}
					this.inputStream[i] = null;
				}
				
				if( this.reader != null ){
					try{		this.reader[i].close();		}catch(Exception e){}
					this.reader[i] = null;
				}
				
				buffer.append("\n");
			}
		
		}
		
		streamData = null;
		readerData = null;
		
		return buffer;
	}
	
	/*		�������� ���� �м�		*/
	private void parsing(String[] messages){
		
		try{
			int startPosition;
			String resourceName;
			for(String message : messages){
				startPosition = message.indexOf("=");
				if( startPosition <= 0 )	continue;
				
				resourceName = message.substring(0, startPosition).trim();
				if(resourceName.length() <= 0 || resourceName.charAt(0) == '#' )		continue;
				
				try{
					ServiceStore.addResourceBundle(resourceName, message.substring(startPosition+1).trim() );
				}catch(ResourceBundleException e){
					logger.log(Level.WARN, e, e.getMessage());
				}
				
			}
		}catch(Exception e){
			logger.log(Level.WARN, e, "���ҽ� ���� �м� �� ������ �߻��Ͽ����ϴ�");
		}

	}
	
}
