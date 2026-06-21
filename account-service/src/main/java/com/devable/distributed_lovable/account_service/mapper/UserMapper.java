package com.devable.distributed_lovable.account_service.mapper;

import com.devable.distributed_lovable.account_service.dto.auth.SignupRequest;
import com.devable.distributed_lovable.account_service.dto.auth.UserProfileResponse;
import com.devable.distributed_lovable.account_service.entity.User;
import com.devable.distributed_lovable.common_lib.dto.UserDto;
import com.devable.distributed_lovable.common_lib.security.JwtUserPrincipal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(SignupRequest signupRequest);

    @Mapping(source = "userId", target = "id")
    UserProfileResponse toUserProfileResponse(JwtUserPrincipal user);

    UserDto toUserDto(User user);

}
