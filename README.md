# NeelPowerPdfViewerLib

**This is a Kotlin library for downloading and displaying PDFs in a WebView**

This is just a simple pdf library you can display a pdf of any size (small/medium/large) (30 mb/ 80mb/ 120 mb)

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
	        implementation("com.github.sandip4337:NeelPowerPdfViewerLib:v1.0.3")
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
	            PdfViewerActivity.openPdfViewer(this, pdfUrl, pdfName, "123dummy")
	        }
	    }
	}

  # How to use : download the pdf 

	class MainActivity : AppCompatActivity() {

	    private lateinit var binding: ActivityMainBinding
	
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        enableEdgeToEdge()
	        binding = ActivityMainBinding.inflate(layoutInflater)
	        setContentView(binding.root)
	
	        binding.downloadPdfButton1.setOnClickListener {
	            downloadPdf(
	                "https://history.nasa.gov/alsj/a17/A17_FlightPlan.pdf",
	                "FlightPlan_1",
	                binding.progressBar1,
	                binding.progressText1,
	                binding.openPdfButton1,
	                binding.downloadPdfButton1
	            )
	        }
	
	        binding.downloadPdfButton2.setOnClickListener {
	            downloadPdf(
	                "https://history.nasa.gov/alsj/a17/A17_FlightPlan.pdf",
	                "FlightPlan_2",
	                binding.progressBar2,
	                binding.progressText2,
	                binding.openPdfButton2,
	                binding.downloadPdfButton2
	            )
	        }
	
	        binding.openPdfButton1.setOnClickListener {
	            PdfViewerActivity.openPdfViewer(this, "https://history.nasa.gov/alsj/a17/A17_FlightPlan.pdf","FlightPlan_1", "123")
	        }
	
	        binding.openPdfButton2.setOnClickListener{
	            PdfViewerActivity.openPdfViewer(this, "https://history.nasa.gov/alsj/a17/A17_FlightPlan.pdf","FlightPlan_2", "123")
	        }
	    }
	
	    private fun downloadPdf(pdfUrl: String, pdfName: String, progressBar: ProgressBar, progressText: TextView, openPdf: Button, downloadButton: Button) {
	        val pdfDownloader = PdfDownloader(this)
	        progressBar.visibility = View.VISIBLE
	        progressText.visibility = View.VISIBLE
	        progressBar.progress = 0
	        progressText.text = "0%"  // Initial percentage
	
	        pdfDownloader.downloadAndProcessPdf(
	            pdfUrl,
	            pdfName,
	            "123",
	            onProgressUpdate = { progress ->
	                lifecycleScope.launch(Dispatchers.Main) {
	                    progressBar.progress = progress
	                    progressText.text = "$progress%"  // Show percentage
	                }
	            },
	            onDownloadComplete = { success, filePath ->
	                lifecycleScope.launch(Dispatchers.Main) {
	                    progressBar.visibility = View.GONE
	                    progressText.visibility = View.GONE
	                    openPdf.visibility = View.VISIBLE
	                    downloadButton.visibility = View.GONE
	                    if (success) {
	                        Toast.makeText(this@MainActivity, "Download complete: $pdfName", Toast.LENGTH_LONG).show()
	                    } else {
	                        Toast.makeText(this@MainActivity, "Download failed!", Toast.LENGTH_LONG).show()
	                    }
	                }
	            }
	        )
    }
	}
     

 # Here is the video of NeelPowerPdfLibrary : only open the pdf 

 https://github.com/user-attachments/assets/a0f88576-c279-4629-92b4-6ffd04a6adc3

 # Here is the video of NeelPowerPdfLibrary : download the pdf and open the pdf 

https://github.com/user-attachments/assets/17330762-5c69-4185-a8f3-87812391440f




