package com.aetrion.flickr;

import java.util.Collection;
import java.util.Iterator;

import org.apache.axis.message.SOAPEnvelope;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aetrion.flickr.util.XMLUtilities;

/**
 * Flickr SOAP Response object.
 *
 * @author Matt Ray
 */
public class SOAPResponse implements Response {

    private String stat;
    private Collection payload;

    private String errorCode;
    private String errorMessage;
    
    private SOAPEnvelope envelope;

    public SOAPResponse(SOAPEnvelope envelope){
        this.envelope = envelope;
    }
    
    public void parse(Document document) {
        Element rspElement = document.getDocumentElement();
        rspElement.normalize();
        stat = rspElement.getAttribute("stat");
        if ("ok".equals(stat)) {
            // TODO: Verify that the payload is always a single XML node
            payload = XMLUtilities.getChildElements(rspElement);
        } else if ("fail".equals(stat)) {
            Element errElement = (Element) rspElement.getElementsByTagName("err").item(0);
            errorCode = errElement.getAttribute("code");
            errorMessage = errElement.getAttribute("msg");
        }
    }

    public String getStat() {
        return stat;
    }
    
    public Element getPayload() {
        Iterator iter = payload.iterator();
        if (iter.hasNext()) {
            return (Element) iter.next();
        }
        throw new RuntimeException("SOAP response payload has no elements");
    }

    public Collection getPayloadCollection() {
        return payload;
    }

    public boolean isError() {
        return errorCode != null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }



}
