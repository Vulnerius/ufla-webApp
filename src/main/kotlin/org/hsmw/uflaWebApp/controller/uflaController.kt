package org.hsmw.uflaWebApp.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class uflaController {

    @RequestMapping("/ufla")
    fun showUFLA(model: Model): String{
        model.addAttribute("headline", "Automatisierung Reporting & Monitoring")
        model.addAttribute("selTempl", "Vorlage auswählen")
        model.addAttribute("rep", "Reporting")
        model.addAttribute("mon", "Monitoring")
        model.addAttribute("save", "Speichern")
        model.addAttribute("saveAs", "Speichern unter")
        model.addAttribute("help", "Hilfe")
        return "ufla/index"
    }
}