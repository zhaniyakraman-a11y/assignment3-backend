package com.assignment3.project.mappers;

import com.assignment3.project.dto.responses.ProjectResponse;
import com.assignment3.project.entities.Project;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-21T21:40:56+0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class ProjectMapperImpl implements ProjectMapper {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ProjectResponse toDto(Project entity) {
        if ( entity == null ) {
            return null;
        }

        ProjectResponse projectResponse = new ProjectResponse();

        projectResponse.setId( entity.getId() );
        projectResponse.setTitle( entity.getTitle() );
        projectResponse.setDescription( entity.getDescription() );
        projectResponse.setAuthor( userMapper.toDto( entity.getAuthor() ) );
        projectResponse.setCategory( categoryMapper.toDto( entity.getCategory() ) );
        projectResponse.setTargetAmount( entity.getTargetAmount() );
        projectResponse.setCollectedAmount( entity.getCollectedAmount() );
        projectResponse.setVerified( entity.isVerified() );

        projectResponse.setImagePaths( mapImagePaths(entity) );

        return projectResponse;
    }
}
