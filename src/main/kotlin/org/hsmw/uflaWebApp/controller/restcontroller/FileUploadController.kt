package org.hsmw.uflaWebApp.controller.restcontroller

import org.hsmw.uflaWebApp.ufla.UFLAController
import org.hsmw.uflaWebApp.webStorage.StorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.io.FileNotFoundException

@Controller
class FileUploadController @Autowired constructor(private val storageService: StorageService, private val uflaController: UFLAController) {

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    fun serveFile(@PathVariable fileName: String): ResponseEntity<Resource> {
        val file: Resource = storageService.loadAsResource(fileName)
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${file.filename}\"")
            .body(file)
    }

    @PostMapping("/api/uploadTemplate")
    fun handleTemplateFileUpload(
        @RequestParam("template.xlsx") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        storageService.store(file);
        redirectAttributes.addFlashAttribute(
            "log",
            "You successfully uploaded template: ${file.originalFilename} !"
        );

        uflaController.setTemplateFile(storageService.load(file.name))

        return "redirect:/ufla";
    }

    @PostMapping("/api/uploadReporting")
    fun handleReportingFileUpload(
        @RequestParam("reporting.xlsx") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        storageService.store(file);
        redirectAttributes.addFlashAttribute(
            "log",
            "You successfully uploaded reporting: ${file.originalFilename} !"
        );

        uflaController.setReportingFile(storageService.load(file.name))

        return "redirect:/ufla";
    }

    @PostMapping("/api/uploadMonitoring")
    fun handleMonitoringFileUpload(
        @RequestParam("monitoring.xlsx") file: MultipartFile,
        redirectAttributes: RedirectAttributes
    ): String {
        storageService.store(file);
        redirectAttributes.addFlashAttribute(
            "log",
            "You successfully uploaded monitoring: ${file.originalFilename} !"
        );

        uflaController.setMonitoringFile(storageService.load(file.name))

        return "redirect:/ufla";
    }

    @ExceptionHandler(FileNotFoundException::class)
    fun handleStorageFileNotFound(exc: FileNotFoundException): ResponseEntity<Any> {
        return ResponseEntity.notFound().build();
    }
}