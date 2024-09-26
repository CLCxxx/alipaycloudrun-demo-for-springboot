package com.alipay.cloudrun.client;

import com.alipay.cloudrun.util.HttpUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceClient {

    @Value("${service.url}")
    private String serviceUrl;

    private static final String QUERY_URL = "/service";

    public String getServiceInfo(){
        return HttpUtil.get(serviceUrl + QUERY_URL);
    }
}
