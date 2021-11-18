package utils


import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.itextpdf.io.image.ImageData
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.HorizontalAlignment
import com.itextpdf.layout.property.TextAlignment
import kotlinx.datetime.Clock
import routes.SerializableInternalOrder
import routes.SerializableOrder
import routes.auth.Base64Encoder
import routes.auth.JavaBase64Encoder
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO


class OrderPDF(private val order: SerializableOrder) {

    val encoder: Base64Encoder = JavaBase64Encoder()
    private fun getHeader(): Paragraph {
        val text = "Prodotto:${this.order.product}, quantità ${this.order.requestedQuantity}\n" +
                "Data richiesta:${convertLongToTime(this.order.requestedDate)}\n" +
                "Cliente:${this.order.client}, Codice ordine cliente:${this.order.clientOrderCode}, Commessa:${this.order.commission} "
        val par = Paragraph(text)
        par.setHorizontalAlignment(HorizontalAlignment.CENTER);
        return par
    }

    private fun getInternals(): List<Table> {
        // maybe is unecessary 
        return this.order.internalOrders.map {
            this.internalAsHtml(it)
        }
    }

    fun generatePdf(): File {
        val outputFile = kotlin.io.path.createTempFile("pdf" + Clock.System.now().epochSeconds.toString(), ".pdf").toFile()
        outputFile.createNewFile()
        val pdfDocument = PdfDocument(PdfWriter(outputFile))
        val document = Document(pdfDocument)
        val header = this.getHeader()
        val internalpg = getInternals()
        document.add(header)
        internalpg.forEach { document.add(it) }
        document.close()

//        val inputStream = pdf.convert(input = getFullHtml(), output = outputFile) // will always return null if output is redirected

        return outputFile
    }


    private fun internalAsHtml(internal: SerializableInternalOrder): Table {


        val imgStart = createQrImage(encoder.encodeString("${internal.id}, start"))
        val imgEnd = createQrImage(encoder.encodeString("${internal.id}, end"))


        val info = "Codice prodotto interno: ${internal.productCode} x ${internal.productQuantity}\n" +
                "Codice prodotto grezzo: ${internal.rawCode} x ${internal.rawQuantity}\n" +
                "Lavorazioni: ${if (internal.processes != null && internal.processes.isNotEmpty()) internal.processes.joinToString("-> ") else "nessuna"}\n" +
                "Lavorazioni Esterne: ${internal.externalTreatments}\n" +
                "Operatore: ${internal.operator}\n"

        val table = Table(floatArrayOf(3f, 3f, 4f)).setAutoLayout()

        table.setTextAlignment(TextAlignment.LEFT)
        table.addCell(Cell().add(imgStart).setBorder(Border.NO_BORDER).setMarginRight(10f))
        table.addCell(Cell().add(imgEnd).setBorder(Border.NO_BORDER).setMarginRight(10f))
        table.addCell(Cell().add(Paragraph(info)).setBorder(Border.NO_BORDER))

        return table

    }


    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yyyy")
        return format.format(date)
    }

    private fun createQrImage(toPrint: String): Image {
        val baos = ByteArrayOutputStream()
        ImageIO.write(createQR(toPrint, 150, 150), "png", baos)

        val data: ImageData = ImageDataFactory.create(baos.toByteArray())
        return Image(data)
    }

    private fun createQR(
        data: String, height: Int, width: Int,
    ): BufferedImage {
        val matrix = MultiFormatWriter().encode(
            String(data.toByteArray(Charsets.UTF_8), Charsets.UTF_8),
            BarcodeFormat.QR_CODE, width, height)
        return MatrixToImageWriter.toBufferedImage(matrix)

    }

}
