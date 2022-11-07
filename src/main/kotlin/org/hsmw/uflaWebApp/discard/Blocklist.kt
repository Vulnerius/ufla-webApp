package org.hsmw.uflaWebApp.discard

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

class Blocklist(preBlockList: List<Pair<String, String>>) {
    var finalList = preBlockList
    var selectedStartYear = LocalDate.now().year
    var selectedRegion: String = ""
    var selectedCity: String = ""
    var isCityDisabled = false

    //sets the internal tablenames so Tables can be queried correctly
    fun setProperties(year: Int, regionTableName: String?, cityTableName: String?, isCityDisabled: Boolean) {
        selectedStartYear = year
        if (regionTableName != null) {
            selectedRegion = regionTableName
        }
        if (cityTableName != null) {
            selectedCity = cityTableName
        }
        this.isCityDisabled = isCityDisabled

        stringsAreNull()
    }

    //querying the Table and returning a MutableList of Pairs
    fun setRegionItems(): MutableList<Pair<String, String>> {
        setSelected()
        return transaction {
            RegionPowerSupplyTable.select {
                RegionPowerSupplyTable.sw_o_dl.eq("") and RegionPowerSupplyTable.sw_w_dl.eq("") and RegionPowerSupplyTable.year.eq(
                    selectedStartYear
                )
            }.map {
                it[RegionPowerSupplyTable.Umspannwerk] to it[RegionPowerSupplyTable.trafo]
            } as MutableList<Pair<String, String>>
        }
    }

    //querying the Table and returning a MutableList of Pairs
    fun setCityItems(): List<Pair<String, String>> {
        setSelected()
        return transaction {
            CityPowerSupplyTable.select {
                CityPowerSupplyTable.year.eq(selectedStartYear)
            }.map {
                CityPowerSupplyTable.internalTableName to it[CityPowerSupplyTable.AbgangZugang]
            } as MutableList<Pair<String, String>>
        }
    }

    //returning a boolean indicating whether networkoperator and region are set or not
    private fun stringsAreNull(): Boolean {
        if (selectedRegion == "") {
            //TODO find<EntlastungUserInterface>().addError("Netzregion nicht ausgewählt")
            return true
        } else if (!isCityDisabled && selectedCity == "") {
            //TODO find<EntlastungUserInterface>().addError("Stadtwerk nicht ausgewählt")
            return true
        }
        return false
    }

    // setting the internal tableName
    private fun setSelected() {
        if (isCityDisabled) RegionPowerSupplyTable.internalTableName = selectedRegion
        else CityPowerSupplyTable.internalTableName = selectedCity
    }

}