# NeelPowerPdfViewerLib

**This is a Kotlin library for downloading and displaying PDFs in a WebView**

This is just a simple pdf library you can display or downlad and then display a pdf 

pdf size should be within 50mb 

For the large pdf you can compress the pdf and show it or your pdf can be in the range between 40-50mb

Application size will be increase with 3-4 mb

# step by step guide to add the library

Step 1: Add it in your ***settings.gradle.kts*** at the end of repositories:

	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url = uri("https://jitpack.io") }
		}
	}

 if you use ***gradle*** then Add it in your root ***settings.gradle*** at the end of repositories:

	dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}

 
Step 2:  Add the dependency

	dependencies {
	        implementation("com.github.sandip4337:NeelPowerPdfViewerLib:v1.0.6")
	}

 # 🔥 Features

1. PDF Download & Processing
   
	The PdfDownloader class is responsible for:
	
	✅ Downloading PDFs using Retrofit with efficient background execution via Coroutines.
	
	✅ Displaying real-time download progress through a callback function.
	
	✅ Checking for existing processed files before re-downloading to improve efficiency.
	
	✅ Encoding the downloaded PDF to Base64 to enable embedding in an HTML file.
	
	✅ Generating an HTML file that loads the Base64-encoded PDF using JavaScript-based rendering.
	
	✅ Automatic cleanup – Once processed, the original PDF and temporary files are deleted.

2. PDF Viewing in WebView
   
	The PdfViewerActivity ensures a seamless user experience by:
	
	✅ Loading the HTML file into a WebView to display the PDF.
	
	✅ Ensuring safe file access with restricted external visibility.
	
	✅ Providing smooth navigation and error handling within the WebView.
	
	✅ Showing a progress dialog while the file is being downloaded and processed.
	
	✅ Allowing users to open the viewer with a single function call.

#  Why is This Implementation Beneficial?

🚀 Fast & Efficient – Uses Coroutines for background tasks, reducing UI thread workload.

🔄 Offline Access – Once processed, PDFs can be viewed anytime without re-downloading.

🔐 Secure – Prevents unauthorized access by restricting file permissions.

📜 Lightweight Alternative to PDF Viewers – No need for third-party PDF libraries.

📡 Handles Large PDFs Effectively – Base64 conversion prevents memory overflow.

📊 Download Progress Updates – Users can track file download progress.

 # How to use : only open the pdf 

	 class MainActivity : AppCompatActivity() {
	
	    private lateinit var pdfButton: Button
	
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        enableEdgeToEdge()
	        setContentView(R.layout.activity_main)
	
	        pdfButton = findViewById(R.id.openPdfButton)
	
	        pdfButton.setOnClickListener {
	            val pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
	            val pdfName = "dummy"
	     
	     	    // Pass the context, pdfurl, pdfname, pdfid, pdf_base_url should be must
	            PdfViewerActivity.openPdfViewer(this, pdfUrl, pdfName, "123dummy", https://www.w3.org/)
	        }
	    }
	}

  # How to use : download the pdf 

  1: Initialize WorkManager for PDF Download
	Use WorkManager to enqueue the PDF download task.


	val workRequest = OneTimeWorkRequestBuilder<PdfDownloadWorker>()
	    .setInputData(
	        workDataOf(
	            "PDF_URL" to "https://example.com/sample.pdf",
	            "PDF_NAME" to "SamplePDF",
	            "PDF_ID" to "1234"
	     	    "BASE_URL" to "https://example.com/"
	        )
	    )
	    .build()
	
	WorkManager.getInstance(context).enqueue(workRequest)
 
2: Observe Download Progress
	Monitor progress using WorkManager's getWorkInfoByIdLiveData():

	WorkManager.getInstance(context)
	    .getWorkInfoByIdLiveData(workRequest.id)
	    .observe(this) { workInfo ->
	        if (workInfo != null) {
	            val progress = workInfo.progress.getInt("PROGRESS", 0)
	            Log.d("DownloadProgress", "Progress: $progress%")
	
	            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
	                val htmlFilePath = workInfo.outputData.getString("HTML_FILE_PATH")
	                Log.d("DownloadComplete", "HTML File Path: $htmlFilePath")
	            }
	        }
	    }
3: Load HTML in WebView
	Once the HTML file is ready, display it in a WebView:

	PdfViewerActivity.openPdfViewer(context, pdfUrl, pdfName, pdfId)

 # Here is the video of NeelPowerPdfLibrary : only open the pdf 

 https://github.com/user-attachments/assets/a0f88576-c279-4629-92b4-6ffd04a6adc3

 # Here is the repository of a project with the impementation of the library

 [Repo Link -> https://github.com/sandip4337/TestPDFLibrary](https://github.com/sandip4337/TestPDFLibrary)

 # Here is the video of NeelPowerPdfLibrary : download the pdf and open the pdf 

   Here the pdfs are same because same link with different pdf name , I am saving my pdf to fileDir with different pdf name and id - it is only for demo 

https://github.com/user-attachments/assets/47fc0c15-2b44-455a-b453-f2442bc4ce29


## 📝 License  
This library is licensed under the [MIT License](LICENSE).







