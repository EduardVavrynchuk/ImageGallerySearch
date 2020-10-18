package com.imagegallery.service;

import com.imagegallery.configs.AppConfig;
import com.imagegallery.util.ImageInfo;
import com.imagegallery.util.RestSessionCreator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class GalleryService implements InitializingBean {

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private AppConfig appConfig;

    private Map<String, ImageInfo> cache = new ConcurrentHashMap<>();
    private RestSessionCreator restSession;

    public Collection<ImageInfo> getImageInfoByTerm(String searchTerm) {
        return fullObjectSearch(searchTerm.toLowerCase());
    }

    private Collection<ImageInfo> fullObjectSearch(String searchTerm) {
        List<ImageInfo> imageInfoList = new LinkedList<>();
        String[] terms = ImageInfo.splitSearchTerms(searchTerm);
        cache.values().forEach(imageInfo -> {
            if (imageInfo.containSearchTerm(terms)) {
                imageInfoList.add(imageInfo);
            }
        });

        return imageInfoList;
    }

    @Override
    public void afterPropertiesSet() {
        restSession = new RestSessionCreator(appConfig.authUrl, appConfig.apiKey);
        PeriodicTrigger periodicTrigger = new PeriodicTrigger(appConfig.scheduleSecTime, TimeUnit.SECONDS);
        periodicTrigger.setFixedRate(true);

        taskScheduler.schedule(new CacheUpdateService(), periodicTrigger);
    }


    private class CacheUpdateService implements Runnable {

        private final Log LOGGER = LogFactory.getLog(CacheUpdateService.class);

        @Override
        public void run() {
            loadPaginatedImages();
        }

        private void loadPaginatedImages() {
            LOGGER.info("Starting cache synchronization!");
            restSession.generateRestToken();
            try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                JSONObject responseObject = restSession.getResponseFromHttpGetRequest(appConfig.imageUrl, httpClient);
                if (responseObject.has("pictures")) {
                    JSONArray jsonArray = responseObject.getJSONArray("pictures");
                    generateImageInfoFromArray(jsonArray);

                    int startPage = 2;
                    while (responseObject.getBoolean("hasMore")) {
                        responseObject = restSession.getResponseFromHttpGetRequest((appConfig.imageUrl + "?page=" + startPage), httpClient);
                        jsonArray = responseObject.getJSONArray("pictures");
                        generateImageInfoFromArray(jsonArray);
                        startPage++;
                    }

                    for (Map.Entry<String, ImageInfo> entry : cache.entrySet()) {
                        responseObject = restSession.getResponseFromHttpGetRequest(appConfig.imageUrl + "/" + entry.getKey(), httpClient);
                        entry.getValue().updateInfo(responseObject);
                    }
                }

                LOGGER.info("Cache synchronization is finished!");
            } catch (Exception e) {
                LOGGER.error("Error while cache synchronization", e);
            }
        }

        private void generateImageInfoFromArray(JSONArray jsonArray) {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject paginatedImageInfo = (JSONObject) jsonArray.get(i);
                String key = paginatedImageInfo.getString("id");
                if (!cache.containsKey(key)) {
                    cache.put(key, new ImageInfo(key));
                }
            }
        }

    }

}
