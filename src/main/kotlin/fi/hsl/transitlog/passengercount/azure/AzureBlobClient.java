package fi.hsl.transitlog.passengercount.azure;

import com.azure.storage.blob.*;

/**
 * Azure blob client definition
 */
public class AzureBlobClient {
    BlobContainerClient blobContainerClient;

    public AzureBlobClient(String connectionString, String blobContainer) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        if (blobServiceClient.getBlobContainerClient(blobContainer).exists())
            this.blobContainerClient = blobServiceClient.getBlobContainerClient(blobContainer);
        else
            this.blobContainerClient = blobServiceClient.createBlobContainer(blobContainer);

    }

    void uploadFromFile(String filePath) {
        BlobClient blobClient = blobContainerClient.getBlobClient(filePath);
        blobClient.uploadFromFile(filePath, true);

    }

    public boolean fileExists(String filePath) {
        blobContainerClient.getBlobClient(filePath);
        return blobContainerClient.exists();
    }
}

