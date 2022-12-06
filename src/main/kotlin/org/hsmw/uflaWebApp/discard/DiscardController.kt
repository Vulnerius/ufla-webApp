package org.hsmw.uflaWebApp.discard

import org.hsmw.uflaWebApp.config.Configurations
import org.springframework.stereotype.Service
import java.time.YearMonth

@Service
class DiscardController {
    lateinit var connection: DBConnection

    val outputWriter: ExcelWriter = ExcelWriter(this)
    internal val query = Query(this)

    lateinit var mappedRegionTables: List<List<RegionPowerSupplyData>>
    lateinit var mappedCityTables: List<List<CityPowerSupplyData>>

    var summedAverages = .0

    val preBlockList: MutableList<Pair<String, String>> = mutableListOf<Pair<String, String>>()
    val blockList: Blocklist

    var errorThrown = false

    init {
        addBlockListItems()
        blockList = Blocklist(preBlockList)
    }

    //instantiating the DatabaseConnection and querying the database for the tableNames
    fun startConnection() {
        connection = DBConnection()
        createTables()
        //TODO find<EntlastungUserInterface>().setTableItems(find<EntlastungUserInterface>().tableChoices)
    }

    // creating the Region and City Tables
    private fun createTables() {
        mappedRegionTables = if (connection.tryConnection()) connection.createRegionDataList(getTablesList())
        else emptyList()
        mappedCityTables = if (connection.tryConnection()) connection.createCityDataList(getTablesList())
        else emptyList()
    }

    // returning the List of tableNames
    fun getTablesList(): List<String> {
        return connection.queryTableNames()
    }

    // base function on Button-press Query - this also gets most Errors while query and handles them
    fun onQuery(
        limit: String?,
        startMonth: YearMonth?,
        endMonth: YearMonth?,
        networkOperator: String?,
        citySelector: String?,
        cityDisabled: Boolean,
        limitDisabled: Boolean
    ) {
        errorThrown = false
        if (cityDisabled) {
            if (networkOperator == null) {
                errorThrown = true
                //TODO find<EntlastungUserInterface>().addError("\tkein Netzbetreiber ausgewählt\n")
                return
            }
            try {
                query.queryRegion(limit, networkOperator, startMonth, endMonth, limitDisabled)
            } catch (e: Exception) {
                errorThrown = true
                //TODO find<EntlastungUserInterface>().addError("zuerst mit Datenbank verbinden - ${e.message}")
                return
            }
            return
        }
        if (citySelector == null) {
            errorThrown = true
            //TODO find<EntlastungUserInterface>().addError("\tkein Stadtwerk ausgewählt\n")
            return
        }
        try {
            query.queryCity(limit, citySelector, startMonth, endMonth, limitDisabled)
        } catch (e: Exception) {
            errorThrown = true
            //TODO find<EntlastungUserInterface>().addError("zuerst mit Datenbank verbinden - ${e.message}")
            return
        }
    }

    // basic function to save the query as an excel-file
    fun onSaveAs() {
        /* TODO val fc = FileChooser()
        fc.extensionFilters.add(FileChooser.ExtensionFilter("Excel Files", "*.xlsx"))
        val savePath = fc.showSaveDialog(find<EntlastungUserInterface>().currentWindow)
        if (savePath == null) {
            //TODO find<EntlastungUserInterface>().addError("keine Datei angegeben")
            return
        }
        outputWriter.saveAs(savePath.path)
        */
    }


    // given a String this function returns all the cityTableNames indicated as a municipial with a service
    fun getCitiesWithService(selectedItem: String?): List<String> {
        val returnList: MutableList<String> = mutableListOf()
        mappedRegionTables.forEach {
            it.forEach cityAdd@{ tableRow ->
                if (tableRow.internalName == selectedItem && tableRow.sw_w.toString() == "x") {
                    preBlockList.forEach blocklistCheck@{ blocklisted ->
                        if (blocklisted.first == tableRow.Umspannwerk && blocklisted.second == tableRow.trafo) return@cityAdd
                    }
                    returnList.add(tableRow.Umspannwerk)
                }
            }
        }
        return returnList.distinct()
    }

    // iterating through the fixed blocklisted items and adding them to the preBlockList
    private fun addBlockListItems() {
        splitConfigurationItems().forEach { blocklisted ->
            preBlockList.add(blocklisted)
        }
    }

    // taking the Element from the Configuration File and returning a List of Pairs with String,String
    private fun splitConfigurationItems(): List<Pair<String, String>> {
        return Configurations().discardBlocklist.getAttribute("items").split(",").map {
            val splitBlock = it.split(":")
            splitBlock.first() to splitBlock.last()
        }
    }

    // iterating through the queried values and returning the average for each Substation
    fun getAverageSum(): String {
        var summedAverage = .0
        query.selectedMonthsList.forEach {
            summedAverage += it.second
        }
        query.clearMonthsList()
        summedAverages += summedAverage
        return summedAverage.toString()
    }

    fun setBlockListProperties(year: Int, regionTableName: String?, cityTableName: String?, isCityDisabled: Boolean) {
        blockList.setProperties(year, regionTableName, cityTableName, isCityDisabled)
    }
}