/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mycompany.youtubesearchapp;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author aandrew1
 */
public class Search 
{
    private static String PROPERTIES_FILENAME = "youtube.properties";
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final long NUMBER_OF_VIDEOS_RETURNED = 50;
    private static YouTube youtube;
   
    
    public static void main(String[] args)
    {
        // Read the dev key from youtube.properties
        Properties properties = new Properties();
        
        try 
        {
            InputStream in = Search.class.getResourceAsStream("/" + PROPERTIES_FILENAME);
            properties.load(in);
        } 
        catch (IOException e) 
        {
            System.err.println("There was an error reading " + PROPERTIES_FILENAME + ": " + e.getCause()
                + " : " + e.getMessage());
            System.exit(1);
        }
        
        try
        {
            /*
            * The YouTube object is used to make all API requests. The last argument is required, but
            * because we don't need anything initialized when the HttpRequest is initialized, we override
            * the interface and provide a no-op function.
            */
            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer()
            {
                @Override
                public void initialize(HttpRequest request) throws IOException {}
            }).setApplicationName("YouTubeSearchApp").build();
            
            // Get query term from the user
            String queryTerm = getInputQuery();
            
            YouTube.Search.List search = youtube.search().list("id,snippet");
            
            /*
            * It is important to set your developer key from the Google Developer Console for
            * non-authenticated requests (found under the API Access tab at this link:
            * code.google.com/apis/). This is good practice and increased your quota.
            */
            String apiKey = properties.getProperty("youtube.apikey");
            search.setKey(apiKey);
            search.setQ(queryTerm);
            
            // We are only searching for the vidoes (not playlists or channels). 
            search.setType("video");
            
            // This method reduces the info returned to only the fields we need and makes calls more efficient
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
            SearchListResponse searchResponse = search.execute();
            
            List<SearchResult> searchResultList = searchResponse.getItems();
            
            if(searchResultList != null)
            {
                ShowResultsToUser(searchResultList.iterator(), queryTerm);
            }
        } 
        catch (GoogleJsonResponseException ex)
        {
            System.err.println("There was a service error: " + ex.getDetails().getCode() + " : " + ex.getDetails().getMessage());
        }
        catch (IOException ex)
        {
            System.err.println("There was an IO error: " + ex.getCause() + " : " + ex.getMessage());
        }
        catch (Throwable t)
        {
            t.printStackTrace();
        } 
    }
    
    // Gets a search term from the user
    private static String getInputQuery() throws IOException
    {
        String inputQuery = "";
        
        System.out.print("Please enter a search term: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputQuery = bReader.readLine();
        
        if(inputQuery.length() < 1)
        {
            // If nothing is entered, defaulst to "Gangnam Style"
            inputQuery = "Gangnam Style";
        }
        return inputQuery;
    }
    
    // Prints out the Search Results
    private static void ShowResultsToUser(Iterator<SearchResult> iteratorSearchResults, String query)
    {
        System.out.println("\n=============================================================");
        System.out.println("   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) 
        {
          System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) 
        {
            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Double checks the kind is video.
            if (rId.getKind().equals("youtube#video")) 
            {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().get("default");

                System.out.println(" Video Id" + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }
}

