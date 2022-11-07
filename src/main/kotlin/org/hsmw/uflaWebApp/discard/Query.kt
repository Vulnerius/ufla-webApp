package org.hsmw.uflaWebApp.discard

import org.hsmw.uflaWebApp.MonthMapping
import org.hsmw.uflaWebApp.QueryConverter
import org.hsmw.uflaWebApp.format
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.YearMonth

class Query(private val controller: DiscardController) {

    private var cityTable: List<CityPowerSupplyData> = mutableListOf()
    private var regionTable: List<RegionPowerSupplyData> = mutableListOf()
    internal val selectedMonthsList: MutableList<Pair<String, Double>> = mutableListOf()

    // given the tableName selecting the chosen Table to query
    private fun useSelectedTable(tableName: String, isCityQuery: Boolean) {
        if (isCityQuery) {
            cityTable = getSelectedCityTable(tableName)
            CityPowerSupplyTable.internalTableName = tableName
        } else {
            regionTable = getSelectedRegionTable(tableName)
            RegionPowerSupplyTable.internalTableName = tableName
        }
    }

    // iterating the cityTables for the given tableName then returning the found list or an emptyList if not found
    private fun getSelectedCityTable(tableName: String): List<CityPowerSupplyData> {
        controller.mappedCityTables.forEach {
            it.forEach { table ->
                if ((table.internalName.substring(controller.connection.dbName.length + 1) == tableName)) {
                    return it
                }

            }
        }
        return emptyList()
    }

    // iterating the regionTables for the given tableName then returning the found list or an emptyList if not found
    private fun getSelectedRegionTable(tableName: String): List<RegionPowerSupplyData> {
        controller.mappedRegionTables.forEach {
            it.forEach { table ->
                if ((table.internalName == tableName)) return it
            }
        }
        return emptyList()
    }

    // querying the region and transmitting the result of the Query to an excel workbook
    fun queryRegion(
        limit: String?,
        networkOperator: String,
        startMonth: YearMonth?,
        endMonth: YearMonth?,
        limitDisabled: Boolean
    ) {
        useSelectedTable(networkOperator, false)
        regionTable = regionTable.filter {
            it.sw_w == "" && it.sw_o == "" && checkBlockList(it.Umspannwerk, it.trafo)
        }

        val selectedMonths: List<String> =
            getPeriod(QueryConverter.convertToSqlString(startMonth!!), QueryConverter.convertToSqlString(endMonth!!))

        if (regionTable.isEmpty()) {
            //TODO
            return
        } else if (selectedMonths.isEmpty()) {
            //TODO
            return
        }


        selectedMonths.forEach { month ->
            val tableMonth = MonthMapping.changeMonthNumToString(month.substring(0, 2))
            mapSelectedMonthsRegion(tableMonth, month)
        }

        controller.outputWriter.transmitToExcelFile(
            limitList(getAverage(selectedMonths.size), limit, limitDisabled),
            "${startMonth.month.value}-${startMonth.year} - ${endMonth.month.value}-${endMonth.year}_$networkOperator",
            true
        )
    }

    //given the substation Name and identifier returning if it is an item of the Blocklist
    private fun checkBlockList(substation: String, trafo: String): Boolean {
        controller.blockList.finalList.forEach {
            if (substation.lowercase().contains(it.first.lowercase()) &&
                trafo.lowercase().contains(it.second.lowercase())
            ) return false
        }
        return true
    }

    // given the amount of numbers this function returns a List of Pairs with the name of the substation to the averaged values
    private fun getAverage(size: Int): List<Pair<String, Double>> {
        val averagedList = mutableListOf<Pair<String, Double>>()

        val listsSortedBySubstation: MutableList<MutableList<Pair<String, Double>>> = mutableListOf()

        selectedMonthsList.forEach { substationToValue ->
            if (!subStationInList(substationToValue.first, listsSortedBySubstation))
                listsSortedBySubstation.add(mutableListOf(substationToValue))
            else
                getListForSubstation(listsSortedBySubstation, substationToValue.first).add(substationToValue)
        }

        listsSortedBySubstation.forEach { listOfSubstations ->
            var average = .0
            var key = ""
            listOfSubstations.forEach { substation ->
                average += substation.second
                key = substation.first
            }
            averagedList.add(Pair(key, (average / size).format(2)))
        }

        return averagedList.sortedBy { it.second }.reversed()
    }

    // given a 2 dimensional list of SubstationName - value pairs and the name of the substation this function returns the found list
    private fun getListForSubstation(
        listsSortedBySubstation: MutableList<MutableList<Pair<String, Double>>>,
        first: String
    ): MutableList<Pair<String, Double>> {
        listsSortedBySubstation.forEach { listOfSubstation ->
            if (listOfSubstation[0].first == first) {
                return listOfSubstation
            }
        }
        //TODO
        return mutableListOf()
    }

    // returns true if the substation name is in the 2 dimensional list of substations
    private fun subStationInList(
        subStationName: String,
        listsSortedBySubstation: MutableList<MutableList<Pair<String, Double>>>
    ): Boolean {
        listsSortedBySubstation.forEach { listOfSubStation ->
            if (listOfSubStation[0].first == subStationName)
                return true
        }
        return false
    }

    //given the Month this gets the value in the selected Row and Column
    private fun mapSelectedMonthsRegion(tableMonth: Months, monthYear: String) {
        var monthColumn: Column<Double> = Column(Table(), "", DoubleColumnType())
        transaction {
            RegionPowerSupplyTable.columns.forEach {
                if (it.name == tableMonth.toString()) monthColumn = it as Column<Double>
            }
            if (monthColumn.name == "") monthColumn = RegionPowerSupplyTable.double(tableMonth.toString())
            queryRegionTableForSelectedYear(monthYear, monthColumn)
        }
    }

    // given monthYear String and the Column this first checks the queryConditions then mapping the Values in the table
    private fun queryRegionTableForSelectedYear(startMonth: String, monthColumn: Column<Double>) {
        RegionPowerSupplyTable.selectAll().forEach { row ->
            if (checkConditionsRegion(row, startMonth.substring(3, 7).toInt()))
                mapRegionValuesInTable(row, monthColumn)
        }
    }

    // given row and year this returns true if the year matches and if the substation is in the RegionTable
    private fun checkConditionsRegion(row: ResultRow, year: Int): Boolean {
        return row[RegionPowerSupplyTable.year] == year &&
                row[RegionPowerSupplyTable.Umspannwerk] == getRegionTableSubstation(row)
    }

    private fun getRegionTableSubstation(row: ResultRow): String {
        regionTable.forEach { regionPowerSupplyData ->
            if (row[RegionPowerSupplyTable.Umspannwerk] == regionPowerSupplyData.Umspannwerk)
                return regionPowerSupplyData.Umspannwerk
        }
        return ""
    }

    // adding a pair of String-Double to the selectedMonthsList
    private fun mapRegionValuesInTable(row: ResultRow, monthColumn: Column<Double>) {
        selectedMonthsList.add(
            Pair(
                "${row[RegionPowerSupplyTable.Umspannwerk]}:${row[RegionPowerSupplyTable.trafo]}",
                row[monthColumn]
            )
        )
    }

    // given start and end this returns a List of Strings in between start and end (both inclusive)
    internal fun getPeriod(startMonth: String, endMonth: String): List<String> {
        if (checkForNull(startMonth, endMonth)) return emptyList()
        else if (checkReversedDates(startMonth, endMonth)) return getMonthsBetweenSelection(endMonth, startMonth)
        return getMonthsBetweenSelection(startMonth, endMonth)
    }

    // returns true if the endMonth is earlier than the startmonth
    private fun checkReversedDates(startMonthYear: String, endMonthYear: String): Boolean {
        val startMonth = startMonthYear.substring(0, 2).toInt()
        val startYear = startMonthYear.substring(3, 7).toInt()
        val endMonth = endMonthYear.substring(0, 2).toInt()
        val endYear = endMonthYear.substring(3, 7).toInt()

        return if (startYear == endYear) startMonth > endMonth
        else endMonth > startMonth
    }

    // startMonth: String
    // endMonth : String
    // returns a list including start- and endMonth of Strings like ["01-YYYY","02-YYYY",...,"12-YYYY"]
    private fun getMonthsBetweenSelection(startMonthYear: String, endMonthYear: String): MutableList<String> {
        val startMonth = startMonthYear.substring(0, 2).toInt()
        val startYear = startMonthYear.substring(3, 7)
        val endMonth = endMonthYear.substring(0, 2).toInt()
        val endYear = endMonthYear.substring(3, 7)

        return if (startYear.toInt() != endYear.toInt()) {
            getCrossYearTable(startMonth, endMonth, startYear, endYear)
        } else {
            return loopMonthsSingleYear(startMonth, endMonth, endYear)
        }
    }

    // returning the Strings of months as a List
    private fun loopMonthsSingleYear(start: Int, end: Int, year: String): MutableList<String> {
        val monthsList = mutableListOf<String>()
        for (i in start..end) {
            monthsList.add(
                if (i.toString().length == 1) "0$i-$year"
                else "$i-$year"
            )
        }
        return monthsList
    }

    // returns a concatenated list if the query is cross year
    private fun getCrossYearTable(
        startMonth: Int, endMonth: Int, startYear: String, endYear: String
    ): MutableList<String> {
        return concatenate(loopMonthsSingleYear(startMonth, 12, startYear), loopMonthsSingleYear(1, endMonth, endYear))
    }

    /*
        taking multiple lists and returning a single list with all given list elements
       credits to: https://www.techiedelight.com/concatenate-multiple-lists-kotlin/
    */
    private fun concatenate(vararg lists: List<String>): MutableList<String> {
        return mutableListOf(*lists).flatten().toMutableList()
    }

    // returns true if one of the given months is null
    private fun checkForNull(startMonth: String?, endMonth: String?): Boolean {
        if (startMonth == null || endMonth == null) return true
        return false
    }

    // querying the city and transmitting the result of the Query to an excel workbook
    fun queryCity(
        limit: String?,
        citySelector: String,
        startMonth: YearMonth?,
        endMonth: YearMonth?,
        limitDisabled: Boolean
    ) {
        useSelectedTable(citySelector, true)
        cityTable = cityTable.filter {
            checkBlockList(it.internalName, it.AbgangZugang)
        }
        val selectedMonths: List<String> =
            getPeriod(QueryConverter.convertToSqlString(startMonth!!), QueryConverter.convertToSqlString(endMonth!!))

        if (cityTable.isEmpty()) {
            //TODO
            return
        } else if (selectedMonths.isEmpty()) {
            //TODO
            return
        }

        selectedMonths.forEach { month ->
            val tableMonth = MonthMapping.changeMonthNumToString(month.substring(0, 2))
            mapSelectedMonthsCity(tableMonth, month)
        }
        controller.outputWriter.transmitToExcelFile(
            limitList(getAverage(selectedMonths.size), limit, limitDisabled),
            "${startMonth.month.value}-${startMonth.year} - ${endMonth.month.value}-${endMonth.year}_$citySelector",
            false
        )
    }

    // given a limit this returns a list limited by the limi
    private fun limitList(
        averagedList: List<Pair<String, Double>>,
        limit: String?,
        isLimitedDisabled: Boolean
    ): List<Pair<String, Double>> {
        if (isLimitedDisabled) return averagedList

        val printingList = mutableListOf<Pair<String, Double>>()
        try {
            var limitNum = limit!!.toInt()
            if (limitNum > averagedList.size) {
                limitNum = averagedList.size
                //TODO
            }
            for (i in 0 until limitNum) printingList.add(i, averagedList[i])
        } catch (e: Exception) {
            println(e.message)
        }
        return printingList
    }

    //given the Month this gets the value in the selected Row and Column
    private fun mapSelectedMonthsCity(tableMonth: Months, monthYear: String) {
        var monthColumn: Column<Double> = Column(Table(), "", DoubleColumnType())
        try {
            transaction {
                CityPowerSupplyTable.columns.forEach { columnInCityTable ->
                    if (columnInCityTable.name == tableMonth.toString()) monthColumn =
                        columnInCityTable as Column<Double>
                }
                if (monthColumn.name == "") monthColumn = CityPowerSupplyTable.double(tableMonth.toString())
                queryCityTableForSelectedYear(monthYear, monthColumn)
            }
        } catch (e: Exception) {
            //TODO
            return
        }
    }

    internal fun clearMonthsList() {
        selectedMonthsList.clear()
    }

    // given monthYear String and the Column this first checks the queryConditions then mapping the Values in the table
    private fun queryCityTableForSelectedYear(startMonth: String, monthColumn: Column<Double>) {
        CityPowerSupplyTable.selectAll().forEach { row ->
            if (checkConditionsCity(row, startMonth.substring(3, 7).toInt()))
                mapCityValuesInTable(row, monthColumn)
        }
    }

    // iterating through the cityTAbles this returns the name of the substation if found in selected Table
    private fun getCityTableSubstation(row: ResultRow): String {
        cityTable.forEach { cityPowerSupplyData ->
            if (row[CityPowerSupplyTable.AbgangZugang] == cityPowerSupplyData.AbgangZugang)
                return cityPowerSupplyData.AbgangZugang
        }
        return ""
    }

    // given row and year this returns true if the year matches and if the substation is in the CityTAble
    private fun checkConditionsCity(row: ResultRow, year: Int): Boolean {
        return row[CityPowerSupplyTable.year] == year &&
                row[CityPowerSupplyTable.AbgangZugang] == getCityTableSubstation(row)
    }

    // adding a pair of String-Double to the selectedMonthsList
    private fun mapCityValuesInTable(row: ResultRow, monthColumn: Column<Double>) {
        selectedMonthsList.add(Pair(row[CityPowerSupplyTable.AbgangZugang], row[monthColumn]))
    }

}