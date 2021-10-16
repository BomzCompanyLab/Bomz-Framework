package kr.co.bomz.plugin.tool.console;

import java.util.Iterator;
import java.util.Scanner;

import kr.co.bomz.di.ServiceObject;
import kr.co.bomz.logger.Level;
import kr.co.bomz.logger.Logger;
import kr.co.bomz.logger.LoggerManager;
import kr.co.bomz.osgi.OSGiCommand;
import kr.co.bomz.osgi.OSGiResponse;
import kr.co.bomz.osgi.OSGiState;
import kr.co.bomz.plugin.tool.CtrTool;

/**
 * 		���ǳ� �����ӿ�ũ ��� ���� �ܼ�â
 * 
 * @author ���ǳ�
 * @since 1.4
 * @version 1.4
 *
 */
public class CtrConsole extends CtrTool implements Runnable{

	private static final Logger logger = Logger.getRootLogger();
	
	// �ʱ�ȭ ����
	private boolean init = false;
	
	private Scanner scanner;
	
	// ���� ó���̱� ������ ���������� ��
	private int consoleCmd;
	private OSGiCommand command;
	private Object userCmd;
	
	private static final String HELP = 
			"\n***************************************************\n" +
			"    Bomz Framework 1.4\n\n" +
			"    ���ɾ� �ȳ�\n" +
			"    - help : ���ɾ� �ȳ�\n" +
			"    - ss : ��ϵ� ������ ���¿� �̸� ����\n" +
			"    - stop [name] : ���� ����\n" +
			"    - start [name] : ���� ����\n" +
			"    - update [name] : ���� ������Ʈ\n" +
			"    - uninstall [name] : ���� ����\n" +
			"    - install : ���� ���\n" +
			"        ��) install c:/sample/sample.jar\n" +
			"    - exit : ���ǳ� �����ӿ�ũ ����\n" +
			"***************************************************\n";
	
	
	public CtrConsole(){}
	
	/**		�ܼ� ������ �ʱ�ȭ		*/
	public synchronized void init(){
		
		// �ʱ�ȭ�� �̹� �̷�����ٸ� ��û ��ȯ
		if( this.init )		return;
		
		this.init = true;		// �ʱ�ȭ �������� ���� ����
		
		this.scanner = new Scanner(System.in);
		
		Thread thread = new Thread(this);
		thread.start();
	}
	
	public void run(){
		
		// ���� �� ���ɾ� �ȳ� ��� �� ������
		System.out.println(HELP);
		
		while( true ){
			this.readCtr();
			this.writeCtr();
		}
		
	}
	
	private void writeCtr(){
		
		switch( this.consoleCmd ){
		case ConsoleCMD.CONSOLE_CMD_SS :		
			this.writeShortStatus( super.getServiceList() );	break;
		case ConsoleCMD.CONSOLE_CMD_HELP :
			System.out.println(HELP);			break;
		case ConsoleCMD.CONSOLE_CMD_EXIT:
			System.out.println("\nSupinan Framework Good bye\n");
			LoggerManager.LogFlush();
			System.exit(0);
			break;
		default:
			if( this.command != null )		
				this.printCommandResult( super.updateServiceState(this.userCmd, this.command) );
			break;
		}
	
	}
	
	/*		�����ӿ�ũ�� ó�� ����� �ֿܼ� ����Ѵ�		*/
	private void printCommandResult(OSGiResponse response){
		if( response.isSuccess() )		return;
		
		System.out.print("supinan error> ");
		System.out.println(response.getErrMsg());
		
		System.out.println();
	}
	
	private void writeShortStatus(Iterator<ServiceObject> list){
		try{
			System.out.println();
			
			if( !list.hasNext() )		System.out.println("��ϵ� ���񽺰� �����ϴ�");
			else							System.out.println("Type\t\tState\t\tService");
			
			ServiceObject obj;
			while( list.hasNext() ){
				System.out.println();
				obj = list.next();
				System.out.print( obj.getFileInfo() instanceof String ? "CLS" : "JAR");
				System.out.print("\t\t");
				System.out.print( this.replaceState(obj.getServiceState()) );
				System.out.print("\t");
				System.out.println( obj.getClassName() );
			}
			
			System.out.println();
			System.out.println();
			
		}catch(Exception e){
			logger.log(Level.WARN, e);
		}
	}
	
	/*
	 * �ܼ�â�� ���� ������ ���� ó���ȴ�
	 */
	private String replaceState(OSGiState state){
		switch( state ){
		case RESOLVED:		return "RESOLVED";
		case ACTIVE:			return "ACTIVE      ";
		case STOPPING:		return "STOPPING ";
		default:					return "                  ";
		}
	}
	
	private void readCtr(){
		this.clean();
		System.out.print("supinan>");
		try{
			this.readCtr( this.scanner.nextLine() );
		}catch(Throwable e){
			// �ý��� ����� �߻��� �� ����
		} 
	}
	
	/*		�Է� ���� ���ڿ� �м� �� ó��		*/
	private void readCtr(String cmd) throws Throwable{
		if( cmd == null )		return;
		
		String[] cmds = cmd.split(" ");
		
		if( !(cmds.length ==1 || cmds.length ==  2) )		return;
		
		cmds[0] = cmds[0].trim();
		
		for(int i=0; i < ConsoleCMD.CMD_LENGTH; i++){
			if( ConsoleCMD.USER_CMD[i].equals(cmds[0]) ){
								
				if ( i < 3 ){
					// ss , help , exit
					this.consoleCmd = i;
				}else{
					this.userCmd = cmds[1].trim();
					this.command = ConsoleCMD.FRM_CMD[i];
				}
								
				break;
			}
		}
		
	}
	
	private void clean(){
		this.consoleCmd = -1;
		this.command = null;
		this.userCmd = null;
	}
}
