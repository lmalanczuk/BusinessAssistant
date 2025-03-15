//package com.licencjat.BusinessAssistant.service.impl;
//
//import com.google.cloud.speech.v1.*;
//import com.google.protobuf.ByteString;
//import org.springframework.stereotype.Service;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardCopyOption;
//
//@Service
//public class AudioTranscriptionService {
//
//    private final SpeechClient speechClient;
//
//    public AudioTranscriptionService() throws Exception {
//        this.speechClient = SpeechClient.create();
//    }
//
//    public String transcribeAudio(Path audioFile) throws Exception {
//        byte[] audioData = Files.readAllBytes(audioFile);
//        ByteString audioBytes = ByteString.copyFrom(audioData);
//
//        RecognitionConfig config = RecognitionConfig.newBuilder()
//            .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
//            .setLanguageCode("pl-PL")
//            .setModel("default")
//            .build();
//
//        RecognitionAudio audio = RecognitionAudio.newBuilder()
//            .setContent(audioBytes)
//            .build();
//
//        RecognizeResponse response = speechClient.recognize(config, audio);
//
//        StringBuilder transcription = new StringBuilder();
//        for (SpeechRecognitionResult result : response.getResultsList()) {
//            transcription.append(result.getAlternatives(0).getTranscript())
//                        .append("\n");
//        }
//
//        return transcription.toString();
//    }
//}