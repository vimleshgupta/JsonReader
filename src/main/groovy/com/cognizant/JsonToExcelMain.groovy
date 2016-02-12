package com.cognizant;

/**
 * Created by guptav on 11/02/16.
 */
public class JsonToExcelMain {

    public static void main(String[] args) {
        if (!args) {
            throw new RuntimeException("Please specify product calalogue path.")
        }

        JsonToExcelConverter excelConverter = new JsonToExcelConverter(args[0])
        excelConverter.processJsonFeed()

    }
}
