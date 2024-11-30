package com.foolish.movieservice.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.foolish.movieservice.model.Movie;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class AzureBlobService {
  private final Environment env;
  private final MovieService movieService;

  private String generateUniqueBlobName(String original) {
    String extension = "";
    StringBuilder fileName = new StringBuilder();
    int dotIndex = original.lastIndexOf(".");
    if (dotIndex != -1) {
      extension = original.substring(dotIndex);
      fileName.append(original, 0, dotIndex);
    }

    String combine = fileName + "_" + UUID.randomUUID() + extension;
    Movie movie = movieService.findMovieByPoster(combine);
    if (movie != null) return generateUniqueBlobName(original);
    return combine;
  }

  public String readBlobFile(String filename) {
    // Chức năng thực hiện đọc một Blob file có sẵn và trả về public URL tới Blob file đó.
    BlobClient blobClient = new BlobClientBuilder()
            .connectionString(env.getProperty("AZURE_STORAGE_CONNECTION_STRING"))
            .containerName("posters")
            .blobName(filename)
            .buildClient();

    if (!blobClient.exists()) {
      return null;
    }
    return blobClient.getBlobUrl();
  }

  public String writeBlobFile(MultipartFile file) {
    String fileName = !file.getOriginalFilename().isEmpty() ? file.getOriginalFilename() : file.getName();
    BlobClient blobClient = new BlobClientBuilder()
            .connectionString(env.getProperty("AZURE_STORAGE_CONNECTION_STRING"))
            .containerName("posters")
            .blobName(generateUniqueBlobName(fileName))
            .buildClient();
    try {
      blobClient.upload(file.getInputStream(), file.getBytes().length, false);
    } catch (IOException e) {
      log.error(e.getMessage(), e);
      return null;
    }
    return blobClient.getBlobUrl();
  }

  public boolean deleteBlobFile(String filename) {
    BlobClient blobClient = new BlobClientBuilder()
            .connectionString(env.getProperty("AZURE_STORAGE_CONNECTION_STRING"))
            .containerName("posters")
            .blobName(filename)
            .buildClient();
    return blobClient.deleteIfExists();
  }
}
