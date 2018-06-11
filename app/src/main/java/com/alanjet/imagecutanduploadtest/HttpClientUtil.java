package com.alanjet.imagecutanduploadtest;


import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by hongjian.chen on 2018/6/11.
 */

public class HttpClientUtil {

    public static String uploadFile(String url, String params, File file) throws FileNotFoundException {
        String result = "";
        HttpClient client = new HttpClient();
        PostMethod filePost = new PostMethod(url);
//       MultipartPostMethod filePost = new MultipartPostMethod(msUrl);
        // 若上传的文件比较大 , 可在此设置最大的连接超时时间
        client.getHttpConnectionManager().getParams().setConnectionTimeout(8000);
        try {
            FilePart fp = new FilePart(file.getName(), file);
            StringPart sp = new StringPart("params", params);
            MultipartRequestEntity mrp = new MultipartRequestEntity(new Part[]{sp, fp}, filePost.getParams());
            filePost.setRequestEntity(mrp);
            //使用系统提供的默认的恢复策略
            filePost.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
            int httpStat = client.executeMethod(filePost);
            if (httpStat == HttpStatus.SC_OK) {
                InputStream in = filePost.getResponseBodyAsStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                result = new String(br.readLine().getBytes("utf-8"), "utf-8");
                System.out.println("result=" + result);
                br.close();
                in.close();
                System.out.println("UPLOAD FILE SUCCESS");
            } else {
                result = "200";
            }
        } catch (IOException e) {
            e.printStackTrace();
            result = "200";
        }
        filePost.releaseConnection();
        return result;
    }
}
