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

    /**
     * Gets the String representation of a JSON file
     * @param jsonFilePath File path to JSON file
     * @return JSON as a String, or null if IOException occurs
     */
    public String getFileAsString(String jsonFilePath) {
        try {
            return Files.readString(new File(jsonFilePath).toPath());
        } catch (IOException e) { System.err.println("error occurred reading file -> " + e.getLocalizedMessage()); return null; }
    }

    /**
     * Gets the item in the given array String at the index specified.
     * Note: JSONArray and JSONObject types returned as Strings
     * @param jsonArray The JSON array in String format
     * @param index Array index of item to retrieve
     * @return Primitive type if item is a value, JSONArray/JSONObject for nested objects and arrays, or
     * null if ParseException or IndexOutOfBoundsException occurs
     */
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

    /**
     * Gets the size/length of a given JSON array String
     * @param json JSON array as a String
     * @return The size/length of the given JSON array, or 0 if ParseException or ClassCastException is
     * thrown
     */
    public int getJsonArraySize(String json) {
        try {
            return ((JSONArray) parser.parse(json)).size();
        } catch (ParseException e) { System.err.println("problem parsing array string"); return 0; }
        catch (ClassCastException e) { System.err.println("provided string is not an array"); return 0; }
    }

    /**
     * Gets a set of values using the provided keys from the JSON String. The returned values will
     * correspond to each key that was passed in the order they were passed. Note: Keys for any nested
     * objects must precede the intended key to look up. Last key cannot be a nested object, only a value,
     * or nested array
     *
     * For example, to get the value for item_1, call
     * getJsonObjectValues("main_object", "nested_object", "item_1")[0]
     *
     * "main_object": {
     *      "nested_object": {
     *          "item_1": true
     *      }
     * }
     * Any nested arrays will be returned as a String, use {@link #getJsonArrayItem(String, int)} to fetch
     * a specific item from the array
     * @param json The JSON to parse as a String
     * @param keys A variable length of keys of the key-value pairs to look up. For nested objects,
     *             precede required key to look up with parent object keys
     * @return An Object[] array containing the JSON values. This may vary in length from the amount of keys
     * passed. JSONArray in String format if value is a nested array. Method will return null if:
     *      - ParseException, NullPointerException or IndexOutOfBoundsException occurs
     *      - One or more keys does not exist
     */
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