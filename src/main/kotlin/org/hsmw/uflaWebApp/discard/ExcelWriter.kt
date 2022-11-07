package org.hsmw.uflaWebApp.discard

import org.hsmw.uflaWebApp.Main
import hsmw.config.Configurations
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFFont
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileOutputStream


class ExcelWriter {
    private val workbook: XSSFWorkbook = XSSFWorkbook()

    private val customStyle = workbook.createCellStyle()
    private val fatFont = createFatFont()

    private fun createFatFont(): XSSFFont {
        val returnFont = workbook.createFont()
        returnFont.bold = true
        returnFont.italic = false
        return returnFont
    }


    // given a List of Pairs, the sheetName and a Boolean this function creates a Sheet in an excel workbook and filling the values
    fun transmitToExcelFile(arrayOfCopied: List<Pair<String, Double>>, sheetName: String, isRegionQuery: Boolean) {
        val cellStyle = workbook.createCellStyle()
        cellStyle.fillBackgroundColor = IndexedColors.GREEN.index
        cellStyle.fillPattern = FillPatternType.BIG_SPOTS

        val newSheetName = sheetName.replace(
            "${DBConnection().dbName}.${Configurations().region.getAttribute("Identifier")}",
            ""
        )

        val querySheet: XSSFSheet = workbook.createSheet()

        try {
            workbook.setSheetName(workbook.getSheetIndex(querySheet), newSheetName)
        } catch (e: Exception) {
            Main.discardController.errorThrown = true
            //TODO
            println(e.message)
        }
        querySheet.createRow(0).createCell(1).setCellValue("Reihenfolge der Entlastung")
        querySheet.getRow(0).createCell(2).setCellValue("Bezeichnung")
        if (isRegionQuery) {
            querySheet.getRow(0).createCell(3).setCellValue("Trafo")
            querySheet.getRow(0).createCell(4).setCellValue("Leistung")
        } else {
            querySheet.getRow(0).createCell(3).setCellValue("Leistung")
        }
        customStyle.setFont(fatFont)
        querySheet.getRow(0).rowStyle = customStyle

        for (i in arrayOfCopied.indices) {
            querySheet.createRow(i + 1).createCell(1).setCellValue((i + 1).toString())
            querySheet.getRow(i + 1).createCell(2).setCellValue(splitPairOnFirst(arrayOfCopied[i].first)[0])
            if (isRegionQuery) {
                querySheet.getRow(i + 1).createCell(3).setCellValue(splitPairOnFirst(arrayOfCopied[i].first)[1])
                querySheet.getRow(i + 1).createCell(4).setCellValue(arrayOfCopied[i].second)
            } else {
                querySheet.getRow(i + 1).createCell(3).setCellValue(arrayOfCopied[i].second)
            }
        }
    }

    private fun splitPairOnFirst(arrayOfCopied: String): List<String> {
        return arrayOfCopied.split(":")
    }

    fun saveAs(savePath: String) {
        val newSavePath = if (!savePath.endsWith(".xlsx", false)) "$savePath.xlsx" else savePath
        workbook.write(FileOutputStream(newSavePath))

        //TODO
    }

}

