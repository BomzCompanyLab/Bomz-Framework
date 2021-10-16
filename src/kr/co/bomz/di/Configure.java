package kr.co.bomz.di;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import kr.co.bomz.di.annotation.Service;
import kr.co.bomz.di.annotation.ServiceOrder;
import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;
import kr.co.bomz.osgi.OSGiConfigure;
import kr.co.bomz.osgi.OSGiConfigureFile;
import kr.co.bomz.osgi.OSGiJarLoader;
import kr.co.bomz.osgi.OSGiState;

/**
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.4
 *
 */
public class Configure{

	private Logger logger = Logger.getRootLogger(); 
	
	private final Set<String> classNameSet= new HashSet<String>();
	
	private ArrayList<ClassPath> classPathList = new ArrayList<ClassPath>();
	
	private final String regex = ( File.separatorChar == '\\' ) ? "\\\\" : File.separator;
			
	private OSGiConfigureFile configureFile = new OSGiConfigureFile();
	
	Configure(){}
		
	/**		�⺻ ���� ��ο��� Ŭ���� ���� �˻�		*/
	void init() throws Exception{
		
		// ���� ���� ������ ����Ǿ� �ִ��� �˻��Ѵ�
		java.util.List<OSGiConfigure> configureList = configureFile.getOSGiConfigures();
		
		if( configureList == null ){
			// ���������� ���ų� �м� �� ������ ���� ��� �н� ���� ��� Ŭ�������� �˻�
			this.classFileAllSearch();			
		}else{
			// �������� ������ ���� ��� �ش� ������ ���
			this.configureFileSearch(configureList);
		}
		
	}
	
	/*		Ŭ���� ���Ϸ� ����� ���� ��� ���� ���		*/
	private void configureFileSearch(java.util.List<OSGiConfigure> configureList){
		int size = configureList.size();
		OSGiConfigure configure;
		for(int i=0; i < size; i++){
			configure = configureList.get(i);
			this.regitService(configure.getName(), configure.getState());		
		}
	}
	
	/**		�н��� �ִ� ��� Ŭ���� ���� �˻�		*/
	private void classFileAllSearch() throws Exception{
		String path = System.getProperty("java.class.path");
		String[] paths = path.split(";");
		for(String p : paths){
			this.classPathList.add( new ClassPath(p, p.length()) );
		}
		
		if( this.classPathList.size() == 0 )		throw new Exception("�⺻ ���� ��� �˻� ����");
	}
	
	void buildService() throws Exception{

		// classes ��ο� �ִ� class ���� �߰�
		for(ClassPath path : this.classPathList){
			// . �н� ������ ����
			if( !path.defualtPath.equals(".") )		this.searchClass(path);
		}
			
		for(String className : this.classNameSet){
			// ������ ���������� ���� ���� �����Ƿ� ������ ACTIVE ���·� ������
			this.regitService(className, OSGiState.ACTIVE);
		}
		
		// ������ ȣ�� �� �ʵ� �� ����
		Map<ServiceOrder, Set<ServiceObject>> serviceOrderMap = this.callServiceConstructOrSettingField();
		
		// �켱������ ���� �ʱ�ȭ �޼ҵ�� OSGi Active �޼ҵ� ȣ��
		this.callServiceInitializationMethod(serviceOrderMap);
		
		// ���� ���� ������Ʈ
		configureFile.updateConfigureFile( ServiceStore.getServiceObjectList() );
	}
	
	/*		ServiceObject ��ü�� �����Ͽ� ServiceStore �� ����Ѵ�		*/
	private void regitService(Object osgiService, OSGiState state){
		ServiceObject serviceObject = this.checkServiceClass(osgiService);
		if( serviceObject == null )		return;
		
		if( state != null )		serviceObject.setState(state);
	
		ServiceStore.addService(serviceObject.getClazz(), serviceObject);
	}
	
	/*		���� �ʱ�ȭ ������ ���� �ʱ�ȭ �޼ҵ�� OSGi Active �޼ҵ� ȣ��		*/
	private void callServiceInitializationMethod(Map<ServiceOrder, Set<ServiceObject>> serviceOrderMap) throws Exception{
		this.callServiceInitializationMethod( serviceOrderMap.get(ServiceOrder.ORDER_1) );
		this.callServiceInitializationMethod( serviceOrderMap.get(ServiceOrder.ORDER_2) );
		this.callServiceInitializationMethod( serviceOrderMap.get(ServiceOrder.ORDER_3) );
		this.callServiceInitializationMethod( serviceOrderMap.get(ServiceOrder.ORDER_4) );
		this.callServiceInitializationMethod( serviceOrderMap.get(ServiceOrder.ORDER_5) );
	}
	
	/*		���� �ʱ�ȭ ������ ���� �ʱ�ȭ �޼ҵ带 ȣ���Ѵ�		*/
	private void callServiceInitializationMethod(Set<ServiceObject> serviceOrderMap) throws Exception{
		if( serviceOrderMap == null )		return;
		
		java.util.Iterator<ServiceObject> serviceObjectList = serviceOrderMap.iterator();
		ServiceObject obj;
		while( serviceObjectList.hasNext() ){
			obj = serviceObjectList.next();
			obj.callServiceInitializationMethod();
			obj.callOSGiActiveMethod();
		}
	}
		
	/*		
	 * 		������ ������ ȣ�� �� �ʵ� �� ����
	 * 		���н� ���� �߻�
	 */
	private Map<ServiceOrder, Set<ServiceObject>> callServiceConstructOrSettingField() throws Exception{
		ServiceObject serviceObject = null;
		java.util.Iterator<String> serviceKeys;
		int success = 0, beforeSuccess = -1;
		final int serviceSize = ServiceStore.getServiceSize();		
	
		InitializationException initException = null;
		
		// ������ �ʱ�ȭ �޼ҵ� ȣ���� ���� ��
		HashMap<ServiceOrder, Set<ServiceObject>> serviceObjectOrderMap = new HashMap<ServiceOrder, Set<ServiceObject>>(5);
				
		while( true ){
			
			serviceKeys = ServiceStore.getServiceKeys();
			while( serviceKeys.hasNext() ){
				serviceObject = ServiceStore.getServiceObject( serviceKeys.next() );
				
				try{
					if( serviceObject.initService() ){
						success++;
						// ������ �ʿ� �߰�
						Set<ServiceObject> set = serviceObjectOrderMap.get(serviceObject.getServiceOrder());
						if( set == null ){
							set = new HashSet<ServiceObject>();
							serviceObjectOrderMap.put(serviceObject.getServiceOrder(), set);
						}
						
						set.add(serviceObject);
					}
				}catch(InitializationException e){
					initException = e;
				}

			}
			
			// ���� ���� ��� ���� �� ���� ����
			if( serviceSize == success )	break;
			
			if( success == 0 ){
				// ���� ����
				if( serviceObject != null )
					logger.log(Level.FATAL, serviceObject.getClazz().getName(), " ���� �ʱ�ȭ ����");
				
				throw initException;
				
			}else if( beforeSuccess == -1 ){
				beforeSuccess = success;
				
			}else if( beforeSuccess >= success ){
				// ������ ���Ͽ� ���Ӱ� �߰��Ȱ� ���� ��� ���� ���� ����
				if( serviceObject != null )
					logger.log(Level.FATAL, serviceObject.getClazz().getName(), " ���� �ʱ�ȭ ����");
				
				throw initException;
				
			}else{
				beforeSuccess = success;
			}
			
		}
		
		return serviceObjectOrderMap;
	}
		
	/*
	 *		@kr.co.bomz.di.annotation.Service ������̼��� ������ Ŭ������ ���
	 *		�ش� Ŭ���� ��ü�� �����Ѵ�.
	 *		�ƴ� ��� NULL �� �����Ѵ� 		
	 */
	ServiceObject checkServiceClass(Object osgiService){

		Class<?> clazz = null;
		OSGiJarLoader loader = null;
		try {
			if( osgiService instanceof String ){		// Ŭ���� �������� ���
				clazz = Class.forName(osgiService.toString());
				
			}else if( osgiService instanceof File ){	// JAR ���� �������� ���
				loader = new OSGiJarLoader((File)osgiService);
				loader.loadManifest();
				clazz = loader.findJarClass();
			}else{
				logger.log(Level.ERROR, "�߸��� �������� OSGi ���� ��� ��û (", osgiService, ")");
				return null;
			}
			
			// �ε� �� ������ ��� null �� ���ϵ�
			if( clazz == null )			return null;
			
			Annotation[] annotations = clazz.getDeclaredAnnotations();
			for(Annotation annotation : annotations){
				
				if( annotation.annotationType() == Service.class){
					Service service = (Service)annotation;
					ServiceObject result = new ServiceObject(service.type(), service.order(), clazz);
					result.setFileInfo( osgiService );
					return result;
				}
			}
			
		} catch (Exception e){
			logger.log(Level.ERROR, e, "Ŭ���� �ʱ�ȭ ����");
			return null;
		} finally{
			if( loader != null)		loader.closeJarFile();
		}

		return null;
	}
//	ServiceObject checkServiceClass(Object osgiService){
//
//		Class<?> clazz = null;
//		OSGiClassLoader loader = null;
//		try {
//			if( osgiService instanceof String[] ){		// Ŭ���� �������� ���
//				loader = new OSGiClassLoader();
//				
//				clazz = loader.defineClass((String[])osgiService);
////				String[] data = (String[])osgiService;
////				Class.forName(data[0] + "+" + data[1], true, loader);
//				
//			}else if( osgiService instanceof File ){	// JAR ���� �������� ���
//				loader = new OSGiJarLoader((File)osgiService);
//				((OSGiJarLoader)loader).loadManifest();
//				clazz = ((OSGiJarLoader)loader).findJarClass();
//				
//			}else{
//				logger.log(Level.ERROR, "�߸��� �������� OSGi ���� ��� ��û (", osgiService, ")");
//				return null;
//			}
//			
//			// �ε� �� ������ ��� null �� ���ϵ�
//			if( clazz == null )			return null;
//			
//			Annotation[] annotations = clazz.getDeclaredAnnotations();
//			for(Annotation annotation : annotations){
//				
//				if( annotation.annotationType() == Service.class){
//					Service service = (Service)annotation;
//					ServiceObject result = new ServiceObject(service.type(), service.order(), clazz);
//					result.setFileInfo( osgiService );
//					return result;
//				}
//			}
//			
//		} catch (ClassNotFoundException e) {
//			logger.log(Level.ERROR, e, "Ŭ���� ���� �˻� ����");
//			return null;
//		} catch (Exception e){
//			logger.log(Level.ERROR, e, "Ŭ���� �ʱ�ȭ ����");
//			return null;
//		} finally{
//			if( loader != null && loader instanceof OSGiJarLoader){
//				((OSGiJarLoader)loader).closeJarFile();
//			}
//		}
//
//		
//		
//		return null;
//	}

	/*
	 * 		Ŭ���� �н� ��ο� �ִ� ��� Ŭ���� ���� ����� �˻��Ͽ� ����Ʈ�� �����Ѵ�
	 */
	private void searchClass(ClassPath classPath){
		String path = classPath.getDefualtPath();
		File file = new File(path);
		
		if( file.isFile() )		return;
		
		this.searchClassDir(file, classPath);
	}

	/*		�˻��� ������ ���丮�� ���		*/
	private void searchClassDir(File file, ClassPath classPath){
		File[] files = file.listFiles();
		
		if( files == null )		return;
		
		String path = classPath.getDefualtPath();
		final String defaultPath = path.substring( ((path.length() > classPath.cutLength)?classPath.cutLength+1:classPath.cutLength) ).replaceAll(this.regex, ".");
		String fileName;
		for(File f : files){
			if( f.isFile() ){
				fileName = f.getName();
				// Ȯ���ڰ� class �� �ƴ� ��� ó�� ����
				if( !fileName.endsWith(".class") )		continue;
				this.classNameSet.add( (defaultPath.equals("") ? "" : (defaultPath + ".")) + fileName.substring(0, fileName.length()-6) );
			}else{
				this.searchClass( new ClassPath(f.getPath(), classPath.cutLength) );
			}
		}
	}

	class ClassPath{
		private String defualtPath;
		private int cutLength;
		
		public ClassPath(String defaultPath, int cutLength){
			this.defualtPath = defaultPath;
			this.cutLength = cutLength;
		}

		public String getDefualtPath() {
			return defualtPath;
		}

		public int getCutLength() {
			return cutLength;
		}
	}
		
}



