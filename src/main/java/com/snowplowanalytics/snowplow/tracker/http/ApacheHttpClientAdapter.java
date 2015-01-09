package com.snowplowanalytics.snowplow.tracker.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snowplowanalytics.snowplow.tracker.Constants;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class ApacheHttpClientAdapter extends AbstractHttpClientAdapter {

    private final Logger LOGGER = LoggerFactory.getLogger(ApacheHttpClientAdapter.class);
    private final String uri;

    private CloseableHttpClient httpClient;
    
    public ApacheHttpClientAdapter(String uri, CloseableHttpClient httpClient, ObjectMapper objectMapper) {
        super(objectMapper);
        this.uri = uri;
        this.httpClient = httpClient;
    }

    
    public int doGet(Map<String, Object> payload) {
        HttpResponse httpResponse;
        try {
            URIBuilder uriBuilder = new URIBuilder(uri);
            for (String key : payload.keySet()) {
                String value = (String) payload.get(key);
                uriBuilder.setParameter(key, value);
            }
            HttpGet httpGet = new HttpGet(uriBuilder.setPath("/i").build());
            httpResponse = httpClient.execute(httpGet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return httpResponse.getStatusLine().getStatusCode();
    }
    
    public int doPost(byte[] payload) {

        HttpResponse httpResponse;
        try {
            URIBuilder uriBuilder = new URIBuilder(uri);
            HttpPost httpPost = new HttpPost(uriBuilder.setPath("/" + Constants.PROTOCOL_VENDOR + "/" + Constants.PROTOCOL_VERSION).build());
            httpPost.addHeader("Content-Type", "application/json; charset=utf-8");

            ByteArrayEntity params = new ByteArrayEntity(payload);
            httpPost.setEntity(params);

            httpResponse = httpClient.execute(httpPost);
            LOGGER.debug(httpResponse.getStatusLine().toString());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return httpResponse.getStatusLine().getStatusCode();
    }
}
