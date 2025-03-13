# NeelPowerPdfViewerLib

**This is a Kotlin library for downloading and displaying PDFs in a WebView**

This is just a simple pdf library you can display a pdf of any size (small/medium/large) (30 mb/ 80mb/ 120 mb)

Application size will be increase with 3-4 mb

# 🔥 Features

  1. Easy PDF Downloading

  2. Base64 Conversion

  3. WebView Display Support

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
	        implementation("com.github.sandip4337:NeelPowerPdfViewerLib:v1.0.2")
	}

 # How to use 

 class MainActivity : AppCompatActivity() {

    private lateinit var pdfButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        pdfButton = findViewById(R.id.openPdfButton)

        pdfButton.setOnClickListener {

            // arrange the information about the pdf
            val pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"
            val pdfName = "dummy"
	    val pdfId = "123dummy"

            // open the pdf on a new activity in a webview
            PdfViewerActivity.openPdfViewer(this, pdfUrl, pdfName, pdfId)
        }
    }
}

 
