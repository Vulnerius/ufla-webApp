package org.hsmw.uflaWebApp.discard

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

object RegionPowerSupplyTable : Table("") {
    override val tableName: String
        get() = internalTableName
    var internalTableName = ""
    val num: Column<Int> = RegionPowerSupplyTable.integer("Nummer")
    val Umspannwerk: Column<String> = RegionPowerSupplyTable.varchar("UW", 255)
    val trafo: Column<String> = RegionPowerSupplyTable.varchar("Trafo", 255)
    val year: Column<Int> = RegionPowerSupplyTable.integer("Jahr")
    val sw_w_dl: Column<String> = RegionPowerSupplyTable.varchar("SW mit DL", 1)
    val sw_o_dl: Column<String> = RegionPowerSupplyTable.varchar("SW ohne DL", 1)
}

data class RegionPowerSupplyData(
    val internalName: String,
    val num: Int,
    val Umspannwerk: String,
    val trafo: String,
    val year: Int,
    val sw_w: String?,
    val sw_o: String?,
) {
    companion object {
        fun fromRow(resultRow: ResultRow) = RegionPowerSupplyData(
            internalName = RegionPowerSupplyTable.internalTableName,
            num = resultRow[RegionPowerSupplyTable.num],
            Umspannwerk = resultRow[RegionPowerSupplyTable.Umspannwerk],
            trafo = resultRow[RegionPowerSupplyTable.trafo],
            year = resultRow[RegionPowerSupplyTable.year],
            sw_w = resultRow[RegionPowerSupplyTable.sw_w_dl],
            sw_o = resultRow[RegionPowerSupplyTable.sw_o_dl]
        )
    }
}

object CityPowerSupplyTable : Table("") {
    override val tableName: String
        get() = internalTableName
    var internalTableName = ""
    val num: Column<Int> = CityPowerSupplyTable.integer("Nummer")
    val AbgangZugang: Column<String> = CityPowerSupplyTable.varchar("Abgang/Zugang", 255)
    val year: Column<Int> = CityPowerSupplyTable.integer("Jahr")
}

data class CityPowerSupplyData(
    val internalName: String,
    val num: Int,
    val AbgangZugang: String,
    val year: Int,
) {
    companion object {
        fun fromRow(resultRow: ResultRow) = CityPowerSupplyData(
            internalName = CityPowerSupplyTable.internalTableName,
            num = resultRow[CityPowerSupplyTable.num],
            AbgangZugang = resultRow[CityPowerSupplyTable.AbgangZugang],
            year = resultRow[CityPowerSupplyTable.year],
        )
    }
}

