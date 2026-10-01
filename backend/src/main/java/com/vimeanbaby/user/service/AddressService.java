package com.vimeanbaby.user.service;

import com.vimeanbaby.exception.BadRequestException;
import com.vimeanbaby.exception.ResourceNotFoundException;
import com.vimeanbaby.user.dto.AccountDtos.AddressRequest;
import com.vimeanbaby.user.dto.AccountDtos.AddressResponse;
import com.vimeanbaby.user.entity.Address;
import com.vimeanbaby.user.mapper.UserMapper;
import com.vimeanbaby.user.repository.AddressRepository;
import com.vimeanbaby.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    static final int MAX_ADDRESSES = 10;

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public List<AddressResponse> list(Long userId) {
        return addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(userId).stream()
                .map(userMapper::toAddressResponse)
                .toList();
    }

    @Transactional
    public AddressResponse create(Long userId, AddressRequest request) {
        long count = addressRepository.countByUserId(userId);
        if (count >= MAX_ADDRESSES) {
            throw new BadRequestException("You can save up to " + MAX_ADDRESSES + " addresses", "ADDRESS_LIMIT");
        }
        Address address = new Address();
        address.setUser(userRepository.getReferenceById(userId));
        apply(address, request);

        boolean makeDefault = request.defaultAddress() || count == 0;
        if (makeDefault) {
            addressRepository.clearDefault(userId);
        }
        address.setDefaultAddress(makeDefault);
        return userMapper.toAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long userId, Long addressId, AddressRequest request) {
        Address address = load(userId, addressId);
        apply(address, request);
        if (request.defaultAddress() && !Boolean.TRUE.equals(address.getDefaultAddress())) {
            addressRepository.clearDefault(userId);
            address.setDefaultAddress(true);
        }
        return userMapper.toAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse setDefault(Long userId, Long addressId) {
        Address address = load(userId, addressId);
        addressRepository.clearDefault(userId);
        address.setDefaultAddress(true);
        return userMapper.toAddressResponse(addressRepository.save(address));
    }

    @Transactional
    public void delete(Long userId, Long addressId) {
        Address address = load(userId, addressId);
        boolean wasDefault = Boolean.TRUE.equals(address.getDefaultAddress());
        addressRepository.delete(address);
        addressRepository.flush();
        if (wasDefault) {
            addressRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
                    .ifPresent(next -> next.setDefaultAddress(true));
        }
    }

    private Address load(Long userId, Long addressId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    }

    private void apply(Address address, AddressRequest request) {
        address.setReceiverName(request.receiverName().trim());
        address.setPhone(AuthService.requireValidPhone(request.phone()));
        address.setProvince(request.province().trim());
        address.setDistrict(request.district().trim());
        address.setCommune(request.commune().trim());
        address.setStreetDetail(request.streetDetail().trim());
        address.setNote(request.note() == null || request.note().isBlank() ? null : request.note().trim());
    }
}
