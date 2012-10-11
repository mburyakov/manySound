package manySound.sound;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: mburyakov
 * Date: 07.10.12
 * Time: 21:58
 * To change this template use File | Settings | File Templates.
 */
public class Player {

    public static void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        playStream(new BufferedInputStream(new FileInputStream("cap.au")));
    }


    public static void playStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        /*AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
        AudioFormat audioFormat = audioInputStream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);

        SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        sourceDataLine.open(audioFormat);
        sourceDataLine.start();

        int numRead = 0;
        byte[] buf = new byte[sourceDataLine.getBufferSize()];
        while ((numRead = audioInputStream.read(buf, 0, buf.length)) >= 0) {
            int offset = 0;
            while (offset < numRead) {
                offset += sourceDataLine.write(buf, offset, numRead-offset);
            }
        }
        sourceDataLine.drain();
        sourceDataLine.stop();*/
        Clip clip = AudioSystem.getClip();
        clip.open(AudioSystem.getAudioInputStream(inputStream));
        clip.start();

    }

}
