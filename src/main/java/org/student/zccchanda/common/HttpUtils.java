package org.student.zccchanda.common;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

@Component
public class HttpUtils {

    public String processHttpRequest(HttpConfiguration httpConfiguration){
        HttpURLConnection httpURLConnection = null;
        try {
            StringBuilder serviceUrl = new StringBuilder(httpConfiguration.getServiceURL());
            if (httpConfiguration.getQueryParameters() != null) {
                serviceUrl.append("?").append(httpConfiguration.getQueryParameters());
            }

            URL url = new URL(serviceUrl.toString());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod(httpConfiguration.getMethod());
            httpURLConnection.setReadTimeout(httpConfiguration.getTimeout());

            for (Map.Entry<String,String> entry : httpConfiguration.getHeaderMap().entrySet()){
                httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }

            if(HttpMethod.POST.matches(httpConfiguration.getMethod())){
                httpURLConnection.setDoOutput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(httpConfiguration.getPayLoad().getBytes());
                outputStream.flush();
            }

            if(httpURLConnection.getResponseCode() == 500){
                throw new RuntimeException("Failed -> Http Code : " + httpURLConnection.getResponseCode());
            }
            InputStream inputStream;
            if(httpURLConnection.getResponseCode() >= 400 ){
                inputStream = httpURLConnection.getErrorStream();
            } else {
                inputStream = httpURLConnection.getInputStream();
            }

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String output;
            StringBuilder responseBuilder = new StringBuilder();
            while ((output = bufferedReader.readLine()) != null) {
                responseBuilder.append(output).append("\n");
            }
            return responseBuilder.toString();
        } catch (Exception ex) {
            return null;
        } finally {
            if (httpURLConnection != null){
                httpURLConnection.disconnect();
            }
        }
    }
}
