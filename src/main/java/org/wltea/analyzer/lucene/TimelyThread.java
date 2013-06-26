package org.wltea.analyzer.lucene;

import java.io.IOException;
import java.util.Vector;

//TODO optimize
public class TimelyThread implements Runnable{
	
	public static interface UpdateJob{
		public void update() throws IOException ;
	}
	
	
	final static int INTEVER = 2 * 60 * 1000;
	
	private static TimelyThread singleton;
	Vector<UpdateJob> filterFactorys;
	Thread worker;

	private TimelyThread(){
		filterFactorys = new Vector<UpdateJob>();

		worker = new Thread(this);
		worker.setDaemon(true);
		worker.start();
	}
	
	public static TimelyThread getInstance(){
		if(singleton == null){
			synchronized(TimelyThread.class){
				if(singleton == null){
					singleton = new TimelyThread();
					return singleton;
				}
			}
		}
		return singleton;
	}
	
	/*保留各个FilterFactory实例对象的引用，用于后期更新操作*/
	public void register(TimelyThread.UpdateJob filterFactory ){
		filterFactorys.add(filterFactory);
	}
	
	public void run() {
		while(true){
			
			try {
				Thread.sleep(INTEVER);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(!filterFactorys.isEmpty())
			{
				System.out.println("------------update--------");
				for(UpdateJob factory: filterFactorys){
					try {
						factory.update();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
}

