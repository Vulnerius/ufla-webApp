package org.hsmw.uflaWebApp

import org.hsmw.uflaWebApp.webStorage.StorageProperties
import org.hsmw.uflaWebApp.webStorage.StorageService
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean


@SpringBootApplication
@EnableConfigurationProperties(StorageProperties::class)
class UflaWebAppApplication

@Bean
fun init(storageService: StorageService): CommandLineRunner {
    return CommandLineRunner { args: Array<String?>? ->
        storageService.deleteAll()
        storageService.init()
    }
}

fun main(args: Array<String>) {
    runApplication<UflaWebAppApplication>(*args)
}
