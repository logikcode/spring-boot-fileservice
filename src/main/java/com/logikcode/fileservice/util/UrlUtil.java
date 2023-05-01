package com.logikcode.fileservice.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
public class UrlUtil {

     public static String siteBaseUrl(HttpServletRequest request){
         return  ServletUriComponentsBuilder.fromRequestUri(request)
                 .replacePath(null)
                 .build()
                 .toUriString();
     }

    public static String normalizedUrl(String requestUrl, int from, int to){
        int OFFSET = 1;
        return requestUrl.substring(from, OFFSET + to);

    } public static String getSiteUriPath(HttpServletRequest request){
        log.info("REQUEST URI "+ request.getRequestURI());
        return request.getRequestURI();
    }
    public static String buildDownloadUrl(String urlPath, String fileName, String variablePath){
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(urlPath + variablePath)
                .path(fileName)
                .toUriString();
        return url;
    }
}
