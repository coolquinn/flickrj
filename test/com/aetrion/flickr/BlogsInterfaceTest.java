/* Copyright 2004, Aetrion LLC.  All Rights Reserved. */

package com.aetrion.flickr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import com.aetrion.flickr.blogs.BlogsInterface;
import com.aetrion.flickr.util.IOUtilities;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

/**
 * @author Anthony Eden
 */
public class BlogsInterfaceTest extends TestCase {

    Flickr flickr = null;
    Authentication auth = null;

    public void setUp() throws ParserConfigurationException, IOException {

        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/setup.properties");
            Properties properties = new Properties();
            properties.load(in);

            REST rest = new REST();
            rest.setHost(properties.getProperty("host"));

            flickr = new Flickr(properties.getProperty("apiKey"), rest);

            auth = new Authentication();
            auth.setEmail(properties.getProperty("email"));
            auth.setPassword(properties.getProperty("password"));
        } finally {
            IOUtilities.close(in);
        }
    }

    public void testGetList() throws FlickrException, IOException, SAXException {
        BlogsInterface blogsInterface = flickr.getBlogsInterface();
        Collection blogs = blogsInterface.getList(auth);
        assertNotNull(blogs);
        assertEquals(1, blogs.size());
    }

    public void testPostImage() {

    }

}