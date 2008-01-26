package com.aetrion.flickr.places;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.Parameter;
import com.aetrion.flickr.Response;
import com.aetrion.flickr.Transport;
import com.aetrion.flickr.auth.AuthUtilities;
import com.aetrion.flickr.photos.PhotoUtils;
import com.aetrion.flickr.util.XMLUtilities;

/**
 * Lookup Flickr Places.<p>
 *
 * Announcement on places from yahoo:<p>
<PRE>
From: kellan <kellan@yahoo-inc.com>
Date: Fri, 11 Jan 2008 15:57:59 -0800
Subject: [yws-flickr] Flickr and "Place IDs"

At Flickr we've got a really big database that lists a significant 
percentage of the places that exist in the world, and a few that don't. 
When you geotag a photo we try to identify the "place" (neighborhood, 
village, city, county, state, or country) where the photo was taken. And 
we assign that photo a "place ID".

A place ID is a globally unique identifier for a place on Earth.  A city 
has a place ID, so do counties, states, and countries.  Even some 
neighborhoods and landmarks have them, though Flickr isn't currently 
tracking those. And we're starting to expose these place IDs around Flickr.

### Place IDs and flickr.photos.search()

The Flickr API method flickr.photos.search() now accepts place_id as an 
argument.  Along with all of the other parameters you can
search on you can now scope your search to a given place.   Historically 
you've been able to pass bounding boxes to the API, but calculating the 
right bounding box for a city is tricky, and you can get noise and bad 
results around the edge.  Now you can pass a single non-ambiguous string 
and get photos geotagged in San Francisco, CA, or Ohio, or Beijing. 
(kH8dLOubBZRvX_YZ, LtkqzVqbApjAbJxv, and wpK7URqbAJnWB90W respectively)

The documentation has been updated at:
http://www.flickr.com/services/api/flickr.photos.search.html

### Sources of Place IDs

Place IDs are now returned from a number of source:
   * flickr.photos.getInfo will return place IDs for geotagged photos
   * available as a microformat on the appropriate Places page
   * flickr.places.resolvePlaceURL, and flickr.places.resolvePlaceId are
available for round tripping Flickr Places URLs.

http://www.flickr.com/services/api/flickr.photos.getInfo.html
http://www.flickr.com/services/api/flickr.places.resolvePlaceURL.html
http://www.flickr.com/services/api/flickr.places.resolvePlaceId.html

### More Place IDs

Right now you can also place IDs in the places URL, and pass them to the 
map like so:

   * http://flickr.com/places/wpK7URqbAJnWB90W
   * http://flickr.com/map?place_id=kH8dLOubBZRvX_YZ

### Place IDs elsewhere

The especially eagle-eyed among you might recognize Place IDs.  Upcoming 
has been quietly using them for months to uniquely identify their metros.

See events from San Francisco at:
http://upcoming.yahoo.com/place/kH8dLOubBZRvX_YZ

See photos from San Francisco at: http://flickr.com/places/kH8dLOubBZRvX_YZ

Additionally Yahoo's skunkworks project FireEagle will also support 
place IDs.

And yes, there is more work to do, but we're exciting about this as a start.

Thanks,
-kellan
</PRE>

 * @author mago
 * @version $Id: PlacesInterface.java,v 1.2 2008/01/19 22:53:56 x-mago Exp $
 */
public class PlacesInterface {
    public static final String METHOD_FIND = "flickr.places.find";
    public static final String METHOD_RESOLVE_PLACE_ID = "flickr.places.resolvePlaceId";
    public static final String METHOD_RESOLVE_PLACE_URL = "flickr.places.resolvePlaceURL";

    private String apiKey;
    private String sharedSecret;
    private Transport transportAPI;

    public PlacesInterface(String apiKey, String sharedSecret, Transport transportAPI) {
        this.apiKey = apiKey;
        this.sharedSecret = sharedSecret;
        this.transportAPI = transportAPI;
    }

    public PlacesList find(String query)
      throws FlickrException, IOException, SAXException {
        List parameters = new ArrayList();
        PlacesList placesList = new PlacesList();
        parameters.add(new Parameter("method", METHOD_FIND));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("query", query));

        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Element placesElement = response.getPayload();
        NodeList placesNodes = placesElement.getElementsByTagName("place");
        placesList.setPage("1");
        placesList.setPages("1");
        placesList.setPerPage("" + placesNodes.getLength());
        placesList.setTotal("" + placesNodes.getLength());
        for (int i = 0; i < placesNodes.getLength(); i++) {
            Element placeElement = (Element) placesNodes.item(i);
            placesList.add(parsePlace(placeElement));
        }
        return placesList;
    }

    public Location resolvePlaceId(String placeId)
      throws FlickrException, IOException, SAXException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_RESOLVE_PLACE_ID));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("place_id", placeId));

        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Element locationElement = response.getPayload();
        return parseLocation(locationElement);
    }

    public Location resolvePlaceURL(String flickrPlacesUrl)
      throws FlickrException, IOException, SAXException {
        List parameters = new ArrayList();
        parameters.add(new Parameter("method", METHOD_RESOLVE_PLACE_URL));
        parameters.add(new Parameter("api_key", apiKey));

        parameters.add(new Parameter("url", flickrPlacesUrl));

        parameters.add(
            new Parameter(
                "api_sig",
                AuthUtilities.getSignature(sharedSecret, parameters)
            )
        );

        Response response = transportAPI.get(transportAPI.getPath(), parameters);
        if (response.isError()) {
            throw new FlickrException(response.getErrorCode(), response.getErrorMessage());
        }
        Element locationElement = response.getPayload();
        return parseLocation(locationElement);
    }

    private Location parseLocation(Element locationElement) {
        Location location = new Location();
        Element localityElement = (Element) locationElement.getElementsByTagName("locality").item(0);
        Element countyElement = (Element) locationElement.getElementsByTagName("county").item(0);
        Element regionElement = (Element) locationElement.getElementsByTagName("region").item(0);
        Element countryElement = (Element) locationElement.getElementsByTagName("country").item(0);

        location.setPlaceId(locationElement.getAttribute("place_id"));
        location.setName(locationElement.getAttribute("name"));
        location.setPlaceUrl(locationElement.getAttribute("place_url"));

        location.setLocality(
            new Place(
                localityElement.getAttribute("place_id"),
                XMLUtilities.getChildValue(locationElement, "locality"),
                Place.TYPE_LOCALITY
            )
         );
        location.setCounty(
            new Place(
                countyElement.getAttribute("place_id"),
                XMLUtilities.getChildValue(locationElement, "county"),
                Place.TYPE_COUNTY
            )
        );
        location.setRegion(
            new Place(
                regionElement.getAttribute("place_id"),
                XMLUtilities.getChildValue(locationElement, "region"),
                Place.TYPE_REGION
            )
        );
        location.setCountry(
            new Place(
                countryElement.getAttribute("place_id"),
                XMLUtilities.getChildValue(locationElement, "country"),
                Place.TYPE_COUNTRY
            )
        );
        return location;
    }

    private Place parsePlace(Element placeElement) {
        Place place = new Place();
        place.setPlaceId(placeElement.getAttribute("place_id"));
        place.setPlaceUrl(placeElement.getAttribute("place_url"));
        String typeString = placeElement.getAttribute("place_type");
        if (typeString.equals("locality")) {
            place.setPlaceType(Place.TYPE_LOCALITY);
        } else if (typeString.equals("county")) {
            place.setPlaceType(Place.TYPE_COUNTY);
        } else if (typeString.equals("region")) {
            place.setPlaceType(Place.TYPE_REGION);
        } else if (typeString.equals("country")) {
            place.setPlaceType(Place.TYPE_COUNTRY);
        }
        return place;
    }
}