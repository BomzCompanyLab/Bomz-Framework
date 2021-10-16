package kr.co.bomz.timer;

import kr.co.bomz.custom.util.SupinanThread;

/**
 * 
 * @author ���ǳ�
 * @since 1.2
 * @version 1.2
 *
 */
public class TimerRunning extends SupinanThread{

	private SupinanTimer timerService;
	private Timer timer;
	
	@Override
	protected void execute() throws Exception {
		this.timer.execute();
	}

	@Override
	protected void initParameter(Object ... parameters) throws Exception{
		this.timer = (Timer)parameters[0];
	}
	
	@Override
	protected void startInit(Object ... parameters) throws Exception{
		this.timerService = (SupinanTimer)parameters[0];
	}
	
	@Override
	protected void close(){
		if( this.timer.isEndTimer() ){
			this.timerService.removeTimer(this.timer);	
		}
		
		this.timer = null;
	}
}
