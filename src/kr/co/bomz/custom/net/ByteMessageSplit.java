package kr.co.bomz.custom.net;

import java.io.InputStream;
import java.net.ConnectException;
import java.util.ArrayList;


/**
 * 
 * ����Ʈ ������ �޼����� �����ϴ� ��� �ش� ��ƿ�� �̿��ؼ� ���� �ش� �κи� ������ �� �ִ�.
 * 
 * 		��) 0x12 0x33 0x02 0x00 0x12 0x43 0x03
 * 
 * ���� ������ 0x02 ���� 0x03 ������ ������ �ް� ���� ���
 * 
 * @author ���ǳ�
 * @since 1.2
 * @version 1.2
 *
 */
public class ByteMessageSplit {

	private InputStream inputStream;
	private final byte[] start;
	private final byte[] end;
	private boolean isStartAndEndMessageCut;
	
	private int readMsgStartPoint;
	private int readMsgLastPoint;
	
	private int readSize;
	
	private byte[] isDatas;
	
	private final ArrayList<Byte> readDataList = new ArrayList<Byte>();
	
	private boolean indexCheck;
	
	/**
	 * 	isStartAndEndMessageCut	: �������� ������ ǥ���ϴ� �޼����� �����Ͽ� ���Ϲް��� �� ��� false
	 */
	public ByteMessageSplit(InputStream inputStream, int readArrayLength, byte[] start, byte[] end, boolean isStartAndEndMessageCut){
		this.inputStream = inputStream;
		this.start = start;
		this.end = end;
		this.isStartAndEndMessageCut = isStartAndEndMessageCut;
		this.isDatas = new byte[ this.getLength(readArrayLength) ];
	}
	
	private int getLength(int readArrayLength){
		return (readArrayLength > 0) ? readArrayLength : 50;
	}
	
	public void setInputStream(InputStream is){
		this.readDataList.clear();
		
		if( this.isDatas == null )			this.isDatas = new byte[ this.getLength(0) ];
		
		this.inputStream = is;
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
	public byte[] readMessage() throws Exception{
		while(true){
			this.readMsgStartPoint = this.readDataIndexOf(this.start);
			this.readMsgLastPoint = this.readDataIndexOf(this.end, this.readMsgStartPoint);
			if( this.readMsgStartPoint != -1 && this.readMsgLastPoint != -1 )
				if( this.readMsgStartPoint < this.readMsgLastPoint )	break;
			
			this.readSize = this.inputStream.read(this.isDatas);
			
			if( this.readSize == -1 )			throw new ConnectException();
			
			for(int i=0; i < this.readSize; i++)		this.readDataList.add(this.isDatas[i]);			
		}
		
		if( isStartAndEndMessageCut )		readMsgStartPoint += start.length;
		if( !isStartAndEndMessageCut )	readMsgLastPoint += end.length;
		
		byte[] resultMsg = new byte[readMsgLastPoint - readMsgStartPoint];
		
		// �պκ��� ������ �ʴ� ������ ����
		for(int i=0; i < readMsgStartPoint; i++)		this.readDataList.remove(0);
		
		// ���� ������ �̵�
		for(int i=readMsgStartPoint, index=0; i < readMsgLastPoint; i++, index++)
			resultMsg[index] = this.readDataList.remove(0).byteValue();
		
		if( isStartAndEndMessageCut ){
			for(int i=0; i < end.length; i++)		this.readDataList.remove(0);
		}
		
		return resultMsg;
	}
	
	private int readDataIndexOf(byte[] msg){
		return this.readDataIndexOf(msg, 0);
	}
	
	private int readDataIndexOf(byte msg[], int startWith){
		
		if( startWith == -1)		return -1;
		
		int size = this.readDataList.size() - msg.length;
		
		int j;
		for(int i=startWith; i <= size; i++){
			
			if( this.readDataList.get(i) == msg[0] ){
				this.indexCheck = true;
				for(j=1; j < msg.length; j++){
					if( this.readDataList.get(i+j) != msg[j] ){
						this.indexCheck = false;
						break;
					}
				}
				
				if( this.indexCheck )		return i;
			}
		}
			
		return -1;
	}
	
}
