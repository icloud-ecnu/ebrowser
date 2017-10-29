package api;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyscService {
	public static Random random = new Random();

	@Async
	public void doTaskOne() throws Exception {
		System.out.println("开始做任务一");
		long start = System.currentTimeMillis();
		Thread.sleep(random.nextInt(20000));
		long end = System.currentTimeMillis();
		System.out.println("完成任务一，耗时：" + (end - start) + "毫秒");
	}

	@Async
	public void doTaskTwo() throws Exception {
		System.out.println("开始做任务二");
		long start = System.currentTimeMillis();
		Thread.sleep(random.nextInt(20000));
		long end = System.currentTimeMillis();
		System.out.println("完成任务二，耗时：" + (end - start) + "毫秒");
	}

	// 使用单个用户的文件训练模型
	@Async
	public void doTrain(Boolean isShared, String deviceId) throws Exception {
		long start = System.currentTimeMillis();
		if (isShared) {
			String trainPath = "trains/train";
			String modelPath = "models/model";
			try {
				svm_train.main(new String[] { trainPath, modelPath });
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			String trainPath = "trains/" + deviceId;
			String modelPath = "models/" + deviceId;
			try {
				svm_train.main(new String[] { trainPath, modelPath });
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Trained Over. Use time: "+ (end-start));
	}

	// 接受用户反馈的速度和点击的次数，使用速度测出原先的fps
	@Async
	public void doReceive(String deviceId, String speed, String step) throws Exception {
		String modelPath = "models/model";
		double predictResult = 0;
		int feedbackFps = 0; // =predictResult+step
		File trainDataFile = new File("trains/" + deviceId);
		try {
			predictResult = svm_predict.main(new String[] { modelPath, speed });
		} catch (IOException e) {
			e.printStackTrace();
		}
		feedbackFps = (int) (Math.ceil(predictResult) + Integer.parseInt(step));
		FileWriter fw;
		BufferedWriter bufw;
		if (trainDataFile.exists()) {
			try {
				trainDataFile.createNewFile();
				fw = new FileWriter(trainDataFile, true);
				bufw = new BufferedWriter(fw);
				bufw.write(feedbackFps + " 1:" + speed);
				bufw.newLine();
				bufw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				trainDataFile.createNewFile();
				fw = new FileWriter(trainDataFile);
				FileReader fr = new FileReader("trains/train");
				char[] buf = new char[1024];
				int len = 0;
				while ((len = fr.read(buf)) != -1) {
					fw.write(buf, 0, len);
					fw.flush();
				}
				fw.write(feedbackFps + " 1:" + speed+"\n");
				fw.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println(Math.ceil(feedbackFps));
	}

	public void doPinch(String deviceId, String speed, String fps) {
		File trainDataFile = new File("pinchs/" + deviceId);
	
		FileWriter fw;
		BufferedWriter bufw;
		if (trainDataFile.exists()) {
			try {
				fw = new FileWriter(trainDataFile, true);
				bufw = new BufferedWriter(fw);
				bufw.write(speed + ":" + fps);
				bufw.newLine();
				bufw.close();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				trainDataFile.createNewFile();
				fw = new FileWriter(trainDataFile);
				fw.write(speed + ":" + fps+"\n");
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//System.out.println(Math.ceil(feedbackFps));		
	}
}
