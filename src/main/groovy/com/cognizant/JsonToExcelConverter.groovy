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
    final List headers = [
//            "ID",
            "sku",
          "Brand",
           "Model",
            "costToO2",
            "cashPrice",
            "rrpnreplacement",
//            "ModelFamily",
//            "Consumer New",
//            "Consumer Upgrade",
//            "Voice New",
//            "Voice Upgrade",
//            "Stock Limited",
//            "Life Cycle",
//            "RRP",
//            "Replacement cost",
//            "Is RRP & RCost equal",
//            "Is listHalf present",
//            "Relationship type",
           // "productID",
         //   "price",
            "plan",
            "oneoff",
            "monthly",
//            "Is Price & RRP equal"
    ]

    JsonToExcelConverter(String path) {
        this.path = path
        this.excelWriter = new ExcelWriter(workbookFileName, sheetName)
    }

    public void processJsonFeed() {

        logger.info("Start processing json files")
        excelWriter.writeHeaders(headers, headerFormat)

        int count = 1
        def files = getFiles(path + "/device")

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
                         //   newCell(0, data.id, verticalCenterFormat),
                            newCell(0, data.sku.code, verticalCenterFormat),
                            newCell(1, data.brand, verticalCenterFormat),
                            newCell(2, data.model, verticalCenterFormat),
                            newCell(3, data.costToO2, verticalCenterFormat),
                            newCell(4, data.cashPrice, verticalCenterFormat),
                            newCell(5, data.rrp, verticalCenterFormat),

//                            newCell(5, data.channelPermissions.ConsumerNew, verticalCenterFormat),
//                            newCell(6, data.channelPermissions.ConsumerUpgrade, verticalCenterFormat),
//                            newCell(7, data.channelPermissions.VoiceNew, verticalCenterFormat),
//                            newCell(8, data.channelPermissions.VoiceUpgrade, verticalCenterFormat),
//                            newCell(9, getResult(data.stockLimited), verticalCenterFormat),
//                            newCell(10, data.lifecycle.status, verticalCenterFormat),
//                            newCell(5, data.rrp, verticalCenterFormat),
//                            newCell(6, data.replacementCost, verticalCenterFormat),
//                            newCell(7, getResult(data.replacementCost == data.rrp), verticalCenterFormat),
//                            newCell(8, data.id == data.leadModelInFamily ? getResult(data.images.standard.listHalf) : "Not a lead model in family", verticalCenterFormat)
                    ],
                    subRows : []
            ]

            try {
                relationships.each {
                  //  def planDetails = getPlanDetails(it.id)
                    def subRow = [
                          //  newCell(4, planDetails.productID),
                          //  newCell(5, planDetails.price),
                            newCell(6, it.id),
                            newCell(7, it.prices.oneOff.toString().replaceAll(" ", "")),
                            newCell(8, it.prices[0].monthly ? it.prices.monthly.toString().replaceAll(" ", "") : "NA"),
                          //  newCell(16, it.prices[0].monthly ? it.prices.monthly.toString() : "NA"),
                            //   newCell(9, it.id.contains("prepaySims") ? getResult(it.prices[0].oneOff == data.rrp) : "Not a prepay sims")
                    ]
                    excelRows.subRows << subRow
                }

            } catch (FileNotFoundException e) {
                println file.getAbsolutePath()
            }
         //   if (data.model.toLowerCase().contains("s6 edge 32gb"))
            if(data.modelFamily == "Lumia 640")
            excelWriter.writeEntireRows(excelRows)
        }

        excelWriter.writeNotes(newCell(0, "Total file processed: $count", headerFormat))
        excelWriter.close()

        logger.info("End processing json files")
    }

    Map getPlanDetails(String planId) {
        if (planId.startsWith("/plan")) {
            File file = new File(path + "/" + planId)
            Map data = JsonParser.parseJson(file, file.absolutePath)
            [productID: data.productID, price: data.price?:data.topUpAmount]
        } else {
            [productID: "", price: ""]
        }
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