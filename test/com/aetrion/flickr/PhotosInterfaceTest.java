/* Copyright 2004, Aetrion LLC.  All Rights Reserved. */

package com.aetrion.flickr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import com.aetrion.flickr.photos.Permissions;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.Photocount;
import com.aetrion.flickr.photos.PhotosInterface;
import com.aetrion.flickr.photos.SearchParameters;
import com.aetrion.flickr.tags.Tag;
import com.aetrion.flickr.util.IOUtilities;
import junit.framework.TestCase;
import org.xml.sax.SAXException;

/**
 * @author Anthony Eden
 */
public class PhotosInterfaceTest extends TestCase {

    Flickr flickr = null;
    Authentication auth = null;
    Properties properties = null;

    public void setUp() throws ParserConfigurationException, IOException {

        InputStream in = null;
        try {
            in = getClass().getResourceAsStream("/setup.properties");
            properties = new Properties();
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

    public void testGetInfo() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Photo photo = iface.getInfo(properties.getProperty("photoid"), null);
        assertNotNull(photo);
        System.out.println("photo id: " + photo.getId());
    }

    public void testAddAndRemoveTags() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        String[] tagsToAdd = {"test"};
        iface.addTags(auth, photoId, tagsToAdd);
        Photo photo = iface.getInfo(photoId, null);
        Collection tags = photo.getTags();
        assertNotNull(tags);
        assertEquals(1, tags.size());

        String tagId = null;
        Iterator tagsIter = tags.iterator();
        TAG_LOOP: while (tagsIter.hasNext()) {
            Tag tag = (Tag) tagsIter.next();
            if (tag.getValue().equals("test")) {
                tagId = tag.getId();
                break TAG_LOOP;
            }
        }

        iface.removeTag(auth, tagId);
    }

    public void testGetContactsPhotos() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection photos = iface.getContactsPhotos(auth, 0, false, false, false);
        assertNotNull(photos);
        assertTrue(photos.size() > 0);
    }

    public void testGetContactsPublicPhotos() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection photos = iface.getContactsPublicPhotos(properties.getProperty("nsid"), 0, false, false, false);
        assertNotNull(photos);
        assertTrue(photos.size() > 0);
    }

    public void testGetCounts() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Date[] dates = new Date[2];
        dates[0] = new Date(100000);
        dates[1] = new Date(); // now
        Collection counts = iface.getCounts(auth, dates, null);
        assertNotNull(counts);

        Iterator countsIter = counts.iterator();
        while (countsIter.hasNext()) {
            Photocount photocount = (Photocount) countsIter.next();
            System.out.println("count: " + photocount.getCount());
        }
    }

    public void testGetPerms() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Permissions perms = iface.getPerms(auth, properties.getProperty("photoid"));
        assertNotNull(perms);
    }

    public void testGetRecent() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection photos = iface.getRecent(0, 0);
        assertNotNull(photos);
    }

    public void testGetSizes() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection sizes = iface.getSizes(properties.getProperty("photoid"));
        assertNotNull(sizes);
    }

    public void testGetUntagged() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        Collection photos = iface.getUntagged(auth, 0, 0);
        assertNotNull(photos);
    }

    public void testSearch() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        SearchParameters searchParams = new SearchParameters();
        searchParams.setUserId(properties.getProperty("nsid"));
        Collection photos = iface.search(searchParams, 0, 0);
        assertNotNull(photos);
    }

    public void testSetDates() {

    }

    public void testSetMeta() {

    }

    public void testSetPerms() {

    }

    public void testSetTags() throws FlickrException, IOException, SAXException {
        PhotosInterface iface = flickr.getPhotosInterface();
        String photoId = properties.getProperty("photoid");
        String[] tagsToAdd = {"test"};
        iface.setTags(auth, photoId, tagsToAdd);

        Photo photo = iface.getInfo(photoId, null);
        Collection tags = photo.getTags();
        assertNotNull(tags);
        assertEquals(1, tags.size());

        String tagId = null;
        Iterator tagsIter = tags.iterator();
        TAG_LOOP: while (tagsIter.hasNext()) {
            Tag tag = (Tag) tagsIter.next();
            if (tag.getValue().equals("test")) {
                tagId = tag.getId();
                break TAG_LOOP;
            }
        }

        String[] tagsAfterRemove = {};
        iface.setTags(auth, photoId, tagsAfterRemove);

        photo = iface.getInfo(photoId, null);
        tags = photo.getTags();
        assertNotNull(tags);
        assertEquals(0, tags.size());
    }

}