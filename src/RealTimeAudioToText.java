/*
  Copyright 2017, Google Inc.
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
      http://www.apache.org/licenses/LICENSE-2.0
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/


// [START speech_quickstart]
// Imports the Google Cloud client library
import com.google.api.gax.rpc.ApiStreamObserver;
import com.google.api.gax.rpc.BidiStreamingCallable;
import com.google.cloud.speech.v1.RecognitionConfig;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.StreamingRecognitionConfig;
import com.google.cloud.speech.v1.StreamingRecognizeRequest;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;

import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class RealTimeAudioToText {
	
	private ArrayList<RequestThread> reqtArray = new ArrayList<RequestThread>();
	private boolean stopCapture = false;

	
	public AudioFormat getAudioFormat() {
	      float sampleRate = 16000;
	      int sampleSizeInBits = 16;
	      int channels = 1;
	      boolean signed = true;
	      boolean bigEndian = true;
	      return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,
	                bigEndian);
	}
	
	public void streamingRecognize(int seconds) throws Exception, IOException {
		
		  // Instantiates a client with GOOGLE_APPLICATION_CREDENTIALS
		  SpeechClient speech = SpeechClient.create();

		  // Configure request with local raw PCM audio
		  RecognitionConfig recConfig = RecognitionConfig.newBuilder()
		      .setEncoding(AudioEncoding.LINEAR16)
		      .setLanguageCode("en-US")
		      .setSampleRateHertz(16000)
		      .build();
		  StreamingRecognitionConfig config = StreamingRecognitionConfig.newBuilder()
		      .setConfig(recConfig)
		      .build();


		  //instantiate our response observer custom class
		  ResponseApiStreamingObserver<StreamingRecognizeResponse> responseObserver =
		      new ResponseApiStreamingObserver<StreamingRecognizeResponse>();

		  //bidirectional callable
		  BidiStreamingCallable<StreamingRecognizeRequest,StreamingRecognizeResponse> callable =
		      speech.streamingRecognizeCallable();

		  //Connect the request observer with the response observer
		  ApiStreamObserver<StreamingRecognizeRequest> requestObserver =
		      callable.bidiStreamingCall(responseObserver);

		  // The first request must **only** contain the audio configuration:
		  requestObserver.onNext(StreamingRecognizeRequest.newBuilder()
		      .setStreamingConfig(config)
		      .build());
		  
		  //get audio format
		  AudioFormat format = this.getAudioFormat();
		  
		  //have the byte buffer created
		  byte tempBuffer[] = new byte[16000]; 
		  
		  //targetdataline helps us read the data into a buffer
	      TargetDataLine targetDataLine;
	      try {
	    	  	targetDataLine = AudioSystem.getTargetDataLine(format);
	    	  	targetDataLine.open();
	    	  	//don't forget to start the microphone
	    	  	targetDataLine.start();
	    	  	//Speak when you see the microphone started printed!
	    	  	System.out.println("microphone start");
	      } catch (LineUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;	
	      }
	      
		  
		  //call sleep thread (how long specified by sleep parameter)
		  AudioSleepThread ast = new AudioSleepThread(this, seconds);
		  ast.start();
		  
		  while(targetDataLine!=null) {
			//this method blocks
			int cnt = targetDataLine.read(tempBuffer, 0, tempBuffer.length);
			if (cnt>0) {
				  // Subsequent requests must **only** contain the audio data.
				  RequestThread reqt = new RequestThread(requestObserver, tempBuffer);
				  reqt.start();
				  //Add it to array list
				  this.reqtArray.add(reqt);
				  responseObserver.onCompleted();
			}
			//thread will set this to stop
			if (this.getStopCapture()) {break;}
		  }
		  
		  //Make sure that each request thread is finished before moving on
		  for (RequestThread reqt : this.reqtArray) {
			  reqt.join();
		  }
		  
		  requestObserver.onCompleted();
		  		  
	      if (targetDataLine != null) {
	          targetDataLine.drain();
	          targetDataLine.close();
	      }
 
		  speech.close();
	}
	
	public synchronized boolean getStopCapture() {
		return this.stopCapture;
	}
	public synchronized void setStopCapture(boolean b) {
		this.stopCapture = b;
	}
	
	public static void main(String... args) throws Exception {
		RealTimeAudioToText rtatt = new RealTimeAudioToText();
		rtatt.streamingRecognize(40000);
	}
}


