package kr.co.bomz.di;

import java.util.LinkedHashMap;
import java.util.Map;

import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;
import kr.co.bomz.osgi.OSGiCommand;
import kr.co.bomz.osgi.OSGiConfigureFile;
import kr.co.bomz.osgi.OSGiResponse;
import kr.co.bomz.osgi.OSGiSecurityException;
import kr.co.bomz.osgi.OSGiState;
import kr.co.bomz.osgi.OSGiStateException;
import kr.co.bomz.plugin.tool.console.CtrConsole;


/**
 * 
 * 		���� ������ �̿��Ͽ� ���� ����� ������ �� �ִ�<p>
 * 		
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.4
 *
 */
public class ServiceStore {

	private static final Logger logger = Logger.getRootLogger();
	
	private static final ServiceStore _this = new ServiceStore();
	
	/*
	 * 	���� ��ü�� ����ִ� ��
	 */
	private final Map<String, ServiceObject> serviceMap = new LinkedHashMap<String, ServiceObject>();
	
	/*
	 * 	���ҽ� ���� ����ִ� ��
	 */
	private final Map<String, String> resourceMap = new LinkedHashMap<String, String>();
		
	private static boolean initFlag = true;
	
	private final Object lock = new Object();
	
	protected ServiceStore(){
		
	}
	
	void init(){
		try {
			Configure configure = new Configure();
			configure.init();		// �⺻ �н� �˻��Ͽ� Ŭ���� �˻�
			configure.buildService();
		} catch (Exception e) {
			logger.log(Level.FATAL, e);
			System.exit(0);
		}
		
	}
		
	static final void addService(Class<?> clazz, ServiceObject serviceObject){
		_this.serviceMap.put(clazz.getName(), serviceObject);
		
//		logger.log(Level.DEBUG, "���� ��� : ", clazz.getName());
	}
	
	/*		��ϵ� ���� ����		*/
	private final void removeService(Class<?> clazz){
		_this.serviceMap.remove(clazz.getName());
		logger.log(Level.DEBUG, "���� ���� : ", clazz.getName());
	}
	
	/**
	 * 	Service ������̼��� ������ ���� ��ü�� �˻��Ͽ� ����Ѵ�<br>
	 * 
	 * �ѹ��� ȣ�� �����ϸ�, �̹� ȣ��Ǿ��ٸ� �ι�° ���ʹ� ���õȴ�<br>
	 * 
	 * ���� �ܺ� ���������� ���� �̿��Ͽ��� �ϴٸ�<br>
	 * 
	 * addResourceBundle(ResourceBundle ... resourceBundle) �޼ҵ带 ���� ȣ���Ͽ��� �Ѵ�
	 */
	public static final void buildService(){
		synchronized(_this.lock){
			
			if( initFlag ){
				_this.init();
				initFlag = false;
				
				new CtrConsole().init();		// �ܼ� ����â Ȱ��ȭ
			}
		}
	}
	
	/**
	 * 	�ܺ� ���� ���Ͽ� �Էµ� �������� ��Ͻ�Ų��<br>
	 * 
	 * ��ϵ� ���� getResourceValue(String resourceName) �޼ҵ带 ���� ��밡���ϴ�
	 */
	public static final void addResourceBundle(ResourceBundle ... resourceBundle){
		synchronized(_this.lock){
			for(ResourceBundle resource : resourceBundle){
				if( resource == null )			continue;
				resource.load();
			}
		}
	}
	
	/**
	 * 	addResourceBundle(ResourceBundle ... resourceBundle) �޼ҵ带 ���� ��ϵ� ���ҽ� ���� �����Ѵ�
	 */
	public static final String getResourceValue(String resourceName){
		if( resourceName == null || _this == null )		return null;
		
		return _this.resourceMap.get(resourceName);
	}
	
	/**
	 * 	���ҽ��� ����Ѵ�<br>
	 * 
	 *  �̹� ������ �̸��� ���ҽ��� ��ϵǾ� �ִٸ� ��� ���� �ʴ´�<br>
	 * 
	 *  ��� ���н� false , ������ true ����
	 */
	public static final boolean addResourceBundle(String resourceName, String resourceValue) throws ResourceBundleException{
		if( resourceName == null || resourceValue == null )				return false;
		if( resourceName.equals("") || resourceValue.equals("") )		return false;

		if( _this.resourceMap.get(resourceName) != null )		throw new ResourceBundleException("�̹� ��ϵǾ� �ִ� ���ҽ� �̸����� ��û : " + resourceName);

		_this.resourceMap.put(resourceName, resourceValue);
		
		logger.log(Level.DEBUG, "���ҵ� ��� (", resourceName, "=", resourceValue + ")");
		return true;
	}
	
	/**
	 * 	�ش� Ŭ������ �´� ���� ��ü ��û
	 * 
	 * @param clazz		���� ��ü ����
	 * @return				������ ���� ��ü
	 * 								��ϵ��� �ʾҰų� ACTIVE ���°� �ƴ� ��� NULL ����
	 */
	public static final Object getService(Class<?> clazz){
		return getService(clazz.getName());
	}
		
	/**
	 * 	�ش� Ŭ������ �´� ���� ��ü ��û
	 * 
	 * @param clazz		���� ��ü ���� (��Ű����.���ϸ�)
	 * @return				������ ���� ��ü.
	 * 								��ϵ��� �ʾҰų� ACTIVE ���°� �ƴ� ��� NULL ����
	 * 
	 */
	public static final Object getService(String clazz){
		synchronized(_this.lock){
			ServiceObject obj = _this.serviceMap.get(clazz);
			if( obj == null ){
//				logger.log(Level.DEBUG, "��ϵ��� ���� ���� ��û (", clazz, ")");
				return null;
			}else{
				try{
					return obj.getService(initFlag);
				}catch(OSGiStateException e){
					logger.log(Level.DEBUG, "Active ���°� �ƴ� ���� ��û (", e.getMessage(), ")");
					return null;
				}
			}
		}
	}
	
	static final java.util.Iterator<String> getServiceKeys(){
		return _this.serviceMap.keySet().iterator();
	}
	
	static final int getServiceSize(){
		return _this.serviceMap.size();
	}
	
	static final ServiceObject getServiceObject(String serviceName){
		return _this.serviceMap.get(serviceName);
	}
	
	static final java.util.Iterator<ServiceObject> getServiceObjectList(){
		return _this.serviceMap.values().iterator();
	}
		
	/**
	 * 
	 * 	���ο� OSGi ���񽺸� �߰� ����ϰų�<br>
	 * 	������ ���� ���� ���񽺸� ����, ����, ���� �۾��� �Ѵ� 
	 * 	
	 * @param osgiServiceName		OSGi ���� (Service Ŭ������ �Ǵ� JAR���ϰ�ü)
	 * @param cmd							OSGi ���� ����
	 * @param state							OSGi ���� ����
	 * @throws OSGiSecurityException		�������� ���� ����ڰ� ȣ�� �� �߻�
	 */
	public static final OSGiResponse updateServiceState(Object osgiService, OSGiCommand cmd) throws OSGiSecurityException{
		
		// ���Ȱ˻�
		_this.checkSecurity();
		
		OSGiResponse result = null;
		
		synchronized(_this.lock){
			switch(cmd){
			case INSTALLED :				// ���ο� OSGi ���� ���
				result = _this.osgiServiceInstalled(osgiService);			break;
				
			case UNINSTALLED :			// ��ϵ� OSGi ���� ����
				result = _this.osgiServiceUninstalled(osgiService.toString());		break;
				
			case STATE_STOP :		// ��ϵ� OSGi ������ ���� ���� ����
				result = _this.changeServiceState(osgiService.toString(), OSGiState.STOPPING);		break;
				
			case STATE_ACTIVE :		// ��ϵ� OSGi ������ ���� ���� ����
				result = _this.changeServiceState(osgiService.toString(), OSGiState.ACTIVE);		break;
				
			case UPDATE:		// ��ϵ� ���� ������Ʈ
				result = _this.osgiServiceUpdate(osgiService);		break;
			}
		}
		
		OSGiConfigureFile configureFile = new OSGiConfigureFile();
		configureFile.updateConfigureFile( ServiceStore.getServiceObjectList() );
		
		return result;
	}
	
	/**
	 * OSGi ���� ���� ��û�� ���� ����� ����
	 * @param msg		���� �޼���
	 * @param e			���� �߻� �� ���
	 * @return				���䰪
	 */
	private OSGiResponse makeOSGiResponse(String msg, Throwable e){
		if( e == null )		logger.log(Level.WARN, msg);
		else						logger.log(Level.WARN, e, msg);
		return new OSGiResponse(msg);
	}
		
	/*
	 * 		OSGi ���� ������Ʈ
	 * 		1. ���ν���
	 * 		2. �ν���
	 */
	private OSGiResponse osgiServiceUpdate(Object osgiService){
		ServiceObject serviceObject = _this.serviceMap.get(osgiService.toString());
				
		if( serviceObject.getFileInfo() instanceof String )
			return this.makeOSGiResponse("JAR ���Ϸ� ��ϵ� OSGi ���񽺸� Update ���� (" + osgiService + ")", null);
		
		OSGiResponse result = _this.osgiServiceUninstalled(osgiService.toString());
	
		if( result.isSuccess() )		result = _this.osgiServiceInstalled( serviceObject.getFileInfo() );
		
		return result;
	}
	
	/*		
	 * 		OSGi ���� ���ν���
	 * 		1. �ش� �̸��� ���� �˻�
	 * 		2. ���� ���¸� STOPPING ���� ����
	 * 		3. ���� ���� ����		
	 */
	private OSGiResponse osgiServiceUninstalled(String osgiServiceName){
		// 1. �ش� �̸��� ���� �˻�
		ServiceObject serviceObject = _this.serviceMap.get(osgiServiceName);
		if( serviceObject == null )
			return this.makeOSGiResponse("��ϵ��� ���� OSGi ���� UNINSTALL ��û (" + osgiServiceName + ")", null);

		if( serviceObject.getFileInfo() instanceof String )
			return this.makeOSGiResponse("JAR ���Ϸ� ��ϵ� OSGi ���񽺸� Uninstall ���� (" + osgiServiceName + ")", null);
		
		// 2. ���� ���¸� STOPPING ���� ����
		try{
			if( !serviceObject.updateState(ServiceObject.OSGI_STOPPING) )
				// false ���� �� ó�� ����. �̱��� ���񽺰� �ƴ� ��� ��
				return this.makeOSGiResponse("���� Ÿ���� SINGLETON �� �ƴ� ���� ���� ��û (" + osgiServiceName + ")", null);
			
		}catch(OSGiSecurityException e){
			return this.makeOSGiResponse("���ȿ����� UNINSTALL ��û ���� (" + osgiServiceName + ")", e);
		}
		
		// 3. ���� ���� ����
		_this.serviceMap.remove(osgiServiceName);
		serviceObject.callOSGiUninstall();
				
		logger.log(Level.DEBUG, "���� ���ν��� (", osgiServiceName, ")");
		return new OSGiResponse();
	}
	
	/*		OSGi ���� �ν���		*/
	private OSGiResponse osgiServiceInstalled(Object osgiService){
		
		Configure configure = new Configure();
		ServiceObject serviceObject = configure.checkServiceClass(osgiService);
		
		if( serviceObject == null )
			return this.makeOSGiResponse("OSGi ���� ��ġ�� ���� �ʱ�ȭ �۾� ����", null);
		
		if( _this.serviceMap.containsKey(serviceObject.getClassName()) )		// �ߺ� ���� �˻�
			return this.makeOSGiResponse("�̹� ��ϵ� OSGi �����̹Ƿ� ��� ����", null);
				
		try{
			addService(serviceObject.getClazz(), serviceObject);
			serviceObject.initService();
			serviceObject.callServiceInitializationMethod();
			serviceObject.updateState(ServiceObject.OSGI_ACTIVE);
//			serviceObject.callOSGiActiveMethod();
			
		}catch(InitializationException e){
			_this.removeService(serviceObject.getClazz());
			return this.makeOSGiResponse("OSGi ���� �ʱ�ȭ ����", e);
		}
				
		logger.log(Level.DEBUG, "���� �ν���");
		return new OSGiResponse();
	}
		
	/*		��ϵ� OSGi ������ ���� ���¸� ����		*/
	private OSGiResponse changeServiceState(String osgiServiceName, OSGiState state){
		ServiceObject serviceObject = getServiceObject(osgiServiceName);
		if( serviceObject == null )
			return this.makeOSGiResponse("��ϵ��� ���� ���� ���� ���� ��û (" + osgiServiceName + ")", null);
		
		// ���� ���� ���¿� �����Ϸ��� ���°� ������ Ȯ���Ѵ�
		if( serviceObject.getServiceState() == state )	
			return this.makeOSGiResponse("������ ���� ���¿� �����Ϸ��� ���°� ���� (���񽺸�=" + osgiServiceName + " , ����=" + state.name() + ")", null);
				
		// ���º��� ��û�� ���� ó��
		switch( state ){
		case ACTIVE :		// ���� ���·� ����
			serviceObject.updateState(ServiceObject.OSGI_ACTIVE);
			break;
			
		case STOPPING :		// ���� ���·� ����
			serviceObject.updateState(ServiceObject.OSGI_STOPPING);
			break;
			
		case RESOLVED:		break;		// ���ٸ� ó�� ����
		}
		
		logger.log(Level.DEBUG, "���� ���� ���� ���� (���񽺸�=", osgiServiceName, ", ����=", state.name(), ")");
				
		return new OSGiResponse();
	}
	
	/**
	 * ��ϵ� ��� ���� ���� ����
	 * 
	 * @return	���� ���
	 * @throws OSGiSecurityException	���� ���� ����ڰ� ȣ�� �� �߻�
	 */
	public static final java.util.Iterator<ServiceObject> getServices() throws OSGiSecurityException{
		
		_this.checkSecurity();
		
		return _this.serviceMap.values().iterator();
	}
	
	private void checkSecurity() throws OSGiSecurityException{
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		
		// �ּ� 4�� �Ǿ����. [0] Thread [1] ServiceStore [2] ServiceStore [2] ȣ����Ŭ����
		if( stack.length < 4 )				throw new OSGiSecurityException();

//		for(StackTraceElement e : stack){
//			System.out.println("-------------" + e.getClassName());
//		}
		
		// ���� �˻� ����
		if( !stack[3].getClassName().equals("kr.co.bomz.plugin.tool.CtrTool") )		throw new OSGiSecurityException();
	}
	
//	
//	/*		������ �̸��� ���񽺰�ü�� ����		*/
//	private ServiceObject getServiceObject(String osgiServiceName){
//		java.util.Iterator<ServiceObject> iter = _this.serviceMap.values().iterator();
//		ServiceObject obj;
//		while( iter.hasNext() ){
//			obj = iter.next();
//			if( obj.getClassName().equals(osgiServiceName) )		return obj;
//		}
//		
//		logger.log(Level.DEBUG, "��û�� ���� ���� �˻� ���� (���񽺸�=", osgiServiceName, ")");
//		return null;
//	}
}
