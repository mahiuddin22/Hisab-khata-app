package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.PersonalDebtEntity
import com.example.data.model.TransactionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    fun generateAndShareMonthlyReport(
        context: Context,
        monthYearTitle: String,
        transactions: List<TransactionEntity>,
        debts: List<PersonalDebtEntity>,
        monthlyBudget: Double
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size in points: 595 x 842
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }

        // Background
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRect(0f, 0f, 595f, 842f, paint)

        // Top Header Banner
        paint.color = Color.rgb(15, 23, 42) // Deep Slate / Titanium
        canvas.drawRect(0f, 0f, 595f, 85f, paint)

        // Accent Gold Line
        paint.color = Color.rgb(217, 119, 6)
        canvas.drawRect(0f, 85f, 595f, 88f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("হিসাব খাতা (Hisab Khata)", 30f, 40f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawText("মাসিক ব্যয় বিবরণী ও আর্থিক রিপোর্ট • $monthYearTitle", 30f, 62f, paint)

        val currentDateStr = SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.getDefault()).format(Date())
        paint.textSize = 10f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("জেনারেট তারিখ: $currentDateStr", 565f, 62f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Calculations
        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        // 3 Summary Cards
        var startY = 105f
        val cardWidth = 165f
        val cardHeight = 65f

        // Card 1: Total Expense
        drawStatCard(canvas, paint, 30f, startY, cardWidth, cardHeight, "মোট খরচ (Expense)", "৳ ${formatAmount(totalExpense)}", Color.rgb(225, 29, 72), Color.rgb(255, 241, 242))

        // Card 2: Total Income
        drawStatCard(canvas, paint, 215f, startY, cardWidth, cardHeight, "মোট আয় (Income)", "৳ ${formatAmount(totalIncome)}", Color.rgb(5, 150, 105), Color.rgb(236, 253, 245))

        // Card 3: Net Balance
        val netColor = if (netBalance >= 0) Color.rgb(16, 185, 129) else Color.rgb(239, 68, 68)
        drawStatCard(canvas, paint, 400f, startY, cardWidth, cardHeight, "অবশিষ্ট স্থিতি (Net)", "৳ ${formatAmount(netBalance)}", netColor, Color.rgb(241, 245, 249))

        startY += cardHeight + 20f

        // Category Breakdown Section
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ক্যাটাগরি ভিত্তিক খরচের বিবরণ (Category Breakdown):", 30f, startY, paint)

        startY += 12f
        val expensesByCategory = transactions.filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }

        // Category Table Header
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRoundRect(RectF(30f, startY, 565f, startY + 22f), 4f, 4f, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ক্যাটাগরি", 40f, startY + 15f, paint)
        canvas.drawText("মোট খরচ (টাকা)", 320f, startY + 15f, paint)
        canvas.drawText("শতাংশ (%)", 480f, startY + 15f, paint)

        startY += 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 9.5f

        if (expensesByCategory.isEmpty()) {
            paint.color = Color.GRAY
            canvas.drawText("এই মাসে খরচের কোনো রেকর্ড পাওয়া যায়নি।", 40f, startY + 14f, paint)
            startY += 20f
        } else {
            for ((index, item) in expensesByCategory.take(6).withIndex()) {
                val rowColor = if (index % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                paint.color = rowColor
                canvas.drawRect(30f, startY, 565f, startY + 18f, paint)

                paint.color = Color.rgb(30, 41, 59)
                canvas.drawText(item.first, 40f, startY + 13f, paint)
                canvas.drawText("৳ ${formatAmount(item.second)}", 320f, startY + 13f, paint)

                val percentage = if (totalExpense > 0) (item.second / totalExpense * 100) else 0.0
                canvas.drawText(String.format(Locale.US, "%.1f%%", percentage), 480f, startY + 13f, paint)

                startY += 18f
            }
        }

        startY += 15f

        // Personal Ledger Section (দেনা-পাওনা)
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ব্যক্তিগত দেনা-পাওনা স্থিতি (Personal Ledger):", 30f, startY, paint)

        startY += 12f
        val totalReceivable = debts.filter { it.type == "RECEIVABLE" && !it.isSettled }.sumOf { it.remainingAmount }
        val totalPayable = debts.filter { it.type == "PAYABLE" && !it.isSettled }.sumOf { it.remainingAmount }

        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRoundRect(RectF(30f, startY, 565f, startY + 28f), 6f, 6f, paint)

        paint.color = Color.rgb(5, 150, 105)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("মোট পাওনা (Receivable): ৳ ${formatAmount(totalReceivable)}", 45f, startY + 18f, paint)

        paint.color = Color.rgb(225, 29, 72)
        canvas.drawText("মোট দেনা (Payable): ৳ ${formatAmount(totalPayable)}", 320f, startY + 18f, paint)

        startY += 40f

        // Itemized Transactions Table Header
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("সাম্প্রতিক লেনদেনের তালিকা (Transaction Log):", 30f, startY, paint)

        startY += 12f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRoundRect(RectF(30f, startY, 565f, startY + 22f), 4f, 4f, paint)

        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("তারিখ", 40f, startY + 15f, paint)
        canvas.drawText("বিবরণ", 115f, startY + 15f, paint)
        canvas.drawText("ক্যাটাগরি", 260f, startY + 15f, paint)
        canvas.drawText("পেমেন্ট মাধ্যম", 380f, startY + 15f, paint)
        canvas.drawText("পরিমাণ", 485f, startY + 15f, paint)

        startY += 24f
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        val listToShow = transactions.take(16)
        if (listToShow.isEmpty()) {
            paint.color = Color.GRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("কোন লেনদেন নেই", 40f, startY + 15f, paint)
            startY += 20f
        } else {
            for ((idx, tx) in listToShow.withIndex()) {
                val rowBg = if (idx % 2 == 0) Color.WHITE else Color.rgb(248, 250, 252)
                paint.color = rowBg
                canvas.drawRect(30f, startY, 565f, startY + 19f, paint)

                paint.color = Color.rgb(51, 65, 85)
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(dateFormat.format(Date(tx.dateMillis)), 40f, startY + 13f, paint)

                val displayTitle = if (tx.title.length > 20) tx.title.take(18) + ".." else tx.title
                canvas.drawText(displayTitle, 115f, startY + 13f, paint)
                canvas.drawText(tx.category, 260f, startY + 13f, paint)
                canvas.drawText(tx.paymentMethod, 380f, startY + 13f, paint)

                val amountColor = if (tx.type == "INCOME") Color.rgb(5, 150, 105) else Color.rgb(225, 29, 72)
                paint.color = amountColor
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                val sign = if (tx.type == "INCOME") "+" else "-"
                canvas.drawText("$sign৳ ${formatAmount(tx.amount)}", 485f, startY + 13f, paint)

                startY += 19f
            }
        }

        // Footer at bottom
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("হিসাব খাতা • স্বয়ংক্রিয় আর্থিক হিসাব ও বাজেট ট্র্যাকার • গোপনীয় ও সুরক্ষিত", 297.5f, 825f, paint)

        pdfDocument.finishPage(page)

        // Write to file
        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) reportsDir.mkdirs()

        val fileName = "HisabKhata_Report_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(reportsDir, fileName)

        try {
            val fos = FileOutputStream(pdfFile)
            pdfDocument.writeTo(fos)
            fos.flush()
            fos.close()
            pdfDocument.close()
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }

    private fun drawStatCard(
        canvas: Canvas,
        paint: Paint,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        value: String,
        accentColor: Int,
        bgColor: Int
    ) {
        val rect = RectF(x, y, x + width, y + height)
        paint.color = bgColor
        canvas.drawRoundRect(rect, 8f, 8f, paint)

        // Left accent stripe
        paint.color = accentColor
        canvas.drawRoundRect(RectF(x, y, x + 4f, y + height), 4f, 4f, paint)

        // Label
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(label, x + 12f, y + 20f, paint)

        // Value
        paint.color = accentColor
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + 12f, y + 45f, paint)
    }

    private fun formatAmount(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            String.format(Locale.US, "%,.0f", amount)
        } else {
            String.format(Locale.US, "%,.2f", amount)
        }
    }

    fun sharePdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "হিসাব খাতা - মাসিক ব্যয় বিবরণী")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "পিডিএফ রিপোর্ট শেয়ার বা সংরক্ষণ করুন")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun viewPdf(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            sharePdf(context, file)
        }
    }
}
