package com.alipay.cloudrun.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Http工具
 */
public class HttpUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtil.class);
    private static final CloseableHttpClient httpClient;

    static {
        httpClient = HttpClients.custom().build();
    }

    /**
     * 执行get请求获取响应
     *
     * @param url 请求地址
     * @return 响应内容
     */
    public static String get(String url) {
        return get(url, null, null);
    }

    /**
     * 执行get请求获取响应
     *
     * @param url     请求地址
     * @param headers 请求头参数
     * @return 响应内容
     */
    public static String get(String url, Map<String, String> headers) {
        return get(url, headers, null);
    }

    public static String get(String url, Map<String, String> headers, Map<String, String> params) {
        ClassicRequestBuilder builder = ClassicRequestBuilder.get(url);
        if (headers != null) {
            headers.forEach(builder::addHeader);
        }
        if (params != null) {
            params.forEach(builder::addParameter);
        }
        ClassicHttpRequest httpGet = builder.build();
        try {
            return httpClient.execute(httpGet, response -> HttpUtil.handleResponse(httpGet, response));
        } catch (IOException e) {
            LOGGER.error("getForString error: {},{}", url, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 帖子
     * 执行post请求获取响应
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return 响应内容
     */
    public static String postForm(String url, Map<String, String> params) {
        return postForm(url, null, params);
    }

    /**
     * 执行post请求获取响应
     *
     * @param url     请求地址
     * @param headers 请求头参数
     * @param params  请求参数
     * @return 响应内容
     */
    public static String postForm(String url, Map<String, String> headers, Map<String, String> params) {
        ClassicRequestBuilder builder = ClassicRequestBuilder.post(url);
        if (headers != null) {
            headers.forEach(builder::addHeader);
        }
        if (params != null) {
            params.forEach(builder::addParameter);
        }
        ClassicHttpRequest httpPost = builder.build();
        try {
            return httpClient.execute(httpPost, response -> HttpUtil.handleResponse(httpPost, response));
        } catch (IOException e) {
            LOGGER.error("postForm error: {},{}", url, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行post请求获取响应（请求体为JOSN数据）
     *
     * @param url  请求地址
     * @param json 请求的JSON数据
     * @return 响应内容
     */
    public static String postJson(String url, String json) {
        return postJson(url, null, json);
    }

    public static String postJson(String url, Map<String, String> headers, String json) {
        ClassicRequestBuilder builder = ClassicRequestBuilder.post(url);
        if (headers != null) {
            headers.forEach(builder::addHeader);
        }
        ClassicHttpRequest httpPost = builder.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON)).build();
        try {
            return httpClient.execute(httpPost, response -> HttpUtil.handleResponse(httpPost, response));
        } catch (IOException e) {
            LOGGER.error("postJson error: {},{}", url, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static String handleResponse(ClassicHttpRequest request, ClassicHttpResponse response) throws IOException, ParseException {
        final HttpEntity responseEntity = response.getEntity();
        int statusCode = response.getCode();
        if (statusCode >= HttpStatus.SC_REDIRECTION) {
            logRequest(request, statusCode);
            EntityUtils.consume(responseEntity);
            throw new HttpResponseException(statusCode, response.getReasonPhrase());
        }
        if (responseEntity == null) {
            return null;
        }
        return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8.name());
    }

    private static void logRequest(ClassicHttpRequest request, int statusCode) throws IOException, ParseException {
        URI uri = null;
        try {
            uri = request.getUri();
        } catch (URISyntaxException ignored) {
            // 忽略异常, 只影响日志打印
        }
        HttpEntity requestEntity = request.getEntity();
        String body = requestEntity == null ? null : EntityUtils.toString(requestEntity, StandardCharsets.UTF_8.name());
        LOGGER.warn("HttpStatusCodeErr:{} {} {}", uri, body, statusCode);
    }
}