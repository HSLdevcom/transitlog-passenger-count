package fi.hsl.transitlog.passengercount.azure;

/**
 * Uploads file to blob on Azure
 */
public class AzureUploadTask {
    private final AzureBlobClient blobClient;
    private final String filePath;

    AzureUploadTask(AzureBlobClient blobClient, String filePath) {
        this.blobClient = blobClient;
        this.filePath = filePath;
    }

    public AzureUploadTask run() {
        //log.info("Uploading dump from filepath: {}", filePath);
        this.blobClient.uploadFromFile(filePath);
        return this;
    }

    public boolean isUploaded() {
        return this.blobClient.fileExists(filePath);
    }
}
