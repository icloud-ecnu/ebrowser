package api;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {
	@Autowired
	private AsyscService task;
//	@RequestMapping("/async")
//	public Message async(String name, Model model) {
//
//		try {
//			task.doTaskOne();
//			task.doTaskTwo();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return new Message("200", "done");
//	}

	@RequestMapping("/download")
	public void download(@RequestParam(value = "fileName", required = false, defaultValue = "model") String fileName,
			HttpServletResponse res) {
		System.out.println("GreetingController:download, fileName: " + fileName);
		
		String modelPath = "models/" + fileName;
		String attachment = fileName;
		res.setHeader("content-type", "application/octet-stream");
		res.setContentType("application/octet-stream");
		res.setHeader("Content-Disposition", "attachment;filename=" + attachment);
		byte[] buffer = new byte[1024];
		BufferedInputStream bis = null;
		FileInputStream fis = null;
		OutputStream os = null;
		try {
			os = res.getOutputStream();
			File file = new File(modelPath);
			fis = new FileInputStream(file);
			bis = new BufferedInputStream(fis);
			int len = 0;
			while ((len = bis.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// return "greeting";
	}

	@RequestMapping("/train")
	public Message train(@RequestParam(value = "deviceId", required = true) String deviceId, Model model) {
		System.out.println("GreetingController:train, deviceId: " + deviceId);
		
		try {
			if (deviceId.trim().isEmpty()) {
				task.doTrain(true, deviceId);
			} else {
				task.doTrain(false, deviceId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Message("200", "success");
	}

	@RequestMapping("/save")
	public Message save(@RequestParam(value = "deviceId", required = true) String deviceId, String speed, String step,
			Model model) {// 参数speed由客户端除以50
		System.out.println("GreetingController:save, deviceId: " + deviceId + ", speed: " + speed + ", step: " + step);
		try {
			task.doReceive(deviceId, speed, step);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Message("200", "success");
	}
	
	@RequestMapping("/pinch")
	public Message pinch(@RequestParam(value = "deviceId", required = true) String deviceId, String speed, String fps,
			Model model) {// 参数speed由客户端除以50
		System.out.println("GreetingController:pinch, deviceId: " + deviceId + ", speed: " + speed + ", fps: " + fps);
		try {
			task.doPinch(deviceId, speed, fps);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Message("200", "success");
	}
}
