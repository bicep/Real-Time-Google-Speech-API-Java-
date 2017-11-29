# Real-time Audio-To-Text Streaming with Google Speech API in Java

---
## Why this project?
On [Google's speech API site](https://cloud.google.com/speech/docs/streaming-recognize) there are a couple of great examples for real-time speech recognition in C#, node.js and Python. There is no example for Java!

I recently had to learn about Rx and grpc for [Say or Die] (https://github.com/payallal/sayordie), and found the lack of documentation for basic things to be frustrating. Hopefully this can help someone who wants to do a simple real-time audio streaming using the Speech API and Java.

---
## Usage
* Use Maven and install the dependencies from the pom.xml file.
* Make sure you set the Global environment variable `GOOGLE_APPLICATION_CREDENTIALS` to the JSON file containing your authentification json from Google.
* See more detailed instructions [here] (https://github.com/payallal/sayordie) for more information.


