package com.cognizant

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import jxl.Sheet
import jxl.Workbook
import org.apache.commons.io.FileUtils

/**
 * Created by guptav on 14/02/16.
 */
class JsonUpdater {

    public static void main(String[] args) {

        // /home/guptav/projects/o2/productCatalogueData_Master/catalogueData

        if (args.length < 3) {
            println "please provide 3 arguments"
            println "1) Catalogue path 2) Updater sheet path 3) Update smartly"
            return
        }

        File[] lists = FileUtils.listFiles(new File(args[0]), ["json"] as String[], true)
        Workbook workbook = Workbook.getWorkbook(new File(args[1]))
        def updateSmartly = Boolean.getBoolean(args[2])

        Sheet sheet = workbook.getSheet(0)

        def rows = getAllRows(sheet)

        def newRows = [:]

        rows.each { row ->
            def skus = row.sku.split(",")
            println skus
            skus.each {
                newRows.put(it, row)
            }
        }

        println "rows found - ${newRows.size()}"
        for (file in lists) {
            def (Map data, Map savedStrings) = JsonParser.parseJson(file, file.absolutePath)

            if (!data || !data.sku) {
                continue
            }
            def entry = newRows.find { key, value ->
                key == data.sku.code
            }

            if (entry) {
                def row = entry.value
            //    println "sku - $data.sku found."
                data.costToO2 = row.costToO2 as String
                data.cashPrice = row.cashPrice as String
                data.rrp = row.rrpnreplacement as String
                data.replacementCost = row.rrpnreplacement as String

                if (updateSmartly) {
                    def otherRelationship = data.relationships.findAll {
                        it.type != "plan"
                    }
                    def newOrUpdatedRelationship = []
                    row.plans.each { plan ->
                        newOrUpdatedRelationship << addOrUpdatePlans(plan, data, file, savedStrings)
                    }
                    data.relationships = newOrUpdatedRelationship + otherRelationship
                } else {
                    row.plans.each { plan ->
                        updatedPlans(plan, data, file)
                    }
                }

                ObjectMapper stringMapper = new ObjectMapper( new Factory())
                stringMapper.configure(SerializationFeature.INDENT_OUTPUT, true)
                StringWriter result = new StringWriter()
                stringMapper.writeValue(result, data)
                def jsonData = result.toString()

                savedStrings.each {String key, String value ->
                    String replaceString = key
                    jsonData = jsonData.replace(replaceString, value)
                }

                file.withWriter { Writer writer ->
                    writer.write(jsonData)
                }

            } else {
                continue
            }
            newRows.remove(entry)
            if (newRows.isEmpty()) {
                break
            }
        }
        println("Success!!!")
    }

    private static Map addOrUpdatePlans(def plan, def data, File file, def savedStrings) {

        def updatedRelationship = updatedPlans(plan, data, file)
        if (updatedRelationship) {
            return updatedRelationship
        } else {
            println "added $plan in json file - $file.absolutePath"
            def relationship = [
                    type  : "plan",
                    id    : plan.plan,
                    prices: []
            ]

            savedStrings["\"$plan.plan\""] = "\${idOf('${relationship.id}')}"

            try {
                if (plan.oneoff != "NA" && plan.monthly != "NA") {
                    def oneOffPrices = plan.oneoff.substring(1, plan.oneoff.length() - 1).split(",")
                    def monthlyPrices = plan.monthly.substring(1, plan.monthly.length() - 1).split(",")
                    oneOffPrices.eachWithIndex { it, index ->
                        def oneoff = it as String
                        def monthly = monthlyPrices[index] as String
                        relationship.prices << [
                                oneOff : (oneoff),
                                monthly: (monthly)
                        ]
                    }
                }

                if (plan.oneoff != "NA" && plan.monthly == "NA") {
                    def oneOffPrices = plan.oneoff.substring(1, plan.oneoff.length() - 1).split(",")
                    oneOffPrices.eachWithIndex { it, index ->
                        def oneoff = it as String
                        relationship.prices << [
                                oneOff : (oneoff)
                        ]
                    }
                }
            } catch (Exception ex) {
                println "found exception while updating plan - $plan"
                ex.printStackTrace()
            }
            return relationship
        }
    }

    private static Map updatedPlans(def plan, def data, def file) {
        def relationship = data.relationships.find {
            it.type == "plan" && it.id == plan.plan
        }

        //  println "plan found - $plan.plan"
        if (!relationship || (plan.oneoff == "NA" && plan.monthly == "NA")) {
            println "$plan not found in json file - $file.absolutePath"
            return null
        } else {
            try {
                if (plan.oneoff != "NA" && plan.monthly != "NA") {
                    def oneOffPrices = plan.oneoff.substring(1, plan.oneoff.length() - 1).split(",")
                    def monthlyPrices = plan.monthly.substring(1, plan.monthly.length() - 1).split(",")
                    if (relationship.prices) {
                        oneOffPrices.eachWithIndex { it, index ->
                            relationship.prices[index].oneOff = it as String
                            relationship.prices[index].monthly = monthlyPrices[index] as String
                        }
                    } else {
                        relationship.prices = []
                        oneOffPrices.eachWithIndex { it, index ->
                            def oneoff = it as String
                            def monthly = monthlyPrices[index] as String
                            relationship.prices << [
                                    oneOff : (oneoff),
                                    monthly: (monthly)
                            ]
                        }
                    }
                }

                if (plan.oneoff != "NA" && plan.monthly == "NA") {
                    def oneOffPrices = plan.oneoff.substring(1, plan.oneoff.length() - 1).split(",")
                    if (relationship.prices) {
                        oneOffPrices.eachWithIndex { it, index ->
                            relationship.prices[index].oneOff = it as String
                        }
                    } else {
                        relationship.prices = []
                        oneOffPrices.eachWithIndex { it, index ->
                            def oneoff = it as String
                            relationship.prices << [
                                    oneOff: (oneoff)
                            ]
                        }
                    }
                }
            } catch (Exception ex) {
                println "found exception while updating plan - $plan"
                ex.printStackTrace()
            }
        }
        relationship
    }

    static getAllRows(Sheet sheet) {

        def rows = []

        def headers = []
        (0..sheet.columns-1).each { column ->
            headers << sheet.getCell(column, 0).contents
        }

        for (def i = 1; i < sheet.rows ; i++) {
            def sku = [:]
            sku.sku = sheet.getCell(0, i).contents
            if (!sku.sku) {
                continue
            }
            sku.plans = []
            (1..3).each { column ->
                sku."${headers[column]}" = "${sheet.getCell(column, i).contents}"
            }
            while ((i < sheet.rows && sheet.getCell(4, i).contents != "")){
                sku.plans << [
                        (headers[4]) : sheet.getCell(4, i).contents,
                        (headers[5]) : sheet.getCell(5, i).contents,
                        (headers[6]) : sheet.getCell(6, i).contents
                ]
                i++
            }
            rows << sku
        }
        rows
    }
}
