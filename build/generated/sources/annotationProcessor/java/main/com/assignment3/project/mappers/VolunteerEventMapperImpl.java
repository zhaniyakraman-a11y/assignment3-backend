package com.assignment3.project.mappers;

import com.assignment3.project.dto.responses.VolunteerEventResponse;
import com.assignment3.project.entities.VolunteerEvent;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-21T21:40:56+0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class VolunteerEventMapperImpl implements VolunteerEventMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public VolunteerEventResponse toDto(VolunteerEvent entity) {
        if ( entity == null ) {
            return null;
        }

        VolunteerEventResponse volunteerEventResponse = new VolunteerEventResponse();

        volunteerEventResponse.setId( entity.getId() );
        volunteerEventResponse.setTitle( entity.getTitle() );
        volunteerEventResponse.setDescription( entity.getDescription() );
        volunteerEventResponse.setEventDate( entity.getEventDate() );
        volunteerEventResponse.setLocation( entity.getLocation() );
        volunteerEventResponse.setOrganizer( userMapper.toDto( entity.getOrganizer() ) );
        volunteerEventResponse.setCreatedAt( entity.getCreatedAt() );
        volunteerEventResponse.setUpdatedAt( entity.getUpdatedAt() );

        volunteerEventResponse.setParticipants( mapParticipants(entity) );

        return volunteerEventResponse;
    }
}
