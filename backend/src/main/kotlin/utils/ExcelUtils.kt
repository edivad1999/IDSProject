package utils

import kotlinx.datetime.Clock
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import routes.SerializableOrder
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*


class ExcelUtils {

    fun createExcel(serializables: List<SerializableOrder>, processes: List<String>): File {
        var file: File = kotlin.io.path.createTempFile("excel" + Clock.System.now().epochSeconds.toString(), ".xslx").toFile()

        val workbook: Workbook = XSSFWorkbook()

        val sheet: Sheet = workbook.createSheet("Persons")
        sheet.setColumnWidth(0, 6000)
        sheet.setColumnWidth(1, 4000)

        val header: Row = sheet.createRow(0)

        val headerStyle = workbook.createCellStyle()
        headerStyle.fillForegroundColor = IndexedColors.LIGHT_BLUE.getIndex()
        headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND

        val font = (workbook as XSSFWorkbook).createFont()
        font.fontName = "Arial"
        font.fontHeightInPoints = 16.toShort()
        font.bold = true
        headerStyle.setFont(font)

        val headers = setHeaders(header, processes)
        var counter = 1
        var cell: Cell

        converToRows(serializables).forEach {
            val row: Row = sheet.createRow(counter)
            for ((headerIndex, header) in headers.withIndex()) {
                cell = row.createCell(headerIndex)
                cell.setCellValue(getValueByHeader(header, it))
            }
            counter++
        }
        workbook.write(file.outputStream())
        workbook.close()
        return file

    }

    private fun getValueByHeader(header: String, row: OrderTableRow): String {
        if (header == "Prodotto") {
            return row.product ?: ""
        } else if (header == "Consegna richiesta") {
            return if (row.requestedDate != null) convertLongToTime(row.requestedDate)
            else ""

        } else if (header == "Qtà prod") {
            return if (row.requestedQuantity != null) row.requestedQuantity.toString()
            else ""
        } else if (header == "Codice richiesto") {
            return row.productCode ?: ""
        } else if (header == "Qtà rich.") {
            return if (row.productQuantity != null) row.productQuantity.toString()
            else ""
        } else if (header == "Codice primario") {
            return row.rawCode ?: ""
        } else if (header == "Qtà necess.") {
            return if (row.rawQuantity != null) row.rawQuantity.toString()
            else ""
        } else if (header == "Commessa") {
            return row.commission ?: ""

        } else if (header == "Cliente") {
            return row.client ?: ""

        } else if (header == "Ordine Cliente") {
            return row.clientOrderCode ?: ""

        } else if (header == "Operatore") {
            return row.operator ?: ""

        } else if (header == "Data INIZIO lavorazine") {
            return if (row.startDate != null) convertLongToTime(row.startDate)
            else ""
        } else if (header == "Data PRESUNTA fine") {
            return if (row.expectedEndDate != null) convertLongToTime(row.expectedEndDate)
            else ""
        } else if (header == "Data FINE lavorazione") {
            return if (row.endDate != null) convertLongToTime(row.endDate)
            else ""
        } else {
            if (row.processes != null) {
                row.processes.forEach { if (it == header) return "X" }
            }
            return ""

        }
    }

    private fun setHeaders(header: Row, processes: List<String>): List<String> {

        val preList = mutableListOf<String>("Prodotto", "Consegna richiesta", "Qtà prod", "Codice richiesto", "Qtà rich.", "Codice primario", "Qtà necess.")
        val postList = listOf<String>("Commessa", "Cliente", "Ordine Cliente", "Operatore", "Data INIZIO lavorazine", "Data PRESUNTA fine", "Data FINE lavorazione")
        preList.addAll(processes)
        preList.addAll(postList)
        var headerCell: Cell
        var counter = 0
        preList.forEach {
            headerCell = header.createCell(counter)
            headerCell.setCellValue(it)
            counter++
        }
        return preList
    }

    private fun converToRows(serializables: List<SerializableOrder>): List<OrderTableRow> {
        val list = emptyList<OrderTableRow>().toMutableList()
        serializables.forEach {
            list.add(OrderTableRow(
                it.id,
                it.product,
                it.requestedDate,
                it.requestedQuantity,
                it.internalOrders[0].productCode,
                it.internalOrders[0].productQuantity,
                it.internalOrders[0].rawCode,
                it.internalOrders[0].rawQuantity,
                it.internalOrders[0].operator,
                it.internalOrders[0].processes,
                it.internalOrders[0].externalTreatments,
                it.commission,
                it.client,
                it.clientOrderCode,
                it.internalOrders[0].startDate,
                it.internalOrders[0].endDate,
                it.internalOrders[0].expectedEndDate
            ))
            val mutableInternal = it.internalOrders.toMutableList()
            mutableInternal.removeFirst()
            mutableInternal.forEach { it1 ->
                list.add(
                    OrderTableRow(
                        null,
                        null,
                        null,
                        null,
                        it1.productCode,
                        it1.productQuantity,
                        it1.rawCode,
                        it1.rawQuantity,
                        it1.operator,
                        it1.processes,
                        it1.externalTreatments,
                        null,
                        null,
                        null,
                        it1.startDate,
                        it1.endDate,
                        it1.expectedEndDate
                    )
                )
            }
        }
        return list
    }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }


}


data class OrderTableRow(
    val id: String?,
    val product: String?,
    val requestedDate: Long?,
    val requestedQuantity: Int?,
    val productCode: String?,
    val productQuantity: Int?,
    val rawCode: String?,
    val rawQuantity: Int?,
    val operator: String?,
    val processes: List<String>? = emptyList(),
    val externalTreatments: String?,
    val commission: String?,
    val client: String?,
    val clientOrderCode: String?,
    val startDate: Long?,
    val endDate: Long?,
    val expectedEndDate: Long?,
)
