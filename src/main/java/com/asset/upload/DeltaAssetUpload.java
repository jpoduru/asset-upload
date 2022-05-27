package com.asset.upload;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * Hello world!
 *
 */
public class DeltaAssetUpload
{
    static String userCredentials;
    static String basicAuth;
    static Properties prop;

    public DeltaAssetUpload() {
        prop = readProperty("environment.properties");
        System.out.println( prop.getProperty("devEnvironment"));
        System.out.println( prop.getProperty("env-username"));
        System.out.println( prop.getProperty("env-password"));
        System.out.println( prop.getProperty("maindirpath"));
        userCredentials = prop.getProperty("env-username")+":"+prop.getProperty("env-password");
        basicAuth = "Basic " + new String(Base64.getEncoder().encode(userCredentials.getBytes()));
    }

    public static void main(String[] args) throws IOException {
        String fileName = "";
        String fileSize = "";
        String filePath = "";
        DeltaAssetUpload deltaAssetUpload = new DeltaAssetUpload();
        String maindirpath =  deltaAssetUpload.prop.getProperty("maindirpath");
        // File object
        File maindir = new File(maindirpath);

        if (maindir.exists() && maindir.isDirectory()) {
            // array for files and sub-directories
            // of directory pointed by maindir
            File arr[] = maindir.listFiles();
            getSortedList(arr);
            String jsonInputString = "{\r\n" + "\"properties\":{\r\n" + "\"title\":\"target-folder3\"\r\n"
                    + "   }\r\n" + "}";
            String folderName = maindir.getName();
            //createAssetFolder(basicAuth, jsonInputString, folderName);

            // Calling recursive method
            RecursivePrint(arr, 0, 0, folderName, fileName, fileSize, filePath);
            //System.out.println("Execution finished");
        }
    }

    private static void getSortedList(File[] arr) {
        Arrays.sort(arr, (f1, f2) -> {
            if (f1.isFile() && !f2.isFile()) {
                return -1;
            } else if (!f1.isFile() && f2.isFile()) {
                return 1;
            } else {
                return f1.compareTo(f2);
            }
        });
    }


    /**
     * Creating the folder if doesn't exist
     * @param basicAuth
     * @param jsonInputString
     * @param folderName
     */
    private static void createAssetFolder(String basicAuth, String jsonInputString, String folderName) {

        StringEntity entity;
        try {
            entity = new StringEntity(jsonInputString);
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(prop.getProperty("devEnvironment")+"api/assets/" + "target-folder3");
            request.addHeader("content-type", "application/json");
            request.setEntity(entity);
            request.setHeader("class", "assetFolder");
            request.setHeader("Authorization", basicAuth);
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 409) {
                System.out.printf("Targer-folder already exist");
            }
            if (response.getStatusLine().getStatusCode() == 201) {
                System.out.printf("Content Created");
            }
        } catch (UnsupportedEncodingException e) {
            System.out.printf("UnsupportedEncodingException", e.getMessage());
        } catch (ClientProtocolException e) {
            System.out.printf("ClientProtocolException", e.getMessage());
        } catch (IOException e) {
            System.out.printf("IOException", e.getMessage());
        }

    }

    static void RecursivePrint(File[] arr, int index, int level, String folderName, String fileName, String fileSize,
                               String filePath) {
        List<File> list = Arrays.asList(arr);
        long directories = list.stream().filter(a -> !a.isFile()).count();

        if (index == arr.length - directories) {
            // POST call to initiate upload
            ArrayList<AssetProperties> assetList = getInitiateUpload(basicAuth, fileName, fileSize, folderName,
                    filePath);
            Iterator<AssetProperties> iterator = assetList.iterator();
            while (iterator.hasNext()) {
                AssetProperties assetProperty = iterator.next();
                String uploadURI = assetProperty.getUploadURIs();
                filePath = assetProperty.getFilePath();
                fileName = assetProperty.getFileName();
                String uploadToken = assetProperty.getUploadToken();

                try {
                    // PUT call to upload asset
                    uploadAssetToServer(uploadURI, filePath);
                    // complete upload
                    String completeUrl = "https://author-p58004-e461554.adobeaemcloud.com/content/dam/" + "target-folder3"
                            + ".completeUpload.json";
                    completeUpload(basicAuth, completeUrl, fileName, uploadToken);
                } catch (Exception e) {
                    System.out.printf("Exception", e.getMessage());
                }
            }
            if (directories > 0) {
                for (int i = 0; i < directories; i++) {

                    // for sub-directories
                    // System.out.println("[" + arr[index+i].getName() + "]");
                    String jsonInputString = "{\r\n" + "   \"properties\":{\r\n"
                            + "      \"title\":\"target-folder3\"\r\n" + "   }\r\n" + "}";
                    String newFolderName = folderName + "/" + arr[index + i].getName();
                    System.out.println("FolderName ::" + newFolderName);
                   // createAssetFolder(basicAuth, jsonInputString, newFolderName);
                    File[] fileArr = arr[index + i].listFiles();
                    getSortedList(fileArr);
                    // recursion for sub-directories
                    RecursivePrint(fileArr, 0, level + 1, newFolderName, "", "", "");
                }
            }
        }
        // terminate condition
        if (index == arr.length) {
            return;
        }

        // for files
        if (arr[index].isFile() && !arr[index].getName().startsWith(".")) {
            // System.out.println(arr[index].getName());
            // System.out.println(arr[index].length());
            fileName = fileName + "&" + "fileName=" + arr[index].getName();
            if(arr[index].getName().equals("original")){
                fileName = "&" + "fileName=test";;
            }
            long fileSizeValue = arr[index].length();
            String newFileSize = "fileSize=" + Long.toString(fileSizeValue);
            fileSize = fileSize + "&" + "fileSize=" + Long.toString(fileSizeValue);
            filePath = filePath + "&" + arr[index].getAbsolutePath();
        }

        // System.out.println("filename::" + fileName);
        // System.out.println("size::" + fileSize);
        // recursion for main directory
        RecursivePrint(arr, ++index, level, folderName, fileName, fileSize, filePath);
    }


    private static ArrayList<AssetProperties> getInitiateUpload(String basicAuth, String fileName, String fileSize,
                                                                String folderName, String filePath) {
        ArrayList<AssetProperties> assetsList = null;
        try {

            String dummy = "https://author-p58004-e461554.adobeaemcloud.com/content/dam/" + "target-folder3"
                    + ".initiateUpload.json?path=/content/dam/" + "target-folder3" + fileName + fileSize;
            // System.out.println("dummy::"+dummy);
            URL url = new URL(dummy);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;");
            conn.setRequestProperty("addContentLengthHeader", "true");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", basicAuth);

            if (conn.getResponseCode() == 200) {
                // System.out.println("Con1 Successfull");
                // System.out.println("Con1 Successfull messsage::" + conn.getResponseCode());
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String output;
                while ((output = br.readLine()) != null) {
                    JSONObject jsonObject = new JSONObject(output);
                    // System.out.println("First Response jsonObject::" + jsonObject);
                    JSONArray filesArray = jsonObject.getJSONArray("files");
                    assetsList = new ArrayList<AssetProperties>();
                    for (int i = 0; i < filesArray.length(); i++) {
                        AssetProperties assetProperties = new AssetProperties();
                        JSONObject objects = filesArray.getJSONObject(i);
                        String uploadURIs = objects.getJSONArray("uploadURIs").get(0).toString();
                        assetProperties.setUploadURIs(uploadURIs);
                        fileName = objects.get("fileName").toString();
                        String[] filePaths = filePath.split("&");
                        for (String path : filePaths) {
                            if (path.contains(fileName) && !path.equals("")) {
                                assetProperties.setFilePath(path);
                            }
                        }
                        // System.out.println("fileName::" + fileName);
                        assetProperties.setFileName(fileName);
                        String mimeType = objects.get("mimeType").toString();
                        assetProperties.setMimeType(mimeType);
                        String uploadToken = objects.get("uploadToken").toString();
                        assetProperties.setUploadToken(uploadToken);
                        assetsList.add(assetProperties);
                        // System.out.println("assetsList--size::" + assetsList.size());
                    }
                }
            } else if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }
            conn.disconnect();
        } catch (ProtocolException e) {
            System.out.printf("ProtocolException", e.getMessage());
        } catch (MalformedURLException e) {
            System.out.printf("MalformedURLException", e.getMessage());
        } catch (IOException e) {
            System.out.printf("IOException", e.getMessage());
        } catch (JSONException e) {
            System.out.printf("JSONException", e.getMessage());
        }

        return assetsList;
    }

    /**
     * Read the Property file from the resource
     * @param fileName
     * @return
     */
    private static Properties readProperty(String fileName) {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName);
            prop.load(input);
        } catch (IOException io) {
            io.printStackTrace();
        }
        return prop;
    }

    private static void uploadAssetToServer(String uploadURIs, String filePath) {

        try {
            doTrustToCertificates();
            URL url = new URL(uploadURIs);
            HttpURLConnection conn2 = (HttpURLConnection) url.openConnection();
            conn2.setRequestMethod("PUT");
            conn2.setRequestProperty("Content-Type", "application/json;");
            conn2.setDoOutput(true);
            InputStream inputStream = new FileInputStream(filePath);

            try (OutputStream os = conn2.getOutputStream()) {
                int byteRead;
                while ((byteRead = inputStream.read()) != -1) {
                    os.write(byteRead);
                }
            }
            if (conn2.getResponseCode() != 201) {
                throw new RuntimeException("Failed : HTTP error code : " + conn2.getResponseCode());
            }
            if (conn2.getResponseCode() == 201) {
                System.out.println("Con2 Successfull::Asset Created");
                System.out.println("Con2 Successfull messsage::" + conn2.getResponseCode());
            }
            conn2.disconnect();
        } catch (Exception e) {
            System.out.printf("Exception", e.getMessage());
        }

    }

    private static void completeUpload(String basicAuth, String completeUrl, String fileName, String uploadToken) {
        String newUrl = completeUrl + "?fileName=" + fileName + "&mimeType=image/png" + "&" + "uploadToken="
                + uploadToken + "&" + "uploadDuration=1679";
        // System.out.println("newUrl::::" + newUrl);

        try {
            URL url = new URL(newUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json;");
            conn.setRequestProperty("addContentLengthHeader", "true");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", basicAuth);
            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
            }
            if (conn.getResponseCode() == 200) {
                System.out.println("Con3 Successfull::asset uploaded to the target instance to targeted folder");
                System.out.println("Con3 Successfull messsage::" + conn.getResponseCode());
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            System.out.printf("MalformedURLException", e.getMessage());
        } catch (IOException e) {
            System.out.printf("IOException", e.getMessage());
        }
    }
    // trusting all certificate
    public static void doTrustToCertificates() {
        Security.addProvider(Security.getProvider("SunJSSE"));
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                return;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                return;
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                        /*
                         * System.out.println("Warning: URL host '" + urlHostName +
                         * "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                         */
                    }
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);

        } catch (KeyManagementException e) {
            System.out.printf("KeyManagementException", e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.printf("NoSuchAlgorithmException", e.getMessage());
        }
    }
}
