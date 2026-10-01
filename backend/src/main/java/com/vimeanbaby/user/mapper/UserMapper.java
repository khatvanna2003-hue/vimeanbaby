package com.vimeanbaby.user.mapper;

import com.vimeanbaby.user.dto.AccountDtos.AddressResponse;
import com.vimeanbaby.user.dto.AdminCustomerDtos.CustomerSummaryResponse;
import com.vimeanbaby.user.dto.UserProfileResponse;
import com.vimeanbaby.user.entity.Address;
import com.vimeanbaby.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse toProfile(User user);

    AddressResponse toAddressResponse(Address address);

    CustomerSummaryResponse toCustomerSummary(User user);
}
