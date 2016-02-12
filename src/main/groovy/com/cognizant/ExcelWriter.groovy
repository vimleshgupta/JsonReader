package com.cognizant

import jxl.write.Label
import jxl.write.WritableCellFormat
import jxl.write.WritableSheet
import jxl.write.WritableWorkbook

import static jxl.Workbook.createWorkbook

/**
 * Created by guptav on 11/02/16.
 */
class ExcelWriter {

    final WritableWorkbook workbook
    final WritableSheet sheet

    int rowIndex = 0
    ExcelWriter(String workbookFilename, String sheetName) {
        this.workbook = createWorkbook(new File(workbookFilename))
        this.sheet = workbook.createSheet(sheetName, 0)

    }

    void writeHeaders(List<String> headers, WritableCellFormat format) {

        headers.eachWithIndex { it, index ->
            sheet.addCell new Label(index, rowIndex, it, format)
        }
        rowIndex++
    }

    def writeNotes(Cell cell) {
        sheet.addRowPageBreak(rowIndex)
        sheet.addCell(new Label(cell.colIndex, rowIndex + 2, cell.data, cell.format))
        rowIndex += 2
    }

    def writeEntireRows(Map rows) {
        margeCells(rows.mainRow.size, rows.subRows.size)
        rows.mainRow.each {
            sheet.addCell(new Label(it.colIndex, rowIndex, it.data, it.format))
        }
        rows.subRows.eachWithIndex { subRow, index ->
            subRow.each { it ->
                sheet.addCell(new Label(it.colIndex, index + rowIndex, it.data, it.format))
            }
        }
        rowIndex += rows.subRows.size()
    }

    void close() {
        workbook.write()
        workbook.close()
    }

    void margeCells(int columns, int rowsForEachColumn) {
        (0..columns-1).each {
            sheet.mergeCells(it, rowIndex, it, rowIndex + rowsForEachColumn - 1)
        }
    }
}
