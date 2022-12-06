package org.hsmw.uflaWebApp.ufla

import org.hsmw.uflaWebApp.LanguageTexts
import org.hsmw.uflaWebApp.config.Configurations
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import kotlin.math.log

@Service
class UFLAController @Autowired constructor(private val monitoring: Monitoring, private val reporting: Reporting) {
    private var templateFile = File("")
    final var textStrings: MutableList<String> =
        LanguageTexts.get(Configurations().getConfigElement("preferences").getAttribute("language"))
    var copiedTemplate = XSSFWorkbook()

    fun writeSaveFile(): Boolean {
        if (templateFile.path.isBlank()) return false
        if (copiedTemplate.numberOfSheets == 0) {
            reportError(textStrings[30])
            return false
        } else
            if (!templateFile.path.endsWith(".xlsx", false))
                copiedTemplate.write(FileOutputStream("${templateFile.path}.xlsx"))
            else
                copiedTemplate.write(FileOutputStream(templateFile.path))
        return true
    }

    fun setTemplateFile(file: Path){
        templateFile = file.toFile()
        setCopy()
        monitoring.setController(this)
        reporting.setController(this)
    }

    fun setCopy() {
        if (checkValidTemplate(XSSFWorkbook(FileInputStream(templateFile))))
            copiedTemplate = XSSFWorkbook(FileInputStream(templateFile))
        else
            reportError(textStrings[22])
    }

    fun setReportingFile(file: Path) {
        reporting.start(file.toString())
    }

    fun setMonitoringFile(file: Path) {
        monitoring.start(file.toString())
    }

    fun canEvaluate(): Boolean {
        return templateFile.canRead()
    }

    fun reportFlaw(flaw: String) {
        println(flaw)
    }

    fun reportError(error: String) {
        println(error)
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
