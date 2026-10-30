package pe.bioleather.reports;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
@Service
public class PedidoReporteService {
    private final PedidoReporteDAO dao;
    public PedidoReporteService(PedidoReporteDAO dao) { this.dao = dao; }

    public byte[] generarExcel() throws IOException {
        List<Map<String,Object>> datos = dao.listarDetallePedidos();
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Pedidos");
            sheet.createFreezePane(0, 1);
            CellStyle headingStyle = workbook.createCellStyle();
            Font font = workbook.createFont(); font.setBold(true);
            headingStyle.setFont(font);
            CellStyle moneyStyle = workbook.createCellStyle();
            DataFormat fmt = workbook.createDataFormat();
            moneyStyle.setDataFormat(fmt.getFormat("#,##0.00"));
            String[] headers = {"Código", "Estado", "Fecha", "Cliente", "Producto", "Cantidad", "P. unitario (S/)", "Total (S/)"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                var cell = header.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headingStyle);
                sheet.setColumnWidth(i, (i == 3 || i == 4 ? 30 : 19) * 256);
            }
            int index = 1;
            for (Map<String,Object> dato : datos) {
                Row row = sheet.createRow(index++);
                row.createCell(0).setCellValue(String.valueOf(dato.get("codigo")));
                row.createCell(1).setCellValue(String.valueOf(dato.get("estado")));
                row.createCell(2).setCellValue(String.valueOf(dato.get("creado_en")));
                row.createCell(3).setCellValue(String.valueOf(dato.get("cliente")));
                row.createCell(4).setCellValue(String.valueOf(dato.get("producto")));
                row.createCell(5).setCellValue(((Number) dato.get("cantidad")).intValue());
                var unit = row.createCell(6);
                unit.setCellValue(((BigDecimal) dato.get("precio_unitario")).doubleValue());
                unit.setCellStyle(moneyStyle);
                var total = row.createCell(7);
                total.setCellValue(((BigDecimal) dato.get("total")).doubleValue());
                total.setCellStyle(moneyStyle);
            }
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, index - 1, 0, headers.length - 1));
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
