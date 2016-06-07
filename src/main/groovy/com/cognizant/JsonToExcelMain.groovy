package com.cognizant;

/**
 * Created by guptav on 11/02/16.
 */
public class JsonToExcelMain {

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new RuntimeException("Please specify product calalogue path and excel sheet path")
        }

        JsonToExcelConverter excelConverter = new JsonToExcelConverter(args[0], args[1])
        excelConverter.processJsonFeed()

    }
}
