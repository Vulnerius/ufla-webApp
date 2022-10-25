package org.hsmw.uflaWebApp.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class ReportingController {
    @RequestMapping("/reporting")
    fun getReporting(model: Model): String{
        model["title"] = "Reporting"
        return "reporting/index"
    }
}