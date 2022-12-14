package org.hsmw.uflaWebApp.ufla

import org.hsmw.uflaWebApp.Main
import org.hsmw.uflaWebApp.Reader
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.w3c.dom.Element
import java.io.FileInputStream

class Monitoring(templateConfig: Element, monitoringConfig : Element) : Reader {

    private val templateC : Element = templateConfig
    private val config : Element = monitoringConfig

    data class MonitoringCell(
        val timeStamp: String,
        val value: Double
    )
    /*
      creates a XSSFWorkbook to calculate the active UFLA
      @param name: path of input File
   */
    fun start(name: String) {
        val workbook = Reader.createWorkbook(FileInputStream(name))
        try {
            fillMonitoring(workbook)
        } catch (e: Exception) {
            Main.controllerK.reportError(e.message.toString() + Main.controllerK.textStrings[15])
        }
    }

    /*
    creates a listOf(listOf(MonitoringCells))
    fills the template with copied Data
    @param workbook: input
     */
    private fun fillMonitoring(workbook: XSSFWorkbook) {
        val monitoringDataForEachSheet: MutableList<MutableList<MonitoringCell>> = mutableListOf(mutableListOf())
        workbook.forEach { sheet ->
            val listOfTimestampValuePairs: MutableList<MonitoringCell> = mutableListOf()
            val calcCol = findSumCol(sheet.getRow(0))
            sheet.asSequence()
                .drop(config.getAttribute("HeaderRows").toInt())
                .forEach sheetIteration@{ row ->
                    if(calcCol == -1) return@forEach
                    listOfTimestampValuePairs.add(getTimeStamp(row, calcCol))
                }
            monitoringDataForEachSheet.add(listOfTimestampValuePairs)
        }
        fillTemplate(monitoringDataForEachSheet)
        monitoringDataForEachSheet.clear()
    }

    /*
        returns timeStamp: date time to value: monitored value for specific time
     */
    private fun getTimeStamp(row: Row, col: Int): MonitoringCell {
        if (col == -1)
            println("you failed getTimeStamp ${row.rowNum}")
        return MonitoringCell(row.getCell(1).toString(), row.getCell(col).numericCellValue)
    }

    /*
        searches for Regex declared in Config
        returns the column as integer
     */
    private fun findSumCol(row: Row): Int {
        row.forEachIndexed { index, cell ->
            if (cell.toString().lowercase() == config.getAttribute("HeaderString").lowercase())
                return index
        }
        return -1
    }


    /*
        fills the template Sheet with data
     */
    private fun fillTemplate(monitoringDataForEachSheet: List<List<MonitoringCell>>) {
        val toFill = findSheet(Main.controllerK.copiedTemplate)
        val skipper = templateC.getAttribute("HeaderRowsMonitoring").toInt()
        monitoringDataForEachSheet.forEachIndexed { index, element ->
            element.forEachIndexed { eleIndex, elem ->
                setCellValue(index, skipper + eleIndex, elem.value, toFill)
            }
        }
    }
    /*
        @param copiedTemplate : template Workbook
        returns the XSSFSheet in template that has to be filled or null
    */
    private fun findSheet(copiedTemplate: XSSFWorkbook): XSSFSheet? {
        copiedTemplate.forEach { sheet ->
            if (sheet.sheetName.equals(config.getAttribute("NameInTemplate")))
                return sheet as XSSFSheet
        }
        return null
    }

    /*
        sets the Value for the specific cell in template sheet
        @param listIndex: Index of current iterated list
        @param rowCounter: Index of current iterated row
        @param value: value to be set in Cell
        @param sheet: sheet to be filled with data
     */
    private fun setCellValue(listIndex: Int, rowCounter: Int, value: Double, sheet: Sheet?) {
        val colToBeFilled = templateC.getAttribute("ColToBeFilledMonitoring").toInt() - 1

        if (sheet == null)
            throw Exception(Main.controllerK.textStrings[22])
        val cellToBeFilled = sheet.getRow(rowCounter).getCell(colToBeFilled + listIndex)
        cellToBeFilled.setCellValue(cellToBeFilled.numericCellValue + value)
        }
}