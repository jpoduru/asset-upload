package com.asset.upload;

public class AssetProperties {

	private String fileName;
	private String uploadURIs;
	private String mimeType;
	private String uploadToken;
	private String filePath;
	private String fileSize;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUploadURIs() {
		return uploadURIs;
	}

	public void setUploadURIs(String uploadURIs) {
		this.uploadURIs = uploadURIs;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getUploadToken() {
		return uploadToken;
	}

	public void setUploadToken(String uploadToken) {
		this.uploadToken = uploadToken;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}
}
