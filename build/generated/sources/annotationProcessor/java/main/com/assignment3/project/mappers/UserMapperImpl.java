package com.assignment3.project.mappers;

import com.assignment3.project.dto.responses.UserResponse;
import com.assignment3.project.entities.User;
import com.assignment3.project.enums.UserRole;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-21T20:21:42+0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserResponse toDto(User entity) {
        if ( entity == null ) {
            return null;
        }

        UserResponse userResponse = new UserResponse();

        userResponse.setId( entity.getId() );
        userResponse.setFullName( entity.getFullName() );
        userResponse.setEmail( entity.getEmail() );
        if ( entity.getRole() != null ) {
            userResponse.setRole( entity.getRole().name() );
        }
        userResponse.setAvatarPath( entity.getAvatarPath() );
        userResponse.setDocPath( entity.getDocPath() );
        userResponse.setVerified( entity.isVerified() );

        return userResponse;
    }

    @Override
    public User toEntity(UserResponse dto) {
        if ( dto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( dto.getId() );
        user.fullName( dto.getFullName() );
        user.email( dto.getEmail() );
        user.avatarPath( dto.getAvatarPath() );
        user.docPath( dto.getDocPath() );
        if ( dto.getRole() != null ) {
            user.role( Enum.valueOf( UserRole.class, dto.getRole() ) );
        }

        return user.build();
    }
}
