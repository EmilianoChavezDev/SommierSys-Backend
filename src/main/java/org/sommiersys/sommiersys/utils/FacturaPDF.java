package org.sommiersys.sommiersys.utils;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.pack.sommierJar.entity.facturaCabecera.FacturaCabeceraEntity;
import org.pack.sommierJar.entity.facturaDetalle.FacturaDetalleEntity;
import org.sommiersys.sommiersys.repository.facturaDetalle.FacturaDetalleRepository;

import java.io.IOException;
import java.util.List;
public class FacturaPDF {

    FacturaDetalleRepository detalleRepository;

    public FacturaPDF(FacturaDetalleRepository detalleRepository) {
        this.detalleRepository = detalleRepository;
    }

    public PDDocument generarFactura(FacturaCabeceraEntity factura) {
        PDFont pdfFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.beginText();
            contentStream.setFont(pdfFont, 12);
            contentStream.newLineAtOffset(100, 700);

            // Agregar información de la factura
            contentStream.showText("Factura para: " + factura.getCliente().getNombre());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Fecha: " + factura.getFecha());
            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Detalles de la factura:");

            // Agregar detalles de la factura
            double total = 0.0;
//            if (factura.getFacturaDetalles().size()<= 0) {
//                System.out.printf("NO HAY FACUTRAS DETALLES");
//            }
//            else {
//                System.out.printf("Si hay ");
//            }
            List<FacturaDetalleEntity> facturaDetalles = detalleRepository.findAllbyFacturaId(factura.getId());

//            if (facturaDetalles.size()<= 0) {
//                System.out.printf("NO FUNCIONA ESTA PORQUERIA DE MIERDA ");
//            }
//            else {
//                System.out.printf("Si FUNCIONA  ");
//            }
            for (FacturaDetalleEntity detalle : facturaDetalles ) {
                contentStream.newLineAtOffset(0, -20);
                String detalleTexto = "Producto: " + detalle.getProducto().getNombre() +
                        ", Cantidad: " + detalle.getCantidad() +
                        ", Precio Unitario: " + detalle.getPrecioUnitario() +
                        ", Subtotal: " + detalle.getSubtotal();
                contentStream.showText(detalleTexto);
                total += detalle.getSubtotal();
            }

            contentStream.newLineAtOffset(0, -20);
            contentStream.showText("Total: $" + total);
            contentStream.endText();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return document;
    }

}