package kr.co.bomz.plugin.tool.console;

import kr.co.bomz.osgi.OSGiCommand;

/**
 * 
 * �ַܼ� ���ǳ� �����ӿ�ũ ���� �� ����� �� �ִ� ���ɾ� ����
 * 
 * @author ���ǳ�
 * @since 1.4
 * @version 1.4
 *
 */
public class ConsoleCMD {

	/**		����ڰ� ��û�� �� �ִ� ���ɾ�		*/
	static final String[] USER_CMD = new String[]{
		"ss", "help", "exit", "start", "stop", "update", "install", "uninstall"
	};
	
	static final OSGiCommand[] FRM_CMD= new OSGiCommand[]{
		null,		// ���� ���ɾ�
		null,		// ���� ���ɾ�
		null,		// ���� ���ɾ�
		OSGiCommand.STATE_ACTIVE,
		OSGiCommand.STATE_STOP,
		OSGiCommand.UPDATE,
		OSGiCommand.INSTALLED,
		OSGiCommand.UNINSTALLED
	};
		
	/**		��ü ����� ���ɾ� ��		*/
	static final int CMD_LENGTH = USER_CMD.length;
	
	static final int CONSOLE_CMD_SS = 0;
	static final int CONSOLE_CMD_HELP = 1;
	static final int CONSOLE_CMD_EXIT = 2;
}
