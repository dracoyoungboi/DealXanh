package com.dealxanh.app.controller.admin;

import com.dealxanh.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/api/buyers")
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
public class AdminBuyerApiController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stats")
    public Map<String, Object> getBuyerStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalBuyers = userRepository.countByRoleName("ROLE_USER");
        long activeBuyers = userRepository.countByRoleAndActive(true);
        long inactiveBuyers = userRepository.countByRoleAndActive(false);

        stats.put("totalBuyers", totalBuyers);
        stats.put("activeBuyers", activeBuyers);
        stats.put("inactiveBuyers", inactiveBuyers);

        return stats;
    }
}
