import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;



public class APICheck {

	long timeInMillis;
	long hitConstraint;
	
	long currentHits;
	
	String url;
	String accessToken;
	
	public long getTimeInMillis() {
		return timeInMillis;
	}

	public void setTimeInMillis(long timeInMillis) {
		this.timeInMillis = timeInMillis;
	}

	public long getHitConstraint() {
		return hitConstraint;
	}

	public void setHitConstraint(long hitConstraint) {
		this.hitConstraint = hitConstraint;
	}

	public long getCurrentHits() {
		return currentHits;
	}

	public void setCurrentHits(long currentHits) {
		this.currentHits = currentHits;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public void startScheduleTask() {
		/**
		 * not using the taskHandle returned here, but it can be used to cancel
		 * the task, or check if it's done (for recurring tasks, that's not
		 * going to be very useful)
		 */
		@SuppressWarnings("unused")
		final ScheduledFuture<?> taskHandle = scheduler.scheduleAtFixedRate(new Runnable() {
			public void run() {
				try {
					reinitializeHits();
				} catch (Exception e) {
					
				}
			}

			
		}, 0, timeInMillis, TimeUnit.MILLISECONDS);
	}
	
	
	
	private void reinitializeHits() {
		// TODO Auto-generated method stub
		this.currentHits=0;
	}
	
	public APICheck(long tim,long hc) throws InterruptedException {

		this.timeInMillis=tim;
		this.hitConstraint=hc;
		this.startScheduleTask();
		Thread.sleep(1, 0);
		
	}
	
	
	
	
	
	
	
	
	

	
	
	
}
