package kr.co.bomz.osgi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kr.co.bomz.di.ServiceObject;
import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;

/**
 * 
 * �絿�� �� ������ ȯ�濡�� ������ �� �ֵ��� ����������<br>
 * ȯ�漳�����Ͽ� �����Ѵ�<br><br>
 * 
 * ȯ�漳�����ϸ� : supinan.ini<br><br>
 * 
 * ����<br>
 * type:(c or j),name:(Ŭ������ or JAR���ϸ�),state:(���°�)<br>
 * 
 * @author ���ǳ�
 * @since 1.4
 * @version 1.4
 *
 */
public class OSGiConfigureFile {
	
	private static final Logger logger = Logger.getRootLogger();
	
	private final String OSGI_FILE = "./supinan.ini";
	
	public OSGiConfigureFile(){}
	
	/**
	 * ���� ���ǳ� �����ӿ�ũ ���� ������ ��û�Ѵ�
	 * 
	 * @return	���� ���� ���� ����. ���������� ���ų� �м� �� ���� �߻� �� null ����
	 */
	public List<OSGiConfigure> getOSGiConfigures(){
		
		File file = new File(OSGI_FILE);
		if( !file.isFile() )		return null;		// ���� ������ ���� ���. �� ù ���� ��
		
		return this.readConfigureFile(file);
	}
	
	/**
	 * ���� ������ �м��Ͽ� ����Ʈ ���·� ����
	 * @param file		supinan.ini ���� 
	 * @return			�м� ���. �м� �� ���� �߻� �� null
	 */
	private List<OSGiConfigure> readConfigureFile(File file){
		FileReader fr = null;
		BufferedReader br = null;
		
		String msg = null;
		
		try{
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			
			ArrayList<OSGiConfigure> result = new ArrayList<OSGiConfigure>();
			
			while( true ){
				msg = br.readLine();
				if( msg == null || msg.length() == 0 )		break;
				
				result.add( this.analysisReadConfigureMessage(msg.split(",")) );
			}
			
			return result;
	
		}catch(Exception e){
			
			// ���� �߻� �� null ����
			Logger logger = Logger.getRootLogger();
			logger.log(Level.ERROR, e, "supinan.ini ���� ���� �м� �� ���� (", msg , ")'");
			
			return null;
		}finally{
			if( br != null )		try{		br.close();		}catch(Exception e){}
			if( fr != null )			try{		fr.close();		}catch(Exception e){}
			
		}
		
	}
	
	/**
	 * 	�о���� ���� ���� ������ �м��Ѵ�
	 * @param msg		���� ���� ����
	 * @return				�м� ���
	 */
	private OSGiConfigure analysisReadConfigureMessage(String[] msg) throws AnalysisConfigureFileException{
		// c,kr.co.bomz.schedule.ScheduleService,ACTIVE
		
		OSGiConfigure result = new OSGiConfigure();
		
		// c : classFile , j : JAR File
		if( msg[0].length() != 1 )		throw new AnalysisConfigureFileException();
		result.setClassFile( msg[0].equals("c") );		// c �� ��� Ŭ��������, �ƴҰ�� (j) JAR ����
		
		// Ŭ�������� �Ǵ� JAR ���� ���
		if( msg[1].length() <= 1 )		throw new AnalysisConfigureFileException();
		
		if( result.isClassFile() )		result.setName( msg[1] );
		else									result.setName( new File(msg[1]) );
		
		try{
			result.setState( OSGiState.valueOf(msg[2]) );
		}catch(Exception e){
			throw new AnalysisConfigureFileException(e);
		}
		
		return result;
	}
	
	
	/**		���ǳ� OSGi ȯ�� ���� ���� ���� ����	 */
	private boolean makeFile(File file){
		if( file.isFile() )		return true;

		try {
			return file.createNewFile();
		} catch (IOException e) {
			logger.log(Level.WARN, e, "���ǳ� ȯ�漳�� ���� ���� ����");
			return false;
		}
	}
	
	/**		���ǳ� OSGi ȯ�� ���� ����		*/
	public boolean updateConfigureFile(java.util.Iterator<ServiceObject> serviceObjects){
		
		File file = new File(OSGI_FILE);
		
		if( !this.makeFile(file) )		return false;
		
		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(file));
			
			while( serviceObjects.hasNext() ){
				bw.write(serviceObjects.next().toString());
				if( serviceObjects.hasNext() )	bw.newLine();
			}
			
			bw.flush();
			logger.log(Level.DEBUG, "���ǳ� ȯ�漳������ ���� ����");
			return true;
		}catch(Exception e){
			logger.log(Level.WARN, e, "���ǳ� ȯ�漳������ ���� ����");
			return false;
		}finally{
			if( bw != null )		try{		bw.close();	}catch(Exception e){}
			bw = null;
			file = null;
		}
		
	}
	
}
