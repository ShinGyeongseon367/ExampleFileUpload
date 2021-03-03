package org.zerock.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.log4j.Log4j;

@Controller
@Log4j
public class UploadController {

	String uploadFolder = "C:\\upload";
	
	@GetMapping("/uploadForm")
	public void uploadFrm() {
		log.info("upload form");
	}
	
	@PostMapping("/uploadFormAction")
	public void uplaodFormPost(MultipartFile[] uploadFile, Model model) {
		
		for (MultipartFile multipartFile : uploadFile) {
			
			log.info("----------------------------------");
			log.info("Upload file Name : " + multipartFile.getOriginalFilename());
			log.info("Upload File Size : " + multipartFile.getSize());
			
			File saveFile = new File(uploadFolder, multipartFile.getOriginalFilename());
			
			try {
				multipartFile.transferTo(saveFile);
			} catch(Exception e) {
				log.error(e);
			}
		}
	}
	
	@GetMapping("/uploadAjax")
	public void uploadAjax() {
		
		log.info("update ajax post .... ");
		
	}
	
	@PostMapping("/uploadAjaxAction")
	public void uploadAjaxPost(MultipartFile[] uploadFile) {
		String uploadFolder = "C:\\upload";
		
		//make folder
		File uploadPath = new File(uploadFolder, getFolder());
		log.info("uploadPath : " + uploadPath);
		
		// File exists() 해당 파일이 존재하는지 확인해준다. 
		if (uploadPath.exists() == false) {
			uploadPath.mkdirs(); // 해당 경로로 폴더를 생성해준다. 
		}
		
		for (MultipartFile file : uploadFile) {
			log.info("------------------");
			log.info("Upload file Name : " + file.getOriginalFilename());
			log.info("Upload file size : " + file.getSize());
			
			String uploadFileName = file.getOriginalFilename();
			
			//IE has file path
			try { 
				uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\") + 1);
			} catch (Exception e) {
				log.info("IE의 경우 해당" + e);
			}
			log.info("only file name : " + uploadFileName);
			
			// File fileSave = new File(uploadFolder, file.getOriginalFilename());
			File fileSave = new File(uploadPath, uploadFileName);
			
			try {
				file.transferTo(fileSave);
			} catch (Exception e ) {
				log.error("filesave error : " + e);
			}
			
		}
		
		log.info("upload ajax");
	}
 
	
	private String getFolder() {
		// 파일 경로가 어떻게 시작이 되는건지 ? file.separator가 어떻게 사용이 되는건지 확인해주는게 좋을 것 같습니다. 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date date = new Date();
		
		String str = sdf.format(date); // date 를 sdf에서 언급되어 있는 format으로 변환을 시킨다. 
		
		return str.replace("-", File.separator);
	}
}
