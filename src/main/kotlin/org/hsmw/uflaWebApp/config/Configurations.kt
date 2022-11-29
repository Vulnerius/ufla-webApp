package org.hsmw.uflaWebApp.config

import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
import javax.xml.parsers.DocumentBuilderFactory

@Configuration
class Configurations {
    final val region: Element
    private val configFile: Document
    private final val configPath: String
    final val templateConfig: Element
    final val reportingConfig: Element
    final val monitoringConfig: Element
    final val discardConfig: Element
    final val discardBlocklist: Element

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
                    .parse(javaClass.getResourceAsStream("/static/hsmw/Config.xml"))
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
    internal final fun getConfigElement(elementTag: String): Element {
        val configList = configFile.getElementsByTagName(elementTag)
        return configList.item(0) as Element
    }
}