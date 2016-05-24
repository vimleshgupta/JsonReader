package com.cognizant

import groovy.json.JsonSlurper

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by guptav on 11/02/16.
 */
public class JsonParser {

    private static JsonReaderLogger logger = JsonReaderLogger.getLogger()

    static Object parseJson(File dataFile, String filePath) {

        def reader = new BufferedReader(new FileReader(dataFile))
        Map savedStrings = [:]

        StringBuffer sb = new StringBuffer()
        reader.eachLine { String line ->
            sb.append(line)
        }

        def data = sb.toString()

        def regex = "(\\\$[^\\}]*\\})"
        Pattern pattern = Pattern.compile(regex)
        Matcher matcher = pattern.matcher(data)

        while(matcher.find()) {
            def existingString = matcher.group(1)
            if (existingString.contains("idOf")) {
                existingString = existingString.replaceAll(" ", "")
                def newString = "\"" + existingString.substring(8, existingString.length() - 3) + "\""
                data = data.replace(existingString, newString)
            } else {
                String random = "\"RANDOM-" + new Random().nextInt().toString() + "\""
                savedStrings[random] = existingString
                data = data.replace(existingString, random)
            }
        }

        data = data.replaceAll("\ufeff", "")

        def jsonObject = null
        try {
            jsonObject = new JsonSlurper().parseText(data)
            logger.info("File parsed successfully: " + filePath)
        } catch (Exception ex) {
            logger.error("Unable to parse json file: " + filePath, ex)
        }
        jsonObject
    }
}
