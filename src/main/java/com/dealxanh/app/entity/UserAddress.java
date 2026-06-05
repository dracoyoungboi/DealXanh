package com.dealxanh.app.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "user_addresses")
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String address;
    private String city;
    private String district;
    private String ward;
    private Boolean isDefault = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    public UserAddress() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public Boolean getIsDefault() { return isDefault != null ? isDefault : false; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null && !address.isEmpty()) sb.append(address);
        if (ward != null && !ward.isEmpty()) { if (sb.length() > 0) sb.append(", "); sb.append(ward); }
        if (district != null && !district.isEmpty()) { if (sb.length() > 0) sb.append(", "); sb.append(district); }
        if (city != null && !city.isEmpty()) { if (sb.length() > 0) sb.append(", "); sb.append(city); }
        return sb.length() > 0 ? sb.toString() : null;
    }
}
