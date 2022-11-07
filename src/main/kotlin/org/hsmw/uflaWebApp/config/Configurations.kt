package hsmw.config

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilderFactory

class Configurations {
    val region: Element
    private val configFile: Document
    internal val configPath: String
    val templateConfig: Element
    val reportingConfig: Element
    val monitoringConfig: Element
    val discardConfig: Element
    val discardBlocklist: Element

    init {
        val osName = System.getProperty("os.name")
        if (File("./config/Config.xml").exists()) {
            configFile =
                DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(FileInputStream("./config/Config.xml"))
            configPath = "config/Config.xml"
        } else {
            if (osName.contains(Regex("win")) && File(".\\config\\Config.xml").exists()) {
                configFile =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder()
                        .parse(FileInputStream(".\\config\\Config.xml"))
                configPath = "config/Config.xml"
            } else {
                configFile = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(javaClass.getResourceAsStream("/hsmw/Config.xml"))
                configPath = "resources"
            }
        }
        templateConfig = getConfigElement("template")
        reportingConfig = getConfigElement("reporting")
        monitoringConfig = getConfigElement("monitoring")
        discardConfig = getConfigElement("discardDatabase")
        discardBlocklist = getConfigElement("blocklist")
        region = getConfigElement("regionData")
    }

    //returns an XML-Element from the config File
    internal fun getConfigElement(elementTag: String): Element {
        val configList = configFile.getElementsByTagName(elementTag)
        return configList.item(0) as Element
    }
}