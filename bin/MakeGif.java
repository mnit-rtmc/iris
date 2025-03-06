import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MakeGif {
	static public void main(String[] args) throws Exception {
		int width = 256;
		int height = 256;
		BufferedImage buffer = new BufferedImage(width, height,
			BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int rgb = (y << 16) | (x << 8);
				buffer.setRGB(x, y, rgb);
			}
		}
		ImageIO.write(buffer, "gif", new File("test.gif"));
	}
}
