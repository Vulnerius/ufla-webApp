package org.hsmw.uflaWebApp.ufla

import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.hsmw.uflaWebApp.Reader
import org.hsmw.uflaWebApp.config.Configurations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.w3c.dom.Element
import java.io.FileInputStream
import kotlin.properties.Delegates

@Component
class Reporting @Autowired constructor(private val config: Configurations) {

    private var sheetPower by Delegates.notNull<Double>()
    private var flawsPerFile by Delegates.notNull<Int>()

    private val reportingConfig: Element = config.reportingConfig
    private val templateConfig: Element = config.templateConfig
    private lateinit var uflaController: UFLAController
    private lateinit var messages: MutableList<String>

    data class ReportingCell(
        val col: Int,
        val row: Int,
    )

    /*
        creates a XSSFWorkbook to calculate the active UFLA
        @param name: path of input File
     */
    fun start(name: String) {
        val workbook = Reader.createWorkbook(FileInputStream(name))
        sheetPower = .0
        flawsPerFile = 0
        try {
            calcReleasePerformance(workbook)
        } catch (e: Exception) {
            uflaController.reportError(e.message.toString() + messages[15])
        }
    }

    /*
        evaluates a list of Pairs<String, Double>
            String: frequency - Double: UFLA
        adds up the values of UFLA in @param sheetPower
        adds text to Console in UI if UFLA value is <= 0 or > 0 but not in calculation
        @param wb: XSSFWorkbook - apache poi workbook - Excel file
     */
    private fun calcReleasePerformance(wb: XSSFWorkbook) {
        val frequencyAndSum: MutableList<Pair<String, Double>> = mutableListOf()
        wb.forEach { sheet ->
            val powerColumn = findPowerColumn(sheet as XSSFSheet)
            sheet.asSequence()
                .forEach calc@{ row ->
                    if (row == null) return@calc
                    if (valueIsInUFLACalculation(row)
                        && getPowerValue(row, powerColumn.col) <= 0
                    ) {
                        uflaController.reportFlaw(
                            messages[16] + "${sheet.sheetName}:row${row.rowNum + 1}"
                        )
                        flawsPerFile++
                    } else if (valueIsInUFLACalculation(row) && getPowerValue(row, powerColumn.col) > 0) {
                        if (findFrequency(sheet, row) == "")
                            uflaController.reportFlaw(
                                messages[17] +
                                        "${sheet.sheetName}, ${row.rowNum + 1}: ${getPowerValue(row, powerColumn.col)}"
                            )
                        frequencyAndSum.add(
                            Pair(
                                findFrequency(sheet, row),
                                getPowerValue(row, powerColumn.col)
                            )
                        )
                        sheetPower += getPowerValue(row, powerColumn.col)
                    }
                }
        }
        fillTemplate(frequencyAndSum)
        frequencyAndSum.clear()
    }

    /*
        @param row : current row of iteration
        @param col : current column of iteration
        returns the found UFLA value
     */
    private fun getPowerValue(row: Row, col: Int): Double {
        return when {
            row.getCell(col) == null -> 0.0
            row.getCell(col).cellType.equals(CellType.FORMULA) -> row.getCell(col).numericCellValue
            row.getCell(col).cellType.equals(CellType.NUMERIC) -> row.getCell(col).numericCellValue
            else -> 0.0
        }
    }

    /*
    @param row: current row of iteration
     returns if the current row is marked with "x" - active in UFLA
     */
    private fun valueIsInUFLACalculation(row: Row): Boolean {
        row.forEach { column ->
            if (column.cellType.equals(CellType.STRING) && column.stringCellValue.lowercase().trim()
                    .matches(Regex("^x$"))
            )
                return true
        }
        return false
    }

    /*
    @param sheet: current sheet of workbook iteration
     returns row and column of the current marked UFLA-value
     */
    private fun findPowerColumn(sheet: XSSFSheet): ReportingCell {
        sheet.asSequence()
            .forEachIndexed { rIdx, row ->
                row.forEachIndexed innerFor@{ cIdx, element ->
                    when {
                        element == null -> return@innerFor
                        element.cellType.equals(CellType.STRING) && element.stringCellValue.lowercase() == reportingConfig.getAttribute(
                            "HeaderString"
                        ).lowercase() -> return ReportingCell(
                            col = cIdx + 1,
                            row = rIdx
                        )

                        else -> return@innerFor
                    }
                }
            }
        return ReportingCell(-1, -1)
    }

    /*
        searches for an x in the row of the current sheet -> x marks active in UFLA
        @param sheet: current Sheet of workbook iteration
        @param row: current row of sheet iteration
        returns the frequency of the current calculated UFLA value
     */
    private fun findFrequency(sheet: XSSFSheet, row: Row): String {
        var freqCol: Int = -1
        row.forEachIndexed { index, column ->
            if (column == null) return@forEachIndexed
            else if (column.toString().lowercase().matches(Regex("^x$")))
                freqCol = index + 1
        }
        if (freqCol == -1) {
            return ""
        }
        sheet.asSequence()
            .drop(reportingConfig.getAttribute("HeaderRows").toInt())
            .forEach { fRow ->
                val freqCell = fRow.getCell(freqCol)
                when {
                    freqCell == null -> return@forEach
                    freqCell.cellType != CellType.STRING -> return@forEach
                    freqCell.stringCellValue.length < 3 -> return@forEach
                    freqCell.stringCellValue.lowercase()
                        .substring(freqCell.stringCellValue.length - 2, freqCell.stringCellValue.length) == "hz"
                    -> return freqCell.stringCellValue.replace(Regex("^f.+="), "")
                        .replace(Regex("Hz$"), "")
                        .replace(",", ".")
                        .trim()
                }
            }
        return ""
    }

    /*
        fills the template with data from evaluated List
        @param reportingData: List of Mapped Frequency-Value
     */
    private fun fillTemplate(reportingData: MutableList<Pair<String, Double>>) {
        val toFill = findSheet(uflaController.copiedTemplate)
        reportingData.forEach { (str, value) ->
            if (str.isNotBlank()) {
                setCellValue(toFill, str, value)
            }
        }
    }

    /*
        @param copiedTemplate : template Workbook
        returns the XSSFSheet in template that has to be filled
     */
    private fun findSheet(copiedTemplate: XSSFWorkbook): XSSFSheet {
        copiedTemplate.forEach { sheet ->
            if (sheet.sheetName.equals(reportingConfig.getAttribute("NameInTemplate")))
                return sheet as XSSFSheet
        }
        return copiedTemplate.getSheetAt(0)
    }

    /*
        @param toFill: template sheet to be filled with data
        @param frequency: frequency for current value to add
        @param activeReleasePerformance: value to be filled in template sheet
        sets the double value in template-sheet
     */
    private fun setCellValue(toFill: XSSFSheet, frequency: String, activeReleasePerformance: Double) {
        if (toFill.sheetName.equals(templateConfig.getAttribute("NameOfFirstSheet"))) throw Exception(messages[18])
        val reportCell = findFrequencyMatch(toFill, frequency)
        if (reportCell.col == -1 && reportCell.row == -1) throw Exception("\t $frequency: " + messages[19])
        toFill.getRow(reportCell.row).getCell(reportCell.col)
            .setCellValue(
                toFill.getRow(reportCell.row).getCell(reportCell.col).numericCellValue
                        + activeReleasePerformance
            )
    }

    /*
        iterates through given sheet in template and
        returns the cell to be filled with UFLA value
     */
    private fun findFrequencyMatch(toFill: XSSFSheet, frequency: String): ReportingCell {
        toFill.asSequence()
            .forEachIndexed { rIdx, row ->
                row.forEachIndexed { cIdx, cell ->
                    if (cell.cellType.equals(CellType.NUMERIC) && cell.numericCellValue == frequency.toDouble()) {
                        return ReportingCell(cIdx + 2, rIdx + 2)
                    }
                }
            }
        return ReportingCell(-1, -1)
    }

    //returns summed UFLA-values
    fun getFilePower(): Double {
        return sheetPower
    }

    //returns active UFLA values <= 0 or > 0 but not in calculation
    fun getInternalFlaws(): Int {
        return flawsPerFile
    }

    fun setController(uflaController: UFLAController) {
        this.uflaController = uflaController
        messages = uflaController.textStrings
    }
}
