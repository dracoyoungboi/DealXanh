package com.dealxanh.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        // Gửi data tới header / mobile-nav fragment
        model.addAttribute("activeNav", "home");
        model.addAttribute("cartItemCount", 3); // Dummy cart items
        model.addAttribute("notifCount", 5);    // Dummy unread notifications

        // Gửi data dummy cho body của home.html để tránh null pointer (nếu view sử dụng)
        model.addAttribute("totalStores", "1,200");
        model.addAttribute("totalUsers", "45K");
        model.addAttribute("totalDeals", "95K");

        // Thymeleaf sẽ tìm file d:\exe201\deal xanh\DealXanh\src\main\resources\templates\buyer\home.html
        return "buyer/home"; 
    }
}
