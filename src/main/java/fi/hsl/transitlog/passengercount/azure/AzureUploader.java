package fi.hsl.transitlog.passengercount.azure;


import java.io.File;

/**
 * Uploads file to a blob
 */

public class AzureUploader {


    private final AzureBlobClient azureBlobClient;

    public AzureUploader(AzureBlobClient azureBlobClient) {
        this.azureBlobClient = azureBlobClient;
    }

    public AzureUploadTask uploadBlob(File file) {
        //Register as task for the asynchronous uploader
        return new AzureUploadTask(azureBlobClient, file).run();
    }


}
