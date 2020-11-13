package fi.hsl.passengercount.azure;


/**
 * Uploads file to a blob
 */

public
class AzureUploader {
    final AzureBlobClient azureBlobClient;

    public AzureUploader(AzureBlobClient azureBlobClient) {
        this.azureBlobClient = azureBlobClient;
    }

    public AzureUploadTask uploadBlob(String filePath) {
        //Register as task for the asynchronous uploader
        return new AzureUploadTask(azureBlobClient, filePath).run();
    }


}
