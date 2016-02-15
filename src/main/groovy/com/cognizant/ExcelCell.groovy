package com.cognizant

import jxl.format.CellFormat
import jxl.write.WritableWorkbook

/**
 * Created by guptav on 11/02/16.
 */
class ExcelCell {
    int colIndex
    String data
    CellFormat format = WritableWorkbook.NORMAL_STYLE

    def static newCell(int colIndex, String data) {
        new ExcelCell(colIndex: colIndex, data: data, format: WritableWorkbook.NORMAL_STYLE)
    }

    def static newCell(int colIndex, String data, CellFormat format) {
        new ExcelCell(colIndex: colIndex, data: data, format: format)
    }
}
