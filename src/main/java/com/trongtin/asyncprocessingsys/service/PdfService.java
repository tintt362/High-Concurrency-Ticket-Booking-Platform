package com.trongtin.asyncprocessingsys.service;


import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.trongtin.asyncprocessingsys.dto.request.PdfPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class PdfService {

    @Value("${app.storage.pdf-dir}")
    private String pdfDir;

    @Value("${app.storage.base-url}")
    private String baseUrl;

    // Màu chủ đạo — xanh đậm
    private static final DeviceRgb COLOR_PRIMARY = new DeviceRgb(44, 62, 80);
    // Màu header table
    private static final DeviceRgb COLOR_TABLE_HEADER = new DeviceRgb(52, 73, 94);
    // Màu row chẵn
    private static final DeviceRgb COLOR_ROW_EVEN = new DeviceRgb(236, 240, 241);
    // Màu text nhạt
    private static final DeviceRgb COLOR_TEXT_LIGHT = new DeviceRgb(127, 140, 141);

    // ─────────────────────────────────────────────────────────
    // Entry point — PdfWorker gọi vào đây
    // Trả về URL để client download
    // ─────────────────────────────────────────────────────────
    public String generate(UUID jobId, PdfPayload payload) throws Exception {
        // Tạo thư mục nếu chưa có
        File dir = new File(pdfDir);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("[PdfService] Created directory | path={}", dir.getAbsolutePath());
        }

        // Tên file duy nhất theo jobId
        String fileName = "report-" + jobId + ".pdf";
        String filePath = pdfDir + File.separator + fileName;
        String downloadUrl = baseUrl + "/" + fileName;

        // Generate PDF theo loại báo cáo
        switch (payload.getReportType()) {
            case "monthly_revenue" -> generateMonthlyRevenue(filePath, payload);
            case "invoice" -> generateInvoice(filePath, payload);
            default -> generateGeneric(filePath, payload);
        }

        log.info("[PdfService] Generated | file={} | url={}", filePath, downloadUrl);
        return downloadUrl;
    }

    // ─────────────────────────────────────────────────────────
    // Template 1: Báo cáo doanh thu tháng
    // ─────────────────────────────────────────────────────────
    private void generateMonthlyRevenue(String filePath, PdfPayload payload)
            throws Exception {

        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            document.setMargins(40, 50, 40, 50);
            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // ── Header ──
            addHeader(document, fontBold, fontRegular,
                    payload.getTitle() != null
                            ? payload.getTitle()
                            : "Báo cáo doanh thu",
                    payload.getPeriod());

            // ── Thông tin báo cáo ──
            addInfoSection(document, fontRegular, fontBold, payload);

            // ── Bảng doanh thu ──
            document.add(new Paragraph("\nChi tiết doanh thu:")
                    .setFont(fontBold)
                    .setFontSize(12)
                    .setFontColor(COLOR_PRIMARY)
                    .setMarginBottom(8));

            // Header bảng: Danh mục | Số lượng | Đơn giá | Thành tiền
            float[] cols = {200f, 80f, 120f, 130f};
            Table table = new Table(UnitValue.createPointArray(cols));
            table.setWidth(UnitValue.createPercentValue(100));

            addTableHeader(table, fontBold,
                    "Danh mục", "Số lượng", "Đơn giá", "Thành tiền");

            // Dữ liệu mẫu — thực tế query từ DB theo payload
            addTableRow(table, fontRegular, 0,
                    "Sản phẩm A", "150", "500,000", "75,000,000");
            addTableRow(table, fontRegular, 1,
                    "Sản phẩm B", "80", "1,200,000", "96,000,000");
            addTableRow(table, fontRegular, 0,
                    "Dịch vụ tư vấn", "12", "5,000,000", "60,000,000");
            addTableRow(table, fontRegular, 1,
                    "Phí vận chuyển", "230", "50,000", "11,500,000");

            document.add(table);

            // ── Tổng kết ──
            addSummarySection(document, fontBold, fontRegular);

            // ── Footer ──
            addFooter(document, fontRegular, payload);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Template 2: Hoá đơn
    // ─────────────────────────────────────────────────────────
    private void generateInvoice(String filePath, PdfPayload payload)
            throws Exception {

        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            document.setMargins(40, 50, 40, 50);
            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // ── Tiêu đề hoá đơn ──
            document.add(new Paragraph("HOÁ ĐƠN BÁN HÀNG")
                    .setFont(fontBold)
                    .setFontSize(22)
                    .setFontColor(COLOR_PRIMARY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(4));

            document.add(new Paragraph("Kỳ: " + payload.getPeriod())
                    .setFont(fontRegular)
                    .setFontSize(11)
                    .setFontColor(COLOR_TEXT_LIGHT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(24));

            // ── Thông tin khách hàng ──
            document.add(new Paragraph("Khách hàng: " + payload.getRequestedBy())
                    .setFont(fontBold).setFontSize(12).setMarginBottom(4));
            document.add(new Paragraph("Ngày xuất: " +
                    LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                    .setFont(fontRegular).setFontSize(11).setMarginBottom(20));

            // ── Bảng hàng hoá ──
            float[] cols = {240f, 80f, 120f, 100f};
            Table table = new Table(UnitValue.createPointArray(cols));
            table.setWidth(UnitValue.createPercentValue(100));

            addTableHeader(table, fontBold,
                    "Mô tả dịch vụ", "SL", "Đơn giá", "Tổng");

            addTableRow(table, fontRegular, 0,
                    "Gói dịch vụ Premium", "1", "2,000,000", "2,000,000");
            addTableRow(table, fontRegular, 1,
                    "Phí setup hệ thống", "1", "500,000", "500,000");
            addTableRow(table, fontRegular, 0,
                    "Hỗ trợ kỹ thuật (3 tháng)", "1", "1,500,000", "1,500,000");

            document.add(table);

            // ── Tổng hoá đơn ──
            document.add(new Paragraph("\nTổng cộng: 4,000,000 VND")
                    .setFont(fontBold).setFontSize(14)
                    .setFontColor(COLOR_PRIMARY)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(12));

            addFooter(document, fontRegular, payload);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Template 3: Generic — dùng khi reportType không khớp
    // ─────────────────────────────────────────────────────────
    private void generateGeneric(String filePath, PdfPayload payload)
            throws Exception {

        try (PdfWriter writer = new PdfWriter(filePath);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            document.setMargins(40, 50, 40, 50);
            PdfFont fontRegular = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            addHeader(document, fontBold, fontRegular,
                    payload.getTitle() != null ? payload.getTitle() : "Báo cáo",
                    payload.getPeriod());
            addInfoSection(document, fontRegular, fontBold, payload);

            if (payload.getSummary() != null) {
                document.add(new Paragraph(payload.getSummary())
                        .setFont(fontRegular).setFontSize(11))
                ;
            }

            addFooter(document, fontRegular, payload);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Helper methods — dùng chung cho nhiều template
    // ─────────────────────────────────────────────────────────

    private void addHeader(Document doc, PdfFont bold, PdfFont regular,
                           String title, String period) throws Exception {
        // Line trang trí trên cùng
        doc.add(new Paragraph(" ")
                .setBackgroundColor(COLOR_PRIMARY)
                .setHeight(4)
                .setMarginBottom(16));

        doc.add(new Paragraph(title)
                .setFont(bold)
                .setFontSize(20)
                .setFontColor(COLOR_PRIMARY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(4));

        if (period != null) {
            doc.add(new Paragraph("Kỳ báo cáo: " + period)
                    .setFont(regular)
                    .setFontSize(11)
                    .setFontColor(COLOR_TEXT_LIGHT)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
        }
    }

    private void addInfoSection(Document doc, PdfFont regular, PdfFont bold,
                                PdfPayload payload) throws Exception {
        String createdAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        // Bảng 2 cột thông tin
        Table info = new Table(new float[]{150f, 350f});
        info.setWidth(UnitValue.createPercentValue(100));
        info.setMarginBottom(20);
        info.setBorder(Border.NO_BORDER);

        addInfoRow(info, regular, bold, "Ngày tạo:", createdAt);
        addInfoRow(info, regular, bold, "Người yêu cầu:",
                payload.getRequestedBy() != null ? payload.getRequestedBy() : "—");
        addInfoRow(info, regular, bold, "Loại báo cáo:",
                payload.getReportType() != null ? payload.getReportType() : "—");

        doc.add(info);

        // Đường kẻ phân cách
        doc.add(new Paragraph(" ")
                .setBorderBottom(new SolidBorder(COLOR_TEXT_LIGHT, 0.5f))
                .setMarginBottom(16));
    }

    private void addInfoRow(Table table, PdfFont regular, PdfFont bold,
                            String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(bold).setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setPadding(3));
        table.addCell(new Cell()
                .add(new Paragraph(value).setFont(regular).setFontSize(10))
                .setBorder(Border.NO_BORDER)
                .setPadding(3));
    }

    private void addTableHeader(Table table, PdfFont bold, String... headers) {
        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header)
                            .setFont(bold)
                            .setFontSize(11)
                            .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(COLOR_TABLE_HEADER)
                    .setPadding(8)
                    .setBorder(Border.NO_BORDER));
        }
    }

    private void addTableRow(Table table, PdfFont regular,
                             int rowIndex, String... values) {
        // Màu xen kẽ cho dễ đọc
        DeviceRgb rowColor = (rowIndex % 2 == 0)
                ? (DeviceRgb) ColorConstants.WHITE
                : COLOR_ROW_EVEN;

        for (String value : values) {
            table.addCell(new Cell()
                    .add(new Paragraph(value)
                            .setFont(regular)
                            .setFontSize(10))
                    .setBackgroundColor(rowColor)
                    .setPadding(7)
                    .setBorder(Border.NO_BORDER));
        }
    }

    private void addSummarySection(Document doc, PdfFont bold, PdfFont regular)
            throws Exception {
        doc.add(new Paragraph("\nTổng kết:")
                .setFont(bold).setFontSize(12)
                .setFontColor(COLOR_PRIMARY).setMarginTop(16));

        Table summary = new Table(new float[]{300f, 200f});
        summary.setWidth(UnitValue.createPercentValue(60));
        summary.setHorizontalAlignment(
                com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);

        addSummaryRow(summary, regular, bold, "Tổng doanh thu:", "242,500,000 VND");
        addSummaryRow(summary, regular, bold, "Thuế VAT (10%):", "24,250,000 VND");
        addSummaryRow(summary, regular, bold, "Thực nhận:", "218,250,000 VND");

        doc.add(summary);
    }

    private void addSummaryRow(Table table, PdfFont regular, PdfFont bold,
                               String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setFont(regular).setFontSize(10))
                .setBorder(Border.NO_BORDER).setPadding(5));
        table.addCell(new Cell()
                .add(new Paragraph(value).setFont(bold).setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER).setPadding(5));
    }

    private void addFooter(Document doc, PdfFont regular, PdfPayload payload)
            throws Exception {
        doc.add(new Paragraph("\n\n")
                .setBorderTop(new SolidBorder(COLOR_TEXT_LIGHT, 0.5f))
                .setMarginTop(24));

        doc.add(new Paragraph(
                "Tài liệu được tạo tự động bởi Async Job System. "
                        + "Ngày: " + LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setFont(regular)
                .setFontSize(9)
                .setFontColor(COLOR_TEXT_LIGHT)
                .setTextAlignment(TextAlignment.CENTER));
    }
}