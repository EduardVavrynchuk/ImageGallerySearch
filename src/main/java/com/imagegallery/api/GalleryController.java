package com.imagegallery.api;

import com.google.gson.Gson;
import com.imagegallery.service.GalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/images")
public class GalleryController {

    @Autowired
    private GalleryService galleryService;

    private Gson formatter = new Gson();

    @GetMapping(value = "/{searchTerm}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getImageBySearchTerm(@PathVariable String searchTerm) {
        return formatter.toJson(galleryService.getImageInfoByTerm(searchTerm));
    }

}
