package com.example.kiwi.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.kiwi.R
import com.example.kiwi.viewmodel.SharedViewModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PdfGenerator(private val context: Context) {

    private val pageWidth = 595
    private val pageHeight = 842
    private val marginBottom = 50f

    fun generarFactura(solicitud: SharedViewModel.Solicitud) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()
        val dataPaint = Paint()

        titlePaint.textSize = 20f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.color = Color.BLACK
        titlePaint.textAlign = Paint.Align.RIGHT

        paint.textSize = 12f
        paint.color = Color.DKGRAY
        paint.textAlign = Paint.Align.RIGHT

        dataPaint.textSize = 12f
        dataPaint.color = Color.BLACK
        dataPaint.textAlign = Paint.Align.LEFT
        dataPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = 60f

        fun dibujarCabecera(isFirstPage: Boolean) {
            if (isFirstPage) {

                try {
                    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logo_kiwi)
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, false)
                    canvas.drawBitmap(scaledBitmap, 40f, 40f, null)
                } catch (e: Exception) { e.printStackTrace() }

                var headerY = 55f
                canvas.drawText("KIWI MODA INC.", 550f, headerY, titlePaint)
                headerY += 18f
                paint.color = Color.GRAY
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("Zona Libre, Calle 15, Avenida Santa Isabel", 550f, headerY, paint)
                headerY += 15f
                canvas.drawText("Frente al Banco Davivienda", 550f, headerY, paint)
                headerY += 15f
                canvas.drawText("Colón, República de Panamá", 550f, headerY, paint)
                headerY += 15f
                canvas.drawText("Teléfono: 447-3994", 550f, headerY, paint)
                headerY += 15f
                canvas.drawText("Email: Kiwimodainc@gmail.com", 550f, headerY, paint)

                y = headerY + 50f
            } else {
                y = 60f
                paint.color = Color.GRAY
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("KIWI MODA INC.", 550f, y, paint)
                y += 40f
            }
        }

        dibujarCabecera(true)

        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(40f, y, 550f, y, paint)

        y += 20f
        dataPaint.textSize = 14f
        canvas.drawText("DETALLES DEL PEDIDO", 40f, y, dataPaint)

        y += 10f
        canvas.drawLine(40f, y, 550f, y, paint)

        y += 30f
        dataPaint.textSize = 12f

        canvas.drawText("CLIENTE:", 40f, y, dataPaint)
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.BLACK
        canvas.drawText(solicitud.comprador.uppercase(), 120f, y, paint)

        y += 20f
        canvas.drawText("CELULAR:", 40f, y, dataPaint)
        canvas.drawText(solicitud.celular, 120f, y, paint)

        val yFecha = y - 20f
        paint.textAlign = Paint.Align.RIGHT
        dataPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("FECHA:", 480f, yFecha, dataPaint)
        canvas.drawText(solicitud.fecha, 550f, yFecha, paint)
        canvas.drawText("HORA:", 480f, y, dataPaint)
        canvas.drawText(solicitud.hora, 550f, y, paint)

        y += 40f

        fun dibujarEncabezadosTabla() {
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val headerPaint = Paint()
            headerPaint.color = Color.parseColor("#EEEEEE")
            canvas.drawRect(40f, y - 15f, 550f, y + 10f, headerPaint)
            canvas.drawText("DESCRIPCIÓN", 50f, y, paint)
            canvas.drawText("CANT.", 330f, y, paint)
            canvas.drawText("PRECIO DOC.", 400f, y, paint)
            canvas.drawText("TOTAL", 502f, y, paint)

            y += 30f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        dibujarEncabezadosTabla()

        solicitud.productos.forEach { item ->
            if (y > pageHeight - marginBottom) {
                paint.color = Color.GRAY
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = 10f
                canvas.drawText("Pág. $pageNumber", pageWidth / 2f, pageHeight - 20f, paint)

                pdfDocument.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas

                dibujarCabecera(false)
                dibujarEncabezadosTabla()
            }

            val nombre = item.producto?.nombre ?: "Item"
            val referencia = item.producto?.referencia ?: ""
            val precioDocena = item.producto?.precio ?: 0.0

            val descripcion = "$nombre | $referencia"
            val descCorto = if (descripcion.length > 35) descripcion.substring(0, 35) + "..." else descripcion

            val totalItem = precioDocena * item.cantidad

            paint.textAlign = Paint.Align.LEFT
            paint.color = Color.BLACK

            canvas.drawText(descCorto, 50f, y, paint)
            canvas.drawText("${item.cantidad} dncs.", 327f, y, paint)
            canvas.drawText("$${String.format("%.2f", precioDocena)}", 415f, y, paint)
            canvas.drawText("$${String.format("%.2f", totalItem)}", 500f, y, paint)

            y += 25f
        }

        y += 10f
        paint.color = Color.BLACK
        canvas.drawLine(40f, y, 550f, y, paint)
        y += 30f

        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL A PAGAR: $${String.format("%.2f", solicitud.total)}", 550f, y, paint)

        if (y + 300 > pageHeight - marginBottom) {
            paint.color = Color.GRAY
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = 10f
            canvas.drawText("Pág. $pageNumber", pageWidth / 2f, pageHeight - 20f, paint)

            pdfDocument.finishPage(page)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            page = pdfDocument.startPage(pageInfo)
            canvas = page.canvas

            dibujarCabecera(false)
        }

        y += 60f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val paymentBgPaint = Paint()
        paymentBgPaint.color = Color.parseColor("#F5F5F5")
        canvas.drawRect(30f, y - 20f, 565f, y + 230f, paymentBgPaint)

        canvas.drawText("MÉTODOS DE PAGO", 40f, y, paint)

        y += 25f
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("⬤ Opción 1: Pago en efectivo directamente en el local.", 40f, y, paint)
        y += 20f
        canvas.drawText("⬤ Opción 2: Transferencia Bancaria a las siguientes cuentas:", 40f, y, paint)

        y += 20f

        try {
            val logoBG = BitmapFactory.decodeResource(context.resources, R.drawable.logobg)
            val scaledBG = Bitmap.createScaledBitmap(logoBG, 40, 40, false)
            canvas.drawBitmap(scaledBG, 50f, y, null)
        } catch (e: Exception) { }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BANCO GENERAL", 100f, y + 15f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Mohamad Hamadeh | AHORRO | 04-27-97-152529-7", 100f, y + 30f, paint)

        y += 50f

        try {
            val logoBan = BitmapFactory.decodeResource(context.resources, R.drawable.logobanitsmo)
            val scaledBan = Bitmap.createScaledBitmap(logoBan, 40, 40, false)
            canvas.drawBitmap(scaledBan, 50f, y, null)
        } catch (e: Exception) { }

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BANISTMO", 100f, y + 15f, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Mohamad Hamadeh | AHORRO | 01-14-669-089", 100f, y + 30f, paint)

        y += 60f
        paint.color = Color.RED
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        paint.textSize = 11f
        canvas.drawText("IMPORTANTE:", 40f, y, paint)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Si transfiere desde OTRO BANCO, debe realizarse por ACH EXPRESS.", 130f, y, paint)
        y += 15f
        canvas.drawText("De lo contrario el pago no se reflejará de inmediato y no se le podrá entregar la mercancía,", 40f, y, paint)
        y += 15f
        canvas.drawText("se le contactará luego reflejarse el monto y entonces podrá retirar su mercancía.", 40f, y, paint)

        y += 75f
        paint.color = Color.BLACK
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("¡MUCHAS GRACIAS POR SU COMPRA!", pageWidth / 2f, y, paint)

        paint.color = Color.GRAY
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Este documento es un comprobante de pedido, no es una factura fiscal. - Pág. $pageNumber", pageWidth / 2f, pageHeight - 40f, paint)

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Kiwi_Pedido_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "Comprobante generado", Toast.LENGTH_SHORT).show()
            abrirPdf(file)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
        }
        pdfDocument.close()
    }

    private fun abrirPdf(file: File) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.setDataAndType(uri, "application/pdf")
            intent.flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No hay aplicación para ver PDF", Toast.LENGTH_LONG).show()
        }
    }
}