tatic async Task<object> StreamingMicRecognizeAsync(int seconds)
{
    //check if there is a microphone	
    if (NAudio.Wave.WaveIn.DeviceCount < 1)
    {
        Console.WriteLine("No microphone!");
        return -1;
    }
    var speech = SpeechClient.Create();
    var streamingCall = speech.StreamingRecognize();
    
    // Write the initial request with the config- it is async and await ensures non-blocking
    // and sequential
    await streamingCall.WriteAsync(
        new StreamingRecognizeRequest()
        {
            StreamingConfig = new StreamingRecognitionConfig()
            {
                Config = new RecognitionConfig()
                {
                    Encoding =
                    RecognitionConfig.Types.AudioEncoding.Linear16,
                    SampleRateHertz = 16000,
                    LanguageCode = "en",
                },
                InterimResults = true,
            }
        });

    // Print responses as they arrive. We start the task, and we await for it to be finished later.
    // This is a while loop, where we keep getting repsonses form the repsonse stream
    Task printResponses = Task.Run(async () =>
    {
        while (await streamingCall.ResponseStream.MoveNext(
            default(CancellationToken)))
        {
            foreach (var result in streamingCall.ResponseStream
                .Current.Results)
            {
                foreach (var alternative in result.Alternatives)
                {
                    Console.WriteLine(alternative.Transcript);
                }
            }
        }
    });

    // Read from the microphone and stream to API.
    object writeLock = new object();
    bool writeMore = true;
    var waveIn = new NAudio.Wave.WaveInEvent();
    waveIn.DeviceNumber = 0;
    waveIn.WaveFormat = new NAudio.Wave.WaveFormat(16000, 1);
    waveIn.DataAvailable +=
        (object sender, NAudio.Wave.WaveInEventArgs args) =>
        {
            lock (writeLock)
            {
                if (!writeMore) return;
                streamingCall.WriteAsync(
                    new StreamingRecognizeRequest()
                    {
                        AudioContent = Google.Protobuf.ByteString
                            .CopyFrom(args.Buffer, 0, args.BytesRecorded)
                    }).Wait();
                //wait is a synchronization method that causes the calling thread to wait until the current task has completed.
            }
        };
    waveIn.StartRecording();
    Console.WriteLine("Speak now.");
    await Task.Delay(TimeSpan.FromSeconds(seconds));
    // Stop recording and shut down.
    waveIn.StopRecording();
    lock (writeLock) writeMore = false;
    await streamingCall.WriteCompleteAsync();
    await printResponses;
    return 0;
}
