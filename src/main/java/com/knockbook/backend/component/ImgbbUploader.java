package com.knockbook.backend.component;

import com.knockbook.backend.config.ImgbbApiProperties;
import com.knockbook.backend.dto.ImgbbUploadResponse;
import com.knockbook.backend.exception.EmptyFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class ImgbbUploader {

    @Qualifier("imgbbWebClient")
    private final WebClient webClient;
    private final ImgbbApiProperties props;

//    upload file to ImgBB and return public URL
//    throw runtime exception if failed
    public String upload(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            final var name = file != null ? file.getOriginalFilename() : null;
            throw new EmptyFileException(name != null ? name : "unknown");
        }

        final var safeName = sanitizeFileName(file.getOriginalFilename());
        final var form = new LinkedMultiValueMap<String, Object>();
        form.add("image", new ByteArrayResource(toBytes(file)) {
            @Override public String getFilename() { return safeName; }
        });
        form.add("name", safeName);

        try {
            final var res = webClient.post()
                    .uri(u -> u.path(props.getUploadPath())
                            .queryParam("key", props.getKey())
                            .build())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(form))
                    .retrieve()
                    .bodyToMono(ImgbbUploadResponse.class)
                    .block();

            if (res == null || !Boolean.TRUE.equals(res.getSuccess())
                    || res.getData() == null || res.getData().getUrl() == null) {
                final var err = (res != null && res.getError() != null)
                        ? res.getError().getMessage() : "unknown";
                throw new RuntimeException("ImgBB upload failed: " + err);
            }
            return res.getData().getUrl();

        } catch (WebClientResponseException e) {
            throw new RuntimeException("ImgBB http error: " + e.getRawStatusCode()
                    + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("ImgBB upload error: " + e.getMessage(), e);
        }
    }

    private static byte[] toBytes(final MultipartFile file) {
        try { return file.getBytes();
        } catch (Exception e) {
            throw new RuntimeException("failed to read file bytes", e);
        }
    }

    private static String sanitizeFileName(final String name) {
        final var validName = (name == null || name.isBlank()) ? "file" : name;
        return validName.replaceAll("[^\\w.\\-]", "_");
    }
}
