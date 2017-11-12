import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.cloud.speech.v1.SpeechRecognitionAlternative;
import com.google.cloud.speech.v1.StreamingRecognizeResponse;

public class ResponseThread extends Thread {
	
	  private ResponseApiStreamingObserver<StreamingRecognizeResponse> responseObserver;
	  
	  public ResponseThread(ResponseApiStreamingObserver<StreamingRecognizeResponse> responseObserver) {
		  this.responseObserver = responseObserver;
	  }
	  
	  @Override 
	  public void run() {
		  
		List<StreamingRecognizeResponse> responses;
		try {
			//now this is blocking
			responses = responseObserver.future().get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		for (StreamingRecognizeResponse response: responses) {
		    // For streaming recognize, the results list has one is_final result (if available) followed
		    // by a number of in-progress results (if iterim_results is true) for subsequent utterances.
		    // Just print the first result here.
		    com.google.cloud.speech.v1.StreamingRecognitionResult result = response.getResultsList().get(0);
		    // There can be several alternative transcripts for a given chunk of speech. Just use the
		    // first (most likely) one here.
		    SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
		    System.out.println(alternative.getTranscript());
		  }
	  }
}
