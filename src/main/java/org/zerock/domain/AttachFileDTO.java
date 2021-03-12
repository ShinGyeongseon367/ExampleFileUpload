package org.zerock.domain;

import lombok.Data;

@Data
public class AttachFileDTO {

	private String fileName;
	private String uploadPath;
	private String uuid;
	private String imageUri;
	private String fileExtends;
	private boolean image;
}
