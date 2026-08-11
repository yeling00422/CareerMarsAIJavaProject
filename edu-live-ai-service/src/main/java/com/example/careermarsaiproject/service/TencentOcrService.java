package com.example.careermarsaiproject.service;

import com.example.careermarsaiproject.utils.TencentOcrProp;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.ocr.v20181119.OcrClient;
import com.tencentcloudapi.ocr.v20181119.models.GeneralBasicOCRRequest;
import com.tencentcloudapi.ocr.v20181119.models.GeneralBasicOCRResponse;
import com.tencentcloudapi.ocr.v20181119.models.TextDetection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TencentOcrService {

    private final TencentOcrProp ocrProp;

    /**
     * 图片文件OCR识别 返回拼接完整文本
     */
    public String ocrImage(MultipartFile file) {
        try {
            // 1. 图片转Base64
            String base64 = null;
            try {
                base64 = fileToBase64(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // 2. 初始化腾讯云凭证
            Credential cred = new Credential(ocrProp.getSecretId(), ocrProp.getSecretKey());

            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("ocr.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            // 3. 初始化OCR客户端
            OcrClient client = new OcrClient(cred, "ap-beijing", clientProfile);
            GeneralBasicOCRRequest req = new GeneralBasicOCRRequest();
            // 传base64图片
            req.setImageBase64(base64);

            // 4. 调用接口
            GeneralBasicOCRResponse resp = client.GeneralBasicOCR(req);
            StringBuilder sb = new StringBuilder();
            for (TextDetection text : resp.getTextDetections()) {
                sb.append(text.getDetectedText()).append("\n");
            }
            return sb.toString().trim();

        } catch (TencentCloudSDKException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * MultipartFile 转 Base64
     */
    private String fileToBase64(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        }
    }
}