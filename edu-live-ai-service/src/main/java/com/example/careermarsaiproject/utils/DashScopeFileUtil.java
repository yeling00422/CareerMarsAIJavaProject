//package com.example.careermarsaiproject.utils;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.http.*;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//
//public class DashScopeFileUtil {
//
//    private static final String UPLOAD_URL = "https://dashscope.aliyuncs.com/api/v1/files";
//    private final String apiKey;
//    private final RestTemplate restTemplate;
//
//    public DashScopeFileUtil(String apiKey) {
//        this.apiKey = apiKey;
//        this.restTemplate = new RestTemplate();
//    }
//
//    //  新增一个方法：直接接收字节数组，不需要 File 对象
//    // 新增一个方法：直接接收字节数组，不需要 File 对象
//    public String uploadBytes(byte[] fileContent, String originalFilename, String purpose) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        headers.set("Authorization", "Bearer " + apiKey);
//
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//
//        // 使用 ByteArrayResource 模拟文件
//        ByteArrayResource resource = new ByteArrayResource(fileContent) {
//            @Override
//            public String getFilename() {
//                return originalFilename != null ? originalFilename : "upload_file";
//            }
//        };
//
//        body.add("file", resource);
//        body.add("purpose", purpose);
//
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    UPLOAD_URL,
//                    HttpMethod.POST,
//                    requestEntity,
//                    Map.class
//            );
//
//            Map<String, Object> responseBody = response.getBody();
//
//            // 1. 检查顶层状态
//            if (responseBody == null) {
//                throw new RuntimeException("上传接口返回为空");
//            }
//
//            // 兼容两种返回格式
//            String code = (String) responseBody.get("code");
//            String requestId = (String) responseBody.get("request_id");
//
//            // 如果返回的是 SUCCESS (旧格式) 或者 200 (新格式可能没有 code 字段直接是 data)
//            boolean isSuccess = "SUCCESS".equals(code) || (responseBody.containsKey("data") && !responseBody.containsKey("code"));
//
//            if (isSuccess || "SUCCESS".equals(code)) {
//                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
//                if (data == null) {
//                    throw new RuntimeException("上传成功但 data 为空");
//                }
//
//                // 【关键修改】适配新返回格式：data.uploaded_files[0].file_id
//                if (data.containsKey("uploaded_files")) {
//                    List<Map<String, Object>> uploadedFiles = (List<Map<String, Object>>) data.get("uploaded_files");
//                    if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
//                        Map<String, Object> firstFile = uploadedFiles.get(0);
//                        String fileId = (String) firstFile.get("file_id"); // 新字段名可能是 file_id 或 id
//                        if (fileId == null) {
//                            fileId = (String) firstFile.get("id");
//                        }
//                        if (fileId != null) {
//                            System.out.println("✅ 文件上传成功 (新格式): " + fileId);
//                            return fileId;
//                        }
//                    }
//                    // 如果有 failed_uploads 且不为空，才报错
//                    List<?> failed = (List<?>) data.get("failed_uploads");
//                    if (failed != null && !failed.isEmpty()) {
//                        throw new RuntimeException("部分文件上传失败：" + failed);
//                    }
//                }
//                // 兼容旧格式：data.id
//                else if (data.containsKey("id")) {
//                    String fileId = (String) data.get("id");
//                    System.out.println("✅ 文件上传成功 (旧格式): " + fileId);
//                    return fileId;
//                }
//
//                // 如果都没找到，打印完整日志以便调试
//                throw new RuntimeException("上传成功但未找到 file_id，返回数据：" + responseBody);
//            } else {
//                throw new RuntimeException("文件上传 API 返回失败 [" + code + "]: " + responseBody);
//            }
//        } catch (Exception e) {
//            throw new RuntimeException("调用上传接口异常：" + e.getMessage(), e);
//        }
//    }
//}
