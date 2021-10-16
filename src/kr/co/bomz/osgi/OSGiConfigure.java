package kr.co.bomz.osgi;

/**
 * 
 * ���α׷� ���� ���� ����
 * 
 * @author ���ǳ�
 * @since 1.4
 * @version 1.4
 *
 */
public class OSGiConfigure {
	
	
	/**
	 * 		class Ÿ���� ��� java.lang.String
	 * 		jar Ÿ���� ��� java.io.File
	 */
	private Object name;
	
	/**
	 * 		OSGi state
	 */
	private OSGiState state;
	
	public OSGiConfigure(){}

	public boolean isClassFile() {
		return classFile;
	}

	public void setClassFile(boolean classFile) {
		this.classFile = classFile;
	}

	public Object getName() {
		return name;
	}

	public void setName(Object name) {
		this.name = name;
	}

	public OSGiState getState() {
		return state;
	}

	public void setState(OSGiState state) {
		this.state = state;
	}
	
}
