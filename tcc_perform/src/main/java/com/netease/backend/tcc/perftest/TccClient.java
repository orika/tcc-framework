package com.netease.backend.tcc.perftest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netease.backend.tcc.perftest.worker.Metric;
import com.netease.backend.tcc.perftest.worker.TccWorker;

public class TccClient {
	
	public static int parallelism;
	private int paral;
	private TccWorker[] workers;
	private TestContainer container;
	private List<Metric> metricList = new ArrayList<Metric>();
	private int metricInterval = 10000;
	private int lastTime = 600000;
	
	private static Map<String, String> OPT = new HashMap<String, String>();
	
	public TccClient(TestContainer container) {
		if (parallelism <= 0)
			throw new RuntimeException("parallelism must > 0");
		this.paral = TccClient.parallelism;
		this.workers = new TccWorker[parallelism];
		this.container = container;
		String lastTime = OPT.get("-l");
		if (lastTime != null)
			setLastTime(Integer.valueOf(lastTime));
		String interval = OPT.get("-i");
		if (interval != null)
			setMetricInterval(Integer.valueOf(interval));
	}
	
	public void setLastTime(int lastTime) {
		this.lastTime = lastTime;
	}

	public void setMetricInterval(int metricInterval) {
		this.metricInterval = metricInterval;
	}

	public void start() {
		for (int i = 0; i < paral; i++) {
			workers[i] = new TccWorker(i, container);
			workers[i].start();
		}
		for (int i = lastTime / metricInterval; i > 0; i--) {
			Metric metric = new Metric(parallelism);
			try {
				Thread.sleep(metricInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (TccWorker worker: workers) {
				worker.collectMetric(metricInterval, metric);
			}
			metricList.add(metric);
			System.out.println(metricInterval + "ms, count:" + metric.getCount() + ", avg time:" + metric.getResTime());
		}
		for (int i = 0; i < paral; i++) {
			workers[i].stop();
		} 
		long count = 0;
		long avgTime = 0;
		for (Metric m : metricList) {
			count += m.getCount();
			avgTime += m.getResTime();
		}
		long tps = count * 1000 / lastTime;
		avgTime = avgTime / metricList.size();
		System.out.println(lastTime + "ms, tps:" + tps + ", avg time:" + avgTime);
		System.exit(0);
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < args.length; i += 2) {
			String opt = args[i];
			String param = args[i + 1];
			OPT.put(opt, param);
		}
		String parallelism = OPT.get("-p");
		if (parallelism != null)
			TccClient.parallelism = Integer.valueOf(parallelism);
		System.setProperty("dubbo.spring.config", "classpath*:/client.xml");
		com.alibaba.dubbo.container.Main.main(new String[0]);
	}
}
