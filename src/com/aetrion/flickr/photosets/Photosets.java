package com.aetrion.flickr.photosets;

import java.util.Collection;

/**
 * Photoset collection.  This class is required instead of a basic Collection object because of the cancreate flag.
 *
 * @author Anthony Eden
 */
public class Photosets {

    private boolean canCreate;
    private Collection photosets;

    public Photosets() {

    }

    public boolean isCanCreate() {
        return canCreate;
    }

    public void setCanCreate(boolean canCreate) {
        this.canCreate = canCreate;
    }

    public Collection getPhotosets() {
        return photosets;
    }

    public void setPhotosets(Collection photosets) {
        this.photosets = photosets;
    }

}
