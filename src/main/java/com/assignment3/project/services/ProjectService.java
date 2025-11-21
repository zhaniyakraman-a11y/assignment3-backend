package com.assignment3.project.services;

import com.assignment3.project.dto.requests.ProjectCreateRequest;
import com.assignment3.project.dto.requests.ProjectUpdateRequest;
import com.assignment3.project.dto.responses.ProjectResponse;
import com.assignment3.project.entities.Project;
import com.assignment3.project.entities.Category;
import com.assignment3.project.enums.UserRole;
import com.assignment3.project.repositories.CategoryRepository;
import com.assignment3.project.entities.User;
import com.assignment3.project.mappers.ProjectMapper;
import com.assignment3.project.repositories.ImageRepository;
import com.assignment3.project.repositories.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public List<ProjectResponse> getAllProjects() {
        log.info("ProjectService.getAllProjects called");
        List<Project> projects = projectRepository.findByIsVerifiedTrue();
        return projects.stream().map(projectMapper::toDto).toList();
    }

    public List<ProjectResponse> getAllProjectsForAdmin() {
        log.info("ProjectService.getAllProjectsForAdmin called");
        List<Project> projects = projectRepository.findAll();
        return projects.stream().map(projectMapper::toDto).toList();
    }

    public ProjectResponse getProjectById(Long projectId) {
        log.info("ProjectService.getProjectById called id={}", projectId);
        Project project = projectRepository.findById(projectId).orElseThrow();
        return projectMapper.toDto(project);
    }

    public ProjectResponse createProject(ProjectCreateRequest request) {
        log.info("ProjectService.createProject called authorId={} title={}", request.getAuthorId(), request.getTitle());
        User author = userService.getUserByIdOrThrow(request.getAuthorId());

        if (author.getRole() != UserRole.NEEDS_HELP && author.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("Only users with role NEEDS_HELP or ADMIN can create projects");
        }

        if (author.getRole() == UserRole.NEEDS_HELP && !author.isVerified()) {
            throw new IllegalArgumentException("Only verified users with role NEEDS_HELP can create projects");
        }

        Project project = new Project();
        project.setAuthor(author);
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category with id " + request.getCategoryId() + " not found"));
            project.setCategory(category);
        }
        
        project.setTargetAmount(request.getTargetAmount() != null ? request.getTargetAmount() : 0L);
        project.setCollectedAmount(request.getCollectedAmount() != null ? request.getCollectedAmount() : 0L);
        
        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            var images = new HashSet<>(imageRepository.findAllById(request.getImageIds()));
            images.forEach(img -> img.setProject(project));
            project.setImages(images);
        }

        Project saved = projectRepository.save(project);
        return projectMapper.toDto(saved);
    }
    
    public ProjectResponse updateProject(
            Long projectId,
            ProjectUpdateRequest projectToUpdate
    ) {
        log.info("ProjectService.updateProject called id={} title={} authorId={}", projectId, projectToUpdate.getTitle(), projectToUpdate.getAuthorId());
        var project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project with id " + projectId + " not found"));
        
        User currentUser = userService.getCurrentUser();
        Long projectAuthorId = project.getAuthor().getId();
        Long currentUserId = currentUser.getId();
        boolean isAuthor = projectAuthorId.equals(currentUserId);
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        
        log.info("ProjectService.updateProject access check: projectAuthorId={}, currentUserId={}, isAuthor={}, isAdmin={}", 
                projectAuthorId, currentUserId, isAuthor, isAdmin);
        
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("Only project author or administrator can update the project");
        }
        
        if (!projectAuthorId.equals(projectToUpdate.getAuthorId())) {
            throw new IllegalArgumentException("Project author cannot be changed");
        }
        User author = userService.getUserByIdOrThrow(projectToUpdate.getAuthorId());

        project.setTitle(projectToUpdate.getTitle());
        project.setDescription(projectToUpdate.getDescription());
        project.setAuthor(author);
        
        if (projectToUpdate.getCategoryId() != null) {
            Category category = categoryRepository.findById(projectToUpdate.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category with id " + projectToUpdate.getCategoryId() + " not found"));
            project.setCategory(category);
        }
        
        if (projectToUpdate.getTargetAmount() != null) {
            project.setTargetAmount(projectToUpdate.getTargetAmount());
        }

        if (projectToUpdate.getImageIds() != null) {
            var newImages = new HashSet<>(imageRepository.findAllById(projectToUpdate.getImageIds()));
            var oldImages = new HashSet<>(project.getImages());

            for (var oldImg : oldImages) {
                if (!projectToUpdate.getImageIds().contains(oldImg.getId())) {
                    imageRepository.delete(oldImg);
                }
            }

            for (var img : newImages) {
                img.setProject(project);
            }

            project.setImages(newImages);
        }
        
        return projectMapper.toDto(projectRepository.save(project));
    }

    public void deleteProject(
            Long projectId
    ) {
        log.info("ProjectService.deleteProject called id={}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project with id " + projectId + " not found"));
        
        User currentUser = userService.getCurrentUser();
        Long projectAuthorId = project.getAuthor().getId();
        Long currentUserId = currentUser.getId();
        boolean isAuthor = projectAuthorId.equals(currentUserId);
        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;
        
        log.info("ProjectService.deleteProject access check: projectAuthorId={}, currentUserId={}, isAuthor={}, isAdmin={}", 
                projectAuthorId, currentUserId, isAuthor, isAdmin);
        
        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("Only project author or administrator can delete the project");
        }
        
        projectRepository.deleteById(projectId);
        log.info("Project with id {} deleted", projectId);
    }

    public List<ProjectResponse> filterProjects(Long categoryId, String text) {
        log.info("ProjectService.filterProjects called");
        List<Project> filteredProjects = projectRepository.findProjectsByCategoryAndTitleContaining(
                categoryId,
                text
        );
        return filteredProjects.stream()
                .map(projectMapper::toDto)
                .toList();
    }

    @Transactional
    public ProjectResponse verifyProject(Long projectId) {
        log.info("ProjectService.verifyProject called id={}", projectId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project with id " + projectId + " not found"));
        
        project.setVerified(true);
        Project saved = projectRepository.save(project);
        log.info("Project with id {} verified", projectId);
        return projectMapper.toDto(saved);
    }

}
