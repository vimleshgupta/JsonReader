package com.cognizant

import groovy.json.JsonSlurper
import jxl.format.VerticalAlignment
import jxl.write.Label
import jxl.write.WritableCellFormat
import jxl.write.WritableFont
import org.apache.commons.io.FileUtils

import java.util.regex.Matcher
import java.util.regex.Pattern

import static jxl.Workbook.createWorkbook

/**
 * Created by guptav on 31/01/16.
 */
class JsonToExcelConverter {

    public static void main(String[] args) {

        def location = getLocation(args)
        WritableCellFormat headerFormat = new WritableCellFormat();
        WritableFont headerFont = new WritableFont(WritableFont.COURIER, 10, WritableFont.BOLD);
        headerFormat.setFont(headerFont)

        WritableCellFormat verticalFormat = new WritableCellFormat();
        verticalFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

        def workbookFilename = 'Report.xls'

        def workbook = createWorkbook(new File(workbookFilename))
        def sheet = workbook.createSheet('Report Worksheet', 0)

        def header = ["SKU", "Brand", "Model", "Consumer New", "Consumer Upgrade", "Voice New", "Voice Upgrade", "Stock limited","Life Cycle",
                      "RRP", "Replacement cost", "Is RRP & RCost equal", "Is listHalf present",
                      "Relationship type", "Id/path", "Price one-off", "Price monthly", "Is Price & RRP equal"]

        header.eachWithIndex { it, index ->
            sheet.addCell new Label(index, 0, it, headerFormat)
        }

        File file = new File(location)
        int row = 1
        def fileList
        if (file.isDirectory()) {
             fileList = FileUtils.listFiles(file, ["json"] as String[], true)
        } else {
            fileList = [file]
        }

        int count = 0
        for (f in fileList) {
            def data = getData(f, f.absolutePath)
            if (!data) {
                continue;
            }
            count++
            def relationships = data.relationships.findAll {
                return it.type == "plan"
            }

            def size = relationships.size()

            (0..11).each {
                sheet.mergeCells(it, row, it, row + size - 1)
            }

            sheet.addCell(new Label(0, row, data.sku.code, verticalFormat))
            sheet.addCell(new Label(1, row, data.brand, verticalFormat))
            sheet.addCell(new Label(2, row, data.model, verticalFormat))
            sheet.addCell(new Label(3, row, data.channelPermissions.ConsumerNew, verticalFormat))
            sheet.addCell(new Label(4, row, data.channelPermissions.ConsumerUpgrade, verticalFormat))
            sheet.addCell(new Label(5, row, data.channelPermissions.VoiceNew, verticalFormat))
            sheet.addCell(new Label(6, row, data.channelPermissions.VoiceUpgrade, verticalFormat))
            sheet.addCell(new Label(7, row, getResult(data.stockLimited), verticalFormat))
            sheet.addCell(new Label(8, row, data.lifecycle.status, verticalFormat))
            sheet.addCell(new Label(9, row, data.rrp, verticalFormat))
            sheet.addCell(new Label(10, row, data.replacementCost, verticalFormat))
            sheet.addCell(new Label(11, row, getResult(data.replacementCost == data.rrp), verticalFormat))
            sheet.addCell(new Label(12, row, data.id == data.leadModelInFamily ? getResult(data.images.standard.listHalf) : "Not a lead model in family", verticalFormat))

            relationships.eachWithIndex { it, index ->
                sheet.addCell(new Label(13, index + row, it.type))
                sheet.addCell(new Label(14, index + row, it.id))
                sheet.addCell(new Label(15, index + row, it.prices.oneOff.toString()))
                sheet.addCell(new Label(16, index + row, it.prices[0].monthly ? it.prices.monthly.toString() : "NA"))
                sheet.addCell(new Label(17, index + row, it.id.contains("prepaySims") ? getResult(it.prices[0].oneOff == data.rrp) : "Not a prepay sims"))
            }
            row += size
        }
        sheet.addRowPageBreak(row + 2)
        sheet.addCell(new Label(0, row + 4, "Total file processed: $count", headerFormat))
        workbook.write()
        workbook.close()
    }

    static Object getLocation(String[] args) {
        if (args) {
            args[0]
        } else {
            throw new RuntimeException("please run with product calalogue path")
        }
    }

    static Object getData(File dataFile, String filePath) {
        def reader = new FileReader(dataFile)
        Map savedStrings = [:]

        StringBuffer sb = new StringBuffer()
        reader.eachLine { String line ->
            sb.append(line)
        }

        def string = sb.toString()

        def regex = "(\\\$[^\\}]*\\})"
        Pattern pattern = Pattern.compile(regex)
        Matcher matcher = pattern.matcher(string)

        while(matcher.find()) {
            def existingString = matcher.group(1)
            if (existingString.contains("idOf")) {
                def newString = "\"" + existingString.substring(8, existingString.length() - 3) + "\""
                string = string.replace(existingString, newString)
            } else {
                String random = "\"RANDOM-" + new Random().nextInt().toString() + "\""
                savedStrings[random] = existingString
                string = string.replace(existingString, random)
            }
        }

        string = string.replaceAll("\ufeff", "")

        def jsonObject = null
        try {
            jsonObject = new JsonSlurper().parseText(string)
        } catch (Exception e) {
            println "unable to parse json file: " + filePath
        }
        jsonObject
    }

    private static String getResult(data) {
        data ? "Yes" : "No"
    }
}
