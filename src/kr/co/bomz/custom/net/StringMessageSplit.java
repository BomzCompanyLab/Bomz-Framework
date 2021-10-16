package kr.co.bomz.custom.net;

import java.io.InputStream;
import java.io.Reader;
import java.net.ConnectException;

/**
 * 
 * Socket �� ���ؼ� �����͸� ������ ���ڿ����� �������� ������ ������ ���� ���<br>
 * 
 * �ش� ��ƿ�� �̿��ؼ� ���� �ش� �κи� ������ �� �ִ�.
 * 
 * @author ���ǳ�
 * @since 1.0
 * @version 1.2
 *
 */
public class StringMessageSplit {

	private Reader reader;
	private char[] readerDatas;
	
	private InputStream is;
	private byte[] isDatas;
	
	private String start;
	private String end;
	
	private boolean isReader;
	private boolean isStartAndEndMessageCut;
	
	private int readSize;
	private String returnMsg;
	private StringBuilder readMessageBuilder = new StringBuilder();
	private int readMsgStartPoint;
	private int readMsgLastPoint;
	
	private String encoding;
	
	/**
	 * 	isStartAndEndMessageCut	: �������� ������ ǥ���ϴ� �޼����� �����Ͽ� ���Ϲް��� �� ��� false
	 */
	public StringMessageSplit(Reader reader, int readArrayLength, String start, String end, boolean isStartAndEndMessageCut){
		this.reader = reader;	
		this.readerDatas = new char[ this.getLength(readArrayLength) ];
		this.isReader = true;
		this.start = start;
		this.end = end;
		this.isStartAndEndMessageCut = isStartAndEndMessageCut;
	}
	
	/**
	 * 	isStartAndEndMessageCut	: �������� ������ ǥ���ϴ� �޼����� �����Ͽ� ���Ϲް��� �� ��� false
	 */
	public StringMessageSplit(InputStream is, int readArrayLength, String start, String end, boolean isStartAndEndMessageCut){
		this.is = is;	
		this.isDatas = new byte[ this.getLength(readArrayLength) ];
		this.isReader = false;
		this.start = start;
		this.end = end;
		this.isStartAndEndMessageCut = isStartAndEndMessageCut;
	}
	
	private int getLength(int readArrayLength){
		return (readArrayLength > 0) ? readArrayLength : 50;
	}
	
	/**
	 * TCP ������� �����͸� �����Ͽ� ������ �������� ������ ã�Ƴ��� �����Ѵ�.
	 * 
	 * ���� ������ ���� �� ������ ����Ǿ��ٸ� ConnectException() �� �߻��Ѵ�.
	 * 
	 * ������ ���� �� setReader() �Ǵ� setInputStream() �� ���� ���� �����Ͽ���
	 * 
	 * NullPointerException() ���� ������ �߻��� �� �ִ�.
	 */
	public String readMessage() throws Exception{
		while(true){
			readMsgStartPoint = readMessageBuilder.indexOf(start);
			readMsgLastPoint = readMessageBuilder.indexOf(end, readMsgStartPoint);
			if( readMsgStartPoint != -1 && readMsgLastPoint != -1 )
				if( readMsgStartPoint < readMsgLastPoint )	break;
			
			readSize = isReader ? reader.read(readerDatas) : is.read(isDatas);
			
			if( readSize == -1 ){
				throw new ConnectException();
			}
			readMessageBuilder.append( 
					isReader? String.valueOf( readerDatas, 0, readSize) :
						(this.encoding == null ?
								new String( isDatas, 0, readSize) :
								new String( isDatas, 0, readSize, this.encoding)
						)
				);
			
		}
		
		if( isStartAndEndMessageCut )	readMsgStartPoint += start.length();
				
		if( !isStartAndEndMessageCut )	readMsgLastPoint += end.length();
		
		returnMsg = readMessageBuilder.substring(readMsgStartPoint, readMsgLastPoint);
		if( isStartAndEndMessageCut )	readMsgLastPoint += end.length();
		
		readMessageBuilder.delete(0, readMsgLastPoint);
		
		return returnMsg;
	}
	
	public void setEncoding(String charactorSet){
		this.encoding = charactorSet;
	}
	
	public String getEncoding(){
		return this.encoding;
	}
	
	public void setReader(Reader reader){
		this.reader = reader;
		this.isReader = true;
		readMessageBuilder.delete(0, readMessageBuilder.length() );
		
		if( readerDatas == null )
			readerDatas = new char[ this.getLength(0) ];
		
		isDatas = null;
	}
	
	public void setInputStream(InputStream is){
		this.is = is;
		this.isReader = false;
		readMessageBuilder.delete(0, readMessageBuilder.length() );
		
		if( isDatas == null )
			isDatas = new byte[ this.getLength(0) ];
		
		readerDatas = null;
	}

	public boolean isStartAndEndMessageCut() {
		return isStartAndEndMessageCut;
	}

	public void setStartAndEndMessageCut(boolean isStartAndEndMessageCut) {
		this.isStartAndEndMessageCut = isStartAndEndMessageCut;
	}
}
