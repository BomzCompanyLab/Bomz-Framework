package kr.co.bomz.plugin.tool;

import java.util.Iterator;

import kr.co.bomz.di.ServiceObject;
import kr.co.bomz.di.ServiceStore;
import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;
import kr.co.bomz.osgi.OSGiCommand;
import kr.co.bomz.osgi.OSGiInstallFileTypeException;
import kr.co.bomz.osgi.OSGiResponse;

/**
 * 
 * ���ǳ� �����ӿ�ũ ��� ���� �� ������ �ش� �������̽��� ��ӹ޾ƾ��Ѵ�
 * 
 * @author ���ǳ�
 * @since 1.4
 * @version 1.4
 *
 */
public abstract class CtrTool {

	private static final Logger logger = Logger.getRootLogger();
	
	protected final OSGiResponse updateServiceState(Object value, OSGiCommand cmd){

		try{
			return ServiceStore.updateServiceState(
				cmd == OSGiCommand.INSTALLED ? this.executeMessageAsInstall(value) : value, cmd);
		}catch(OSGiInstallFileTypeException e){
			logger.log(Level.WARN, "JAR Ȯ���ڰ� �ƴ� ���Ϸ� �ν��� ��û (", value, ")");
			return new OSGiResponse("JAR Ȯ���ڰ� �ƴ� ���Ϸ� �ν��� ��û");
		}
	}
	
	protected Iterator<ServiceObject> getServiceList(){
		return ServiceStore.getServices();
	}
	
	/*
	 * 	INSTALL ��û�� ��� ������ CLASS ���� JAR ���� �˻� �� ó��
	 *  CLASS �� ��� ��Ʈ�� �״�� ����
	 *  JAR �� ��� java.io.File ���·� ����		
	 */
	private Object executeMessageAsInstall(Object file) throws OSGiInstallFileTypeException{
		
		if( file.toString().endsWith(".jar") ){
			// jar ������ �̿��� ����� ���
			return new java.io.File((String)file);
		}
				
		// class �Ǵ� jar ������ �ƴ� ��� ���� �߻�
		throw new OSGiInstallFileTypeException();
	}
}
