package kr.co.bomz.osgi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;

/**
 * 
 * �������� JAR ������ ��� / ���� �� ���Ǵ� Ŭ�����δ�
 * 
 * @author ���ǳ�
 * @since 1.4
 * @version 1.4
 *
 */
public class OSGiJarLoader extends URLClassLoader{
	
	private static final Logger logger = Logger.getRootLogger();
	
	private String serviceClass;
	
	/**		Ŭ���� �Ǵ� JAR ����		*/
	private final File loadFile;
	private JarFile jarFile;
		
	public OSGiJarLoader(File loadFile) throws Exception{
		super(new URL[]{loadFile.toURI().toURL()});
		this.loadFile = loadFile;
		this.jarFile = new JarFile(this.loadFile);
	}
	
	public Class<?> findJarClass() throws ClassNotFoundException{
		
		try{
			this.loadClassData(this.jarFile, this.jarFile.entries());
			return super.loadClass(this.serviceClass);
		}catch(ClassNotFoundException e){
			throw e;
		}catch(Exception e){
			logger.log(Level.ERROR, e, "Ŭ���� �ε� �� ���� (", this.serviceClass, ")");
			return null;
		}
	}
		
	public void loadManifest() throws Exception{

		Manifest m = this.jarFile.getManifest();

		Attributes attributes = m.getMainAttributes();
		
		// �ε� Ŭ���� ��������
		this.serviceClass = attributes.getValue("Bundle-Activator");		
		if( this.serviceClass == null )		throw new Exception("Bundle-Activator �� �������� �ʾҽ��ϴ�");
		
//		����)  	/lib/log4j-1.2.15.jar,/lib/junit-4.11.jar
		String bundleValue = attributes.getValue("Bundle-ClassPath");
		if( bundleValue == null )		return;
		
		String[] jarFiles = bundleValue.split(",");
		int size = jarFiles.length;
		
		String path = this.loadFile.getParent();

		for(int i=0; i < size; i++){
			this.callAddURL( new File(path + jarFiles[i]) );
		}
	
	}
	
	/**		������ ���̺귯�� �� Ŭ������ �̿��ϰ����� ��� �ش� ������ �߰��Ѵ�		*/
	private void callAddURL(java.io.File file) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, MalformedURLException{
		final Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
		method.setAccessible(true);
		method.invoke(this, new Object[]{file.toURI().toURL()});
	}
	
	private void loadClassData(JarFile jarFile, Enumeration<JarEntry> e) throws Exception{

		String className;
		while( e.hasMoreElements() ){
			
			JarEntry entry = e.nextElement();
			
			if( entry.isDirectory() )		continue;
			if( !entry.getName().endsWith(".class") )		continue;
			
			className = entry.getName();
			className = className.substring(0, className.length() - 6).replaceAll("/", ".").replaceAll("\\\\", ".");
			this.readClassByteData(className,  new BufferedInputStream(jarFile.getInputStream(entry)));
		}
		
	}
	
	/**
	 * 		Ŭ���� ���� ������ �о� �м��Ѵ�
	 * 
	 * @param className		�м��� Ŭ������
	 * @param is					�м��� Ŭ���� ��Ʈ��
	 * @param printLog			���� �߻��� �������� ����Ʈ ����
	 * @return
	 */
	private void readClassByteData(String className, InputStream is){
		try{
			int size = is.available();
			byte[] data = new byte[size];
			is.read(data, 0, size);
			super.defineClass(className, data, 0, size);
		}catch(Throwable e){
			logger.log(Level.ERROR, e, "Ŭ���� �ε� �� ���� (", className, ")");
		}finally{
			try{		is.close();		}catch(Exception e){}
		}
	}
		
	/**		Jar ���� ����		*/
	public void closeJarFile(){
		try{
			this.jarFile.close();
		}catch(Exception e){}
	}
	
	/**		OSGi JAR ���� ��� ����		 */
	public String getJarFilePath(){
		return this.loadFile.getAbsolutePath();
	}
}
