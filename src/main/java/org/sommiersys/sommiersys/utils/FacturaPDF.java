package org.sommiersys.sommiersys.utils;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.pack.sommierJar.entity.facturaCabecera.FacturaCabeceraEntity;
import org.pack.sommierJar.entity.facturaDetalle.FacturaDetalleEntity;

import java.io.IOException;
import java.util.List;
public class FacturaPDF {
    public PDDocument generarFactura(FacturaCabeceraEntity factura) {
        PDFont pdfFont=  new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        try {
            // Crear un documento PDF
            PDDocument document = new PDDocument();
            PDPage page = new PDPage();
            document.addPage(page);

            // Crear un flujo de contenido en la página PDF
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Agregar contenido a la factura (nombre del cliente, fecha, detalles, total, etc.)
            contentStream.beginText();
            contentStream.setFont(pdfFont, 12);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Factura para: " + factura.getCliente().getNombre());
            contentStream.newLineAtOffset(0, -20);
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Detalles de la factura:");
            // Agregar detalles de la factura
            List<FacturaDetalleEntity> detalles = factura.getFacturaDetalles();
            double total = 0.0;
            for (FacturaDetalleEntity detalle : detalles) {
                contentStream.newLineAtOffset(0, -20);
                total += detalle.getSubtotal();
            }

            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Total: $" + total);
            contentStream.endText();

            // Cerrar el flujo de contenido
            contentStream.close();

            // Guardar el documento PDF en un archivo o transmitirlo
            // document.save("factura.pdf");

            // Cerrar el documento PDF
            //document.close();
            return  document;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}