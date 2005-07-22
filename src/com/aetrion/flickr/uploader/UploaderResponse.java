/*
 * Copyright (c) 2005 Aetrion LLC.
 */

package com.aetrion.flickr.uploader;

import com.aetrion.flickr.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * @author Anthony Eden
 */
public class UploaderResponse implements Response {

    private String status;
    private String photoId;
    private String errorCode;
    private String errorMessage;

    public void parse(Document document) {
        Element uploaderElement = document.getDocumentElement();
        Element statusElement = (Element) uploaderElement.getElementsByTagName("status").item(0);

        status = ((Text)statusElement.getFirstChild()).getData();
        if ("ok".equals(status)) {
            Element photoIdElement = (Element) uploaderElement.getElementsByTagName("photoid").item(0);
            photoId = ((Text)photoIdElement.getFirstChild()).getData();
        } else {
            Element errorCodeElement = (Element) uploaderElement.getElementsByTagName("error").item(0);
            errorCode = ((Text)errorCodeElement.getFirstChild()).getData();
            Element errorMessageElement = (Element) uploaderElement.getElementsByTagName("verbose").item(0);
            errorMessage = ((Text)errorMessageElement.getFirstChild()).getData();
        }
    }

    public String getStatus() {
        return status;
    }

    public String getPhotoId() {
        return photoId;
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
