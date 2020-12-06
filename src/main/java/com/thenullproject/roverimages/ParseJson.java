package com.thenullproject.roverimages;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class ParseJson {
    private final JSONParser parser;

    public ParseJson() {
        parser = new JSONParser();
    }

    public String getFileAsString(String jsonFilePath) {
        try {
            return Files.readString(new File(jsonFilePath).toPath());
        } catch (IOException e) { System.err.println("error occurred reading file -> " + e.getLocalizedMessage()); return null; }
    }


    public Object getJsonArrayItem(String jsonArray, int index) {
        try {
            JSONArray arr = (JSONArray) parser.parse(jsonArray);
            Object item = arr.get(index);
            if(item instanceof JSONArray || item instanceof JSONObject) {
                return item.toString();
            } else {
                return item;
            }
        } catch (ParseException e) {
            System.err.println("problem parsing");
            return null;
        } catch(IndexOutOfBoundsException e) {
            System.err.println("index out of bounds");
            return null;
        }

    }

    public int getJsonArraySize(String json) {
        try {
            return ((JSONArray) parser.parse(json)).size();
        } catch (ParseException e) { System.err.println("problem parsing array string"); return 0; }
        catch (ClassCastException e) { System.err.println("provided string is not an array"); return 0; }
    }


    public Object[] getJsonObjectValues(String json, String... keys) {
        ArrayList<Object> jsonValues = new ArrayList<>();

        try{
            JSONObject responseJson = (JSONObject) parser.parse(json);
            for(int i=0; i<keys.length; i++) {
                Object val = responseJson.get(keys[i]);
                while(val instanceof JSONObject) {
                    JSONObject nestedObj = (JSONObject) parser.parse(val.toString());
                    val = nestedObj.get(keys[i + 1]);
                    i++;
                }
                if(val instanceof JSONArray) {
                    val = val.toString();
                }
                if(val == null)
                    return null;

                jsonValues.add(val);
            }
            return jsonValues.toArray();
        }
        catch (ParseException | NullPointerException e){System.err.println("null pointer or problem parsing"); return null;}
        catch(IndexOutOfBoundsException e) {System.err.println("last argument is object, specify sub-value(s)"); return null;}
    }
}