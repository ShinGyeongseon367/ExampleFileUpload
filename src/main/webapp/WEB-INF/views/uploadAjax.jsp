<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<style>
.uploadResult {width: 100%;background-color:gray;}
.uploadResult ul {display:flex; flex-flow:row; justify-content: center; align-items: center;}
.uploadResult ul li {list-style: none; padding: 10px;}
.uploadResult ul li img {width: 20px;}
</style>

<html>
<head>
<meta http-euqiv="Content-Type" content="text/html;" charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
	<h1>Uplaod with ajax</h1>
	
	<div class="uploadDiv">
	  <input type="file" name="uploadFile" multiple>
	</div>
	
	<button id="uploadBtn">Upload</button>
	
	<div class="uploadResult">
	  <ul>
	  </ul>
	</div>
	
	<!-- HTML CUT LINE -->
	
	<script src="https://code.jquery.com/jquery-3.5.1.js" integrity="sha256-QWo7LDvxbWT2tbbQ97B53yJnYU3WhH/C8ycbRAkjPDc=" crossorigin="anonymous"></script>
	
	<script type="text/javascript">
	$(document).ready(function(){
		
		var regex = new RegExp("(.*?)\.(exe|sh|zip|alz)$");
		var maxSize = 5242880; // 5MB
		
		function checkExtension(fileName, fileSize) {
			if (fileSize >= maxSize) {
				alert("파일 사이즈 초과");
				return false;
			}
			
			if (regex.test(fileName)) {
				alert("해당 종류의 파일은 업로드할 수 없습니다.")
				return false;
			}
			
			return true;
		}
		
		var cloneObj = $('.uploadDiv').clone();
		
		$(uploadBtn).on('click', function(e){
			var formData = new FormData();
			var inputFile = $("input[name='uploadFile']");
			var files = inputFile[0].files;
			
			console.log('files : ', files);
			
			// add files to formdata
			for (var i = 0; i < files.length; i++) {
				
				if (!checkExtension(files[i].name, files[i].size)) {
					return false;
				}
				formData.append("uploadFile", files[i]);
			}
			
			$.ajax({
				url			: '/uploadAjaxAction',
				processData : false,
				contentType : false,
				data		: formData,
				type		: 'POST',
				dataType	: "json",
				success : function(result) {
					// 요기까지는 그냥 서버에서만 저장을 한거지 브라우져에서는 뭔가 딱히 해준게 없다. 그래서 , 브라우져에게 데이터를 제공해 줄건데 ,
					// 업로드된 파일의 이름과 원본 파일의 이름 , 파일이 저장된 경로, 업로드된 파일이 이미지 인지 아닌지 
				
					console.log('ajax result : ',result);
					
					showUploadedFile(result);
					
					$(".uploadDiv").html(cloneObj.html());
					
				}
			});
			
		});// end click event 
		
		var uploadResult = $(".uploadResult ul");
		
		function showUploadedFile(uplaodResultArr) {
			
			var str = "";
			console.log('uplaodResultArr : ', uplaodResultArr);
			$(uplaodResultArr).each(function(i, obj){
				
				if (!obj.image) {
					str += "<li><img src='/resources/img/chumbu.png'>" + obj.fileName + "</img></li>";
				} else {
					// str += "<li>" + obj.fileName + "</li>";
					
					str += "<li><img src='/display?fileName=" + obj.imageUri +"'></li>"
				}
				
			});
			
			uploadResult.append(str);
		}
	}); // end ready
	
	
	</script>
</body>
</html>