package org.hsmw.uflaWebApp

import org.springframework.stereotype.Service

@Service
object LanguageTexts {

    fun get(language: String): MutableList<String> {
        return if (language == "de") ger
        else en
    }

    //german values for UI
    private val ger: MutableList<String> = mutableListOf(
        "UFLA-Tool", //title                                                      0
        "UFLA-Reporting & Monitoring Tool", //Header                                                 1
        "", //empty                                           2
        "Vorlage", //templateButton                                    3
        "Reporting", //reportButton                                        4
        "Monitoring", //monitoringButton                                5
        "Hilfe", //helpButton                                           6
        "Speichern", //saveButton                                           7
        "Speichern unter...", //saveAsButton                                       8
        "Schließen", //exitButton                                            9
        "", //empty                                    10
        "Sind Sie sicher?", //safetyCheckExitText                            11
        "Die Daten werden auf die Vorlage geschrieben \n Sind Sie sicher?", //safetyCheckSaveTemplateText             12
        "gespeichert unter\t", //savedToText                                         13
        "speichern abgebrochen", //savingCancelledText                             14
        "\n \tÜberprüfen Sie die Vorlage und starten Sie das Programm erneut", //defaultErrorText                                    15
        "kein Zahlenwert oder Zahlenwert <= 0", //falseOrNullValueReporting                    16
        "ausgwählter Datensatz, aber nicht in Kalkulation enthalten: ", //markedButNotInCalculationText           17
        "\t keine Vorlage zum Schreiben der Reporting Daten gefunden", //noReportingSheetInTemplate                18
        "Frequenz nicht in Vorlage gefunden: ", //frequencyNotFoundInTemplate             19
        "in UFLA enthaltene MegaWatt: ", //MegaWattInfo                                         20
        "Datei konnte nicht gespeichert werden", //empty                              21
        "kein Reporting oder Monitoring Sheet in Vorlage gefunden", //checkValidateNotFound                         22
        "Lade Netzentlastung", //LoadScreenTitle                                      23
        "Abbrechen", //cancel        24
        "Datendurchsuche Reporting\t", //reportStart  25
        "Reporting beendet\t", //reportEnd                26
        "Datenübertragung Monitoring\t", //monitoringStart    27
        "Monitoring beendet\t", //monitoringEnd       28
        "Wählen Sie eine Datei aus", //InsertFileError 29
        "Wählen Sie eine Vorlage aus", //TemplateFileError  30
        "Vorlage von\t", //templateSetToText  31

        /// -- Entlastung --
        "Vorlage laden", //get template 32
        "Abfragen", //start query 33
    )

    //english values for UI
    private val en: MutableList<String> = mutableListOf(
        "UFLA-Tool", //title                                                      0
        "UFLA-Reporting & Monitoring Tool", //Header                                                 1
        "", //empty                                           2
        "template", //templateButton                                    3
        "report", //reportButton                                        4
        "monitor", //monitoringButton                                5
        "help", //helpButton                                           6
        "save", //saveButton                                           7
        "save as...", //saveAsButton                                       8
        "exit", //exitButton                                            9
        "", //empty                                    10
        "Are you sure?", //safetyCheckExitText                            11
        "You are going to save the data to the template file!\n Are you sure?", //safetyCheckSaveTemplateText             12
        "saved to", //savedToText                                         13
        "saving cancelled", //savingCancelledText                             14
        "\n \tInsert a valid template or restart the application", //defaultErrorText                                    15
        "false or null value@ ", //falseOrNullValueReporting                    16
        "not in calculation but with \"x\" marked: ", //markedButNotInCalculationText           17
        "\t no reporting sheet found in Template", //noReportingSheetInTemplate                18
        "frequency not found in reporting sheet", //frequencyNotFoundInTemplate             19
        "MegaWatt for file: ", //MegaWattInfo                                         20
        "File could not be saved", //empty                              21
        "no reporting or monitoring template found\n \tcancelled", //checkValidateNotFound                         22
        "Loading UFLA-Tool", //LoadScreenTitle                                      23
        "Cancel", //cancel 24
        "reporting file\t", //reportStart 25
        "reporting end for file\t", //reportEnd 26
        "monitoring file\t", //monitoringStart 27
        "monitoring end for file\t", //monitoringEnd 28
        "Choose a file", //InsertFileError 29
        "Choose a template file", //TemplateFileError 30
        "template set to\t", //templateSetToText 31

        // --- Discard ---
        "Load template", //template 32
        "Query", //query 33
    )
}
