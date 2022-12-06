package org.hsmw.uflaWebApp.controller

import org.hsmw.uflaWebApp.ufla.UFLAController
import org.hsmw.uflaWebApp.webStorage.StorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


@Controller
class UflaController @Autowired constructor(private val storageService: StorageService, private val uflaController: UFLAController) {

    @RequestMapping("/ufla")
    fun showUFLA(model: Model): String {
        model.addAttribute("headline", "Automatisierung Reporting & Monitoring")
        model.addAttribute("save", "Speichern")
        model.addAttribute("saveAs", "Speichern unter")
        model.addAttribute("help", "Hilfe")

        return "ufla/index"
    }

    @RequestMapping(path = ["/ufla/download"], method = [RequestMethod.GET])
    @Throws(IOException::class)
    fun download(): ResponseEntity<Resource?>? {
        uflaController.writeSaveFile()
        val templateFileName = "template.xlsx"
        val file = File(storageService.load(templateFileName).toUri())

        val header = HttpHeaders()
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=template.xlsx")
        header.add("Cache-Control", "no-cache, no-store, must-revalidate")
        header.add("Pragma", "no-cache")
        header.add("Expires", "0")

        val path: Path = Paths.get(file.absolutePath)
        val resource = ByteArrayResource(Files.readAllBytes(path))

        return ResponseEntity.ok()
            .headers(header)
            .contentLength(file.length())
            .contentType(MediaType.parseMediaType("application/octet-stream"))
            .body<Resource>(resource)
    }
}