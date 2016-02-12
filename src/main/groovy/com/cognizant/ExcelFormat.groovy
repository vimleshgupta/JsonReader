package com.cognizant

import jxl.format.VerticalAlignment
import jxl.write.WritableCellFormat
import jxl.write.WritableFont

/**
 * Created by guptav on 11/02/16.
 */
class ExcelFormat {

    public static final WritableCellFormat headerFormat
    public static final WritableCellFormat verticalCenterFormat


    static {
        headerFormat = new WritableCellFormat()
        headerFormat.setFont(new WritableFont(WritableFont.COURIER, 10, WritableFont.BOLD))

        verticalCenterFormat = new WritableCellFormat()
        verticalCenterFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
    }
}
