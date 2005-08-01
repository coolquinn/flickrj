/*
 * Copyright (c) 2005 Aetrion LLC.
 */
package com.aetrion.flickr.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.aetrion.flickr.Authentication;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RESTResponse;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.people.User;

/**
 * REST Interface implementation for testing Flickr connectivity.
 *
 * @author Anthony Eden
 */
public class TestInterfaceREST extends TestInterface {

    private String apiKey;
    private REST restInterface;

    /**
     * Construct a TestInterface.
     *
     * @param apiKey The API key
     * @param restInterface The REST interface
     */
    public TestInterfaceREST(String apiKey, REST restInterface) {
        this.apiKey = apiKey;
        this.restInterface = restInterface;
    }

    /**
     * A testing method which echo's all paramaters back in the response.
     *
     * @param params The parameters
     * @return The Collection of echoed elements
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public Collection echo(Collection params) throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_ECHO));
        parameters.add(new Parameter("api_key", apiKey));
        parameters.addAll(params);

        RESTResponse response = (RESTResponse) restInterface.post("/services/rest/", parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        return response.getPayloadCollection();
    }

    /**
     * A testing method which checks if the caller is logged in then returns a User object.
     *
     * @return The User object
     * @throws IOException
     * @throws SAXException
     * @throws FlickrException
     */
    public User login() throws IOException, SAXException, FlickrException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_LOGIN));
        parameters.add(new Parameter("api_key", apiKey));
        
        RequestContext requestContext = RequestContext.getRequestContext();
        Authentication auth = requestContext.getAuthentication();
        if (auth != null) {
            parameters.addAll(auth.getAsParameters());
        }

        RESTResponse response = (RESTResponse) restInterface.post("/services/rest/", parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Element userElement = response.getPayload();
        User user = new User();
        user.setId(userElement.getAttribute("id"));
        
        Element usernameElement = (Element) userElement.getElementsByTagName("username").item(0);
        user.setUsername(((Text)usernameElement.getFirstChild()).getData());
        
        return user;
    }

}
