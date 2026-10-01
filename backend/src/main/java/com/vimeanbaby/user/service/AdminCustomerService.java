package com.vimeanbaby.user.service;

import com.vimeanbaby.common.PageResponse;
import com.vimeanbaby.exception.ResourceNotFoundException;
import com.vimeanbaby.user.dto.AdminCustomerDtos.CustomerDetailResponse;
import com.vimeanbaby.user.dto.AdminCustomerDtos.CustomerSummaryResponse;
import com.vimeanbaby.user.entity.Role;
import com.vimeanbaby.user.entity.User;
import com.vimeanbaby.user.mapper.UserMapper;
import com.vimeanbaby.user.repository.AddressRepository;
import com.vimeanbaby.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCustomerService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final UserMapper userMapper;

    public PageResponse<CustomerSummaryResponse> list(String q, String status, int page, int size) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("role"), Role.CUSTOMER));
            if (StringUtils.hasText(q)) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                String phoneLike = "%" + q.replaceAll("[^0-9]", "") + "%";
                List<Predicate> search = new ArrayList<>();
                search.add(cb.like(cb.lower(root.get("fullName")), like));
                search.add(cb.like(cb.lower(root.get("email")), like));
                if (phoneLike.length() > 2) {
                    search.add(cb.like(root.get("phone"), phoneLike));
                }
                predicates.add(cb.or(search.toArray(Predicate[]::new)));
            }
            if ("active".equals(status)) {
                predicates.add(cb.isTrue(root.get("active")));
            } else if ("disabled".equals(status)) {
                predicates.add(cb.isFalse(root.get("active")));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        Page<User> result = userRepository.findAll(spec, pageable);
        return new PageResponse<>(
                result.getContent().stream().map(userMapper::toCustomerSummary).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public CustomerDetailResponse get(Long id) {
        User user = load(id);
        var addresses = addressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(id).stream()
                .map(userMapper::toAddressResponse)
                .toList();
        return new CustomerDetailResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                Boolean.TRUE.equals(user.getActive()),
                user.getDateOfBirth(),
                user.getGender(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                addresses
        );
    }

    /** Disabling a customer also invalidates every token they hold. */
    @Transactional
    public CustomerDetailResponse setActive(Long id, boolean active) {
        User user = load(id);
        if (Boolean.TRUE.equals(user.getActive()) != active) {
            user.setActive(active);
            if (!active) {
                user.setTokenVersion(user.getTokenVersion() + 1);
            }
        }
        return get(id);
    }

    private User load(Long id) {
        return userRepository.findByIdAndRole(id, Role.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }
}
