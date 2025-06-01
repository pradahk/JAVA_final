package com.smwujava.medicineapp.music;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.io.FileNotFoundException;

public class BGMPlayer extends Thread {
    private String resourcePath;
    private Clip clip;

    public BGMPlayer(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public void run() {
        AudioInputStream audioStream = null;
        try {
            URL audioURL = getClass().getResource(resourcePath);
            if (audioURL == null) {
                throw new FileNotFoundException("Resource not found in classpath: " + resourcePath);
            }
            audioStream = AudioSystem.getAudioInputStream(audioURL);

            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("Error: Audio line for this format is not supported for resource: " + resourcePath);
                return;
            }

            clip = (Clip) AudioSystem.getLine(info);
            clip.open(audioStream);

            clip.loop(Clip.LOOP_CONTINUOUSLY); // 루프 재생 시작
            clip.start(); // 음악 재생 시작

            while (clip.isRunning()) {
                Thread.sleep(100);
            }

        } catch (FileNotFoundException e) {
            System.err.println("BGM Loading Error: " + e.getMessage());
        } catch (UnsupportedAudioFileException e) {
            System.err.println("BGM Format Error: Unsupported audio file format for " + resourcePath + ". Please use WAV format.");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("BGM IO Error: Problem reading audio resource " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("BGM Audio Line Error: Audio line not available. Check sound device or if another app is using it.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("BGMPlayer thread was interrupted during playback for " + resourcePath + ".");
            Thread.currentThread().interrupt();
        } finally {
            if (clip != null) {
                clip.close();
            }
            if (audioStream != null) {
                try {
                    audioStream.close();
                } catch (IOException e) {
                    System.err.println("Error closing audio stream: " + e.getMessage());
                }
            }
            System.out.println("BGMPlayer thread finished or stopped for " + resourcePath);
        }
    }

    public void stopBGM() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
            this.interrupt();
            System.out.println("BGM stopped for " + resourcePath);
        }
    }
}