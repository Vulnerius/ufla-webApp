package org.hsmw.uflaWebApp.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class FileUpload {

    @PostMapping("/api/files")
    fun uploadFile(@RequestBody file: String): String{
        //upload & save to db
        return file
    }

}