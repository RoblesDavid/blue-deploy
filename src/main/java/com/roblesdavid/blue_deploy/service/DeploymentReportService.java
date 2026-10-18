package com.roblesdavid.blue_deploy.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.roblesdavid.blue_deploy.model.Deployment;
import com.roblesdavid.blue_deploy.model.DeploymentStep;
import com.roblesdavid.blue_deploy.model.SecurityCheck;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio para generar informes ejecutivos y técnicos de despliegue en formato PDF.
 */
@Service
public class DeploymentReportService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Color PRIMARY_COLOR = new Color(24, 76, 120);
    private static final Color SECONDARY_COLOR = new Color(51, 65, 85);
    private static final Color HEADER_BG = new Color(241, 245, 249);
    private static final Color BORDER_COLOR = new Color(203, 213, 225);
    private static final Color SUCCESS_COLOR = new Color(22, 101, 52);
    private static final Color ERROR_COLOR = new Color(153, 27, 27);
    private static final Color WARNING_COLOR = new Color(133, 77, 14);

    public byte[] generateDeploymentReport(Deployment deployment, List<DeploymentStep> steps, List<SecurityCheck> checks) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, out);
            document.open();

            // Tipografías
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, PRIMARY_COLOR);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, SECONDARY_COLOR);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

            // Cabecera Principal
            Paragraph title = new Paragraph("BLUEDEPLOY — INFORME DE DESPLIEGUE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Generado el: " + LocalDateTime.now().format(DATE_FMT) + " | Blue Team Security Suite", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(15);
            document.add(subtitle);

            // Resumen de Identificación y Metadatos
            Paragraph metaHeading = new Paragraph("1. Información del Despliegue", sectionFont);
            metaHeading.setSpacingAfter(6);
            document.add(metaHeading);

            PdfPTable infoTable = new PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{25, 25, 25, 25});

            addInfoCell(infoTable, "Deployment ID:", boldFont, deployment.getDeploymentId(), regularFont);
            addInfoCell(infoTable, "Hostname:", boldFont, deployment.getHostname(), regularFont);
            addInfoCell(infoTable, "Dispositivo:", boldFont, deployment.getDeviceModel() != null ? deployment.getDeviceModel() : "N/D", regularFont);
            addInfoCell(infoTable, "Dominio:", boldFont, deployment.getDomainName() != null ? deployment.getDomainName() : "N/D", regularFont);

            String techName = deployment.getTechnician() != null ? deployment.getTechnician().getName() + " (" + deployment.getTechnician().getUsername() + ")" : "No asignado";
            addInfoCell(infoTable, "Técnico Asignado:", boldFont, techName, regularFont);
            addInfoCell(infoTable, "Fase Actual:", boldFont, deployment.getCurrentStep() != null ? deployment.getCurrentStep() : "N/D", regularFont);

            String statusStr = deployment.getDeploymentStatus() != null ? deployment.getDeploymentStatus().name() : "UNKNOWN";
            addInfoCell(infoTable, "Estado Global:", boldFont, statusStr, getStatusFont(statusStr));

            String compStr = deployment.getComplianceStatus() != null ? deployment.getComplianceStatus().name() : "UNKNOWN";
            addInfoCell(infoTable, "Compliance:", boldFont, compStr, getStatusFont(compStr));

            addInfoCell(infoTable, "Inicio:", boldFont, deployment.getStartedAt() != null ? deployment.getStartedAt().format(DATE_FMT) : "—", regularFont);
            addInfoCell(infoTable, "Fin:", boldFont, deployment.getFinishedAt() != null ? deployment.getFinishedAt().format(DATE_FMT) : "—", regularFont);
            addInfoCell(infoTable, "Último latido:", boldFont, deployment.getLastSeenAt() != null ? deployment.getLastSeenAt().format(DATE_FMT) : "—", regularFont);
            addInfoCell(infoTable, "Puntos de Control:", boldFont, steps.size() + " fases / " + checks.size() + " checks", regularFont);

            infoTable.setSpacingAfter(15);
            document.add(infoTable);

            // Tabla de Fases / Steps
            Paragraph stepsHeading = new Paragraph("2. Línea Temporal de Fases OSDCloud", sectionFont);
            stepsHeading.setSpacingAfter(6);
            document.add(stepsHeading);

            PdfPTable stepsTable = new PdfPTable(5);
            stepsTable.setWidthPercentage(100);
            stepsTable.setWidths(new float[]{8, 27, 15, 35, 15});

            addTableHeader(stepsTable, "#", headerFont);
            addTableHeader(stepsTable, "Fase / Step", headerFont);
            addTableHeader(stepsTable, "Estado", headerFont);
            addTableHeader(stepsTable, "Detalle / Mensaje", headerFont);
            addTableHeader(stepsTable, "Fecha", headerFont);

            if (steps.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No hay registros de pasos para este despliegue.", regularFont));
                emptyCell.setColspan(5);
                emptyCell.setPadding(6);
                stepsTable.addCell(emptyCell);
            } else {
                int stepIndex = 1;
                for (DeploymentStep s : steps) {
                    addTableCell(stepsTable, String.valueOf(stepIndex++), cellFont);
                    addTableCell(stepsTable, s.getStepName(), cellFont);
                    addTableCell(stepsTable, s.getStepStatus() != null ? s.getStepStatus().name() : "-", getStatusFont(s.getStepStatus() != null ? s.getStepStatus().name() : ""));
                    String msg = (s.getMessage() != null ? s.getMessage() : "") + (s.getErrorCode() != null ? " [Err: " + s.getErrorCode() + "]" : "");
                    addTableCell(stepsTable, msg, cellFont);
                    addTableCell(stepsTable, s.getReportedAt() != null ? s.getReportedAt().format(DATE_FMT) : "-", cellFont);
                }
            }
            stepsTable.setSpacingAfter(15);
            document.add(stepsTable);

            // Tabla de Checks de Seguridad
            Paragraph checksHeading = new Paragraph("3. Verificaciones de Seguridad y Cumplimiento", sectionFont);
            checksHeading.setSpacingAfter(6);
            document.add(checksHeading);

            PdfPTable checksTable = new PdfPTable(6);
            checksTable.setWidthPercentage(100);
            checksTable.setWidths(new float[]{25, 14, 12, 17, 17, 15});

            addTableHeader(checksTable, "Check", headerFont);
            addTableHeader(checksTable, "Estado", headerFont);
            addTableHeader(checksTable, "Severidad", headerFont);
            addTableHeader(checksTable, "Esperado", headerFont);
            addTableHeader(checksTable, "Obtenido", headerFont);
            addTableHeader(checksTable, "Fecha", headerFont);

            if (checks.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No hay checks de seguridad registrados.", regularFont));
                emptyCell.setColspan(6);
                emptyCell.setPadding(6);
                checksTable.addCell(emptyCell);
            } else {
                for (SecurityCheck c : checks) {
                    addTableCell(checksTable, c.getCheckName(), cellFont);
                    addTableCell(checksTable, c.getCheckStatus() != null ? c.getCheckStatus().name() : "-", getStatusFont(c.getCheckStatus() != null ? c.getCheckStatus().name() : ""));
                    addTableCell(checksTable, c.getSeverity() != null ? c.getSeverity().name() : "-", cellFont);
                    addTableCell(checksTable, c.getExpectedValue() != null ? c.getExpectedValue() : "-", cellFont);
                    addTableCell(checksTable, c.getActualValue() != null ? c.getActualValue() : "-", cellFont);
                    addTableCell(checksTable, c.getReportedAt() != null ? c.getReportedAt().format(DATE_FMT) : "-", cellFont);
                }
            }
            checksTable.setSpacingAfter(20);
            document.add(checksTable);

            // Pie de página de conformidad
            Paragraph footer = new Paragraph("Este documento constituye un informe técnico y de auditoría generado automáticamente por BlueDeploy. Confidencial.", subtitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException("Error al generar reporte PDF: " + ex.getMessage(), ex);
        }
    }

    private void addInfoCell(PdfPTable table, String label, Font labelFont, String value, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(4);
        cell.addElement(new Paragraph(label, labelFont));
        cell.addElement(new Paragraph(value != null ? value : "—", valueFont));
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String header, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(header, font));
        cell.setBackgroundColor(HEADER_BG);
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBorderColor(BORDER_COLOR);
        cell.setPadding(4);
        table.addCell(cell);
    }

    private Font getStatusFont(String status) {
        if (status.contains("COMPLETED") || status.contains("PASSED") || status.contains("SUCCESS") || status.contains("COMPLIANT") && !status.contains("NON")) {
            return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, SUCCESS_COLOR);
        } else if (status.contains("FAILED") || status.contains("NON_COMPLIANT") || status.contains("ERROR")) {
            return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, ERROR_COLOR);
        } else if (status.contains("WARNING") || status.contains("PENDING")) {
            return FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, WARNING_COLOR);
        }
        return FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
    }
}
