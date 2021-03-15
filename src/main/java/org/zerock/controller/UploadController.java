package org.zerock.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.zerock.domain.AttachFileDTO;

import lombok.extern.log4j.Log4j;
import net.coobird.thumbnailator.Thumbnailator;

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
	
	@PostMapping(value = "/uploadAjaxAction", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<List<AttachFileDTO>> uploadAjaxPost(MultipartFile[] uploadFile) {
		List<AttachFileDTO> list = new ArrayList<AttachFileDTO>();
		String uploadFolder = "C:\\upload";
		
		//make folder
		File uploadPath = new File(uploadFolder, getFolder());
		log.info("uploadPath : " + uploadPath);
		
		// File exists() 해당 파일이 존재하는지 확인해준다. 
		if (uploadPath.exists() == false) {
			uploadPath.mkdirs(); // 해당 경로로 폴더를 생성해준다. 
		}
		
		for (MultipartFile file : uploadFile) {
			
			AttachFileDTO attFileDto = new AttachFileDTO();
			
			log.info("------------------");
			log.info("Upload file Name : " + file.getOriginalFilename());
			log.info("Upload file size : " + file.getSize());
			
			String uploadFileName = file.getOriginalFilename();
			String extendFile = null;
			
			//IE has file path
			try { 
				if (uploadFileName.indexOf("\\") > 0) {
					uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\") + 1);
				}
				
				if (uploadFileName.lastIndexOf(".") > 0) {
					extendFile = uploadFileName.substring(uploadFileName.lastIndexOf("."));
					uploadFileName = uploadFileName.substring(0, uploadFileName.lastIndexOf("."));
				}
			} catch (Exception e) {
				log.info("IE의 경우 해당" + e);
			}
			
			attFileDto.setFileExtends(extendFile);
			attFileDto.setFileName(uploadFileName);
			UUID uuid = UUID.randomUUID();
			
			uploadFileName = uploadFileName + "_" + uuid + extendFile;
			
			log.info("only file name : " + uploadFileName);
			
			try {
				// File fileSave = new File(uploadFolder, file.getOriginalFilename());
				File fileSave = new File(uploadPath, uploadFileName);
				file.transferTo(fileSave);
				
				attFileDto.setUuid(uuid.toString());
				attFileDto.setUploadPath(uploadPath.toString());
				
				//check image type 
				if (checkImageType(fileSave)) {
					attFileDto.setImage(true);
					attFileDto.setImageUri("/" + getDate() + "/s_" + uploadFileName); // 업로드 url생성
					
					FileOutputStream thumbrail = new FileOutputStream(new File(uploadPath, "s_" + uploadFileName));
					
					Thumbnailator.createThumbnail(file.getInputStream(), thumbrail, 100, 100);
					
					thumbrail.close();
				}
				
				list.add(attFileDto);
			} catch (Exception e) {
				e.getStackTrace();
				log.error("filesave error : " + e);
			}			
		} // End for 
		log.info("upload ajax");
		
		return new ResponseEntity<List<AttachFileDTO>>(list, HttpStatus.OK);
	}
	
	
	/* 
	 * UploadController에서는 다음과 같은 단계를 이용해서 섬네 생성
	 * 업로드된 파일이 이미지 종류의 파일인지 확인
	 * 이미지 파일의 경우에는 섬네일 이미지 생성 및 저장 
	 * */
 
	@GetMapping("/display")
	@ResponseBody
	public ResponseEntity<byte[]> getFile(String fileName) {
		
		log.info("FileName : " + fileName);
		
		File file = new File("c:\\upload\\" + fileName);
		
		log.info("file : " + file);
		
		ResponseEntity<byte[]> result = null;
		
		try {
			// 피들러로 확인을 해보면 헤더에 image가 찍혀 있을 것이고 , filecopyutils에서 바이트 배열로 복사를 해서 전달하도록 합니다. 
			HttpHeaders header = new HttpHeaders();
			String contentType = Files.probeContentType(file.toPath());
			header.add("Content-Type", contentType);		
			
//			result = new ResponseEntity<byte[]>(FileCopyUtils.
//					copyToByteArray(file), HttpStatus.OK);
//			위를 보면 ResponseEntity 파라미터가 2개가 들어갔지만 헤더까지 같이 넣어줘야한다. 
			
			result = new ResponseEntity<byte[]>(FileCopyUtils.copyToByteArray(file), header, HttpStatus.OK);
			
			log.info("Content-Type : " + contentType);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	@GetMapping(value="/download" , produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseBody
	// @RequestHeader를 이용해서 요청에 헤더를 수집할 수 있다. 
	public ResponseEntity<Resource> downloadFile(@RequestHeader("User-agent") String userAgent, String fileName) {
		
		log.info("download file : " + fileName);
		
		// byte[] 를 이용해서 줄 수 있지만 , 
		Resource resource = new FileSystemResource("c:\\upload" + fileName);
		
		if (resource.exists() == false) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		log.info("resource : " + resource);
		
		String resourceName = resource.getFilename();
		
		
		HttpHeaders headers = new HttpHeaders();
		try {

			String downloadName = null;
			
			/* Trident가 IE 헤더에 포함되어 있는 사항인 것 같음, edge의 경우는 Edge라는 단어가 포함되어 있음. 
			 * 그리고 Trident, Edge는 브라우져 엔진 이름*/
			log.info("user Agent : " + userAgent);
			if (userAgent.contains("Trident")) {
				log.info("IE Browser");
				
				downloadName = URLEncoder.encode(resourceName, "UTF-8").replaceAll("\\+", " ");
				
			} else if (userAgent.contains("Edge")) {
				log.info("Edge Browger");
				
				downloadName = URLEncoder.encode(resourceName, "UTF-8");
				
				log.info("Edge name : " + downloadName);
				
			} else {
				log.info("chrome browser");
				
				downloadName = new String(resourceName.getBytes("UTF-8"), "ISO-8859-1");
			}
			downloadName = downloadName.substring(downloadName.indexOf("_") + 1);
			headers.add("Content-Disposition", "attachment; filename=" + downloadName);
		}catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return new ResponseEntity<Resource>(resource, headers, HttpStatus.OK);
	}
	
	@PostMapping("/deleteFile")
	@ResponseBody
	public ResponseEntity<String> deleteFile(String fileName, String type) {
		
		log.info("deleteFile  : " + fileName);
		
		File file;
		try {
			file = new File(URLDecoder.decode(fileName, "UTF-8"));
			
			file.delete();
			
			// iamge일 경우 섬네일도 삭제를 해줘야하니 아래의 코드를 넣어준 것이다. 
			if (type.equals("image")) {
				String largeFileName = file.getAbsolutePath().replace("s_", "");
				log.info("largeFilename : " + largeFileName);
				file = new File(largeFileName);
				
				file.delete();
				
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		
		return new ResponseEntity<String>("deleted", HttpStatus.OK);
	}
	
	private String getFolder() {
		// 파일 경로가 어떻게 시작이 되는건지 ? file.separator가 어떻게 사용이 되는건지 확인해주는게 좋을 것 같습니다. 
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date date = new Date();
		
		String str = sdf.format(date); // date 를 sdf에서 언급되어 있는 format으로 변환을 시킨다. 
		
		return str.replace("-", File.separator);
	}
	
	private String getDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		
		Date date = new Date();
		
		String str = sdf.format(date); // date 를 sdf에서 언급되어 있는 format으로 변환을 시킨다. 
		
		return str.replace("-", "/");
	}
	
	// ajax로 호출은 반드시 브라우저만을 통해 들어오는 것이 아니므로 서버에서 확실히 확인할 필요가 있다. 
	private boolean checkImageType(File file) {
		
		try {
			String contentType = Files.probeContentType(file.toPath());
			
			return contentType.startsWith("image");
		} catch (IOException e) {

			e.printStackTrace();
		}
		
		return false;
	}
}
