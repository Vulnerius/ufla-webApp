package org.hsmw.uflaWebApp.controller

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.ui.set
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class MonitoringController {
    @RequestMapping("/monitoring")
    fun getMonitoring(model: Model) : String{
        model["title"] = "Monitoring"
        return "monitoring/index"
    }
}