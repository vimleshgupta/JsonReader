package com.cognizant

import org.apache.commons.io.FileUtils
import org.apache.log4j.Logger

import static ExcelCell.newCell
import static com.cognizant.ExcelFormat.headerFormat
import static com.cognizant.ExcelFormat.verticalCenterFormat

/**
 * Created by guptav on 31/01/16.
 */
class JsonToExcelConverter {

    private static Logger logger = Logger.getLogger(JsonToExcelConverter.class);

    final ExcelWriter excelWriter
    final String path

    final String workbookFileName = 'Report.xls'
    final String sheetName = 'Report Worksheet'
    final List headers = ["ID","SKU", "Brand", "Model", "Consumer New", "Consumer Upgrade", "Voice New", "Voice Upgrade", "Stock Limited", "Life Cycle",
                          "RRP", "Replacement cost", "Is RRP & RCost equal", "Is listHalf present",
                          "Relationship type", "Id/path", "Price one-off", "Price monthly", "Is Price & RRP equal","ModelFamily"]

    JsonToExcelConverter(String path) {
        this.path = path
        this.excelWriter = new ExcelWriter(workbookFileName, sheetName)
    }

    public void processJsonFeed() {

        logger.info("Start processing json files")
        excelWriter.writeHeaders(headers, headerFormat)

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
                            newCell(0, data.id, verticalCenterFormat),
                            newCell(1, data.sku.code, verticalCenterFormat),
                            newCell(2, data.brand, verticalCenterFormat),
                            newCell(3, data.model, verticalCenterFormat),
                            newCell(4, data.channelPermissions.ConsumerNew, verticalCenterFormat),
                            newCell(5, data.channelPermissions.ConsumerUpgrade, verticalCenterFormat),
                            newCell(6, data.channelPermissions.VoiceNew, verticalCenterFormat),
                            newCell(7, data.channelPermissions.VoiceUpgrade, verticalCenterFormat),
                            newCell(8, getResult(data.stockLimited), verticalCenterFormat),
                            newCell(9, data.lifecycle.status, verticalCenterFormat),
                            newCell(10, data.rrp, verticalCenterFormat),
                            newCell(11, data.replacementCost, verticalCenterFormat),
                            newCell(12, getResult(data.replacementCost == data.rrp), verticalCenterFormat),
                            newCell(13, data.id == data.leadModelInFamily ? getResult(data.images.standard.listHalf) : "Not a lead model in family", verticalCenterFormat),
                            newCell(19, data.modelFamily, verticalCenterFormat),
                    ],
                    subRows : []
            ]

            relationships.each {
                def subRow = [
                        newCell(14, it.type),
                        newCell(15, it.id),
                        newCell(16, it.prices.oneOff.toString()),
                        newCell(17, it.prices[0].monthly ? it.prices.monthly.toString() : "NA"),
                        newCell(18, it.id.contains("prepaySims") ? getResult(it.prices[0].oneOff == data.rrp) : "Not a prepay sims")
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