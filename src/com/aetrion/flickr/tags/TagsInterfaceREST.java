/*
 * Copyright (c) 2005 Aetrion LLC.
 */
package com.aetrion.flickr.tags;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.aetrion.flickr.Authentication;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RESTResponse;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.util.XMLUtilities;

/**
 * REST Interface for working with Flickr tags.
 *
 * @author Anthony Eden
 */
public class TagsInterfaceREST extends TagsInterface{
    
    private String apiKey;
    private REST restInterface;
    
    /**
     * Construct a TagsInterface.
     *
     * @param apiKey The API key
     * @param restInterface The REST interface
     */
    public TagsInterfaceREST(String apiKey, REST restInterface) {
        this.apiKey = apiKey;
        this.restInterface = restInterface;
    }
    
    /**
     * Get a list of tags for the specified photo.
     *
     * @param photoId The photo ID
     * @return The collection of Tag objects
     */
    public Photo getListPhoto(String photoId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_LIST_PHOTO));
        parameters.add(new Parameter("api_key", apiKey));
        
        RequestContext requestContext = RequestContext.getRequestContext();
        Authentication auth = requestContext.getAuthentication();
        if (auth != null) {
            parameters.addAll(auth.getAsParameters());
        }
        
        parameters.add(new Parameter("photo_id", photoId));
        
        RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        } 
        
        Element photoElement = response.getPayload();
        Photo photo = new Photo();
        photo.setId(photoElement.getAttribute("id"));
        
        List tags = new ArrayList();
        Element tagsElement = (Element) photoElement.getElementsByTagName("tags").item(0);
        NodeList tagElements = tagsElement.getElementsByTagName("tag");
        for (int i = 0; i < tagElements.getLength(); i++) {
            Element tagElement = (Element) tagElements.item(i);
            Tag tag = new Tag();
            tag.setAuthor(tagElement.getAttribute("author"));
            tag.setAuthorName(tagElement.getAttribute("authorname"));
            tag.setValue(((Text) tagElement.getFirstChild()).getData());
            tags.add(tag);
        }
        photo.setTags(tags);
        return photo;
    }
    
    /**
     * Get a collection of tags used by the specified user.
     *
     * @param userId The User ID
     * @return The User object
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Collection getListUser(String userId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_LIST_USER));
        parameters.add(new Parameter("api_key", apiKey));
        
        RequestContext requestContext = RequestContext.getRequestContext();
        Authentication auth = requestContext.getAuthentication();
        if (auth != null) {
            parameters.addAll(auth.getAsParameters());
        }
        
        parameters.add(new Parameter("user_id", userId));
        
        RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        } 
        
        Element whoElement = response.getPayload();
        
        List tags = new ArrayList();
        Element tagsElement = (Element) whoElement.getElementsByTagName("tags").item(0);
        NodeList tagElements = tagsElement.getElementsByTagName("tag");
        for (int i = 0; i < tagElements.getLength(); i++) {
            Element tagElement = (Element) tagElements.item(i);
            Tag tag = new Tag();
            tag.setValue(((Text) tagElement.getFirstChild()).getData());
            tags.add(tag);
        }
        return tags;
    }
    
    /**
     * Get a list of the user's popular tags.
     *
     * @param userId The user ID
     * @return The collection of Tag objects
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Collection getListUserPopular(String userId) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_LIST_USER_POPULAR));
        parameters.add(new Parameter("api_key", apiKey));
        
        RequestContext requestContext = RequestContext.getRequestContext();
        Authentication auth = requestContext.getAuthentication();
        if (auth != null) {
            parameters.addAll(auth.getAsParameters());
        }
        
        parameters.add(new Parameter("user_id", userId));
        
        RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        } 
        
        Element whoElement = response.getPayload();
        
        List tags = new ArrayList();
        Element tagsElement = (Element) whoElement.getElementsByTagName("tags").item(0);
        NodeList tagElements = tagsElement.getElementsByTagName("tag");
        for (int i = 0; i < tagElements.getLength(); i++) {
            Element tagElement = (Element) tagElements.item(i);
            Tag tag = new Tag();
            tag.setCount(tagElement.getAttribute("count"));
            tag.setValue(((Text) tagElement.getFirstChild()).getData());
            tags.add(tag);
        }
        return tags;
    }
    
    /**
     * Get the related tags.
     *
     * @param tag The source tag
     * @return A RelatedTagsList object
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public RelatedTagsList getRelated(String tag) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_GET_RELATED));
        parameters.add(new Parameter("api_key", apiKey));
        
        RequestContext requestContext = RequestContext.getRequestContext();
        Authentication auth = requestContext.getAuthentication();
        if (auth != null) {
            parameters.addAll(auth.getAsParameters());
        }
        
        parameters.add(new Parameter("tag", tag));
        
        RESTResponse response = (RESTResponse) restInterface.get("/services/rest/", parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        } 
        
        Element tagsElement = response.getPayload();
        
        RelatedTagsList tags = new RelatedTagsList();
        tags.setSource(tagsElement.getAttribute("source"));
        NodeList tagElements = tagsElement.getElementsByTagName("tag");
        for (int i = 0; i < tagElements.getLength(); i++) {
            Element tagElement = (Element) tagElements.item(i);
            Tag t = new Tag();
            t.setValue(XMLUtilities.getValue(tagElement));
            tags.add(t);
        }
        return tags;
    }
    
}