package com.example.healthflow.service;

import com.example.healthflow.model.AuditLog;
import com.example.healthflow.model.User;
import com.example.healthflow.repository.AuditLogRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public AuditLog logActivity(String action, String entityType, Long entityId, String description, User user, String ipAddress, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setUser(user);
        log.setIpAddress(ipAddress);
        log.setDetails(details);
        log.setCreatedAt(LocalDateTime.now());
        
        return auditLogRepository.save(log);
    }

    @Override
    public List<AuditLog> findByUser(User user) {
        return auditLogRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<AuditLog> findByEntityTypeAndId(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    @Override
    public List<AuditLog> findByAction(String action) {
        return auditLogRepository.findByActionOrderByCreatedAtDesc(action);
    }

    @Override
    public List<AuditLog> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    @Override
    public Page<AuditLog> findAllPaginated(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public AuditLog findById(Long id) {
        return auditLogRepository.findById(id).orElse(null);
    }

    @Override
    public byte[] exportLogsToPdf(List<AuditLog> logs) {
        try {
            Document document = new Document(PageSize.A4.rotate());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            
            // Add header and footer
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    
                    // Header
                    cb.saveState();
                    String header = "HealthFlow Audit Logs";
                    float headerWidth = document.getPageSize().getWidth();
                    cb.beginText();
                    cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12).getBaseFont(), 12);
                    cb.showTextAligned(Element.ALIGN_CENTER, header, headerWidth / 2, document.getPageSize().getHeight() - 20, 0);
                    cb.endText();
                    
                    // Footer
                    String footer = "Page " + writer.getPageNumber();
                    cb.beginText();
                    cb.setFontAndSize(FontFactory.getFont(FontFactory.HELVETICA, 10).getBaseFont(), 10);
                    cb.showTextAligned(Element.ALIGN_CENTER, footer, (document.getPageSize().getWidth() / 2), document.bottomMargin() - 10, 0);
                    cb.endText();
                    cb.restoreState();
                }
            });
            
            document.open();
            
            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Paragraph title = new Paragraph("Audit Log Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Add timestamp
            Font timestampFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.ITALIC, BaseColor.DARK_GRAY);
            Paragraph timestamp = new Paragraph("Generated on: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), timestampFont);
            timestamp.setAlignment(Element.ALIGN_RIGHT);
            timestamp.setSpacingAfter(15);
            document.add(timestamp);
            
            // Add table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            
            // Set column widths
            float[] columnWidths = {0.5f, 0.8f, 1.2f, 2.5f, 1f, 1.5f};
            table.setWidths(columnWidths);
            
            // Add table headers
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
            
            PdfPCell headerCell;
            String[] headers = {"ID", "Action", "Entity", "Description", "User", "Timestamp"};
            for (String header : headers) {
                headerCell = new PdfPCell(new Phrase(header, headerFont));
                headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
                headerCell.setPaddingTop(5);
                headerCell.setPaddingBottom(5);
                headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(headerCell);
            }
            
            // Add data rows
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.BLACK);
            PdfPCell cell;
            
            for (AuditLog log : logs) {
                // ID
                cell = new PdfPCell(new Phrase(log.getId().toString(), dataFont));
                cell.setPadding(5);
                table.addCell(cell);
                
                // Action
                cell = new PdfPCell(new Phrase(log.getAction(), dataFont));
                cell.setPadding(5);
                table.addCell(cell);
                
                // Entity
                String entity = log.getEntityType() + (log.getEntityId() != null ? " #" + log.getEntityId() : "");
                cell = new PdfPCell(new Phrase(entity, dataFont));
                cell.setPadding(5);
                table.addCell(cell);
                
                // Description
                cell = new PdfPCell(new Phrase(log.getDescription(), dataFont));
                cell.setPadding(5);
                table.addCell(cell);
                
                // User
                String username = log.getUser() != null ? log.getUser().getUsername() : "System";
                cell = new PdfPCell(new Phrase(username, dataFont));
                cell.setPadding(5);
                table.addCell(cell);
                
                // Timestamp
                String timestampStr = log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                cell = new PdfPCell(new Phrase(timestampStr, dataFont));
                cell.setPadding(5);
                table.addCell(cell);
            }
            
            document.add(table);
            
            // Add metadata
            Font metadataFont = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);
            Paragraph metadata = new Paragraph("Total records: " + logs.size(), metadataFont);
            metadata.setAlignment(Element.ALIGN_RIGHT);
            document.add(metadata);
            
            document.close();
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating PDF", e);
            return new byte[0];
        }
    }
} 