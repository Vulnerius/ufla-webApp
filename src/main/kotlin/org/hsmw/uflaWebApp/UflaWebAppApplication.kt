package org.hsmw.uflaWebApp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class UflaWebAppApplication

fun main(args: Array<String>) {
	runApplication<UflaWebAppApplication>(*args)
}
