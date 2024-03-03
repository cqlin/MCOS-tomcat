package org.remchurch.mealservice.util;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeUtil {
	public static Path generateQRwithLogo(
			String DIR,// "/directory/to/save/images";
			String ext,//".png";
			String LOGO,//"logo_url";
			String CONTENT,//"some content here";
			int WIDTH,//300;
			int HEIGHT, //300;
			float transparency,
			Colors color
			) {
		// Create new configuration that specifies the error correction
		Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

		QRCodeWriter writer = new QRCodeWriter();
		BitMatrix bitMatrix = null;
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		try {
			// Create a qr code with the url as content and a size of WxH px
			bitMatrix = writer.encode(CONTENT, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);

			// Load QR image
			BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, new MatrixToImageConfig(
					color.getArgb(), //foreground
					//Colors.DARKBLUE.getArgb(), //foreground
					Colors.WHITE.getArgb())); //background

			// Load logo image
			BufferedImage overly = getOverly(LOGO);

			// Calculate the delta height and width between QR code and logo
			int deltaHeight = qrImage.getHeight() - overly.getHeight();
			int deltaWidth = qrImage.getWidth() - overly.getWidth();

			// Initialize combined image
			BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = (Graphics2D) combined.getGraphics();

			// Write QR code to new image at position 0/0
			g.drawImage(qrImage, 0, 0, null);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));

			// Write logo into combine image at position (deltaWidth / 2) and
			// (deltaHeight / 2). Background: Left/Right and Top/Bottom must be
			// the same space for the logo to be centered
			g.drawImage(overly, (int) Math.round(deltaWidth / 2), (int) Math.round(deltaHeight / 2), null);

			// Write combined image as PNG to OutputStream
			ImageIO.write(combined, "png", os);
			// Store Image
			Path path = Paths.get(DIR + CONTENT +ext);
			Files.copy( new ByteArrayInputStream(os.toByteArray()), path, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("QR path:"+path);
			return path;
		} catch (WriterException e) {
			e.printStackTrace();
			//LOG.error("WriterException occured", e);
		} catch (IOException e) {
			e.printStackTrace();
			//LOG.error("IOException occured", e);
		}
		return null;
	}

	private static BufferedImage getOverly(String LOGO) throws IOException {
		URL url = new URL(LOGO);
		return ImageIO.read(url);
	}

	public static enum Colors {

		BLUE(0xFF0060D0),
		DARKBLUE(0xFF005A80),
		GREEN(0xFF00A030),
		RED(0xFFE91C43),
		INDIGO(0xFF600082),
		PURPLE(0xFF8A4F9E),
		ORANGE(0xFFEF5000),
		WHITE(0xFFFFFFFF),
		BLACK(0xFF000000);

		private final int argb;

		Colors(final int argb){
			this.argb = argb;
		}

		public int getArgb(){
			return argb;
		}
	}


}
