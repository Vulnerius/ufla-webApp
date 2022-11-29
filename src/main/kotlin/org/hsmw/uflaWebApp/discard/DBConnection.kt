package org.hsmw.uflaWebApp.discard

import org.hsmw.uflaWebApp.config.Configurations
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.name
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.w3c.dom.Element

class DBConnection {
    private val discardConfig: Element = Configurations().discardConfig
    private val regionConfig: Element = Configurations().region

    private val identifier: String = regionConfig.getAttribute("Identifier")

    private val host: String = discardConfig.getAttribute("Host")
    private val port: String = discardConfig.getAttribute("Port")
    internal val dbName: String = discardConfig.getAttribute("databaseName")

    private val mySQLurl: String = "jdbc:mysql://$host:$port/$dbName"
    private val mariaDBurl: String = "jdbc:mariadb://$host:$port/$dbName"

    private lateinit var url: String

    private lateinit var connectedDatabase: Database

    init {
        connect()
    }

    //connecting to the Database
    private fun connect() {
        url = if (discardConfig.getAttribute("Struktur").lowercase() == "mysql") mySQLurl
        else if (discardConfig.getAttribute("Struktur").lowercase() == "mariadb") mariaDBurl
        else ""
        val driver =
            if (url == mySQLurl) "com.mysql.cj.jdbc.Driver" else if (url == mariaDBurl) "org.mariadb.jdbc.Driver" else ""

        if (url == "") /* TODO find<EntlastungUserInterface>().addError(
            "\tkeine valide Datenbank-Struktur\n" +
                    "\t\tStruktur-Element in Konfigurationsdatei überprüfen\n"
        )*/
        connectedDatabase = Database.connect(
            url,
            driver = driver,
            user = discardConfig.getAttribute("UserName"),
            password = discardConfig.getAttribute("Password")
        )
//        DriverManager.getConnection(url, discardConfig.getAttribute("UserName"), discardConfig.getAttribute("Password"))
    }

    // returning true if the connection is established
    fun tryConnection(): Boolean {
        try {
            return transaction {
                TransactionManager.current().db.name == dbName
            }
        } catch (e: Exception) {
            println(e.cause)
        }
        return false
    }

    fun disconnect() {
        if (tryConnection()) {
            transaction {
                TransactionManager.closeAndUnregister(connectedDatabase)
            }
            //TODO find<EntlastungUserInterface>().addNormalText("Verbindung getrennt")
        }
    }

    //querying all names in the database if the connection is established
    fun queryTableNames(): List<String> {
        if (tryConnection()) return transaction {
            TransactionManager.current().db.dialect.allTablesNames()
        }
        return emptyList()
    }

    // returning a List of tableNames, items in this List match the given identifier in the Config
    fun getRegionTableNames(tableNames: List<String>): List<String> {
        val returnList: MutableList<String> = mutableListOf()
        tableNames.forEach { name ->
            if (name.contains(identifier)) returnList.add(name)
        }
        return returnList
    }

    // creates a 2 dimensional List of RegionPowerSupplyData with data mapped from the database
    fun createRegionDataList(tableNames: List<String>): List<List<RegionPowerSupplyData>> {
        val returnList: MutableList<MutableList<RegionPowerSupplyData>> = mutableListOf()

        getRegionTableNames(tableNames).forEach { name ->
            returnList.add(mapToRegionData(name))
        }

        return returnList
    }

    // given the tableName data is mapped in RegionPowerSupplyData class and returned as a MutableList
    private fun mapToRegionData(name: String): MutableList<RegionPowerSupplyData> {
        RegionPowerSupplyTable.internalTableName = name
        return transaction {
            RegionPowerSupplyTable.selectAll()
                .map { RegionPowerSupplyData.fromRow(it) }
        } as MutableList<RegionPowerSupplyData>
    }

    // creates a 2 dimensional List of CityPowerSupplyData with data mapped from the database
    fun createCityDataList(tablesList: List<String>): List<List<CityPowerSupplyData>> {
        val returnList: MutableList<MutableList<CityPowerSupplyData>> = mutableListOf()

        getCityTableNames(tablesList).forEach { name ->
            returnList.add(mapToCityData(name))
        }
        return returnList
    }

    // given the tableName data is mapped in CityPowerSupplyData class and returned as a MutableList
    private fun mapToCityData(name: String): MutableList<CityPowerSupplyData> {
        CityPowerSupplyTable.internalTableName = name
        return transaction {
            CityPowerSupplyTable.selectAll()
                .map { CityPowerSupplyData.fromRow(it) } as MutableList<CityPowerSupplyData>
        }
    }

    // returning a List of tableNames, items in this List don't match the given identifier in the Config
    private fun getCityTableNames(tableNames: List<String>): List<String> {
        val returnList: MutableList<String> = mutableListOf()
        tableNames.forEach { name ->
            if (!(name.contains(identifier))) returnList.add(name)
        }
        return returnList
    }

}