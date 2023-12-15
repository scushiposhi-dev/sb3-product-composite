package com.example.sb3productcomposite.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Component
@Slf4j
public class ServiceUtil {
    private final String port;
    private String serviceAddress;
    public ServiceUtil(@Value("${server.port}") String port) {
        this.port = port;
    }

    public String getServiceAddress(){
        if(serviceAddress==null){
            serviceAddress=findHostName()+"/"+findIpAddress()+":"+port;
        }
        return serviceAddress;
    }

    private String findIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown host name";
        }
    }

    private String findHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }catch (UnknownHostException exc){
            return "unknown host name";
        }
    }
}
