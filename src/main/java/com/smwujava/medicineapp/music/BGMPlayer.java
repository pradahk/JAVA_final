package com.smwujava.medicineapp.music;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class BGMPlayer extends Thread {
    private String resourcePath;
    private Clip clip;
    private volatile boolean isRunning = true; // 스레드 실행 상태를 제어하기 위한 플래그

    public BGMPlayer(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public void run() {
        try {
            URL audioURL = getClass().getResource(resourcePath);
            if (audioURL == null) {
                System.err.println("BGMPlayer: [오류] 리소스를 찾지 못했습니다: " + resourcePath);
                return;
            }

            try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioURL)) {
                clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, audioStream.getFormat()));
                clip.open(audioStream);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();

                while (isRunning) {
                    Thread.sleep(100);
                }
            }
        } catch (UnsupportedAudioFileException e) {
            System.err.println("BGMPlayer: 지원하지 않는 오디오 파일 형식입니다: " + resourcePath);
        } catch (IOException e) {
            System.err.println("BGMPlayer: 오디오 파일 입출력 오류: " + resourcePath);
        } catch (LineUnavailableException e) {
            System.err.println("BGMPlayer: 오디오 라인을 사용할 수 없습니다. 사운드 장치를 확인해주세요.");
        } catch (InterruptedException e) {
            System.out.println("BGMPlayer: 스레드가 중지되었습니다.");
            Thread.currentThread().interrupt();
        } finally {
            if (clip != null) {
                clip.stop();
                clip.close();
            }
            System.out.println("BGMPlayer: 스레드 및 오디오 리소스가 완전히 종료되었습니다.");
        }
    }
}