package kr.co.bomz.osgi;

/**
 * 	OSGi ���� ���� ���ɾ�
 * 
 * @author ���ǳ�
 * @since 1.4
 * @version 1.4
 *
 */
public enum OSGiCommand {

	/**		
	 * 	���ο� OSGi ���񽺸� ���<br>
	 * 	��� ���� �� STOPPING ���·� ����Ѵ�
	 */
	INSTALLED,
	
	/**
	 * 	��ϵ� OSGi ���񽺸� ����<br>
	 * 	STOPPING ���� ���� ���� �� ���񽺸� �����Ѵ�
	 */
	UNINSTALLED,
	
	/**
	 * 	OSGi ������ ���� ���� ����
	 */
	STATE_ACTIVE,
	
	/**
	 * 	OSGi ������ ���� ���� ����
	 */
	STATE_STOP,
	
	/**
	 * 	OSGI ���� ������Ʈ
	 */
	UPDATE
}
