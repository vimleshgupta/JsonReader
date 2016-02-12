package com.cognizant

import groovy.json.JsonSlurper
import jxl.format.VerticalAlignment
import jxl.write.Label
import jxl.write.WritableCellFormat
import jxl.write.WritableFont
import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger

import java.util.regex.Matcher
import java.util.regex.Pattern

import static com.cognizant.Cell.newCell
import static com.cognizant.ExcelFormat.headerFormat
import static com.cognizant.ExcelFormat.verticalCenterFormat
import static jxl.Workbook.createWorkbook

/**
 * Created by guptav on 31/01/16.
 */
class JsonToExcelConverter {

    private static Logger logger = Logger.getLogger(JsonToExcelConverter.class);

    final ExcelWriter excelWriter
    final String path

    final String workbookFileName = 'Report.xls'
    final String sheetName = 'Report Worksheet'
    final List headers = ["SKU", "Brand", "Model", "Consumer New", "Consumer Upgrade", "Voice New", "Voice Upgrade", "Stock Limited", "Life Cycle",
                          "RRP", "Replacement cost", "Is RRP & RCost equal", "Is listHalf present",
                          "Relationship type", "Id/path", "Price one-off", "Price monthly", "Is Price & RRP equal"]

    JsonToExcelConverter(String path) {
        this.path = path
        this.excelWriter = new ExcelWriter(workbookFileName, sheetName)
    }

    public void processJsonFeed() {

        logger.info("Start processing json files")
        excelWriter.writeHeaders(headers, headerFormat)

        int row = 1
        int count = 1
        def files = getFiles(path)

        for (file in files) {
            def data = JsonParser.parseJson(file, file.absolutePath)

            if (!data) continue

            count++
            def relationships = data.relationships.findAll {
                return it.type == "plan"
            }

            def size = relationships.size()

            def excelRows = [
                    mainRow : [
                            newCell(0, data.sku.code, verticalCenterFormat),
                            newCell(1, data.brand, verticalCenterFormat),
                            newCell(2, data.model, verticalCenterFormat),
                            newCell(3, data.channelPermissions.ConsumerNew, verticalCenterFormat),
                            newCell(4, data.channelPermissions.ConsumerUpgrade, verticalCenterFormat),
                            newCell(5, data.channelPermissions.VoiceNew, verticalCenterFormat),
                            newCell(6, data.channelPermissions.VoiceUpgrade, verticalCenterFormat),
                            newCell(7, getResult(data.stockLimited), verticalCenterFormat),
                            newCell(8, data.lifecycle.status, verticalCenterFormat),
                            newCell(9, data.rrp, verticalCenterFormat),
                            newCell(10, data.replacementCost, verticalCenterFormat),
                            newCell(11, getResult(data.replacementCost == data.rrp), verticalCenterFormat),
                            newCell(12, data.id == data.leadModelInFamily ? getResult(data.images.standard.listHalf) : "Not a lead model in family", verticalCenterFormat),
                    ],
                    subRows : []
            ]

            relationships.each {
                def subRow = [
                        newCell(13, it.type),
                        newCell(14, it.id),
                        newCell(15, it.prices.oneOff.toString()),
                        newCell(16, it.prices[0].monthly ? it.prices.monthly.toString() : "NA"),
                        newCell(17, it.id.contains("prepaySims") ? getResult(it.prices[0].oneOff == data.rrp) : "Not a prepay sims")
                ]
                excelRows.subRows << subRow
            }

            excelWriter.writeEntireRows(excelRows)
        }

        excelWriter.writeNotes(newCell(0, "Total file processed: $count", headerFormat))
        excelWriter.close()

        logger.info("End processing json files")
    }

    File[] getFiles(String path) {
        File file = new File(path)
        def files = [file]
        if (file.isDirectory()) {
            files = FileUtils.listFiles(file, ["json"] as String[], true)
        }
        files
    }

    String getResult(data) {
        data ? "Yes" : "No"
    }
}