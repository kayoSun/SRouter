package com.kayo.srouter.api;

import java.util.HashMap;
import java.util.Map;

public class RouterPath {
    private String url;
    private String path;
    private String query;
    private String host;
    private String protocol;
    private Map<String,String> queries;

    public RouterPath(String url) {
        this.url = url;
        matchPath(url);
        matchHost(path);
        matchQuery(query);
    }

    private void matchQuery(String query) {
        queries = new HashMap<>();
        if (!isEmpty(query)){
            if (query.contains("&")){
                String[] split = query.split("&");
                for (String s : split) {
                    if (s.contains("=")){
                        String[] split1 = s.split("=");
                        queries.put(split1[0],split1[1]);
                    }
                }
            }else if (query.contains("=")){
                String[] split = query.split("=");
                queries.put(split[0],split[1]);
            }
        }
    }

    private void matchHost(String path) {
        if (!isEmpty(path) && path.contains("://")) {
            int index = path.indexOf("://");
            protocol = path.substring(0,index);
            path = path.substring(index + 3, path.length());
            host = path;
            if (path.contains("/")) {
                host = path.substring(0, path.indexOf("/"));
            }
        }
    }

    private void matchPath(String url) {
        if (!isEmpty(url)) {
            path = url;
            if (url.contains("?")) {
                String[] split = url.split("\\?");
                path = split[0];
                query = split[1];
            }
        }
    }

    public String getOriginUrl() {
        return url;
    }

    public String getPath(){
        return path;
    }

    public String getQuery() {
        return query;
    }

    public String getHost() {
        return host;
    }

    public String getProtocol() {
        return protocol;
    }

    public Map<String, String> getQueries() {
        return queries == null? new HashMap<String, String>():queries;
    }

    private boolean isEmpty(String s) {
        if (s == null) {
            return true;
        }
        if (s.length() == 0) {
            return true;
        }
        if (s.equals("null")) {
            return true;
        }
        return false;
    }
}
