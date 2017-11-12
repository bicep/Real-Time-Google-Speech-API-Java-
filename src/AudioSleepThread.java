

public class AudioSleepThread extends Thread{
	
	private int recordTime; 
	private RealTimeAudioToText qst;
	
	public AudioSleepThread(RealTimeAudioToText qst, int recordTime) {
		this.recordTime = recordTime;
		this.qst = qst;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(this.recordTime);
			this.qst.setStopCapture(true);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
