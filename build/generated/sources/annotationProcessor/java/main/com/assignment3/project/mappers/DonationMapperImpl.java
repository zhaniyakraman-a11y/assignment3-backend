package com.assignment3.project.mappers;

import com.assignment3.project.dto.responses.DonationResponse;
import com.assignment3.project.entities.Donation;
import com.assignment3.project.entities.Project;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-21T20:21:42+0500",
    comments = "version: 1.5.5.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.14.3.jar, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class DonationMapperImpl implements DonationMapper {

    @Autowired
    private UserMapper userMapper;

    @Override
    public DonationResponse toDto(Donation entity) {
        if ( entity == null ) {
            return null;
        }

        DonationResponse donationResponse = new DonationResponse();

        donationResponse.setProjectId( entityProjectId( entity ) );
        donationResponse.setId( entity.getId() );
        donationResponse.setAmount( entity.getAmount() );
        donationResponse.setDonor( userMapper.toDto( entity.getDonor() ) );
        donationResponse.setCreatedAt( entity.getCreatedAt() );

        return donationResponse;
    }

    private Long entityProjectId(Donation donation) {
        if ( donation == null ) {
            return null;
        }
        Project project = donation.getProject();
        if ( project == null ) {
            return null;
        }
        Long id = project.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
