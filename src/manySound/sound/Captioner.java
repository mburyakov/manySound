package manySound.sound;

import manySound.sound.Player;

import javax.sound.sampled.*;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: misha
 * Date: 07.10.12
 * Time: 21:05
 * To change this template use File | Settings | File Templates.
 */
public class Captioner {
    public static void main(String[] args) throws LineUnavailableException, IOException, InterruptedException {
        final AudioFormat audioFormat = getAudioFormat();
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        final TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        final AudioFileFormat.Type fileType = AudioFileFormat.Type.AU;
        final PipedOutputStream pos = new PipedOutputStream();
        final PipedInputStream pis = new PipedInputStream(pos);
        final BufferedInputStream bis = new BufferedInputStream(pis);
        final FileOutputStream fos = new FileOutputStream("cap.au");
        new Thread(new Runnable() {
            public void run() {
                try {
                    startCaptureAudio(targetDataLine, audioFormat, fileType, pos);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (LineUnavailableException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }).start();
        Thread.sleep(1000);
        new Thread(new Runnable() {
            public void run() {
                try {
                    Player.playStream(bis);
                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (LineUnavailableException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }).start();
        //stopCaptureAudio(targetDataLine);
    }

    public static void startCaptureAudio(TargetDataLine targetDataLine, AudioFormat audioFormat, AudioFileFormat.Type fileType, OutputStream outputFile) throws IOException, LineUnavailableException {
        //AudioFormat audioFormat = getAudioFormat();
        //DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
        //TargetDataLine targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
        //AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
        targetDataLine.open(audioFormat);
        targetDataLine.start();
        AudioSystem.write(new AudioInputStream(targetDataLine), fileType, outputFile);
    }

    public static void stopCaptureAudio(TargetDataLine targetDataLine) {
        targetDataLine.stop();
    }

    public static AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(sampleRate,
                sampleSizeInBits,
                channels,
                signed,
                bigEndian);
    }

}
