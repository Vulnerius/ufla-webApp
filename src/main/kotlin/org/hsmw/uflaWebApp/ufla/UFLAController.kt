package org.hsmw.uflaWebApp.ufla

import org.hsmw.uflaWebApp.LanguageTexts
import org.hsmw.uflaWebApp.config.Configurations
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

@Service
class UFLAController {
    private var templateFile = File("")
    var textStrings: MutableList<String> =
        LanguageTexts.get(Configurations().getConfigElement("preferences").getAttribute("language"))
    private val monitoring: Monitoring = Monitoring(Configurations().templateConfig, Configurations().monitoringConfig)
    val reporting: Reporting = Reporting(Configurations().templateConfig, Configurations().reportingConfig, textStrings)
    var copiedTemplate = XSSFWorkbook()

    fun writeSaveFile(name: String): Boolean {
        if (name.isBlank()) return false
        if (copiedTemplate.numberOfSheets == 0) {
            reportError(textStrings[30])
            return false
        } else
            if (!name.endsWith(".xlsx", false))
                copiedTemplate.write(FileOutputStream("$name.xlsx"))
            else
                copiedTemplate.write(FileOutputStream(name))
        return true
    }

    fun setCopy() {
        if (checkValidTemplate(XSSFWorkbook(FileInputStream(templateFile))))
            copiedTemplate = XSSFWorkbook(FileInputStream(templateFile))
        else
            reportError(textStrings[22])
    }

    fun setReportingFile(file: String) {
        reporting.start(file)
    }

    fun setMonitoringFile(file: String) {
        monitoring.start(file)
    }

    fun canEvaluate(): Boolean {
        return templateFile.canRead()
    }

    fun reportFlaw(flaw: String) {
        //TODO
    }

    fun reportError(error: String) {
        //TODO
    }

    private fun checkValidTemplate(workbook: XSSFWorkbook): Boolean {
        workbook.forEach { sheet ->
            val sheetName = sheet.sheetName
            if (sheetName == Configurations().reportingConfig.getAttribute("NameInTemplate")
                || sheetName == Configurations().monitoringConfig.getAttribute("NameInTemplate")
            )
                return true
        }
        return false
    }
}
