package com.imagegallery.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class RestSessionCreator {

    private static final Log LOGGER = LogFactory.getLog(RestSessionCreator.class);
    private String token = null;
    private String authUrl;
    private String apiKey;

    public RestSessionCreator(String authUrl, String apiKey) {
        this.authUrl = authUrl;
        this.apiKey = apiKey;
    }

    public void generateRestToken() {
        if (token != null) {
            return;
        }

        renewToken();
    }

    public void renewToken() {
        LOGGER.info("Getting API token");
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
            HttpPost request = generateHttpPostRequest();
            CloseableHttpResponse response = httpClient.execute(request);
            JSONObject responseObject = new JSONObject(EntityUtils.toString(response.getEntity()));
            if (responseObject.getBoolean("auth")) {
                token = responseObject.getString("token");
            } else {
                LOGGER.warn("Error while retrieving token");
            }
        } catch (Exception e) {
            LOGGER.error("Error while getting token", e);
        }
    }

    private HttpPost generateHttpPostRequest() throws UnsupportedEncodingException {
        HttpPost request = new HttpPost(authUrl);
        request.addHeader("content-type", "application/json");
        request.setEntity(generateStringEntity());
        return request;
    }

    private StringEntity generateStringEntity() throws UnsupportedEncodingException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("apiKey", apiKey);
        return new StringEntity(jsonObject.toString());
    }

    public JSONObject getResponseFromHttpGetRequest(String url, HttpClient httpClient) throws IOException {
        HttpGet request = new HttpGet(url);
        request.addHeader("Authorization", "Bearer " + token);
        HttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            renewToken();
            response = httpClient.execute(request);
        }
        return new JSONObject(EntityUtils.toString(response.getEntity()));
    }
}
