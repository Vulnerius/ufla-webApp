package org.hsmw.uflaWebApp

import org.hsmw.uflaWebApp.discard.DiscardController
import org.hsmw.uflaWebApp.discard.Months
import org.hsmw.uflaWebApp.ufla.UFLAController
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object Main {
    val controllerK = UFLAController()

    //    lateinit var discardController: DiscardController
    val discardController = DiscardController()
}

fun Double.format(digits: Int): Double = "%.${digits}f".format(this).toDouble()

interface Reader {
    companion object {
        fun createWorkbook(fs: FileInputStream): XSSFWorkbook = XSSFWorkbook(fs)
    }
}

interface QueryConverter {
    companion object {
        fun convertToSqlString(input: YearMonth): String {
            return input.format(DateTimeFormatter.ofPattern("MM-yyyy"))
        }
    }

}

interface MonthMapping {
    companion object {
        fun changeMonthNumToString(month: String): Months {
            when (month) {
                "01" -> return Months.Jan
                "02" -> return Months.Feb
                "03" -> return Months.Mrz
                "04" -> return Months.Apr
                "05" -> return Months.Mai
                "06" -> return Months.Jun
                "07" -> return Months.Jul
                "08" -> return Months.Aug
                "09" -> return Months.Sep
                "10" -> return Months.Okt
                "11" -> return Months.Nov
                "12" -> return Months.Dez
            }
            return Months.NotFound
        }
    }
}