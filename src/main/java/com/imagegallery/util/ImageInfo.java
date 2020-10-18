package com.imagegallery.util;

import org.json.JSONObject;

public class ImageInfo {
    public String id;
    public String tags;
    public String camera;
    public String author;
    public String fullPicture;
    public String croppedPicture;

    public ImageInfo(String id) {
        this.id = id;
    }

    public boolean containSearchTerm(String[] searchTerms) {
        for (String term: searchTerms) {
            if (id.toLowerCase().contains(term)) {
                return true;
            }

            if (tags != null && tags.toLowerCase().contains(term)) {
                return true;
            }

            if (camera != null && camera.toLowerCase().contains(term)) {
                return true;
            }

            if (author != null && author.toLowerCase().contains(term)) {
                return true;
            }
        }

        return false;
    }

    public static String[] splitSearchTerms(String searchTerm) {
        return searchTerm.toLowerCase().split(",");
    }

    public void updateInfo(JSONObject jsonObject) {
        if (jsonObject.has("author")) {
            author = jsonObject.getString("author");
        }
        if (jsonObject.has("camera")) {
            camera = jsonObject.getString("camera");
        }
        if (jsonObject.has("tags")) {
            tags = jsonObject.getString("tags");
        }
        if (jsonObject.has("cropped_picture")) {
            croppedPicture = jsonObject.getString("cropped_picture");
        }
        if (jsonObject.has("full_picture")) {
            fullPicture = jsonObject.getString("full_picture");
        }
    }
}