package com.gautam.socialfly.Model;

public class VCModel
{
    String key,response;

    public VCModel(String key, String response) {
        this.key = key;
        this.response = response;
    }

    public VCModel() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
