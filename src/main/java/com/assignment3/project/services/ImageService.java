package com.assignment3.project.services;

import com.assignment3.project.entities.Image;
import com.assignment3.project.repositories.ImageRepository;
import com.assignment3.project.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {
    private final FileStorageService fileStorageService;
    private final ImageRepository imageRepository;
    private final ProjectRepository projectRepository;

    public Map<String, Object> uploadImage(MultipartFile file, Long projectId) throws IOException {
        log.info("ImageService.uploadImage called projectId={} originalFile={}", projectId, file != null ? file.getOriginalFilename() : null);
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        String folderName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String relativePath = fileStorageService.saveProjectImage(folderName, file);
        var project = projectRepository.findById(projectId).orElseThrow();
        Image image = Image.builder()
                .relativePath(relativePath)
                .project(project)
                .build();
        Image saved = imageRepository.save(image);
        log.info("Uploaded image id={}, path={}, projectId={}", saved.getId(), saved.getRelativePath(), projectId);
        return Map.of(
                "id", saved.getId(),
                "relativePath", saved.getRelativePath()
        );
    }
}
