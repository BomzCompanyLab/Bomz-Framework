package kr.co.bomz.di;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import kr.co.bomz.di.annotation.Resource;
import kr.co.bomz.di.annotation.ServiceConstructor;
import kr.co.bomz.di.annotation.ServiceInitialization;
import kr.co.bomz.di.annotation.ServiceOrder;
import kr.co.bomz.di.annotation.ServiceType;
import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;
import kr.co.bomz.osgi.OSGiSecurityException;
import kr.co.bomz.osgi.OSGiState;
import kr.co.bomz.osgi.OSGiStateException;
import kr.co.bomz.osgi.annotation.OSGiActive;
import kr.co.bomz.osgi.annotation.OSGiStop;


/**
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.4
 *
 */
public class ServiceObject {
	
	private static final Logger logger = Logger.getRootLogger();
	
	private final ServiceType policy;
	private final ServiceOrder order;
	
	/*
	 * 		OSGi ������̼��� ������ �޼ҵ�
	 * 		[0] = Active Method
	 * 		[1] = Stopping Method
	 */
	private Method[] osgiMethod = new Method[2];

	/*		OSGi Active Method Number		*/
	static final int OSGI_ACTIVE = 0;
	/*		OSGi Stopping Method Number		*/
	static final int OSGI_STOPPING = 1;
	
	/*		���� ���� ���� ����		*/
	private OSGiState state = OSGiState.RESOLVED;
		
	private final Class<?> clazz;
	private Object instance;
//	private OSGiClassLoader classLoader;
		
	/*
	 * 1. JAR ������ �̿��� �ν����� ���
	 * 		- java.io.File ��ü �̿�
	 * 		- update ����� ���� ���
	 * 
	 * 2. Class ������ ���
	 * 		- java.lang.String ��ü �̿�
	 */
	private Object fileInfo;
		
	ServiceObject(ServiceType policy, ServiceOrder order, Class<?> clazz){//, OSGiClassLoader classLoader){
		this.policy = (policy == null) ? ServiceType.SINGLETON : policy;
		this.order = (order == null) ? ServiceOrder.ORDER_3 : order;
		this.clazz = clazz;
//		this.classLoader = classLoader;
	}
	
	private Object init(boolean isNewInstance) throws InitializationException{		
		// ������ �˻� �� ����
		Object obj =  this.newInstanceService(isNewInstance);
		
		// �ʵ忡 �� ����
		this.initFieldValue(obj, isNewInstance);
		this.checkOSGiMethod();
		
		if( isNewInstance )		logger.log(Level.DEBUG, "���� ���� : ", this.clazz.getName());
		
		return obj;
	}
	
	/*
	 * 		OSGi ��� ���� OSGiActive() , OSGiStop() ������̼���
	 * 		�ΰ��̻� �����Ǿ� �ִ��� ��ȿ�� �˻�
	 * 
	 * 		�ش� ������̼��� ���ų� �Ѱ��� �����Ǿ� �־�� �Ѵ�
	 */
	private void checkOSGiMethod() throws InitializationException{
		int active = 0, stop = 0, parameterLength;
		Class<? extends Annotation> methodAnno;
		for(Method m : this.clazz.getDeclaredMethods()){
			parameterLength = m.getParameterTypes().length;
			
			for(Annotation anno : m.getAnnotations()){
				methodAnno = anno.annotationType();
				if( methodAnno == OSGiActive.class ){
					active = this.checkOSGiMethod(OSGI_ACTIVE, m, active, parameterLength);
					
				}else if( methodAnno == OSGiStop.class ){
					stop = this.checkOSGiMethod(OSGI_STOPPING, m, stop, parameterLength);
				}
			}
		}
		
		if( active > 1 || stop > 1 ){
			logger.log(Level.DEBUG, "OSGi ���� ������̼��� �ߺ� ���� ���� (���񽺸�=", this.clazz.getName(), ", ACTIVE=", active, ", STOP=", stop, ")");
			this.osgiMethod[OSGI_ACTIVE] = null;
			this.osgiMethod[OSGI_STOPPING] = null;
			throw new InitializationException();
		}
		
	}
	
	private int checkOSGiMethod(int type, Method m, int count, int parameterLength){
		if( parameterLength != 0 ){
			logger.log(Level.DEBUG, (type==OSGI_ACTIVE?"ACTIVE":"STOPPING"), " ������̼� �޼ҵ��� �Ķ���� ���� 0�� �̻��̹Ƿ� ����ó��");
			return count;
			
		}else{
			this.osgiMethod[type] = m;
			return count++;
		}
	}
	
	private void callServiceInitializationMethod(Object obj) throws InitializationException{
		Method[] methods = this.clazz.getDeclaredMethods();
		Method method = null;
		
		for(Method m : methods){
			
			if( m.getAnnotation(ServiceInitialization.class) != null ){
				if( method == null )		method = m;
				else	{
					throw new ServiceInitializationException("ServiceInitialization ������̼��� 2�� �̻� �����Ǿ� �ֽ��ϴ� : " + this.clazz.getName());
				}
			}
			
		}
		
		if( method == null )	return;
		
		String methodName = method.getName();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Class<?>[] parameterTypes = method.getParameterTypes();
		Object[] parameters = this.getParameterValues(methodName, parameterAnnotations, parameterTypes);
		
		boolean accessible = method.isAccessible();
		if( !accessible )		method.setAccessible(true);
		try {
			method.invoke(obj, parameters);
		} catch (Exception e) {
			System.out.println("----------" + obj);
			throw new ServiceInitializationException("ServiceInitialization ������̼� �޼ҵ� ���� �� ����", e);
		}finally{
			if( !accessible )		method.setAccessible(false);
		}
	}
	
	/*
	 * 	ServiceInitialization ������̼��� ������ �޼ҵ带 �˻��Ͽ� �����Ѵ�
	 */
	void callServiceInitializationMethod() throws InitializationException{
		
		if( this.policy != ServiceType.SINGLETON )		return;
		
		this.callServiceInitializationMethod(this.instance);		
	}
	
	/**
	 * 		���ǳ� �����ӿ�ũ �ʱ�ȭ �� ���񽺰� ACTIVE ������ ���<br>
	 * 		OSGi ACTIVE �޼ҵ带 ȣ��
	 */
	void callOSGiActiveMethod(){
		
		if( this.policy != ServiceType.SINGLETON )		return;
	
		if( this.state == OSGiState.ACTIVE)		this.callOsgiMethod(OSGI_ACTIVE);
		
	}
	
	/**
	 * 		OSGi Uninstall �۾��� �����Ѵ�
	 */
	void callOSGiUninstall(){
		this.osgiMethod = null;
		this.state = null;
		this.instance = null;
//		this.classLoader.close();
//		this.classLoader = null;
	}
	
	/*
	 * 	Resource ������̼��� ����� �ʵ带 �˻��Ͽ� ���� �����Ѵ�
	 * 	setter �޼ҵ尡 �ִٸ� setter �޼ҵ带 ���� ���� �����ϰ�
	 *  ���ٸ� �ʵ忡 ���� ���� �����Ѵ�
	 */
	private void initFieldValue(Object instance, boolean isNewInstance) throws InitializationException{
		
		Annotation[] annotations;
		Field[] fields = this.clazz.getDeclaredFields();

		for(Field field : fields){
			
			annotations = field.getAnnotations();
			for(Annotation annotation : annotations){
				if( annotation.annotationType() == Resource.class){
					this.initFieldValue(instance, field, (Resource)annotation, isNewInstance);
					break;
				}
			}
			
		}
		
	}
	
	private void initFieldValue(Object instance, Field field, Resource resource, boolean isNewInstance) throws InitializationException{
		String methodName = null;
		Object parameter = null;
		
		switch( resource.type() ){
		case SERVICE:
			try{
				parameter = ServiceStore.getService(field.getType());
				if( parameter == null ){
					throw new ServiceParameterException("��ϵ��� ���� ���� : " + field.getType());
				}
				methodName = this.getSetterMethodName(field.getName(), false);
			}catch(OSGiStateException e){
				// ���񽺰� STOP �̳� RESOLVED ������ ��� �߻��ϹǷ� �����Ѵ�
				return;
			}
			break;
			
		case PROPERTIES_FILE:
			String resourceValue = ServiceStore.getResourceValue(resource.value());
			if( resourceValue == null ){
				if( resource.useDefaultValue() )
					resourceValue = resource.defaultValue();
				else
					throw new ServiceParameterException("��ϵ��� ���� ���ҽ� : " + resource.value());
			}
			FieldType fieldTypeProperties =  this.isFieldType(field.getType());
			methodName = this.getSetterMethodName(field.getName(), fieldTypeProperties == FieldType.BOOLEAN );
			parameter = this.changeFieldType( fieldTypeProperties, resourceValue, resource.useDefaultValue(), resource.defaultValue());
			break;
			
		case CODE:
			FieldType fieldTypeCode =  this.isFieldType(field.getType());
			methodName = this.getSetterMethodName(field.getName(), fieldTypeCode == FieldType.BOOLEAN );
			parameter = this.changeFieldType( fieldTypeCode, resource.value(), resource.useDefaultValue(), resource.defaultValue() );
			break;
			
		default:
			throw new InitializationException("���ǵ��� ���� ���ҽ� Ÿ�� : " + resource.type());
		}
		
		if( parameter == null ){
			throw new ServiceParameterException("�ʵ��� ���� ã�� �� �����ϴ�");
		}
				
		this.initFieldValue(this.clazz, instance, methodName, field, parameter, isNewInstance);		
	}
	
	/*		�ʵ� �� ����		*/
	private void initFieldValue(Class<?> clazz, Object serviceInstance, String methodName, Field field, Object parameter, boolean isNewInstance){
		Method method = null;
		try {
			method = clazz.getDeclaredMethod(methodName, field.getType());
		}catch(NoSuchMethodException e){	
		}catch(Exception e){
			throw new InitializationException("Setter �޼ҵ� �˻� �� ������ �߻� : " + methodName, e);
		}
		
		/*
		 *  ���Ӱ� �����Ұ� �ƴ϶�� �ʵ������ ���� �ʴ´�
		 *  ������Ÿ���� ���� �ʱ�ȭ�˻�� ���� �����Ǵ°��� �������� �κ�
		 */
		if( !isNewInstance )	return;
		
		if( method == null ){
			boolean accessible = field.isAccessible();
			try {
				if( !accessible )	field.setAccessible(true);
				field.set(serviceInstance, parameter);
			} catch (Exception e) {
				throw new InitializationException(e);
			}finally{
				if( !accessible )	field.setAccessible(false);
			}
		}else{
			boolean accessible = method.isAccessible();
			try {
				if( !accessible )	method.setAccessible(true);
				method.invoke(serviceInstance, parameter);
			} catch (Exception e) {
				throw new InitializationException(e);
			}finally{
				if( !accessible )	method.setAccessible(false);
			}
		}
	}
	
	// setter �� �°� �̸� ��ȯ
	private String getSetterMethodName(String fieldName, boolean isBoolean){
		
		String _propertyName = null;
		
		if( isBoolean ){
			if( fieldName.length() < 2 )	_propertyName = "set" + Character.toUpperCase(fieldName.charAt(0));
			else if( fieldName.substring(0, 2).equals("is") || fieldName.substring(0, 2).equals("Is") ){
				// Isabcd = setIsabcd   ,   IsAbcd = setAbcd
				int _tmp = (int)fieldName.charAt(2);
				
				if( _tmp >= 65 && _tmp <= 90)	// �빮���� ���
					_propertyName = "set" + fieldName.substring(2);
				else
					_propertyName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
				
			}else
				_propertyName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
			
		}else{
			_propertyName = "set" + Character.toUpperCase(fieldName.charAt(0));
			if( fieldName.length() > 1 )	_propertyName += fieldName.substring(1);
		}
		
		return _propertyName;
	}
	
	/*
	 * 	�˸��� �����ڸ� �˻��Ͽ� �����Ѵ�
	 */
	private Object newInstanceService(boolean isNewInstance) throws InitializationException{
		
		Constructor<?>[] constructors = this.clazz.getDeclaredConstructors();
		
		Constructor<?> defaultConstructor = null;
		Constructor<?> serviceConstructor = null;
		for(Constructor<?> constructor : constructors){
			
			if( constructor.getAnnotation(ServiceConstructor.class) != null ){
				if( serviceConstructor == null )		serviceConstructor = constructor;
				else						throw new ServiceConstructorException(this.clazz.getName() + " Ŭ������ �ΰ� �̻��� ServiceConstructor �� ������");
			}
			
			if( constructor.getParameterTypes().length == 0 ){
				// �⺻ ������ ���
				defaultConstructor = constructor;
			}
			
		}
		
		if( serviceConstructor == null ){	// �⺻ �����ڷ� ����
			
			// ���� �����ڵ� ���� �⺻ �����ڵ� ���� ��� ���� �߻�
			if( defaultConstructor == null )		throw new ServiceConstructorException(clazz.getName() + " Ŭ������ �⺻ ������ �� ServiceConstructor �� �����Ǿ� ���� ����");
			return isNewInstance ? this.newInstanceConstructor(defaultConstructor) : null;
			
		}else{
			// ServiceConstructor ������̼��� ������ �����ڷ� ����
			return isNewInstance ? this.newInstanceConstructor(serviceConstructor) : null;
		}
		
	}
	
	private Object newInstanceConstructor(Constructor<?> constructor) throws ServiceConstructorException, NoSuchFieldTypeException{
		
		Object[] parameterValues = this.getParameterValues(constructor.getName(), constructor.getParameterAnnotations(), constructor.getParameterTypes());

		boolean accessible = constructor.isAccessible();
		if( !accessible )	constructor.setAccessible(true);
		
		try{
				return constructor.newInstance(parameterValues);
		}catch (Exception e) {
			throw new ServiceConstructorException(clazz.getName() + " �� �⺻�����ڷ� ���� �� ������ �߻�", e);
		}finally{
			if( !accessible )		constructor.setAccessible(false);
		}
		
	}

	/*
	 * 	Resource ������̼ǰ� �Ķ���� Ÿ���� �м��Ͽ� �˸��� �Ķ���� ���� ����ִ�
	 *  ������Ʈ �迭�� �Ѱ��ش�
	 *  
	 *   ���� �߻��� ���� �߻�
	 */
	private Object[] getParameterValues(String methodName, Annotation[][] parameterAnnotations, Class<?>[] parameterTypes) throws ServiceParameterException, NoSuchFieldTypeException{
		final int parameterLength = parameterAnnotations.length;
		final Object[] result = new Object[parameterLength];
		
		boolean checkFlag;
		for(int i=0; i < parameterLength; i++){
			int annotationLength = parameterAnnotations[i].length;
			
			// ������̼��� �ּ� �Ѱ��� �־�� �Ѵ�.
			if( annotationLength == 0 )	throw new ServiceParameterException(this.clazz.getName() + " " + methodName + "() " + (i+1) + " ��° �Ķ���Ϳ� Resource ������̼��� ���ǵ��� �ʾҽ��ϴ�");
			
			checkFlag = true;
			for(int index=0; index < annotationLength; index++){
				if( parameterAnnotations[i][index].annotationType() == Resource.class ){
					
					result[i] = this.getParameterValue(methodName, (Resource)parameterAnnotations[i][index], parameterTypes[i]);
					checkFlag = false;
					
				}
			}
			
			if( checkFlag )	throw new ServiceParameterException(this.clazz.getName() + " " + methodName + "() " + (i+1) + " ��° �Ķ���Ϳ� Resource ������̼��� ���ǵ��� �ʾҽ��ϴ�");
			
		}
		
		return result;
	}
		
	private Object getParameterValue(String methodName, Resource resource, Class<?> parameterType) throws ServiceParameterException, NoSuchFieldTypeException{
		
		switch( resource.type() ){
		case SERVICE:
			return this.getParameterValueServiceType(methodName, resource, parameterType);
			
		case PROPERTIES_FILE:
			return this.getParameterValuePropertiesFileType(methodName, resource, parameterType);
			
		case CODE:
			return this.getParameterValueCodeType(methodName, resource, parameterType);
			
		default:
			throw new ServiceParameterException(this.clazz.getName() + " " + methodName + " �� ���ǵ��� ���� Resource ������̼� Ÿ��  : " + resource.type());
		}
		
	}
	
	private Object getParameterValuePropertiesFileType(String methodName, Resource resource, Class<?> parameterType) throws ServiceParameterException, NoSuchFieldTypeException{
		
		String resourceValue = resource.value();
		resourceValue = ServiceStore.getResourceValue(resourceValue);
		
		if( resourceValue == null ){
			if( resource.useDefaultValue() )
				resourceValue = resource.defaultValue();
			else
				throw new ServiceParameterException("��ϵǾ� ���� ���� ���ҽ� ��û : " + resource.value());
		}
		
		FieldType fieldType = this.isFieldType(parameterType);		
		return this.changeFieldType(fieldType, resourceValue, resource.useDefaultValue(), resource.defaultValue());
	}
	
	private Object getParameterValueCodeType(String methodName, Resource resource, Class<?> parameterType) throws ServiceParameterException, NoSuchFieldTypeException{
		String resourceValue = resource.value();
		FieldType fieldType = this.isFieldType(parameterType);		
		return this.changeFieldType(fieldType, resourceValue, resource.useDefaultValue(), resource.defaultValue());
	}
	
	private Object changeFieldType(FieldType fieldType, String fieldValue, boolean useDefaultValue, String defaultValue) throws ServiceParameterException{
		try{
			switch( fieldType ){
			case INTEGER:
				return Integer.parseInt(fieldValue);
			case STRING:
				return fieldValue;
			case BOOLEAN:
				if( fieldValue.equalsIgnoreCase("true") )		return true;
				if( fieldValue.equalsIgnoreCase("false") )		return false;
				throw new ServiceParameterException("�߸��� Boolean �� ��û : " + fieldValue);
			case CHARACTER:
				if( fieldValue.length() != 1 )	throw new ServiceParameterException("�߸��� Character �� ��û : " + fieldValue);
				return Character.valueOf( fieldValue.charAt(0) );
			case DOUBLE:
				return Double.valueOf(fieldValue);
			case FLOAT:
				return Float.valueOf(fieldValue);
			case LONG:
				return Long.valueOf(fieldValue);
			case SHORT:
				return Short.valueOf(fieldValue);
			case BYTE:
				return Byte.valueOf(fieldValue);
			case OBJECT:
				return (Object)fieldValue;
			default:
				throw new ServiceParameterException("���ǵ��� ���� ��ü Ÿ�� : " + fieldType.name());
			}
		}catch(Exception e){
			// ����ȯ �� ���� �߻�
			if( useDefaultValue )
				return this.changeFieldType(fieldType, defaultValue, false, null);
			else
				throw new ServiceParameterException("�ʵ� �� ����ȯ �� ���� �߻� : �ʵ�Ÿ��(" + fieldType + ") , �ʵ� ��(" + fieldValue + ")", e);
		}
		
	}
	
	/*
	 * 	�ʵ� �� �Ķ���� Ŭ���� ������ �����Ѵ�
	 */
	private FieldType isFieldType(Class<?> field) throws NoSuchFieldTypeException{
		
		String fieldTypeAsString = field.toString();
		
		if( fieldTypeAsString.indexOf("int") != -1 || fieldTypeAsString.indexOf("Integer") != -1){
			return FieldType.INTEGER;
		}else if( fieldTypeAsString.indexOf("String") != -1 ){
			return FieldType.STRING;
		}else if( fieldTypeAsString.indexOf("boolean") != -1 || fieldTypeAsString.indexOf("Boolean") != -1){
			return FieldType.BOOLEAN;
		}else if( fieldTypeAsString.indexOf("char") != -1 || fieldTypeAsString.indexOf("Character") != -1){
			return FieldType.CHARACTER;
		}else if( fieldTypeAsString.indexOf("double") != -1 || fieldTypeAsString.indexOf("Double") != -1){
			return FieldType.DOUBLE;
		}else if( fieldTypeAsString.indexOf("float") != -1 || fieldTypeAsString.indexOf("Float") != -1){
			return FieldType.FLOAT;
		}else if( fieldTypeAsString.indexOf("long") != -1 || fieldTypeAsString.indexOf("Long") != -1){
			return FieldType.LONG;
		}else if( fieldTypeAsString.indexOf("short") != -1 || fieldTypeAsString.indexOf("Short") != -1){
			return FieldType.SHORT;
		}else if( fieldTypeAsString.indexOf("byte") != -1 || fieldTypeAsString.indexOf("Byte") != -1){
			return FieldType.BYTE;
		}else if( fieldTypeAsString.indexOf("Object") != -1 ){
			return FieldType.OBJECT;
		}
		
		throw new NoSuchFieldTypeException("�˼� ���� �ʵ� Ÿ�� : " + field);
	}
	
	private Object getParameterValueServiceType(String methodName, Resource resource, Class<?> parameterType) throws ServiceParameterException{
		
		Object obj = ServiceStore.getService(parameterType);
		
		if( obj == null ){
			throw new ServiceParameterException(this.clazz.getName() + " " + methodName + " ���� ������ �Ķ���� Ÿ�� " + parameterType.getName() + " �� ��ϵǾ� ���� ����");
		}
		
		if( obj.getClass() == parameterType )		return obj;
		
		throw new ServiceParameterException(this.clazz.getName() + " " + methodName + " Service Ÿ���� �Ķ���� ��ü�� ���� ��ü�� ������ �ٸ�");
	}
	
	Class<?> getClazz() {
		return clazz;
	}
	
	final synchronized Object getService(boolean initFlag) throws OSGiStateException{
		
		// ���� ���� ���� �˻�
		if( this.state != OSGiState.ACTIVE )		throw new OSGiStateException(this.clazz.getName() + " : " + this.state.name()); 
		
		if( policy == ServiceType.SINGLETON ){
			return this.instance;
			
		}else{		// ServiceType.PROTOTYPE
			
			try{
				Object protoInstance = this.init(true);
				if( !initFlag )				this.callServiceInitializationMethod(protoInstance);
				return protoInstance;
			}catch(InitializationException e){
				return null;
			}
			
		}
		
	}
	
	/**
	 * 		�ʱ�ȭ �۾����� ȣ��ȴ�<br>
	 * 		����Ÿ���� �̱����� ��� �ν��Ͻ� ����<br>
	 * 		����Ÿ���� ������Ÿ���� ��� ������ �˻�
	 * 
	 * @return	�ʱ�ȭ ���� �� true, ���н� false
	 * @throws InitializationException		�ʱ�ȭ ���н� �߻�
	 */
	final boolean initService() throws InitializationException{
		
		if( this.instance == null ){
			this.instance = this.init( this.policy == ServiceType.SINGLETON );
			return true;
		}else{
			return false;
		}
	}
	
	final ServiceOrder getServiceOrder(){
		return this.order;
	}
	
	public String getClassName(){
		return this.clazz.getName();
	}
	
	public OSGiState getServiceState(){
		return this.state;
	}
	
	/**
	 * 	���� ���¸� STOPPING ���� �����Ѵ�
	 * 
	 * @throws OSGiSecurityException		�������� ���� ����� ȣ��� ���� �߻�
	 */
	boolean updateState(int state) throws OSGiSecurityException{
		// �̱��� ���񽺰� �ƴ϶�� OSGi ����� ����� �� ����
		if( policy != ServiceType.SINGLETON ){
			logger.log(Level.INFO, "@Service() , @Service(type=ServiceType.SINGLETON) �� ���ǵ� ���񽺸� OSGi ���� ���� (", this.clazz.getName(), ")");
			return false;
		}
		
		// ������ ����� ���� �˻�
		this.checkSecurity();
		
		// ������̼����� ������ �޼ҵ� ȣ��
		this.callOsgiMethod(state);
		
		// �ٸ� ���� �� �ش� ���񽺸� �̿��ϴ� ���� ���� ����
		switch( state ){
		case OSGI_ACTIVE :
			this.updateFieldValues( this.instance );
			this.state = OSGiState.ACTIVE;		// ���°� ����
			break;
			
		case OSGI_STOPPING :
			this.updateFieldValues(null);				// ����� ���� ��� NULL �� ����
			this.state = OSGiState.STOPPING;		// ���°� ����
			break;
		}
			
		return true;
	}
	
	private void updateFieldValues(Object parameter){
		java.util.Iterator<ServiceObject> list = ServiceStore.getServiceObjectList();
		ServiceObject obj;
		while( list.hasNext() ){
			obj = list.next();
			
			// ������Ÿ������ ����� ������ �ʵ尪�� �ٲ� �� ����
			if( obj.policy != ServiceType.SINGLETON )		continue;
			
			if( obj.clazz == this.clazz )		continue;		// �ڱ� �ڽ��� ó������ �ʴ´�
			
			this.updateFieldValues( 
					obj.clazz, 
					obj.instance,
					parameter
				);
		}
	}
	
	/*		�ش� ���񽺸� �̿��ϴ� ��� �ʵ� ���� �����Ѵ�		*/
	private void updateFieldValues(Class<?> clazz, Object instance, Object parameter){
				
		String methodName;
		
		for(Field field : instance.getClass().getDeclaredFields()){
			if( field.getType() != this.clazz )					continue;
						
			methodName = this.getSetterMethodName(field.getName(), false);
			this.initFieldValue(clazz, instance, methodName, field, parameter, true);
			
		}
		
	}
	
	/*		
	 * 		OSGi ���� �޼ҵ� ȣ��
	 * 		Active ������̼��̳� Stopping ������̼��� ������ �޼ҵ� ȣ��
	 */
	private void callOsgiMethod(int type){
		if( this.osgiMethod[type] == null ){
//			logger.log(Level.DEBUG, (type==OSGI_ACTIVE?"ACTIVE":"STOPPING"), "������̼��� ������ �޼ҵ尡 �����ϴ� (", this.clazz.getName(), ")");
			return;
		}
		
		this.osgiMethod[type].setAccessible(true);
		try {
			this.osgiMethod[type].invoke( this.instance );
		}catch(Exception e){
			logger.log(Level.WARN, e);
		}finally{
			this.osgiMethod[type].setAccessible(false);
		}
	}
	
	/*		�������� ���� ����ڰ� ���� �� ���� �߻�		*/
	private void checkSecurity() throws OSGiSecurityException{
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		if( stack.length < 4 )		return;		// �ּ� 3 �̻��� ��
		
		if( !stack[3].getClassName().equals("kr.co.bomz.di.ServiceStore") )		throw new OSGiSecurityException();
	}
	
	void setState(OSGiState state) {
		this.state = state;
	}
	
	public Object getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(Object fileInfo) {
		this.fileInfo = fileInfo;
	}

	@Override
	public String toString(){
		StringBuilder buffer = new StringBuilder();
		
		buffer.append( this.fileInfo instanceof java.io.File ? "j," : "c," );
		
		if( this.fileInfo instanceof File ){
			buffer.append( ((File)this.fileInfo).getAbsolutePath() );
		}else{
			// class �ε��� ���
			buffer.append(this.fileInfo.toString());
		}
		
		buffer.append(",");
		buffer.append(this.state.name());
		
		return buffer.toString();
		
//		return (this.classLoader instanceof OSGiJarLoader ? "j," : "c,") +
//				(this.classLoader instanceof OSGiJarLoader ? ((OSGiJarLoader)this.classLoader).getJarFilePath() : this.clazz.getName() ) + 
//				"," + this.state.name();
	}
	
}
