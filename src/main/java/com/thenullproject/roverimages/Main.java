package com.thenullproject.roverimages;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;
import static java.time.temporal.ChronoUnit.MINUTES;

public class Main{
    public static void main(String[] args) {
        if(args.length == 1 && (args[0].equals("--help")||args[0].equals("-h"))) {
            String help = """
                    usage: rovfetch <sol> <rover> <directory>
                    
                    sol         : the martian sol to fetch images from
                    rover       : one of four mars rovers; curiosity, opportunity, spirit or perseverance
                    directory   : the resource directory to save json and the fetched images
                    
                    example: the following would fetch all curiosity rover images from sol 19 saving them into ./res:
                    rovfetch 19 curiosity .
                    
                    note: you will need an api key, get one here -> https://api.nasa.gov
                    """;

            System.out.println(help);
            System.exit(0);
        }

        boolean failed = true;

        if(args.length == 3) {
            try {
                int sol = Integer.parseInt(args[0]);
                boolean validRover = List.of("curiosity", "opportunity", "spirit", "perseverance").contains(args[1].toLowerCase());
                if(sol >= 0 && validRover) {
                    run(sol, args[1], args[2]);
                    failed = false;
                }
            } catch(NumberFormatException e) {}
        }

        if(failed) {
            System.out.println("usage: rovfetch <sol> <rover> <directory>");
            System.out.println("more info: rovfetch --help");
            System.exit(0);
        }
    }
    public static void run(int sol, String rover, String resourcePath) {
        String resDir = resourcePath+"/res/";
        String jsonDir = resDir + "/response_json/";
        String photoDir = resDir + "/rover_images/";
        if(!createExternalResourceFolder(jsonDir) ||
                !createExternalResourceFolder(photoDir)){
            System.err.println("failed to create resource directories");
            System.exit(0);
        }


        String apiKey = "DEMO_KEY"; //TODO: ENTER YOUR API KEY HERE. GET ONE AT https://api.nasa.gov


        System.out.println("-----requesting json from sol " + sol + "-----");

        StringBuilder uriSB = new StringBuilder("https://api.nasa.gov/mars-photos/api/v1/rovers/")
                .append(rover)
                .append("/photos?sol=").append(sol).append("&")
                .append("api_key=").append(apiKey);


        HttpResponse<Path> response = connect(uriSB.toString(), jsonDir+"/responseJson.json");

        if(response != null) {
            System.out.println("successful, parsing response data...");

            ParseJson parseJson = new ParseJson();
            String jsonString = parseJson.getFileAsString(response.body().toString());
            String photosArray = (String) parseJson.getJsonObjectValues(jsonString,"photos")[0];
            int photosArraySize = parseJson.getJsonArraySize(photosArray);

            System.out.println("image total: " + photosArraySize);
            System.out.println("downloading images...\n");
            int totalDownloadedImages = 0;

            for(int i=0; i<photosArraySize; i++) {
                String photo = (String) parseJson.getJsonArrayItem(photosArray, i);
                Object[] photoValues = parseJson.getJsonObjectValues(photo,
                        "sol",
                        "img_src",
                        "rover", "name",
                        "camera", "full_name",
                        "id");

                if (photoValues != null) {

                    long photoSol = (long) photoValues[0];
                    String photoImageSrc = (String) photoValues[1];
                    String photoRover = (String) photoValues[2];
                    String photoCamera = (String) photoValues[3];
                    long photoId = (long) photoValues[4];

                    StringBuilder filePath = new StringBuilder(photoDir)
                            .append("sol_").append(photoSol).append("/")
                            .append("rover_").append(photoRover).append("/")
                            .append("camera_").append(photoCamera).append("/")
                            .append("photoid_").append(photoId)
                            .append(photoImageSrc.substring(photoImageSrc.length()-4));


                    if (downloadFile(photoImageSrc, filePath.toString())) {
                        totalDownloadedImages++;
                    } else {
                        System.err.print("\nimage " + (i + 1) + " download unsuccessful or problem creating directory\n");
                    }

                    int perc = (((i+1)*100)/photosArraySize);
                    for(int bs=0;bs<String.valueOf(perc).length();bs++)
                        System.out.print("\b");
                    System.out.print("\b"+perc+"%");
                } else {
                    System.err.println("problem parsing or no such key exists for one or more keys passed");
                }
            }

            System.out.println("\ntotal images downloaded: "+totalDownloadedImages);

        } else {
            System.err.println("request was unsuccessful :( ");
        }
    }



    private static HttpResponse<Path> connect(String uri, String filePath) {
        HttpRequest request;

        try{
            request = HttpRequest.newBuilder()
                    .uri(new URI(uri))
                    .timeout(Duration.of(1, MINUTES))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            System.err.println("error occurred when parsing uri -> " + e.getLocalizedMessage());
            return null;
        }


        HttpClient client =  HttpClient.newBuilder()
                .proxy(ProxySelector.getDefault())
                .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_NONE))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();


        try{
            return client.send(request, HttpResponse.BodyHandlers.ofFile(Paths.get(filePath), CREATE, WRITE, TRUNCATE_EXISTING));
        } catch(IOException | InterruptedException e) {
            System.err.println("error occurred when sending or receiving -> " + e.getLocalizedMessage());
            return null;
        }



        
    }

    private static boolean downloadFile(String uri, String path) {

        if( !createExternalResourceFolder(Paths.get(path).getParent().toString()) )
            return false;


        return connect(uri, path) != null;
    }


    private static boolean createExternalResourceFolder(String path) {

        Path dir = Paths.get(path);
        if(!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                System.err.println("error occurred when creating directory -> " + e.getLocalizedMessage());
                return false;
            }
        }
        return true;
    }
}