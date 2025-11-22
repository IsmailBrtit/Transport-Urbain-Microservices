package com.example.billets_service.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Service
@Slf4j
public class QRCodeService {

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    public String generateQRCode(UUID ticketId) {
        try {
            String qrContent = "TICKET:" + ticketId.toString();

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                qrContent,
                BarcodeFormat.QR_CODE,
                QR_CODE_WIDTH,
                QR_CODE_HEIGHT
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            byte[] qrCodeBytes = outputStream.toByteArray();
            String base64QRCode = Base64.getEncoder().encodeToString(qrCodeBytes);

            log.info("QR Code généré pour ticket: {}", ticketId);
            return "data:image/png;base64," + base64QRCode;

        } catch (WriterException | IOException e) {
            log.error("Erreur lors de la génération du QR code: {}", e.getMessage());
            throw new RuntimeException("Échec de génération du QR code", e);
        }
    }

    public boolean isValidQRCode(String qrCode) {
        return qrCode != null && qrCode.startsWith("data:image/png;base64,");
    }
}
