package com.cognizant

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.io.IOContext
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by guptav on 11/02/16.
 */
public class JsonParser {

    private static JsonReaderLogger logger = JsonReaderLogger.getLogger()

    def static parseJson(File dataFile, String filePath) {

        ObjectMapper objectMapper = new ObjectMapper()

        Map savedStrings = [:]
        def data = dataFile.text

        def regex = "(\\\$[^\\}]*\\})"
        Pattern pattern = Pattern.compile(regex)
        Matcher matcher = pattern.matcher(data)

        while(matcher.find()) {
            def existingString = matcher.group(1)
            if (existingString.contains("idOf")) {
                def newString = "\"" + existingString.substring(8, existingString.length() - 3) + "\""
                savedStrings[newString] = existingString
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
            jsonObject = objectMapper.readValue(data, Map)
            logger.info("File parsed successfully: " + filePath)
        } catch (Exception ex) {
            logger.error("Unable to parse json file: " + filePath, ex)
        }
        [jsonObject, savedStrings]
    }
}


class Factory extends JsonFactory {
    @Override
    protected JsonGenerator _createGenerator(Writer out, IOContext ctxt) throws IOException {
        return super._createGenerator(out, ctxt).setPrettyPrinter(getPrettyPrinter());
    }

    def getPrettyPrinter () {
        DefaultPrettyPrinter defaultPrettyPrinter = new DefaultPrettyPrinter ()
        defaultPrettyPrinter.withArrayIndenter(DefaultPrettyPrinter.Lf2SpacesIndenter.instance)
    }
}